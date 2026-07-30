package org.mvplugins.multiverse.core.utils.matcher;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ApiStatus.AvailableSince("5.2")
public class ExactStringMatcher implements StringMatcher {
    private final Set<String> exactMatches;

    @ApiStatus.AvailableSince("5.2")
    public ExactStringMatcher() {
        this.exactMatches = new HashSet<>();
    }

    @ApiStatus.AvailableSince("5.2")
    public ExactStringMatcher(@NotNull String exactMatch) {
        this.exactMatches = new HashSet<>();
        this.exactMatches.add(exactMatch);
    }

    @ApiStatus.AvailableSince("5.2")
    public ExactStringMatcher(@NotNull Collection<String> exactMatches) {
        this.exactMatches = new HashSet<>(exactMatches);
    }

    @ApiStatus.AvailableSince("5.2")
    public void addExactMatch(@NotNull String value) {
        this.exactMatches.add(value);
    }

    @Override
    public boolean matches(@Nullable String value) {
        return exactMatches.contains(value);
    }
}
