package com.insightself.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ZodiacMatchRequest(
        Long userId,
        String targetNickname,
        LocalDate targetBirthDate,
        LocalTime targetBirthTime,
        String targetBirthTimezone,
        Double targetLatitude,
        Double targetLongitude,
        String targetPersonalityTag
) {
}
