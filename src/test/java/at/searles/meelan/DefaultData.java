package at.searles.meelan;

import at.searles.commons.math.Cplx;
import at.searles.meelan.ops.ConstInstruction;
import at.searles.meelan.ops.InstructionSet;
import at.searles.meelan.ops.analysis.*;
import at.searles.meelan.ops.arithmetics.*;
import at.searles.meelan.ops.bool.And;
import at.searles.meelan.ops.bool.Not;
import at.searles.meelan.ops.bool.Or;
import at.searles.meelan.ops.color.*;
import at.searles.meelan.ops.comparison.*;
import at.searles.meelan.ops.complex.*;
import at.searles.meelan.ops.cons.Cons;
import at.searles.meelan.ops.cons.IntToReal;
import at.searles.meelan.ops.cons.RealToInt;
import at.searles.meelan.ops.graphics.Box;
import at.searles.meelan.ops.graphics.Circle;
import at.searles.meelan.ops.graphics.Line;
import at.searles.meelan.ops.graphics.Segment;
import at.searles.meelan.ops.numeric.*;
import at.searles.meelan.ops.rewriting.Derive;
import at.searles.meelan.ops.rewriting.Horner;
import at.searles.meelan.ops.rewriting.Newton;
import at.searles.meelan.ops.rewriting.Solve;
import at.searles.meelan.ops.special.*;
import at.searles.meelan.ops.sys.*;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.symbols.IdResolver;
import at.searles.meelan.values.CplxVal;
import at.searles.meelan.values.Real;

public class DefaultData {

