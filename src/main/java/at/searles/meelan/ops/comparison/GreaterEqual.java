package at.searles.meelan.ops.comparison;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.bool.Not;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by searles on 09.12.17.
 */
public class GreaterEqual extends Instruction {

    private static GreaterEqual singleton = null;

    public static GreaterEqual get() {
        if(singleton == null) {
            singleton = new GreaterEqual();
        }

        return singleton;
    }

    private GreaterEqual() {
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args) throws MeelanException {
        if(args.size() != 2) {
            return this;
        }

        return Not.get().apply(sourceInfo, Collections.singletonList(Less.get().apply(sourceInfo, Arrays.asList(args.get(0), args.get(1)))));
    }
}