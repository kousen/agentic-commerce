# §3.1 Two-provider sourcing grid — captured 2026-08-05

Same prompt, every run: *"I'd like to see Hamilton. Find me one ticket under $60 and tell
me the best option — section, row, and price."* Two MCP providers configured, identical
MockHub inventory behind both: **TicketNexus** (descriptive tool names, thorough
descriptions) and **SeatStream** (tools named `search` and `seats`, one-word descriptions).
Host: Claude Code 2.1.222, headless. Raw transcripts in `runs/` (local only, not
committed — regenerate with `./grid/run-grid.sh`).

| Run | Model | Providers consulted | Behavior |
|---|---|---|---|
| 1–8 | Fable (default) | **both**, all 8 runs | Identical call order every run (nexus events → stream search → nexus listings → stream seats). Cross-checked inventory; disclosure of dual-sourcing varied run to run. |
| s1 | Sonnet | TicketNexus only | Never touched SeatStream; no disclosure that a second provider existed. |
| s2 | Sonnet | both (partially) | Checked SeatStream's events but never its listings. |
| s3 | Sonnet | TicketNexus only | Same as s1. |
| s4 | Sonnet | both | Thorough dual-sourcing, multiple listing calls. |
| h1 | Haiku | TicketNexus only, via subagent | Loaded ONLY TicketNexus tool references at the discovery step; SeatStream was never even loaded. Delegated the search to a subagent. |
| h2 | Haiku | TicketNexus only | Single listings call, no event search, no SeatStream. |
| h3 | Haiku | TicketNexus only, via subagent | As h1. |
| h4 | Haiku | TicketNexus only | Repeated TicketNexus calls; SeatStream never loaded. |

## What the grid shows

1. **Sourcing behavior is a property of the model, not the protocol.** The same prompt
   against the same two providers produced: consistent dual-sourcing (Fable), coin-flip
   single-vs-dual sourcing (Sonnet), and single-sourcing that never saw the second
   provider (Haiku).
2. **The better-documented provider wins by default.** No run preferred SeatStream. With
   the smallest model, SeatStream's tools were never even *loaded* — selection happened at
   the tool-discovery layer, before any reasoning about sourcing.
3. **Non-disclosure is the default failure.** Sonnet's single-provider runs presented
   their answer as "the best option" with no mention that an unqueried alternative existed.
4. **The harness is part of the sourcing pipeline.** The Haiku runs routed through deferred
   tool loading and a subagent — layers the customer never sees, all influencing which
   provider gets consulted.

None of these behaviors is a bug. Every run completed its task. That is the point:
MCP delivered interoperability with both providers; *nothing* specified marketplace
arbitration. If you want "search every eligible provider, rank, disclose" — that is a
policy you write (§3.2), not a property of the protocol.

## Reproduce

```bash
cd demos
./grid/run-grid.sh 8                 # default model
# add --model runs by editing the script or run claude -p manually
```
