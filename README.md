# Agentic Commerce

Materials for the O'Reilly Live Learning course **"Agentic Commerce: Building Systems That
Let AI Agents Search, Decide, and Buy"** by [Ken Kousen](https://kousenit.com).

> Giving an agent tools is easy. Giving it bounded authority to conduct commerce safely is
> the real engineering problem.

## Students start here

1. **[setup.md](setup.md)** — ten minutes, before class. Pick one language track and run the
   checkpoint command.
2. **[labs.md](labs.md)** — the hands-on lab.
3. **[spec-map.md](spec-map.md)** — every acronym in the course on one page (MCP, AP2, ACP,
   UCP, TAP, BOTS Act), with primary sources.

**Slides:** [download the current PDF](https://github.com/kousen/agentic-commerce/releases/latest/download/agentic-commerce-slides.pdf)
— rebuilt automatically on every change.

## What's here

| Path | What it is |
|---|---|
| `slides.md` | The deck (Slidev). `npm install && npm run dev` to present. |
| `labs/{java,python,typescript}/` | The mandate-boundary lab, same contract in three languages |
| `demos/` | MCP servers used in the live demos, runnable yourself |
| `demo-runbook.md` | Per-demo prompts, expected behavior, failure modes |
| `instructor-guide.md` | Run sheet and delivery notes |
| `agentic-commerce-course-design.md` | Course design and rationale |

Everything runs against a hosted mock ticket marketplace
([MockHub](https://mockhub.kousenit.com)). No accounts, API keys, Docker, or databases.

## The lab in one paragraph

An AI agent shops for tickets on your behalf. The only authority it holds is a **mandate**
you created — a spending ceiling and an approval mode. The lab proves, with tests you run,
that the boundary is enforced by the platform rather than by the agent's good behavior: the
ceiling holds, a revoked mandate grants nothing, a purchase needing approval cannot complete
without one, and the agent's own credential cannot approve anything. You write the fifth
test yourself — the one proving an agent cannot mint its own mandate.

## Running the demos

```bash
cd demos && npm install

# Watch an agent approve its own purchase (§2.4)
claude --mcp-config grid/naive.json --strict-mcp-config

# The same boundary, guarded by protocol-level elicitation (§2.5)
npx tsx src/test-guarded-client.ts approve

# Two providers, identical inventory, different tool descriptions (§3.1)
./grid/run-grid.sh 8
```

See [demos/grid/GRID.md](demos/grid/GRID.md) for what sixteen recorded runs across three
models actually did — including the model that never loaded the second provider's tools at all.

## License

Course materials © Ken Kousen. Code samples are provided for course use.
