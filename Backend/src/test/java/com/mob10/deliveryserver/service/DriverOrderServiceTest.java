package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.dto.OrderDtos.RejectOrderRequest;
import com.mob10.deliveryserver.dto.OrderDtos.UpdateStatusRequest;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.*;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverOrderServiceTest {
    @Mock DeliveryRequestRepository orders;
    @Mock UserRepository users;
    @Mock StatusHistoryRepository histories;
    @Mock RejectionReasonRepository reasons;
    @Mock OrderRejectionRepository rejections;
    @Mock DriverStatisticsRepository statistics;
    @Mock DtoMapper mapper;
    private DriverOrderService service;

    @BeforeEach
    void setUp() {
        service = new DriverOrderService(orders, users, histories, reasons, rejections, statistics, mapper, 3, 30);
    }

    @Test
    void onlyOneOfTwoDriversCanWinAtomicAccept() {
        User driverOne = availableDriver(11L);
        when(driverOne.getDriverAvailability()).thenReturn(DriverAvailability.AVAILABLE);
        DeliveryRequest assigned = mock(DeliveryRequest.class);
        when(assigned.getPackages()).thenReturn(new ArrayList<>());

        when(users.findByIdForUpdate(11L)).thenReturn(Optional.of(driverOne));
        when(statistics.findById(11L)).thenReturn(Optional.of(new DriverStatistics(driverOne)));
        when(rejections.existsByDeliveryRequestIdAndDriverId(99L, 11L)).thenReturn(false);
        when(orders.findByIdForUpdate(99L)).thenReturn(Optional.of(assigned));
        when(assigned.getStatus()).thenReturn(DeliveryStatus.CHO_TIEP_NHAN, DeliveryStatus.DA_CHAP_NHAN);

        assertDoesNotThrow(() -> service.accept(principal(11L), 99L));
        ApiException loser = assertThrows(ApiException.class, () -> service.accept(principal(12L), 99L));

        assertEquals("ORDER_ALREADY_TAKEN", loser.getCode());
        assertEquals(409, loser.getStatus().value());
        verify(histories, times(1)).save(any(StatusHistory.class));
    }

    @Test
    void rejectKeepsOrderOpenAndUpdatesReliability() {
        User driver = availableDriver(11L);
        DeliveryRequest order = mock(DeliveryRequest.class);
        RejectionReason reason = new RejectionReason("BUSY", "Đang bận", false, 10, false);
        OrderRejection rejection = new OrderRejection(order, driver, reason, null);
        DriverStatistics stats = new DriverStatistics(driver);

        when(users.findByIdForUpdate(11L)).thenReturn(Optional.of(driver));
        when(orders.findByIdForUpdate(99L)).thenReturn(Optional.of(order));
        when(order.getStatus()).thenReturn(DeliveryStatus.CHO_TIEP_NHAN);
        when(order.getDeliveryPerson()).thenReturn(null);
        when(reasons.findById("BUSY")).thenReturn(Optional.of(reason));
        when(rejections.save(any(OrderRejection.class))).thenReturn(rejection);
        when(rejections.countByDriverIdAndPenaltyAppliedTrueAndRejectedAtAfter(eq(11L), any(Instant.class))).thenReturn(1L);
        when(statistics.findById(11L)).thenReturn(Optional.of(stats));

        var result = service.reject(principal(11L), 99L, new RejectOrderRequest("BUSY", null));

        assertTrue(result.penaltyApplied());
        assertEquals(90, stats.getReliabilityScore().intValue());
        verify(order, never()).changeStatus(any());
        verify(histories, never()).save(any());
    }

    @Test
    void statusUpdateRequiresAssignedDriverAndWritesHistory() {
        User assignedDriver = availableDriver(11L);
        User otherDriver = availableDriver(12L);
        DeliveryRequest order = mock(DeliveryRequest.class);
        when(order.getDeliveryPerson()).thenReturn(assignedDriver);
        when(order.getStatus()).thenReturn(DeliveryStatus.DA_CHAP_NHAN);
        when(order.getPackages()).thenReturn(new ArrayList<>());
        when(users.findById(11L)).thenReturn(Optional.of(assignedDriver));
        when(users.findById(12L)).thenReturn(Optional.of(otherDriver));
        when(orders.findByIdForUpdate(99L)).thenReturn(Optional.of(order));

        ApiException forbidden = assertThrows(ApiException.class, () -> service.updateStatus(
                principal(12L), 99L, new UpdateStatusRequest(DeliveryStatus.DA_DEN_NHA_HANG, "Đã đến")));
        assertEquals("NOT_ASSIGNED_DRIVER", forbidden.getCode());

        assertDoesNotThrow(() -> service.updateStatus(
                principal(11L), 99L, new UpdateStatusRequest(DeliveryStatus.DA_DEN_NHA_HANG, "Đã đến")));
        verify(order).changeStatus(DeliveryStatus.DA_DEN_NHA_HANG);
        verify(histories).save(any(StatusHistory.class));
    }

    private User availableDriver(Long id) {
        User driver = mock(User.class);
        when(driver.getId()).thenReturn(id);
        lenient().when(driver.getRole()).thenReturn(Role.DELIVERY);
        return driver;
    }

    private AuthenticatedUser principal(Long id) {
        return new AuthenticatedUser(id, "shipper" + id, Role.DELIVERY);
    }
}
