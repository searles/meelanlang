package at.searles.meelan.compiler.test;

import at.searles.lexer.TokStream;
import at.searles.meelan.DefaultData;
import at.searles.meelan.MeelanException;
import at.searles.meelan.compiler.Ast;
import at.searles.meelan.compiler.Executable;
import at.searles.meelan.compiler.IntCode;
import at.searles.meelan.optree.Tree;
import at.searles.parsing.ParserStream;
import at.searles.parsing.Recognizable;
import at.searles.parsing.printing.ConcreteSyntaxTree;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: This test is highly dependant on instruction count. Best solution
 * is to keep fixed instructions high in list.
 */
public class IntCodeTest {

    private String source;
    private Ast ast;
    private Tree preprocessed;
    private Executable linearized;
    private IntCode intCode;

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

    private void actLinearize() throws MeelanException {
        linearized = new Executable();
        preprocessed.linearizeStmt(linearized);
    }

    private void actCreateIntCode() {
        intCode = linearized.createIntCode(DefaultData.getDefaultInstructionSet());
    }

    private void assertResult(String expected) {
        Assert.assertEquals(expected, intCode.toString());
    }

    @Test
    public void testHelloWorld() throws MeelanException {
        setSource("var a = 1; debug(a);");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        assertResult("const int code[] = {398, 1, 0, 383, 0}; // data size = 1, code size = 5\n" +
                "int codeLen = 5;");
    }

    @Test
    public void testAdditionOfThree() {
        setSource("var x int, y int, color int; var a = #050; var b quat = int2lab a; var c quat = b + b + b; color = lab2int c");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        assertResult("00(000): Mov(1, r0(000))\n" +
                "01(003): Add(1, r0(000), r1(001))\n");
    }


    @Test
    public void testSimple() throws MeelanException {
        setSource("var a = 1; var b = a + 1;");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        assertResult("const int code[] = {398, 1, 0, 25, 1, 0, 1}; // data size = 2, code size = 7\n" +
                "int codeLen = 7;");
    }

    @Test
    public void testSimpleWithReals() throws MeelanException {
        setSource("var a real = 1; var b = a + 1;");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        assertResult("const int code[] = {400, 0, 1072693248, 0, 29, 0, 1072693248, 0, 2}; // data size = 4, code size = 9\n" +
                "int codeLen = 9;");
    }

    @Test
    public void testEuclid() throws MeelanException {
        setSource("var a = 255, b = 300; while a >< b do if a < b then b = b - a else a = a - b; debug a;");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        assertResult("const int code[] = {398, 255, 0, 398, 300, 1, 406, 23, 291, 0, 1, 13, 19, 43, 1, 0, 1, 406, 23, 43, 0, 1, 0, 283, 0, 1, 28, 8, 383, 0}; // data size = 2, code size = 30\n" +
                "int codeLen = 30;");
    }

    @Test
    public void testConstsInBlock() throws MeelanException {
        setSource("var sum = {1} + {1};");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        assertResult("const int code[] = {24, 1, 1, 0}; // data size = 1, code size = 4\n" +
                "int codeLen = 4;");
    }

    @Test
    public void testForEach() throws MeelanException {
        setSource("var sum = 0.; for i in [1, 2, 3] do sum = sum + i; debug(sum)");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        assertResult("const int code[] = {400, 0, 0, 0, 398, 0, 2, 86, 2, 3, 4, 407, 4, 16, 21, 26, 398, 1, 3, 406, 31, 398, 2, 3, 406, 31, 398, 3, 3, 406, 31, 23, 3, 4, 31, 0, 4, 0, 390, 2, 3, 7, 43, 385, 0}; // data size = 6, code size = 45\n" +
                "int codeLen = 45;");
    }

