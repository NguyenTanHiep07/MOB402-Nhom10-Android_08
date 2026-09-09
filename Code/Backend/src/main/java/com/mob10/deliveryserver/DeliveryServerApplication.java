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
        // Đảm bảo ImageIO hoạt động đúng trong môi trường server không có màn hình (Linux headless)
        System.setProperty("java.awt.headless", "true");
        SpringApplication.run(DeliveryServerApplication.class, args);
    }
}
