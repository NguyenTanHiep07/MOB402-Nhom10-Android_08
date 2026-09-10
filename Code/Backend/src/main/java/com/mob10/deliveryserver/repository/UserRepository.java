package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, Long> {
    default Optional<User> findByIdForUpdate(Long id) { return findById(id); }
    Optional<User> findByUsername(String username);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findAllByRoleOrderByIdAsc(Role role);
}
