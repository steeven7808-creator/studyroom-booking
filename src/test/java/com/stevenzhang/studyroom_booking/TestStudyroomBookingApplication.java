package com.stevenzhang.studyroom_booking;

import org.springframework.boot.SpringApplication;

public class TestStudyroomBookingApplication {

	public static void main(String[] args) {
		SpringApplication.from(StudyroomBookingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
