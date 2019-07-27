package at.searles.meelan.ops.color;

import at.searles.commons.math.Quat;
import at.searles.commons.color.Colors;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.QuatVal;

import java.util.Collections;
import java.util.List;

public class Rgb2Lab extends SystemInstruction {

    private static Rgb2Lab singleton = null;

    public static Rgb2Lab get() {
        if (singleton == null) {
            singleton = new Rgb2Lab();
        }

        return singleton;
    }

    private Rgb2Lab() {
        super(
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.quat)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Quat rgb = ((QuatVal) args.get(0)).value();
        float[] lab = Colors.rgb2lab(
                new float[]{(float) rgb.s0(), (float) rgb.s1(), (float) rgb.s2(), (float) rgb.s3()});

        return new QuatVal(new Quat(lab[0], lab[1], lab[2], lab[3]));
    }
}
