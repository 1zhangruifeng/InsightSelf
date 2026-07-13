package com.insightself.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightself.common.ApiException;
import com.insightself.common.LanguageSupport;
import com.insightself.domain.AssessmentResult;
import com.insightself.domain.UserProfile;
import com.insightself.domain.ZodiacResult;
import com.insightself.dto.ZodiacMatchRequest;
import com.insightself.dto.ZodiacMatchResponse;
import com.insightself.repository.AssessmentResultRepository;
import com.insightself.repository.ZodiacResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ZodiacService {
    private static final String METHOD = "Swiss Ephemeris Java port, bundled sepl_18/semo_18 data, Placidus houses, transit-aspect-v1";
    private static final Pattern MBTI_PATTERN = Pattern.compile("\\b[EI][NS][TF][JP]\\b", Pattern.CASE_INSENSITIVE);

    private static final List<AspectSpec> ASPECTS = List.of(
            new AspectSpec("Conjunction", 0, 8, 12),
            new AspectSpec("Sextile", 60, 5, 16),
            new AspectSpec("Square", 90, 6, -18),
            new AspectSpec("Trine", 120, 6, 20),
            new AspectSpec("Opposition", 180, 8, -14)
    );

    private static final List<ScorePair> EMOTION_PAIRS = List.of(
            new ScorePair("Moon", "Moon", 1.2),
            new ScorePair("Venus", "Moon", 0.7),
            new ScorePair("Sun", "Moon", 0.5)
    );
    private static final List<ScorePair> COMMUNICATION_PAIRS = List.of(
            new ScorePair("Mercury", "Mercury", 1.2),
            new ScorePair("Moon", "Mercury", 0.5),
            new ScorePair("Jupiter", "Mercury", 0.5)
    );
    private static final List<ScorePair> ACTION_PAIRS = List.of(
            new ScorePair("Mars", "Mars", 1.1),
            new ScorePair("Sun", "Mars", 0.6),
            new ScorePair("Jupiter", "Mars", 0.5)
    );

    private static final Map<String, String> ELEMENTS = Map.ofEntries(
            Map.entry("Aries", "Fire"), Map.entry("Leo", "Fire"), Map.entry("Sagittarius", "Fire"),
            Map.entry("Taurus", "Earth"), Map.entry("Virgo", "Earth"), Map.entry("Capricorn", "Earth"),
            Map.entry("Gemini", "Air"), Map.entry("Libra", "Air"), Map.entry("Aquarius", "Air"),
            Map.entry("Cancer", "Water"), Map.entry("Scorpio", "Water"), Map.entry("Pisces", "Water")
    );

    private static final Map<String, String> SIGN_CN = Map.ofEntries(
            Map.entry("Aries", "白羊座"), Map.entry("Taurus", "金牛座"), Map.entry("Gemini", "双子座"),
            Map.entry("Cancer", "巨蟹座"), Map.entry("Leo", "狮子座"), Map.entry("Virgo", "处女座"),
            Map.entry("Libra", "天秤座"), Map.entry("Scorpio", "天蝎座"), Map.entry("Sagittarius", "射手座"),
            Map.entry("Capricorn", "摩羯座"), Map.entry("Aquarius", "水瓶座"), Map.entry("Pisces", "双鱼座")
    );

    private static final Map<String, String> LEVEL_CN = Map.of(
            "Excellent", "极佳", "Good", "良好", "Moderate", "中等", "Needs care", "需谨慎"
    );

    private final ZodiacResultRepository zodiacRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final ProfileService profileService;
    private final AstrologyChartCalculator chartCalculator;
    private final ObjectMapper objectMapper;

    public ZodiacService(ZodiacResultRepository zodiacRepository,
                         AssessmentResultRepository assessmentResultRepository,
                         ProfileService profileService,
                         AstrologyChartCalculator chartCalculator,
                         ObjectMapper objectMapper) {
        this.zodiacRepository = zodiacRepository;
        this.assessmentResultRepository = assessmentResultRepository;
        this.profileService = profileService;
        this.chartCalculator = chartCalculator;
        this.objectMapper = objectMapper;
    }

    public ZodiacResult daily(Long userId) {
        UserProfile profile = profileService.get(userId);
        LocalDate today = LocalDate.now(ZoneId.of(profile.getBirthTimezone()));
        return daily(userId, today);
    }

    public ZodiacResult daily(Long userId, LocalDate date) {
        if (date == null) {
            return daily(userId);
        }
        UserProfile profile = profileService.get(userId);
        ZoneId zoneId = ZoneId.of(profile.getBirthTimezone());
        LocalDate today = LocalDate.now(zoneId);
        if (date.isAfter(today)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "date cannot be in the future");
        }
        AstrologyChartCalculator.NatalChart natalChart = chartCalculator.natal(profile);
        return zodiacRepository.findByUserIdAndInsightDate(userId, date)
                .orElseGet(() -> generateDaily(profile, date, natalChart));
    }

    public AstrologyChartCalculator.NatalChart natal(Long userId) {
        return chartCalculator.natal(profileService.get(userId));
    }

    public ZodiacMatchResponse match(ZodiacMatchRequest request) {
        if (request == null || request.userId() == null || request.targetBirthDate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId and targetBirthDate are required");
        }
        UserProfile profile = profileService.get(request.userId());
        boolean isChinese = LanguageSupport.isChinese(profile.getLanguage());
        AstrologyChartCalculator.NatalChart userChart = chartCalculator.natal(profile);
        TargetChart targetChart = targetChart(request);

        int zodiacScore = zodiacScore(userChart, targetChart);
        int personalityScore = personalityScore(request.userId(), request.targetPersonalityTag());
        int finalScore = clamp(Math.round(zodiacScore * 0.7f + personalityScore * 0.3f), 0, 100);
        String nickname = request.targetNickname() == null || request.targetNickname().isBlank()
                ? (isChinese ? "对方" : "Target")
                : request.targetNickname().trim();
        String level = finalScore >= 82 ? "Excellent" : finalScore >= 68 ? "Good" : finalScore >= 52 ? "Moderate" : "Needs care";

        if (isChinese) {
            return new ZodiacMatchResponse(
                    nickname,
                    localizeSign(targetChart.sunSign(), true),
                    zodiacScore,
                    personalityScore,
                    finalScore,
                    LEVEL_CN.getOrDefault(level, level),
                    targetChart.precise()
                            ? "基于双方本命盘的主要个人行星相位与太阳星座元素关系计算。"
                            : "由于只提供生日，匹配只使用对方生日中午的太阳黄经与星座元素关系计算。",
                    "MBTI 仅作为流行的偏好语言保留，用于帮助对话，不作为人格诊断或关系预测。",
                    finalScore >= 72 ? "优先讨论共同节奏，再处理差异" : "先建立边界和沟通规则，再推进亲密或协作"
            );
        }
        return new ZodiacMatchResponse(
                nickname,
                targetChart.sunSign(),
                zodiacScore,
                personalityScore,
                finalScore,
                level,
                targetChart.precise()
                        ? "Calculated from major personal-planet synastry aspects and Sun-sign element affinity."
                        : "Only the target birthday was provided, so the match uses the target noon Sun longitude and sign element affinity.",
                "MBTI is retained as a popular preference language for conversation, not as a diagnostic label or relationship prediction.",
                finalScore >= 72 ? "Start with shared rhythm, then discuss differences" : "Set boundaries and communication rules before deepening the connection"
        );
    }

    public String sunSign(LocalDate date) {
        return chartCalculator.dateOnlySun(date).sign();
    }

    private ZodiacResult generateDaily(UserProfile profile, LocalDate date, AstrologyChartCalculator.NatalChart natalChart) {
        boolean isChinese = LanguageSupport.isChinese(profile.getLanguage());
        Instant transitInstant = date.atTime(LocalTime.NOON).atZone(ZoneId.of(profile.getBirthTimezone())).toInstant();
        AstrologyChartCalculator.TransitChart transitChart = chartCalculator.transitAt(transitInstant);

        ScoreComputation emotion = score(natalChart.planets(), transitChart.planets(), EMOTION_PAIRS);
        ScoreComputation communication = score(natalChart.planets(), transitChart.planets(), COMMUNICATION_PAIRS);
        ScoreComputation action = score(natalChart.planets(), transitChart.planets(), ACTION_PAIRS);

        ZodiacResult result = new ZodiacResult();
        result.setUserId(profile.getUserId());
        result.setZodiacSign(localizeSign(natalChart.sunSign(), isChinese));
        result.setInsightDate(date);
        result.setEmotionScore(emotion.score());
        result.setCommunicationScore(communication.score());
        result.setActionScore(action.score());
        result.setSuggestion(suggestion(emotion.score(), communication.score(), action.score(), isChinese));
        result.setCalculationMethod(METHOD);
        result.setChartJson(chartJson(natalChart, transitChart, emotion, communication, action));
        result.setCreatedAt(LocalDateTime.now());
        return zodiacRepository.save(result);
    }

    private String chartJson(AstrologyChartCalculator.NatalChart natalChart,
                             AstrologyChartCalculator.TransitChart transitChart,
                             ScoreComputation emotion,
                             ScoreComputation communication,
                             ScoreComputation action) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("method", METHOD);
        payload.put("natal", natalChart);
        payload.put("transit", transitChart);
        payload.put("scores", Map.of(
                "emotion", emotion.score(),
                "communication", communication.score(),
                "action", action.score()
        ));
        payload.put("scoreDetails", Map.of(
                "emotion", emotion.aspects(),
                "communication", communication.aspects(),
                "action", action.aspects()
        ));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("zodiac chart serialization failed", ex);
        }
    }

    private ScoreComputation score(Map<String, AstrologyChartCalculator.PlanetPosition> natalPlanets,
                                   Map<String, AstrologyChartCalculator.PlanetPosition> transitPlanets,
                                   List<ScorePair> pairs) {
        List<AspectEvaluation> evaluations = new ArrayList<>();
        double weightedTotal = 0;
        double weightTotal = 0;
        for (ScorePair pair : pairs) {
            AspectEvaluation evaluation = aspectEvaluation(
                    pair.transitPlanet(),
                    transitPlanets.get(pair.transitPlanet()),
                    pair.natalPlanet(),
                    natalPlanets.get(pair.natalPlanet())
            );
            evaluations.add(evaluation);
            weightedTotal += evaluation.score() * pair.weight();
            weightTotal += pair.weight();
        }
        return new ScoreComputation(clamp(Math.round((float) (weightedTotal / weightTotal)), 0, 100), evaluations);
    }

    private AspectEvaluation aspectEvaluation(String transitName,
                                              AstrologyChartCalculator.PlanetPosition transit,
                                              String natalName,
                                              AstrologyChartCalculator.PlanetPosition natal) {
        if (transit == null || natal == null) {
            throw new IllegalStateException("missing planet position for aspect scoring");
        }
        double distance = AstrologyChartCalculator.angularDistance(transit.longitude(), natal.longitude());
        AspectSpec best = null;
        double bestDelta = Double.MAX_VALUE;
        for (AspectSpec aspect : ASPECTS) {
            double delta = Math.abs(distance - aspect.angle());
            if (delta <= aspect.orb() && delta < bestDelta) {
                best = aspect;
                bestDelta = delta;
            }
        }
        if (best == null) {
            return new AspectEvaluation(transitName, natalName, "Sign element", AstrologyChartCalculator.round(distance), null,
                    elementCompatibilityScore(transit.sign(), natal.sign()));
        }
        double strength = 1 - bestDelta / best.orb();
        int score = clamp(Math.round((float) (60 + best.effect() * strength)), 0, 100);
        return new AspectEvaluation(transitName, natalName, best.name(), AstrologyChartCalculator.round(distance),
                AstrologyChartCalculator.round(bestDelta), score);
    }

    private int zodiacScore(AstrologyChartCalculator.NatalChart userChart, TargetChart targetChart) {
        AstrologyChartCalculator.PlanetPosition userSun = userChart.planets().get("Sun");
        int elementScore = elementCompatibilityScore(userChart.sunSign(), targetChart.sunSign());
        if (!targetChart.precise()) {
            int sunAspectScore = aspectEvaluation("Target Sun", targetChart.sun(), "User Sun", userSun).score();
            return clamp(Math.round(sunAspectScore * 0.75f + elementScore * 0.25f), 0, 100);
        }

        List<ScorePair> pairs = List.of(
                new ScorePair("Sun", "Sun", 1.0),
                new ScorePair("Moon", "Moon", 0.8),
                new ScorePair("Venus", "Mars", 0.5),
                new ScorePair("Mars", "Venus", 0.5),
                new ScorePair("Mercury", "Mercury", 0.6)
        );
        ScoreComputation synastry = score(userChart.planets(), targetChart.planets(), pairs);
        return clamp(Math.round(synastry.score() * 0.8f + elementScore * 0.2f), 0, 100);
    }

    private TargetChart targetChart(ZodiacMatchRequest request) {
        boolean hasAnyPreciseField = request.targetBirthTime() != null
                || !blank(request.targetBirthTimezone())
                || request.targetLatitude() != null
                || request.targetLongitude() != null;
        boolean hasAllPreciseFields = request.targetBirthTime() != null
                && !blank(request.targetBirthTimezone())
                && request.targetLatitude() != null
                && request.targetLongitude() != null;
        if (hasAnyPreciseField && !hasAllPreciseFields) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "targetBirthTime, targetBirthTimezone, targetLatitude, and targetLongitude must be provided together");
        }
        if (!hasAllPreciseFields) {
            AstrologyChartCalculator.PlanetPosition sun = chartCalculator.dateOnlySun(request.targetBirthDate());
            return new TargetChart(sun.sign(), sun, Map.of("Sun", sun), false);
        }

        UserProfile targetProfile = new UserProfile();
        targetProfile.setBirthDate(request.targetBirthDate());
        targetProfile.setBirthTime(request.targetBirthTime());
        targetProfile.setBirthTimezone(request.targetBirthTimezone().trim());
        targetProfile.setLatitude(request.targetLatitude());
        targetProfile.setLongitude(request.targetLongitude());
        AstrologyChartCalculator.NatalChart chart = chartCalculator.natal(targetProfile);
        return new TargetChart(chart.sunSign(), chart.planets().get("Sun"), chart.planets(), true);
    }

    private int personalityScore(Long userId, String targetPersonalityTag) {
        String targetType = extractMbti(targetPersonalityTag);
        if (targetType == null) {
            return 68;
        }
        String userType = assessmentResultRepository.findFirstByUserIdAndTypeOrderByCreatedAtDesc(userId, "MBTI")
                .map(AssessmentResult::getResultLabel)
                .map(this::extractMbti)
                .orElse(null);
        if (userType == null) {
            return 72;
        }

        int score = 60;
        int[] sameDimensionWeights = {5, 8, 5, 5};
        int[] differentDimensionWeights = {3, 1, 3, 3};
        for (int i = 0; i < 4; i++) {
            score += userType.charAt(i) == targetType.charAt(i) ? sameDimensionWeights[i] : differentDimensionWeights[i];
        }
        return clamp(score, 0, 100);
    }

    private String extractMbti(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = MBTI_PATTERN.matcher(value.toUpperCase());
        return matcher.find() ? matcher.group() : null;
    }

    private String suggestion(int emotionScore, int communicationScore, int actionScore, boolean isChinese) {
        if (emotionScore <= communicationScore && emotionScore <= actionScore) {
            return isChinese
                    ? "今天先照顾情绪节律，再安排高强度互动；把日运视为计划提示，不当作预测。"
                    : "Start by protecting emotional rhythm before scheduling high-intensity interaction; treat this as a planning prompt, not a prediction.";
        }
        if (communicationScore <= actionScore) {
            return isChinese
                    ? "今天沟通更适合写清楚、慢确认；重要决定留出二次校对。"
                    : "Use slower, explicit communication today and give important decisions a second check.";
        }
        return isChinese
                ? "今天行动力需要节制使用；先做小步验证，再扩大投入。"
                : "Use action carefully today: validate in small steps before increasing commitment.";
    }

    private int elementCompatibilityScore(String userSign, String targetSign) {
        String a = ELEMENTS.getOrDefault(userSign, "Air");
        String b = ELEMENTS.getOrDefault(targetSign, "Air");
        if (a.equals(b)) {
            return 76;
        }
        if ((a.equals("Fire") && b.equals("Air")) || (a.equals("Air") && b.equals("Fire"))) {
            return 72;
        }
        if ((a.equals("Earth") && b.equals("Water")) || (a.equals("Water") && b.equals("Earth"))) {
            return 72;
        }
        if ((a.equals("Fire") && b.equals("Water")) || (a.equals("Water") && b.equals("Fire"))) {
            return 52;
        }
        return 62;
    }

    private String localizeSign(String sign, boolean isChinese) {
        return isChinese ? SIGN_CN.getOrDefault(sign, sign) : sign;
    }

    private boolean blank(String value) {
        return value == null || value.trim().isBlank();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record AspectSpec(String name, double angle, double orb, int effect) {
    }

    private record ScorePair(String transitPlanet, String natalPlanet, double weight) {
    }

    private record AspectEvaluation(String transitPlanet, String natalPlanet, String aspect, double distance, Double orb, int score) {
    }

    private record ScoreComputation(int score, List<AspectEvaluation> aspects) {
    }

    private record TargetChart(String sunSign,
                               AstrologyChartCalculator.PlanetPosition sun,
                               Map<String, AstrologyChartCalculator.PlanetPosition> planets,
                               boolean precise) {
    }
}
