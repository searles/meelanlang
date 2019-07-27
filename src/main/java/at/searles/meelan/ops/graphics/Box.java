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
public class Box extends SystemInstruction {
    private static Box singleton = null;

    public static Box get() {
        if(singleton == null) {
            singleton = new Box();
        }

        return singleton;
    }

    private Box() {
        super(new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx, BaseType.cplx), BaseType.real));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return null;
    }
}
