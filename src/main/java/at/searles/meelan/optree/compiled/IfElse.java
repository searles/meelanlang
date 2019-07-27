package at.searles.meelan.optree.compiled;

import at.searles.meelan.*;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.sys.Jump;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Bool;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.Environment;
import at.searles.parsing.Fold;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import at.searles.utils.GenericBuilder;
import at.searles.utils.Pair;

import java.util.stream.Stream;

public class IfElse extends Tree {

    // order is (thenExpr, condition), elseExpr.
    public static final Fold<Pair<Tree, Tree>, Tree, Tree> EXPR = new Fold<Pair<Tree, Tree>, Tree, Tree>() {

        @Override
        public Tree apply(Environment env, Pair<Tree, Tree> left, Tree right, ParserStream stream) {
            return new IfElse(stream.createSourceInfo(), left.r(), left.l(), right);
        }

        @Override
        public Pair<Tree, Tree> leftInverse(Environment env, Tree result) {
            if(!(result instanceof IfElse)) {
                return null;
            }

            IfElse ifElse = (IfElse) result;

            return new Pair<>(ifElse.thenPart, ifElse.cond);
        }

        @Override
        public Tree rightInverse(Environment env, Tree result) {
            if(!(result instanceof IfElse)) {
                return null;
            }

            IfElse ifElse = (IfElse) result;

            return ifElse.elsePart;
        }
    };

    public static final Fold<Pair<Tree, Tree>, Tree, Tree> STMT = new Fold<Pair<Tree, Tree>, Tree, Tree>() {
        @Override
        public Tree apply(Environment env, Pair<Tree, Tree> left, Tree right, ParserStream stream) {
            return new IfElse(stream.createSourceInfo(), left.l(), left.r(), right);
        }

        @Override
        public Pair<Tree, Tree> leftInverse(Environment env, Tree result) {
            if(!(result instanceof IfElse)) {
                return null;
            }

            return new Pair<>(((IfElse) result).cond, ((IfElse) result).thenPart);
        }

        @Override
        public Tree rightInverse(Environment env, Tree result) {
            if(!(result instanceof IfElse)) {
                return null;
            }

            return ((IfElse) result).elsePart;
        }
    };

    private Tree cond;
    private Tree thenPart;
    private Tree elsePart;

    /**
     * @param elsePart may be null for statements.
     */
    public IfElse(SourceInfo info, Tree cond, Tree thenPart, Tree elsePart) {
        super(info);
        this.cond = cond;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * This constructor is important for IfElse-expressions because
     * it contains the common type.
     */
    public IfElse(SourceInfo info, Tree cond, Tree thenPart, Tree elsePart, BaseType type) {
        this(info, cond, thenPart, elsePart);
        assignType(type);
    }

    public String toString() {
        return String.format("if %s then %s else %s", cond, thenPart, elsePart);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        Tree inlineCond = cond.preprocessor(table, resolver, frameBuilder);

        if(inlineCond == null || inlineCond.type() != BaseType.bool) {
            throw new MeelanException("condition is not a boolean", cond);
        }

        if(inlineCond instanceof Bool) {
            if(((Bool) inlineCond).value) {
                return thenPart.preprocessor(table, resolver, frameBuilder);
            } else if(elsePart == null) {
                return null;
            } else {
                return elsePart.preprocessor(table, resolver, frameBuilder);
            }
        }

        Tree thenInlined = thenPart.preprocessor(table, resolver, frameBuilder);

        if(elsePart == null) {
            return new IfElse(sourceInfo(), inlineCond, thenInlined, null);
        }

        // make types match in "then" and "else"

        Tree elseInlined = elsePart.preprocessor(table, resolver, frameBuilder);

        if(thenInlined.type().canConvertTo(elseInlined.type())) {
            thenInlined = thenInlined.convertTo(elseInlined.type());
        } else if(elseInlined.type().canConvertTo(thenInlined.type())) {
            elseInlined = elseInlined.convertTo(thenInlined.type());
        } else {
            throw new MeelanException("then and else return different types", this);
        }


        return new IfElse(sourceInfo(), inlineCond, thenInlined, elseInlined, thenInlined.type());
    }

    @Override
    public Stream<Tree> children() {
        return elsePart != null ?
                Stream.of(cond, thenPart, elsePart) :
                Stream.of(cond, thenPart);
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        Label trueLabel = new Label();
        Label falseLabel = new Label();

        cond.linearizeBool(trueLabel, falseLabel, program);

        program.add(trueLabel);
        thenPart.linearizeStmt(program);

        if(elsePart == null) {
            program.add(falseLabel);
            return;
        }

        Label endLabel = new Label();

        program.add(Jump.get().createCall(endLabel));

        program.add(falseLabel);

        elsePart.linearizeStmt(program);

        program.add(endLabel);
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        // a = if cond then b else c

        // else must not be null. This is guaranteed from the parser.

        Label trueLabel = new Label(); // label for condition
        Label falseLabel = new Label(); // label for condition
        Label endLabel = new Label();

        cond.linearizeBool(trueLabel, falseLabel, program);

        program.add(trueLabel);

        if(target == null) {
            target = program.createRegister(type());
        }

        thenPart.linearizeExpr(target, program); // FIXME inner (unit test!)?

        program.add(Jump.get().createCall(endLabel));

        program.add(falseLabel);

        elsePart.linearizeExpr(target, program); // FIXME inner (unit test!)?

        program.add(endLabel);

        return target;
    }

    @Override
    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        Label condTrueLabel = new Label(); // label for condition
        Label condFalseLabel = new Label(); // label for condition

        // if condition is true then thenPart.linearizeBool(true, false) else elsePart.linearizeBool(true, false)
        cond.linearizeBool(condTrueLabel, condFalseLabel, program); // FIXME

        program.add(condTrueLabel);
        thenPart.linearizeBool(trueLabel, falseLabel, program); // FIXME

        program.add(condFalseLabel);
        elsePart.linearizeBool(trueLabel, falseLabel, program); // FIXME
    }

    public static class Builder extends GenericBuilder<Builder, IfElse> {
        public Tree condition;
        public Tree thenPart;
        public Tree elsePart; // may be null

        @Override
        public IfElse build(Environment env, ParserStream stream) {
            return new IfElse(stream.createSourceInfo(), condition, thenPart, elsePart);
        }

        public static Builder toBuilder(IfElse ifElse) {
            Builder b = new Builder();
            b.condition = ifElse.cond;
            b.thenPart = ifElse.thenPart;
            b.elsePart = ifElse.elsePart;
            return b;
        }
    }
}
