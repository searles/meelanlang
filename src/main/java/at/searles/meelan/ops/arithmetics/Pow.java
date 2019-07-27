package at.searles.meelan.ops.arithmetics;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.analysis.Log;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Pow extends SystemInstruction implements DerivableInstruction {

    private static Pow singleton = null;

    public static Pow get() {
        if(singleton == null) {
            singleton = new Pow();
        }

        return singleton;
    }

    private Pow() {
        super(
                new FunctionType(Arrays.asList(BaseType.integer, BaseType.integer), BaseType.integer),
                new FunctionType(Arrays.asList(BaseType.real, BaseType.integer), BaseType.real),
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.real),
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.integer), BaseType.cplx),
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.real), BaseType.cplx),
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.cplx)
        );
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        if(args.get(1).type() == BaseType.integer) {
            switch (functionType.returnType()) {
                case integer:
                    // TODO integer method?
                    return new Int((int) Math.round(Math.pow(((Int) args.get(0)).value(), ((Int) args.get(1)).value())));
                case real:
                    // TODO integer method?
                    return new Real(Math.pow(((Real) args.get(0)).value(), ((Int) args.get(1)).value()));
                case cplx:
                    return new CplxVal(new Cplx().powInt(((CplxVal) args.get(0)).value(), ((Int) args.get(1)).value()));
                default:
                    throw new IllegalArgumentException("this case is not part of the signature.");
            }
        }

        switch(functionType.returnType()) {
            case real:
                return new Real(Math.pow(((Real) args.get(0)).value(), ((Real) args.get(1)).value()));
            case cplx:
                Cplx exponent;

                if(args.get(1).type() == BaseType.real) {
                    exponent = new Cplx(((Real) args.get(1)).value());
                } else {
                    exponent = ((CplxVal) args.get(1)).value();
                }

                return new CplxVal(new Cplx().pow(((CplxVal) args.get(0)).value(), exponent));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        // a^b' = a^b * (b * da / a + ln a * db)

        Tree a0 = args.get(0);
        Tree a1 = args.get(1);

        Tree p = apply(sourceInfo, Arrays.asList(a0, a1));

        Tree s0 = Div.get().apply(
                sourceInfo, Arrays.asList(
                        Mul.get().apply(
                                sourceInfo, Arrays.asList(a1, dargs.get(0))), a0));

        Tree s1 = Mul.get().apply(sourceInfo, Arrays.asList(Log.get().apply(sourceInfo, Collections.singletonList(a0)), dargs.get(1)));

        Tree s = Add.get().apply(sourceInfo, Arrays.asList(s0, s1));

        return Mul.get().apply(sourceInfo, Arrays.asList(p, s));
    }
}
