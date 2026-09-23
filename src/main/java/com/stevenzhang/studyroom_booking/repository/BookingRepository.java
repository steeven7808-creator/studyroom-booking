package com.stevenzhang.studyroom_booking.repository;

import com.stevenzhang.studyroom_booking.domain.Booking;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Counts non-cancelled bookings in the room that overlap [slotStart, slotEnd).
     * Mirrors the half-open interval logic in TimeSlot.overlaps.
     */
    @Query("""
            select count(b) from Booking b
            where b.room.id = :roomId
              and b.status <> com.stevenzhang.studyroom_booking.domain.BookingStatus.CANCELLED
              and b.slot.start < :slotEnd
              and :slotStart < b.slot.end
            """)
    long countOverlapping(@Param("roomId") Long roomId,
                          @Param("slotStart") LocalDateTime slotStart,
                          @Param("slotEnd") LocalDateTime slotEnd);

    /**
     * Non-cancelled bookings of a user that start within [dayStart, dayEnd).
     */
    @Query("""
            select b from Booking b
            where b.user.id = :userId
              and b.status <> com.stevenzhang.studyroom_booking.domain.BookingStatus.CANCELLED
              and b.slot.start >= :dayStart
              and b.slot.start < :dayEnd
            """)
    List<Booking> findNonCancelledByUserStartingBetween(@Param("userId") Long userId,
                                                        @Param("dayStart") LocalDateTime dayStart,
                                                        @Param("dayEnd") LocalDateTime dayEnd);
}