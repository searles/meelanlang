package at.searles.meelan.values;

import at.searles.commons.math.Quat;
import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;

public class QuatVal extends Const {

    private Quat value;

    public QuatVal(Quat value) {
        super(DummyInfo.getInstance(), BaseType.quat);
        this.value = value;
    }


    @Override
    public boolean isZero() {
        return value.s0() == 0.0 && value.s1() == 0.0 && value.s2() == 0.0 && value.s3() == 0.0;
    }

    @Override
    public boolean isOne() {
        return value.s0() == 1.0 && value.s1() == 0.0 && value.s2() == 0.0 && value.s3() == 0.0;
    }

    @Override
    public boolean isNum(int n) {
        return value.s0() == n && value.s1() == 0.0 && value.s2() == 0.0 && value.s3() == 0.0;
    }

    @Override
    public Value convertTo(BaseType type) {
        switch (type) {
            case quat:
                return this;
            default:
                throw new IllegalArgumentException(String.format("cannot convert quat to %s", type));
        }
    }

    @Override
    public SystemType systemType() {
        return SystemType.quat;
    }

    @Override
    public void addIntCode(IntCode code) {
        new Real(value.s0()).addIntCode(code);
        new Real(value.s1()).addIntCode(code);
        new Real(value.s2()).addIntCode(code);
        new Real(value.s3()).addIntCode(code);
    }

    public Quat value() {
        return value;
    }

    @Override
    public Value member(SourceInfo info, String id) throws MeelanException {
        switch (id) {
            case "a":
                return new Real(value.s0());
            case "b":
                return new Real(value.s1());
            case "c":
                return new Real(value.s2());
            case "d":
                return new Real(value.s3());
        }

        return super.member(info, id);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
