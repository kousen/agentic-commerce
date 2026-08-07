# course-client — the buyer's side, as code

The course's Module 3 §3.4 reference: what a **trustworthy buyer's representative** does
that no protocol does for it. Spring Boot 4 / Spring AI 2 (the instructor stack), Java 21+.

The heart is deliberately **not an LLM**:

- [`SourcingPolicy`](src/main/java/client/SourcingPolicy.java) — the sourcing contract,
  implemented: search *every* provider (failures disclosed, never skipped), normalize,
  deduplicate the same physical seat across providers, rank against *declared*
  preferences in the customer's units (all-in total), select, disclose. Includes the
  checkable postcondition as a method — *the selected listing is the minimum-cost
  listing satisfying the profile, or an exception record explains why not.*
- [`EvidenceRecord`](src/main/java/client/EvidenceRecord.java) — the record that answers
  "why did you buy that?": providers searched, candidates considered, ranking policy,
  profile version, selection, reasons, exceptions.
- [`MockHubProvider`](src/main/java/client/MockHubProvider.java) — a provider over
  MockHub's ACP listings feed; two instances play the TicketNexus/SeatStream pair from
  the demos (identical inventory, so dedup is exercised for real).
- [`agent/BuyingAgent`](src/main/java/client/agent/BuyingAgent.java) — the Spring AI
  wiring: the *same* policy exposed to a model as a `@Tool`. The model converses; the
  policy decides; the tool cannot purchase. Structure crosses the boundary — authority
  doesn't.

## Run it

```bash
./gradlew test        # the sourcing contract, offline, one sentence per test
./gradlew bootRun     # live two-provider sourcing against hosted MockHub, no AI, no keys
./gradlew bootRun --args="'Monster Jam' 45.00 'Lower Level'"

# the agent variant (requires an Anthropic API key):
ANTHROPIC_API_KEY=... SPRING_PROFILES_ACTIVE=agent ./gradlew bootRun
```

The default run prints the evidence record from a real cross-provider search —
including, typically, an exception record ("no listing in preferred section under
budget; fell back to lowest total cost"), which is the disclosure discipline the
course argues for: deviations go on the record, not into silence.

## Python / TypeScript

The policy is ~150 lines of dependency-free logic — port the *sentences*, not the Java.
The tests state the contract one sentence per test; re-implement them with the official
MCP SDKs and your HTTP client of choice:

- Python: https://github.com/modelcontextprotocol/python-sdk
- TypeScript: https://github.com/modelcontextprotocol/typescript-sdk

The take-home exercise in `labs.md` frames this as a behavior spec.
