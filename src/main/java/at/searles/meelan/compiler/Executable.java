package at.searles.meelan.compiler;

import at.searles.meelan.ops.InstructionSet;
import at.searles.meelan.optree.Call;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Reg;

import java.util.LinkedList;

public class Executable {

    private final Code code; // shared with scope.
    private int memoryOffset; // not shared.

    public Executable() {
        this(0, new Code());
    }

    private Executable(int memoryOffset, Code code) {
        this.code = code;
        this.memoryOffset = memoryOffset;
    }

    public void add(Label label) {
        code.add(label);
    }

    public void add(Call call) {
        code.add(call);
    }

    public String toString() {
        int line = 0;

        StringBuilder sb = new StringBuilder();

        int offset = 0;

        for(Call call : code.calls) {
            sb.append(String.format("%02d(%03d): %s\n", line++, offset, call));
            offset += call.intSize();
        }

        return sb.toString();
    }

    public IntCode createIntCode(InstructionSet instructionSet) {
        IntCode intCode = new IntCode(code.codeOffset);

        for(Call call : code.calls) {
            call.addIntCode(instructionSet, intCode);
        }

        return intCode;
    }

    public Reg createRegister(BaseType type) {
        if(type == null) {
            throw new NullPointerException("type must not be null");
        }

        Reg reg = new Reg(DummyInfo.getInstance(), memoryOffset, type); // TODO
        memoryOffset += type.size();
        return reg;
    }

    public Executable inner() {
        // code is shared.
        return new Executable(memoryOffset, code);
    }

    private static class Code {
        LinkedList<Call> calls;
        int codeOffset;

        Code() {
            this.calls = new LinkedList<>();
            this.codeOffset = 0;
        }

        void add(Label label) {
            label.initIndex(this.calls.size(), codeOffset);
        }

        public void add(Call call) {
            calls.add(call);
            codeOffset += call.intSize();
        }
    }
}
