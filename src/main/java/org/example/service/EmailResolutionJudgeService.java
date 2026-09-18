package org.example.service;

import org.example.model.EmailLlmAnalysis;
import org.example.model.EmailMessage;
import org.example.model.JudgeOptions;

public interface EmailResolutionJudgeService {
    default EmailLlmAnalysis evaluate(EmailMessage email) {
        return evaluate(email, JudgeOptions.defaults());
    }

    EmailLlmAnalysis evaluate(EmailMessage email, JudgeOptions options);
}
