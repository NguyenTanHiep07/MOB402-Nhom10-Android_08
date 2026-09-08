package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.AccountChallenge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.Instant;
import java.util.List;

public interface AccountChallengeRepository extends MongoRepository<AccountChallenge, String> {
    @Query(value = "{'userId': ?0, 'purpose': ?1, 'consumed': false}", sort = "{'createdAt': -1}")
    List<AccountChallenge> findByUserIdAndPurposeAndConsumedFalseOrderByCreatedAtDesc(Long userId, String purpose);

    @Query(value = "{'userId': ?0, 'purpose': ?1}", sort = "{'createdAt': -1}")
    List<AccountChallenge> findByUserIdAndPurposeOrderByCreatedAtDesc(Long userId, String purpose);

    @Query(value = "{'mailStatus': 'QUEUED', 'consumed': false, 'expiresAt': {'$gt': ?0}}", sort = "{'createdAt': 1}")
    List<AccountChallenge> findQueuedMails(Instant now);
}
