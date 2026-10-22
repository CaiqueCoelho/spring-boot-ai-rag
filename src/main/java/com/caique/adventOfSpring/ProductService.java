package com.caique.adventOfSpring;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;

//@Service
//public class ProductService {
//
//    private final RestClient restClient;
//
//    public ProductService(RestClient restClient) {
//        this.restClient = restClient;
//    }
//
//    public List<Product> getProducts() {
//        ParameterizedTypeReference<List<Product>> type = new ParameterizedTypeReference<>() {};
//        return restClient.get().uri("/products").retrieve().body(type);
//    }
//
//    public Product getProductById(int id) {
//        return restClient.get().uri("/products/{id}", id).retrieve().body(Product.class);
//    }
//}
