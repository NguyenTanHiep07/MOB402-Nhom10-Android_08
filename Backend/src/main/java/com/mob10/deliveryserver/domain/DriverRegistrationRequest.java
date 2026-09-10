package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "driver_requests")
public class DriverRegistrationRequest {
    @Id
    private Long id;

    private Long userId;

    private String licensePlate;

    private String status; // PENDING, APPROVED, REJECTED

    private Instant createdAt = Instant.now();

    public DriverRegistrationRequest() {}

    public DriverRegistrationRequest(Long userId, String licensePlate) {
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.status = "PENDING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
