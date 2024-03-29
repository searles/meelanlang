package at.searles.meelan.compiler;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ops.InstructionSet;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.Block;
import at.searles.meelan.optree.inlined.Frame;
import at.searles.meelan.parser.MeelanParser;
import at.searles.meelan.symbols.*;
import at.searles.parsing.ParserStream;

import java.util.List;

public class Ast {

    public static Ast parse(ParserStream input) {
        List<Tree> ast = MeelanParser.stmts().parse(input);
        return new Ast(new Block(input.createSourceInfo(), ast));
    }

    private final Tree root;

    public Ast(Tree root) {
        this.root = root;
    }

    public IntCode compile(InstructionSet instructions, IdResolver resolver) {
        Tree preprocessedAst = preprocess(resolver);
        Executable program = linearize(preprocessedAst);
        return program.createIntCode(instructions);
    }

    public Tree preprocess(IdResolver idResolver) throws MeelanException {
        SymTable table = new SymTable();
        Frame.Builder frameBuilder = new Frame.Builder(root.sourceInfo());

        return root.preprocessor(table, idResolver, frameBuilder);
    }

    private Executable linearize(Tree ppAst) throws MeelanException {
        Executable program = new Executable();

        ppAst.linearizeStmt(program);

        return program;
    }

    public String toString() {
        return root.toString();
    }
}
