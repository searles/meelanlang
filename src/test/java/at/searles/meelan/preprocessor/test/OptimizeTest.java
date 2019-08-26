package at.searles.meelan.preprocessor.test;

import at.searles.lexer.TokStream;
import at.searles.meelan.DefaultData;
import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Ast;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.ParserStream;
import at.searles.parsing.Recognizable;
import at.searles.parsing.printing.ConcreteSyntaxTree;
import org.junit.Assert;
import org.junit.Test;

public class OptimizeTest {
    private String source;
    private Ast ast;
    private Tree preprocessed;
    private Executable linearized;

    private void setAst(Tree tree) {
        this.ast = new Ast(tree);
    }

    private void setSource(String program) {
        this.source = program;

        ParserStream stream = new ParserStream(TokStream.fromString(source));
        this.ast = Ast.parse(stream);
    }

    private void actRunPreprocessor() throws MeelanException {
        // This ds will contain the code
        this.preprocessed = ast.preprocess(DefaultData.getDefaultExternData());
    }

    private void assertResult(String expected) {
        Assert.assertEquals(expected, preprocessed.toString());
    }

    @Test
    public void binaryChaining() throws MeelanException {
        setSource("var x int, y int, z int; add(x, y, z)");

        actRunPreprocessor();
        assertResult("[var(x): integer, var(y): integer, var(z): integer] {\n" +
                "  Add(Add(var(x), var(y)), var(z))\n" +
                "}\n");
    }

    @Test
    public void constantPi() throws MeelanException{
        setSource("PI");

        actRunPreprocessor();
        assertResult("[] {\n" +
                "  3.141592653589793\n" +
                "}\n");
    }

    @Test
    public void constantE() throws MeelanException{
        setSource("E");

        actRunPreprocessor();
        assertResult("[] {\n" +
                "  2.718281828459045\n" +
                "}\n");
    }

    @Test
    public void constantI() throws MeelanException{
        setSource("I");

        actRunPreprocessor();
        assertResult("[] {\n" +
                "  0.0:1.0\n" +
                "}\n");
    }

    @Test
    public void testAddIntInt() throws MeelanException {
        setSource("1 + 1");

        actRunPreprocessor();
        assertResult("[] {\n  2\n}\n");
    }

    @Test
    public void testAddCplxInt() throws MeelanException {
        setSource("1:1 + 1");

        actRunPreprocessor();
        assertResult("[] {\n  2.0:1.0\n}\n");
    }

    @Test
    public void testCmpStringFalse() throws MeelanException {
        setSource("\"a\" == \"b\"");

        actRunPreprocessor();
        assertResult("[] {\n  bool[false]\n}\n");
    }

    @Test
    public void testCmpStringTrue() throws MeelanException {
        setSource("\"a\" == \"a\"");

        actRunPreprocessor();
        assertResult("[] {\n  bool[true]\n}\n");
    }

    @Test
    public void testPowRealInt() throws MeelanException {
        setSource("1.5 ^ 2");

        actRunPreprocessor();
        assertResult("[] {\n" +
                "  2.25\n" +
                "}\n");
    }

    @Test
    public void testPowCplxInt() throws MeelanException {
        setSource("1.5:0.5 ^ 2");

        actRunPreprocessor();
        assertResult("[] {\n" +
                "  2.0:1.5\n" +
                "}\n");
    }

    @Test
    public void testPowCplxReal() throws MeelanException {
        setSource("2.0:1.5 ^ 0.5");

        actRunPreprocessor();
        assertResult("[] {\n" +
                "  1.5:0.5\n" +
                "}\n");
    }
}
