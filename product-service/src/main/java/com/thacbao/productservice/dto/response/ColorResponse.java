package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Color;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColorResponse {
    private Integer id;
    private String name;
    private String hexCode;

    public static ColorResponse from(Color color) {
        return ColorResponse.builder().id(color.getId()).name(color.getName()).hexCode(color.getHexCode()).build();
    }
}
