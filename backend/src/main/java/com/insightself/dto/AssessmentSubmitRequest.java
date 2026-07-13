package com.insightself.dto;

import java.util.List;

public record AssessmentSubmitRequest(Long userId, List<Answer> answers) {
    public record Answer(Long questionId, int score) {
    }
}
