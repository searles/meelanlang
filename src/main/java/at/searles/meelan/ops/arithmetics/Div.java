package at.searles.meelan.ops.arithmetics;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.ops.BinaryInstruction;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.ops.analysis.Sqr;
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

public class Div extends BinaryInstruction implements DerivableInstruction {

    private static Div singleton = null;

    public static Div get() {
        if(singleton == null) {
            singleton = new Div();
        }

        return singleton;
    }

    private Div() {
        super(
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.real),
                new FunctionType(Arrays.asList(BaseType.cplx, BaseType.cplx), BaseType.cplx),
                new FunctionType(Arrays.asList(BaseType.quat, BaseType.quat), BaseType.quat)
        );
    }

    @Override
    public Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs) {

        // (u * 1/v)' = du / v - u * dv / sqr v

        Tree duv = Div.get().apply(sourceInfo, Arrays.asList(dargs.get(0), args.get(1)));
        Tree udv = Mul.get().apply(sourceInfo, Arrays.asList(args.get(0), dargs.get(1)));
        Tree v2 = Sqr.get().apply(sourceInfo, Collections.singletonList(args.get(1)));

        Tree udvv2 = Div.get().apply(sourceInfo, Arrays.asList(udv, v2));

        return Sub.get().apply(sourceInfo, Arrays.asList(duv, udvv2));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        switch(functionType.returnType()) {
            case real:
                return new Real(((Real) args.get(0)).value() / ((Real) args.get(1)).value());
            case cplx:
                return new CplxVal(new Cplx().div(((CplxVal) args.get(0)).value(), ((CplxVal) args.get(1)).value()));
            case quat:
                return new QuatVal(new Quat().div(((QuatVal) args.get(0)).value(), ((QuatVal) args.get(1)).value()));
            default:
                throw new IllegalArgumentException("this case is not part of the signature.");
        }
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        SystemType type = signature.get(0);

        if(type == SystemType.integer || type == SystemType.integerReg ||
                type == SystemType.real || type == SystemType.realReg) {
            return String.format("%s / %s", arguments.get(0), arguments.get(1));
        }

        return super.call(arguments, size, signature);
    }


//    @Override
//    public Const evalConsts(Const c0, Const c1) {
//        return c0.div(c1);
//    }
//
//    @Override
//    public Tree eval(List<Tree> arguments) {
//        if(arguments.size() == 2) {
//            Tree a0 = arguments.get(0);
//            Tree a1 = arguments.get(1);
//
//            // div by 0 is bad.
//
//            if(a0 instanceof Int && ((Int) a0).value == 0) return a0;
//            // if(a1 instanceof Int && ((Int) a1).value == 0) return a1;
//            if(a0 instanceof Real && ((Real) a0).value == 0) return a0;
//            // if(a1 instanceof Real && ((Real) a1).value == 0) return a1;
//            if(a0 instanceof CplxVal && ((CplxVal) a0).value.equals(new Cplx(0))) return a0;
//            // if(a1 instanceof CplxVal && ((CplxVal) a1).value.equals(new Cplx(0))) return a1;
//            if(a0 instanceof QuatVal && ((QuatVal) a0).value.equals(new Quat(0, 0, 0, 0))) return a0;
//            // if(a1 instanceof QuatVal && ((QuatVal) a1).value.equals(new Quat(0, 0, 0, 0))) return a1;
//
//            if(a0 instanceof Int && ((Int) a0).value == 1) return recip.eval(a1);
//            if(a1 instanceof Int && ((Int) a1).value == 1) return a0;
//            if(a0 instanceof Real && ((Real) a0).value == 1) return recip.eval(a1);
//            if(a1 instanceof Real && ((Real) a1).value == 1) return a0;
//            if(a0 instanceof CplxVal && ((CplxVal) a0).value.equals(new Cplx(1))) return recip.eval(a1);
//            if(a1 instanceof CplxVal && ((CplxVal) a1).value.equals(new Cplx(1))) return a0;
//            if(a0 instanceof QuatVal && ((QuatVal) a0).value.equals(new Quat(1, 0, 0, 0))) return recip.eval(a1);
//            if(a1 instanceof QuatVal && ((QuatVal) a1).value.equals(new Quat(1, 0, 0, 0))) return a0;
//        }
//
//        return binaryEval(arguments);
//    }
}
