# Agentic Commerce

Materials for the O'Reilly Live Learning course **"Agentic Commerce: Building Systems That
Let AI Agents Search, Decide, and Buy"** by [Ken Kousen](https://kousenit.com).

> Giving an agent tools is easy. Giving it bounded authority to conduct commerce safely is
> the real engineering problem.

The course builds the **front door** a site needs before an agent can safely cross the
checkout boundary — discovery → connectivity → identity → authorization → guardrails &
approval → payment authority → evidence — and breaks the naive version of each step
along the way.

## Students start here

1. **[setup.md](setup.md)** — ten minutes, before class. Pick one language track and run the
   checkpoint command.
2. **[labs.md](labs.md)** — both hands-on labs: prove a mandate boundary (Lab 1), then
   build your own guarded MCP tool (Lab 2).
3. **[spec-map.md](spec-map.md)** — every spec in the course on one page (MCP, AP2, ACP,
   UCP, TAP, BOTS Act), with primary sources.

**Slides:** [download the current PDF](https://github.com/kousen/agentic-commerce/releases/latest/download/agentic-commerce-slides.pdf)
— rebuilt automatically on every change.

## What's here

| Path | What it is |
|---|---|
| `slides.md` | The deck (Slidev). `npm install && npm run dev` to present. |
| `labs/{java,python,typescript}/` | **Lab 1** — the mandate-boundary contract, three languages |
| `labs/guarded-tool/` | **Lab 2** — build your own guarded MCP server (official SDKs, three tracks) |
| `examples/` | One runnable file per course principle: discovery docs, mandate check, purchase profile, idempotency, free-text boundary |
| `code-tour.md` | Where every pattern lives in the real platform (MockHub), with file:line |
| `course-client/` | The buyer's side: deterministic sourcing policy + evidence record, Spring Boot / Spring AI |
| `demos/` | MCP servers used in the live demos, runnable yourself |
| `demo-runbook.md` | Per-demo prompts, expected behavior, failure modes |
| `instructor-guide.md` | Run sheet and delivery notes |
| `agentic-commerce-course-design.md` | Course design and rationale (v2) |

Everything runs against a hosted mock ticket marketplace
([MockHub](https://mockhub.kousenit.com)) — except Lab 2, which runs entirely on your
machine. No accounts, API keys, Docker, or databases.

## The labs in one paragraph each

**Lab 1.** An AI agent shops for tickets on your behalf. The only authority it holds is a
**mandate** you created — a spending ceiling and an approval mode. The lab proves, with
tests you run, that the boundary is enforced by the platform rather than by the agent's
good behavior: the ceiling holds, a revoked mandate grants nothing, a purchase needing
approval cannot complete without one, and the agent's credential cannot approve anything.
You write the fifth test yourself — an agent cannot mint its own mandate.

**Lab 2.** You stand up a tiny MCP server of your own — official SDK, your language —
and write the guard between the agent and the money: five to ten lines of deterministic
authorization. One canned listing is priced to catch a guard that checks the subtotal
instead of the all-in total; the platform's most expensive real bug, waiting for you
personally. When it's green, connect Claude Code to your server and watch your own
refusal come back through the agent's mouth.

## Running the demos

```bash
cd demos && npm install

# Watch an agent approve its own purchase (§2.4)
claude --mcp-config grid/naive.json --strict-mcp-config

# The same boundary, guarded by protocol-level elicitation (§2.5)
npx tsx src/test-guarded-client.ts approve

# Two providers, identical inventory, different tool descriptions (§1.2)
./grid/run-grid.sh 8
```

See [demos/grid/GRID.md](demos/grid/GRID.md) for what sixteen recorded runs across three
models actually did — including the model that never loaded the second provider's tools at all.

## License

Course materials © Ken Kousen. Code samples are provided for course use.
