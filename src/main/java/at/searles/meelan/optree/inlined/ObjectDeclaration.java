package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.parsing.Fold;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ObjectDeclaration extends Tree {
    public static final Fold<String, Tree, Tree> CREATE = new Fold<String, Tree, Tree>() {
        @Override
        public Tree apply(ParserStream stream, @NotNull String left, @NotNull Tree right) {
            return new ObjectDeclaration(stream.createSourceInfo(), left, right);
        }

        @Override
        public String leftInverse(@NotNull Tree result) {
            if(!(result instanceof ObjectDeclaration)) {
                return null;
            }

            return ((ObjectDeclaration) result).id;
        }

        @Override
        public Tree rightInverse(@NotNull Tree result) {
            if(!(result instanceof ObjectDeclaration)) {
                return null;
            }

            return ((ObjectDeclaration) result).init;
        }
    };

    private String id;
    private Tree init; // this is an expr

    public ObjectDeclaration(SourceInfo sourceInfo, String id, Tree init) { // expr may be null
        super(sourceInfo);
        this.id = id;
        this.init = init;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(init);
    }

    public String toString() {
        return "object " + id + " = " + init;
    }


    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        Tree preprocessed = init.preprocessor(table, resolver, frameBuilder);

        if(preprocessed == null) {
            throw new MeelanException("not an objectable element", init);
        }

        table.add(id, preprocessed);

        return null;
    }
}
