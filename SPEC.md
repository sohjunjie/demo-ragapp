# LLM-as-a-Judge Email Resolution Interpretation Specification

## Problem Statement

Operations, IT support, and customer support organizations process hundreds of multi-turn email communication threads. Support managers and audit teams struggle to reliably evaluate whether reported incidents and inquiries are actually solved, bypassed with temporary workarounds, remaining unresolved, or abandoned due to unresponsive participants. Manually reading each email thread and tracking chronological progression is slow, error-prone, and unscalable. Furthermore, organizations need the flexibility to run evaluations across different LLM providers and models (e.g., OpenAI GPT-4o, Anthropic Claude, Ollama local models, Google Gemini, or custom endpoints) depending on operational costs, privacy/compliance requirements, or offline batch evaluation needs.

## Solution

A pluggable, model-agnostic Java service that leverages LLM-as-a-Judge evaluation techniques to assess structured email conversation threads (EmailMessage with chronological EmailTurn records). The system produces strongly-typed evaluation verdicts containing the resolution category (RESOLVED, WORKAROUND, UNRESOLVED, ABANDONED, UNKNOWN), confidence score, chain-of-thought justification, cited textual evidence, and sentiment analysis. The service architecture is decoupled from any single LLM provider through dynamic model/client selection driven by configuration and runtime parameters.

## User Stories

1. As a support operations lead, I want the system to analyze multi-turn email chains, so that I can automatically determine if customer issues were fully resolved.
2. As an operations analyst, I want to distinguish between a permanent resolution and a temporary workaround, so that technical debt and recurring incidents can be tracked accurately.
3. As a quality assurance manager, I want the judge service to detect abandoned or stalled conversations, so that our team can proactively follow up on cold tickets.
4. As an incident response coordinator, I want to identify unresolved critical issues, so that unresolved high-severity incidents can be escalated before SLA breaches occur.
5. As a system administrator, I want to configure the default LLM provider and model name via application properties, so that the service can run on different cloud or on-premise model providers without code modifications.
6. As an automated evaluation engineer, I want the ability to specify a target LLM model or temperature on a per-request basis, so that I can benchmark different model architectures against our ground-truth evaluation dataset.
7. As a data engineer, I want structured JSON outputs mapped to strongly typed Java records, so that downstream services and analytics pipelines receive guaranteed schemas without parsing plain text.
8. As an auditor, I want each judgment to include chain-of-thought rationale and exact quotation evidence from the email turns, so that decisions are transparent and human-verifiable.
9. As a security compliance officer, I want the option to route sensitive customer emails to self-hosted/local LLM models (e.g., via Ollama) while sending non-sensitive batch data to cloud models, so that strict data residency regulations are respected.
10. As an API consumer, I want clear error handling and fallback behaviors when an LLM service is unreachable or returns malformed data, so that client applications remain resilient.
11. As a batch pipeline processor, I want to evaluate hundreds of parsed email messages in parallel with configurable concurrency, so that historical archives can be audited efficiently.
12. As an operations lead, I want customer sentiment and root-cause summaries included in the verdict, so that we can measure customer satisfaction and recurring defect drivers.

## Implementation Decisions

### 1. Architectural Strategy & Multi-Model Pluggability
- **Core Abstraction (ChatClient / ChatModel Factory / Strategy):** The main judge service (EmailResolutionJudgeService) will not be hardcoded to a single static ChatModel. Instead, it will delegate model execution through a configurable ChatClient factory or a model resolver strategy pattern (LlmModelResolver / ChatClientProvider).
- **Configuration-Driven Model Selection:**
  - Default model provider, model name, and inference parameters (e.g. `temperature`, `timeout`, `max-retries`) will be configurable in `application.yml`.
  - The service will accept optional execution options (e.g. `ModelOptions` / `ModelId`) to allow callers to override the model per evaluation request.
- **Spring AI Framework Integration:** Leverages Spring AI's unified `ChatClient` / `ChatModel` abstraction and structured output converters (`entity(ResolutionVerdict.class)`).

### 2. Core Taxonomy & Type Contracts

```
ResolutionStatus:
  - RESOLVED: Root issue addressed and confirmed or finalized with successful outcome.
  - WORKAROUND: Temporary bypass/mitigation applied; underlying issue remains open.
  - UNRESOLVED: Issue is ongoing, failing, blocked, or explicitly escalated without fix.
  - ABANDONED: Conversation halted without resolution confirmation; customer or agent non-responsive.
  - UNKNOWN: Inconclusive context or unclassifiable content.
```

```
ResolutionVerdict:
  - status: ResolutionStatus
  - confidenceScore: double (0.0 to 1.0)
  - rationale: String (Chain-of-thought analysis of the email progression)
  - keyEvidence: List<String> (Direct quoted sentences from the thread turns)
  - rootCauseSummary: String (Brief synthesis of identified issue)
  - finalCustomerSentiment: String (e.g., SATISFIED, FRUSTRATED, NEUTRAL)
```

```
JudgeOptions:
  - modelName: Optional<String> (LLM model identifier to use, or default if empty)
  - temperature: Optional<Double> (Evaluation determinism control, default: 0.0)
```

### 3. Chronological Transcript Formatting
- The judge service will serialize `EmailMessage` and its ordered `turns` into a standardized prompt structure:
  - Email header context (Subject, Initial Sender, Recipient).
  - Chronologically ascending turns (Turn 1 to Turn N) with turn number, sender identity, timestamp, and sanitized body.

### 4. Prompt & Rubric Engineering
- System prompt incorporates strict rubrics and few-shot disambiguation rules to clearly differentiate subtle scenarios (such as Workaround vs Permanent Resolution, and Inactive/Abandoned vs Blocked/Unresolved).
- Enforces reasoning generation prior to classification output to maximize evaluation accuracy.

### 5. API and Service Surface
- Java Service Interface:
  - evaluate(EmailMessage email): ResolutionVerdict
  - evaluate(EmailMessage email, JudgeOptions options): ResolutionVerdict
- REST Controller Seam:
  - POST /api/judge/evaluate: Accepts email text or structured email payload, with optional query/header params for model selection, returning ResolutionVerdict.

## Testing Decisions

- **Single High-Level Seam Testing:** Test at the service boundary (EmailResolutionJudgeService) and integration boundary (ApiController).
- **External Behavior Verification:** Tests will assert on structured contract invariants (valid enum values, non-empty rationale, score bounded in [0.0, 1.0]) rather than mocking prompt internals.
- **Dataset Ground-Truth Benchmark Suite:** Integration tests will run against the existing 48+ email fixtures in dataset/ (evaluating parsed .eml files against the known labels RESOLVED, WORKAROUND, UNRESOLVED, ABANDONED) to measure precision and recall across model configurations.
- **Mock Model Test Harness:** Unit tests will utilize simulated/mocked ChatModel responses to verify fallback handling, error scenarios, and multi-model routing without incurring live API costs.

## Out of Scope

- Real-time IMAP/POP3 mailbox syncing and polling (ingestion operates on provided email payloads or parsed .eml files).
- Automated ticket auto-closing execution in external ticketing systems (Jira, ServiceNow) — service acts strictly as the evaluation engine.
- Fine-tuning custom LLM foundation models.

## Further Notes

- The design aligns with Spring AI 2.0 / Spring Boot 4.1.0 conventions already present in the workspace.
- The prompt rubric can be extended in future iterations to support custom domain taxonomies (e.g., security incident severity, compliance breaches) via pluggable rubric templates.
