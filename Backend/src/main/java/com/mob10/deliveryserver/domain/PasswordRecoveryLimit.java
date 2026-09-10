package com.mob10.deliveryserver.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * Lưu trạng thái rate-limit cho từng key (phone/IP) trong luồng khôi phục mật khẩu.
 * id  = key duy nhất (vd: "reset-phone:<hashed>", "reset-ip:<hashed>", "link:<userId>")
 */
@Document(collection = "password_recovery_limits")
public class PasswordRecoveryLimit {

    @Id
    private String id;

    /** Thời điểm bắt đầu cửa sổ đếm 1 giờ hiện tại. */
    private Instant windowStart;

    /** Thời điểm request gần nhất (dùng để kiểm tra cooldown giây). */
    private Instant lastRequest;

    /** Số request trong cửa sổ hiện tại. */
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
