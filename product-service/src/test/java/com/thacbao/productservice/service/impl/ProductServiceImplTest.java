package com.thacbao.productservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import com.thacbao.productservice.model.*;
import com.thacbao.productservice.repository.elasticsearch.ProductElasticsearchRepository;
import com.thacbao.productservice.repository.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private ProductImage buildProductImage(Integer id, String imageUrl, Integer displayOrder, Boolean isPrimary) {
        ProductImage img = ProductImage.builder().imageUrl(imageUrl).displayOrder(displayOrder).isPrimary(isPrimary).build();
        img.setId(id);
        return img;
    }
    
    private ProductVariant buildVariant(Integer id, Boolean isActive) {
        ProductVariant v = ProductVariant.builder().isActive(isActive).build();
        v.setId(id);
        return v;
    }
    
    private Color buildColor(Integer id, String name) {
        Color c = Color.builder().name(name).build();
        c.setId(id);
        return c;
    }
    
    private Size buildSize(Integer id, String name) {
        Size s = Size.builder().name(name).build();
        s.setId(id);
        return s;
    }


    @Mock private ProductRepository productRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private SubCategoryRepository subCategoryRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private ColorRepository colorRepository;
    @Mock private SizeRepository sizeRepository;
    @Mock private CollectionRepository collectionRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductElasticsearchRepository elasticsearchRepository;
    @Mock private com.thacbao.productservice.service.CloudinaryService cloudinaryService;
    @Mock private org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations;
    @Mock private com.thacbao.productservice.client.RecommendationServiceClient recommendationServiceClient;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().name("Apparel").build();
        category.setId(1);
        SubCategory subCategory = SubCategory.builder().name("T-Shirts").category(category).build();
        subCategory.setId(1);
        Brand brand = Brand.builder().name("Nike").build();
        brand.setId(1);

        product = Product.builder()
                .name("Test Product")
                .slug("test-product")
                .description("Desc")
                .subCategory(subCategory)
                .brand(brand)
                .isActive(true)
                .gender(Product.Gender.UNISEX)
                .averageRating(BigDecimal.valueOf(4.5))
                .basePrice(BigDecimal.valueOf(100))
                .reviewCount(10)
                .viewCount(100L)
                .images(new HashSet<>())
                .variants(new HashSet<>())
                .build();
        product.setId(1);

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Desc");
        productRequest.setSubCategoryId(1);
        productRequest.setBrandId(1);
        productRequest.setBasePrice(BigDecimal.valueOf(100));
        productRequest.setGender(Product.Gender.UNISEX);
        productRequest.setCollectionIds(Set.of(1));
        productRequest.setTopicIds(Set.of(1));
    }

    @Test
    void createProduct_success() {
        when(subCategoryRepository.findById(1)).thenReturn(Optional.of(product.getSubCategory()));
        when(brandRepository.findById(1)).thenReturn(Optional.of(product.getBrand()));
        when(collectionRepository.findById(any())).thenReturn(Optional.empty());
        when(topicRepository.findById(any())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(1);
            return p;
        });
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductDetailResponse result = productService.createProduct(productRequest);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
        verify(elasticsearchRepository).save(any());
    }

    @Test
    void createProduct_subCategoryNotFound_throws() {
        when(subCategoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.createProduct(productRequest));
    }

    @Test
    void updateProduct_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(subCategoryRepository.findById(1)).thenReturn(Optional.of(product.getSubCategory()));
        when(brandRepository.findById(1)).thenReturn(Optional.of(product.getBrand()));
        when(collectionRepository.findById(any())).thenReturn(Optional.empty());
        when(topicRepository.findById(any())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        productRequest.setName("Updated Name");
        ProductDetailResponse result = productService.updateProduct(1, productRequest);

        assertEquals("Updated Name", result.getName());
        verify(elasticsearchRepository).save(any());
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        productService.deleteProduct(1);

        verify(productRepository).delete(product);
        verify(elasticsearchRepository).deleteById(1);
    }

    @Test
    void toggleProductActive_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        productService.toggleProductActive(1);

        assertFalse(product.getIsActive());
        verify(elasticsearchRepository).save(any());
    }

    @Test
    void getProductById_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductDetailResponse result = productService.getProductById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void getProductBySlug_success() {
        when(productRepository.findBySlug("test-product")).thenReturn(Optional.of(product));

        ProductDetailResponse result = productService.getProductBySlug("test-product");

        assertNotNull(result);
        assertEquals("test-product", result.getSlug());
    }

    @Test
    void getProducts_returnsPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.filterProducts(any(), any())).thenReturn(page);

        Page<ProductListResponse> result = productService.getProducts(new ProductFilterRequest(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getFeaturedProducts() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByIsFeaturedTrueAndIsActiveTrue(any())).thenReturn(page);

        Page<ProductListResponse> result = productService.getFeaturedProducts(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }


    // ========== Images ==========
    @Test
    void addProductImages_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cloudinaryService.uploadImages(any(), anyString())).thenReturn(List.of("url1", "url2"));
        when(productImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(productImageRepository.findByProductIdOrderByDisplayOrder(1)).thenReturn(List.of(
                buildProductImage(1, "url1", 0, false),
                buildProductImage(2, "url2", 1, false)
        ));

        List<ProductImageResponse> result = productService.addProductImages(1, List.of(mock(org.springframework.web.multipart.MultipartFile.class)), List.of(new ProductImageRequest()));

        assertEquals(2, result.size());
    }

    @Test
    void deleteProductImage_success() {
        ProductImage image = buildProductImage(1, "url", 0, false);
        when(productImageRepository.findById(1)).thenReturn(Optional.of(image));

        productService.deleteProductImage(1);

        verify(cloudinaryService).deleteImage("url");
        verify(productImageRepository).delete(image);
    }

    @Test
    void updateProductImageOrder_success() {
        ProductImage image = buildProductImage(1, "url", 1, false);
        when(productImageRepository.findById(1)).thenReturn(Optional.of(image));
        when(productImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.updateProductImageOrder(1, 2);

        assertEquals(2, image.getDisplayOrder());
    }

    @Test
    void setPrimaryImage_success() {
        ProductImage image1 = buildProductImage(1, "url", 0, false);
        ProductImage image2 = buildProductImage(2, "url", 1, false);
        when(productImageRepository.findByProductIdOrderByDisplayOrder(1)).thenReturn(List.of(image1, image2));
        when(productImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.setPrimaryImage(1, 1);

        assertTrue(image1.getIsPrimary());
        assertFalse(image2.getIsPrimary());
    }

    // ========== Variants ==========
    @Test
    void addVariant_success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(colorRepository.findById(any())).thenReturn(Optional.of(buildColor(1, "Red")));
        when(sizeRepository.findById(any())).thenReturn(Optional.of(buildSize(1, "M")));
        when(productVariantRepository.save(any())).thenAnswer(i -> {
            ProductVariant v = i.getArgument(0);
            v.setId(1);
            return v;
        });

        ProductVariantRequest vr = new ProductVariantRequest();
        vr.setColorId(1);
        vr.setSizeId(1);
        vr.setAdditionalPrice(BigDecimal.TEN);
        vr.setQuantity(10);

        ProductVariantResponse result = productService.addVariant(1, vr);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void updateVariant_success() {
        ProductVariant variant = buildVariant(1, true);
        variant.setProduct(product);
        variant.setInventory(new Inventory());
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(colorRepository.findById(any())).thenReturn(Optional.of(buildColor(2, "Blue")));
        when(sizeRepository.findById(any())).thenReturn(Optional.of(buildSize(2, "L")));
        when(productVariantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProductVariantRequest vr = new ProductVariantRequest();
        vr.setColorId(2);
        vr.setSizeId(2);
        vr.setQuantity(20);

        ProductVariantResponse result = productService.updateVariant(1, vr);

        assertNotNull(result);
        assertEquals(20, variant.getInventory().getQuantity());
    }

    @Test
    void deleteVariant_success() {
        productService.deleteVariant(1);
        verify(productVariantRepository).deleteById(1);
    }

    @Test
    void toggleVariantActive_success() {
        ProductVariant variant = buildVariant(1, true);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));

        productService.toggleVariantActive(1);

        assertFalse(variant.getIsActive());
    }

    // ========== Inventory ==========
    @Test
    void adjustInventory_success() {
        ProductVariant variant = buildVariant(1, true);
        Inventory inv = Inventory.builder().variant(variant).quantity(10).build();
        variant.setInventory(inv);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.adjustInventory(1, 5);

        assertEquals(15, inv.getQuantity());
    }

    @Test
    void updateInventory_success() {
        ProductVariant variant = buildVariant(1, true);
        Inventory inv = Inventory.builder().variant(variant).quantity(10).build();
        variant.setInventory(inv);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.updateInventory(1, 20);

        assertEquals(20, inv.getQuantity());
    }

    @Test
    void reserveInventory_success() {
        ProductVariant variant = buildVariant(1, true);
        Inventory inv = Inventory.builder().variant(variant).quantity(10).reservedQuantity(2).build();
        variant.setInventory(inv);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.reserveInventory(1, 5);

        assertEquals(7, inv.getReservedQuantity());
    }

    @Test
    void reserveInventory_insufficient_throws() {
        ProductVariant variant = buildVariant(1, true);
        Inventory inv = Inventory.builder().variant(variant).quantity(10).reservedQuantity(8).build();
        variant.setInventory(inv);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));

        assertThrows(IllegalArgumentException.class, () -> productService.reserveInventory(1, 5));
    }

    @Test
    void confirmInventory_success() {
        ProductVariant variant = buildVariant(1, true);
        Inventory inv = Inventory.builder().variant(variant).quantity(10).reservedQuantity(5).build();
        variant.setInventory(inv);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.confirmInventory(1, 5);

        assertEquals(5, inv.getQuantity());
        assertEquals(0, inv.getReservedQuantity());
    }

    @Test
    void restoreInventory_success() {
        ProductVariant variant = buildVariant(1, true);
        Inventory inv = Inventory.builder().variant(variant).quantity(10).reservedQuantity(5).build();
        variant.setInventory(inv);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        productService.restoreInventory(1, 5);

        assertEquals(10, inv.getQuantity());
        assertEquals(0, inv.getReservedQuantity());
    }

    // ========== Other reads ==========
    @Test
    void getNewProducts_returnsPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByIsNewTrueAndIsActiveTrue(any())).thenReturn(page);

        Page<ProductListResponse> result = productService.getNewProducts(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOnSaleProducts_returnsPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findOnSaleProducts(any())).thenReturn(page);

        Page<ProductListResponse> result = productService.getOnSaleProducts(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getBestSellers_returnsPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findBestSellers(any())).thenReturn(page);

        Page<ProductListResponse> result = productService.getBestSellers(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchProducts_fallbackToDb() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.filterProducts(any(), any())).thenReturn(page);

        Page<ProductListResponse> result = productService.searchProducts(new ProductFilterRequest(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }
}
