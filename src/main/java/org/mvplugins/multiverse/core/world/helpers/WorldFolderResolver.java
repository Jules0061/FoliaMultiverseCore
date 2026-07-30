package org.mvplugins.multiverse.core.world.helpers;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.utils.compatibility.BukkitCompatibility;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

import java.io.File;

@ApiStatus.AvailableSince("5.7")
public final class WorldFolderResolver {

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static File resolve(@NotNull WorldKeyOrName keyOrName) {
        return BukkitCompatibility.isUsingNewDimensionStorage()
                ? resolveAsDimensionKey(keyOrName.usableKey())
                : resolveAsLegacyWorldName(keyOrName.usableName());
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static File resolve(@NotNull MultiverseWorld multiverseWorld) {
        File dimensionKey = resolveAsDimensionKey(multiverseWorld.getKey());
        File legacy = resolveAsLegacyWorldName(multiverseWorld.getName());
        if (BukkitCompatibility.isUsingNewDimensionStorage()) {
            return !dimensionKey.isDirectory() && legacy.isDirectory() ? legacy : dimensionKey;
        }
        return legacy;
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static File resolveAsDimensionKey(@NotNull NamespacedKey namespacedKey) {
        return BukkitCompatibility.getWorldFoldersDirectory()
                .resolve(namespacedKey.getNamespace())
                .resolve(namespacedKey.getKey())
                .toFile();
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static File resolveAsLegacyWorldName(@NotNull String worldName) {
        return Bukkit.getWorldContainer()
                .toPath()
                .resolve(worldName)
                .toFile();
    }

    private WorldFolderResolver() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
