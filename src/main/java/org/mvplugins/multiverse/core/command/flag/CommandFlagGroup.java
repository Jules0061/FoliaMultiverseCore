package org.mvplugins.multiverse.core.command.flag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandFlagGroup {
    public static @NotNull Builder builder(@NotNull String name) {
        return new Builder(name);
    }

    private final String name;
    private final List<String> keys;
    private final Map<String, CommandFlag> keysFlagMap;

    protected CommandFlagGroup(@NotNull Builder builder) {
        name = builder.name;
        keys = builder.keys;
        keysFlagMap = builder.keysFlagMap;
    }

    public @NotNull String getName() {
        return name;
    }

    public boolean hasKey(@Nullable String key) {
        return keysFlagMap.containsKey(key);
    }

    public @NotNull Set<String> getRemainingKeys(@NotNull String[] flags) {
        Set<String> keysRemaining = new HashSet<>(this.keys);
        for (String flag : flags) {
            CommandFlag mvFlag = this.getFlagByKey(flag);
            if (mvFlag != null) {
                keysRemaining.remove(mvFlag.getKey());
            }
        }
        return keysRemaining;
    }

    public @Nullable CommandFlag getFlagByKey(String key) {
        return keysFlagMap.get(key);
    }

    public static class Builder {
        private final String name;
        private final List<String> keys;
        private final Map<String, CommandFlag> keysFlagMap;

        public Builder(@NotNull String name) {
            this.name = name;
            this.keys = new ArrayList<>();
            this.keysFlagMap = new HashMap<>();
        }

        public @NotNull Builder add(CommandFlag flag) {
            keys.add(flag.getKey());
            keysFlagMap.put(flag.getKey(), flag);
            flag.getAliases().forEach((alias) -> keysFlagMap.put(alias, flag));
            return this;
        }

        public @NotNull CommandFlagGroup build() {
            return new CommandFlagGroup(this);
        }
    }
}
