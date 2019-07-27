package at.searles.meelan.ops.special;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.Vec;
import at.searles.meelan.optree.compiled.VectorAccessor;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Int;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;

public class Select extends Instruction {

    private static Select singleton = null;

    public static Select get() {
        if(singleton == null) {
            singleton = new Select();
        }

        return singleton;
    }

    private Select() {
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args, SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        if(args.size() != 2) {
            throw new MeelanException("must have 2 arguments", this);
        }

        Tree ppIndex = args.get(0);
        Tree ppVector = args.get(1);

        if(ppIndex.type() != BaseType.integer) {
            throw new MeelanException("index must be an integer", args.get(0));
        }

        if(!(ppVector instanceof Vec)) {
            throw new MeelanException("not a vector", args.get(1));
        }

        Vec vec = (Vec) ppVector;

        if(vec.size() == 0) {
            throw new MeelanException("empty vector", args.get(1));
        }

        if(ppIndex instanceof Int) {
            int i = ((Int) ppIndex).value();

            i %= vec.size();
            if(i < 0) i = vec.size() - i;

            return vec.get(i);
        }

        Vec typedVec = vec.convertToMostCommonType();

        return new VectorAccessor(sourceInfo, ppIndex, typedVec, typedVec.get(0).type());
    }


}
