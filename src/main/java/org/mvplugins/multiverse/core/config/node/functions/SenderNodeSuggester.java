package org.mvplugins.multiverse.core.config.node.functions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ApiStatus.AvailableSince("5.1")
@FunctionalInterface
public interface SenderNodeSuggester extends NodeSuggester {

    @ApiStatus.AvailableSince("5.1")
    @NotNull Collection<String> suggest(@NotNull CommandSender sender, @Nullable String input);

    @Override
    default @NotNull Collection<String> suggest(@Nullable String input) {
        return suggest(Bukkit.getConsoleSender(), input);
    }
}
