package com.thacbao.recommendationservice.service;

import com.thacbao.recommendationservice.dto.ProductSimpleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecommendationService {

    void calculateSimilarities();

    Page<ProductSimpleDTO> getSimilarProducts(Integer productId, Pageable pageable);

    List<ProductSimpleDTO> getRecommendedForYou(Integer userId, int limit);
}
