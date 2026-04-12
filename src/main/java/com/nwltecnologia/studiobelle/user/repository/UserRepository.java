package com.nwltecnologia.studiobelle.user.repository;

import com.nwltecnologia.studiobelle.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findAllByTenantId(String tenantId);
    Optional<User> findByIdAndTenantId(Long id, String tenantId);
}
