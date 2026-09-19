package com.shopverse.service;

import com.shopverse.dto.request.CategoryRequest;
import com.shopverse.dto.response.CategoryResponse;
import com.shopverse.entity.Category;
import com.shopverse.exception.DuplicateResourceException;
import com.shopverse.exception.ResourceNotFoundException;
import com.shopverse.mapper.CategoryMapper;
import com.shopverse.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;
    @Mock
    CategoryMapper categoryMapper;
    @InjectMocks
    CategoryService categoryService;

    private Category sampleCategory() {
        return Category.builder().id(1L).name("Electronics").description("Electronic devices").build();
    }

    private CategoryResponse sampleCategoryResponse() {
        CategoryResponse resp = new CategoryResponse();
        resp.setId(1L);
        resp.setName("Electronics");
        return resp;
    }

    // --- getAllCategories() ---

    @Test
    void getAllCategories_returnsMappedList() {
        List<Category> categories = List.of(sampleCategory(), Category.builder().id(2L).name("Clothing").build());
        CategoryResponse r1 = sampleCategoryResponse();
        CategoryResponse r2 = new CategoryResponse();
        r2.setId(2L);
        r2.setName("Clothing");
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(categories);
        when(categoryMapper.toResponseList(categories)).thenReturn(List.of(r1, r2));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(1).getName()).isEqualTo("Clothing");
    }

    @Test
    void getAllCategories_empty_returnsEmptyList() {
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of());
        when(categoryMapper.toResponseList(List.of())).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategories();
        assertThat(result).isEmpty();
    }

    @Test
    void getAllCategories_sortsByName() {
        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of());
        when(categoryMapper.toResponseList(any())).thenReturn(List.of());

        categoryService.getAllCategories();

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(categoryRepository).findAll(sortCaptor.capture());
        Sort capturedSort = sortCaptor.getValue();
        assertThat(capturedSort.getOrderFor("name")).isNotNull();
    }

    // --- getCategoryById() ---

    @Test
    void getCategoryById_existing_returnsResponse() {
        Category cat = sampleCategory();
        CategoryResponse resp = sampleCategoryResponse();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryMapper.toResponse(cat)).thenReturn(resp);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    void getCategoryById_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- createCategory() ---

    @Test
    void createCategory_success_returnsMappedResponse() {
        CategoryRequest req = new CategoryRequest("New Category", "A description");
        Category saved = Category.builder().id(3L).name("New Category").description("A description").build();
        CategoryResponse resp = new CategoryResponse();
        resp.setId(3L);
        resp.setName("New Category");
        when(categoryRepository.existsByName("New Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(resp);

        CategoryResponse result = categoryService.createCategory(req);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("New Category");
    }

    @Test
    void createCategory_duplicateName_throwsDuplicateResourceException() {
        CategoryRequest req = new CategoryRequest("Electronics", null);
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Electronics");
    }

    @Test
    void createCategory_savesCorrectFields() {
        CategoryRequest req = new CategoryRequest("Books", "All genres");
        Category saved = Category.builder().id(4L).name("Books").description("All genres").build();
        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(new CategoryResponse());

        categoryService.createCategory(req);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Books");
        assertThat(captor.getValue().getDescription()).isEqualTo("All genres");
    }

    @Test
    void createCategory_doesNotThrowOnValidRequest() {
        CategoryRequest req = new CategoryRequest("Sports", "Sports equipment");
        when(categoryRepository.existsByName(any())).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(sampleCategory());
        when(categoryMapper.toResponse(any())).thenReturn(new CategoryResponse());

        assertThatCode(() -> categoryService.createCategory(req)).doesNotThrowAnyException();
    }

    // --- updateCategory() ---

    @Test
    void updateCategory_notFound_throwsResourceNotFoundException() {
        CategoryRequest req = new CategoryRequest("Updated", "Desc");
        when(categoryRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(77L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCategory_duplicateName_throwsDuplicateResourceException() {
        Category existing = sampleCategory();
        CategoryRequest req = new CategoryRequest("Clothing", "Apparel");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndIdNot("Clothing", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(1L, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Clothing");
    }

    @Test
    void updateCategory_success_updatesFields() {
        Category existing = sampleCategory();
        CategoryRequest req = new CategoryRequest("Updated Name", "New Desc");
        Category saved = Category.builder().id(1L).name("Updated Name").description("New Desc").build();
        CategoryResponse resp = new CategoryResponse();
        resp.setName("Updated Name");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndIdNot("Updated Name", 1L)).thenReturn(false);
        when(categoryRepository.save(existing)).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(resp);

        CategoryResponse result = categoryService.updateCategory(1L, req);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(existing.getName()).isEqualTo("Updated Name");
        assertThat(existing.getDescription()).isEqualTo("New Desc");
    }

    // --- deleteCategory() ---

    @Test
    void deleteCategory_existing_deletesById() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(categoryRepository, never()).deleteById(any());
    }
}
