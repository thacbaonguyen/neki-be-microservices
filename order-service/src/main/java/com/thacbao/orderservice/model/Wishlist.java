package com.thacbao.orderservice.model;

import com.thacbao.orderservice.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "wishlist_items", joinColumns = @JoinColumn(name = "wishlist_id"))
    @Column(name = "product_id")
    private List<Integer> productIds = new ArrayList<>();
}
