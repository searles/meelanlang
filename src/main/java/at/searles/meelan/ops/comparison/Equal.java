package at.searles.meelan.ops.comparison;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Bool;
import at.searles.meelan.values.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Equal extends SystemInstruction {
    private static Equal singleton = null;

    public static Equal get() {
        if(singleton == null) {
            singleton = new Equal();
        }

        return singleton;
    }

    private Equal() {
        super(
                new FunctionType(Arrays.asList(BaseType.integer, BaseType.integer), BaseType.bool),
                new FunctionType(Arrays.asList(BaseType.real, BaseType.real), BaseType.bool));
    }

    @Override
    protected Const evaluate(List<Tree> args) {
        if(args.size() == 2) {
            if(args.get(0).getClass() == args.get(1).getClass()) {
                return evaluate(null, args);
            }

            return super.evaluate(args);
        }

        return null;
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        return new Bool(args.get(0).equals(args.get(1)));
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("%s == %s", arguments.get(0), arguments.get(1));
    }

//    @Override
//    public String getInfixOpStringInC() {
//        return "==";
//    }
//
//    @Override
//    public Const evalConsts(Const c0, Const c1) {
//        return null;
//    }



//    /**
//     * Super class of all infix c comparisons.
//     */
//    public abstract class Comparison extends BinaryOp {
//        public Comparison(String name, Signature... signatures) {
//            super(name, signatures);
//        }
//
//        public abstract String getInfixOpStringInC();
//
//        @Override
//        public String generateCase(Signature signature, List<Value> values) {
//            return generateCmpCase(getInfixOpStringInC(), signature, values);
//        }
//
//        private String generateCmpCase(String op, Signature signature, List<Value> values) {
//            // Steps:
//            // 1. calculate Argument-Indices
//            int indices[] = new int[values.size() + 1];
//
//            indices[0] = 1; // instruction takes the first byte.
//
//            int i = 1;
//            for(Value v : values) {
//                indices[i] = indices[i - 1] + v.vmCodeSize();
//                i++;
//            }
//
//            int instructionSize = indices[values.size()];
//
//            // 2. get C-code that converts values.get(i).vmAccessCode(argumentIndex) to signature.get(i)
//            List<String> argConversion = Value.vmAccessCodes(values, indices);
//
//            // 3. create code to add both values
//            return String.format("pc = (%s %s %s) ? %s : %s;",
//                    argConversion.get(0), op,
//                    argConversion.get(1),
//                    values.get(2).vmAccessCode(indices[2]),
//                    values.get(3).vmAccessCode(indices[3])
//            );
//        }
//    }
}
