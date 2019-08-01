package at.searles.meelan.optree;

import at.searles.meelan.*;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.arithmetics.Mul;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.utils.ast.AstNode;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class Tree extends AstNode {

    // FIXME use head.applyArgs(App app) without preprocessing arguments
    // FIXME make While/IfElse instructions

    /**
     * Type set later during semantic analysis
     */
    private BaseType type = null;

    /**
     * Creates a tree with the given source info.
     * @param info
     */
    protected Tree(SourceInfo info) {
        super(info);
    }

    public abstract Stream<Tree> children();

    public Tree assignType(BaseType type) {
        if(this.type != null && this.type != type) {
            throw new MeelanException("Type could not be determined", this);
        }

        this.type = type;
        return this;
    }

    /**
     * Called by semantic analysis if supported.
     * @param type type of this Tree
     */
    public Tree convertTo(BaseType type) {
        if(this.type() == null) {
            throw new MeelanException("Type could not be determined", this);
        }

        if(this.type() == type) {
            return this;
        }

        return this.type().convertTo(this, type);
    }

    public BaseType type() {
        return type;
    }

    public abstract Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder);

    /**
     * Creates an application of this. Default is a normal multiplication (like in "2 x")
     * unless there is more than one argument (then it is an error).
     *
     * @param sourceInfo
     * @param args Arguments
     * @param table (optional) symbol table
     * @return Something equivalent to App(this, arg).
     * @throws MeelanException In case of an error.
     */
    public Tree apply(SourceInfo sourceInfo, List<Tree> args, SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        if(args.size() == 1) {
            // override to change this behavior.
            return Mul.get().apply(sourceInfo, Arrays.asList(this, args.get(0)));
        }

        throw new MeelanException("Not a valid function call", this);
    }

    public Tree member(SourceInfo info, String memberId) throws MeelanException {
        throw new MeelanException(String.format("No such member: %s", memberId), this);
    }



    public void linearizeStmt(Executable program) throws MeelanException {
        throw new MeelanException("Not a statement", this);
    }

    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        throw new MeelanException("Not an expression", this);
    }

    public Reg linearizeLValue(Executable program) throws MeelanException {
        throw new MeelanException("Not an L-Value", this);
    }

    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        throw new MeelanException("Not a bool", this);
    }

}
