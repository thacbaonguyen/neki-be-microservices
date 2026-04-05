package com.thacbao.productservice.repository.elasticsearch;

import com.thacbao.productservice.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, Integer> {

    Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    @Query("""
{
  "bool": {
    "must": [
      {
        "multi_match": {
          "query": "?0",
          "fields": ["name^3", "description"]
        }
      }
    ],
    "filter": [
      {
        "term": {
          "isActive": true
        }
      }
    ]
  }
}
""")
    Page<ProductDocument> searchProducts(String keyword, Pageable pageable);
}
