package at.searles.meelan.ops;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class InstructionSet {

    private HashMap<String, Instruction> instructions;

    private LinkedHashMap<SystemInstruction, Integer> sysInstructionOffsets;
    private int sysInstructionSize;

    public InstructionSet() {
        this.sysInstructionSize = 0;
        this.sysInstructionOffsets = new LinkedHashMap<>();
        this.instructions = new HashMap<>();
    }

    public Instruction get(String id) {
        return instructions.get(id);
    }

    public void addInstruction(String id, Instruction instruction) {
        if(instructions.put(id, instruction) != null) {
            throw new IllegalArgumentException("already defined: " + id);
        }
    }

    public void addSystemInstruction(String id, SystemInstruction instruction) {
        addSystemInstruction(instruction);
        addInstruction(id, instruction);
    }

    /**
     * Useful for instructions that are not accessible via the programming
     * language
     * @param systemInstruction
     */
    public void addSystemInstruction(SystemInstruction systemInstruction) {
        sysInstructionOffsets.put(systemInstruction, sysInstructionSize);
        sysInstructionSize += systemInstruction.systemTypeCount();
    }

    public String createVM() {
        StringBuilder sb = new StringBuilder();
        sb.append("    // The VM requires two int-arrays to operate:\n" +
                  "    // * The data-array holds all registers and thus is not shared amongst instances. \n" +
                  "    //   The required size is obtained by calling 'dataSize' in IntCode.\n" +
                  "    // * The code-array, that contains the actual program. It is read-only.\n\n");
        sb.append("    int pc = 0; // program counter, current index in 'code'-array\n\n");
        sb.append("    while(pc < codeLen) {\n");
        sb.append("        switch(code[pc]) {\n");

        boolean first = true;

        for(Map.Entry<SystemInstruction, Integer> entry : sysInstructionOffsets.entrySet()) {
            if(first) {
                first = false;
            } else {
                sb.append("\n");
            }

            sb.append(String.format("            // %s with %s cases\n\n", entry.getKey(), entry.getKey().systemTypeCount()));
            sb.append(entry.getKey().vmCases(entry.getValue()));
        }

        sb.append("        }\n");
        sb.append("    }\n");

        return sb.toString();
    }

    public int offset(SystemInstruction systemInstruction) {
        if(!sysInstructionOffsets.containsKey(systemInstruction)) {
            throw new IllegalArgumentException("does not exist: " + systemInstruction);
        }

        return sysInstructionOffsets.get(systemInstruction);
    }

    @Override
    public String toString() {
        return sysInstructionOffsets.toString();
    }
}
