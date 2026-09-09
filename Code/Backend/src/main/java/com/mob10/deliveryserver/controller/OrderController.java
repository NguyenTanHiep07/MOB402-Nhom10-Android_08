package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.dto.OrderDtos.*;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import com.mob10.deliveryserver.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(user, request);
    }

    @GetMapping
    public List<OrderResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.list(user);
    }

    @GetMapping("/{id}")
    public OrderResponse detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return orderService.detail(user, id);
    }

    @GetMapping("/{id}/history")
    public List<HistoryResponse> history(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return orderService.history(user, id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return orderService.cancel(user, id);
    }
}
