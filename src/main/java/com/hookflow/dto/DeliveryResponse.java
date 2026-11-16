package com.hookflow.dto;

import java.time.LocalDateTime;

public class DeliveryResponse {

    private Long id;
    private Long eventId;
    private Long endpointId;
    private String status;
    private Integer attemptCount;
    private Integer lastResponseCode;
    private String lastError;
    private LocalDateTime createdAt;

    public DeliveryResponse(
            Long id,
            Long eventId,
            Long endpointId,
            String status,
            Integer attemptCount,
            Integer lastResponseCode,
            String lastError,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.endpointId = endpointId;
        this.status = status;
        this.attemptCount = attemptCount;
        this.lastResponseCode = lastResponseCode;
        this.lastError = lastError;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getEndpointId() { return endpointId; }
    public String getStatus() { return status; }
    public Integer getAttemptCount() { return attemptCount; }
    public Integer getLastResponseCode() { return lastResponseCode; }
    public String getLastError() { return lastError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}