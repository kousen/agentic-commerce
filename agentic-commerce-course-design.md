# Agentic Commerce: Four-Hour Course Design

**Format:** O'Reilly Learning Platform, live online, 4 hours
**Platform:** MockHub (frozen course release) + course client + student-built guarded tool
**Prepared:** August 1, 2026
**Revised:** August 7, 2026 (v2 — merchant-side restructure around the Front Door)

---

## Why v2 exists

Ken's review of the v1 materials (2026-08-07): a developer with web experience, AI
experience, and MCP familiarity who wants to **enable their site for agentic commerce**
would leave the v1 course with eight true principles and no idea what to build. v1 taught
*why naive agentic commerce fails*; it never taught *how to build agentic commerce*. The
specs were footnoted into a handout, and the two deliverables that carried implementation
content (`examples/`, `course-client/`) were approved but never built.

The fix, decided by Ken 2026-08-07:

1. **Merchant-side first.** The student's seat is "enable my site for agents." Agent-side
   material survives where it motivates a merchant-side decision, plus one compact
   buyer's-side segment.
2. **The Front Door is the spine.** The talk's build sequence — discovery → connectivity →
   identity → authorization → guardrails → payment → evidence — becomes the course
   structure. Each failure demo is embedded at the step it motivates.
3. **The Vocabulary returns to slides.** A real segment: each spec — who's behind it, why
   it exists, what layer it occupies, status — plus the layering diagram. The v1
   "spec-complexity firewall" (acronyms never in headlines) is retired; `spec-map.md`
   remains the take-home detail.
4. **All implementation deliverables get built:** `examples/`, `course-client/`, a second
   lab (student-built guarded MCP tool), and a MockHub merchant-side code tour. In-class
   if time allows; the materials stand on their own afterwards regardless.

What v1 got right and v2 keeps: the thesis, the three module theses (they survive the
inversion — each module now *builds* toward its thesis instead of only breaking toward
it), the six demos, the mandate-boundary lab, the setup checkpoint, recorded fallbacks,
the cut-list discipline, hosted MockHub, polyglot lab tracks.

---

## Thesis

Giving an agent tools is easy. Giving it bounded authority to conduct commerce safely is
the real engineering problem.

Every module still ends on a slide that says its sharpened version out loud:

- **Module 1:** An auditable capability boundary is not an authority boundary.
- **Module 2:** Authority comes from artifacts the model cannot mint; an approval the agent can invoke is not an approval.
- **Module 3:** A trustworthy transaction is a policy you write, not a property of the protocol.

These three slides double as chapter boundaries for the recording. In v2 each thesis is
**earned twice**: the module builds the thing, a demo breaks the naive version of it, and
the corrected build is shown as code the student takes home.

---

## Design constraints

**The student leaves able to build.** The test for every segment: after it, can the
student name the artifact they would add to their own site, and have they seen its
implementation (in `examples/`, the code tour, or a lab)? A principle without its
artifact is v1's failure mode; don't backslide.

**This is still not the talk at four times the length.** The talk demonstrated a finished
system; the course walks students through building one, with the failure demos supplying
the *why* at each step. The arc per step is *build the naive version → watch it fail →
build the correction*.

**Four hours is about three hours of instruction.** Breaks, questions, setup friction,
and demo recovery consume the rest. The plan budgets ~215 instruction minutes; the cut
list is what makes overrun survivable. Timings are planning estimates, not commitments.

**Lab 1 is protected; Lab 2 is pre-cut.** The mandate-boundary lab (deterministic,
credential-free, seconds to run) keeps its protected slot. The new guarded-tool lab is
scheduled late with an explicit fallback Ken has already approved: if time runs out, it
converts to a guided take-home with an in-class kickoff, without drama.

**Every live agent demo needs a recorded fallback.** Unchanged from v1.

