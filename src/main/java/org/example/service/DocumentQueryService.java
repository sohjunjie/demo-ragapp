package org.example.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentQueryService {

    @Autowired
    private VectorStore vectorStore;

    public List<Document> searchSimilarDocuments(String queryText) {
        SearchRequest request = SearchRequest.builder()
                .query(queryText)
                .topK(5)
                .build();

        return vectorStore.similaritySearch(request);
    }

}
