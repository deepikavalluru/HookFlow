package com.hookflow.dto;

import tools.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String eventType,
        JsonNode payload,
        String status,
        LocalDateTime createdAt
) {}