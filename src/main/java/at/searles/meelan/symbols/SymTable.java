package at.searles.meelan.symbols;

import at.searles.meelan.optree.Tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymTable {

    private final SymTable parent;
    private final Map<String, Tree> content;

    public SymTable(SymTable parent) {
        this.parent = parent;
        this.content = new HashMap<>();
    }

    public SymTable() {
        this.parent = null;
        this.content = new HashMap<>();
    }

    /**
     * @return true if there was no other element on top level.
     */
    public boolean add(String id, Tree tree) {
        return content.put(id, tree) == null;
    }

    public Tree get(String id) {
        Tree val = content.get(id);
        return val == null && parent != null ? parent.get(id) : val;
    }

    public SymTable inner() {
        return new SymTable(this);
    }

    public SymTable snapshot() {
        SymTable snapshot = new SymTable();
        collectSymbols(snapshot);
        return snapshot;
    }

    private void collectSymbols(SymTable table) {
        if(parent != null) {
            parent.collectSymbols(table);
        }

        table.content.putAll(content);
    }

    public String toString() {
        String str = content.toString();

        if(parent != null) {
            return str + "-" + parent.toString();
        } else {
            return str;
        }
    }

    /**
     * Creates a sub-symbol table of parent and adds the parameters that are provided
     * in the other arguments. Must check before whether it is ok.
     * @throws IllegalArgumentException if the number of required arguments and the number of provided arguments is different.
     */
    public SymTable createCallTable(List<String> args, List<Tree> argValues) {
        if(args.size() != argValues.size()) {
            throw new IllegalArgumentException("bad number of arguments");
        }

        SymTable table = inner();

        for(int i = 0; i < args.size(); ++i) {
            table.add(args.get(i), argValues.get(i));
        }

        return table;
    }

    public Map<String, Tree> topLayer() {
        return content;
    }
}
