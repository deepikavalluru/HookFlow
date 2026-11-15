package com.hookflow.service;

import com.hookflow.entity.Delivery;
import com.hookflow.repository.DeliveryRepository;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
public class WebhookDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final WebClient webClient;

    public WebhookDeliveryService(
            DeliveryRepository deliveryRepository,
            WebClient webClient
    ) {
        this.deliveryRepository = deliveryRepository;
        this.webClient = webClient;
    }

    @Async
    public void deliverWebhook(Long deliveryId) {

        Delivery delivery = deliveryRepository
                .findWithDetailsById(deliveryId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found")
                );

        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        deliveryRepository.save(delivery);

        try {

            Integer responseCode = webClient.post()
                    .uri(delivery.getWebhookEndpoint().getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(delivery.getEvent().getPayload())
                    .exchangeToMono(response ->
                            response.releaseBody()
                                    .thenReturn(response.statusCode().value())
                    )
                    .block();

            delivery.setLastResponseCode(responseCode);

            delivery.setLastResponseCode(responseCode);

            if (responseCode >= 200 && responseCode < 300) {

                delivery.setStatus("DELIVERED");
                delivery.setLastError(null);
                delivery.setNextRetryAt(null);

            } else {

                delivery.setLastError("HTTP " + responseCode);

                if (isRetryableStatusCode(responseCode)) {
                    scheduleRetry(delivery);
                } else {
                    delivery.setStatus("FAILED");
                }
            }


        } catch (Exception exception) {

            delivery.setLastError(exception.getMessage());

            scheduleRetry(delivery);

        }

        deliveryRepository.save(delivery);
    }

    private void scheduleRetry(Delivery delivery) {

        int attempts = delivery.getAttemptCount();

        if (attempts >= 5) {
            delivery.setStatus("FAILED");
            delivery.setNextRetryAt(null);
            return;
        }

        int delay = switch (attempts) {
            case 1 -> 5;
            case 2 -> 10;
            case 3 -> 20;
            case 4 -> 40;
            default -> 0;
        };

        delivery.setStatus("RETRY_SCHEDULED");

        delivery.setNextRetryAt(
                LocalDateTime.now().plusSeconds(delay)
        );
    }

    private boolean isRetryableStatusCode(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }
}