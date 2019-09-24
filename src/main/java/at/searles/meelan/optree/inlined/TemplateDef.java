package at.searles.meelan.optree.inlined;

import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.Block;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;
import java.util.stream.Stream;

public class TemplateDef extends Tree {
    private final String id;
    private final List<String> args;
    private final Tree body;

    public TemplateDef(SourceInfo info, String id, List<String> args, Tree body) {
        super(info);
        this.id = id;
        this.args = args;
        this.body = body;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(body);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        Template t = new Template(sourceInfo(), args, ((Block) body).stmts());
        return new Definition(sourceInfo(), id, t)
                .preprocessor(table, resolver, frameBuilder);
    }
}
