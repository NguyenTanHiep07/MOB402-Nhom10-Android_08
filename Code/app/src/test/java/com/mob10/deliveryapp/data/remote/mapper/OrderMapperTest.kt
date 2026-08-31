package com.mob10.deliveryapp.data.remote.mapper

import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.remote.dto.*
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomain
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainList
import org.junit.Assert.*
import org.junit.Test

class OrderMapperTest {

    @Test
    fun `OrderResponseDto toDomain maps all fields correctly`() {
        val dto = OrderResponseDto(
            id = 100L,
            client = PersonResponseDto(id = 1L, fullName = "Nguyễn Văn A", phoneNumber = "0901234567", licensePlate = null),
            deliveryPerson = PersonResponseDto(id = 2L, fullName = "Trần Văn B", phoneNumber = "0987654321", licensePlate = "59-X1 12345"),
            pickupAddress = "123 Quận 1",
            deliveryAddress = "456 Quận 3",
            pickupLatitude = 10.776889,
            pickupLongitude = 106.700806,
            deliveryLatitude = 10.782681,
            deliveryLongitude = 106.695754,
            senderName = "Shop A",
            senderPhone = "0900000001",
            recipientName = "Khách B",
            recipientPhone = "0900000002",
            distanceKm = 4.2,
            baseFee = 15000.0,
            distanceFee = 21000.0,
            weightFee = 3000.0,
            fragileCharge = 5000.0,
            totalCost = 44000.0,
            status = "DA_CHAP_NHAN",
            scheduledPickupTime = null,
            actualDeliveryTime = null,
            note = "Giao hàng giờ hành chính",
            createdAt = "2026-08-31T08:00:00Z",
            updatedAt = "2026-08-31T08:05:00Z",
            packages = listOf(
                PackageResponseDto(
                    id = 10L,
                    name = "Cơm gà",
                    packageType = "FOOD",
                    weightKg = 0.8,
                    quantity = 2,
                    notes = "Ít ớt",
                    fragile = false,
                    express = true
                )
            )
        )

        val domain = dto.toDomain()

        assertEquals(100L, domain.id)
        assertNotNull(domain.client)
        assertEquals("Nguyễn Văn A", domain.client?.fullName)
        assertNotNull(domain.deliveryPerson)
        assertEquals("59-X1 12345", domain.deliveryPerson?.licensePlate)
        assertEquals("123 Quận 1", domain.pickupAddress)
        assertEquals("456 Quận 3", domain.deliveryAddress)
        assertEquals(10.776889, domain.pickupLatitude!!, 0.0001)
        assertEquals(4.2, domain.distanceKm, 0.01)
        assertEquals(44000.0, domain.totalCost, 0.01)
        assertEquals(DeliveryStatus.DA_CHAP_NHAN, domain.status)
        assertEquals(1, domain.packages.size)

        val pkg = domain.packages.first()
        assertEquals("Cơm gà", pkg.name)
        assertEquals(0.8, pkg.weightKg, 0.01)
        assertTrue(pkg.isExpress)
        assertFalse(pkg.isFragile)
    }

    @Test
    fun `OrderResponseDto with invalid status fallback to CHO_TIEP_NHAN`() {
        val dto = OrderResponseDto(
            id = 1L,
            client = null,
            deliveryPerson = null,
            pickupAddress = "A",
            deliveryAddress = "B",
            pickupLatitude = null,
            pickupLongitude = null,
            deliveryLatitude = null,
            deliveryLongitude = null,
            senderName = "S",
            senderPhone = "01",
            recipientName = "R",
            recipientPhone = "02",
            distanceKm = null,
            baseFee = null,
            distanceFee = null,
            weightFee = null,
            fragileCharge = null,
            totalCost = null,
            status = "UNKNOWN_STATUS_STRING",
            scheduledPickupTime = null,
            actualDeliveryTime = null,
            note = null,
            createdAt = null,
            updatedAt = null,
            packages = null
        )

        val domain = dto.toDomain()
        assertEquals(DeliveryStatus.CHO_TIEP_NHAN, domain.status)
        assertEquals(0.0, domain.totalCost, 0.001)
        assertEquals(0.0, domain.distanceKm, 0.001)
        assertTrue(domain.packages.isEmpty())
    }

    @Test
    fun `DriverStatisticsResponseDto toDomain maps correctly`() {
        val dto = DriverStatisticsResponseDto(
            driverId = 5L,
            totalAccepted = 20,
            totalRejected = 2,
            penalizedRejections = 1,
            reliabilityScore = 95.0,
            lockedUntil = null,
            locked = false,
            warning = false
        )

        val domain = dto.toDomain()
        assertEquals(5L, domain.driverId)
        assertEquals(20, domain.totalAccepted)
        assertEquals(2, domain.totalRejected)
        assertEquals(95.0, domain.reliabilityScore, 0.01)
        assertFalse(domain.isLocked)
        assertFalse(domain.isWarning)
    }

    @Test
    fun `toDomainList maps empty and non-empty lists`() {
        val emptyList = emptyList<OrderResponseDto>()
        assertTrue(emptyList.toDomainList().isEmpty())
    }
}
