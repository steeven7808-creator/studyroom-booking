package com.stevenzhang.studyroom_booking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String building;
    private int capacity;
    private LocalTime openTime;
    private LocalTime closeTime;

    /** Required by JPA. Not for application code. */
    protected Room() {
    }

    public Room(String name, String building, int capacity,
                LocalTime openTime, LocalTime closeTime) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (building == null || building.isBlank()) {
            throw new IllegalArgumentException("building must not be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        Objects.requireNonNull(openTime, "openTime must not be null");
        Objects.requireNonNull(closeTime, "closeTime must not be null");
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("openTime must be before closeTime");
        }
        this.name = name;
        this.building = building;
        this.capacity = capacity;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public boolean isOpenDuring(TimeSlot slot) {
        boolean sameDay = slot.start().toLocalDate().equals(slot.end().toLocalDate());
        return sameDay
                && !slot.start().toLocalTime().isBefore(openTime)
                && !slot.end().toLocalTime().isAfter(closeTime);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBuilding() { return building; }
    public int getCapacity() { return capacity; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
}