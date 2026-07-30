package org.mvplugins.multiverse.core.command.flag;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import co.aikar.commands.InvalidCommandArgument;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandValueFlag<T> extends CommandFlag {
    public static @NotNull <T> Builder<T, ?> builder(@NotNull String key, @NotNull Class<T> type) {
        return new Builder<>(key, type);
    }

    public static @NotNull <T extends Enum<T>> EnumBuilder<T, ?> enumBuilder(@NotNull String key, @NotNull Class<T> type) {
        return new EnumBuilder<>(key, type);
    }

    private final Class<T> type;
    private final boolean optional;
    private final Supplier<? extends T> defaultValueSupplier;
    private final Function<String, T> context;
    private final Function<String, Collection<String>>  completion;

    protected CommandValueFlag(
            @NotNull String key,
            @NotNull List<String> aliases,
            @NotNull Class<T> type,
            boolean optional,
            @Nullable Supplier<? extends T> defaultValueSupplier,
            @Nullable Function<String, T> context,
            @Nullable Function<String, Collection<String>>  completion
    ) {
        super(key, aliases);
        this.type = type;
        this.optional = optional;
        this.defaultValueSupplier = defaultValueSupplier;
        this.context = context;
        this.completion = completion;
    }

    public @NotNull Class<T> getType() {
        return type;
    }

    public boolean isOptional() {
        return optional;
    }

    public @Nullable T getDefaultValue() {
        return defaultValueSupplier == null ? null : defaultValueSupplier.get();
    }

    public @Nullable Function<String, T> getContext() {
        return context;
    }

    public @Nullable Function<String, Collection<String>> getCompletion() {
        return completion;
    }

    public static class Builder<T, S extends Builder<T, S>> extends CommandFlag.Builder<S> {
        protected final Class<T> type;
        protected boolean optional = false;
        protected Supplier<? extends T> defaultValueSupplier = null;
        protected Function<String, T> context = null;
        protected Function<String, Collection<String>> completion = null;

        public Builder(@NotNull String key, @NotNull Class<T> type) {
            super(key);
            this.type = type;
        }

        public @NotNull S optional() {
            this.optional = true;
            return (S) this;
        }

        public @NotNull S defaultValue(@NotNull T defaultValue) {
            return defaultValue(() -> defaultValue);
        }

        @ApiStatus.AvailableSince("5.7")
        public @NotNull S defaultValue(@NotNull Supplier<? extends T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
            return (S) this;
        }

        public @NotNull S context(@NotNull Function<String, T> context) {
            this.context = context;
            return (S) this;
        }

        public @NotNull S completion(@NotNull Function<String, Collection<String>>  completion) {
            this.completion = completion;
            return (S) this;
        }

        @Override
        public @NotNull CommandValueFlag<T> build() {
            if (context == null && !String.class.equals(type)) {
                throw new IllegalStateException("Context is required for non-string value flags");
            }
            return new CommandValueFlag<>(key, aliases, type, optional, defaultValueSupplier, context, completion);
        }
    }

    public static class EnumBuilder<T extends Enum<T>, S extends EnumBuilder<T, S>> extends CommandFlag.Builder<S> {
        protected final Class<T> type;
        protected boolean optional = false;
        protected Supplier<? extends T> defaultValueSupplier = null;
        protected Function<String, T> context = null;
        protected Function<String, Collection<String>>  completion = null;

        public EnumBuilder(@NotNull String key, @NotNull Class<T> type) {
            super(key);
            this.type = type;
            setEnumContext();
            setEnumCompletion();
        }

        private void setEnumContext() {
            this.context = (String value) -> {
                try {
                    return Enum.valueOf(type, value.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException e) {
                    throw new InvalidCommandArgument("Invalid value for argument " + key + ": " + value);
                }
            };
        }

        private void setEnumCompletion() {
            List<String> types = Arrays.stream(type.getEnumConstants())
                    .map(typeClass -> typeClass.name().toLowerCase(Locale.ENGLISH))
                    .toList();

            this.completion = (input) -> types;
        }

        public @NotNull S optional() {
            this.optional = true;
            return (S) this;
        }

        public @NotNull S defaultValue(@NotNull T defaultValue) {
            return defaultValue(() -> defaultValue);
        }

        @ApiStatus.AvailableSince("5.7")
        public @NotNull S defaultValue(@NotNull Supplier<? extends T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
            return (S) this;
        }

        @Override
        public @NotNull CommandValueFlag<T> build() {
            return new CommandValueFlag<>(key, aliases, type, optional, defaultValueSupplier, context, completion);
        }
    }
}
