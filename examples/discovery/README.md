# Discovery — the two documents that make you findable

*Front Door step 1. Cost: nearly zero. Useful even if you build nothing else.*

This directory holds MockHub's **real, live discovery documents**, captured 2026-08-07
from the hosted instance. The live versions are canonical:

- https://mockhub.kousenit.com/llms.txt
- https://mockhub.kousenit.com/.well-known/agent.json

## `llms.txt` — the prose description

Markdown in a `.txt` file, served at the site root — `robots.txt`'s opposite number:
thirty years after we wrote a file to keep robots out, this one invites them in and
tells them the house rules.

What to notice, reading MockHub's:

- **It's the API described for a reader with no context.** Endpoints, parameters,
  response shapes, error format — the same information your OpenAPI spec has, in prose
  an LLM ingests in one pass.
- **The commerce rules are stated up front**: purchase requires a mandate, mandates
  have scopes and ceilings, approval mode may require a human. An agent that reads this
  file knows *before its first call* that it cannot simply buy things.
- **The tool list carries the descriptions the model will actually read** — including
  steering like "RECOMMENDED: compound search… reduces round-trips from 3 to 1."
  Discovery and tool design (step 2) meet here.

## `agent-card.json` — the machine-readable capability card

Served at `/.well-known/agent.json`: name, version, the MCP endpoint and transport,
the security scheme (OAuth 2.1 with Dynamic Client Registration), and named **skills**
with examples — the elevator pitch an agent platform can index.

What to notice:

- **`security_schemes` points at real metadata** (`oauth2_metadata_url`). A client you
  have never met follows it, registers itself via DCR, and authenticates — no manual
  onboarding. That is the observed behavior: publish two documents, and the client
  works out the rest.
- **Skills are goals, not endpoints** — "ticket-purchase," not "POST /orders." Each
  carries example utterances, which is documentation for a *model*, not a person.

## Generation beats authorship

MockHub generates the agent card from the code that serves the API, so it cannot drift.
The llms.txt is maintained prose — riskier, and worth a CI check that its endpoint list
matches the routes. A discovery document that lies is worse than none: agents act on it.

## Port it to your stack

1. Write `llms.txt` for your public API. Start from your OpenAPI spec; rewrite it as
   prose with the commerce rules first.
2. Serve `/.well-known/agent.json` naming your MCP endpoint (or, until you have one,
   just your documentation URL and security scheme).
3. Steps 3–7 of the Front Door can wait. These two files are already useful — agents
   are already looking.
