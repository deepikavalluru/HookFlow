package com.hookflow.dto.webhook;

import java.time.LocalDateTime;

public record WebhookResponse(

        Long id,
        String name,
        String url,
        String eventType,
        boolean active,
        LocalDateTime createdAt

) {}