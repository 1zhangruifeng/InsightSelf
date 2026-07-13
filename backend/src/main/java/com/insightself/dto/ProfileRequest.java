package com.insightself.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ProfileRequest(
        String nickname,
        String gender,
        LocalDate birthDate,
        LocalTime birthTime,
        String birthPlace,
        String birthTimezone,
        Double latitude,
        Double longitude,
        String calendarType,
        String preference,
        boolean aiEnabled,
        String language
) {
}
