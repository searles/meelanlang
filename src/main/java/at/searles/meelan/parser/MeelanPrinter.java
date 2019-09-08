package at.searles.meelan.parser;

import at.searles.parsing.printing.ConcreteSyntaxTree;
import at.searles.parsing.printing.CstPrinter;
import at.searles.parsing.printing.OutStream;

public class MeelanPrinter extends CstPrinter {

    private static final String INDENTATION = "    ";
    private int indentation = 0;
    private boolean mustIndent = false;

    public MeelanPrinter(OutStream outStream) {
        super(outStream);
    }

    private void indent() {
        indentation++;
    }

    private void unindent() {
        indentation--;
    }
    
    private void newline() {
        append("\n");
        mustIndent = true;
    }

    private void checkIndentation() {
        if(mustIndent) {
            for(int i = 0; i < indentation; ++i) {
                append(INDENTATION);
            }
        }

        mustIndent = false;
    }

    private void space() {
        append(" ");
    }

    @Override
    public CstPrinter print(CharSequence seq) {
        checkIndentation();
        return append(seq);
    }

    @Override
    public CstPrinter print(ConcreteSyntaxTree tree, Object annotation) {
        switch ((MeelanParser.Annotation) annotation) {
            case SEPARATOR:
                print(tree);
                space();
                break;
            case BREAK:
                space();
                print(tree);
                break;
            case KEYWORD_PREFIX:
                print(tree);
                space();
                break;
            case KEYWORD_DEF:
                print(tree);
                space();
                break;
            case KEYWORD_INFIX:
                space();
                print(tree);
                space();
                break;
            case BLOCK:
                // space();
                print(tree);
                break;
            case IN_BLOCK:
                newline();
                indent();
                print(tree);
                unindent();
                break;
            case STMT:
                print(tree);
                newline();
                break;
            default:
                print(tree);
                break;
        }

        return this;
    }
}
