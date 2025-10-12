package com.freyza.employee.domain.model

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import io.github.jan.supabase.auth.user.UserInfo
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class User(
    val id: String,
    val name: String,
    val role: UserRole,
    val status: UserStatus,
    val location: String?,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val userInfo: UserInfo?
)

enum class UserRole {
    EMPLOYEE,
    ADMIN;

    fun titleCase(): String =
        this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)
}

enum class UserStatus {
    UNCONFIRMED,
    ACTIVE,
    REVOKED;

    fun titleCase(): String =
        this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)
}

@OptIn(ExperimentalTime::class)
fun dummyUser(): User = User(
    id = "e599b508-cf9c-417d-99b5-08cf9ca17d31",
    name = "Mario Mario",
    role = UserRole.EMPLOYEE,
    status = UserStatus.ACTIVE,
    location = "Patna",
    createdAt = Instant.parse("2025-10-10T18:15:03.410287+00"),
    updatedAt = null,
    userInfo = null
)
