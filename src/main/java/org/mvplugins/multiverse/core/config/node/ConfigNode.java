package org.mvplugins.multiverse.core.config.node;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.node.functions.NodeChangeCallback;
import org.mvplugins.multiverse.core.config.node.functions.NodeValueCallback;
import org.mvplugins.multiverse.core.config.node.functions.SenderNodeChangeCallback;
import org.mvplugins.multiverse.core.config.node.functions.SenderNodeStringParser;
import org.mvplugins.multiverse.core.config.node.functions.SenderNodeSuggester;
import org.mvplugins.multiverse.core.config.node.serializer.DefaultSerializerProvider;
import org.mvplugins.multiverse.core.config.node.functions.DefaultStringParserProvider;
import org.mvplugins.multiverse.core.config.node.functions.DefaultSuggesterProvider;
import org.mvplugins.multiverse.core.config.node.serializer.NodeSerializer;
import org.mvplugins.multiverse.core.config.node.functions.NodeStringParser;
import org.mvplugins.multiverse.core.config.node.functions.NodeSuggester;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;

public class ConfigNode<T> extends ConfigHeaderNode implements ValueNode<T> {

    public static @NotNull <T> ConfigNode.Builder<T, ? extends ConfigNode.Builder<T, ?>> builder(
            @NotNull String path,
            @NotNull Class<T> type) {
        return new ConfigNode.Builder<>(path, type);
    }

    protected final @Nullable String name;
    protected final @NotNull Class<T> type;
    protected final @NotNull String[] aliases;
    protected @Nullable Supplier<T> defaultValue;
    protected @Nullable NodeSuggester suggester;
    protected @Nullable NodeStringParser<T> stringParser;
    protected @Nullable NodeSerializer<T> serializer;
    protected @Nullable Function<T, Try<Void>> validator;
    protected @Nullable NodeValueCallback<T> onLoad;
    protected @Nullable NodeChangeCallback<T> onLoadAndChange;
    protected @Nullable NodeChangeCallback<T> onChange;

    protected ConfigNode(
            @NotNull String path,
            @NotNull String[] comments,
            @Nullable String name,
            @NotNull Class<T> type,
            @NotNull String[] aliases,
            @Nullable Supplier<T> defaultValue,
            @Nullable NodeSuggester suggester,
            @Nullable NodeStringParser<T> stringParser,
            @Nullable NodeSerializer<T> serializer,
            @Nullable Function<T, Try<Void>> validator,
            @Nullable NodeValueCallback<T> onLoad,
            @Nullable NodeChangeCallback<T> onLoadAndChange,
            @Nullable NodeChangeCallback<T> onChange) {
        super(path, comments);
        this.name = name;
        this.type = type;
        this.aliases = aliases;
        this.defaultValue = defaultValue;
        this.suggester = (suggester != null)
                ? suggester
                : DefaultSuggesterProvider.getDefaultSuggester(type);
        this.stringParser = (stringParser != null)
                ? stringParser
                : DefaultStringParserProvider.getDefaultStringParser(type);
        this.serializer = (serializer != null)
                ? serializer
                : DefaultSerializerProvider.getDefaultSerializer(type);
        this.validator = validator;
        this.onLoad = onLoad;
        this.onLoadAndChange = onLoadAndChange;
        this.onChange = onChange;
    }

    @Override
    public @NotNull Option<String> getName() {
        return Option.of(name);
    }

    @Override
    public @NotNull Class<T> getType() {
        return type;
    }

    @Override
    public @NotNull String[] getAliases() {
        return aliases;
    }

    @Override
    public @Nullable T getDefaultValue() {
        if (defaultValue != null) {
            return defaultValue.get();
        }
        return null;
    }

