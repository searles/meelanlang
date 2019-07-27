package at.searles.meelan.ops.graphics;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.Arrays;
import java.util.List;

/**
 * Line of infinite length though a and b
 */
public class Line extends SystemInstruction {
    private static Line singleton = null;

    public static Line get() {
        if(singleton == null) {
            singleton = new Line();
        }

        return singleton;
    }

    private Line() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx, BaseType.cplx), BaseType.real));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }
}
