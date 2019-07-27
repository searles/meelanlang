package at.searles.meelan;

import at.searles.meelan.optree.Tree;
import at.searles.parsing.utils.ast.SourceInfo;

public class MeelanException extends RuntimeException {
    private final Tree source;

    public MeelanException(String msg, Tree source) {
        super(msg);
        this.source = source;
    }

    public SourceInfo sourceInfo() {
        return source.sourceInfo();
    }

    public String toString() {
        return getMessage() + ": " + source.sourceInfo();
    }
}
