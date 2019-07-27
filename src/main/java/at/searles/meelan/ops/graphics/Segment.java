package at.searles.meelan.ops.graphics;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.Arrays;
import java.util.List;

/**
 * Segment = line from start to end.
 */
public class Segment extends SystemInstruction {
    private static Segment singleton = null;

    public static Segment get() {
        if(singleton == null) {
            singleton = new Segment();
        }

        return singleton;
    }

    private Segment() {
        super(
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx, BaseType.cplx), BaseType.real)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }
}
