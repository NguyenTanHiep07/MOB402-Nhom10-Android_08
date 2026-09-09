package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "users")
public class User {
    @Id
    private Long id;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;

    private String fullName;

    @Indexed(unique = true)
    private String phoneNumber;

    private Role role;

    private String licensePlate;

    private DriverAvailability driverAvailability = DriverAvailability.OFFLINE;

    private boolean active = true;

    private Instant createdAt = Instant.now();

    private long credentialVersion = 0;

    private String recoveryEmail;

    private Instant emailVerifiedAt;

    private String avatarBase64;

    public User() {}

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
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public DriverAvailability getDriverAvailability() { return driverAvailability; }
    public void setDriverAvailability(DriverAvailability driverAvailability) { this.driverAvailability = driverAvailability; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public long getCredentialVersion() { return credentialVersion; }
    public void setCredentialVersion(long credentialVersion) { this.credentialVersion = credentialVersion; }
    public String getRecoveryEmail() { return recoveryEmail; }
    public void setRecoveryEmail(String recoveryEmail) { this.recoveryEmail = recoveryEmail; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(Instant emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
    public String getAvatarBase64() { return avatarBase64; }
    public void setAvatarBase64(String avatarBase64) { this.avatarBase64 = avatarBase64; }
}
