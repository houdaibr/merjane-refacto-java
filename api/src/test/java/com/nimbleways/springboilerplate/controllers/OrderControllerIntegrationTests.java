package com.nimbleways.springboilerplate.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.ProductFactory;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTests {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void processOrderAppliesTheRulesOfEachProductType() throws Exception {
        Product usbCable = ProductFactory.normal("USB Cable", 30, 15);
        Product usbDongle = ProductFactory.normal("USB Dongle", 0, 10);
        Product butter = ProductFactory.expirable("Butter", 30, 15, TODAY.plusDays(26));
        Product milk = ProductFactory.expirable("Milk", 6, 90, TODAY.minusDays(2));
        Product watermelon = ProductFactory.seasonal("Watermelon", 30, 15, TODAY.minusDays(2), TODAY.plusDays(58));
        Product grapes = ProductFactory.seasonal("Grapes", 30, 15, TODAY.plusDays(180), TODAY.plusDays(240));
        Product strawberry = ProductFactory.seasonal("Strawberry", 0, 30, TODAY.minusDays(10), TODAY.plusDays(5));
        Order order = saveOrder(List.of(usbCable, usbDongle, butter, milk, watermelon, grapes, strawberry));

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));

        assertEquals(29, availableOf(usbCable));
        assertEquals(0, availableOf(usbDongle));
        verify(notificationService).sendDelayNotification(10, "USB Dongle");

        assertEquals(29, availableOf(butter));
        assertEquals(0, availableOf(milk));
        verify(notificationService).sendExpirationNotification("Milk", TODAY.minusDays(2));

        assertEquals(29, availableOf(watermelon));
        assertEquals(30, availableOf(grapes));
        verify(notificationService).sendOutOfStockNotification("Grapes");
        assertEquals(0, availableOf(strawberry));
        verify(notificationService).sendOutOfStockNotification("Strawberry");

        verify(notificationService, never()).sendOutOfStockNotification("Watermelon");
        verify(notificationService, never()).sendDelayNotification(15, "USB Cable");
        verify(notificationService, never()).sendExpirationNotification("Butter", TODAY.plusDays(26));
    }

    @Test
    void processOrderReturnsNotFoundForUnknownOrder() throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", Long.MAX_VALUE)
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    private Order saveOrder(List<Product> products) {
        productRepository.saveAll(products);
        Order order = new Order();
        order.setItems(new HashSet<>(products));
        return orderRepository.save(order);
    }

    private int availableOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getAvailable();
    }
}
