package at.searles.meelan.ops.special;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;

import java.util.Arrays;
import java.util.List;

/**
 * Shortcut for z^2 + p
 */
public class Mandelbrot extends SystemInstruction {
    private static Mandelbrot singleton = null;

    public static Mandelbrot get() {
        if(singleton == null) {
            singleton = new Mandelbrot();
        }

        return singleton;
    }

    private Mandelbrot() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.cplx));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Cplx z = ((CplxVal) args.get(0)).value();
        Cplx p = ((CplxVal) args.get(1)).value();

        return new CplxVal(new Cplx().add(new Cplx().sqr(z), p));
    }
}
