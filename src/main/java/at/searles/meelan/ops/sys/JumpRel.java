package at.searles.meelan.ops.sys;

import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Call;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Label;
import at.searles.meelan.values.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Relative jump, advances by the argument
 */
public class JumpRel extends SystemInstruction {

    private static JumpRel singleton = null;

    public static JumpRel get() {
        if (singleton == null) {
            singleton = new JumpRel();
        }

        return singleton;
    }

    private JumpRel() {
        super(
                new FunctionType[]{new FunctionType(Collections.singletonList(BaseType.integer),  BaseType.unit)},
                SystemType.signatures(
                        SystemType.signature(SystemType.integerReg)
                ),
                Kind.Unit
                );
    }

    public int findMatchingSystemTypeIndex(List<Value> args) {
        // this is easy
        return 0;
    }

    public void addToProgram(Value indexNormalized, List<Label> labels, Executable program) {
        ArrayList<Value> arguments = new ArrayList<>(labels.size() + 1);
        arguments.add(indexNormalized);
        arguments.addAll(labels);
        program.add(new Call(this, arguments, 0));
    }

    @Override
    protected String call(ArrayList<String> accessArgs, int size, ArrayList<SystemType> signature) {
        return String.format("pc = code[pc + %s + 2] /* +2 because of instruction and argument */;", accessArgs.get(0));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        throw new UnsupportedOperationException();
    }


//        @Override
//        String generateCase(Signature signature, List< Value > values) {
//            // 2 + because the instruction and argument take position 0 and 1.
//            return "pc = is[pc + 2 + " + values.get(0).vmAccessCode(1) + "];";
//        }
        // is not linearized since it is automatically added to the program
}
