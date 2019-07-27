package at.searles.meelan.ops.numeric;

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

public class Dot extends SystemInstruction {
    private static Dot singleton = null;

    public static Dot get() {
        if(singleton == null) {
            singleton = new Dot();
        }

        return singleton;
    }

    private Dot() {
        super(
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.real)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Cplx z0 = ((CplxVal) args.get(0)).value();
        Cplx z1 = ((CplxVal) args.get(1)).value();
        return new Real(z0.re() * z1.re() + z0.im() * z1.im());
    }
}
