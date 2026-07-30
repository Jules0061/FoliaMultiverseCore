package org.mvplugins.multiverse.core.world.helpers;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.vavr.control.Option;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.core.utils.compatibility.BukkitCompatibility;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

@Service
public final class WorldNameChecker {

    private static final Set<String> BLACKLIST_NAMES = Set.of(
            "cache",
            "config",
            "crash-reports",
            "libraries",
            "logs",
            "plugins",
            "versions");

    private static final List<WorldFolderSchema> WORLD_FOLDER_SCHEMA = List.of(
            WorldFolderSchema.file("level.dat"),
            WorldFolderSchema.folder("DIM1"),
            WorldFolderSchema.folder("DIM-1"),
            WorldFolderSchema.file("paper-world.yml"),
            WorldFolderSchema.folder("data"),
            WorldFolderSchema.folder("entities"),
            WorldFolderSchema.folder("poi"),
            WorldFolderSchema.folder("region"));

    public boolean isValidWorldName(@Nullable String worldName) {
        return checkName(worldName) == NameStatus.VALID;
    }

    @NotNull
    public NameStatus checkName(@Nullable String worldName) {
        return Option.of(worldName)
                .map(name -> name.toLowerCase(Locale.ENGLISH))
                .map(name -> {
                    if (name.isEmpty()) {
                        return NameStatus.EMPTY;
                    }
                    if (!BukkitCompatibility.isUsingNewDimensionStorage() && BLACKLIST_NAMES.contains(name)) {
                        return NameStatus.BLACKLISTED;
                    }
                    if (!REPatterns.NAMESPACE_KEY.matcher(name).matches()) {
                        return NameStatus.INVALID_CHARS;
                    }
                    return NameStatus.VALID;
                })
                .getOrElse(NameStatus.EMPTY);
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean hasWorldFolder(@Nullable String worldName) {
        return checkFolder(worldName) != FolderStatus.DOES_NOT_EXIST;
    }

    @ApiStatus.AvailableSince("5.7")
    public boolean hasWorldFolder(@Nullable WorldKeyOrName worldKey) {
        return checkFolder(worldKey) != FolderStatus.DOES_NOT_EXIST;
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public boolean isValidWorldFolder(@Nullable String worldName) {
        return checkFolder(worldName).loadable;
    }

    @ApiStatus.AvailableSince("5.7")
    public boolean isValidWorldFolder(@Nullable WorldKeyOrName nameOrKey) {
        return checkFolder(nameOrKey).loadable;
    }

    public boolean isValidWorldFolder(@Nullable File worldFolder) {
        return checkFolder(worldFolder).loadable;
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    @NotNull
    public FolderStatus checkFolder(@Nullable String worldName) {
        if (worldName == null) {
            return FolderStatus.DOES_NOT_EXIST;
        }

        return WorldKeyOrName.parse(worldName)
                .map(this::checkFolder)
                .getOrElse(FolderStatus.NOT_A_WORLD);
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public FolderStatus checkFolder(@Nullable WorldKeyOrName keyOrName) {
        if (keyOrName == null) {
            return FolderStatus.DOES_NOT_EXIST;
        }

        if (BukkitCompatibility.isUsingNewDimensionStorage()) {
            File oldWorldFolder = WorldFolderResolver.resolveAsLegacyWorldName(keyOrName.usableName());
            if (checkFolder(oldWorldFolder) == FolderStatus.VALID) {
                return FolderStatus.REQUIRES_MIGRATION;
            }
        }

        return checkFolder(WorldFolderResolver.resolve(keyOrName));
    }

    @NotNull
    public FolderStatus checkFolder(@Nullable File worldFolder) {
        if (worldFolder == null || !worldFolder.exists() || !worldFolder.isDirectory()) {
            return FolderStatus.DOES_NOT_EXIST;
        }
        if (!folderWorldSchemaCheck(worldFolder)) {
            return FolderStatus.NOT_A_WORLD;
        }
        return FolderStatus.VALID;
    }

    private boolean folderWorldSchemaCheck(@NotNull File worldFolder) {
        return WORLD_FOLDER_SCHEMA.stream()
                .filter(schema -> schema.check(worldFolder))
                .count() >= 2;
    }

    private interface WorldFolderSchema {

        static WorldFolderSchema file(String path) {
            return new WorldFile(path);
        }

        static WorldFolderSchema folder(String path) {
            return new WorldFolder(path);
        }

        boolean check(File worldFolder);

        final class WorldFile implements WorldFolderSchema {
            private final String path;

            private WorldFile(String path) {
                this.path = path;
            }

            @Override
            public boolean check(File worldFolder) {
                File thisFolder = worldFolder.toPath().resolve(path).toFile();
                return thisFolder.exists() && thisFolder.isFile();
            }
        }

        final class WorldFolder implements WorldFolderSchema {
            private final String path;

            private WorldFolder(String path) {
                this.path = path;
            }

            @Override
            public boolean check(File worldFolder) {
                File thisFolder = worldFolder.toPath().resolve(path).toFile();
                return thisFolder.exists() && thisFolder.isDirectory();
            }
        }
    }

    public enum NameStatus {
        VALID,

        INVALID_CHARS,

        EMPTY,

        BLACKLISTED
    }

    public enum FolderStatus {
        VALID(true),

        REQUIRES_MIGRATION(true),

        NOT_A_WORLD(false),

        DOES_NOT_EXIST(false),
        ;

        private final boolean loadable;

        FolderStatus(boolean loadable) {
            this.loadable = loadable;
        }

        @ApiStatus.AvailableSince("5.6")
        public boolean isLoadable() {
            return loadable;
        }
    }
}
