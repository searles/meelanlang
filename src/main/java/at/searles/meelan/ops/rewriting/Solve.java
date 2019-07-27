package at.searles.meelan.ops.rewriting;

import at.searles.meelan.ops.Instruction;

/**
 * Created by searles on 14.02.18.
 */
public class Solve extends Instruction {
    private static Solve singleton = null;

    public static Solve get() {
        if(singleton == null) {
            singleton = new Solve();
        }

        return singleton;
    }

    private Solve() {}
}
