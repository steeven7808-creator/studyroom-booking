package com.stevenzhang.studyroom_booking.web;

import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.BookingStatus;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long roomId,
        Long userId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getUser().getId(),
                booking.getSlot().start(),
                booking.getSlot().end(),
                booking.getStatus());
    }
}