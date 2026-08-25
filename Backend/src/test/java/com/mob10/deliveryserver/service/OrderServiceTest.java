package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.DeliveryRequest;
import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.domain.StatusHistory;
import com.mob10.deliveryserver.domain.User;
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
    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orders, users, histories, rejections, mapper);
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
}
