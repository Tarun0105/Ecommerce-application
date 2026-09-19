package com.shopverse.service;

import com.shopverse.dto.request.CategoryRequest;
import com.shopverse.dto.response.CategoryResponse;
import com.shopverse.entity.Category;
import com.shopverse.exception.DuplicateResourceException;
import com.shopverse.exception.ResourceNotFoundException;
import com.shopverse.mapper.CategoryMapper;
import com.shopverse.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAll(Sort.by("name")));
    }

    public CategoryResponse getCategoryById(Long id) {
        return categoryMapper.toResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id)));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists.");
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists.");
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }
}
