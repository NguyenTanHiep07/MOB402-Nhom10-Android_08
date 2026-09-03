package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.Rating;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByDeliveryRequestId(Long deliveryRequestId);

    @EntityGraph(attributePaths = {"deliveryRequest", "client", "driver"})
    Optional<Rating> findByDeliveryRequestId(Long deliveryRequestId);

    long countByDriverId(Long driverId);

    @Query("select avg(r.stars) from Rating r where r.driver.id = :driverId")
    Double findAverageStarsByDriverId(@Param("driverId") Long driverId);
}
