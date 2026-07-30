package org.mvplugins.multiverse.core.config.handle;

import java.util.Collection;
import java.util.Collections;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.node.ConfigNodeNotFoundException;
import org.mvplugins.multiverse.core.config.node.ListValueNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.ValueNode;

@SuppressWarnings("unchecked")
public class StringPropertyHandle {
    private final @NotNull BaseConfigurationHandle<?> handle;

    public StringPropertyHandle(@NotNull BaseConfigurationHandle<?> handle) {
        this.handle = handle;
    }

    public Collection<String> getAllPropertyNames() {
        return handle.getNodes().getNames();
    }

    public Collection<String> getModifiablePropertyNames(PropertyModifyAction action) {
        return switch (action) {
            case SET, RESET -> handle.getNodes().getNames();

            case ADD, REMOVE -> handle.getNodes().stream()
                    .filter(node -> node instanceof ListValueNode)
                    .map(node -> ((ValueNode<?>) node).getName())
                    .filter(Option::isDefined)
                    .map(Option::get)
                    .toList();

            default -> Collections.emptyList();
        };
    }

    public Try<Class<?>> getPropertyType(@Nullable String name) {
        return findNode(name, ValueNode.class).map(ValueNode::getType);
    }

    public Collection<String> getSuggestedPropertyValue(
            @Nullable String name,
            @Nullable String input,
            @NotNull PropertyModifyAction action
    ) {
        return getSuggestedPropertyValue(name, input, action, Bukkit.getConsoleSender());
    }

    @ApiStatus.AvailableSince("5.1")
    public Collection<String> getSuggestedPropertyValue(
            @Nullable String name,
            @Nullable String input,
            @NotNull PropertyModifyAction action,
            @NotNull CommandSender sender
    ) {
        return switch (action) {
            case SET -> findNode(name, ValueNode.class)
                    .map(node -> node.suggest(sender, input))
                    .getOrElse(Collections.emptyList());

            case ADD -> findNode(name, ListValueNode.class)
                    .map(node -> node.suggestItem(sender, input))
                    .getOrElse(Collections.emptyList());

            case REMOVE -> findNode(name, ListValueNode.class)
                    .map(node -> handle.get((ListValueNode<?>) node))
                    .map(valueList -> valueList.stream()
                            .map(String::valueOf)
                            .toList())
                    .getOrElse(Collections.emptyList());

            default -> Collections.emptyList();
        };
    }

    public Try<Object> getProperty(@Nullable String name) {
        return findNode(name, ValueNode.class).map(handle::get);
    }

    public Try<Void> setProperty(@Nullable String name, @Nullable Object value) {
        return findNode(name, ValueNode.class).flatMap(node -> handle.set(node, value));
    }

    public Try<Void> addProperty(@Nullable String name, @Nullable Object value) {
        return findNode(name, ListValueNode.class).flatMap(node -> handle.add(node, value));
    }

    public Try<Void> removeProperty(@Nullable String name, @Nullable Object value) {
        return findNode(name, ListValueNode.class).flatMap(node -> handle.remove(node, value));
    }

    public Try<Void> resetProperty(@Nullable String name) {
        return findNode(name, ValueNode.class).flatMap(handle::reset);
    }

    public Try<Void> modifyProperty(
            @Nullable String name, @Nullable Object value, @NotNull PropertyModifyAction action) {
        return switch (action) {
            case SET -> setProperty(name, value);
            case ADD -> addProperty(name, value);
            case REMOVE -> removeProperty(name, value);
            case RESET -> resetProperty(name);
            default -> Try.failure(new IllegalArgumentException("Unknown action: " + action));
        };
    }

    public Try<Void> setPropertyString(@Nullable String name, @Nullable String value) {
        return setPropertyString(Bukkit.getConsoleSender(), name, value);
    }

    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setPropertyString(@NotNull CommandSender sender, @Nullable String name, @Nullable String value) {
        return findNode(name, ValueNode.class)
                .flatMap(node -> node.parseFromString(sender, value)
                        .flatMap(parsedValue -> handle.set(sender, node, parsedValue)));
    }

    public Try<Void> addPropertyString(@Nullable String name, @Nullable String value) {
        return addPropertyString(Bukkit.getConsoleSender(), name, value);
    }

    @ApiStatus.AvailableSince("5.1")
    public Try<Void> addPropertyString(@NotNull CommandSender sender, @Nullable String name, @Nullable String value) {
        return findNode(name, ListValueNode.class)
                .flatMap(node -> node.parseItemFromString(sender, value)
                        .flatMap(parsedValue -> handle.add(node, parsedValue)));
    }

    public Try<Void> removePropertyString(@Nullable String name, @Nullable String value) {
        return removePropertyString(Bukkit.getConsoleSender(), name, value);
    }

    @ApiStatus.AvailableSince("5.1")
    public Try<Void> removePropertyString(@NotNull CommandSender sender, @Nullable String name, @Nullable String value) {
        return findNode(name, ListValueNode.class)
                .flatMap(node -> node.parseItemFromString(sender, value)
                        .flatMap(parsedValue -> handle.remove(node, parsedValue)));
    }

    public Try<Void> modifyPropertyString(
            @Nullable String name,
            @Nullable String value,
            @NotNull PropertyModifyAction action
    ) {
        return modifyPropertyString(Bukkit.getConsoleSender(), name, value, action);
    }

    @ApiStatus.AvailableSince("5.1")
    public Try<Void> modifyPropertyString(
            @NotNull CommandSender sender,
            @Nullable String name,
            @Nullable String value,
            @NotNull PropertyModifyAction action
    ) {
        if (action.isRequireValue() && (value == null)) {
            return Try.failure(new IllegalArgumentException("Value is required for PropertyModifyAction: " + action));
        }
        return switch (action) {
            case SET -> setPropertyString(sender, name, value);
            case ADD -> addPropertyString(sender, name, value);
            case REMOVE -> removePropertyString(sender, name, value);
            case RESET -> resetProperty(name);
            default -> Try.failure(new IllegalArgumentException("Unknown action: " + action));
        };
    }

    private <T extends Node> Try<T> findNode(@Nullable String name, @NotNull Class<T> type) {
        return handle.getNodes().findNode(name, type)
                .toTry(() -> new ConfigNodeNotFoundException(name));
    }
}
