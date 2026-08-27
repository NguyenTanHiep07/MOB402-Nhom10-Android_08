package com.mob10.deliveryserver.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_requests", indexes = {
        @Index(name = "idx_delivery_requests_status", columnList = "status"),
        @Index(name = "idx_delivery_requests_client", columnList = "client_id"),
        @Index(name = "idx_delivery_requests_driver", columnList = "delivery_person_id")
})
public class DeliveryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_person_id")
    private User deliveryPerson;

    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;
    @Column(name = "base_fee", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseFee;
    @Column(name = "distance_fee", nullable = false, precision = 14, scale = 2)
    private BigDecimal distanceFee;
    @Column(name = "weight_fee", nullable = false, precision = 14, scale = 2)
    private BigDecimal weightFee;
    @Column(name = "fragile_charge", nullable = false, precision = 14, scale = 2)
    private BigDecimal fragileCharge;
    @Column(name = "total_cost", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status = DeliveryStatus.CHO_TIEP_NHAN;

    @Column(name = "pickup_address", nullable = false, length = 500)
    private String pickupAddress;
    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;
    @Column(name = "pickup_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLatitude;
    @Column(name = "pickup_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal pickupLongitude;
    @Column(name = "delivery_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal deliveryLatitude;
    @Column(name = "delivery_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal deliveryLongitude;
    @Column(name = "sender_name", nullable = false, length = 120)
    private String senderName;
    @Column(name = "sender_phone", nullable = false, length = 20)
    private String senderPhone;
    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;
    @Column(length = 1000)
    private String note;
    @Column(name = "scheduled_pickup_time")
    private Instant scheduledPickupTime;
    @Column(name = "actual_delivery_time")
    private Instant actualDeliveryTime;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "deliveryRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackageItem> packages = new ArrayList<>();

    protected DeliveryRequest() {}

    public DeliveryRequest(User client, BigDecimal distanceKm, String pickupAddress, String deliveryAddress,
                           BigDecimal pickupLatitude, BigDecimal pickupLongitude,
                           BigDecimal deliveryLatitude, BigDecimal deliveryLongitude,
                           String senderName, String senderPhone, String recipientName, String recipientPhone,
                           String note, Instant scheduledPickupTime) {
        this(client, distanceKm, pickupAddress, deliveryAddress, pickupLatitude, pickupLongitude,
                deliveryLatitude, deliveryLongitude, senderName, senderPhone, recipientName, recipientPhone,
                note, scheduledPickupTime, Instant.now());
    }

    public DeliveryRequest(User client, BigDecimal distanceKm, String pickupAddress, String deliveryAddress,
                           BigDecimal pickupLatitude, BigDecimal pickupLongitude,
                           BigDecimal deliveryLatitude, BigDecimal deliveryLongitude,
                           String senderName, String senderPhone, String recipientName, String recipientPhone,
                           String note, Instant scheduledPickupTime, Instant createdAt) {
        this.client = client;
        this.distanceKm = distanceKm;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.deliveryLatitude = deliveryLatitude;
        this.deliveryLongitude = deliveryLongitude;
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.note = note;
        this.scheduledPickupTime = scheduledPickupTime;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public Long getId() { return id; }
    public User getClient() { return client; }
    public User getDeliveryPerson() { return deliveryPerson; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public BigDecimal getBaseFee() { return baseFee; }
    public BigDecimal getDistanceFee() { return distanceFee; }
    public BigDecimal getWeightFee() { return weightFee; }
    public BigDecimal getFragileCharge() { return fragileCharge; }
    public BigDecimal getTotalCost() { return totalCost; }
    public DeliveryStatus getStatus() { return status; }
    public String getPickupAddress() { return pickupAddress; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public BigDecimal getPickupLatitude() { return pickupLatitude; }
    public BigDecimal getPickupLongitude() { return pickupLongitude; }
    public BigDecimal getDeliveryLatitude() { return deliveryLatitude; }
    public BigDecimal getDeliveryLongitude() { return deliveryLongitude; }
    public String getSenderName() { return senderName; }
    public String getSenderPhone() { return senderPhone; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getNote() { return note; }
    public Instant getScheduledPickupTime() { return scheduledPickupTime; }
    public Instant getActualDeliveryTime() { return actualDeliveryTime; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<PackageItem> getPackages() { return packages; }
    public void addPackage(PackageItem item) { packages.add(item); item.attachTo(this); }
    public void applyFees(BigDecimal base, BigDecimal distance, BigDecimal weight, BigDecimal optional, BigDecimal total) {
        this.baseFee = base; this.distanceFee = distance; this.weightFee = weight;
        this.fragileCharge = optional; this.totalCost = total;
    }
    public void assignDriver(User driver) { assignDriver(driver, Instant.now()); }
    public void assignDriver(User driver, Instant occurredAt) {
        this.deliveryPerson = driver;
        this.status = DeliveryStatus.DA_CHAP_NHAN;
        this.updatedAt = occurredAt;
    }
    public void changeStatus(DeliveryStatus status) {
        changeStatus(status, Instant.now());
    }
    public void changeStatus(DeliveryStatus status, Instant occurredAt) {
        this.status = status;
        this.updatedAt = occurredAt;
        if (status == DeliveryStatus.DA_GIAO) this.actualDeliveryTime = occurredAt;
    }
}
