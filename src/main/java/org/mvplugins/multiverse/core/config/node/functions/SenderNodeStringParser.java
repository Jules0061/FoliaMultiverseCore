package org.mvplugins.multiverse.core.config.node.functions;

import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.AvailableSince("5.1")
@FunctionalInterface
public interface SenderNodeStringParser<T> extends NodeStringParser<T> {
    @ApiStatus.AvailableSince("5.1")
    @NotNull Try<T> parse(@NotNull CommandSender sender, @Nullable String string, @NotNull Class<T> type);

    @Override
    default @NotNull Try<T> parse(@Nullable String string, @NotNull Class<T> type) {
        return parse(Bukkit.getConsoleSender(), string, type);
    }
}
