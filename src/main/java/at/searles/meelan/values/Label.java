package at.searles.meelan.values;

import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.types.BaseType;

/**
 * Class for labels in the program.
 * The position is set later.
 */
public class Label extends Const {

    private int index;
    private int codeIndex;

    public Label() {
        super(DummyInfo.getInstance(), BaseType.label);
        this.index = -1; // default for label meaning that it will be initialized later.
        this.codeIndex = -1;
    }

    public String toString() {
        return String.format("Label%02d(%03d)", index, codeIndex);
    }

    @Override
    public Value convertTo(BaseType type) {
        if(type != BaseType.label) {
            throw new IllegalArgumentException("cannot convert to label");
        }

        return this;
    }

    public void initIndex(int index, int codeIndex) {
        if(index < 0) {
            // If this happens, it is a bug.
            throw new IllegalArgumentException("i must be >= 0");
        }

        if(this.index != -1) {
            throw new IllegalArgumentException("Cannot double-set label");
        }

        this.index = index;
        this.codeIndex = codeIndex;
    }

    @Override
    public boolean isNum(int n) {
        return false;
    }

    @Override
    public SystemType systemType() {
        return SystemType.integer;
    }

    @Override
    public void addIntCode(IntCode code) {
        if(codeIndex == -1) {
            throw new IllegalArgumentException("uninitialized label");
        }

        code.add(codeIndex);
    }
}
