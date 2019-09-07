package at.searles.meelan.parser;

import at.searles.lexer.LexerWithHidden;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.arithmetics.*;
import at.searles.meelan.ops.bool.*;
import at.searles.meelan.ops.comparison.*;
import at.searles.meelan.ops.cons.Cons;
import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.Vec;
import at.searles.meelan.optree.compiled.*;
import at.searles.meelan.optree.inlined.*;
import at.searles.meelan.values.Bool;
import at.searles.meelan.values.Int;
import at.searles.meelan.values.Real;
import at.searles.meelan.values.StringVal;
import at.searles.parsing.*;
import at.searles.parsing.utils.Utils;
import at.searles.regex.Regex;
import at.searles.regex.RegexParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MeelanParser {

    private static class Holder {
        static final MeelanParser INSTANCE = new MeelanParser();
    }

    public static Parser<List<Tree>> stmts() {
        return Holder.INSTANCE.stmts;
    }

    public static Parser<Tree> stmt() {
        return Holder.INSTANCE.stmt;
    }

    public static Parser<Tree> expr() {
        return Holder.INSTANCE.expr;
    }

    public static int whiteSpace() {
        return Holder.INSTANCE.wsId;
    }

    public static int singleLineComment() {
        return Holder.INSTANCE.slCommentId;
    }

    public static int multiLineComment() {
        return Holder.INSTANCE.mlCommentId;
    }

    private LexerWithHidden lexer;

    private Parser<List<Tree>> stmts;
    private Parser<Tree> expr;
    private Parser<Tree> stmt;

    private int mlCommentId;
    private int slCommentId;
    private int wsId;

    public enum Annotation {
        STMT,
        EXPR,
        BLOCK,
        IN_BLOCK,
        STRING,
        VALUE,
        SEPARATOR, // separates two elements. Usually spacing after separator
        BREAK,
        PREFIX_OP,
        INFIX_OP,
        PREFIX_KW,
        INFIX_KW
    }

    private MeelanParser() {
        initLexer();
        initParser();
    }

    private Regex r(String regex) {
        return RegexParser.parse(regex);
    }

    private Recognizer op(String s) {
        return Recognizer.fromString(s, lexer, false);
    }

    private Recognizer op(String s, Annotation labelId) {
        return Recognizer.fromString(s, lexer, false).annotate(labelId);
    }

    private void initLexer() {
        this.lexer = new LexerWithHidden();
        wsId = lexer.addHiddenToken(r("[\n\r\t ]+"));
        slCommentId = lexer.addHiddenToken(r("('//'.*'\n')!"));
        mlCommentId = lexer.addHiddenToken(r("('/*'.*'*/')!"));
    }

    private void initParser() {
        Parser<String> idString = Parser.fromToken(lexer.token(r("[A-Za-z_][0-9A-Za-z_]*")), new Mapping<CharSequence, String>() {
            @Override
            public String parse(ParserStream stream, @NotNull CharSequence left) {
                return left.toString();
            }

            @Override
            public CharSequence left(@NotNull String result) {
                return result;
            }
        }, true);

        Parser<String> quoted = Parser.fromToken(lexer.token(r("('\"'.*'\"')!")), new Mapping<CharSequence, String>() {
            @Override
            public String parse(ParserStream stream, @NotNull CharSequence left) {
                return left.toString().substring(1, left.length() - 1);
            }

            @Override
            public CharSequence left(@NotNull String result) {
                return "\"" + result + "\"";
            }
        }, true).annotate(Annotation.STRING);

        Parser<Tree> id = Parser.fromToken(lexer.token(r("[A-Za-z_][0-9A-Za-z_]*")), Id.TOK, true);

        Parser<Tree> integer = Parser.fromToken(lexer.token(r("'0'|[1-9][0-9]{0,8}")), Int.NUM, false); // max is 99999999
        Parser<Tree> string = Parser.fromToken(lexer.token(r("('\"'.*'\"')!")), StringVal.TOK, false);
        Parser<Tree> hexColor = Parser.fromToken(lexer.token(r("'#'[0-9A-Fa-f]{1,8}")), Int.HEX, false);
        Parser<Tree> real = Parser.fromToken(lexer.token(r("('0'|[1-9][0-9]*)('.'[0-9]*)?([eE][+\\-]?[0-9]+)?")), Real.TOK, false);
        Parser<Tree> bool = Parser.fromToken(lexer.token(r("'true'|'false'")), Bool.TOK, false);

        Parser<Tree> primitives = id.or(
                integer.or(string).or(hexColor).or(real).or(bool)
                .annotate(Annotation.VALUE)
        );

        Ref<Tree> exprRef = new Ref<>("expr");

        Parser<List<Tree>> exprList = Utils.list(exprRef, op(",", Annotation.SEPARATOR));

        Parser<Tree> vector =
                op("[")
                .then(exprList)
                .then(op("]"))
                .then(Vec.CREATOR);

        Parser<Tree> primary = primitives.or(vector)
                .or(
                        op("(")
                        .then(exprRef)
                        .then(op(")")));

        Parser<Tree> postfix = primary.then(
                Reducer.rep(op(".").then(idString.fold(Qualified.CREATOR))));

        Ref<Tree> appRef = new Ref<>("app");

        // If there is only one argument, inversion will take the second branch.
        Parser<List<Tree>> parameters =
                        op("(")
                        .then(exprList)
                        .then(op(")"))
                                .then(Reducer.opt(
                                        appRef.fold(App.APPLY_TUPEL)
                                ))
                .or(Utils.singleton(appRef.annotate(Annotation.BREAK)), true);

        Parser<Tree> app = postfix.then(Reducer.opt(parameters.fold(App.CREATOR)));

        appRef.set(app);

        Ref<List<Tree>> stmtsRef = new Ref<>("stmt");

        Parser<Tree> block = op("{")
                .then(stmtsRef.annotate(Annotation.IN_BLOCK))
                .then(op("}"))
                .then(Block.CREATOR)
                .annotate(Annotation.BLOCK);

        Parser<Tree> term = block.or(app);

        Ref<Tree> unexprRef = new Ref<>("unexpr");

        Parser<Tree> unexpr =
                op("-", Annotation.PREFIX_OP).then(unexprRef).then(Instruction.unary(Neg.get()))
                .or(op("/", Annotation.PREFIX_OP).then(unexprRef).then(Instruction.unary(Recip.get())))
                .or(term);

        unexprRef.set(unexpr);

        Parser<Tree> ifexpr = unexpr.then(
                Reducer.opt(
                     op("if", Annotation.INFIX_KW)
                     .then(Utils.<IfElse.Builder, Tree>builder(IfElse.Builder.class, "thenPart"))
                     .then(Utils.setter("condition", exprRef))
                     .then(op("else", Annotation.INFIX_KW))
                     .then(Utils.setter("elsePart", exprRef))
                     .then(Utils.build(IfElse.Builder.class))
                )
        );

        // a ( ':' a ( ':' )* )?

        Parser<Tree> consexpr = ifexpr.then(
            Reducer.opt(
                op(":", Annotation.INFIX_OP)
                .then(Utils.binary(ifexpr))
                .then(
                    Reducer.rep(
                        Utils.append(op(":", Annotation.INFIX_OP).then(ifexpr), 2)
                    )
                )
                .then(Instruction.app(Cons.get()))
            )
        );

        Parser<Tree> powexpr = consexpr.then(Reducer.rep(
            op("^", Annotation.INFIX_OP).then(consexpr.fold(Instruction.binary(Pow.get())))
        ));

        Parser<Tree> mulexpr = powexpr.then(Reducer.rep(
                op("*", Annotation.INFIX_OP).then(powexpr.fold(Instruction.binary(Mul.get())))
                .or(op("/", Annotation.INFIX_OP).then(powexpr.fold(Instruction.binary(Div.get()))))
                .or(op("%", Annotation.INFIX_OP).then(powexpr.fold(Instruction.binary(Mod.get()))))
        ));

        Parser<Tree> sumexpr = mulexpr.then(Reducer.rep(
                op("+", Annotation.INFIX_OP).then(mulexpr.fold(Instruction.binary(Add.get())))
                .or(op("-", Annotation.INFIX_OP).then(mulexpr.fold(Instruction.binary(Sub.get()))))
        ));

        Parser<Tree> compexpr = sumexpr.then(
                Reducer.opt(
                    op("<", Annotation.INFIX_OP).then(sumexpr).fold(Instruction.binary(Less.get()))
                    .or(op("=<", Annotation.INFIX_OP).then(sumexpr).fold(Instruction.binary(LessEq.get())))
                    .or(op("==", Annotation.INFIX_OP).then(sumexpr).fold(Instruction.binary(Equal.get())))
                    .or(op("><", Annotation.INFIX_OP).then(sumexpr).fold(Instruction.binary(NonEqual.get())))
                    .or(op(">=", Annotation.INFIX_OP).then(sumexpr).fold(Instruction.binary(GreaterEqual.get())))
                    .or(op(">", Annotation.INFIX_OP).then(sumexpr).fold(Instruction.binary(Greater.get())))
                )
        );

        Ref<Tree> literalRef = new Ref<>("literal");

        Parser<Tree> literal =
                op("not", Annotation.PREFIX_KW).then(literalRef).then(Instruction.unary(Not.get()))
                .or(compexpr);

        literalRef.set(literal);

        Parser<Tree> andexpr = literal.then(Reducer.rep(op("and", Annotation.INFIX_KW).then(literal.fold(Instruction.binary(And.get())))));

        Parser<Tree> orexpr = andexpr.then(Reducer.rep(op("or", Annotation.INFIX_KW).then(andexpr.fold(Instruction.binary(Or.get())))));

        Parser<Tree> assignexpr = orexpr.then(
                Reducer.opt(
                        op("=", Annotation.INFIX_OP).then(orexpr).fold(Assign.CREATE)
                )
        );

        expr = assignexpr.annotate(Annotation.EXPR);

        exprRef.set(expr);

        // now for statements

        Ref<Tree> stmtRef = new Ref<>("stmt");

        Parser<Tree> whilestmt= op("while", Annotation.PREFIX_KW).then(expr).then(
                op("do", Annotation.INFIX_KW).then(stmtRef.fold(While.CREATE_DO))
                        .or(While.CREATE));

        Parser<Tree> forstmt =
                op("for", Annotation.PREFIX_KW)
                .then(Utils.builder(ForEach.Builder.class))
                .then(Utils.setter("varName", idString))
                .then(op("in", Annotation.INFIX_KW))
                .then(Utils.setter("vector", expr))
                .then(op("do", Annotation.INFIX_KW))
                .then(Utils.setter("body", stmtRef))
                .then(Utils.build(ForEach.Builder.class));

        Parser<Tree> ifstmt = op("if", Annotation.PREFIX_KW)
                .then(Utils.builder(IfElse.Builder.class))
                .then(Utils.setter("condition", expr))
                .then(op("then", Annotation.INFIX_KW))
                .then(Utils.setter("thenPart", stmtRef))
                .then(
                    Reducer.opt(
                        op("else", Annotation.INFIX_KW)
                        .then(Utils.setter("elsePart", stmtRef))
                    )
                )
                .then(Utils.build(IfElse.Builder.class));

        stmt = whilestmt.or(forstmt).or(ifstmt).or(expr);

        stmtRef.set(stmt);

        // now for declarations

        Parser<Tree> externDef =
                op("extern", Annotation.PREFIX_KW)
                .then(Utils.builder(ExternDeclaration.Builder.class))
                .then(Utils.setter("id", idString))
                .then(Utils.setter("type", idString.annotate(Annotation.BREAK)))
                .then(Reducer.opt(Utils.setter("description", quoted.annotate(Annotation.BREAK))))
                .then(op("=", Annotation.INFIX_OP))
                .then(Utils.setter("value", expr))
                .then(Utils.build(ExternDeclaration.Builder.class));

        Parser<List<String>> arguments = op("(").then(
                Utils.list(idString, op(",", Annotation.SEPARATOR))
                .then(op(")")));

        Parser<Tree> funcDef =
                op("func", Annotation.PREFIX_KW)
                .then(Utils.builder(Definition.FuncBuilder.class))
                .then(Utils.setter("id", idString))
                .then(Utils.setter("args", arguments))
                .then(Utils.setter("body", stmt.annotate(Annotation.BREAK)))
                .then(Utils.build(Definition.FuncBuilder.class));

        Parser<List<String>> possiblyEmptyArguments = arguments.or(Utils.empty());

        Parser<Tree> templateDef =
                op("template", Annotation.PREFIX_KW)
                .then(Utils.builder(Definition.TemplateBuilder.class))
                .then(Utils.setter("id", idString))
                .then(Utils.setter("args", possiblyEmptyArguments))
                .then(Utils.setter("body", block.annotate(Annotation.BREAK)))
                .then(Utils.build(Definition.TemplateBuilder.class));

        Parser<Tree> definition =
                op("def", Annotation.PREFIX_KW)
                .then(idString)
                .then(op("=", Annotation.INFIX_OP))
                .then(expr.fold(Definition.CREATE));

        Parser<Tree> defs = externDef.or(funcDef).or(templateDef).or(definition);

        // variable declarations may be chained
        // XXX Meelan2: Change chaining-rules.

        Parser<Tree> varDecl =
                Utils.builder(VarDeclaration.Builder.class)
                .then(Utils.setter("id", idString))
                .then(Reducer.opt(Utils.setter("type", idString.annotate(Annotation.BREAK))))
                .then(Reducer.opt(Utils.setter("value", op("=", Annotation.INFIX_OP).then(assignexpr))))
                .then(Utils.build(VarDeclaration.Builder.class));

        Reducer<List<Tree>, List<Tree>> vars =
                op("var", Annotation.PREFIX_KW)
                .then(op(",", Annotation.SEPARATOR).joinPlus(Utils.append(varDecl, 0)));

        Parser<Tree> objectDecl = // object a = A(c,b)
                idString.then(
                        op("=", Annotation.INFIX_OP).then(expr)
                        .fold(ObjectDeclaration.CREATE));

        Reducer<List<Tree>, List<Tree>> objects =
                op("object", Annotation.PREFIX_KW).then(
                        op(",", Annotation.SEPARATOR).joinPlus(Utils.append(objectDecl, 0)));

        Reducer<List<Tree>, List<Tree>> stmtAppender =
                vars
                .or(objects)
                .or(Utils.append(defs, 0))
                .or(Utils.append(stmt, 0))
                .then(op(";").opt(true)).annotate(Annotation.STMT);

        stmts = Utils.<Tree>empty()
                .then(Reducer.rep(stmtAppender));

        stmtsRef.set(stmts);
    }
}
