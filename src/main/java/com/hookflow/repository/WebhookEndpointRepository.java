package com.hookflow.repository;

import com.hookflow.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebhookEndpointRepository
        extends JpaRepository<WebhookEndpoint, Long> {

    List<WebhookEndpoint> findAllByUser_Id(Long userId);

    Optional<WebhookEndpoint> findByIdAndUser_Id(Long id, Long userId);

    List<WebhookEndpoint> findAllByUser_IdAndEventTypeAndActiveTrue(
            Long userId,
            String eventType
    );
}