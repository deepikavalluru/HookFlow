package com.hookflow.dto;

import java.util.List;

public class DashboardResponse {

    private final Long totalEvents;
    private final Long totalEndpoints;
    private final DeliveryStatsResponse deliveryStats;
    private final List<DeliveryResponse> recentDeliveries;

    public DashboardResponse(
            Long totalEvents,
            Long totalEndpoints,
            DeliveryStatsResponse deliveryStats,
            List<DeliveryResponse> recentDeliveries
    ) {
        this.totalEvents = totalEvents;
        this.totalEndpoints = totalEndpoints;
        this.deliveryStats = deliveryStats;
        this.recentDeliveries = recentDeliveries;
    }

    public Long getTotalEvents() {
        return totalEvents;
    }

    public Long getTotalEndpoints() {
        return totalEndpoints;
    }

    public DeliveryStatsResponse getDeliveryStats() {
        return deliveryStats;
    }

    public List<DeliveryResponse> getRecentDeliveries() {
        return recentDeliveries;
    }
}