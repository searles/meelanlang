package at.searles.meelan.ops.rewriting;

import at.searles.meelan.ops.Instruction;

/**
 * Created by searles on 14.02.18.
 */
public class Horner extends Instruction {
    private static Horner singleton = null;

    public static Horner get() {
        if(singleton == null) {
            singleton = new Horner();
        }

        return singleton;
    }

    private Horner() {}
}
