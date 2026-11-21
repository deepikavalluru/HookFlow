package com.hookflow.repository;

import com.hookflow.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebhookEndpointRepository
        extends JpaRepository<WebhookEndpoint, Long> {

    // Get all webhooks belonging to a user
    List<WebhookEndpoint> findAllByUser_Id(Long userId);

    // Find webhook belonging to a user
    Optional<WebhookEndpoint> findByIdAndUser_Id(Long id, Long userId);

    // Find active, non-deleted endpoints for event delivery
    List<WebhookEndpoint> findAllByUser_IdAndEventTypeAndActiveTrueAndDeletedFalse(
            Long userId,
            String eventType
    );

    // Get all non-deleted webhooks
    List<WebhookEndpoint> findAllByUser_IdAndDeletedFalse(Long userId);

    // Find a specific non-deleted webhook belonging to a user
    Optional<WebhookEndpoint> findByIdAndUser_IdAndDeletedFalse(
            Long id,
            Long userId
    );

    // Count non-deleted webhooks
    long countByUser_IdAndDeletedFalse(Long userId);
}