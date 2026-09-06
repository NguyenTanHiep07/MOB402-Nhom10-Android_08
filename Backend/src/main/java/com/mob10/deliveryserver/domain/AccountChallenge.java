package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "account_challenges")
public class AccountChallenge {
    @Id
    private String id;
    private Long userId;
    private String purpose;
    private String targetEmail;
    private String phoneSnapshot;
    private Long credentialVersion;
    private String codeHash;
    private String mailPayload;
    private String mailStatus = "QUEUED";
    private int attempts = 0;
    private boolean consumed = false;
    private Instant expiresAt;
    private Instant createdAt = Instant.now();

    public AccountChallenge() {}

    public AccountChallenge(String id, Long userId, String purpose, String targetEmail,
                            String phoneSnapshot, Long credentialVersion, String codeHash,
                            String mailPayload, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.purpose = purpose;
        this.targetEmail = targetEmail;
        this.phoneSnapshot = phoneSnapshot;
        this.credentialVersion = credentialVersion;
        this.codeHash = codeHash;
        this.mailPayload = mailPayload;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
    public String getPhoneSnapshot() { return phoneSnapshot; }
    public void setPhoneSnapshot(String phoneSnapshot) { this.phoneSnapshot = phoneSnapshot; }
    public Long getCredentialVersion() { return credentialVersion; }
    public void setCredentialVersion(Long credentialVersion) { this.credentialVersion = credentialVersion; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public String getMailPayload() { return mailPayload; }
    public void setMailPayload(String mailPayload) { this.mailPayload = mailPayload; }
    public String getMailStatus() { return mailStatus; }
    public void setMailStatus(String mailStatus) { this.mailStatus = mailStatus; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public boolean isConsumed() { return consumed; }
    public void setConsumed(boolean consumed) { this.consumed = consumed; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
