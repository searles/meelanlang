package at.searles.meelan.ops.arithmetics;

import at.searles.meelan.ops.SystemInstruction;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mod extends SystemInstruction {

    private static Mod singleton = null;

    public static Mod get() {
        if(singleton == null) {
            singleton = new Mod();
        }

        return singleton;
    }


    private Mod() {
        super(new FunctionType(Arrays.asList(BaseType.integer, BaseType.integer), BaseType.integer));
    }

    @Override
    protected Const evaluate(FunctionType functionType, List<Tree> args) {
        // XXX Tooling method for positive modulo
        int a = ((Int) args.get(0)).value();
        int b = ((Int) args.get(1)).value();

        a %= b;

        if(a < 0) {
            a += b;
        }

        return new Int(a);
    }

    @Override
    protected String call(ArrayList<String> arguments, int size, ArrayList<SystemType> signature) {
        return String.format("%s %% %s", arguments.get(0), arguments.get(1));
    }

}
