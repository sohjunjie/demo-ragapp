package org.example.ingestion;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StringIngestionService {

    @Autowired
    private VectorStore vectorStore;

    public void ingestString(String content, String source, String description, List<String> topics) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put("description", description);
        metadata.put("topics", topics);

        Document doc = Document.builder()
                .text(content)
                .metadata(metadata)
                .build();

        var splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(50)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();

        var vecDoc = splitter.split(doc);
        vectorStore.add(vecDoc);

    }

}
