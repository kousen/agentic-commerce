# Training Materials Plan (APPROVED — Ken, 2026-08-05)

Plan for building the actual course materials from `agentic-commerce-course-design.md` (canonical).
Follows the file conventions of Ken's existing courses (claude-code-training, codex-training,
spring-and-spring-boot). Once approved, move accepted decisions to the AGENTS.md Decision Log.

## Audience baseline (assumed, per Ken 2026-08-05)

Everyone knows coding agents; most have used (some have built) MCP servers. Nobody is assumed
to know the commerce specs. Consequences:

- **MCP gets a compact review, not a from-scratch tutorial** (Ken, 2026-08-05). Folded into
  the design doc as §1.0 "MCP in five slides" (5 min) before the naive tool; Module 1 is now
  50 min, paid from slack (20→15). §1.1 lands on shared vocabulary; §1.4 stays as-is.
- **No commerce spec is introduced before its problem is felt.** The design doc already
  sequences this; the materials must not backslide by front-loading definitions.

## The spec-complexity firewall

The stated danger: the specifications get complicated fast. Three rules for every artifact:

1. **Concept names first, spec names once.** Teach *mandate, approval, evidence, profile* in
   plain language. Each module gets exactly one "here's what the specs call this" slide;
   acronyms (AP2, ACP, SEP-2322, MRTR) never appear on a teaching slide's headline.
2. **Spec detail lives in the handout, not the deck.** A one-page `spec-map.md` (glossary +
   which-spec-solves-what table + links) absorbs all acronym pressure. Slides may point at it;
   they never reproduce it. The talk's `state-of-the-specs.md` becomes a take-home appendix,
   not slide content.
3. **Hard slide budgets for protocol material:** Module 1 §1.4 ≤ 3 protocol slides; §2.5 is the
   *only* place an MRTR payload appears (one diagram + the recorded demo); §3.4's MCP/ACP
   comparison is one slide + a pointer (already on the cut list). If a protocol point can't fit
   the budget, it goes to spec-map.md.

## Deliverables

| File | Contents | Modeled on |
|---|---|---|
| `slides.md` | Slidev, seriph theme, same frontmatter/page-number conventions as claude-code-training. Structure = design doc segments; the three thesis slides are literal chapter-boundary slides. | claude-code-training/slides.md |
| `labs.md` | Setup checkpoint (the one command + expected output), the §2.6 mandate-boundary lab with predict-then-run fallback inline, then take-home exercises (runnable versions of each demo, clearly marked "after class"). | spring-and-spring-boot/labs.md |
| `instructor-guide.md` | Minute-by-minute run sheet with the cut list embedded *at the decision points* (e.g. "02:50 — if behind, drop to predict-then-run now"), per-segment prompts, chat-triage script for the setup checkpoint. | claude-code-training/instructor-guide.md |
| `demo-runbook.md` | Extends the talk's runbook: per demo — exact prompt, expected agent behavior, known failure modes, recording filename, cue for cutting to tape. | ticketnetwork-agentic-commerce/demo-runbook.md |
| `spec-map.md` | The complexity firewall handout (above). | claude-code-training/glossary.md |
| `setup.md` | Student prerequisites + checkpoint command, linked from the registration/pre-class email. | codex-training/exercises/SETUP.md |
| `labs/{java,python,typescript}/` | The three lab tracks (see Polyglot section). | codex-training/exercises/ |
| `examples/` | Small pattern excerpts *referencing* MockHub, not rebuilding it: the `PurchaseProfile` validation boundary, a mandate check, an idempotency key, the injection filter. One file each, runnable where cheap. | — |
| `course-client/` | The small Java/Spring AI course client — just big enough for the demos and lab (resolves former Open #1: it lives here, not in the MockHub repo; MockHub's repo keeps only the frozen server release). | — |

Deliberately **not** building: per-module exercise projects (one client + one lab is the design),
a printed lab handout separate from labs.md, webinar variants. **No recordings until the
materials are set** (Ken, 2026-08-05): demo-runbook.md tracks *what* each demo must show and
its fallback cue, with recording links added later when Ken posts them to his YouTube channel.
No MP4s in the repo, ever.

## The GitHub repo

Everything ships in **one public repo, `agentic-commerce`** (Ken, 2026-08-05) — this directory
becomes it. Matches the other courses, where even the instructor guide is public.

- `git init` here; `.gitignore` from the start: `node_modules/`, `dist/`, `*.zip`, `*.pptx`.
- **Flag before pushing:** `ticketnetwork-agentic-commerce/` carries a client name from the
  conference engagement. Recommend keeping it out of the public repo (local raw material only)
  unless Ken confirms it's fine to publish.
- MockHub itself is never vendored in — it stays a hosted service the labs point at, plus the
  small excerpts in `examples/`.

## Build order (risk-driven, not document-driven)

1. **Lab + setup checkpoint first.** Depends on the longest external chain (MockHub Tracks C4
   course client, C5 frozen release — design doc says freeze C5 first). Writing the lab surfaces
   exactly what the course client must expose; the checkpoint command falls out of it for free.
   ✅ *Done 2026-08-05 — all three tracks green against hosted MockHub; freeze requirements
   captured in `mockhub-course-requirements.md`.*
2. **Get the three protected demos working** (§2.4 self-approval, §2.5 elicitation, §3.1
   two-provider grid): working demo path + exact prompts + runbook entry each. They're
   nondeterministic and need iteration time; §2.5 also resolves design-doc Open #3 (which
   client can run it — whichever works first wins). The §3.1 grid needs 8–10 captured runs
   (transcripts/screenshots suffice until recording time), which is real calendar time.
   ✅ *Done 2026-08-05 — `demos/` servers + `demo-runbook.md`; §2.4 self-approval captured
   headlessly, §2.5 proven end-to-end on the TS v2 SDK, §3.1 grid run 16× across 3 models
   (`demos/grid/GRID.md`). One pre-class task remains: verify Claude Code renders the
   §2.5 elicitation interactively.*
