package com.thacbao.productservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import com.thacbao.productservice.model.*;
import com.thacbao.productservice.repository.jpa.*;
import com.thacbao.productservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    // ========== Category ==========

    @Override @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("Danh mục đã tồn tại: " + request.getName());
        Category category = Category.builder().name(request.getName())
                .slug(toSlug(request.getName())).description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true).build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        category.setName(request.getName());
        category.setSlug(toSlug(request.getName()));
        category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override @Transactional
    public void deleteCategory(Integer id) {
        categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        return CategoryResponse.fromWithSubCategories(category);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("Category not found with slug: " + slug));
        return CategoryResponse.fromWithSubCategories(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllOrderByDisplayOrder().stream()
                .map(CategoryResponse::fromWithSubCategories).collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getAllCategoriesWithHierarchy() {
        return categoryRepository.findAllActiveWithSubCategories().stream()
                .map(cat -> {
                    CategoryResponse resp = CategoryResponse.from(cat);
                    List<SubCategory> rootSubs = subCategoryRepository.findHierarchyByCategory(cat);
                    resp.setSubCategories(rootSubs.stream()
                            .sorted(Comparator.comparing(SubCategory::getDisplayOrder))
                            .map(SubCategoryResponse::fromWithChildren)
                            .collect(Collectors.toList()));
                    return resp;
                }).collect(Collectors.toList());
    }

    @Override @Transactional
    public void reorderCategories(List<Integer> categoryIds) {
        AtomicInteger order = new AtomicInteger(0);
        categoryIds.forEach(id -> {
            Category cat = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
            cat.setDisplayOrder(order.getAndIncrement());
            categoryRepository.save(cat);
        });
    }

    // ========== SubCategory ==========

    @Override @Transactional
    public SubCategoryResponse createSubCategory(SubCategoryRequest request) {
        if (subCategoryRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("Danh mục con đã tồn tại: " + request.getName());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + request.getCategoryId()));
        SubCategory parent = null;
        int level = 1;
        if (request.getParentId() != null) {
            parent = subCategoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("SubCategory not found: " + request.getParentId()));
            level = parent.getLevel() + 1;
        }
        SubCategory subCategory = SubCategory.builder().category(category).parent(parent)
                .name(request.getName()).slug(toSlug(request.getName())).description(request.getDescription())
                .level(level).displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true).build();
        return SubCategoryResponse.from(subCategoryRepository.save(subCategory));
    }

    @Override @Transactional
    public SubCategoryResponse updateSubCategory(Integer id, SubCategoryRequest request) {
        SubCategory subCategory = subCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("SubCategory not found with id: " + id));
        subCategory.setName(request.getName());
        subCategory.setSlug(toSlug(request.getName()));
        subCategory.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) subCategory.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) subCategory.setIsActive(request.getIsActive());
        return SubCategoryResponse.from(subCategoryRepository.save(subCategory));
    }

    @Override @Transactional
    public void deleteSubCategory(Integer id) {
        subCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("SubCategory not found with id: " + id));
        subCategoryRepository.deleteById(id);
    }

    @Override
    public SubCategoryResponse getSubCategoryById(Integer id) {
        return SubCategoryResponse.from(subCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("SubCategory not found with id: " + id)));
    }

    @Override
    public SubCategoryResponse getSubCategoryBySlug(String slug) {
        return SubCategoryResponse.from(subCategoryRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("SubCategory not found with slug: " + slug)));
    }

    @Override
    public List<SubCategoryResponse> getSubCategoriesByCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        return subCategoryRepository.findRootSubCategoriesByCategory(category).stream()
                .map(SubCategoryResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<SubCategoryResponse> getSubCategoryHierarchy(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        return subCategoryRepository.findHierarchyByCategory(category).stream()
                .map(SubCategoryResponse::fromWithChildren).collect(Collectors.toList());
    }

    @Override
    public List<SubCategoryResponse> getSubCategoryChildren(Integer parentId) {
        SubCategory parent = subCategoryRepository.findById(parentId).orElseThrow(() -> new NotFoundException("SubCategory not found with id: " + parentId));
        return subCategoryRepository.findChildrenByParent(parent).stream()
                .map(SubCategoryResponse::from).collect(Collectors.toList());
    }

    @Override @Transactional
    public void reorderSubCategories(Integer parentId, List<Integer> subCategoryIds) {
        AtomicInteger order = new AtomicInteger(0);
        subCategoryIds.forEach(id -> {
            SubCategory sc = subCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("SubCategory not found with id: " + id));
            sc.setDisplayOrder(order.getAndIncrement());
            subCategoryRepository.save(sc);
        });
    }

    private String toSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-").replaceAll("-+", "-");
    }
}
