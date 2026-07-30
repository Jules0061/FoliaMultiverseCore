package org.mvplugins.multiverse.core.utils.matcher;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@ApiStatus.AvailableSince("5.2")
public interface StringMatcher {

    @ApiStatus.AvailableSince("5.2")
    static @NotNull StringMatcher fromString(@NotNull String matcherString) {
        if (matcherString.startsWith("r=")) {
            return new RegexStringMatcher(matcherString);
        } else if (matcherString.contains("*")) {
            return new WildcardStringMatcher(matcherString);
        } else {
            return new ExactStringMatcher(matcherString);
        }
    }

    @ApiStatus.AvailableSince("5.2")
    boolean matches(@Nullable String value);

    @ApiStatus.AvailableSince("5.2")
    default @NotNull List<String> filter(@NotNull List<String> values) {
        return values.stream()
                .filter(this::matches)
                .collect(Collectors.toList());
    }
}
