package at.searles.meelan.optree.inlined;

import at.searles.meelan.ops.comparison.Greater;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.*;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.symbols.SymTable;
import at.searles.meelan.values.Int;
import at.searles.parsing.ParserStream;
import at.searles.parsing.utils.ast.AstNode;
import at.searles.parsing.utils.ast.SourceInfo;
import at.searles.utils.GenericStruct;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForEach extends Tree {

    private static final String INDEX = "__index__";
    private final String varName;
    private final Tree vector;
    private final Tree body;

    public ForEach(SourceInfo sourceInfo, String varName, Tree vector, Tree body) {
        super(sourceInfo);
        this.varName = varName;
        this.vector = vector;
        this.body = body;
    }

    @Override
    public Stream<Tree> children() {
        return Stream.of(vector);
    }

    @Override
    public Tree preprocessor(SymTable table, IdResolver resolver, Frame.Builder frameBuilder) {
        Id index = new Id(sourceInfo(), INDEX);
        Id lengthInstruction = new Id(sourceInfo(), "length"); // mainly for unit-tests.
        Id nextInstruction = new Id(sourceInfo(), "next"); // mainly for unit-tests.
        Id selectInstruction = new Id(sourceInfo(), "select"); // mainly for unit-tests.

        Tree vecLen = new App(sourceInfo(), lengthInstruction, Collections.singletonList(vector));
        Tree vecAccess = new App(sourceInfo(), selectInstruction, Arrays.asList(index, vector));
        Tree assignment = new VarDeclaration(sourceInfo(), varName, null, vecAccess);
        Tree condition = new App(sourceInfo(), nextInstruction, Arrays.asList(index, vecLen));

        Tree whileLoop = new While(sourceInfo(), new Block(sourceInfo(), Arrays.asList(assignment, body, condition)), null);

        Tree indexDecl = new VarDeclaration(sourceInfo(), INDEX, null, new Int(0)); // int will be deduced.

        Tree isNonEmpty = Greater.get().apply(sourceInfo(), Arrays.asList(vecLen, new Int(0))); // will be removed by optimizations

        return new IfElse(sourceInfo(), isNonEmpty, new Block(sourceInfo(), Arrays.asList(indexDecl, whileLoop)), null).preprocessor(table, resolver, frameBuilder);
    }
    
    public String toString() {
        return "foreach" + children().map(AstNode::toString).collect(Collectors.joining(", "));
    }

    public static class Builder extends GenericStruct<Builder> {

        public String varName;
        public Tree vector;
        public Tree body;

        public ForEach build(ParserStream stream) {
            return new ForEach(stream.createSourceInfo(), varName, vector, body);
        }

        public static Builder toBuilder(ForEach fe) {
            Builder b = new Builder();

            b.vector = fe.vector;
            b.varName = fe.varName;
            b.body = fe.body;

            return b;
        }
    }
}
