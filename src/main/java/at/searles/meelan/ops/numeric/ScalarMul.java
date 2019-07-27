package at.searles.meelan.ops.numeric;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Component wise multiplication
 */
public class ScalarMul extends SystemInstruction {
    private static ScalarMul singleton = null;

    public static ScalarMul get() {
        if(singleton == null) {
            singleton = new ScalarMul();
        }

        return singleton;
    }

    private ScalarMul() {
        super(
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.cplx),
                new FunctionType(Arrays.asList(BaseType.quat, BaseType.quat), BaseType.quat)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("%s * %s", arguments.get(0), arguments.get(1));
    }
}
