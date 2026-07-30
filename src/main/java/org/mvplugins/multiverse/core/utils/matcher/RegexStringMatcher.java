package org.mvplugins.multiverse.core.utils.matcher;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@ApiStatus.AvailableSince("5.2")
public class RegexStringMatcher implements StringMatcher {
    private final @NotNull String regexString;
    private final @Nullable Pattern regexPattern;

    @ApiStatus.AvailableSince("5.2")
    public RegexStringMatcher(@NotNull String regexString) {
        this.regexString = regexString;
        this.regexPattern = compileRegex(regexString);
    }

    private Pattern compileRegex(String regexString) {
        if (regexString.startsWith("r=")) {
            regexString = regexString.substring(2);
        }

        String finalRegexString = regexString;
        return Try.of(() -> Pattern.compile(finalRegexString))
                .onFailure(ex -> Logging.warning("Failed to compile regex '%s': %s",
                        finalRegexString, ex.getMessage()))
                .getOrNull();
    }

    @Override
    public boolean matches(@Nullable String value) {
        if (regexPattern == null || value == null) {
            return false;
        }
        return regexPattern.matcher(value).matches();
    }
}
