package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.OrderRejection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.Instant;
import java.util.List;

public interface OrderRejectionRepository extends MongoRepository<OrderRejection, Long> {
    @Query(value = "{'deliveryRequestId': ?0, 'driverId': ?1}", exists = true)
    boolean existsByDeliveryRequestIdAndDriverId(Long requestId, Long driverId);

    @Query(value = "{'driverId': ?0, 'penaltyApplied': true, 'rejectedAt': {'$gt': ?1}}", count = true)
    long countByDriverIdAndPenaltyAppliedTrueAndRejectedAtAfter(Long driverId, Instant after);

    List<OrderRejection> findAllByDriverId(Long driverId);
}
