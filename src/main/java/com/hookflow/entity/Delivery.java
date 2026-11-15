package com.hookflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    private LocalDateTime nextRetryAt;

    private Integer lastResponseCode;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_endpoint_id", nullable = false)
    private WebhookEndpoint webhookEndpoint;

    public Delivery() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public Integer getLastResponseCode() {
        return lastResponseCode;
    }

    public void setLastResponseCode(Integer lastResponseCode) {
        this.lastResponseCode = lastResponseCode;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public WebhookEndpoint getWebhookEndpoint() { return webhookEndpoint; }
    public void setWebhookEndpoint(WebhookEndpoint webhookEndpoint) {
        this.webhookEndpoint = webhookEndpoint;
    }
}