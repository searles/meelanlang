package at.searles.meelan.ops.numeric;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.Collections;
import java.util.List;

/**
 * Absolute value, for cplx and quat component wise.
 */
public class Abs extends SystemInstruction {
    private static Abs singleton = null;

    public static Abs get() {
        if(singleton == null) {
            singleton = new Abs();
        }

        return singleton;
    }

    private Abs() {
        super(
                new FunctionType(Collections.singletonList(BaseType.integer), BaseType.integer),
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx),
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.quat)
        );
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch (functionType.returnType()) {
            case integer:
                return new Int(Math.abs(((Int) args.get(0)).value()));
            case real:
                return new Real(Math.abs(((Real) args.get(0)).value()));
            case cplx:
                return new CplxVal(new Cplx().abs(((CplxVal) args.get(0)).value()));
            case quat:
                return new QuatVal(new Quat().abs(((QuatVal) args.get(0)).value()));
        }
        return null;
    }
}
