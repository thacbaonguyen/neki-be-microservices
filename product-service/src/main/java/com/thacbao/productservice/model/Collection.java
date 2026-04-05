package com.thacbao.productservice.model;

import com.thacbao.productservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "collection_subcategories",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "sub_category_id")
    )
    private Set<SubCategory> subCategories = new HashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "collections")
    private Set<Product> products = new HashSet<>();
}
