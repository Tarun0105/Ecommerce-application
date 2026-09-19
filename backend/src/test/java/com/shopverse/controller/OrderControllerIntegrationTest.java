package com.shopverse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopverse.dto.request.AddToCartRequest;
import com.shopverse.dto.request.CheckoutRequest;
import com.shopverse.dto.request.LoginRequest;
import com.shopverse.dto.request.ProductRequest;
import com.shopverse.dto.request.RegisterRequest;
import com.shopverse.dto.request.ShippingAddressRequest;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController.
 * Each test class run creates its own isolated user and product to avoid cross-test pollution.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    static final String ORDERS_URL   = "/api/orders";
    static final String CART_URL     = "/api/cart";
    static final String PRODUCTS_URL = "/api/products";
    static final String LOGIN_URL    = "/api/auth/login";
    static final String REGISTER_URL = "/api/auth/register";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private Long   productId;
    private ShippingAddressRequest shippingAddress;

    @BeforeEach
    void setup() throws Exception {
        adminToken = loginAndGetToken("testadmin@shopverse.com", "TestAdmin@123");

        // Register unique user per test run
        String email = "ordertest_" + System.currentTimeMillis() + "@test.com";
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Order", "Tester", email, "Password@123"))))
                .andExpect(status().isCreated());
        userToken = loginAndGetToken(email, "Password@123");

        // Create a product with enough stock for testing
        String sku = "ORD-PROD-" + System.currentTimeMillis();
        ProductRequest product = new ProductRequest(
                "Order Test Product", "For order tests", BigDecimal.valueOf(50.00), null, 100, sku, null, true);
        MvcResult res = mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andReturn();
        productId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        shippingAddress = new ShippingAddressRequest(
                "Test", "User", "123 Main St", "Springfield", "IL", "62701", "US");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult res = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    /** Adds the given product to the current user's cart. */
    private void addProductToCart(int quantity) throws Exception {
        AddToCartRequest addReq = new AddToCartRequest(productId, quantity);
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk());
    }

    /** Places a checkout and returns the created order ID. */
    private Long checkout() throws Exception {
        CheckoutRequest req = new CheckoutRequest(shippingAddress, null);
        MvcResult res = mockMvc.perform(post(ORDERS_URL + "/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    // --- POST /api/orders/checkout ---

    @Test
    void checkout_withItemsInCart_returns201AndClearsCart() throws Exception {
        addProductToCart(2);
        CheckoutRequest req = new CheckoutRequest(shippingAddress, "Handle with care");

        mockMvc.perform(post(ORDERS_URL + "/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.notes").value("Handle with care"))
                .andExpect(jsonPath("$.items").isArray());

        // Cart should now be empty
        mockMvc.perform(get(CART_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void checkout_emptyCart_returns400() throws Exception {
        // Do not add anything to cart
        CheckoutRequest req = new CheckoutRequest(shippingAddress, null);
        mockMvc.perform(post(ORDERS_URL + "/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_unauthenticated_returns401() throws Exception {
        CheckoutRequest req = new CheckoutRequest(shippingAddress, null);
        mockMvc.perform(post(ORDERS_URL + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkout_missingShippingAddress_returns400() throws Exception {
        addProductToCart(1);
        CheckoutRequest req = new CheckoutRequest(null, null);
        mockMvc.perform(post(ORDERS_URL + "/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/orders ---

    @Test
    void getMyOrders_authenticated_returns200WithPage() throws Exception {
        addProductToCart(1);
        checkout();

        mockMvc.perform(get(ORDERS_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getMyOrders_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(ORDERS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyOrders_noOrders_returnsEmptyPage() throws Exception {
        mockMvc.perform(get(ORDERS_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // --- GET /api/orders/{id} ---

    @Test
    void getOrderById_ownOrder_returns200() throws Exception {
        addProductToCart(1);
        Long orderId = checkout();

        mockMvc.perform(get(ORDERS_URL + "/" + orderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrderById_notFound_returns404() throws Exception {
        mockMvc.perform(get(ORDERS_URL + "/99999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderById_otherUsersOrder_returns403() throws Exception {
        // Create another user and their order
        String otherEmail = "other_order_" + System.currentTimeMillis() + "@test.com";
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Other", "User", otherEmail, "Password@123"))))
                .andExpect(status().isCreated());
        String otherToken = loginAndGetToken(otherEmail, "Password@123");

        // Other user adds to cart and checks out
        AddToCartRequest addReq = new AddToCartRequest(productId, 1);
        mockMvc.perform(post(CART_URL + "/items")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk());

        CheckoutRequest req = new CheckoutRequest(shippingAddress, null);
        MvcResult otherOrderResult = mockMvc.perform(post(ORDERS_URL + "/checkout")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        Long otherOrderId = objectMapper.readTree(otherOrderResult.getResponse().getContentAsString()).get("id").asLong();

        // Current user tries to access other user's order
        mockMvc.perform(get(ORDERS_URL + "/" + otherOrderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // --- PUT /api/orders/{id}/cancel ---

    @Test
    void cancelOrder_pendingOrder_returns200WithCancelledStatus() throws Exception {
        addProductToCart(1);
        Long orderId = checkout();

        mockMvc.perform(put(ORDERS_URL + "/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_notFound_returns404() throws Exception {
        mockMvc.perform(put(ORDERS_URL + "/99999/cancel")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put(ORDERS_URL + "/1/cancel"))
                .andExpect(status().isUnauthorized());
    }
}