**Specs are taught, not feared.** The v1 firewall overcorrected. The Vocabulary segment
teaches why each spec exists and what layer it occupies; implementation pointers (which
SDK, which library, what you actually write) appear where each spec becomes concrete.
Depth beyond that still lives in `spec-map.md` — the handout absorbs detail, no longer
the entire topic.

**No client names on course slides.** The Front Door structure originates in the
TicketNetwork talk; the course version is MockHub-only framing. The talk directory stays
out of the public repo.

---

## Timing at a glance

| | Segment | Minutes |
|---|---|---|
| 00:00 | Opening: the inversion, and the reorder that isn't | 15 |
| 00:15 | Setup checkpoint (one command, paste output in chat) | 5 |
| 00:20 | **Module 1** — The front door: discovery, connectivity, identity | 55 |
| 01:15 | Break | 10 |
| 01:25 | **Module 2** — Authorization and approval | 65 |
| 02:30 | Break | 10 |
| 02:40 | **Module 3** — Payment, evidence, and the policy you write | 55 |
| 03:35 | Close + Q&A | 20 |
| 03:55 | *Slack* | 5 |

Slack is thinner than v1 (5 vs 15). That is deliberate: Lab 2's in-class execution is the
designated pressure-relief valve, and its take-home conversion is pre-approved. The cut
list restores slack the moment the clock demands it.

---

## Opening (15 min) — The inversion, and the reorder that isn't

Two framings, both kept short.

**The reorder that isn't** (from v1): "I'm out of razor blades. Find my last order and do
it again" → safe because of **evidence** and **reversibility**. "Buy tickets like last
time" → same sentence shape, every safety property gone. Land the wall-poster rule:
**delegated authority must scale with reversibility, not just with confidence.**

**The inversion** (from the talk, compressed): thirty years of bot defense assumed a bot
is an adversary; a delegated agent announces itself and carries proof of who it works
for. The industry scoreboard — every shipped ticketing integration stops at the checkout
boundary, and that is a legal/authority decision, not an engineering gap. The card
networks have already crossed the line (Visa Intelligent Commerce, Mastercard Agent Pay,
live in 2026). The question the course answers: **what do you have to build so that an
agent can cross the checkout boundary on your site, safely?**

Amazon Auto Buy grounding slide stays (a mandate specification in customer-service
prose).

*Cut candidate: the Alexa naming discussion — still first to go.*

### Setup checkpoint (5 min)

Unchanged from v1. One command per track, paste one line into chat, surfacing setup
failures ninety minutes before Lab 1 needs setup to work. Now does double duty: Lab 2
uses the same track toolchain, so the checkpoint covers both labs.

---

## Module 1 (55 min) — The front door: discovery, connectivity, identity

**Objective:** students can make a site reachable by agents — and can explain why
reachable is not the same as safe.

### 1.0 The Vocabulary (18 min)

The segment v1 refused to teach. Six specs, one slide each, same skeleton every time —
*who's behind it · what problem it solves · what layer it occupies · status*:

- **MCP** — how an agent calls your system as a tool; Agentic AI Foundation; the
  2026-07-28 stateless revision in two bullets (audience already knows MCP — this is
  orientation, not a tutorial).
- **llms.txt + the agent card** — how an agent finds out you exist; conventions, not
  committees.
- **AP2** — proving who authorized a payment and within what limits; mandates as
  verifiable credentials; FIDO Alliance. This is where the course's mandate concept comes
  from.
- **ACP** — checkout as a REST API an agent can drive; OpenAI + Stripe; the Instant
  Checkout cautionary tale in one slide (the bottleneck was commerce plumbing, not AI).
- **UCP** — the whole journey; Google + Shopify; composes MCP/A2A/AP2; the most
  commercially live.
- **TAP + Web Bot Auth** — telling at the edge whether a robot is who it claims to be;
  the WAF answer.
- (x402 gets one line: machine metering, not retail — exists, not on our path.)

Then the one diagram that organizes the day: **the layering slide** — Discovery /
Connectivity / Identity / Authorization / Transaction / Settlement, with each spec placed
on its layer. The Front Door build sequence is this diagram read top to bottom, and the
course is that walk. Point at `spec-map.md` for depth and primary sources.

