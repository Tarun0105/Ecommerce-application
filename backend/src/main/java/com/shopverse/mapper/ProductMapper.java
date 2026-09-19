package com.shopverse.mapper;
import com.shopverse.dto.response.*;
import com.shopverse.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final CategoryMapper categoryMapper;

    public ProductResponse toResponse(Product p) {
        if (p == null) return null;
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .price(p.getPrice()).category(categoryMapper.toResponse(p.getCategory()))
                .stockQuantity(p.getStockQuantity()).sku(p.getSku())
                .imageUrl(p.getImageUrl()).active(p.isActive())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }

    public Page<ProductResponse> toResponsePage(Page<Product> page) {
        return page.map(this::toResponse);
    }
}
