package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.DeliveryRequest;
import com.mob10.deliveryserver.domain.DeliveryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Long> {
    boolean existsByNote(String note);

    @EntityGraph(attributePaths = {"client", "deliveryPerson", "packages"})
    List<DeliveryRequest> findAllByClientIdOrderByCreatedAtDesc(Long clientId);

    @EntityGraph(attributePaths = {"client", "deliveryPerson", "packages"})
    List<DeliveryRequest> findAllByDeliveryPersonIdOrderByCreatedAtDesc(Long driverId);

    @EntityGraph(attributePaths = {"client", "deliveryPerson", "packages"})
    @Query("select distinct d from DeliveryRequest d where d.status = :status and d.id not in " +
           "(select r.deliveryRequest.id from OrderRejection r where r.driver.id = :driverId) order by d.createdAt desc")
    List<DeliveryRequest> findOpenForDriver(@Param("driverId") Long driverId, @Param("status") DeliveryStatus status);

    @EntityGraph(attributePaths = {"client", "deliveryPerson", "packages"})
    @Query("select distinct d from DeliveryRequest d order by d.createdAt desc")
    List<DeliveryRequest> findAllDetailed();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DeliveryRequest d where d.id = :id")
    Optional<DeliveryRequest> findByIdForUpdate(@Param("id") Long id);

    // Không clear persistence context tại đây: User/DriverStatistics đang được quản lý
    // trong cùng transaction và phải tiếp tục ghi availability/điểm thống kê sau Accept.
    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("update DeliveryRequest d set d.deliveryPerson = :driver, d.status = :acceptedStatus, d.updatedAt = CURRENT_TIMESTAMP " +
           "where d.id = :requestId and d.deliveryPerson is null and d.status = :waitingStatus")
    int assignAtomically(@Param("requestId") Long requestId, @Param("driver") com.mob10.deliveryserver.domain.User driver,
                         @Param("waitingStatus") DeliveryStatus waitingStatus, @Param("acceptedStatus") DeliveryStatus acceptedStatus);
}
