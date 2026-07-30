package org.mvplugins.multiverse.core.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import jakarta.inject.Inject;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;

@Service
public final class MVScheduler {

    private static final boolean REGIONISED = detectRegionisedServer();

    private static boolean detectRegionisedServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

    public static boolean isRegionisedServer() {
        return REGIONISED;
    }

    private final Plugin plugin;
    private final Server server;

    @Inject
    MVScheduler(@NotNull MultiverseCore plugin, @NotNull Server server) {
        this.plugin = plugin;
        this.server = server;
    }

    public boolean isGlobalThread() {
        return server.isGlobalTickThread();
    }

    public boolean ownsRegion(@NotNull Location location) {
        return !REGIONISED || server.isOwnedByCurrentRegion(location);
    }

    public void runAtLocation(@NotNull Location location, @NotNull Runnable action) {
        if (!plugin.isEnabled()) {
            return;
        }
        server.getRegionScheduler().run(plugin, location, ignore -> action.run());
    }

    public void runGlobal(@NotNull Runnable action) {
        if (!plugin.isEnabled()) {
            return;
        }
        server.getGlobalRegionScheduler().run(plugin, ignore -> action.run());
    }

    public void runGlobalNow(@NotNull Runnable action) {
        if (isGlobalThread()) {
            action.run();
            return;
        }
        runGlobal(action);
    }

    @Nullable
    public ScheduledTask runGlobalLater(@NotNull Runnable action, long delayTicks) {
        if (!plugin.isEnabled()) {
            return null;
        }
        return server.getGlobalRegionScheduler()
                .runDelayed(plugin, ignore -> action.run(), Math.max(1L, delayTicks));
    }

    public void runAtEntity(@NotNull Entity entity, @NotNull Runnable action) {
        if (!plugin.isEnabled()) {
            return;
        }
        entity.getScheduler().run(plugin, ignore -> action.run(), null);
    }

    public void runAtEntityLater(@NotNull Entity entity, @NotNull Runnable action, long delayTicks) {
        if (!plugin.isEnabled()) {
            return;
        }
        entity.getScheduler().runDelayed(plugin, ignore -> action.run(), null, Math.max(1L, delayTicks));
    }

    public void runAtChunk(@NotNull World world, int chunkX, int chunkZ, @NotNull Runnable action) {
        if (!plugin.isEnabled()) {
            return;
        }
        server.getRegionScheduler().run(plugin, world, chunkX, chunkZ, ignore -> action.run());
    }

    public void runAsync(@NotNull Runnable action) {
        if (!plugin.isEnabled()) {
            return;
        }
        server.getAsyncScheduler().runNow(plugin, ignore -> action.run());
    }
}
