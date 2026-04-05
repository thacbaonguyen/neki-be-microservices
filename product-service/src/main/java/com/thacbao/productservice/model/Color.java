package com.thacbao.productservice.model;

import com.thacbao.productservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "colors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Color extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode;

    @Builder.Default
    @OneToMany(mappedBy = "color", cascade = CascadeType.ALL)
    private Set<ProductVariant> variants = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "color", cascade = CascadeType.ALL)
    private Set<ProductImage> images = new HashSet<>();
}
