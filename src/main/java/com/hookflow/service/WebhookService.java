package com.hookflow.service;

import com.hookflow.dto.webhook.*;
import com.hookflow.entity.User;
import com.hookflow.entity.WebhookEndpoint;
import com.hookflow.exception.ResourceNotFoundException;
import com.hookflow.repository.UserRepository;
import com.hookflow.repository.WebhookEndpointRepository;
import com.hookflow.security.WebhookUrlValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class WebhookService {

    private final WebhookEndpointRepository webhookRepository;
    private final UserRepository userRepository;
    private final WebhookUrlValidator urlValidator;

    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookService(
            WebhookEndpointRepository webhookRepository,
            UserRepository userRepository,
            WebhookUrlValidator urlValidator) {

        this.webhookRepository = webhookRepository;
        this.userRepository = userRepository;
        this.urlValidator = urlValidator;
    }

    // 1. Register webhook
    @Transactional
    public WebhookResponse createWebhook(CreateWebhookRequest request, Long userId) {
        urlValidator.validate(request.url());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WebhookEndpoint endpoint = new WebhookEndpoint();

        endpoint.setName(request.name());
        endpoint.setUrl(request.url());
        endpoint.setEventType(request.eventType());
        endpoint.setSecret(generateSecret());
        endpoint.setUser(user);
        endpoint.setActive(true);

        WebhookEndpoint saved = webhookRepository.save(endpoint);

        return mapToResponse(saved);
    }

    // 2. Get all webhooks belonging to user
    @Transactional(readOnly = true)
    public List<WebhookResponse> getAllWebhooks(Long userId) {

        return webhookRepository.findAllByUser_IdAndDeletedFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 3. Get webhook by ID
    @Transactional(readOnly = true)
    public WebhookResponse getWebhook(Long id, Long userId) {

        WebhookEndpoint endpoint = getOwnedWebhook(id, userId);

        return mapToResponse(endpoint);
    }

    // 4. Update webhook
    @Transactional
    public WebhookResponse updateWebhook(
            Long id,
            UpdateWebhookRequest request,
            Long userId) {

        urlValidator.validate(request.url());

        WebhookEndpoint endpoint = getOwnedWebhook(id, userId);

        endpoint.setName(request.name());
        endpoint.setUrl(request.url());
        endpoint.setEventType(request.eventType());

        WebhookEndpoint updated = webhookRepository.save(endpoint);

        return mapToResponse(updated);
    }

    // 5. Activate / deactivate webhook
    @Transactional
    public WebhookResponse updateWebhookStatus(
            Long id,
            UpdateWebhookStatusRequest request,
            Long userId) {

        WebhookEndpoint endpoint = getOwnedWebhook(id, userId);

        endpoint.setActive(request.active());

        return mapToResponse(webhookRepository.save(endpoint));
    }

    // 6. Delete webhook
    @Transactional
    public void deleteWebhook(Long id, Long userId) {

        WebhookEndpoint endpoint = getOwnedWebhook(id, userId);

        endpoint.setDeleted(true);
        endpoint.setActive(false);

        webhookRepository.save(endpoint);
    }

    // Check ownership
    private WebhookEndpoint getOwnedWebhook(Long id, Long userId) {
        return webhookRepository.findByIdAndUser_IdAndDeletedFalse(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Webhook endpoint not found"));
    }

    // Generate secure random secret
    private String generateSecret() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // Convert entity into response DTO
    private WebhookResponse mapToResponse(WebhookEndpoint endpoint) {

        return new WebhookResponse(
                endpoint.getId(),
                endpoint.getName(),
                endpoint.getUrl(),
                endpoint.getEventType(),
                endpoint.isActive(),
                endpoint.getCreatedAt()
        );
    }
}