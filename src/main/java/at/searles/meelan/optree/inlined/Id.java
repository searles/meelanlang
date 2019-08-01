package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.parsing.Environment;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

public class Id extends Tree {

    public static final Mapping<CharSequence, Tree> TOK = new Mapping<CharSequence, Tree>() {
        @Override
        public Tree parse(Environment env, ParserStream stream, CharSequence left) {
            return new Id(stream.createSourceInfo(), left.toString());
        }

        @Override
        public CharSequence left(Environment env, @NotNull Tree result) {
            return result instanceof Id ? ((Id) result).id : null;
        }

        @Override
        public String toString() {
            return "{id}";
        }
    };

    public String id;

    public Id(SourceInfo sourceInfo, String id) {
        super(sourceInfo);
        this.id = id;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.empty();
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Id && Objects.equals(((Id) o).id, id);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        Tree value = table.get(id);

        if(value == null) {
            value = resolver.valueOf(id);
        }

        if(value == null) {
            throw new MeelanException("Not defined", this);
        }

        return value.preprocessor(table, resolver, frameBuilder);
    }
}
