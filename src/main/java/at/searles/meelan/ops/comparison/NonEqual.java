package at.searles.meelan.ops.comparison;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.bool.Not;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Collections;
import java.util.List;

/**
 * Created by searles on 09.12.17.
 */
public class NonEqual extends Instruction {

    private static NonEqual singleton = null;

    public static NonEqual get() {
        if(singleton == null) {
            singleton = new NonEqual();
        }

        return singleton;
    }

    private NonEqual() {}

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        return Not.get().apply(sourceInfo, Collections.singletonList(Equal.get().apply(sourceInfo, arguments)));
    }
}
