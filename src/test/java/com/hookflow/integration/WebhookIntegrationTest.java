package com.hookflow.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateWebhookSuccessfully() throws Exception {

        String email = "webhook-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        // Register user
        String registerRequest = """
            {
                "name": "Webhook User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        // Login
        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("accessToken")
                .asText();

        // Create webhook
        String webhookRequest = """
            {
                "name": "Order Webhook",
                "url": "https://example.com/webhook",
                "eventType": "ORDER_CREATED"
            }
            """;

        mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Order Webhook"))
                .andExpect(jsonPath("$.eventType").value("ORDER_CREATED"));
    }

    @Test
    void shouldGetWebhooksSuccessfully() throws Exception {

        String email = "list-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        String registerRequest = """
            {
                "name": "List User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("accessToken").asText();

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

        mockMvc.perform(get("/api/webhooks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Order Webhook"))
                .andExpect(jsonPath("$[0].eventType").value("ORDER_CREATED"));
    }

    @Test
    void shouldUpdateWebhookSuccessfully() throws Exception {

        String email = "update-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Update User",
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
                .readTree(loginResponse).get("accessToken").asText();

        String createResponse = mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Old Webhook",
                                "url": "https://example.com/old",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long webhookId = new ObjectMapper()
                .readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/webhooks/" + webhookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Updated Webhook",
                                "url": "https://example.com/updated",
                                "eventType": "ORDER_UPDATED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Webhook"))
                .andExpect(jsonPath("$.eventType").value("ORDER_UPDATED"));
    }

    @Test
    void shouldDeleteWebhookSuccessfully() throws Exception {

        String email = "delete-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Delete User",
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
                .readTree(loginResponse).get("accessToken").asText();

        String createResponse = mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Delete Webhook",
                                "url": "https://example.com/webhook",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long webhookId = new ObjectMapper()
                .readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/webhooks/" + webhookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/webhooks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldRejectWebhookRequestWithoutToken() throws Exception {

        mockMvc.perform(get("/api/webhooks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Unauthorized Webhook",
                                "url": "https://example.com/webhook",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectWebhookRequestWithInvalidToken() throws Exception {

        mockMvc.perform(get("/api/webhooks")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Invalid Token Webhook",
                                "url": "https://example.com/webhook",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPreventUserFromAccessingAnotherUsersWebhook() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        // Register User A
        String emailA = "userA-" + UUID.randomUUID() + "@example.com";

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

        // Login User A
        String loginA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(emailA)))
                .andReturn().getResponse().getContentAsString();

        String tokenA = objectMapper.readTree(loginA)
                .get("accessToken").asText();

        // User A creates webhook
        String createResponse = mockMvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Private Webhook",
                                "url": "https://example.com/webhook",
                                "eventType": "ORDER_CREATED"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long webhookId = objectMapper.readTree(createResponse)
                .get("id").asLong();

        // Register User B
        String emailB = "userB-" + UUID.randomUUID() + "@example.com";

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

        // Login User B
        String loginB = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "%s",
                                "password": "password123"
                            }
                            """.formatted(emailB)))
                .andReturn().getResponse().getContentAsString();

        String tokenB = objectMapper.readTree(loginB)
                .get("accessToken").asText();

        // User B attempts to access User A's webhook
        mockMvc.perform(get("/api/webhooks/" + webhookId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // User B attempts to update User A's webhook
        mockMvc.perform(put("/api/webhooks/" + webhookId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "Stolen Webhook",
                            "url": "https://example.com/stolen",
                            "eventType": "ORDER_UPDATED"
                        }
                        """))
                .andExpect(status().isNotFound());

        // User B attempts to delete User A's webhook
                mockMvc.perform(delete("/api/webhooks/" + webhookId)
                                .header("Authorization", "Bearer " + tokenB))
                        .andExpect(status().isNotFound());
    }
}