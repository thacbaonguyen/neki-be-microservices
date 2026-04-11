package com.thacbao.productservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BannerRequest {
    private String title;
    private String linkUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @JsonProperty("isActive") private boolean isActive;
    @JsonProperty("isPin") private boolean isPin;
}
