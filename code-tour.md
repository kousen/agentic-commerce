# MockHub code tour — the merchant side, at production weight

The `examples/` directory shows each pattern at its smallest. This tour shows where the
same patterns live in MockHub — the real Spring Boot 4 / Java 25 codebase behind every
demo in the course — with the load-bearing lines cited. Read it with the source open:
`github.com/kousen/mockhub`, pinned at commit **`775be23`** (2026-08-05, the build with
the fee-inclusive mandate fix). All paths are repo-relative; line numbers drift, names
mostly don't.

Counted at that commit: **1,272 backend tests** across 154 test classes, 528 frontend
specs. The repo's own `docs/agentic-commerce.md` (~600 lines) covers this ground in
prose and is worth a read on its own.

## The layout, in one paragraph

The backend is vertical feature slices under `com.mockhub`, each with its own
`controller`/`service`/`entity`/`repository`. The marketplace core — `event`, `ticket`,
`cart`, `order`, `payment`, `venue`, `auth` — predates the agent work. The agentic layer
sits **alongside it as peers, not as a wrapper**: `mandate`, `agentapproval`,
`agentrisk`, `agentpurchaseevidence`, `paymentcredential`, `eval`, `commerce`, plus
three protocol adapters (`mcp`, `acp`, `a2a`). That arrangement is itself the lesson:
the agent surface *reuses* `CartService` and `OrderService` rather than duplicating
them. Enabling your site for agents is mostly additions, not a rewrite.

---

## Step 1 — Discovery

Two documents, two serving mechanisms:

- **`/llms.txt`** is a static file: `backend/src/main/resources/static/llms.txt`,
  permitted anonymously at `SecurityConfig.java:81`. Maintained prose — which is why
  drift is its failure mode (see below).
- **`/.well-known/agent.json`** is *generated*: `a2a/controller/AgentCardController.java:34`
  builds the card from the code that serves the API — interfaces, OAuth metadata URL,
  and four skills with example utterances (`buildSkills()`, `:67–114`). The base URL is
  configuration (`mockhub.public-base-url`), so dev and prod cards are both truthful.

A live drift example the course can point at: `llms.txt` says the DCR endpoint is
`POST /connect/register`, while a stale javadoc in `McpOAuth2SecurityConfig.java:104`
says `/oauth2/register`. Generated documents can't disagree with the code; maintained
ones eventually do.

---

## Step 2 — Connectivity (tool design)

The MCP tool surface lives in `mcp/tools/` — one `@Tool`-annotated Spring AI class per
concern (`MandateTools`, `OrderTools`, `CartTools`, `AgentApprovalTools`,
`PaymentCredentialTools`, `AgentRiskTools`, `AgentPurchaseEvidenceTools`). Notice the
descriptions steer the model: `findTickets` says "RECOMMENDED … reduces round-trips
from 3 to 1", and `proposePurchase`'s description states out loud that approval "is
human-only, on the MockHub website — there is no MCP tool for it." Tool descriptions
are the interface; MockHub writes them for a reader that has never seen the domain.

---

## Step 3 — Identity

**The MCP path has two modes, and only one binds identity.** The switch is the
`mcp-oauth2` Spring profile:

- Without it: `mcp/McpApiKeyFilter.java:26` checks a single static `X-API-Key` — that
  authenticates the *deployment*, not a person.
- With it (production): `mcp/config/McpOAuth2SecurityConfig.java:75` stands up an
  embedded authorization server with Dynamic Client Registration (`:112`) and a
  resource server validating Bearer JWTs on `/mcp/**` with audience checks (`:153–170`).

**The pinning point** is `McpAuthenticatedEmailFilter` (`:62–73`): it takes the token
subject — the user's email — from the security context and stores it per-request,
cleared in a `finally`. Then every tool funnels through one method,
`ChatContext.resolveEmail` (`ai/service/ChatContext.java:36–45`): if an authenticated
email is pinned, the caller-supplied `userEmail` parameter is **ignored**. Fourteen
call sites. The parameter still exists in the tool schema — the model can type whatever
it likes, and the server simply doesn't use it. That is §1.3's whole lesson in one
method: *anything the agent can type is an assertion, not a credential.*

