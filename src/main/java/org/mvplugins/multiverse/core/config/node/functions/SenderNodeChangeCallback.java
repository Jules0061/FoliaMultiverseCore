package org.mvplugins.multiverse.core.config.node.functions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.4")
@FunctionalInterface
public interface SenderNodeChangeCallback<T> extends NodeChangeCallback<T> {
    @ApiStatus.AvailableSince("5.4")
    void run(CommandSender sender, T oldValue, T newValue);

    @ApiStatus.AvailableSince("5.4")
    @Override
    default void run(T oldValue, T newValue) {
        run(Bukkit.getConsoleSender(), oldValue, newValue);
    }
}
