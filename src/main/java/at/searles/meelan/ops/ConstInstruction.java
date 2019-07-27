package at.searles.meelan.ops;

import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.values.Const;

public class ConstInstruction extends Instruction {
    private final Const value;

    public ConstInstruction(Const value) {
        this.value = value;
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        return value;
    }
}
