package at.searles.meelan.optree.compiled;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Reg;
import at.searles.parsing.Environment;
import at.searles.parsing.Fold;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class Assign extends Tree {
    public static final Fold<Tree, Tree, Tree> CREATE = new Fold<Tree, Tree, Tree>() {
        @Override
        public Tree apply(Environment env, ParserStream stream, @NotNull Tree left, @NotNull Tree right) {
            return new Assign(stream.createSourceInfo(), left, right);
        }

        @Override
        public Tree leftInverse(Environment env, @NotNull Tree result) {
            return result instanceof Assign ? ((Assign) result).lv : null;
        }

        @Override
        public Tree rightInverse(Environment env, @NotNull Tree result) {
            return result instanceof Assign ? ((Assign) result).rv : null;
        }
    };

    private Tree rv;
    private final Tree lv;

    public Assign(SourceInfo info, Tree lv, Tree rv) {
        super(info);
        this.lv = lv;
        this.rv = rv;
        assignType(BaseType.unit);
    }

    @Override
    public String toString() {
        return String.format("%s = %s", lv, rv);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        Tree inlinedRight = rv.preprocessor(table, resolver, frameBuilder);
        Tree inlinedLeft = lv.preprocessor(table, resolver, frameBuilder);

        if(inlinedLeft.type() != inlinedRight.type()) {
            inlinedRight = inlinedRight.convertTo(inlinedLeft.type());
        }

        return new Assign(sourceInfo(), inlinedLeft, inlinedRight);
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(lv, rv);
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        Reg reg = lv.linearizeLValue(program);

        rv.linearizeExpr(reg, program);
    }
}
