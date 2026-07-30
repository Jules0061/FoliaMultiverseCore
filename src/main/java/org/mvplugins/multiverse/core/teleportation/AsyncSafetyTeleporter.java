package org.mvplugins.multiverse.core.teleportation;

import io.vavr.control.Either;
import jakarta.inject.Inject;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.utils.MVScheduler;

@Service
public final class AsyncSafetyTeleporter {
    @NotNull
    private final MVScheduler scheduler;
    private final BlockSafety blockSafety;
    private final TeleportQueue teleportQueue;
    private final PluginManager pluginManager;

    @Inject
    AsyncSafetyTeleporter(
            @NotNull MVScheduler scheduler,
            @NotNull BlockSafety blockSafety,
            @NotNull TeleportQueue teleportQueue,
            @NotNull PluginManager pluginManager) {
        this.scheduler = scheduler;
        this.blockSafety = blockSafety;
        this.teleportQueue = teleportQueue;
        this.pluginManager = pluginManager;
    }

    public AsyncSafetyTeleporterAction to(@Nullable Location location) {
        return new AsyncSafetyTeleporterAction(
                scheduler,
                blockSafety,
                teleportQueue,
                pluginManager,
                Either.left(location)
        );
    }

    public AsyncSafetyTeleporterAction to(@Nullable DestinationInstance<?, ?> destination) {
        return new AsyncSafetyTeleporterAction(
                scheduler,
                blockSafety,
                teleportQueue,
                pluginManager,
                Either.right(destination)
        );
    }
}
