# What the course build surfaced for MockHub

Findings from building and live-verifying the lab and demos against
https://mockhub.kousenit.com on 2026-08-05. Everything here was hit in practice, not
read off the source. Ordered by how much it would hurt during a live class.

## Bugs worth fixing

### 0. The mandate ceiling is denominated in subtotal, but the customer is charged the total
**The most consequential one, and it doubles as course material.**
`AcpCheckoutService.java:345` validates the mandate against `cartDto.subtotal()`, so the
service fee falls outside the ceiling entirely. A $35.00 per-transaction mandate authorizes
a real charge of $35 + 10% ≈ $38.50.

Observed live, with no attacker involved: an agent told "don't spend more than $35" bought
a $32.15 listing and completed order `MH-20260805-0026` at **$35.37 all-in**, and another
completed `MH-20260805-0025` at **$35.29**. One of the agents flagged the overrun itself,
having noticed the fee only after the sale was final.

Nobody lied and nothing was exploited. The mandate held, in its own units — which are not
the units the customer was thinking in. Fix by validating against the all-in total (or
making the ceiling's basis explicit in the schema and the API docs).

*Course value: this is the thesis in miniature and it should probably become a slide in
§2.3 — a bound whose units differ from the customer's intent is not a bound. It also
supplies a natural eval condition for §3.4: "the amount charged never exceeds the
customer's stated ceiling."*

### 1. `maxPrice` / `minPrice` filter at the EVENT level, silently emptying results
**The worst one.** `GET /acp/v1/listings?query=Monster&maxPrice=40` returns **zero
listings** even though 50 Monster Jam listings are priced $30–$35. Cause:
`AcpCatalogService.getListings` (backend/.../acp/service/AcpCatalogService.java:78-100)
passes `minPrice`/`maxPrice` into the `EventSearchRequest` *and* into the per-listing
`matchesFilters`. Any event whose price range extends past the cap is excluded whole, so
its affordable listings never surface.

Why it matters here: "find me a ticket under $60" is the single most natural agentic
commerce query, and it returns nothing with no error — the agent concludes the event is
sold out. Every course demo now filters prices client-side to route around it. Fix by
dropping the price bounds from the event query and keeping only the listing-level filter.

### 2. `POST /acp/v1/checkout/{id}/complete` with no body → 500
Missing body NPEs on `request.agentId()` instead of returning 400. Students hand-rolling
curl will hit this first thing.

### 3. `POST /acp/v1/checkout/{id}/cancel` requires `{agentId, mandateId}`, 400s with no hint
Cost me two silently-failing cleanup paths before I read `AcpActionRequest`. The 400 body
names no missing field. Either accept an empty body (the checkout already knows its agent
and mandate) or name the fields in the error.

### 4. Abandoned `CREATED` checkouts hold seats indefinitely
Hit repeatedly during demo iteration: a checkout that is never completed or cancelled
keeps its listing unavailable, and the next attempt on that listing fails with "no longer
available." The lab retries other listings to survive it, but with thirty students
hammering the same inventory a TTL sweep on stale `CREATED` checkouts would remove a
real class-size failure mode.

### 5. Documented-vs-actual: idempotent checkout retry
`POST /api/v1/orders/checkout` advertises `200` for an idempotent retry but the controller
unconditionally returns `201`. Harmless, but the take-home idempotency exercise would
assert against the docs and fail.

## Needed before the course freeze (Track C5)

1. **A course-specific ACP API key.** Demos and labs currently default to
   `mockhub-dev-key`, which is in MockHub's source. A `course-2026` key rotatable after
   each delivery is enough; everything reads `MOCKHUB_ACP_KEY`.
2. **Demo reset before class** (`POST /api/v1/admin/demo/reset`). Two reasons: mandates
   and orders accumulate, and §2.1's demo reads the buyer's order history — it should
   anchor on the *seeded* history (Foo Fighters 100 Level $92, Green Day balcony $61,
   Yo-Yo Ma orchestra $106), not on leftovers from rehearsal.
3. **Second demo buyer account.** §1.2 needs two customers to demonstrate the confused
   deputy. The demo server currently self-registers `alice@mockhub.com` /
   `bob@mockhub.com` via `POST /api/v1/auth/register` — works, but seeding them (with a
   little order history for Bob) would be cleaner and survives a demo reset.

## Nice to have (course-side workaround exists)

- **Track A5, listing-text injection surface.** §3.3's demo provider attaches
  seller-written descriptions to real MockHub listings, and the agent's behavior under
  injection is genuine. A real `sellerDescription` field on listings would let students
  see the hostile text on the live site, which lands harder than a facade adding it.
- **Track C3, second provider — no longer needed.** The two-provider demo uses two MCP
  facades over the same MockHub inventory, which is *better* for the teaching point
  (identical inventory isolates tool-surface quality as the only variable).
- **No reversibility field in the mandate schema** (Track A4) — §2.3's reversibility gate
  still needs it; nothing in the schema expresses refundability today.

## Facts the course content depends on

- **Mandates are enforced only on the ACP path** (`/acp/v1/checkout`). Plain REST checkout
  (`POST /api/v1/orders/checkout`) takes no agent/mandate context and enforces nothing;
  MCP tools enforce but report errors as JSON strings, not HTTP statuses. The §1.2 demo
  actively *uses* this gap (its naive tool buys through plain REST). Worth deciding
  whether that stays a teachable gap or gets closed.
- **Self-approval is blocked structurally on MCP** (approve/deny tools deliberately
  removed, commit `2acc16c`) and guarded only by the human's JWT on REST. This is exactly
  the §2.4 narrative and the lab's credential-separation test.
- **Demo agents on the hosted instance:** `naive-demo-agent`, `guarded-demo-agent`,
  `injection-demo-agent` (auto-minted by the demo servers), plus per-run lab agents.
  Demo reset clears them.
