package org.mvplugins.multiverse.core.utils.matcher;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@ApiStatus.AvailableSince("5.2")
public class WildcardStringMatcher implements StringMatcher {

    private final String wildcard;
    private final Pattern pattern;

    @ApiStatus.AvailableSince("5.2")
    public WildcardStringMatcher(@NotNull String wildcard) {
        this.wildcard = wildcard;
        this.pattern = Try.of(() -> Pattern.compile(("\\Q" + wildcard + "\\E").replace("*", "\\E.*\\Q")))
                .onFailure(ex -> Logging.warning("Failed to compile wildcard '%s': %s",
                        wildcard, ex.getMessage()))
                .getOrNull();
    }

    @Override
    public boolean matches(@Nullable String value) {
        if (pattern == null || value == null) {
            return false;
        }
        return pattern.matcher(value).matches();
    }
}
