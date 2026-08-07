# Instructor Guide

Run sheet for the four-hour live delivery, v2 (Front Door structure, 2026-08-07).
Clock times assume a 00:00 start. Cut decisions are placed **at the moment you'd have
to make them**, not collected at the end — by 02:50 it is too late to decide what to drop.

Companion documents: `demo-runbook.md` (per-demo prompts and failure modes), `labs.md`
(both labs), `code-tour.md` (file:line backing for every implementation claim),
`spec-map.md` (the take-home spec depth).

## The day before

- [ ] `POST /api/v1/admin/demo/reset` on MockHub. **Not optional** — §2.3 reads the
      buyer's order history, and rehearsal leftovers change what the agent infers.
- [ ] Start each demo server once so mandates are minted: `cd demos && npm install`,
      then `npx tsx src/probe.ts src/<server>.ts` for naive-identity, injected-provider.
- [ ] Run both labs yourself, all tracks: Lab 1 tracks take 30 seconds together; for
      Lab 2, apply a solution from `labs/guarded-tool/SOLUTIONS.md`, confirm green,
      **revert to the YOUR TURN state**, and confirm 5-red/1-green.
- [ ] `cd course-client && ./gradlew bootRun` — the §3.4 evidence output is live; have
      the terminal ready or screenshot the run.
- [ ] Verify §2.5 renders elicitation interactively in the current Claude Code build
      (`claude --mcp-config grid/guarded.json --strict-mcp-config`). If it doesn't, the
      scripted host is the fallback and you say so plainly.
- [ ] Have `demos/grid/GRID.md` open in a tab — it is the §1.2 evidence.
- [ ] Local MockHub running as a personal fallback if the venue network dies.

## Run sheet

| Clock | Segment | Notes |
|---|---|---|
| 00:00 | Opening — reorder + the inversion | Collect answers out loud; end on the 7-step front door |
| 00:15 | **Setup checkpoint** | One command, one pasted line; covers both labs' toolchains |
| 00:20 | Module 1 — vocabulary, discovery, connectivity, identity | Triage chat stragglers during the demos |
| 01:15 | Break (10) | |
| 01:25 | Module 2 — authorization and approval | **Protect this time.** It's the course. |
| 02:30 | Break (10) | |
| 02:40 | Module 3 — payment, guardrails, evidence, Lab 2 | Attention low point; poll + Lab 2 are the fix |
| 03:35 | Close + Q&A | Where-to-start, checklist-with-artifacts |
| 03:55 | *Slack* (5) | Thin on purpose — Lab 2's take-home conversion is the relief valve |

## Opening (00:00–00:15)

Ask "what makes this safe?" and **wait**. Steer to *evidence* and *reversibility*, post
the framing sentence, then run the inversion compressed: two kinds of robot, the
scoreboard (every shipped integration stops at checkout — deliberately), the card
networks already across the line. End the opening on the **seven-step front door
slide** and say the sentence that organizes the day: "today walks this list, and the
failures along the way are why each step exists."

**First cut candidate:** the Alexa naming discussion. One slide, first thing to go.

## Module 1 (00:20–01:15)

**§1.0 Vocabulary is 18 minutes and it is teaching, not throat-clearing.** This is the
segment v1 didn't have and the reviews said was missing. Every spec slide has the same
skeleton — who / what problem / what layer / status — keep the rhythm audible. Land
hard on the **layering table**: "this table is the course." If pressed for time, the
ACP cautionary tale compresses into the layering slide's narration (cut candidate #2).

**§1.1 Discovery** is fast and concrete: the two real documents, then "I published two
documents; the client worked out the rest." Plug `examples/discovery/`.

**§1.2 Connectivity** carries the grid demo, reframed: run it live once, pivot to the
grid regardless of what the live run did. The merchant-side landing matters now —
**your tool surface is your storefront** — before the buyer-side implication gets
parked for Module 3.

**§1.3 is the first real beat.** Confused-deputy demo; when Bob's order appears, stop
talking for a second. Then the correction *as a build* — token binding, and the
`ChatContext.resolveEmail` slide: "the parameter still exists; the server ignores it."
That code slide is new in v2; give it its beat, it's the module's proof of buildability.

