package at.searles.meelan.values;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.ops.sys.Mov;
import at.searles.meelan.optree.Derivable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.symbols.*;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.stream.Stream;

/**
 * This class represents (possibly read-only-) values that can be encoded as an integer.
 */
public abstract class Value extends Tree implements Derivable {

	protected Value(SourceInfo info) {
        super(info);
    }

    @Override
    public Stream<Tree> children() {
        return Stream.empty();
    }

    @Override
    public Tree derive(Var var) {
        return new Int(0);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        return this;
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) {
        if(target == null) {
            return this;
        }

        program.add(Mov.get().createAssignment(this, target));

        return target;
    }

    public abstract SystemType systemType();

    /**
     * Adds this to the given array list
     * @param code The list to which the code is added to.
     */
    public void addIntCode(IntCode code) {
        throw new UnsupportedOperationException();
    }

    public abstract Value accessMember(String memberId);
}
