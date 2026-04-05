package com.thacbao.productservice.model;

import com.thacbao.productservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Size extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String name;

    @Column(name = "category_type", nullable = false, length = 50)
    private String categoryType;

    @Builder.Default
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Builder.Default
    @OneToMany(mappedBy = "size", cascade = CascadeType.ALL)
    private Set<ProductVariant> variants = new HashSet<>();
}
