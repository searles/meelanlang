package at.searles.meelan.ops.special;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * |z - zlast| < eps
 */
public class DistLess extends SystemInstruction {
    private static DistLess singleton = null;

    public static DistLess get() {
        if(singleton == null) {
            singleton = new DistLess();
        }

        return singleton;
    }

    private DistLess() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx, BaseType.real), BaseType.bool));
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("dist2(%s, %s) < sqr(%s)",
                arguments.get(0),
                arguments.get(1),
                arguments.get(2));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Cplx z0 = ((CplxVal) args.get(0)).value();
        Cplx z1 = ((CplxVal) args.get(1)).value();
        double eps = ((Real) args.get(1)).value();

        return new Bool(z0.dist2(z1) < eps * eps);
    }
}
