package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.DriverRegistrationRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DriverRegistrationRequestRepository extends MongoRepository<DriverRegistrationRequest, Long> {
    List<DriverRegistrationRequest> findAllByStatusOrderByIdDesc(String status);
    boolean existsByUserIdAndStatus(Long userId, String status);
}
