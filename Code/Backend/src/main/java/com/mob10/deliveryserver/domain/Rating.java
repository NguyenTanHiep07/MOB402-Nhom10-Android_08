package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import java.time.Instant;

@Document(collection = "ratings")
public class Rating {
    @Id
    private Long id;
    @DocumentReference(lazy = false)
    private DeliveryRequest deliveryRequest;
    private Long deliveryRequestId;
    @DocumentReference(lazy = false)
    private User client;
    private Long clientId;
    @DocumentReference(lazy = false)
    private User driver;
    private Long driverId;
    private short stars;
    private String comment;
    private Instant createdAt = Instant.now();

    public Rating() {}

    public Rating(DeliveryRequest deliveryRequest, User client, User driver, int stars, String comment) {
        this.deliveryRequest = deliveryRequest;
        this.deliveryRequestId = deliveryRequest != null ? deliveryRequest.getId() : null;
        this.client = client;
        this.clientId = client != null ? client.getId() : null;
        this.driver = driver;
        this.driverId = driver != null ? driver.getId() : null;
        this.stars = (short) stars;
        this.comment = comment;
        this.createdAt = Instant.now();
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
    public User getClient() { return client; }
    public void setClient(User client) {
        this.client = client;
        this.clientId = client != null ? client.getId() : null;
    }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public User getDriver() { return driver; }
    public void setDriver(User driver) {
        this.driver = driver;
        this.driverId = driver != null ? driver.getId() : null;
    }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = (short) stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
