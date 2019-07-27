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

public class Rgb2Int extends SystemInstruction {

    private static Rgb2Int singleton = null;

    public static Rgb2Int get() {
        if (singleton == null) {
            singleton = new Rgb2Int();
        }

        return singleton;
    }

    private Rgb2Int() {
        super(
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.integer)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Quat qrgb = ((QuatVal) args.get(0)).value();
        float[] rgb = new float[]{(float) qrgb.s0(), (float) qrgb.s1(), (float) qrgb.s2(), (float) qrgb.s3()};

        return new Int(Colors.rgb2int(rgb));
    }
}
