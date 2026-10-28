package com.nimbleways.springboilerplate.services.implementations;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.implementations.processors.ProductProcessor;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import com.nimbleways.springboilerplate.utils.ProductFactory;

@ExtendWith(MockitoExtension.class)
@UnitTest
class OrderServiceTest {

    private static final Long ORDER_ID = 1L;

    @Mock
    private OrderRepository orderRepository;

    private ProductProcessor normalProcessor;
    private ProductProcessor expirableProcessor;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        normalProcessor = processorFor(ProductType.NORMAL);
        expirableProcessor = processorFor(ProductType.EXPIRABLE);
        orderService = new OrderService(orderRepository, List.of(normalProcessor, expirableProcessor));
    }

    @Test
    void processesEachItemWithTheProcessorOfItsType() {
        Product cable = ProductFactory.normal("USB Cable", 30, 15);
        Product milk = ProductFactory.expirable("Milk", 6, 90, null);
        Order order = new Order(ORDER_ID, Set.of(cable, milk));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        Order result = orderService.processOrder(ORDER_ID);

        assertSame(order, result);
        verify(normalProcessor).process(cable);
        verify(expirableProcessor).process(milk);
        verify(normalProcessor, never()).process(milk);
        verify(expirableProcessor, never()).process(cable);
    }

    @Test
    void ignoresItemsWithoutAMatchingProcessor() {
        Product watermelon = ProductFactory.seasonal("Watermelon", 30, 15, null, null);
        Order order = new Order(ORDER_ID, Set.of(watermelon));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.processOrder(ORDER_ID);

        verify(normalProcessor, never()).process(watermelon);
        verify(expirableProcessor, never()).process(watermelon);
    }

    @Test
    void throwsWhenOrderDoesNotExist() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.processOrder(ORDER_ID));
    }

    private static ProductProcessor processorFor(ProductType type) {
        ProductProcessor processor = mock(ProductProcessor.class);
        when(processor.supportedType()).thenReturn(type);
        return processor;
    }
}
