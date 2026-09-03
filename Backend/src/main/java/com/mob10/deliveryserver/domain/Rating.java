package com.mob10.deliveryserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ratings_delivery_request",
                columnNames = "delivery_request_id"),
        indexes = {
                @Index(name = "idx_ratings_client_created_at", columnList = "client_id, created_at"),
                @Index(name = "idx_ratings_driver_created_at", columnList = "driver_id, created_at")
        })
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_request_id", nullable = false)
    private DeliveryRequest deliveryRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(nullable = false)
    private short stars;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Rating() {}

    public Rating(DeliveryRequest deliveryRequest, User client, User driver, int stars, String comment) {
        this.deliveryRequest = deliveryRequest;
        this.client = client;
        this.driver = driver;
        this.stars = (short) stars;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public DeliveryRequest getDeliveryRequest() { return deliveryRequest; }
    public User getClient() { return client; }
    public User getDriver() { return driver; }
    public int getStars() { return stars; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
