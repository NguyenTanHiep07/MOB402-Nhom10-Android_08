package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.StatusHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface StatusHistoryRepository extends MongoRepository<StatusHistory, Long> {
    @Query(value = "{'deliveryRequestId': ?0}", sort = "{'timestamp': 1, '_id': 1}")
    List<StatusHistory> findAllByDeliveryRequestIdOrderByTimestampAscIdAsc(Long requestId);
}
