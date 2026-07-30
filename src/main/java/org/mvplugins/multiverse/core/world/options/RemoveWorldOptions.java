package org.mvplugins.multiverse.core.world.options;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

@ApiStatus.AvailableSince("5.2")
public final class RemoveWorldOptions {

    @ApiStatus.AvailableSince("5.2")
    public static RemoveWorldOptions world(MultiverseWorld world) {
        return new RemoveWorldOptions(world);
    }

    private final MultiverseWorld world;
    private boolean saveBukkitWorld = true;
    private boolean unloadBukkitWorld = true;

    private RemoveWorldOptions(MultiverseWorld world) {
        this.world = world;
    }

    @ApiStatus.AvailableSince("5.2")
    public MultiverseWorld world() {
        return world;
    }

    @ApiStatus.AvailableSince("5.2")
    public RemoveWorldOptions saveBukkitWorld(boolean saveBukkitWorldInput) {
        this.saveBukkitWorld = saveBukkitWorldInput;
        return this;
    }

    @ApiStatus.AvailableSince("5.2")
    public boolean saveBukkitWorld() {
        return saveBukkitWorld;
    }

    @ApiStatus.AvailableSince("5.2")
    public RemoveWorldOptions unloadBukkitWorld(boolean unloadBukkitWorldInput) {
        this.unloadBukkitWorld = unloadBukkitWorldInput;
        return this;
    }

    @ApiStatus.AvailableSince("5.2")
    public boolean unloadBukkitWorld() {
        return unloadBukkitWorld;
    }
}
