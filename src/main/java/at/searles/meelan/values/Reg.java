package at.searles.meelan.values;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.ops.sys.Mov;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;

/**
 * These instances of Value represent variables.
 */
public class Reg extends Value { // FIXME why should this be a value?

    private final int memoryOffset;

    public Reg(SourceInfo info, int memoryOffset, BaseType type) {
        super(info);
        this.memoryOffset = memoryOffset;
        assignType(type);
    }

    public Reg(Reg parent, String memberId) {
        this(parent.sourceInfo(), parent.type().memberOffset(memberId) + parent.memoryOffset, parent.type().memberType(memberId));
    }

    @Override
    public String toString() {
        return String.format("r%03d", memoryOffset);
    }

    @Override
    public Reg linearizeLValue(Executable program) throws MeelanException {
        return this;
    }

    @Override
    public SystemType systemType() {
        switch(type()) {
            case integer:
                return SystemType.integerReg;
            case real:
                return SystemType.realReg;
            case cplx:
                return SystemType.cplxReg;
            case quat:
                return SystemType.quatReg;
        }

        throw new IllegalArgumentException("could not determine system type");
    }

    @Override
    public Reg linearizeExpr(Reg target, Executable program) {
        if(target != null && target.memoryOffset != memoryOffset) {
            program.add(Mov.get().createAssignment(this, target));
            return target;
        }

        return this;
    }

    @Override
    public void addIntCode(IntCode code) {
        code.add(memoryOffset);
        code.updateDataSize(memoryOffset, type().size());
    }

    @Override
    public Reg accessMember(String memberId) {
        return new Reg(this, memberId);
    }
}
