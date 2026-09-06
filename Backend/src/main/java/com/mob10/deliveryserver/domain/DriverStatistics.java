package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "driver_statistics")
public class DriverStatistics {
    @Id
    private Long driverId;
    private User driver;
    private int totalAccepted;
    private int totalRejected;
    private int penalizedRejections;
    private BigDecimal reliabilityScore = new BigDecimal("100.00");
    private Instant lockedUntil;
    private Instant updatedAt = Instant.now();

    public DriverStatistics() {}

    public DriverStatistics(User driver) {
        this.driver = driver;
        this.driverId = driver != null ? driver.getId() : null;
    }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; if (driver != null) this.driverId = driver.getId(); }
    public DriverAvailability getAvailability() { return driver != null ? driver.getDriverAvailability() : DriverAvailability.OFFLINE; }
    public int getTotalAccepted() { return totalAccepted; }
    public void setTotalAccepted(int totalAccepted) { this.totalAccepted = totalAccepted; }
    public int getTotalRejected() { return totalRejected; }
    public void setTotalRejected(int totalRejected) { this.totalRejected = totalRejected; }
    public int getPenalizedRejections() { return penalizedRejections; }
    public void setPenalizedRejections(int penalizedRejections) { this.penalizedRejections = penalizedRejections; }
    public BigDecimal getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(BigDecimal reliabilityScore) { this.reliabilityScore = reliabilityScore; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public boolean isLocked() { return lockedUntil != null && lockedUntil.isAfter(Instant.now()); }
    public void recordAcceptance() { totalAccepted++; updatedAt = Instant.now(); }
    public void recordRejection(int penaltyPoints, boolean penalized) {
        totalRejected++;
        if (penalized) penalizedRejections++;
        reliabilityScore = reliabilityScore.subtract(BigDecimal.valueOf(penaltyPoints)).max(BigDecimal.ZERO);
        updatedAt = Instant.now();
    }
    public void lockUntil(Instant value) { lockedUntil = value; updatedAt = Instant.now(); }
}
