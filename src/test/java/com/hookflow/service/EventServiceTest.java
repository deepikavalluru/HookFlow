package com.hookflow.service;

import com.hookflow.repository.*;
import com.hookflow.security.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import com.hookflow.dto.EventRequest;
import com.hookflow.dto.EventResponse;
import com.hookflow.entity.Event;
import com.hookflow.entity.User;

import tools.jackson.databind.JsonNode;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hookflow.entity.WebhookEndpoint;
import com.hookflow.entity.Delivery;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebhookEndpointRepository webhookEndpointRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WebhookDeliveryService webhookDeliveryService;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldReturnEmptyListWhenUserHasNoEvents() {
        Long userId = 1L;

        when(eventRepository.findAllByUser_Id(userId))
                .thenReturn(java.util.List.of());

        var response = eventService.getAllEvents(userId);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(eventRepository).findAllByUser_Id(userId);
    }

    @Test
    void shouldCreateEventWithoutMatchingWebhooks() throws Exception {
        Long userId = 1L;

        EventRequest request = mock(EventRequest.class);
        User user = new User();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(request.eventType()).thenReturn("ORDER_CREATED");
        JsonNode payload = new ObjectMapper().readTree("{\"orderId\":101}");

        when(request.payload()).thenReturn(payload);

        Event savedEvent = new Event();

        savedEvent.setEventType("ORDER_CREATED");
        savedEvent.setPayload("{\"orderId\":101}");
        savedEvent.setUser(user);

        when(eventRepository.save(any(Event.class)))
                .thenReturn(savedEvent);

        when(webhookEndpointRepository
                .findAllByUser_IdAndEventTypeAndActiveTrueAndDeletedFalse(
                        userId, "ORDER_CREATED"))
                .thenReturn(java.util.List.of());

        when(deliveryRepository.saveAll(anyList()))
                .thenReturn(java.util.List.of());

        when(objectMapper.readTree(any(String.class)))
                .thenReturn(payload);

        EventResponse response = eventService.createEvent(request, userId);

        assertNotNull(response);
        assertEquals("ORDER_CREATED", response.eventType());

        verify(eventRepository).save(any(Event.class));
        verify(deliveryRepository).saveAll(anyList());
        verify(webhookDeliveryService, never()).deliverWebhook(anyLong());
    }

    @Test
    void shouldCreateDeliveriesForMatchingWebhooks() throws Exception {
        Long userId = 1L;

        JsonNode payload = new ObjectMapper()
                .readTree("{\"orderId\":101}");

        EventRequest request = mock(EventRequest.class);

        when(request.eventType()).thenReturn("ORDER_CREATED");
        when(request.payload()).thenReturn(payload);

        User user = new User();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Event savedEvent = new Event();
        savedEvent.setEventType("ORDER_CREATED");
        savedEvent.setPayload(payload.toString());
        savedEvent.setUser(user);

        when(eventRepository.save(any(Event.class)))
                .thenReturn(savedEvent);

        WebhookEndpoint endpoint1 = new WebhookEndpoint();
        WebhookEndpoint endpoint2 = new WebhookEndpoint();

        when(webhookEndpointRepository
                .findAllByUser_IdAndEventTypeAndActiveTrueAndDeletedFalse(
                        userId, "ORDER_CREATED"))
                .thenReturn(java.util.List.of(endpoint1, endpoint2));

        when(deliveryRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.readTree(any(String.class)))
                .thenReturn(payload);

        TransactionSynchronizationManager.initSynchronization();

        try {
            EventResponse response = eventService.createEvent(request, userId);

            assertNotNull(response);
            assertEquals("ORDER_CREATED", response.eventType());

            verify(deliveryRepository).saveAll(anyList());

            // Delivery must not start before transaction commit.
            verify(webhookDeliveryService, never())
                    .deliverWebhook(anyLong());

            // Simulate transaction commit.
            for (TransactionSynchronization synchronization :
                    TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            // Both deliveries should now be triggered.
            verify(webhookDeliveryService, times(2))
                    .deliverWebhook(null);

        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        Long userId = 99L;

        EventRequest request = mock(EventRequest.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.createEvent(request, userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(eventRepository, never()).save(any(Event.class));
        verify(deliveryRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldReturnAllEventsForUser() throws Exception {
        Long userId = 1L;

        Event event = new Event();
        event.setEventType("ORDER_CREATED");
        event.setPayload("{\"orderId\":101}");

        JsonNode payload = new ObjectMapper()
                .readTree("{\"orderId\":101}");

        when(eventRepository.findAllByUser_Id(userId))
                .thenReturn(java.util.List.of(event));

        when(objectMapper.readTree("{\"orderId\":101}"))
                .thenReturn(payload);

        var response = eventService.getAllEvents(userId);

        assertEquals(1, response.size());
        assertEquals("ORDER_CREATED", response.get(0).eventType());
        assertEquals(101, response.get(0).payload().get("orderId").intValue());

        verify(eventRepository).findAllByUser_Id(userId);
    }

    @Test
    void shouldReturnEventById() throws Exception {
        Long eventId = 10L;
        Long userId = 1L;

        Event event = new Event();
        event.setEventType("ORDER_CREATED");
        event.setPayload("{\"orderId\":101}");

        JsonNode payload = new ObjectMapper()
                .readTree("{\"orderId\":101}");

        when(eventRepository.findByIdAndUser_Id(eventId, userId))
                .thenReturn(Optional.of(event));

        when(objectMapper.readTree("{\"orderId\":101}"))
                .thenReturn(payload);

        EventResponse response = eventService.getEventById(eventId, userId);

        assertNotNull(response);
        assertEquals("ORDER_CREATED", response.eventType());
        assertEquals(101, response.payload().get("orderId").intValue());

        verify(eventRepository).findByIdAndUser_Id(eventId, userId);
    }

    @Test
    void shouldThrowExceptionWhenEventDoesNotExistOrBelongToUser() {
        Long eventId = 10L;
        Long userId = 1L;

        when(eventRepository.findByIdAndUser_Id(eventId, userId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventService.getEventById(eventId, userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(eventRepository).findByIdAndUser_Id(eventId, userId);
    }
}