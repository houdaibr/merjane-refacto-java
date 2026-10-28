package com.nimbleways.springboilerplate.services.implementations;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * Stock operations shared by every product type: each one persists the product
 * and sends the matching customer notification when needed.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public void sellOneUnit(Product product) {
        product.decrementStock();
        productRepository.save(product);
    }

    public void notifyDelay(Product product) {
        productRepository.save(product);
        notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
    }

    public void notifyOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        productRepository.save(product);
    }

    public void markOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.clearStock();
        productRepository.save(product);
    }

    public void markExpired(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.clearStock();
        productRepository.save(product);
    }
}
