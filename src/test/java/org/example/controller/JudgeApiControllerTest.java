package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.EmailLlmAnalysis;
import org.example.model.EmailLlmAnalysisIssue;
import org.example.model.EmailMessage;
import org.example.model.EmailTurn;
import org.example.model.EvaluationRequest;
import org.example.model.JudgeOptions;
import org.example.service.EmailParser;
import org.example.service.EmailResolutionJudgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JudgeApiControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmailResolutionJudgeService emailResolutionJudgeService;

    @Mock
    private EmailParser emailParser;

    @InjectMocks
    private JudgeApiController judgeApiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(judgeApiController).build();
    }

    @Test
    void testEvaluateEndpoint() throws Exception {
        EmailMessage email = new EmailMessage(
                "Payment Service Error",
                "2026-03-01",
                "user@example.com",
                "support@example.com",
                List.of(new EmailTurn(1, "user@example.com", "2026-03-01", "Fixed now."))
        );
        EvaluationRequest request = new EvaluationRequest(email, new JudgeOptions("gemini-2.5-flash", 0.0));

        EmailLlmAnalysisIssue issue = new EmailLlmAnalysisIssue(
                "RESOLVED",
                "Payment service error on checkout",
                List.of("Fixed now."),
                "Payment gateway token expired.",
                "SATISFIED",
                "Refreshed payment token."
        );

        EmailLlmAnalysis analysis = new EmailLlmAnalysis(
                0.98,
                "Turn 1 resolved.",
                List.of(issue)
        );

        when(emailResolutionJudgeService.evaluate(any(), any())).thenReturn(analysis);

        mockMvc.perform(post("/api/judge/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidenceScore").value(0.98))
                .andExpect(jsonPath("$.rationale").value("Turn 1 resolved."))
                .andExpect(jsonPath("$.issues[0].status").value("RESOLVED"))
                .andExpect(jsonPath("$.issues[0].issue").value("Payment service error on checkout"))
                .andExpect(jsonPath("$.issues[0].rootCauseSummary").value("Payment gateway token expired."))
                .andExpect(jsonPath("$.issues[0].finalCustomerSentiment").value("SATISFIED"))
                .andExpect(jsonPath("$.issues[0].resolutionStepsTaken").value("Refreshed payment token."));
    }

    @Test
    void testEvaluateFileEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "input",
                "test.eml",
                "message/rfc822",
                "Subject: Test\n\nBody content".getBytes()
        );

        EmailMessage parsed = new EmailMessage("Test", "2026-03-01", "a@b.com", "b@c.com", List.of());
        EmailLlmAnalysis analysis = new EmailLlmAnalysis(
                0.5,
                "Inconclusive context.",
                List.of()
        );

        when(emailParser.parse(any())).thenReturn(parsed);
        when(emailResolutionJudgeService.evaluate(any(), any())).thenReturn(analysis);

        mockMvc.perform(multipart("/api/judge/evaluate-file")
                        .file(file)
                        .param("modelName", "gemini-2.5-flash")
                        .param("temperature", "0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidenceScore").value(0.5))
                .andExpect(jsonPath("$.rationale").value("Inconclusive context."));
    }
}
