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

public class Arcnorm extends SystemInstruction {
    private static Arcnorm singleton = null;

    public static Arcnorm get() {
        if(singleton == null) {
            singleton = new Arcnorm();
        }

        return singleton;
    }

    private Arcnorm() {
        super(
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.real)
        );
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Real(((CplxVal) args.get(0)).value().arc() / (2 * Math.PI));
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("arc(%s) / (2 * M_PI)", arguments.get(0));
    }
}
