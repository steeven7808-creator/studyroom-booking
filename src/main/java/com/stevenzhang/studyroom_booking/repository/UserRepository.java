package com.stevenzhang.studyroom_booking.repository;

import com.stevenzhang.studyroom_booking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}