package at.searles.meelan.preprocessor.test;

import at.searles.lexer.TokStream;
import at.searles.meelan.DefaultData;
import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Ast;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.compiled.App;
import at.searles.meelan.optree.inlined.Id;
import at.searles.meelan.optree.inlined.Lambda;
import at.searles.meelan.parser.DummyInfo;
import at.searles.meelan.values.Int;
import at.searles.parsing.ParserStream;
import at.searles.parsing.Recognizable;
import at.searles.parsing.printing.ConcreteSyntaxTree;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class PreprocessorSimpleTest {

    private String source;
    private Ast ast;
    private Tree preprocessed;

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
    public void testLambda() {
        Tree lambda = new Lambda(DummyInfo.getInstance(), Collections.singletonList("x"), new Id(DummyInfo.getInstance(), "x"));
        App app = new App(DummyInfo.getInstance(), lambda, new Int(31));

        setAst(app);

        actRunPreprocessor();

        Tree result = preprocessed;

        Assert.assertTrue(result instanceof Int);
        Assert.assertEquals(31, ((Int) result).value());
    }

    @Test
    public void testMemberOfCplx() throws MeelanException {
        setSource("var a cplx = 0:0; var b = a.x;");

        // act
        actRunPreprocessor();

        // test
        assertResult(
                "[var(a): cplx, var(b): real] {\n" +
                        "  var(a) = 0.0\n" +
                        "  var(b) = var(a)::x\n" +
                        "}\n");
    }

    @Test
    public void testMemberOfQuat() throws MeelanException {
        setSource("var a quat = 0:0:0:0; var b = a.a;");

        // act
        actRunPreprocessor();

        // test
        assertResult(
                "[var(a): quat, var(b): real] {\n" +
                        "  var(a) = [0.0, 0.0, 0.0, 0.0]\n" +
                        "  var(b) = var(a)::a\n" +
                        "}\n");
    }

    @Test
    public void testDeclareVar() throws MeelanException {
        // set up
        setSource("var a int = 1;");

        // act
        actRunPreprocessor();

        // test
        assertResult(
                "[var(a): integer] {\n" +
                        "  var(a) = 1\n" +
                        "}\n");
    }

    @Test
    public void testDeclareVarInBlock() throws MeelanException {
        // set up
        setSource("var a int = { var b int = 1; b } + { var c int = 2; c }");

        // act
        actRunPreprocessor();

        // test
        assertResult(
                "[var(a): integer] {\n" +
                        "  var(a) = Add([var(b): integer] {\n" +
                        "    var(b) = 1\n" +
                        "    var(b)\n" +
                        "  }\n" +
                        "  , [var(c): integer] {\n" +
                        "    var(c) = 2\n" +
                        "    var(c)\n" +
                        "  }\n" +
                        "  )\n" +
                        "}\n");
    }

    @Test
    public void testOverlayVar() throws MeelanException {
        // set up
        setSource("var a int = 0; var b = { var a int = a; a };");

        // act
        actRunPreprocessor();

        // test
        assertResult( // fixme check in linearization
                "[var(a): integer, var(b): integer] {\n" +
                        "  var(a) = 0\n" +
                        "  var(b) = [var(a): integer] {\n" +
                        "    var(a) = var(a)\n" +
                        "    var(a)\n" +
                        "  }\n" +
                        "}\n");
    }


    @Test
    public void testForVector() throws MeelanException {
        // Test more complex type conversion
        setSource("var a = 6; for i in [{var a = 8; a}, 10] do a = a + i;");
        // act
        actRunPreprocessor();

        // test
        assertResult(
                "[var(a): integer] {\n" +
                        "  var(a) = 6\n" +
                        "  [var(__index__): integer] {\n" +
                        "    var(__index__) = 0\n" +
                        "    while [var(i): integer] {\n" +
                        "      var(i) = select var(__index__) in [[var(a): integer] {\n" +
                        "        var(a) = 8\n" +
                        "        var(a)\n" +
                        "      }\n" +
                        "      , 10]\n" +
                        "      var(a) = Add(var(a), var(i))\n" +
                        "      Next(var(__index__), 2)\n" +
                        "    }\n" +
                        "     do null\n" +
                        "  }\n" +
                        "}\n");
    }

    @Test
    public void testDeclareVarNoType() throws MeelanException {
        setSource("var a = 1;");

        // act
        actRunPreprocessor();

        // test
        assertResult(
                "[var(a): integer] {\n" +
                        "  var(a) = 1\n" +
                        "}\n");
    }

    @Test
    public void testOptimizeAdd() throws MeelanException {
        setSource("var a = 1; var b = a + 2; var c = a + (3 + (b + 1)); var d = a + b + 0;");

        // act
        actRunPreprocessor();

        // test
        assertResult(
                "[var(a): integer, var(b): integer, var(c): integer, var(d): integer] {\n" +
                        "  var(a) = 1\n" +
                        "  var(b) = Add(2, var(a))\n" +
                        "  var(c) = Add(Add(4, var(a)), var(b))\n" +
                        "  var(d) = Add(var(a), var(b))\n" +
                        "}\n");
    }

    @Test
    public void testFuncClosure() throws MeelanException {
        setSource("var a = 1; func incr() { a = a + 1 }; incr();");

        // act
        actRunPreprocessor();

        // test
        assertResult("[var(a): integer] {\n" +
                "  var(a) = 1\n" +
                "  [] {\n" +
                "    var(a) = Add(1, var(a))\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    public void testFuncCallWithParameter() throws MeelanException {
        setSource("var a = 1; func incr(b) { b = b + 1 }; incr(a);");

        // act
        actRunPreprocessor();

        // test
        assertResult("[var(a): integer] {\n" +
                "  var(a) = 1\n" +
                "  [] {\n" +
                "    var(a) = Add(1, var(a))\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    public void testFuncClosureWithParameterAndDef() throws MeelanException {
        setSource("var a = 1; def c = 1; func incr(b) { b = b + c }; incr(a);");

        // act
        actRunPreprocessor();

        // test
        assertResult("[var(a): integer] {\n" +
                "  var(a) = 1\n" +
                "  [] {\n" +
                "    var(a) = Add(1, var(a))\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    public void testFuncClosureWithParameterAndSameIdentifiers() throws MeelanException {
        setSource("def c = 1; func incr(b) { b = b + c }; { var a = 1; def c = 2; incr(a); }");

        // act
        actRunPreprocessor();

        // test
        assertResult("[] {\n" +
                "  [var(a): integer] {\n" +
                "    var(a) = 1\n" +
                "    [] {\n" +
                "      var(a) = Add(1, var(a))\n" +
                "    }\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    public void testFuncClassMember() throws MeelanException {
        setSource(
                "template A(b) { \n" +
                "    func incr(a) { \n" +
                "        a = a + b \n" +
                "    } " +
                "}; " +
                "object c = A(2); " +
                "var d = 1; " +
                "c.incr(d)");

        // act
        actRunPreprocessor();

        // test
        assertResult("[var(d): integer] {\n" +
                "  var(d) = 1\n" +
                "  [] {\n" +
                "    var(d) = Add(2, var(d))\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    public void testVarClassMember() throws MeelanException {
        setSource("template A { var a int;}; object b = A(); b.a = 1; b.a = b.a + 1");

        // act
        actRunPreprocessor();

        // test
        assertResult("[var(a): integer] {\n" +
                "  var(a) = 1\n" +
                "  var(a) = Add(1, var(a))\n" +
                "}\n");
    }

    @Test
    public void testClassClosure() throws MeelanException {
        setSource("var a = 1; template A(b) { func incr() { a = a + b } }; object c = A(1); c.incr()");

        // act
        actRunPreprocessor();

        // test
        assertResult("[var(a): integer] {\n" +
                "  var(a) = 1\n" +
                "  [] {\n" +
                "    var(a) = Add(1, var(a))\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    public void testNewton() throws MeelanException {
        setSource("var z real = 1; var a = newton(z^2-1, z);");
        actRunPreprocessor();

        assertResult("[var(z): real, var(a): real] {\n" +
                "  var(z) = 1.0\n" +
                "  var(a) = Sub(var(z), Div(Add(-1.0, Pow(var(z), 2)), Mul(Pow(var(z), 2), Div(2.0, var(z)))))\n" +
                "}\n");
    }

    @Test
    public void testBasicClassInline() throws MeelanException {
        // set up
        setSource("var a = 1;\n" +
                "\n" +
                "template A(b) {\n" +
                "    var c int;\n" +
                "    func init() {\n" +
                "        c = a + b;\n" +
                "    }\n" +
                "    func d(e) {\n" +
                "        c = c + e;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "def b = a;\n" +
                "\n" +
                "{\n" +
                "    def a = 0;\n" +
                "    \n" +
                "    object f = A(3); \n" +
                "    f.init();\n" +
                "\n" +
                "    f.d(2);\n" +
                "\n" +
                "    b = f.c;\n" +
                "\n" +
                "    object g = A(5);\n" +
                "    g.init();\n" +
                "    g.d(2);\n" +
                "\n" +
                "    b = g.c;\n" +
                "}\n");

        // act
        actRunPreprocessor();

        // test
        assertResult("[var(a): integer] {\n" +
                "  var(a) = 1\n" +
                "  [var(c): integer, var(c): integer] {\n" +
                "    [] {\n" +
                "      var(c) = Add(3, var(a))\n" +
                "    }\n" +
                "    [] {\n" +
                "      var(c) = Add(2, var(c))\n" +
                "    }\n" +
                "    var(a) = var(c)\n" +
                "    [] {\n" +
                "      var(c) = Add(5, var(a))\n" +
                "    }\n" +
                "    [] {\n" +
                "      var(c) = Add(2, var(c))\n" +
                "    }\n" +
                "    var(a) = var(c)\n" +
                "  }\n" +
                "}\n");
    }

    @Test
    public void testSimpleTypes() throws MeelanException {
        setSource("var a = 1;");
        actRunPreprocessor();
        assertResult("[var(a): integer] {\n" +
                "  var(a) = 1\n" +
                "}\n");
    }

    @Test
    public void testRealConversion() throws MeelanException {
        setSource("var a = 1; var b real = a;");
        actRunPreprocessor();
        assertResult("[var(a): integer, var(b): real] {\n" +
                "  var(a) = 1\n" +
                "  var(b) = IntToReal(var(a))\n" +
                "}\n");
    }

    @Test
    public void testCplxConversion() throws MeelanException {
        setSource("var a = 1; var b cplx = a;");
        actRunPreprocessor();
        assertResult("[var(a): integer, var(b): cplx] {\n" +
                "  var(a) = 1\n" +
                "  var(b) = Cons(IntToReal(var(a)), 0.0)\n" +
                "}\n");
    }

    @Test
    public void testQuatConversion() throws MeelanException {
        setSource("var a = 1; var b quat = a;");
        actRunPreprocessor();
        assertResult("[var(a): integer, var(b): quat] {\n" +
                "  var(a) = 1\n" +
                "  var(b) = Cons(IntToReal(var(a)), 0.0, 0.0, 0.0)\n" +
                "}\n");
    }

    @Test
    public void testValueConversion() throws MeelanException {
        setSource("var a real = 1;");
        actRunPreprocessor();
        assertResult("[var(a): real] {\n" +
                "  var(a) = 1.0\n" +
                "}\n");
    }

    @Test
    public void testNestingOperations() throws MeelanException {
        // Test scope
        setSource("var a = 1, b = 2, c = 3, d = 4; var e = ((a + b) + c) + d");

        actRunPreprocessor();

        assertResult("[var(a): integer, var(b): integer, var(c): integer, var(d): integer, var(e): integer] {\n" +
                "  var(a) = 1\n" +
                "  var(b) = 2\n" +
                "  var(c) = 3\n" +
                "  var(d) = 4\n" +
                "  var(e) = Add(Add(Add(var(a), var(b)), var(c)), var(d))\n" +
                "}\n");
    }

    @Test
    public void testEuclid() throws MeelanException {
        // Test more complex type conversion
        setSource("var a = 4, b = 3; while a >< b do if a < b then b = b - a else a = a - b;");

        actRunPreprocessor();

        assertResult("[var(a): integer, var(b): integer] {\n" +
                "  var(a) = 4\n" +
                "  var(b) = 3\n" +
                "  while Not(Equal(var(a), var(b))) do if Less(var(a), var(b)) then var(b) = Sub(var(b), var(a)) else var(a) = Sub(var(a), var(b))\n" +
                "}\n");
    }



    @Test
    public void testIfBranchesWithDifferentTypes() throws MeelanException {
        setSource("var flag = 1; var a = 1 if flag == 1 else 2.0;");
        actRunPreprocessor();
        assertResult("[var(flag): integer, var(a): real] {\n" +
                "  var(flag) = 1\n" +
                "  var(a) = if Equal(var(flag), 1) then 1.0 else 2.0\n" +
                "}\n");
    }

}
