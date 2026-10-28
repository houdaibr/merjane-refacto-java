package com.nimbleways.springboilerplate.services.implementations.processors;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

/**
 * Applies the business rules of one {@link ProductType} when a product is ordered.
 */
public interface ProductProcessor {

    ProductType supportedType();

    void process(Product product);
}
