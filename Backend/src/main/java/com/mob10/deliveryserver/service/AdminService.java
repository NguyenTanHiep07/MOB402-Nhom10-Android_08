package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.dto.AdminDtos.*;
import com.mob10.deliveryserver.dto.AuthDtos.MessageResponse;
import com.mob10.deliveryserver.dto.OrderDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminService {
    private final UserRepository users;
    private final DeliveryRequestRepository orders;
    private final DriverStatisticsRepository statistics;
    private final DriverRegistrationRequestRepository driverRequests;
    private final StatusHistoryRepository histories;
    private final DtoMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final SequenceGeneratorService sequences;
    private final BigDecimal alertThreshold;
    private final String demoPassword;

    public AdminService(UserRepository users, DeliveryRequestRepository orders,
                        DriverStatisticsRepository statistics, DriverRegistrationRequestRepository driverRequests, StatusHistoryRepository histories,
                        DtoMapper mapper, PasswordEncoder passwordEncoder,
                        SequenceGeneratorService sequences,
                        @Value("${app.reliability.alert-score-threshold}") BigDecimal alertThreshold,
                        @Value("${app.demo.password:123456}") String demoPassword) {
        this.users = users; this.orders = orders; this.statistics = statistics; this.driverRequests = driverRequests;
        this.histories = histories; this.mapper = mapper; this.passwordEncoder = passwordEncoder;
        this.sequences = sequences; this.alertThreshold = alertThreshold; this.demoPassword = demoPassword;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> allUsers() {
        return users.findAll().stream().map(this::toUser).toList();
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> drivers() {
        return users.findAllByRoleOrderByIdAsc(Role.DELIVERY).stream().map(driver ->
                new DriverResponse(toUser(driver), mapper.toStatistics(statistics.findById(driver.getId())
                        .orElseGet(() -> new DriverStatistics(driver))))).toList();
    }

    public List<DriverResponse> alerts() {
        return statistics.findAllByReliabilityScoreLessThanOrderByReliabilityScoreAsc(alertThreshold).stream()
                .map(stats -> new DriverResponse(toUser(users.findById(stats.getDriverId()).orElse(null)), mapper.toStatistics(stats)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        return orders.findAllDetailed().stream().map(mapper::toOrderResponse).toList();
    }

    /** Vô hiệu hóa hoặc kích hoạt lại tài khoản người dùng. */
    @Transactional
    public UserResponse toggleActive(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản"));
        user.setActive(!user.isActive());
        users.save(user);
        return toUser(user);
    }

    /** Reset mật khẩu người dùng về mật khẩu mặc định. */
    @Transactional
    public MessageResponse resetPassword(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản"));
        user.setPasswordHash(passwordEncoder.encode(demoPassword));
        user.setCredentialVersion(user.getCredentialVersion() + 1);
        users.save(user);
        return new MessageResponse("Đã reset mật khẩu cho " + user.getFullName() + " về mật khẩu mặc định.");
    }

    /** Admin hủy đơn hàng bất kỳ — override quyền chủ đơn. */
    @Transactional
    public OrderResponse adminCancelOrder(Long orderId) {
        DeliveryRequest order = orders.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
        if (order.getStatus() == DeliveryStatus.DA_GIAO) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_ALREADY_DELIVERED", "Đơn hàng đã giao thành công, không thể hủy");
        }
        if (order.getStatus() == DeliveryStatus.DA_HUY) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_ALREADY_CANCELLED", "Đơn hàng đã bị hủy trước đó");
        }
        DeliveryStatus previous = order.getStatus();
        order.changeStatus(DeliveryStatus.DA_HUY);
        orders.save(order);
        // Giải phóng tài xế nếu đã được phân công
        if (order.getDeliveryPerson() != null) {
            User driver = users.findById(order.getDeliveryPerson().getId()).orElse(null);
            if (driver != null) {
                driver.setDriverAvailability(DriverAvailability.AVAILABLE);
                users.save(driver);
            }
        }
        StatusHistory history = new StatusHistory(order, previous, DeliveryStatus.DA_HUY, null, "Admin hủy đơn hàng");
        history.setId(sequences.generateSequence("status_histories"));
        histories.save(history);
        return mapper.toOrderResponse(order);
    }

    private UserResponse toUser(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getPhoneNumber(),
                user.getRole(), user.getLicensePlate(), user.getDriverAvailability(), user.isActive(), user.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<DriverRegistrationResponse> pendingDriverRequests() {
        return driverRequests.findAllByStatusOrderByIdDesc("PENDING").stream().map(req -> {
            User u = users.findById(req.getUserId()).orElse(null);
            return new DriverRegistrationResponse(req.getId(), u != null ? toUser(u) : null, req.getLicensePlate(), req.getStatus(), req.getCreatedAt());
        }).toList();
    }

    @Transactional
    public DriverRegistrationResponse processDriverRequest(Long requestId, boolean approve) {
        DriverRegistrationRequest req = driverRequests.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "Không tìm thấy yêu cầu"));
        
        if (!"PENDING".equals(req.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Yêu cầu đã được xử lý");
        }

        User u = users.findById(req.getUserId()).orElse(null);
        if (approve && u != null) {
            u.setRole(Role.DELIVERY);
            u.setLicensePlate(req.getLicensePlate());
            u.setDriverAvailability(DriverAvailability.AVAILABLE);
            users.save(u);
            req.setStatus("APPROVED");
        } else {
            req.setStatus("REJECTED");
        }
        driverRequests.save(req);

        return new DriverRegistrationResponse(req.getId(), u != null ? toUser(u) : null, req.getLicensePlate(), req.getStatus(), req.getCreatedAt());
    }
}

