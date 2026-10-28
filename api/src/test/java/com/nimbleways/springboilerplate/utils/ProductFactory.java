package com.nimbleways.springboilerplate.utils;

import java.time.LocalDate;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

public final class ProductFactory {

    private ProductFactory() {
    }

    public static Product normal(String name, int available, int leadTime) {
        return new Product(null, leadTime, available, ProductType.NORMAL, name, null, null, null);
    }

    public static Product seasonal(String name, int available, int leadTime, LocalDate seasonStart, LocalDate seasonEnd) {
        return new Product(null, leadTime, available, ProductType.SEASONAL, name, null, seasonStart, seasonEnd);
    }

    public static Product expirable(String name, int available, int leadTime, LocalDate expiryDate) {
        return new Product(null, leadTime, available, ProductType.EXPIRABLE, name, expiryDate, null, null);
    }
}
