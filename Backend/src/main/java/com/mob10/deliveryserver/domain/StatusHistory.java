package com.mob10.deliveryserver.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "status_histories")
public class StatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_request_id", nullable = false)
    private DeliveryRequest deliveryRequest;
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private DeliveryStatus fromStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private DeliveryStatus toStatus;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    @Column(nullable = false)
    private Instant timestamp = Instant.now();
    @Column(length = 500)
    private String note;

    protected StatusHistory() {}
    public StatusHistory(DeliveryRequest request, DeliveryStatus from, DeliveryStatus to, User updatedBy, String note) {
        this(request, from, to, updatedBy, note, Instant.now());
    }
    public StatusHistory(DeliveryRequest request, DeliveryStatus from, DeliveryStatus to, User updatedBy,
                         String note, Instant timestamp) {
        this.deliveryRequest = request; this.fromStatus = from; this.toStatus = to;
        this.updatedBy = updatedBy; this.note = note; this.timestamp = timestamp;
    }
    public Long getId() { return id; }
    public DeliveryStatus getFromStatus() { return fromStatus; }
    public DeliveryStatus getToStatus() { return toStatus; }
    public User getUpdatedBy() { return updatedBy; }
    public Instant getTimestamp() { return timestamp; }
    public String getNote() { return note; }
}
