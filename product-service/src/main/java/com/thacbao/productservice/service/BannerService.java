package com.thacbao.productservice.service;

import com.thacbao.productservice.dto.request.BannerRequest;
import com.thacbao.productservice.dto.response.BannerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BannerService {
    BannerResponse create(BannerRequest request);
    BannerResponse updateBanner(Integer bannerId, BannerRequest request);
    BannerResponse uploadBannerImage(Integer bannerId, MultipartFile file);
    void deleteBanner(Integer bannerId);
    BannerResponse toggleActive(Integer bannerId);
    BannerResponse togglePinned(Integer bannerId);
    Page<BannerResponse> getAllBanner(Pageable pageable);
    BannerResponse findFirstPinned();
}
