package at.searles.meelan.rewriting;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;

import java.util.LinkedList;

public class TRS {
    private LinkedList<Rule> rules;

    public TRS() {
        this.rules = new LinkedList<>();
    }

    public TRS add(Rule rule) {
        rules.add(rule);
        return this;
    }

    public Tree apply(Tree t) throws MeelanException {
        for(Rule rule : rules) {
            Tree result = rule.apply(t);

            if(result != null) {
                return result;
            }
        }

        return null;
    }
}