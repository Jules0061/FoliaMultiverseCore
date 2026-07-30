package org.mvplugins.multiverse.core.utils.compatibility;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.core.utils.position.EntityPosition;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

import java.lang.reflect.Method;

@ApiStatus.AvailableSince("5.7")
public final class WorldCreatorCompatibility {

    private static final Try<Class<?>> POSITION_CLASS;
    private static final Try<Method> FORCED_SPAWN_POSITION_METHOD;
    private static final Try<Method> OF_KEY_METHOD;
    private static final Try<Method> OF_NAME_AND_KEY_METHOD;
    private static final Try<Method> BONUS_CHEST_METHOD;

    static {
        POSITION_CLASS = ReflectHelper.tryGetClass("io.papermc.paper.math.Position");
        FORCED_SPAWN_POSITION_METHOD = POSITION_CLASS.flatMap(positionClass ->
                ReflectHelper.tryGetMethod(WorldCreator.class, "forcedSpawnPosition", positionClass, float.class, float.class));
        BONUS_CHEST_METHOD = ReflectHelper.tryGetMethod(WorldCreator.class, "bonusChest", boolean.class);
        OF_KEY_METHOD = ReflectHelper.tryGetMethod(WorldCreator.class, "ofKey", NamespacedKey.class);
        OF_NAME_AND_KEY_METHOD = ReflectHelper.tryGetMethod(WorldCreator.class, "ofNameAndKey", String.class, NamespacedKey.class);
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean canCreateWorldWithKey() {
        return OF_KEY_METHOD.isSuccess();
    }

    @ApiStatus.AvailableSince("5.7")
    public static WorldCreator ofKeyOrName(@NotNull WorldKeyOrName keyOrName) {
        return ofNameAndKey(keyOrName.usableKey(), keyOrName.usableName());
    }

    @ApiStatus.AvailableSince("5.7")
    public static WorldCreator ofNameAndKey(@NotNull NamespacedKey worldKey, String worldName) {
        if (OF_NAME_AND_KEY_METHOD.isSuccess() && canPassIntoNameAndKey(worldKey)) {
            return WorldCreator.ofNameAndKey(worldName, worldKey);
        }
        if  (OF_KEY_METHOD.isSuccess()) {
            return WorldCreator.ofKey(worldKey);
        }
        return WorldCreator.name(worldName);
    }

    private static boolean canPassIntoNameAndKey(@NotNull NamespacedKey worldKey) {
        return !BukkitCompatibility.isUsingNewDimensionStorage()
                || worldKey.getNamespace().equals(NamespacedKey.MINECRAFT);
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean supportsForcedSpawnPosition() {
        return FORCED_SPAWN_POSITION_METHOD.isSuccess();
    }

    @ApiStatus.AvailableSince("5.7")
    public static void setForcedSpawnPosition(WorldCreator worldCreator, EntityPosition position) {
        if (!supportsForcedSpawnPosition()) {
            Logging.fine("Server does not support forced spawn position configuration via WorldCreator API.");
            return;
        }
        ReflectHelper.tryInvokeMethod(
                worldCreator,
                FORCED_SPAWN_POSITION_METHOD.get(),
                io.papermc.paper.math.Position.fine(
                        position.getVector().getX().getRawValue(),
                        position.getVector().getY().getRawValue(),
                        position.getVector().getZ().getRawValue()
                ),
                (float) position.getDirection().getYaw().getRawValue(),
                (float) position.getDirection().getPitch().getRawValue()
        ).onFailure(ex ->
                Logging.warning("Failed to set forced spawn position on WorldCreator: %s", ex.getMessage()));
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean supportsBonusChest() {
        return BONUS_CHEST_METHOD.isSuccess();
    }

    @ApiStatus.AvailableSince("5.7")
    public static void setBonusChest(WorldCreator worldCreator, boolean generateBonusChest) {
        if (!supportsBonusChest()) {
            Logging.fine("Server does not support bonus chest generation via WorldCreator API.");
            return;
        }
        worldCreator.bonusChest(generateBonusChest);
    }

    private WorldCreatorCompatibility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
