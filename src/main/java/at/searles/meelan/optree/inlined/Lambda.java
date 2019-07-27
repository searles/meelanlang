package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;
import java.util.stream.Stream;

public class Lambda extends Tree {

    private final List<String> args;
    private final Tree body;

    public Lambda(SourceInfo info, List<String> args, Tree body) {
        super(info);
        this.args = args;
        this.body = body;
        assignType(BaseType.unit);
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(body);
    }

    public Tree body() {
        return body;
    }

    public List<String> args() {
        return args;
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        // preprocessing only happens when it is applied.
        return new Context(sourceInfo(), table.snapshot(), this);
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args, SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        if(args.size() != this.args.size()) {
            throw new MeelanException("bad number of arguments", this);
        }

        return this.body.preprocessor(table.createCallTable(this.args, args), resolver, frameBuilder);
    }

    @Override
    public String toString() {
        return "lambda " + args + ": " + body;
    }
}
