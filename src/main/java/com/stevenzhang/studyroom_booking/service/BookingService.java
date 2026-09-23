package com.stevenzhang.studyroom_booking.service;

import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.Room;
import com.stevenzhang.studyroom_booking.domain.TimeSlot;
import com.stevenzhang.studyroom_booking.domain.User;
import com.stevenzhang.studyroom_booking.exception.BookingRuleViolationException;
import com.stevenzhang.studyroom_booking.exception.ResourceNotFoundException;
import com.stevenzhang.studyroom_booking.repository.BookingRepository;
import com.stevenzhang.studyroom_booking.repository.RoomRepository;
import com.stevenzhang.studyroom_booking.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    public static final Duration DAILY_LIMIT = Duration.ofHours(3);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public Booking createBooking(Long roomId, Long userId, TimeSlot slot) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomId + " not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));

        // Rule 1 and "no past bookings": the domain object checks these itself, no DB needed.
        Booking booking = Booking.create(room, user, slot, LocalDateTime.now(clock));

        // Rule 2: no overlapping non-cancelled booking in the same room.
        if (bookingRepository.countOverlapping(roomId, slot.start(), slot.end()) > 0) {
            throw new BookingRuleViolationException("The room is already booked for this time");
        }

        // Rule 3: a user's non-cancelled bookings on one day must not exceed DAILY_LIMIT.
        LocalDateTime dayStart = slot.start().toLocalDate().atStartOfDay();
        Duration alreadyBooked = bookingRepository
                .findNonCancelledByUserStartingBetween(userId, dayStart, dayStart.plusDays(1))
                .stream()
                .map(existing -> existing.getSlot().duration())
                .reduce(Duration.ZERO, Duration::plus);
        if (alreadyBooked.plus(slot.duration()).compareTo(DAILY_LIMIT) > 0) {
            throw new BookingRuleViolationException("Daily booking limit of 3 hours would be exceeded");
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = findBooking(bookingId);
        booking.cancel(LocalDateTime.now(clock));
        return booking;
    }

    @Transactional(readOnly = true)
    public Booking getBooking(Long bookingId) {
        return findBooking(bookingId);
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking " + bookingId + " not found"));
    }
}