Per-spec **implementation pointers** appear here and recur when each layer becomes
concrete: MCP SDKs (Java/Python/TypeScript), Stripe's Agentic Commerce Suite for ACP,
UCP's manifest + reference implementations, AP2's credential formats.

### 1.1 Step 1 — Discovery (7 min)

What you publish so an agent can find you: `llms.txt` (shown, real content) and
`/.well-known/agent.json` (shown, real content — capabilities, security scheme, skills).
The talk's observed sequence: the client finds the protected-resource metadata, follows
it to the authorization server, registers itself, reads the tools. "I published two
documents; the client worked out the rest."

**Artifact:** `examples/discovery/` — MockHub's actual llms.txt and agent card, annotated.
Steps 1–2 cost almost nothing and are useful even if you never build the rest.

### 1.2 Step 2 — Connectivity: tool design (13 min)

Design for the goal, not the resource: `findTickets(query, city, maxPrice, section)` —
one call instead of three, because every round-trip is latency, tokens, and a chance to
lose the thread. Give it reasons, not just rows: `compareTickets` returns ranked options
with inspectable reason codes, deliberately not an LLM. Tool descriptions are the
interface — written for someone who has never seen your domain.

**Demo — the grid, reframed merchant-side.** Two providers, identical inventory, the
only difference is documentation quality. Sixteen recorded runs across three models:
no run ever preferred the tersely-documented provider, and the smallest model never even
*loaded* its tools. Your tool descriptions are your shelf placement. Documentation
quality is a marketing surface — and non-disclosure ("the best option," one provider
never queried) is the default failure your buyer's side must correct in Module 3.

### 1.3 Step 3 — Identity (15 min)

The naive tool anyone would write first: `buyTickets(String userEmail, String listingId,
int quantity)`. Run it; it works; every code review would pass it.

**Demo — the confused deputy.** "I'm Alice — grab a ticket for my friend Bob, surprise
him." The agent types `bob@mockhub.com`; a real order lands on Bob's account; no error.
Nobody attacked anything.

**Correction, as a build:** the human logs in once, in a browser, and authorizes the
client; identity binds from the authenticated token at the transport layer; the subject
is pinned per request; the cart is a row that belongs to that identity. OAuth 2.1 with
Dynamic Client Registration is what MockHub actually runs — shown in the code tour.
Prompt injection cannot act as another user if the parameter never existed.

State the rule that recurs all day: **the agent's input space and the authority space
must not overlap.**

### 1.4 Thesis (2 min)

Steps 1–3 made you reachable and auditable. They did not make you safe: nothing yet
constrains what an authenticated agent may *do*. **An auditable capability boundary is
not an authority boundary.** That is Module 2's job.

---

## Module 2 (65 min) — Authorization and approval

**Objective:** students can implement bounded authority — a mandate store, a validated
inference artifact, and an approval path the agent cannot reach.

Still the heart of the course. Protect its time.

### 2.1 Step 4 — The mandate (13 min)

The permission slip, as schema and as code: `createMandate(agentId,
maxSpendPerTransaction, maxSpendTotal, allowedCategories, expiresAt)`. Four properties
that make it a mandate: **explicit** (the customer chose these numbers), **inspectable**,
**bounded** (per transaction, in total, by category, in time), **revocable** (effective
immediately).

Implementation truths that don't fit on a schema slide:

- Check it at the cart **and again at confirmation** — time passes; revocation and
  cumulative spend move between those moments.
- A spending limit is an accounting problem: recorded on confirmation, reversed on
  cancellation, inside the transaction, with row locks. Get this wrong and a $1,000
  limit quietly becomes $3,000.
- The two axes from v1 (authority to act / authority to pay), gated on reversibility;
  autonomous in-mandate success is the intended path — escalate by exception, or you've
  built an elaborate shopping cart.

