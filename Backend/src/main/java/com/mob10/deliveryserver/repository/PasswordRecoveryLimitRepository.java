package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.PasswordRecoveryLimit;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository cho PasswordRecoveryLimit — lưu trạng thái rate-limit
 * của các thao tác khôi phục mật khẩu / liên kết email.
 *
 * Chỉ dùng findById / save (kế thừa từ MongoRepository) nên không cần
 * khai báo thêm query method.
 */
public interface PasswordRecoveryLimitRepository extends MongoRepository<PasswordRecoveryLimit, String> {
}
