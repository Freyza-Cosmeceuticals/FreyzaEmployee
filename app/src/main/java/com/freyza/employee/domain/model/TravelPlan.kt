package com.freyza.employee.domain.model

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase

data class TravelPlan(
    val id: String,
    val employeeId: String,
    val month: String,
    val createdById: String,

    val travelPlanEntries: List<TravelPlanEntry>,

    val createdAt: String,
    val updatedAt: String?,
)

data class TravelPlanEntry(
    val id: String,
    val tpId: String,
    val date: String,
    val dayType: DayType,
    val routeId: String?,

    val createdAt: String,
    val updatedAt: String?,
)

enum class DayType {
    WORK,
    HOLIDAY,
    LEAVE;

    fun titleCase(): String =
        this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)
}

fun dummyTravelPlan(): TravelPlan = TravelPlan(
    id = "2d201f46-0310-4a9a-a01f-4603108a9af5",
    employeeId = "a79ae89b-af0f-4f0f-9ae8-9baf0f4f0f59",
    month = "2026-01-01",
    createdById = "857e6915-f617-4e5f-be69-15f6175e5f9a",
    travelPlanEntries = listOf(
        TravelPlanEntry(
            id = "6f813189-94e1-49a6-8131-8994e169a656",
            tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
            date = "2026-01-01",
            dayType = DayType.WORK,
            routeId = "f138ed75-c8cf-4431-b8ed-75c8cf9431d2",
            createdAt = "2026-01-10T08:05:02.681Z",
            updatedAt = "2026-01-10T08:05:02.681Z"
        ), TravelPlanEntry(
            id = "8dc07977-bb02-4a9e-8079-77bb02ba9ebe",
            tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
            date = "2026-01-02",
            dayType = DayType.LEAVE,
            routeId = null,
            createdAt = "2026-01-10T08:05:02.681Z",
            updatedAt = "2026-01-10T08:05:02.681Z"
        ), TravelPlanEntry(
            id = "1ce70926-e09a-457e-a709-26e09a257e39",
            tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
            date = "2026-01-01",
            dayType = DayType.HOLIDAY,
            routeId = null,
            createdAt = "2026-01-10T08:05:02.681Z",
            updatedAt = "2026-01-10T08:05:02.681Z"
        )
    ),
    createdAt = "2026-01-10T08:05:02.681Z",
    updatedAt = "2026-01-10T08:05:02.681Z"
)
