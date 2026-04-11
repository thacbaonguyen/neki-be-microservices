package com.thacbao.productservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import com.thacbao.productservice.model.*;
import com.thacbao.productservice.repository.jpa.*;
import com.thacbao.productservice.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final BrandRepository brandRepository;
    private final CollectionRepository collectionRepository;
    private final TopicRepository topicRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final SubCategoryRepository subCategoryRepository;

    // ========== Brand ==========

    @Override @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        if (brandRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("Thương hiệu đã tồn tại: " + request.getName());
        Brand brand = Brand.builder().name(request.getName()).description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true).build();
        return BrandResponse.from(brandRepository.save(brand));
    }

    @Override @Transactional
    public BrandResponse updateBrand(Integer id, BrandRequest request) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new NotFoundException("Brand not found with id: " + id));
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        if (request.getIsActive() != null) brand.setIsActive(request.getIsActive());
        return BrandResponse.from(brandRepository.save(brand));
    }

    @Override @Transactional
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new NotFoundException("Brand not found with id: " + id));
        brandRepository.delete(brand);
    }

    @Override
    public BrandResponse getBrandById(Integer id) {
        return BrandResponse.from(brandRepository.findById(id).orElseThrow(() -> new NotFoundException("Brand not found with id: " + id)));
    }

    @Override
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream().map(BrandResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<BrandResponse> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrue().stream().map(BrandResponse::from).collect(Collectors.toList());
    }

    // ========== Collection ==========

    @Override @Transactional
    public CollectionResponse createCollection(CollectionRequest request) {
        if (collectionRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("Bộ sưu tập đã tồn tại: " + request.getName());
        Collection collection = Collection.builder().name(request.getName())
                .slug(toSlug(request.getName())).description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true).build();
        if (request.getSubCategoryIds() != null && !request.getSubCategoryIds().isEmpty()) {
            collection.setSubCategories(request.getSubCategoryIds().stream()
                    .map(scId -> subCategoryRepository.findById(scId).orElseThrow(() -> new NotFoundException("SubCategory not found with id: " + scId)))
                    .collect(Collectors.toSet()));
        }
        return CollectionResponse.from(collectionRepository.save(collection));
    }

    @Override @Transactional
    public CollectionResponse updateCollection(Integer id, CollectionRequest request) {
        Collection collection = collectionRepository.findById(id).orElseThrow(() -> new NotFoundException("Collection not found with id: " + id));
        collection.setName(request.getName());
        collection.setSlug(toSlug(request.getName()));
        collection.setDescription(request.getDescription());
        if (request.getIsActive() != null) collection.setIsActive(request.getIsActive());
        if (request.getSubCategoryIds() != null) {
            collection.getSubCategories().clear();
            collection.setSubCategories(request.getSubCategoryIds().stream()
                    .map(scId -> subCategoryRepository.findById(scId).orElseThrow(() -> new NotFoundException("SubCategory not found with id: " + scId)))
                    .collect(Collectors.toSet()));
        }
        return CollectionResponse.from(collectionRepository.save(collection));
    }

    @Override @Transactional
    public void deleteCollection(Integer id) {
        collectionRepository.deleteById(id);
    }

    @Override
    public CollectionResponse getCollectionById(Integer id) {
        return CollectionResponse.from(collectionRepository.findByIdWithSubCategories(id).orElseThrow(() -> new NotFoundException("Collection not found with id: " + id)));
    }

    @Override
    public CollectionResponse getCollectionBySlug(String slug) {
        return CollectionResponse.from(collectionRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("Collection not found with slug: " + slug)));
    }

    @Override
    public List<CollectionResponse> getAllCollections() {
        return collectionRepository.findAll().stream().map(CollectionResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<CollectionResponse> getAllActiveCollections() {
        return collectionRepository.findByIsActiveTrue().stream().map(CollectionResponse::from).collect(Collectors.toList());
    }

    // ========== Topic ==========

    @Override @Transactional
    public TopicResponse createTopic(TopicRequest request) {
        if (topicRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("Chủ đề đã tồn tại: " + request.getName());
        Topic topic = Topic.builder().name(request.getName()).slug(toSlug(request.getName()))
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true).build();
        return TopicResponse.from(topicRepository.save(topic));
    }

    @Override @Transactional
    public TopicResponse updateTopic(Integer id, TopicRequest request) {
        Topic topic = topicRepository.findById(id).orElseThrow(() -> new NotFoundException("Topic not found with id: " + id));
        topic.setName(request.getName());
        topic.setSlug(toSlug(request.getName()));
        topic.setDescription(request.getDescription());
        if (request.getIsActive() != null) topic.setIsActive(request.getIsActive());
        return TopicResponse.from(topicRepository.save(topic));
    }

    @Override @Transactional
    public void deleteTopic(Integer id) { topicRepository.deleteById(id); }

    @Override
    public TopicResponse getTopicById(Integer id) {
        return TopicResponse.from(topicRepository.findById(id).orElseThrow(() -> new NotFoundException("Topic not found with id: " + id)));
    }

    @Override
    public TopicResponse getTopicBySlug(String slug) {
        return TopicResponse.from(topicRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("Topic not found with slug: " + slug)));
    }

    @Override
    public List<TopicResponse> getAllTopics() {
        return topicRepository.findAll().stream().map(TopicResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<TopicResponse> getAllActiveTopics() {
        return topicRepository.findByIsActiveTrueOrderByName().stream().map(TopicResponse::from).collect(Collectors.toList());
    }

    // ========== Color ==========

    @Override @Transactional
    public ColorResponse createColor(ColorRequest request) {
        if (colorRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("Màu đã tồn tại: " + request.getName());
        if (colorRepository.existsByHexCode(request.getHexCode()))
            throw new IllegalArgumentException("Mã hex đã tồn tại: " + request.getHexCode());
        Color color = Color.builder().name(request.getName()).hexCode(request.getHexCode()).build();
        return ColorResponse.from(colorRepository.save(color));
    }

    @Override @Transactional
    public ColorResponse updateColor(Integer id, ColorRequest request) {
        Color color = colorRepository.findById(id).orElseThrow(() -> new NotFoundException("Color not found with id: " + id));
        color.setName(request.getName());
        color.setHexCode(request.getHexCode());
        return ColorResponse.from(colorRepository.save(color));
    }

    @Override @Transactional
    public void deleteColor(Integer id) { colorRepository.deleteById(id); }

    @Override
    public ColorResponse getColorById(Integer id) {
        return ColorResponse.from(colorRepository.findById(id).orElseThrow(() -> new NotFoundException("Color not found with id: " + id)));
    }

    @Override
    public List<ColorResponse> getAllColors() {
        return colorRepository.findAllByOrderByName().stream().map(ColorResponse::from).collect(Collectors.toList());
    }

    // ========== Size ==========

    @Override @Transactional
    public SizeResponse createSize(SizeRequest request) {
        if (sizeRepository.existsByNameAndCategoryType(request.getName(), request.getCategoryType()))
            throw new IllegalArgumentException("Size đã tồn tại: " + request.getName() + " - " + request.getCategoryType());
        Size size = Size.builder().name(request.getName()).categoryType(request.getCategoryType())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0).build();
        return SizeResponse.from(sizeRepository.save(size));
    }

    @Override @Transactional
    public SizeResponse updateSize(Integer id, SizeRequest request) {
        Size size = sizeRepository.findById(id).orElseThrow(() -> new NotFoundException("Size not found with id: " + id));
        size.setName(request.getName());
        size.setCategoryType(request.getCategoryType());
        if (request.getDisplayOrder() != null) size.setDisplayOrder(request.getDisplayOrder());
        return SizeResponse.from(sizeRepository.save(size));
    }

    @Override @Transactional
    public void deleteSize(Integer id) { sizeRepository.deleteById(id); }

    @Override
    public SizeResponse getSizeById(Integer id) {
        return SizeResponse.from(sizeRepository.findById(id).orElseThrow(() -> new NotFoundException("Size not found with id: " + id)));
    }

    @Override
    public List<SizeResponse> getSizesByCategoryType(String categoryType) {
        return sizeRepository.findByCategoryTypeOrderByDisplayOrder(categoryType).stream().map(SizeResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<SizeResponse> getAllSizes() {
        return sizeRepository.findAllByOrderByCategoryTypeAscDisplayOrderAsc().stream().map(SizeResponse::from).collect(Collectors.toList());
    }

    // Helper
    private String toSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-").replaceAll("-+", "-");
    }
}
