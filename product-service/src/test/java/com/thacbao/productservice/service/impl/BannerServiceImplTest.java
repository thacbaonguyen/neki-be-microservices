package com.thacbao.productservice.service.impl;

import com.thacbao.productservice.dto.request.BannerRequest;
import com.thacbao.productservice.dto.response.BannerResponse;
import com.thacbao.productservice.model.Banner;
import com.thacbao.productservice.repository.jpa.BannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private com.thacbao.productservice.service.CloudinaryService cloudinaryService;

    @InjectMocks
    private BannerServiceImpl bannerService;

    private Banner banner;

    @BeforeEach
    void setUp() {
        banner = Banner.builder().title("Title").linkUrl("link").isActive(true).isPin(false).build();
        banner.setId(1);
    }

    @Test
    void create() {
        BannerRequest req = new BannerRequest();
        req.setTitle("Title");
        when(bannerRepository.save(any())).thenReturn(banner);
        BannerResponse res = bannerService.create(req);
        assertNotNull(res);
    }

    @Test
    void updateBanner() {
        BannerRequest req = new BannerRequest();
        req.setTitle("Updated Title");
        when(bannerRepository.findById(1)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any())).thenReturn(banner);
        BannerResponse res = bannerService.updateBanner(1, req);
        assertNotNull(res);
    }

    @Test
    void uploadBannerImage() {
        when(bannerRepository.findById(1)).thenReturn(Optional.of(banner));
        when(cloudinaryService.uploadImage(any(), anyString())).thenReturn("new_url");
        when(bannerRepository.save(any())).thenReturn(banner);
        BannerResponse res = bannerService.uploadBannerImage(1, mock(MultipartFile.class));
        assertNotNull(res);
    }

    @Test
    void deleteBanner() {
        when(bannerRepository.findById(1)).thenReturn(Optional.of(banner));
        bannerService.deleteBanner(1);
        verify(bannerRepository).delete(banner);
    }

    @Test
    void toggleActive() {
        when(bannerRepository.findById(1)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any())).thenReturn(banner);
        BannerResponse res = bannerService.toggleActive(1);
        assertNotNull(res);
    }

    @Test
    void togglePinned() {
        when(bannerRepository.findById(1)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any())).thenReturn(banner);
        BannerResponse res = bannerService.togglePinned(1);
        assertNotNull(res);
    }

    @Test
    void getAllBanner() {
        Page<Banner> page = new PageImpl<>(List.of(banner));
        when(bannerRepository.findAll(any(PageRequest.class))).thenReturn(page);
        Page<BannerResponse> res = bannerService.getAllBanner(PageRequest.of(0, 10));
        assertNotNull(res);
    }

    @Test
    void findFirstPinned() {
        when(bannerRepository.findFirstByIsPinTrueOrderByCreatedAtDesc()).thenReturn(Optional.of(banner));
        BannerResponse res = bannerService.findFirstPinned();
        assertNotNull(res);
    }
}
