package com.nimbleways.springboilerplate.services.implementations.processors;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import lombok.RequiredArgsConstructor;

/**
 * SEASONAL products: sold while in season and in stock. Otherwise, the product is unavailable
 * if restocking would end after the season or if the season has not started yet;
 * in any other case customers are told about the restocking delay.
 */
@Component
@RequiredArgsConstructor
public class SeasonalProductProcessor implements ProductProcessor {

    private final ProductService productService;
    private final Clock clock;

    @Override
    public ProductType supportedType() {
        return ProductType.SEASONAL;
    }

    @Override
    public void process(Product product) {
        LocalDate today = LocalDate.now(clock);
        if (product.isInSeason(today) && product.isInStock()) {
            productService.sellOneUnit(product);
        } else if (product.isRestockedAfterSeasonEnd(today)) {
            productService.markOutOfStock(product);
        } else if (product.isSeasonNotStarted(today)) {
            productService.notifyOutOfStock(product);
        } else {
            productService.notifyDelay(product);
        }
    }
}
