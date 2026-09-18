package org.example.ingestion;

import org.example.model.EmailMessage;
import org.example.service.EmailResolutionJudgeService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailIngestionService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmailResolutionJudgeService emailResolutionJudgeService;

    public void ingestEmail(EmailMessage emailMessage) {

        var emailAnalysis = emailResolutionJudgeService.evaluate(emailMessage, null);
        var confidence = emailAnalysis.confidenceScore();
        var rationale = emailAnalysis.rationale();

        for(var issue: emailAnalysis.issues()) {

            String content = String.format(
                    "Issue: %s\nRoot Cause: %s",
                    issue.issue(),
                    issue.rootCauseSummary()
            );

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", issue.status());
            metadata.put("customer_sentiment", issue.finalCustomerSentiment());
            metadata.put("resolution_steps", issue.resolutionStepsTaken());
            metadata.put("original_issue", issue.issue());
            metadata.put("confidence", confidence);
            metadata.put("rationale", rationale);

            Document doc = Document.builder()
                    .text(content)
                    .metadata(metadata)
                    .build();

            vectorStore.add(List.of(doc));

        }

    }

}
