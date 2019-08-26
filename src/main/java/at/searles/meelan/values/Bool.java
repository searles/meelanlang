package at.searles.meelan.values;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.sys.Jump;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Bool extends Const {
    public static final Mapping<CharSequence, Tree> TOK = new Mapping<CharSequence, Tree>() {
        @Override
        public Tree parse(ParserStream stream, CharSequence left) {
            return new Bool(Boolean.parseBoolean(left.toString()));
        }

        @Override
        public CharSequence left(@NotNull Tree result) {
            return result instanceof Bool ? Boolean.toString(((Bool) result).value()) : null;
        }
    };
    public boolean value;

    public Bool(boolean value) {
        super(DummyInfo.getInstance(), BaseType.bool);
        this.value = value;
    }

    public boolean value() {
        return value;
    }

    public String toString() {
        return "bool[" + value + "]";
    }

    @Override
    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        program.add(Jump.get().createCall(value ? trueLabel : falseLabel));
    }

    @Override
    public Value convertTo(BaseType type){
        if(type != BaseType.bool) {
            throw new IllegalArgumentException("cannot convert bool to anything");
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bool bool = (Bool) o;
        return value == bool.value;
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }

    @Override
    public boolean isNum(int n) {
        return false;
    }
}
