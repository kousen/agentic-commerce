# Agentic Commerce: Four-Hour Course Design

**Format:** O'Reilly Learning Platform, live online, 4 hours
**Platform:** MockHub (frozen course release) + small Java/Spring AI course client
**Prepared:** August 1, 2026
**Revised:** August 2, 2026

---

## Thesis

Giving an agent tools is easy. Giving it bounded authority to conduct commerce safely is the real engineering problem.

Every module should end with the audience holding a sharper version of that sentence than they had at the start — and each module ends on an actual slide that says its sharpened version out loud:

- **Module 1:** An auditable capability boundary is not an authority boundary.
- **Module 2:** Authority comes from artifacts the model cannot mint; an approval the agent can invoke is not an approval.
- **Module 3:** A trustworthy buyer's representative is a policy you write, not a property of the protocol.

These three slides double as chapter boundaries for the recording.

---

## Design constraints

**This is not the talk at four times the length.** A conference talk demonstrates a working system. A course walks students into failures and lets them feel the pull of each fix. The arc is *naive implementation → discovered failure → principled correction*, three times.

**Four hours is about three hours of instruction.** Breaks, questions, setup friction, and demo recovery consume the rest. The plan below budgets 225 minutes and leaves ~15 minutes of slack. Live online always overruns; the slack is the plan, not a cushion. (Timings throughout are planning estimates, not commitments — they never match reality. The cut list is what makes that survivable.)

**One real hands-on lab, not four.** In a live online format, every hands-on segment risks a long tail of students stuck on setup. Budget one lab that genuinely lands (§Module 2, L2) and make everything else demo-driven with runnable code students take home.

**Every live agent demo needs a recorded fallback.** Agent behavior is nondeterministic; a demo that depends on the model choosing a particular tool will eventually not. Record each demo in advance and be willing to cut to tape without apology.

**Protocol content is a supporting character.** The 2026-07-28 stateless spec is new and topical and it is tempting to open with it. Resist. Students who don't yet feel the authority problem have no frame for the protocol answer. Split it: brief framing in Module 1, the elicitation mechanism in Module 2 where it solves a problem the audience already wants solved, and the retrospective argument in Module 3.

---

## Timing at a glance

| | Segment | Minutes |
|---|---|---|
| 00:00 | Opening: the reorder that isn't | 15 |
| 00:15 | Setup checkpoint (one command, paste output in chat) | 5 |
| 00:20 | **Module 1** — Tools, identity, and the naive purchase | 50 |
| 01:10 | Break | 10 |
| 01:20 | **Module 2** — Authority, mandates, and approval | 65 |
| 02:25 | Break | 10 |
| 02:35 | **Module 3** — Sourcing, evidence, and evaluation | 50 |
| 03:25 | Close + Q&A | 20 |
| 03:45 | *Slack* | 15 |

---

## Opening (15 min) — The reorder that isn't

Open with the easy case, in the audience's own voice:

> "I'm out of razor blades. Find my last order and do it again."

Ask what makes this safe, and collect answers. Steer toward two: **evidence** (exact SKU, quantity, address, payment, delivery preference all known; price predictable) and **reversibility** (wrong order costs twelve dollars and ships back).

Then swap the domain:

> "Buy tickets like last time."

Same sentence shape. Every safety property gone — novel inventory, scarce, expensive, time-boxed, non-refundable. Nothing to repeat, because there is no SKU.

Land the framing: **delegated authority must scale with reversibility, not just with confidence.** Post it on the wall for the rest of the day.

Grounding slide — Amazon's Auto Buy constraints, verbatim from the help page: Prime members only, Fulfilled-by-Amazon items only, one active request per item, one unit per item, no promotional discounts or coupons, up to 200 active requests, cancellable any time before the order is placed. That is a mandate specification written in customer-service prose. Note also that Amazon's *scheduled* recurring purchases add to the cart for review rather than shipping automatically. The company with the strongest incentive to remove friction chose full autonomy only for the narrow single-SKU, price-triggered case.

*Cut candidate if running late: the Alexa naming discussion. One slide maximum, and it is the first thing to go.*

### Setup checkpoint (5 min)

Immediately after the opening: everyone runs one command against the frozen course client and pastes the output line into chat. Nothing is taught here — the entire purpose is to surface setup failures **ninety minutes before the lab needs setup to work**. Triage stragglers via chat during Module 1's demos; anyone still broken at the lab falls back to predict-then-run without drama. This is the cheapest insurance in the course: the design's biggest stated fear is students stuck on setup, so setup gets exercised first, not at minute 125.

---

## Module 1 (50 min) — Tools, identity, and the naive purchase

**Objective:** students leave believing that an auditable capability boundary is not the same thing as safety.

### 1.0 MCP in five slides (5 min)

