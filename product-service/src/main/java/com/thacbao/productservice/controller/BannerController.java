package com.thacbao.productservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.productservice.dto.request.BannerRequest;
import com.thacbao.productservice.dto.response.BannerResponse;
import com.thacbao.productservice.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BannerResponse>>> getAllBanners(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.getAllBanner(pageable)));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<BannerResponse>> getPinnedBanner() {
        return ResponseEntity.ok(ApiResponse.success(bannerService.findFirstPinned()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BannerResponse>> createBanner(@Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> updateBanner(@PathVariable Integer id, @Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.updateBanner(id, request)));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ApiResponse<BannerResponse>> uploadImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.uploadBannerImage(id, file)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Integer id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<BannerResponse>> toggleActive(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.toggleActive(id)));
    }

    @PatchMapping("/{id}/toggle-pinned")
    public ResponseEntity<ApiResponse<BannerResponse>> togglePinned(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.togglePinned(id)));
    }
}
