package com.insightself.service;

import com.insightself.domain.AssessmentResult;
import com.insightself.domain.BaziResult;
import com.insightself.domain.UserProfile;
import com.insightself.domain.ZodiacResult;
import com.insightself.dto.DashboardResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiInsightContextService {
    private final DashboardService dashboardService;

    public AiInsightContextService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public InsightContextSnapshot snapshot(Long userId) {
        DashboardResponse dashboard = dashboardService.load(userId);
        return new InsightContextSnapshot(
                profileContext(dashboard.profile()),
                baziContext(dashboard.bazi()),
                astrologyContext(dashboard.zodiacDaily()),
                assessmentContexts(dashboard.assessmentResults()),
                dashboard.integratedSummary()
        );
    }

    private ProfileContext profileContext(UserProfile profile) {
        return new ProfileContext(
                profile.getNickname(),
                profile.getLanguage(),
                profile.getPreference()
        );
    }

    private BaziContext baziContext(BaziResult bazi) {
        Map<String, String> pillars = new LinkedHashMap<>();
        pillars.put("year", bazi.getYearPillar());
        pillars.put("month", bazi.getMonthPillar());
        pillars.put("day", bazi.getDayPillar());
        pillars.put("hour", bazi.getHourPillar());

        Map<String, Integer> elementScores = bazi.getElementScores();
        return new BaziContext(
                bazi.getCalculationMethod(),
                pillars,
                elementScores,
                strongestElement(elementScores),
                weakestElement(elementScores),
                bazi.getConclusion(),
                bazi.getSuggestion()
        );
    }

    private AstrologyContext astrologyContext(ZodiacResult zodiacDaily) {
        return new AstrologyContext(
                zodiacDaily.getCalculationMethod(),
                zodiacDaily.getZodiacSign(),
                zodiacDaily.getInsightDate(),
                Map.of(
                        "emotion", zodiacDaily.getEmotionScore(),
                        "communication", zodiacDaily.getCommunicationScore(),
                        "action", zodiacDaily.getActionScore()
                ),
                zodiacDaily.getSuggestion()
        );
    }

    private List<AssessmentContext> assessmentContexts(List<AssessmentResult> assessmentResults) {
        Map<String, AssessmentContext> latestByType = new LinkedHashMap<>();
        for (AssessmentResult result : assessmentResults) {
            latestByType.putIfAbsent(result.getType(), new AssessmentContext(
                    result.getType(),
                    result.getInstrumentVersion(),
                    result.getResultLabel(),
                    result.getScores(),
                    result.getSummary()
            ));
        }
        return List.copyOf(latestByType.values());
    }

    private String strongestElement(Map<String, Integer> elementScores) {
        return elementScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();
    }

    private String weakestElement(Map<String, Integer> elementScores) {
        return elementScores.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();
    }

    public record InsightContextSnapshot(
            ProfileContext profile,
            BaziContext bazi,
            AstrologyContext astrology,
            List<AssessmentContext> assessments,
            String integratedSummary
    ) {
    }

    public record ProfileContext(
            String nickname,
            String language,
            String preference
    ) {
    }

    public record BaziContext(
            String method,
            Map<String, String> pillars,
            Map<String, Integer> elementScores,
            String strongestElement,
            String weakestElement,
            String conclusion,
            String suggestion
    ) {
    }

    public record AstrologyContext(
            String method,
            String sunSign,
            LocalDate insightDate,
            Map<String, Integer> dailyScores,
            String suggestion
    ) {
    }

    public record AssessmentContext(
            String type,
            String instrumentVersion,
            String resultLabel,
            Map<String, Double> scores,
            String summary
    ) {
    }
}