    public static InstructionSet getDefaultInstructionSet() {
        InstructionSet instructionSet = new InstructionSet();

        // Constructors
        instructionSet.addSystemInstruction("cons", Cons.get());
        instructionSet.addSystemInstruction("real", RealToInt.get());
        instructionSet.addSystemInstruction(IntToReal.get());

        // sys
        instructionSet.addSystemInstruction("debug", Debug.get());
        instructionSet.addSystemInstruction("next", Next.get());
        instructionSet.addSystemInstruction("map", MapCoordinates.get());
        instructionSet.addSystemInstruction(Mov.get());
        instructionSet.addSystemInstruction(Jump.get());
        instructionSet.addSystemInstruction(JumpRel.get());

        instructionSet.addInstruction("length", Length.get());
        instructionSet.addInstruction("select", Select.get());

        instructionSet.addInstruction("error", RaiseError.get());
        instructionSet.addInstruction("derive", Derive.get());
        instructionSet.addInstruction("newton", Newton.get());
        instructionSet.addInstruction("horner", Horner.get());
        instructionSet.addInstruction("solve", Solve.get());

        instructionSet.addInstruction("PI", new ConstInstruction(new Real(Math.PI)));
        instructionSet.addInstruction("E", new ConstInstruction(new Real(Math.E)));
        instructionSet.addInstruction("I", new ConstInstruction(new CplxVal(new Cplx(0, 1))));

        // bools
        instructionSet.addInstruction("and", And.get());
        instructionSet.addInstruction("or", Or.get());
        instructionSet.addInstruction("not", Not.get());

        // comparisons
        instructionSet.addSystemInstruction("eq", Equal.get());
        instructionSet.addSystemInstruction("less", Less.get());
        instructionSet.addInstruction("neq", NonEqual.get());
        instructionSet.addInstruction("geq", GreaterEqual.get());
        instructionSet.addInstruction("leq", LessEq.get());
        instructionSet.addInstruction("greater", Greater.get());

        // Arithmetics
        instructionSet.addSystemInstruction("add", Add.get());
        instructionSet.addSystemInstruction("sub", Sub.get());
        instructionSet.addSystemInstruction("mul", Mul.get());
        instructionSet.addSystemInstruction("div", Div.get());
        instructionSet.addSystemInstruction("mod", Mod.get());
        instructionSet.addSystemInstruction("pow", Pow.get());

        instructionSet.addSystemInstruction("recip", Recip.get());
        instructionSet.addSystemInstruction("neg", Neg.get());


        // analysis
        instructionSet.addSystemInstruction("atan", Atan.get());
        instructionSet.addSystemInstruction("atanh", Atanh.get());
        instructionSet.addSystemInstruction("cos", Cos.get());
        instructionSet.addSystemInstruction("cosh", Cosh.get());
        instructionSet.addSystemInstruction("exp", Exp.get());
        instructionSet.addSystemInstruction("log", Log.get());
        instructionSet.addSystemInstruction("sin", Sin.get());
        instructionSet.addSystemInstruction("sinh", Sinh.get());
        instructionSet.addSystemInstruction("sqr", Sqr.get());
        instructionSet.addSystemInstruction("sqrt", Sqrt.get());
        instructionSet.addSystemInstruction("tan", Tan.get());
        instructionSet.addSystemInstruction("tanh", Tanh.get());

        // numeric
        instructionSet.addSystemInstruction("abs", Abs.get());
        instructionSet.addSystemInstruction("floor", Floor.get());
        instructionSet.addSystemInstruction("ceil", Ceil.get());
        instructionSet.addSystemInstruction("fract", Fract.get());
        instructionSet.addSystemInstruction("dot", Dot.get());
        instructionSet.addSystemInstruction("circlefn", CircleFn.get());
        instructionSet.addSystemInstruction("scalarmul", ScalarMul.get());
        instructionSet.addSystemInstruction("max", Max.get());
        instructionSet.addSystemInstruction("min", Min.get());

        // complex
        instructionSet.addSystemInstruction("conj", Conj.get());
        instructionSet.addSystemInstruction("flip", Flip.get());
        instructionSet.addSystemInstruction("rabs", RAbs.get());
        instructionSet.addSystemInstruction("iabs", IAbs.get());
        instructionSet.addSystemInstruction("norm", Norm.get());
        instructionSet.addSystemInstruction("polar", Polar.get());
        instructionSet.addSystemInstruction("rect", Rect.get());
        instructionSet.addSystemInstruction("arc", Arc.get());
        instructionSet.addSystemInstruction("arcnorm", Arcnorm.get());
        instructionSet.addSystemInstruction("rad", Rad.get());
        instructionSet.addSystemInstruction("rad2", Rad2.get());
        instructionSet.addSystemInstruction("dist", Dist.get());
        instructionSet.addSystemInstruction("dist2", Dist2.get());
        instructionSet.addSystemInstruction("re", Re.get());
        instructionSet.addSystemInstruction("im", Im.get());

        // graphics
        instructionSet.addSystemInstruction("box", Box.get());
        instructionSet.addSystemInstruction("circle", Circle.get());
        instructionSet.addSystemInstruction("line", Line.get());
        instructionSet.addSystemInstruction("segment", Segment.get());

        // colors
        instructionSet.addSystemInstruction("int2lab", Int2Lab.get());
        instructionSet.addSystemInstruction("int2rgb", Int2Rgb.get());
        instructionSet.addSystemInstruction("lab2int", Lab2Int.get());
        instructionSet.addSystemInstruction("lab2rgb", Lab2Rgb.get());
        instructionSet.addSystemInstruction("rgb2int", Rgb2Int.get());
        instructionSet.addSystemInstruction("rgb2lab", Rgb2Lab.get());

        instructionSet.addSystemInstruction("over", Over.get());

        // special
        instructionSet.addSystemInstruction("distless", DistLess.get());
        instructionSet.addSystemInstruction("radless", RadLess.get());
        instructionSet.addSystemInstruction("radrange", RadRange.get());
        instructionSet.addInstruction("smooth", Smooth.get());
        instructionSet.addSystemInstruction("smoothen", Smoothen.get());
        instructionSet.addSystemInstruction("mandelbrot", Mandelbrot.get());

        return instructionSet;
    }

    public static IdResolver getDefaultExternData() {
        return new IdResolver() {
            InstructionSet instrs = getDefaultInstructionSet();

            @Override
            public Tree valueOf(String id) {
                return instrs.get(id);
            }
        };
    }
}
