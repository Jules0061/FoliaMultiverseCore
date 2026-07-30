package org.mvplugins.multiverse.core.world.options;

import co.aikar.commands.ACFUtil;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;

import java.util.Collections;
import java.util.List;

public final class RegenWorldOptions implements KeepWorldSettingsOptions {

    private static final long UNINITIALIZED_SEED_VALUE = Long.MIN_VALUE;

    public static @NotNull RegenWorldOptions world(@NotNull LoadedMultiverseWorld world) {
        return new RegenWorldOptions(world);
    }

    private final LoadedMultiverseWorld world;
    private Biome biome;
    private boolean keepGameRule = true;
    private boolean keepWorldConfig = true;
    private boolean keepWorldBorder = true;
    private boolean randomSeed = false;
    private long seed = UNINITIALIZED_SEED_VALUE;
    private List<String> keepFiles = Collections.emptyList();

    RegenWorldOptions(@NotNull LoadedMultiverseWorld world) {
        this.world = world;
    }

    private boolean isSeedInitialized() {
        return seed != UNINITIALIZED_SEED_VALUE;
    }

    public @NotNull LoadedMultiverseWorld world() {
        return world;
    }

    public @NotNull RegenWorldOptions biome(@Nullable Biome biome) {
        this.biome = biome;
        return this;
    }

    public @NotNull Biome biome() {
        return biome;
    }

    @Override
    public @NotNull RegenWorldOptions keepGameRule(boolean keepGameRuleInput) {
        this.keepGameRule = keepGameRuleInput;
        return this;
    }

    @Override
    public boolean keepGameRule() {
        return keepGameRule;
    }

    @Override
    public @NotNull RegenWorldOptions keepWorldConfig(boolean keepWorldConfigInput) {
        this.keepWorldConfig = keepWorldConfigInput;
        return this;
    }

    @Override
    public boolean keepWorldConfig() {
        return keepWorldConfig;
    }

    @Override
    public @NotNull RegenWorldOptions keepWorldBorder(boolean keepWorldBorderInput) {
        this.keepWorldBorder = keepWorldBorderInput;
        return this;
    }

    @Override
    public boolean keepWorldBorder() {
        return keepWorldBorder;
    }

    public @NotNull RegenWorldOptions randomSeed(boolean randomSeedInput) {
        if (randomSeedInput && isSeedInitialized()) {
            throw new IllegalStateException("Cannot set randomSeed to true when seed is set");
        }
        this.randomSeed = randomSeedInput;
        return this;
    }

    @SuppressWarnings("unused")
    public boolean randomSeed() {
        return randomSeed;
    }

    public @NotNull RegenWorldOptions seed(@Nullable String seedInput) {
        if (seedInput == null) {
            this.seed = UNINITIALIZED_SEED_VALUE;
            return this;
        }
        if (randomSeed) {
            randomSeed(false);
        }
        this.seed = parseOrHashSeed(seedInput);
        return this;
    }

    private long parseOrHashSeed(String seedInput) {
        try {
            return Long.parseLong(seedInput);
        } catch (NumberFormatException numberformatexception) {
            return seedInput.hashCode();
        }
    }

    public @NotNull RegenWorldOptions seed(long seedInput) {
        this.seed = seedInput;
        return this;
    }

    public long seed() {
        if (randomSeed) {
            return ACFUtil.RANDOM.nextLong();
        } else if (isSeedInitialized()) {
            return seed;
        }
        return world.getSeed();
    }

    public @NotNull RegenWorldOptions keepFiles(@Nullable List<String> keepFilesInput) {
        this.keepFiles = keepFilesInput == null ? Collections.emptyList() : keepFilesInput.stream().toList();
        return this;
    }

    public @NotNull List<String> keepFiles() {
        return keepFiles;
    }
}
