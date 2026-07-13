package com.insightself;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "QWEN_API_KEY=")
@AutoConfigureMockMvc
class AssessmentValidationIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void submissionRequiresEveryQuestionExactlyOnceAndScoresInRange() throws Exception {
        AuthFixture user = register("assessment-validation");
        JsonNode bfiQuestions = getJson("/api/assessments/BFI10/questions", user);
        JsonNode attachmentQuestions = getJson("/api/assessments/ATTACHMENT/questions", user);
        List<Map<String, Object>> validAnswers = answersFor(bfiQuestions, 4);

        List<Map<String, Object>> missingAnswer = new ArrayList<>(validAnswers);
        missingAnswer.remove(missingAnswer.size() - 1);
        postAssessment(user, "BFI10", missingAnswer)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("all questions for the assessment type must be answered once"));

        List<Map<String, Object>> duplicateAnswer = new ArrayList<>(validAnswers.subList(0, validAnswers.size() - 1));
        duplicateAnswer.add(validAnswers.get(0));
        postAssessment(user, "BFI10", duplicateAnswer)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("duplicate answer for question"));

        List<Map<String, Object>> outOfRange = new ArrayList<>(validAnswers);
        outOfRange.set(0, Map.of(
                "questionId", bfiQuestions.path("data").get(0).path("id").asLong(),
                "score", 6
        ));
        postAssessment(user, "BFI10", outOfRange)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("scores must be from 1 to 5"));

        List<Map<String, Object>> wrongTypeQuestion = new ArrayList<>(validAnswers.subList(0, validAnswers.size() - 1));
        wrongTypeQuestion.add(Map.of(
                "questionId", attachmentQuestions.path("data").get(0).path("id").asLong(),
                "score", 4
        ));
        postAssessment(user, "BFI10", wrongTypeQuestion)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("question does not belong to assessment type"));
    }

    @Test
    void localizedAssessmentMetadataAndQuestionsUseProfileLanguage() throws Exception {
        AuthFixture user = register("assessment-zh");
        createProfile(user, "zh");

        mockMvc.perform(get("/api/assessments/types/{userId}", user.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].displayName").value("IPIP 大五人格 20 题"));

        JsonNode questions = getJson("/api/assessments/BFI10/questions/" + user.userId(), user);
        assertThat(questions.path("data").get(0).path("questionText").asText()).contains("我");
        assertThat(questions.path("data").get(0).path("instrumentVersion").asText()).isEqualTo("IPIP-BIG5-20-v1");
        assertThat(questions.path("data").get(0).path("sourceNote").asText()).contains("IPIP");
    }

    @Test
    void mbtiStyleAndAttachmentSubmissionsReturnStructuredScores() throws Exception {
        AuthFixture user = register("assessment-score");

        JsonNode mbtiQuestions = getJson("/api/assessments/MBTI/questions", user);
        assertThat(mbtiQuestions.path("data")).hasSize(16);
        postAssessment(user, "MBTI", answersFor(mbtiQuestions, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.instrumentVersion").value("MBTI-STYLE-16-v1"))
                .andExpect(jsonPath("$.data.resultLabel").isNotEmpty())
                .andExpect(jsonPath("$.data.scores['E/I:E']").isNumber());

        JsonNode attachmentQuestions = getJson("/api/assessments/ATTACHMENT/questions", user);
        postAssessment(user, "ATTACHMENT", answersFor(attachmentQuestions, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.instrumentVersion").value("ECR-RS-GENERAL-9-v1"))
                .andExpect(jsonPath("$.data.resultLabel").isNotEmpty())
                .andExpect(jsonPath("$.data.scores.Avoidance").isNumber())
                .andExpect(jsonPath("$.data.scores.Anxiety").isNumber());

    }

    @Test
    void onetInterestProfilerSubmissionReturnsSixRiasecDimensionsAndDetails() throws Exception {
        AuthFixture user = register("assessment-career");
        JsonNode careerQuestions = getJson("/api/assessments/CAREER/questions", user);
        assertThat(careerQuestions.path("data")).hasSize(30);
        assertThat(careerQuestions.path("data").get(0).path("instrumentVersion").asText()).isEqualTo("ONET-MINI-IP-30-v1");
        assertThat(careerQuestions.path("data").get(0).path("sourceNote").asText()).contains("O*NET");
        String body = postAssessment(user, "CAREER", answersFor(careerQuestions, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.instrumentVersion").value("ONET-MINI-IP-30-v1"))
                .andExpect(jsonPath("$.data.resultLabel").isNotEmpty())
                .andExpect(jsonPath("$.data.scores.Realistic").isNumber())
                .andExpect(jsonPath("$.data.scores.Conventional").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        JsonNode details = objectMapper.readTree(body).path("data").path("resultJson");
        assertThat(details.asText()).contains("primaryInterest");
        assertThat(details.asText()).contains("secondaryInterest");
    }

    @Test
    void who5AndRsesUseTheirOwnResponseScales() throws Exception {
        AuthFixture user = register("assessment-scale");

        JsonNode who5Questions = getJson("/api/assessments/WHO5/questions", user);
        assertThat(who5Questions.path("data")).hasSize(5);
        postAssessment(user, "WHO5", answersFor(who5Questions, 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.instrumentVersion").value("WHO-5-WELLBEING-5-v1"))
                .andExpect(jsonPath("$.data.scores['Well-being']").isNumber());

        JsonNode rsesQuestions = getJson("/api/assessments/RSES/questions", user);
        assertThat(rsesQuestions.path("data")).hasSize(10);
        postAssessment(user, "RSES", answersFor(rsesQuestions, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.instrumentVersion").value("ROSENBERG-SELF-ESTEEM-10-v1"))
                .andExpect(jsonPath("$.data.scores['Self-esteem']").isNumber());

        postAssessment(user, "RSES", answersFor(rsesQuestions, 5))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("scores must be from 1 to 4"));
    }

    private org.springframework.test.web.servlet.ResultActions postAssessment(
            AuthFixture user,
            String type,
            List<Map<String, Object>> answers
    ) throws Exception {
        return mockMvc.perform(post("/api/assessments/{type}/submit", type)
                .header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("userId", user.userId(), "answers", answers))));
    }

    private List<Map<String, Object>> answersFor(JsonNode questions, int score) {
        List<Map<String, Object>> answers = new ArrayList<>();
        for (JsonNode question : questions.path("data")) {
            answers.add(Map.of("questionId", question.path("id").asLong(), "score", score));
        }
        return answers;
    }

    private AuthFixture register(String label) throws Exception {
        String username = label + "-" + System.nanoTime();
        JsonNode node = postJson("/api/users/register", Map.of(
                "username", username,
                "password", "pass1234"
        ));
        return new AuthFixture(
                node.path("data").path("userId").asLong(),
                node.path("data").path("accessToken").asText()
        );
    }

    private void createProfile(AuthFixture user, String language) throws Exception {
        mockMvc.perform(post("/api/profiles/{userId}", user.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.ofEntries(
                                Map.entry("nickname", "Alex"),
                                Map.entry("gender", "Prefer not to say"),
                                Map.entry("birthDate", LocalDate.of(2001, 8, 18).toString()),
                                Map.entry("birthTime", "09:30:00"),
                                Map.entry("birthPlace", "香港特别行政区|香港特别行政区|中西区"),
                                Map.entry("calendarType", "SOLAR"),
                                Map.entry("preference", "BALANCED"),
                                Map.entry("language", language),
                                Map.entry("aiEnabled", false)
                        ))))
                .andExpect(status().isOk());
    }

    private JsonNode getJson(String url, AuthFixture user) throws Exception {
        String content = mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer(user.accessToken())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(content);
    }

    private JsonNode postJson(String url, Object body) throws Exception {
        String content = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(content);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private record AuthFixture(long userId, String accessToken) {
    }
}
