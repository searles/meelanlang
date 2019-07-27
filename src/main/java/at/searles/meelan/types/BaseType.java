package at.searles.meelan.types;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.cons.Cons;
import at.searles.meelan.ops.cons.IntToReal;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.values.Real;

import java.util.Arrays;
import java.util.Collections;

/*
Some thoughts of type-checks in this app:

I have statements, bools and exprs. Putting type-check into inline is difficult
because I allow tree transformations that are not type compatible (add with 3 arguments for instance).
Furthermore, what is the type of ifOp? I would need genericity.

Putting eval into linearizeExpr is also not a good idea because I could not do tree transformations in another way.
Thus the type checks are done only in linearizeExpr.
 */

public enum BaseType {
    unit {
        @Override
        public int size() {
            throw new IllegalArgumentException();
        }
    },
    bool {
        @Override
        public int size() {
            throw new IllegalArgumentException();
        }
    },
    string {
        @Override
        public int size() {
            throw new IllegalArgumentException();
        }
    },
    label {
        @Override
        public int size() {
            return 1;
        }
    },
    integer {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean canConvertTo(BaseType type) {
            return type == integer || type == real || type == cplx || type == quat;
        }

        @Override
        public Tree convertTo(Tree src, BaseType dstType) {
            if(dstType == integer) {
                return src;
            }

            try {
                Tree toReal = IntToReal.get().apply(src.sourceInfo(), Collections.singletonList(src));
                toReal.assignType(BaseType.real);

                switch (dstType) {
                    case real:
                        return toReal;
                    case cplx:
                        return toReal.convertTo(BaseType.cplx);
                    case quat:
                        return toReal.convertTo(BaseType.quat);
                }
            } catch (MeelanException e) {
                throw new IllegalArgumentException(e);
            }

            return super.convertTo(src, dstType);
        }
    }, // integer
    real {
        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean canConvertTo(BaseType type) {
            return type == real || type == cplx || type == quat;
        }

        @Override
        public Tree convertTo(Tree src, BaseType dstType) {
            try {
                switch (dstType) {
                    case real:
                        return src;
                    case cplx:
                        return Cons.get().apply(src.sourceInfo(), Arrays.asList(src, Real.ZERO));
                    case quat:
                        return Cons.get().apply(src.sourceInfo(), Arrays.asList(src, Real.ZERO, Real.ZERO, Real.ZERO));
                }
            } catch (MeelanException e) {
                throw new IllegalArgumentException(e);
            }

            return super.convertTo(src, dstType);
        }
    },
    cplx {
        @Override
        public int size() {
            return 4;
        }

        @Override
        public boolean canConvertTo(BaseType type) {
            return type == cplx || type == quat;
        }

        @Override
        public BaseType memberType(String id) {
            switch(id) {
                case "x": return BaseType.real;
                case "y": return BaseType.real;
            }

            return super.memberType(id);
        }

        @Override
        public int memberOffset(String id) {
            switch(id) {
                case "x": return 0;
                case "y": return 2;
            }

            return super.memberOffset(id);
        }

        @Override
        public Tree convertTo(Tree src, BaseType dstType) {
            switch(dstType) {
                case cplx: return src;
// TODO                case quat: return new QuatVal(new Quat(v.value.re(), v.value.im(), 0, 0));
            }

            return super.convertTo(src, dstType);
        }
    },
    quat {
        @Override
        public int size() {
            return 8;
        }

        @Override
        public boolean canConvertTo(BaseType type) {
            return type == quat;
        }

        @Override
        public BaseType memberType(String id) {
            switch(id) {
                case "a": return BaseType.real;
                case "b": return BaseType.real;
                case "c": return BaseType.real;
                case "d": return BaseType.real;
            }

            return super.memberType(id);
        }

        @Override
        public int memberOffset(String id) {
            switch(id) {
                case "a": return 0;
                case "b": return 2;
                case "c": return 4;
                case "d": return 6;
            }

            return super.memberOffset(id);
        }

        @Override
        public Tree convertTo(Tree src, BaseType dstType) {
            switch(dstType) {
                case quat: return src;
            }

            return super.convertTo(src, dstType);
        }
    };

    public static BaseType get(String t) {
        switch (t) {
            case "int":
                return BaseType.integer;
            case "realf": // deprecated, kept for historic reasons
                return BaseType.real;
            case "real":
                return BaseType.real;
            case "cplxf": // deprecated, kept for historic reasons
                return BaseType.cplx;
            case "cplx":
                return BaseType.cplx;
            case "quadf": // deprecated, kept for historic reasons
                return BaseType.quat;
            case "quad": // deprecated, kept for historic reasons
                return BaseType.quat;
            case "quat":
                return BaseType.quat;
            default:
                return null;
        }
    }

    /**
     * Size of type in integers
     * @return the size
     */
    public abstract int size();

    /**
     * Converts src to this type.
     * @param src some Tree of type 'this'.
     * @param dstType dstType.
     * @return The converted element.
     * @throws IllegalArgumentException if conversion failed. Use 'canConvertTo' to avoid this.
     */
    public Tree convertTo(Tree src, BaseType dstType) {
        if(this == dstType) {
            return src;
        }

        throw new MeelanException("cannot convert to " + dstType, src);
    }

    public boolean canConvertTo(BaseType type) {
        return this == type;
    }

    /**
     * Similar to structs in C, returns the offset of this member
     * @param id the id of the member
     * @return the offset (in integer size)
     */
    public int memberOffset(String id) {
        throw new IllegalArgumentException("must check memberType first");
    }

    /**
     * Returns the type of the member
     * @param id the id of the member
     * @return null if there is no such member
     */
    public BaseType memberType(String id) {
        return null;
    }
}
