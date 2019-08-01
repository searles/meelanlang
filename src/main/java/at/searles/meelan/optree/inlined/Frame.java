package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Namespace. Replaces Block after preprocessing.
 */
public class Frame extends Tree {

    private List<Tree> stmts;
    private final List<Var> namespace;

    private Frame(SourceInfo info, BaseType type, List<Tree> stmts, List<Var> namespace) {
        super(info);
        this.stmts = stmts;
        this.namespace = namespace;
        assignType(type);
    }

    @Override
    public Stream<Tree> children() {
        return stmts.stream();
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Builder frameBuilder) {
        return this; // was already preprocessed.
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        boolean isFirst = true;

        for(Var var : namespace) {
            if(!isFirst) {
                sb.append(", ");
            } else {
                isFirst = false;
            }

            sb.append(var).append(": ").append(var.type());
        }

        sb.append("] {\n");

        for (Tree t : stmts) {
            String[] lines = t.toString().split("\n");// indent

            for(String line : lines) {
                sb.append("  ").append(line).append("\n");
            }
        }

        return sb.append("}\n").toString();
    }

    private Executable allocateRegisters(Executable exec) {
        Executable inner = exec.inner();
        namespace.forEach(var -> var.allocate(inner));

        return inner;
    }

    @Override
    public void linearizeStmt(Executable program) throws MeelanException {
        Executable inner = allocateRegisters(program);
        stmts.forEach(stmt -> stmt.linearizeStmt(inner));
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        Executable inner = allocateRegisters(program);

        Iterator<Tree> it = stmts.iterator();

        if(!it.hasNext()) {
            throw new MeelanException("Not an expression", this);
        }

        Tree tree = it.next();

        while(it.hasNext()) {
            tree.linearizeStmt(inner);
            tree = it.next(); // the last element must be returned
        }

        // the last one, assign it to a new register.
        // otherwise, we might mix up stuff.
        if(target == null && !(tree instanceof Value)) {
            target = program.createRegister(tree.type());
        }

        return tree.linearizeExpr(target, inner);
    }

    @Override
    public void linearizeBool(Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        Executable inner = allocateRegisters(program);

        Iterator<Tree> it = stmts.iterator();

        if(!it.hasNext()) {
            throw new MeelanException("Not a boolean", this);
        }

        Tree tree = it.next();

        while(it.hasNext()) {
            tree.linearizeStmt(inner);
            tree = it.next();
        }

        // last element. inner because of allocations in loops
        tree.linearizeBool(trueLabel, falseLabel, inner);
    }

    public static class Builder {
        private final SourceInfo sourceInfo;
        List<Tree> stmts;
        List<Var> vars;

        private Tree lastStmt;

        public Builder(SourceInfo sourceInfo) {
            this.sourceInfo = sourceInfo;
            this.stmts = new LinkedList<>();
            this.vars = new LinkedList<>();
        }

        Var declareVar(String id, BaseType type) {
            Var var = new Var(sourceInfo, id, type); // keep it for readability
            vars.add(var);
            return var;
        }

        public Builder inner(SourceInfo sourceInfo) {
            return new Builder(sourceInfo);
        }

        public Frame commit() {
            BaseType type = lastStmt != null ? lastStmt.type() : BaseType.unit;
            return new Frame(sourceInfo, type, stmts, vars);
        }

        public void addStmt(Tree stmt) {
            if(stmt != null) {
                stmts.add(stmt);
            }

            lastStmt = stmt;
        }
    }
}
