package at.searles.meelan.ops.sys;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Var;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Next extends SystemInstruction {

    private static Next singleton = null;

    public static Next get() {
        if(singleton == null) {
            LinkedList<ArrayList<SystemType>> types = new LinkedList<>();

            // special types because the first argument must be a register

            ArrayList<SystemType> type1 = new ArrayList<>();

            type1.add(SystemType.integerReg);
            type1.add(SystemType.integer);
            type1.add(SystemType.integer);
            type1.add(SystemType.integer);

            ArrayList<SystemType> type2 = new ArrayList<>();

            type2.add(SystemType.integerReg);
            type2.add(SystemType.integerReg);
            type2.add(SystemType.integer);
            type2.add(SystemType.integer);

            types.add(type1);
            types.add(type2);

            singleton = new Next(types);
        }

        return singleton;
    }

    private Next(LinkedList<ArrayList<SystemType>> types) {
        // Strictly speaking, this has two return types...
        // The case where the first parameter is not an lvalue is forbidden.
        super(
                new FunctionType[]{new FunctionType(Arrays.asList(BaseType.integer, BaseType.integer), BaseType.bool)},
                types,
                Kind.Bool
        );
    }

    @Override
    public FunctionType matchArguments(List<Tree> args) {
        FunctionType type = super.matchArguments(args);

        // Special treatment
        if(type != null && args.get(0) instanceof Var) {
            return type;
        }

        return null;
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("(++%s) < %s", arguments.get(0), arguments.get(1));
    }

    // replacement for for-loops.
//    @Override
//    String generateCase(Signature signature, List<Value> values) {
//        return "pc = (++" + values.get(0).vmAccessCode(1) +  ") < " + values.get(1).vmAccessCode(2) + " ? "
//                + values.get(2).vmAccessCode(3) + " : " + values.get(3).vmAccessCode(4) + ";";
//    }
}
