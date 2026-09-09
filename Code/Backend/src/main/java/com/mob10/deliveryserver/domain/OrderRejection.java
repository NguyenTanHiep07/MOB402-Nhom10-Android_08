package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import java.time.Instant;

@Document(collection = "order_rejections")
public class OrderRejection {
    @Id
    private Long id;
    @DocumentReference(lazy = false)
    private DeliveryRequest deliveryRequest;
    @DocumentReference(lazy = false)
    private User driver;
    private Long deliveryRequestId;
    private Long driverId;
    @DocumentReference(lazy = false)
    private RejectionReason reason;
    private String note;
    private boolean penaltyApplied;
    private Instant rejectedAt = Instant.now();

    public OrderRejection() {}

    public OrderRejection(DeliveryRequest request, User driver, RejectionReason reason, String note) {
        this(request, driver, reason, note, Instant.now());
    }

    public OrderRejection(DeliveryRequest request, User driver, RejectionReason reason, String note,
                          Instant rejectedAt) {
        this.deliveryRequest = request;
        this.deliveryRequestId = request != null ? request.getId() : null;
        this.driver = driver;
        this.driverId = driver != null ? driver.getId() : null;
        this.reason = reason;
        this.note = note;
        this.penaltyApplied = reason != null && !reason.isValid() && reason.getPenaltyPoints() > 0;
        this.rejectedAt = rejectedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DeliveryRequest getDeliveryRequest() { return deliveryRequest; }
    public void setDeliveryRequest(DeliveryRequest deliveryRequest) {
        this.deliveryRequest = deliveryRequest;
        this.deliveryRequestId = deliveryRequest != null ? deliveryRequest.getId() : null;
    }
    public User getDriver() { return driver; }
    public void setDriver(User driver) {
        this.driver = driver;
        this.driverId = driver != null ? driver.getId() : null;
    }
    public Long getDeliveryRequestId() { return deliveryRequestId; }
    public void setDeliveryRequestId(Long deliveryRequestId) { this.deliveryRequestId = deliveryRequestId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public RejectionReason getReason() { return reason; }
    public void setReason(RejectionReason reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isPenaltyApplied() { return penaltyApplied; }
    public void setPenaltyApplied(boolean penaltyApplied) { this.penaltyApplied = penaltyApplied; }
    public Instant getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }
}
