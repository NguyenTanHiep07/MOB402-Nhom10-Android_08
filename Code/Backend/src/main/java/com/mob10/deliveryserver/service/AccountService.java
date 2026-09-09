package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.AccountChallenge;
import com.mob10.deliveryserver.domain.PasswordRecoveryLimit;
import com.mob10.deliveryserver.domain.User;
import com.mob10.deliveryserver.dto.AccountDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.AccountChallengeRepository;
import com.mob10.deliveryserver.repository.PasswordRecoveryLimitRepository;
import com.mob10.deliveryserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class AccountService {
    private final UserRepository users;
    private final AccountChallengeRepository challenges;
    private final PasswordRecoveryLimitRepository limits;
    private final PasswordEncoder passwords;
    private final AccountMail mail;
    private final byte[] secret;
    private final SecureRandom random = new SecureRandom();

    public AccountService(UserRepository users, AccountChallengeRepository challenges,
                          PasswordRecoveryLimitRepository limits, PasswordEncoder passwords,
                          AccountMail mail, @Value("${app.jwt.secret}") String secret) {
        this.users = users;
        this.challenges = challenges;
        this.limits = limits;
        this.passwords = passwords;
        this.mail = mail;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    private String hash(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Hash unavailable");
        }
    }

    private String phone(String raw) {
        String value = raw.replaceAll("[\\s().-]", "");
        if (value.startsWith("+84")) value = "0" + value.substring(3);
        if (!value.matches("0[0-9]{9,10}")) throw bad("Số điện thoại cần 10–11 chữ số, bắt đầu bằng 0.");
        return value;
    }

    private synchronized void limit(String key, int max, int cooldown) {
        Instant now = Instant.now();
        PasswordRecoveryLimit row = limits.findById(key).orElse(null);
        if (row == null) {
            limits.save(new PasswordRecoveryLimit(key, now, now, 1));
            return;
        }
        Instant start = row.getWindowStart();
        int count = row.getCount();
        if (now.isAfter(start.plusSeconds(3600))) {
            start = now;
            count = 0;
        }
        if (count >= max || (cooldown > 0 && now.isBefore(row.getLastRequest().plusSeconds(cooldown)))) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT",
                    "Đã đạt giới hạn thao tác. Gửi mã cách nhau 60 giây, tối đa 3 lần mỗi giờ; hãy thử lại sau.");
        }
        row.setWindowStart(start);
        row.setLastRequest(now);
        row.setCount(count + 1);
        limits.save(row);
    }

    private User user(long id) {
        return users.findById(id).filter(User::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "SESSION_INVALID", "Phiên đăng nhập không hợp lệ."));
    }

    private void reauth(User user, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) throw bad("Mật khẩu hiện tại không hợp lệ.");
        if (!passwords.matches(password, user.getPasswordHash())) throw bad("Mật khẩu hiện tại không đúng.");
    }

    public Profile profile(long id) {
        return profile(user(id));
    }

    public Message emailStatus(long id) {
        var list = challenges.findByUserIdAndPurposeOrderByCreatedAtDesc(id, "LINK");
        if (list.isEmpty()) return new Message("Chưa yêu cầu mã xác minh email.");
        var c = list.getFirst();
        if ("FAILED".equals(c.getMailStatus())) return new Message("Không gửi được email. Kiểm tra cấu hình Gmail/kết nối backend, rồi gửi mã mới sau thời gian chờ.");
        if (c.isConsumed() || !c.getExpiresAt().isAfter(Instant.now())) return new Message("Yêu cầu đã kết thúc. Nếu chưa xác minh thành công, hãy gửi mã mới.");
        return new Message("SENT".equals(c.getMailStatus()) ? "Máy chủ thư đã tiếp nhận email. Kiểm tra hộp thư và thư rác." : "Đang gửi email xác minh…");
    }

    private Profile profile(User u) {
        return new Profile(u.getId(), u.getUsername(), u.getFullName(),
                u.getPhoneNumber(), u.getRole() != null ? u.getRole().name() : null, u.getLicensePlate(),
                u.getRecoveryEmail(), u.getEmailVerifiedAt() != null, u.getAvatarBase64());
    }

    private String avatar(String encoded) {
        if (encoded == null || encoded.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            if (bytes.length > 160000) throw bad("Ảnh quá lớn. Chọn ảnh nhỏ hơn.");
            try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw bad("Ảnh không hợp lệ.");
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    int w = reader.getWidth(0), h = reader.getHeight(0);
                    if (w < 1 || h < 1 || w > 1024 || h > 1024) throw bad("Ảnh cần có kích thước tối đa 1024 × 1024.");
                    var image = reader.read(0);
                    var rgb = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    var graphics = rgb.createGraphics();
                    graphics.drawImage(image, 0, 0, null);
                    graphics.dispose();
                    var output = new ByteArrayOutputStream();
                    ImageIO.write(rgb, "jpg", output);
                    if (output.size() > 160000) throw bad("Ảnh quá lớn. Chọn ảnh nhỏ hơn.");
                    return Base64.getEncoder().encodeToString(output.toByteArray());
                } finally {
                    reader.dispose();
                }
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw bad("Không đọc được ảnh đại diện. Hãy chọn ảnh khác.");
        }
    }

    public Profile edit(long id, Edit body) {
        limit("edit:" + id, 20, 0);
        String number = phone(body.phoneNumber());
        String image = avatar(body.avatarBase64());
        User u = user(id);
        reauth(u, body.currentPassword());
        if (u.getRole() == null || (!"CLIENT".equals(u.getRole().name()) && !"DELIVERY".equals(u.getRole().name()))) {
            throw bad("Chỉ khách hàng và tài xế được sửa hồ sơ tại đây.");
        }
        users.findByUsername(body.username()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw bad("Tên đăng nhập hoặc số điện thoại đã được sử dụng.");
        });
        users.findByPhoneNumber(number).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw bad("Tên đăng nhập hoặc số điện thoại đã được sử dụng.");
        });
        boolean phoneChanged = !number.equals(u.getPhoneNumber());
        u.setUsername(body.username());
        u.setFullName(body.fullName().trim());
        u.setPhoneNumber(number);
        u.setAvatarBase64(image);
        users.save(u);

        if (phoneChanged) {
            challenges.findByUserIdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(id, "RESET").forEach(c -> {
                c.setConsumed(true);
                c.setMailPayload(null);
                challenges.save(c);
            });
        }
        return profile(u);
    }

    private void challenge(User u, String purpose, String email) {
        long id = u.getId();
        challenges.findByUserIdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(id, purpose).forEach(c -> {
            c.setConsumed(true);
            c.setMailPayload(null);
            challenges.save(c);
        });
        String challengeId = UUID.randomUUID().toString();
        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        AccountChallenge c = new AccountChallenge(challengeId, id, purpose, email,
                u.getPhoneNumber(), u.getCredentialVersion(), hash(challengeId + ":" + code),
                mail.encrypt(code), Instant.now().plusSeconds(600));
        challenges.save(c);
    }

    public void link(long id, Link body) {
        mail.requireConfigured();
        limit("reauth:" + id, 20, 0);
        User u = user(id);
        reauth(u, body.currentPassword());
        limit("link:" + id, 3, 60);
        String email = body.email().trim().toLowerCase(Locale.ROOT);
        users.findAll().stream()
                .filter(other -> !other.getId().equals(id) && other.getRecoveryEmail() != null
                        && other.getRecoveryEmail().equalsIgnoreCase(email))
                .findAny()
                .ifPresent(other -> { throw bad("Không thể liên kết email này. Hãy dùng email khác."); });
        challenge(u, "LINK", email);
    }

    private AccountChallenge validChallenge(long id, String purpose, String code, User u) {
        var list = challenges.findByUserIdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(id, purpose);
        if (list.isEmpty()) return null;
        var c = list.getFirst();
        if (c.getAttempts() >= 5 || !c.getExpiresAt().isAfter(Instant.now())
                || !Objects.equals(c.getCredentialVersion(), u.getCredentialVersion())
                || !"SENT".equals(c.getMailStatus())) return null;
        c.setAttempts(c.getAttempts() + 1);
        challenges.save(c);
        return MessageDigest.isEqual(hash(c.getId() + ":" + code).getBytes(StandardCharsets.US_ASCII),
                c.getCodeHash().getBytes(StandardCharsets.US_ASCII)) ? c : null;
    }

    public Profile verify(long id, Verify body) {
        limit("reauth:" + id, 20, 0);
        User u = user(id);
        reauth(u, body.currentPassword());
        AccountChallenge c = validChallenge(id, "LINK", body.code(), u);
        if (c == null) throw invalidCode();
        String targetEmail = c.getTargetEmail();
        users.findAll().stream()
                .filter(other -> !other.getId().equals(id) && other.getRecoveryEmail() != null
                        && other.getRecoveryEmail().equalsIgnoreCase(targetEmail))
                .findAny()
                .ifPresent(other -> { throw bad("Email đã được liên kết với tài khoản khác. Hãy dùng email khác."); });
        u.setRecoveryEmail(targetEmail);
        u.setEmailVerifiedAt(Instant.now());
        users.save(u);
        c.setConsumed(true);
        c.setMailPayload(null);
        challenges.save(c);
        return profile(u);
    }

    public void request(String raw, String ip) {
        mail.requireConfigured();
        String number = phone(raw);
        limit("reset-ip:" + hash(ip), 20, 0);
        limit("reset-phone:" + hash(number), 3, 60);
        users.findByPhoneNumber(number).ifPresent(u -> {
            if (u.isActive() && u.getEmailVerifiedAt() != null && u.getRecoveryEmail() != null) {
                challenge(u, "RESET", u.getRecoveryEmail());
            }
        });
    }

    public void reset(Reset body, String ip) {
        String number = phone(body.phoneNumber());
        String password = body.newPassword();
        if (password.getBytes(StandardCharsets.UTF_8).length > 72 || !password.matches("(?s).*\\p{L}.*") || !password.matches("(?s).*\\p{N}.*"))
            throw bad("Mật khẩu cần 12–64 ký tự, có chữ và số, tối đa 72 byte UTF-8.");
        limit("confirm-ip:" + hash(ip), 60, 0);
        User u = users.findByPhoneNumber(number).filter(User::isActive).orElse(null);
        if (u == null) throw invalidCode();
        long id = u.getId();
        AccountChallenge c = validChallenge(id, "RESET", body.code(), u);
        if (c == null || !Objects.equals(c.getPhoneSnapshot(), number) || u.getEmailVerifiedAt() == null || !Objects.equals(c.getTargetEmail(), u.getRecoveryEmail())) {
            throw invalidCode();
        }
        if (passwords.matches(password, u.getPasswordHash())) throw bad("Mật khẩu mới phải khác mật khẩu cũ.");
        u.setPasswordHash(passwords.encode(password));
        u.setCredentialVersion(u.getCredentialVersion() + 1);
        users.save(u);
        c.setConsumed(true);
        c.setMailPayload(null);
        challenges.save(c);
    }

    private ApiException invalidCode() {
        return bad("Mã không đúng, hết hạn, đã dùng hoặc đã nhập sai 5 lần. Hãy kiểm tra hoặc gửi mã mới.");
    }

    private ApiException bad(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_INVALID", message);
    }
}
