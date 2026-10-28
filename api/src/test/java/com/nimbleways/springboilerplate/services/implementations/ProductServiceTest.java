package com.nimbleways.springboilerplate.services.implementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import com.nimbleways.springboilerplate.utils.ProductFactory;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ProductServiceTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void sellOneUnitDecrementsStockAndSavesWithoutNotifying() {
        Product product = ProductFactory.normal("USB Cable", 30, 15);

        productService.sellOneUnit(product);

        assertEquals(29, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void notifyDelaySavesAndSendsDelayNotification() {
        Product product = ProductFactory.normal("RJ45 Cable", 0, 15);

        productService.notifyDelay(product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "RJ45 Cable");
    }

    @Test
    void notifyOutOfStockSendsNotificationAndKeepsStock() {
        Product product = seasonalProduct(5);

        productService.notifyOutOfStock(product);

        assertEquals(5, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendOutOfStockNotification("Grapes");
    }

    @Test
    void markOutOfStockSendsNotificationAndClearsStock() {
        Product product = seasonalProduct(5);

        productService.markOutOfStock(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendOutOfStockNotification("Grapes");
    }

    @Test
    void markExpiredSendsExpirationNotificationAndClearsStock() {
        LocalDate expiryDate = LocalDate.of(2024, 1, 1);
        Product product = ProductFactory.expirable("Milk", 6, 90, expiryDate);

        productService.markExpired(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendExpirationNotification("Milk", expiryDate);
    }

    private static Product seasonalProduct(int available) {
        return ProductFactory.seasonal("Grapes", available, 15, LocalDate.of(2024, 9, 1), LocalDate.of(2024, 11, 1));
    }
}
