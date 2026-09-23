package com.stevenzhang.studyroom_booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class RoomTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    private static Room room() {
        return new Room("IKB 101", "Irving K. Barber Learning Centre", 6,
                LocalTime.of(8, 0), LocalTime.of(22, 0));
    }

    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(
                LocalDateTime.of(DAY, LocalTime.parse(start)),
                LocalDateTime.of(DAY, LocalTime.parse(end)));
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () ->
                new Room("IKB 101", "IKB", 0, LocalTime.of(8, 0), LocalTime.of(22, 0)));
    }

    @Test
    void rejectsOpenTimeNotBeforeCloseTime() {
        assertThrows(IllegalArgumentException.class, () ->
                new Room("IKB 101", "IKB", 6, LocalTime.of(22, 0), LocalTime.of(8, 0)));
    }

    @Test
    void allowsSlotWithinOpeningHours() {
        assertTrue(room().isOpenDuring(slot("10:00", "11:00")));
    }

    @Test
    void allowsSlotTouchingOpeningAndClosingTimes() {
        assertTrue(room().isOpenDuring(slot("08:00", "09:00")));
        assertTrue(room().isOpenDuring(slot("21:00", "22:00")));
    }

    @Test
    void rejectsSlotStartingBeforeOpening() {
        assertFalse(room().isOpenDuring(slot("07:30", "08:30")));
    }

    @Test
    void rejectsSlotEndingAfterClosing() {
        assertFalse(room().isOpenDuring(slot("21:30", "22:30")));
    }

    @Test
    void rejectsSlotSpanningMidnight() {
        TimeSlot overnight = new TimeSlot(
                LocalDateTime.of(DAY, LocalTime.of(21, 0)),
                LocalDateTime.of(DAY.plusDays(1), LocalTime.of(1, 0)));
        assertFalse(room().isOpenDuring(overnight));
    }
}