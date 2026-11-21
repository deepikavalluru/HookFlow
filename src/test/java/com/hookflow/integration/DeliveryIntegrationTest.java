
package com.hookflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hookflow.entity.Delivery;
import com.hookflow.repository.DeliveryRepository;
import com.hookflow.service.WebhookDeliveryService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeliveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DeliveryRepository deliveryRepository;

    @MockitoBean
    private WebhookDeliveryService webhookDeliveryService;


    // Helper: Register and login user

    private String createAndLoginUser(String name) throws Exception {

        String email = name + "-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "email": "%s",
                                    "password": "password123"
                                }
                                """.formatted(name, email)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("accessToken")
                .asText();
    }


    // Helper: Create webhook and event, then return delivery ID

    private Long createDelivery(String token) throws Exception {

        String webhookResponse = mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Delivery Test Webhook",
                                    "url": "https://example.com/webhook",
                                    "eventType": "ORDER_CREATED"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

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

        String deliveriesResponse = mockMvc.perform(get("/api/deliveries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode deliveries = objectMapper.readTree(deliveriesResponse);

        return deliveries.get(0).get("id").asLong();
    }


    // 1. List deliveries

    @Test
    void shouldGetAllDeliveries() throws Exception {

        String token = createAndLoginUser("DeliveryListUser");

        createDelivery(token);

        mockMvc.perform(get("/api/deliveries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].eventId").exists())
                .andExpect(jsonPath("$[0].endpointId").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].attemptCount").exists());
    }


    // 2. Get delivery by ID

    @Test
    void shouldGetDeliveryById() throws Exception {

        String token = createAndLoginUser("DeliveryByIdUser");

        Long deliveryId = createDelivery(token);

        mockMvc.perform(get("/api/deliveries/" + deliveryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deliveryId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }


    // 3. Return 404 for nonexistent delivery

    @Test
    void shouldReturn404ForNonexistentDelivery() throws Exception {

        String token = createAndLoginUser("MissingDeliveryUser");

        mockMvc.perform(get("/api/deliveries/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }


    // 4. Get delivery attempts

    @Test
    void shouldGetDeliveryAttempts() throws Exception {

        String token = createAndLoginUser("DeliveryAttemptsUser");

        Long deliveryId = createDelivery(token);

        mockMvc.perform(get("/api/deliveries/" + deliveryId + "/attempts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }


    // 5. Get delivery statistics

    @Test
    void shouldGetDeliveryStats() throws Exception {

        String token = createAndLoginUser("DeliveryStatsUser");

        createDelivery(token);

        mockMvc.perform(get("/api/deliveries/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeliveries").value(1))
                .andExpect(jsonPath("$.pendingDeliveries").value(1))
                .andExpect(jsonPath("$.successfulDeliveries").value(0))
                .andExpect(jsonPath("$.failedDeliveries").value(0));
    }


    // 6. Reject requests without authentication

    @Test
    void shouldRejectRequestsWithoutToken() throws Exception {

        mockMvc.perform(get("/api/deliveries"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/deliveries/stats"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/deliveries/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/deliveries/1/attempts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/deliveries/1/retry"))
                .andExpect(status().isUnauthorized());
    }


    // 7. Reject requests with invalid JWT

    @Test
    void shouldRejectRequestsWithInvalidToken() throws Exception {

        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/api/deliveries")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/deliveries/stats")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/deliveries/1/retry")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }


    // 8. Prevent another user from accessing a delivery

    @Test
    void shouldPreventOtherUsersFromAccessingDelivery() throws Exception {

        String userAToken = createAndLoginUser("DeliveryOwner");

        Long deliveryId = createDelivery(userAToken);

        String userBToken = createAndLoginUser("DeliveryOtherUser");

        mockMvc.perform(get("/api/deliveries/" + deliveryId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/deliveries/" + deliveryId + "/attempts")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/retry")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/deliveries")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    // 9. Successfully retry a failed delivery

    @Test
    void shouldRetryFailedDelivery() throws Exception {

        String token = createAndLoginUser("RetryDeliveryUser");

        Long deliveryId = createDelivery(token);

        org.mockito.Mockito.clearInvocations(webhookDeliveryService);

        Delivery delivery = deliveryRepository
                .findById(deliveryId)
                .orElseThrow();

        delivery.setStatus("FAILED");
        delivery.setRetryCount(5);
        delivery.setLastError("Previous delivery failed");
        delivery.setNextRetryAt(null);

        deliveryRepository.save(delivery);

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/retry")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Delivery retry initiated"));

        Delivery updatedDelivery = deliveryRepository
                .findById(deliveryId)
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "PROCESSING", updatedDelivery.getStatus());

        org.junit.jupiter.api.Assertions.assertEquals(
                0, updatedDelivery.getRetryCount());

        org.junit.jupiter.api.Assertions.assertNull(
                updatedDelivery.getNextRetryAt());

        org.junit.jupiter.api.Assertions.assertNull(
                updatedDelivery.getLastError());

        verify(webhookDeliveryService).deliverWebhook(deliveryId);
    }


    // 10. Reject retry when delivery is not FAILED

    @Test
    void shouldRejectRetryForNonFailedDelivery() throws Exception {

        String token = createAndLoginUser("InvalidRetryUser");

        Long deliveryId = createDelivery(token);

        mockMvc.perform(post("/api/deliveries/" + deliveryId + "/retry")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }


    // 11. Reject retry for nonexistent delivery

    @Test
    void shouldRejectRetryForNonexistentDelivery() throws Exception {

        String token = createAndLoginUser("MissingRetryUser");

        mockMvc.perform(post("/api/deliveries/999999/retry")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
