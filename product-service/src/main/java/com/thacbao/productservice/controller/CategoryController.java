package com.thacbao.productservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.productservice.dto.request.CategoryRequest;
import com.thacbao.productservice.dto.request.SubCategoryRequest;
import com.thacbao.productservice.dto.response.CategoryResponse;
import com.thacbao.productservice.dto.response.SubCategoryResponse;
import com.thacbao.productservice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Public
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getHierarchy() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategoriesWithHierarchy()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryBySlug(slug)));
    }

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<ApiResponse<List<SubCategoryResponse>>> getSubCategories(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getSubCategoriesByCategory(categoryId)));
    }

    @GetMapping("/{categoryId}/subcategories/hierarchy")
    public ResponseEntity<ApiResponse<List<SubCategoryResponse>>> getSubCategoryHierarchy(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getSubCategoryHierarchy(categoryId)));
    }

    @GetMapping("/subcategories/{slug}")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> getSubCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getSubCategoryBySlug(slug)));
    }

    // Admin
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subcategories")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> createSubCategory(@Valid @RequestBody SubCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createSubCategory(request)));
    }

    @PutMapping("/subcategories/{id}")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> updateSubCategory(@PathVariable Integer id, @Valid @RequestBody SubCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateSubCategory(id, request)));
    }

    @DeleteMapping("/subcategories/{id}")
    public ResponseEntity<Void> deleteSubCategory(@PathVariable Integer id) {
        categoryService.deleteSubCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderCategories(@RequestBody List<Integer> categoryIds) {
        categoryService.reorderCategories(categoryIds);
        return ResponseEntity.ok().build();
    }
}
