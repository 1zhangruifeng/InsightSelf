package com.example.insightself.data.model

data class ZodiacNatalDto(
    val engine: String? = null,
    val ephemerisMode: String? = null,
    val birthInstantUtc: String? = null,
    val julianDayUt: Double? = null,
    val sunSign: String? = null,
    val ascendantSign: String? = null,
    val ascendantLongitude: Double? = null,
    val mediumCoeliSign: String? = null,
    val mediumCoeliLongitude: Double? = null,
    val houseCusps: List<Double>? = null,
    val planets: Map<String, PlanetPositionDto>? = null
)

data class PlanetPositionDto(
    val name: String? = null,
    val longitude: Double? = null,
    val sign: String? = null,
    val degreeInSign: Double? = null,
    val retrograde: Boolean? = null,
    val house: Int? = null
)
