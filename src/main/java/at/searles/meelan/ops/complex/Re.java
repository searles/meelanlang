package at.searles.meelan.ops.complex;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Real;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Re extends SystemInstruction {
    private static Re singleton = null;

    public static Re get() {
        if(singleton == null) {
            singleton = new Re();
        }

        return singleton;
    }

    private Re() {
        super(new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.real));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Real(((CplxVal) args.get(0)).value().re());
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("(%s).x", arguments.get(0));
    }
}
