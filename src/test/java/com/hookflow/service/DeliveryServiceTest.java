package com.hookflow.service;

import com.hookflow.dto.DeliveryResponse;
import com.hookflow.dto.DeliveryStatsResponse;
import com.hookflow.entity.Delivery;
import com.hookflow.repository.DeliveryRepository;
import com.hookflow.repository.DeliveryAttemptRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryAttemptRepository deliveryAttemptRepository;

    @Mock
    private WebhookDeliveryService webhookDeliveryService;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    void shouldReturnAllDeliveriesForUser() {

        // Arrange
        Long userId = 1L;

        DeliveryResponse response = mock(DeliveryResponse.class);
        List<DeliveryResponse> expected = List.of(response);

        when(deliveryRepository.findAllByUserId(userId))
                .thenReturn(expected);

        // Act
        List<DeliveryResponse> actual =
                deliveryService.getAllDeliveries(userId);

        // Assert
        assertEquals(expected, actual);

        verify(deliveryRepository).findAllByUserId(userId);
    }

    @Test
    void shouldReturnDeliveryByIdForUser() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 1L;

        DeliveryResponse response = mock(DeliveryResponse.class);

        when(deliveryRepository.findDeliveryByIdAndUserId(deliveryId, userId))
                .thenReturn(java.util.Optional.of(response));

        // Act
        var actual = deliveryService.getDeliveryById(deliveryId, userId);

        // Assert
        assertEquals(java.util.Optional.of(response), actual);

        verify(deliveryRepository).findDeliveryByIdAndUserId(deliveryId, userId);
    }

    @Test
    void shouldReturnEmptyWhenDeliveryDoesNotBelongToUser() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 2L;

        when(deliveryRepository.findDeliveryByIdAndUserId(deliveryId, userId))
                .thenReturn(java.util.Optional.empty());

        // Act
        var actual = deliveryService.getDeliveryById(deliveryId, userId);

        // Assert
        assertEquals(java.util.Optional.empty(), actual);

        verify(deliveryRepository).findDeliveryByIdAndUserId(deliveryId, userId);
    }

    @Test
    void shouldReturnDeliveryAttemptsForUser() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 1L;

        when(deliveryRepository.findDeliveryByIdAndUserId(deliveryId, userId))
                .thenReturn(java.util.Optional.of(mock(DeliveryResponse.class)));

        when(deliveryAttemptRepository.findAttemptsByDeliveryAndUser(deliveryId, userId))
                .thenReturn(List.of());

        // Act
        var actual = deliveryService.getDeliveryAttempts(deliveryId, userId);

        // Assert
        assertEquals(0, actual.size());

        verify(deliveryRepository).findDeliveryByIdAndUserId(deliveryId, userId);
        verify(deliveryAttemptRepository).findAttemptsByDeliveryAndUser(deliveryId, userId);
    }

    @Test
    void shouldThrowNotFoundWhenDeliveryDoesNotBelongToUser() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 2L;

        when(deliveryRepository.findDeliveryByIdAndUserId(deliveryId, userId))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> deliveryService.getDeliveryAttempts(deliveryId, userId)
        );

        assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(deliveryAttemptRepository, never())
                .findAttemptsByDeliveryAndUser(deliveryId, userId);
    }

    @Test
    void shouldReturnDeliveryStatsForUser() {

        // Arrange
        Long userId = 1L;

        DeliveryStatsResponse expected =
                new DeliveryStatsResponse(10L, 6L, 3L, 1L);

        when(deliveryRepository.getDeliveryStats(userId))
                .thenReturn(expected);

        // Act
        DeliveryStatsResponse actual =
                deliveryService.getDeliveryStats(userId);

        // Assert
        assertEquals(expected, actual);

        verify(deliveryRepository).getDeliveryStats(userId);
    }

    @Test
    void shouldSuccessfullyRetryFailedDelivery() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 1L;

        Delivery delivery = new Delivery();

        delivery.setStatus("FAILED");
        delivery.setRetryCount(3);
        delivery.setNextRetryAt(java.time.LocalDateTime.now());
        delivery.setLastError("HTTP 500");

        when(deliveryRepository.findByIdAndEvent_User_Id(deliveryId, userId))
                .thenReturn(java.util.Optional.of(delivery));

        // Act
        deliveryService.retryDelivery(deliveryId, userId);

        // Assert
        assertEquals("PROCESSING", delivery.getStatus());
        assertEquals(0, delivery.getRetryCount());
        assertEquals(null, delivery.getNextRetryAt());
        assertEquals(null, delivery.getLastError());

        verify(deliveryRepository).save(delivery);

        verify(webhookDeliveryService).deliverWebhook(deliveryId);
    }

    @Test
    void shouldThrowNotFoundWhenRetryingNonExistingDelivery() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 2L;

        when(deliveryRepository.findByIdAndEvent_User_Id(deliveryId, userId))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> deliveryService.retryDelivery(deliveryId, userId)
        );

        assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(deliveryRepository, never()).save(any(Delivery.class));

        verify(webhookDeliveryService, never()).deliverWebhook(anyLong());
    }

    @Test
    void shouldRejectRetryWhenDeliveryIsNotFailed() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 1L;

        Delivery delivery = new Delivery();
        delivery.setStatus("DELIVERED");

        when(deliveryRepository.findByIdAndEvent_User_Id(deliveryId, userId))
                .thenReturn(java.util.Optional.of(delivery));

        // Act & Assert
        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> deliveryService.retryDelivery(deliveryId, userId)
        );

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(deliveryRepository, never()).save(any(Delivery.class));

        verify(webhookDeliveryService, never()).deliverWebhook(anyLong());
    }

    @Test
    void shouldAllowManualRetryAfterMaximumAttempts() {

        // Arrange
        Long deliveryId = 20L;
        Long userId = 1L;

        Delivery delivery = new Delivery();

        delivery.setStatus("FAILED");
        delivery.setAttemptCount(5);
        delivery.setRetryCount(5);
        delivery.setLastError("HTTP 500");

        when(deliveryRepository.findByIdAndEvent_User_Id(deliveryId, userId))
                .thenReturn(java.util.Optional.of(delivery));

        // Act
        deliveryService.retryDelivery(deliveryId, userId);

        // Assert
        assertEquals("PROCESSING", delivery.getStatus());
        assertEquals(0, delivery.getRetryCount());
        assertEquals(5, delivery.getAttemptCount());
        assertNull(delivery.getNextRetryAt());
        assertNull(delivery.getLastError());

        verify(deliveryRepository).save(delivery);
        verify(webhookDeliveryService).deliverWebhook(deliveryId);
    }
}