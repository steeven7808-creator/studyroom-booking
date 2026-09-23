package com.stevenzhang.studyroom_booking.web;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull Long roomId,
        @NotNull Long userId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime) {

    @AssertTrue(message = "startTime must be before endTime")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }
}