3. **Remaining demos** (§1.2 confused deputy, §2.1 ambiguity, §3.3 injection).
   ✅ *Done 2026-08-05 — all three built course-side; none required MockHub changes first.
   §3.3's lesson changed shape (see design doc + runbook).*
4. **slides.md**, written around demos that already exist. Slides written before demos always
   get rewritten. When it lands, run the `slidev-pdf-release` skill to add the PDF-on-push
   GitHub Action (not before — the action would fail on a repo with no slides).
   ✅ *Done 2026-08-05 — 94 slides, Slidev/seriph, Mermaid diagrams for the MCP shape, tool-call
   lifecycle, PurchaseProfile boundary, and the MRTR round-trip. PDF workflow live; rolling
   download at `releases/latest/download/agentic-commerce-slides.pdf`.*
5. **instructor-guide.md + demo-runbook.md** finalized from what the rehearsal actually looked
   like; spec-map.md accumulates throughout.
6. **Recordings, then O'Reilly Google Doc sync, last.** Recording happens only after the
   materials are set; Ken records and posts to YouTube, links go into demo-runbook.md.

## Polyglot accommodation (Ken, 2026-08-05: many students prefer Python/JS)

The design already contains most of the answer: students only *type* code in one place (§2.6),
and that lab runs against **hosted MockHub over HTTP** — so the client language is almost
irrelevant. Accommodate at the lab, not everywhere:

- **Instructor path stays Spring Boot 4 + Spring AI 2 + React.** Demos are watched, not typed.
  Slides label snippets by concept ("the validation boundary"), keep them short enough to read
  as pseudocode, and never argue Java is the right choice — it's simply the instructor's stack.
- **The lab ships in three parallel tracks: Java/JUnit (canonical), Python/pytest,
  TypeScript/Vitest.** It's ~5–6 deterministic assertions against a frozen HTTP API; the
  English-readable-assertion rule already required names like
  `assert_agent_cannot_approve_own_purchase` — that constraint now does double duty as the
  cross-language contract. The frozen release (Track C5) is what makes 3× maintenance cheap:
  the API can't drift under the ports.
- **The setup checkpoint becomes per-track**: one command per language that exercises the
  student's chosen lab track end-to-end (`./gradlew check` / `pytest -k checkpoint` /
  `npm test -- checkpoint`), each printing the same one pasteable line. Students pick a track
  at setup time; labs.md presents the three side by side.
- **Do NOT port the full Spring AI course client.** Only the lab gets ported. The client is
  reference material for the take-home exercises; its README points Python/JS students at the
  official MCP Python/TypeScript SDKs for reimplementing the patterns on their own stack.
- Take-home exercises are written as *behavior specs* ("make the injection demo fail against
  your own client") rather than Java instructions, so they're stack-portable by construction.

## Open questions for Ken

1. Does `ticketnetwork-agentic-commerce/` (client name) go into the public repo, or stay
   local-only raw material?
2. Approve the proposed §1.0 MCP-review placement (Module 1 45→50 min, from slack) so it can
   be folded into the design doc.
