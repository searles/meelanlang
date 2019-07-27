package at.searles.meelan.ops.sys;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapCoordinates extends SystemInstruction {

    private static MapCoordinates singleton = null;

    public static MapCoordinates get() {
        if(singleton == null) {
            singleton = new MapCoordinates();
        }

        return singleton;
    }

    private MapCoordinates() {
        super(
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.cplx),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        // no const implementation.
        return null;
    }
}
