package com.mob10.deliveryserver.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "license_plate", length = 30)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "driver_availability", nullable = false, length = 20)
    private DriverAvailability driverAvailability = DriverAvailability.OFFLINE;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "credential_version", nullable = false)
    private long credentialVersion = 0;

    public long getCredentialVersion() { return credentialVersion; }

    protected User() {}

    public User(String username, String passwordHash, String fullName, String phoneNumber, Role role, String licensePlate) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.licensePlate = licensePlate;
        this.driverAvailability = role == Role.DELIVERY ? DriverAvailability.AVAILABLE : DriverAvailability.OFFLINE;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public Role getRole() { return role; }
    public String getLicensePlate() { return licensePlate; }
    public DriverAvailability getDriverAvailability() { return driverAvailability; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setDriverAvailability(DriverAvailability value) { this.driverAvailability = value; }
}
