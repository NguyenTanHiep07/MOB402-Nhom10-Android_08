package com.mob10.deliveryapp.data.remote.mapper

import com.mob10.deliveryapp.data.model.AdminDriver
import com.mob10.deliveryapp.data.model.AdminUser
import com.mob10.deliveryapp.data.remote.dto.AdminDriverResponseDto
import com.mob10.deliveryapp.data.remote.dto.AdminUserResponseDto
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomain

/**
 * Mapper: chuyển đổi Admin DTO → Domain Model.
 */
object AdminMapper {

    fun AdminUserResponseDto.toDomain(): AdminUser = AdminUser(
        id = id,
        username = username,
        fullName = fullName,
        phoneNumber = phoneNumber,
        role = role,
        licensePlate = licensePlate,
        availability = availability,
        active = active,
        createdAt = createdAt
    )

    fun AdminDriverResponseDto.toDomain(): AdminDriver = AdminDriver(
        user = user.toDomain(),
        statistics = statistics?.toDomain()  // Dùng OrderMapper.toDomain() cho DriverStatistics
    )

    fun com.mob10.deliveryapp.data.remote.dto.AdminDriverRegistrationResponseDto.toDomain(): com.mob10.deliveryapp.data.model.AdminDriverRequest = com.mob10.deliveryapp.data.model.AdminDriverRequest(
        id = id,
        user = user?.toDomain(),
        licensePlate = licensePlate,
        status = status,
        createdAt = createdAt
    )

    fun List<AdminUserResponseDto>.toDomainUserList(): List<AdminUser> = map { it.toDomain() }
    fun List<AdminDriverResponseDto>.toDomainDriverList(): List<AdminDriver> = map { it.toDomain() }
    fun List<com.mob10.deliveryapp.data.remote.dto.AdminDriverRegistrationResponseDto>.toDomainRequestList(): List<com.mob10.deliveryapp.data.model.AdminDriverRequest> = map { it.toDomain() }
}