    @Test
    public void fullTest() throws MeelanException {
        setSource("var x int, y int, color int;\n" +
                "def maxdepth = 120;\n" +
                "def juliaset = false;\n" +
                "def juliapoint = -0.8:0.16;\n" +
                "func escapetime(c, breakcondition) {\n" +
                "    var i int = 0,\n" +
                "        p cplx = juliapoint if juliaset else c,\n" +
                "        zlast cplx = 0,\n" +
                "        z cplx,\n" +
                "        znext cplx = 0;\n" +
                "\n" +
                "    def mandelinit = 0;\n" +
                "\n" +
                "    z = c if juliaset else mandelinit;\n" +
                "\n" +
                "    def function = mandelbrot(z, p);\n" +
                "\n" +
                "    var color quat;\n" +
                "\n" +
                "    while {\n" +
                "        znext = function;\n" +
                "        not breakcondition(i, znext, z, zlast, c, p, color)\n" +
                "    } do {\n" +
                "        zlast = z;\n" +
                "        z = znext;\n" +
                "    }\n" +
                "    color\n" +
                "}\n" +
                "func get_color(c, value) {\n" +
                "    func breakcondition(i, znext, z, zlast, c, p, color) {\n" +
                "        def bailout = 128;\n" +
                "        func bailoutcolor() {\n" +
                "            def max_power = 2;\n" +
                "            var smooth_i = smoothen(znext, bailout, max_power) ;\n" +
                "            def bailoutvalue = log(20 + i + smooth_i);\n" +
                "            value = bailoutvalue ;\n" +
                "            def bailouttransfer = value;\n" +
                "            func bailoutpalette(a) {#000}\n" +
                "            color = bailoutpalette bailouttransfer\n" +
                "        }\n" +
                "        def epsilon = 1e-9;\n" +
                "        func lakecolor() {\n" +
                "            \n" +
                "            def lakevalue = log(1 + rad znext);\n" +
                "            value = lakevalue;\n" +
                "            def laketransfer =\n" +
                "                arcnorm znext : value;\n" +
                "            func lakepalette(a) {#000}\n" +
                "            color = lakepalette laketransfer\n" +
                "        }\n" +
                "        { lakecolor() ; true } if not next(i, maxdepth) else\n" +
                "        radrange(znext, z, bailout, epsilon, bailoutcolor(), lakecolor())\n" +
                "    }\n" +
                "    escapetime(c, breakcondition)\n" +
                "}\n" +
                "func get_color_test(c, value) {\n" +
                "    var rc = rad c;\n" +
                "    { value = (circlefn rc + 5); int2lab #0000ff} if rc < 1 else\n" +
                "    { value = circlefn abs (rc - 3); int2lab #ff0000 } if rc =< 4 and rc >= 2 else\n" +
                "    { value = -10; int2lab #00ff00 }\n" +
                "}\n" +
                "def supersampling = true;\n" +
                "def light = true;\n" +
                "func drawpixel_2d(x, y) { \n" +
                "    var c cplx = map(x, y);\n" +
                "    var value real;\n" +
                "    get_color(c, value) // value is not used\n" +
                "}\n" +
                "func drawpixel_3d(x, y) {\n" +
                "    var c00 cplx = map(x, y),\n" +
                "        c10 cplx = map(x + 1, y + 0.5),\n" +
                "        c01 cplx = map(x + 0.5, y + 1);\n" +
                "    var h00 real, h10 real, h01 real; // heights\n" +
                "    var color = (get_color(c00, h00) + get_color(c10, h10) + get_color(c01, h01)) / 3;\n" +
                "    func height(value) {\n" +
                "        def valuetransfer = value;\n" +
                "        valuetransfer\n" +
                "    }\n" +
                "    \n" +
                "    h00 = height h00; h01 = height h01; h10 = height h10;\n" +
                "    var xp = c10 - c00, xz = h10 - h00;\n" +
                "    var yp = c01 - c00, yz = h01 - h00;\n" +
                "    \n" +
                "    var np cplx = (xp.y yz - xz yp.y) : (xz yp.x - xp.x yz);\n" +
                "    var nz real = xp.x yp.y - xp.y yp.x;\n" +
                "        \n" +
                "    var nlen = sqrt(rad2 np + sqr nz);\n" +
                "    np = np / nlen; nz = nz / nlen;\n" +
                "        \n" +
                "    // get light direction\n" +
                "    def lightvector = -0.667 : -0.667; // direction from which the light is coming\n" +
                "    def lz = sqrt(1 - sqr re lightvector - sqr im lightvector); // this is inlined\n" +
                "    var cos_a real = dot(lightvector, np) + lz nz;\n" +
                "    def lightintensity = 1.;\n" +
                "    def ambientlight = 0.5;\n" +
                "    def d = lightintensity / 2; // will be inlined later\n" +
                "    color.a = color.a (((d - ambientlight) cos_a + d) cos_a + ambientlight);\n" +
                "    def specularintensity = 1.0;\n" +
                "\n" +
                "    def shininess = 8.;\n" +
                "    var spec_refl = 2 cos_a nz - lz;\n" +
                "    if spec_refl > 0 then\n" +
                "        color.a = color.a + 100 * specularintensity * spec_refl ^ shininess;\n" +
                "    color\n" +
                "}\n" +
                "func do_pixel(x, y) {\n" +
                "    def drawpixel = drawpixel_3d if light else drawpixel_2d;\n" +
                "    func drawaapixel(x, y) {\n" +
                "        0.25 (\n" +
                "            drawpixel(x - 0.375, y - 0.125) + \n" +
                "            drawpixel(x + 0.125, y - 0.375) + \n" +
                "            drawpixel(x + 0.375, y + 0.125) +\n" +
                "            drawpixel(x - 0.125, y + 0.375)         \n" +
                "        );\n" +
                "    }\n" +
                "    def fn = drawpixel if not supersampling else drawaapixel;\n" +
                "    color = lab2int fn(x, y)\n" +
                "}\n" +
                "do_pixel(x, y)\n");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        Assert.assertNotNull(intCode);
    }