A compressed review, not a tutorial — the audience has used MCP; this buys shared vocabulary so §1.1 lands. Five slides, hard cap:

1. Host / client / server roles, one diagram.
2. Tools, resources, prompts — and the honest note that tools are all this course needs.
3. One tool-call lifecycle diagram (request → tool selection → call → result → model continues).
4. The 2026-07-28 spec release exists and is current; the day's protocol content builds on it (the stateless argument itself waits for §1.4).
5. Pointer to the `spec-map.md` handout: every acronym today lives there, not on slides.

### 1.1 The naive tool (8 min)

Show the tool anyone would write first:

```java
buyTickets(String userEmail, String listingId, int quantity)
```

Run it. It works. Everyone recognizes it as reasonable.

### 1.2 Break it: the agent supplies the identity (12 min)

**Demo.** Prompt the agent so it passes a different `userEmail`. It buys tickets on someone else's account, cheerfully, with no error.

This is the confused deputy in three lines of Java, and it is the moment the room gets quiet.

**Correction.** Identity binds from the authenticated OAuth token, never from an agent-supplied argument. Anything the agent can type is an assertion, not a credential.

State the general rule, because it recurs all day: **the agent's input space and the authority space must not overlap.**

### 1.3 Break it again: too many tools (8 min)

Show the tool surface at full size. Discuss wrong-tool selection as a measurable property, not a vibe — you can count it.

Brief treatment of tool descriptions as an attack surface and a correctness surface simultaneously.

### 1.4 What MCP actually buys you (10 min)

MCP gives a more auditable capability boundary than handing an agent a shell, `curl`, and unrestricted network access. That is real and worth having. It does not address authorization, prompt injection, confused deputies, or excessive tool power.

Brief protocol framing here — five minutes, no more:

- MCP 2026-07-28 (published four days before this course was designed) removed protocol-level sessions and the `initialize` handshake. MCP now behaves like an ordinary HTTP API.
- What that fixed: sticky routing, shared session stores, sessions dying when a container is replaced.
- What it did not fix: everything in this module.

Set up the closing argument without making it yet: *production MCP complexity moved up the stack, from transport into identity, authority, and deployment durability.*

### 1.5 Discussion (7 min)

Poll: which of these failures would your existing test suite catch? Seed the honest answer — a thousand tests inside modeled environments miss failures at process boundaries, under the real production profile, through real clients, and under nondeterminism.

End on the module's thesis slide: **an auditable capability boundary is not an authority boundary.**

---

## Module 2 (65 min) — Authority, mandates, and approval

**Objective:** students can distinguish authority to act from authority to pay, and can explain why in-band approval is not human approval.

This is the heart of the course. Protect its time.

### 2.1 From "do it again" to "like last time" (10 min)

The reorder case carries transaction-level authorization inside the instruction — "do it again" *is* the authorization. "Like last time" is not; it is an invitation to infer one.

**Demo the ambiguity.** Ask the agent to interpret a prior ticket order. Watch it produce a plausible, unaudited inference: *you sat in section 112, so lower bowl is acceptable.* Then show what lower bowl includes — seats behind the stage.

The purchase would be fully inside the mandate and completely wrong.

### 2.2 `PurchaseProfile`: the inference must be an artifact (12 min)

The fix is a boundary. The LLM *proposes* a structured profile; deterministic Java validates it; the customer can inspect it before it is spent against; the mandate attaches to the profile, not to the raw utterance.

Show the code on both sides of that line. This is the single most transferable idea in the course and it generalizes far beyond ticketing: **let the model produce structure, never let it produce authority.**

### 2.3 Mandates: two axes, gated on reversibility (12 min)

Separate authority to act from authority to pay. Show the matrix rather than a ladder, with reversibility as the gate:

- Recommend only.
- Hold automatically, approval required to buy.
- Buy autonomously within a mandate.
- Request a narrowly scoped exception at a boundary.

Emphasize that **autonomous in-mandate success is the intended path.** If every purchase ends in an approval prompt, the agent is an elaborate shopping cart and the customer will reasonably ask why they didn't just use the website.

### 2.4 In-band approval, and why it fails (8 min)

**Demo — the memorable one.** With approval exposed as an MCP tool, prompt the agent through a purchase that exceeds its mandate. Watch it call the approval tool on itself and proceed.

Nothing malfunctioned. The architecture permitted it.

Rule: an approval an agent can invoke is not an approval.

### 2.5 The protocol answer (8 min)

Here is where the stateless spec earns its place, because the room now wants this.

Multi Round-Trip Requests (SEP-2322): a `tools/call` can return an `InputRequiredResult` carrying `inputRequests` — full elicitation requests — plus an opaque `requestState`. The client gathers the answers and re-issues the original call with `inputResponses` and the echoed state. All the state rides in the payload, so any stateless instance can resume.

