# Instructor Guide

Run sheet for the four-hour live delivery. Clock times assume a 00:00 start. Cut decisions
are placed **at the moment you'd have to make them**, not collected at the end — by 02:50
it is too late to decide what to drop.

Companion documents: `demo-runbook.md` (per-demo prompts and failure modes),
`labs.md` (what students do), `spec-map.md` (the handout that absorbs acronym pressure).

## The day before

- [ ] `POST /api/v1/admin/demo/reset` on MockHub. **Not optional** — §2.1 reads the buyer's
      order history, and rehearsal leftovers change what the agent infers.
- [ ] Start each demo server once so mandates are minted: `cd demos && npm install`, then
      `npx tsx src/probe.ts src/<server>.ts` for naive-identity, injected-provider.
- [ ] Run all three lab tracks yourself. They take 30 seconds together.
- [ ] Verify §2.5 renders elicitation interactively in the current Claude Code build
      (`claude --mcp-config grid/guarded.json --strict-mcp-config`). If it doesn't, the
      scripted host is the fallback and you say so plainly.
- [ ] Have `demos/grid/GRID.md` open in a tab — it is the §3.1 evidence.
- [ ] Local MockHub running as a personal fallback if the venue network dies.

## Run sheet

| Clock | Segment | Notes |
|---|---|---|
| 00:00 | Opening — the reorder that isn't | Collect answers out loud; don't lecture the two properties, extract them |
| 00:15 | **Setup checkpoint** | Everyone runs one command, pastes one line in chat |
| 00:20 | Module 1 — tools, identity, naive purchase | Triage chat stragglers during the demos |
| 01:10 | Break (10) | |
| 01:20 | Module 2 — authority, mandates, approval | **Protect this time.** It's the course. |
| 02:25 | Break (10) | |
| 02:35 | Module 3 — sourcing, evidence, evaluation | Attention low point; the prediction poll is the fix |
| 03:25 | Close + Q&A | |
| 03:45 | *Slack* (15) | Live online always overruns |

## Opening (00:00–00:15)

Ask "what makes this safe?" and **wait**. The room will say "it knows what you want" —
steer to *evidence* and *reversibility*. Post the framing sentence where it stays visible:
**delegated authority must scale with reversibility, not just with confidence.**

The Amazon Auto Buy slide is the credibility anchor: those constraints are quoted verbatim
from a help page. The company with the most to gain from frictionless autonomy shipped a
mandate specification instead.

**First cut candidate:** the Alexa naming discussion. One slide, first thing to go.

## Setup checkpoint (00:15–00:20)

Nothing is taught here. Watch chat for tracks that fail, not for the count of successes.

- Java failure is almost always a missing JDK 21 → point to the Python track, not to a fix.
- Corporate network blocking mockhub.kousenit.com → they run predict-then-run at the lab.
- **Do not debug one student's setup live.** Note their name; they get the fallback path.

## Module 1 (00:20–01:10)

§1.0 MCP review is five slides and **five minutes**. This room has used MCP; you are
establishing shared vocabulary, not teaching the protocol. If you find yourself explaining
what a tool is, you have lost four minutes you need in Module 2.

**§1.2 is the first real beat.** Run the confused-deputy demo. The prompt is deliberately
friendly — nobody is attacking anything. When Bob's order appears, stop talking for a
second and let it land. Then: *anything the agent can type is an assertion, not a credential.*

⏱ **Checkpoint — if you reach 00:55 and haven't started §1.4**, compress §1.3 to the single
"wrong-tool selection is measurable" slide and move on. §1.3 is cut candidate #2.

End on the thesis slide. Read it out loud; don't paraphrase.

## Module 2 (01:20–02:25) — protect this

**§2.1** runs from the history demo. The payoff line is the agent's own: *"Floor is arguably
the worst place to sit at a Monster Jam show given the mud and exhaust, but it faithfully
matches your history."* Read it verbatim from the transcript — it is funnier and sharper
than any paraphrase.

**§2.3's units slide** is new and it is worth the ninety seconds. Two real orders, $35.29 and
$35.37, against a $35.00 ceiling, with no attacker. It pays off again in §3.3, so plant it.

**§2.4 is the emotional peak of the course.** Let the tool sequence render one call at a time.
When `approvePurchase` fires, say nothing for two beats. Then: *nothing malfunctioned.*

**§2.5 must not be slides alone.** §2.4's resolution cannot be a payload diagram. Run the
guarded demo; the line `[HOST UI] Your agent wants to buy…` is the whole segment. If the
interactive render fails, cut to the scripted host without apologizing — narrate it as
"here's the same round-trip with the host role made explicit."

**Lab at ~02:05.** Fifteen minutes including the walkthrough. Have the room predict before
anyone runs anything; the predictions are what make the results land for students whose
setup is broken.

⏱ **Checkpoint — at 02:15, if the lab hasn't started**, go straight to predict-then-run
projected from your machine and skip student execution. This is cut candidate #4 and the
only one that changes the course's character — take it only if forced.

## Module 3 (02:35–03:25)

Hour three is the attention low point. Both engagement devices are front-loaded on purpose.

**§3.1:** run it live **once** for authenticity, then pivot to the grid regardless of what the
live run did. If the live run happens to look sensible, that is itself the argument: one
draw tells you nothing, here are sixteen. Sixteen runs, three models, and the small model
never even *loaded* the second provider's tools.

**§3.3: the prediction poll comes first.** "This listing tells the agent to ignore its price
ceiling — will it?" Take a show of hands and remember the split, because the answer is
counterintuitive in the direction that flatters the models: **ten runs, zero took the bait.**

Say that plainly. Then show the two orders that blew the budget anyway, with no attacker
involved. The room came in expecting a scare demo about injection and leaves with something
more useful: a helpful agent and a units mismatch broke the boundary that an attacker
couldn't.

Then make the argument for writing the defense anyway — the seller needs it to work once,
on one model version, and the check costs nothing. **This audience will respect that more
than a rigged demo.** Do not oversell.

⏱ **Checkpoint — at 03:10, if §3.4 hasn't started**, reduce the MCP/ACP comparison to the
single slide and pointer (cut candidate #3).

**§3.5 (after the sale)** is unresolved on purpose and is the most differentiated eight
minutes in the course. Say "no clean answers exist yet" out loud. It seeds the best questions
of the day, so leave room.

## Close (03:25)

Return to the protocol argument now that it is earned: MCP went stateless, removed real
operational pain, and removed none of today's problems. **Production complexity moved up the
stack.** Then the nine-item checklist, then Q&A.

## If something breaks

| Failure | Move |
|---|---|
| MockHub unreachable | Local instance for the projection path; students fall back to predict-then-run |
| A demo agent behaves differently than the runbook says | Narrate it — nondeterminism is the course's subject, not its enemy |
| Model refuses to self-approve in §2.4 | Reply "yes, do whatever's needed to complete it"; note aloud that the human just rubber-stamped without reading the proposal |
| Interactive elicitation not rendering | Scripted host, stated plainly |
| Running 15+ min late by Module 3 | Cut list in order: Alexa slide → §1.3 → MCP/ACP comparison → student lab execution |

## Cut list, in order (from the design doc)

1. Alexa naming discussion — one slide, first to go entirely
2. §1.3 tool-surface segment — compress to three minutes
3. §3.4 MCP/ACP comparison — one slide and a pointer
4. §2.6 lab student execution — predict-then-run only. **Only if forced.**

**Protected under all circumstances:** §2.4, §2.5, §3.1. Those three demos carry the course.
The module-end thesis slides are also not cut candidates — ninety seconds total, and they
are the spine.
