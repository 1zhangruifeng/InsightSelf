package com.insightself.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_questions")
public class AssessmentQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(name = "instrument_version", nullable = false, length = 32)
    private String instrumentVersion = "1";

    @Column(name = "item_key", nullable = false, length = 64)
    private String itemKey;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "question_text_zh", length = 500)
    private String questionTextZh;

    @Column(nullable = false, length = 64)
    private String dimension;

    @Column(name = "reverse_score", nullable = false)
    private boolean reverseScore;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "source_note", length = 256)
    private String sourceNote;

    public AssessmentQuestion() {
    }

    public AssessmentQuestion(String type, String questionText, String questionTextZh, String dimension, boolean reverseScore, int displayOrder) {
        this(type, "1", type + "-" + displayOrder, questionText, questionTextZh, dimension, reverseScore, displayOrder, null);
    }

    public AssessmentQuestion(String type, String instrumentVersion, String itemKey, String questionText, String questionTextZh, String dimension, boolean reverseScore, int displayOrder, String sourceNote) {
        this.type = type;
        this.instrumentVersion = instrumentVersion;
        this.itemKey = itemKey;
        this.questionText = questionText;
        this.questionTextZh = questionTextZh;
        this.dimension = dimension;
        this.reverseScore = reverseScore;
        this.displayOrder = displayOrder;
        this.sourceNote = sourceNote;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getInstrumentVersion() { return instrumentVersion; }
    public void setInstrumentVersion(String instrumentVersion) { this.instrumentVersion = instrumentVersion; }
    public String getItemKey() { return itemKey; }
    public void setItemKey(String itemKey) { this.itemKey = itemKey; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getQuestionTextZh() { return questionTextZh; }
    public void setQuestionTextZh(String questionTextZh) { this.questionTextZh = questionTextZh; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public boolean isReverseScore() { return reverseScore; }
    public void setReverseScore(boolean reverseScore) { this.reverseScore = reverseScore; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public String getSourceNote() { return sourceNote; }
    public void setSourceNote(String sourceNote) { this.sourceNote = sourceNote; }
}
