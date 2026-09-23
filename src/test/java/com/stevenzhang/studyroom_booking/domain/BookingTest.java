package com.stevenzhang.studyroom_booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.stevenzhang.studyroom_booking.exception.BookingRuleViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);
    private static final LocalDateTime DAY_BEFORE = DAY.minusDays(1).atTime(12, 0);

    private static final Room ROOM = new Room("IKB 101", "Irving K. Barber Learning Centre", 6,
            LocalTime.of(8, 0), LocalTime.of(22, 0));
    private static final User USER = new User("Steven", "steven@example.com");

    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(at(start), at(end));
    }

    private static LocalDateTime at(String time) {
        return LocalDateTime.of(DAY, LocalTime.parse(time));
    }

    private static Booking validBooking() {
        return Booking.create(ROOM, USER, slot("10:00", "11:00"), DAY_BEFORE);
    }

    // --- Creation rules ---

    @Test
    void newBookingIsConfirmed() {
        assertEquals(BookingStatus.CONFIRMED, validBooking().getStatus());
    }

    @Test
    void rejectsSlotThatHasAlreadyStarted() {
        assertThrows(BookingRuleViolationException.class,
                () -> Booking.create(ROOM, USER, slot("10:00", "11:00"), at("10:00")));
    }

    @Test
    void rejectsMisalignedSlot() {
        assertThrows(BookingRuleViolationException.class,
                () -> Booking.create(ROOM, USER, slot("10:15", "11:00"), DAY_BEFORE));
    }

    @Test
    void allowsExactlyMaxDuration() {
        assertDoesNotThrow(() -> Booking.create(ROOM, USER, slot("10:00", "12:00"), DAY_BEFORE));
    }

    @Test
    void rejectsSlotLongerThanMaxDuration() {
        assertThrows(BookingRuleViolationException.class,
                () -> Booking.create(ROOM, USER, slot("10:00", "12:30"), DAY_BEFORE));
    }

    @Test
    void rejectsSlotOutsideOpeningHours() {
        assertThrows(BookingRuleViolationException.class,
                () -> Booking.create(ROOM, USER, slot("21:30", "22:30"), DAY_BEFORE));
    }

    // --- Status transitions ---

    @Test
    void canCancelBeforeStart() {
        Booking booking = validBooking();
        booking.cancel(at("09:59"));
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void cannotCancelAfterStart() {
        Booking booking = validBooking();
        assertThrows(BookingRuleViolationException.class, () -> booking.cancel(at("10:30")));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void cannotCancelTwice() {
        Booking booking = validBooking();
        booking.cancel(at("09:00"));
        assertThrows(BookingRuleViolationException.class, () -> booking.cancel(at("09:00")));
    }

    @Test
    void canCompleteAfterEnd() {
        Booking booking = validBooking();
        booking.complete(at("11:00"));
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    void cannotCompleteBeforeEnd() {
        Booking booking = validBooking();
        assertThrows(BookingRuleViolationException.class, () -> booking.complete(at("10:30")));
    }

    @Test
    void cannotCancelCompletedBooking() {
        Booking booking = validBooking();
        booking.complete(at("11:00"));
        assertThrows(BookingRuleViolationException.class, () -> booking.cancel(at("11:30")));
    }
}