package at.searles.meelan.ops;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.App;
import at.searles.meelan.types.BaseType;
import at.searles.meelan.types.FunctionType;
import at.searles.meelan.values.Const;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class TypedInstruction extends Instruction {
    // This one has types.
    /**
     * For overlaying...
     */
    private FunctionType[] types;

    protected TypedInstruction(FunctionType...types) {
        this.types = types;
    }

    @Override
    public Tree apply(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        boolean isConst = true;

        for(Tree arg : arguments) {
            if(!(arg instanceof Const)) {
                isConst = false;
                break;
            }
        }

        if(isConst) {
            Const result = evaluate(arguments);

            if(result != null) {
                return result;
            }

            // if it is not allowed, an error will be thrown later.
        }

        return createApp(sourceInfo, arguments);
    }

    /**
     * Override this method for optimizations based on rewrite rules
     *
     * @param sourceInfo Source info of the caller
     * @param arguments Arguments for app.
     * @return new App(this, arguments) or an equivalent but simplified expression
     */
    protected Tree createApp(SourceInfo sourceInfo, List<Tree> arguments) throws MeelanException {
        FunctionType type = matchArguments(arguments);

        if(type == null) {
            return new App(sourceInfo, this, arguments);
        }

        // if we can type it, do so.

        // insert casts/conversions for arguments
        List<Tree> typedArgs = type.convertArgumentsToType(arguments);

        App app = new App(sourceInfo, this, typedArgs);
        app.assignType(type.returnType());

        Tree optimized = optimize(app);

        return optimized != null ? optimized : app;
    }

    /**
     * Override this method to apply rewrite rules
     * @param app The application that is rooted by 'this'
     * @return null if there was no optimization
     * @throws MeelanException If the optimization found an error.
     */
    protected Tree optimize(App app) throws MeelanException {
        return null;
    }
    /**
     * For overlays: Returns the first index whose inputTypes match the ones in the arguments.
     * @param inputTypes BaseTypes for which a possible call should be found.
     * @return null if no function type matches
     */
    private FunctionType matchTypes(ArrayList<BaseType> inputTypes) {
        for (FunctionType type : types) {
            if (type.inputMatches(inputTypes)) {
                return type;
            }
        }

        return null;
    }

    public FunctionType matchArguments(List<Tree> args) {
        ArrayList<BaseType> inputTypes = new ArrayList<>(args.size());

        for(Tree arg : args) {
            BaseType type = arg.type();

            if(type == null) {
                // not all types are set.
                return null;
            }

            inputTypes.add(type);
        }

        return matchTypes(inputTypes);
    }

    /**
     *
     * @param args List of arguments, all of which are assured to be instances of Const
     * @return null if this instruction could not be evaluated with these arguments.
     */
    protected Const evaluate(List<Tree> args) {
        // Know: all are instances of Const
        // If all are const, evaluate
        FunctionType functionType = matchArguments(args);

        if(functionType == null) {
            return null;
        }

        // convert values/types
        ListIterator<Tree> it = args.listIterator();

        Iterator<BaseType> typeIt = functionType.argTypes().iterator();

        while(it.hasNext()) {
            Tree arg = it.next();
            BaseType type = typeIt.next();

            if(arg.type() != type) {
                it.set(arg.convertTo(type));
            }
        }

        return evaluate(functionType, args);
    }

    /**
     * @param functionType The signature that matches the given ops.
     */
    protected abstract Const evaluate(FunctionType functionType, List<Tree> args);
}
