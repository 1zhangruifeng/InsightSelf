package com.example.insightself.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class RegionRoot(
    val version: Int = 1,
    val provinces: List<RegionProvince> = emptyList()
)

data class RegionProvince(
    val name: String,
    val timezone: String = "Asia/Shanghai",
    val cities: List<RegionCity> = emptyList()
)

data class RegionCity(
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val districts: List<RegionDistrict> = emptyList()
)

data class RegionDistrict(
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

object RegionCatalog {
    private val gson = Gson()

    fun load(context: Context): RegionRoot {
        val text = context.assets.open("cn_regions.json").bufferedReader().use { it.readText() }
        return gson.fromJson(text, RegionRoot::class.java)
    }

    fun formatBirthPlace(province: String, city: String, district: String): String {
        return listOf(province, city, district).joinToString("|")
    }

    fun parseBirthPlace(birthPlace: String?): Triple<String, String, String>? {
        if (birthPlace.isNullOrBlank()) return null
        val parts = birthPlace.split("|", "/", "｜").map { it.trim() }.filter { it.isNotBlank() }
        if (parts.size < 3) return null
        return Triple(parts[0], parts[1], parts[2])
    }
}
