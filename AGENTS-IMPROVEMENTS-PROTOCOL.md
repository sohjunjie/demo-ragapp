# Agents Improvement Protocol

This protocol defines the continuous improvement framework, categorization taxonomy, and quality gating for updating [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md).

---

## 1. Continuous Improvement Feedback Loop

Apply this feedback loop upon completion of every task (after build/test verification succeeds and before presenting results):

1. **Reflect on Trajectory & Anomalies**:
   - Evaluate whether any execution drift, state assumption errors, layout defects, or testing bottlenecks occurred.

2. **Apply the Generalizability Filter (Pruning Rule)**:
   - **Generic Best Practices ONLY**: Persist learnings that represent reusable software engineering, architecture, testing, accessibility, or agent execution patterns.
   - **Reject One-Off / Micro-Rules**: Do NOT record library-specific micro-tweaks (e.g. fixed pixel constants, tool-specific quirks, or one-off workarounds). If an item applies only to a single isolated component, do not persist it.

3. **Route into Proper Section of [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md)**:
   - **Section 1: Post-Mortem & Agent Operational Principles**
     - Execution bottlenecks, shell/CLI invocation rules, tool usage strategies, and filesystem state verification habits.
   - **Section 2: Architecture, Styling & UI Best Practices**
     - Cross-cutting utility decoupling, WCAG accessibility rules, responsive design paradigms, CSS layout/flex chains, and UI interaction patterns.
   - **Section 3: Java, Testing & Algorithm Best Practices**
     - Interface contracts, Angular DI test compatibility, isomorphic test guards, and resilient algorithm/loop patterns.

4. **Conduct Recursive Self-Improvement**:
   - When you update [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md), also consider updating this document to improve the way that you improve.

---

## 2. Learning Entry Quality Criteria

When documenting a learning in [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md), ensure it satisfies:
- **Concise & Principle-Driven**: Formatted as `- **Concept Name**: Direct explanation of the root cause, mechanism, and actionable preventive pattern.`
- **Immediately Actionable**: Provides concrete guidance that any agent or developer can follow to avoid recurring mistakes across diverse tasks.
- **Code-Free / Principle-Focused**: Express concepts, mechanisms, and preventive actions through descriptive principles and patterns rather than embedding raw code snippets or syntax examples
