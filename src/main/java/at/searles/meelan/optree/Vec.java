package at.searles.meelan.optree;

import at.searles.meelan.*;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.sys.Jump;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.Mapping;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Vec is a special case and no value on its own.
 */
public class Vec extends Tree implements Iterable<Tree> {
    public static final Mapping<List<Tree>, Tree> CREATOR = new Mapping<List<Tree>, Tree>() {
        @Override
        public Tree parse(ParserStream stream, @NotNull List<Tree> left) {
            return new Vec(stream.createSourceInfo(), left);
        }

        @Override
        public List<Tree> left(@NotNull Tree result) {
            return result instanceof Vec ? ((Vec) result).values() : null;
        }
    };

    private List<Tree> ts;

    public Vec(SourceInfo info, List<Tree> ts) {
        super(info);
        this.ts = ts;
    }

    @Override
    public Stream<Tree> children() {
        return ts.stream();
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        List<Tree> ppArgs = new LinkedList<>();

        for(Tree t : ts) {
            // The only reason why this is not a problem is that
            // only one element of a vector is used at a time.
            Tree preprocessed = t.preprocessor(table, resolver, frameBuilder);

            if(preprocessed == null) {
                throw new MeelanException("Element in vector must not be a statement", t);
            }

            ppArgs.add(preprocessed);
        }

        return new Vec(sourceInfo(), ppArgs);
    }

    public Tree get(int i) {
        return ts.get(i);
    }

    public int size() {
        return ts.size();
    }

    public List<Tree> values() {
        return ts;
    }

    @NotNull
    @Override
    public Iterator<Tree> iterator() {
        return ts.iterator();
    }

    public String toString() {
        return ts.toString();
    }

    public Vec convertToMostCommonType() throws MeelanException {
        BaseType mostCommonType = null;

        for(Tree t : ts) {
            if(mostCommonType == null) {
                mostCommonType = t.type();
            } else if(mostCommonType.canConvertTo(t.type())) {
                mostCommonType = t.type();
            } else if(!t.type().canConvertTo(mostCommonType)) {
                throw new MeelanException("Incompatible types", this);
            }
        }

        List<Tree> typedVec = new LinkedList<>();

        for(int i = 0; i < size(); ++i) {
            typedVec.add(get(i).convertTo(mostCommonType));
        }

        return new Vec(sourceInfo(), typedVec);
    }

    public void linearizeVectorBool(List<Label> labels, Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        if (labels.size() != ts.size()) throw new IllegalArgumentException("amount of created labels is wrong");

        Iterator<Label> li = labels.iterator();
        Iterator<Tree> vi = ts.iterator();

        while (li.hasNext()) {
            program.add(li.next()); // add label
            vi.next().linearizeBool(trueLabel, falseLabel, program); // do statement
            // jump is not necessary because of true/false-label.
            // Op.__jump.addToProgram(Collections.singletonList(endLabel), currentScope, program); // and jump to end.
        }

        // no need for an end-label.
    }

    public Value linearizeVectorExpr(List<Label> labels, Reg target, Executable program) throws MeelanException {
        if (labels.size() != ts.size()) throw new IllegalArgumentException("amount of created labels is wrong");

        Iterator<Label> li = labels.iterator();
        Iterator<Tree> vi = ts.iterator();


        Label endLabel = new Label();

        while (li.hasNext()) {
            program.add(li.next()); // add label

            Tree current = vi.next();

            // get register for return value.
            if (target == null) {
                target = program.createRegister(type());
            }

            current.linearizeExpr(target, program); // do statement
            program.add(Jump.get().createCall(endLabel));
        }

        // FIXME Is this always using a new scope?

        program.add(endLabel);

        return target;
    }

    public void linearizeVectorStmt(List<Label> labels, Executable program) throws MeelanException {
        if (labels.size() != ts.size()) throw new IllegalArgumentException("amount of created labels is wrong");

        Iterator<Label> li = labels.iterator();
        Iterator<Tree> vi = ts.iterator();

        Label endLabel = new Label();

        while (li.hasNext()) {
            program.add(li.next()); // add label
            vi.next().linearizeStmt(program); // do statement
            program.add(Jump.get().createCall(endLabel));
        }

        program.add(endLabel);
    }
}
