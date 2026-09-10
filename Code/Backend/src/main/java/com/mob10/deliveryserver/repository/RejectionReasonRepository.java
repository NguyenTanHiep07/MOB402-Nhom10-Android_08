package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.RejectionReason;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface RejectionReasonRepository extends MongoRepository<RejectionReason, String> {
    @Query(value = "{'active': true}", sort = "{'code': 1}")
    List<RejectionReason> findAllByActiveTrueOrderByCodeAsc();
}
