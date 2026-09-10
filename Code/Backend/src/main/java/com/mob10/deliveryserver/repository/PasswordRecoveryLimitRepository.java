package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.PasswordRecoveryLimit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PasswordRecoveryLimitRepository extends MongoRepository<PasswordRecoveryLimit, String> {
}
