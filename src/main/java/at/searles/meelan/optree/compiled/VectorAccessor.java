package at.searles.meelan.optree.compiled;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.arithmetics.Mod;
import at.searles.meelan.ops.sys.JumpRel;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.Vec;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Int;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// This is only called from preprocessor.
public class VectorAccessor extends Tree {

    private final Tree index;
    private final Vec vector;

    public VectorAccessor(SourceInfo info, Tree index, Vec vector, BaseType type) {
        super(info);
        // this one is only called from preprocessor.
        this.index = index;
        this.vector = vector;
        assignType(type);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        return this;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(index, vector);
    }

    private List<Label> linearize(Executable program) throws MeelanException {
        if(index.type() != BaseType.integer) {
            throw new MeelanException("Not an integer", index);
        }

        // add modulo-instruction
        Value indexNormalized = Mod.get().linearizeExpr(Arrays.asList(index, new Int(vector.size())), null,
                BaseType.integer, program);

        // the modulo also makes sure that this is an integer!

        List<Label> labels = new ArrayList<>(vector.size());

        for(int i = 0; i < vector.size(); ++i) {
            labels.add(new Label());
        }

        // jumprelative is special
        JumpRel.get().addToProgram(indexNormalized, labels, program);

        return labels;
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        // this is the difference in all others
        List<Label> labels = linearize(program);
        vector.linearizeVectorStmt(labels, program);
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        List<Label> labels = linearize(program);
        return vector.linearizeVectorExpr(labels, target, program);
    }

    @Override
    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        List<Label> labels = linearize(program);
        vector.linearizeVectorBool(labels, trueLabel, falseLabel, program);
    }

    @Override
    public String toString() {
        return String.format("select %s in %s", this.index, this.vector);
    }
}