    @Test
    public void testIfLinearization() {
        setSource(
                "var a = 0;\n" +
                "var b int = 1 if a < 0 else 2\n");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        // assert no fail.
    }

    @Test
    public void testLyapunov() {
        setSource("// Lyapunov\n" +
                "var x int, y int, color int;\n" +
                "\n" +
                "func get_color(c, value) {\n" +
                "    def breakbound = 1e9;\n" +
                "    def maxdepth = 250;\n" +
                "    var lyaexp real = 0;\n" +
                "    var i = 0;\n" +
                "    var z real = 0.5;\n" +
                "\n" +
                "    func step(r) {\n" +
                "       z = r * z * (1 - z);\n" +
                "       lyaexp = lyaexp + log abs r (1 - 2z);\n" +
                "       abs lyaexp < breakbound // returns a boolean\n" +
                "    }\n" +
                "\n" +
                "    def a = step(c.x);\n" +
                "    def b = step(c.y);\n" +
                "\n" +
                "    def lyastring = [a,a,a,a,b,b,b,b];\n" +
                "\n" +
                "    func pluspalette(a) { #000 };\n" +
                "    func minuspalette(a) { #000 };\n" +
                "\n" +
                "    while {\n" +
                "        select(i, lyastring) and next(i, maxdepth)\n" +
                "    };\n" +
                "\n" +
                "    lyaexp = lyaexp / i;\n" +
                "    \n" +
                "    // and get values\n" +
                "    def plusvalue = sqrt(atan(-lyaexp) (2 / 3.1415));\n" +
                "    def plustransfer = value;\n" +
                "\n" +
                "    def minusvalue = 0;\n" +
                "    def minustransfer = lyaexp;\n" +
                "\n" +
                "    { value = plusvalue ; pluspalette plustransfer } if lyaexp < 0 else\n" +
                "    { value = minusvalue; minuspalette minustransfer }\n" +
                "}\n" +
                "// drawpixel for 2D\n" +
                "func drawpixel(x, y) { \n" +
                "    var c cplx = map(x, y);\n" +
                "    var value real;\n" +
                "    get_color(c, value) // value is not used\n" +
                "}\n" +
                "func do_pixel(x, y) {\n" +
                "    color = lab2int drawpixel(x, y)\n" +
                "}\n" +
                "do_pixel(x, y)\n");

        actRunPreprocessor();
        actLinearize();
        actCreateIntCode();

        // assert no fail.
    }
}
