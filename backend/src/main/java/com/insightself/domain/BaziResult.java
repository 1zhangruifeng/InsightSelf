package com.insightself.domain;

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
@Table(name = "bazi_results")
public class BaziResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String yearPillar;
    private String monthPillar;
    private String dayPillar;
    private String hourPillar;
    private int woodScore;
    private int fireScore;
    private int earthScore;
    private int metalScore;
    private int waterScore;

    @Column(length = 16000)
    private String chartJson;

    @Column(name = "calculation_method", length = 128)
    private String calculationMethod;

    @Column(length = 1200)
    private String conclusion;

    @Column(length = 1200)
    private String evidence;

    @Column(length = 1200)
    private String suggestion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    public Map<String, Integer> getElementScores() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Wood", woodScore);
        scores.put("Fire", fireScore);
        scores.put("Earth", earthScore);
        scores.put("Metal", metalScore);
        scores.put("Water", waterScore);
        return scores;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getYearPillar() { return yearPillar; }
    public void setYearPillar(String yearPillar) { this.yearPillar = yearPillar; }
    public String getMonthPillar() { return monthPillar; }
    public void setMonthPillar(String monthPillar) { this.monthPillar = monthPillar; }
    public String getDayPillar() { return dayPillar; }
    public void setDayPillar(String dayPillar) { this.dayPillar = dayPillar; }
    public String getHourPillar() { return hourPillar; }
    public void setHourPillar(String hourPillar) { this.hourPillar = hourPillar; }
    public int getWoodScore() { return woodScore; }
    public void setWoodScore(int woodScore) { this.woodScore = woodScore; }
    public int getFireScore() { return fireScore; }
    public void setFireScore(int fireScore) { this.fireScore = fireScore; }
    public int getEarthScore() { return earthScore; }
    public void setEarthScore(int earthScore) { this.earthScore = earthScore; }
    public int getMetalScore() { return metalScore; }
    public void setMetalScore(int metalScore) { this.metalScore = metalScore; }
    public int getWaterScore() { return waterScore; }
    public void setWaterScore(int waterScore) { this.waterScore = waterScore; }
    public String getChartJson() { return chartJson; }
    public void setChartJson(String chartJson) { this.chartJson = chartJson; }
    public String getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
