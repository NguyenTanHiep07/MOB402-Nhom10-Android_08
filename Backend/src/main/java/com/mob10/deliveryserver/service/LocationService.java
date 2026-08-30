package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.dto.LocationDtos.AddressSuggestionResponse;
import com.mob10.deliveryserver.dto.LocationDtos.CoordinateInput;
import com.mob10.deliveryserver.dto.LocationDtos.RouteEstimateRequest;
import com.mob10.deliveryserver.dto.LocationDtos.RouteEstimateResponse;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.service.OpenStreetMapClient.RouteMetrics;
import com.mob10.deliveryserver.service.PricingService.PricingQuote;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LocationService {
    private static final BigDecimal VIETNAM_MIN_LAT = new BigDecimal("8.0");
    private static final BigDecimal VIETNAM_MAX_LAT = new BigDecimal("24.0");
    private static final BigDecimal VIETNAM_MIN_LON = new BigDecimal("102.0");
    private static final BigDecimal VIETNAM_MAX_LON = new BigDecimal("110.0");

    private final OpenStreetMapClient provider;
    private final PricingService pricingService;

    public LocationService(OpenStreetMapClient provider, PricingService pricingService) {
        this.provider = provider;
        this.pricingService = pricingService;
    }

    public List<AddressSuggestionResponse> autocomplete(String query, int requestedLimit) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.length() < 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ADDRESS_QUERY_TOO_SHORT",
                    "Vui lòng nhập ít nhất 3 ký tự để tìm địa chỉ");
        }
        int limit = Math.max(1, Math.min(requestedLimit, 8));
        return provider.search(normalized, limit);
    }

    public RouteEstimateResponse estimate(RouteEstimateRequest request) {
        RouteMetrics route = calculateRoute(request.pickup(), request.delivery());
        PricingQuote quote = pricingService.quote(route.distanceKm(), request.totalWeightKg(),
                request.fragile(), request.express());
        return new RouteEstimateResponse(route.distanceKm(), route.estimatedDurationMinutes(),
                quote.baseFee(), quote.distanceFee(), quote.weightFee(), quote.serviceFee(), quote.totalFee());
    }

    public RouteMetrics calculateRoute(CoordinateInput pickup, CoordinateInput delivery) {
        assertInVietnam(pickup, "pickup");
        assertInVietnam(delivery, "delivery");
        if (pickup.latitude().compareTo(delivery.latitude()) == 0
                && pickup.longitude().compareTo(delivery.longitude()) == 0) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ROUTE_POINTS_IDENTICAL",
                    "Điểm lấy hàng và điểm giao hàng phải khác nhau");
        }
        return provider.route(pickup, delivery);
    }

    private void assertInVietnam(CoordinateInput point, String field) {
        boolean valid = point != null && point.latitude() != null && point.longitude() != null
                && point.latitude().compareTo(VIETNAM_MIN_LAT) >= 0
                && point.latitude().compareTo(VIETNAM_MAX_LAT) <= 0
                && point.longitude().compareTo(VIETNAM_MIN_LON) >= 0
                && point.longitude().compareTo(VIETNAM_MAX_LON) <= 0;
        if (!valid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LOCATION_OUTSIDE_VIETNAM",
                    "Tọa độ " + field + " phải nằm trong phạm vi Việt Nam");
        }
    }
}
