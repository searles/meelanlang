package at.searles.meelan.ops.analysis;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.arithmetics.Div;
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

public class Tan extends SystemInstruction implements DerivableInstruction {

    private static Tan singleton = null;

    public static Tan get() {
        if (singleton == null) {
            singleton = new Tan();
        }

        return singleton;
    }

    private Tan() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                return new Real(Math.tan(((Real) args.get(0)).value()));
            case cplx:
                return new CplxVal(new Cplx().tan(((CplxVal) args.get(0)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        Tree x = args.get(0);
        Tree dx = dargs.get(0);
        return Div.get().apply(
                sourceInfo, Arrays.asList(
                        dx,
                        Sqr.get().apply(sourceInfo, Collections.singletonList(
                                Cos.get().apply(sourceInfo, Collections.singletonList(x))))
                )
        );
    }
}
