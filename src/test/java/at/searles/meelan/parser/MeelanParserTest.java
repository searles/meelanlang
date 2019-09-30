package at.searles.meelan.parser;

import at.searles.lexer.Lexer;
import at.searles.lexer.TokenStream;
import at.searles.parsing.Parser;
import at.searles.parsing.Recognizer;
import at.searles.parsing.printing.ConcreteSyntaxTree;
import at.searles.parsing.printing.CstPrinter;
import at.searles.parsing.printing.StringOutStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * Basic parser tests
 */
public class MeelanParserTest {

    @Test
    public void testInt() {
        withSource("1");

        runParser(MeelanParser.expr());

        assertResult("1");
    }

    @Test
    public void testReal() {
        withSource("1.1");

        runParser(MeelanParser.expr());

        assertResult("1.1");
    }

    @Test
    public void testExpReal() {
        withSource("1e9");
        runParser(MeelanParser.expr());
        assertResult("1.0E9");
    }

    @Test
    public void testId() {
        withSource("a");

        runParser(MeelanParser.expr());

        assertResult("a");
    }

    @Test
    public void testQualified() {
        withSource("a.b");

        runParser(MeelanParser.expr());

        assertResult("a.b");
    }

    @Test
    public void testPostfixNoQualifier() {
        withSource("1");

        runParser(MeelanParser.expr());

        assertResult("1");
    }

    @Test
    public void testAppInt() {
        withSource("1");

        runParser(MeelanParser.expr());

        assertResult("1");
    }

    @Test
    public void testNeg() {
        withSource("-1");
        runParser(MeelanParser.expr());
        assertResult("- 1");
    }

    @Test
    public void testAdd() {
        withSource("1 + a");

        runParser(MeelanParser.expr());

        assertResult("1 + a");
    }

    @Test
    public void testInParentheses() {
        withSource("(1 + a)");

        runParser(MeelanParser.expr());

        assertResult("1 + a");
    }

    @Test
    public void testSimpleId() {
        withSource("a");

        runParser(MeelanParser.expr());

        assertResult("a");
    }

    @Test
    public void testMultipleArgsInBrackets() {
        withSource("a(1, 2)");

        runParser(MeelanParser.expr());

        assertResult("a(1, 2)");
    }

    @Test
    public void testEmptyArgsInBrackets() {
        withSource("a()");

        runParser(MeelanParser.expr());

        assertResult("a()");
    }

    @Test
    public void testSingleAppArg() {
        withSource("a b c");

        runParser(MeelanParser.expr());

        assertResult("a b c");
    }

    @Test
    public void testSingleAppArgsInBrackets() {
        withSource("(a (b+1) c)");

        runParser(MeelanParser.expr());

        assertResult("a (b + 1) c");
    }

    @Test
    public void testMultipleAppArgsInBracketsFail() {
        withSource("(a (b+1,d) c)");

        runParser(MeelanParser.expr());

        assertResult("a((b + 1) c, d c)");
    }

    @Test
    public void testEmptyAppArgsInBrackets() {
        withSource("(a () c)");

        runParser(MeelanParser.expr());

        assertResult("a()");
    }

    @Test
    public void testIfElseExpr() {
        withSource("a if 1 == 1 else b");

        runParser(MeelanParser.expr());

        assertResult("a if 1 == 1 else b");
    }

    @Test
    public void testCons() {
        withSource("a:b:c:d");

        runParser(MeelanParser.expr());

        assertResult("a : b : c : d");
    }

    @Test
    public void testConsWithTwo() {
        withSource("a:b");

        runParser(MeelanParser.expr());

        assertResult("a : b");
    }

    @Test
    public void testSub() {
        withSource("a - b");

        runParser(MeelanParser.expr());

        assertResult("a - b");
    }

    @Test
    public void testVector() {
        withSource("[1,2,3]");

        runParser(MeelanParser.expr());

        assertResult("[1, 2, 3]");
    }

    @Test
    public void testDiv() {
        withSource("a / b");

        runParser(MeelanParser.expr());

        assertResult("a / b");
    }


    @Test
    public void testAnd() {
        withSource("rc =< 4 and rc >= 2");

        runParser(MeelanParser.expr());

        assertResult("rc =< 4 and rc >= 2");
    }

    @Test
    public void testDeclarations() {
        withSource("{def a = 12; def b = 5;}");

        runParser(MeelanParser.stmts());

        assertResult("{\n" +
                "    def a = 12;\n" +
                "    def b = 5;\n" +
                "};\n");
    }

