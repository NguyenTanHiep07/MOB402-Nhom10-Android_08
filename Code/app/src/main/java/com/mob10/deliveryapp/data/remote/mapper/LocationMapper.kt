package com.mob10.deliveryapp.data.remote.mapper

import com.mob10.deliveryapp.data.model.AddressSuggestion
import com.mob10.deliveryapp.data.model.RouteEstimate
import com.mob10.deliveryapp.data.remote.dto.AddressSuggestionResponseDto
import com.mob10.deliveryapp.data.remote.dto.RouteEstimateResponseDto

/**
 * Mapper: chuyển đổi Location/Route DTO → Domain Model.
 */
object LocationMapper {

    fun AddressSuggestionResponseDto.toDomain(): AddressSuggestion = AddressSuggestion(
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

    fun RouteEstimateResponseDto.toDomain(): RouteEstimate = RouteEstimate(
        distanceKm = distanceKm,
        estimatedDurationMinutes = estimatedDurationMinutes,
        baseFee = baseFee,
        distanceFee = distanceFee,
        weightFee = weightFee,
        serviceFee = serviceFee,
        totalFee = totalFee
    )

    fun List<AddressSuggestionResponseDto>.toDomainSuggestionList(): List<AddressSuggestion> =
        map { it.toDomain() }
}
