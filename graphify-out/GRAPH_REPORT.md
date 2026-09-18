# Graph Report - demo-ragapp  (2026-09-18)

## Corpus Check
- 23 files · ~2,965 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 121 nodes · 313 edges · 9 communities (7 shown, 2 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 33 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b0a7e7c2`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ApiController
- EmailMessage
- DemoApplicationTests.java
- EmailLlmAnalysis
- EmailResolutionJudgeServiceImpl
- JudgeOptions
- PostgreSQL Datasource Configuration
- EmailParser
- org.example:demo-ragapp

## God Nodes (most connected - your core abstractions)
1. `EmailMessage` - 19 edges
2. `JudgeOptions` - 16 edges
3. `EmailParser` - 16 edges
4. `EmailResolutionJudgeServiceImpl` - 15 edges
5. `EmailResolutionJudgeServiceImplTest` - 14 edges
6. `EmailLlmAnalysis` - 13 edges
7. `ApiController` - 11 edges
8. `EmailTurn` - 10 edges
9. `EmailResolutionJudgeService` - 10 edges
10. `JudgeApiController` - 9 edges

## Surprising Connections (you probably didn't know these)
- `H2 Test Datasource Configuration` --semantically_similar_to--> `PostgreSQL Datasource Configuration`  [INFERRED] [semantically similar]
  src/test/resources/application.yml → src/main/resources/application.yml
- `ApiController` --references--> `EmailParser`  [EXTRACTED]
  src/main/java/org/example/controller/ApiController.java → src/main/java/org/example/service/EmailParser.java
- `EmailIngestionService` --references--> `EmailResolutionJudgeService`  [EXTRACTED]
  src/main/java/org/example/ingestion/EmailIngestionService.java → src/main/java/org/example/service/EmailResolutionJudgeService.java
- `EmailResolutionJudgeServiceImpl` --references--> `EmailLlmAnalysis`  [EXTRACTED]
  src/main/java/org/example/service/EmailResolutionJudgeServiceImpl.java → src/main/java/org/example/model/EmailLlmAnalysis.java
- `EmailResolutionJudgeServiceImpl` --references--> `ChatModelResolver`  [EXTRACTED]
  src/main/java/org/example/service/EmailResolutionJudgeServiceImpl.java → src/main/java/org/example/service/ChatModelResolver.java

## Import Cycles
- None detected.

## Communities (9 total, 2 thin omitted)

### Community 0 - "ApiController"
Cohesion: 0.15
Nodes (15): GetMapping, lombok.Getter, lombok.Setter, org.springframework.ai.document.Document, org.springframework.ai.vectorstore.VectorStore, org.springframework.stereotype.Service, org.springframework.web.multipart.MultipartFile, ApiController (+7 more)

### Community 1 - "EmailMessage"
Cohesion: 0.25
Nodes (7): CallResponseSpec, ChatClientRequestSpec, org.junit.jupiter.api.Test, EmailMessage, EmailTurn, Override, EmailResolutionJudgeServiceImplTest

### Community 3 - "EmailLlmAnalysis"
Cohesion: 0.28
Nodes (3): PostMapping, EmailLlmAnalysis, EmailLlmAnalysisIssue

### Community 4 - "EmailResolutionJudgeServiceImpl"
Cohesion: 0.33
Nodes (7): com.fasterxml.jackson.databind.ObjectMapper, java.util.regex.Pattern, lombok.extern.slf4j.Slf4j, org.springframework.ai.converter.BeanOutputConverter, org.springframework.boot.autoconfigure.SpringBootApplication, DemoApplication, EmailResolutionJudgeServiceImpl

### Community 5 - "JudgeOptions"
Cohesion: 0.16
Nodes (14): BeanOutputConverter, org.junit.jupiter.api.extension.ExtendWith, org.springframework.ai.chat.client.ChatClient, org.springframework.ai.chat.model.ChatModel, org.springframework.beans.factory.annotation.Autowired, org.springframework.beans.factory.ObjectProvider, JudgeOptions, ChatModelResolver (+6 more)

### Community 7 - "PostgreSQL Datasource Configuration"
Cohesion: 0.50
Nodes (4): PostgreSQL Datasource Configuration, Embedding Transformer Configuration, PgVector Configuration, H2 Test Datasource Configuration

### Community 8 - "EmailParser"
Cohesion: 0.15
Nodes (10): org.junit.jupiter.api.BeforeEach, org.springframework.stereotype.Component, org.springframework.test.web.servlet.MockMvc, RequestMapping, RestController, JudgeApiController, EmailParser, EmailResolutionJudgeService (+2 more)

## Knowledge Gaps
- **3 isolated node(s):** `org.example:demo-ragapp`, `Embedding Transformer Configuration`, `H2 Test Datasource Configuration`
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `EmailParser` connect `EmailParser` to `ApiController`, `EmailResolutionJudgeServiceImpl`?**
  _High betweenness centrality (0.139) - this node is a cross-community bridge._
- **Why does `EmailResolutionJudgeServiceImpl` connect `EmailResolutionJudgeServiceImpl` to `ApiController`, `EmailMessage`, `EmailLlmAnalysis`, `JudgeOptions`, `EmailParser`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Why does `ApiController` connect `ApiController` to `EmailParser`, `EmailResolutionJudgeServiceImpl`?**
  _High betweenness centrality (0.100) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `EmailMessage` (e.g. with `.testEvaluateFileEndpoint()` and `.testEvaluateWithDirectJsonResponse()`) actually correct?**
  _`EmailMessage` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 3 inferred relationships involving `JudgeOptions` (e.g. with `.evaluateFile()` and `.testResolveChatClientWithSpecificRegisteredModel()`) actually correct?**
  _`JudgeOptions` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `org.example:demo-ragapp`, `Embedding Transformer Configuration`, `H2 Test Datasource Configuration` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._