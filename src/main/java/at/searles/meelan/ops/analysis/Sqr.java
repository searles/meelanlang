package at.searles.meelan.ops.analysis;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.ops.arithmetics.Mul;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sqr extends SystemInstruction implements DerivableInstruction {

    private static Sqr singleton = null;

    public static Sqr get() {
        if (singleton == null) {
            singleton = new Sqr();
        }

        return singleton;
    }

    private Sqr() {
        super(
                new FunctionType(Collections.singletonList(BaseType.integer), BaseType.integer),
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx),
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.quat)
        );
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case integer:
                int i = ((Int) args.get(0)).value();
                return new Int(i * i);
            case real:
                double r = ((Real) args.get(0)).value();
                return new Real(r * r);
            case cplx:
                Cplx c = ((CplxVal) args.get(0)).value();
                return new CplxVal(new Cplx().mul(c, c));
            case quat:
                Quat q = ((QuatVal) args.get(0)).value();
                return new QuatVal(new Quat().mul(q, q));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        switch(signature.get(0)) {
            case integer:
            case integerReg:
            case real:
            case realReg:
                return String.format("%s * %s", arguments.get(0), arguments.get(0));
            case cplx:
            case cplxReg:
            case quat:
            case quatReg:
                return String.format("mul(%s, %s)", arguments.get(0), arguments.get(0));
        }

        return super.call(arguments, size, signature);
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        Tree x = args.get(0);
        Tree dx = dargs.get(0);
        return Mul.get().apply(
                sourceInfo, Arrays.asList(
                        new Int(2),
                        x,
                        dx
                )
        );
    }
}
