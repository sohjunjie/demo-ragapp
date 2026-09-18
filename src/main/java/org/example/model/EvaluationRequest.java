package org.example.model;

public record EvaluationRequest(
    EmailMessage email,
    JudgeOptions options
) {}
