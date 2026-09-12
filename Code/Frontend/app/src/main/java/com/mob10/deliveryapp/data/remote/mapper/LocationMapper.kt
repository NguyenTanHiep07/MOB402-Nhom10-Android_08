package com.mob10.deliveryapp.data.remote.mapper

import com.mob10.deliveryapp.data.model.AddressSuggestion
import com.mob10.deliveryapp.data.model.RouteEstimate
import com.mob10.deliveryapp.data.remote.dto.AddressSuggestionResponseDto
import com.mob10.deliveryapp.data.remote.dto.RouteEstimateResponseDto

/**
 * Mapper: chuyển đổi Location/Route DTO → Domain Model.
 */
object LocationMapper {

    fun AddressSuggestionResponseDto.toDomain(): AddressSuggestion {
        require(formattedAddress.isNotBlank() && latitude.isFinite() && longitude.isFinite()
            && latitude in -90.0..90.0 && longitude in -180.0..180.0)
        return AddressSuggestion(
        placeId = placeId,
        formattedAddress = formattedAddress,
        primaryText = primaryText,
        secondaryText = secondaryText,
        ward = ward,
        district = district,
        province = province,
        country = country,
        latitude = latitude,
        longitude = longitude
        )
    }

    fun RouteEstimateResponseDto.toDomain(): RouteEstimate {
        require(distanceKm.isFinite() && distanceKm > 0 && estimatedDurationMinutes >= 0)
        require(listOf(baseFee, distanceFee, weightFee, serviceFee, totalFee).all { it.isFinite() && it >= 0 })
        require(kotlin.math.abs(totalFee - (baseFee + distanceFee + weightFee + serviceFee)) < 0.1)
        return RouteEstimate(
        distanceKm = distanceKm,
        estimatedDurationMinutes = estimatedDurationMinutes,
        baseFee = baseFee,
        distanceFee = distanceFee,
        weightFee = weightFee,
        serviceFee = serviceFee,
        totalFee = totalFee
        )
    }

    fun List<AddressSuggestionResponseDto>.toDomainSuggestionList(): List<AddressSuggestion> =
        map { it.toDomain() }
}
