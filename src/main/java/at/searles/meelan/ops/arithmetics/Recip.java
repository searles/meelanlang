package at.searles.meelan.ops.arithmetics;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.ops.analysis.Sqr;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.App;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.rewriting.Rule;
import at.searles.meelan.rewriting.TRS;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static at.searles.meelan.rewriting.Term.a;
import static at.searles.meelan.rewriting.Term.v;

public class Recip extends SystemInstruction implements DerivableInstruction {

    private static Recip singleton = null;
    private TRS trs;

    public static Recip get() {
        if(singleton == null) {
            singleton = new Recip();
            singleton.initOptimizations();
        }

        return singleton;
    }

    private Recip() {
        super(
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx),
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.quat)
        );
    }

    private void initOptimizations() {
        trs = new TRS();

        trs.add(// //x -> x
                new Rule(
                        a(this, a(this, v("x"))),
                        v("x")
                )
        );

        trs.add(// /(x * y) -> /x / y
                new Rule(
                        a(this, a(Mul.get(), v("x"), v("y"))),
                        a(Div.get(), a(this, v("x")), v("y"))
                )
        );

        trs.add(// /(x / y) -> /x * y
                new Rule(
                        a(this, a(Div.get(), v("x"), v("y"))),
                        a(Mul.get(), a(this, v("x")), v("y"))
                )
        );
    }

    @Override
    protected Tree optimize(App app) throws MeelanException {
        return trs.apply(app);
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                return new Real(1. / ((Real) args.get(0)).value());
            case cplx:
                return new CplxVal(new Cplx().rec(((CplxVal) args.get(0)).value()));
            case quat:
                return new QuatVal(new Quat().rec(((QuatVal) args.get(0)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        SystemType type = signature.get(0);

        if(type == SystemType.real || type == SystemType.realReg) {
            return String.format("1.0 / %s", arguments.get(0));
        }

        return super.call(arguments, size, signature);
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        // /x' -> -dx/sqr x
        Tree x = args.get(0);
        Tree ndx = Neg.get().apply(sourceInfo, Collections.singletonList(dargs.get(0)));
        return Div.get().apply(sourceInfo, Arrays.asList(ndx, Sqr.get().apply(sourceInfo, Collections.singletonList(x))));
    }
}
