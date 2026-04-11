package com.thacbao.productservice.service;

import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import java.util.List;

public interface AttributeService {
    // Brand
    BrandResponse createBrand(BrandRequest request);
    BrandResponse updateBrand(Integer id, BrandRequest request);
    void deleteBrand(Integer id);
    BrandResponse getBrandById(Integer id);
    List<BrandResponse> getAllBrands();
    List<BrandResponse> getAllActiveBrands();

    // Collection
    CollectionResponse createCollection(CollectionRequest request);
    CollectionResponse updateCollection(Integer id, CollectionRequest request);
    void deleteCollection(Integer id);
    CollectionResponse getCollectionById(Integer id);
    CollectionResponse getCollectionBySlug(String slug);
    List<CollectionResponse> getAllCollections();
    List<CollectionResponse> getAllActiveCollections();

    // Topic
    TopicResponse createTopic(TopicRequest request);
    TopicResponse updateTopic(Integer id, TopicRequest request);
    void deleteTopic(Integer id);
    TopicResponse getTopicById(Integer id);
    TopicResponse getTopicBySlug(String slug);
    List<TopicResponse> getAllTopics();
    List<TopicResponse> getAllActiveTopics();

    // Color
    ColorResponse createColor(ColorRequest request);
    ColorResponse updateColor(Integer id, ColorRequest request);
    void deleteColor(Integer id);
    ColorResponse getColorById(Integer id);
    List<ColorResponse> getAllColors();

    // Size
    SizeResponse createSize(SizeRequest request);
    SizeResponse updateSize(Integer id, SizeRequest request);
    void deleteSize(Integer id);
    SizeResponse getSizeById(Integer id);
    List<SizeResponse> getSizesByCategoryType(String categoryType);
    List<SizeResponse> getAllSizes();
}
