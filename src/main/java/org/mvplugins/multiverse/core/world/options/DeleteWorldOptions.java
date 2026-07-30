package org.mvplugins.multiverse.core.world.options;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

import java.util.Collections;
import java.util.List;

public final class DeleteWorldOptions {

    public static @NotNull DeleteWorldOptions world(@NotNull MultiverseWorld world) {
        return new DeleteWorldOptions(world);
    }

    private final MultiverseWorld world;
    private List<String> keepFiles = Collections.emptyList();

    DeleteWorldOptions(MultiverseWorld world) {
        this.world = world;
    }

    public MultiverseWorld world() {
        return world;
    }

    public @NotNull DeleteWorldOptions keepFiles(List<String> keepFilesInput) {
        this.keepFiles = keepFilesInput == null ? Collections.emptyList() : keepFilesInput.stream().toList();
        return this;
    }

    public List<String> keepFiles() {
        return keepFiles;
    }
}
