package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.AccountChallenge;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.AccountChallengeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Component
@EnableScheduling
public class AccountMail {
    private final JavaMailSender sender;
    private final AccountChallengeRepository challenges;
    private final boolean enabled;
    private final String from;
    private final SecretKeySpec key;

    public AccountMail(JavaMailSender sender, AccountChallengeRepository challenges,
                       @Value("${app.mail.enabled:false}") boolean enabled,
                       @Value("${app.mail.from:}") String from,
                       @Value("${app.jwt.secret}") String secret) throws Exception {
        this.sender = sender;
        this.challenges = challenges;
        this.enabled = enabled;
        this.from = from.trim();
        this.key = new SecretKeySpec(MessageDigest.getInstance("SHA-256")
                .digest(("account-mail:" + secret).getBytes(StandardCharsets.UTF_8)), "AES");
    }

    public void requireConfigured() {
        if (!enabled || from.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_UNAVAILABLE",
                    "Dịch vụ email chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        }
    }

    public String encrypt(String code) {
        try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(cipher.doFinal(code.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Mail encryption unavailable");
        }
    }

    private String decrypt(String value) throws Exception {
        String[] parts = value.split(":");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(parts[0])));
        return new String(cipher.doFinal(Base64.getDecoder().decode(parts[1])), StandardCharsets.UTF_8);
    }

    @Scheduled(fixedDelay = 2000)
    public void deliver() {
        if (!enabled || from.isBlank()) return;
        Instant now = Instant.now();
        var queued = challenges.findQueuedMails(now);
        for (AccountChallenge c : queued) {
            if (c.getMailPayload() == null) continue;
            try {
                var mime = sender.createMimeMessage();
                var message = new MimeMessageHelper(mime, "UTF-8");
                message.setFrom(from, "GoDrop | Bảo mật tài khoản");
                message.setTo(c.getTargetEmail());
                boolean link = "LINK".equals(c.getPurpose());
                message.setSubject(link ? "GoDrop - Xác minh email bảo mật" : "GoDrop - Khôi phục mật khẩu");
                message.setText("Mã xác minh GoDrop của bạn: " + decrypt(c.getMailPayload()) +
                        "\nMã có hiệu lực 10 phút kể từ lúc yêu cầu và chỉ dùng một lần. Không chia sẻ mã với người khác.\nNếu không yêu cầu thao tác này, bạn có thể bỏ qua email.");
                sender.send(mime);
                c.setMailStatus("SENT");
                c.setMailPayload(null);
                challenges.save(c);
            } catch (Exception ex) {
                c.setMailStatus("FAILED");
                c.setMailPayload(null);
                c.setConsumed(true);
                challenges.save(c);
                org.slf4j.LoggerFactory.getLogger(AccountMail.class)
                        .warn("Account email delivery failed; check SMTP configuration and connectivity.");
            }
        }
    }
}
