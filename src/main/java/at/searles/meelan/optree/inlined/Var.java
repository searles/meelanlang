package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.optree.Derivable;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Int;
import at.searles.meelan.values.Reg;
import at.searles.meelan.values.Value;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.stream.Stream;

public class Var extends Tree implements Derivable {

    private final String id;
    private Reg reg;

    public Var(SourceInfo info, String id, BaseType type) {
        super(info);
        this.id = id;
        if(type != null) {
            assignType(type);
        }
    }

    @Override
    public Stream<Tree> children() {
        return Stream.empty();
    }

    @Override
    public Tree derive(Var var) {
        return new Int(var.id.equals(id) ? 1 : 0);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        // should not happen here.
        return this;
    }

    Reg allocate(Executable exec) {
        if(this.reg != null) {
            throw new IllegalArgumentException("already allocated this register.");
        }

        return this.reg = exec.createRegister(type());
    }

    @Override
    public Tree member(SourceInfo info, String memberId) throws MeelanException {
        BaseType memberType = type().memberType(memberId);

        if(memberType != null) {
            return new QualifiedMember(sourceInfo(), this, memberId);
        }

        return super.member(info, memberId);
    }

    @Override
    public Reg linearizeLValue(Executable program) throws MeelanException {
        if(reg == null) {
            throw new IllegalArgumentException("not allocated");
        }

        return this.reg;
    }

    @Override
    public Value linearizeExpr(Reg target, Executable program) throws MeelanException {
        if(reg == null) {
            throw new IllegalArgumentException("not allocated");
        }

        return reg.linearizeExpr(target, program);
    }

    public String toString() {
        return reg != null ?
                String.format("var(%s:%s)", id, reg) : String.format("var(%s)", id);
    }

    private static class QualifiedMember extends Tree {
        private Reg reg;
        private final Var var;
        private final String memberId;

        QualifiedMember(SourceInfo info, Var var, String memberId) {
            super(info);
            this.var = var;
            this.memberId = memberId;
            assignType(var.type().memberType(memberId));
        }

        @Override
        public Reg linearizeExpr(Reg target, Executable program) throws MeelanException {
            if(var.reg == null) {
                throw new NullPointerException("reg in var not set");
            }

            if(this.reg == null) {
                this.reg = new Reg(var.reg, memberId);
            }

            return this.reg.linearizeExpr(target, program);
        }

        @Override
        public Reg linearizeLValue(Executable program) throws MeelanException {
            return linearizeExpr(null, program);
        }

        @Override
        public Stream<Tree> children() {
            return Stream.empty();
        }

        @Override
        public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
            return this;
        }

        @Override
        public String toString() {
            return var.toString() + "::" + memberId;
        }
    }
}
