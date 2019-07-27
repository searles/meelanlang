package at.searles.meelan.rewriting;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;

import java.util.HashMap;
import java.util.Map;

public class Rule {
    private final Term lhs;
    private final Term rhs;

    public Rule(Term lhs, Term rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    protected boolean condition(Map<String, Tree> matcher) {
        return true;
    }

    public Tree apply(Tree t) throws MeelanException {
        Map<String, Tree> matcher = new HashMap<>();

        if(lhs.match(t, matcher) && condition(matcher)) {
            return rhs.apply(t.sourceInfo(), matcher);
        }

        return null;
    }
}
