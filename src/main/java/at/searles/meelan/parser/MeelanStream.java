package at.searles.meelan.parser;

import at.searles.lexer.TokStream;
import at.searles.meelan.MeelanException;
import at.searles.meelan.ParsingException;
import at.searles.meelan.optree.inlined.ExternDeclaration;
import at.searles.parsing.ParserStream;
import at.searles.parsing.Recognizable;
import at.searles.parsing.printing.ConcreteSyntaxTree;

import java.util.LinkedHashMap;
import java.util.Map;

public class MeelanStream extends ParserStream {

    private Map<String, ExternDeclaration> externDecls = new LinkedHashMap<>();

    public MeelanStream(TokStream stream) {
        super(stream);
    }

    public void registerExternDecl(ExternDeclaration decl) {
        if(externDecls.put(decl.id, decl) != null) {
            throw new MeelanException("Extern with this id is already defined", decl);
        }
    }

    public Map<String, ExternDeclaration> getExternDecls() {
        return externDecls;
    }
}
