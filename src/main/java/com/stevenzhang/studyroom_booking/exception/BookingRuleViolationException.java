package com.stevenzhang.studyroom_booking.exception;

public class BookingRuleViolationException extends RuntimeException {

    public BookingRuleViolationException(String message) {
        super(message);
    }
}