package com.hookflow.dto;

import java.time.LocalDateTime;

public class DeliveryAttemptResponse {

    private Long id;
    private Integer attemptNumber;
    private String status;
    private Integer responseCode;
    private String errorMessage;
    private LocalDateTime attemptedAt;

    public DeliveryAttemptResponse(
            Long id,
            Integer attemptNumber,
            String status,
            Integer responseCode,
            String errorMessage,
            LocalDateTime attemptedAt
    ) {
        this.id = id;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.attemptedAt = attemptedAt;
    }

    public Long getId() { return id; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public String getStatus() { return status; }
    public Integer getResponseCode() { return responseCode; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
}