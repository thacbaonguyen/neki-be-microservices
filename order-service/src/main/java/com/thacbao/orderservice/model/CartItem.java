package com.thacbao.orderservice.model;

import com.thacbao.orderservice.model.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 1;
}
