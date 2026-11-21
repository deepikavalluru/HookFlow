package com.hookflow.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        String email = "test-" + UUID.randomUUID() + "@example.com";

        String request = """
            {
                "name": "Test User",
                "email": "%s",
                "password": "password123"
            }
            """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {

        String email = "login-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        String registerRequest = """
            {
                "name": "Login Test",
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

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldGetCurrentUserWithValidToken() throws Exception {

        String email = "me-" + UUID.randomUUID() + "@example.com";
        String password = "password123";

        String registerRequest = """
            {
                "name": "Current User",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(registerResponse)
                .get("id")
                .asLong();

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

        String token = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(loginResponse)
                .get("accessToken")
                .asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Current User"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithInvalidToken() throws Exception {

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}