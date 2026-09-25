package com.letsTrade.demo.order.controller;

import com.letsTrade.demo.order.dto.CreateOrderRequest;
import com.letsTrade.demo.order.dto.OrderResponse;
import com.letsTrade.demo.order.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse createOrder(
            Authentication authentication,
            @RequestBody CreateOrderRequest request
    ) {
        String email = authentication.getName();
        return orderService.createAndExecuteOrder(email, request);
    }

    @GetMapping
    public List<OrderResponse> getOrders(Authentication authentication) {
        String email = authentication.getName();
        return orderService.getUserOrders(email);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        return orderService.getOrderById(email, id);
    }
}
