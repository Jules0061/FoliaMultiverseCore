package org.mvplugins.multiverse.core.world.options;

import co.aikar.commands.ACFUtil;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.mvplugins.multiverse.core.utils.position.EntityPosition;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CreateWorldOptions {

    public static @NotNull CreateWorldOptions worldName(@NotNull String worldName) {
        return new CreateWorldOptions(Either.left(worldName));
    }

    @ApiStatus.AvailableSince("5.7")
    public static @NotNull CreateWorldOptions worldKey(@NotNull NamespacedKey key) {
        return new CreateWorldOptions(Either.right(WorldKeyOrName.parseKey(key)));
    }

    @ApiStatus.AvailableSince("5.7")
    public static @NotNull CreateWorldOptions worldKeyOrName(@NotNull WorldKeyOrName keyOrName) {
        return new CreateWorldOptions(Either.right(keyOrName));
    }

    private final Either<String, WorldKeyOrName> keyOrName;
    private String biome = "";
    private boolean bonusChest = false;
    private World.Environment environment = World.Environment.NORMAL;
    private EntityPosition forcedSpawnPosition = null;
    private boolean generateStructures = true;
    private String generator = null;
    private String generatorSettings = "";
    private long seed;
    private boolean useSpawnAdjust = true;
    private WorldType worldType = WorldType.NORMAL;
    private boolean doFolderCheck = true;
    private final Map<String, String> worldPropertyStrings = new HashMap<>();

    CreateWorldOptions(@NotNull Either<String, WorldKeyOrName> keyOrName) {
        this.keyOrName = keyOrName;
        this.seed = ACFUtil.RANDOM.nextLong();
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull Either<String, WorldKeyOrName> keyOrName() {
        return keyOrName;
    }

    @Deprecated(forRemoval = true, since = "5.7")
    public @NotNull String worldName() {
        return keyOrName.fold(name -> name ,WorldKeyOrName::usableName);
    }

    public @NotNull CreateWorldOptions biome(@NotNull String biome) {
        this.biome = biome;
        return this;
    }

    public @NotNull String biome() {
        return biome;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull CreateWorldOptions bonusChest(boolean bonusChest) {
        this.bonusChest = bonusChest;
        return this;
    }

    @ApiStatus.AvailableSince("5.7")
    public boolean bonusChest() {
        return bonusChest;
    }

    public @NotNull CreateWorldOptions environment(@NotNull World.Environment environmentInput) {
        this.environment = environmentInput;
        return this;
    }

    public @NotNull World.Environment environment() {
        return environment;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull CreateWorldOptions forcedSpawnPosition(@Nullable EntityPosition forcedSpawnPosition) {
        this.forcedSpawnPosition = forcedSpawnPosition;
        return this;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull Option<EntityPosition> forcedSpawnPosition() {
        return Option.of(this.forcedSpawnPosition);
    }

    public @NotNull CreateWorldOptions generateStructures(boolean generateStructuresInput) {
        this.generateStructures = generateStructuresInput;
        return this;
    }

    public boolean generateStructures() {
        return generateStructures;
    }

    public @NotNull CreateWorldOptions generator(@Nullable String generatorInput) {
        this.generator = generatorInput;
        return this;
    }

    public @Nullable String generator() {
        return generator;
    }

    public @NotNull CreateWorldOptions generatorSettings(@NotNull String generatorSettings) {
        this.generatorSettings = generatorSettings;
        return this;
    }

    public @NotNull String generatorSettings() {
        return generatorSettings;
    }

    public @NotNull CreateWorldOptions seed(@Nullable String seedInput) {
        if (seedInput == null) {
            return this;
        }
        try {
            this.seed = Long.parseLong(seedInput);
        } catch (NumberFormatException numberformatexception) {
            this.seed = seedInput.hashCode();
        }
        return this;
    }

    public @NotNull CreateWorldOptions seed(long seedInput) {
        this.seed = seedInput;
        return this;
    }

    public long seed() {
        return seed;
    }

    public @NotNull CreateWorldOptions useSpawnAdjust(boolean useSpawnAdjustInput) {
        this.useSpawnAdjust = useSpawnAdjustInput;
        return this;
    }

    public boolean useSpawnAdjust() {
        return useSpawnAdjust;
    }

    public @NotNull CreateWorldOptions worldType(@NotNull WorldType worldTypeInput) {
        this.worldType = worldTypeInput;
        return this;
    }

    public @NotNull WorldType worldType() {
        return worldType;
    }

    public @NotNull CreateWorldOptions doFolderCheck(boolean doFolderCheckInput) {
        this.doFolderCheck = doFolderCheckInput;
        return this;
    }

    public boolean doFolderCheck() {
        return doFolderCheck;
    }

    @ApiStatus.AvailableSince("5.5")
    public @NotNull CreateWorldOptions worldPropertyString(@NotNull String key, @Nullable String value) {
        this.worldPropertyStrings.put(key, value);
        return this;
    }

    @ApiStatus.AvailableSince("5.5")
    public @NotNull CreateWorldOptions worldPropertyStrings(@NotNull Map<@NotNull String, @Nullable String> worldProperties) {
        this.worldPropertyStrings.putAll(worldProperties);
        return this;
    }

    @ApiStatus.AvailableSince("5.5")
    public @UnmodifiableView @NotNull Map<String, String> worldPropertyStrings() {
        return Collections.unmodifiableMap(worldPropertyStrings);
    }
}
