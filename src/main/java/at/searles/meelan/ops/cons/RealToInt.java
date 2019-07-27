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

public class RealToInt extends SystemInstruction {

    private static RealToInt singleton = null;

    public static RealToInt get() {
        if (singleton == null) {
            singleton = new RealToInt();
        }

        return singleton;
    }

    private RealToInt() {
        super(new FunctionType(Collections.singletonList(BaseType.real), BaseType.integer));
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("(int) %s", arguments.get(0));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Int((int) ((Real) args.get(0)).value());
    }
}
