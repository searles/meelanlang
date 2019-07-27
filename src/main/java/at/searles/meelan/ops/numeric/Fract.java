package at.searles.meelan.ops.numeric;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Real;

import java.util.Collections;
import java.util.List;

/**
 * Fractional part - returns only digits behind comma
 */
public class Fract extends SystemInstruction {
    private static Fract singleton = null;

    public static Fract get() {
        if(singleton == null) {
            singleton = new Fract();
        }

        return singleton;
    }

    private Fract() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                double d = ((Real) args.get(0)).value();
                return new Real(d - Math.floor(d));
            case cplx:
                Cplx c = ((CplxVal) args.get(0)).value();
                return new CplxVal(new Cplx().sub(c, new Cplx().floor(c)));
        }

        return null;
    }
}
