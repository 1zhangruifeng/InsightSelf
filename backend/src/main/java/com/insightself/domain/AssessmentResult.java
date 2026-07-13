package com.insightself.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "assessment_results")
public class AssessmentResult {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(name = "instrument_version", nullable = false, length = 32)
    private String instrumentVersion = "1";

    @Column(name = "result_label", nullable = false, length = 64)
    private String resultLabel;

    @Column(name = "result_json", nullable = false, length = 12000)
    private String resultJson;

    @Column(length = 1200)
    private String summary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    public Map<String, Double> getScores() {
        Map<String, Object> wrapper = readResultWrapper();
        Object scores = wrapper.get("scores");
        if (scores == null) {
            return Map.of();
        }
        return OBJECT_MAPPER.convertValue(scores, new TypeReference<LinkedHashMap<String, Double>>() {});
    }

    @JsonIgnore
    public static String scoresJson(Map<String, Double> scores) {
        return resultJson(scores, Map.of());
    }

    @JsonIgnore
    public static String resultJson(Map<String, Double> scores, Map<String, Object> details) {
        try {
            return OBJECT_MAPPER.writeValueAsString(Map.of("scores", scores, "details", details));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to serialize assessment result", ex);
        }
    }

    private Map<String, Object> readResultWrapper() {
        try {
            return OBJECT_MAPPER.readValue(resultJson, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("failed to parse assessment result JSON", ex);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getInstrumentVersion() { return instrumentVersion; }
    public void setInstrumentVersion(String instrumentVersion) { this.instrumentVersion = instrumentVersion; }
    public String getResultLabel() { return resultLabel; }
    public void setResultLabel(String resultLabel) { this.resultLabel = resultLabel; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
