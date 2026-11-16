package com.hookflow.repository;

import com.hookflow.dto.DeliveryAttemptResponse;
import com.hookflow.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryAttemptRepository
        extends JpaRepository<DeliveryAttempt, Long> {

    List<DeliveryAttempt> findAllByDelivery_IdOrderByAttemptedAtAsc(Long deliveryId);

    @Query("""
    SELECT new com.hookflow.dto.DeliveryAttemptResponse(
        a.id,
        a.attemptNumber,
        a.status,
        a.responseCode,
        a.errorMessage,
        a.attemptedAt
    )
    FROM DeliveryAttempt a
    WHERE a.delivery.id = :deliveryId
    AND a.delivery.event.user.id = :userId
    ORDER BY a.attemptNumber ASC
    """)
    List<DeliveryAttemptResponse> findAttemptsByDeliveryAndUser(
            @Param("deliveryId") Long deliveryId,
            @Param("userId") Long userId
    );
}