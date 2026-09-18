# Agents Learnings

This document records agent self-reflections, post-mortem root causes from past executions, and generic software engineering / operational best practices.

---

## 1. Post-Mortem & Agent Operational Principles

- **Spring AI Cloud Auto-Configuration Guards**: External AI starter dependencies often trigger mandatory credentials validation during full application context bootstrapping. Always configure placeholder or mock credentials in test profile properties to prevent context creation failures during unit and integration test phases.

---

## 2. Architecture, Styling & UI Best Practices


---

## 3. Java, Testing & Algorithm Best Practices

- **Optional Bean Injection with Fallback Instantiation**: When creating reusable service components that rely on optional standard utilities (such as serialization mappers or output converters), use dependency providers with graceful local instance instantiation fallbacks to ensure decoupled execution across both minimal slice tests and complete container environments.
