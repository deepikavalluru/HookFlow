package com.hookflow.service;

import com.hookflow.dto.DeliveryAttemptResponse;
import com.hookflow.dto.DeliveryResponse;
import com.hookflow.dto.DeliveryStatsResponse;
import com.hookflow.entity.Delivery;
import com.hookflow.repository.DeliveryRepository;
import com.hookflow.repository.DeliveryAttemptRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final WebhookDeliveryService webhookDeliveryService;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           DeliveryAttemptRepository deliveryAttemptRepository,
                           WebhookDeliveryService webhookDeliveryService) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    public List<DeliveryResponse> getAllDeliveries(Long userId) {
        return deliveryRepository.findAllByUserId(userId);
    }

    public Optional<DeliveryResponse> getDeliveryById(
            Long deliveryId,
            Long userId
    ) {
        return deliveryRepository.findDeliveryByIdAndUserId(
                deliveryId,
                userId
        );
    }

    public List<DeliveryAttemptResponse> getDeliveryAttempts(Long deliveryId, Long userId) {

        if (deliveryRepository.findDeliveryByIdAndUserId(deliveryId, userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found");
        }

        return deliveryAttemptRepository.findAttemptsByDeliveryAndUser(deliveryId, userId);
    }

    public DeliveryStatsResponse getDeliveryStats(Long userId) {
        return deliveryRepository.getDeliveryStats(userId);
    }

    public void retryDelivery(Long deliveryId, Long userId) {

        Delivery delivery = deliveryRepository
                .findByIdAndEvent_User_Id(deliveryId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Delivery not found"
                ));

        if (!delivery.getStatus().equals("FAILED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only failed deliveries can be retried"
            );
        }

        delivery.setRetryCount(0);
        delivery.setNextRetryAt(null);
        delivery.setLastError(null);
        delivery.setStatus("PROCESSING");

        deliveryRepository.save(delivery);

        webhookDeliveryService.deliverWebhook(deliveryId);
    }
}