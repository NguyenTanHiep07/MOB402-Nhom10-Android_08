package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findAllByDeliveryRequestIdOrderByTimestampAscIdAsc(Long requestId);
}
