package com.example.insightself.ui.components

/** Normalizes UI time (HH:mm or HH:mm:ss) to API format HH:mm:ss with seconds fixed to 00. */
fun normalizeBirthTimeForApi(hourMinute: String): String {
    val trimmed = hourMinute.trim()
    if (trimmed.isBlank()) return ""
    val parts = trimmed.split(":")
    return when {
        parts.size >= 3 -> "${parts[0]}:${parts[1]}:00"
        parts.size == 2 -> "${parts[0]}:${parts[1]}:00"
        else -> trimmed
    }
}

/** Strips seconds for picker display. */
fun birthTimeHourMinute(birthTime: String?): String {
    if (birthTime.isNullOrBlank()) return ""
    val parts = birthTime.split(":")
    return parts.take(2).joinToString(":")
}
