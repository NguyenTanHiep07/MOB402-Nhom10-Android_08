package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.DeliveryRequest;
import com.mob10.deliveryserver.domain.DeliveryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DeliveryRequestRepository extends MongoRepository<DeliveryRequest, Long> {
    boolean existsByNote(String note);

    @Query(value = "{'client.id': ?0}", sort = "{'createdAt': -1}")
    List<DeliveryRequest> findAllByClientIdOrderByCreatedAtDesc(Long clientId);

    @Query(value = "{'deliveryPerson.id': ?0}", sort = "{'createdAt': -1}")
    List<DeliveryRequest> findAllByDeliveryPersonIdOrderByCreatedAtDesc(Long driverId);

    @Query(value = "{'status': ?1, 'deliveryPerson': null, '_id': {'$nin': ?2}}", sort = "{'createdAt': -1}")
    List<DeliveryRequest> findOpenForDriverNotIn(Long driverId, DeliveryStatus status, List<Long> rejectedIds);

    @Query(value = "{'status': ?0, 'deliveryPerson': null}", sort = "{'createdAt': -1}")
    List<DeliveryRequest> findAllByStatusAndDeliveryPersonIsNullOrderByCreatedAtDesc(DeliveryStatus status);

    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<DeliveryRequest> findAllDetailed();

    default Optional<DeliveryRequest> findByIdForUpdate(Long id) { return findById(id); }
}
