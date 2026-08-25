package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.OrderRejection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;

public interface OrderRejectionRepository extends JpaRepository<OrderRejection, Long> {
    boolean existsByDeliveryRequestIdAndDriverId(Long requestId, Long driverId);
    long countByDriverIdAndPenaltyAppliedTrueAndRejectedAtAfter(Long driverId, Instant after);
}
