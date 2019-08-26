package at.searles.meelan.optree.compiled;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.sys.Jump;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Label;
import at.searles.parsing.Fold;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class While extends Tree {

    public static final Mapping<Tree, Tree> CREATE = new Mapping<Tree, Tree>() {
        @Override
        public Tree parse(ParserStream stream, @NotNull Tree left) {
            return new While(stream.createSourceInfo(), left, null);
        }

        @Override
        public Tree left(@NotNull Tree result) {
            if(!(result instanceof While) || ((While) result).body != null) {
                return null;
            }

            return ((While) result).condition;
        }
    };

    public static final Fold<Tree, Tree, Tree> CREATE_DO = new Fold<Tree, Tree, Tree>() {
        @Override
        public Tree apply(ParserStream stream, @NotNull Tree left, @NotNull Tree right) {
            return new While(stream.createSourceInfo(), left, right);
        }

        @Override
        public Tree leftInverse(@NotNull Tree result) {
            if(!(result instanceof While) || ((While) result).body == null) {
                return null;
            }

            return ((While) result).condition;
        }

        @Override
        public Tree rightInverse(@NotNull Tree result) {
            if(!(result instanceof While) || ((While) result).body == null) {
                return null;
            }

            return ((While) result).body;
        }
    };

    private final Tree condition;
    private final Tree body; // may be null!

    public While(SourceInfo info, Tree condition, @Nullable Tree body) {
        super(info);

        if(condition == null) {
            throw new NullPointerException("condition must not be null");
        }

        this.condition = condition;
        this.body = body;
        assignType(BaseType.unit);
    }

    public String toString() {
        return String.format("while %s do %s", condition, body);
    }

    @Override
    public Stream<Tree> children() {
        return body != null ? Stream.of(condition, body) : Stream.of(condition);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        Tree ppCond = condition.preprocessor(table, resolver, frameBuilder);

        if(ppCond == null || ppCond.type() != BaseType.bool) {
            throw new MeelanException("condition is not a boolean", this);
        }

        Tree ppBody = body == null ? null : body.preprocessor(table, resolver, frameBuilder);

        return new While(this.sourceInfo(), ppCond, ppBody);
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        // two possible versions: one argument is like do-while, two arguments is a while.

        if(body == null) {
            Label trueLabel = new Label();
            Label falseLabel = new Label();

            program.add(trueLabel);

            condition.linearizeBool(trueLabel, falseLabel, program);

            program.add(falseLabel);
        } else {
            Label trueLabel = new Label();
            Label falseLabel = new Label();
            Label conditionLabel = new Label();

            // first, jump to the condition.
            program.add(Jump.get().createCall(conditionLabel));

            // but if it is true, we return here.
            program.add(trueLabel);

            body.linearizeStmt(program);

            // now the condition
            program.add(conditionLabel);

            condition.linearizeBool(trueLabel, falseLabel, program);

            program.add(falseLabel);
        }
    }
}
