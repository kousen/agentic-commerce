# What the lab build surfaced for MockHub (Tracks C4/C5)

Findings from building and live-verifying the mandate-boundary lab against
https://mockhub.kousenit.com on 2026-08-05. This is the input the frozen course release
(Track C5) needs; the course-side lab is done and green in all three tracks.

## Works today, verified live

- Seeded demo login `buyer@mockhub.com` / `buyer123` works on the hosted instance.
- The dev ACP key (`mockhub-dev-key`) is accepted on hosted `/acp/v1/**`.
- All four lab rejections behave as designed: over-ceiling checkout → 409
  `mandate-authorization`; revoked mandate → 409 `No active mandate`; APPROVAL_REQUIRED
  completion without approval → 409; approve/mandate-create endpoints with the agent
  credential (API key, no JWT) → 401.
- `/actuator/health` is public — the setup checkpoint uses it.

## Needed before the course freeze (C5)

1. **Mint a course-specific ACP API key.** The lab currently defaults to `mockhub-dev-key`,
   which is visible in MockHub's source. Fine for a mock platform, but a dedicated
   `course-2026` key would let it be rotated after each delivery. The lab reads
   `MOCKHUB_ACP_KEY`, so only the default in three small files changes.
2. **Demo reset before class.** Students share the seeded buyer account; the lab is
   concurrency-safe (unique agent IDs per run, self-cleanup) but mandates/checkout records
   still accumulate across reruns. `POST /api/v1/admin/demo/reset` before the session.
3. **Abandoned ACP checkouts hold seats.** A `CREATED` checkout that is never completed or
   cancelled keeps its listing unavailable (hit during dry-run: re-checkout of the same
   listing failed). The lab works around it (random listing selection, cancel-on-cleanup),
   but a TTL sweep on stale `CREATED` checkouts would remove a class-size failure mode.
4. **Papercut:** `POST /acp/v1/checkout/{id}/complete` with no request body returns 500
   (NPE on `request.agentId()`); should be 400. Cosmetic, but students will hit it the
   first time they hand-roll a curl.
5. **Papercut:** `POST /acp/v1/checkout/{id}/cancel` requires `{agentId, mandateId}` in
   the body and 400s otherwise — easy to miss and the 400 carries no hint. Cost me two
   silently-failing cleanup paths before I read the DTO. Either accept an empty body or
   name the missing fields in the error.
6. **Demo agents exist on the hosted instance:** `naive-demo-agent` and
   `guarded-demo-agent` (both APPROVAL_REQUIRED, $500) are auto-minted by the demo
   servers in `demos/`; the §2.4/§2.5 demo runs created a few real orders and approval
   records on the buyer account. Demo reset clears them.

## Facts the course content should know (not bugs to fix)

- **Mandates are enforced only on the ACP path** (`/acp/v1/checkout`). The plain REST
  checkout (`POST /api/v1/orders/checkout`) takes no agent/mandate context and enforces
  nothing; MCP tools enforce but report errors as JSON strings, not HTTP statuses.
  Teachable: "the boundary is per-door, not per-house" — but confirm this is the story we
  want, or close the REST gap before class.
- **Self-approval is blocked structurally on MCP** (approve/deny tools deliberately
  removed, commit `2acc16c`) and guarded only by the human's JWT on REST. This is exactly
  the §2.4 narrative and the lab's test 4.
- **No reversibility field exists in the mandate schema** — §2.3's reversibility gate
  still depends on Track A4 as the design doc says.
- **No frozen/course configuration exists yet in the MockHub repo**; the hosted instance
  is a moving target until C5 lands. The lab pins itself to observed behavior and env-var
  overrides (`MOCKHUB_URL`, `MOCKHUB_ACP_KEY`, `MOCKHUB_EMAIL`, `MOCKHUB_PASSWORD`).
