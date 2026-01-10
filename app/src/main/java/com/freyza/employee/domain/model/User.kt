package com.freyza.employee.domain.model

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import io.github.jan.supabase.auth.user.UserInfo

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,

    val role: UserRole,
    val status: UserStatus,

    val tier: EmployeeTier?,
    val hqId: String?,

    val joiningDate: String,
    val resignDate: String?,

    val createdAt: String,
    val updatedAt: String?,

    val userInfo: UserInfo?
)

enum class UserRole {
    EMPLOYEE,
    ADMIN;

    fun titleCase(): String =
        this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)
}

enum class UserStatus {
    ACTIVE,
    REVOKED;

    fun titleCase(): String =
        this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)
}

enum class EmployeeTier(val fullForm: String) {
    FSO("Field Sales Officer"),
    TABM("Training Area Business Manager"),
    ASM("Area Sales Manager");
}

fun dummyUser(): User = User(
    id = "e599b508-cf9c-417d-99b5-08cf9ca17d31",
    name = "Mario Mario",
    email = "employee.sample@freyza.com",
    phone = "9876543210",
    role = UserRole.EMPLOYEE,
    status = UserStatus.ACTIVE,
    tier = EmployeeTier.FSO,
    hqId = "fca17731-c0af-4f2e-a177-31c0af0f2ea3",
    joiningDate = "2025-10-10T18:15:03.410287+00",
    resignDate = null,
    createdAt = "2025-10-10T18:15:03.410287+00",
    updatedAt = null,
    userInfo = null,
)
