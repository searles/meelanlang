package at.searles.meelan.ops.special;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.Arrays;
import java.util.List;

/**
 * Was an old Errorness function of fractal smoothing.
 * In version 4.0 replaced by the working one. Smoothen was deprecated.
 */
public class Smoothen extends SystemInstruction {
    private static Smoothen singleton = null;

    public static Smoothen get() {
        if(singleton == null) {
            singleton = new Smoothen();
        }

        return singleton;
    }

    private Smoothen() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.real, BaseType.real), BaseType.real));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }
}
