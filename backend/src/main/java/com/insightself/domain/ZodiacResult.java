package com.insightself.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "zodiac_results")
public class ZodiacResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "zodiac_sign", nullable = false)
    private String zodiacSign;

    @Column(name = "insight_date", nullable = false)
    private LocalDate insightDate;

    private int emotionScore;
    private int communicationScore;
    private int actionScore;

    @Column(length = 20000)
    private String chartJson;

    @Column(name = "calculation_method", length = 128)
    private String calculationMethod;

    @Column(length = 1200)
    private String suggestion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonProperty("date")
    public LocalDate getDate() {
        return insightDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getZodiacSign() { return zodiacSign; }
    public void setZodiacSign(String zodiacSign) { this.zodiacSign = zodiacSign; }
    public LocalDate getInsightDate() { return insightDate; }
    public void setInsightDate(LocalDate insightDate) { this.insightDate = insightDate; }
    public int getEmotionScore() { return emotionScore; }
    public void setEmotionScore(int emotionScore) { this.emotionScore = emotionScore; }
    public int getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(int communicationScore) { this.communicationScore = communicationScore; }
    public int getActionScore() { return actionScore; }
    public void setActionScore(int actionScore) { this.actionScore = actionScore; }
    public String getChartJson() { return chartJson; }
    public void setChartJson(String chartJson) { this.chartJson = chartJson; }
    public String getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
