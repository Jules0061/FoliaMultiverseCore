package org.mvplugins.multiverse.core.command.flag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class CommandFlag {
    public static @NotNull Builder<?> builder(@NotNull String key){
        return new Builder<>(key);
    }

    private final String key;
    private final List<String> aliases;

    protected CommandFlag(@NotNull String key, @NotNull List<String> aliases) {
        this.key = key;
        this.aliases = aliases;
    }

    public @NotNull String getKey() {
        return key;
    }

    public @NotNull List<String> getAliases() {
        return aliases;
    }

    @Override
    public String toString() {
        return "Builder{"
                + "key='" + key + '\''
                + ", aliases=" + aliases
                + '}';
    }

    public static class Builder<S extends Builder<?>> {
        protected final String key;
        protected final List<String> aliases;

        public Builder(@NotNull String key) {
            this.key = key;
            aliases = new ArrayList<>();
        }

        public @NotNull S addAlias(@NotNull String...alias){
            Collections.addAll(this.aliases, alias);
            return (S) this;
        }

        public @NotNull CommandFlag build(){
            return new CommandFlag(key, aliases);
        }
    }
}
