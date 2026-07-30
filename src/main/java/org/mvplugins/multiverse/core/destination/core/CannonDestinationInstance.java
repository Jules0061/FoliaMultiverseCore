package org.mvplugins.multiverse.core.destination.core;

import io.vavr.control.Option;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.world.location.UnloadedWorldLocation;

public final class CannonDestinationInstance extends DestinationInstance<CannonDestinationInstance, CannonDestination> {
    private final UnloadedWorldLocation location;
    private final double speed;

    CannonDestinationInstance(@NotNull CannonDestination destination, @NotNull UnloadedWorldLocation location, double speed) {
        super(destination);
        this.location = location;
        this.speed = speed;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        if (location.getWorld() == null) {
            return Option.none();
        }
        return Option.of(location.toBukkitLocation());
    }

    @Override
    public @NotNull Option<Vector> getVelocity(@NotNull Entity teleportee) {
        double pitchRadians = Math.toRadians(location.getPitch());
        double yawRadians = Math.toRadians(location.getYaw());
        double x = Math.sin(yawRadians) * speed * -1;
        double y = Math.sin(pitchRadians) * speed * -1;
        double z = Math.cos(yawRadians) * speed;
        x = Math.cos(pitchRadians) * x;
        z = Math.cos(pitchRadians) * z;
        return Option.of(new Vector(x, y, z));
    }

    @Override
    public boolean checkTeleportSafety() {
        return false;
    }

    @Override
    public @NotNull Option<String> getFinerPermissionSuffix() {
        return Option.of(location.getWorld()).map(World::getName);
    }

    @Override
    public @NotNull String serialise() {
        return location.getWorldName() + ":" + location.getX() + "," + location.getY()
                + "," + location.getZ() + ":" + location.getPitch() + ":" + location.getYaw() + ":" + this.speed;
    }
}
