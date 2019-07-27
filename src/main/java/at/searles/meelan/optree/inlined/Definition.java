package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.Block;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.parsing.Environment;
import at.searles.parsing.Fold;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import at.searles.utils.GenericBuilder;

import java.util.List;
import java.util.stream.Stream;

public class Definition extends Tree {

    public static final Fold<String, Tree, Tree> CREATE = new Fold<String, Tree, Tree>() {
        @Override
        public Tree apply(Environment env, String left, Tree right, ParserStream stream) {
            return new Definition(stream.createSourceInfo(), left, right);
        }

        @Override
        public String leftInverse(Environment env, Tree result) {
            return result instanceof Definition ? ((Definition) result).id : null;
        }

        @Override
        public Tree rightInverse(Environment env, Tree result) {
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

    public static class FuncBuilder extends GenericBuilder<FuncBuilder, Definition> {
        public String id;
        public List<String> args;
        public Tree body;

        public Definition build(Environment env, ParserStream parserStream) {
            Lambda l = new Lambda(parserStream.createSourceInfo(), args, body);
            return new Definition(parserStream.createSourceInfo(), id, l);
        }

        public static FuncBuilder toBuilder(Definition def) {
            if(!(def.expr instanceof  Lambda)) {
                return null;
            }

            Lambda l = (Lambda) def.expr;

            FuncBuilder b = new FuncBuilder();
            b.id = def.id;
            b.args = l.args();
            b.body = l.body();

            return b;
        }
    }

    public static class TemplateBuilder extends GenericBuilder<TemplateBuilder, Definition> {
        public String id;
        public List<String> args;
        public Tree body;

        public Definition build(Environment env, ParserStream parserStream) {
            Template t = new Template(parserStream.createSourceInfo(), args, ((Block) body).stmts());
            return new Definition(parserStream.createSourceInfo(), id, t);

        }

        public static TemplateBuilder toBuilder(Definition def) {
            if(!(def.expr instanceof Template)) {
                return null;
            }

            Template t = (Template) def.expr;

            TemplateBuilder b = new TemplateBuilder();
            b.id = def.id;
            b.args = t.args();
            b.body = new Block(DummyInfo.getInstance(), t.body());
            return b;
        }
    }
}
