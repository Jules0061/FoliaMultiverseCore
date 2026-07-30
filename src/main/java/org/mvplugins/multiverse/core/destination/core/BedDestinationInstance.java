package org.mvplugins.multiverse.core.destination.core;

import io.vavr.control.Option;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;

public final class BedDestinationInstance extends DestinationInstance<BedDestinationInstance, BedDestination> {
    private final @Nullable Player player;

    BedDestinationInstance(@NotNull BedDestination destination, @Nullable Player player) {
        super(destination);
        this.player = player;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        if (player != null) {
            return Option.of(player.getBedSpawnLocation());
        }
        if (teleportee instanceof Player) {
            return Option.of(((Player) teleportee).getBedSpawnLocation());
        }
        return Option.none();
    }

    @Override
    public @NotNull Option<Vector> getVelocity(@NotNull Entity teleportee) {
        return Option.none();
    }

    @Override
    public boolean checkTeleportSafety() {
        return false;
    }

    @Override
    public @NotNull Option<String> getFinerPermissionSuffix() {
        return Option.of(player != null ? player.getName() : BedDestination.OWN_BED_STRING);
    }

    @Override
    public @NotNull Message getDisplayMessage() {
        return player == null
                ? Message.of(MVCorei18n.DESTINATION_BED_DISPLAY_OWN)
                : Message.of(MVCorei18n.DESTINATION_BED_DISPLAY_OTHER, Replace.PLAYER.with(player.getName()));
    }

    @Override
    public @NotNull String serialise() {
        return player != null ? player.getName() : BedDestination.OWN_BED_STRING;
    }
}