Why this is the answer and not just a mechanism: **the elicitation is fulfilled by the client, with the human in the loop.** It is not a tool the agent can call on itself. The separation enforced in §2.4 by architectural discipline is now enforced by the protocol. And the customer approves in the agent host's UI, in conversation — no second visit to the site.

**This segment needs a demo, not just slides.** §2.4 is the emotional peak of the course; its resolution cannot be a payload diagram. The Java SDK lagging the spec is one honest sentence, not a reason to skip the demo — record an elicitation/MRTR flow in *any* client that supports it (TypeScript reference client, Claude Desktop, whatever works first) and play the tape: the agent hits the boundary, the approval question appears in the **host's** UI, the human answers, the call resumes. Thirty seconds of that lands harder than every diagram in the deck.

Two constraints worth stating:

- Elicitation must not use form mode for sensitive credentials; URL mode is required for those.
- Sampling is deprecated as of 2026-07-28. Build on elicitation only.

Mention **MCP Apps** (server-rendered HTML in a sandboxed iframe, UI actions flowing through the same audit and consent path as a direct tool call) as the complementary inline-approval-card option, one slide.

Flag the retry consequence, which sets up Module 3: MRTR re-issues *the same call*. Duplicate delivery is now normal control flow. Every state-changing tool needs an idempotency key.

### 2.6 LAB (15 min, walkthrough included) — Mandate boundary test

**The one hands-on lab.** Chosen because it is deterministic, requires no paid credentials, runs in seconds, and verifies unambiguously. The earlier "10 min + walkthrough" framing hid an overrun inside the most protected module; the walkthrough is now budgeted, not smuggled.

Students write and run a test asserting that an agent cannot exceed, widen, or self-approve its own mandate. Given: the frozen course client and hosted MockHub. Framed as design by contract — precondition, postcondition, invariant.

**Runs against hosted MockHub — decided.** Local Docker reintroduces exactly the setup tail the whole design avoids. The instructor keeps a local instance as a personal fallback for the projection path if the venue network dies; students never touch Docker.

**Authoring constraint:** the assertions must read as English. The registration page invites PMs and engineering leaders who will not write Java; each assertion should be legible as a plain statement — `assertAgentCannotApproveOwnPurchase()`, "the mandate ceiling holds under retry" — so a non-coder following along still receives the contract even if they never run it.

*Fallback for students blocked on setup (already surfaced at the 00:15 checkpoint):* project the test, have the room predict pass/fail before running it. The learning survives the setup failure.

End on the module's thesis slide: **authority comes from artifacts the model cannot mint; an approval the agent can invoke is not an approval.**

---

## Module 3 (50 min) — Sourcing, evidence, and evaluation

**Objective:** students can specify what a trustworthy buyer's representative must do that MCP does not specify for them.

### 3.1 Two providers, no policy (12 min)

Connect the agent to two ticket services and ask for the same event.

**Demo: one live run, then the grid.** Run it live once for authenticity. Then show a prepared grid of eight to ten recorded runs of the same prompt with the divergent choices highlighted — the model preferring the better-named tool, the first listed, one it used successfully before, querying both, asking, or picking arbitrarily without disclosing that alternatives existed. Three live runs gamble on nondeterminism showing up on cue; if the model happens to behave consistently, the demo argues against you. The grid is better *evidence* of inconsistency than any live sample, and it is immune to the demo gods. The inconsistency *is* the demonstration; do not clean it up.

The lesson: MCP provides interoperability, not neutral marketplace arbitration. It lets an agent reach multiple sellers. It says nothing about how to act as a trustworthy buyer's representative.

### 3.2 Sourcing as a contract (12 min)

The policy the application must supply: search every eligible provider → normalize price, fees, seat information, refundability, confidence → deduplicate equivalent listings → rank against declared customer preferences → disclose the selection and the reason → purchase only through providers covered by the customer's authority.

Then sharpen it from narrative into a postcondition:

> The selected listing is the minimum-cost listing satisfying the profile across all searched providers, or an exception record explains why not.

That is checkable. Narrative evidence is not.

Disclosure of affiliate relationships, marketplace ownership, and preferred-provider agreements belongs here too — otherwise "the model chose it" conceals self-preferencing. Note without belaboring that this is the same shape as best-execution obligations in other regulated markets.

### 3.3 Injection: the listing is attacker-controlled text (10 min)

**Prediction poll first.** Before the demo: "This listing's description tells the agent to ignore its price ceiling. Will it?" Committing to a prediction is the cheapest engagement in hour three — Module 3 is otherwise demo-lecture at the attention low point, and the students who vote "no, it'll be fine" are the ones the demo converts.

**Demo.** A seller-written listing description that instructs the agent to disregard its price ceiling or prefer that provider.

