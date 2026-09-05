package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.dto.AccountDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
public class AccountService {
    private final JdbcTemplate db;
    private final TransactionTemplate tx;
    private final PasswordEncoder passwords;
    private final AccountMail mail;
    private final byte[] secret;
    private final SecureRandom random = new SecureRandom();
    public AccountService(JdbcTemplate db, PlatformTransactionManager manager, PasswordEncoder passwords,
                          AccountMail mail, @Value("${app.jwt.secret}") String secret) {
        this.db=db; this.tx=new TransactionTemplate(manager); this.passwords=passwords;
        this.mail=mail; this.secret=secret.getBytes(StandardCharsets.UTF_8);
    }
    private String hash(String value) {
        try {
            Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret,"HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) { throw new IllegalStateException("Hash unavailable"); }
    }
    private String phone(String raw) {
        String value=raw.replaceAll("[\\s().-]","");
        if(value.startsWith("+84")) value="0"+value.substring(3);
        if(!value.matches("0[0-9]{9,10}")) throw bad("Số điện thoại cần 10–11 chữ số, bắt đầu bằng 0.");
        return value;
    }
    // Separate committed transaction: unsuccessful authentication also consumes the quota.
    private void limit(String key, int max, int cooldown) {
        Boolean allowed=tx.execute(status -> {
            Instant now=Instant.now();
            db.update("INSERT INTO password_recovery_limits(key,window_start,last_request,count) VALUES (?,?,?,0) ON CONFLICT DO NOTHING",
                    key,Timestamp.from(now),Timestamp.from(Instant.EPOCH));
            var row=db.queryForMap("SELECT * FROM password_recovery_limits WHERE key=? FOR UPDATE",key);
            Instant start=((Timestamp)row.get("window_start")).toInstant();
            int count=((Number)row.get("count")).intValue();
            if(now.isAfter(start.plusSeconds(3600))) { start=now; count=0; }
            if(count>=max || (cooldown>0 && now.isBefore(((Timestamp)row.get("last_request")).toInstant().plusSeconds(cooldown)))) return false;
            db.update("UPDATE password_recovery_limits SET window_start=?,last_request=?,count=? WHERE key=?",Timestamp.from(start),Timestamp.from(now),count+1,key);
            return true;
        });
        if(!Boolean.TRUE.equals(allowed)) throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"RATE_LIMIT","Đã đạt giới hạn thao tác. Gửi mã cách nhau 60 giây, tối đa 3 lần mỗi giờ; hãy thử lại sau.");
    }
    private Map<String,Object> user(long id, boolean lock) {
        var rows=db.queryForList("SELECT * FROM users WHERE id=? AND active=true"+(lock?" FOR UPDATE":""),id);
        if(rows.isEmpty()) throw new ApiException(HttpStatus.UNAUTHORIZED,"SESSION_INVALID","Phiên đăng nhập không hợp lệ.");
        return rows.getFirst();
    }
    private void reauth(Map<String,Object> user,String password) {
        if(password.getBytes(StandardCharsets.UTF_8).length>72) throw bad("Mật khẩu hiện tại không hợp lệ.");
        if(!passwords.matches(password,user.get("password_hash").toString())) throw bad("Mật khẩu hiện tại không đúng.");
    }
    public Profile profile(long id) { return profile(user(id,false)); }
    public Message emailStatus(long id) {
        var rows=db.queryForList("SELECT mail_status,consumed,expires_at FROM account_challenges WHERE user_id=? AND purpose='LINK' ORDER BY created_at DESC LIMIT 1",id);
        if(rows.isEmpty()) return new Message("Chưa yêu cầu mã xác minh email.");
        var c=rows.getFirst();
        if("FAILED".equals(c.get("mail_status"))) return new Message("Không gửi được email. Kiểm tra cấu hình Gmail/kết nối backend, rồi gửi mã mới sau thời gian chờ.");
        if(Boolean.TRUE.equals(c.get("consumed")) || !((Timestamp)c.get("expires_at")).toInstant().isAfter(Instant.now())) return new Message("Yêu cầu đã kết thúc. Nếu chưa xác minh thành công, hãy gửi mã mới.");
        return new Message("SENT".equals(c.get("mail_status")) ? "Máy chủ thư đã tiếp nhận email. Kiểm tra hộp thư và thư rác." : "Đang gửi email xác minh…");
    }
    private Profile profile(Map<String,Object> u) {
        return new Profile(((Number)u.get("id")).longValue(),(String)u.get("username"),(String)u.get("full_name"),
                (String)u.get("phone_number"),(String)u.get("role"),(String)u.get("license_plate"),
                (String)u.get("recovery_email"),u.get("email_verified_at")!=null,(String)u.get("avatar_base64"));
    }
    private String avatar(String encoded) {
        if(encoded==null || encoded.isEmpty()) return null;
        try {
            byte[] bytes=Base64.getDecoder().decode(encoded);
            if(bytes.length>160000) throw bad("Ảnh quá lớn. Chọn ảnh nhỏ hơn.");
            try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers=ImageIO.getImageReaders(input);
                if(!readers.hasNext()) throw bad("Ảnh không hợp lệ.");
                var reader=readers.next();
                try {
                    reader.setInput(input);
                    int w=reader.getWidth(0),h=reader.getHeight(0);
                    if(w<1 || h<1 || w>1024 || h>1024) throw bad("Ảnh cần có kích thước tối đa 1024 × 1024.");
                    var image=reader.read(0);
                    var rgb=new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_INT_RGB);
                    var graphics=rgb.createGraphics(); graphics.drawImage(image,0,0,null); graphics.dispose();
                    var output=new ByteArrayOutputStream(); ImageIO.write(rgb,"jpg",output);
                    if(output.size()>160000) throw bad("Ảnh quá lớn. Chọn ảnh nhỏ hơn.");
                    return Base64.getEncoder().encodeToString(output.toByteArray());
                } finally { reader.dispose(); }
            }
        } catch(ApiException ex) { throw ex; }
        catch(Exception ex) { throw bad("Không đọc được ảnh đại diện. Hãy chọn ảnh khác."); }
    }
    public Profile edit(long id,Edit body) {
        limit("edit:"+id,20,0);
        String number=phone(body.phoneNumber()), image=avatar(body.avatarBase64());
        try {
            return tx.execute(status -> {
                var u=user(id,true); reauth(u,body.currentPassword());
                if(!List.of("CLIENT","DELIVERY").contains(u.get("role"))) throw bad("Chỉ khách hàng và tài xế được sửa hồ sơ tại đây.");
                if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users WHERE id<>? AND (username=? OR phone_number=?))",Boolean.class,id,body.username(),number)))
                    throw bad("Tên đăng nhập hoặc số điện thoại đã được sử dụng.");
                db.update("UPDATE users SET username=?,full_name=?,phone_number=?,avatar_base64=? WHERE id=?",body.username(),body.fullName().trim(),number,image,id);
                if(!number.equals(u.get("phone_number"))) db.update("UPDATE account_challenges SET consumed=true,mail_payload=null WHERE user_id=? AND purpose='RESET'",id);
                return profile(id);
            });
        } catch(DataIntegrityViolationException ex) { throw bad("Tên đăng nhập hoặc số điện thoại đã được sử dụng."); }
    }
    private void challenge(Map<String,Object> u,String purpose,String email) {
        long id=((Number)u.get("id")).longValue();
        db.update("UPDATE account_challenges SET consumed=true,mail_payload=null WHERE user_id=? AND purpose=? AND consumed=false",id,purpose);
        UUID challengeId=UUID.randomUUID();
        String code=String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000));
        db.update("INSERT INTO account_challenges(id,user_id,purpose,target_email,phone_snapshot,credential_version,code_hash,mail_payload,expires_at) VALUES (?,?,?,?,?,?,?,?,?)",
                challengeId,id,purpose,email,u.get("phone_number"),u.get("credential_version"),hash(challengeId+":"+code),mail.encrypt(code),Timestamp.from(Instant.now().plusSeconds(600)));
    }
    public void link(long id,Link body) {
        mail.requireConfigured(); limit("reauth:"+id,20,0);
        reauth(user(id,false),body.currentPassword()); limit("link:"+id,3,60);
        String email=body.email().trim().toLowerCase(Locale.ROOT);
        tx.executeWithoutResult(status -> {
            var u=user(id,true); reauth(u,body.currentPassword());
            if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM users WHERE lower(recovery_email)=? AND id<>?)",Boolean.class,email,id))) throw bad("Không thể liên kết email này. Hãy dùng email khác.");
            challenge(u,"LINK",email);
        });
    }
    private Map<String,Object> validChallenge(long id,String purpose,String code,Map<String,Object> u) {
        var rows=db.queryForList("SELECT * FROM account_challenges WHERE user_id=? AND purpose=? AND consumed=false ORDER BY created_at DESC LIMIT 1 FOR UPDATE",id,purpose);
        if(rows.isEmpty()) return null;
        var c=rows.getFirst();
        if(((Number)c.get("attempts")).intValue()>=5 || !((Timestamp)c.get("expires_at")).toInstant().isAfter(Instant.now())
                || !Objects.equals(c.get("credential_version"),u.get("credential_version")) || !"SENT".equals(c.get("mail_status"))) return null;
        db.update("UPDATE account_challenges SET attempts=attempts+1 WHERE id=?",c.get("id"));
        return MessageDigest.isEqual(hash(c.get("id")+":"+code).getBytes(StandardCharsets.US_ASCII),c.get("code_hash").toString().getBytes(StandardCharsets.US_ASCII))?c:null;
    }
    public Profile verify(long id,Verify body) {
        limit("reauth:"+id,20,0);
        try {
            Boolean ok=tx.execute(status -> {
                var u=user(id,true); reauth(u,body.currentPassword());
                var c=validChallenge(id,"LINK",body.code(),u); if(c==null) return false;
                db.update("UPDATE users SET recovery_email=?,email_verified_at=now() WHERE id=?",c.get("target_email"),id);
                db.update("UPDATE account_challenges SET consumed=true,mail_payload=null WHERE user_id=?",id);
                return true;
            });
            if(!Boolean.TRUE.equals(ok)) throw invalidCode();
            return profile(id);
        } catch(DataIntegrityViolationException ex) { throw bad("Email đã được liên kết với tài khoản khác. Hãy dùng email khác."); }
    }
    public void request(String raw,String ip) {
        mail.requireConfigured(); String number=phone(raw);
        limit("reset-ip:"+hash(ip),20,0); limit("reset-phone:"+hash(number),3,60);
        tx.executeWithoutResult(status -> {
            var rows=db.queryForList("SELECT * FROM users WHERE phone_number=? AND active=true AND email_verified_at IS NOT NULL AND recovery_email IS NOT NULL FOR UPDATE",number);
            if(!rows.isEmpty()) challenge(rows.getFirst(),"RESET",(String)rows.getFirst().get("recovery_email"));
        });
    }
    public void reset(Reset body,String ip) {
        String number=phone(body.phoneNumber()), password=body.newPassword();
        if(password.getBytes(StandardCharsets.UTF_8).length>72 || !password.matches("(?s).*\\p{L}.*") || !password.matches("(?s).*\\p{N}.*"))
            throw bad("Mật khẩu cần 12–64 ký tự, có chữ và số, tối đa 72 byte UTF-8.");
        limit("confirm-ip:"+hash(ip),60,0);
        String result=tx.execute(status -> {
            var rows=db.queryForList("SELECT * FROM users WHERE phone_number=? AND active=true FOR UPDATE",number);
            if(rows.isEmpty()) return "INVALID";
            var u=rows.getFirst(); long id=((Number)u.get("id")).longValue();
            var c=validChallenge(id,"RESET",body.code(),u);
            if(c==null || !Objects.equals(c.get("phone_snapshot"),number) || u.get("email_verified_at")==null || !Objects.equals(c.get("target_email"),u.get("recovery_email"))) return "INVALID";
            if(passwords.matches(password,u.get("password_hash").toString())) return "SAME";
            db.update("UPDATE users SET password_hash=?,credential_version=credential_version+1 WHERE id=?",passwords.encode(password),id);
            db.update("UPDATE account_challenges SET consumed=true,mail_payload=null WHERE user_id=?",id);
            return "OK";
        });
        if("SAME".equals(result)) throw bad("Mật khẩu mới phải khác mật khẩu cũ.");
        if(!"OK".equals(result)) throw invalidCode();
    }
    private ApiException invalidCode() { return bad("Mã không đúng, hết hạn, đã dùng hoặc đã nhập sai 5 lần. Hãy kiểm tra hoặc gửi mã mới."); }
    private ApiException bad(String message) { return new ApiException(HttpStatus.BAD_REQUEST,"ACCOUNT_INVALID",message); }
}
