package com.hookflow.dto.webhook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWebhookRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 2048)
        String url,

        @NotBlank
        @Size(max = 100)
        String eventType

) {}