package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import java.time.Instant;

@Document(collection = "status_histories")
public class StatusHistory {
    @Id
    private Long id;
    @DocumentReference(lazy = false)
    private DeliveryRequest deliveryRequest;
    private Long deliveryRequestId;
    private DeliveryStatus fromStatus;
    private DeliveryStatus toStatus;
    @DocumentReference(lazy = false)
    private User updatedBy;
    private Long updatedById;
    private Instant timestamp = Instant.now();
    private String note;

    public StatusHistory() {}

    public StatusHistory(DeliveryRequest request, DeliveryStatus from, DeliveryStatus to, User updatedBy, String note) {
        this(request, from, to, updatedBy, note, Instant.now());
    }

    public StatusHistory(DeliveryRequest request, DeliveryStatus from, DeliveryStatus to, User updatedBy,
                         String note, Instant timestamp) {
        this.deliveryRequest = request;
        this.deliveryRequestId = request != null ? request.getId() : null;
        this.fromStatus = from;
        this.toStatus = to;
        this.updatedBy = updatedBy;
        this.updatedById = updatedBy != null ? updatedBy.getId() : null;
        this.note = note;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DeliveryRequest getDeliveryRequest() { return deliveryRequest; }
    public void setDeliveryRequest(DeliveryRequest deliveryRequest) {
        this.deliveryRequest = deliveryRequest;
        this.deliveryRequestId = deliveryRequest != null ? deliveryRequest.getId() : null;
    }
    public Long getDeliveryRequestId() { return deliveryRequestId; }
    public void setDeliveryRequestId(Long deliveryRequestId) { this.deliveryRequestId = deliveryRequestId; }
    public DeliveryStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(DeliveryStatus fromStatus) { this.fromStatus = fromStatus; }
    public DeliveryStatus getToStatus() { return toStatus; }
    public void setToStatus(DeliveryStatus toStatus) { this.toStatus = toStatus; }
    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
        this.updatedById = updatedBy != null ? updatedBy.getId() : null;
    }
    public Long getUpdatedById() { return updatedById; }
    public void setUpdatedById(Long updatedById) { this.updatedById = updatedById; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
