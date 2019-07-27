package at.searles.meelan.ops.color;

import at.searles.commons.math.Quat;
import at.searles.commons.color.Colors;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.Collections;
import java.util.List;

public class Int2Lab extends SystemInstruction {

    private static Int2Lab singleton = null;

    public static Int2Lab get() {
        if (singleton == null) {
            singleton = new Int2Lab();
        }

        return singleton;
    }

    private Int2Lab() {
        super(
                new FunctionType(Collections.singletonList(BaseType.integer), BaseType.quat)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        float[] rgb = Colors.int2rgb(((Int) args.get(0)).value());
        float[] lab = Colors.rgb2lab(rgb);

        return new QuatVal(new Quat(lab[0], lab[1], lab[2], lab[3]));
    }
}
