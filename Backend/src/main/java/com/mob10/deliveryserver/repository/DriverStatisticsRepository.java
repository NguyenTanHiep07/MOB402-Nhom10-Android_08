package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.DriverStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface DriverStatisticsRepository extends JpaRepository<DriverStatistics, Long> {
    List<DriverStatistics> findAllByReliabilityScoreLessThanOrderByReliabilityScoreAsc(BigDecimal threshold);
}
