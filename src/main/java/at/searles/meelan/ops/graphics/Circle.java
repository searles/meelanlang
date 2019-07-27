package at.searles.meelan.ops.graphics;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.Arrays;
import java.util.List;

/**
 * Created by searles on 09.12.17.
 */
public class Circle extends SystemInstruction {
    private static Circle singleton = null;

    public static Circle get() {
        if(singleton == null) {
            singleton = new Circle();
        }

        return singleton;
    }

    private Circle() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.real, BaseType.cplx), BaseType.real));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }
}
