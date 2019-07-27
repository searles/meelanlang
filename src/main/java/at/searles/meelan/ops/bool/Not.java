package at.searles.meelan.ops.bool;

import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.ops.TypedInstruction;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Bool;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Label;

import java.util.Collections;
import java.util.List;

public class Not extends TypedInstruction {

    private static Not singleton = null;

    public static Not get() {
        if (singleton == null) {
            singleton = new Not();
        }

        return singleton;
    }

    private Not() {
        super(new FunctionType(Collections.singletonList(BaseType.bool), BaseType.bool));
    }

    @Override
    public void linearizeBool(List<Tree> args, Label trueLabel, Label falseLabel, Executable program) throws MeelanException {
        args.get(0).linearizeBool(falseLabel, trueLabel, program);
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Bool(!((Bool) args.get(0)).value());
    }
}

            /*@Override
        public Tree eval(List<Tree> arguments) {
            return unaryEval(arguments);
        }

        @Override
        public void linearizeBool(List<Tree> args, Value.Label trueLabel, Value.Label falseLabel, DataScope currentScope, Program program) throws CompileException {
            if(args.size() != 1) {
                throw new CompileException("not requires 1 argument");
            }

            args.get(0).linearizeBool(falseLabel, trueLabel, currentScope, program);
        }

        @Override
        String generateCase(Signature signature, List<Value> values) {
            throw new IllegalArgumentException("not a C-instruction");
        }

}*/
