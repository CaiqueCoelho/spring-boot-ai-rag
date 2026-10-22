package com.caique.adventOfSpring;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/products")
public interface ProductServiceInterface {

    @GetExchange
    List<Product> getProducts();

    @GetExchange("/{id}")
    @Cacheable(value="getProductById", key = "#id")
    Product getProductById(@PathVariable int id);
}
