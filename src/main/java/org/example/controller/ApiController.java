package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.model.IngestionReq;
import org.example.service.DocumentQueryService;
import org.example.service.EmailParser;
import org.example.service.IngestionService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private DocumentQueryService documentQueryService;

    @Autowired
    private EmailParser emailParser;

    @PostMapping(value = "/ingest-email", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void ingestEmails(@RequestPart MultipartFile input) {

        TikaDocumentReader tikaReader = new TikaDocumentReader(input.getResource());
        List<Document> rawDocuments = tikaReader.read();

        if(rawDocuments.isEmpty()) return;

        var emailRaw = rawDocuments.getFirst().getText();
        var emailFormatted = emailParser.parse(emailRaw);

        log.info("email content = {}", emailRaw);

    }

    @PostMapping("/ingest")
    public void ingest(@RequestBody IngestionReq req) {
        ingestionService.ingestString(
                req.getContent(),
                req.getSource(),
                req.getDescription(),
                req.getTopics()
        );
    }

    @GetMapping("/ask")
    public List<Document> ask(@RequestParam String q) {
        return documentQueryService.searchSimilarDocuments(q);
    }

}
