package com.shopverse.service;

import com.shopverse.dto.request.ProductRequest;
import com.shopverse.dto.response.CategoryResponse;
import com.shopverse.dto.response.PageResponse;
import com.shopverse.dto.response.ProductResponse;
import com.shopverse.entity.Category;
import com.shopverse.entity.Product;
import com.shopverse.exception.DuplicateResourceException;
import com.shopverse.exception.ResourceNotFoundException;
import com.shopverse.mapper.ProductMapper;
import com.shopverse.repository.CategoryRepository;
import com.shopverse.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    ProductMapper productMapper;
    @InjectMocks
    ProductService productService;

    private Product sampleProduct() {
        return Product.builder()
                .id(1L).name("Test Product").sku("TEST-001")
                .price(BigDecimal.valueOf(29.99)).stockQuantity(50).active(true)
                .build();
    }

    private ProductResponse sampleProductResponse() {
        ProductResponse resp = new ProductResponse();
        resp.setId(1L);
        resp.setName("Test Product");
        resp.setSku("TEST-001");
        resp.setPrice(BigDecimal.valueOf(29.99));
        resp.setStockQuantity(50);
        resp.setActive(true);
        return resp;
    }

    // --- getProductById() ---

    @Test
    void getProductById_existing_returnsResponse() {
        Product p = sampleProduct();
        ProductResponse resp = sampleProductResponse();
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(p));
        when(productMapper.toResponse(p)).thenReturn(resp);

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- getProductByIdAdmin() ---

    @Test
    void getProductByIdAdmin_inactive_stillReturnsResponse() {
        Product p = sampleProduct();
        p.setActive(false);
        ProductResponse resp = sampleProductResponse();
        resp.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productMapper.toResponse(p)).thenReturn(resp);

        ProductResponse result = productService.getProductByIdAdmin(1L);
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void getProductByIdAdmin_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductByIdAdmin(88L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getAllProducts() ---

    @Test
    void getAllProducts_noFilters_returnsPageResponse() {
        Product p = sampleProduct();
        Page<Product> productPage = new PageImpl<>(List.of(p));
        Page<ProductResponse> responsePage = new PageImpl<>(List.of(sampleProductResponse()));
        when(productRepository.findAllWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponsePage(productPage)).thenReturn(responsePage);

        PageResponse<ProductResponse> result = productService.getAllProducts(0, 12, "newest", null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllProducts_withBlankSearch_treatsAsNull() {
        Page<Product> emptyPage = new PageImpl<>(List.of());
        Page<ProductResponse> emptyResponsePage = new PageImpl<>(List.of());
        when(productRepository.findAllWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(productMapper.toResponsePage(emptyPage)).thenReturn(emptyResponsePage);

        PageResponse<ProductResponse> result = productService.getAllProducts(0, 12, "price_asc", "  ", null, null, null);
        assertThat(result.getContent()).isEmpty();
    }

    // --- createProduct() ---

    @Test
    void createProduct_duplicateSku_throwsDuplicateResourceException() {
        ProductRequest req = new ProductRequest("Name", "Desc", BigDecimal.TEN, null, 10, "DUPE-SKU", null, true);
        when(productRepository.existsBySku("DUPE-SKU")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("DUPE-SKU");
    }

    @Test
    void createProduct_withoutCategory_savesProduct() {
        ProductRequest req = new ProductRequest("Widget", "A widget", BigDecimal.valueOf(9.99), null, 100, "WID-001", null, true);
        Product saved = sampleProduct();
        ProductResponse resp = sampleProductResponse();
        when(productRepository.existsBySku("WID-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(resp);

        ProductResponse result = productService.createProduct(req);

        assertThat(result).isNotNull();
        verify(categoryRepository, never()).findById(anyLong());
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isNull();
    }

    @Test
    void createProduct_withValidCategory_associatesCategory() {
        Category cat = Category.builder().id(5L).name("Electronics").build();
        ProductRequest req = new ProductRequest("Phone", "Smartphone", BigDecimal.valueOf(499.99), 5L, 20, "PHN-001", null, true);
        Product saved = sampleProduct();
        ProductResponse resp = sampleProductResponse();
        when(productRepository.existsBySku("PHN-001")).thenReturn(false);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(cat));
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(resp);

        productService.createProduct(req);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo(cat);
    }

    @Test
    void createProduct_withInvalidCategory_throwsResourceNotFoundException() {
        ProductRequest req = new ProductRequest("Widget", "Desc", BigDecimal.TEN, 999L, 5, "WID-002", null, true);
        when(productRepository.existsBySku("WID-002")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // --- updateProduct() ---

    @Test
    void updateProduct_notFound_throwsResourceNotFoundException() {
        ProductRequest req = new ProductRequest("New Name", "Desc", BigDecimal.TEN, null, 5, "NEW-SKU", null, true);
        when(productRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(77L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProduct_duplicateSkuOnOtherProduct_throwsDuplicateResourceException() {
        Product existing = sampleProduct();
        ProductRequest req = new ProductRequest("Updated", "Desc", BigDecimal.TEN, null, 5, "TAKEN-SKU", null, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuAndIdNot("TAKEN-SKU", 1L)).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(1L, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("TAKEN-SKU");
    }

    @Test
    void updateProduct_sameSku_updatesSuccessfully() {
        Product existing = sampleProduct();
        ProductRequest req = new ProductRequest("Updated Name", "New Desc", BigDecimal.valueOf(39.99), null, 25, "TEST-001", null, true);
        Product saved = Product.builder().id(1L).name("Updated Name").sku("TEST-001")
                .price(BigDecimal.valueOf(39.99)).stockQuantity(25).active(true).build();
        ProductResponse resp = new ProductResponse();
        resp.setId(1L);
        resp.setName("Updated Name");
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuAndIdNot("TEST-001", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(resp);

        ProductResponse result = productService.updateProduct(1L, req);

        assertThat(result.getName()).isEqualTo("Updated Name");
    }

    // --- deleteProduct() ---

    @Test
    void deleteProduct_setsProductInactive() {
        Product p = sampleProduct();
        assertThat(p.isActive()).isTrue();
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenReturn(p);

        productService.deleteProduct(1L);

        assertThat(p.isActive()).isFalse();
        verify(productRepository).save(p);
    }

    @Test
    void deleteProduct_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
