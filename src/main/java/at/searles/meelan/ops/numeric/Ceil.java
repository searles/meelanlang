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

public class Ceil extends SystemInstruction {
    private static Ceil singleton = null;

    public static Ceil get() {
        if(singleton == null) {
            singleton = new Ceil();
        }

        return singleton;
    }

    private Ceil() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                return new Real(Math.ceil(((Real) args.get(0)).value()));
            case cplx:
                return new CplxVal(new Cplx().ceil(((CplxVal) args.get(0)).value()));
        }

        return null;
    }
}
