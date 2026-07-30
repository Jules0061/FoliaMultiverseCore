package org.mvplugins.multiverse.core.utils;

import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.utils.tick.TickDuration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ApiStatus.AvailableSince("5.1")
public final class MinecraftTimeFormatter {

    @ApiStatus.AvailableSince("5.1")
    public static String format12h(long time) {
        return formatTime(time, "hh:mm a");
    }

    @ApiStatus.AvailableSince("5.1")
    public static String format24h(long time) {
        return formatTime(time, "HH:mm");
    }

    @ApiStatus.AvailableSince("5.1")
    public static String formatTime(long ticks, String format) {
        LocalTime localTime = TickDuration.ofTicks(ticks).toLocalTime();
        return Try.of(() -> DateTimeFormatter.ofPattern(format))
                .map(localTime::format)
                .getOrElse("invalid time format: " + format);
    }

    private MinecraftTimeFormatter() {
        throw new UnsupportedOperationException();
    }
}
