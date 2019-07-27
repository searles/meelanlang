package at.searles.meelan.ops.color;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.Arrays;
import java.util.List;

/**
 * Blends two colors, using their alpha channel information.
 */
public class Over extends SystemInstruction {

    private static Over singleton = null;

    public static Over get() {
        if (singleton == null) {
            singleton = new Over();
        }

        return singleton;
    }

    private Over() {
        super(new FunctionType(Arrays.asList(BaseType.quat, BaseType.quat), BaseType.quat));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        // const is allowed
        return null;
    }
}
