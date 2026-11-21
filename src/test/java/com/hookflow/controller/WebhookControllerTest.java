package com.hookflow.controller;

import com.hookflow.dto.webhook.CreateWebhookRequest;
import com.hookflow.dto.webhook.UpdateWebhookRequest;
import com.hookflow.dto.webhook.UpdateWebhookStatusRequest;
import com.hookflow.dto.webhook.WebhookResponse;
import com.hookflow.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private WebhookService webhookService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WebhookController webhookController;

    @Test
    void shouldReturnAllWebhooks() {
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);
        when(webhookService.getAllWebhooks(userId))
                .thenReturn(java.util.List.of());

        var response = webhookController.getAllWebhooks(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(webhookService).getAllWebhooks(userId);
    }

    @Test
    void shouldCreateWebhook() {
        Long userId = 1L;

        CreateWebhookRequest request = mock(CreateWebhookRequest.class);
        WebhookResponse expectedResponse = mock(WebhookResponse.class);

        when(authentication.getPrincipal()).thenReturn(userId);
        when(webhookService.createWebhook(request, userId))
                .thenReturn(expectedResponse);

        var response = webhookController.createWebhook(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(webhookService).createWebhook(request, userId);
    }

    @Test
    void shouldReturnWebhookById() {
        Long webhookId = 10L;
        Long userId = 1L;

        WebhookResponse expectedResponse = mock(WebhookResponse.class);

        when(authentication.getPrincipal()).thenReturn(userId);
        when(webhookService.getWebhook(webhookId, userId))
                .thenReturn(expectedResponse);

        var response = webhookController.getWebhook(webhookId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(webhookService).getWebhook(webhookId, userId);
    }

    @Test
    void shouldUpdateWebhook() {
        Long webhookId = 10L;
        Long userId = 1L;

        UpdateWebhookRequest request = mock(UpdateWebhookRequest.class);
        WebhookResponse expectedResponse = mock(WebhookResponse.class);

        when(authentication.getPrincipal()).thenReturn(userId);
        when(webhookService.updateWebhook(webhookId, request, userId))
                .thenReturn(expectedResponse);

        var response = webhookController.updateWebhook(
                webhookId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(webhookService).updateWebhook(webhookId, request, userId);
    }

    @Test
    void shouldUpdateWebhookStatus() {
        Long webhookId = 10L;
        Long userId = 1L;

        UpdateWebhookStatusRequest request =
                mock(UpdateWebhookStatusRequest.class);

        WebhookResponse expectedResponse = mock(WebhookResponse.class);

        when(authentication.getPrincipal()).thenReturn(userId);
        when(webhookService.updateWebhookStatus(webhookId, request, userId))
                .thenReturn(expectedResponse);

        var response = webhookController.updateWebhookStatus(
                webhookId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(webhookService).updateWebhookStatus(webhookId, request, userId);
    }

    @Test
    void shouldDeleteWebhook() {
        Long webhookId = 10L;
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);

        var response = webhookController.deleteWebhook(webhookId, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(webhookService).deleteWebhook(webhookId, userId);
    }
}

