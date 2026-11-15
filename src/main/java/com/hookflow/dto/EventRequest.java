package com.hookflow.dto;

import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @NotBlank(message = "Event type is required")
        String eventType,

        @NotNull(message = "Payload is required")
        JsonNode payload
) {}