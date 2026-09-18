package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailLlmAnalysis;
import org.example.model.EmailMessage;
import org.example.model.EmailTurn;
import org.example.model.JudgeOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class EmailResolutionJudgeServiceImpl implements EmailResolutionJudgeService {

    private static final String SYSTEM_RUBRIC = """
        You are an expert LLM-as-a-Judge evaluating multi-turn customer support and IT operations email conversation threads.
        Your goal is to perform a deep semantic analysis of the email thread, decompose all distinct customer problems or inquiries into individual issue records, and assess the resolution status of each issue.

        Taxonomy for Issue Resolution Status:
        1. RESOLVED:
           - The root cause of the issue was diagnosed and a permanent fix, configuration change, or definitive answer was applied.
           - The customer confirmed the fix works, or the issue concluded with a verified successful outcome.
        2. WORKAROUND:
           - A temporary mitigation, bypass, hotfix, or rollback was applied to restore operations.
           - The underlying root cause remains unaddressed, or a permanent solution ticket is still open.
        3. UNRESOLVED:
           - The issue remains failing, blocked on external dependencies/permissions, abandoned due to non-responsive participants, or escalated without a fix.
        4. UNKNOWN:
           - The issue context is ambiguous, incomplete, corrupted, or insufficient to classify.

        Evaluation Process:
        Step 1: Thread-Level Chain-of-Thought Analysis ("rationale"):
        - Trace the chronological turn-by-turn evolution of the email thread (Turn 1 to Turn N).
        - Detail the actions taken by participants, blockers encountered, and shifting customer sentiment.
        - Calculate an overall thread confidence score (0.0 to 1.0) into "confidenceScore".

        Step 2: Granular Customer Issue Decomposition ("issues"):
        - Identify every distinct, unique customer problem, incident, or request reported across the conversation.
        - For EACH identified issue, produce an issue record containing:
          * "issue": Concise summary of the specific problem or request.
          * "status": Resolution status (RESOLVED, WORKAROUND, UNRESOLVED, UNKNOWN).
          * "keyEvidence": List of exact quoted sentences from the thread turns directly justifying this status.
          * "rootCauseSummary": The diagnosed root cause or underlying driver for this specific issue.
          * "finalCustomerSentiment": Customer sentiment regarding this issue (e.g. SATISFIED, FRUSTRATED, NEUTRAL).
          * "resolutionStepsTaken": Concrete diagnostic steps, fixes, or bypass actions executed to address this issue.
        """;

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");

    private final ChatModelResolver chatModelResolver;
    private final ObjectMapper objectMapper;
    private final BeanOutputConverter<EmailLlmAnalysis> outputConverter;

    @Autowired
    public EmailResolutionJudgeServiceImpl(
            ChatModelResolver chatModelResolver,
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        this.chatModelResolver = chatModelResolver;
        ObjectMapper mapper = objectMapperProvider != null ? objectMapperProvider.getIfAvailable() : null;
        this.objectMapper = (mapper != null) ? mapper : new ObjectMapper();
        this.outputConverter = new BeanOutputConverter<>(EmailLlmAnalysis.class);
    }

    public EmailResolutionJudgeServiceImpl(ChatModelResolver chatModelResolver, ObjectMapper objectMapper) {
        this.chatModelResolver = chatModelResolver;
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
        this.outputConverter = new BeanOutputConverter<>(EmailLlmAnalysis.class);
    }

    @Override
    public EmailLlmAnalysis evaluate(EmailMessage email, JudgeOptions options) {
        if (email == null) {
            throw new IllegalArgumentException("EmailMessage must not be null");
        }

        JudgeOptions effectiveOptions = options != null ? options : JudgeOptions.defaults();
        ChatClient chatClient = chatModelResolver.resolveChatClient(effectiveOptions);

        String emailTranscript = formatEmailTranscript(email);
        String schemaFormat = outputConverter.getFormat();

        String combinedSystemPrompt = SYSTEM_RUBRIC + "\n\nSchema and Output Format:\n" + schemaFormat;

        try {
            var promptSpec = chatClient.prompt()
                    .system(combinedSystemPrompt)
                    .user(u -> u.text("Please evaluate the following email conversation thread:\n\n{transcript}")
                            .param("transcript", emailTranscript));

            if (effectiveOptions.temperature() != null || effectiveOptions.modelName() != null) {
                var optionsBuilder = ChatOptions.builder();
                if (effectiveOptions.temperature() != null) {
                    optionsBuilder.temperature(effectiveOptions.temperature());
                }
                if (effectiveOptions.modelName() != null) {
                    optionsBuilder.model(effectiveOptions.modelName());
                }
                promptSpec.options(optionsBuilder);
            }

            String rawResponse = promptSpec.call().content();
            log.debug("Raw LLM Judge response: {}", rawResponse);

            return parseAnalysisResponse(rawResponse);
        } catch (Exception e) {
            log.error("Error executing LLM judge evaluation: {}", e.getMessage(), e);
            throw new RuntimeException("LLM judge evaluation failed: " + e.getMessage(), e);
        }
    }

    public String formatEmailTranscript(EmailMessage email) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EMAIL THREAD CONTEXT ===\n");
        sb.append("Subject: ").append(email.subject() != null ? email.subject() : "N/A").append("\n");
        sb.append("Date: ").append(email.date() != null ? email.date() : "N/A").append("\n");
        sb.append("From: ").append(email.from() != null ? email.from() : "N/A").append("\n");
        sb.append("To: ").append(email.to() != null ? email.to() : "N/A").append("\n\n");
        sb.append("=== CHRONOLOGICAL TURNS ===\n");

        List<EmailTurn> turns = email.turns() != null ? email.turns() : Collections.emptyList();
        if (turns.isEmpty()) {
            sb.append("(No email turns found in message)\n");
        } else {
            for (EmailTurn turn : turns) {
                sb.append(String.format("--- Turn %d ---\n", turn.turnNumber()));
                sb.append("Timestamp: ").append(turn.timestamp() != null ? turn.timestamp() : "Unknown").append("\n");
                sb.append("Sender: ").append(turn.sender() != null ? turn.sender() : "Unknown").append("\n");
                sb.append("Content:\n").append(turn.body() != null ? turn.body().strip() : "").append("\n\n");
            }
        }
        return sb.toString();
    }

    private EmailLlmAnalysis parseAnalysisResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException("Received empty response from LLM judge");
        }

        try {
            return outputConverter.convert(rawResponse);
        } catch (Exception primaryEx) {
            log.warn("Standard output converter failed on response, attempting regex JSON extraction: {}", primaryEx.getMessage());
            Matcher matcher = JSON_BLOCK_PATTERN.matcher(rawResponse);
            String jsonContent = matcher.find() ? matcher.group(1).trim() : rawResponse.trim();

            try {
                return objectMapper.readValue(jsonContent, EmailLlmAnalysis.class);
            } catch (Exception fallbackEx) {
                log.error("Failed to parse JSON content: {}", jsonContent, fallbackEx);
                throw new IllegalStateException("Failed to parse LLM judge response: " + rawResponse, fallbackEx);
            }
        }
    }
}
