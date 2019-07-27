package at.searles.meelan.ops.numeric;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.Collections;
import java.util.List;

/**
 * TODO: What does the cplx case do?
 */
public class CircleFn extends SystemInstruction {
    private static CircleFn singleton = null;

    public static CircleFn get() {
        if(singleton == null) {
            singleton = new CircleFn();
        }

        return singleton;
    }

    private CircleFn() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }
}
