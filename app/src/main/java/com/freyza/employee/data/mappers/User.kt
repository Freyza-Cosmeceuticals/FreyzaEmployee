package com.freyza.employee.data.mappers

import com.freyza.employee.core.util.toLocalDate
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.model.User
import kotlin.time.Instant

fun UserDto.toDomain(): User {
  return User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    role = role,
    status = status,
    tier = tier,
    hqId = hqId,
    joiningDate = joiningDate.toLocalDate(),
    resignDate = resignDate?.toLocalDate(),
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) },
    userInfo = null
  )
}

fun User.toDto(): UserDto {
  return UserDto(
    id = id,
    name = name,
    email = email,
    phone = phone,
    role = role,
    status = status,
    tier = tier,
    hqId = hqId,
    joiningDate = joiningDate.toString(),
    resignDate = resignDate?.toString(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
  )
}