    @Override
    public @NotNull Collection<String> suggest(@Nullable String input) {
        if (suggester != null) {
            return suggester.suggest(input);
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSender sender, @Nullable String input) {
        if (suggester != null && suggester instanceof SenderNodeSuggester senderSuggester) {
            return senderSuggester.suggest(sender, input);
        }
        return suggest(input);
    }

    @Override
    public @NotNull Try<T> parseFromString(@Nullable String input) {
        if (stringParser != null) {
            return stringParser.parse(input, type);
        }
        return Try.failure(new UnsupportedOperationException("No string parser for type " + type.getName()));
    }

    @Override
    public @NotNull Try<T> parseFromString(@NotNull CommandSender sender, @Nullable String input) {
        if (stringParser != null && stringParser instanceof SenderNodeStringParser<T> senderStringParser) {
            return senderStringParser.parse(sender, input, type);
        }
        return parseFromString(input);
    }

    @Override
    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    @SuppressWarnings("removal")
    public @Nullable NodeSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public T deserialize(@Nullable Object object) {
        return serializer == null
                ? (type.isInstance(object)) ? type.cast(object) : getDefaultValue()
                : serializer.deserialize(object, type);
    }

    @Override
    public Object serialize(T value) {
        return serializer ==  null ? value : serializer.serialize(value, type);
    }

    @Override
    public Try<Void> validate(@Nullable T value) {
        if (validator != null) {
            return validator.apply(value);
        }
        return Try.success(null);
    }

    @Override
    public void onLoad(@Nullable T value) {
        if (onLoad != null) {
            onLoad.run(value);
        }
    }

    @Override
    public void onLoadAndChange(@NotNull CommandSender sender, @Nullable T oldValue, @Nullable T newValue) {
        if (onLoadAndChange != null) {
            onLoadAndChange.run(sender, oldValue, newValue);
        }
    }

    @Override
    public void onChange(@NotNull CommandSender sender, @Nullable T oldValue, @Nullable T newValue) {
        if (onChange != null) {
            onChange.run(sender, oldValue, newValue);
        }
    }

    public static class Builder<T, B extends ConfigNode.Builder<T, B>> extends ConfigHeaderNode.Builder<B> {

        protected @Nullable String name;
        protected @NotNull final Class<T> type;
        protected @NotNull String[] aliases = Strings.EMPTY_ARRAY;
        protected @Nullable Supplier<T> defaultValue;
        protected @Nullable NodeSuggester suggester;
        protected @Nullable NodeStringParser<T> stringParser;
        protected @Nullable NodeSerializer<T> serializer;
        protected @Nullable Function<T, Try<Void>> validator;
        protected @Nullable NodeValueCallback<T> onLoad;
        protected @Nullable NodeChangeCallback<T> onLoadAndChange;
        protected @Nullable NodeChangeCallback<T> onChange;

        protected Builder(@NotNull String path, @NotNull Class<T> type) {
            super(path);
            this.name = path;
            this.type = type;
        }

        public @NotNull String path() {
            return path;
        }

        public @NotNull B defaultValue(@NotNull T defaultValue) {
            this.defaultValue = () -> defaultValue;
            return self();
        }

        public @NotNull B defaultValue(@NotNull Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
            return self();
        }

        public @NotNull B name(@Nullable String name) {
            this.name = name;
            return self();
        }

        public @Nullable String name() {
            return name;
        }

        public @NotNull B hidden() {
            return name(null);
        }

        @ApiStatus.AvailableSince("5.1")
        public @NotNull B aliases(@NotNull String... aliases) {
            this.aliases = aliases;
            return self();
        }

        public @NotNull B suggester(@NotNull NodeSuggester suggester) {
            this.suggester = suggester;
            return self();
        }

        @ApiStatus.AvailableSince("5.1")
        public @NotNull B suggester(@NotNull SenderNodeSuggester suggester) {
            this.suggester = suggester;
            return self();
        }

        public @NotNull B stringParser(@NotNull NodeStringParser<T> stringParser) {
            this.stringParser = stringParser;
            return self();
        }

        @ApiStatus.AvailableSince("5.1")
        public @NotNull B stringParser(@NotNull SenderNodeStringParser<T> stringParser) {
            this.stringParser = stringParser;
            return self();
        }

        public @NotNull B serializer(@NotNull NodeSerializer<T> serializer) {
            this.serializer = serializer;
            return self();
        }

        public @NotNull B validator(@NotNull Function<T, Try<Void>> validator) {
            this.validator = validator;
            return self();
        }

        @ApiStatus.AvailableSince("5.4")
        public @NotNull B onLoad(@NotNull NodeValueCallback<T> onLoad) {
            this.onLoad = this.onLoad == null ? onLoad : this.onLoad.then(onLoad);
            return self();
        }

        @ApiStatus.AvailableSince("5.4")
        public @NotNull B onLoadAndChange(@NotNull NodeChangeCallback<T> onLoadAndChange) {
            this.onLoadAndChange = this.onLoadAndChange == null ? onLoadAndChange : this.onLoadAndChange.then(onLoadAndChange);
            return self();
        }

        @ApiStatus.AvailableSince("5.4")
        public @NotNull B onLoadAndChange(@NotNull SenderNodeChangeCallback<T> onLoadAndChange) {
            return onLoadAndChange((NodeChangeCallback<T>) onLoadAndChange);
        }

        @ApiStatus.AvailableSince("5.4")
        public @NotNull B onChange(@NotNull NodeChangeCallback<T> onChange) {
            this.onChange = this.onChange == null ? onChange : this.onChange.then(onChange);
            return self();
        }

        @ApiStatus.AvailableSince("5.4")
        public @NotNull B onChange(@NotNull SenderNodeChangeCallback<T> onChange) {
            return onChange((NodeChangeCallback<T>) onChange);
        }

        @Deprecated(since = "5.4", forRemoval = true)
        public @NotNull B onSetValue(@NotNull BiConsumer<T, T> onSetValue) {
            return onLoadAndChange(onSetValue::accept);
        }

        @Override
        public @NotNull ConfigNode<T> build() {
            return new ConfigNode<>(path, comments.toArray(new String[0]),
                    name, type, aliases, defaultValue, suggester, stringParser, serializer, validator,
                    onLoad, onLoadAndChange, onChange);
        }
    }
}
