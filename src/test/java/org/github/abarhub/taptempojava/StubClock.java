package org.github.abarhub.taptempojava;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class StubClock extends Clock {

    private Instant currentInstant;
    private final Duration duration;
    private final ZoneId zoneId;

    public StubClock(Instant currentInstant, Duration duration, ZoneId zoneId) {
        this.currentInstant = currentInstant;
        this.duration = duration;
        this.zoneId = zoneId;
    }

    @Override
    public ZoneId getZone() {
        return this.zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new StubClock(currentInstant, duration, zone);
    }

    @Override
    public Instant instant() {
        Instant result = currentInstant;
        currentInstant = currentInstant.plus(duration);
        return result;
    }
}
