package at.searles.meelan.ops.bool;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Or extends Instruction {

    private static final int ARITY = 2;

    private static Or singleton = null;

    public static Or get() {
        if(singleton == null) {
            singleton = new Or();
        }

        return singleton;
    }

    private Or() {}

    private Tree n(Tree t) throws MeelanException {
        return Not.get().apply(t.sourceInfo(), Collections.singletonList(t));
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        if(arguments.size() != ARITY) {
            return super.apply(sourceInfo, arguments);
        }

        // De Morgan
        return n(And.get().apply(sourceInfo, Arrays.asList(n(arguments.get(0)), n(arguments.get(1)))));
    }
}
