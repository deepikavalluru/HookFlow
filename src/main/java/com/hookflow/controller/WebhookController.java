package com.hookflow.controller;

import com.hookflow.dto.webhook.*;
import com.hookflow.service.WebhookService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    // 1. Register webhook
    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(
            @Valid @RequestBody CreateWebhookRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        WebhookResponse response =
                webhookService.createWebhook(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Get all webhooks
    @GetMapping
    public ResponseEntity<List<WebhookResponse>> getAllWebhooks(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                webhookService.getAllWebhooks(userId)
        );
    }

    // 3. Get webhook by ID
    @GetMapping("/{id}")
    public ResponseEntity<WebhookResponse> getWebhook(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                webhookService.getWebhook(id, userId)
        );
    }

    // 4. Update webhook
    @PutMapping("/{id}")
    public ResponseEntity<WebhookResponse> updateWebhook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWebhookRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                webhookService.updateWebhook(id, request, userId)
        );
    }

    // 5. Activate / deactivate webhook
    @PatchMapping("/{id}/status")
    public ResponseEntity<WebhookResponse> updateWebhookStatus(
            @PathVariable Long id,
            @RequestBody UpdateWebhookStatusRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                webhookService.updateWebhookStatus(id, request, userId)
        );
    }

    // 6. Delete webhook
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        webhookService.deleteWebhook(id, userId);

        return ResponseEntity.noContent().build();
    }
}