package com.hookflow.repository;

import com.hookflow.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}