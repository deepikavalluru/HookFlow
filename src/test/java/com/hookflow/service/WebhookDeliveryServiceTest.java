package com.hookflow.service;

import com.hookflow.entity.Delivery;
import com.hookflow.entity.DeliveryAttempt;
import com.hookflow.entity.Event;
import com.hookflow.entity.WebhookEndpoint;

import com.hookflow.repository.DeliveryRepository;
import com.hookflow.repository.DeliveryAttemptRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryAttemptRepository deliveryAttemptRepository;

    @Test
    void shouldMarkDeliveryAsDeliveredWhenResponseIs200() {

        // Arrange

        Long deliveryId = 10L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("PENDING");
        delivery.setAttemptCount(0);
        delivery.setRetryCount(0);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        org.springframework.web.reactive.function.client.ClientResponse
                                .create(HttpStatus.OK)
                                .body("Webhook received")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act

        service.deliverWebhook(deliveryId);

        // Assert

        assertEquals("DELIVERED", delivery.getStatus());
        assertEquals(1, delivery.getAttemptCount());
        assertEquals(1, delivery.getRetryCount());
        assertEquals(200, delivery.getLastResponseCode());
        assertNull(delivery.getLastError());
        assertNull(delivery.getNextRetryAt());

        verify(deliveryRepository, times(2)).save(delivery);

        verify(deliveryAttemptRepository).save(
                argThat(attempt ->
                        attempt.getStatus().equals("SUCCESS")
                                && attempt.getResponseCode() == 200
                                && attempt.getResponseBody().equals("Webhook received")
                                && attempt.getAttemptNumber() == 1
                )
        );
    }

    @Test
    void shouldScheduleRetryWhenServerReturns500() {

        // Arrange
        Long deliveryId = 11L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("PENDING");
        delivery.setAttemptCount(0);
        delivery.setRetryCount(0);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Internal Server Error")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act
        service.deliverWebhook(deliveryId);

        // Assert
        assertEquals("RETRY_SCHEDULED", delivery.getStatus());
        assertEquals(1, delivery.getRetryCount());
        assertEquals("HTTP 500", delivery.getLastError());

        assertNotNull(delivery.getNextRetryAt());

        assertTrue(
                !delivery.getNextRetryAt().isBefore(
                        java.time.LocalDateTime.now().plusSeconds(4)
                )
        );

        verify(deliveryAttemptRepository).save(
                argThat(attempt ->
                        "FAILED".equals(attempt.getStatus())
                                && Integer.valueOf(500).equals(attempt.getResponseCode())
                )
        );
    }

    @Test
    void shouldNotRetryWhenServerReturns400() {

        // Arrange
        Long deliveryId = 12L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("PENDING");
        delivery.setAttemptCount(0);
        delivery.setRetryCount(0);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse.create(HttpStatus.BAD_REQUEST)
                                .body("Bad Request")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act
        service.deliverWebhook(deliveryId);

        // Assert
        assertEquals("FAILED", delivery.getStatus());
        assertEquals(400, delivery.getLastResponseCode());
        assertEquals("HTTP 400", delivery.getLastError());
        assertNull(delivery.getNextRetryAt());

        verify(deliveryAttemptRepository).save(
                argThat(attempt ->
                        "FAILED".equals(attempt.getStatus())
                                && Integer.valueOf(400).equals(attempt.getResponseCode())
                )
        );
    }

    @Test
    void shouldScheduleRetryWhenServerReturns429() {

        // Arrange
        Long deliveryId = 13L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("PENDING");
        delivery.setAttemptCount(0);
        delivery.setRetryCount(0);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse.create(HttpStatus.TOO_MANY_REQUESTS)
                                .body("Too Many Requests")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act
        service.deliverWebhook(deliveryId);

        // Assert
        assertEquals("RETRY_SCHEDULED", delivery.getStatus());
        assertEquals(429, delivery.getLastResponseCode());
        assertEquals("HTTP 429", delivery.getLastError());
        assertNotNull(delivery.getNextRetryAt());

        verify(deliveryAttemptRepository).save(
                argThat(attempt ->
                        "FAILED".equals(attempt.getStatus())
                                && Integer.valueOf(429).equals(attempt.getResponseCode())
                )
        );
    }

    @Test
    void shouldScheduleRetryWhenConnectionFails() {

        // Arrange
        Long deliveryId = 14L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("PENDING");
        delivery.setAttemptCount(0);
        delivery.setRetryCount(0);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.error(new RuntimeException("Connection refused"));

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act
        service.deliverWebhook(deliveryId);

        // Assert
        assertEquals("RETRY_SCHEDULED", delivery.getStatus());
        assertEquals(1, delivery.getRetryCount());
        assertEquals("Connection refused", delivery.getLastError());
        assertNotNull(delivery.getNextRetryAt());

        verify(deliveryAttemptRepository).save(
                argThat(attempt ->
                        "FAILED".equals(attempt.getStatus())
                                && "Connection refused".equals(attempt.getErrorMessage())
                )
        );
    }

    @Test
    void shouldMarkDeliveryAsFailedAfterMaximumAttempts() {

        // Arrange
        Long deliveryId = 15L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("RETRY_SCHEDULED");
        delivery.setAttemptCount(4);
        delivery.setRetryCount(4);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Internal Server Error")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act
        service.deliverWebhook(deliveryId);

        // Assert
        assertEquals(5, delivery.getRetryCount());
        assertEquals(5, delivery.getAttemptCount());
        assertEquals("FAILED", delivery.getStatus());
        assertNull(delivery.getNextRetryAt());

        verify(deliveryAttemptRepository).save(
                argThat(attempt ->
                        "FAILED".equals(attempt.getStatus())
                                && Integer.valueOf(500).equals(attempt.getResponseCode())
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDeliveryDoesNotExist() {

        // Arrange
        Long deliveryId = 999L;

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.empty());

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                WebClient.builder().build(),
                deliveryAttemptRepository
        );

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.deliverWebhook(deliveryId)
        );

        assertEquals("Delivery not found", exception.getMessage());

        verify(deliveryRepository, never()).save(any(Delivery.class));

        verifyNoInteractions(deliveryAttemptRepository);
    }

    @Test
    void shouldScheduleSecondRetryAfter10Seconds() {

        // Arrange
        Long deliveryId = 16L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("RETRY_SCHEDULED");
        delivery.setAttemptCount(1);
        delivery.setRetryCount(1);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Internal Server Error")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act
        service.deliverWebhook(deliveryId);

        // Assert
        assertEquals(2, delivery.getRetryCount());
        assertEquals("RETRY_SCHEDULED", delivery.getStatus());

        assertNotNull(delivery.getNextRetryAt());

        assertTrue(
                !delivery.getNextRetryAt().isBefore(
                        java.time.LocalDateTime.now().plusSeconds(9)
                )
        );

        assertTrue(
                !delivery.getNextRetryAt().isAfter(
                        java.time.LocalDateTime.now().plusSeconds(11)
                )
        );
    }

    @Test
    void shouldScheduleThirdRetryAfter20Seconds() {

        // Arrange
        Long deliveryId = 17L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("RETRY_SCHEDULED");
        delivery.setAttemptCount(2);
        delivery.setRetryCount(2);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Internal Server Error")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        var before = java.time.LocalDateTime.now();

        // Act
        service.deliverWebhook(deliveryId);

        var after = java.time.LocalDateTime.now();

        // Assert
        assertEquals(3, delivery.getRetryCount());
        assertEquals("RETRY_SCHEDULED", delivery.getStatus());

        assertFalse(delivery.getNextRetryAt().isBefore(
                before.plusSeconds(20)
        ));

        assertFalse(delivery.getNextRetryAt().isAfter(
                after.plusSeconds(20)
        ));
    }

    @Test
    void shouldScheduleFourthRetryAfter40Seconds() {

        // Arrange
        Long deliveryId = 18L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");

        Delivery delivery = new Delivery();
        delivery.setStatus("RETRY_SCHEDULED");
        delivery.setAttemptCount(3);
        delivery.setRetryCount(3);
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        ExchangeFunction exchangeFunction = request ->
                Mono.just(
                        ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Internal Server Error")
                                .build()
                );

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        var before = java.time.LocalDateTime.now();

        // Act
        service.deliverWebhook(deliveryId);

        var after = java.time.LocalDateTime.now();

        // Assert
        assertEquals(4, delivery.getRetryCount());
        assertEquals("RETRY_SCHEDULED", delivery.getStatus());

        assertFalse(delivery.getNextRetryAt().isBefore(
                before.plusSeconds(40)
        ));

        assertFalse(delivery.getNextRetryAt().isAfter(
                after.plusSeconds(40)
        ));
    }

    @Test
    void shouldNotRetryDeliveryWhenWebhookIsDeleted() {

        // Arrange
        Long deliveryId = 19L;

        Event event = new Event();
        event.setPayload("{\"message\":\"Hello\"}");

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl("https://example.com/webhook");
        endpoint.setActive(true);
        endpoint.setDeleted(true);

        Delivery delivery = new Delivery();
        delivery.setStatus("RETRY_SCHEDULED");
        delivery.setAttemptCount(1);
        delivery.setRetryCount(1);
        delivery.setNextRetryAt(LocalDateTime.now().plusSeconds(5));
        delivery.setEvent(event);
        delivery.setWebhookEndpoint(endpoint);

        when(deliveryRepository.findWithDetailsById(deliveryId))
                .thenReturn(Optional.of(delivery));

        AtomicInteger requestCount = new AtomicInteger(0);

        ExchangeFunction exchangeFunction = request -> {
            requestCount.incrementAndGet();

            return Mono.just(
                    ClientResponse.create(HttpStatus.OK)
                            .body("Webhook received")
                            .build()
            );
        };

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        WebhookDeliveryService service = new WebhookDeliveryService(
                deliveryRepository,
                webClient,
                deliveryAttemptRepository
        );

        // Act
        service.deliverWebhook(deliveryId);

        // Assert
        assertEquals("FAILED", delivery.getStatus());
        assertEquals("Webhook endpoint has been deleted",
                delivery.getLastError());

        assertNull(delivery.getNextRetryAt());

        // Retry counters must remain unchanged
        assertEquals(1, delivery.getAttemptCount());
        assertEquals(1, delivery.getRetryCount());

        // No HTTP request should be sent
        assertEquals(0, requestCount.get());

        // Delivery should be saved
        verify(deliveryRepository).save(delivery);

        // No new delivery attempt should be recorded
        verifyNoInteractions(deliveryAttemptRepository);
    }
}