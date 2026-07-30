package org.mvplugins.multiverse.core.world.options;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

@ApiStatus.AvailableSince("5.2")
public final class LoadWorldOptions {

    @ApiStatus.AvailableSince("5.2")
    public static LoadWorldOptions world(MultiverseWorld world) {
        return new LoadWorldOptions(world);
    }

    private final MultiverseWorld world;
    private boolean doFolderCheck = true;

    LoadWorldOptions(MultiverseWorld world) {
        this.world = world;
    }

    @ApiStatus.AvailableSince("5.2")
    public MultiverseWorld world() {
        return world;
    }

    @ApiStatus.AvailableSince("5.2")
    public boolean doFolderCheck() {
        return doFolderCheck;
    }

    @ApiStatus.AvailableSince("5.2")
    public LoadWorldOptions doFolderCheck(boolean doFolderCheck) {
        this.doFolderCheck = doFolderCheck;
        return this;
    }
}
