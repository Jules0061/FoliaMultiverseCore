package org.mvplugins.multiverse.core.command.flag;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParsedCommandFlags
{
    public static final ParsedCommandFlags EMPTY = new ParsedCommandFlags();

    private final Map<String, Object> flagValues;

    ParsedCommandFlags() {
        flagValues = new HashMap<>();
    }

    void addFlagResult(@NotNull String key, @Nullable Object value) {
        flagValues.put(key, value);
    }

    public boolean hasFlag(@NotNull CommandFlag flag) {
        return hasFlag(flag.getKey());
    }

    public boolean hasFlag(@Nullable String key) {
        return this.flagValues.containsKey(key);
    }

    public boolean hasFlagValue(@Nullable String key) {
        return flagValue(key, Object.class) != null;
    }

    public @Nullable <T> T flagValue(@NotNull CommandFlag flag, @NotNull Class<T> type) {
        return flagValue(flag.getKey(), type);
    }

    public @Nullable <T> T flagValue(@Nullable String key, @NotNull Class<T> type) {
        Object value = this.flagValues.get(key);
        return (T) value;
    }

    public @Nullable <T> T flagValue(@NotNull CommandValueFlag<T> flag) {
        return flagValue(flag.getKey(), flag.getType());
    }

    public @NotNull <T> T flagValue(@NotNull CommandValueFlag<T> flag, @NotNull T defaultValue) {
        return flagValue(flag.getKey(), defaultValue, flag.getType());
    }

    public @NotNull <T> T flagValue(@Nullable String key, @NotNull T defaultValue, @NotNull Class<T> type) {
        T value = flagValue(key, type);
        return value != null ? value : defaultValue;
    }
}
