package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends MongoRepository<Rating, Long> {
    @Query(value = "{'deliveryRequestId': ?0}", exists = true)
    boolean existsByDeliveryRequestId(Long deliveryRequestId);

    @Query(value = "{'deliveryRequestId': ?0}")
    Optional<Rating> findByDeliveryRequestId(Long deliveryRequestId);

    @Query(value = "{'driverId': ?0}", count = true)
    long countByDriverId(Long driverId);

    @Query(value = "{'driverId': ?0}")
    List<Rating> findAllByDriverId(Long driverId);

    default Double findAverageStarsByDriverId(Long driverId) {
        List<Rating> list = findAllByDriverId(driverId);
        if (list.isEmpty()) return null;
        return list.stream().mapToInt(Rating::getStars).average().orElse(0.0);
    }
}
