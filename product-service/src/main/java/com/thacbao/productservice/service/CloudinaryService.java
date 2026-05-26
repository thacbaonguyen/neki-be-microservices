package com.thacbao.productservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folder);
    List<String> uploadImages(List<MultipartFile> files, String folder);
    void deleteImage(String imageUrl);
    void deleteImages(List<String> imageUrls);
    String extractPublicId(String imageUrl);
}