    @Test
    public void testString() {
        withSource("\"0\"");
        runParser(MeelanParser.expr());

        assertResult("\"0\"");
    }

    @Test
    public void testIfElseStmt() {
        withSource("if 1 == 1 then a else b");

        runParser(MeelanParser.stmt());

        assertResult("if 1 == 1 then a else b");
    }

    @Test
    public void testForVector() {
        withSource("for i in [1, 2, 3] do {}");

        runParser(MeelanParser.stmt());

        assertResult("for i in [1, 2, 3] do {\n" +
                "}");
    }


    @Test
    public void testIfStmt() {
        withSource("if 1 == 1 then a");

        runParser(MeelanParser.stmt());

        assertResult("if 1 == 1 then a");
    }


    @Test
    public void testWhileStmt() {
        withSource("while 1 == 1 do a = 1");

        runParser(MeelanParser.stmt());

        assertResult("while 1 == 1 do a = 1");
    }

    @Test
    public void testExprBlock() {
        withSource("1+{2}");

        runParser(MeelanParser.expr());

        assertResult("1 + {\n" +
                "    2;\n" +
                "}");
    }

    @Test
    public void testEmptyBlock() {
        withSource("{}");

        runParser(MeelanParser.stmts());

        assertResult("{\n};\n");
    }

    @Test
    public void testBlock() {
        withSource("{ def a = 1; def b = 2 }");

        runParser(MeelanParser.stmts());

        assertResult("{\n" +
                "    def a = 1;\n" +
                "    def b = 2;\n" +
                "};\n");
    }

    @Test
    public void testExprAsStmt() {
        withSource("1");

        runParser(MeelanParser.stmt());

        assertResult("1");
    }

    @Test
    public void testExprsAsStmts() {
        withSource("1;");

        runParser(MeelanParser.stmts());

        assertResult("1;\n");
    }

    @Test
    public void testVarStmtSimple() {
        withSource("var a = 1;");

        runParser(MeelanParser.stmts());

        assertResult("var a = 1;\n");
    }

    @Test
    public void testVarStmtComplex() {
        withSource("var a int = 2, b real, c;");

        runParser(MeelanParser.stmts());

        assertResult("var a int = 2, b real, c;\n");
    }

    @Test
    public void fullEuclid() {
        withSource("var a = 4, b = 3; while a >< b do if a < b then b = b - a else a = a - b;");

        runParser(MeelanParser.stmts());

        assertResult("var a = 4, b = 3;\n" +
                "while a >< b do if a < b then b = b - a else a = a - b;\n");
    }

    @Test
    public void testSingleLineComments() {
        withSource("// this is a comment\na = a");

        runParser(MeelanParser.stmt());

        assertResult("a = a");
    }

    @Test
    public void testSingleLineCommentAtEnd() {
        withSource("a = a// this is a comment");

        runParser(MeelanParser.stmt());

        assertResult("a = a");
    }

    @Test
    public void testMultiLineComments() {
        withSource("/* this \n is \n a \n comment\n*/a = a/*and another one */");

        runParser(MeelanParser.stmt());

        assertResult("a = a");
    }

    @Test
    public void testFnWithSingleArgument() {
        withSource("func f(a) {}");

        runParser(MeelanParser.stmts());

        assertResult("func f(a) {\n" +
                "};\n");
    }

    @Test
    public void testFnWithMultipleArguments() {
        withSource("func f(a, b, c) {}\n"
        );

        runParser(MeelanParser.stmts());

        assertResult("func f(a, b, c) {\n" +
                "};\n");
    }

    @Test
    public void testExtern() {
        withSource("extern maxdepth int = 120;");
        runParser(MeelanParser.stmts());
        assertResult("extern maxdepth int \"maxdepth\" = 120;\n");
    }

    @Test
    public void testClass() {
        withSource("template a(b) { func c(d) { b + d } };");
        runParser(MeelanParser.stmts());
        assertResult("template a(b) {\n" +
                "    func c(d) {\n" +
                "        b + d;\n" +
                "    };\n" +
                "};\n");
    }

    @Test
    public void testAssignmentInArguments() {
        withSource("fn(a = 1)");
        runParser(MeelanParser.stmts());
        assertResult("fn (a = 1);\n");
    }

    @Test
    public void testObjects() {
        withSource("object a = A(1,2), b = B(2,3);");
        runParser(MeelanParser.stmts());
        assertResult("object a = A(1, 2), b = B(2, 3);\n");
    }

