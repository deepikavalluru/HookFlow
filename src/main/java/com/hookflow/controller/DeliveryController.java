package com.hookflow.controller;

import com.hookflow.dto.DeliveryAttemptResponse;
import com.hookflow.dto.DeliveryResponse;
import com.hookflow.dto.DeliveryStatsResponse;
import com.hookflow.service.DeliveryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries(
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                deliveryService.getAllDeliveries(userId)
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<DeliveryStatsResponse> getDeliveryStats(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                deliveryService.getDeliveryStats(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return deliveryService.getDeliveryById(id, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<List<DeliveryAttemptResponse>> getDeliveryAttempts(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                deliveryService.getDeliveryAttempts(id, userId)
        );
    }
}