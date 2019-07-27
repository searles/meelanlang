package at.searles.meelan.optree;

import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.ops.InstructionSet;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.values.Value;

import java.util.List;

/**
 * Instances of this class are created during linearization.
 */
public class Call {

    private SystemInstruction op;
    private List<Value> args;

    private int systemTypeIndex;

    private int intSize;

    public static Call createCall(SystemInstruction op, List<Value> args) {
        int systemTypeIndex = op.findMatchingSystemTypeIndex(args);

        if (systemTypeIndex == -1) {
            return null; // FIXME here
        }

        return new Call(op, args, systemTypeIndex);
    }

    public Call(SystemInstruction op, List<Value> args, int systemTypeIndex) {
        this.op = op;
        this.args = args;

        this.systemTypeIndex = systemTypeIndex;

        // initialize int size
        intSize = 1;

        for(Value arg : args) {
            intSize += arg.systemType().size();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(op).append("(");

        boolean first = true;
        for (Tree arg : args) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(arg);
        }

        sb.append(")");

        return sb.toString();
    }

    /**
     * Returns the space occupied by this call.
     * @return the number of int elements
     */
    public int intSize() {
        return intSize;
    }

    /**
     * Returns the case index of this call if the given instruction set is used.
     * @param instructionSet The instruction set that was used to create the vm
     * @return the case index.
     */
    private int caseIndex(InstructionSet instructionSet) {
        return instructionSet.offset(this.op) + systemTypeIndex;
    }

    public void addIntCode(InstructionSet instructionSet, IntCode code) {
        code.add(caseIndex(instructionSet));

        for(Value arg : args) {
            arg.addIntCode(code);
        }
    }
}
