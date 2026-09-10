package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.dto.AdminDtos.*;
import com.mob10.deliveryserver.dto.OrderDtos.OrderResponse;
import com.mob10.deliveryserver.service.AdminService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    @GetMapping("/users")
    public List<UserResponse> users() { return service.allUsers(); }
    @GetMapping("/drivers")
    public List<DriverResponse> drivers() { return service.drivers(); }
    @GetMapping("/drivers/alerts")
    public List<DriverResponse> alerts() { return service.alerts(); }
    @GetMapping("/orders")
    public List<OrderResponse> orders() { return service.allOrders(); }
}
