# What the course build surfaced for MockHub

Findings from building and live-verifying the lab and demos against
https://mockhub.kousenit.com on 2026-08-05. Everything here was hit in practice, not read
off the source.

**Status: merged to MockHub `main` (2026-08-05, CI green) and deploying to Railway.**
Five defects fixed across four commits plus a review-fix commit. Two independent reviews of
the diff caught a critical bug in the new cleanup sweep — it shared the scheduled job's
transaction, so a single failing order would have silently discarded every other cleanup
operation at commit, forever. Each order now commits in its own transaction. The reviews also
caught that the new completion-time re-authorization broke retry idempotency (the order's
spend was counted twice, rejecting a retry after a lost response and logging a mandate
mismatch that can block an agent); completion now returns an already-confirmed order
unchanged.

## Fixed

### 0. The mandate ceiling was denominated in subtotal; the buyer is charged the total
**The most consequential one, and it doubles as course material.**
`AcpCheckoutService` validated mandates against `cartDto.subtotal()`, so the service fee fell
outside the ceiling entirely: a $35.00 per-transaction mandate authorized ~$38.50.

Observed live, with no attacker involved: an agent told "don't spend more than $35" bought a
$32.15 listing and completed order `MH-20260805-0026` at **$35.37 all-in**; another completed
`MH-20260805-0025` at **$35.29**. One agent flagged the overrun itself, after the sale.

Also found while fixing it: `completeCheckout` — the step that actually charges the buyer —
never re-authorized at all. The order-total check existed but was wired only to
`updateCheckout`, so a mandate could be revoked or outgrown between creating a checkout and
completing it.

**Fix:** new `OrderPricing` record is the single place the fee is applied, used by both order
creation and every authorization path (ACP, `OrderTools`, `CartTools`); `completeCheckout`
now re-authorizes against the order total. Two existing tests asserted the subtotal behavior
and were updated — the bug was pinned by its own tests.

### 1. `maxPrice`/`minPrice` filtered at the event level, silently emptying results
`GET /acp/v1/listings?query=Monster&maxPrice=40` returned **zero** listings while 50 Monster
Jam listings sat between $30 and $35. The bounds were passed to the event query as well as
the listing filter, and event-level filtering compares against the event's own price range —
so any event with a seat above the cap was dropped whole.

"Find me a ticket under $60" is the single most natural agentic-commerce query, and it failed
silently: the agent concludes the event is sold out. **Fix:** price bounds apply per listing
only, which is the question the caller asked.

### 2. Missing or malformed request body returned 500
No handler for `HttpMessageNotReadableException`, so it fell through the catch-all as
"An unexpected error occurred." **Fix:** returns 400 naming the actual problem. Applies to
every `@RequestBody` endpoint, not just ACP.

### 3. Abandoned checkouts held seats indefinitely
A checkout never completed or cancelled kept its listing unavailable forever; the next
attempt on that seat failed with "no longer available" though nobody bought it. Hit
repeatedly during demo iteration, and a real class-size risk with thirty students on shared
inventory. **Fix:** the lifecycle sweep fails pending orders older than
`mockhub.lifecycle.abandoned-checkout-minutes` (default 30), releasing the tickets via the
existing `failOrder` path; an order completed concurrently is skipped, not fatal.

### 4. Documented-vs-actual idempotent retry
The checkout endpoint advertised 200 on an idempotent retry but always returns 201 with the
original order. **Fix:** corrected the documentation rather than the status code — the
behavior is already correct and idempotent, and changing the code could break clients for a
cosmetic gain.

## Correction to an earlier finding

I previously reported that `POST /acp/v1/checkout/{id}/cancel` "400s with no hint" when the
`{agentId, mandateId}` body is missing. **That was wrong** — the validation handler does
return a `fieldErrors` map naming each missing field. My cleanup path was failing for a
different reason and I misattributed it. No change needed.

## Still outstanding (course-side workarounds exist)

1. ~~**A course-specific ACP API key.**~~ **Done at `course-2026` (MockHub #309):**
   `AcpApiKeyFilter` accepts extra comma-separated keys from the `ACP_EXTRA_API_KEYS`
   env var alongside `mockhub-dev-key`. Remaining step is operational: Ken sets the
   course key on Railway before first delivery and rotates it after each one.
2. **Demo reset before class** (`POST /api/v1/admin/demo/reset`). §2.1's demo reads the
   buyer's order history and should anchor on the *seeded* orders (Foo Fighters 100 Level
   $92, Green Day balcony $61, Yo-Yo Ma orchestra $106), not on rehearsal leftovers.
   Still a pre-class operational step; note the reset now *preserves* orders tagged with
   `demo-seed-*` idempotency keys instead of cancelling them.
3. ~~**Second demo buyer account.**~~ **Done at `course-2026` (MockHub #308):**
   `DemoAccountSeeder` seeds `alice@mockhub.com` / `bob@mockhub.com` (same passwords the
   demos used) in every environment, with 3 confirmed orders of history for Bob that
   survive demo reset. Password drift is reverted on startup; kill switch
   `DEMO_SEED_ACCOUNTS=false`.
4. **Track A5, listing-text injection surface.** §3.3's demo provider attaches seller
   descriptions to real listings and the agent's behavior is genuine; a real
   `sellerDescription` field would let students see the hostile text on the live site.
5. **No reversibility field in the mandate schema** (Track A4) — §2.3's reversibility gate
   still needs it.
6. **Track C3, second provider — no longer needed.** Two MCP facades over the same inventory
   is *better* for the teaching point: identical inventory isolates tool-surface quality as
   the only variable.

## What the fixes change for the course

- **The §2.3 units slide and the §3.3 payoff still stand.** They present captured evidence
  from real orders, not a live reproduction — and "we found this while building the course,
  here's the commit" is a better story than a bug left in place on purpose.
- **The §3.3 demo still works after the fix.** Its agent's $35 ceiling now blocks the
  poisoned $39.37 listing at $43.31 all-in while still allowing ~$30 seats at ~$34 all-in,
  so the deterministic refusal the segment needs is intact — and now correct.
- **The lab is unaffected.** All five assertions pass under the new behavior; the
  approval-gate test still trips on the approval check before the new authorization check.
- **Worth re-running** `labs/` and the demos against the fixed build once it is deployed.

## Facts the course content depends on

- **Mandates are enforced only on the ACP path** (`/acp/v1/checkout`). Plain REST checkout
  takes no agent/mandate context and enforces nothing; MCP tools enforce but report errors as
  JSON strings, not HTTP statuses. The §1.2 demo actively *uses* this gap. Worth deciding
  whether that stays a teachable gap or gets closed.
- **Self-approval is blocked structurally on MCP** (approve/deny tools deliberately removed,
  commit `2acc16c`) and guarded by the human's JWT on REST — exactly the §2.4 narrative.
- **Demo agents on the hosted instance:** `naive-demo-agent`, `guarded-demo-agent`,
  `injection-demo-agent`, plus per-run lab agents. Demo reset clears them.
