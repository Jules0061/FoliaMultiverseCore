package org.mvplugins.multiverse.core.utils.tick;

import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

@ApiStatus.AvailableSince("5.7")
public final class TickUnit implements TemporalUnit {

    private static final long SINGLE_TICK_DURATION_MS = 50L;
    private static final long TICKS_PER_GAME_DAY = 24_000L;

    @ApiStatus.AvailableSince("5.7")
    public static final TickUnit TICK = new TickUnit(SINGLE_TICK_DURATION_MS);

    @ApiStatus.AvailableSince("5.7")
    public static final TickUnit GAME_DAY = new TickUnit(SINGLE_TICK_DURATION_MS * TICKS_PER_GAME_DAY);

    private final long milliseconds;
    private final Duration duration;

    private TickUnit(long milliseconds) {
        this.milliseconds = milliseconds;
        this.duration = Duration.ofMillis(milliseconds);
    }

    @ApiStatus.AvailableSince("5.7")
    public long inMillis() {
        return milliseconds;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean isDurationEstimated() {
        return false;
    }

    @Override
    public boolean isDateBased() {
        return false;
    }

    @Override
    public boolean isTimeBased() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R addTo(R temporal, long amount) {
        return (R) temporal.plus(duration.multipliedBy(amount));
    }

    @Override
    public long between(final Temporal start, final Temporal end) {
        return start.until(end, ChronoUnit.MILLIS) / milliseconds;
    }
}
