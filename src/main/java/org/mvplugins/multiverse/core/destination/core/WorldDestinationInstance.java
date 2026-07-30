package org.mvplugins.multiverse.core.destination.core;

import io.vavr.control.Option;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

public final class WorldDestinationInstance extends DestinationInstance<WorldDestinationInstance, WorldDestination> {
    private final MultiverseWorld world;
    private final String direction;
    private final float yaw;

    WorldDestinationInstance(
            @NotNull WorldDestination destination,
            @NotNull MultiverseWorld world,
            @Nullable String direction,
            float yaw
    ) {
        super(destination);
        this.world = world;
        this.direction = direction;
        this.yaw = yaw;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        if (!world.isLoaded()) {
            return Option.none();
        }

        Location worldLoc = world.getSpawnLocation();
        if (!worldLoc.isWorldLoaded()) {
            return Option.none();
        }

        if (this.yaw >= 0) {
            worldLoc.setYaw(this.yaw);
        }
        return Option.of(worldLoc);
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
        return Option.of(world.getName());
    }

    @Override
    public @NotNull Message getDisplayMessage() {
        return Message.of(world.getAliasOrName());
    }

    @Override
    public @NotNull String serialise() {
        if (this.direction != null) {
            return this.world.getName() + ":" + this.direction;
        }
        return this.world.getName();
    }
}
