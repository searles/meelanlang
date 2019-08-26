package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.Fold;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * This one is the '.'-operator, like the one in objects or structs
 */
public class Qualified extends Tree {

    public static final Fold<Tree, String, Tree> CREATOR = new Fold<Tree, String, Tree>() {
        @Override
        public Tree apply(ParserStream stream, @NotNull Tree left, @NotNull String right) {
            return new Qualified(stream.createSourceInfo(), left, right);
        }

        private boolean cannotInvert(Tree result) {
            return !(result instanceof Qualified);
        }

        @Override
        public Tree leftInverse(@NotNull Tree result) {
            if(cannotInvert(result)) {
                return null;
            }

            return ((Qualified) result).expr();
        }

        @Override
        public String rightInverse(@NotNull Tree result) {
            if(cannotInvert(result)) {
                return null;
            }

            return ((Qualified) result).memberId;
        }
    };

    private Tree expr;
    private String memberId;

    public Qualified(SourceInfo info, Tree expr, String memberId) {
        super(info);
        this.expr = expr;
        this.memberId = memberId;
    }

    public Tree expr() {
        return expr;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(expr);
    }

    public String memberId() {
        return memberId;
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        Tree inlineExpr = expr.preprocessor(table, resolver, frameBuilder);
        return inlineExpr.member(sourceInfo(), memberId);
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        Value value = expr.linearizeExpr(null, program);
        return value.accessMember(memberId);
    }

    @Override
    public Reg linearizeLValue(Executable program) throws MeelanException {
        Reg reg = expr.linearizeLValue(program);
        return reg.accessMember(memberId);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", expr, memberId);
    }
}
