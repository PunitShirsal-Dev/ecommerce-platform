package com.example.ecommerce.order.web;

import com.example.ecommerce.order.domain.OrderEntity;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.order.service.OrderCommandService;
import com.example.ecommerce.order.service.OrderDetailsService;
import com.example.ecommerce.order.web.dto.PlaceOrderRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderCommandService commandService;
    private final OrderDetailsService detailsService;

    public OrderController(
            OrderRepository orderRepository,
            OrderCommandService commandService,
            OrderDetailsService detailsService) {
        this.orderRepository = orderRepository;
        this.commandService = commandService;
        this.detailsService = detailsService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<OrderEntity> list() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public OrderEntity get(@PathVariable String id) {
        return orderRepository.findWithLinesById(id).orElseThrow();
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public OrderDetailsService.OrderDetails details(@PathVariable String id) {
        return detailsService.aggregate(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderEntity place(@RequestBody PlaceOrderRequest request) {
        return commandService.placeOrder(request);
    }
}
