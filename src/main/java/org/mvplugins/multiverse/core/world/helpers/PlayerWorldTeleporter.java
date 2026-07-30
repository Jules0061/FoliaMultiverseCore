package org.mvplugins.multiverse.core.world.helpers;

import java.util.List;

import jakarta.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.core.teleportation.TeleportFailureReason;
import org.mvplugins.multiverse.core.utils.result.AsyncAttemptsAggregate;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;

@Service
public final class PlayerWorldTeleporter {
    private final WorldManager worldManager;
    private final AsyncSafetyTeleporter safetyTeleporter;

    @Inject
    PlayerWorldTeleporter(@NotNull WorldManager worldManager, @NotNull AsyncSafetyTeleporter safetyTeleporter) {
        this.worldManager = worldManager;
        this.safetyTeleporter = safetyTeleporter;
    }

    public AsyncAttemptsAggregate<Void, TeleportFailureReason> removeFromWorld(@NotNull LoadedMultiverseWorld world) {
        World toWorld = worldManager.getDefaultWorld().flatMap(LoadedMultiverseWorld::getBukkitWorld)
                .getOrElse(Bukkit.getWorlds().get(0));
        return transferFromWorldTo(world, toWorld);
    }

    public AsyncAttemptsAggregate<Void, TeleportFailureReason> transferFromWorldTo(
            @NotNull LoadedMultiverseWorld from,
            @NotNull MultiverseWorld to) {
        return transferAllFromWorldToLocation(from, to.getSpawnLocation());
    }

    public AsyncAttemptsAggregate<Void, TeleportFailureReason> transferFromWorldTo(
            @NotNull LoadedMultiverseWorld from,
            @NotNull World to) {
        return transferAllFromWorldToLocation(from, to.getSpawnLocation());
    }

    public AsyncAttemptsAggregate<Void, TeleportFailureReason> transferAllFromWorldToLocation(
            @NotNull LoadedMultiverseWorld world,
            @NotNull Location location) {
        return world.getPlayers()
                .map(players -> safetyTeleporter.to(location).teleport(players))
                .getOrElse(AsyncAttemptsAggregate::emptySuccess);
    }

    @ApiStatus.AvailableSince("5.7")
    public AsyncAttemptsAggregate<Void, TeleportFailureReason> transferAllFromWorldToDestination(
            @NotNull LoadedMultiverseWorld world,
            @NotNull DestinationInstance<?, ?> destinationInstance) {
        return world.getPlayers()
                .map(players -> safetyTeleporter.to(destinationInstance).teleport(players))
                .getOrElse(AsyncAttemptsAggregate::emptySuccess);
    }

    public AsyncAttemptsAggregate<Void, TeleportFailureReason> teleportPlayersToWorld(
            @NotNull List<Player> players,
            @NotNull MultiverseWorld world) {
        Location spawnLocation = world.getSpawnLocation();
        return safetyTeleporter.to(spawnLocation).teleport(players);
    }
}
