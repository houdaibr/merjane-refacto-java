package com.nimbleways.springboilerplate.services.implementations.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import com.nimbleways.springboilerplate.utils.ProductFactory;

@ExtendWith(MockitoExtension.class)
@UnitTest
class NormalProductProcessorTest {

    @Mock
    private ProductService productService;
    @InjectMocks
    private NormalProductProcessor processor;

    @Test
    void supportsNormalProducts() {
        assertEquals(ProductType.NORMAL, processor.supportedType());
    }

    @Test
    void sellsOneUnitWhenInStock() {
        Product product = ProductFactory.normal("USB Cable", 30, 15);

        processor.process(product);

        verify(productService).sellOneUnit(product);
    }

    @Test
    void notifiesDelayWhenOutOfStockWithLeadTime() {
        Product product = ProductFactory.normal("USB Dongle", 0, 10);

        processor.process(product);

        verify(productService).notifyDelay(product);
    }

    @Test
    void doesNothingWhenOutOfStockWithoutLeadTime() {
        Product product = ProductFactory.normal("USB Dongle", 0, 0);

        processor.process(product);

        verifyNoInteractions(productService);
    }
}
