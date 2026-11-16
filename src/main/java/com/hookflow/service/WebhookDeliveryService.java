package com.hookflow.service;

import com.hookflow.entity.Delivery;
import com.hookflow.repository.DeliveryRepository;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.hookflow.entity.DeliveryAttempt;
import com.hookflow.repository.DeliveryAttemptRepository;

import java.time.LocalDateTime;

@Service
public class WebhookDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final WebClient webClient;
    private final DeliveryAttemptRepository deliveryAttemptRepository;

    public WebhookDeliveryService(
            DeliveryRepository deliveryRepository,
            WebClient webClient,
            DeliveryAttemptRepository deliveryAttemptRepository
    ) {
        this.deliveryRepository = deliveryRepository;
        this.webClient = webClient;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
    }

    @Async
    public void deliverWebhook(Long deliveryId) {

        Delivery delivery = deliveryRepository
                .findWithDetailsById(deliveryId)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found")
                );

        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        delivery.setRetryCount(delivery.getRetryCount() + 1);

        deliveryRepository.save(delivery);

        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setAttemptNumber(delivery.getAttemptCount());
        attempt.setDelivery(delivery);

        try {

            Integer responseCode = webClient.post()
                    .uri(delivery.getWebhookEndpoint().getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(delivery.getEvent().getPayload())
                    .exchangeToMono(response -> {

                        int statusCode = response.statusCode().value();

                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> {
                                    attempt.setResponseBody(body);
                                    attempt.setResponseCode(statusCode);
                                    return statusCode;
                                });
                    })
                    .block();

            delivery.setLastResponseCode(responseCode);

            if (responseCode >= 200 && responseCode < 300) {

                attempt.setStatus("SUCCESS");

                delivery.setStatus("DELIVERED");
                delivery.setLastError(null);
                delivery.setNextRetryAt(null);

            } else {

                attempt.setStatus("FAILED");
                attempt.setErrorMessage("HTTP " + responseCode);

                delivery.setLastError("HTTP " + responseCode);

                if (isRetryableStatusCode(responseCode)) {
                    scheduleRetry(delivery);
                } else {
                    delivery.setStatus("FAILED");
                }
            }

        } catch (Exception exception) {

            String errorMessage = exception.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = exception.getClass().getSimpleName();
            }

            attempt.setStatus("FAILED");
            attempt.setErrorMessage(errorMessage);

            delivery.setLastError(errorMessage);

            scheduleRetry(delivery);
        }

        deliveryRepository.save(delivery);
        deliveryAttemptRepository.save(attempt);
    }

    private void scheduleRetry(Delivery delivery) {

        int attempts = delivery.getRetryCount();

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