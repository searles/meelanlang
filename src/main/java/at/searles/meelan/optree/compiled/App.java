package at.searles.meelan.optree.compiled;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.DerivableInstruction;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.TypedInstruction;
import at.searles.meelan.optree.Derivable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.optree.inlined.Id;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.symbols.*;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.Environment;
import at.searles.parsing.Fold;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App extends Tree implements Derivable {

    public static final Fold<Tree, List<Tree>, Tree> CREATOR = new Fold<Tree, List<Tree>, Tree>() {
        @Override
        public Tree apply(Environment env, ParserStream stream, Tree left, List<Tree> args) {
            return new App(stream.createSourceInfo(), left, args);
        }

        @Override
        public Tree leftInverse(Environment env, Tree result) {
            if(!(result instanceof App)) {
                return null;
            }

            App app = (App) result;

            if(app.head() instanceof Instruction) {
                // instructions cannot be parsed directly...
                return new Id(app.head.sourceInfo(), app.head().toString());
            }

            return app.head();
        }

        @Override
        public List<Tree> rightInverse(Environment env, Tree result) {
            if(!(result instanceof App)) {
                return null;
            }

            return ((App) result).args;
        }
    };

    public static final Fold<List<Tree>, Tree, List<Tree>> APPLY_TUPEL = new Fold<List<Tree>, Tree, List<Tree>>() {

        @Override
        public List<Tree> apply(Environment environment, ParserStream parserStream, List<Tree> args, Tree tree) {
            ArrayList<Tree> appArgs = new ArrayList<>(args.size());

            appArgs.addAll(args.stream().map(arg -> new App(parserStream.createSourceInfo(), arg, tree)).collect(Collectors.toList()));

            // case "a (b) c"
            return appArgs;
        }

        @Override
        public List<Tree> leftInverse(Environment env, List<Tree> result) {
            // it is handled by other rules
            return null;
        }

        @Override
        public Tree rightInverse(Environment env, List<Tree> result) {
            return null;
        }
    };

    private Tree head;
    private ArrayList<Tree> args;

    /**
     * Apps always have unknown type.
     * @param head operator
     * @param args arguments
     */
    public App(SourceInfo info, Tree head, List<Tree> args) {
        super(info);

        this.head = head;
        this.args = new ArrayList<>(args.size());
        this.args.addAll(args);
    }

    public App(SourceInfo info, Tree head, Tree...args) {
        this(info, head, Arrays.asList(args));
    }

    public Tree head() {
        return head;
    }

    public List<Tree> args() {
        return args;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.concat(Stream.of(head), args.stream());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder().append(head).append("(");

        boolean first = true;

        for (Tree arg : args) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(arg);
        }

        sb.append(")");

        return sb.toString();
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        // Resolve top symbol
        Tree ppHead = head.preprocessor(table, resolver, frameBuilder);

        if(ppHead == null) {
            throw new MeelanException("not a suitable head", head);
        }

        // inline arguments
        List<Tree> ppArgs = new LinkedList<>();

        for(Tree arg : args) {
            Tree ppArg = arg.preprocessor(table, resolver, frameBuilder);

            if(ppArg == null) {
                throw new MeelanException("not a valid argument", arg);
            }

            ppArgs.add(ppArg);
        }

        // create application.
        return ppHead.apply(sourceInfo(), ppArgs, table, resolver, frameBuilder);
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        ((TypedInstruction) head).linearizeStmt(args, program);
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        return ((TypedInstruction) head).linearizeExpr(args, target, type(), program);
    }

    @Override
    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        ((TypedInstruction) head).linearizeBool(args, trueLabel, falseLabel, program);
    }

    @Override
    public Tree derive(Var var) {
        if (head instanceof DerivableInstruction) {
            List<Tree> dargs = new LinkedList<>(); // we need the derivatives anyways

            for (Tree arg : args) {
                if (!(arg instanceof Derivable)) {
                    throw new MeelanException("cannot derive", arg);
                }

                Tree darg = ((Derivable) arg).derive(var);
                dargs.add(darg);
            }

            return ((DerivableInstruction) head).derive(sourceInfo(), var, args, dargs);
        }

        throw new MeelanException("not a derivable instruction", head);
    }
}
