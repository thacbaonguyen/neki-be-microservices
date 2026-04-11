package com.thacbao.productservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.productservice.document.ProductDocument;
import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import com.thacbao.productservice.model.*;
import com.thacbao.productservice.repository.elasticsearch.ProductElasticsearchRepository;
import com.thacbao.productservice.repository.jpa.*;
import com.thacbao.productservice.service.CloudinaryService;
import com.thacbao.productservice.service.ProductService;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.UntypedRangeQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final BrandRepository brandRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final CollectionRepository collectionRepository;
    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;
    private final ProductElasticsearchRepository elasticsearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final CloudinaryService cloudinaryService;
    private final com.thacbao.productservice.client.RecommendationServiceClient recommendationServiceClient;

    // ========== CRUD ==========

    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductRequest request) {
        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new NotFoundException("SubCategory not found: " + request.getSubCategoryId()));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new NotFoundException("Brand not found: " + request.getBrandId()));

        Product product = Product.builder()
                .subCategory(subCategory).brand(brand)
                .name(request.getName()).excerpt(request.getExcerpt() != null ? request.getExcerpt() : "")
                .slug(toSlug(request.getName())).description(request.getDescription() != null ? request.getDescription() : "")
                .basePrice(request.getBasePrice()).salePrice(request.getSalePrice())
                .gender(request.getGender())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isNew(request.getIsNew() != null ? request.getIsNew() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .metaTitle(request.getMetaTitle()).metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        // Collections
        if (request.getCollectionIds() != null && !request.getCollectionIds().isEmpty()) {
            Set<Collection> collections = new HashSet<>();
            request.getCollectionIds().forEach(id -> collectionRepository.findById(id).ifPresent(collections::add));
            product.setCollections(collections);
        }
        // Topics
        if (request.getTopicIds() != null && !request.getTopicIds().isEmpty()) {
            Set<Topic> topics = new HashSet<>();
            request.getTopicIds().forEach(id -> topicRepository.findById(id).ifPresent(topics::add));
            product.setTopics(topics);
        }

        Product saved = productRepository.save(product);

        // Create variants
        if (request.getVariants() != null) {
            request.getVariants().forEach(vr -> createVariantInternal(saved, vr));
        }

        syncToElasticsearch(saved);
        return ProductDetailResponse.from(productRepository.findById(saved.getId()).get());
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Integer id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new NotFoundException("SubCategory not found: " + request.getSubCategoryId()));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new NotFoundException("Brand not found: " + request.getBrandId()));

        product.setSubCategory(subCategory);
        product.setBrand(brand);
        product.setName(request.getName());
        product.setExcerpt(request.getExcerpt() != null ? request.getExcerpt() : "");
        product.setSlug(toSlug(request.getName()));
        product.setDescription(request.getDescription() != null ? request.getDescription() : "");
        product.setBasePrice(request.getBasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setGender(request.getGender());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        if (request.getIsNew() != null) product.setIsNew(request.getIsNew());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setMetaKeywords(request.getMetaKeywords());

        // Update collections
        if (request.getCollectionIds() != null) {
            product.getCollections().clear();
            request.getCollectionIds().forEach(cId -> collectionRepository.findById(cId).ifPresent(product.getCollections()::add));
        }
        // Update topics
        if (request.getTopicIds() != null) {
            product.getTopics().clear();
            request.getTopicIds().forEach(tId -> topicRepository.findById(tId).ifPresent(product.getTopics()::add));
        }

        Product saved = productRepository.save(product);
        syncToElasticsearch(saved);
        return ProductDetailResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        // Delete images from Cloudinary
        product.getImages().forEach(img -> cloudinaryService.deleteImage(img.getImageUrl()));
        productRepository.delete(product);
        elasticsearchRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleProductActive(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
        syncToElasticsearch(product);
    }

    // ========== Images ==========

    @Override
    @Transactional
    public List<ProductImageResponse> addProductImages(Integer productId, List<MultipartFile> files, List<ProductImageRequest> imageRequests) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        List<String> urls = cloudinaryService.uploadImages(files, "neki/products/" + productId);

        for (int i = 0; i < urls.size(); i++) {
            ProductImageRequest req = i < imageRequests.size() ? imageRequests.get(i) : null;
            Color color = null;
            if (req != null && req.getColorId() != null) {
                color = colorRepository.findById(req.getColorId()).orElse(null);
            }
            ProductImage image = ProductImage.builder()
                    .product(product).imageUrl(urls.get(i)).color(color)
                    .displayOrder(req != null && req.getDisplayOrder() != null ? req.getDisplayOrder() : i)
                    .isPrimary(req != null && req.getIsPrimary() != null ? req.getIsPrimary() : false)
                    .build();
            productImageRepository.save(image);
        }

        return productImageRepository.findByProductIdOrderByDisplayOrder(productId).stream()
                .map(ProductImageResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProductImage(Integer imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found: " + imageId));
        cloudinaryService.deleteImage(image.getImageUrl());
        productImageRepository.delete(image);
    }

    @Override
    @Transactional
    public void updateProductImageOrder(Integer imageId, Integer displayOrder) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found: " + imageId));
        image.setDisplayOrder(displayOrder);
        productImageRepository.save(image);
    }

    @Override
    @Transactional
    public void setPrimaryImage(Integer productId, Integer imageId) {
        productImageRepository.findByProductIdOrderByDisplayOrder(productId)
                .forEach(img -> { img.setIsPrimary(img.getId().equals(imageId)); productImageRepository.save(img); });
    }

    // ========== Variants ==========

    @Override
    @Transactional
    public ProductVariantResponse addVariant(Integer productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        ProductVariant variant = createVariantInternal(product, request);
        return ProductVariantResponse.from(variant, product.getCurrentPrice());
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(Integer variantId, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new NotFoundException("Color not found: " + request.getColorId()));
        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new NotFoundException("Size not found: " + request.getSizeId()));
        variant.setColor(color);
        variant.setSize(size);
        variant.setAdditionalPrice(request.getAdditionalPrice() != null ? request.getAdditionalPrice() : BigDecimal.ZERO);
        if (request.getIsActive() != null) variant.setIsActive(request.getIsActive());
        if (request.getQuantity() != null && variant.getInventory() != null) {
            variant.getInventory().setQuantity(request.getQuantity());
        }
        productVariantRepository.save(variant);
        return ProductVariantResponse.from(variant, variant.getProduct().getCurrentPrice());
    }

    @Override
    @Transactional
    public void deleteVariant(Integer variantId) {
        productVariantRepository.deleteById(variantId);
    }

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        return product.getVariants().stream()
                .map(v -> ProductVariantResponse.from(v, product.getCurrentPrice()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleVariantActive(Integer variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));
        variant.setIsActive(!variant.getIsActive());
        productVariantRepository.save(variant);
    }

    @Override
    @Transactional
    public void adjustInventory(Integer variantId, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));
        Inventory inv = variant.getInventory();
        if (inv == null) {
            inv = Inventory.builder().variant(variant).quantity(Math.max(0, quantity)).reservedQuantity(0)
                    .lastRestockedAt(LocalDateTime.now()).build();
        } else {
            inv.setQuantity(Math.max(0, inv.getQuantity() + quantity));
            inv.setLastRestockedAt(LocalDateTime.now());
        }
        inventoryRepository.save(inv);
    }

    // ========== Inventory ==========

    @Override
    @Transactional
    public void updateInventory(Integer variantId, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));
        Inventory inv = variant.getInventory();
        if (inv == null) {
            inv = Inventory.builder().variant(variant).quantity(quantity).reservedQuantity(0)
                    .lastRestockedAt(LocalDateTime.now()).build();
        } else {
            inv.setQuantity(quantity);
            inv.setLastRestockedAt(LocalDateTime.now());
        }
        inventoryRepository.save(inv);
    }

    @Override
    @Transactional
    public void reserveInventory(Integer variantId, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));
        Inventory inv = variant.getInventory();
        if (inv == null || inv.getQuantity() - inv.getReservedQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for variant: " + variantId);
        }
        inv.setReservedQuantity(inv.getReservedQuantity() + quantity);
        inventoryRepository.save(inv);
    }

    @Override
    @Transactional
    public void confirmInventory(Integer variantId, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));
        Inventory inv = variant.getInventory();
        if (inv != null) {
            inv.setQuantity(inv.getQuantity() - quantity);
            inv.setReservedQuantity(inv.getReservedQuantity() - quantity);
            inventoryRepository.save(inv);
        }
    }

    @Override
    @Transactional
    public void restoreInventory(Integer variantId, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));
        Inventory inv = variant.getInventory();
        if (inv != null) {
            inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - quantity));
            inventoryRepository.save(inv);
        }
    }

    // ========== Public Read ==========

    @Override
    public ProductDetailResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return ProductDetailResponse.from(product);
    }

    @Override
    @Transactional
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Product not found: " + slug));
        productRepository.incrementViewCount(product.getId());
        return ProductDetailResponse.from(product);
    }

    @Override
    public Page<ProductListResponse> getProducts(ProductFilterRequest filter, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (filter.getSortBy() != null) {
            Sort.Direction dir = "asc".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, filter.getSortBy());
        }
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return productRepository.filterProducts(filter, sortedPageable).map(ProductListResponse::from);
    }

    @Override
    public Page<ProductListResponse> getFeaturedProducts(Pageable pageable) {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable).map(ProductListResponse::from);
    }

    @Override
    public Page<ProductListResponse> getNewProducts(Pageable pageable) {
        return productRepository.findByIsNewTrueAndIsActiveTrue(pageable).map(ProductListResponse::from);
    }

    @Override
    public Page<ProductListResponse> getOnSaleProducts(Pageable pageable) {
        return productRepository.findOnSaleProducts(pageable).map(ProductListResponse::from);
    }

    @Override
    public Page<ProductListResponse> getBestSellers(Pageable pageable) {
        return productRepository.findBestSellers(pageable).map(ProductListResponse::from);
    }

    @Override
    public List<ProductListResponse> getRelatedProducts(Integer productId, int limit) {
        // Try getting cosine-similarity recommendations from recommendation-service first
        try {
            var response = recommendationServiceClient.getSimilarProducts(productId, 0, limit);
            if (response != null && response.getData() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.getData().get("content");
                if (content != null && !content.isEmpty()) {
                    List<Integer> recommendedIds = content.stream()
                            .map(item -> ((Number) item.get("id")).intValue())
                            .collect(Collectors.toList());
                    List<Product> products = productRepository.findAllById(recommendedIds);
                    if (!products.isEmpty()) {
                        Map<Integer, Product> productMap = products.stream()
                                .collect(Collectors.toMap(Product::getId, p -> p));
                        return recommendedIds.stream()
                                .map(productMap::get)
                                .filter(Objects::nonNull)
                                .map(ProductListResponse::from)
                                .collect(Collectors.toList());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get recommendations for product {}, falling back to subcategory: {}",
                    productId, e.getMessage());
        }

        // Fallback to category-based related products
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        return productRepository.findRelatedProducts(productId, product.getSubCategory().getId(), limit)
                .stream().map(ProductListResponse::from).collect(Collectors.toList());
    }

    @Override
    public FilterOptionsResponse getFilterOptions(Integer categoryId) {
        FilterOptionsResponse.FilterOptionsResponseBuilder builder = FilterOptionsResponse.builder();
        builder.categories(categoryRepository.findAllActiveWithSubCategories().stream()
                .map(CategoryResponse::fromWithSubCategories).collect(Collectors.toList()));
        builder.brands(brandRepository.findByIsActiveTrue().stream().map(BrandResponse::from).collect(Collectors.toList()));
        builder.collections(collectionRepository.findByIsActiveTrue().stream().map(CollectionResponse::from).collect(Collectors.toList()));
        builder.topics(topicRepository.findByIsActiveTrueOrderByName().stream().map(TopicResponse::from).collect(Collectors.toList()));
        builder.colors(colorRepository.findAllByOrderByName().stream().map(ColorResponse::from).collect(Collectors.toList()));
        builder.sizes(sizeRepository.findAllByOrderByCategoryTypeAscDisplayOrderAsc().stream().map(SizeResponse::from).collect(Collectors.toList()));
        if (categoryId != null) {
            BigDecimal[] priceRange = productRepository.getPriceRangeByCategory(categoryId);
            builder.minPrice(priceRange[0]).maxPrice(priceRange[1]);
        }
        return builder.build();
    }

    @Override
    @Transactional
    public void incrementViewCount(Integer productId) {
        productRepository.incrementViewCount(productId);
    }

    // ========== Elasticsearch Search ==========

    @Override
    public Page<ProductListResponse> searchProducts(ProductFilterRequest filter, Pageable pageable) {
        try {
            NativeQuery query = buildElasticsearchQuery(filter, pageable);
            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

            List<Integer> ids = searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getId().intValue())
                    .toList();

            if (!ids.isEmpty()) {
                List<Product> products = productRepository.findAllById(ids);
                Map<Integer, Product> productMap = products.stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

                List<ProductListResponse> responses = ids.stream()
                        .map(productMap::get)
                        .filter(Objects::nonNull)
                        .map(ProductListResponse::from)
                        .collect(Collectors.toList());

                if (!responses.isEmpty()) {
                    long total = searchHits.getTotalHits();
                    return new PageImpl<>(responses, pageable, total);
                }
            }
            // ES returned no results or results didn't match DB — fallback
            log.info("ES returned no usable results for keyword='{}', falling back to DB", filter.getKeyword());
        } catch (Exception e) {
            log.error("Elasticsearch search failed, fallback to DB", e);
        }
        return getProducts(filter, pageable);
    }

    private NativeQuery buildElasticsearchQuery(ProductFilterRequest filter, Pageable pageable) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // keyword search
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            String keyword = filter.getKeyword();
            boolQuery.must(m -> m.bool(b -> b
                    .should(s -> s.matchPhrase(mp -> mp.field("name").query(keyword)))
                    .should(s -> s.matchPhrase(mp -> mp.field("description").query(keyword)))
                    .should(s -> s.matchPhrase(mp -> mp.field("excerpt").query(keyword)))
                    .minimumShouldMatch("1")
            ));
        }

        // isActive filter (always for public)
        if (filter.getIncludeInactive() == null || !filter.getIncludeInactive()) {
            boolQuery.filter(f -> f.term(t -> t.field("isActive").value(true)));
        }

        // category
        if (filter.getCategoryId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("categoryId").value(filter.getCategoryId())));
        }

        // subcategory
        if (filter.getSubCategoryId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("subCategoryId").value(filter.getSubCategoryId())));
        }

        // brand
        if (filter.getBrandId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("brandId").value(filter.getBrandId())));
        }

        // collection
        if (filter.getCollectionId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("collectionIds").value(filter.getCollectionId())));
        }

        // topic
        if (filter.getTopicId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("topicIds").value(filter.getTopicId())));
        }

        // gender
        if (filter.getGender() != null) {
            String genderValue = filter.getGender().name().toLowerCase();
            boolQuery.filter(f -> f.term(t -> t.field("gender").value(genderValue)));
        }

        // colorIds
        if (filter.getColorIds() != null && !filter.getColorIds().isEmpty()) {
            List<FieldValue> colorValues = filter.getColorIds().stream()
                    .map(FieldValue::of).toList();
            boolQuery.filter(f -> f.terms(t -> t.field("colorIds").terms(tv -> tv.value(colorValues))));
        }

        // sizeIds
        if (filter.getSizeIds() != null && !filter.getSizeIds().isEmpty()) {
            List<FieldValue> sizeValues = filter.getSizeIds().stream()
                    .map(FieldValue::of).toList();
            boolQuery.filter(f -> f.terms(t -> t.field("sizeIds").terms(tv -> tv.value(sizeValues))));
        }

        // inStock
        if (filter.getInStock() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("inStock").value(filter.getInStock())));
        }

        // featured
        if (Boolean.TRUE.equals(filter.getIsFeatured())) {
            boolQuery.filter(f -> f.term(t -> t.field("isFeatured").value(true)));
        }

        // new
        if (Boolean.TRUE.equals(filter.getIsNew())) {
            boolQuery.filter(f -> f.term(t -> t.field("isNew").value(true)));
        }

        // price range
        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            boolQuery.filter(f -> f.range(r -> {
                UntypedRangeQuery.Builder rangeBuilder = new UntypedRangeQuery.Builder().field("price");
                if (filter.getMinPrice() != null) {
                    rangeBuilder.gte(JsonData.of(filter.getMinPrice()));
                }
                if (filter.getMaxPrice() != null) {
                    rangeBuilder.lte(JsonData.of(filter.getMaxPrice()));
                }
                return r.untyped(rangeBuilder.build());
            }));
        }

        Query esQuery = Query.of(q -> q.bool(boolQuery.build()));
        Pageable esPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
                .withQuery(esQuery)
                .withPageable(esPageable);

        if (filter.getSortBy() != null && !filter.getSortBy().isBlank()) {
            SortOrder order = "asc".equalsIgnoreCase(filter.getSortDirection()) ? SortOrder.Asc : SortOrder.Desc;
            queryBuilder.withSort(s -> s.field(f -> f.field(filter.getSortBy()).order(order)));
        } else {
            queryBuilder.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
        }

        return queryBuilder.build();
    }

    // ========== Internal API ==========

    @Override
    @Transactional
    public void updateRating(Integer productId, BigDecimal rating, Integer count) {
        productRepository.updateRating(productId, rating, count);
    }

    @Override
    @Transactional
    public void updateTotalSold(Integer productId, Integer additionalSold) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        product.setTotalSold(product.getTotalSold() + additionalSold);
        productRepository.save(product);
        syncToElasticsearch(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSimpleDTO> getProductsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return productRepository.findAllById(ids).stream()
                .map(ProductSimpleDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSimpleDTO> getPopularProducts(int limit) {
        Page<Product> popularPage = productRepository.findByIsActiveTrueOrderByTotalSoldDescAverageRatingDesc(
                PageRequest.of(0, limit)
        );
        return popularPage.getContent().stream()
                .map(ProductSimpleDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAllActiveProductIds() {
        return productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .map(Product::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductEntitiesByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return productRepository.findAllById(ids);
    }

    // ========== Helpers ==========

    private ProductVariant createVariantInternal(Product product, ProductVariantRequest request) {
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new NotFoundException("Color not found: " + request.getColorId()));
        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new NotFoundException("Size not found: " + request.getSizeId()));

        ProductVariant variant = ProductVariant.builder()
                .product(product).color(color).size(size)
                .additionalPrice(request.getAdditionalPrice() != null ? request.getAdditionalPrice() : BigDecimal.ZERO)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        variant = productVariantRepository.save(variant);

        Inventory inventory = Inventory.builder()
                .variant(variant).quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .reservedQuantity(0).lastRestockedAt(LocalDateTime.now()).build();
        inventoryRepository.save(inventory);

        return variant;
    }

    @Override
    public List<com.thacbao.common.dto.ProductVariantDTO> getVariantsByIds(List<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return java.util.Collections.emptyList();
        
        return productVariantRepository.findAllById(variantIds).stream()
            .map(variant -> {
                Product product = variant.getProduct();
                com.thacbao.productservice.model.ProductImage primaryImg = product.getImages().stream()
                        .filter(img -> java.util.Objects.equals(img.getIsPrimary(), true))
                        .findFirst()
                        .orElse(product.getImages().isEmpty() ? null : product.getImages().iterator().next());
                        
                return com.thacbao.common.dto.ProductVariantDTO.builder()
                        .id(variant.getId())
                        .productId(product.getId())
                        .productName(product.getName())
                        .colorName(variant.getColor().getName())
                        .sizeName(variant.getSize().getName())
                        .price(product.getBasePrice())
                        .salePrice(product.isOnSale() ? product.getSalePrice() : null)
                        .stockQuantity(variant.getInventory() != null ? variant.getInventory().getQuantity() : 0)
                        .imageUrl(primaryImg != null ? primaryImg.getImageUrl() : null)
                        .isActive(variant.getIsActive() && product.getIsActive())
                        .build();
            })
            .collect(Collectors.toList());
    }

    private void syncToElasticsearch(Product product) {
        try {
            ProductDocument doc = ProductDocument.builder()
                    .id(product.getId().longValue())
                    .name(product.getName()).excerpt(product.getExcerpt()).description(product.getDescription())
                    .price(product.getCurrentPrice())
                    .brandId(product.getBrand().getId())
                    .categoryId(product.getSubCategory().getCategory().getId())
                    .subCategoryId(product.getSubCategory().getId())
                    .collectionIds(product.getCollections().stream().map(Collection::getId).collect(Collectors.toList()))
                    .topicIds(product.getTopics().stream().map(Topic::getId).collect(Collectors.toList()))
                    .gender(product.getGender().getValue())
                    .sizeIds(product.getVariants().stream().map(v -> v.getSize().getId()).distinct().collect(Collectors.toList()))
                    .colorIds(product.getVariants().stream().map(v -> v.getColor().getId()).distinct().collect(Collectors.toList()))
                    .isFeatured(product.getIsFeatured()).isNew(product.getIsNew())
                    .isOnSale(product.isOnSale()).isActive(product.getIsActive())
                    .inStock(product.getVariants().stream().anyMatch(v -> v.getInventory() != null && v.getInventory().getQuantity() > 0))
                    .rating(product.getAverageRating() != null ? product.getAverageRating().doubleValue() : 0.0)
                    .totalSold(product.getTotalSold()).viewCount(product.getViewCount())
                    .createdAt(Instant.now()).updatedAt(Instant.now())
                    .build();
            elasticsearchRepository.save(doc);
        } catch (Exception e) {
            log.error("Failed to sync product {} to Elasticsearch", product.getId(), e);
        }
    }

    private String toSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-").replaceAll("-+", "-");
    }
}
