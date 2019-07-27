package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;
import java.util.stream.Stream;

/**
 * This is a class declaration. By inlining it it is combined with the symbol table of the current scope
 * into a ClassClosure.
 */
public class Template extends Tree {

    private final List<String> args;
    private final List<Tree> body;

    public Template(SourceInfo info, List<String> args, List<Tree> body) {
        super(info);
        this.args = args;
        this.body = body;
    }

    @Override
    public Stream<Tree> children() {
        return body().stream();
    }

    @Override
    public String toString() {
        return String.format("template(%s): %s", args, body);
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args, SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        // Table is usually passed from a Context object
        SymTable objectTable = table.createCallTable(args(), args);

        for(Tree tree : body) {
            // no inner varCounter because we need to allocate it before.
            if(tree.preprocessor(objectTable, resolver, frameBuilder) != null) {
                throw new MeelanException("No expressions allowed", tree);
            }
        }

        return new ObjectNode(sourceInfo(), objectTable.topLayer());
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        // preprocessing only happens when it is applied.
        return new Context(sourceInfo(), table.snapshot(), this);
    }

    public List<String> args() {
        return args;
    }

    public List<Tree> body() {
        return body;
    }
}
