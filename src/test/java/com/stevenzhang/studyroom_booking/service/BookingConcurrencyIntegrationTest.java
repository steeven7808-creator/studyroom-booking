package com.stevenzhang.studyroom_booking.service;

import static org.junit.jupiter.api.Assertions.*;

import com.stevenzhang.studyroom_booking.TestcontainersConfiguration;
import com.stevenzhang.studyroom_booking.config.ClockConfig;
import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.Room;
import com.stevenzhang.studyroom_booking.domain.TimeSlot;
import com.stevenzhang.studyroom_booking.domain.User;
import com.stevenzhang.studyroom_booking.exception.BookingRuleViolationException;
import com.stevenzhang.studyroom_booking.repository.BookingRepository;
import com.stevenzhang.studyroom_booking.repository.RoomRepository;
import com.stevenzhang.studyroom_booking.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookingConcurrencyIntegrationTest {

    private static final LocalDate DAY = LocalDate.now(ClockConfig.CAMPUS_ZONE).plusDays(7);

    @Autowired private BookingService bookingService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();
    }

    private static TimeSlot slot(String start, String end) {
        return new TimeSlot(
                LocalDateTime.of(DAY, LocalTime.parse(start)),
                LocalDateTime.of(DAY, LocalTime.parse(end)));
    }

    private Room saveRoom(String name) {
        return roomRepository.save(new Room(name, "Irving K. Barber Learning Centre", 6,
                LocalTime.of(8, 0), LocalTime.of(22, 0)));
    }

    private Callable<Boolean> bookingAttempt(Long roomId, Long userId, TimeSlot slot) {
        return () -> {
            try {
                bookingService.createBooking(roomId, userId, slot);
                return true;
            } catch (BookingRuleViolationException e) {
                return false;
            }
        };
    }

    /** Starts all tasks at the same moment and returns how many of them succeeded. */
    private int countSuccesses(List<Callable<Boolean>> tasks) throws Exception {
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (Callable<Boolean> task : tasks) {
                results.add(executor.submit(() -> {
                    startSignal.await(); // every thread waits here, then all start at once
                    return task.call();
                }));
            }
            startSignal.countDown();

            int successes = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
            return successes;
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void databaseRejectsOverlapEvenWithoutTheServiceCheck() {
        Room room = saveRoom("IKB 101");
        User alice = userRepository.save(new User("Alice", "alice@example.com"));
        User bob = userRepository.save(new User("Bob", "bob@example.com"));
        LocalDateTime now = DAY.minusDays(1).atTime(12, 0);

        bookingRepository.saveAndFlush(Booking.create(room, alice, slot("10:00", "11:00"), now));

        assertThrows(DataIntegrityViolationException.class, () -> bookingRepository.saveAndFlush(
                Booking.create(room, bob, slot("10:30", "11:30"), now)));
    }

    @Test
    void onlyOneOfManyConcurrentRequestsForTheSameSlotSucceeds() throws Exception {
        Room room = saveRoom("IKB 101");
        TimeSlot slot = slot("10:00", "11:00");
        List<Callable<Boolean>> attempts = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            User user = userRepository.save(new User("User " + i, "user" + i + "@example.com"));
            attempts.add(bookingAttempt(room.getId(), user.getId(), slot));
        }

        assertEquals(1, countSuccesses(attempts));
        assertEquals(1, bookingRepository.count());
    }

    @Test
    void concurrentRequestsFromOneUserCannotExceedTheDailyLimit() throws Exception {
        User user = userRepository.save(new User("Steven", "steven@example.com"));
        TimeSlot oneHour = slot("10:00", "11:00");
        List<Callable<Boolean>> attempts = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Room room = saveRoom("Room " + i);
            attempts.add(bookingAttempt(room.getId(), user.getId(), oneHour));
        }

        // Six one-hour requests in different rooms; the 3-hour daily limit allows exactly three.
        assertEquals(3, countSuccesses(attempts));
        assertEquals(3, bookingRepository.count());
    }
}