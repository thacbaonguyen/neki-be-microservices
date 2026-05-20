package com.thacbao.recommendationservice.service.impl;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.recommendationservice.client.OrderServiceClient;
import com.thacbao.recommendationservice.client.ProductServiceClient;
import com.thacbao.recommendationservice.dto.InteractionBatchDTO;
import com.thacbao.recommendationservice.dto.ProductSimpleDTO;
import com.thacbao.recommendationservice.dto.UserInteractionDTO;
import com.thacbao.recommendationservice.model.ProductSimilarity;
import com.thacbao.recommendationservice.repository.ProductSimilarityRepository;
import com.thacbao.recommendationservice.service.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock private ProductSimilarityRepository productSimilarityRepository;
    @Mock private OrderServiceClient orderServiceClient;
    @Mock private ProductServiceClient productServiceClient;
    @Mock private RedisService redisService;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Test
    void getSimilarProducts_cached_returnsCached() {
        String cacheKey = "recommendation:similar:1";
        List<ProductSimpleDTO> cached = List.of(
                ProductSimpleDTO.builder().id(2).name("Product 2").build());
        when(redisService.get(cacheKey)).thenReturn(cached);

        Page<ProductSimpleDTO> result = recommendationService.getSimilarProducts(1, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(productSimilarityRepository, never()).findSimilarProducts(anyInt(), any());
    }

    @Test
    void getSimilarProducts_notCached_queriesDB() {
        when(redisService.get(anyString())).thenReturn(null);
        ProductSimilarity sim = ProductSimilarity.builder().productId1(1).productId2(2).score(0.8).build();
        when(productSimilarityRepository.findSimilarProducts(eq(1), any())).thenReturn(List.of(sim));

        ProductSimpleDTO product = ProductSimpleDTO.builder().id(2).name("Product 2").build();
        when(productServiceClient.getProductsByIds(List.of(2))).thenReturn(
                ApiResponse.<List<ProductSimpleDTO>>builder().data(List.of(product)).build());

        Page<ProductSimpleDTO> result = recommendationService.getSimilarProducts(1, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(redisService).set(anyString(), anyList(), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void getSimilarProducts_empty_noCache() {
        when(redisService.get(anyString())).thenReturn(null);
        when(productSimilarityRepository.findSimilarProducts(eq(1), any())).thenReturn(Collections.emptyList());

        Page<ProductSimpleDTO> result = recommendationService.getSimilarProducts(1, PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
        verify(redisService, never()).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void getRecommendedForYou_cached_returnsCached() {
        List<ProductSimpleDTO> cached = List.of(
                ProductSimpleDTO.builder().id(1).name("Product 1").build());
        when(redisService.get("recommendation:user:1")).thenReturn(cached);

        List<ProductSimpleDTO> result = recommendationService.getRecommendedForYou(1, 10);

        assertEquals(1, result.size());
    }

    @Test
    void getRecommendedForYou_anonymous_returnsPopular() {
        when(redisService.get(anyString())).thenReturn(null);
        ProductSimpleDTO popular = ProductSimpleDTO.builder().id(1).name("Popular").build();
        when(productServiceClient.getPopularProducts(10)).thenReturn(
                ApiResponse.<List<ProductSimpleDTO>>builder().data(List.of(popular)).build());

        List<ProductSimpleDTO> result = recommendationService.getRecommendedForYou(null, 10);

        assertEquals(1, result.size());
    }

    @Test
    void getRecommendedForYou_noInteractions_returnsPopular() {
        when(redisService.get(anyString())).thenReturn(null);
        UserInteractionDTO dto = new UserInteractionDTO();
        dto.setProductIds(Collections.emptySet());
        when(orderServiceClient.getUserInteractionProducts(1)).thenReturn(
                ApiResponse.<UserInteractionDTO>builder().data(dto).build());
        ProductSimpleDTO popular = ProductSimpleDTO.builder().id(1).name("Popular").build();
        when(productServiceClient.getPopularProducts(10)).thenReturn(
                ApiResponse.<List<ProductSimpleDTO>>builder().data(List.of(popular)).build());

        List<ProductSimpleDTO> result = recommendationService.getRecommendedForYou(1, 10);

        assertEquals(1, result.size());
    }

    @Test
    void getRecommendedForYou_withInteractions_returnsPersonalized() {
        when(redisService.get(anyString())).thenReturn(null);
        UserInteractionDTO dto = new UserInteractionDTO();
        dto.setProductIds(Set.of(1, 2));
        when(orderServiceClient.getUserInteractionProducts(1)).thenReturn(
                ApiResponse.<UserInteractionDTO>builder().data(dto).build());

        ProductSimilarity sim = ProductSimilarity.builder().productId1(1).productId2(3).score(0.9).build();
        when(productSimilarityRepository.findSimilarProducts(eq(1), any())).thenReturn(List.of(sim));
        when(productSimilarityRepository.findSimilarProducts(eq(2), any())).thenReturn(Collections.emptyList());

        ProductSimpleDTO recommended = ProductSimpleDTO.builder().id(3).name("Recommended").isActive(true).build();
        when(productServiceClient.getProductsByIds(anyList())).thenReturn(
                ApiResponse.<List<ProductSimpleDTO>>builder().data(List.of(recommended)).build());

        List<ProductSimpleDTO> result = recommendationService.getRecommendedForYou(1, 10);

        assertEquals(1, result.size());
        verify(redisService).set(anyString(), anyList(), eq(6L), eq(TimeUnit.HOURS));
    }

    @Test
    void getRecommendedForYou_noSimilarProducts_returnsPopular() {
        when(redisService.get(anyString())).thenReturn(null);
        UserInteractionDTO dto = new UserInteractionDTO();
        dto.setProductIds(Set.of(1));
        when(orderServiceClient.getUserInteractionProducts(1)).thenReturn(
                ApiResponse.<UserInteractionDTO>builder().data(dto).build());
        when(productSimilarityRepository.findSimilarProducts(eq(1), any())).thenReturn(Collections.emptyList());
        ProductSimpleDTO popular = ProductSimpleDTO.builder().id(5).name("Popular").build();
        when(productServiceClient.getPopularProducts(10)).thenReturn(
                ApiResponse.<List<ProductSimpleDTO>>builder().data(List.of(popular)).build());

        List<ProductSimpleDTO> result = recommendationService.getRecommendedForYou(1, 10);

        assertEquals(1, result.size());
    }

    @Test
    void calculateSimilarities_noData_returns() {
        InteractionBatchDTO batch = new InteractionBatchDTO();
        batch.setUserProductScores(null);
        batch.setHasNext(false);
        when(orderServiceClient.getReviewInteractions(0, 500)).thenReturn(
                ApiResponse.<InteractionBatchDTO>builder().data(batch).build());
        when(orderServiceClient.getOrderInteractions(0, 500)).thenReturn(
                ApiResponse.<InteractionBatchDTO>builder().data(batch).build());
        when(orderServiceClient.getWishlistInteractions(0, 500)).thenReturn(
                ApiResponse.<InteractionBatchDTO>builder().data(batch).build());

        recommendationService.calculateSimilarities();

        verify(productSimilarityRepository, never()).saveAll(anyList());
    }

    @Test
    void calculateSimilarities_withData_savesResults() {
        Map<Integer, Map<Integer, Double>> scores = new HashMap<>();
        scores.put(1, Map.of(10, 1.0, 20, 1.0));
        scores.put(2, Map.of(10, 1.0, 20, 1.0));

        InteractionBatchDTO batch = new InteractionBatchDTO();
        batch.setUserProductScores(scores);
        batch.setHasNext(false);

        InteractionBatchDTO emptyBatch = new InteractionBatchDTO();
        emptyBatch.setUserProductScores(null);
        emptyBatch.setHasNext(false);

        when(orderServiceClient.getReviewInteractions(0, 500)).thenReturn(
                ApiResponse.<InteractionBatchDTO>builder().data(batch).build());
        when(orderServiceClient.getOrderInteractions(0, 500)).thenReturn(
                ApiResponse.<InteractionBatchDTO>builder().data(emptyBatch).build());
        when(orderServiceClient.getWishlistInteractions(0, 500)).thenReturn(
                ApiResponse.<InteractionBatchDTO>builder().data(emptyBatch).build());

        recommendationService.calculateSimilarities();

        verify(productSimilarityRepository).deleteAllInBatchCustom();
        verify(productSimilarityRepository).saveAll(anyList());
    }

    @Test
    void calculateSimilarities_feignError_logsAndContinues() {
        when(orderServiceClient.getReviewInteractions(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Feign error"));
        when(orderServiceClient.getOrderInteractions(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Feign error"));
        when(orderServiceClient.getWishlistInteractions(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Feign error"));

        assertDoesNotThrow(() -> recommendationService.calculateSimilarities());
    }
}
