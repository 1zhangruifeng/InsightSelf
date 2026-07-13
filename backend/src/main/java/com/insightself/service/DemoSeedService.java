package com.insightself.service;

import com.insightself.common.ApiException;
import com.insightself.domain.AssessmentResult;
import com.insightself.domain.User;
import com.insightself.dto.AuthRequest;
import com.insightself.dto.DemoSeedResponse;
import com.insightself.dto.ProfileRequest;
import com.insightself.dto.UserResponse;
import com.insightself.repository.AiReportRepository;
import com.insightself.repository.AssessmentResultRepository;
import com.insightself.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DemoSeedService {
    private static final String DEMO_USERNAME = "hkict_demo";
    private static final String DEMO_PASSWORD = "demo2026";

    private final boolean demoEnabled;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ProfileService profileService;
    private final AssessmentResultRepository assessmentResultRepository;
    private final BaziService baziService;
    private final ZodiacService zodiacService;
    private final AiReportService aiReportService;
    private final AiReportRepository aiReportRepository;

    public DemoSeedService(
            @Value("${insightself.demo.enabled:true}") boolean demoEnabled,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserService userService,
            ProfileService profileService,
            AssessmentResultRepository assessmentResultRepository,
            BaziService baziService,
            ZodiacService zodiacService,
            AiReportService aiReportService,
            AiReportRepository aiReportRepository
    ) {
        this.demoEnabled = demoEnabled;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.profileService = profileService;
        this.assessmentResultRepository = assessmentResultRepository;
        this.baziService = baziService;
        this.zodiacService = zodiacService;
        this.aiReportService = aiReportService;
        this.aiReportRepository = aiReportRepository;
    }

    @Transactional
    public DemoSeedResponse seed() {
        if (!demoEnabled) {
            throw new ApiException(HttpStatus.NOT_FOUND, "demo seed is disabled");
        }

        UserResponse user = ensureDemoUser();
        Long userId = user.userId();
        profileService.createOrReplace(userId, demoProfile());
        seedAssessmentIfMissing(userId, "BFI10", "High openness, high agreeableness", bigFiveScores(),
                "Curious, cooperative, moderately disciplined, and responsive to social feedback.");
        seedAssessmentIfMissing(userId, "MBTI", "ENFJ", mbtiScores(),
                "MBTI-style communication preference snapshot for familiar demo exploration.");
        seedAssessmentIfMissing(userId, "CAREER", "Social", careerScores(),
                "O*NET-style interest profile with strongest Social and Artistic preferences for reflection.");
        seedAssessmentIfMissing(userId, "WHO5", "Moderate well-being", who5Scores(),
                "WHO-5 snapshot showing recent well-being as a reflection cue.");
        seedAssessmentIfMissing(userId, "RSES", "Moderate self-esteem", rsesScores(),
                "Rosenberg self-esteem snapshot for self-acceptance reflection.");
        baziService.latestOrGenerate(userId);
        zodiacService.daily(userId);
        if (aiReportRepository.findTopByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            aiReportService.generate(userId);
        }

        return new DemoSeedResponse(
                user,
                "HKICT demo account is ready. Username: " + DEMO_USERNAME + ", password: " + DEMO_PASSWORD + ".",
                "Profile seeded with Hong Kong birth data and bilingual wellness preference.",
                "Big Five, MBTI-style, O*NET, WHO-5, and self-esteem snapshots seeded for immediate dashboard/report review.",
                "Bazi, astrology prompt, and AI/template report are ready."
        );
    }

    private UserResponse ensureDemoUser() {
        userRepository.findByUsername(DEMO_USERNAME).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
        if (userRepository.existsByUsername(DEMO_USERNAME)) {
            return userService.login(new AuthRequest(DEMO_USERNAME, DEMO_PASSWORD));
        }
        return userService.register(new AuthRequest(DEMO_USERNAME, DEMO_PASSWORD));
    }

    private ProfileRequest demoProfile() {
        return new ProfileRequest(
                "HKICT Demo Student",
                "Prefer not to say",
                LocalDate.of(2001, 8, 18),
                LocalTime.of(9, 30),
                "香港特别行政区|香港特别行政区|中西区",
                null,
                null,
                null,
                "SOLAR",
                "BALANCED",
                false,
                "en"
        );
    }

    private void seedAssessmentIfMissing(
            Long userId,
            String type,
            String label,
            Map<String, Double> scores,
            String summary
    ) {
        if (assessmentResultRepository.findFirstByUserIdAndTypeOrderByCreatedAtDesc(userId, type).isPresent()) {
            return;
        }
        AssessmentResult result = new AssessmentResult();
        result.setUserId(userId);
        result.setType(type);
        result.setInstrumentVersion("HKICT-DEMO-v1");
        result.setResultLabel(label);
        result.setResultJson(AssessmentResult.resultJson(scores, Map.of(
                "seededFor", "HKICT demo",
                "note", "Synthetic but plausible sample data for competition demonstration"
        )));
        result.setSummary(summary);
        result.setCreatedAt(LocalDateTime.now());
        assessmentResultRepository.save(result);
    }

    private Map<String, Double> bigFiveScores() {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Openness", 84.0);
        scores.put("Conscientiousness", 62.0);
        scores.put("Extraversion", 70.0);
        scores.put("Agreeableness", 80.0);
        scores.put("Neuroticism", 46.0);
        return scores;
    }

    private Map<String, Double> mbtiScores() {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("E/I:E", 4.0);
        scores.put("E/I:I", 2.0);
        scores.put("S/N:S", 2.5);
        scores.put("S/N:N", 4.5);
        scores.put("T/F:T", 2.0);
        scores.put("T/F:F", 4.0);
        scores.put("J/P:J", 4.0);
        scores.put("J/P:P", 2.5);
        return scores;
    }

    private Map<String, Double> careerScores() {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Realistic", 42.0);
        scores.put("Investigative", 66.0);
        scores.put("Artistic", 74.0);
        scores.put("Social", 86.0);
        scores.put("Enterprising", 68.0);
        scores.put("Conventional", 48.0);
        return scores;
    }

    private Map<String, Double> who5Scores() {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Well-being", 64.0);
        return scores;
    }

    private Map<String, Double> rsesScores() {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Self-esteem", 29.0);
        return scores;
    }
}
