package at.searles.meelan.parser;

import at.searles.lexer.Lexer;
import at.searles.lexer.SkipTokenizer;
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

    public static Recognizer eof() {
        // XXX This should rather be part of the parser itself,
        // but due to backwards compatibility it is not.
        return Holder.INSTANCE.eof;
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

    private SkipTokenizer lexer;

    private Parser<List<Tree>> stmts;
    private Parser<Tree> expr;
    private Parser<Tree> stmt;
    private Recognizer eof;

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
        KEYWORD_INFIX,
        KEYWORD_PREFIX,
        KEYWORD_DEF,
    }

    private MeelanParser() {
        initLexer();
        initParser();
        this.eof = Recognizer.eof(lexer);
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
        this.lexer = new SkipTokenizer(new Lexer());
        wsId = lexer.add(r("[\n\r\t ]+"));
        slCommentId = lexer.add(r("'//'[^\n]*"));
        mlCommentId = lexer.add(r("('/*'.*'*/')!"));
        
        lexer.addSkipped(wsId);
        lexer.addSkipped(slCommentId);
        lexer.addSkipped(mlCommentId);
    }

    private void initParser() {
        Parser<String> idString = Parser.fromRegex(r("[A-Za-z_][0-9A-Za-z_]*"), lexer, true, new Mapping<CharSequence, String>() {
            @Override
            public String parse(ParserStream stream, @NotNull CharSequence left) {
                return left.toString();
            }

            @Override
            public CharSequence left(@NotNull String result) {
                return result;
            }
        });

        Parser<String> quoted = Parser.fromRegex(r("('\"'.*'\"')!"), lexer, true, new Mapping<CharSequence, String>() {
            @Override
            public String parse(ParserStream stream, @NotNull CharSequence left) {
                return left.toString().substring(1, left.length() - 1);
            }

            @Override
            public CharSequence left(@NotNull String result) {
                return "\"" + result + "\"";
            }
        }).annotate(Annotation.STRING);

        Parser<Tree> id = Parser.fromRegex(r("[A-Za-z_][0-9A-Za-z_]*"), lexer, true, Id.TOK);

        Parser<Tree> integer = Parser.fromRegex(r("'0'|[1-9][0-9]{0,8}"), lexer, false, Int.NUM); // max is 99999999
        Parser<Tree> string = Parser.fromRegex(r("('\"'.*'\"')!"), lexer, false, StringVal.TOK);
        Parser<Tree> hexColor = Parser.fromRegex(r("'#'[0-9A-Fa-f]{1,8}"), lexer, false, Int.HEX);
        Parser<Tree> real = Parser.fromRegex(r("('0'|[1-9][0-9]*)('.'[0-9]*)?([eE][+\\-]?[0-9]+)?"), lexer, false, Real.TOK);
        Parser<Tree> bool = Parser.fromRegex(r("'true'|'false'"), lexer, false, Bool.TOK);

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
                op("-", Annotation.KEYWORD_PREFIX).then(unexprRef).then(Instruction.unary(Neg.get()))
                .or(op("/", Annotation.KEYWORD_PREFIX).then(unexprRef).then(Instruction.unary(Recip.get())))
                .or(term);

        unexprRef.set(unexpr);

        Parser<Tree> ifexpr = unexpr.then(
                Reducer.opt(
                     op("if", Annotation.KEYWORD_INFIX)
                     .then(Utils.<Tree>properties("thenBranch"))
                     .then(Utils.put("condition", exprRef))
                     .then(op("else", Annotation.KEYWORD_INFIX))
                     .then(Utils.put("elseBranch", exprRef))
                     .then(Utils.create(IfElse.class, "condition", "thenBranch", "elseBranch"))
                )
        );

        // a ( ':' a ( ':' )* )?

        Parser<Tree> consexpr = ifexpr.then(
            Reducer.opt(
                op(":", Annotation.KEYWORD_INFIX)
                .then(Utils.binary(ifexpr))
                .then(
                    Reducer.rep(
                        Utils.append(op(":", Annotation.KEYWORD_INFIX).then(ifexpr), 2)
                    )
                )
                .then(Instruction.app(Cons.get()))
            )
        );

        Parser<Tree> powexpr = consexpr.then(Reducer.rep(
            op("^", Annotation.KEYWORD_INFIX).then(consexpr.fold(Instruction.binary(Pow.get())))
        ));

        Parser<Tree> mulexpr = powexpr.then(Reducer.rep(
                op("*", Annotation.KEYWORD_INFIX).then(powexpr.fold(Instruction.binary(Mul.get())))
                .or(op("/", Annotation.KEYWORD_INFIX).then(powexpr.fold(Instruction.binary(Div.get()))))
                .or(op("%", Annotation.KEYWORD_INFIX).then(powexpr.fold(Instruction.binary(Mod.get()))))
        ));

        Parser<Tree> sumexpr = mulexpr.then(Reducer.rep(
                op("+", Annotation.KEYWORD_INFIX).then(mulexpr.fold(Instruction.binary(Add.get())))
                .or(op("-", Annotation.KEYWORD_INFIX).then(mulexpr.fold(Instruction.binary(Sub.get()))))
        ));

        Parser<Tree> compexpr = sumexpr.then(
                Reducer.opt(
                    op("<", Annotation.KEYWORD_INFIX).then(sumexpr).fold(Instruction.binary(Less.get()))
                    .or(op("=<", Annotation.KEYWORD_INFIX).then(sumexpr).fold(Instruction.binary(LessEq.get())))
                    .or(op("==", Annotation.KEYWORD_INFIX).then(sumexpr).fold(Instruction.binary(Equal.get())))
                    .or(op("><", Annotation.KEYWORD_INFIX).then(sumexpr).fold(Instruction.binary(NonEqual.get())))
                    .or(op(">=", Annotation.KEYWORD_INFIX).then(sumexpr).fold(Instruction.binary(GreaterEqual.get())))
                    .or(op(">", Annotation.KEYWORD_INFIX).then(sumexpr).fold(Instruction.binary(Greater.get())))
                )
        );

        Ref<Tree> literalRef = new Ref<>("literal");

        Parser<Tree> literal =
                op("not", Annotation.KEYWORD_PREFIX).then(literalRef).then(Instruction.unary(Not.get()))
                .or(compexpr);

        literalRef.set(literal);

        Parser<Tree> andexpr = literal.then(Reducer.rep(op("and", Annotation.KEYWORD_INFIX).then(literal.fold(Instruction.binary(And.get())))));

        Parser<Tree> orexpr = andexpr.then(Reducer.rep(op("or", Annotation.KEYWORD_INFIX).then(andexpr.fold(Instruction.binary(Or.get())))));

        Parser<Tree> assignexpr = orexpr.then(
                Reducer.opt(
                        op("=", Annotation.KEYWORD_INFIX).then(orexpr).fold(Assign.CREATE)
                )
        );

        expr = assignexpr.annotate(Annotation.EXPR);

        exprRef.set(expr);

        // now for statements

        Ref<Tree> stmtRef = new Ref<>("stmt");

        Parser<Tree> whilestmt= op("while", Annotation.KEYWORD_PREFIX).then(expr).then(
                op("do", Annotation.KEYWORD_INFIX).then(stmtRef.fold(While.CREATE_DO))
                        .or(While.CREATE));

        Parser<Tree> forstmt =
                op("for", Annotation.KEYWORD_PREFIX)
                .then(Utils.properties())
                .then(Utils.put("varName", idString))
                .then(op("in", Annotation.KEYWORD_INFIX))
                .then(Utils.put("vector", expr))
                .then(op("do", Annotation.KEYWORD_INFIX))
                .then(Utils.put("body", stmtRef))
                .then(Utils.create(ForEach.class, "varName", "vector", "body"));

        Parser<Tree> ifstmt = op("if", Annotation.KEYWORD_PREFIX)
                .then(Utils.properties())
                .then(Utils.put("condition", expr))
                .then(op("then", Annotation.KEYWORD_INFIX))
                .then(Utils.put("thenBranch", stmtRef))
                .then(
                    Reducer.opt(
                        op("else", Annotation.KEYWORD_INFIX)
                        .then(Utils.put("elseBranch", stmtRef))
                    )
                )
                .then(Utils.create(IfElse.class, "condition", "thenBranch", "elseBranch"));

        stmt = whilestmt.or(forstmt).or(ifstmt).or(expr);

        stmtRef.set(stmt);

        // now for declarations

        Parser<Tree> externDef =
                op("extern", Annotation.KEYWORD_DEF)
                .then(Utils.properties())
                .then(Utils.put("id", idString))
                .then(Utils.put("type", idString.annotate(Annotation.BREAK)))
                .then(Reducer.opt(Utils.put("description", quoted.annotate(Annotation.BREAK))))
                .then(op("=", Annotation.KEYWORD_INFIX))
                .then(Utils.put("value", expr))
                .then(Utils.create(ExternDeclaration.class, "id", "type", "description", "value"));

        Parser<List<String>> arguments = op("(").then(
                Utils.list(idString, op(",", Annotation.SEPARATOR))
                .then(op(")")));

        Parser<Tree> funcDef =
                op("func", Annotation.KEYWORD_DEF)
                .then(Utils.properties())
                .then(Utils.put("id", idString))
                .then(Utils.put("args", arguments))
                .then(Utils.put("body", stmt.annotate(Annotation.BREAK)))
                .then(Utils.create(FuncDef.class, "id", "args", "body"));

        Parser<List<String>> possiblyEmptyArguments = arguments.or(Utils.empty());

        Parser<Tree> templateDef =
                op("template", Annotation.KEYWORD_DEF)
                .then(Utils.properties())
                .then(Utils.put("id", idString))
                .then(Utils.put("args", possiblyEmptyArguments))
                .then(Utils.put("body", block.annotate(Annotation.BREAK)))
                .then(Utils.create(TemplateDef.class, "id", "args", "body"));

        Parser<Tree> definition =
                op("def", Annotation.KEYWORD_DEF)
                .then(idString)
                .then(op("=", Annotation.KEYWORD_INFIX))
                .then(expr.fold(Definition.CREATE));

        Parser<Tree> defs = externDef.or(funcDef).or(templateDef).or(definition);

        // variable declarations may be chained
        // XXX Meelan2: Change chaining-rules.

        Parser<Tree> varDecl =
                Utils.properties()
                .then(Utils.put("id", idString))
                .then(Reducer.opt(Utils.put("typeString", idString.annotate(Annotation.BREAK))))
                .then(Reducer.opt(Utils.put("init", op("=", Annotation.KEYWORD_INFIX).then(assignexpr))))
                .then(Utils.create(VarDeclaration.class, "id", "typeString", "init"));

        Reducer<List<Tree>, List<Tree>> vars =
                op("var", Annotation.KEYWORD_DEF)
                .then(op(",", Annotation.SEPARATOR).joinPlus(Utils.append(varDecl, 0)));

        Parser<Tree> objectDecl = // object a = A(c,b)
                idString.then(
                        op("=", Annotation.KEYWORD_INFIX).then(expr)
                        .fold(ObjectDeclaration.CREATE));

        Reducer<List<Tree>, List<Tree>> objects =
                op("object", Annotation.KEYWORD_DEF).then(
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
