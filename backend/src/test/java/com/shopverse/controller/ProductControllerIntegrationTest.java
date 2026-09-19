package com.shopverse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopverse.dto.request.LoginRequest;
import com.shopverse.dto.request.ProductRequest;
import com.shopverse.dto.request.RegisterRequest;
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
 * Integration tests for ProductController.
 * Uses the H2 in-memory database (application-test.yml, active profile "test").
 * Admin credentials come from the DataInitializer seeded at startup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    static final String PRODUCTS_URL = "/api/products";
    static final String LOGIN_URL    = "/api/auth/login";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void obtainTokens() throws Exception {
        adminToken = loginAndGetToken("testadmin@shopverse.com", "TestAdmin@123");
        userToken  = registerAndLoginUser();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String registerAndLoginUser() throws Exception {
        String email = "prodtest_user_" + System.currentTimeMillis() + "@test.com";
        RegisterRequest reg = new RegisterRequest("Prod", "Tester", email, "Password@123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andReturn();
        return loginAndGetToken(email, "Password@123");
    }

    private ProductRequest uniqueProduct(String suffix) {
        return new ProductRequest(
                "Test Product " + suffix, "Integration product", BigDecimal.valueOf(49.99),
                null, 20, "SKU-" + suffix, null, true);
    }

    // --- GET /api/products ---

    @Test
    void getProducts_public_returns200WithPage() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.size").exists());
    }

    @Test
    void getProducts_withPagination_returnsCorrectPageSize() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL).param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void getProducts_withSearch_filtersResults() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL).param("search", "nonexistentproductxyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getProducts_withPriceFilter_returns200() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL)
                        .param("minPrice", "10.00")
                        .param("maxPrice", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // --- GET /api/products/{id} ---

    @Test
    void getProductById_notFound_returns404() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL + "/99999"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/products ---

    @Test
    void createProduct_asAdmin_returns201() throws Exception {
        String sku = "INT-" + System.currentTimeMillis();
        ProductRequest req = new ProductRequest(
                "Integration Product", "Desc", BigDecimal.valueOf(19.99), null, 10, sku, null, true);

        mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Product"))
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createProduct_unauthenticated_returns401() throws Exception {
        ProductRequest req = uniqueProduct("NOAUTH-" + System.currentTimeMillis());
        mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_asRegularUser_returns403() throws Exception {
        ProductRequest req = uniqueProduct("USER-" + System.currentTimeMillis());
        mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_duplicateSku_returns409() throws Exception {
        String sku = "DUPE-" + System.currentTimeMillis();
        ProductRequest req = new ProductRequest("First Product", "Desc", BigDecimal.TEN, null, 5, sku, null, true);

        // Create first
        mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Try duplicate
        mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void createProduct_invalidPrice_returns400() throws Exception {
        ProductRequest req = new ProductRequest(
                "Bad Price Product", "Desc", BigDecimal.valueOf(-1.00), null, 5, "NEG-PRICE-001", null, true);
        mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_missingSku_returns400() throws Exception {
        ProductRequest req = new ProductRequest("No SKU", "Desc", BigDecimal.TEN, null, 5, null, null, true);
        mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /api/products/{id} ---

    @Test
    void updateProduct_asAdmin_returns200() throws Exception {
        // First create
        String sku = "UPD-" + System.currentTimeMillis();
        ProductRequest createReq = new ProductRequest("Original", "Desc", BigDecimal.TEN, null, 10, sku, null, true);
        MvcResult created = mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long productId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Then update
        ProductRequest updateReq = new ProductRequest("Updated Name", "New Desc", BigDecimal.valueOf(15.00), null, 20, sku, null, true);
        mockMvc.perform(put(PRODUCTS_URL + "/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.stockQuantity").value(20));
    }

    @Test
    void updateProduct_notFound_returns404() throws Exception {
        ProductRequest req = new ProductRequest("Ghost", "Desc", BigDecimal.TEN, null, 5, "GHOST-SKU-X", null, true);
        mockMvc.perform(put(PRODUCTS_URL + "/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /api/products/{id} ---

    @Test
    void deleteProduct_asAdmin_returns204() throws Exception {
        String sku = "DEL-" + System.currentTimeMillis();
        ProductRequest createReq = new ProductRequest("To Delete", "Desc", BigDecimal.TEN, null, 5, sku, null, true);
        MvcResult created = mockMvc.perform(post(PRODUCTS_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long productId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete(PRODUCTS_URL + "/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Soft-deleted product should no longer appear in public listing
        mockMvc.perform(get(PRODUCTS_URL + "/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete(PRODUCTS_URL + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteProduct_asRegularUser_returns403() throws Exception {
        mockMvc.perform(delete(PRODUCTS_URL + "/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
