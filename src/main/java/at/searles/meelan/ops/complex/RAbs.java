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

public class RAbs extends SystemInstruction {
    private static RAbs singleton = null;

    public static RAbs get() {
        if(singleton == null) {
            singleton = new RAbs();
        }

        return singleton;
    }

    private RAbs() {
        super(new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx));
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Cplx z = ((CplxVal) args.get(0)).value();

        return new CplxVal(new Cplx(Math.abs(z.re()), z.im()));
    }
}
