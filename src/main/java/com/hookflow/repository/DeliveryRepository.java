package com.hookflow.repository;

import com.hookflow.dto.DeliveryStatsResponse;
import com.hookflow.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hookflow.dto.DeliveryResponse;

import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("""
        SELECT d FROM Delivery d
        JOIN FETCH d.event
        JOIN FETCH d.webhookEndpoint
        WHERE d.id = :deliveryId
        """)
    Optional<Delivery> findWithDetailsById(
            @Param("deliveryId") Long deliveryId
    );

    List<Delivery> findAllByStatusAndNextRetryAtLessThanEqual(
            String status,
            LocalDateTime currentTime
    );

    @Query("""
    SELECT new com.hookflow.dto.DeliveryResponse(
        d.id,
        d.event.id,
        d.webhookEndpoint.id,
        d.status,
        d.attemptCount,
        d.lastResponseCode,
        d.lastError,
        d.createdAt
    )
    FROM Delivery d
    WHERE d.event.user.id = :userId
    ORDER BY d.createdAt DESC
    """)
    List<DeliveryResponse> findAllByUserId(
            @Param("userId") Long userId
    );

    @Query("""
    SELECT new com.hookflow.dto.DeliveryResponse(
        d.id,
        d.event.id,
        d.webhookEndpoint.id,
        d.status,
        d.attemptCount,
        d.lastResponseCode,
        d.lastError,
        d.createdAt
    )
    FROM Delivery d
    WHERE d.id = :deliveryId
    AND d.event.user.id = :userId
    """)
    Optional<DeliveryResponse> findDeliveryByIdAndUserId(
            @Param("deliveryId") Long deliveryId,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT new com.hookflow.dto.DeliveryStatsResponse(
        COUNT(d),
        SUM(CASE WHEN d.status = 'DELIVERED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN d.status = 'FAILED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN d.status IN ('PENDING', 'RETRY_SCHEDULED')
            THEN 1 ELSE 0 END)
    )
    FROM Delivery d
    WHERE d.event.user.id = :userId
    """)
    DeliveryStatsResponse getDeliveryStats(
            @Param("userId") Long userId
    );

}