package at.searles.meelan.ops;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.App;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.Environment;
import at.searles.parsing.Fold;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Instruction extends Tree {

    protected Instruction() {
        super(new DummyInfo());
    }

    @Override
    public Stream<Tree> children() {
        return Stream.empty();
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        return this;
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args, SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        return apply(sourceInfo, args);
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        throw new MeelanException("not linearizable", this);
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        throw new MeelanException("not linearizable", this);
    }

    @Override
    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        throw new MeelanException("not linearizable", this);
    }

    public static List<Value> linearizeValues(List<Tree> args, Executable program) throws MeelanException {
        return args.stream().map(arg -> arg.linearizeExpr(null, program)).collect(Collectors.toList());
    }

    public void linearizeStmt(List<Tree> args, Executable program) throws MeelanException {
        throw new MeelanException("cannot linearize stmt", this);
    }

    public Value linearizeExpr(List<Tree> args, Reg target, BaseType targetType, Executable program) throws MeelanException {
        throw new MeelanException("cannot linearize expr", this);
    }

    public void linearizeBool(List<Tree> args, Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        throw new MeelanException("cannot linearize bool", this);
    }


    /**
     * Allows for simplifications of an application of this app. Overwrite this one
     * for optimizations
     */
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        return new App(sourceInfo, this, arguments);
    }

    public String toString() {
        return getClass().getSimpleName();
    }

    public static Mapping<Tree, Tree> unary(Instruction instruction) {
        return new Mapping<Tree, Tree>() {
            @Override
            public Tree parse(Environment env, ParserStream stream, @NotNull Tree left) {
                return new App(stream.createSourceInfo(), instruction, Collections.singletonList(left)); // todo check
            }

            @Override
            public Tree left(Environment env, @NotNull Tree result) {
                if(!(result instanceof App)) {
                    return null;
                }

                App app = (App) result;

                if(!app.head().equals(instruction)) {
                    return null;
                }

                if(app.args().size() != 1) {
                    return null;
                }

                return app.args().get(0);
            }

            @Override
            public String toString() {
                return "{" + instruction + "/1}";
            }
        };
    }

    public static Fold<Tree, Tree, Tree> binary(Instruction instruction) {
        return new Fold<Tree, Tree, Tree>() {
            @Override
            public Tree apply(Environment env, ParserStream stream, Tree left, Tree right) {
                return new App(stream.createSourceInfo(), instruction, Arrays.asList(left, right));
            }

            private boolean canInvert(Tree tree) {
                return
                        (tree instanceof App)
                                && ((App) tree).head().equals(instruction)
                                && ((App) tree).args().size() == 2;
            }

            @Override
            public Tree leftInverse(Environment env, Tree result) {
                return canInvert(result) ? ((App) result).args().get(0) : null;
            }

            @Override
            public Tree rightInverse(Environment env, Tree result) {
                return canInvert(result) ? ((App) result).args().get(1) : null;
            }

            @Override
            public String toString() {
                return "{" + instruction + "/2}";
            }
        };
    }

    public static Mapping<List<Tree>, Tree> app(Instruction instr) {
        return new Mapping<List<Tree>, Tree>() {
            @Override
            public Tree parse(Environment env, ParserStream stream, @NotNull List<Tree> left) {
                return new App(stream.createSourceInfo(), instr, left); // TODO check
            }

            @Override
            public List<Tree> left(Environment env, @NotNull Tree result) {
                if(!(result instanceof App)) {
                    return null;
                }

                if(!((App) result).head().equals(instr)) {
                    return null;
                }

                return ((App) result).args();
            }

            @Override
            public String toString() {
                return "{" + instr + "}";
            }
        };
    }

    private static class DummyInfo implements SourceInfo {
        @Override
        public long end() {
            return 0;
        }

        @Override
        public long start() {
            return 0;
        }
    }
}
