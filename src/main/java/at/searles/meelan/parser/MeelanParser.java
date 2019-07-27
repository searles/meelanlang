package at.searles.meelan.parser;


import at.searles.lexer.LexerWithHidden;
import at.searles.lexer.Token;
import at.searles.meelan.ops.Instruction;
import at.searles.meelan.ops.arithmetics.*;
import at.searles.meelan.ops.arithmetics.Neg;
import at.searles.meelan.ops.arithmetics.Recip;
import at.searles.meelan.ops.bool.*;
import at.searles.meelan.ops.bool.Not;
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
import at.searles.parsing.utils.builder.Setter;
import at.searles.parsing.utils.builder.SetterUnsafe;
import at.searles.regex.Regex;
import at.searles.regex.RegexParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MeelanParser {

    private LexerWithHidden lexer;

    private Token ws;
    private Token singleLineComment;
    private Token multilinelComment;

    private Mapping<CharSequence, Integer> toInt;
    private Mapping<CharSequence, Double> toReal;
    private Mapping<CharSequence, Integer> toHexColor;
    private Mapping<CharSequence, String> toUnquotedString;

    // XXX Parsers are public for tests, but they shouldn't be...

    public Parser<List<Tree>> stmts;
    public Parser<Tree> app;
    public Parser<Tree> expr;
    public Parser<Tree> stmt;

    public MeelanParser() {
        initLexer();
        initParser();
    }


    private Regex r(String regex) {
        return RegexParser.parse(regex);
    }


    public Recognizer op(String s) {
        return Recognizer.fromString(s, lexer, false);
    }

    public Recognizer op(String s, LabelIds labelId) {
        return Recognizer.fromString(s, lexer, false).annotate(labelId);
    }

    private void initLexer() {
        this.lexer = new LexerWithHidden();
        lexer.hiddenToken(r("[\n\r\t ]+"));
        lexer.hiddenToken(r("('//'.*'\n')!"));
        lexer.hiddenToken(r("('/*'.*'*/')!"));
    }

    private void initParser() {
        Parser<String> idString = Parser.fromToken(lexer.token(r("[A-Za-z_][0-9A-Za-z_]*")), new Mapping<CharSequence, String>() {
            @Override
            public String parse(Environment env, @NotNull CharSequence left, ParserStream stream) {
                return left.toString();
            }

            @Override
            public CharSequence left(Environment env, @NotNull String result) {
                return result;
            }
        }, true);

        Parser<String> quoted = Parser.fromToken(lexer.token(r("('\"'.*'\"')!")), new Mapping<CharSequence, String>() {
            @Override
            public String parse(Environment env, @NotNull CharSequence left, ParserStream stream) {
                return left.toString().substring(1, left.length() - 1);
            }

            @Override
            public CharSequence left(Environment env, @NotNull String result) {
                return "\"" + result + "\"";
            }
        }, true);

        Parser<Tree> integer = Parser.fromToken(lexer.token(r("'0'|[1-9][0-9]{0,8}")), Int.NUM, false); // max is 99999999
        Parser<Tree> id = Parser.fromToken(lexer.token(r("[A-Za-z_][0-9A-Za-z_]*")), Id.TOK, true);
        Parser<Tree> string = Parser.fromToken(lexer.token(r("('\"'.*'\"')!")), StringVal.TOK, false);
        Parser<Tree> hexColor = Parser.fromToken(lexer.token(r("'#'[0-9A-Fa-f]{1,8}")), Int.HEX, false);
        Parser<Tree> real = Parser.fromToken(lexer.token(r("('0'|[1-9][0-9]*)('.'[0-9]*)?([eE][+\\-]?[0-9]+)?")), Real.TOK, false);
        Parser<Tree> bool = Parser.fromToken(lexer.token(r("'true'|'false'")), Bool.TOK, false);

        Parser<Tree> primitives = integer.or(id).or(string).or(hexColor).or(real).or(bool);

        Ref<Tree> exprRef = new Ref<>("expr");

        Parser<List<Tree>> exprList = Utils.list(exprRef, op(",", LabelIds.SEPARATOR));

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
                .or(Utils.singleton(appRef.annotate(LabelIds.SPACING_BEFORE)), true);

        Parser<Tree> app = postfix.then(Reducer.opt(parameters.fold(App.CREATOR)));

        appRef.set(app);

        Ref<List<Tree>> stmtsRef = new Ref<>("stmt");

        Parser<Tree> block = op("{")
                .then(stmtsRef.annotate(LabelIds.INSIDE_BLOCK))
                .then(op("}"))
                .then(Block.CREATOR)
                .annotate(LabelIds.BLOCK);

        Parser<Tree> term = block.or(app);

        Ref<Tree> unexprRef = new Ref<>("unexpr");

        Parser<Tree> unexpr =
                op("-", LabelIds.PREFIX_OP).then(unexprRef).then(Instruction.unary(Neg.get()))
                .or(op("/", LabelIds.PREFIX_OP).then(unexprRef).then(Instruction.unary(Recip.get())))
                .or(term);

        unexprRef.set(unexpr);

        Parser<Tree> ifexpr = unexpr.then(
                Reducer.opt(
                     op("if", LabelIds.INFIX_KW)
                     .then(Utils.<IfElse.Builder, Tree>builder(IfElse.Builder.class, "thenPart"))
                     .then(Utils.setter("condition", exprRef))
                     .then(op("else", LabelIds.INFIX_KW))
                     .then(Utils.setter("elsePart", exprRef))
                     .then(Utils.build(IfElse.Builder.class))
                )
        );

        // a ( ':' a ( ':' )* )?

        Parser<Tree> consexpr = ifexpr.then(
            Reducer.opt(
                op(":", LabelIds.INFIX_OP)
                .then(Utils.prepend(Utils.list(ifexpr, op(":", LabelIds.INFIX_OP))))
                .then(Instruction.app(Cons.get()))
            )
        );

        Parser<Tree> powexpr = consexpr.then(Reducer.rep(
            op("^", LabelIds.INFIX_OP).then(consexpr.fold(Instruction.binary(Pow.get())))
        ));

        Parser<Tree> mulexpr = powexpr.then(Reducer.rep(
                op("*", LabelIds.INFIX_OP).then(powexpr.fold(Instruction.binary(Mul.get())))
                .or(op("/", LabelIds.INFIX_OP).then(powexpr.fold(Instruction.binary(Div.get()))))
                .or(op("%", LabelIds.INFIX_OP).then(powexpr.fold(Instruction.binary(Mod.get()))))
        ));

        Parser<Tree> sumexpr = mulexpr.then(Reducer.rep(
                op("+", LabelIds.INFIX_OP).<Tree, Tree>then(mulexpr.fold(Instruction.binary(Add.get())))
                .or(op("-", LabelIds.INFIX_OP).then(mulexpr.fold(Instruction.binary(Sub.get()))))
        ));

        Parser<Tree> compexpr = sumexpr.then(
                Reducer.<Tree>opt(
                    op("<", LabelIds.INFIX_OP).then(sumexpr).fold(Instruction.binary(Less.get()))
                    .or(op("=<", LabelIds.INFIX_OP).then(sumexpr).fold(Instruction.binary(LessEq.get())))
                    .or(op("==", LabelIds.INFIX_OP).then(sumexpr).fold(Instruction.binary(Equal.get())))
                    .or(op("><", LabelIds.INFIX_OP).then(sumexpr).fold(Instruction.binary(NonEqual.get())))
                    .or(op(">=", LabelIds.INFIX_OP).then(sumexpr).fold(Instruction.binary(GreaterEqual.get())))
                    .or(op(">", LabelIds.INFIX_OP).then(sumexpr).fold(Instruction.binary(Greater.get())))
                )
        );

        Ref<Tree> literalRef = new Ref<>("literal");

        Parser<Tree> literal =
                op("not", LabelIds.PREFIX_KW).then(literalRef).then(Instruction.unary(Not.get()))
                .or(compexpr);

        literalRef.set(literal);

        Parser<Tree> andexpr = op("and", LabelIds.INFIX_KW).join(literal, Instruction.binary(And.get()));

        Parser<Tree> orexpr = op("or", LabelIds.INFIX_KW).join(andexpr, Instruction.binary(Or.get()));

        Parser<Tree> assignexpr = orexpr.then(
                Reducer.opt(
                        op("=", LabelIds.INFIX_OP).then(orexpr).fold(Assign.CREATE)
                )
        );

        Parser<Tree> expr = assignexpr;

        exprRef.set(assignexpr);

        // now for statements

        Ref<Tree> stmtRef = new Ref<>("stmt");

        Parser<Tree> whilestmt= op("while", LabelIds.PREFIX_KW).then(expr).then(
                op("do", LabelIds.INFIX_KW).then(stmtRef.fold(While.CREATE_DO))
                        .or(While.CREATE));

        Parser<Tree> forstmt =
                op("for", LabelIds.PREFIX_KW)
                .then(Utils.builder(ForEach.Builder.class))
                .then(Utils.setter("varName", idString))
                .then(op("in", LabelIds.INFIX_KW))
                .then(Utils.setter("vector", expr))
                .then(op("do", LabelIds.INFIX_KW))
                .then(Utils.setter("body", stmtRef))
                .then(Utils.build(ForEach.Builder.class));

        Parser<Tree> ifstmt = op("if", LabelIds.PREFIX_KW)
                .then(Utils.builder(IfElse.Builder.class))
                .then(Utils.setter("condition", expr))
                .then(op("then", LabelIds.INFIX_KW))
                .then(Utils.setter("thenPart", stmtRef))
                .then(
                    Reducer.opt(
                        op("else", LabelIds.INFIX_KW)
                        .then(Utils.setter("elsePart", stmtRef))
                    )
                )
                .then(Utils.build(IfElse.Builder.class));

        Parser<Tree> stmt = whilestmt.or(forstmt).or(ifstmt).or(expr);

        stmtRef.set(stmt);

        // now for declarations

        Parser<Tree> externDef =
                op("extern", LabelIds.PREFIX_KW)
                .then(Utils.builder(ExternDeclaration.Builder.class))
                .then(Utils.setter("id", idString))
                .then(Utils.setter("type", idString.annotate(LabelIds.SPACING_BEFORE)))
                .then(Reducer.opt(Utils.setter("description", quoted.annotate(LabelIds.SPACING_BEFORE))))
                .then(op("=", LabelIds.INFIX_OP))
                .then(Utils.setter("value", expr))
                .then(Utils.build(ExternDeclaration.Builder.class));

        Parser<List<String>> arguments = op("(").then(
                Utils.list(idString, op(",", LabelIds.SEPARATOR))
                .then(op(")")));

        Parser<Tree> funcDef =
                op("func", LabelIds.PREFIX_KW)
                .then(Utils.builder(Definition.FuncBuilder.class))
                .then(Utils.setter("id", idString))
                .then(Utils.setter("args", arguments))
                .then(Utils.setter("body", stmt.annotate(LabelIds.SPACING_BEFORE)))
                .then(Utils.build(Definition.FuncBuilder.class));

        Parser<List<String>> possiblyEmptyArguments = arguments.or(Utils.empty());

        Parser<Tree> templateDef =
                op("template", LabelIds.PREFIX_KW)
                .then(Utils.builder(Definition.TemplateBuilder.class))
                .then(Utils.setter("id", idString))
                .then(Utils.setter("args", possiblyEmptyArguments))
                .then(Utils.setter("body", block.annotate(LabelIds.SPACING_BEFORE)))
                .then(Utils.build(Definition.TemplateBuilder.class));

        Parser<Tree> definition =
                op("def", LabelIds.PREFIX_KW)
                .then(idString)
                .then(op("=", LabelIds.INFIX_OP))
                .then(expr.fold(Definition.CREATE));

        Parser<Tree> defs = externDef.or(funcDef).or(templateDef).or(definition);

        Parser<Tree> varDecl =
                Utils.builder(VarDeclaration.Builder.class)
                .then(Utils.setter("id", idString))
                .then(Reducer.opt(Utils.setter("type", idString.annotate(LabelIds.SPACING_BEFORE))))
                .then(Reducer.opt(Utils.setter("value", op("=", LabelIds.INFIX_OP).then(assignexpr))))
                .then(Utils.build(VarDeclaration.Builder.class));

        Reducer<List<Tree>, List<Tree>> vars =
                op("var", LabelIds.PREFIX_KW)
                .then(op(",", LabelIds.SEPARATOR).joinPlus(Utils.append(varDecl, 0)));

        Parser<Tree> objectDecl = // object a = A(c,b)
                idString.then(
                        op("=", LabelIds.INFIX_OP).then(expr)
                        .fold(ObjectDeclaration.CREATE));

        Reducer<List<Tree>, List<Tree>> objects =
                op("object", LabelIds.PREFIX_KW).then(
                        op(",", LabelIds.SEPARATOR).joinPlus(Utils.append(objectDecl, 0)));

        Reducer<List<Tree>, List<Tree>> stmtAppender =
                vars
                .or(objects)
                .or(Utils.append(defs, 0))
                .or(Utils.append(stmt, 0))
                .then(op(";", LabelIds.STMT_END)
                .opt(true));

        this.stmts = Utils.<Tree>empty()
                .then(Reducer.rep(stmtAppender));

        stmtsRef.set(stmts);

        this.app = app;
        this.expr = expr;
        this.stmt = stmt;
    }

    public List<Tree> parse(Environment env, ParserStream stream) {
        return stmts.parse(env, stream);
    }

    public Tree parseExpr(Environment env, ParserStream stream) {
        return stmt.parse(env, stream);
    }

    public String print(Environment env, List<Tree> program) {
        return stmts.print(env, program).toString();
    }

    public boolean recognize(Environment env, ParserStream stream) {
        return stmts.recognize(env, stream);
    }
}
