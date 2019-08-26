package at.searles.meelan.optree.compiled;

import at.searles.meelan.*;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Block extends Tree implements Iterable<Tree> {

    public static final Mapping<List<Tree>, Tree> CREATOR = new Mapping<List<Tree>, Tree>() {

        @Override
        public Tree parse(ParserStream stream, @NotNull List<Tree> left) {
            return new Block(stream.createSourceInfo(), left);
        }

        @Override
        public List<Tree> left(@NotNull Tree result) {
            return result instanceof Block ? ((Block) result).stmts() : null;
        }
    };

    /**
     * The last element in the stmts is the value that is returned.
     */
    private List<Tree> stmts;

    public Block(SourceInfo info, List<Tree> stmts) {
        super(info);
        this.stmts = stmts;
    }

    @Override
    public Stream<Tree> children() {
        return stmts.stream();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");

        for (Tree t : stmts) {
            String[] lines = t.toString().split("\n");// indent

            for(String line : lines) {
                sb.append("  ").append(line).append("\n");
            }
        }

        return sb.append("}\n").toString();
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {

        SymTable innerTable = table.inner();
        Frame.Builder inner = frameBuilder.inner(this.sourceInfo());

        for(Tree stmt : stmts) {
            inner.addStmt(stmt.preprocessor(innerTable, resolver, inner));
        }

        return inner.commit();
    }

    @NotNull
    @Override
    public Iterator<Tree> iterator() {
        return stmts.iterator();
    }

    public List<Tree> stmts() {
        return stmts;
    }
}
