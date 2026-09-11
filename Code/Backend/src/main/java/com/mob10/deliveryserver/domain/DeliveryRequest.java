package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "delivery_requests")
public class DeliveryRequest {
    @Id
    private Long id;

    @Version
    private Long version;

    @DocumentReference(lazy = false)
    private User client;
    @DocumentReference(lazy = false)
    private User deliveryPerson;

    private BigDecimal distanceKm;
    private BigDecimal baseFee;
    private BigDecimal distanceFee;
    private BigDecimal weightFee;
    private BigDecimal fragileCharge;
    private BigDecimal totalCost;

    @Indexed
    private DeliveryStatus status = DeliveryStatus.CHO_TIEP_NHAN;

    private String pickupAddress;
    private String deliveryAddress;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    private String senderName;
    private String senderPhone;
    private String recipientName;
    private String recipientPhone;
    private String note;
    private String deliveryPhoto;
    private Instant scheduledPickupTime;
    private Instant actualDeliveryTime;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    private List<PackageItem> packages = new ArrayList<>();

    public DeliveryRequest() {}

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
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    public User getDeliveryPerson() { return deliveryPerson; }
    public void setDeliveryPerson(User deliveryPerson) { this.deliveryPerson = deliveryPerson; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
    public BigDecimal getBaseFee() { return baseFee; }
    public void setBaseFee(BigDecimal baseFee) { this.baseFee = baseFee; }
    public BigDecimal getDistanceFee() { return distanceFee; }
    public void setDistanceFee(BigDecimal distanceFee) { this.distanceFee = distanceFee; }
    public BigDecimal getWeightFee() { return weightFee; }
    public void setWeightFee(BigDecimal weightFee) { this.weightFee = weightFee; }
    public BigDecimal getFragileCharge() { return fragileCharge; }
    public void setFragileCharge(BigDecimal fragileCharge) { this.fragileCharge = fragileCharge; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public BigDecimal getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(BigDecimal pickupLatitude) { this.pickupLatitude = pickupLatitude; }
    public BigDecimal getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(BigDecimal pickupLongitude) { this.pickupLongitude = pickupLongitude; }
    public BigDecimal getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(BigDecimal deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }
    public BigDecimal getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(BigDecimal deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getDeliveryPhoto() { return deliveryPhoto; }
    public void setDeliveryPhoto(String deliveryPhoto) { this.deliveryPhoto = deliveryPhoto; }
    public Instant getScheduledPickupTime() { return scheduledPickupTime; }
    public void setScheduledPickupTime(Instant scheduledPickupTime) { this.scheduledPickupTime = scheduledPickupTime; }
    public Instant getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(Instant actualDeliveryTime) { this.actualDeliveryTime = actualDeliveryTime; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<PackageItem> getPackages() { return packages; }
    public void setPackages(List<PackageItem> packages) { this.packages = packages; }
    public void addPackage(PackageItem item) { packages.add(item); }
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
