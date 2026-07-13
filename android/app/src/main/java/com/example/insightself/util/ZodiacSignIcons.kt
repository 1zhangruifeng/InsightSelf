package com.example.insightself.util

import androidx.annotation.DrawableRes
import com.example.insightself.R

object ZodiacSignIcons {
  @DrawableRes
  fun drawableRes(sign: String?): Int {
    return when (ZodiacSignUtils.signKey(sign)) {
      "Aries" -> R.drawable.ic_zodiac_aries
      "Taurus" -> R.drawable.ic_zodiac_taurus
      "Gemini" -> R.drawable.ic_zodiac_gemini
      "Cancer" -> R.drawable.ic_zodiac_cancer
      "Leo" -> R.drawable.ic_zodiac_leo
      "Virgo" -> R.drawable.ic_zodiac_virgo
      "Libra" -> R.drawable.ic_zodiac_libra
      "Scorpio" -> R.drawable.ic_zodiac_scorpio
      "Sagittarius" -> R.drawable.ic_zodiac_sagittarius
      "Capricorn" -> R.drawable.ic_zodiac_capricorn
      "Aquarius" -> R.drawable.ic_zodiac_aquarius
      "Pisces" -> R.drawable.ic_zodiac_pisces
      else -> R.drawable.ic_zodiac_generic
    }
  }

  /** Unicode zodiac glyph fallback (e.g. ♈) when used as decorative text. */
  fun symbol(sign: String?): String {
    return when (ZodiacSignUtils.signKey(sign)) {
      "Aries" -> "♈"
      "Taurus" -> "♉"
      "Gemini" -> "♊"
      "Cancer" -> "♋"
      "Leo" -> "♌"
      "Virgo" -> "♍"
      "Libra" -> "♎"
      "Scorpio" -> "♏"
      "Sagittarius" -> "♐"
      "Capricorn" -> "♑"
      "Aquarius" -> "♒"
      "Pisces" -> "♓"
      else -> "✦"
    }
  }
}
