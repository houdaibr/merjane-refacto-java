package com.nimbleways.springboilerplate.services.implementations.processors;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import lombok.RequiredArgsConstructor;

/**
 * EXPIRABLE products: sold while in stock and not expired, otherwise marked as expired.
 */
@Component
@RequiredArgsConstructor
public class ExpirableProductProcessor implements ProductProcessor {

    private final ProductService productService;
    private final Clock clock;

    @Override
    public ProductType supportedType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public void process(Product product) {
        LocalDate today = LocalDate.now(clock);
        if (product.isInStock() && !product.isExpired(today)) {
            productService.sellOneUnit(product);
        } else {
            productService.markExpired(product);
        }
    }
}
