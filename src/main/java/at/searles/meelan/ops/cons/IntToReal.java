package at.searles.meelan.ops.cons;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Int;
import at.searles.meelan.values.Real;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntToReal extends SystemInstruction {
    private static IntToReal singleton = null;

    public static IntToReal get() {
        if(singleton == null) {
            singleton = new IntToReal();
        }

        return singleton;
    }

    private IntToReal() {
        super(new FunctionType(Collections.singletonList(BaseType.integer), BaseType.real));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Real(((Int) args.get(0)).value());
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("(double) %s", arguments.get(0));
    }
}
