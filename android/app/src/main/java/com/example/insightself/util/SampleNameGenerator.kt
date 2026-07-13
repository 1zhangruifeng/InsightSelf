package com.example.insightself.util

import kotlin.random.Random

/**
 * Demo placeholders only — never use real personal names (e.g. xiaotong, zhangruifeng).
 */
object SampleNameGenerator {
    private val englishNames = listOf(
        "Avery", "Blake", "Casey", "Drew", "Ellis", "Finley", "Gray", "Harper",
        "Jordan", "Kai", "Logan", "Morgan", "Noel", "Parker", "Quinn", "Reese",
        "River", "Sage", "Taylor", "Winter"
    )

    private val blockedPlaceholders = setOf(
        "xiaotong", "zhangruifeng", "张瑞峰", "小童", "晓童"
    )

    fun randomEnglishName(): String = englishNames[Random.nextInt(englishNames.size)]

    fun nicknamePlaceholder(isChinese: Boolean): String {
        val name = randomEnglishName()
        require(name.lowercase() !in blockedPlaceholders) { "placeholder must not be a real name" }
        return if (isChinese) "例如：$name" else "e.g. $name"
    }

    fun usernamePlaceholder(): String = "user_${Random.nextInt(1000, 9999)}"
}