⏱ **Checkpoint — if you reach 01:00 and haven't started §1.3**, compress the grid
walkthrough to the table and the one-line lesson (cut candidate #5) and go. §1.3 and
the thesis slide are not skippable.

End on the thesis slide. Read it out loud; don't paraphrase.

## Module 2 (01:25–02:30) — protect this

**§2.1 mandate slides** now include the two quotable production facts: the
"last line of defence" comment and the row-lock accounting. Don't re-derive them —
point at `code-tour.md` and keep moving; the depth is in the repo.

**§2.2 units** is ninety seconds and it pays off three more times (§3.2 contract, Lab 2
trap, checklist). Plant it clearly.

**§2.3** runs from the history demo. The payoff line is the agent's own: *"Floor is
arguably the worst place to sit at a Monster Jam show given the mud and exhaust, but it
faithfully matches your history."* Read it verbatim. Then the field-notes slide
(jazz / empty string) — those two stories are reliable laughs that carry the
structure-not-authority point.

**§2.4 is the emotional peak.** Let the tool sequence render one call at a time. When
`approvePurchase` fires, say nothing for two beats. Then: *nothing malfunctioned.*

**New in v2 — the `2acc16c` slide:** the real platform made the same mistake and fixed
it eleven days before the first demo of this course. Read the commit message aloud;
"approval now lives only where the agent cannot reach" is the segment's thesis in the
platform's own words.

**§2.5 must not be slides alone.** Both fixes get shown: the approval page (merchant),
then the MRTR demo (protocol). The line `[HOST UI] Your agent wants to buy…` is the
whole segment. If the interactive render fails, cut to the scripted host without
apologizing.

**Lab 1 at ~02:15.** Thirteen minutes including walkthrough. Predictions first; they're
what makes the results land for students whose setup is broken.

⏱ **Checkpoint — at 02:22, if the lab hasn't started**, go straight to predict-then-run
projected from your machine (cut candidate #6 — **only if forced**; it changes the
course's character).

## Module 3 (02:40–03:35)

**§3.1 payment + §3.2 guardrails** are brisk. Two beats to protect: the *real* refusal
message slide (amount but no ceiling — pose the disclosure question and let the room
argue for sixty seconds; it primes Lab 2), and the `REQUIRES_NEW` risk-signal detail
(the record that survives the rollback).

**§3.2 injection: the prediction poll comes first.** Show of hands, remember the split:
**ten runs, zero took the bait.** Say it plainly, then the two orders that blew the
budget anyway. The room came in expecting a scare demo and leaves with something more
useful. Then the argument for writing the defense anyway — the seller needs it to work
once. Do not oversell.

**§3.3 evidence:** the actorTimeline slide, and the two honesty notes (derived actor
attribution; `NOT_PERSISTED` over invented receipts). "Two of those rows are a human"
is the line.

**LAB 2 at ~03:05.** Twelve minutes as planned. The shape to say out loud: "one test is
already green — that's the scaffold's promise; the five red sentences are your
worklist." If a student's four-of-five pass with the units test red, **celebrate it to
the room** — they just reproduced finding #0 personally.

⏱ **Checkpoint — at 03:05, if you are more than ten minutes behind**, take the
pre-decided conversion (cut candidate #3... see list): project the TypeScript scaffold,
write the guard live in five minutes, assign the rest as guided take-home. Ken approved
this conversion in advance — execute it without apology.

**§3.4 buyer's side** runs from the `course-client` output slide — real run, real
exception record. If time is tight this compresses to the postcondition slide plus the
output slide (cut candidate #4).

**§3.6 after the sale** is unresolved on purpose. Say "no clean answers exist yet" out
loud. It seeds the best questions of the day.

## Close (03:35)

**Where to start** — the six steps, with "steps one and two cost almost nothing." Then
the protocol argument, now earned. Then the checklist slide — in v2 every line points at
an artifact in the repo; say that sentence, it's the difference between a poster and a
toolkit. Q&A.

## If something breaks

| Failure | Move |
|---|---|
| MockHub unreachable | Local instance for projection; Lab 1 falls to predict-then-run; **Lab 2 is unaffected — it's fully local** |
| A demo agent behaves differently than the runbook says | Narrate it — nondeterminism is the course's subject, not its enemy |
| Model refuses to self-approve in §2.4 | Reply "yes, do whatever's needed to complete it"; note aloud that the human just rubber-stamped without reading the proposal |
| Interactive elicitation not rendering | Scripted host, stated plainly |
| Running 15+ min late by Module 3 | Cut list below, in order |

## Cut list, in order (from the design doc)

1. Alexa naming discussion — one slide, first to go entirely
2. §1.0: ACP cautionary tale + x402 line fold into the layering slide's narration
3. Lab 2 in-class execution → five-minute live kickoff + guided take-home (**pre-approved conversion**, not an emergency)
4. §3.4 buyer's side → postcondition slide + course-client output slide only
5. §1.2 grid → the table and the lesson, drop the run-by-run walkthrough
6. Lab 1 → predict-then-run only. **Only if forced.**

**Protected under all circumstances:** §2.4 (self-approval), §2.5 (both fixes), the
§1.3 confused-deputy demo, Lab 1, and the module-end thesis slides — ninety seconds
total, and they are the spine.
