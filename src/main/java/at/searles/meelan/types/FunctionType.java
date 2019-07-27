package at.searles.meelan.types;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.SystemType;
import at.searles.meelan.optree.Tree;

import java.util.*;

/**
 * Function types have multiple input types and one return type.
 */
public class FunctionType {

    private ArrayList<BaseType> parameterTypes;
    private BaseType returnType;

    public FunctionType(List<BaseType> parameterTypes, BaseType returnType) {
        this.parameterTypes = new ArrayList<>(parameterTypes.size());
        this.parameterTypes.addAll(parameterTypes);

        this.returnType = returnType;
    }

    public boolean inputMatches(ArrayList<BaseType> inputTypes) {
        Iterator<BaseType> i1 = parameterTypes.iterator();
        Iterator<BaseType> i2 = inputTypes.iterator();

        while(i1.hasNext() && i2.hasNext()) {
            if(!i2.next().canConvertTo(i1.next())) {
                return false;
            }
        }
        return !i1.hasNext() && !i2.hasNext();
    }

    public BaseType returnType() {
        return returnType;
    }

    public ArrayList<BaseType> argTypes() {
        return parameterTypes;
    }

    /**
     * Adjusts args to this function type.
     * @param args
     * @return
     * @throws MeelanException
     */
    public List<Tree> convertArgumentsToType(List<Tree> args) {
        if(args.size() != parameterTypes.size()) {
            throw new IllegalArgumentException("arity must match");
        }

        List<Tree> typedArgs = new ArrayList<>(args.size());

        Iterator<Tree> it = args.iterator();
        Iterator<BaseType> type = parameterTypes.iterator();

        while(it.hasNext()) {
            BaseType argType = type.next();
            Tree adjusted = it.next().convertTo(argType);

            typedArgs.add(adjusted);
        }

        return typedArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionType that = (FunctionType) o;
        return Objects.equals(parameterTypes, that.parameterTypes) &&
                returnType == that.returnType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterTypes, returnType);
    }

    /**
     * Adds all possible system types to systemTypeList.
     * @param systemTypeList A list that is filled with all system types that are generated by this FunctionType. The
     *                       first element added will use all consts, so if this case is not possible for the given
     *                       instruction, simply remove the first element.
     */
    public void addSystemTypes(List<ArrayList<SystemType>> systemTypeList) {
        addArgTypes(0, new LinkedList<>(), systemTypeList);
    }

    private void addArgTypes(int argIndex, LinkedList<SystemType> systemSignature, List<ArrayList<SystemType>> systemTypeList) {
        if(argIndex == argTypes().size()) {
            addReturnAndAppend(systemSignature, systemTypeList);
            return;
        }

        switch(argTypes().get(argIndex)) {
            case unit:
                systemSignature.addLast(SystemType.integer);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                break;
            case integer:
                systemSignature.addLast(SystemType.integer);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                systemSignature.addLast(SystemType.integerReg);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                break;
            case real:
                systemSignature.addLast(SystemType.real);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                systemSignature.addLast(SystemType.realReg);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                break;
            case cplx:
                systemSignature.addLast(SystemType.cplx);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                systemSignature.addLast(SystemType.cplxReg);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                break;
            case quat:
                systemSignature.addLast(SystemType.quat);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                systemSignature.addLast(SystemType.quatReg);
                addArgTypes(argIndex + 1, systemSignature, systemTypeList);
                systemSignature.removeLast();
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void addReturnAndAppend(LinkedList<SystemType> systemSignature, List<ArrayList<SystemType>> systemTypeList) {
        switch (returnType) {
            case unit:
                systemTypeList.add(new ArrayList<>(systemSignature));
                return;
            case bool:
                systemSignature.addLast(SystemType.integer);
                systemSignature.addLast(SystemType.integer);
                systemTypeList.add(new ArrayList<>(systemSignature));
                systemSignature.removeLast();
                systemSignature.removeLast();
                return;
            case integer:
                systemSignature.addLast(SystemType.integerReg);
                systemTypeList.add(new ArrayList<>(systemSignature));
                systemSignature.removeLast();
                return;
            case real:
                systemSignature.addLast(SystemType.realReg);
                systemTypeList.add(new ArrayList<>(systemSignature));
                systemSignature.removeLast();
                return;
            case cplx:
                systemSignature.addLast(SystemType.cplxReg);
                systemTypeList.add(new ArrayList<>(systemSignature));
                systemSignature.removeLast();
                return;
            case quat:
                systemSignature.addLast(SystemType.quatReg);
                systemTypeList.add(new ArrayList<>(systemSignature));
                systemSignature.removeLast();
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

}
