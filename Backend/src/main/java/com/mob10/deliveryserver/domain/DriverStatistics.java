package com.mob10.deliveryserver.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "driver_statistics")
public class DriverStatistics {
    @Id
    @Column(name = "driver_id")
    private Long driverId;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "driver_id")
    private User driver;
    @Column(name = "total_accepted", nullable = false)
    private int totalAccepted;
    @Column(name = "total_rejected", nullable = false)
    private int totalRejected;
    @Column(name = "penalized_rejections", nullable = false)
    private int penalizedRejections;
    @Column(name = "reliability_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal reliabilityScore = new BigDecimal("100.00");
    @Column(name = "locked_until")
    private Instant lockedUntil;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected DriverStatistics() {}
    public DriverStatistics(User driver) { this.driver = driver; }
    public Long getDriverId() { return driverId; }
    public DriverAvailability getAvailability() { return driver.getDriverAvailability(); }
    public int getTotalAccepted() { return totalAccepted; }
    public int getTotalRejected() { return totalRejected; }
    public int getPenalizedRejections() { return penalizedRejections; }
    public BigDecimal getReliabilityScore() { return reliabilityScore; }
    public Instant getLockedUntil() { return lockedUntil; }
    public Instant getUpdatedAt() { return updatedAt; }
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
