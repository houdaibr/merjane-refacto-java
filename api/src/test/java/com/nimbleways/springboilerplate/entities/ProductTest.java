package com.nimbleways.springboilerplate.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import com.nimbleways.springboilerplate.utils.ProductFactory;

@UnitTest
class ProductTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 6, 15);

    @Test
    void isInStockOnlyWhenAtLeastOneUnitIsAvailable() {
        assertTrue(ProductFactory.normal("Cable", 1, 0).isInStock());
        assertFalse(ProductFactory.normal("Cable", 0, 0).isInStock());
    }

    @Test
    void decrementStockRemovesOneUnit() {
        Product product = ProductFactory.normal("Cable", 3, 0);

        product.decrementStock();

        assertEquals(2, product.getAvailable());
    }

    @Test
    void clearStockSetsAvailableToZero() {
        Product product = ProductFactory.normal("Cable", 3, 0);

        product.clearStock();

        assertEquals(0, product.getAvailable());
    }

    @Test
    void hasLeadTimeOnlyWhenPositive() {
        assertTrue(ProductFactory.normal("Cable", 0, 1).hasLeadTime());
        assertFalse(ProductFactory.normal("Cable", 0, 0).hasLeadTime());
    }

    @Test
    void isExpiredFromExpiryDateOnwards() {
        assertFalse(ProductFactory.expirable("Milk", 1, 0, TODAY.plusDays(1)).isExpired(TODAY));
        assertTrue(ProductFactory.expirable("Milk", 1, 0, TODAY).isExpired(TODAY));
        assertTrue(ProductFactory.expirable("Milk", 1, 0, TODAY.minusDays(1)).isExpired(TODAY));
    }

    @Test
    void isInSeasonStrictlyBetweenSeasonBounds() {
        assertTrue(seasonal(TODAY.minusDays(1), TODAY.plusDays(1)).isInSeason(TODAY));
        assertFalse(seasonal(TODAY, TODAY.plusDays(1)).isInSeason(TODAY));
        assertFalse(seasonal(TODAY.minusDays(1), TODAY).isInSeason(TODAY));
        assertFalse(seasonal(TODAY.plusDays(1), TODAY.plusDays(10)).isInSeason(TODAY));
    }

    @Test
    void isSeasonNotStartedWhenSeasonStartsAfterToday() {
        assertTrue(seasonal(TODAY.plusDays(1), TODAY.plusDays(10)).isSeasonNotStarted(TODAY));
        assertFalse(seasonal(TODAY, TODAY.plusDays(10)).isSeasonNotStarted(TODAY));
    }

    @Test
    void isRestockedAfterSeasonEndWhenLeadTimeGoesBeyondSeasonEnd() {
        Product product = ProductFactory.seasonal("Watermelon", 0, 10, TODAY.minusDays(1), TODAY.plusDays(10));
        assertFalse(product.isRestockedAfterSeasonEnd(TODAY));

        product.setLeadTime(11);
        assertTrue(product.isRestockedAfterSeasonEnd(TODAY));
    }

    private static Product seasonal(LocalDate seasonStart, LocalDate seasonEnd) {
        return ProductFactory.seasonal("Watermelon", 1, 0, seasonStart, seasonEnd);
    }
}
