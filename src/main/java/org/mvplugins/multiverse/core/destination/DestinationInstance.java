package org.mvplugins.multiverse.core.destination;

import io.vavr.control.Option;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.world.location.UnloadedWorldLocation;

public abstract class DestinationInstance<I extends  DestinationInstance<I, T>, T extends Destination<T, I, ?>> {

    protected final T destination;

    protected DestinationInstance(@NotNull T destination) {
        this.destination = destination;
    }

    public @NotNull T getDestination() {
        return this.destination;
    }

    public @NotNull String getIdentifier() {
        return this.destination.getIdentifier();
    }

    public abstract @NotNull Option<Location> getLocation(@NotNull Entity teleportee);

    public abstract @NotNull Option<Vector> getVelocity(@NotNull Entity teleportee);

    public abstract boolean checkTeleportSafety();

    public abstract @NotNull Option<String> getFinerPermissionSuffix();

    @ApiStatus.AvailableSince("5.4")
    @NotNull
    public Message getDisplayMessage() {
        return Message.of(this.toString());
    }

    @NotNull
    protected abstract String serialise();

    @Override
    public String toString() {
        return this.destination.getIdentifier() + ":" + this.serialise();
    }
}