Contract to enforce: no content originating from a listing can widen a mandate. Boundaries evaluate in deterministic Java against structured fields only; free text is never an input to an authorization decision.

Closes the loop with §1.2 — same principle, second appearance: **input space and authority space must not overlap.**

### 3.4 Evidence and evaluation (8 min)

A purchase evidence record lists providers searched, candidates considered, ranking policy applied, `PurchaseProfile` version used, selected listing, and reason.

Frame eval conditions as design by contract — preconditions, postconditions, invariants — and show a small suite: identity binding holds, mandate cannot be self-widened, idempotency holds under retry, sourcing postcondition holds or an exception record exists.

Brief MCP/ACP comparison here, kept to what the audience needs.

### 3.5 After the sale (8 min)

The path everyone skips. Wrong purchase, customer dispute, refund attempt against a non-refundable listing, chargeback — and where liability sits when an agent acted correctly inside a mandate but against intent.

No clean answers exist yet. Say so. This is the most differentiated eight minutes in the course precisely because it is unresolved, and it seeds good questions.

End on the module's thesis slide: **a trustworthy buyer's representative is a policy you write, not a property of the protocol.**

---

## Close (20 min)

Return to the protocol argument, now earned:

MCP went stateless at the protocol layer on 2026-07-28. It removed real operational pain — sticky sessions, shared session stores, sessions dying with a container. It removed none of the problems in this course. **Production complexity moved up the stack: into identity, authority, tool design, evidence, and deployment durability.**

Restate the thesis. Then the practical checklist students take home:

1. Bind identity from the token, never from an agent argument.
2. Let the model produce structure; never let it produce authority.
3. Separate authority to act from authority to pay; gate both on reversibility.
4. An approval the agent can invoke is not an approval.
5. Make in-mandate autonomy the success path; escalate by exception.
6. Idempotency on every state-changing tool.
7. Free text never widens a mandate.
8. Evidence must be checkable, not narrative.

Q&A.

---

## Cut list, in order

Live online overruns. Decide these now, not at 02:50.

1. Alexa naming discussion (Opening) — one slide, first to go entirely.
2. §1.3 tool-surface segment (now 8 min) — compress to three minutes, keep wrong-tool-selection-is-measurable, drop the walkthrough.
3. §3.4 MCP/ACP comparison — reduce to a single slide and a pointer.
4. §2.6 lab (now 15 min with walkthrough budgeted) — shorten to the predict-then-run walkthrough, drop student execution. **Cut this only if forced;** it is the only hands-on segment and its absence changes the course's character. The module-end thesis slides are *not* cut candidates; they cost ninety seconds total and are the course's stated spine.

Protected under all circumstances: §2.4 (in-band approval failure), §2.5 (the elicitation answer), §3.1 (two-provider inconsistency). Those three demos carry the course.

---

## Dependencies on MockHub

From the handoff document, in the order the course needs them:

- **Track A3** `PurchaseProfile` persisted and inspectable — required for §2.2.
- **Track A4** reversibility in the mandate schema — required for §2.3.
- **Track A5** listing-text injection surface — required for §3.3.
- **Track C3** second mock provider — required for §3.1.
- **Track C1** repeat-purchase slice — required for §2.1–2.2.
- **Track C4** course client — required for the lab.
- **Track C5** frozen release, credential-free student path — required for the lab. **Freeze this first.**

§2.5 and the MCP Apps slide are Track B (blocked on Java SDK support) and should be taught as demonstration and specification reading, not as student implementation.

---

## Decided

- **Lab runs against hosted MockHub** (was Open #1). Local Docker reintroduces the setup tail the design exists to avoid; the instructor keeps a local instance as a personal fallback for the projection path.
- **This document is canonical over the O'Reilly Google Doc outline.** Make the course as good as possible first; sync the published outline afterwards.
- **2026-08-05 (Ken): An MCP review is in scope.** The audience knows coding agents and has used MCP, but the course still reviews MCP — compressed, not skipped. Folded in as §1.0 (Module 1 45→50 min, slack 20→15).
- **2026-08-05 (Ken): Course code stays small; MockHub is referenced, never rebuilt.** MockHub serves as the hosted platform and the source of excerpted examples. Everything students clone is course-sized.

- **2026-08-05 (was Open #3): the §2.5 elicitation demo runs on the TypeScript v2 SDK.** A scripted host (`demos/src/test-guarded-client.ts`) proves the full multi-round-trip live against hosted MockHub: agent hits boundary → question in the host → human answers → purchase completes with a real approval artifact. Claude Code ≥2.1.76 renders elicitation interactively (verify once before class); Claude Desktop does not support elicitation at all.

## Open

1. Is the repeat-purchase slice ready enough by delivery to demo live, or does §2.1 run from recording?
2. Whether §3.5 (after the sale) is eight minutes or grows into a fourth module in a future longer version.
