package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.EmailLlmAnalysis;
import org.example.model.EmailMessage;
import org.example.service.EmailParser;
import org.example.service.EmailResolutionJudgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
