package com.shopverse.mapper;
import com.shopverse.dto.response.CategoryResponse;
import com.shopverse.entity.Category;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category c) {
        if (c == null) return null;
        return CategoryResponse.builder()
                .id(c.getId()).name(c.getName())
                .description(c.getDescription()).createdAt(c.getCreatedAt())
                .build();
    }
    public List<CategoryResponse> toResponseList(List<Category> list) {
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
