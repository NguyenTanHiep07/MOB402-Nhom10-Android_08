package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.DriverStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface DriverStatisticsRepository extends MongoRepository<DriverStatistics, Long> {
    @Query(value = "{'reliabilityScore': {'$lt': ?0}}", sort = "{'reliabilityScore': 1}")
    List<DriverStatistics> findAllByReliabilityScoreLessThanOrderByReliabilityScoreAsc(BigDecimal threshold);
}
