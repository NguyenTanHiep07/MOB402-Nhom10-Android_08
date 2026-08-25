package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.dto.OrderDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.*;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DriverOrderService {
    private final DeliveryRequestRepository orders;
    private final UserRepository users;
    private final StatusHistoryRepository histories;
    private final RejectionReasonRepository reasons;
    private final OrderRejectionRepository rejections;
    private final DriverStatisticsRepository statistics;
    private final DtoMapper mapper;
    private final int lockThreshold;
    private final int lockDurationMinutes;

    public DriverOrderService(DeliveryRequestRepository orders, UserRepository users, StatusHistoryRepository histories,
                              RejectionReasonRepository reasons, OrderRejectionRepository rejections,
                              DriverStatisticsRepository statistics, DtoMapper mapper,
                              @Value("${app.reliability.lock-threshold-in-24-hours}") int lockThreshold,
                              @Value("${app.reliability.lock-duration-minutes}") int lockDurationMinutes) {
        this.orders = orders; this.users = users; this.histories = histories; this.reasons = reasons;
        this.rejections = rejections; this.statistics = statistics; this.mapper = mapper;
        this.lockThreshold = lockThreshold; this.lockDurationMinutes = lockDurationMinutes;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> openOrders(AuthenticatedUser principal) {
        requireDriver(principal);
        return orders.findOpenForDriver(principal.id(), DeliveryStatus.CHO_TIEP_NHAN).stream()
                .map(mapper::toOrderResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(AuthenticatedUser principal) {
        requireDriver(principal);
        return orders.findAllByDeliveryPersonIdOrderByCreatedAtDesc(principal.id()).stream()
                .map(mapper::toOrderResponse).toList();
    }

    @Transactional
    public OrderResponse accept(AuthenticatedUser principal, Long requestId) {
        requireDriver(principal);
        User driver = getDriver(principal.id());
        DriverStatistics stats = getStatistics(driver);
        if (stats.isLocked()) {
            throw new ApiException(HttpStatus.LOCKED, "DRIVER_TEMPORARILY_LOCKED",
                    "Tài xế bị giới hạn nhận đơn đến " + stats.getLockedUntil());
        }
        if (driver.getDriverAvailability() != DriverAvailability.AVAILABLE) {
            throw new ApiException(HttpStatus.CONFLICT, "DRIVER_NOT_AVAILABLE", "Tài xế phải ở trạng thái AVAILABLE để nhận đơn");
        }
        if (rejections.existsByDeliveryRequestIdAndDriverId(requestId, driver.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_ALREADY_REJECTED", "Bạn đã từ chối đơn hàng này");
        }
        int affected = orders.assignAtomically(requestId, driver, DeliveryStatus.CHO_TIEP_NHAN, DeliveryStatus.DA_CHAP_NHAN);
        if (affected == 0) {
            if (!orders.existsById(requestId)) throw new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng");
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_ALREADY_TAKEN", "Đơn hàng đã được tài xế khác nhận hoặc không còn chờ tiếp nhận");
        }
        DeliveryRequest assigned = orders.findByIdForUpdate(requestId).orElseThrow();
        driver.setDriverAvailability(DriverAvailability.BUSY);
        stats.recordAcceptance();
        histories.save(new StatusHistory(assigned, DeliveryStatus.CHO_TIEP_NHAN, DeliveryStatus.DA_CHAP_NHAN,
                driver, "Tài xế đã nhận đơn"));
        assigned.getPackages().size();
        return mapper.toOrderResponse(assigned);
    }

    @Transactional
    public RejectResult reject(AuthenticatedUser principal, Long requestId, RejectOrderRequest input) {
        requireDriver(principal);
        User driver = getDriver(principal.id());
        DeliveryRequest order = orders.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
        if (order.getStatus() != DeliveryStatus.CHO_TIEP_NHAN || order.getDeliveryPerson() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_NOT_OPEN", "Đơn hàng không còn trong danh sách chờ");
        }
        if (rejections.existsByDeliveryRequestIdAndDriverId(requestId, driver.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_ALREADY_REJECTED", "Bạn đã từ chối đơn hàng này");
        }
        RejectionReason reason = reasons.findById(input.reasonCode())
                .filter(RejectionReason::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REJECTION_REASON", "Lý do từ chối không hợp lệ"));
        if (reason.isRequiresNote() && (input.note() == null || input.note().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "REJECTION_NOTE_REQUIRED", "Lý do này yêu cầu nhập ghi chú");
        }
        OrderRejection rejection = rejections.save(new OrderRejection(order, driver, reason, clean(input.note())));
        DriverStatistics stats = getStatistics(driver);
        stats.recordRejection(reason.getPenaltyPoints(), rejection.isPenaltyApplied());
        if (rejection.isPenaltyApplied()) {
            long recentPenalties = rejections.countByDriverIdAndPenaltyAppliedTrueAndRejectedAtAfter(
                    driver.getId(), Instant.now().minus(Duration.ofHours(24)));
            if (recentPenalties >= lockThreshold) stats.lockUntil(Instant.now().plus(Duration.ofMinutes(lockDurationMinutes)));
        }
        return new RejectResult("Đã ghi nhận từ chối; đơn vẫn hiển thị cho tài xế khác",
                rejection.isPenaltyApplied(), mapper.toStatistics(stats));
    }

    @Transactional
    public OrderResponse updateStatus(AuthenticatedUser principal, Long requestId, UpdateStatusRequest input) {
        requireDriver(principal);
        User driver = getDriver(principal.id());
        DeliveryRequest order = orders.findByIdForUpdate(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
        if (order.getDeliveryPerson() == null || !order.getDeliveryPerson().getId().equals(driver.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_ASSIGNED_DRIVER", "Chỉ tài xế đang phụ trách mới được cập nhật trạng thái");
        }
        DeliveryStatus previous = order.getStatus();
        if (!isValidTransition(previous, input.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
                    "Không thể chuyển trạng thái từ " + previous + " sang " + input.status());
        }
        order.changeStatus(input.status());
        histories.save(new StatusHistory(order, previous, input.status(), driver, clean(input.note())));
        if (input.status() == DeliveryStatus.DA_GIAO) driver.setDriverAvailability(DriverAvailability.AVAILABLE);
        order.getPackages().size();
        return mapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<RejectionReasonResponse> rejectionReasons() {
        return reasons.findAllByActiveTrueOrderByCodeAsc().stream()
                .map(reason -> new RejectionReasonResponse(reason.getCode(), reason.getLabel(), reason.isValid(),
                        reason.getPenaltyPoints(), reason.isRequiresNote())).toList();
    }

    @Transactional(readOnly = true)
    public DriverStatisticsResponse myStatistics(AuthenticatedUser principal) {
        requireDriver(principal);
        User driver = getDriver(principal.id());
        return mapper.toStatistics(statistics.findById(driver.getId()).orElseGet(() -> new DriverStatistics(driver)));
    }

    @Transactional
    public DriverAvailability updateAvailability(AuthenticatedUser principal, UpdateAvailabilityRequest input) {
        requireDriver(principal);
        User driver = getDriver(principal.id());
        boolean hasActiveOrder = orders.findAllByDeliveryPersonIdOrderByCreatedAtDesc(driver.getId()).stream()
                .anyMatch(order -> order.getStatus() != DeliveryStatus.DA_GIAO && order.getStatus() != DeliveryStatus.DA_HUY);
        if (hasActiveOrder && input.availability() != DriverAvailability.BUSY) {
            throw new ApiException(HttpStatus.CONFLICT, "DRIVER_HAS_ACTIVE_ORDER",
                    "Tài xế đang có đơn hoạt động nên trạng thái phải là BUSY");
        }
        driver.setDriverAvailability(input.availability());
        return driver.getDriverAvailability();
    }

    private boolean isValidTransition(DeliveryStatus from, DeliveryStatus to) {
        return switch (from) {
            case DA_CHAP_NHAN -> to == DeliveryStatus.DA_DEN_NHA_HANG;
            case DA_DEN_NHA_HANG -> to == DeliveryStatus.DA_LAY_HANG;
            case DA_LAY_HANG -> to == DeliveryStatus.DA_DEN_KHACH_HANG;
            case DA_DEN_KHACH_HANG -> to == DeliveryStatus.DA_GIAO;
            default -> false;
        };
    }

    private DriverStatistics getStatistics(User driver) {
        return statistics.findById(driver.getId()).orElseGet(() -> statistics.save(new DriverStatistics(driver)));
    }
    private User getDriver(Long id) {
        User user = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Tài khoản không tồn tại"));
        if (user.getRole() != Role.DELIVERY) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_A_DRIVER", "Tài khoản không phải tài xế");
        return user;
    }
    private void requireDriver(AuthenticatedUser principal) {
        if (principal.role() != Role.DELIVERY) throw new ApiException(HttpStatus.FORBIDDEN, "NOT_A_DRIVER", "Chức năng chỉ dành cho tài xế");
    }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
