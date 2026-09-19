package com.shopverse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopverse.dto.request.AddToCartRequest;
import com.shopverse.dto.request.LoginRequest;
import com.shopverse.dto.request.ProductRequest;
import com.shopverse.dto.request.RegisterRequest;
import com.shopverse.dto.request.UpdateCartItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CartController.
 * Each test class run creates its own isolated user to avoid cross-test pollution.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerIntegrationTest {

    static final String CART_URL     = "/api/cart";
    static final String PRODUCTS_URL = "/api/products";
    static final String LOGIN_URL    = "/api/auth/login";
    static final String REGISTER_URL = "/api/auth/register";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private Long   productId;

    @BeforeEach
    void setup() throws Exception {
        adminToken = loginAndGetToken("testadmin@shopverse.com", "TestAdmin@123");

        // Create a unique user per test run
        String email = "carttest_" + System.currentTimeMillis() + "@test.com";
        RegisterRequest reg = new RegisterRequest("Cart", "Tester", email, "Password@123");
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
        userToken = loginAndGetToken(email, "Password@123");

        // Create a product for cart operations
        String sku = "CART-PROD-" + System.currentTimeMillis();
        ProductRequest product = new ProductRequest(
                "Cart Test Product", "For cart tests", BigDecimal.valueOf(29.99), null, 100, sku, null, true);
        MvcResult res = mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andReturn();
        productId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult res = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    // --- GET /api/cart ---

    @Test
    void getCart_authenticated_returns200() throws Exception {
        mockMvc.perform(get(CART_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void getCart_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(CART_URL))
                .andExpect(status().isUnauthorized());
    }

    // --- POST /api/cart/items ---

    @Test
    void addToCart_validRequest_returns200WithItem() throws Exception {
        AddToCartRequest req = new AddToCartRequest(productId, 2);
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    void addToCart_sameProduct_incrementsQuantity() throws Exception {
        AddToCartRequest req = new AddToCartRequest(productId, 1);
        // Add once
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        // Add again
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    void addToCart_nonExistentProduct_returns404() throws Exception {
        AddToCartRequest req = new AddToCartRequest(999999L, 1);
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToCart_unauthenticated_returns401() throws Exception {
        AddToCartRequest req = new AddToCartRequest(productId, 1);
        mockMvc.perform(post(CART_URL + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addToCart_zeroQuantity_returns400() throws Exception {
        AddToCartRequest req = new AddToCartRequest(productId, 0);
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /api/cart/items/{itemId} ---

    @Test
    void updateCartItem_validRequest_updatesQuantity() throws Exception {
        // Add item first
        AddToCartRequest addReq = new AddToCartRequest(productId, 1);
        MvcResult addResult = mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long itemId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .get("items").get(0).get("id").asLong();

        // Update quantity
        UpdateCartItemRequest updateReq = new UpdateCartItemRequest(5);
        mockMvc.perform(put(CART_URL + "/items/" + itemId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(5));
    }

    @Test
    void updateCartItem_zeroQuantity_returns400() throws Exception {
        UpdateCartItemRequest updateReq = new UpdateCartItemRequest(0);
        mockMvc.perform(put(CART_URL + "/items/1")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /api/cart/items/{itemId} ---

    @Test
    void removeCartItem_existingItem_returns200WithEmptyCart() throws Exception {
        // Add item first
        AddToCartRequest addReq = new AddToCartRequest(productId, 3);
        MvcResult addResult = mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long itemId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .get("items").get(0).get("id").asLong();

        // Remove item
        mockMvc.perform(delete(CART_URL + "/items/" + itemId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void removeCartItem_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete(CART_URL + "/items/1"))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE /api/cart ---

    @Test
    void clearCart_authenticated_returns204() throws Exception {
        // Add item first so cart is non-empty
        AddToCartRequest addReq = new AddToCartRequest(productId, 2);
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk());

        // Clear cart
        mockMvc.perform(delete(CART_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Verify cart is empty
        mockMvc.perform(get(CART_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void clearCart_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete(CART_URL))
                .andExpect(status().isUnauthorized());
    }
}
