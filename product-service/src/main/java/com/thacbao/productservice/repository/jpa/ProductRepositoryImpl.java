package com.thacbao.productservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thacbao.productservice.dto.request.ProductFilterRequest;
import com.thacbao.productservice.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QProduct product = QProduct.product;
    private final QProductVariant variant = QProductVariant.productVariant;
    private final QInventory inventory = QInventory.inventory;
    private final QSubCategory subCategory = QSubCategory.subCategory;
    private final QCategory category = QCategory.category;
    private final QBrand brand = QBrand.brand;
    private final QColor color = QColor.color;
    private final QSize size = QSize.size;
    private final QCollection collection = QCollection.collection;
    private final QTopic topic = QTopic.topic;

    @Override
    public Page<Product> filterProducts(ProductFilterRequest filter, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (filter.getIsActive() != null) {
            builder.and(product.isActive.eq(filter.getIsActive()));
        } else if (filter.getIncludeInactive() == null || !filter.getIncludeInactive()) {
            builder.and(product.isActive.isTrue());
        }

        // Keyword search (DB fallback for when ES is unavailable)
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            String kw = "%" + filter.getKeyword().toLowerCase() + "%";
            builder.and(
                    product.name.lower().like(kw)
                            .or(product.excerpt.lower().like(kw))
                            .or(product.description.lower().like(kw))
            );
        }

        if (filter.getCategoryId() != null) {
            builder.and(subCategory.category.id.eq(filter.getCategoryId()));
        }

        if (filter.getSubCategoryId() != null) {
            builder.and(product.subCategory.id.eq(filter.getSubCategoryId())
                    .or(product.subCategory.parent.id.eq(filter.getSubCategoryId())));
        }

        if (filter.getBrandId() != null) {
            builder.and(product.brand.id.eq(filter.getBrandId()));
        }

        if (filter.getCollectionId() != null) {
            builder.and(product.collections.any().id.eq(filter.getCollectionId()));
        }

        if (filter.getTopicId() != null) {
            builder.and(product.topics.any().id.eq(filter.getTopicId()));
        }

        if (filter.getGender() != null) {
            builder.and(product.gender.eq(filter.getGender()));
        }

        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            NumberExpression<BigDecimal> effectivePrice =
                    Expressions.cases()
                            .when(product.salePrice.isNotNull().and(product.salePrice.gt(BigDecimal.ZERO)))
                            .then(product.salePrice)
                            .otherwise(product.basePrice);

            if (filter.getMinPrice() != null) {
                builder.and(effectivePrice.goe(filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                builder.and(effectivePrice.loe(filter.getMaxPrice()));
            }
        }

        if (filter.getColorIds() != null && !filter.getColorIds().isEmpty()) {
            builder.and(product.variants.any().color.id.in(filter.getColorIds()));
        }

        if (filter.getSizeIds() != null && !filter.getSizeIds().isEmpty()) {
            builder.and(product.variants.any().size.id.in(filter.getSizeIds()));
        }

        if (filter.getIsFeatured() != null && filter.getIsFeatured()) {
            builder.and(product.isFeatured.isTrue());
        }

        if (filter.getIsNew() != null && filter.getIsNew()) {
            builder.and(product.isNew.isTrue());
        }

        if (filter.getIsOnSale() != null && filter.getIsOnSale()) {
            builder.and(product.salePrice.isNotNull())
                    .and(product.salePrice.gt(BigDecimal.ZERO))
                    .and(product.salePrice.lt(product.basePrice));
        }

        if (filter.getInStock() != null && filter.getInStock()) {
            builder.and(product.variants.any().inventory.quantity.gt(0));
        }

        JPAQuery<Product> query = queryFactory
                .selectDistinct(product)
                .from(product)
                .leftJoin(product.subCategory, subCategory).fetchJoin()
                .leftJoin(subCategory.category, category).fetchJoin()
                .leftJoin(product.brand, brand).fetchJoin()
                .where(builder);

        query.orderBy(getOrderSpecifiers(pageable.getSort()));

        long total = queryFactory
                .selectDistinct(product)
                .from(product)
                .leftJoin(product.subCategory, subCategory)
                .where(builder)
                .fetchCount();

        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Product> findByCollectionId(Integer collectionId, Pageable pageable) {
        JPAQuery<Product> query = queryFactory
                .selectFrom(product)
                .leftJoin(product.collections, collection)
                .where(collection.id.eq(collectionId).and(product.isActive.isTrue()))
                .orderBy(getOrderSpecifiers(pageable.getSort()));

        long total = query.fetchCount();
        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Product> findByTopicId(Integer topicId, Pageable pageable) {
        JPAQuery<Product> query = queryFactory
                .selectFrom(product)
                .leftJoin(product.topics, topic)
                .where(topic.id.eq(topicId).and(product.isActive.isTrue()))
                .orderBy(getOrderSpecifiers(pageable.getSort()));

        long total = query.fetchCount();
        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Product> findByBrandId(Integer brandId, Pageable pageable) {
        JPAQuery<Product> query = queryFactory
                .selectFrom(product)
                .where(product.brand.id.eq(brandId).and(product.isActive.isTrue()))
                .orderBy(getOrderSpecifiers(pageable.getSort()));

        long total = query.fetchCount();
        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Product> findRelatedProducts(Integer productId, Integer subCategoryId, int limit) {
        return queryFactory
                .selectFrom(product)
                .where(product.subCategory.id.eq(subCategoryId)
                        .and(product.id.ne(productId))
                        .and(product.isActive.isTrue()))
                .orderBy(product.totalSold.desc(), product.viewCount.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Product> findProductsWithStock(Pageable pageable) {
        JPAQuery<Product> query = queryFactory
                .selectDistinct(product)
                .from(product)
                .leftJoin(product.variants, variant)
                .leftJoin(variant.inventory, inventory)
                .where(product.isActive.isTrue()
                        .and(inventory.quantity.gt(0)))
                .orderBy(getOrderSpecifiers(pageable.getSort()));

        long total = query.fetchCount();
        List<Product> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public BigDecimal[] getPriceRangeByCategory(Integer categoryId) {
        BigDecimal minPrice = queryFactory
                .select(
                        Expressions.cases()
                                .when(product.salePrice.isNotNull().and(product.salePrice.gt(BigDecimal.ZERO)))
                                .then(product.salePrice)
                                .otherwise(product.basePrice)
                                .min()
                )
                .from(product)
                .leftJoin(product.subCategory, subCategory)
                .where(subCategory.category.id.eq(categoryId).and(product.isActive.isTrue()))
                .fetchOne();

        BigDecimal maxPrice = queryFactory
                .select(
                        Expressions.cases()
                                .when(product.salePrice.isNotNull().and(product.salePrice.gt(BigDecimal.ZERO)))
                                .then(product.salePrice)
                                .otherwise(product.basePrice)
                                .max()
                )
                .from(product)
                .leftJoin(product.subCategory, subCategory)
                .where(subCategory.category.id.eq(categoryId).and(product.isActive.isTrue()))
                .fetchOne();

        return new BigDecimal[]{
                minPrice != null ? minPrice : BigDecimal.ZERO,
                maxPrice != null ? maxPrice : BigDecimal.ZERO
        };
    }

    @Override
    public List<String> getAvailableColors(ProductFilterRequest filter) {
        BooleanBuilder builder = buildFilterConditions(filter);

        return queryFactory
                .selectDistinct(color.name)
                .from(product)
                .leftJoin(product.variants, variant)
                .leftJoin(variant.color, color)
                .where(builder)
                .fetch();
    }

    @Override
    public List<String> getAvailableSizes(ProductFilterRequest filter) {
        BooleanBuilder builder = buildFilterConditions(filter);

        return queryFactory
                .selectDistinct(size.name)
                .from(product)
                .leftJoin(product.variants, variant)
                .leftJoin(variant.size, size)
                .where(builder)
                .fetch();
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "price":
                    orders.add(new OrderSpecifier<>(direction,
                            Expressions.cases()
                                    .when(product.salePrice.isNotNull().and(product.salePrice.gt(BigDecimal.ZERO)))
                                    .then(product.salePrice)
                                    .otherwise(product.basePrice)
                    ));
                    break;
                case "name":
                    orders.add(new OrderSpecifier<>(direction, product.name));
                    break;
                case "totalSold":
                    orders.add(new OrderSpecifier<>(direction, product.totalSold));
                    break;
                case "viewCount":
                    orders.add(new OrderSpecifier<>(direction, product.viewCount));
                    break;
                case "averageRating":
                case "rating":
                    orders.add(new OrderSpecifier<>(direction, product.averageRating));
                    break;
                case "createdAt":
                    orders.add(new OrderSpecifier<>(direction, product.createdAt));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(direction, product.createdAt));
                    break;
            }
        }

        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, product.createdAt));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    private BooleanBuilder buildFilterConditions(ProductFilterRequest filter) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(product.isActive.isTrue());

        if (filter.getCategoryId() != null) {
            builder.and(subCategory.category.id.eq(filter.getCategoryId()));
        }
        if (filter.getSubCategoryId() != null) {
            builder.and(product.subCategory.id.eq(filter.getSubCategoryId()));
        }
        if (filter.getBrandId() != null) {
            builder.and(product.brand.id.eq(filter.getBrandId()));
        }

        return builder;
    }
}
