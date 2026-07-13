package com.insightself.dto;

public record DemoSeedResponse(
        UserResponse user,
        String demoNote,
        String seededProfile,
        String seededAssessments,
        String seededReport
) {
}
