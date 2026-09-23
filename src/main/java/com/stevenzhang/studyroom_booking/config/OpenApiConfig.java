package com.stevenzhang.studyroom_booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studyRoomBookingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Study Room Booking API")
                .version("v1")
                .description("Book UBC study rooms with overlap-safe scheduling rules."));
    }
}