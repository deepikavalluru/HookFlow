package com.hookflow.dto;

public class DeliveryStatsResponse {

    private long totalDeliveries;
    private long successfulDeliveries;
    private long failedDeliveries;
    private long pendingDeliveries;

    public DeliveryStatsResponse(
            long totalDeliveries,
            long successfulDeliveries,
            long failedDeliveries,
            long pendingDeliveries
    ) {
        this.totalDeliveries = totalDeliveries;
        this.successfulDeliveries = successfulDeliveries;
        this.failedDeliveries = failedDeliveries;
        this.pendingDeliveries = pendingDeliveries;
    }

    public long getTotalDeliveries() { return totalDeliveries; }
    public long getSuccessfulDeliveries() { return successfulDeliveries; }
    public long getFailedDeliveries() { return failedDeliveries; }
    public long getPendingDeliveries() { return pendingDeliveries; }
}