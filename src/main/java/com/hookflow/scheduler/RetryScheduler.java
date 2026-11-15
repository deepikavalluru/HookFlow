package com.hookflow.scheduler;

import com.hookflow.entity.Delivery;
import com.hookflow.repository.DeliveryRepository;
import com.hookflow.service.WebhookDeliveryService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RetryScheduler {

    private final DeliveryRepository deliveryRepository;
    private final WebhookDeliveryService webhookDeliveryService;

    public RetryScheduler(
            DeliveryRepository deliveryRepository,
            WebhookDeliveryService webhookDeliveryService
    ) {
        this.deliveryRepository = deliveryRepository;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @Scheduled(fixedDelay = 1000)
    public void processRetries() {

        List<Delivery> deliveries =
                deliveryRepository.findAllByStatusAndNextRetryAtLessThanEqual(
                        "RETRY_SCHEDULED",
                        LocalDateTime.now()
                );

        for (Delivery delivery : deliveries) {

            delivery.setStatus("PROCESSING");
            deliveryRepository.save(delivery);

            webhookDeliveryService.deliverWebhook(delivery.getId());
        }
    }
}