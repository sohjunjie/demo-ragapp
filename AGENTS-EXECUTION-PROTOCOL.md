# Agents execution protocol

This protocol defines standard operational requirements, execution steps, and workspace verification procedures for all codebase modifications, feature implementations, refactoring tasks, bug fixes, and knowledge-graph synchronization.

---

## 1. Universal Execution Protocol

All files modifications must strictly adhere to the following sequence:

### Phase 1: Mandatory Best Practices
- **Consult Coding Best Practices & Past Learnings**: **MANDATORY**: Before formulating a plan, designing architecture, or modifying code for ANY task, open and read [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md) and [`AGENTS-IMPROVEMENTS-PROTOCOL.md`](./AGENTS-IMPROVEMENTS-PROTOCOL.md). Review all documented operational principles, architecture/UI guidelines, and TypeScript/testing best practices (e.g. cross-cutting utility decoupling, WCAG AA dynamic contrast, and Windows CLI execution). Actively apply these best practices to the current task to prevent recurring defects.
- **Graphify Query Over File Listing**: If `graphify-out` exist, prioritize using the graphify skill for all codebase or architecture question over listing all files

### Phase 2: Execution & Verification
- **Direct Workspace File Operations**: Perform code changes, file creations, and structural updates directly in the workspace, applying the consulted best practices.
- **Empirical State Verification**: Immediately inspect directory contents (`list_dir`) and modified file structures (`view_file`) after writing changes to guarantee file existence, correct paths, and accurate byte sizes.

### Phase 3: Graphify Synchronization & Build Verification
- **Synchronize Knowledge Graph**: Run `graphify update .` whenever code files have been edited, added, or deleted (skip if `graphify-out` is not initialized).
- **Run Verification Suite**: Execute project build scripts, type checks, or test suites (e.g., via `cmd /c "<build-command>"` on Windows) to confirm zero compilation errors.
- **Enforce Zero-Regression Guarantee**: Ensure existing API contracts, exports, and module dependencies remain unbroken.

### Phase 4: Self-Improvement & Continuous Learning
- **Conduct Continuous Self-Improvement**: Follow the continuous improvement feedback loop in [`AGENTS-IMPROVEMENTS-PROTOCOL.md`](./AGENTS-IMPROVEMENTS-PROTOCOL.md). Apply the Generalizability Filter (pruning one-offs/micro-rules) and route any newly identified generic best practices or operational principles into the proper section of [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md).

---

## 2. Pre-Completion Checklist

Before marking any task as complete, verify:

- [ ] **Pre-Task Best Practices Consultation**: [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md) and [`AGENTS-IMPROVEMENTS-PROTOCOL.md`](./AGENTS-IMPROVEMENTS-PROTOCOL.md) were read and all relevant coding best practices were applied to the implementation.
- [ ] **State Discovery**: All created or modified files exist on disk with valid non-zero content.
- [ ] **Issue & Spec Artifact Synchronization**: Originating issue tracking files (e.g., in `.scratch/**/issues/*.md`) have all completed acceptance criteria checked off (`[x]`) and status updated to `done`.
- [ ] **Clean Syntax & Imports**: No broken imports, syntax errors, or unhandled file path resolution issues exist.
- [ ] **Graphify Sync**: `graphify update .` was executed following file changes, and `graphify-out/GRAPH_REPORT.md` is up to date (n/a if `graphify-out` not initialized).
- [ ] **Build Verification**: Project build/test suite executes cleanly (Exit Code 0).
- [ ] **Self-Improvement Review**: Continuous improvement feedback loop in [`AGENTS-IMPROVEMENTS-PROTOCOL.md`](./AGENTS-IMPROVEMENTS-PROTOCOL.md) was executed, applying the generalizability filter and properly categorizing any new lessons in [`AGENTS-LEARNINGS.md`](./AGENTS-LEARNINGS.md).
