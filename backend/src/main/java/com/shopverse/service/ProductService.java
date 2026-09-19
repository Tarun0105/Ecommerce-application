package com.shopverse.service;

import com.shopverse.dto.request.ProductRequest;
import com.shopverse.dto.response.*;
import com.shopverse.entity.*;
import com.shopverse.exception.*;
import com.shopverse.mapper.ProductMapper;
import com.shopverse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sort,
                                                         String search, Long categoryId,
                                                         BigDecimal minPrice, BigDecimal maxPrice) {
        Sort sortObj = buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        String searchParam = StringUtils.hasText(search) ? search : null;
        Page<Product> products = productRepository.findAllWithFilters(categoryId, minPrice, maxPrice, searchParam, pageable);
        return PageResponse.from(productMapper.toResponsePage(products));
    }

    public PageResponse<ProductResponse> getAllProductsAdmin(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchParam = StringUtils.hasText(search) ? search : null;
        Page<Product> products = productRepository.findAllAdminWithFilters(searchParam, pageable);
        return PageResponse.from(productMapper.toResponsePage(products));
    }

    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }

    public ProductResponse getProductByIdAdmin(Long id) {
        return productMapper.toResponse(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id)));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists.");
        }
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        }
        Product product = Product.builder()
                .name(request.getName()).description(request.getDescription())
                .price(request.getPrice()).category(category)
                .stockQuantity(request.getStockQuantity()).sku(request.getSku())
                .imageUrl(request.getImageUrl()).active(request.isActive())
                .build();
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists.");
        }
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        }
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.isActive());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
    }

    private Sort buildSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "price_asc"  -> Sort.by(Sort.Direction.ASC,  "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_asc"   -> Sort.by(Sort.Direction.ASC,  "name");
            case "name_desc"  -> Sort.by(Sort.Direction.DESC, "name");
            case "newest"     -> Sort.by(Sort.Direction.DESC, "createdAt");
            default           -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
