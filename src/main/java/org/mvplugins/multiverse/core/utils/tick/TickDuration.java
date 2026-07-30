package org.mvplugins.multiverse.core.utils.tick;

import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

@ApiStatus.AvailableSince("5.7")
public final class TickDuration {

    @ApiStatus.AvailableSince("5.7")
    public static Try<TickDuration> parseString(@NotNull String timeString) {
        return Try.of(() -> {
            if (timeString.endsWith("s")) {
                String subString = timeString.substring(0, timeString.length() - 1);
                return of(Long.parseLong(subString), TimeUnit.SECONDS);
            } else if (timeString.endsWith("d")) {
                String subString = timeString.substring(0, timeString.length() - 1);
                return ofGameDays(Long.parseLong(subString));
            }
            return ofTicks(Long.parseLong(timeString));
        });
    }

    @ApiStatus.AvailableSince("5.7")
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TickDuration ofDuration(@NotNull Duration duration) {
        return new TickDuration(duration);
    }

    @ApiStatus.AvailableSince("5.7")
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TickDuration ofSeconds(@NotNull Duration duration) {
        return new TickDuration(duration);
    }

    @ApiStatus.AvailableSince("5.7")
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TickDuration ofTicks(long ticks) {
        return of(ticks, TickUnit.TICK);
    }

    @ApiStatus.AvailableSince("5.7")
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TickDuration ofGameDays(long gameDays) {
        return of(gameDays, TickUnit.GAME_DAY);
    }

    @ApiStatus.AvailableSince("5.7")
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull TickDuration of(long time, @NotNull TimeUnit unit) {
        return of(time, unit.toChronoUnit());
    }

    @ApiStatus.AvailableSince("5.7")
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull TickDuration of(long time, @NotNull TemporalUnit unit) {
        return new TickDuration(Duration.of(time, unit));
    }

    private static final double TIME_MULTIPLIER = 3.6;
    private static final long DAY_SECONDS = 24 * 60 * 60;
    private static final long START_OFFSET = 6 * 60 * 60;

    private final Duration duration;

    private TickDuration(@NotNull Duration duration) {
        this.duration = duration;
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull LocalTime toLocalTime() {
        long realSeconds = (long) ((toTicks() * TIME_MULTIPLIER) + START_OFFSET) % DAY_SECONDS;
        return LocalTime.ofSecondOfDay(realSeconds);
    }

    @ApiStatus.AvailableSince("5.7")
    public long toTicks() {
        return duration.toMillis() / TickUnit.TICK.inMillis();
    }

    @ApiStatus.AvailableSince("5.7")
    public double toGameDays() {
        return (double) duration.toMillis() / (double) TickUnit.GAME_DAY.inMillis();
    }

    @ApiStatus.AvailableSince("5.7")
    public double toSeconds() {
        return to(TimeUnit.SECONDS);
    }

    @ApiStatus.AvailableSince("5.7")
    public double to(@NotNull TimeUnit unit) {
        return to(unit.toChronoUnit());
    }

    @ApiStatus.AvailableSince("5.7")
    public double to(@NotNull TemporalUnit unit) {
        return (double) duration.toMillis() / (double) unit.getDuration().toMillis();
    }

    @ApiStatus.AvailableSince("5.7")
    public boolean isExactTo(@NotNull TimeUnit unit) {
        return isExactTo(unit.toChronoUnit());
    }

    @ApiStatus.AvailableSince("5.7")
    public boolean isExactTo(@NotNull TemporalUnit unit) {
        return duration.toMillis() % unit.getDuration().toMillis() == 0;
    }

    @ApiStatus.AvailableSince("5.7")
    public Duration getDuration() {
        return duration;
    }
}
