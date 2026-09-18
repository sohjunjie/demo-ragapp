package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailLlmAnalysis;
import org.example.model.EmailMessage;
import org.example.model.JudgeOptions;
import org.example.service.EmailParser;
import org.example.service.EmailResolutionJudgeService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/judge")
public class JudgeApiController {

    private final EmailResolutionJudgeService emailResolutionJudgeService;
    private final EmailParser emailParser;

    @Autowired
    public JudgeApiController(EmailResolutionJudgeService emailResolutionJudgeService, EmailParser emailParser) {
        this.emailResolutionJudgeService = emailResolutionJudgeService;
        this.emailParser = emailParser;

    }

    @PostMapping(value = "/evaluate-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmailLlmAnalysis evaluateFile(
            @RequestPart MultipartFile input,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) Double temperature
    ) {
        TikaDocumentReader tikaReader = new TikaDocumentReader(input.getResource());
        List<Document> rawDocuments = tikaReader.read();

        if (rawDocuments.isEmpty()) {
            throw new IllegalArgumentException("No content could be extracted from the uploaded email file");
        }

        String emailRaw = rawDocuments.getFirst().getText();
        EmailMessage emailFormatted = emailParser.parse(emailRaw);
        JudgeOptions options = new JudgeOptions(modelName, temperature);

        return emailResolutionJudgeService.evaluate(emailFormatted, options);
    }

}
