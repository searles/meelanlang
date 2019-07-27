package at.searles.meelan.values;

import at.searles.commons.math.Cplx;
import at.searles.commons.math.Quat;
import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.Environment;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Real extends Const /*implements RealConst, CplxConst, QuatConst*/ {

    public static final Mapping<CharSequence, Tree> TOK = new Mapping<CharSequence, Tree>() {
        @Override
        public Tree parse(Environment env, CharSequence left, ParserStream stream) {
            return new Real(Double.parseDouble(left.toString()));
        }

        @Override
        public CharSequence left(Environment env, @NotNull Tree result) {
            return result instanceof Real ? Double.toString(((Real) result).value()) : null;
        }
    };

    public static int[] dtoi(double d) {
        // TODO Move to commons
        long l = Double.doubleToRawLongBits(d);
        // beware of big endian systems [are there any?]
        return new int[]{(int) (l & 0x0ffffffffl), (int) (l >> 32)};
    }

    public static final Real ZERO = new Real(0);

    private double value;

    public Real(double value) {
        super(DummyInfo.getInstance(), BaseType.real);
        this.value = value;
    }

    public double value() {
        return value;
    }


    @Override
    public boolean isZero() {
        return value == 0.0;
    }

    @Override
    public boolean isOne() {
        return value == 1.0;
    }

    @Override
    public Value convertTo(BaseType type) {
        switch (type) {
            case real:
                return this;
            case cplx:
                return new CplxVal(new Cplx(value, 0));
            case quat:
                return new QuatVal(new Quat(value, 0, 0, 0));
            default:
                throw new IllegalArgumentException(String.format("cannot convert integer to %s", type));
        }
    }

    @Override
    public boolean isNum(int n) {
        return value == n;
    }

    @Override
    public SystemType systemType() {
        return SystemType.real;
    }

    @Override
    public void addIntCode(IntCode code) {
        code.add(dtoi(value));
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Real real = (Real) o;
        return Double.compare(real.value, value) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }

//    @Override
//    public int[] generateVMCode() {
//        return dtoi(value);
//    }
//
//    @Override
//    public String toString() {
//        return "real[" + value + "]";
//    }
//
//    static Const applyBinary(OpNew op, double a0, double a1) {
//        switch (op) {
//            case add:
//                return new at.searles.Real(a0 + a1);
//            case sub:
//                return new at.searles.Real(a0 - a1);
//            case mul:
//                return new at.searles.Real(a0 * a1);
//            case div:
//                return new at.searles.Real(a0 / a1);
//            case mod:
//                return new at.searles.Real(a0 % a1);
//            case pow:
//                return new at.searles.Real(Math.pow(a0, a1));
//            case min:
//                return new at.searles.Real(Math.min(a0, a1));
//            case max:
//                return new at.searles.Real(Math.max(a0, a1));
//            case g:
//                return new Bool(a0 > a1);
//            case ge:
//                return new Bool(a0 >= a1);
//            case eq:
//                return new Bool(a0 == a1);
//            case ne:
//                return new Bool(a0 != a1);
//            case le:
//                return new Bool(a0 <= a1);
//            case l:
//                return new Bool(a0 < a1);
//            default:
//                return CplxVal.applyBinary(op, new Cplx(a0), new Cplx(a1));
//        }
//    }
//
//    static Const applyUnary(OpNew op, double a) {
//        switch (op) {
//            case neg:
//                return new at.searles.Real(-a);
//            case recip:
//                return new at.searles.Real(1. / a);
//            case abs:
//                return new at.searles.Real(Math.abs(a));
//            case sqr:
//                return new at.searles.Real(a * a);
//            case sqrt:
//                return new at.searles.Real(Math.sqrt(a));
//            case log:
//                return new at.searles.Real(Math.log(a));
//            case exp:
//                return new at.searles.Real(Math.exp(a));
//            case sin:
//                return new at.searles.Real(Math.sin(a));
//            case cos:
//                return new at.searles.Real(Math.cos(a));
//            case tan:
//                return new at.searles.Real(Math.tan(a));
//            case atan:
//                return new at.searles.Real(Math.atan(a));
//            case sinh:
//                return new at.searles.Real(Math.sinh(a));
//            case cosh:
//                return new at.searles.Real(Math.cosh(a));
//            case tanh:
//                return new at.searles.Real(Math.tanh(a));
//            // fixme case atanh: return new Real(Math.atanh);
//            case floor:
//                return new at.searles.Real(Math.floor(a));
//            case ceil:
//                return new at.searles.Real(Math.ceil(a));
//            case fract:
//                return new at.searles.Real(a - Math.floor(a));
//            default:
//                return CplxVal.applyUnary(op, new Cplx(a, 0));
//        }
//    }
//
//
//    @Override
//    public Const apply(OpNew op, Const a1) {
//        return applyBinary(op, this.value, ((at.searles.Real) a1).value);
//    }
//
//    @Override
//    public Const apply(OpNew op) {
//        return applyUnary(op, this.value);
//    }

    /*@Override
    public Real realConst() {
        return this;
    }

    @Override
    public CplxVal cplxConst() {
        return new CplxVal(new Cplx(value, 0));
    }

    @Override
    public QuatVal quatConst() {
        return new QuatVal(new Quat(value, 0, 0, 0));
    }*/
}
