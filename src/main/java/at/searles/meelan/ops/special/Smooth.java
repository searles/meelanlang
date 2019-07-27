package at.searles.meelan.ops.special;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.arithmetics.Sub;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.values.Int;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Was an old Errorness function of fractal smoothing.
 * In version 4.0 replaced by the working one. Smooth was deprecated.
 */
public class Smooth extends Instruction {
    private static Smooth singleton = null;

    public static Smooth get() {
        if(singleton == null) {
            singleton = new Smooth();
        }

        return singleton;
    }

    private Smooth() {}

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        return Sub.get().apply(sourceInfo, Arrays.asList(Smoothen.get().apply(sourceInfo, arguments), new Int(1)));
    }
}
