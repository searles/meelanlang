package at.searles.meelan.ops.complex;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Real;

import java.util.Collections;
import java.util.List;

public class Rad2 extends SystemInstruction {
    private static Rad2 singleton = null;

    public static Rad2 get() {
        if(singleton == null) {
            singleton = new Rad2();
        }

        return singleton;
    }

    private Rad2() {
        super(
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.real)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Real(((CplxVal) args.get(0)).value().rad2());
    }

}
