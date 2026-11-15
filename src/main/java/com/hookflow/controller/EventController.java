package com.hookflow.controller;

import com.hookflow.dto.EventRequest;
import com.hookflow.dto.EventResponse;
import com.hookflow.service.EventService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Submit a new event
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        EventResponse response = eventService.createEvent(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all events belonging to authenticated user
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(eventService.getAllEvents(userId));
    }

    // Get a specific event
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(eventService.getEventById(id, userId));
    }
}