package at.searles.meelan.symbols;

import at.searles.meelan.optree.Tree;

/**
 * Basic symbol table. This is used eg for extern data that are automatically added if
 * undefined.
 */
public interface IdResolver {

    /**
     * Returns the value of id in its representation for the compiler. This
     * method is only called by the compiler if it did not
     * find a parameter or instruction id.
     * @return null if undefined
     */
    Tree valueOf(String id);
}
