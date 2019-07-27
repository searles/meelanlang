package at.searles.meelan.ops.rewriting;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.optree.Derivable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Var;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;

/**
 * Syntax:
 * arg0: expression
 * arg1: variable as string
 * arg2: mapping [["x", 1], ["y", 2]]
 */
public class Derive extends Instruction {
    private static Derive singleton = null;

    public static Derive get() {
        if(singleton == null) {
            singleton = new Derive();
        }

        return singleton;
    }

    private Derive() {}

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        Tree expr = arguments.get(0);

        if (!(arguments.get(1) instanceof Var)) {
            throw new MeelanException("must be a variable", arguments.get(1));
        }

        Var var = (Var) arguments.get(1);

        return derive(expr, var);
    }

    private Tree derive(Tree expr, Var var) {
        if (!(expr instanceof Derivable)) {
            throw new MeelanException("not derivable", expr);
        }

        return ((Derivable) expr).derive(var);
    }
}
