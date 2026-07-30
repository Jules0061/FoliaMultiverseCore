package org.mvplugins.multiverse.core.config.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.vavr.Value;
import io.vavr.control.Try;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.node.functions.DefaultStringParserProvider;
import org.mvplugins.multiverse.core.config.node.functions.DefaultSuggesterProvider;
import org.mvplugins.multiverse.core.config.node.functions.NodeChangeCallback;
import org.mvplugins.multiverse.core.config.node.functions.NodeValueCallback;
import org.mvplugins.multiverse.core.config.node.functions.NodeStringParser;
import org.mvplugins.multiverse.core.config.node.functions.NodeSuggester;
import org.mvplugins.multiverse.core.config.node.functions.SenderNodeStringParser;
import org.mvplugins.multiverse.core.config.node.functions.SenderNodeSuggester;
import org.mvplugins.multiverse.core.config.node.serializer.DefaultSerializerProvider;
import org.mvplugins.multiverse.core.config.node.serializer.NodeSerializer;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.core.utils.StringFormatter;

public class ListConfigNode<I> extends ConfigNode<List<I>> implements ListValueNode<I> {

    public static @NotNull <I, B  extends Builder<I, B>> Builder<I, B> listBuilder(
            @NotNull String path,
            @NotNull Class<I> type) {
        return new Builder<>(path, type);
    }

    protected final Class<I> itemType;
    protected final NodeSuggester itemSuggester;
    protected final NodeStringParser<I> itemStringParser;
    protected final NodeSerializer<I> itemSerializer;
    protected final Function<I, Try<Void>> itemValidator;
    protected final BiConsumer<I, I> onSetItemValue;

    protected ListConfigNode(
            @NotNull String path,
            @NotNull String[] comments,
            @Nullable String name,
            @NotNull Class<List<I>> type,
            @NotNull String[] aliases,
            @Nullable Supplier<List<I>> defaultValueSupplier,
            @Nullable NodeSuggester suggester,
            @Nullable NodeStringParser<List<I>> stringParser,
            @Nullable NodeSerializer<List<I>> serializer,
            @Nullable Function<List<I>, Try<Void>> validator,
            @Nullable NodeValueCallback<List<I>> onLoad,
            @Nullable NodeChangeCallback<List<I>> onLoadAndChange,
            @Nullable NodeChangeCallback<List<I>> onChange,
            @NotNull Class<I> itemType,
            @Nullable NodeSuggester itemSuggester,
            @Nullable NodeStringParser<I> itemStringParser,
            @Nullable NodeSerializer<I> itemSerializer,
            @Nullable Function<I, Try<Void>> itemValidator,
            @Nullable BiConsumer<I, I> onSetItemValue) {
        super(path, comments, name, type, aliases, defaultValueSupplier, suggester, stringParser, serializer,
                validator, onLoad, onLoadAndChange, onChange);
        this.itemType = itemType;
        this.itemSuggester = itemSuggester != null
                ? itemSuggester
                : DefaultSuggesterProvider.getDefaultSuggester(itemType);
        this.itemStringParser = itemStringParser != null
                ? itemStringParser
                : DefaultStringParserProvider.getDefaultStringParser(itemType);
        this.itemSerializer = itemSerializer != null
                ? itemSerializer
                : DefaultSerializerProvider.getDefaultSerializer(itemType);
        this.itemValidator = itemValidator;
        this.onSetItemValue = onSetItemValue;

        setDefaults();
    }

    private void setDefaults() {
        if (this.itemSuggester != null && this.suggester == null) {
            setDefaultSuggester();
        }
        if (this.itemStringParser != null && this.stringParser == null) {
            setDefaultStringParser();
        }
        if (this.itemValidator != null && this.validator == null) {
            setDefaultValidator();
        }
        if (this.itemSerializer != null && this.serializer == null) {
            setDefaultSerialiser();
        }
        if (this.onSetItemValue != null && this.onLoadAndChange == null) {
            setDefaultOnLoadAndChange();
        }
        if (this.defaultValue == null) {
            this.defaultValue = ArrayList::new;
        }
    }

    private void setDefaultSuggester() {
        if (itemSuggester instanceof SenderNodeSuggester senderItemSuggester) {
            this.suggester = (SenderNodeSuggester)(sender, input) ->
                    StringFormatter.addOnToCommaSeparated(input, senderItemSuggester.suggest(sender, input));
        } else {
            this.suggester = input -> StringFormatter.addOnToCommaSeparated(input, itemSuggester.suggest(input));
        }
    }

    private void setDefaultStringParser() {
        this.stringParser = (input, type) -> {
            if (input == null) {
                return Try.failure(new IllegalArgumentException("Input cannot be null"));
            }
            return Try.sequence(Arrays.stream(REPatterns.COMMA.split(input))
                    .map(inputItem -> itemStringParser.parse(inputItem, itemType))
                    .toList()).map(Value::toJavaList);
        };
    }

    private void setDefaultValidator() {
        this.validator = value -> {
            if (value != null) {
                return Try.sequence(value.stream().map(itemValidator).toList()).map(v -> null);
            }
            return Try.success(null);
        };
    }

