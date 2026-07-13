package com.example.insightself.ui.navigation

object Routes {
    const val Startup = "startup"
    const val Login = "login"
    const val Register = "register"
    const val OnboardingProfile = "onboarding_profile"
    const val Main = "main"
    const val Home = "home"
    const val Bazi = "bazi"
    const val Zodiac = "zodiac"
    const val Match = "match"
    const val Assessments = "assessments"
    const val AssessmentQuestions = "assessment_questions/{type}"
    const val AssessmentResult = "assessment_result/{resultId}"
    const val Report = "report"
    const val Profile = "profile"

    fun assessmentQuestions(type: String): String = "assessment_questions/$type"
    fun assessmentResult(resultId: Long): String = "assessment_result/$resultId"
}
