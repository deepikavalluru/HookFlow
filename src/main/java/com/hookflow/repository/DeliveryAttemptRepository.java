package com.hookflow.repository;

import com.hookflow.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {
}