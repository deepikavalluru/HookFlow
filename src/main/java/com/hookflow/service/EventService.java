package com.hookflow.service;

import com.hookflow.dto.EventRequest;
import com.hookflow.dto.EventResponse;
import com.hookflow.entity.*;
import com.hookflow.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final WebhookEndpointRepository webhookEndpointRepository;
    private final DeliveryRepository deliveryRepository;
    private final ObjectMapper objectMapper;
    private final WebhookDeliveryService webhookDeliveryService;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository,
            WebhookEndpointRepository webhookEndpointRepository,
            DeliveryRepository deliveryRepository,
            ObjectMapper objectMapper,
            WebhookDeliveryService webhookDeliveryService) {

        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.webhookEndpointRepository = webhookEndpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.objectMapper = objectMapper;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    // Create event
    @Transactional
    public EventResponse createEvent(EventRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        Event event = new Event();
        event.setEventType(request.eventType());
        event.setPayload(request.payload().toString());
        event.setUser(user);

        Event savedEvent = eventRepository.save(event);

        // Find matching active endpoints
        // Find matching active, non-deleted endpoints
        List<WebhookEndpoint> endpoints =
                webhookEndpointRepository
                        .findAllByUser_IdAndEventTypeAndActiveTrueAndDeletedFalse(
                                userId,
                                request.eventType()
                        );

        // Create delivery records
        List<Delivery> deliveries = endpoints.stream()
                .map(endpoint -> {
                    Delivery delivery = new Delivery();
                    delivery.setEvent(savedEvent);
                    delivery.setWebhookEndpoint(endpoint);
                    return delivery;
                })
                .toList();

        List<Delivery> savedDeliveries =
                deliveryRepository.saveAll(deliveries);

        // Trigger delivery only after the transaction commits
        if (!savedDeliveries.isEmpty()) {

            org.springframework.transaction.support.TransactionSynchronizationManager
                    .registerSynchronization(
                            new org.springframework.transaction.support.TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    for (Delivery delivery : savedDeliveries) {
                                        webhookDeliveryService.deliverWebhook(
                                                delivery.getId());
                                    }
                                }
                            });
        }

        return mapToResponse(savedEvent);
    }

    // Get all events belonging to user
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents(Long userId) {

        return eventRepository.findAllByUser_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get specific event
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId, Long userId) {

        Event event = eventRepository
                .findByIdAndUser_Id(eventId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Event not found"));

        return mapToResponse(event);
    }

    // Convert entity into response DTO
    private EventResponse mapToResponse(Event event) {

        JsonNode payload = objectMapper.readTree(event.getPayload());

        return new EventResponse(
                event.getId(),
                event.getEventType(),
                payload,
                event.getStatus(),
                event.getCreatedAt()
        );
    }
}