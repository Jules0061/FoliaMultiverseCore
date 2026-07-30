package org.mvplugins.multiverse.core.listeners;

import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.LoadWorldOptions;
import org.mvplugins.multiverse.core.world.options.UnloadWorldOptions;
import org.mvplugins.multiverse.core.world.reasons.LoadFailureReason;
import org.mvplugins.multiverse.core.world.reasons.UnloadFailureReason;

@Service
final class MVWorldListener implements CoreListener {

    private final WorldManager worldManager;

    @Inject
    MVWorldListener(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventMethod
    @DefaultEventPriority(EventPriority.MONITOR)
    void worldUnload(WorldUnloadEvent event) {
        if (event.isCancelled()) {
            return;
        }
        worldManager.getLoadedWorld(event.getWorld().getName())
                .peek(world -> worldManager.unloadWorld(UnloadWorldOptions
                                .world(world)
                                .unloadBukkitWorld(false))
                        .onFailure(failure -> {
                            if (failure.getFailureReason() != UnloadFailureReason.WORLD_ALREADY_UNLOADING) {
                                Logging.severe("Failed to unload world: " + failure);
                            }
                        }));
    }

    @EventMethod
    @DefaultEventPriority(EventPriority.MONITOR)
    void worldLoad(WorldLoadEvent event) {
        worldManager.getUnloadedWorld(event.getWorld().getName())
                .peek(world -> {
                    Logging.fine("Loading world: " + world.getName());
                    worldManager.loadWorld(LoadWorldOptions.world(world)).onFailure(failure -> {
                        if (failure.getFailureReason() != LoadFailureReason.WORLD_ALREADY_LOADING) {
                            Logging.severe("Failed to load world: " + failure);
                        }
                    });
                });
    }
}
