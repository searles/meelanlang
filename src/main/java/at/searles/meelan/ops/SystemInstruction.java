package at.searles.meelan.ops;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.optree.Call;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class SystemInstruction extends TypedInstruction {

    protected enum Kind { Bool, Unit, Expr }

    private List<ArrayList<SystemType>> systemTypes;

    private Kind kind = null;

    protected SystemInstruction(List<ArrayList<SystemType>> systemTypes) {
        this.systemTypes = systemTypes;
        this.kind = Kind.Unit;
    }

    /**
     * @param types Types for this system instruction.
     */
    protected SystemInstruction(FunctionType... types) {
        super(types);
        systemTypes = new LinkedList<>();

        for(FunctionType type : types) {
            LinkedList<ArrayList<SystemType>> localSystemType = localSystemType(type);

            systemTypes.addAll(localSystemType);
        }
    }

    protected SystemInstruction(FunctionType[] types, LinkedList<ArrayList<SystemType>> systemTypes, Kind kind) {
        super(types);
        this.systemTypes = systemTypes;
        this.kind = kind;
    }

    private LinkedList<ArrayList<SystemType>> localSystemType(FunctionType type) {
        LinkedList<ArrayList<SystemType>> localSystemType = new LinkedList<>();

        if(type.returnType() == BaseType.bool) {
            if(kind == null) {
                kind = Kind.Bool;
            }

            if(kind != Kind.Bool) {
                throw new IllegalArgumentException();
            }
        } else if(type.returnType() == BaseType.unit) {
            if(kind == null) {
                kind = Kind.Unit;
            }

            if(kind != Kind.Unit) {
                throw new IllegalArgumentException();
            }
        } else if(type.returnType() == BaseType.integer || type.returnType() == BaseType.real ||
                type.returnType() == BaseType.cplx || type.returnType() == BaseType.quat) {
            if(kind == null) {
                kind = Kind.Expr;
            }

            if(kind != Kind.Expr) {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }

        type.addSystemTypes(localSystemType);
        return localSystemType;
    }

    public Value linearizeExpr(List<Tree> exprArgs, Reg target, BaseType targetType, Executable program) throws MeelanException {
        List<Value> args = linearizeValues(exprArgs, program.inner());

        if(target == null) {
            target = program.createRegister(targetType);
        }

        args.add(target);

        Call call = Call.createCall(this, args);

        if(call == null) {
            throw new MeelanException("not an expr", this);
        }

        program.add(call);

        return target;
    }

    public void linearizeBool(List<Tree> exprArgs, Label trueLabel, Label falseLabel, Executable program) throws MeelanException {

        List<Value> args = linearizeValues(exprArgs, program);

        args.add(trueLabel);
        args.add(falseLabel);

        Call call = Call.createCall(this, args);

        if(call == null) {
            throw new MeelanException("not an expr", this);
        }

        program.add(call);
    }

    public void linearizeStmt(List<Tree> exprArgs, Executable program) throws MeelanException {
        List<Value> args = linearizeValues(exprArgs, program);
        Call call = Call.createCall(this, args);

        if(call == null) {
            throw new MeelanException("not an expr", this);
        }

        program.add(call);
    }

    // System types are used in assembler.
    public int findMatchingSystemTypeIndex(List<Value> args) {
        for(int i = 0; i < systemTypes.size(); ++i) {
            ArrayList<SystemType> systemType = systemTypes.get(i);
            if(matches(systemType, args)) {
                return i;
            }
        }

        return -1;
    }

    private boolean matches(ArrayList<SystemType> systemType, List<Value> args) {
        if(args.size() != systemType.size()) {
            return false;
        }

        for (int k = 0; k < args.size(); ++k) {
            if(systemType.get(k) != args.get(k).systemType()) {
                return false;
            }
        }

        return true;
    }

    /**
     * By default, the name of the c function is the same as the class name.
     * To change this behavior, this method should be overridden.
     */
    protected String getFunctionName(ArrayList<SystemType> signature) {
        return getClass().getSimpleName().toLowerCase();
    }

    String vmCases(int instructionOffset) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < systemTypes.size(); ++i) {
            ArrayList<SystemType> signature = systemTypes.get(i);

            sb.append(String.format("            case %3d: // %s%s\n", i + instructionOffset, getClass().getSimpleName(), signature.toString()));
            ArrayList<String> accessArgs = new ArrayList<>(systemTypes.size());
            int offset = 1; // size of instruction part.

            for (SystemType type : signature) {
                accessArgs.add(type.vmAccessCode(offset));
                offset += type.size();
            }

            sb.append("                ");
            sb.append(vmCode(accessArgs, offset, signature)).append("\n");
            sb.append("                break;\n");
        }

        return sb.toString();
    }

    private static <A> String csl(ArrayList<A> l, int count) {
        // Create comma-separated list.
        StringBuilder csl = new StringBuilder();

        for(int i = 0; i < count; ++i) {
            if(i > 0) {
                csl.append(", ");
            }

            csl.append(l.get(i));
        }

        return csl.toString();
    }

    /**
     * Creates code for the renderscript engine using the given arguments.
     *
     * @param accessArgs The code to get the values of the parameters.
     * @return the source.
     */
    private String vmCode(ArrayList<String> accessArgs, int size, ArrayList<SystemType> signature) {

        if(kind == Kind.Expr) {
            // result = className(args); pc += size
            return String.format("%s = %s; pc += %d;", accessArgs.get(accessArgs.size() - 1),
                    call(accessArgs, size, signature), size);
        }

        if(kind == Kind.Bool) {
            return String.format("pc = %s ? %s : %s;",
                    call(accessArgs, size, signature),
                    accessArgs.get(accessArgs.size() - 2), accessArgs.get(accessArgs.size() - 1));
        }

        // eg jumps. They know best.
        return call(accessArgs, size, signature);
    }

    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        if(kind == Kind.Expr) {
            return String.format("%s(%s)", getFunctionName(signature), csl(arguments, arguments.size() - 1));
        }

        if(kind == Kind.Bool) {
            return String.format("%s(%s)", getFunctionName(signature), csl(arguments, arguments.size() - 2));
        }

        throw new UnsupportedOperationException("unit functions must implement call: " + getClass());
    }

    int systemTypeCount() {
        return systemTypes.size();
    }
}