package at.searles.meelan.ops.arithmetics;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.BinaryInstruction;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemType;
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
import java.util.List;
import java.util.Map;

import static at.searles.meelan.rewriting.Term.*;

public class Mul extends BinaryInstruction implements DerivableInstruction {

    private static Mul singleton = null;

    public static Mul get() {
        if(singleton == null) {
            singleton = new Mul();
            singleton.initOptimizations();
        }

        return singleton;
    }

    private TRS trs;

    private Mul() {
        super(
                new FunctionType(Arrays.asList(BaseType.integer, BaseType.integer), BaseType.integer),
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.real),
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.cplx),
                new FunctionType(Arrays.asList(BaseType.quat, BaseType.quat), BaseType.quat)
        );
    }

    private void initOptimizations() {

        trs = new TRS();

        trs.add(// x * y -> y * x if x is not Const and y is Const
                new Rule(
                        a(this, v("x"), v("y")),
                        a(this, v("y"), v("x"))
                ) {
                    @Override
                    protected boolean condition(Map<String, Tree> matcher) {
                        return !(matcher.get("x") instanceof Const) && matcher.get("y") instanceof Const;
                    }
                }
        );

        trs.add(// x * y -> 0 if x is 0
                new Rule(
                        a(this, v("x"), v("y")),
                        n(0)
                ) {
                    @Override
                    protected boolean condition(Map<String, Tree> matcher) {
                        Tree x = matcher.get("x");

                        return x instanceof Const && ((Const) x).isZero();
                    }
                }
        );

        trs.add(// x * y -> y if x is 1
                new Rule(
                        a(this, v("x"), v("y")),
                        v("y")
                ) {
                    @Override
                    protected boolean condition(Map<String, Tree> matcher) {
                        Tree x = matcher.get("x");

                        return x instanceof Const && ((Const) x).isOne();
                    }
                }
        );

        trs.add(// x * (y * z) -> (x * y) * z
                new Rule(
                        a(this, v("x"), a(this, v("y"), v("z"))),
                        a(this, a(this, v("x"), v("y")), v("z"))
                )
        );

        trs.add(// x * (y / z) -> (x * y) / z
                new Rule(
                        a(this, v("x"), a(this, v("y"), v("z"))),
                        a(Div.get(), a(this, v("x"), v("y")), v("z"))
                )
        );

        trs.add(// x * /y -> x / y
                new Rule(
                        a(this, v("x"), a(Recip.get(), v("y"))),
                        a(Div.get(), v("x"), v("y"))
                )
        );

        trs.add(// -x * y -> -(x * y)
                new Rule(
                        a(this, a(Neg.get(), v("x")), v("y")),
                        a(Neg.get(), a(Mul.get(), v("x"), v("y")))
                )
        );

        trs.add(// x * -y -> -(x * y)
                new Rule(
                        a(this, v("x"), a(Neg.get(), v("y"))),
                        a(Neg.get(), a(Mul.get(), v("x"), v("y")))
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
            case integer:
                return new Int(((Int) args.get(0)).value() * ((Int) args.get(1)).value());
            case real:
                return new Real(((Real) args.get(0)).value() * ((Real) args.get(1)).value());
            case cplx:
                return new CplxVal(new Cplx().mul(((CplxVal) args.get(0)).value(), ((CplxVal) args.get(1)).value()));
            case quat:
                return new QuatVal(new Quat().mul(((QuatVal) args.get(0)).value(), ((QuatVal) args.get(1)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        SystemType type = signature.get(0);

        if(type == SystemType.integer || type == SystemType.integerReg ||
                type == SystemType.real || type == SystemType.realReg) {
            return String.format("%s * %s", arguments.get(0), arguments.get(1));
        }

        return super.call(arguments, size, signature);
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        Tree duv = apply(sourceInfo, Arrays.asList(dargs.get(0), args.get(1)));
        Tree udv = apply(sourceInfo, Arrays.asList(args.get(0), dargs.get(1)));

        return Add.get().apply(sourceInfo, Arrays.asList(duv, udv));
    }
}
