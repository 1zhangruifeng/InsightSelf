package com.insightself.dto;

import com.insightself.domain.AssessmentResult;
import com.insightself.domain.BaziResult;
import com.insightself.domain.UserProfile;
import com.insightself.domain.ZodiacResult;

import java.util.List;

public record DashboardResponse(
        UserProfile profile,
        BaziResult bazi,
        ZodiacResult zodiacDaily,
        List<AssessmentResult> assessmentResults,
        AiReportResponse latestAiReport,
        String integratedSummary,
        String safetyNotice
) {
}
