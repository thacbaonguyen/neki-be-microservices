package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Topic;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopicResponse {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static TopicResponse from(Topic topic) {
        return TopicResponse.builder().id(topic.getId()).name(topic.getName()).slug(topic.getSlug())
                .description(topic.getDescription()).isActive(topic.getIsActive()).createdAt(topic.getCreatedAt()).build();
    }
}
