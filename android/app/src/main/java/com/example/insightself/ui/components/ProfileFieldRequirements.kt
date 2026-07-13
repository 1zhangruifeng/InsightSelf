package com.example.insightself.ui.components

/** Which chart calculations need a profile field. */
enum class ProfileCalcRequirement {
    NONE,
    /** Required to save profile (nickname). */
    PROFILE,
    BAZI,
    ZODIAC,
    BAZI_AND_ZODIAC;

    val showRequiredMark: Boolean
        get() = this != NONE
}

object ProfileFieldRequirements {
    val nickname = ProfileCalcRequirement.PROFILE
    val birthDate = ProfileCalcRequirement.BAZI_AND_ZODIAC
    val birthTime = ProfileCalcRequirement.BAZI_AND_ZODIAC
    val birthplace = ProfileCalcRequirement.BAZI_AND_ZODIAC
    val calendarType = ProfileCalcRequirement.BAZI
}
