package com.thacbao.recommendationservice.service.impl;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.recommendationservice.client.OrderServiceClient;
import com.thacbao.recommendationservice.client.ProductServiceClient;
import com.thacbao.recommendationservice.dto.InteractionBatchDTO;
import com.thacbao.recommendationservice.dto.ProductSimpleDTO;
import com.thacbao.recommendationservice.dto.UserInteractionDTO;
import com.thacbao.recommendationservice.model.ProductSimilarity;
import com.thacbao.recommendationservice.repository.ProductSimilarityRepository;
import com.thacbao.recommendationservice.service.RecommendationService;
import com.thacbao.recommendationservice.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductSimilarityRepository productSimilarityRepository;
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;
    private final RedisService redisService;

    private static final String REDIS_PREFIX = "recommendation:similar:";
    private static final double WEIGHT_REVIEW = 1.0;
    private static final double WEIGHT_ORDER = 5.0;
    private static final double WEIGHT_WISHLIST = 3.0;

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void calculateSimilarities() {
        log.info("Start scheduled similarity calculation");
        long startTime = System.currentTimeMillis();

        try {
            productSimilarityRepository.deleteAllInBatchCustom();

            // Build user-item matrix from Order Service data via Feign
            Map<Integer, Map<Integer, Double>> userMatrix = new HashMap<>();

            processInteractionsInBatches(userMatrix, "reviews", WEIGHT_REVIEW);
            processInteractionsInBatches(userMatrix, "orders", WEIGHT_ORDER);
            processInteractionsInBatches(userMatrix, "wishlists", WEIGHT_WISHLIST);

            if (userMatrix.isEmpty()) {
                log.warn("No interaction data for similarity calculation");
                return;
            }

            // Convert user-item → item-user matrix
            Map<Integer, Map<Integer, Double>> itemMatrix = new HashMap<>();
            userMatrix.forEach((userId, products) ->
                    products.forEach((productId, score) ->
                            itemMatrix.computeIfAbsent(productId, k -> new HashMap<>())
                                    .put(userId, score)
                    )
            );

            // Calculate cosine similarity between product pairs
            List<Integer> productIds = new ArrayList<>(itemMatrix.keySet());
            List<ProductSimilarity> similarities = new ArrayList<>();

            for (int i = 0; i < productIds.size(); i++) {
                for (int j = i + 1; j < productIds.size(); j++) {
                    Integer p1 = productIds.get(i);
                    Integer p2 = productIds.get(j);

                    Map<Integer, Double> v1 = itemMatrix.get(p1);
                    Map<Integer, Double> v2 = itemMatrix.get(p2);

                    int commonUsers = getCommonUsers(v1, v2);
                    if (commonUsers < 2) continue;

                    double sim = calculateCosineSimilarity(v1, v2);
                    if (sim > 0.15) {
                        similarities.add(ProductSimilarity.builder()
                                .productId1(p1).productId2(p2).score(sim).build());
                        similarities.add(ProductSimilarity.builder()
                                .productId1(p2).productId2(p1).score(sim).build());
                    }
                }
            }

            if (!similarities.isEmpty()) {
                productSimilarityRepository.saveAll(similarities);
                log.info("Saved {} similarity records", similarities.size());
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Similarity calculation completed in {} ms, products={}, pairs={}",
                    elapsed, productIds.size(), similarities.size());
        } catch (Exception e) {
            log.error("Error during similarity calculation: {}", e.getMessage(), e);
        }
    }

    private void processInteractionsInBatches(Map<Integer, Map<Integer, Double>> userMatrix,
                                               String type, double weight) {
        int page = 0;
        boolean hasNext = true;

        while (hasNext) {
            try {
                ApiResponse<InteractionBatchDTO> response;
                switch (type) {
                    case "reviews":
                        response = orderServiceClient.getReviewInteractions(page, 500);
                        break;
                    case "orders":
                        response = orderServiceClient.getOrderInteractions(page, 500);
                        break;
                    case "wishlists":
                        response = orderServiceClient.getWishlistInteractions(page, 500);
                        break;
                    default:
                        return;
                }

                if (response.getData() != null && response.getData().getUserProductScores() != null) {
                    response.getData().getUserProductScores().forEach((userId, products) ->
                            products.forEach((productId, score) ->
                                    userMatrix.computeIfAbsent(userId, k -> new HashMap<>())
                                            .merge(productId, score * weight, Double::max)
                            )
                    );
                    hasNext = response.getData().isHasNext();
                } else {
                    hasNext = false;
                }
            } catch (Exception e) {
                log.error("Error processing {} batch page {}: {}", type, page, e.getMessage());
                hasNext = false;
            }
            page++;
        }
        log.debug("Processed {} interactions in {} batches", type, page);
    }

    private double calculateCosineSimilarity(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        Set<Integer> commonUsers = new HashSet<>(v1.keySet());
        commonUsers.retainAll(v2.keySet());

        if (commonUsers.isEmpty()) return 0.0;

        double dotProduct = 0.0;
        for (Integer userId : commonUsers) {
            dotProduct += v1.get(userId) * v2.get(userId);
        }

        double norm1 = v1.values().stream().mapToDouble(s -> Math.pow(s, 2)).sum();
        double norm2 = v2.values().stream().mapToDouble(s -> Math.pow(s, 2)).sum();

        if (norm1 == 0 || norm2 == 0) return 0.0;

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private int getCommonUsers(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        Set<Integer> common = new HashSet<>(v1.keySet());
        common.retainAll(v2.keySet());
        return common.size();
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Page<ProductSimpleDTO> getSimilarProducts(Integer productId, Pageable pageable) {
        String cacheKey = REDIS_PREFIX + productId;

        List<ProductSimpleDTO> cached = (List<ProductSimpleDTO>) redisService.get(cacheKey);
        if (cached != null) {
            log.debug("Return cached recommendations for product {}", productId);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), cached.size());
            List<ProductSimpleDTO> page = start < cached.size()
                    ? cached.subList(start, end)
                    : Collections.emptyList();
            return new PageImpl<>(page, pageable, cached.size());
        }

        // Not cached: query DB then enrich via Product Service
        List<ProductSimilarity> similarities = productSimilarityRepository
                .findSimilarProducts(productId, PageRequest.of(0, 50));

        List<Integer> productIds = similarities.stream()
                .map(ProductSimilarity::getProductId2)
                .collect(Collectors.toList());

        List<ProductSimpleDTO> products = Collections.emptyList();
        if (!productIds.isEmpty()) {
            ApiResponse<List<ProductSimpleDTO>> response = productServiceClient.getProductsByIds(productIds);
            if (response.getData() != null) {
                products = response.getData();
            }
        }

        if (!products.isEmpty()) {
            redisService.set(cacheKey, products, 24, TimeUnit.HOURS);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), products.size());
        List<ProductSimpleDTO> page = start < products.size()
                ? products.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(page, pageable, products.size());
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ProductSimpleDTO> getRecommendedForYou(Integer userId, int limit) {
        String cacheKey = "recommendation:user:" + userId;

        List<ProductSimpleDTO> cached = (List<ProductSimpleDTO>) redisService.get(cacheKey);
        if (cached != null) {
            log.debug("Return cached recommendations for user {}", userId);
            return cached.stream().limit(limit).collect(Collectors.toList());
        }

        if (userId == null) {
            log.debug("Anonymous user, returning popular products");
            List<ProductSimpleDTO> popular = getPopularProducts(limit);
            redisService.set(cacheKey, popular, 1, TimeUnit.HOURS);
            return popular;
        }

        // Get user interaction history via Feign
        ApiResponse<UserInteractionDTO> response = orderServiceClient.getUserInteractionProducts(userId);
        Set<Integer> userInteractedProducts = response.getData() != null
                ? response.getData().getProductIds()
                : Collections.emptySet();

        if (userInteractedProducts.isEmpty()) {
            log.debug("User {} has no interactions, returning popular products", userId);
            List<ProductSimpleDTO> popular = getPopularProducts(limit);
            redisService.set(cacheKey, popular, 1, TimeUnit.HOURS);
            return popular;
        }

        // Aggregate scores from similar products
        Map<Integer, Double> productScores = new HashMap<>();

        for (Integer interactedProductId : userInteractedProducts) {
            List<ProductSimilarity> similarities = productSimilarityRepository
                    .findSimilarProducts(interactedProductId, PageRequest.of(0, 20));

            similarities.forEach(ps -> {
                Integer recommendedId = ps.getProductId2();
                if (!userInteractedProducts.contains(recommendedId)) {
                    productScores.merge(recommendedId, ps.getScore(), Double::sum);
                }
            });
        }

        if (productScores.isEmpty()) {
            log.debug("No similar products found for user {}, returning popular", userId);
            List<ProductSimpleDTO> popular = getPopularProducts(limit);
            redisService.set(cacheKey, popular, 1, TimeUnit.HOURS);
            return popular;
        }

        // Sort by score, fetch product info, filter active
        List<Integer> topProductIds = productScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit * 2L)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<ProductSimpleDTO> recommendations = Collections.emptyList();
        if (!topProductIds.isEmpty()) {
            ApiResponse<List<ProductSimpleDTO>> productResponse = productServiceClient.getProductsByIds(topProductIds);
            if (productResponse.getData() != null) {
                recommendations = productResponse.getData().stream()
                        .filter(p -> p.getIsActive() == null || p.getIsActive())
                        .limit(limit)
                        .collect(Collectors.toList());
            }
        }

        if (!recommendations.isEmpty()) {
            redisService.set(cacheKey, recommendations, 6, TimeUnit.HOURS);
        }

        log.debug("Generated {} personalized recommendations for user {}", recommendations.size(), userId);
        return recommendations;
    }

    private List<ProductSimpleDTO> getPopularProducts(int limit) {
        ApiResponse<List<ProductSimpleDTO>> response = productServiceClient.getPopularProducts(limit);
        return response.getData() != null ? response.getData() : Collections.emptyList();
    }
}
