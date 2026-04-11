package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thacbao.productservice.model.Banner;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerResponse {
    private Integer id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @JsonProperty("isActive") private boolean isActive;
    @JsonProperty("isPin") private boolean isPin;
    private LocalDateTime createdAt;

    public static BannerResponse from(Banner banner) {
        return BannerResponse.builder().id(banner.getId()).title(banner.getTitle())
                .imageUrl(banner.getImageUrl()).linkUrl(banner.getLinkUrl())
                .startDate(banner.getStartDate()).endDate(banner.getEndDate())
                .isActive(banner.getIsActive()).isPin(banner.getIsPin()).createdAt(banner.getCreatedAt()).build();
    }
}
