package com.insightself;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightself.repository.AiChatMessageRepository;
import com.insightself.repository.AiReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "QWEN_API_KEY=")
@AutoConfigureMockMvc
class BackendApiIntegrationTest {
    private static final AtomicInteger SEQ = new AtomicInteger();
    private final Map<Long, String> accessTokens = new ConcurrentHashMap<>();

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AiChatMessageRepository aiChatMessageRepository;

    @Autowired
    AiReportRepository aiReportRepository;

    @Test
    void healthEndpointUsesWrapper() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.aiProviderConfigured").value(false))
                .andExpect(jsonPath("$.data.fallbackMode").value("TEMPLATE"))
                .andExpect(jsonPath("$.data.safetyNotice").isNotEmpty());
    }

    @Test
    void registerAndLoginReturnBearerSessionTokens() throws Exception {
        String username = "demo" + SEQ.incrementAndGet();
        long userId = register(username);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", "pass1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("login ok"))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void demoSeedCreatesReadyToJudgeAccount() throws Exception {
        JsonNode seed = postJson("/api/demo/seed", null);
        long userId = seed.path("data").path("user").path("userId").asLong();
        String accessToken = seed.path("data").path("user").path("accessToken").asText();
        assertThat(accessToken).isNotBlank();
        accessTokens.put(userId, accessToken);

        mockMvc.perform(get("/api/dashboard/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.nickname").value("HKICT Demo Student"))
                .andExpect(jsonPath("$.data.assessmentResults").isArray())
                .andExpect(jsonPath("$.data.latestAiReport.reportText").isNotEmpty());

        mockMvc.perform(get("/api/ai-reports/latest/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.safetyNotice").isNotEmpty())
                .andExpect(jsonPath("$.data.sourceExplanation").isNotEmpty());
    }

    @Test
    void profileCreateGetAndUpdateWork() throws Exception {
        long userId = register("profile" + SEQ.incrementAndGet());

        createProfile(userId, "Alex");
        mockMvc.perform(get("/api/profiles/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Alex"))
                .andExpect(jsonPath("$.data.preference").value("BALANCED"));

        mockMvc.perform(put("/api/profiles/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson("Avery")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Avery"));
    }

    @Test
    void baziGenerationReturnsPillarsAndElementScores() throws Exception {
        long userId = registeredProfile("bazi" + SEQ.incrementAndGet());

        mockMvc.perform(post("/api/bazi/generate/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.yearPillar").isNotEmpty())
                .andExpect(jsonPath("$.data.elementScores.Wood").isNumber())
                .andExpect(jsonPath("$.data.calculationMethod").isNotEmpty())
                .andExpect(jsonPath("$.data.chartJson").isNotEmpty())
                .andExpect(jsonPath("$.data.conclusion").isNotEmpty());

        mockMvc.perform(get("/api/bazi/latest/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId));
    }

    @Test
    void zodiacDailyAndMatchWorkDeterministically() throws Exception {
        long userId = registeredProfile("zodiac" + SEQ.incrementAndGet());

        JsonNode first = getJson("/api/zodiac/daily/" + userId, userId);
        JsonNode second = getJson("/api/zodiac/daily/" + userId, userId);
        assertThat(first.path("data").path("zodiacSign").asText()).isEqualTo(second.path("data").path("zodiacSign").asText());
        assertThat(first.path("data").path("emotionScore").asInt()).isBetween(0, 100);
        assertThat(first.path("data").path("calculationMethod").asText()).contains("Swiss Ephemeris");
        assertThat(first.path("data").path("chartJson").asText()).contains("transit");

        mockMvc.perform(get("/api/zodiac/natal/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sunSign").value("Leo"))
                .andExpect(jsonPath("$.data.planets.Sun.longitude").isNumber());

        mockMvc.perform(post("/api/zodiac/match")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "userId", userId,
                                "targetNickname", "Jamie",
                                "targetBirthDate", "2002-02-14",
                                "targetPersonalityTag", "INFP"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetZodiacSign").value("Aquarius"))
                .andExpect(jsonPath("$.data.finalScore").isNumber());
    }

    @Test
    void assessmentQuestionsLoadAndSubmissionScoresResult() throws Exception {
        long userId = register("assess" + SEQ.incrementAndGet());
        JsonNode questions = getJson("/api/assessments/BFI10/questions", userId);
        assertThat(questions.path("data").size()).isGreaterThanOrEqualTo(20);

        List<Map<String, Object>> answers = questions.path("data").findValuesAsText("id").stream()
                .map(id -> Map.<String, Object>of("questionId", Long.parseLong(id), "score", 4))
                .toList();

        mockMvc.perform(post("/api/assessments/BFI10/submit")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", userId, "answers", answers))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("BFI10"))
                .andExpect(jsonPath("$.data.instrumentVersion").value("IPIP-BIG5-20-v1"))
                .andExpect(jsonPath("$.data.resultLabel").isNotEmpty())
                .andExpect(jsonPath("$.data.scores").isMap());
    }

    @Test
    void dashboardAutoGeneratesBaziAndZodiac() throws Exception {
        long userId = registeredProfile("dash" + SEQ.incrementAndGet());

        mockMvc.perform(get("/api/dashboard/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.nickname").value("Alex"))
                .andExpect(jsonPath("$.data.bazi.id").isNumber())
                .andExpect(jsonPath("$.data.zodiacDaily.zodiacSign").isNotEmpty())
                .andExpect(jsonPath("$.data.assessmentResults").isArray())
                .andExpect(jsonPath("$.data.integratedSummary").isNotEmpty());
    }

    @Test
    void aiReportUsesTemplateFallbackWithoutQwenKey() throws Exception {
        long userId = registeredProfile("report" + SEQ.incrementAndGet());

        JsonNode report = postJson("/api/ai-reports/generate/" + userId, null, userId);
        assertThat(report.path("data").path("generatedBy").asText()).isEqualTo("TEMPLATE");
        assertThat(report.path("data").path("reportText").asText()).isNotEmpty();
        assertThat(report.path("data").path("sourceExplanation").asText()).contains("template fallback");
        assertThat(report.path("data").path("safetyNotice").asText()).contains("self-reflection");
        assertThat(report.path("data").has("sourceSnapshotJson")).isFalse();

        String sourceSnapshotJson = aiReportRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow()
                .getSourceSnapshotJson();
        assertThat(sourceSnapshotJson)
                .contains("\"bazi\"", "\"astrology\"", "\"assessments\"", "\"elementScores\"", "\"dailyScores\"")
                .doesNotContain("chartJson", "sourceSnapshotJson", "latestAiReport", "reportText");
        JsonNode snapshot = objectMapper.readTree(sourceSnapshotJson);
        assertThat(snapshot.path("profile").path("nickname").asText()).isEqualTo("Alex");

        mockMvc.perform(get("/api/ai-reports/latest/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generatedBy").value("TEMPLATE"))
                .andExpect(jsonPath("$.data.sourceExplanation").isNotEmpty())
                .andExpect(jsonPath("$.data.safetyNotice").isNotEmpty())
                .andExpect(jsonPath("$.data.sourceSnapshotJson").doesNotExist());

        mockMvc.perform(get("/api/dashboard/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latestAiReport.generatedBy").value("TEMPLATE"))
                .andExpect(jsonPath("$.data.latestAiReport.sourceExplanation").isNotEmpty())
                .andExpect(jsonPath("$.data.safetyNotice").isNotEmpty())
                .andExpect(jsonPath("$.data.latestAiReport.sourceSnapshotJson").doesNotExist());
    }

    @Test
    void aiChatReturnsSessionAndPersistsMultiTurnMessages() throws Exception {
        long userId = registeredProfile("chat" + SEQ.incrementAndGet());

        JsonNode first = postJson("/api/ai/chat", Map.of("userId", userId, "message", "What should I focus on today?"), userId);
        String sessionId = first.path("data").path("sessionId").asText();
        assertThat(sessionId).isNotBlank();
        assertThat(first.path("data").path("reply").asText()).contains("QWEN_API_KEY");

        JsonNode second = postJson("/api/ai/chat", Map.of(
                "userId", userId,
                "sessionId", sessionId,
                "message", "Can you connect that with my assessment result?"
        ), userId);
        assertThat(second.path("data").path("sessionId").asText()).isEqualTo(sessionId);
        assertThat(aiChatMessageRepository.countBySessionId(sessionId)).isEqualTo(4);

        mockMvc.perform(post("/api/ai/chat")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", userId, "sessionId", "not-a-session", "message", "hello"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpointsRequireTokenAndUserOwnership() throws Exception {
        long ownerId = register("owner" + SEQ.incrementAndGet());
        long attackerId = register("attacker" + SEQ.incrementAndGet());

        mockMvc.perform(get("/api/profiles/{userId}", ownerId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/profiles/{userId}", ownerId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attackerId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    private long registeredProfile(String username) throws Exception {
        long userId = register(username);
        createProfile(userId, "Alex");
        return userId;
    }

    private long register(String username) throws Exception {
        JsonNode node = postJson("/api/users/register", Map.of("username", username, "password", "pass1234"));
        assertThat(node.path("success").asBoolean()).isTrue();
        long userId = node.path("data").path("userId").asLong();
        String accessToken = node.path("data").path("accessToken").asText();
        assertThat(accessToken).isNotBlank();
        accessTokens.put(userId, accessToken);
        return userId;
    }

    private void createProfile(long userId, String nickname) throws Exception {
        mockMvc.perform(post("/api/profiles/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson(nickname)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private String profileJson(String nickname) throws Exception {
        return json(Map.ofEntries(
                Map.entry("nickname", nickname),
                Map.entry("gender", "Prefer not to say"),
                Map.entry("birthDate", LocalDate.of(2001, 8, 18).toString()),
                Map.entry("birthTime", "09:30:00"),
                Map.entry("birthPlace", "香港特别行政区|香港特别行政区|中西区"),
                Map.entry("calendarType", "SOLAR"),
                Map.entry("preference", "BALANCED"),
                Map.entry("language", "en"),
                Map.entry("aiEnabled", false)
        ));
    }

    private JsonNode getJson(String url, long userId) throws Exception {
        String content = mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode postJson(String url, Object body) throws Exception {
        var builder = post(url).contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            builder.content(json(body));
        }
        String content = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(content);
    }

    private JsonNode postJson(String url, Object body, long userId) throws Exception {
        var builder = post(url)
                .header(HttpHeaders.AUTHORIZATION, bearer(userId))
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            builder.content(json(body));
        }
        String content = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(content);
    }

    private String bearer(long userId) {
        String accessToken = accessTokens.get(userId);
        assertThat(accessToken).as("registered test user has an access token").isNotBlank();
        return "Bearer " + accessToken;
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }
}
