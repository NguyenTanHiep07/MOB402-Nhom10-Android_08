package com.mob10.deliveryserver.repository;

import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findAllByRoleOrderByIdAsc(Role role);
}
