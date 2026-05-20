package com.thacbao.productservice.service.impl;

import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import com.thacbao.productservice.model.*;
import com.thacbao.productservice.repository.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttributeServiceImplTest {

    @Mock private BrandRepository brandRepository;
    @Mock private CollectionRepository collectionRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private ColorRepository colorRepository;
    @Mock private SizeRepository sizeRepository;
    @Mock private com.thacbao.productservice.service.CloudinaryService cloudinaryService;

    @InjectMocks
    private AttributeServiceImpl attributeService;

    private Brand brand;
    private Collection collection;
    private Topic topic;
    private Color color;
    private Size size;

    @BeforeEach
    void setUp() {
        brand = Brand.builder().name("Brand 1").build(); brand.setId(1);
        collection = Collection.builder().name("Col 1").slug("col-1").build(); collection.setId(1);
        topic = Topic.builder().name("Top 1").slug("top-1").build(); topic.setId(1);
        color = Color.builder().name("Red").build(); color.setId(1);
        size = Size.builder().name("M").build(); size.setId(1);
    }

    @Test
    void createBrand() {
        BrandRequest req = new BrandRequest(); req.setName("Brand 1");
        when(brandRepository.save(any())).thenReturn(brand);
        assertNotNull(attributeService.createBrand(req));
    }

    @Test
    void updateBrand() {
        BrandRequest req = new BrandRequest(); req.setName("Brand updated");
        when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
        when(brandRepository.save(any())).thenReturn(brand);
        assertNotNull(attributeService.updateBrand(1, req));
    }

    @Test
    void deleteBrand() {
        when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
        attributeService.deleteBrand(1);
        verify(brandRepository).delete(brand);
    }

    @Test
    void getBrandById() {
        when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
        assertNotNull(attributeService.getBrandById(1));
    }

    @Test
    void getAllBrands() {
        when(brandRepository.findAll()).thenReturn(List.of(brand));
        assertNotNull(attributeService.getAllBrands());
    }

    @Test
    void getAllActiveBrands() {
        when(brandRepository.findByIsActiveTrue()).thenReturn(List.of(brand));
        assertNotNull(attributeService.getAllActiveBrands());
    }

    @Test
    void createCollection() {
        CollectionRequest req = new CollectionRequest(); req.setName("Col 1");
        when(collectionRepository.save(any())).thenReturn(collection);
        assertNotNull(attributeService.createCollection(req));
    }

    @Test
    void updateCollection() {
        CollectionRequest req = new CollectionRequest(); req.setName("Col updated");
        when(collectionRepository.findById(1)).thenReturn(Optional.of(collection));
        when(collectionRepository.save(any())).thenReturn(collection);
        assertNotNull(attributeService.updateCollection(1, req));
    }

    @Test
    void deleteCollection() {
        attributeService.deleteCollection(1);
        verify(collectionRepository).deleteById(1);
    }

    @Test
    void getCollectionById() {
        when(collectionRepository.findByIdWithSubCategories(1)).thenReturn(Optional.of(collection));
        assertNotNull(attributeService.getCollectionById(1));
    }

    @Test
    void getCollectionBySlug() {
        when(collectionRepository.findBySlug("col-1")).thenReturn(Optional.of(collection));
        assertNotNull(attributeService.getCollectionBySlug("col-1"));
    }

    @Test
    void getAllCollections() {
        when(collectionRepository.findAll()).thenReturn(List.of(collection));
        assertNotNull(attributeService.getAllCollections());
    }

    @Test
    void getAllActiveCollections() {
        when(collectionRepository.findByIsActiveTrue()).thenReturn(List.of(collection));
        assertNotNull(attributeService.getAllActiveCollections());
    }

    @Test
    void createTopic() {
        TopicRequest req = new TopicRequest(); req.setName("Top 1");
        when(topicRepository.save(any())).thenReturn(topic);
        assertNotNull(attributeService.createTopic(req));
    }

    @Test
    void updateTopic() {
        TopicRequest req = new TopicRequest(); req.setName("Top updated");
        when(topicRepository.findById(1)).thenReturn(Optional.of(topic));
        when(topicRepository.save(any())).thenReturn(topic);
        assertNotNull(attributeService.updateTopic(1, req));
    }

    @Test
    void deleteTopic() {
        attributeService.deleteTopic(1);
        verify(topicRepository).deleteById(1);
    }

    @Test
    void getTopicById() {
        when(topicRepository.findById(1)).thenReturn(Optional.of(topic));
        assertNotNull(attributeService.getTopicById(1));
    }

    @Test
    void getTopicBySlug() {
        when(topicRepository.findBySlug("top-1")).thenReturn(Optional.of(topic));
        assertNotNull(attributeService.getTopicBySlug("top-1"));
    }

    @Test
    void getAllTopics() {
        when(topicRepository.findAll()).thenReturn(List.of(topic));
        assertNotNull(attributeService.getAllTopics());
    }

    @Test
    void getAllActiveTopics() {
        when(topicRepository.findByIsActiveTrueOrderByName()).thenReturn(List.of(topic));
        assertNotNull(attributeService.getAllActiveTopics());
    }

    @Test
    void createColor() {
        ColorRequest req = new ColorRequest(); req.setName("Red");
        when(colorRepository.save(any())).thenReturn(color);
        assertNotNull(attributeService.createColor(req));
    }

    @Test
    void updateColor() {
        ColorRequest req = new ColorRequest(); req.setName("Red updated");
        when(colorRepository.findById(1)).thenReturn(Optional.of(color));
        when(colorRepository.save(any())).thenReturn(color);
        assertNotNull(attributeService.updateColor(1, req));
    }

    @Test
    void deleteColor() {
        attributeService.deleteColor(1);
        verify(colorRepository).deleteById(1);
    }

    @Test
    void getColorById() {
        when(colorRepository.findById(1)).thenReturn(Optional.of(color));
        assertNotNull(attributeService.getColorById(1));
    }

    @Test
    void getAllColors() {
        when(colorRepository.findAllByOrderByName()).thenReturn(List.of(color));
        assertNotNull(attributeService.getAllColors());
    }

    @Test
    void createSize() {
        SizeRequest req = new SizeRequest(); req.setName("M");
        when(sizeRepository.save(any())).thenReturn(size);
        assertNotNull(attributeService.createSize(req));
    }

    @Test
    void updateSize() {
        SizeRequest req = new SizeRequest(); req.setName("M updated");
        when(sizeRepository.findById(1)).thenReturn(Optional.of(size));
        when(sizeRepository.save(any())).thenReturn(size);
        assertNotNull(attributeService.updateSize(1, req));
    }

    @Test
    void deleteSize() {
        attributeService.deleteSize(1);
        verify(sizeRepository).deleteById(1);
    }

    @Test
    void getSizeById() {
        when(sizeRepository.findById(1)).thenReturn(Optional.of(size));
        assertNotNull(attributeService.getSizeById(1));
    }

    @Test
    void getSizesByCategoryType() {
        when(sizeRepository.findByCategoryTypeOrderByDisplayOrder("type")).thenReturn(List.of(size));
        assertNotNull(attributeService.getSizesByCategoryType("type"));
    }

    @Test
    void getAllSizes() {
        when(sizeRepository.findAllByOrderByCategoryTypeAscDisplayOrderAsc()).thenReturn(List.of(size));
        assertNotNull(attributeService.getAllSizes());
    }
}
