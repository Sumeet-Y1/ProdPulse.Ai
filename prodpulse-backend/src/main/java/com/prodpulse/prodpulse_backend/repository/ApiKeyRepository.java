package com.prodpulse.prodpulse_backend.repository;

import com.prodpulse.prodpulse_backend.model.entity.ApiKey;
import com.prodpulse.prodpulse_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findByUser(User user);

    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);

    boolean existsByKeyValueAndActiveTrue(String keyValue);

    List<ApiKey> findByUserAndActiveTrue(User user);
}