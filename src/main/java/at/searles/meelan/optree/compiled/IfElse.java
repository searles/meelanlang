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
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.stream.Stream;

public class IfElse extends Tree {

    // order is (thenExpr, condition), elseExpr.
    private Tree condition;
    private Tree thenBranch;
    private Tree elseBranch;

    /**
     * @param elseBranch may be null for statements.
     */
    public IfElse(SourceInfo info, Tree condition, Tree thenBranch, Tree elseBranch) {
        super(info);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    /**
     * This constructor is important for IfElse-expressions because
     * it contains the common type.
     */
    public IfElse(SourceInfo info, Tree cond, Tree thenBranch, Tree elseBranch, BaseType type) {
        this(info, cond, thenBranch, elseBranch);
        assignType(type);
    }

    public String toString() {
        return String.format("if %s then %s else %s", condition, thenBranch, elseBranch);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        Tree inlineCond = condition.preprocessor(table, resolver, frameBuilder);

        if(inlineCond == null || inlineCond.type() != BaseType.bool) {
            throw new MeelanException("condition is not a boolean", condition);
        }

        if(inlineCond instanceof Bool) {
            if(((Bool) inlineCond).value) {
                return thenBranch.preprocessor(table, resolver, frameBuilder);
            } else if(elseBranch == null) {
                return null;
            } else {
                return elseBranch.preprocessor(table, resolver, frameBuilder);
            }
        }

        Tree thenInlined = thenBranch.preprocessor(table, resolver, frameBuilder);

        if(elseBranch == null) {
            return new IfElse(sourceInfo(), inlineCond, thenInlined, null, BaseType.unit);
        }

        // make types match in "then" and "else"

        Tree elseInlined = elseBranch.preprocessor(table, resolver, frameBuilder);

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
        return elseBranch != null ?
                Stream.of(condition, thenBranch, elseBranch) :
                Stream.of(condition, thenBranch);
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        Label trueLabel = new Label();
        Label falseLabel = new Label();

        condition.linearizeBool(trueLabel, falseLabel, program);

        program.add(trueLabel);
        thenBranch.linearizeStmt(program);

        if(elseBranch == null) {
            program.add(falseLabel);
            return;
        }

        Label endLabel = new Label();

        program.add(Jump.get().createCall(endLabel));

        program.add(falseLabel);

        elseBranch.linearizeStmt(program);

        program.add(endLabel);
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        // a = if cond then b else c

        // else must not be null. This is guaranteed from the parser.

        Label trueLabel = new Label(); // label for condition
        Label falseLabel = new Label(); // label for condition
        Label endLabel = new Label();

        condition.linearizeBool(trueLabel, falseLabel, program);

        program.add(trueLabel);

        if(target == null) {
            target = program.createRegister(type());
        }

        thenBranch.linearizeExpr(target, program); // FIXME inner (unit test!)?

        program.add(Jump.get().createCall(endLabel));

        program.add(falseLabel);

        elseBranch.linearizeExpr(target, program); // FIXME inner (unit test!)?

        program.add(endLabel);

        return target;
    }

    @Override
    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        Label condTrueLabel = new Label(); // label for condition
        Label condFalseLabel = new Label(); // label for condition

        // if condition is true then thenBranch.linearizeBool(true, false) else elseBranch.linearizeBool(true, false)
        condition.linearizeBool(condTrueLabel, condFalseLabel, program); // FIXME

        program.add(condTrueLabel);
        thenBranch.linearizeBool(trueLabel, falseLabel, program); // FIXME

        program.add(condFalseLabel);
        elseBranch.linearizeBool(trueLabel, falseLabel, program); // FIXME
    }
}
