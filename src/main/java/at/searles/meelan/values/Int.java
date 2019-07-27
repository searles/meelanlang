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

import java.util.Objects;

public class Int extends Const {

    public static final Mapping<CharSequence, Tree> NUM = new Mapping<CharSequence, Tree>() {
        @Override
        public Tree parse(Environment env, CharSequence left, ParserStream stream) {
            return new Int(Integer.parseInt(left.toString()));
        }

        @Override
        public CharSequence left(Environment env, Tree result) {
            return result instanceof Int ? Integer.toString(((Int) result).value) : null;
        }
    };

    public static final Mapping<CharSequence, Tree> HEX = new Mapping<CharSequence, Tree>() {
        @Override
        public Tree parse(Environment env, CharSequence left, ParserStream stream) {
            return new Int(hexColor(left));
        }

        @Override
        public CharSequence left(Environment env, Tree result) {
            return null; // covered by NUM
        }
    };

    private int value;

    public Int(int value) {
        super(DummyInfo.getInstance(), BaseType.integer); // TODO
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public Value convertTo(BaseType type) {
        switch (type) {
            case integer:
                return this;
            case real:
                return new Real(value);
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
        return SystemType.integer;
    }

    @Override
    public void addIntCode(IntCode code) {
        code.add(value);
    }

    @Override
    public boolean isZero() {
        return value == 0;
    }

    @Override
    public boolean isOne() {
        return value == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Int anInt = (Int) o;
        return value == anInt.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private static int hexColor(CharSequence charSequence) {
        // some tricks
        int r, g, b, a = 0xff;

        switch(charSequence.length()) {
            case 2: r = g = b = hex(charSequence.charAt(1)) * 0x11; break;
            case 3: r = g = b = hex(charSequence.charAt(1)) * 0x10 + hex(charSequence.charAt(2)); break;
            case 4:
                r = hex(charSequence.charAt(1)) * 0x11;
                g = hex(charSequence.charAt(2)) * 0x11;
                b = hex(charSequence.charAt(3)) * 0x11;
                break;
            case 5:
                a = hex(charSequence.charAt(1)) * 0x11; // alpha
                r = hex(charSequence.charAt(2)) * 0x11;
                g = hex(charSequence.charAt(3)) * 0x11;
                b = hex(charSequence.charAt(4)) * 0x11;
                break;
            case 6:
                a = hex(charSequence.charAt(1)) * 0x10 + hex(charSequence.charAt(2)); // alpha
                r = hex(charSequence.charAt(3)) * 0x11;
                g = hex(charSequence.charAt(4)) * 0x11;
                b = hex(charSequence.charAt(5)) * 0x11;
                break;
            case 7:
                r = hex(charSequence.charAt(1)) * 0x10 + hex(charSequence.charAt(2));
                g = hex(charSequence.charAt(3)) * 0x10 + hex(charSequence.charAt(4));
                b = hex(charSequence.charAt(5)) * 0x10 + hex(charSequence.charAt(6));
                break;
            case 8:
                a = hex(charSequence.charAt(1)) * 0x11; // alpha
                r = hex(charSequence.charAt(2)) * 0x10 + hex(charSequence.charAt(3));
                g = hex(charSequence.charAt(4)) * 0x10 + hex(charSequence.charAt(5));
                b = hex(charSequence.charAt(6)) * 0x10 + hex(charSequence.charAt(7));
                break;
            case 9:
                a = hex(charSequence.charAt(7)) * 0x10 + hex(charSequence.charAt(8));
                r = hex(charSequence.charAt(1)) * 0x10 + hex(charSequence.charAt(2));
                g = hex(charSequence.charAt(3)) * 0x10 + hex(charSequence.charAt(4));
                b = hex(charSequence.charAt(5)) * 0x10 + hex(charSequence.charAt(6));
                break;
            default:
                throw new IllegalArgumentException("error in toHex! case for " + charSequence);
        }
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int hex(char ch) {
        if(ch <= '9') return ch - '0';
        else if(ch <= 'F') return ch - 'A' + 10;
        else return ch - 'a' + 10;
    }
}
