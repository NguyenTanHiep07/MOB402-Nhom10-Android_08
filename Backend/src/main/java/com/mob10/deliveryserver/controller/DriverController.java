package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.domain.DriverAvailability;
import com.mob10.deliveryserver.dto.OrderDtos.*;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import com.mob10.deliveryserver.service.DriverOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    private final DriverOrderService service;
    public DriverController(DriverOrderService service) { this.service = service; }

    @GetMapping("/orders/open")
    public List<OrderResponse> openOrders(@AuthenticationPrincipal AuthenticatedUser user) { return service.openOrders(user); }

    @GetMapping("/orders/mine")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal AuthenticatedUser user) { return service.myOrders(user); }

    @PostMapping("/orders/{id}/accept")
    public OrderResponse accept(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return service.accept(user, id);
    }

    @PostMapping("/orders/{id}/reject")
    public RejectResult reject(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                               @Valid @RequestBody RejectOrderRequest request) {
        return service.reject(user, id, request);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateStatus(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                                      @Valid @RequestBody UpdateStatusRequest request) {
        return service.updateStatus(user, id, request);
    }

    @GetMapping("/rejection-reasons")
    public List<RejectionReasonResponse> rejectionReasons() { return service.rejectionReasons(); }

    @GetMapping("/statistics/me")
    public DriverStatisticsResponse statistics(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.myStatistics(user);
    }

    @PatchMapping("/availability")
    public DriverAvailability updateAvailability(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @Valid @RequestBody UpdateAvailabilityRequest request) {
        return service.updateAvailability(user, request);
    }
}
