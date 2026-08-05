# Lab — The Mandate Boundary Test

*Module 2, §2.6 · 15 minutes including walkthrough · runs against hosted MockHub, no credentials to configure*

You are the customer. An AI agent shops for tickets on your behalf. The only authority it
holds is a **mandate** you created — a per-transaction spending ceiling and an approval mode.
This lab proves, with tests you run yourself, that the boundary is enforced by the platform,
not by the agent's good behavior.

## The contract, in English

- **Precondition** — a mandate exists, and it is the *only* authority the agent holds.
- **Postcondition** — every purchase inside the mandate can succeed; everything outside is refused.
- **Invariant** — nothing the agent can do changes the mandate's boundaries.

## Before you run: predict

For each sentence, write down **pass** or **fail** — will the platform enforce it?

| # | The claim | Your prediction |
|---|---|---|
| 1 | The mandate ceiling holds — a purchase above the limit is refused, not negotiated | |
| 2 | A revoked mandate grants nothing — yesterday's authority is not today's | |
| 3 | A purchase needing approval cannot complete without one | |
| 4 | The agent's credential cannot approve a purchase | |

(If your setup is broken, this table **is** your lab: predict, then watch the projected run.
The learning survives the setup failure.)

## Run it

You already did this once at the setup checkpoint. Same directory, one command:

| Track | Command |
|---|---|
| Java | `cd labs/java && ./gradlew test` |
| Python | `cd labs/python && pytest -v` |
| TypeScript | `cd labs/typescript && npm test` |

Four tests pass. One is skipped, and it's yours.

## Your turn (5–8 lines)

The skipped test is the course thesis as an assertion:

> **The agent's credential cannot mint a mandate — authority comes from artifacts the model cannot mint.**

Test 4 is the pattern: it takes the *agent's* credential (the API key, not your login) to a
door that only the human's credential opens, and asserts the platform answers **401**. Your
test does the same one level deeper — try to **create a mandate** (`POST /api/v1/my/mandates`)
with only the agent's credential.

1. Open the lab test file for your track and find the test marked **YOUR TURN**.
2. Remove the skip/disabled marker.
3. Write the body: POST to `/api/v1/my/mandates` with the agent headers and any mandate
   payload, assert the response status is 401.
4. Re-run. Five passing tests means the contract holds — including the part you proved.

Why this one matters enough to write yourself: if this test ever *failed*, every other
boundary in the lab would be decorative. An agent that can mint its own mandate can grant
itself any ceiling, any scope, any approval mode. The artifact the model cannot mint is the
entire foundation — which is exactly why it's the one you should not take our word for.

## What each test actually does (walkthrough reference)

1. **Ceiling** — creates a mandate with a $1.00 per-transaction limit, then has the agent
   attempt a real ~$30 listing through the agentic checkout path. The platform refuses with
   409 and names the mandate in the refusal. The refusal is *deterministic Java on structured
   fields* — no model involved.
2. **Revocation** — creates a generous mandate, revokes it, attempts the purchase. Refused:
   the mandate is checked at spend time, not at issue time.
3. **Approval gate** — a mandate in `APPROVAL_REQUIRED` mode lets the agent *stage* a
   checkout (201) but refuses *completion* (409) until a human approves on the MockHub
   website. The approval lives out-of-band, where the agent cannot reach it.
4. **Credential separation** — the approve endpoint exists and works — for the human's JWT.
   Presenting the agent's API key gets 401. Same action, different door: the agent's key
   does not fit, by construction.

Each test creates its own uniquely-named agent, so a room full of students sharing the demo
account never collide, and each cleans up its mandates and checkouts afterward.

## Take-home extensions (after class)

- **The ceiling holds under retry** — the agentic checkout accepts an `idempotencyKey`.
  Complete the same purchase twice with the same key and assert you get the same order
  number and a single spend against the mandate. (Creates real orders on the demo account,
  which is why it's take-home.)
- **Port the contract to your own stack** — the five sentences in this lab are the spec.
  Re-implement them against any commerce API you work with; the sentences shouldn't change,
  only the plumbing.
