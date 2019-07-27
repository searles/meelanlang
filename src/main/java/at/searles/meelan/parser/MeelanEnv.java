package at.searles.meelan.parser;

import at.searles.meelan.MeelanException;
import at.searles.meelan.ParsingException;
import at.searles.meelan.optree.inlined.ExternDeclaration;
import at.searles.parsing.Environment;
import at.searles.parsing.ParserStream;
import at.searles.parsing.Recognizable;
import at.searles.parsing.printing.ConcreteSyntaxTree;

import java.util.LinkedHashMap;
import java.util.Map;

public class MeelanEnv implements Environment {

    private Map<String, ExternDeclaration> externDecls = new LinkedHashMap<>();

    @Override
    public void notifyNoMatch(ParserStream stream, Recognizable.Then failedParser) {
        throw new ParsingException("parsing error", stream, failedParser);
    }

    @Override
    public void notifyLeftPrintFailed(ConcreteSyntaxTree rightTree, Recognizable.Then failed) {

    }

    public void registerExternDecl(ExternDeclaration decl) {
        if(externDecls.put(decl.id, decl) != null) {
            throw new MeelanException("Extern with this id is already defined", decl);
        }
    }
}
