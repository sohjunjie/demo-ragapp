package org.example.model;

import java.util.List;

public record EmailLlmAnalysis(
    double confidenceScore,
    String rationale,
    List<EmailLlmAnalysisIssue> issues
) {}
