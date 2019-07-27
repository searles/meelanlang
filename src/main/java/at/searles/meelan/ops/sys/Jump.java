package at.searles.meelan.ops.sys;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Call;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.meelan.values.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unconditional jump.
 */
public class Jump extends SystemInstruction {

    private static Jump singleton = null;

    public static Jump get() {
        if (singleton == null) {
            singleton = new Jump();
        }

        return singleton;
    }

    private Jump() {
        super(new FunctionType(Collections.singletonList(BaseType.unit), BaseType.unit));
    }

    public Call createCall(Label label) {
        return new Call(this, Collections.singletonList(label), 0);
    }

    @Override
    protected String call(ArrayList<String> accessArgs, int size, ArrayList<SystemType> signature) {
        return String.format("pc = %s;", accessArgs.get(0));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        throw new UnsupportedOperationException();
    }


//    // is not linearized since it is automatically added to the program
//    @Override
//    public String generateCase(Signature signature, List< Value > values) {
//        return "pc = " + values.get(0).vmAccessCode(1) + ";";
//    }
//
//    @Override
//    public Value linearizeExpr(List<Tree> args, Reg target, DataScope targetScope, DataScope currentScope, Program program) throws MeelanException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void linearizeStmt(List<Tree> args, DataScope currentScope, Program program) throws MeelanException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void linearizeBool(List<Tree> args, Label trueLabel, Label falseLabel, DataScope currentScope, Program program) throws MeelanException {
//        throw new UnsupportedOperationException();
//    }
}
