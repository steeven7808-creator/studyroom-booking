package com.stevenzhang.studyroom_booking.repository;

import com.stevenzhang.studyroom_booking.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}