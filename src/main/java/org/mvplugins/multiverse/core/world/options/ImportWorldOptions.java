package org.mvplugins.multiverse.core.world.options;

import io.vavr.control.Either;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

public final class ImportWorldOptions {

    public static @NotNull ImportWorldOptions worldName(@NotNull String worldName) {
        return new ImportWorldOptions(Either.left(worldName));
    }

    @ApiStatus.AvailableSince("5.7")
    public static @NotNull ImportWorldOptions worldKey(@NotNull NamespacedKey key) {
        return new ImportWorldOptions(Either.right(WorldKeyOrName.parseKey(key)));
    }

    @ApiStatus.AvailableSince("5.7")
    public static @NotNull ImportWorldOptions worldKeyOrName(@NotNull WorldKeyOrName keyOrName) {
        return new ImportWorldOptions(Either.right(keyOrName));
    }

    private final Either<String, WorldKeyOrName> keyOrName;
    private String biome = "";
    private World.Environment environment = World.Environment.NORMAL;
    private String generator = null;
    private String generatorSettings = "";
    private boolean useSpawnAdjust = true;
    private boolean doFolderCheck = true;

    ImportWorldOptions(Either<String, WorldKeyOrName> keyOrName) {
        this.keyOrName = keyOrName;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull Either<String, WorldKeyOrName> keyOrName() {
        return keyOrName;
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public @NotNull String worldName() {
        return keyOrName.fold(name -> name ,WorldKeyOrName::usableName);
    }

    public @NotNull ImportWorldOptions biome(@NotNull String biome) {
        this.biome = biome;
        return this;
    }

    public @NotNull String biome() {
        return biome;
    }

    public @NotNull ImportWorldOptions environment(@NotNull World.Environment environmentInput) {
        this.environment = environmentInput;
        return this;
    }

    public @NotNull World.Environment environment() {
        return environment;
    }

    public @NotNull ImportWorldOptions generator(@Nullable String generatorInput) {
        this.generator = generatorInput;
        return this;
    }

    public @Nullable String generator() {
        return generator;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull ImportWorldOptions generatorSettings(@NotNull String generatorSettingsInput) {
        this.generatorSettings = generatorSettingsInput;
        return this;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull String generatorSettings() {
        return generatorSettings;
    }

    public @NotNull ImportWorldOptions useSpawnAdjust(boolean useSpawnAdjustInput) {
        this.useSpawnAdjust = useSpawnAdjustInput;
        return this;
    }

    public boolean useSpawnAdjust() {
        return useSpawnAdjust;
    }

    @ApiStatus.AvailableSince("5.2")
    public @NotNull ImportWorldOptions doFolderCheck(boolean doFolderCheckInput) {
        this.doFolderCheck = doFolderCheckInput;
        return this;
    }

    @ApiStatus.AvailableSince("5.2")
    public boolean doFolderCheck() {
        return doFolderCheck;
    }
}
