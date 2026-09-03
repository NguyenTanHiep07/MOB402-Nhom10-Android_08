package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.DeliveryRequest;
import com.mob10.deliveryserver.domain.DeliveryStatus;
import com.mob10.deliveryserver.domain.Rating;
import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.domain.User;
import com.mob10.deliveryserver.dto.RatingDtos.CreateRatingRequest;
import com.mob10.deliveryserver.dto.RatingDtos.DriverRatingSummary;
import com.mob10.deliveryserver.dto.RatingDtos.RatingResponse;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.DeliveryRequestRepository;
import com.mob10.deliveryserver.repository.RatingRepository;
import com.mob10.deliveryserver.repository.UserRepository;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RatingService {
    private final RatingRepository ratings;
    private final DeliveryRequestRepository orders;
    private final UserRepository users;

    public RatingService(RatingRepository ratings, DeliveryRequestRepository orders, UserRepository users) {
        this.ratings = ratings;
        this.orders = orders;
        this.users = users;
    }

    @Transactional
    public RatingResponse create(AuthenticatedUser principal, CreateRatingRequest input) {
        requireClient(principal);
        DeliveryRequest order = orders.findByIdForUpdate(input.deliveryRequestId())
                .orElseThrow(() -> orderNotFound(input.deliveryRequestId()));

        if (!order.getClient().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_ORDER_OWNER",
                    "Bạn không phải chủ của đơn hàng này");
        }
        if (order.getStatus() != DeliveryStatus.DA_GIAO) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_NOT_DELIVERED",
                    "Chỉ có thể đánh giá sau khi đơn hàng đã giao thành công");
        }
        User driver = order.getDeliveryPerson();
        if (driver == null) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_HAS_NO_DRIVER",
                    "Đơn hàng chưa có tài xế để đánh giá");
        }
        if (ratings.existsByDeliveryRequestId(order.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "RATING_ALREADY_EXISTS",
                    "Đơn hàng này đã được đánh giá");
        }

        Rating rating = ratings.save(new Rating(
                order,
                order.getClient(),
                driver,
                input.stars(),
                clean(input.comment())));
        return toResponse(rating);
    }

    @Transactional(readOnly = true)
    public RatingResponse getByOrder(AuthenticatedUser principal, Long deliveryRequestId) {
        DeliveryRequest order = orders.findById(deliveryRequestId)
                .orElseThrow(() -> orderNotFound(deliveryRequestId));
        assertCanView(principal, order);
        Rating rating = ratings.findByDeliveryRequestId(deliveryRequestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RATING_NOT_FOUND",
                        "Đơn hàng chưa có đánh giá"));
        return toResponse(rating);
    }

    @Transactional(readOnly = true)
    public DriverRatingSummary getDriverSummary(Long driverId) {
        User driver = users.findById(driverId)
                .filter(user -> user.getRole() == Role.DELIVERY)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DRIVER_NOT_FOUND",
                        "Không tìm thấy tài xế"));
        long count = ratings.countByDriverId(driver.getId());
        Double average = ratings.findAverageStarsByDriverId(driver.getId());
        BigDecimal averageStars = average == null
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        return new DriverRatingSummary(driver.getId(), count, averageStars);
    }

    private void assertCanView(AuthenticatedUser principal, DeliveryRequest order) {
        boolean allowed = principal.role() == Role.ADMIN
                || (principal.role() == Role.CLIENT && order.getClient().getId().equals(principal.id()))
                || (principal.role() == Role.DELIVERY && order.getDeliveryPerson() != null
                    && order.getDeliveryPerson().getId().equals(principal.id()));
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "RATING_ACCESS_DENIED",
                    "Bạn không có quyền xem đánh giá của đơn hàng này");
        }
    }

    private void requireClient(AuthenticatedUser principal) {
        if (principal.role() != Role.CLIENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ROLE_NOT_ALLOWED",
                    "Chức năng đánh giá chỉ dành cho khách hàng");
        }
    }

    private RatingResponse toResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getDeliveryRequest().getId(),
                rating.getClient().getId(),
                rating.getDriver().getId(),
                rating.getStars(),
                rating.getComment(),
                rating.getCreatedAt());
    }

    private ApiException orderNotFound(Long id) {
        return new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng " + id);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
