package com.stevenzhang.studyroom_booking.domain;

import com.stevenzhang.studyroom_booking.exception.BookingRuleViolationException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bookings")
public class Booking {

    public static final Duration GRANULARITY = Duration.ofMinutes(30);
    public static final Duration MAX_DURATION = Duration.ofHours(2);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    @AttributeOverride(name = "start", column = @Column(name = "start_time"))
    @AttributeOverride(name = "end", column = @Column(name = "end_time"))
    private TimeSlot slot;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    /** Required by JPA. Not for application code. */
    protected Booking() {
    }

    private Booking(Room room, User user, TimeSlot slot) {
        this.room = room;
        this.user = user;
        this.slot = slot;
        this.status = BookingStatus.CONFIRMED;
    }

    public static Booking create(Room room, User user, TimeSlot slot, LocalDateTime now) {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(slot, "slot must not be null");
        Objects.requireNonNull(now, "now must not be null");

        if (!slot.start().isAfter(now)) {
            throw new BookingRuleViolationException("Cannot book a slot that has already started");
        }
        if (!slot.isAlignedTo(GRANULARITY)) {
            throw new BookingRuleViolationException("Booking times must align to 30-minute boundaries");
        }
        if (slot.duration().compareTo(MAX_DURATION) > 0) {
            throw new BookingRuleViolationException("A single booking cannot exceed 2 hours");
        }
        if (!room.isOpenDuring(slot)) {
            throw new BookingRuleViolationException("Booking must be within the room's opening hours");
        }
        return new Booking(room, user, slot);
    }

    public void cancel(LocalDateTime now) {
        requireStatus(BookingStatus.CONFIRMED, "cancel");
        if (!now.isBefore(slot.start())) {
            throw new BookingRuleViolationException("Cannot cancel a booking that has already started");
        }
        status = BookingStatus.CANCELLED;
    }

    public void complete(LocalDateTime now) {
        requireStatus(BookingStatus.CONFIRMED, "complete");
        if (now.isBefore(slot.end())) {
            throw new BookingRuleViolationException("Cannot complete a booking before it ends");
        }
        status = BookingStatus.COMPLETED;
    }

    private void requireStatus(BookingStatus expected, String action) {
        if (status != expected) {
            throw new BookingRuleViolationException(
                    "Cannot " + action + " a booking with status " + status);
        }
    }

    public Long getId() { return id; }
    public Room getRoom() { return room; }
    public User getUser() { return user; }
    public TimeSlot getSlot() { return slot; }
    public BookingStatus getStatus() { return status; }
}