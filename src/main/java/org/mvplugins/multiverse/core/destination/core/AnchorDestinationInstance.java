package org.mvplugins.multiverse.core.destination.core;

import io.vavr.control.Option;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

public final class AnchorDestinationInstance extends DestinationInstance<AnchorDestinationInstance, AnchorDestination> {
    private final String anchorName;
    private final Location anchorLocation;

    AnchorDestinationInstance(
            @NotNull AnchorDestination destination,
            @NotNull String anchorName,
            @NotNull Location anchorLocation
    ) {
        super(destination);
        this.anchorName = anchorName;
        this.anchorLocation = anchorLocation;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        return Option.of(anchorLocation.clone());
    }

    @Override
    public @NotNull Option<Vector> getVelocity(@NotNull Entity teleportee) {
        return Option.none();
    }

    @Override
    public boolean checkTeleportSafety() {
        return true;
    }

    @Override
    public @NotNull Option<String> getFinerPermissionSuffix() {
        return Option.of(anchorName);
    }

    @Override
    public @NotNull Message getDisplayMessage() {
        return Message.of(MVCorei18n.DESTINATION_ANCHOR_DISPLAY, replace("{anchor}").with(anchorName));
    }

    @Override
    public @NotNull String serialise() {
        return anchorName;
    }
}
