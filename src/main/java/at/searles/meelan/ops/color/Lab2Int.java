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

public class Lab2Int extends SystemInstruction {

    private static Lab2Int singleton = null;

    public static Lab2Int get() {
        if (singleton == null) {
            singleton = new Lab2Int();
        }

        return singleton;
    }

    private Lab2Int() {
        super(
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.integer)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        Quat lab = ((QuatVal) args.get(0)).value();
        float[] rgb = Colors.lab2rgb(
                new float[]{(float) lab.s0(), (float) lab.s1(), (float) lab.s2(), (float) lab.s3()});

        return new Int(Colors.rgb2int(rgb));
    }
}
