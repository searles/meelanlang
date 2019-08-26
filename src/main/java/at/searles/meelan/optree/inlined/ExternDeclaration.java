package at.searles.meelan.optree.inlined;

import at.searles.meelan.optree.Tree;
import at.searles.meelan.parser.MeelanStream;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.SourceInfo;
import at.searles.utils.GenericStruct;

import java.util.stream.Stream;

public class ExternDeclaration extends Tree {

    // Variable declarations must either have a type or an initialization.
    public final String id;
    public final String externTypeString;
    public final String description; // may be null

    public final Tree value;

    public ExternDeclaration(SourceInfo info, String id, String externTypeString, String description, Tree init) { // expr may be null
        super(info);
        this.id = id;
        this.externTypeString = externTypeString;
        this.description = description != null ? description : id;
        this.value = init;

        assignType(BaseType.unit);
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(value);
    }

    public String toString() {
        return "extern " + id + ": " + externTypeString + (value == null ? "" : " := " + value);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        // do nothing. it was already registered before.
        return null;
    }

    public static class Builder extends GenericStruct<Builder> {

        public String id;
        public String type;
        public String description; // may be null

        public Tree value;

        public ExternDeclaration build(ParserStream stream) {
            ExternDeclaration decl = new ExternDeclaration(stream.createSourceInfo(), id, type, description, value);
            ((MeelanStream) stream).registerExternDecl(decl);
            return decl;
        }

        public static Builder toBuilder(ExternDeclaration decl) {
            Builder b = new Builder();

            b.id = decl.id;
            b.type = decl.externTypeString;
            b.description = decl.description;
            b.value = decl.value;

            return b;
        }
    }
}
