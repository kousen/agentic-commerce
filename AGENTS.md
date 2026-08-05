# Agentic Commerce Course — Agent Instructions

Shared instructions for all AI agents working on these materials (Claude Code and Codex/GPT 5.6 Sol both read this file; `CLAUDE.md` just points here).

## What this is

Materials for a 4-hour O'Reilly Live Learning course: **"Agentic Commerce: Building Systems That Let AI Agents Search, Decide, and Buy"** by Ken Kousen. Not started yet beyond design.

## Source of truth

- **`agentic-commerce-course-design.md` is canonical.** It contains the thesis, module structure, timings, cut list, and a "Decided" section. Read it before creating or editing any material.
- The O'Reilly Google Doc outline (ID `1t1ArGyKbRYvPY87-GCpMYTS1naXeMkbC7SWNqovsBWU`) is **stale by design** — it gets synced to match the design doc later. Don't conform materials to it.
- Timings in the design doc are planning estimates, not commitments. Ken welcomes structural and timing improvements — propose them, and fold accepted changes into the design doc.

## Resources

- **MockHub** (mock ticket marketplace, the course platform): code at `~/Documents/AI/mockhub`, deployed at https://mockhub.kousenit.com (Railway). The course will use a frozen release plus a small Java/Spring AI course client.
- **Prior conference talk** (week of 2026-07-28): `ticketnetwork-agentic-commerce/` — Slidev `slides.md`, `demo-script.md`, `demo-runbook.md`, `state-of-the-specs.md`. Reusable raw material, but the course is deliberately *not* the talk at 4× length (see design doc).
- Exported talk: `MockHub_agentic_commerce_redesign.pptx` and the zip of HTML/screenshots.

## Working agreements

- **Record decisions in the Decision Log below**, one line each, dated, with who/what/why. This is how decisions travel between Claude and Codex — if it's not in the design doc or this log, the other agent doesn't know it.
- Course-design decisions (structure, content, timing) go in the design doc's "Decided" section; process/tooling decisions go here.
- Lab code assertions must read as English (audience includes PMs and engineering leaders).
- Follow Ken's comparison rule: describe features, don't claim superiority over other languages/stacks without verifiable data.

## Decision Log

- 2026-08-01 (Ken): Local design doc is canonical; O'Reilly Google Doc gets updated afterwards.
- 2026-08-02 (Ken): Timings are not definitive; agents may adjust them.
- 2026-08-02 (Claude, Ken approved): Folded 8 design improvements into design doc — setup checkpoint, budgeted lab walkthrough, recorded elicitation demo for §2.5, recorded-run grid for §3.1, prediction poll in §3.3, module-end thesis slides, hosted-MockHub lab, English-legible lab assertions.
- 2026-08-02 (Ken): Both Claude Code and Codex will work on these materials; shared context lives in this file.
- 2026-08-05 (Ken): Everything ships in one public GitHub repo, `agentic-commerce`; this directory becomes that repo. No separate student repo.
- 2026-08-05 (Ken): Recordings are deferred until the materials are set; Ken will post them to his YouTube channel. No video files in the repo — demo-runbook.md gets links later.
- 2026-08-05 (Claude, Ken approved): `materials-plan.md` is the approved materials build plan (deliverables, spec-complexity firewall, polyglot lab tracks, build order). §1.0 MCP review folded into design doc. Repo is git-initialized; `ticketnetwork-agentic-commerce/` is gitignored pending Ken's call on publishing it.
- 2026-08-05 (Claude): Lab + setup checkpoint built and live-verified in all three tracks (`labs/{java,python,typescript}`, `labs.md`, `setup.md`); every assertion exercised against hosted MockHub. `spec-map.md` drafted from the talk's spec brief. Findings for the MockHub freeze recorded in `mockhub-course-requirements.md` — read it before Track C4/C5 work.
- 2026-08-05 (Claude): The three protected demos are working (`demos/`, TypeScript, MCP SDK v2): §2.4 self-approval verified headlessly via Claude Code, §2.5 MRTR elicitation proven end-to-end with a scripted host, §3.1 grid captured 16 runs across Fable/Sonnet/Haiku (`demos/grid/GRID.md` — divergence is model-dependent; small models never consulted the tersely-documented provider). `demo-runbook.md` has per-demo prompts and failure modes. Demo servers are TS for iteration speed; Track C4 may re-home them in the Java course client if Ken prefers.
