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

public class Lab2Rgb extends SystemInstruction {

    private static Lab2Rgb singleton = null;

    public static Lab2Rgb get() {
        if (singleton == null) {
            singleton = new Lab2Rgb();
        }

        return singleton;
    }

    private Lab2Rgb() {
        super(
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.quat)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Quat lab = ((QuatVal) args.get(0)).value();
        float[] rgb = Colors.lab2rgb(
                new float[]{(float) lab.s0(), (float) lab.s1(), (float) lab.s2(), (float) lab.s3()});

        return new QuatVal(new Quat(rgb[0], rgb[1], rgb[2], rgb[3]));
    }
}
