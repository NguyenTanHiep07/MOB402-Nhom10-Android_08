package com.mob10.deliveryserver.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "packages")
public class PackageItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_request_id", nullable = false)
    private DeliveryRequest deliveryRequest;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(name = "package_type", length = 50)
    private String packageType;
    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;
    @Column(nullable = false)
    private int quantity;
    @Column(length = 500)
    private String notes;
    @Column(name = "is_fragile", nullable = false)
    private boolean fragile;
    @Column(name = "is_express", nullable = false)
    private boolean express;

    protected PackageItem() {}
    public PackageItem(String name, String packageType, BigDecimal weightKg, int quantity, String notes, boolean fragile, boolean express) {
        this.name = name; this.packageType = packageType; this.weightKg = weightKg;
        this.quantity = quantity; this.notes = notes; this.fragile = fragile; this.express = express;
    }
    void attachTo(DeliveryRequest request) { this.deliveryRequest = request; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPackageType() { return packageType; }
    public BigDecimal getWeightKg() { return weightKg; }
    public int getQuantity() { return quantity; }
    public String getNotes() { return notes; }
    public boolean isFragile() { return fragile; }
    public boolean isExpress() { return express; }
}
