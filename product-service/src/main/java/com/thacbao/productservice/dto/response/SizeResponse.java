package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SizeResponse {
    private Integer id;
    private String name;
    private String categoryType;
    private Integer displayOrder;

    public static SizeResponse from(Size size) {
        return SizeResponse.builder().id(size.getId()).name(size.getName())
                .categoryType(size.getCategoryType()).displayOrder(size.getDisplayOrder()).build();
    }
}
