package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.DeliveryRequest;
import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.domain.StatusHistory;
import com.mob10.deliveryserver.domain.User;
import com.mob10.deliveryserver.dto.OrderDtos.CreateOrderRequest;
import com.mob10.deliveryserver.dto.OrderDtos.PackageInput;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.DeliveryRequestRepository;
import com.mob10.deliveryserver.repository.OrderRejectionRepository;
import com.mob10.deliveryserver.repository.StatusHistoryRepository;
import com.mob10.deliveryserver.repository.UserRepository;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock DeliveryRequestRepository orders;
    @Mock UserRepository users;
    @Mock StatusHistoryRepository histories;
    @Mock OrderRejectionRepository rejections;
    @Mock DtoMapper mapper;
    @Mock LocationService locationService;
    @Mock PricingService pricingService;
    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orders, users, histories, rejections, mapper, locationService, pricingService);
    }

    @Test
    void ownerCanReadHistoryInRepositoryOrder() {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(21L);
        DeliveryRequest order = mock(DeliveryRequest.class);
        when(order.getClient()).thenReturn(owner);
        StatusHistory first = mock(StatusHistory.class);
        StatusHistory second = mock(StatusHistory.class);
        when(orders.findById(99L)).thenReturn(Optional.of(order));
        when(histories.findAllByDeliveryRequestIdOrderByTimestampAscIdAsc(99L))
                .thenReturn(List.of(first, second));

        var result = service.history(new AuthenticatedUser(21L, "client1", Role.CLIENT), 99L);

        assertEquals(2, result.size());
        verify(mapper).toHistoryResponse(first);
        verify(mapper).toHistoryResponse(second);
    }

    @Test
    void anotherClientCannotReadHistory() {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(21L);
        DeliveryRequest order = mock(DeliveryRequest.class);
        when(order.getClient()).thenReturn(owner);
        when(orders.findById(99L)).thenReturn(Optional.of(order));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.history(new AuthenticatedUser(22L, "client2", Role.CLIENT), 99L));

        assertEquals("ORDER_ACCESS_DENIED", exception.getCode());
        verifyNoInteractions(histories);
    }

    @Test
    void createUsesServerRouteDistanceInsteadOfClientDistance() {
        User client = mock(User.class);
        when(users.findById(21L)).thenReturn(Optional.of(client));
        when(locationService.calculateRoute(any(), any()))
                .thenReturn(new OpenStreetMapClient.RouteMetrics(new BigDecimal("12.40"), 31));
        when(pricingService.quote(new BigDecimal("12.40"), new BigDecimal("1.00"), false, false))
                .thenReturn(new PricingService.PricingQuote(
                        new BigDecimal("15000.00"), new BigDecimal("62000.00"),
                        new BigDecimal("3000.00"), new BigDecimal("0.00"), new BigDecimal("80000.00")));

        CreateOrderRequest input = new CreateOrderRequest(
                "1 Nguyễn Trãi, TP.HCM", "2 Điện Biên Phủ, TP.HCM",
                new BigDecimal("10.7700000"), new BigDecimal("106.6800000"),
                new BigDecimal("10.8000000"), new BigDecimal("106.7100000"),
                "Người gửi", "0901234567", "Người nhận", "0987654321",
                new BigDecimal("0.10"),
                List.of(new PackageInput("Kiện hàng", "STANDARD", new BigDecimal("1.00"),
                        1, null, false, false)), null, null);

        service.create(new AuthenticatedUser(21L, "client1", Role.CLIENT), input);

        var captor = org.mockito.ArgumentCaptor.forClass(DeliveryRequest.class);
        verify(orders).save(captor.capture());
        DeliveryRequest saved = captor.getValue();
        assertEquals(new BigDecimal("12.40"), saved.getDistanceKm());
        assertEquals(new BigDecimal("62000.00"), saved.getDistanceFee());
        verify(locationService).calculateRoute(any(), any());
    }
}
