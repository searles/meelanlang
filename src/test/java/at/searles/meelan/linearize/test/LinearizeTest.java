package at.searles.meelan.linearize.test;

import at.searles.lexer.TokenStream;
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

public class LinearizeTest {

    private String source;
    private Ast ast;
    private Tree preprocessed;
    private Executable linearized;

    private void setAst(Tree tree) {
        this.ast = new Ast(tree);
    }

    private void setSource(String program) {
        this.source = program;

        ParserStream stream = new ParserStream(TokenStream.fromString(source));
        this.ast = Ast.parse(stream);
    }

    private void actRunPreprocessor() throws MeelanException {
        // This ds will contain the code
        this.preprocessed = ast.preprocess(DefaultData.getDefaultExternData());
    }

    private void actLinearize() throws MeelanException {
        linearized = new Executable();
        preprocessed.linearizeStmt(linearized);
    }

    private void assertResult(String expected) {
        Assert.assertEquals(expected, linearized.toString());
    }

    @Test
    public void testSimple() throws MeelanException {
        // Test type resolution
        setSource("var a = 1; var b = a + 1;");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r000)\n" +
                "01(003): Add(1, r000, r001)\n");
    }

    @Test
    public void testIndividualArgumentsForBlockResults() {
        setSource("var a int = { var b int = 1; b } + { var c int = 2; c }");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r001)\n" +
                "01(003): Mov(2, r002)\n" +
                "02(006): Add(r001, r002, r000)\n");
    }

    @Test
    public void testAdditionOfThree() {
        setSource("var x int, y int, color int; var a = #050; var b quat = int2lab a; var c quat = b + b + b; color = lab2int c");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(-16755456, r003)\n" +
                "01(003): Int2Lab(r003, r004)\n" +
                "02(006): Add(r004, r004, r020)\n" +
                "03(010): Add(r020, r004, r012)\n" +
                "04(014): Lab2Int(r012, r002)\n");
    }

    @Test
    public void testBlockWithInnerVar() throws MeelanException {
        // Test scope
        setSource("var a = 1; var b = { var d = 2; a + d }; var c = b;");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r000)\n" +
                "01(003): Mov(2, r003)\n" +
                "02(006): Add(r000, r003, r001)\n" +
                "03(010): Mov(r001, r002)\n");
    }

    @Test
    public void testOverwritingVarsInInnerScope() throws MeelanException {
        // Test scope
        setSource("var a = {var b = 1; b + 2} + {var c = 3; c + 4}");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r001)\n" +
                "01(003): Add(2, r001, r001)\n" +
                "02(007): Mov(3, r002)\n" +
                "03(010): Add(4, r002, r002)\n" +
                "04(014): Add(r001, r002, r000)\n");
    }

    @Test
    public void testNestingOperations() throws MeelanException {
        // Test scope
        setSource("var a = 1, b = 2, c = 3, d = 4; var e = ((a + b) + c) + d");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r000)\n" +
                "01(003): Mov(2, r001)\n" +
                "02(006): Mov(3, r002)\n" +
                "03(009): Mov(4, r003)\n" +
                "04(012): Add(r000, r001, r005)\n" +
                "05(016): Add(r005, r002, r005)\n" +
                "06(020): Add(r005, r003, r004)\n");
    }

    @Test
    public void testScopeInsideBlocks() throws MeelanException {
        // Test scope inside blocks
        setSource("var a = 1; var b = {var c = 2 ; { var d = c + 1; d}};");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r000)\n" +
                "01(003): Mov(2, r002)\n" +
                "02(006): Add(1, r002, r003)\n" +
                "03(010): Mov(r003, r001)\n");
    }

    @Test
    public void testScopeInsideBlocksNoInit() throws MeelanException {
        // Test scope inside blocks
        setSource("var a = {var b = {var c = { var d = 1; d}; c}; b};");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r003)\n" +
                "01(003): Mov(r003, r002)\n" +
                "02(006): Mov(r002, r001)\n" +
                "03(009): Mov(r001, r000)\n");
    }

    @Test
    public void testConversion() throws MeelanException {
        // Test simple type conversion
        setSource("var a integer = 1; var b real = a;");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1, r000)\n" +
                "01(003): IntToReal(r000, r001)\n");
    }

    @Test
    public void testBoolean() throws MeelanException {
        // Test simple type conversion
        setSource("var a integer = 1; var b int = 1; var c int; if a > 1 or b < 1 or a >= 1 or b =< 1 then c = 2;");

        actRunPreprocessor();
        actLinearize();

        // TODO Room for optimizations

        assertResult("00(000): Mov(1, r000)\n" +
                "01(003): Mov(1, r001)\n" +
                "02(006): Less(1, r000, Label09(032), Label03(011))\n" +
                "03(011): Less(r001, 1, Label09(032), Label04(016))\n" +
                "04(016): Jump(Label05(018))\n" +
                "05(018): Less(r000, 1, Label06(023), Label09(032))\n" +
                "06(023): Jump(Label07(025))\n" +
                "07(025): Less(1, r001, Label08(030), Label09(032))\n" +
                "08(030): Jump(Label10(035))\n" +
                "09(032): Mov(2, r002)\n");
    }

    @Test
    public void testCplxConversion() throws MeelanException {
        // Test more complex type conversion
        setSource("var a real = 1; var b cplx = 2 + a;");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(1.0, r000)\n" +
                "01(004): Add(2.0, r000, r006)\n" +
                "02(009): Cons(r006, 0.0, r002)\n");
    }

    @Test
    public void testForVector() throws MeelanException {
        // Test more complex type conversion
        setSource("var a = 6; for i in [{var a = 1; a + 1}, 2] do a = a + i;");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(6, r000)\n" +
                "01(003): Mov(0, r001)\n" +
                "02(006): Mod(r001, 2, r003)\n" +
                "03(010): JumpRel(r003, Label04(014), Label07(023))\n" +
                "04(014): Mov(1, r004)\n" +
                "05(017): Add(1, r004, r002)\n" +
                "06(021): Jump(Label09(028))\n" +
                "07(023): Mov(2, r002)\n" +
                "08(026): Jump(Label09(028))\n" +
                "09(028): Add(r000, r002, r000)\n" +
                "10(032): Next(r001, 2, Label02(006), Label11(037))\n");
    }

    @Test
    public void testForVectorWithEmptyVector() throws MeelanException {
        // Test more complex type conversion
        setSource("var a = 6; for i in [] do a = a + i;");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(6, r000)\n");
    }

    @Test
    public void testEuclid() throws MeelanException {
        // Test more complex type conversion
        setSource("var a = 4, b = 3; while a >< b do if a < b then b = b - a else a = a - b;");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(4, r000)\n" +
                "01(003): Mov(3, r001)\n" +
                "02(006): Jump(Label07(023))\n" +
                "03(008): Less(r000, r001, Label04(013), Label06(019))\n" +
                "04(013): Sub(r001, r000, r001)\n" +
                "05(017): Jump(Label07(023))\n" +
                "06(019): Sub(r000, r001, r000)\n" +
                "07(023): Equal(r000, r001, Label08(028), Label03(008))\n");
    }

    @Test
    public void testMembers() throws MeelanException {
        // Test more complex type conversion
        setSource("var a cplx = 4; var b = (1:1).x; var c = a.x + a.y; ");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(4.0, r000)\n" +
                "01(006): Mov(1.0, r004)\n" +
                "02(010): Add(r000, r002, r006)\n");
    }

    @Test
    public void testIfElseWithWrongType() throws MeelanException {
        setSource("var a real = 0; var b = a if a > 0 else 0");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(0.0, r000)\n" +
                "01(004): Less(0.0, r000, Label02(010), Label04(015))\n" +
                "02(010): Mov(r000, r002)\n" +
                "03(013): Jump(Label05(019))\n" +
                "04(015): Mov(0.0, r002)\n");
    }


    @Test
    public void testLoopConditionWithInnerVar() throws MeelanException {
        setSource("var c int = 0;\n" +
                "\n" +
                "while {\n" +
                "    var a int = c+c;\n" +
                "    a+1 < 4" +
                "}");

        actRunPreprocessor();
        actLinearize();

        assertResult("00(000): Mov(0, r000)\n" +
                "01(003): Add(r000, r000, r001)\n" +
                "02(007): Add(1, r001, r002)\n" +
                "03(011): Less(r002, 4, Label01(003), Label04(016))\n");
    }
}
