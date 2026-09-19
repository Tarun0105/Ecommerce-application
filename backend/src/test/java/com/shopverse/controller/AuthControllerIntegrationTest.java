package com.shopverse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopverse.dto.request.LoginRequest;
import com.shopverse.dto.request.RegisterRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests for AuthController.
 * Runs against the H2 in-memory database configured in application-test.yml.
 * Tests are ordered because later tests depend on the user registered in test #1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    static final String REGISTER_URL = "/api/auth/register";
    static final String LOGIN_URL    = "/api/auth/login";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // --- Register ---

    @Test
    @Order(1)
    void register_validRequest_returns201WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest("Integration", "User", "inttest@test.com", "Password@123");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("inttest@test.com"))
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @Order(2)
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest("Integration", "User", "inttest@test.com", "Password@123");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("Test", "User", "not-an-email", "Password@123");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("Test", "User", "shortpass@test.com", "pass");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void register_missingFirstName_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("", "User", "missingname@test.com", "Password@123");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void register_emptyBody_returns400() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- Login ---

    @Test
    @Order(7)
    void login_validCredentials_returns200WithToken() throws Exception {
        LoginRequest req = new LoginRequest("inttest@test.com", "Password@123");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("inttest@test.com"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @Order(8)
    void login_wrongPassword_returns400() throws Exception {
        LoginRequest req = new LoginRequest("inttest@test.com", "WrongPassword");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    void login_nonExistentUser_returns400() throws Exception {
        LoginRequest req = new LoginRequest("nobody@test.com", "Password@123");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    void login_missingEmail_returns400() throws Exception {
        LoginRequest req = new LoginRequest("", "Password@123");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    void login_adminUser_returns200WithAdminRole() throws Exception {
        // Admin seeded by DataInitializer at startup from application-test.yml
        LoginRequest req = new LoginRequest("testadmin@shopverse.com", "TestAdmin@123");
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }
}
