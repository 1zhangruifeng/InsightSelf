package com.insightself.dto;

public class AssessmentQuestionDto {
    private Long id;
    private String questionText;
    private String dimension;
    private boolean reverseScore;
    private String instrumentVersion;
    private String sourceNote;

    public AssessmentQuestionDto() {}

    public AssessmentQuestionDto(Long id, String questionText, String dimension, boolean reverseScore) {
        this.id = id;
        this.questionText = questionText;
        this.dimension = dimension;
        this.reverseScore = reverseScore;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public boolean isReverseScore() { return reverseScore; }
    public void setReverseScore(boolean reverseScore) { this.reverseScore = reverseScore; }
    public String getInstrumentVersion() { return instrumentVersion; }
    public void setInstrumentVersion(String instrumentVersion) { this.instrumentVersion = instrumentVersion; }
    public String getSourceNote() { return sourceNote; }
    public void setSourceNote(String sourceNote) { this.sourceNote = sourceNote; }
}
