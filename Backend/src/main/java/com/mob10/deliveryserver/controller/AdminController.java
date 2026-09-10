package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.dto.AdminDtos.*;
import com.mob10.deliveryserver.dto.AuthDtos.MessageResponse;
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

    /** Vô hiệu hóa hoặc kích hoạt lại tài khoản. */
    @PatchMapping("/users/{id}/toggle-active")
    public UserResponse toggleActive(@PathVariable Long id) { return service.toggleActive(id); }

    /** Reset mật khẩu người dùng về mật khẩu mặc định. */
    @PostMapping("/users/{id}/reset-password")
    public MessageResponse resetPassword(@PathVariable Long id) { return service.resetPassword(id); }

    /** Admin hủy đơn hàng bất kỳ — override quyền chủ đơn. */
    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) { return service.adminCancelOrder(id); }

    @GetMapping("/driver-requests")
    public List<DriverRegistrationResponse> pendingDriverRequests() { return service.pendingDriverRequests(); }

    @PostMapping("/driver-requests/{id}/approve")
    public DriverRegistrationResponse approveDriverRequest(@PathVariable Long id) { return service.processDriverRequest(id, true); }

    @PostMapping("/driver-requests/{id}/reject")
    public DriverRegistrationResponse rejectDriverRequest(@PathVariable Long id) { return service.processDriverRequest(id, false); }
}

