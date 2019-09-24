package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.Block;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.parsing.Fold;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class Definition extends Tree {

    public static final Fold<String, Tree, Tree> CREATE = new Fold<String, Tree, Tree>() {
        @Override
        public Tree apply(ParserStream stream, @NotNull String left, @NotNull Tree right) {
            return new Definition(stream.createSourceInfo(), left, right);
        }

        @Override
        public String leftInverse(@NotNull Tree result) {
            return result instanceof Definition ? ((Definition) result).id : null;
        }

        @Override
        public Tree rightInverse(@NotNull Tree result) {
            return result instanceof Definition ? ((Definition) result).expr : null;
        }
    };

    private final String id;
    private final Tree expr;

    public Definition(SourceInfo info, String id, Tree expr) {
        super(info);
        this.id = id;
        // from the parsing process, expr is an expr and not
        // a stmt, hence no worries about the scope.
        this.expr = expr;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(expr);
    }

    public String toString() {
        return String.format("def %s: %s", id, expr);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        boolean undefinedOnTopLevel = table.add(id, new Context(sourceInfo(), table.snapshot(), expr));

        if(!undefinedOnTopLevel) {
            throw new MeelanException("already defined", this);
        }
        return null;
    }

}
