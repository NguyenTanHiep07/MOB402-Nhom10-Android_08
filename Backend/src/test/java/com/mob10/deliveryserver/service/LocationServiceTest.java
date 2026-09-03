package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.dto.LocationDtos.CoordinateInput;
import com.mob10.deliveryserver.dto.LocationDtos.RouteEstimateRequest;
import com.mob10.deliveryserver.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
    @Mock OpenStreetMapClient provider;
    @Mock PricingService pricingService;
    private LocationService service;

    @BeforeEach
    void setUp() {
        service = new LocationService(provider, pricingService);
    }

    @Test
    void autocompleteRequiresAtLeastThreeCharacters() {
        ApiException error = assertThrows(ApiException.class, () -> service.autocomplete("ab", 6));

        assertEquals("ADDRESS_QUERY_TOO_SHORT", error.getCode());
        verifyNoInteractions(provider);
    }

    @Test
    void autocompleteCapsResultLimitToProtectProvider() {
        when(provider.search("Nguyễn Trãi", 8)).thenReturn(List.of());

        service.autocomplete("  Nguyễn Trãi  ", 100);

        verify(provider).search("Nguyễn Trãi", 8);
    }

    @Test
    void estimateUsesRoadDistanceForPricing() {
        CoordinateInput pickup = new CoordinateInput(new BigDecimal("10.7700"), new BigDecimal("106.6800"));
        CoordinateInput delivery = new CoordinateInput(new BigDecimal("10.8000"), new BigDecimal("106.7100"));
        when(provider.route(pickup, delivery))
                .thenReturn(new OpenStreetMapClient.RouteMetrics(new BigDecimal("8.40"), 24));
        when(pricingService.quote(new BigDecimal("8.40"), new BigDecimal("2.50"), true, false))
                .thenReturn(new PricingService.PricingQuote(
                        new BigDecimal("15000.00"), new BigDecimal("42000.00"),
                        new BigDecimal("7500.00"), new BigDecimal("5000.00"), new BigDecimal("69500.00")));

        var result = service.estimate(new RouteEstimateRequest(pickup, delivery,
                new BigDecimal("2.50"), true, false));

        assertEquals(new BigDecimal("8.40"), result.distanceKm());
        assertEquals(24, result.estimatedDurationMinutes());
        assertEquals(new BigDecimal("69500.00"), result.totalFee());
    }

    @Test
    void estimateRejectsCoordinatesOutsideVietnam() {
        CoordinateInput pickup = new CoordinateInput(new BigDecimal("40.7128"), new BigDecimal("-74.0060"));
        CoordinateInput delivery = new CoordinateInput(new BigDecimal("10.8000"), new BigDecimal("106.7100"));

        ApiException error = assertThrows(ApiException.class,
                () -> service.calculateRoute(pickup, delivery));

        assertEquals("LOCATION_OUTSIDE_VIETNAM", error.getCode());
        verifyNoInteractions(provider);
    }
}
