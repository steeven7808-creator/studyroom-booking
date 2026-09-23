package com.stevenzhang.studyroom_booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.stevenzhang.studyroom_booking.TestcontainersConfiguration;
import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.Room;
import com.stevenzhang.studyroom_booking.domain.TimeSlot;
import com.stevenzhang.studyroom_booking.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class BookingRepositoryIntegrationTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);
    private static final LocalDateTime NOW = DAY.minusDays(1).atTime(12, 0);

    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private UserRepository userRepository;

    private Room room;
    private Room otherRoom;
    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        room = roomRepository.save(new Room("IKB 101", "Irving K. Barber Learning Centre", 6,
                LocalTime.of(8, 0), LocalTime.of(22, 0)));
        otherRoom = roomRepository.save(new Room("IKB 102", "Irving K. Barber Learning Centre", 4,
                LocalTime.of(8, 0), LocalTime.of(22, 0)));
        user = userRepository.save(new User("Steven", "steven@example.com"));
        otherUser = userRepository.save(new User("Alex", "alex@example.com"));
    }

    private static TimeSlot slot(LocalDate day, String start, String end) {
        return new TimeSlot(
                LocalDateTime.of(day, LocalTime.parse(start)),
                LocalDateTime.of(day, LocalTime.parse(end)));
    }

    private static TimeSlot slot(String start, String end) {
        return slot(DAY, start, end);
    }

    private Booking saveBooking(Room room, User user, TimeSlot slot) {
        return bookingRepository.save(Booking.create(room, user, slot, NOW));
    }

    private long countOverlapping(Room room, TimeSlot slot) {
        return bookingRepository.countOverlapping(room.getId(), slot.start(), slot.end());
    }

    // --- countOverlapping ---

    @Test
    void countsPartiallyOverlappingBooking() {
        saveBooking(room, user, slot("10:00", "11:00"));
        assertEquals(1, countOverlapping(room, slot("10:30", "11:30")));
    }

    @Test
    void doesNotCountAdjacentBooking() {
        saveBooking(room, user, slot("10:00", "11:00"));
        assertEquals(0, countOverlapping(room, slot("11:00", "12:00")));
    }

    @Test
    void doesNotCountCancelledBooking() {
        Booking booking = saveBooking(room, user, slot("10:00", "11:00"));
        booking.cancel(NOW);
        assertEquals(0, countOverlapping(room, slot("10:00", "11:00")));
    }

    @Test
    void doesNotCountBookingInAnotherRoom() {
        saveBooking(otherRoom, user, slot("10:00", "11:00"));
        assertEquals(0, countOverlapping(room, slot("10:00", "11:00")));
    }

    // --- findNonCancelledByUserStartingBetween ---

    @Test
    void findsOnlyTheUsersNonCancelledBookingsOnThatDay() {
        Booking morning = saveBooking(room, user, slot("09:00", "10:00"));
        Booking afternoon = saveBooking(otherRoom, user, slot("14:00", "15:00"));
        saveBooking(room, user, slot(DAY.plusDays(1), "09:00", "10:00")); // next day
        saveBooking(room, otherUser, slot("11:00", "12:00"));              // other user
        Booking cancelled = saveBooking(room, user, slot("16:00", "17:00"));
        cancelled.cancel(NOW);

        List<Booking> result = bookingRepository.findNonCancelledByUserStartingBetween(
                user.getId(), DAY.atStartOfDay(), DAY.plusDays(1).atStartOfDay());

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(morning, afternoon)));
    }
}