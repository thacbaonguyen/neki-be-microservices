package com.thacbao.recommendationservice.model;

import com.thacbao.recommendationservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_similarity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSimilarity extends BaseEntity {

    @Column(name = "product_id_1", nullable = false)
    private Integer productId1;

    @Column(name = "product_id_2", nullable = false)
    private Integer productId2;

    @Column(name = "score", nullable = false)
    private Double score;
}
