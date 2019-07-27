package at.searles.meelan.rewriting;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.App;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Int;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.ArrayList;
import java.util.Map;

public interface Term {
    static Term v(String id) {
        return new V(id);
    }

    static Term a(Instruction fn, Term... args) {
        return new A(fn, args);
    }

    static Term n(int n) {
        return new N(n);
    }

    boolean match(Tree that, Map<String, Tree> matcher);
    Tree apply(SourceInfo info, Map<String, Tree> matcher) throws MeelanException;
}

class A implements Term {
    private final Instruction fn;
    private final Term[] args;

    A(Instruction fn, Term...args) {
        this.fn = fn;
        this.args = args;
    }

    public boolean match(Tree that, Map<String, Tree> matcher) {
        if(that instanceof App) {
            App app = (App) that;

            // Instructions must be singletons!
            if(fn.equals(app.head()) && app.args().size() == args.length) {
                int i = 0;

                for(Tree arg : app.args()) {
                    if(!args[i++].match(arg, matcher)) {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    public Tree apply(SourceInfo info, Map<String, Tree> matcher) throws MeelanException {
        ArrayList<Tree> args = new ArrayList<>(this.args.length);

        for (Term arg : this.args) {
            args.add(arg.apply(info, matcher));
        }

        return fn.apply(info, args);
    }
}

class N implements Term {
    private final int n;

    N(int n) {
        this.n = n;
    }

    @Override
    public boolean match(Tree that, Map<String, Tree> matcher) {
        return that instanceof Const && ((Const) that).isNum(n);
    }

    @Override
    public Tree apply(SourceInfo info, Map<String, Tree> matcher) throws MeelanException {
        return new Int(n);
    }
}

class V implements Term {
    private final String id;

    V(String id) {
        this.id = id;
    }

    public boolean match(Tree that, Map<String, Tree> matcher) {
        if(matcher.put(id, that) != null) {
            throw new IllegalArgumentException("rules must be LL");
        }

        return true;
    }

    public Tree apply(SourceInfo info, Map<String, Tree> matcher) {
        Tree ret = matcher.get(id);

        if(ret == null) {
            throw new IllegalArgumentException("missing: " + id);
        }

        return ret;
    }
}
