package at.searles.meelan.ops;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.FunctionType;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class BinaryInstruction extends SystemInstruction {
    public BinaryInstruction(FunctionType...functionTypes) {
        super(functionTypes);
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        if(arguments.size() > 2) {
            Iterator<Tree> it = arguments.iterator();

            Tree left = it.next();

            while(it.hasNext()) {
                left = super.apply(sourceInfo, Arrays.asList(left, it.next()));
            }

            return left;
        }

        return super.apply(sourceInfo, arguments);
    }
}
