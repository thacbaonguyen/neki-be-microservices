package com.thacbao.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionBatchDTO {
    private Map<Integer, Map<Integer, Double>> userProductScores;
    private int page;
    private int totalPages;
    private boolean hasNext;
}
