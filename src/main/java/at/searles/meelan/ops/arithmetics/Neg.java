package at.searles.meelan.ops.arithmetics;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemInstruction;
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
import java.util.Collections;
import java.util.List;

import static at.searles.meelan.rewriting.Term.a;
import static at.searles.meelan.rewriting.Term.v;

public class Neg extends SystemInstruction implements DerivableInstruction {

    private static Neg singleton = null;
    private TRS trs;

    public static Neg get() {
        if(singleton == null) {
            singleton = new Neg();
            singleton.initOptimizations();
        }

        return singleton;
    }

    private Neg() {
        super(
                new FunctionType(Collections.singletonList(BaseType.integer), BaseType.integer),
                new FunctionType(Collections.singletonList(BaseType.real), BaseType.real),
                new FunctionType(Collections.singletonList(BaseType.cplx), BaseType.cplx),
                new FunctionType(Collections.singletonList(BaseType.quat), BaseType.quat)
        );
    }

    private void initOptimizations() {
        trs = new TRS();

        trs.add(// --x -> x
                new Rule(
                        a(Neg.get(), a(Neg.get(), v("x"))),
                        v("x")
                )
        );

        trs.add(// -(x + y) -> -x - y
                new Rule(
                        a(this, a(Add.get(), v("x"), v("y"))),
                        a(Sub.get(), a(Neg.get(), v("x")), v("y"))
                )
        );

        trs.add(// -(x - y) -> -x + y
                new Rule(
                        a(this, a(Sub.get(), v("x"), v("y"))),
                        a(Add.get(), a(Neg.get(), v("x")), v("y"))
                )
        );
    }

    @Override
    protected Tree optimize(App app) throws MeelanException {
        return trs.apply(app);
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {
        return apply(sourceInfo, dargs);
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case integer:
                return new Int(-((Int) args.get(0)).value());
            case real:
                return new Real(-((Real) args.get(0)).value());
            case cplx:
                return new CplxVal(new Cplx().neg(((CplxVal) args.get(0)).value()));
            case quat:
                return new QuatVal(new Quat().neg(((QuatVal) args.get(0)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("-%s", arguments.get(0));
    }

//    @Override
//    public Tree apply(Tree arg) {
//        return unaryEval(arg);
//    }


//    public class UnaryOp extends Operator {
//        public UnaryOp(String name, Signature... signatures) {
//            super(name, signatures);
//        }
//
//        @Override
//        public Tree eval(List<Tree> arguments) {
//            return unaryEval(arguments);
//        }
//
//        Tree unaryEval(List<Tree> arguments) {
//            if (arguments.size() == 1) {
//                Tree t0 = arguments.get(0);
//                if(t0 instanceof Const) {
//                    Tree t = ((Const) t0).apply(this);
//                    if(t != null) return t;
//                }
//            }
//
//            return new Call(this, arguments);
//        }
//
//
//
//    }

}
