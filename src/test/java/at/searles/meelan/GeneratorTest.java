package at.searles.meelan;

import at.searles.meelan.ops.InstructionSet;
import org.junit.Test;

public class GeneratorTest {
    private InstructionSet instructionSet;

    @Test
    public void simpleTest() {
        initSystemInstructionSet();

        System.out.println(instructionSet.createVM());
    }

    private void initSystemInstructionSet() {
        instructionSet = DefaultData.getDefaultInstructionSet();
    }
}
