# Graph Report - demo-ragapp  (2026-09-18)

## Corpus Check
- 23 files · ~2,987 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 122 nodes · 321 edges · 10 communities (7 shown, 3 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 37 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `271088cb`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ApiController
- EmailMessage
- DemoApplicationTests.java
- JudgeApiControllerTest.java
- EmailResolutionJudgeServiceImpl
- JudgeOptions
- DemoApplication
- PostgreSQL Datasource Configuration
- EmailParser
- org.example:demo-ragapp

## God Nodes (most connected - your core abstractions)
1. `EmailMessage` - 19 edges
2. `JudgeOptions` - 19 edges
3. `EmailParser` - 16 edges
4. `EmailLlmAnalysis` - 15 edges
5. `EmailResolutionJudgeServiceImpl` - 15 edges
6. `EmailResolutionJudgeServiceImplTest` - 14 edges
7. `EmailTurn` - 12 edges
8. `ApiController` - 10 edges
9. `JudgeApiController` - 10 edges
10. `JudgeApiControllerTest` - 10 edges

## Surprising Connections (you probably didn't know these)
- `H2 Test Datasource Configuration` --semantically_similar_to--> `PostgreSQL Datasource Configuration`  [INFERRED] [semantically similar]
  src/test/resources/application.yml → src/main/resources/application.yml
- `ApiController` --references--> `EmailParser`  [EXTRACTED]
  src/main/java/org/example/controller/ApiController.java → src/main/java/org/example/service/EmailParser.java
- `JudgeApiController` --references--> `EmailParser`  [EXTRACTED]
  src/main/java/org/example/controller/JudgeApiController.java → src/main/java/org/example/service/EmailParser.java
- `JudgeApiControllerTest` --references--> `JudgeApiController`  [EXTRACTED]
  src/test/java/org/example/controller/JudgeApiControllerTest.java → src/main/java/org/example/controller/JudgeApiController.java
- `EmailResolutionJudgeServiceImpl` --references--> `EmailLlmAnalysis`  [EXTRACTED]
  src/main/java/org/example/service/EmailResolutionJudgeServiceImpl.java → src/main/java/org/example/model/EmailLlmAnalysis.java

## Import Cycles
- None detected.

## Communities (10 total, 3 thin omitted)

### Community 0 - "ApiController"
Cohesion: 0.16
Nodes (14): GetMapping, lombok.Getter, lombok.Setter, org.springframework.ai.document.Document, org.springframework.ai.vectorstore.VectorStore, org.springframework.stereotype.Service, org.springframework.web.multipart.MultipartFile, ApiController (+6 more)

### Community 1 - "EmailMessage"
Cohesion: 0.25
Nodes (7): CallResponseSpec, ChatClientRequestSpec, org.junit.jupiter.api.Test, EmailMessage, EmailTurn, Override, EmailResolutionJudgeServiceImplTest

### Community 3 - "JudgeApiControllerTest.java"
Cohesion: 0.18
Nodes (8): org.junit.jupiter.api.BeforeEach, org.junit.jupiter.api.extension.ExtendWith, org.springframework.test.web.servlet.MockMvc, PostMapping, EmailLlmAnalysis, EmailLlmAnalysisIssue, EvaluationRequest, JudgeApiControllerTest

### Community 4 - "EmailResolutionJudgeServiceImpl"
Cohesion: 0.22
Nodes (11): BeanOutputConverter, com.fasterxml.jackson.databind.ObjectMapper, lombok.extern.slf4j.Slf4j, org.springframework.ai.converter.BeanOutputConverter, org.springframework.beans.factory.annotation.Autowired, RequestMapping, RestController, JudgeApiController (+3 more)

### Community 5 - "JudgeOptions"
Cohesion: 0.21
Nodes (10): org.springframework.ai.chat.client.ChatClient, org.springframework.ai.chat.model.ChatModel, org.springframework.beans.factory.ObjectProvider, JudgeOptions, ChatModelResolver, DefaultChatModelResolver, Builder, Override (+2 more)

### Community 7 - "PostgreSQL Datasource Configuration"
Cohesion: 0.50
Nodes (4): PostgreSQL Datasource Configuration, Embedding Transformer Configuration, PgVector Configuration, H2 Test Datasource Configuration

### Community 8 - "EmailParser"
Cohesion: 0.30
Nodes (4): java.util.regex.Pattern, org.springframework.stereotype.Component, EmailParser, EmailParserTest

## Knowledge Gaps
- **3 isolated node(s):** `org.example:demo-ragapp`, `Embedding Transformer Configuration`, `H2 Test Datasource Configuration`
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `EmailParser` connect `EmailParser` to `ApiController`, `JudgeApiControllerTest.java`, `EmailResolutionJudgeServiceImpl`?**
  _High betweenness centrality (0.153) - this node is a cross-community bridge._
- **Why does `EmailResolutionJudgeServiceImpl` connect `EmailResolutionJudgeServiceImpl` to `ApiController`, `EmailMessage`, `JudgeApiControllerTest.java`, `JudgeOptions`, `EmailParser`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Why does `ApiController` connect `ApiController` to `EmailParser`, `EmailResolutionJudgeServiceImpl`?**
  _High betweenness centrality (0.093) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `EmailMessage` (e.g. with `.testEvaluateEndpoint()` and `.testEvaluateFileEndpoint()`) actually correct?**
  _`EmailMessage` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `JudgeOptions` (e.g. with `.evaluateFile()` and `.testEvaluateEndpoint()`) actually correct?**
  _`JudgeOptions` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `EmailLlmAnalysis` (e.g. with `.testEvaluateEndpoint()` and `.testEvaluateFileEndpoint()`) actually correct?**
  _`EmailLlmAnalysis` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `org.example:demo-ragapp`, `Embedding Transformer Configuration`, `H2 Test Datasource Configuration` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._