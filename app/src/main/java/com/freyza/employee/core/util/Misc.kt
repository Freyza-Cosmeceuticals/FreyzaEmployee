package com.freyza.employee.core.util

fun String.toTitleCase() = this.split(' ')
    .joinToString(" ") { word ->
        word.lowercase()
            .replaceFirstChar { it.titlecase() }
    }
