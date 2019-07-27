package at.searles.meelan.ops.complex;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Real;

import java.util.Arrays;
import java.util.List;

public class Dist2 extends SystemInstruction {
    private static Dist2 singleton = null;

    public static Dist2 get() {
        if(singleton == null) {
            singleton = new Dist2();
        }

        return singleton;
    }

    private Dist2() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.real));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Cplx z0 = ((CplxVal) args.get(0)).value();
        Cplx z1 = ((CplxVal) args.get(1)).value();

        return new Real(z0.dist2(z1));
    }
}
