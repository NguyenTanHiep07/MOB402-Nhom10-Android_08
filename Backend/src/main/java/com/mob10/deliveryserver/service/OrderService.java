package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.dto.LocationDtos.CoordinateInput;
import com.mob10.deliveryserver.dto.OrderDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.*;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderService {
    private final DeliveryRequestRepository orders;
    private final UserRepository users;
    private final StatusHistoryRepository histories;
    private final OrderRejectionRepository rejections;
    private final DtoMapper mapper;
    private final LocationService locationService;
    private final PricingService pricingService;

    public OrderService(DeliveryRequestRepository orders, UserRepository users, StatusHistoryRepository histories,
                        OrderRejectionRepository rejections, DtoMapper mapper,
                        LocationService locationService, PricingService pricingService) {
        this.orders = orders; this.users = users; this.histories = histories; this.rejections = rejections; this.mapper = mapper;
        this.locationService = locationService; this.pricingService = pricingService;
    }

    @Transactional
    public OrderResponse create(AuthenticatedUser principal, CreateOrderRequest input) {
        requireRole(principal, Role.CLIENT);
        User client = getUser(principal.id());
        CoordinateInput pickup = new CoordinateInput(input.pickupLatitude(), input.pickupLongitude());
        CoordinateInput delivery = new CoordinateInput(input.deliveryLatitude(), input.deliveryLongitude());
        OpenStreetMapClient.RouteMetrics route = locationService.calculateRoute(pickup, delivery);

        DeliveryRequest order = new DeliveryRequest(client, route.distanceKm(), input.pickupAddress().trim(),
                input.deliveryAddress().trim(), coordinate(input.pickupLatitude()), coordinate(input.pickupLongitude()),
                coordinate(input.deliveryLatitude()), coordinate(input.deliveryLongitude()),
                input.senderName().trim(), input.senderPhone().trim(),
                input.recipientName().trim(), input.recipientPhone().trim(), clean(input.note()), input.scheduledPickupTime());

        BigDecimal totalWeight = BigDecimal.ZERO;
        boolean fragile = false;
        boolean express = false;
        for (PackageInput item : input.packages()) {
            BigDecimal itemWeight = money(item.weightKg());
            totalWeight = totalWeight.add(itemWeight.multiply(BigDecimal.valueOf(item.quantity())));
            fragile |= item.fragile();
            express |= item.express();
            order.addPackage(new PackageItem(item.name().trim(), clean(item.packageType()), itemWeight,
                    item.quantity(), clean(item.notes()), item.fragile(), item.express()));
        }
        PricingService.PricingQuote quote = pricingService.quote(route.distanceKm(), totalWeight, fragile, express);
        order.applyFees(quote.baseFee(), quote.distanceFee(), quote.weightFee(), quote.serviceFee(), quote.totalFee());
        orders.save(order);
        histories.save(new StatusHistory(order, null, DeliveryStatus.CHO_TIEP_NHAN, client, "Đơn hàng được tạo"));
        return mapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(AuthenticatedUser principal) {
        List<DeliveryRequest> result = switch (principal.role()) {
            case CLIENT -> orders.findAllByClientIdOrderByCreatedAtDesc(principal.id());
            case DELIVERY -> orders.findAllByDeliveryPersonIdOrderByCreatedAtDesc(principal.id());
            case ADMIN -> orders.findAllDetailed();
        };
        return result.stream().map(mapper::toOrderResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse detail(AuthenticatedUser principal, Long id) {
        DeliveryRequest order = orders.findById(id).orElseThrow(() -> notFound(id));
        assertCanView(principal, order);
        order.getPackages().size();
        return mapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> history(AuthenticatedUser principal, Long id) {
        DeliveryRequest order = orders.findById(id).orElseThrow(() -> notFound(id));
        assertCanView(principal, order);
        return histories.findAllByDeliveryRequestIdOrderByTimestampAscIdAsc(id).stream()
                .map(mapper::toHistoryResponse).toList();
    }

    @Transactional
    public OrderResponse cancel(AuthenticatedUser principal, Long id) {
        requireRole(principal, Role.CLIENT);
        DeliveryRequest order = orders.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
        if (!order.getClient().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_ORDER_OWNER", "Bạn không phải chủ của đơn hàng này");
        }
        if (order.getStatus() != DeliveryStatus.CHO_TIEP_NHAN && order.getStatus() != DeliveryStatus.DA_CHAP_NHAN
                && order.getStatus() != DeliveryStatus.DA_DEN_NHA_HANG) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_CANNOT_BE_CANCELLED", "Đơn hàng không còn ở trạng thái có thể hủy");
        }
        DeliveryStatus previous = order.getStatus();
        order.changeStatus(DeliveryStatus.DA_HUY);
        if (order.getDeliveryPerson() != null) {
            users.findByIdForUpdate(order.getDeliveryPerson().getId()).orElseThrow().setDriverAvailability(DriverAvailability.AVAILABLE);
        }
        histories.save(new StatusHistory(order, previous, DeliveryStatus.DA_HUY, getUser(principal.id()), "Khách hàng hủy đơn"));
        return mapper.toOrderResponse(order);
    }

    private void assertCanView(AuthenticatedUser principal, DeliveryRequest order) {
        boolean allowed = principal.role() == Role.ADMIN
                || (principal.role() == Role.CLIENT && order.getClient().getId().equals(principal.id()))
                || (principal.role() == Role.DELIVERY && order.getDeliveryPerson() != null
                    && order.getDeliveryPerson().getId().equals(principal.id()))
                || (principal.role() == Role.DELIVERY && order.getStatus() == DeliveryStatus.CHO_TIEP_NHAN
                    && !rejections.existsByDeliveryRequestIdAndDriverId(order.getId(), principal.id()));
        if (!allowed) throw new ApiException(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", "Bạn không có quyền xem đơn hàng này");
    }

    private User getUser(Long id) {
        return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Tài khoản không còn tồn tại"));
    }
    private void requireRole(AuthenticatedUser principal, Role role) {
        if (principal.role() != role) throw new ApiException(HttpStatus.FORBIDDEN, "ROLE_NOT_ALLOWED", "Vai trò không được phép thực hiện thao tác");
    }
    private ApiException notFound(Long id) { return new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng " + id); }
    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal coordinate(BigDecimal value) { return value.setScale(7, RoundingMode.HALF_UP); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
