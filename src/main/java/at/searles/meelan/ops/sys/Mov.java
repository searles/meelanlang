package at.searles.meelan.ops.sys;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Call;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Assignment operation. Where is this one used???
 */
public class Mov extends SystemInstruction {

    private static Mov singleton = null;

    public static Mov get() {
        if(singleton == null) {
            singleton = new Mov();
        }

        return singleton;
    }

    private Mov() {
        super(
                Arrays.asList(
                        new ArrayList<>(Arrays.asList(SystemType.integer, SystemType.integerReg)),
                        new ArrayList<>(Arrays.asList(SystemType.integerReg, SystemType.integerReg)),
                        new ArrayList<>(Arrays.asList(SystemType.real, SystemType.realReg)),
                        new ArrayList<>(Arrays.asList(SystemType.realReg, SystemType.realReg)),
                        new ArrayList<>(Arrays.asList(SystemType.cplx, SystemType.cplxReg)),
                        new ArrayList<>(Arrays.asList(SystemType.cplxReg, SystemType.cplxReg)),
                        new ArrayList<>(Arrays.asList(SystemType.quat, SystemType.quatReg)),
                        new ArrayList<>(Arrays.asList(SystemType.quatReg, SystemType.quatReg))
                )
        );
    }

    public Call createAssignment(Value value, Reg target) {
        Call call = Call.createCall(this, Arrays.asList(value, target));

        if(call == null) {
            throw new IllegalArgumentException("this is embarrassing...");
        }

        return call;
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("%s = %s; pc += %d;", arguments.get(1), arguments.get(0), size);
    }
}
