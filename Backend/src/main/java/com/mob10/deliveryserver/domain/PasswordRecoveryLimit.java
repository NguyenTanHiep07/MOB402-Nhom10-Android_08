package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "password_recovery_limits")
public class PasswordRecoveryLimit {
    @Id
    private String id;
    private Instant windowStart;
    private Instant lastRequest;
    private int count;

    public PasswordRecoveryLimit() {}

    public PasswordRecoveryLimit(String id, Instant windowStart, Instant lastRequest, int count) {
        this.id = id;
        this.windowStart = windowStart;
        this.lastRequest = lastRequest;
        this.count = count;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Instant getWindowStart() { return windowStart; }
    public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }
    public Instant getLastRequest() { return lastRequest; }
    public void setLastRequest(Instant lastRequest) { this.lastRequest = lastRequest; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
