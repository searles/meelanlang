package at.searles.meelan.ops.special;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.Vec;
import at.searles.meelan.values.Int;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;

/**
 * Created by searles on 01.07.18.
 */
public class Length extends Instruction {

    private static Length singleton = null;

    public static Length get() {
        if(singleton == null) {
            singleton = new Length();
        }

        return singleton;
    }

    private Length() {
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args) throws MeelanException {
        if(args.size() != 1 || !(args.get(0) instanceof Vec)) {
            throw new MeelanException("length requires one argument", this);
        }

        return new Int(((Vec) args.get(0)).size());
    }

}
