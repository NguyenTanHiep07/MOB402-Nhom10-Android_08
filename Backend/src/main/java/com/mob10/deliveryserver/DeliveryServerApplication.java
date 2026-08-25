package com.mob10.deliveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
public class DeliveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryServerApplication.class, args);
    }
}
