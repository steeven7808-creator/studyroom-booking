package com.stevenzhang.studyroom_booking.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    public static final ZoneId CAMPUS_ZONE = ZoneId.of("America/Vancouver");

    @Bean
    public Clock clock() {
        return Clock.system(CAMPUS_ZONE);
    }
}