package com.stevenzhang.studyroom_booking.web;

import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.BookingStatus;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public record BookingResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long roomId,
        @Schema(example = "1") Long userId,
        @Schema(example = "2030-01-15T10:00:00", description = "Campus local time (America/Vancouver)")
        LocalDateTime startTime,
        @Schema(example = "2030-01-15T11:00:00", description = "Campus local time (America/Vancouver)")
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