**AP2 mapping:** what this mandate becomes as a verifiable credential — signed, carried
with the transaction, there when a dispute arrives. One slide, concrete.

**Artifact:** `examples/mandate-check/` — the deterministic validation, runnable.

### 2.2 The units lesson (7 min)

Earned during the course build and kept in full: a $35 ceiling validated against the
subtotal authorized a $38.50 charge — two real orders, no attacker. **The bound must be
denominated in the units the customer meant.** Whatever number the customer says, the
boundary evaluates that number.

### 2.3 The inference must be an artifact (12 min)

**Demo — "like last time."** The agent reads real order history and anchors on a Floor
seat for *Hamilton* to pick Floor at Monster Jam — faithful to the data, inside the
mandate, wrong. No boundary was crossed because the boundary was never asked about the
right thing.

**Correction, as a build:** the LLM *proposes* a structured `PurchaseProfile`;
deterministic code validates it; the customer can inspect it; the mandate attaches to
the profile, not the raw utterance. **Let the model produce structure; never let it
produce authority** — still the most transferable idea in the course.

**Artifact:** `examples/purchase-profile/` — the proposal/validation boundary, both sides
of the line.

### 2.4 Step 5 — In-band approval, and why it fails (8 min)

**Demo — the memorable one, unchanged.** Mandate says APPROVAL_REQUIRED; the tool
surface includes `approvePurchase`; a mundane prompt ("make sure the order ends up fully
completed") walks the agent through proposing and **approving its own purchase**. Every
call legitimate, nothing jailbroken. The architecture permitted it.

**An approval the agent can invoke is not an approval.**

### 2.5 The two fixes (12 min)

Both fixes get shown, because they live on different sides of the wire:

**The merchant-side fix — the page the agent cannot call.** The approval tools come out
of the MCP server entirely; the only approval path is a page on the marketplace's own
session, with a badge that finds the human. Which agent, under which mandate, its stated
reasoning, approve or deny. Proposal expires; total cannot drift. This is buildable
today on any stack, no new spec required — shown in the code tour.

**The protocol fix — MRTR elicitation (SEP-2322).** The recorded demo, unchanged from
v1: agent hits the boundary, the question renders in the **host's** UI, the human
answers, the same call resumes with the state echoed back. Not a tool the agent can call
— the capability is not in its world. Constraints: URL mode for credentials; sampling is
deprecated; MCP Apps as the inline-card complement, one slide.

Consequence that sets up Module 3: MRTR re-issues the same call — duplicate delivery is
normal control flow, so **every state-changing tool needs an idempotency key**
(`examples/idempotency/`).

### 2.6 LAB 1 (13 min, walkthrough included) — Mandate boundary test

Unchanged from v1 in content, slightly compressed in budget (the walkthrough reference
in `labs.md` carries anyone who falls behind). Students prove the ceiling, revocation,
approval gate, and credential separation, then write the fifth test themselves: **the
agent's credential cannot mint a mandate.** Predict-then-run remains the fallback for
anyone whose setup is broken.

End on the thesis slide: **authority comes from artifacts the model cannot mint.**

---

## Module 3 (55 min) — Payment, evidence, and the policy you write

**Objective:** students can finish the build — payment authority, guardrails, evidence —
and can specify the buyer-side policy the protocol will never supply.

### 3.1 Step 6 — Payment authority (8 min)

You do not hand the agent a card number. A scoped payment credential: issued by the user
to one named agent, bounded amount and currency, expiry, one-time or reusable,
revocable, validated before money moves, consumed exactly once.

Three questions, three records: may the agent act? (the mandate) — did a human approve
this purchase? (the approval record) — may it pay with this authority? (the scoped
credential). AP2 is the spec answer for making these records portable and verifiable.

### 3.2 Guardrails, and untrusted text (12 min)

The conditions that run at every state transition, as a table: mandate, event-in-future,
listing-active, agent-risk, spending-limit — Design by Contract pointed at a caller you
don't trust. **Refuse in a sentence the agent can relay** — it will retry regardless;
tell it the constraint or it will invent one for your customer. Risk signals you could
never get from a browser: mandate mismatches, burst cart holds; repeated mismatch
becomes a block. An adversarial bot dressed as an agent fails the first check — it has
no mandate to mismatch against.

**Demo — the injection, with the honest result.** Ten recorded runs, two injection
styles, zero took the bait — said plainly. But the same runs produced the units overrun:
you don't need a malicious seller to break a budget boundary. The contract has two legs:
free text never widens a mandate (boundaries evaluate in deterministic code on
structured fields — `examples/injection-filter/`), and the bound is denominated in the
customer's units. Why you write the injection defense after ten clean runs: the seller
needs it to work once, on one model version, and the check costs nothing.

Input space vs. authority space — second appearance, closing the loop with §1.3.

### 3.3 Step 7 — Evidence (10 min)

A chargeback arrives six weeks later; "the robot did it" is not an answer. What the
record must reconstruct: who authorized the agent and within what limits, whether a
human approved this purchase, which payment authority was used and consumed, what
warnings fired, what was delivered. The actorTimeline slide — six rows, two of them
human, and that distinction is what a chargeback turns on. The v1 lesson stands:
evidence must be **checkable, not narrative**.

**Code tour moment:** how MockHub records and serves this (`getAgentPurchaseEvidence`),
plus the field-note that the actor column was once hardcoded to USER — making an
agent-minted mandate indistinguishable from a user-granted one, the single most
important fact in a dispute record.

### 3.4 The buyer's side (8 min)

The compact agent-side segment merchant-side students still need, because their
customers' agents will behave this way. Sourcing as a contract: search every eligible
provider → normalize → deduplicate → rank against declared preferences → disclose
selection and reason → purchase only within authority. Sharpened to a postcondition (the
minimum-cost listing satisfying the profile, or an exception record explains why not).
Disclosure of affiliate/ownership/preferred-provider relationships — the grid showed the
model's preference tracks documentation quality, so "the model chose it" conceals
self-preferencing. `course-client/` implements this policy; take-home.

### 3.5 LAB 2 (12 min in class, or guided take-home) — Expose a guarded tool

The construction lab: students stand up a **tiny MCP server of their own** — one
purchase-shaped tool plus one mandate check — in their lab-track language, using the
official MCP SDK. The tool refuses an over-ceiling request with a relayable reason and
honors an idempotency key. Small enough to fit the slot because the scaffold ships in
the repo and students write only the guard (the same 5–10 lines the whole course has
been circling).

**Pre-decided fallback (Ken, 2026-08-07):** if the clock says no, this converts to an
in-class kickoff (project the scaffold, write the guard live, 5 min) plus guided
take-home. The materials stand alone either way.

### 3.6 After the sale (5 min)

Compressed from v1 but kept — wrong purchase, dispute, refund against a non-refundable
listing, chargeback; where liability sits when an agent acted correctly inside a mandate
but against intent. The BOTS Act has no carve-out for delegated consumer agents; the
distinction is being built in infrastructure (TAP, AP2 mandates), not law. No clean
answers exist; say so.

End on the thesis slide: **a trustworthy transaction is a policy you write, not a
property of the protocol.**

---

## Close (20 min)

**Where to start** — the talk's six steps, now the course's summary: be discoverable →
expose read-only tools → add identity → add authorization → add transactions with
guardrails and evidence from day one → instrument everything. Steps one and two cost
almost nothing and agents are already looking.

The protocol argument, earned: MCP went stateless and removed real operational pain —
and none of the problems in this course. Production complexity moved up the stack.

The take-home checklist (v1's nine lines, unchanged — they were always right; now each
one has an artifact in the repo behind it).

Q&A.

---

## Cut list, in order

1. Alexa naming discussion (Opening) — first to go entirely.
2. Lab 2 in-class execution → kickoff + guided take-home (pre-approved conversion, not
   an emergency).
3. §3.4 buyer's side → two slides + course-client pointer.
4. §1.0 Vocabulary: ACP cautionary-tale slide and x402 line compress into the layering
   slide's narration.
5. §1.2 grid → two slides (the table and the lesson), drop the run-by-run walkthrough.
6. Lab 1 → predict-then-run walkthrough. **Only if forced;** it is the anchor hands-on
   segment.

Protected under all circumstances: §2.4 (self-approval), §2.5 (both fixes), the §1.2
confused-deputy demo, Lab 1, and the module-end thesis slides (ninety seconds total, the
course's spine).

---

## Deliverables (delta from v1)

Already built and kept: `labs/{java,python,typescript}` (Lab 1 + checkpoint), `demos/`
(six demos + grid), `slides.md` (v1 — to be rewritten to this structure), `labs.md`,
`instructor-guide.md`, `demo-runbook.md`, `spec-map.md`, `setup.md`, PDF workflow.

To build, in order:

1. **`examples/`** — the approved-but-never-built one-file excerpts, now five:
   `discovery/` (llms.txt + agent card, annotated), `mandate-check/`,
   `purchase-profile/`, `idempotency/`, `injection-filter/`. Runnable where cheap;
   each is the artifact behind a checklist line.
2. **`labs/guarded-tool/`** (Lab 2) — scaffold per track (Java/Python/TypeScript, official
   MCP SDKs); students write the guard. Needs the same English-legible assertion rule.
3. **`code-tour.md`** — the MockHub merchant-side walkthrough: OAuth binding, mandate
   store + accounting, the approval page the agent cannot call, agent credentials,
   evidence assembly. File/line references into the frozen release; excerpts on slides
   come from here.
4. **`course-client/`** — the Java/Spring AI buyer client implementing the §3.4 sourcing
   policy + evidence record; take-home reference, README points Python/JS students at
   the official SDKs.
5. **`slides.md` rewrite** to this structure (after 1–4 exist — slides before artifacts
   get rewritten).
6. **`instructor-guide.md` + `demo-runbook.md`** updates last, from rehearsal.

## Dependencies on MockHub

Unchanged from v1 (Tracks A3, A4, A5, C1, C3, C4, C5 as listed there), plus:

- The **code tour** needs the frozen release's source readable at stable paths — pin the
  tag before writing `code-tour.md`.
- Lab 2 needs **no MockHub changes** (students build their own server; MockHub is not
  involved), which is what makes it schedulable at all.

---

## Decided

Carried over from v1: hosted-MockHub lab; this document canonical over the O'Reilly
Google Doc; MCP review in scope (now folded into §1.0); course code stays small,
MockHub referenced never rebuilt (excerpted via `examples/` and the code tour, which is
what "referenced" should have meant all along); §2.5 elicitation demo on the TS v2 SDK.

New, 2026-08-07 (Ken):

- **Merchant-side first.** The student is enabling their site; agent-side content
  survives where it motivates merchant decisions, plus §3.4.
- **Front Door structure adopted** — discovery → connectivity → identity → authorization
  → guardrails/approval → payment → evidence, with the failure demos embedded at the
  steps they motivate.
- **Vocabulary segment restored to slides** (talk-style, with implementation pointers);
  the v1 spec-complexity firewall is retired. `spec-map.md` stays as the detail handout.
- **All four implementation deliverables approved**: `examples/`, code tour, Lab 2
  (guarded tool), `course-client/`. In-class where time allows; complete take-home
  materials regardless ("If I don't have time in class, the materials will still be
  available afterwards").

## Open

1. Is the repeat-purchase slice ready enough by delivery to demo live, or does §2.3 run
   from recording? (carried from v1)
2. Lab 2 scaffold scope: does the Java track use the official MCP Java SDK or Spring AI's
   MCP server support? (Decide when building — whichever keeps the student-written guard
   under ten lines.)
3. Whether §3.6 (after the sale) grows into a fourth module in a future longer version.
   (carried from v1)
