package com.stevenzhang.studyroom_booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class TimeSlotTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    // Builds a slot on a fixed day from "HH:mm" strings to keep tests readable
    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(
                LocalDateTime.of(DAY, LocalTime.parse(start)),
                LocalDateTime.of(DAY, LocalTime.parse(end)));
    }

    @Test
    void rejectsStartAfterEnd() {
        assertThrows(IllegalArgumentException.class, () -> slot("11:00", "10:00"));
    }

    @Test
    void rejectsZeroLengthSlot() {
        assertThrows(IllegalArgumentException.class, () -> slot("10:00", "10:00"));
    }

    @Test
    void calculatesDuration() {
        assertEquals(Duration.ofMinutes(90), slot("10:00", "11:30").duration());
    }

    @Test
    void partiallyOverlappingSlotsOverlap() {
        assertTrue(slot("10:00", "11:00").overlaps(slot("10:30", "11:30")));
    }

    @Test
    void containedSlotOverlaps() {
        assertTrue(slot("10:00", "12:00").overlaps(slot("10:30", "11:00")));
    }

    @Test
    void adjacentSlotsDoNotOverlap() {
        assertFalse(slot("10:00", "11:00").overlaps(slot("11:00", "12:00")));
    }

    @Test
    void overlapIsSymmetric() {
        TimeSlot a = slot("10:00", "11:00");
        TimeSlot b = slot("10:30", "11:30");
        assertEquals(a.overlaps(b), b.overlaps(a));
    }

    @Test
    void detectsAlignmentToGranularity() {
        Duration halfHour = Duration.ofMinutes(30);
        assertTrue(slot("10:00", "11:30").isAlignedTo(halfHour));
        assertFalse(slot("10:15", "11:00").isAlignedTo(halfHour));
    }
}