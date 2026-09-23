package com.stevenzhang.studyroom_booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stevenzhang.studyroom_booking.config.ClockConfig;
import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.BookingStatus;
import com.stevenzhang.studyroom_booking.domain.Room;
import com.stevenzhang.studyroom_booking.domain.TimeSlot;
import com.stevenzhang.studyroom_booking.domain.User;
import com.stevenzhang.studyroom_booking.exception.BookingRuleViolationException;
import com.stevenzhang.studyroom_booking.exception.ResourceNotFoundException;
import com.stevenzhang.studyroom_booking.repository.BookingRepository;
import com.stevenzhang.studyroom_booking.repository.RoomRepository;
import com.stevenzhang.studyroom_booking.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final Long ROOM_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);
    private static final LocalDateTime NOW = DAY.minusDays(1).atTime(12, 0);

    private static final Room ROOM = new Room("IKB 101", "Irving K. Barber Learning Centre", 6,
            LocalTime.of(8, 0), LocalTime.of(22, 0));
    private static final User USER = new User("Steven", "steven@example.com");

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private UserRepository userRepository;

    private BookingService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                NOW.atZone(ClockConfig.CAMPUS_ZONE).toInstant(), ClockConfig.CAMPUS_ZONE);
        service = new BookingService(bookingRepository, roomRepository, userRepository, fixedClock);
    }

    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(
                LocalDateTime.of(DAY, LocalTime.parse(start)),
                LocalDateTime.of(DAY, LocalTime.parse(end)));
    }

    private void givenRoomAndUserExist() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(ROOM));
        when(userRepository.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(USER));
    }

    private void givenNoOverlap(TimeSlot slot) {
        when(bookingRepository.countOverlapping(ROOM_ID, slot.start(), slot.end())).thenReturn(0L);
    }

    private void givenUserAlreadyBookedThatDay(TimeSlot... existingSlots) {
        List<Booking> existing = Arrays.stream(existingSlots)
                .map(s -> Booking.create(ROOM, USER, s, NOW))
                .toList();
        when(bookingRepository.findNonCancelledByUserStartingBetween(
                USER_ID, DAY.atStartOfDay(), DAY.plusDays(1).atStartOfDay()))
                .thenReturn(existing);
    }

    private void givenSaveReturnsItsArgument() {
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsBookingWhenAllRulesPass() {
        TimeSlot slot = slot("10:00", "11:00");
        givenRoomAndUserExist();
        givenNoOverlap(slot);
        givenUserAlreadyBookedThatDay();
        givenSaveReturnsItsArgument();

        Booking booking = service.createBooking(ROOM_ID, USER_ID, slot);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(slot, booking.getSlot());
        verify(bookingRepository).saveAndFlush(booking);
    }

    @Test
    void rejectsWhenRoomIsAlreadyBooked() {
        TimeSlot slot = slot("10:00", "11:00");
        givenRoomAndUserExist();
        when(bookingRepository.countOverlapping(ROOM_ID, slot.start(), slot.end())).thenReturn(1L);

        assertThrows(BookingRuleViolationException.class,
                () -> service.createBooking(ROOM_ID, USER_ID, slot));
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void allowsBookingUpToExactlyTheDailyLimit() {
        TimeSlot slot = slot("14:00", "15:00");
        givenRoomAndUserExist();
        givenNoOverlap(slot);
        givenUserAlreadyBookedThatDay(slot("09:00", "11:00")); // 2h + 1h = 3h
        givenSaveReturnsItsArgument();

        assertDoesNotThrow(() -> service.createBooking(ROOM_ID, USER_ID, slot));
    }

    @Test
    void rejectsWhenDailyLimitWouldBeExceeded() {
        TimeSlot slot = slot("14:00", "15:30");
        givenRoomAndUserExist();
        givenNoOverlap(slot);
        givenUserAlreadyBookedThatDay(slot("09:00", "11:00")); // 2h + 1.5h = 3.5h

        assertThrows(BookingRuleViolationException.class,
                () -> service.createBooking(ROOM_ID, USER_ID, slot));
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void sumsAllExistingBookingsOfTheDay() {
        TimeSlot slot = slot("16:00", "17:00");
        givenRoomAndUserExist();
        givenNoOverlap(slot);
        givenUserAlreadyBookedThatDay(slot("09:00", "10:00"), slot("12:00", "13:30")); // 1h + 1.5h + 1h = 3.5h

        assertThrows(BookingRuleViolationException.class,
                () -> service.createBooking(ROOM_ID, USER_ID, slot));
    }

    @Test
    void rejectsUnknownRoom() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.createBooking(ROOM_ID, USER_ID, slot("10:00", "11:00")));
    }

    @Test
    void doesNotQueryDatabaseForInvalidSlot() {
        givenRoomAndUserExist();

        assertThrows(BookingRuleViolationException.class,
                () -> service.createBooking(ROOM_ID, USER_ID, slot("10:15", "11:00")));
        verify(bookingRepository, never()).countOverlapping(any(), any(), any());
    }

    @Test
    void cancelsExistingBooking() {
        Booking booking = Booking.create(ROOM, USER, slot("10:00", "11:00"), NOW);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        service.cancelBooking(5L);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }
}