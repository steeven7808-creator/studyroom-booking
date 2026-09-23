package com.stevenzhang.studyroom_booking.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.Embeddable;

/**
 * An immutable half-open time interval [start, end).
 */
@Embeddable
public record TimeSlot(LocalDateTime start, LocalDateTime end) {

    public TimeSlot {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "start must be before end, but got start=" + start + ", end=" + end);
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public boolean isAlignedTo(Duration granularity) {
        return isAligned(start, granularity) && isAligned(end, granularity);
    }

    private static boolean isAligned(LocalDateTime time, Duration granularity) {
        long secondOfDay = time.toLocalTime().toSecondOfDay();
        return time.getNano() == 0 && secondOfDay % granularity.toSeconds() == 0;
    }
}