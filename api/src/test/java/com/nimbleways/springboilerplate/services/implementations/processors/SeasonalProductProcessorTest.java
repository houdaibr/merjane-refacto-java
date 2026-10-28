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
class SeasonalProductProcessorTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 6, 15);

    @Mock
    private ProductService productService;

    private SeasonalProductProcessor processor;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        processor = new SeasonalProductProcessor(productService, clock);
    }

    @Test
    void supportsSeasonalProducts() {
        assertEquals(ProductType.SEASONAL, processor.supportedType());
    }

    @Test
    void sellsOneUnitWhenInSeasonAndInStock() {
        Product product = seasonal(30, 15, TODAY.minusDays(2), TODAY.plusDays(58));

        processor.process(product);

        verify(productService).sellOneUnit(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void notifiesDelayWhenOutOfStockAndRestockedBeforeSeasonEnd() {
        Product product = seasonal(0, 15, TODAY.minusDays(2), TODAY.plusDays(58));

        processor.process(product);

        verify(productService).notifyDelay(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void marksOutOfStockWhenRestockWouldEndAfterSeasonEnd() {
        Product product = seasonal(0, 20, TODAY.minusDays(2), TODAY.plusDays(10));

        processor.process(product);

        verify(productService).markOutOfStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void marksOutOfStockWhenSeasonIsOverEvenIfInStock() {
        Product product = seasonal(30, 5, TODAY.minusDays(60), TODAY.minusDays(1));

        processor.process(product);

        verify(productService).markOutOfStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void notifiesOutOfStockWhenSeasonHasNotStartedYet() {
        Product product = seasonal(30, 15, TODAY.plusDays(180), TODAY.plusDays(240));

        processor.process(product);

        verify(productService).notifyOutOfStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void marksOutOfStockWhenSeasonHasNotStartedAndRestockWouldEndAfterSeason() {
        Product product = seasonal(30, 100, TODAY.plusDays(10), TODAY.plusDays(20));

        processor.process(product);

        verify(productService).markOutOfStock(product);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void notifiesDelayOnFirstDayOfSeasonBecauseSeasonBoundsAreExclusive() {
        Product product = seasonal(30, 15, TODAY, TODAY.plusDays(58));

        processor.process(product);

        verify(productService).notifyDelay(product);
        verifyNoMoreInteractions(productService);
    }

    private static Product seasonal(int available, int leadTime, LocalDate seasonStart, LocalDate seasonEnd) {
        return ProductFactory.seasonal("Watermelon", available, leadTime, seasonStart, seasonEnd);
    }
}
