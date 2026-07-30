package org.mvplugins.multiverse.core.display.filters;

public interface ContentFilter {
    boolean checkMatch(String value);

    boolean needToFilter();

    String toString();
}
