# Demo RAG & LLM-as-a-Judge Application (`demo-ragapp`)

A modern, enterprise-grade Spring Boot and Spring AI application demonstrating **Retrieval-Augmented Generation (RAG)**, **Multi-Turn Email Parsing**, **Local ONNX Vector Embeddings**, and **LLM-as-a-Judge Semantic Evaluation**.

---

## 🌟 Overview

`demo-ragapp` bridges multi-turn IT operations/customer support threads and AI-powered intelligence. The application ingests complex email chains, parses chronological conversation turns, runs deep semantic evaluations via an LLM judge to classify issue resolution statuses and customer sentiment, and indexes structured insights into **PGVector** using local transformer embedding models.

```
                  ┌────────────────────────┐
                  │ Multi-Turn Email File  │ (.eml / raw text)
                  └───────────┬────────────┘
                              │
                              ▼
                  ┌────────────────────────┐
                  │   EmailParser & Tika   │ (Header isolation & chronological turns)
                  └───────────┬────────────┘
                              │
               ┌──────────────┴──────────────┐
               ▼                             ▼
┌─────────────────────────────┐ ┌─────────────────────────────┐
│  LLM-as-a-Judge Evaluation  │ │ Local ONNX Embeddings (384) │
│  (Gemini / Google GenAI)    │ │   (all-MiniLM-L6-v2)        │
│  - Issue decomposition      │ └──────────────┬──────────────┘
│  - Status & Root Cause      │                │
│  - Customer Sentiment       │                ▼
└──────────────┬──────────────┘ ┌─────────────────────────────┐
               │                │       PGVector Store        │
               └───────────────►│  (Issue text + Metadata)    │
                                └──────────────┬──────────────┘
                                               │
                                               ▼
                                ┌─────────────────────────────┐
                                │   Semantic Search / Ask     │
                                └─────────────────────────────┘
```

---

## ✨ Key Features

- **Multi-Turn Email Parsing & Decomposition**:
  - Automatically isolates email headers (`Subject`, `Date`, `From`, `To`) and reply delimiters (`On ... wrote:`).
  - Reconstructs chronological conversation turns (Turn 1 to Turn $N$) from raw `.eml` or text files.
- **LLM-as-a-Judge Semantic Evaluation**:
  - Uses custom system rubrics and Chain-of-Thought (CoT) reasoning to evaluate email threads.
  - Decomposes threads into discrete customer issues and classifies their status: `RESOLVED`, `WORKAROUND`, `UNRESOLVED`, or `UNKNOWN`.
  - Extracts direct quoted evidence, root cause summaries, customer sentiment, and resolution steps.
  - Features dual-layer JSON parsing with Spring AI `BeanOutputConverter` and fallback regex extraction.
- **Dynamic Model Resolution**:
  - Dynamically routes requests to specific LLM models (e.g., Google GenAI / Gemini) and adjusts runtime parameters (temperature, model name) per request.
- **Local ONNX Embeddings (Zero API Embedding Cost)**:
  - Powered by `spring-ai-starter-model-transformers` and Microsoft ONNX runtime, generating 384-dimensional embeddings locally in `./onnx-models`.
- **Vector Storage & Semantic Search**:
  - Integrates with **PostgreSQL + PGVector** (HNSW index, Cosine distance) for metadata-enriched similarity queries.
  - Uses H2 in-memory vector store for automated unit/integration testing.
- **Interactive Documentation**:
  - Built-in Swagger UI and OpenAPI 3 specifications.

---

## 🛠️ Tech Stack

| Component | Technology / Library |
|---|---|
| **Framework** | Spring Boot 4.1.0 |
| **Language** | Java 26 |
| **AI Framework** | Spring AI 2.0.0 |
| **LLM Provider** | Google GenAI (Gemini 3.5 Flash / Gemini 2.5 Flash) |
| **Embedding Engine** | ONNX Runtime + DJL Tokenizers (`spring-ai-starter-model-transformers`) |
| **Vector Database** | PostgreSQL with `pgvector` (H2 for test profiles) |
| **Document Reader** | Apache Tika (`spring-ai-tika-document-reader`) |
| **API Docs** | SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 3.0.3) |
| **Build Tool** | Apache Maven |

---

## 🚀 Getting Started

### Prerequisites

- **Java 26 SDK** (or compatible JDK)
- **Maven 3.9+**
- **PostgreSQL 15+** with the [`pgvector`](https://github.com/pgvector/pgvector) extension enabled

### Configuration & Environment Variables

Set the following environment variables before starting the application:

```bash
# Google Gemini / GenAI API Key
export GEMINI_API_KEY="your-gemini-api-key"

# PostgreSQL / PGVector Credentials
export VECTOR_DB_USR="your_postgres_user"
export VECTOR_DB_PWD="your_postgres_password"
```

Application settings can be reviewed and adjusted in [`src/main/resources/application.yml`](src/main/resources/application.yml):

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY:}
        chat:
          options:
            model: gemini-3.5-flash
    embedding.transformer.enabled: true
    embedding.transformer.cache.directory: ./onnx-models
    vectorstore:
      pgvector:
        initialize-schema: true
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 384
        table-name: vector_store
  datasource:
    url: jdbc:postgresql://localhost:5432/ragdb
    username: ${VECTOR_DB_USR:}
    password: ${VECTOR_DB_PWD:}
```

### Build & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/demo-ragapp.git
   cd demo-ragapp
   ```

2. **Execute Tests**:
   ```bash
   mvn test
   ```

3. **Start the Application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access Swagger UI**:
   Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser.

---

## 📡 REST API Reference

### 1. Evaluate Email Thread (`LLM-as-a-Judge`)

