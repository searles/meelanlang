package at.searles.meelan.ops.analysis;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.arithmetics.Mul;
import at.searles.meelan.ops.arithmetics.Neg;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Real;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Cos extends SystemInstruction implements DerivableInstruction {

    private static Cos singleton = null;

    public static Cos get() {
        if (singleton == null) {
            singleton = new Cos();
        }

        return singleton;
    }

    private Cos() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                return new Real(Math.cos(((Real) args.get(0)).value()));
            case cplx:
                return new CplxVal(new Cplx().cos(((CplxVal) args.get(0)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        Tree x = args.get(0);
        Tree dx = dargs.get(0);
        return Mul.get().apply(
                sourceInfo, Arrays.asList(
                        Neg.get().apply(
                                sourceInfo, Collections.singletonList(
                                        Sin.get().apply(sourceInfo, Collections.singletonList(x)))),
                        dx
                )
        );
    }
}
