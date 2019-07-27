package at.searles.meelan.ops.color;

import at.searles.commons.math.Quat;
import at.searles.commons.color.Colors;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Int;
import at.searles.meelan.values.QuatVal;

import java.util.Collections;
import java.util.List;

public class Int2Rgb extends SystemInstruction {

    private static Int2Rgb singleton = null;

    public static Int2Rgb get() {
        if (singleton == null) {
            singleton = new Int2Rgb();
        }

        return singleton;
    }

    private Int2Rgb() {
        super(
                new FunctionType(Collections.singletonList(BaseType.integer), BaseType.quat)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        float[] rgb = Colors.int2rgb(((Int) args.get(0)).value());

        return new QuatVal(new Quat(rgb[0], rgb[1], rgb[2], rgb[3]));
    }
}
