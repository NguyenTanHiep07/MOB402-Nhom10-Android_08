package com.mob10.deliveryserver.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "order_rejections", uniqueConstraints =
        @UniqueConstraint(name = "uk_rejection_request_driver", columnNames = {"delivery_request_id", "driver_id"}))
public class OrderRejection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_request_id", nullable = false)
    private DeliveryRequest deliveryRequest;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reason_code", nullable = false)
    private RejectionReason reason;
    @Column(length = 500)
    private String note;
    @Column(name = "penalty_applied", nullable = false)
    private boolean penaltyApplied;
    @Column(name = "rejected_at", nullable = false)
    private Instant rejectedAt = Instant.now();

    protected OrderRejection() {}
    public OrderRejection(DeliveryRequest request, User driver, RejectionReason reason, String note) {
        this(request, driver, reason, note, Instant.now());
    }
    public OrderRejection(DeliveryRequest request, User driver, RejectionReason reason, String note,
                          Instant rejectedAt) {
        this.deliveryRequest = request; this.driver = driver; this.reason = reason;
        this.note = note; this.penaltyApplied = !reason.isValid() && reason.getPenaltyPoints() > 0;
        this.rejectedAt = rejectedAt;
    }
    public Long getId() { return id; }
    public RejectionReason getReason() { return reason; }
    public String getNote() { return note; }
    public boolean isPenaltyApplied() { return penaltyApplied; }
    public Instant getRejectedAt() { return rejectedAt; }
}
