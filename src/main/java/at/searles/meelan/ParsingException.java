package at.searles.meelan;

import at.searles.parsing.ParserStream;
import at.searles.parsing.Recognizable;

public class ParsingException extends RuntimeException {
    private final long start;
    private final long end;
    private final ParserStream stream;
    private final Recognizable expected;

    public ParsingException(String msg, ParserStream stream, Recognizable expected) {
        super(msg);
        this.start = stream.start();
        this.end = stream.end();
        this.stream = stream;
        this.expected = expected;
    }
}
