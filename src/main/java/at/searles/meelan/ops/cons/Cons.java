package at.searles.meelan.ops.cons;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cons extends SystemInstruction {

    private static Cons singleton = null;

    public static Cons get() {
        if (singleton == null) {
            singleton = new Cons();
        }

        return singleton;
    }

    private Cons() {
        super(
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.cplx),
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real, BaseType.real, BaseType.real), BaseType.quat)
        );
    }

    @Override
    protected String call(ArrayList<String> accessArgs, int size, ArrayList<SystemType> signature) {
        SystemType returnType = signature.get(signature.size() - 1);

        int count = returnType.equals(SystemType.cplxReg) ? 2 : 4;

        return String.format("(double%d) {%s, %s}", count, accessArgs.get(0), accessArgs.get(1));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        // only one case
        if(functionType.argTypes().size() == 2) {
            return new CplxVal(new Cplx(((Real) args.get(0)).value(), ((Real) args.get(1)).value()));
        }

        assert functionType.argTypes().size() == 4;

        return new QuatVal(new Quat(
                ((Real) args.get(0)).value(), ((Real) args.get(1)).value(),
                ((Real) args.get(2)).value(), ((Real) args.get(3)).value()));
    }
}
