package at.searles.meelan.optree;

import at.searles.meelan.optree.inlined.Var;

public interface Derivable {
    Tree derive(Var var);
}
