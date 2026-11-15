package com.hookflow.dto.webhook;

public record UpdateWebhookStatusRequest(
        boolean active
) {}