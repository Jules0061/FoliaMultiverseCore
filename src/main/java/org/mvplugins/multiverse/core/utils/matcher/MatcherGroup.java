package org.mvplugins.multiverse.core.utils.matcher;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ApiStatus.AvailableSince("5.2")
public class MatcherGroup implements StringMatcher {

    private final ExactStringMatcher exactMatcher;
    private final List<StringMatcher> stringMatchers;

    @ApiStatus.AvailableSince("5.2")
    public MatcherGroup() {
        this.exactMatcher = new ExactStringMatcher();
        this.stringMatchers = new ArrayList<>();
    }

    @ApiStatus.AvailableSince("5.2")
    public MatcherGroup(@NotNull Collection<String> matchStrings) {
        this();
        for (String matchString : matchStrings) {
            addMatcher(matchString);
        }
    }

    @ApiStatus.AvailableSince("5.2")
    public void addMatcher(@Nullable String matchString) {
        if (matchString == null || matchString.isEmpty()) {
            return;
        }
        if (isExact(matchString)) {
            exactMatcher.addExactMatch(matchString);
        } else {
            stringMatchers.add(StringMatcher.fromString(matchString));
        }
    }

    private boolean isExact(@NotNull String matcherString) {
        return !matcherString.contains("*") && !matcherString.startsWith("r=");
    }

    @ApiStatus.AvailableSince("5.2")
    public void addMatcher(@NotNull StringMatcher matcher) {
        stringMatchers.add(matcher);
    }

    @Override
    public boolean matches(@Nullable String value) {
        if (exactMatcher.matches(value)) {
            return true;
        }
        for (StringMatcher matcher : stringMatchers) {
            if (matcher.matches(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MatcherGroup{" + "exactMatcher=" + exactMatcher +
                ", stringMatchers=" + stringMatchers +
                '}';
    }
}
