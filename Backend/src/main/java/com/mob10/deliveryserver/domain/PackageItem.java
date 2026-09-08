package com.mob10.deliveryserver.domain;

import java.math.BigDecimal;

public class PackageItem {
    private Long id;
    private String name;
    private String packageType;
    private BigDecimal weightKg;
    private int quantity;
    private String notes;
    private boolean fragile;
    private boolean express;

    public PackageItem() {}

    public PackageItem(String name, String packageType, BigDecimal weightKg, int quantity, String notes, boolean fragile, boolean express) {
        this.name = name;
        this.packageType = packageType;
        this.weightKg = weightKg;
        this.quantity = quantity;
        this.notes = notes;
        this.fragile = fragile;
        this.express = express;
    }

    public PackageItem(Long id, String name, String packageType, BigDecimal weightKg, int quantity, String notes, boolean fragile, boolean express) {
        this(name, packageType, weightKg, quantity, notes, fragile, express);
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isFragile() { return fragile; }
    public void setFragile(boolean fragile) { this.fragile = fragile; }
    public boolean isExpress() { return express; }
    public void setExpress(boolean express) { this.express = express; }
}