The filter's own javadoc names the threat it closes: without it, any token holder could
"act as — or mint mandates and payment credentials for — a different user simply by
passing that user's email."

**The contrast case — deliberately.** The ACP path does *not* bind identity from its
credential: `acp/AcpApiKeyFilter.java` validates a shared static key, and the buyer is
named by an `X-Buyer-Email` header (or `buyerEmail` body field) — a request parameter.
The repo documents this as a trusted-operator model (`docs/agentic-commerce.md:354`).
Teaching beat: put the two side by side and ask which one your site is accidentally
running. (The course's §1.3 confused-deputy demo works precisely because a naive
identity-as-parameter path exists to demo against.)

**Two credential systems, never on the same request.** The human's
`JwtAuthenticationFilter` explicitly excludes `/mcp` (`auth/security/JwtAuthenticationFilter.java:34–37`).
Human tokens: 15-minute access / 7-day refresh. Agent tokens: 8-hour access / 60-day
rotating refresh — with a comment worth quoting for why the generous window is safe:
*"Spending authority is bounded by mandate limits and expiry, not token lifetime."*
(`McpOAuth2SecurityConfig.java:88–94`.)

One asymmetry to name: `agentId` is self-asserted in every mode — agents name
themselves. Safe, because a mandate lookup matches on `agentId` **and** the resolved
`userEmail`, so an agent cannot borrow another agent's mandate.

---

## Step 4 — Authorization (the mandate)

**The store.** `mandate/entity/Mandate.java` (migrations V22 + V32): `mandateId` UUID,
`agentId`, `userEmail`, `scope` (BROWSE|PURCHASE), `maxSpendPerTransaction`,
`maxSpendTotal`, `totalSpent`, allowed categories/events/sections, `approvalMode`
(default AUTO_PURCHASE), `status`, `expiresAt`, `revokedAt`.

**Creation, two doors.** REST `POST /api/v1/my/mandates` takes the owner from the
authenticated principal — the body has **no email field at all**
(`mandate/controller/MandateController.java:44–48`). The MCP `createMandate` tool
routes `userEmail` through `ChatContext.resolveEmail`. Creation-time validation speaks
to a model: unknown category slugs come back with *"Categories are event types, not
genres"* and the legal values (`MandateService.java:315–318`) — the `examples/purchase-profile`
lesson, running in production.

**Enforcement.** `eval/condition/MandateCondition.java` requires an agentId, requires a
mandateId for purchase-scope actions, finds the active non-expired mandate, and
delegates to `MandateService.validateMandateConstraints` (`:210–263`): scope (PURCHASE
subsumes BROWSE), per-transaction ceiling, projected cumulative spend
(`totalSpent + amount`), then the allow-lists. A section-restricted mandate **fails
closed** when no section context is supplied (`:250–254`).

**The units fix — one bug, one type, four call sites.** `order/dto/OrderPricing.java`
(added 2026-08-05) is the single place the 10% fee is applied; its javadoc states the
bug it killed: *"mandates were validated against the subtotal while buyers were charged
the total, so a $35.00 ceiling silently authorized a $38.50 purchase."*
`totalForSubtotal()` is now the only authorization basis, at `AcpCheckoutService.java:333`
and `:370`, `OrderTools.java:399`, and `CartTools.java:101` — each carrying a comment
naming the invariant. This is the §2.2 slide's receipts.

**Re-authorization at completion.** `AcpCheckoutService.completeCheckout` calls
`ensureOrderStillAuthorizedForConfirmation` (`:213`), with a comment that is the whole
argument: *"Last line of defence before money moves … a mandate could be revoked or
outgrown between the two calls."* An already-confirmed checkout short-circuits first
(`:202–205`) so a retry doesn't double-count spend — the idempotency and accounting
concerns meet on exactly this line.

**Spend accounting.** Recorded on confirmation (`OrderService.java:143–145`), reversed
on cancellation (`:202–204`), floored at zero — under a **pessimistic row lock**
(`MandateRepository.findByMandateIdForUpdate`, `@Lock(PESSIMISTIC_WRITE)`). Get this
wrong and a $1,000 limit quietly becomes $3,000; MockHub's answer is boring,
deliberate locking, not cleverness.

