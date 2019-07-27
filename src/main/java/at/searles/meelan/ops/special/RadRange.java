package at.searles.meelan.ops.special;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.ops.sys.Jump;
import at.searles.meelan.optree.Call;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Complex all-in-one instruction.
 * It is a boolean with many arguments
 * z, zlast, v1, v2, code if |z| > v1, code if |z - zlast| < v2
 */
public class RadRange extends SystemInstruction {
    private static RadRange singleton = null;

    public static RadRange get() {
        if(singleton == null) {
            singleton = new RadRange();
        }

        return singleton;
    }

    private RadRange() {
        super(
                new FunctionType[]{new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx, BaseType.real, BaseType.real, BaseType.unit, BaseType.unit), BaseType.bool)},
                SystemType.signatures(
                        SystemType.signature(SystemType.cplx, SystemType.cplx, SystemType.real, SystemType.real, SystemType.integer, SystemType.integer, SystemType.integer),
                        SystemType.signature(SystemType.cplxReg, SystemType.cplx, SystemType.real, SystemType.real, SystemType.integer, SystemType.integer, SystemType.integer),
                        SystemType.signature(SystemType.cplx, SystemType.cplxReg, SystemType.real, SystemType.real, SystemType.integer, SystemType.integer, SystemType.integer),
                        SystemType.signature(SystemType.cplxReg, SystemType.cplxReg, SystemType.real, SystemType.real, SystemType.integer, SystemType.integer, SystemType.integer)
                ),
                Kind.Unit
        );
    }

    @Override
    public void linearizeBool(List<Tree> exprArgs, Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        Iterator<Tree> ia = exprArgs.iterator();

        Tree zTree = ia.next();
        Tree zzTree = ia.next();
        Tree bailoutTree = ia.next();
        Tree epsilonTree = ia.next();
        Tree bailoutStmtTree = ia.next();
        Tree epsilonStmtTree = ia.next();

        ArrayList<Value> args = new ArrayList<>();

        args.add(zTree.linearizeExpr(null, program));
        args.add(zzTree.linearizeExpr(null, program));
        args.add(bailoutTree.linearizeExpr(null, program));
        args.add(epsilonTree.linearizeExpr(null, program));

        // Following structure:
        // If bailout is true, then jump to label bailoutLabel,
        // If epsilon is true, jump to label epsilonLabel (both of which jump to true)
        // otherwise jump to falseLabel

        Label bailoutLabel = new Label();
        Label epsilonLabel = new Label();

        args.add(bailoutLabel);
        args.add(epsilonLabel);
        // true-label will be ignored.

        args.add(falseLabel);

        Call call = Call.createCall(this, args);

        if(call == null) {
            throw new IllegalArgumentException("this is embarrasing... " +
                    "this case should have been caught during type analysis");
        }

        program.add(call);

        // code for bailout branch
        program.add(bailoutLabel);
        bailoutStmtTree.linearizeStmt(program);

        program.add(Jump.get().createCall(trueLabel));

        // code for epsilon branch
        program.add(epsilonLabel);

        epsilonStmtTree.linearizeStmt(program);

        program.add(Jump.get().createCall(trueLabel));

        // and this is it.
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        // This is linearized as a bool but actually is a Unit
        return String.format("pc = radrange(%s, %s, %s, %s, %s, %s, %s);",
                arguments.get(0), // z
                arguments.get(1), // zlast
                arguments.get(2), // bailout
                arguments.get(3), // epsilon
                arguments.get(4), // bailout branch
                arguments.get(5), // epsilon branch
                arguments.get(6)); // false
    }
}
