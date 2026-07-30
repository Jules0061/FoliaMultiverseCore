package org.mvplugins.multiverse.core.display.filters;

public final class DefaultContentFilter implements ContentFilter {

    private static DefaultContentFilter instance;

    public static DefaultContentFilter get() {
        if (instance == null) {
            instance = new DefaultContentFilter();
        }
        return instance;
    }

    private DefaultContentFilter() {
    }

    @Override
    public boolean checkMatch(String value) {
        return true;
    }

    @Override
    public boolean needToFilter() {
        return false;
    }

    @Override
    public String toString() {
        return "N/A";
    }
}
