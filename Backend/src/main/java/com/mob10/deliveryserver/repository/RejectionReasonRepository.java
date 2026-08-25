package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.RejectionReason;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RejectionReasonRepository extends JpaRepository<RejectionReason, String> {
    List<RejectionReason> findAllByActiveTrueOrderByCodeAsc();
}
