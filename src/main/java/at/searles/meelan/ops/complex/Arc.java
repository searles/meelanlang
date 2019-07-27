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

public class Arc extends SystemInstruction {
    private static Arc singleton = null;

    public static Arc get() {
        if(singleton == null) {
            singleton = new Arc();
        }

        return singleton;
    }

    private Arc() {
        super(
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.real)
        );
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Real(((CplxVal) args.get(0)).value().arc());
    }

}
