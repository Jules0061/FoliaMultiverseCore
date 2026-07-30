package org.mvplugins.multiverse.core.config.node;

import java.util.Collection;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.handle.BaseConfigurationHandle;
import org.mvplugins.multiverse.core.config.node.serializer.NodeSerializer;

public interface ValueNode<T> extends Node {

    @NotNull Option<String> getName();

    @NotNull Class<T> getType();

    @ApiStatus.AvailableSince("5.1")
    default @NotNull String[] getAliases() {
        return Strings.EMPTY_ARRAY;
    }

    @Nullable T getDefaultValue();

    @NotNull Collection<String> suggest(@Nullable String input);

    @ApiStatus.AvailableSince("5.1")
    @NotNull Collection<String> suggest(@NotNull CommandSender sender, @Nullable String input);

    @NotNull Try<T> parseFromString(@Nullable String input);

    @ApiStatus.AvailableSince("5.1")
    default @NotNull Try<T> parseFromString(@NotNull CommandSender sender, @Nullable String input) {
        return parseFromString(input);
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    @Nullable NodeSerializer<T> getSerializer();

    @ApiStatus.AvailableSince("5.7")
    @Nullable T deserialize(@Nullable Object object);

    @ApiStatus.AvailableSince("5.7")
    @Nullable Object serialize(@Nullable T value);

    Try<Void> validate(@Nullable T value);

    @ApiStatus.AvailableSince("5.4")
    void onLoad(@Nullable T value);

    @ApiStatus.AvailableSince("5.4")
    void onLoadAndChange(@NotNull CommandSender sender, @Nullable T oldValue, @Nullable T newValue);

    @ApiStatus.AvailableSince("5.4")
    void onChange(@NotNull CommandSender sender, @Nullable T oldValue, @Nullable T newValue);

    @Deprecated(since = "5.4", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    default void onSetValue(@Nullable T oldValue, @Nullable T newValue) {
        onLoadAndChange(Bukkit.getConsoleSender(), oldValue, newValue);
    }
}
