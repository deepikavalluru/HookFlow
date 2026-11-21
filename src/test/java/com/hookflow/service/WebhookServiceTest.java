package com.hookflow.service;

import com.hookflow.dto.webhook.UpdateWebhookRequest;
import com.hookflow.dto.webhook.UpdateWebhookStatusRequest;
import com.hookflow.exception.ResourceNotFoundException;
import com.hookflow.repository.UserRepository;
import com.hookflow.repository.WebhookEndpointRepository;
import com.hookflow.security.WebhookUrlValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hookflow.dto.webhook.CreateWebhookRequest;
import com.hookflow.dto.webhook.WebhookResponse;
import com.hookflow.entity.User;
import com.hookflow.entity.WebhookEndpoint;

import java.util.Optional;
import java.util.Base64;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookEndpointRepository webhookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebhookUrlValidator urlValidator;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void shouldReturnEmptyListWhenUserHasNoWebhooks() {
        Long userId = 1L;

        when(webhookRepository.findAllByUser_IdAndDeletedFalse(userId))
                .thenReturn(java.util.List.of());

        var response = webhookService.getAllWebhooks(userId);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(webhookRepository).findAllByUser_IdAndDeletedFalse(userId);
    }

    @Test
    void shouldCreateWebhookSuccessfully() {
        Long userId = 1L;

        CreateWebhookRequest request = new CreateWebhookRequest(
                "Order Webhook",
                "https://example.com/webhook",
                "ORDER_CREATED"
        );

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(webhookRepository.save(any(WebhookEndpoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WebhookResponse response = webhookService.createWebhook(request, userId);

        assertNotNull(response);
        assertEquals("Order Webhook", response.name());
        assertEquals("https://example.com/webhook", response.url());
        assertEquals("ORDER_CREATED", response.eventType());
        assertTrue(response.active());

        verify(urlValidator).validate(request.url());
        verify(userRepository).findById(userId);
        verify(webhookRepository).save(any(WebhookEndpoint.class));
    }

    @Test
    void shouldReturnWebhookById() {
        Long webhookId = 10L;
        Long userId = 1L;

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setName("Order Webhook");
        endpoint.setUrl("https://example.com/webhook");
        endpoint.setEventType("ORDER_CREATED");
        endpoint.setActive(true);

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.of(endpoint));

        WebhookResponse response = webhookService.getWebhook(webhookId, userId);

        assertNotNull(response);
        assertEquals("Order Webhook", response.name());
        assertEquals("https://example.com/webhook", response.url());
        assertEquals("ORDER_CREATED", response.eventType());
        assertTrue(response.active());

        verify(webhookRepository).findByIdAndUser_IdAndDeletedFalse(webhookId, userId);
    }

    @Test
    void shouldThrowExceptionWhenWebhookDoesNotBelongToUser() {
        Long webhookId = 10L;
        Long userId = 1L;

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> webhookService.getWebhook(webhookId, userId)
        );

        verify(webhookRepository).findByIdAndUser_IdAndDeletedFalse(webhookId, userId);
    }

    @Test
    void shouldUpdateWebhookSuccessfully() {
        Long webhookId = 10L;
        Long userId = 1L;

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setName("Old Webhook");
        endpoint.setUrl("https://old.example.com/webhook");
        endpoint.setEventType("ORDER_CREATED");
        endpoint.setActive(true);

        UpdateWebhookRequest request = new UpdateWebhookRequest(
                "Updated Webhook",
                "https://new.example.com/webhook",
                "ORDER_UPDATED"
        );

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.of(endpoint));

        when(webhookRepository.save(endpoint)).thenReturn(endpoint);

        WebhookResponse response =
                webhookService.updateWebhook(webhookId, request, userId);

        assertEquals("Updated Webhook", response.name());
        assertEquals("https://new.example.com/webhook", response.url());
        assertEquals("ORDER_UPDATED", response.eventType());

        verify(urlValidator).validate(request.url());
        verify(webhookRepository).save(endpoint);
    }

    @Test
    void shouldUpdateWebhookStatus() {
        Long webhookId = 10L;
        Long userId = 1L;

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setName("Order Webhook");
        endpoint.setUrl("https://example.com/webhook");
        endpoint.setEventType("ORDER_CREATED");
        endpoint.setActive(true);

        UpdateWebhookStatusRequest request =
                new UpdateWebhookStatusRequest(false);

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.of(endpoint));

        when(webhookRepository.save(endpoint)).thenReturn(endpoint);

        WebhookResponse response =
                webhookService.updateWebhookStatus(webhookId, request, userId);

        assertFalse(response.active());

        verify(webhookRepository).save(endpoint);
    }

    @Test
    void shouldDeleteWebhookSuccessfully() {
        Long webhookId = 10L;
        Long userId = 1L;

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setName("Order Webhook");
        endpoint.setUrl("https://example.com/webhook");
        endpoint.setEventType("ORDER_CREATED");
        endpoint.setActive(true);

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.of(endpoint));

        when(webhookRepository.save(endpoint)).thenReturn(endpoint);

        webhookService.deleteWebhook(webhookId, userId);

        assertTrue(endpoint.isDeleted());
        assertFalse(endpoint.isActive());

        verify(webhookRepository).save(endpoint);
        verify(webhookRepository, never()).delete(any(WebhookEndpoint.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWebhookForNonExistingUser() {
        Long userId = 1L;

        CreateWebhookRequest request = new CreateWebhookRequest(
                "Order Webhook",
                "https://example.com/webhook",
                "ORDER_CREATED"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> webhookService.createWebhook(request, userId)
        );

        verify(urlValidator).validate(request.url());
        verify(userRepository).findById(userId);
        verify(webhookRepository, never()).save(any(WebhookEndpoint.class));
    }

    @Test
    void shouldRejectInvalidWebhookUrl() {
        Long userId = 1L;

        CreateWebhookRequest request = new CreateWebhookRequest(
                "Order Webhook",
                "http://localhost:8080/webhook",
                "ORDER_CREATED"
        );

        doThrow(new IllegalArgumentException("Invalid webhook URL"))
                .when(urlValidator).validate(request.url());

        assertThrows(
                IllegalArgumentException.class,
                () -> webhookService.createWebhook(request, userId)
        );

        verify(urlValidator).validate(request.url());
        verify(userRepository, never()).findById(anyLong());
        verify(webhookRepository, never()).save(any(WebhookEndpoint.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWebhookNotOwnedByUser() {
        Long webhookId = 10L;
        Long userId = 1L;

        UpdateWebhookRequest request = new UpdateWebhookRequest(
                "Updated Webhook",
                "https://example.com/webhook",
                "ORDER_UPDATED"
        );

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> webhookService.updateWebhook(webhookId, request, userId)
        );

        verify(urlValidator).validate(request.url());
        verify(webhookRepository, never()).save(any(WebhookEndpoint.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStatusOfUnownedWebhook() {
        Long webhookId = 10L;
        Long userId = 1L;

        UpdateWebhookStatusRequest request =
                new UpdateWebhookStatusRequest(false);

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> webhookService.updateWebhookStatus(webhookId, request, userId)
        );

        verify(webhookRepository).findByIdAndUser_IdAndDeletedFalse(webhookId, userId);
        verify(webhookRepository, never()).save(any(WebhookEndpoint.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingUnownedWebhook() {
        Long webhookId = 10L;
        Long userId = 1L;

        when(webhookRepository.findByIdAndUser_IdAndDeletedFalse(webhookId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> webhookService.deleteWebhook(webhookId, userId)
        );

        verify(webhookRepository).findByIdAndUser_IdAndDeletedFalse(webhookId, userId);
        verify(webhookRepository, never()).delete(any(WebhookEndpoint.class));
    }
}