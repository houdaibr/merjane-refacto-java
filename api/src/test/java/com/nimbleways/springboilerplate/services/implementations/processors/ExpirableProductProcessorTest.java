package com.nimbleways.springboilerplate.services.implementations.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import com.nimbleways.springboilerplate.utils.ProductFactory;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ExpirableProductProcessorTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 6, 15);

    @Mock
    private ProductService productService;

    private ExpirableProductProcessor processor;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        processor = new ExpirableProductProcessor(productService, clock);
    }

    @Test
    void supportsExpirableProducts() {
        assertEquals(ProductType.EXPIRABLE, processor.supportedType());
    }

    @Test
    void sellsOneUnitWhenInStockAndNotExpired() {
        Product product = ProductFactory.expirable("Butter", 30, 15, TODAY.plusDays(26));

        processor.process(product);

        verify(productService).sellOneUnit(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void marksExpiredWhenExpiryDateHasPassed() {
        Product product = ProductFactory.expirable("Milk", 6, 90, TODAY.minusDays(2));

        processor.process(product);

        verify(productService).markExpired(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void marksExpiredWhenExpiringToday() {
        Product product = ProductFactory.expirable("Milk", 6, 90, TODAY);

        processor.process(product);

        verify(productService).markExpired(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void marksExpiredWhenOutOfStockEvenIfNotExpired() {
        Product product = ProductFactory.expirable("Butter", 0, 15, TODAY.plusDays(26));

        processor.process(product);

        verify(productService).markExpired(product);
        verifyNoMoreInteractions(productService);
    }
}
