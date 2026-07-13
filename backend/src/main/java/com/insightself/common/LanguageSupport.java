package com.insightself.common;

import java.util.Locale;

public final class LanguageSupport {
    private LanguageSupport() {
    }

    public static boolean isChinese(String language) {
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("zh");
    }
}
