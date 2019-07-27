package at.searles.meelan.parser;

import at.searles.parsing.utils.ast.SourceInfo;

/**
 * Dummy source info implementation for symbols that are introduced without
 * representation in the source code.
 */
public class DummyInfo implements SourceInfo {
    private DummyInfo() {}

    public static DummyInfo getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public long end() {
        return 0;
    }

    @Override
    public long start() {
        return 0;
    }

    private static class LazyHolder {
        static final DummyInfo INSTANCE = new DummyInfo();
    }
}
