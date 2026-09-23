package com.stevenzhang.studyroom_booking.web;

import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.TimeSlot;
import com.stevenzhang.studyroom_booking.service.BookingService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(
                request.roomId(),
                request.userId(),
                new TimeSlot(request.startTime(), request.endTime()));
        return ResponseEntity
                .created(URI.create("/api/bookings/" + booking.getId()))
                .body(BookingResponse.from(booking));
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return BookingResponse.from(bookingService.getBooking(id));
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return BookingResponse.from(bookingService.cancelBooking(id));
    }
}