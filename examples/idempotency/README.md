# The idempotency key

*Module 2, §2.5's consequence. Checklist line: "idempotency on every state-changing
tool."*

```bash
java IdempotencyKey.java
```

Two things made this mandatory rather than polite in 2026:

1. **MRTR re-issues the same call.** The 2026-07-28 elicitation flow completes an
   approval by re-sending the original `tools/call` with the answers attached — so your
   purchase tool *will* receive the same request twice as normal control flow.
2. **Agents retry in milliseconds.** A human who loses a response clicks once, later.
   An agent re-fires immediately, and a duplicate order is a liability, not a bug
   report.

The pattern is one map lookup: same key → return the **original** order; side effects
(order creation, mandate spend accounting) happen exactly once. The self-check proves
the sentence from the lab's take-home extension: *the ceiling holds under retry.*

The in-memory map stands in for the production version — a database unique constraint
on the key column, so two racing requests can't both miss the cache and both insert.
MockHub's checkout endpoint accepts `idempotencyKey` in the request body and returns
the original order on a retry (201 with the same order — documented behavior; see
`code-tour.md`).
