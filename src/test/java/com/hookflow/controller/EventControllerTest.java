package com.hookflow.controller;

import com.hookflow.dto.EventRequest;
import com.hookflow.dto.EventResponse;
import com.hookflow.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventController eventController;

    @Test
    void shouldCreateEvent() {
        Long userId = 1L;

        EventRequest request = mock(EventRequest.class);
        EventResponse expectedResponse = mock(EventResponse.class);

        when(authentication.getPrincipal()).thenReturn(userId);
        when(eventService.createEvent(request, userId))
                .thenReturn(expectedResponse);

        var response = eventController.createEvent(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(eventService).createEvent(request, userId);
    }

    @Test
    void shouldReturnAllEvents() {
        Long userId = 1L;

        when(authentication.getPrincipal()).thenReturn(userId);
        when(eventService.getAllEvents(userId))
                .thenReturn(java.util.List.of());

        var response = eventController.getAllEvents(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(eventService).getAllEvents(userId);
    }

    @Test
    void shouldReturnEventById() {
        Long eventId = 10L;
        Long userId = 1L;

        EventResponse expectedResponse = mock(EventResponse.class);

        when(authentication.getPrincipal()).thenReturn(userId);
        when(eventService.getEventById(eventId, userId))
                .thenReturn(expectedResponse);

        var response = eventController.getEventById(eventId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(eventService).getEventById(eventId, userId);
    }
}