package com.hookflow.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateEventSuccessfully() throws Exception {

        String email = "event-" + UUID.randomUUID() + "@example.com";

        // Register user
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Event User",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated());

        // Login
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("accessToken")
                .asText();

        // Create event
        String eventRequest = """
            {
                "eventType": "ORDER_CREATED",
                "payload": {
                    "orderId": 101,
                    "amount": 2500
                }
            }
            """;

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("ORDER_CREATED"))
                .andExpect(jsonPath("$.payload.orderId").value(101));
    }

    @Test
    void shouldGetEventsSuccessfully() throws Exception {

        String email = "events-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Events User",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("accessToken").asText();

        // Create event
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_CREATED",
                                "payload": {
                                    "orderId": 101,
                                    "amount": 2500
                                }
                            }
                            """))
                .andExpect(status().isCreated());

        // Retrieve events
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("ORDER_CREATED"))
                .andExpect(jsonPath("$[0].payload.orderId").value(101));
    }

    @Test
    void shouldGetEventByIdSuccessfully() throws Exception {

        String email = "event-id-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Event ID User",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("accessToken").asText();

        String eventResponse = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "PAYMENT_COMPLETED",
                                "payload": {
                                    "paymentId": 501,
                                    "amount": 1500
                                }
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long eventId = new ObjectMapper()
                .readTree(eventResponse)
                .get("id").asLong();

        mockMvc.perform(get("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.eventType").value("PAYMENT_COMPLETED"))
                .andExpect(jsonPath("$.payload.paymentId").value(501));
    }

    @Test
    void shouldPreventUserFromAccessingAnotherUsersEvent() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        // Register and login User A
        String emailA = "eventA-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "User A",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(emailA)))
                .andExpect(status().isCreated());

        String loginA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(emailA)))
                .andReturn().getResponse().getContentAsString();

        String tokenA = mapper.readTree(loginA).get("accessToken").asText();

        // User A creates an event
        String eventResponse = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_CREATED",
                                "payload": {
                                    "orderId": 101
                                }
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long eventId = mapper.readTree(eventResponse).get("id").asLong();

        // Register and login User B
        String emailB = "eventB-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "User B",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(emailB)))
                .andExpect(status().isCreated());

        String loginB = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(emailB)))
                .andReturn().getResponse().getContentAsString();

        String tokenB = mapper.readTree(loginB).get("accessToken").asText();

        // User B tries to retrieve User A's event
        mockMvc.perform(get("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldRejectEventRequestsWithoutToken() throws Exception {

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_CREATED",
                                "payload": {
                                    "orderId": 101
                                }
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectEventRequestsWithInvalidToken() throws Exception {

        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_CREATED",
                                "payload": {
                                    "orderId": 101
                                }
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectEventWithMissingEventType() throws Exception {

        String email = "validation-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Validation User",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("accessToken").asText();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "payload": {
                                    "orderId": 101
                                }
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateDeliveryForMatchingWebhook() throws Exception {

        String email = "delivery-" + UUID.randomUUID() + "@example.com";
        ObjectMapper mapper = new ObjectMapper();

        // Register user
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Delivery User",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated());

        // Login
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andReturn().getResponse().getContentAsString();

        String token = mapper.readTree(loginResponse)
                .get("accessToken").asText();

        // Create matching webhook
        mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Order Webhook",
                                "url": "https://example.com/webhook",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isCreated());

        // Create event
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_CREATED",
                                "payload": {
                                    "orderId": 501
                                }
                            }
                            """))
                .andExpect(status().isCreated());

        // Verify delivery was created
        mockMvc.perform(get("/api/deliveries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    void shouldNotCreateDeliveryForUnmatchedWebhook() throws Exception {

        String email = "unmatched-" + UUID.randomUUID() + "@example.com";
        ObjectMapper mapper = new ObjectMapper();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Unmatched User",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andReturn().getResponse().getContentAsString();

        String token = mapper.readTree(loginResponse)
                .get("accessToken").asText();

        // Subscribe to ORDER_CREATED
        mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Order Webhook",
                                "url": "https://example.com/webhook",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isCreated());

        // Submit PAYMENT_COMPLETED
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "PAYMENT_COMPLETED",
                                "payload": {
                                    "paymentId": 501
                                }
                            }
                            """))
                .andExpect(status().isCreated());

        // No matching delivery should exist
        mockMvc.perform(get("/api/deliveries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldNotCreateDeliveryForInactiveWebhook() throws Exception {

        String email = "inactive-" + UUID.randomUUID() + "@example.com";
        ObjectMapper mapper = new ObjectMapper();

        // Register user
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Inactive User",
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andExpect(status().isCreated());

        // Login
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(email)))
                .andReturn().getResponse().getContentAsString();

        String token = mapper.readTree(loginResponse)
                .get("accessToken").asText();

        // Create webhook
        String webhookResponse = mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Inactive Webhook",
                                "url": "https://example.com/webhook",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long webhookId = mapper.readTree(webhookResponse)
                .get("id").asLong();

        // Deactivate webhook
        mockMvc.perform(patch("/api/webhooks/" + webhookId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "active": false
                            }
                            """))
                .andExpect(status().isOk());

        // Submit matching event
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_CREATED",
                                "payload": {
                                    "orderId": 601
                                }
                            }
                            """))
                .andExpect(status().isCreated());

        // Verify no delivery was created
        mockMvc.perform(get("/api/deliveries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}