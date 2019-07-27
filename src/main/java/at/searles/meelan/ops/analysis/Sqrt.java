package at.searles.meelan.ops.analysis;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.arithmetics.Div;
import at.searles.meelan.ops.arithmetics.Mul;
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

public class Sqrt extends SystemInstruction implements DerivableInstruction {

    private static Sqrt singleton = null;

    public static Sqrt get() {
        if (singleton == null) {
            singleton = new Sqrt();
        }

        return singleton;
    }

    private Sqrt() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                return new Real(Math.sqrt(((Real) args.get(0)).value()));
            case cplx:
                return new CplxVal(new Cplx().sqrt(((CplxVal) args.get(0)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        // sqrt x' = 0.5 dx / sqrt x
        Tree x = args.get(0);
        Tree dx = dargs.get(0);
        return Mul.get().apply(
                sourceInfo, Arrays.asList(
                        new Real(0.5),
                        Div.get().apply(
                                sourceInfo, Arrays.asList(
                                        dx,
                                        this.apply(sourceInfo, Collections.singletonList(x))
                                )
                        )
                )
        );
    }
}
