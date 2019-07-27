package at.searles.meelan.ops.comparison;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.bool.Not;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LessEq extends Instruction {

    private static LessEq singleton = null;

    public static LessEq get() {
        if(singleton == null) {
            singleton = new LessEq();
        }

        return singleton;
    }

    private LessEq() {
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args) throws MeelanException {
        if(args.size() != 2) {
            return this;
        }

        return Not.get().apply(sourceInfo, Collections.singletonList(Less.get().apply(sourceInfo, Arrays.asList(args.get(1), args.get(0)))));
    }
}
