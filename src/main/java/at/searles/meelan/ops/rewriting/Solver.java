package at.searles.meelan.ops.rewriting;

import at.searles.meelan.ops.Instruction;

/**
 * Created by searles on 14.02.18.
 */
public class Solver extends Instruction {
    private static Solver singleton = null;

    public static Solver get() {
        if(singleton == null) {
            singleton = new Solver();
        }

        return singleton;
    }

    private Solver() {}
}
