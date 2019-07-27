package at.searles.meelan.ops.comparison;


import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.List;

public class Greater extends Instruction {
    // implement by converting it to Smaller.

    private static Greater singleton = null;

    public static Greater get() {
        if(singleton == null) {
            singleton = new Greater();
        }

        return singleton;
    }

    private Greater() {
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args) throws MeelanException {
        if(args.size() != 2) {
            return this;
        }

        return Less.get().apply(sourceInfo, Arrays.asList(args.get(1), args.get(0)));
    }
}
