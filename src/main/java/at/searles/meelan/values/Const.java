package at.searles.meelan.values;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;

/**
 * Represents constants, the counterpart of registers.
 */
public abstract class Const extends Value {

    protected Const(SourceInfo info, BaseType type) {
        super(info);
        if(type == null) {
            throw new NullPointerException();
        }

        assignType(type);
    }

    public abstract boolean isNum(int n);

    public SystemType systemType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value member(SourceInfo info, String memberId) throws MeelanException {
        return (Value) super.member(info, memberId);
    }

    @Override
    public Value accessMember(String memberId) {
        try {
            return member(sourceInfo(), memberId);
        } catch (MeelanException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isZero() {
        return false;
    }

    public boolean isOne() {
        return false;
    }
}
