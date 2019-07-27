package at.searles.meelan.ops.sys;


import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Debug extends SystemInstruction {

    private static Debug singleton = null;

    public static Debug get() {
        if(singleton == null) {
            singleton = new Debug();
        }

        return singleton;
    }

    private Debug() {
        super(
                new FunctionType(Collections.singletonList(BaseType.integer), BaseType.unit),
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.unit),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.unit),
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.unit)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        // just passing through
        return null;
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("debug(%s); pc += %d;", arguments.get(0), size);
    }
}
