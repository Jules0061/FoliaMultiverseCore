package org.mvplugins.multiverse.core.world.options;

import io.vavr.control.Either;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

public final class CloneWorldOptions implements KeepWorldSettingsOptions {

    @Deprecated(forRemoval = true, since = "5.6")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static @NotNull CloneWorldOptions fromTo(@NotNull LoadedMultiverseWorld fromWorld, @NotNull String newWorldName) {
        return fromTo((MultiverseWorld) fromWorld, newWorldName);
    }

    @ApiStatus.AvailableSince("5.6")
    public static @NotNull CloneWorldOptions fromTo(@NotNull MultiverseWorld fromWorld, @NotNull String newWorldName) {
        return new CloneWorldOptions(fromWorld, Either.left(newWorldName));
    }

    @ApiStatus.AvailableSince("5.7")
    public static @NotNull CloneWorldOptions fromTo(@NotNull MultiverseWorld fromWorld, @NotNull NamespacedKey key) {
        return new CloneWorldOptions(fromWorld, Either.right(WorldKeyOrName.parseKey(key)));
    }

    @ApiStatus.AvailableSince("5.7")
    public static @NotNull CloneWorldOptions fromTo(@NotNull MultiverseWorld fromWorld, @NotNull WorldKeyOrName keyOrName) {
        return new CloneWorldOptions(fromWorld, Either.right(keyOrName));
    }

    private final MultiverseWorld fromWorld;
    private final Either<String, WorldKeyOrName> newWorldKeyOrName;
    private boolean keepGameRule = true;
    private boolean keepWorldConfig = true;
    private boolean saveBukkitWorld = true;

    private boolean keepWorldBorder = true;

    CloneWorldOptions(MultiverseWorld fromWorld, Either<String, WorldKeyOrName> newWorldKeyOrName) {
        this.fromWorld = fromWorld;
        this.newWorldKeyOrName = newWorldKeyOrName;
    }

    @Deprecated(forRemoval = true, since = "5.6")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public LoadedMultiverseWorld world() {
        return fromWorld.asLoadedWorld().getOrNull();
    }

    @ApiStatus.AvailableSince("5.6")
    public @NotNull MultiverseWorld fromWorld() {
        return fromWorld;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull Either<String, WorldKeyOrName> newWorldKeyOrName() {
        return newWorldKeyOrName;
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public @NotNull String newWorldName() {
        return newWorldKeyOrName.fold(name -> name, WorldKeyOrName::usableName);
    }

    @Override
    public @NotNull CloneWorldOptions keepGameRule(boolean keepGameRuleInput) {
        this.keepGameRule = keepGameRuleInput;
        return this;
    }

    @Override
    public boolean keepGameRule() {
        return keepGameRule;
    }

    @Override
    public @NotNull CloneWorldOptions keepWorldConfig(boolean keepWorldConfigInput) {
        this.keepWorldConfig = keepWorldConfigInput;
        return this;
    }

    @Override
    public boolean keepWorldConfig() {
        return keepWorldConfig;
    }

    @Override
    public @NotNull CloneWorldOptions keepWorldBorder(boolean keepWorldBorderInput) {
        this.keepWorldBorder = keepWorldBorderInput;
        return this;
    }

    @Override
    public boolean keepWorldBorder() {
        return keepWorldBorder;
    }

    public @NotNull CloneWorldOptions saveBukkitWorld(boolean saveBukkitWorldInput) {
        this.saveBukkitWorld = saveBukkitWorldInput;
        return this;
    }

    public boolean saveBukkitWorld() {
        return saveBukkitWorld;
    }
}
