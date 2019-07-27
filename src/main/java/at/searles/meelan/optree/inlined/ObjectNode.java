package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.Map;
import java.util.stream.Stream;

/**
 * similar to the vtable in C++
 */
class ObjectNode extends Tree {
    private Map<String, Tree> members;

    ObjectNode(SourceInfo info, Map<String, Tree> members) {
        super(info);
        this.members = members;
        assignType(BaseType.unit);
    }

    @Override
    public Stream<Tree> children() {
        return Stream.empty();
    }

    @Override
    public String toString() {
        return String.format("object: %s", members);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        // nothing to preprocess
        return this;
    }

    @Override
    public Tree member(SourceInfo info, String memberId) throws MeelanException {
        Tree member = members.get(memberId);

        if(member == null) {
            throw new MeelanException(String.format("No such member: %s",  memberId), this);
        }

        return member;
    }

    public Tree get(String member) {
        return members.get(member);
    }
}
