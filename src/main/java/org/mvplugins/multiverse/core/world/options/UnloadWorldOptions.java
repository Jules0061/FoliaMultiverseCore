package org.mvplugins.multiverse.core.world.options;

import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;

public final class UnloadWorldOptions {

    public static UnloadWorldOptions world(LoadedMultiverseWorld world) {
        return new UnloadWorldOptions(world);
    }

    private final LoadedMultiverseWorld world;
    private boolean saveBukkitWorld = true;
    private boolean unloadBukkitWorld = true;

    UnloadWorldOptions(LoadedMultiverseWorld world) {
        this.world = world;
    }

    public LoadedMultiverseWorld world() {
        return world;
    }

    public UnloadWorldOptions unloadBukkitWorld(boolean unloadBukkitWorldInput) {
        this.unloadBukkitWorld = unloadBukkitWorldInput;
        return this;
    }

    public boolean unloadBukkitWorld() {
        return unloadBukkitWorld;
    }

    public UnloadWorldOptions saveBukkitWorld(boolean saveBukkitWorldInput) {
        this.saveBukkitWorld = saveBukkitWorldInput;
        return this;
    }

    public boolean saveBukkitWorld() {
        return saveBukkitWorld;
    }
}
