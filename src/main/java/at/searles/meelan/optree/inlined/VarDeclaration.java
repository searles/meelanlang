package at.searles.meelan.optree.inlined;

import at.searles.meelan.MeelanException;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.Assign;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.types.BaseType;
import at.searles.parsing.utils.ast.SourceInfo;
import java.util.stream.Stream;

public class VarDeclaration extends Tree {

    // Variable declarations must either have a type or an initialization.
    private String id;
    private String typeString;

    private Tree init; // this is an expr

    public VarDeclaration(SourceInfo info, String id, String typeString, Tree init) { // expr may be null
        super(info);
        this.id = id;
        this.typeString = typeString;
        this.init = init;
    }

    public String toString() {
        return "var " + id + ": " + typeString + (init == null ? "" : " := " + init);
    }

    @Override
    public Stream<Tree> children() {
        return init != null ? Stream.of(init) : Stream.empty();
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        BaseType type = typeString != null ? BaseType.get(typeString) : null;

        Var var = frameBuilder.declareVar(id, type);

        Tree newInit = null;

        if(init != null) {
            newInit = init.preprocessor(table, resolver, frameBuilder);

            if(type == null) {
                var.assignType(newInit.type());
            }
        }

        if(var.type() == null) {
            throw new MeelanException("Cannot assign a type to variable", this);
        }

        if(newInit != null && var.type() != newInit.type()) {
            newInit = newInit.convertTo(var.type());
        }

        // add it to the symbol table
        table.add(id, var);

        return newInit != null ? new Assign(sourceInfo(), var, newInit) : null;
    }
}
