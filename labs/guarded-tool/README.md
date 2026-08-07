# Lab 2 — Expose a guarded tool

*The construction lab. Lab 1 proved someone else's boundary holds; this one is yours.*

You stand up a **tiny MCP server of your own** — a ticket shop with two tools — and
write the guard: the deterministic authorization decision that runs between the agent
and the money. The scaffold is complete except for one function. Full instructions are
in [`labs.md`](../../labs.md); this file is the quick reference.

| Track | Setup | Test | Run the server |
|---|---|---|---|
| [`typescript/`](typescript/) | `npm install` | `npm test` | `npm run serve` |
| [`python/`](python/) | `python3 -m venv .venv && .venv/bin/pip install -r requirements.txt` | `.venv/bin/pytest -v` | `.venv/bin/python server.py` |
| [`java/`](java/) | nothing (Gradle wrapper) | `./gradlew test` | `./gradlew installDist`, then see mcp-config.json |

Each track uses the **official MCP SDK** for its language, all on the same 2.0
generation. Everything runs locally — no network, no MockHub, no credentials.

## The shape of the lab

1. `<test command>` — one test passes (the tool surface: there is **no approval tool**
   for the agent to call), five fail. The red sentences are your worklist.
2. Open the `guard` file — the only file you edit — and write `authorize` (5–10 lines).
3. Tests green. You have now built the thing the whole course describes: a boundary
   that evaluates in deterministic code, on structured fields, in the customer's units.

One listing in the canned inventory is priced to catch a guard that checks the
subtotal instead of the all-in total. If four tests pass and the units test doesn't,
you have just reproduced MockHub's most expensive bug — see §2.2 in the slides.

## After class: connect a real agent

From your track's directory:

```bash
claude --mcp-config mcp-config.json --strict-mcp-config
```

> Buy me the cheapest Spinners ticket. Then get the floor seats too.

Watch your own refusal come back through the agent's mouth — and notice it relays the
constraint accurately, because you gave it one to relay.

Instructor solutions: [`SOLUTIONS.md`](SOLUTIONS.md) (no peeking until your tests are red for reasons you understand).
