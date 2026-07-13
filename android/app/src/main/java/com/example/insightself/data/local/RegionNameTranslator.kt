package com.example.insightself.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

private data class RegionLabelsEn(
    val version: Int = 1,
    val labels: Map<String, String> = emptyMap()
)

object RegionNameTranslator {
    private var labelsEn: Map<String, String>? = null

    fun load(context: Context) {
        if (labelsEn != null) return
        val text = context.assets.open("region_labels_en.json").bufferedReader().use { it.readText() }
        labelsEn = Gson().fromJson(text, RegionLabelsEn::class.java).labels
    }

    fun displayName(context: Context, zhName: String, isChinese: Boolean): String {
        if (zhName.isBlank()) return zhName
        if (isChinese) return zhName
        load(context)
        return labelsEn?.get(zhName) ?: zhName
    }
}
