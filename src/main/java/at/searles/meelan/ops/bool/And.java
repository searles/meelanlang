package at.searles.meelan.ops.bool;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.TypedInstruction;
import at.searles.meelan.ops.sys.Jump;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.Arrays;
import java.util.List;

public class And extends TypedInstruction {

    private static And singleton = null;

    public static And get() {
        if(singleton == null) {
            singleton = new And();
        }

        return singleton;
    }

    private And() {
        super(new FunctionType(Arrays.asList(BaseType.bool, BaseType.bool), BaseType.bool));
    }

    @Override
    public void linearizeBool(List<Tree> args, Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        for(Tree arg : args) {
            Label nextLabel = new Label();
            arg.linearizeBool(nextLabel, falseLabel, program);
            program.add(nextLabel);
        }

        // all were satisfied, thus go to true.
        program.add(Jump.get().createCall(trueLabel));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        // only one case
        return new Bool(((Bool) args.get(0)).value() && ((Bool) args.get(1)).value());
    }
}
