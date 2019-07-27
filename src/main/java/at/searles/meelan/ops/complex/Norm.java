package at.searles.meelan.ops.complex;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;

import java.util.Collections;
import java.util.List;

/**
 * normalizes the length of a
 * cplx num to 1.
 */
public class Norm extends SystemInstruction {
    private static Norm singleton = null;

    public static Norm get() {
        if(singleton == null) {
            singleton = new Norm();
        }

        return singleton;
    }

    private Norm() {
        super(new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Cplx z = ((CplxVal) args.get(0)).value();
        double len = z.rad();
        return new CplxVal(new Cplx(z.re() / len, z.im() / len));
    }
}
