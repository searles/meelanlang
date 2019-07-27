package at.searles.meelan.ops;


import at.searles.meelan.types.BaseType;

import java.util.ArrayList;
import java.util.LinkedList;

public enum SystemType {
    integer {
        @Override
        public int size() {
            return BaseType.integer.size();
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("code[pc + %d]", offset);
        }
    },
    integerReg {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("data[code[pc + %d]]", offset);
        }
    },
    real {
        @Override
        public int size() {
            return BaseType.real.size();
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("*((double*) &code[pc + %d])", offset);
        }
    },
    realReg {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("*((double*) &data[code[pc + %d]])", offset);
        }
    },
    cplx {
        @Override
        public int size() {
            return BaseType.cplx.size();
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("*((double2*) &code[pc + %d])", offset);
        }
    },
    cplxReg {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("*((double2*) &data[code[pc + %d]])", offset);
        }
    },
    quat {
        @Override
        public int size() {
            return BaseType.quat.size();
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("*((double4*) &code[pc + %d])", offset);
        }
    },
    quatReg {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public String vmAccessCode(int offset) {
            return String.format("*((double4*) &data[code[pc + %d]])", offset);
        }
    };

    /**
     *
     * @return the size in ints used up in the code space
     */
    public abstract int size();

    /**
     * For the VM written in Renderscript, access the element at offset in
     * the code-array of the given type.
     * @param offset Offset relative to pc in code
     * @return the code that returns an instance of this type at code[pc+offset] in the vm.
     */
    public abstract String vmAccessCode(int offset);

    public static Signature signature(SystemType...types) {
        Signature ret = new Signature(types.length);

        for(SystemType type : types) {
            ret.add(type);
        }

        return ret;
    }

    public static LinkedList<ArrayList<SystemType>> signatures(Signature...signatures) {
        LinkedList<ArrayList<SystemType>> list = new LinkedList<>();

        for(Signature signature : signatures) {
            list.add(signature.types);
        }

        return list;
    }

    public static class Signature {
        ArrayList<SystemType> types;

        public Signature(int length) {
            types = new ArrayList<>(length);
        }

        public void add(SystemType type) {
            types.add(type);
        }
    }
}
