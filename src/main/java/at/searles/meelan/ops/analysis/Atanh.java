package at.searles.meelan.ops.analysis;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.arithmetics.Div;
import at.searles.meelan.ops.arithmetics.Sub;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Int;
import at.searles.meelan.values.Real;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Atanh extends SystemInstruction implements DerivableInstruction {

    private static Atanh singleton = null;

    public static Atanh get() {
        if (singleton == null) {
            singleton = new Atanh();
        }

        return singleton;
    }

    private Atanh() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx)
        );
    }


    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                double z = ((Real) args.get(0)).value();
                return new Real(0.5 * (Math.log(1 + z ) + Math.log(1 - z)));
            case cplx:
                return new CplxVal(new Cplx().atanh(((CplxVal) args.get(0)).value()));
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
                        Sub.get().apply(sourceInfo, Arrays.asList(new Int(1),
                                Sqr.get().apply(sourceInfo, Collections.singletonList(x))))
                )
        );
    }
}
