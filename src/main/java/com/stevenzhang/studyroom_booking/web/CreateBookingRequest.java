package com.stevenzhang.studyroom_booking.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateBookingRequest(
        @Schema(example = "1") @NotNull Long roomId,
        @Schema(example = "1") @NotNull Long userId,
        @Schema(example = "2030-01-15T10:00:00") @NotNull LocalDateTime startTime,
        @Schema(example = "2030-01-15T11:00:00") @NotNull LocalDateTime endTime) {

    @JsonIgnore
    @AssertTrue(message = "startTime must be before endTime")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }
}