    private void setDefaultSerialiser() {
        this.serializer = new NodeSerializer<>() {
            @Override
            public List<I> deserialize(Object object, Class<List<I>> type) {
                if (object instanceof List list) {
                    return (List<I>) list.stream()
                            .map(item -> itemSerializer != null ? itemSerializer.deserialize(item, itemType) : item)
                            .collect(Collectors.toList());
                }
                return new ArrayList<>();
            }

            @Override
            public Object serialize(List<I> object, Class<List<I>> type) {
                if (object == null) {
                    return new ArrayList<>();
                }
                return object.stream()
                        .map(item -> itemSerializer != null ? itemSerializer.serialize(item, itemType) : item)
                        .collect(Collectors.toList());
            }
        };
    }

    private void setDefaultOnLoadAndChange() {
        this.onLoadAndChange = (oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.stream()
                        .filter(value -> !newValue.contains(value))
                        .forEach(item -> onSetItemValue.accept(item, null));
            }
            newValue.forEach(item -> onSetItemValue.accept(null, item));
        };
    }

    @Override
    public @NotNull Class<I> getItemType() {
        return itemType;
    }

    @Override
    public @NotNull Collection<String> suggestItem(@Nullable String input) {
        if (itemSuggester != null) {
            return itemSuggester.suggest(input);
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<String> suggestItem(@NotNull CommandSender sender, @Nullable String input) {
        if (itemSuggester != null && itemSuggester instanceof SenderNodeSuggester senderSuggester) {
            return senderSuggester.suggest(sender, input);
        }
        return suggestItem(input);
    }

    @Override
    public @NotNull Try<I> parseItemFromString(@Nullable String input) {
        if (itemStringParser != null) {
            return itemStringParser.parse(input, itemType);
        }
        return Try.failure(new UnsupportedOperationException("No item string parser for type " + itemType));
    }

    @Override
    public @NotNull Try<I> parseItemFromString(@NotNull CommandSender sender, @Nullable String input) {
        if (itemStringParser != null && itemStringParser instanceof SenderNodeStringParser<I> senderStringParser) {
            return senderStringParser.parse(sender, input, itemType);
        }
        return parseItemFromString(input);
    }

    @Override
    public @Nullable NodeSerializer<I> getItemSerializer() {
        return itemSerializer;
    }

    @Override
    public Try<Void> validateItem(@Nullable I value) {
        if (itemValidator != null) {
            return itemValidator.apply(value);
        }
        return Try.success(null);
    }

    @Override
    public void onSetItemValue(@Nullable I oldValue, @Nullable I newValue) {
        if (onSetItemValue != null) {
            onSetItemValue.accept(oldValue, newValue);
        }
    }

    public static class Builder<I, B extends ListConfigNode.Builder<I, B>> extends ConfigNode.Builder<List<I>, B> {

        protected final @NotNull Class<I> itemType;
        protected @Nullable NodeSuggester itemSuggester;
        protected @Nullable NodeStringParser<I> itemStringParser;
        protected @Nullable NodeSerializer<I> itemSerializer;
        protected @Nullable Function<I, Try<Void>> itemValidator;
        protected @Nullable BiConsumer<I, I> onSetItemValue;

        protected Builder(@NotNull String path, @NotNull Class<I> itemType) {
            super(path, (Class<List<I>>) (Object) List.class);
            this.itemType = itemType;
            this.defaultValue = ArrayList::new;
        }

        public @NotNull B itemSuggester(@NotNull NodeSuggester itemSuggester) {
            this.itemSuggester = itemSuggester;
            return self();
        }

        @ApiStatus.AvailableSince("5.1")
        public @NotNull B itemSuggester(@NotNull SenderNodeSuggester itemSuggester) {
            this.itemSuggester = itemSuggester;
            return self();
        }

        public @NotNull B itemStringParser(@NotNull NodeStringParser<I> itemStringParser) {
            this.itemStringParser = itemStringParser;
            return self();
        }

        @ApiStatus.AvailableSince("5.1")
        public @NotNull B itemStringParser(@NotNull SenderNodeStringParser<I> itemStringParser) {
            this.itemStringParser = itemStringParser;
            return self();
        }

        public @NotNull B itemSerializer(@NotNull NodeSerializer<I> serializer) {
            this.itemSerializer = serializer;
            return self();
        }

        public @NotNull B itemValidator(@NotNull Function<I, Try<Void>> itemValidator) {
            this.itemValidator = itemValidator;
            return self();
        }

        public @NotNull B onSetItemValue(@Nullable BiConsumer<I, I> onSetItemValue) {
            this.onSetItemValue = onSetItemValue;
            return self();
        }

        @Override
        public @NotNull ListConfigNode<I> build() {
            return new ListConfigNode<>(
                    path,
                    comments.toArray(new String[0]),
                    name,
                    type,
                    aliases,
                    defaultValue,
                    suggester,
                    stringParser,
                    serializer,
                    validator,
                    onLoad,
                    onLoadAndChange,
                    onChange,
                    itemType,
                    itemSuggester,
                    itemStringParser,
                    itemSerializer,
                    itemValidator,
                    onSetItemValue);
        }
    }
}
