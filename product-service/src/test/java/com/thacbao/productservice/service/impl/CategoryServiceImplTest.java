package com.thacbao.productservice.service.impl;

import com.thacbao.productservice.dto.request.CategoryRequest;
import com.thacbao.productservice.dto.request.SubCategoryRequest;
import com.thacbao.productservice.dto.response.CategoryResponse;
import com.thacbao.productservice.dto.response.SubCategoryResponse;
import com.thacbao.productservice.model.Category;
import com.thacbao.productservice.model.SubCategory;
import com.thacbao.productservice.repository.jpa.CategoryRepository;
import com.thacbao.productservice.repository.jpa.SubCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private com.thacbao.productservice.service.CloudinaryService cloudinaryService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private SubCategory subCategory;

    @BeforeEach
    void setUp() {
        category = Category.builder().name("Category 1").slug("cat-1").build();
        category.setId(1);

        subCategory = SubCategory.builder().name("Sub 1").slug("sub-1").category(category).build();
        subCategory.setId(1);
    }

    @Test
    void createCategory() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Category 1");
        when(categoryRepository.save(any())).thenReturn(category);
        CategoryResponse res = categoryService.createCategory(req);
        assertNotNull(res);
    }

    @Test
    void updateCategory() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Category 1 updated");
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);
        CategoryResponse res = categoryService.updateCategory(1, req);
        assertNotNull(res);
    }

    @Test
    void deleteCategory() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        categoryService.deleteCategory(1);
        verify(categoryRepository).deleteById(1);
    }

    @Test
    void getCategoryById() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        CategoryResponse res = categoryService.getCategoryById(1);
        assertNotNull(res);
    }

    @Test
    void getCategoryBySlug() {
        when(categoryRepository.findBySlug("cat-1")).thenReturn(Optional.of(category));
        CategoryResponse res = categoryService.getCategoryBySlug("cat-1");
        assertNotNull(res);
    }

    @Test
    void getAllCategories() {
        when(categoryRepository.findAllOrderByDisplayOrder()).thenReturn(List.of(category));
        List<CategoryResponse> res = categoryService.getAllCategories();
        assertNotNull(res);
    }

    @Test
    void getAllCategoriesWithHierarchy() {
        when(categoryRepository.findAllActiveWithSubCategories()).thenReturn(List.of(category));
        List<CategoryResponse> res = categoryService.getAllCategoriesWithHierarchy();
        assertNotNull(res);
    }

    @Test
    void reorderCategories() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        categoryService.reorderCategories(List.of(1));
        verify(categoryRepository).save(any());
    }

    @Test
    void createSubCategory() {
        SubCategoryRequest req = new SubCategoryRequest();
        req.setName("Sub 1");
        req.setCategoryId(1);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(subCategoryRepository.save(any())).thenReturn(subCategory);
        SubCategoryResponse res = categoryService.createSubCategory(req);
        assertNotNull(res);
    }

    @Test
    void updateSubCategory() {
        SubCategoryRequest req = new SubCategoryRequest();
        req.setName("Sub 1 updated");
        when(subCategoryRepository.findById(1)).thenReturn(Optional.of(subCategory));
        when(subCategoryRepository.save(any())).thenReturn(subCategory);
        SubCategoryResponse res = categoryService.updateSubCategory(1, req);
        assertNotNull(res);
    }

    @Test
    void deleteSubCategory() {
        when(subCategoryRepository.findById(1)).thenReturn(Optional.of(subCategory));
        categoryService.deleteSubCategory(1);
        verify(subCategoryRepository).deleteById(1);
    }

    @Test
    void getSubCategoryById() {
        when(subCategoryRepository.findById(1)).thenReturn(Optional.of(subCategory));
        SubCategoryResponse res = categoryService.getSubCategoryById(1);
        assertNotNull(res);
    }

    @Test
    void getSubCategoryBySlug() {
        when(subCategoryRepository.findBySlug("sub-1")).thenReturn(Optional.of(subCategory));
        SubCategoryResponse res = categoryService.getSubCategoryBySlug("sub-1");
        assertNotNull(res);
    }

    @Test
    void getSubCategoriesByCategory() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(subCategoryRepository.findRootSubCategoriesByCategory(category)).thenReturn(List.of(subCategory));
        List<SubCategoryResponse> res = categoryService.getSubCategoriesByCategory(1);
        assertNotNull(res);
    }

    @Test
    void getSubCategoryHierarchy() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(subCategoryRepository.findHierarchyByCategory(category)).thenReturn(List.of(subCategory));
        List<SubCategoryResponse> res = categoryService.getSubCategoryHierarchy(1);
        assertNotNull(res);
    }

    @Test
    void getSubCategoryChildren() {
        when(subCategoryRepository.findById(1)).thenReturn(Optional.of(subCategory));
        when(subCategoryRepository.findChildrenByParent(subCategory)).thenReturn(List.of(subCategory));
        List<SubCategoryResponse> res = categoryService.getSubCategoryChildren(1);
        assertNotNull(res);
    }

    @Test
    void reorderSubCategories() {
        when(subCategoryRepository.findById(1)).thenReturn(Optional.of(subCategory));
        categoryService.reorderSubCategories(1, List.of(1));
        verify(subCategoryRepository, atLeastOnce()).save(any());
    }
}