**A gap, on purpose left visible:** the MCP `revokeMandate` tool calls the
single-argument service overload, which performs **no ownership check** — any valid MCP
credential can revoke any mandate by ID. The REST endpoint checks. "Where would you
tighten this?" is a better slide than pretending it isn't there.

---

## Step 5 — Guardrails and approval

**The eval framework.** `EvalRunner` runs every applicable `EvalCondition` at each
state transition (`eval/service/EvalRunner.java:25–35`); nine conditions ship, with
kebab-case names that appear verbatim in refusals: `mandate-authorization`,
`event-in-future`, `listing-active` (all CRITICAL), `spending-limit`,
`price-plausibility` (WARNING), `agent-risk` (WARNING, or CRITICAL once the block
threshold trips). Only CRITICAL blocks. They run at `addToCart`
(`CartTools.java:103`), `checkout` (`OrderTools.java:115`, `:131`), `confirmOrder`
(`OrderTools.java:230`), and the ACP equivalents.

**Refusals are structured and relayable** — `"Cannot checkout: " + condition + ": " + message`.
The real mandate refusal, verbatim (`MandateCondition.java:65–68`):

> `Mandate does not authorize this action (scope=PURCHASE, amount=44.61, mandateId=…)`

Note what it *doesn't* include: the ceiling. The limit appears only in server logs.
Defensible as leak-minimization, questionable as agent UX — Lab 2 has students write
the refusal the other way (amount *and* cap), precisely so the room can argue about
which is right. WARNING failures don't block; they ride along in a `warnings[]` array
next to the successful payload — advisory-vs-hard-stop in one response shape.

**Risk signals.** `agentrisk/` records deterministic local signals (mandate mismatches,
failed checkouts, rapid cart holds, high-spend attempts) — written with
`REQUIRES_NEW` propagation (`AgentRiskService.java:71`) so they **survive the rollback
of the action they describe**. A naive implementation loses exactly the records it most
needs. Three mandate mismatches in 24 hours flips `blocked=true`
(`summarizeRisk`, `:301–305`), which `AgentRiskCondition` turns into a CRITICAL
refusal. An adversarial bot dressed as an agent fails the first check — it has no
mandate to mismatch against.

**The approval the agent cannot reach.** The history is in the git log: commit
`2acc16c` (2026-07-25), *"Human-facing purchase approval page; approval is now
web-only"* — its message admits the original design was self-defeating:
`approvePurchase`/`denyPurchase` were MCP tools, and under OAuth the agent acts as the
pinned user, *so the agent that proposed a purchase could approve its own proposal
in-band*. That commit is the §2.4 demo's real-world twin.

What replaced it: `proposePurchase` (MCP, `mcp/tools/AgentApprovalTools.java:34`)
creates a durable proposal with amounts, rationale, and expiry; approval and denial are
REST-only (`agentapproval/controller/AgentPurchaseApprovalController.java:79–97`),
guarded by the human's authenticated principal plus a service-level ownership check
under a row lock, valid only from status PROPOSED, expiry enforced. A test —
`McpConfigTest` — asserts the approve/deny tools are **never registered**: the security
invariant is pinned by CI, not convention. At completion,
`validateApprovedForCompletion` re-checks that agent, mandate, and the *exact total*
match what the human approved — the proposal expires and the total cannot drift.

The frontend badge (`/my/approvals`, `frontend/src/pages/ApprovalsPage.tsx`) is plain
30-second polling — no push, no WebSocket. The boundary is what matters; the transport
is boring on purpose.

---

## Step 6 — Payment authority

`paymentcredential/` is the scoped credential: user, one named agent, merchant
(hardcoded MOCKHUB), max amount, currency, ONE_TIME|REUSABLE, status
(ACTIVE|CONSUMED|REVOKED|EXPIRED), expiry. Deliberately **separate from mandates** —
may-act and may-pay are different questions with different artifacts, and issuing a
credential does not consult any mandate.