Evaluates a multi-turn `.eml` or email text file without modifying the vector store.

- **Endpoint**: `POST /api/judge/evaluate-file`
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `input` (*required*, file): Email file (.eml, .txt).
  - `modelName` (*optional*, query string): Override target LLM model.
  - `temperature` (*optional*, query string): LLM temperature parameter.

**Sample Request (`curl`)**:
```bash
curl -X POST "http://localhost:8080/api/judge/evaluate-file?temperature=0.2" \
  -F "input=@dataset/EMAIL_CHAIN_241_RESOLVED.eml"
```

**Sample Response**:
```json
{
  "confidenceScore": 0.95,
  "rationale": "Chronological trace shows customer reported a database connection timeout in Turn 1. Support provided connection pool tuning parameters in Turn 2. Customer verified in Turn 3 that latency normalized.",
  "issues": [
    {
      "status": "RESOLVED",
      "issue": "Database connection pool exhaustion under peak load",
      "keyEvidence": [
        "All pool connections are exhausted during batch jobs.",
        "Increasing max-pool-size to 50 resolved the timeout errors completely."
      ],
      "rootCauseSummary": "Undersized connection pool configuration for concurrent batch transactions",
      "finalCustomerSentiment": "SATISFIED",
      "resolutionStepsTaken": "Updated HikariCP maximumPoolSize configuration and applied rolling restart"
    }
  ]
}
```

---

### 2. Ingest Email & Store Embeddings

Parses the email, runs the judge evaluation, decomposes issues, and persists each issue with rich metadata into the PGVector store.

- **Endpoint**: `POST /api/ingest-email`
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `input` (*required*, file): The email file to parse and ingest.

**Sample Request (`curl`)**:
```bash
curl -X POST "http://localhost:8080/api/ingest-email" \
  -F "input=@dataset/EMAIL_CHAIN_241_RESOLVED.eml"
```

---

### 3. Ingest Arbitrary Document / Text

Splits text into chunks via `TokenTextSplitter` and embeds them with metadata.

- **Endpoint**: `POST /api/ingest-string`
- **Content-Type**: `application/json`

**Sample Request**:
```json
{
  "content": "Spring AI provides high-level abstractions for AI models, vector stores, and document readers.",
  "source": "knowledge-base",
  "description": "Spring AI overview documentation",
  "topics": ["spring-boot", "spring-ai", "rag"]
}
```

---

### 4. Semantic Similarity Search (`RAG Query`)

Performs cosine similarity search against the vector database to retrieve relevant documents and metadata.

- **Endpoint**: `GET /api/ask?q={query}`

**Sample Request (`curl`)**:
```bash
curl -X GET "http://localhost:8080/api/ask?q=database+connection+pool+exhaustion"
```

**Sample Response**:
```json
[
  {
    "text": "Issue: Database connection pool exhaustion under peak load\nRoot Cause: Undersized connection pool configuration",
    "metadata": {
      "status": "RESOLVED",
      "customer_sentiment": "SATISFIED",
      "resolution_steps": "Updated HikariCP maximumPoolSize configuration",
      "confidence": 0.95
    }
  }
]
```

---

## 📂 Project Structure

```
demo-ragapp/
├── dataset/                        # Sample multi-turn email conversation datasets (.eml)
├── onnx-models/                    # Cached ONNX transformer embedding models
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── DemoApplication.java          # Spring Boot main application entrypoint
│   │   │   ├── controller/
│   │   │   │   ├── ApiController.java        # Ingestion & Query endpoints
│   │   │   │   └── JudgeApiController.java   # LLM Judge evaluation endpoints
│   │   │   ├── ingestion/
│   │   │   │   ├── EmailIngestionService.java   # Decomposes & stores email analysis in vector DB
│   │   │   │   └── StringIngestionService.java  # Token-splits & embeds text strings
│   │   │   ├── model/
│   │   │   │   ├── EmailLlmAnalysis.java        # Judge response record
│   │   │   │   ├── EmailLlmAnalysisIssue.java   # Decomposed issue record
│   │   │   │   ├── EmailMessage.java            # Parsed email message model
│   │   │   │   ├── EmailTurn.java               # Chronological turn model
│   │   │   │   ├── IngestionReq.java            # Ingestion request DTO
│   │   │   │   └── JudgeOptions.java            # Dynamic model evaluation options
│   │   │   └── service/
│   │   │       ├── ChatModelResolver.java           # LLM client resolver contract
│   │   │       ├── DefaultChatModelResolver.java    # Dynamic ChatClient resolution
│   │   │       ├── DocumentQueryService.java        # Semantic similarity vector retrieval
│   │   │       ├── EmailParser.java                 # Regex header & turn extraction
│   │   │       ├── EmailResolutionJudgeService.java # Evaluation service contract
│   │   │       └── EmailResolutionJudgeServiceImpl.java # LLM rubric & prompt execution
│   │   └── resources/
│   │       └── application.yml               # Application configuration
│   └── test/
│       ├── java/org/example/
│       │   ├── DemoApplicationTests.java
│       │   ├── controller/JudgeApiControllerTest.java
│       │   └── service/
│       │       ├── DefaultChatModelResolverTest.java
│       │       ├── EmailParserTest.java
│       │       └── EmailResolutionJudgeServiceImplTest.java
│       └── resources/
│           └── application.yml               # H2 test datasource configuration
└── pom.xml                                   # Maven dependencies & build configuration
```

---

## 🧪 Testing

The test suite validates email parsing, dynamic LLM client resolution, judge parsing fallbacks, mock controller integrations, and full application context bootstrapping with H2 in-memory vector storage:

```bash
mvn clean test
```

---

## 📄 License

This project is licensed under the Apache-2.0 License.
