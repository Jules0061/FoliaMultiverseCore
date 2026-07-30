package org.mvplugins.multiverse.core.utils.compatibility;

import io.vavr.control.Try;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.utils.ReflectHelper;

import java.lang.reflect.Method;

@ApiStatus.AvailableSince("5.7")
public final class WorldCompatibility {

    private static final Try<Method> SAVE_WITH_FLUSH_METHOD;
    private static final boolean HAS_GET_COORDINATE_SCALE_METHOD;
    private static final boolean HAS_HAS_BONUS_CHEST_METHOD;

    static {
        SAVE_WITH_FLUSH_METHOD = ReflectHelper.tryGetMethod(World.class, "save", boolean.class);
        HAS_GET_COORDINATE_SCALE_METHOD = ReflectHelper.hasMethod(World.class, "getCoordinateScale");
        HAS_HAS_BONUS_CHEST_METHOD = ReflectHelper.hasMethod(World.class, "hasBonusChest");
    }

    @ApiStatus.AvailableSince("5.7")
    public static void saveWithFlush(World world, boolean flush) {
        SAVE_WITH_FLUSH_METHOD
                .flatMap(method -> ReflectHelper.tryInvokeMethod(world, method, flush))
                .orElseRun(ignore -> world.save());
    }

    @ApiStatus.AvailableSince("5.7")
    public static double getCoordinateScale(World world) {
        if (HAS_GET_COORDINATE_SCALE_METHOD) {
            return world.getCoordinateScale();
        }
        return switch (world.getEnvironment()) {
            case NORMAL -> 1;
            case NETHER -> 8;
            default -> 1;
        };
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean hasBonusChest(World world) {
        if (HAS_HAS_BONUS_CHEST_METHOD) {
            return world.hasBonusChest();
        }
        return false;
    }

    @ApiStatus.AvailableSince("5.8")
    @SuppressWarnings("removal")
    public static boolean getKeepSpawnInMemory(World world) {
        return world.getKeepSpawnInMemory();
    }

    @ApiStatus.AvailableSince("5.8")
    @SuppressWarnings("removal")
    public static void setKeepSpawnInMemory(World world, boolean keepLoaded) {
        world.setKeepSpawnInMemory(keepLoaded);
    }

    private WorldCompatibility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
