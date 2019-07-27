package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;
import java.util.stream.Stream;

public class Context extends Tree {

    private final SymTable table;
    private final Tree tree;

    public Context(SourceInfo info, SymTable snapshot, Tree tree) {
        super(info);
        this.table = snapshot;
        this.tree = tree;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(tree);
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args, SymTable ignore, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        return tree.apply(sourceInfo(), args, this.table, resolver, frameBuilder);
    }

    @Override
    public Tree preprocessor(SymTable ignore, IdResolver resolver, Frame.Builder frameBuilder) {
        return tree.preprocessor(this.table, resolver, frameBuilder);
    }

    public String toString() {
        return String.format("context(%s, %s)", tree, table);
    }
}
