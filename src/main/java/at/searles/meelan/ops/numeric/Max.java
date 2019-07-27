package at.searles.meelan.ops.numeric;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.ops.BinaryInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.Arrays;
import java.util.List;

/**
 * Component-wise maximum
 */
public class Max extends BinaryInstruction {

    private static Max singleton = null;

    public static Max get() {
        if(singleton == null) {
            singleton = new Max();
        }

        return singleton;
    }

    private Max() {
        super(
                new FunctionType(Arrays.asList(BaseType.integer, BaseType.integer), BaseType.integer),
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.real),
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.cplx),
                new FunctionType(Arrays.asList(BaseType.quat, BaseType.quat), BaseType.quat)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case integer:
                return new Int(Math.max(((Int) args.get(0)).value(), ((Int) args.get(1)).value()));
            case real:
                return new Real(Math.max(((Real) args.get(0)).value(), ((Real) args.get(1)).value()));
            case cplx:
                return new CplxVal(new Cplx().max(((CplxVal) args.get(0)).value(), ((CplxVal) args.get(1)).value()));
            case quat:
                return new QuatVal(new Quat().max(((QuatVal) args.get(0)).value(), ((QuatVal) args.get(1)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }
}