    @Test
    public void fullProgramLastFractview() {
        withSource("// Default Preset\n" +
                "// This is a good start for all kinds of fractals\n" +
                "// including newton sets, nova fractals and others.\n" +
                "{\n" +
                "var x int, y int, color int;\n" +
                "\n" +
                "\n" +
                "extern maxdepth int = 120;\n" +
                "\n" +
                "// some further arguments\n" +
                "extern juliaset bool = false;\n" +
                "extern juliapoint cplx = -0.8:0.16;\n" +
                "\n" +
                "// c: coordinates, breakcondition: a function whether we should stop, \n" +
                "// value: a real variable to return some kind of value\n" +
                "//        used in 3d-types for the height.\n" +
                "// returns a quat representing the color\n" +
                "func escapetime(c, breakcondition) {\n" +
                "    var i int = 0,\n" +
                "        p cplx = juliapoint if juliaset else c,\n" +
                "        zlast cplx = 0,\n" +
                "        z cplx,\n" +
                "        znext cplx = 0;\n" +
                "\n" +
                "    extern mandelinit expr = \"0\";\n" +
                "\n" +
                "    z = c if juliaset else mandelinit;\n" +
                "\n" +
                "    extern function expr = \"mandelbrot(z, p)\";\n" +
                "\n" +
                "    var color quat;\n" +
                "\n" +
                "    while {\n" +
                "        znext = function;\n" +
                "        not breakcondition(i, znext, z, zlast, c, p, color)\n" +
                "    } do {\n" +
                "        // advance to next values\n" +
                "        zlast = z;\n" +
                "        z = znext;\n" +
                "    }\n" +
                "\n" +
                "    // return color\n" +
                "    color\n" +
                "}\n" +
                "\n" +
                "// everything that is drawn must have a get_color-function.\n" +
                "\n" +
                "// c = coordinates (scaled)\n" +
                "// value is a real variable for z-information in 3D\n" +
                "// but also otherwise convenient to separate drawing\n" +
                "// algorithm from transfer\n" +
                "// returns color.\n" +
                "func get_color(c, value) {\n" +
                "\n" +
                "    // if the fractal accumulates some values\n" +
                "    // like in traps or addends, here is a got place to do it.\n" +
                "\n" +
                "    func breakcondition(i, znext, z, zlast, c, p, color) {\n" +
                "        func bailoutcolor() {\n" +
                "            extern bailout real = 128;\n" +
                "            extern max_power real = 2;\n" +
                "            var smooth_i = smoothen(znext, bailout, max_power) ;\n" +
                "\n" +
                "            // the next ones are only used in 3d-fractals\n" +
                "            extern bailoutvalue expr = \"log(20 + i + smooth_i)\";\n" +
                "            value = bailoutvalue ;\n" +
                "        \n" +
                "            extern bailouttransfer expr = \"value\";\n" +
                "\n" +
                "            extern bailoutpalette palette = [\n" +
                "                    [#006, #26c, #fff, #fa0, #303]];\n" +
                "    \n" +
                "            color = bailoutpalette bailouttransfer\n" +
                "        }\n" +
                "\n" +
                "        func lakecolor() {\n" +
                "            extern epsilon real = 1e-9;\n" +
                "        \n" +
                "            // the next ones are only used in 3d-fractals\n" +
                "            extern lakevalue expr = \"log(1 + rad znext)\";\n" +
                "            value = lakevalue;\n" +
                "\n" +
                "            extern laketransfer expr =\n" +
                "                \"arcnorm znext : value\";\n" +
                "\n" +
                "            extern lakepalette palette = [\n" +
                "                [#000, #000, #000, #000],\n" +
                "                [#f00, #ff0, #0f8, #00f],\n" +
                "                [#f88, #ff8, #afc, #88f]];\n" +
                "\n" +
                "            color = lakepalette laketransfer\n" +
                "        }\n" +
                "\n" +
                "        { lakecolor() ; true } if not next(i, maxdepth) else\n" +
                "        radrange(znext, z, bailout, epsilon, bailoutcolor(), lakecolor())\n" +
                "    }\n" +
                "    \n" +
                "    escapetime(c, breakcondition)\n" +
                "}\n" +
                "\n" +
                "func get_color_test(c, value) {\n" +
                "    // this one is just here for testing light effects\n" +
                "    // circle + donut + green bg\n" +
                "    var rc = rad c;\n" +
                "    \n" +
                "    { value = (circlefn rc + 5); int2lab #0000ff} if rc < 1 else\n" +
                "    { value = circlefn abs (rc - 3); int2lab #ff0000 } if rc =< 4 and rc >= 2 else\n" +
                "    { value = -10; int2lab #00ff00 }\n" +
                "}\n" +
                "\n" +
                "// ******************************************\n" +
                "// * Next are just drawing procedures. They *\n" +
                "// * should be the same for all drawings.   *                 \n" +
                "// ******************************************\n" +
                "\n" +
                "extern supersampling bool = false;\n" +
                "extern light bool = false;\n" +
                "\n" +
                "// drawpixel for 2D\n" +
                "func drawpixel_2d(x, y) { \n" +
                "    var c cplx = map(x, y);\n" +
                "    var value real;\n" +
                "    get_color(c, value) // value is not used\n" +
                "}\n" +
                "\n" +
                "// drawpixel for 3D\n" +
                "func drawpixel_3d(x, y) {\n" +
                "    var c00 cplx = map(x, y),\n" +
                "        c10 cplx = map(x + 1, y + 0.5),\n" +
                "        c01 cplx = map(x + 0.5, y + 1);\n" +
                "    \n" +
                "    var h00 real, h10 real, h01 real; // heights\n" +
                "    \n" +
                "    // color is already kinda super-sampled\n" +
                "    var color = (get_color(c00, h00) + get_color(c10, h10) + get_color(c01, h01)) / 3;\n" +
                "\n" +
                "    // get height out of value\n" +
                "    func height(value) {\n" +
                "        extern valuetransfer expr = \"value\";\n" +
                "        valuetransfer\n" +
                "    }\n" +
                "    \n" +
                "    h00 = height h00; h01 = height h01; h10 = height h10;\n" +
                "\n" +
                "    // get the normal vector (cross product)\n" +
                "    var xp = c10 - c00, xz = h10 - h00;\n" +
                "    var yp = c01 - c00, yz = h01 - h00;\n" +
                "    \n" +
                "    var np cplx = (xp.y yz - xz yp.y) : (xz yp.x - xp.x yz);\n" +
                "    var nz real = xp.x yp.y - xp.y yp.x;\n" +
                "        \n" +
                "    // normalize np and nz\n" +
                "    var nlen = sqrt(rad2 np + sqr nz);\n" +
                "    np = np / nlen; nz = nz / nlen;\n" +
                "        \n" +
                "    // get light direction\n" +
                "    extern lightvector cplx = -0.667 : -0.667; // direction from which the light is coming\n" +
                "    def lz = sqrt(1 - sqr re lightvector - sqr im lightvector); // this is inlined\n" +
                "\n" +
                "    // Lambert's law.\n" +
                "    var cos_a real = dot(lightvector, np) + lz nz;\n" +
                "\n" +
                "    // diffuse reflexion with ambient factor\n" +
                "    extern lightintensity real = 1;\n" +
                "    extern ambientlight real = 0.5;\n" +
                "\n" +
                "    // if lumen is negative it is behind, \n" +
                "    // but I tweak it a bit for the sake of the looks:\n" +
                "    // cos_a = -1 (which is super-behind) ==> 0\n" +
                "    // cos_a = 0 ==> ambientlight\n" +
                "    // cos_a = 1 ==> lightintensity\n" +
                "\n" +
                "    // for a mathematically correct look use the following:\n" +
                "    // if cos_a < 0 then cos_a = 0;\n" +
                "    // color.a = color.a * (ambientlight + lightintensity lumen);\n" +
                "    \n" +
                "    def d = lightintensity / 2; // will be inlined later\n" +
                "\n" +
                "    // Change L in Lab-Color\n" +
                "    color.a = color.a (((d - ambientlight) cos_a + d) cos_a + ambientlight);\n" +
                "\n" +
                "    // Next, specular reflection. Viewer is always assumed to be in direction (0,0,1)\n" +
                "    extern specularintensity real = 1;\n" +
                "\n" +
                "    extern shininess real = 8;\n" +
                "\n" +
                "    // r = 2 n l - l; v = 0:0:1\n" +
                "    var spec_refl = 2 cos_a nz - lz;\n" +
                "    \n" +
                "    // 100 because L in the Lab-Model is between 0 and 100\n" +
                "    if spec_refl > 0 then\n" +
                "        color.a = color.a + 100 * specularintensity * spec_refl ^ shininess;\n" +
                "\n" +
                "    color\n" +
                "}\n" +
                "\n" +
                "func do_pixel(x, y) {\n" +
                "    // two or three dimensions?\n" +
                "    def drawpixel = drawpixel_3d if light else drawpixel_2d;\n" +
                "    \n" +
                "    func drawaapixel(x, y) {\n" +
                "        0.25 (\n" +
                "            drawpixel(x - 0.375, y - 0.125) + \n" +
                "            drawpixel(x + 0.125, y - 0.375) + \n" +
                "            drawpixel(x + 0.375, y + 0.125) +\n" +
                "            drawpixel(x - 0.125, y + 0.375)         \n" +
                "        );\n" +
                "    }\n" +
                "\n" +
                "    // which function to apply?\n" +
                "    def fn = drawpixel if not supersampling else drawaapixel;\n" +
                "\n" +
                "    color = lab2int fn(x, y)\n" +
                "}\n" +
                "\n" +
                "// and finally call the drawing procedure\n" +
                "do_pixel(x, y)\n" +
                "}");

        runParser(MeelanParser.stmts());

        assertResult("{\n" +
                "    var x int, y int, color int;\n" +
                "    extern maxdepth int \"maxdepth\" = 120;\n" +
                "    extern juliaset bool \"juliaset\" = false;\n" +
                "    extern juliapoint cplx \"juliapoint\" = - 0.8 : 0.16;\n" +
                "    func escapetime(c, breakcondition) {\n" +
                "        var i int = 0, p cplx = juliapoint if juliaset else c, zlast cplx = 0, z cplx, znext cplx = 0;\n" +
                "        extern mandelinit expr \"mandelinit\" = \"0\";\n" +
                "        z = c if juliaset else mandelinit;\n" +
                "        extern function expr \"function\" = \"mandelbrot(z, p)\";\n" +
                "        var color quat;\n" +
                "        while {\n" +
                "            znext = function;\n" +
                "            not breakcondition(i, znext, z, zlast, c, p, color);\n" +
                "        } do {\n" +
                "            zlast = z;\n" +
                "            z = znext;\n" +
                "        };\n" +
                "        color;\n" +
                "    };\n" +
                "    func get_color(c, value) {\n" +
                "        func breakcondition(i, znext, z, zlast, c, p, color) {\n" +
                "            func bailoutcolor() {\n" +
                "                extern bailout real \"bailout\" = 128;\n" +
                "                extern max_power real \"max_power\" = 2;\n" +
                "                var smooth_i = smoothen(znext, bailout, max_power);\n" +
                "                extern bailoutvalue expr \"bailoutvalue\" = \"log(20 + i + smooth_i)\";\n" +
                "                value = bailoutvalue;\n" +
                "                extern bailouttransfer expr \"bailouttransfer\" = \"value\";\n" +
                "                extern bailoutpalette palette \"bailoutpalette\" = [[-16777114, -14522676, -1, -22016, -13434829]];\n" +
                "                color = bailoutpalette bailouttransfer;\n" +
                "            };\n" +
                "            func lakecolor() {\n" +
                "                extern epsilon real \"epsilon\" = 1.0E-9;\n" +
                "                extern lakevalue expr \"lakevalue\" = \"log(1 + rad znext)\";\n" +
                "                value = lakevalue;\n" +
                "                extern laketransfer expr \"laketransfer\" = \"arcnorm znext : value\";\n" +
                "                extern lakepalette palette \"lakepalette\" = [[-16777216, -16777216, -16777216, -16777216], [-65536, -256, -16711800, -16776961], [-30584, -120, -5570612, -7829249]];\n" +
                "                color = lakepalette laketransfer;\n" +
                "            };\n" +
                "            if not next(i, maxdepth) then {\n" +
                "                lakecolor();\n" +
                "                true;\n" +
                "            } else radrange(znext, z, bailout, epsilon, bailoutcolor(), lakecolor());\n" +
                "        };\n" +
                "        escapetime(c, breakcondition);\n" +
                "    };\n" +
                "    func get_color_test(c, value) {\n" +
                "        var rc = rad c;\n" +
                "        if rc < 1 then {\n" +
                "            value = circlefn rc + 5;\n" +
                "            int2lab -16776961;\n" +
                "        } else if rc =< 4 and rc >= 2 then {\n" +
                "            value = circlefn abs Sub(rc, 3);\n" +
                "            int2lab -65536;\n" +
                "        } else {\n" +
                "            value = - 10;\n" +
                "            int2lab -16711936;\n" +
                "        };\n" +
                "    };\n" +
                "    extern supersampling bool \"supersampling\" = false;\n" +
                "    extern light bool \"light\" = false;\n" +
                "    func drawpixel_2d(x, y) {\n" +
                "        var c cplx = map(x, y), value real;\n" +
                "        get_color(c, value);\n" +
                "    };\n" +
                "    func drawpixel_3d(x, y) {\n" +
                "        var c00 cplx = map(x, y), c10 cplx = map(x + 1, y + 0.5), c01 cplx = map(x + 0.5, y + 1), h00 real, h10 real, h01 real, color = Add(get_color(c00, h00) + get_color(c10, h10), get_color(c01, h01)) / 3;\n" +
                "        func height(value) {\n" +
                "            extern valuetransfer expr \"valuetransfer\" = \"value\";\n" +
                "            valuetransfer;\n" +
                "        };\n" +
                "        h00 = height h00;\n" +
                "        h01 = height h01;\n" +
                "        h10 = height h10;\n" +
                "        var xp = c10 - c00, xz = h10 - h00, yp = c01 - c00, yz = h01 - h00, np cplx = Sub(xp.y yz, xz yp.y) : Sub(xz yp.x, xp.x yz), nz real = xp.x yp.y - xp.y yp.x, nlen = sqrt Add(rad2 np, sqr nz);\n" +
                "        np = np / nlen;\n" +
                "        nz = nz / nlen;\n" +
                "        extern lightvector cplx \"lightvector\" = - 0.667 : - 0.667;\n" +
                "        def lz = sqrt Sub(1 - sqr re lightvector, sqr im lightvector);\n" +
                "        var cos_a real = dot(lightvector, np) + lz nz;\n" +
                "        extern lightintensity real \"lightintensity\" = 1;\n" +
                "        extern ambientlight real \"ambientlight\" = 0.5;\n" +
                "        def d = lightintensity / 2;\n" +
                "        color.a = color.a Add(((d - ambientlight) cos_a + d) cos_a, ambientlight);\n" +
                "        extern specularintensity real \"specularintensity\" = 1;\n" +
                "        extern shininess real \"shininess\" = 8;\n" +
                "        var spec_refl = 2 cos_a nz - lz;\n" +
                "        if spec_refl > 0 then color.a = color.a + 100 * specularintensity * spec_refl ^ shininess;\n" +
                "        color;\n" +
                "    };\n" +
                "    func do_pixel(x, y) {\n" +
                "        def drawpixel = drawpixel_3d if light else drawpixel_2d;\n" +
                "        func drawaapixel(x, y) {\n" +
                "            0.25 Add(drawpixel(x - 0.375, y - 0.125) + drawpixel(x + 0.125, y - 0.375) + drawpixel(x + 0.375, y + 0.125), drawpixel(x - 0.125, y + 0.375));\n" +
                "        };\n" +
                "        def fn = drawpixel if not supersampling else drawaapixel;\n" +
                "        color = lab2int fn(x, y);\n" +
                "    };\n" +
                "    do_pixel(x, y);\n" +
                "};\n");
    }


