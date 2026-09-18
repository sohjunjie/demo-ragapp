package org.example.model;

import java.util.List;

public record EmailLlmAnalysisIssue(
    String status,
    String issue,
    List<String> keyEvidence,
    String rootCauseSummary,
    String finalCustomerSentiment,
    String resolutionStepsTaken
) {}
