package com.insightself.common;

public final class WellnessSafetyNotice {
    private WellnessSafetyNotice() {
    }

    public static String english() {
        return "InsightSelf is for self-reflection and wellness education only. "
                + "It is not medical, psychological diagnosis, legal, financial, or fate-prediction advice.";
    }

    public static String chinese() {
        return "InsightSelf 仅用于自我反思和身心健康教育，不构成医疗、心理诊断、法律、财务或命运预测建议。";
    }

    public static String forLanguage(String language) {
        return LanguageSupport.isChinese(language) ? chinese() : english();
    }
}
