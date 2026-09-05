package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Persistent outbox: OTP is encrypted until SMTP accepts it, then only its HMAC remains. */
@Component
@EnableScheduling
public class AccountMail {
    private final JavaMailSender sender;
    private final JdbcTemplate db;
    private final TransactionTemplate tx;
    private final boolean enabled;
    private final String from;
    private final SecretKeySpec key;
    public AccountMail(JavaMailSender sender,JdbcTemplate db,PlatformTransactionManager manager,
                       @Value("${app.mail.enabled:false}") boolean enabled,@Value("${app.mail.from:}") String from,
                       @Value("${app.jwt.secret}") String secret) throws Exception {
        this.sender=sender; this.db=db; this.tx=new TransactionTemplate(manager); this.enabled=enabled; this.from=from.trim();
        this.key=new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(("account-mail:"+secret).getBytes(StandardCharsets.UTF_8)),"AES");
    }
    public void requireConfigured() {
        if(!enabled || from.isBlank()) throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"EMAIL_UNAVAILABLE","Dịch vụ email chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
    }
    public String encrypt(String code) {
        try {
            byte[] iv=new byte[12]; new SecureRandom().nextBytes(iv);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(128,iv));
            return Base64.getEncoder().encodeToString(iv)+":"+Base64.getEncoder().encodeToString(cipher.doFinal(code.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception ex) { throw new IllegalStateException("Mail encryption unavailable"); }
    }
    private String decrypt(String value) throws Exception {
        String[] parts=value.split(":"); Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,Base64.getDecoder().decode(parts[0])));
        return new String(cipher.doFinal(Base64.getDecoder().decode(parts[1])),StandardCharsets.UTF_8);
    }
    @Scheduled(fixedDelay=2000)
    public void deliver() {
        if(!enabled || from.isBlank()) return;
        tx.executeWithoutResult(status -> {
            db.update("UPDATE account_challenges SET mail_payload=null,consumed=true WHERE expires_at<now() AND consumed=false");
            var rows=db.queryForList("SELECT * FROM account_challenges WHERE mail_status='QUEUED' AND consumed=false AND expires_at>now() ORDER BY created_at LIMIT 1 FOR UPDATE SKIP LOCKED");
            if(rows.isEmpty()) return;
            var c=rows.getFirst();
            try {
                var mime=sender.createMimeMessage();
                var message=new MimeMessageHelper(mime,"UTF-8");
                message.setFrom(from,"GoDrop | Bảo mật tài khoản"); message.setTo(c.get("target_email").toString());
                boolean link="LINK".equals(c.get("purpose"));
                message.setSubject(link?"GoDrop - Xác minh email bảo mật":"GoDrop - Khôi phục mật khẩu");
                message.setText("Mã xác minh GoDrop của bạn: "+decrypt(c.get("mail_payload").toString())+"\nMã có hiệu lực 10 phút kể từ lúc yêu cầu và chỉ dùng một lần. Không chia sẻ mã với người khác.\nNếu không yêu cầu thao tác này, bạn có thể bỏ qua email.");
                sender.send(mime);
                db.update("UPDATE account_challenges SET mail_status='SENT',mail_payload=null WHERE id=?",c.get("id"));
            } catch(Exception ex) {
                // SMTP exceptions may contain addresses or credentials; never log their body.
                db.update("UPDATE account_challenges SET mail_status='FAILED',mail_payload=null,consumed=true WHERE id=?",c.get("id"));
                org.slf4j.LoggerFactory.getLogger(AccountMail.class).warn("Account email delivery failed; check SMTP configuration and connectivity.");
            }
        });
    }
}
