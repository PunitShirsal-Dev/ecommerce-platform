package com.example.ecommerce.order.service;

import com.example.ecommerce.order.domain.OrderEntity;
import com.example.ecommerce.order.feign.CustomerClient;
import com.example.ecommerce.order.feign.InventoryClient;
import com.example.ecommerce.order.repo.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class OrderDetailsService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final InventoryClient inventoryClient;
    private final Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public OrderDetailsService(
            OrderRepository orderRepository,
            CustomerClient customerClient,
            InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
        this.inventoryClient = inventoryClient;
    }

    public OrderDetails aggregate(String orderId) {
        OrderEntity order = orderRepository.findWithLinesById(orderId).orElseThrow();
        CompletableFuture<Map<String, Object>> customerFuture =
                CompletableFuture.supplyAsync(() -> customerClient.getCustomer(order.getCustomerId()), virtualThreadExecutor);
        List<CompletableFuture<Map<String, Object>>> stockFutures = order.getLines().stream()
                .map(line -> CompletableFuture.supplyAsync(
                        () -> inventoryClient.getStock(line.getProductId()), virtualThreadExecutor))
                .toList();
        CompletableFuture<Void> stocksDone =
                CompletableFuture.allOf(stockFutures.toArray(CompletableFuture[]::new));
        CompletableFuture.allOf(customerFuture, stocksDone).join();
        Map<String, Object> customer = customerFuture.join();
        List<Map<String, Object>> stocks = stockFutures.stream().map(CompletableFuture::join).toList();
        return new OrderDetails(order, customer, stocks);
    }

    public record OrderDetails(
            OrderEntity order,
            Map<String, Object> customer,
            List<Map<String, Object>> lineStocks
    ) {
    }
}
