package at.searles.meelan.ops.sys;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.symbols.*;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;

public class RaiseError extends Instruction {
    private static RaiseError singleton = null;

    public static RaiseError get() {
        if(singleton == null) {
            singleton = new RaiseError();
        }

        return singleton;
    }

    private RaiseError() {}

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> args, SymTable table, IdResolver resolver, Frame.Builder frameBuilder) throws MeelanException {
        throw new MeelanException(args.toString(), this);
    }
}
