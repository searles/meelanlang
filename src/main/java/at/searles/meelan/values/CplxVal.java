package at.searles.meelan.values;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Objects;

public class CplxVal extends Const /*implements CplxConst, QuatConst*/ {

    private Cplx value;

    public CplxVal(Cplx value) {
        super(DummyInfo.getInstance(), BaseType.cplx);
        this.value = value;
    }

    @Override
    public Value convertTo(BaseType type) {
        switch (type) {
            case cplx:
                return this;
            case quat:
                return new QuatVal(new Quat(value.re(), value.im(), 0, 0));
            default:
                throw new IllegalArgumentException(String.format("cannot convert cplx to %s", type));
        }
    }


    @Override
    public boolean isZero() {
        return value.re() == 0.0 && value.im() == 0.0;
    }

    @Override
    public boolean isOne() {
        return value.re() == 1.0 && value.im() == 0.0;
    }

    @Override
    public boolean isNum(int n) {
        return value.im() == 0 && value.re() == n;
    }

    @Override
    public SystemType systemType() {
        return SystemType.cplx;
    }

    @Override
    public Value member(SourceInfo info, String memberId) throws MeelanException {
        if(memberId.equals("x")) {
            return new Real(value.re());
        }

        if(memberId.equals("y")) {
            return new Real(value.im());
        }

        return super.member(info, memberId);
    }

    @Override
    public void addIntCode(IntCode code) {
        new Real(value.re()).addIntCode(code);
        new Real(value.im()).addIntCode(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CplxVal cplxVal = (CplxVal) o;
        return Objects.equals(value, cplxVal.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }

    public Cplx value() {
        return value;
    }

    public String toString() {
        return value.toString();
    }

//    public Value subitem(String id) throws CompileException {
//        if (id.equals("x")) {
//            return new Real(value.re());
//        } else if (id.equals("y")) {
//            return new Real(value.im());
//        }
//
//        return super.subitem(id);
//    }
//
//    @Override
//    public int[] generateVMCode() {
//        int[] res = Real.dtoi(value.re());
//        int[] ims = Real.dtoi(value.im());
//        return new int[]{res[0], res[1], ims[0], ims[1]};
//    }
//
//    @Override
//    public String toString() {
//        return "cplx[" + value + "]";
//    }
//
//    static Const applyBinary(OpNew op, Cplx a0, Cplx a1) {
//        // FIXME switch to op.apply(a0, a1);
//
//        switch (op) {
//            case add:
//                return new at.searles.CplxVal(new Cplx().add(a0, a1));
//            case sub:
//                return new at.searles.CplxVal(new Cplx().sub(a0, a1));
//            case mul:
//                return new at.searles.CplxVal(new Cplx().mul(a0, a1));
//            case div:
//                return new at.searles.CplxVal(new Cplx().div(a0, a1));
//            case mod:
//                return new at.searles.CplxVal(new Cplx().mod(a0, a1));
//            case pow:
//                return new at.searles.CplxVal(new Cplx().pow(a0, a1));
//            case min:
//                return new at.searles.CplxVal(new Cplx().min(a0, a1));
//            case max:
//                return new at.searles.CplxVal(new Cplx().max(a0, a1));
//            default:
//                return QuatVal.applyBinary(op, new Quat(a0.re(), a0.im(), 0, 0), new Quat(a1.re(), a1.im(), 0, 0));
//        }
//    }
//
//    static Const applyUnary(OpNew op, Cplx a) {
//        switch (op) {
//            case neg:
//                return new at.searles.CplxVal(new Cplx().neg(a));
//            case recip:
//                return new at.searles.CplxVal(new Cplx().rec(a));
//            case abs:
//                return new at.searles.CplxVal(new Cplx().abs(a));
//            case conj:
//                return new at.searles.CplxVal(new Cplx().conj(a));
//            case sqr:
//                return new at.searles.CplxVal(new Cplx().sqr(a));
//            case sqrt:
//                return new at.searles.CplxVal(new Cplx().sqrt(a));
//            case log:
//                return new at.searles.CplxVal(new Cplx().log(a));
//            case exp:
//                return new at.searles.CplxVal(new Cplx().exp(a));
//            case sin:
//                return new at.searles.CplxVal(new Cplx().sin(a));
//            case cos:
//                return new at.searles.CplxVal(new Cplx().cos(a));
//            case tan:
//                return new at.searles.CplxVal(new Cplx().tan(a));
//            case atan:
//                return new at.searles.CplxVal(new Cplx().atan(a));
//            case sinh:
//                return new at.searles.CplxVal(new Cplx().sinh(a));
//            case cosh:
//                return new at.searles.CplxVal(new Cplx().cosh(a));
//            case tanh:
//                return new at.searles.CplxVal(new Cplx().tanh(a));
//            // fixme case atanh: return new Real(Math.atanh);
//            case floor:
//                return new at.searles.CplxVal(new Cplx().floor(a));
//            case ceil:
//                return new at.searles.CplxVal(new Cplx().ceil(a));
//            case fract:
//                return new at.searles.CplxVal(new Cplx().fract(a));
//            case re:
//                return new Real(a.re());
//            case im:
//                return new Real(a.im());
//            // fixme case mandelbrot: return new CplxVal(new Cplx().fract(a));
//            // case dot: return new Real(new Cplx().fract(a));
//            case rad2:
//                return new Real(a.rad2());
//            case rad:
//                return new Real(a.rad());
//            case arc:
//                return new Real(a.arc());
//            case arcnorm:
//                return new Real(a.arc() / (2 * Math.PI));
//
//            default:
//                return QuatVal.applyUnary(op, new Quat(a.re(), a.im(), 0, 0));
//        }
//    }
//
//
//    @Override
//    public Const apply(OpNew op, Const a1) {
//        return applyBinary(op, this.value, ((at.searles.CplxVal) a1).value);
//    }
//
//    @Override
//    public Const apply(OpNew op) {
//        return applyUnary(op, this.value);
//    }

    /*@Override
    public CplxVal cplxConst() {
        return this;
    }

    @Override
    public QuatVal quatConst() {
        return new QuatVal(new Quat(value.re, value.im, 0, 0));
    }*/
}
