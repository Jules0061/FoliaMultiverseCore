package org.mvplugins.multiverse.core.utils.compatibility;

import org.bukkit.WorldBorder;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.core.utils.tick.TickDuration;

import java.util.concurrent.TimeUnit;

@ApiStatus.AvailableSince("5.7")
@SuppressWarnings("removal")
public final class WorldBorderCompatibility {

    private static final boolean HAS_GET_WARNING_TIME_TICKS_METHOD;
    private static final boolean HAS_SET_WARNING_TIME_TICKS_METHOD;
    private static final boolean HAS_SET_WARNING_TIME_METHOD;
    private static final boolean HAS_GET_WARNING_TIME_METHOD;

    private static final boolean HAS_CHANGE_SIZE_METHOD;
    private static final boolean HAS_SET_SIZE_METHOD;

    static {
        HAS_GET_WARNING_TIME_TICKS_METHOD = ReflectHelper.hasMethod(WorldBorder.class, "getWarningTimeTicks");
        HAS_SET_WARNING_TIME_TICKS_METHOD = ReflectHelper.hasMethod(WorldBorder.class, "setWarningTimeTicks", int.class);
        HAS_GET_WARNING_TIME_METHOD = ReflectHelper.hasMethod(WorldBorder.class, "getWarningTime");
        HAS_SET_WARNING_TIME_METHOD = ReflectHelper.hasMethod(WorldBorder.class, "setWarningTime", int.class);

        HAS_CHANGE_SIZE_METHOD = ReflectHelper.hasMethod(WorldBorder.class, "changeSize", double.class, long.class);
        HAS_SET_SIZE_METHOD = ReflectHelper.hasMethod(WorldBorder.class, "setSize", double.class, long.class);
    }

    @ApiStatus.AvailableSince("5.7")
    public static int getWarningTimeTicks(WorldBorder worldBorder) {
        if (HAS_GET_WARNING_TIME_TICKS_METHOD) {
            return worldBorder.getWarningTimeTicks();
        } else if (HAS_GET_WARNING_TIME_METHOD) {
            return worldBorder.getWarningTime() * 20;
        }
        throw new IllegalStateException("Neither getWarningTimeTicks nor getWarningTime method is available in WorldBorder class.");
    }

    @ApiStatus.AvailableSince("5.7")
    public static double getWarningTime(WorldBorder worldBorder) {
        if (HAS_GET_WARNING_TIME_TICKS_METHOD) {
            return (double) worldBorder.getWarningTimeTicks() / 20.0;
        } else if (HAS_GET_WARNING_TIME_METHOD) {
            return worldBorder.getWarningTime();
        }
        throw new IllegalStateException("Neither getWarningTimeTicks nor getWarningTime method is available in WorldBorder class.");
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean supportsSetWarningTimeInTicks() {
        return HAS_SET_WARNING_TIME_TICKS_METHOD;
    }

    public static void setWarningTimeTicks(WorldBorder worldBorder, int ticks) {
        setWarningTimeDuration(worldBorder, TickDuration.ofTicks(ticks));
    }

    @ApiStatus.AvailableSince("5.7")
    public static void setWarningTimeDuration(WorldBorder worldBorder, TickDuration duration) {
        if (HAS_SET_WARNING_TIME_TICKS_METHOD) {
            worldBorder.setWarningTimeTicks((int) duration.toTicks());
        } else if (HAS_SET_WARNING_TIME_METHOD) {
            worldBorder.setWarningTime((int) Math.round(duration.to(TimeUnit.SECONDS)));
        } else {
            throw new IllegalStateException("Neither setWarningTimeTicks nor setWarningTime method is available in WorldBorder class.");
        }
    }

    @ApiStatus.AvailableSince("5.7")
    public static boolean supportsChangeSizeInTicks() {
        return HAS_CHANGE_SIZE_METHOD;
    }

    @ApiStatus.AvailableSince("5.7")
    public static void changeSizeDuration(WorldBorder worldBorder, double newSize, TickDuration duration) {
        if (HAS_CHANGE_SIZE_METHOD) {
            worldBorder.changeSize(newSize, duration.toTicks());
        } else if (HAS_SET_SIZE_METHOD) {
            worldBorder.setSize(newSize, Math.round(duration.to(TimeUnit.SECONDS)));
        } else {
            throw new IllegalStateException("Neither changeSize nor setSize method is available in WorldBorder class.");
        }
    }

    private WorldBorderCompatibility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
