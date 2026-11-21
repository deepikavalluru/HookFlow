package com.hookflow.controller;

import com.hookflow.service.DeliveryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DeliveryController deliveryController;

    @Test
    void shouldReturnAllDeliveries() {

        // Arrange
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);
        when(deliveryService.getAllDeliveries(userId))
                .thenReturn(java.util.List.of());

        // Act
        var response = deliveryController.getAllDeliveries(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(deliveryService).getAllDeliveries(userId);
    }

    @Test
    void shouldReturnDeliveryStats() {

        // Arrange
        Long userId = 1L;

        var expected = new com.hookflow.dto.DeliveryStatsResponse(
                10L, 6L, 3L, 1L
        );

        when(authentication.getPrincipal()).thenReturn(userId);

        when(deliveryService.getDeliveryStats(userId))
                .thenReturn(expected);

        // Act
        var response = deliveryController.getDeliveryStats(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());

        verify(deliveryService).getDeliveryStats(userId);
    }

    @Test
    void shouldReturnDeliveryById() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 1L;

        var expected = mock(com.hookflow.dto.DeliveryResponse.class);

        when(authentication.getPrincipal()).thenReturn(userId);

        when(deliveryService.getDeliveryById(deliveryId, userId))
                .thenReturn(java.util.Optional.of(expected));

        // Act
        var response = deliveryController.getDeliveryById(
                deliveryId, authentication
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());

        verify(deliveryService).getDeliveryById(deliveryId, userId);
    }

    @Test
    void shouldReturn404WhenDeliveryDoesNotExist() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);

        when(deliveryService.getDeliveryById(deliveryId, userId))
                .thenReturn(java.util.Optional.empty());

        // Act
        var response = deliveryController.getDeliveryById(
                deliveryId, authentication
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(deliveryService).getDeliveryById(deliveryId, userId);
    }

    @Test
    void shouldReturnDeliveryAttempts() {

        // Arrange
        Long deliveryId = 10L;
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);

        when(deliveryService.getDeliveryAttempts(deliveryId, userId))
                .thenReturn(java.util.List.of());

        // Act
        var response = deliveryController.getDeliveryAttempts(
                deliveryId, authentication
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(deliveryService).getDeliveryAttempts(deliveryId, userId);
    }

    @Test
    void shouldAcceptManualRetry() {
        Long deliveryId = 10L;
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);

        var response = deliveryController.retryDelivery(deliveryId, authentication);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Delivery retry initiated", response.getBody());

        verify(deliveryService).retryDelivery(deliveryId, userId);
    }
}