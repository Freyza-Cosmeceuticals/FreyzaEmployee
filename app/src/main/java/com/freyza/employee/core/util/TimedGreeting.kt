package com.freyza.employee.core.util

import kotlinx.datetime.LocalDateTime

fun LocalDateTime.timedGreeting(
    suffix: String,
    morning: String = "Morning",
    afternoon: String = "Afternoon",
    evening: String = "Evening",
    night: String = "Work is over"
): String {
    if (this.hour in 5..<12) {
        return "${morning}, $suffix"
    }

    if (this.hour in 12..<17) {
        return "${afternoon}, $suffix"
    }

    if (this.hour in 17..<20) {
        return "${evening}, $suffix"
    }

    if (this.hour in 20..<5) {
        return "${night}, $suffix"
    }

    return "Hello, $suffix"

}
