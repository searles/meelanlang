package at.searles.meelan.values;

import at.searles.commons.strings.StringQuote;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StringVal extends Const {

    public static final Mapping<CharSequence, Tree> TOK = new Mapping<CharSequence, Tree>() {
        @Override
        public Tree parse(ParserStream stream, CharSequence left) {
            return new StringVal(StringQuote.unquote(left.toString()));
        }

        @Override
        public CharSequence left(@NotNull Tree result) {
            return result instanceof StringVal ? StringQuote.quote(((StringVal) result).value()) : null;
        }
    };

    private final String value;

    public StringVal(String value) {
        super(DummyInfo.getInstance(), BaseType.string);
        this.value = value;
    }

    public String toString() {
        return "string[" + value + "]";
    }

    @Override
    public Value convertTo(BaseType type) {
        throw new IllegalArgumentException("cannot convert stringvals to " + type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringVal stringVal = (StringVal) o;
        return Objects.equals(value, stringVal.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean isNum(int n) {
        return false;
    }
}
