package at.searles.meelan.ops.special;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Bool;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Real;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * |z| < eps
 */
public class RadLess extends SystemInstruction {
    private static RadLess singleton = null;

    public static RadLess get() {
        if(singleton == null) {
            singleton = new RadLess();
        }

        return singleton;
    }

    private RadLess() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.real), BaseType.bool));
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("rad2(%s) < sqr(%s)", arguments.get(0), arguments.get(1));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Cplx z = ((CplxVal) args.get(0)).value();
        double eps = ((Real) args.get(1)).value();

        return new Bool(z.rad() < eps);
    }
}
