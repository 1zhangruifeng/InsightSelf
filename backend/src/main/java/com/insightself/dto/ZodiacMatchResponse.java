package com.insightself.dto;

public record ZodiacMatchResponse(
        String targetNickname,
        String targetZodiacSign,
        int zodiacScore,
        int personalityScore,
        int finalScore,
        String level,
        String communicationTips,
        String riskNotes,
        String collaborationMode
) {
}
