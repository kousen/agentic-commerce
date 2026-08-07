# Spec Map — every spec from the course, on one page

The course's Vocabulary segment introduces each specification — who's behind it, what
problem it solves, what layer it occupies. This page is the depth behind those slides:
concept-to-spec mapping, one-paragraph histories, and primary sources, with status as of
**August 2026**. The concepts are stable; the specifications are moving — when a status
matters, check the primary source.

## Course concept → specification

| Course concept | Where the specs address it |
|---|---|
| **Tool boundary** (what the agent can call) | MCP — tools, the `2026-07-28` stateless core |
| **Mandate** (bounded authority to act and to pay) | AP2 — Checkout Mandate + Payment Mandate as verifiable digital credentials; v0.2 adds "Human Not Present" autonomous purchases |
| **Approval the agent cannot invoke** | MCP Multi Round-Trip Requests (SEP-2322): the server returns `InputRequiredResult`, the *client* gathers the human's answer and re-issues the call |
| **Inline approval UI** | MCP Apps extension — server-rendered UI in a sandboxed iframe, actions flow through the same consent path as a tool call |
| **Checkout as a protocol** | ACP (OpenAI + Stripe) — checkout only; UCP (Google + Shopify) — the whole journey, composing MCP + A2A + AP2 |
| **Agent identity at the door** | Visa Trusted Agent Protocol (TAP) + Cloudflare Web Bot Auth — RFC 9421 HTTP Message Signatures, Ed25519, distinguishes browsing from paying |
| **Machine-to-machine payment** | x402 (Coinbase) — HTTP 402 micropayments; not on the critical path for card-based retail |
| **The legal boundary** | BOTS Act (2016, actively enforced since 2025) — no legal distinction yet between a delegated consumer agent and a scalper bot |

## The specs in one paragraph each

**MCP — Model Context Protocol.** Governed by the Agentic AI Foundation (Linux Foundation)
since December 2025. The `2026-07-28` release removed protocol-level sessions — MCP now
behaves like an ordinary HTTP API. Elicitation became multi-round-trip and stateless;
sampling entered deprecation; MCP Apps and Tasks became official extensions. The most
broadly adopted spec in this list by a wide margin.

**AP2 — Agent Payments Protocol.** Started at Google, donated to the FIDO Alliance April
2026 with ~60 organizations. Defines mandates as verifiable digital credentials — artifacts
the model cannot mint, which is why this course's mandate concept maps to it. Strong
institutional backing; production deployments still pilot-stage.

**ACP — Agentic Commerce Protocol.** OpenAI + Stripe, checkout-focused, still beta and not
yet foundation-governed. Its flagship surface (ChatGPT Instant Checkout) was retired in
March 2026 after roughly a dozen merchants went live; the spec survives via Stripe's
Agentic Commerce Suite. A useful cautionary tale about protocol vs. product.

**UCP — Universal Commerce Protocol.** Google + Shopify, announced January 2026, Apache 2.0.
Broader than ACP: discovery through order management, with MCP, A2A, and AP2 support built
in — a composition layer, not a competitor. Live on Google surfaces; the most commercially
active of the commerce protocols.

**TAP — Visa Trusted Agent Protocol** (+ Cloudflare Web Bot Auth). Cryptographic agent
identity in HTTP headers, so a marketplace can admit delegated consumer agents while
blocking scalper bots. The industry's technical answer to a question the law hasn't
answered yet.

**BOTS Act.** The 2016 U.S. statute against circumventing ticket-purchase controls, newly
enforced since a 2025 executive order (the FTC sued Ticketmaster/Live Nation under it).
No carve-out exists for consumer-delegated agents — the same behavioral signature covers
both. Industry infrastructure (TAP, mandates) is being built to make the distinction the
law doesn't.

## Primary sources

- MCP: https://modelcontextprotocol.io/specification/latest
- AP2: https://ap2-protocol.org/
- ACP: https://github.com/agentic-commerce-protocol/agentic-commerce-protocol
- UCP: https://ucp.dev/
- TAP / Web Bot Auth: https://blog.cloudflare.com/secure-agentic-commerce/
- BOTS Act enforcement: https://www.ftc.gov/ (search "BOTS Act")

*Statuses move fast in this space. This page was accurate in August 2026; check the
primary sources before relying on any status claim.*
