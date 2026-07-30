package org.mvplugins.multiverse.core.utils.compatibility;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

import java.lang.reflect.Method;
import java.nio.file.Path;

@ApiStatus.AvailableSince("5.6")
public final class BukkitCompatibility {

    private static final Try<Method> GET_LEVEL_DIRECTORY_METHOD;
    private static final Try<Method> GET_WORLD_NAMESPACED_KEY_METHOD;

    static {
        GET_LEVEL_DIRECTORY_METHOD = ReflectHelper.tryGetMethod(Server.class, "getLevelDirectory");
        GET_WORLD_NAMESPACED_KEY_METHOD = ReflectHelper.tryGetMethod(Bukkit.class, "getWorld", NamespacedKey.class);
    }

    @ApiStatus.AvailableSince("5.6")
    public static boolean isUsingNewDimensionStorage() {
        return GET_LEVEL_DIRECTORY_METHOD
                .flatMap(method -> ReflectHelper.tryInvokeMethod(Bukkit.getServer(), method))
                .isSuccess();
    }

    @ApiStatus.AvailableSince("5.6")
    @NotNull
    public static Path getWorldFoldersDirectory() {
        Server server = Bukkit.getServer();
        return GET_LEVEL_DIRECTORY_METHOD.flatMap(method -> ReflectHelper.tryInvokeMethod(server, method))
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .map(path -> path.resolve("dimensions"))
                .getOrElse(() -> server.getWorldContainer().toPath());
    }

    @ApiStatus.AvailableSince("5.6")
    @NotNull
    public static Option<World> getWorldByNameOrKey(@NotNull String nameOrKey) {
        return Option.of(Bukkit.getWorld(nameOrKey))
                .orElse(() -> GET_WORLD_NAMESPACED_KEY_METHOD
                        .flatMap(method -> ReflectHelper.tryInvokeStaticMethod(method, NamespacedKey.fromString(nameOrKey)))
                        .filter(World.class::isInstance)
                        .map(World.class::cast)
                        .toOption());
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public static Option<World> getWorldByNameOrKey(@NotNull WorldKeyOrName keyOrName) {
        return Option.of(Bukkit.getWorld(keyOrName.usableName()))
                .orElse(() -> GET_WORLD_NAMESPACED_KEY_METHOD
                        .flatMap(method -> ReflectHelper.tryInvokeStaticMethod(method, keyOrName.usableKey()))
                        .filter(World.class::isInstance)
                        .map(World.class::cast)
                        .toOption());
    }

    private BukkitCompatibility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
