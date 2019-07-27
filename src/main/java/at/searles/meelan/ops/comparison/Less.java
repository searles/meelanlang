package at.searles.meelan.ops.comparison;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Less extends SystemInstruction {

    private static Less singleton = null;

    public static Less get() {
        if(singleton == null) {
            singleton = new Less();
        }

        return singleton;
    }

    private Less() {
        super(
                new FunctionType(Arrays.asList(BaseType.integer, BaseType.integer), BaseType.bool),
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.bool));
    }

    @Override
    public String call(ArrayList<String> args, int size, ArrayList<SystemType> signature) {
        return String.format("%s < %s", args.get(0),args.get(1));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.argTypes().get(0)) {
            case integer:
                return new Bool(((Int) args.get(0)).value() < ((Int) args.get(1)).value());
            case real:
                return new Bool(((Real) args.get(0)).value() < ((Real) args.get(1)).value());
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }
}
