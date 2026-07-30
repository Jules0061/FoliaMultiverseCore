package org.mvplugins.multiverse.core.config.node;

import java.util.Collection;
import java.util.List;

import io.vavr.control.Try;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.node.serializer.NodeSerializer;

public interface ListValueNode<I> extends ValueNode<List<I>> {

    @NotNull Class<I> getItemType();

    @NotNull Collection<String> suggestItem(@Nullable String input);

    @ApiStatus.AvailableSince("5.1")
    default @NotNull Collection<String> suggestItem(@NotNull CommandSender sender, @Nullable String input) {
        return suggestItem(input);
    }

    @NotNull Try<I> parseItemFromString(@Nullable String input);

    @ApiStatus.AvailableSince("5.1")
    default @NotNull Try<I> parseItemFromString(@NotNull CommandSender sender, @Nullable String input) {
        return parseItemFromString(input);
    }

    @Nullable NodeSerializer<I> getItemSerializer();

    Try<Void> validateItem(@Nullable I value);

    void onSetItemValue(@Nullable I oldValue, @Nullable I newValue);
}
