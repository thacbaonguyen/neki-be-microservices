package com.thacbao.productservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.productservice.dto.request.BannerRequest;
import com.thacbao.productservice.dto.response.BannerResponse;
import com.thacbao.productservice.model.Banner;
import com.thacbao.productservice.repository.jpa.BannerRepository;
import com.thacbao.productservice.service.BannerService;
import com.thacbao.productservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final CloudinaryService cloudinaryService;

    @Override @Transactional
    public BannerResponse create(BannerRequest request) {
        Banner banner = Banner.builder().title(request.getTitle())
                .linkUrl(request.getLinkUrl()).startDate(request.getStartDate()).endDate(request.getEndDate())
                .isActive(request.isActive()).isPin(request.isPin()).imageUrl("").build();
        return BannerResponse.from(bannerRepository.save(banner));
    }

    @Override @Transactional
    public BannerResponse updateBanner(Integer bannerId, BannerRequest request) {
        Banner banner = bannerRepository.findById(bannerId).orElseThrow(() -> new NotFoundException("Banner not found with id: " + bannerId));
        banner.setTitle(request.getTitle());
        banner.setLinkUrl(request.getLinkUrl());
        banner.setStartDate(request.getStartDate());
        banner.setEndDate(request.getEndDate());
        banner.setIsActive(request.isActive());
        banner.setIsPin(request.isPin());
        return BannerResponse.from(bannerRepository.save(banner));
    }

    @Override @Transactional
    public BannerResponse uploadBannerImage(Integer bannerId, MultipartFile file) {
        Banner banner = bannerRepository.findById(bannerId).orElseThrow(() -> new NotFoundException("Banner not found with id: " + bannerId));
        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
            cloudinaryService.deleteImage(banner.getImageUrl());
        }
        String imageUrl = cloudinaryService.uploadImage(file, "neki/banners");
        banner.setImageUrl(imageUrl);
        return BannerResponse.from(bannerRepository.save(banner));
    }

    @Override @Transactional
    public void deleteBanner(Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId).orElseThrow(() -> new NotFoundException("Banner not found with id: " + bannerId));
        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
            cloudinaryService.deleteImage(banner.getImageUrl());
        }
        bannerRepository.delete(banner);
    }

    @Override @Transactional
    public BannerResponse toggleActive(Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId).orElseThrow(() -> new NotFoundException("Banner not found with id: " + bannerId));
        banner.setIsActive(!banner.getIsActive());
        return BannerResponse.from(bannerRepository.save(banner));
    }

    @Override @Transactional
    public BannerResponse togglePinned(Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId).orElseThrow(() -> new NotFoundException("Banner not found with id: " + bannerId));
        banner.setIsPin(!banner.getIsPin());
        return BannerResponse.from(bannerRepository.save(banner));
    }

    @Override
    public Page<BannerResponse> getAllBanner(Pageable pageable) {
        return bannerRepository.findAll(pageable).map(BannerResponse::from);
    }

    @Override
    public BannerResponse findFirstPinned() {
        return bannerRepository.findFirstByIsPinTrueOrderByCreatedAtDesc()
                .map(BannerResponse::from).orElse(null);
    }
}
