package com.thacbao.productservice.service;

import com.thacbao.productservice.dto.request.CategoryRequest;
import com.thacbao.productservice.dto.request.SubCategoryRequest;
import com.thacbao.productservice.dto.response.CategoryResponse;
import com.thacbao.productservice.dto.response.SubCategoryResponse;
import java.util.List;

public interface CategoryService {
    // Category CRUD
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Integer id, CategoryRequest request);
    void deleteCategory(Integer id);
    CategoryResponse getCategoryById(Integer id);
    CategoryResponse getCategoryBySlug(String slug);
    List<CategoryResponse> getAllCategories();
    List<CategoryResponse> getAllCategoriesWithHierarchy();
    void reorderCategories(List<Integer> categoryIds);

    // SubCategory CRUD
    SubCategoryResponse createSubCategory(SubCategoryRequest request);
    SubCategoryResponse updateSubCategory(Integer id, SubCategoryRequest request);
    void deleteSubCategory(Integer id);
    SubCategoryResponse getSubCategoryById(Integer id);
    SubCategoryResponse getSubCategoryBySlug(String slug);
    List<SubCategoryResponse> getSubCategoriesByCategory(Integer categoryId);
    List<SubCategoryResponse> getSubCategoryHierarchy(Integer categoryId);
    List<SubCategoryResponse> getSubCategoryChildren(Integer parentId);
    void reorderSubCategories(Integer parentId, List<Integer> subCategoryIds);
}
