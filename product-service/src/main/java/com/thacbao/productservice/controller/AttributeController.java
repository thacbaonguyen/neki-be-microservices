package com.thacbao.productservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import com.thacbao.productservice.service.AttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    // ===== Brand =====
    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllBrands()));
    }
    @GetMapping("/brands/active")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getActiveBrands() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllActiveBrands()));
    }
    @PostMapping("/brands")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.createBrand(request)));
    }
    @PutMapping("/brands/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(@PathVariable Integer id, @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.updateBrand(id, request)));
    }
    @DeleteMapping("/brands/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Integer id) { attributeService.deleteBrand(id); return ResponseEntity.noContent().build(); }

    // ===== Collection =====
    @GetMapping("/collections")
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getAllCollections() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllCollections()));
    }
    @GetMapping("/collections/active")
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getActiveCollections() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllActiveCollections()));
    }
    @GetMapping("/collections/{slug}")
    public ResponseEntity<ApiResponse<CollectionResponse>> getCollectionBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getCollectionBySlug(slug)));
    }
    @PostMapping("/collections")
    public ResponseEntity<ApiResponse<CollectionResponse>> createCollection(@Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.createCollection(request)));
    }
    @PutMapping("/collections/{id}")
    public ResponseEntity<ApiResponse<CollectionResponse>> updateCollection(@PathVariable Integer id, @Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.updateCollection(id, request)));
    }
    @DeleteMapping("/collections/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Integer id) { attributeService.deleteCollection(id); return ResponseEntity.noContent().build(); }

    // ===== Topic =====
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllTopics()));
    }
    @GetMapping("/topics/active")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getActiveTopics() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllActiveTopics()));
    }
    @GetMapping("/topics/{slug}")
    public ResponseEntity<ApiResponse<TopicResponse>> getTopicBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getTopicBySlug(slug)));
    }
    @PostMapping("/topics")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.createTopic(request)));
    }
    @PutMapping("/topics/{id}")
    public ResponseEntity<ApiResponse<TopicResponse>> updateTopic(@PathVariable Integer id, @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.updateTopic(id, request)));
    }
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Integer id) { attributeService.deleteTopic(id); return ResponseEntity.noContent().build(); }

    // ===== Color =====
    @GetMapping("/colors")
    public ResponseEntity<ApiResponse<List<ColorResponse>>> getAllColors() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllColors()));
    }
    @PostMapping("/colors")
    public ResponseEntity<ApiResponse<ColorResponse>> createColor(@Valid @RequestBody ColorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.createColor(request)));
    }
    @PutMapping("/colors/{id}")
    public ResponseEntity<ApiResponse<ColorResponse>> updateColor(@PathVariable Integer id, @Valid @RequestBody ColorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.updateColor(id, request)));
    }
    @DeleteMapping("/colors/{id}")
    public ResponseEntity<Void> deleteColor(@PathVariable Integer id) { attributeService.deleteColor(id); return ResponseEntity.noContent().build(); }

    // ===== Size =====
    @GetMapping("/sizes")
    public ResponseEntity<ApiResponse<List<SizeResponse>>> getAllSizes() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllSizes()));
    }
    @GetMapping("/sizes/{categoryType}")
    public ResponseEntity<ApiResponse<List<SizeResponse>>> getSizesByCategoryType(@PathVariable String categoryType) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getSizesByCategoryType(categoryType)));
    }
    @PostMapping("/sizes")
    public ResponseEntity<ApiResponse<SizeResponse>> createSize(@Valid @RequestBody SizeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.createSize(request)));
    }
    @PutMapping("/sizes/{id}")
    public ResponseEntity<ApiResponse<SizeResponse>> updateSize(@PathVariable Integer id, @Valid @RequestBody SizeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.updateSize(id, request)));
    }
    @DeleteMapping("/sizes/{id}")
    public ResponseEntity<Void> deleteSize(@PathVariable Integer id) { attributeService.deleteSize(id); return ResponseEntity.noContent().build(); }
}
