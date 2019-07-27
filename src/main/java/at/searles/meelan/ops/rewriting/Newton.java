package at.searles.meelan.ops.rewriting;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.arithmetics.Div;
import at.searles.meelan.ops.arithmetics.Sub;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.List;

public class Newton extends Instruction {
    private static Newton singleton = null;

    public static Newton get() {
        if(singleton == null) {
            singleton = new Newton();
        }

        return singleton;
    }

    private Newton() {}

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        Tree expr = arguments.get(0);
        Tree var = arguments.get(1);

        return Sub.get().apply(
                sourceInfo, Arrays.asList(var,
                        Div.get().apply(sourceInfo, Arrays.asList(expr,
                                Derive.get().apply(sourceInfo, Arrays.asList(expr, var))))));
    }
}
