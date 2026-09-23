package com.stevenzhang.studyroom_booking.web;

import com.stevenzhang.studyroom_booking.domain.Booking;
import com.stevenzhang.studyroom_booking.domain.TimeSlot;
import com.stevenzhang.studyroom_booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookings", description = "Create, view and cancel study room bookings")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a booking",
            description = "Checks opening hours, 30-minute alignment, the 2-hour cap, "
                    + "room availability and the user's 3-hour daily limit.")
    @ApiResponse(responseCode = "201", description = "Booking created")
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

    @Operation(summary = "Get a booking by id")
    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return BookingResponse.from(bookingService.getBooking(id));
    }

    @Operation(summary = "Cancel a booking",
            description = "Only confirmed bookings that have not started yet can be cancelled.")
    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return BookingResponse.from(bookingService.cancelBooking(id));
    }
}