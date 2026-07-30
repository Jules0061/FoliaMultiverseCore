package org.mvplugins.multiverse.core.destination;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

@Contract
public interface Destination<D extends Destination<D, T, F>, T extends DestinationInstance<T, D>, F extends FailureReason> {
    @NotNull String getIdentifier();

    @Deprecated(forRemoval = true, since = "5.1")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    default @NotNull Attempt<T, F> getDestinationInstance(@NotNull String destinationParams) {
        return getDestinationInstance(Bukkit.getConsoleSender(), destinationParams);
    }

    @ApiStatus.AvailableSince("5.1")
    default Attempt<T, F> getDestinationInstance(@NotNull CommandSender sender, @NotNull String destinationParams) {
        return getDestinationInstance(destinationParams);
    }

    @NotNull
     Collection<DestinationSuggestionPacket> suggestDestinations(
            @NotNull CommandSender commandSender, @Nullable String destinationParams);
}
