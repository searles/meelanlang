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

/**
 * Created by searles on 09.12.17.
 */
public class Conj extends SystemInstruction {
    private static Conj singleton = null;

    public static Conj get() {
        if(singleton == null) {
            singleton = new Conj();
        }

        return singleton;
    }

    private Conj() {
        super(new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx));
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new CplxVal(new Cplx().conj(((CplxVal) args.get(0)).value()));
    }
}
