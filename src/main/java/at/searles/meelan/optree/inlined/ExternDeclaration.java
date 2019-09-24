package at.searles.meelan.optree.inlined;

import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;
import java.util.stream.Stream;

public class ExternDeclaration extends Tree {

    // Variable declarations must either have a type or an initialization.
    public final String id;
    public final String type;
    public final String description; // may be null

    public final Tree value;

    public ExternDeclaration(SourceInfo info, String id, String type, String description, Tree init) { // expr may be null
        super(info);
        this.id = id;
        this.type = type;
        this.description = description != null ? description : id;
        this.value = init;

        assignType(BaseType.unit);
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(value);
    }

    public String toString() {
        return "extern " + id + ": " + type + (value == null ? "" : " := " + value);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        // do nothing. it was already registered before.
        return null;
    }
}
