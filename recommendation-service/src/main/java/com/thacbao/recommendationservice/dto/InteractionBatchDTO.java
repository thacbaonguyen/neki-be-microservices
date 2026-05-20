package com.thacbao.recommendationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO returned from Order Service containing batch interaction data
 * for similarity matrix construction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionBatchDTO {
    /**
     * Map of userId -> Map of productId -> score (weight)
     */
    private Map<Integer, Map<Integer, Double>> userProductScores;
    private int page;
    private int totalPages;
    private boolean hasNext;
}