"Consumed exactly once" is the same boring answer as spend accounting: a pessimistic
row lock (`PaymentCredentialRepository.findByCredentialIdForUpdate`) plus a status
check inside it (`PaymentCredentialService.java:109–112`) — with an idempotency
carve-out: a CONSUMED credential re-presented for the *same order number* passes, so a
retry after a lost response isn't punished. Consumption happens **before** payment
confirmation, and an integration test proves a payment failure rolls the consumption
back. Expiry runs twice — lazily on the hot path and via a 15-minute background sweep
(`LifecycleCleanupService.java:177–180`) for rows nobody touches again.

Gap worth naming: credentials are MCP-issued and MCP-revoked only — there is no web
page where the human can see and revoke them, unlike mandates. Same "where would you
tighten this?" move.

---

## Step 7 — Evidence

`agentpurchaseevidence/AgentPurchaseEvidenceService.assembleEvidence` (`:116–144`)
builds the record from the domain rows: order and items, mandate (with computed
remaining budget), the approval, the consumed payment credential, ACP-aligned checkout
status, risk signals from a ±15-minute window, derived eval outcomes, and the **actor
timeline** — MANDATE_CREATED (USER), PURCHASE_APPROVAL_PROPOSED (AGENT),
PURCHASE_APPROVAL_APPROVED (USER), ORDER_CONFIRMED (AGENT or USER, decided by whether
`orders.agent_id` is set)…

Two honest design notes the course should say out loud:

1. **Actor attribution is derived at read time**, from timestamps on the domain rows —
   there is no persisted actor column and no append-only event table. It works because
   each step's actor is structurally knowable (only a human can approve; only the ACP
   and MCP paths write `agent_id`). The field-note history matters here: an earlier
   version hardcoded the mandate actor to USER, making an agent-minted mandate
   indistinguishable from a user-granted one — the single most important fact in a
   dispute record, and the schema couldn't represent it.
2. **What isn't persisted is marked, not invented.** Email/SMS provider receipts aren't
   stored, so the evidence record reports those dispatch fields as `NOT_PERSISTED`
   instead of synthesizing plausible IDs. Evidence must be checkable; an evidence
   record that fabricates is worse than a gap.

Access: REST `GET /api/v1/orders/{orderNumber}/agent-evidence` (owner or admin), and
the MCP `getAgentPurchaseEvidence` tool — which **redacts the signed QR/ticket URLs**
before returning (`AgentPurchaseEvidenceTools.java:51–93`), so the audit channel can't
become a ticket-exfiltration channel.

**Idempotency, merchant-side.** `orders.idempotency_key` with a partial unique index
(`V16`, `WHERE idempotency_key IS NOT NULL`); the retry short-circuit runs before any
cart work and returns the original order (`OrderService.java:89–95`). REST reads the
key from the `Idempotency-Key` header; ACP takes it as a body field. Known gap: the
concurrent-duplicate race (two same-key requests both missing the lookup) falls through
to a raw constraint violation instead of returning the original order — the DB
guarantee holds, the graceful path doesn't. Take-home: find it, fix it, and note that
the lab's retry test wouldn't catch it. Concurrency bugs need concurrent tests.

---

## Where each course artifact touches this code

| Course artifact | MockHub counterpart |
|---|---|
| `examples/discovery/` | `static/llms.txt`, `AgentCardController` |
| `examples/mandate-check/` | `MandateCondition` + `MandateService.validateMandateConstraints` + `OrderPricing` |
| `examples/purchase-profile/` | mandate creation validation (`MandateService.java:295–321`) |
| `examples/idempotency/` | `OrderService.checkout` (`:89–95`) + V16 partial unique index |
| `examples/injection-filter/` | every authorization path — free text is structurally absent from eval inputs |
| Lab 1 (mandate boundary) | ACP checkout + `AgentPurchaseApprovalController` auth guards |
| Lab 2 (guarded tool) | `OrderTools`/`CartTools` eval gating, refusal phrasing (yours names the cap; MockHub's doesn't — argue it out) |
| `course-client/` sourcing | the buyer-side mirror of `compareTickets`' reasons-not-rows philosophy |

*Facts in this tour were verified against `main@775be23` on 2026-08-07. MockHub has no
git tags yet; cutting `course-2026` as the frozen release remains on Ken's list
(`mockhub-course-requirements.md`).*
