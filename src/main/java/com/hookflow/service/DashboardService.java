package com.hookflow.service;

import com.hookflow.dto.DashboardResponse;
import com.hookflow.repository.EventRepository;
import com.hookflow.repository.WebhookEndpointRepository;
import com.hookflow.repository.DeliveryRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final EventRepository eventRepository;
    private final WebhookEndpointRepository webhookEndpointRepository;
    private final DeliveryRepository deliveryRepository;

    private final DeliveryService deliveryService;

    public DashboardService(
            EventRepository eventRepository,
            WebhookEndpointRepository webhookEndpointRepository,
            DeliveryRepository deliveryRepository,
            DeliveryService deliveryService
    ) {
        this.eventRepository = eventRepository;
        this.webhookEndpointRepository = webhookEndpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.deliveryService = deliveryService;
    }

    public DashboardResponse getDashboard(Long userId) {

        return new DashboardResponse(
                eventRepository.countByUser_Id(userId),

                webhookEndpointRepository.countByUser_Id(userId),

                deliveryService.getDeliveryStats(userId),

                deliveryRepository.findRecentDeliveries(
                        userId,
                        PageRequest.of(0, 5)
                )
        );
    }
}