# examples/ — the artifact behind each checklist line

Every principle in the course ships with the code that implements it. Each directory here
is one pattern, small enough to read in two minutes, annotated, and runnable where that's
cheap. These are excerpts *of the ideas* in MockHub, simplified to their load-bearing
parts — `code-tour.md` shows where each one lives in the real codebase, at full
production weight.

| Directory | The checklist line it backs | Run it |
|---|---|---|
| [`discovery/`](discovery/) | Be discoverable — llms.txt and an agent card | (documents, not code) |
| [`mandate-check/`](mandate-check/) | Boundaries evaluate in deterministic code, in the customer's units | `java MandateCheck.java` |
| [`purchase-profile/`](purchase-profile/) | Let the model produce structure, never authority | `java PurchaseProfileBoundary.java` |
| [`idempotency/`](idempotency/) | Idempotency on every state-changing tool | `java IdempotencyKey.java` |
| [`injection-filter/`](injection-filter/) | Free text never widens a mandate | `java FreeTextBoundary.java` |

The Java files are single-file programs — any JDK 21+ runs them directly with
`java <File>.java`, no build tool, no dependencies. Each ends in a `main` that
self-checks the pattern with assertions that read as English; if it prints `OK`,
the contract held.

They are written as **patterns, not a framework**: port them to your own stack by
keeping the sentences true, not by keeping the code identical.
