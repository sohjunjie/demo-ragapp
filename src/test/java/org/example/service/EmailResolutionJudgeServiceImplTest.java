package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.EmailLlmAnalysis;
import org.example.model.EmailLlmAnalysisIssue;
import org.example.model.EmailMessage;
import org.example.model.EmailTurn;
import org.example.model.JudgeOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailResolutionJudgeServiceImplTest {

    @Mock
    private ChatModelResolver chatModelResolver;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private EmailResolutionJudgeServiceImpl judgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        judgeService = new EmailResolutionJudgeServiceImpl(chatModelResolver, objectMapper);
    }

    private void mockChatClientFlow(String llmOutput) {
        when(chatModelResolver.resolveChatClient(any())).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(llmOutput);
    }

    @Test
    void testFormatEmailTranscript() {
        EmailTurn turn1 = new EmailTurn(1, "alice@example.com", "2026-03-01T10:00:00Z", "Cannot login to payment gateway.");
        EmailTurn turn2 = new EmailTurn(2, "support@example.com", "2026-03-01T10:15:00Z", "Password reset link sent.");
        EmailMessage message = new EmailMessage(
                "Login Issue",
                "2026-03-01",
                "alice@example.com",
                "support@example.com",
                List.of(turn1, turn2)
        );

        String transcript = judgeService.formatEmailTranscript(message);

        assertNotNull(transcript);
        assertTrue(transcript.contains("Subject: Login Issue"));
        assertTrue(transcript.contains("--- Turn 1 ---"));
        assertTrue(transcript.contains("Cannot login to payment gateway."));
        assertTrue(transcript.contains("--- Turn 2 ---"));
        assertTrue(transcript.contains("Password reset link sent."));
    }

    @Test
    void testEvaluateWithDirectJsonResponse() {
        String jsonAnalysis = """
            {
              "confidenceScore": 0.95,
              "rationale": "Turn 1 reported login error. Turn 2 reset user credentials. Turn 3 user verified login successful.",
              "issues": [
                {
                  "status": "RESOLVED",
                  "issue": "User cannot log in to payment portal",
                  "keyEvidence": ["Customer confirmed login successful."],
                  "rootCauseSummary": "Account locked due to expired security token.",
                  "finalCustomerSentiment": "SATISFIED",
                  "resolutionStepsTaken": "Reset token and unlocked user identity."
                }
              ]
            }
            """;

        mockChatClientFlow(jsonAnalysis);

        EmailMessage message = new EmailMessage(
                "Login Issue",
                "2026-03-01",
                "alice@example.com",
                "support@example.com",
                List.of(new EmailTurn(1, "alice@example.com", "2026-03-01", "Fixed!"))
        );

        EmailLlmAnalysis analysis = judgeService.evaluate(message, JudgeOptions.defaults());

        assertNotNull(analysis);
        assertEquals(0.95, analysis.confidenceScore());
        assertTrue(analysis.rationale().contains("Turn 1 reported"));
        assertNotNull(analysis.issues());
        assertEquals(1, analysis.issues().size());

        EmailLlmAnalysisIssue issue = analysis.issues().get(0);
        assertEquals("RESOLVED", issue.status());
        assertEquals("User cannot log in to payment portal", issue.issue());
        assertEquals("Account locked due to expired security token.", issue.rootCauseSummary());
        assertEquals("SATISFIED", issue.finalCustomerSentiment());
        assertEquals("Reset token and unlocked user identity.", issue.resolutionStepsTaken());
        assertEquals(1, issue.keyEvidence().size());
        assertEquals("Customer confirmed login successful.", issue.keyEvidence().get(0));
    }

    @Test
    void testEvaluateWithMarkdownCodeBlockFallback() {
        String markdownOutput = """
            Here is the analysis of the email thread:
            ```json
            {
              "confidenceScore": 0.88,
              "rationale": "Turn 1 reported primary gateway latency. Turn 2 shifted traffic to standby cluster.",
              "issues": [
                {
                  "status": "WORKAROUND",
                  "issue": "Primary gateway high latency",
                  "keyEvidence": ["Applied temporary failover routing."],
                  "rootCauseSummary": "Network congestion on primary uplink.",
                  "finalCustomerSentiment": "NEUTRAL",
                  "resolutionStepsTaken": "Rerouted traffic to secondary gateway."
                }
              ]
            }
            ```
            """;

        mockChatClientFlow(markdownOutput);

        EmailMessage message = new EmailMessage(
                "Gateway Slowdown",
                "2026-03-01",
                "ops@example.com",
                "eng@example.com",
                List.of(new EmailTurn(1, "ops@example.com", "2026-03-01", "Traffic shifted."))
        );

        EmailLlmAnalysis analysis = judgeService.evaluate(message, new JudgeOptions("gemini-2.5-flash", 0.1));

        assertNotNull(analysis);
        assertEquals(0.88, analysis.confidenceScore());
        assertNotNull(analysis.issues());
        assertEquals(1, analysis.issues().size());

        EmailLlmAnalysisIssue issue = analysis.issues().get(0);
        assertEquals("WORKAROUND", issue.status());
        assertEquals("Primary gateway high latency", issue.issue());
        assertEquals("NEUTRAL", issue.finalCustomerSentiment());
        assertEquals("Network congestion on primary uplink.", issue.rootCauseSummary());
        assertEquals("Rerouted traffic to secondary gateway.", issue.resolutionStepsTaken());
    }

    @Test
    void testEvaluateNullMessageThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> judgeService.evaluate(null));
    }
}