    private String input;
    private String result;

    private <A> String outString(Parser<A> parser, A result) {
        ConcreteSyntaxTree tree = parser.print(result);

        Assert.assertNotNull(tree);

        StringOutStream stream = new StringOutStream();
        CstPrinter printer = new MeelanPrinter(stream);
        printer.print(tree);

        return stream.toString();
    }

    private <A> String check(Parser<A> parser, String input) {
        // Checks for euqivalence of parse and recognize
        // Checks whether the whole input is parsed.
        // Checks whether the print and reparsed result are equivalent.
        MeelanStream stream = new MeelanStream(TokenStream.fromString(input));
        A result = parser.parse(stream);

        Assert.assertNotNull(result);


        MeelanStream stream2 = new MeelanStream(TokenStream.fromString(input));
        Assert.assertTrue(parser.recognize(stream2));
        Assert.assertEquals(stream.end(), stream2.end());

        Assert.assertTrue(Recognizer.eof(new Lexer()).recognize(stream));
        Assert.assertTrue(Recognizer.eof(new Lexer()).recognize(stream2));

        String outString = outString(parser, result);

        A verifiedResult = parser.parse(new MeelanStream(TokenStream.fromString(input)));

        String checkOutString = outString(parser, verifiedResult);

        Assert.assertEquals(outString, checkOutString);

        return outString;
    }

    private void assertResult(String expected) {
        Assert.assertEquals(expected, result);
    }

    private <A> void runParser(Parser<A> parser) {
        this.result = check(parser, input);
    }

    private void withSource(String input) {
        this.input = input;
    }

}