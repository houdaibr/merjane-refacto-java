package com.nimbleways.springboilerplate.services.implementations.processors;

import org.springframework.stereotype.Component;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import lombok.RequiredArgsConstructor;

/**
 * NORMAL products: sold while in stock, otherwise customers are told about the restocking delay.
 */
@Component
@RequiredArgsConstructor
public class NormalProductProcessor implements ProductProcessor {

    private final ProductService productService;

    @Override
    public ProductType supportedType() {
        return ProductType.NORMAL;
    }

    @Override
    public void process(Product product) {
        if (product.isInStock()) {
            productService.sellOneUnit(product);
        } else if (product.hasLeadTime()) {
            productService.notifyDelay(product);
        }
    }
}
