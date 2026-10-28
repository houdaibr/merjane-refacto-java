package com.nimbleways.springboilerplate.services.implementations;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.implementations.processors.ProductProcessor;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final Map<ProductType, ProductProcessor> processorsByType = new EnumMap<>(ProductType.class);

    public OrderService(OrderRepository orderRepository, List<ProductProcessor> processors) {
        this.orderRepository = orderRepository;
        processors.forEach(processor -> processorsByType.put(processor.supportedType(), processor));
    }

    @Transactional
    public Order processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.getItems().forEach(this::processProduct);
        return order;
    }

    private void processProduct(Product product) {
        ProductProcessor processor = processorsByType.get(product.getType());
        if (processor != null) {
            processor.process(product);
        }
    }
}
