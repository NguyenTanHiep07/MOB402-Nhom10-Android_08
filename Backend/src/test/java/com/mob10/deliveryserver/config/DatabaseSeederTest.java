package com.mob10.deliveryserver.config;

import com.mob10.deliveryserver.domain.OrderRejection;
import com.mob10.deliveryserver.domain.RejectionReason;
import com.mob10.deliveryserver.domain.StatusHistory;
import com.mob10.deliveryserver.domain.User;
import com.mob10.deliveryserver.domain.DriverStatistics;
import com.mob10.deliveryserver.domain.DeliveryRequest;
import com.mob10.deliveryserver.domain.DriverAvailability;
import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DatabaseSeederTest {
    @Mock UserRepository users;
    @Mock DeliveryRequestRepository orders;
    @Mock StatusHistoryRepository histories;
    @Mock RejectionReasonRepository reasons;
    @Mock OrderRejectionRepository rejections;
    @Mock DriverStatisticsRepository statistics;
    @Mock PasswordEncoder passwordEncoder;
    private DatabaseSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DatabaseSeeder(users, orders, histories, reasons, rejections, statistics, passwordEncoder);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reasons.save(any(RejectionReason.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void emptyDatabaseGetsFifteenConsistentDemoOrders() throws Exception {
        when(orders.count()).thenReturn(0L);
        when(rejections.save(any(OrderRejection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statistics.save(any(DriverStatistics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        seeder.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<DeliveryRequest> orderCaptor = ArgumentCaptor.forClass(DeliveryRequest.class);
        verify(orders, times(15)).save(orderCaptor.capture());
        verify(histories, times(43)).save(any(StatusHistory.class));
        verify(rejections, times(5)).save(any(OrderRejection.class));

        assertTrue(orderCaptor.getAllValues().stream().allMatch(order ->
                order.getPickupLatitude() != null && order.getPickupLongitude() != null
                        && order.getDeliveryLatitude() != null && order.getDeliveryLongitude() != null));
        assertTrue(orderCaptor.getAllValues().stream().map(DeliveryRequest::getCreatedAt).distinct().count() >= 5);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(users, times(13)).save(userCaptor.capture());
        var drivers = userCaptor.getAllValues().stream().filter(user -> user.getRole() == Role.DELIVERY).toList();
        assertEquals(4, drivers.stream().filter(user -> user.getDriverAvailability() == DriverAvailability.BUSY).count());
        assertEquals(2, drivers.stream().filter(user -> user.getDriverAvailability() == DriverAvailability.AVAILABLE).count());
        assertEquals(1, drivers.stream().filter(user -> user.getDriverAvailability() == DriverAvailability.OFFLINE).count());

        ArgumentCaptor<DriverStatistics> statisticsCaptor = ArgumentCaptor.forClass(DriverStatistics.class);
        verify(statistics, times(7)).save(statisticsCaptor.capture());
        assertTrue(statisticsCaptor.getAllValues().stream().anyMatch(driverStats ->
                driverStats.getReliabilityScore().intValue() == 60 && driverStats.isLocked()));
    }

    @Test
    void existingOrdersAreNotDuplicated() throws Exception {
        when(orders.count()).thenReturn(15L);

        seeder.run(new DefaultApplicationArguments(new String[0]));

        verify(orders, never()).save(any());
        verifyNoInteractions(histories, rejections);
    }
}
