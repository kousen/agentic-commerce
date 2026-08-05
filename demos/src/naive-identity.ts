// §1.2 demo: the confused deputy in one tool signature.
//
// This server is the tool anyone would write first: buyTickets(userEmail, listingId, qty).
// It holds live sessions for TWO customers and trusts whatever email the agent types.
// Anything the agent can type is an assertion, not a credential — the server is the
// confused deputy, spending one user's session on another user's name.

import { McpServer } from "@modelcontextprotocol/server";
import { serveStdio } from "@modelcontextprotocol/server/stdio";
import * as z from "zod/v4";
import { acpGet, asText, compactListing, BASE_URL } from "./mockhub.js";

const DEMO_USERS: Record<string, { password: string; first: string; last: string }> = {
  "alice@mockhub.com": { password: "alicedemo123", first: "Alice", last: "Demo" },
  "bob@mockhub.com": { password: "bobdemo123", first: "Bob", last: "Demo" },
};

const sessions = new Map<string, string>();

async function post(path: string, body: unknown, token?: string): Promise<Response> {
  return fetch(BASE_URL + path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  });
}

/** Login, registering the demo account on first use. */
async function sessionFor(email: string): Promise<string> {
  const cached = sessions.get(email);
  if (cached) return cached;
  const user = DEMO_USERS[email];
  if (!user) throw new Error(`No demo session for ${email} — use alice@mockhub.com or bob@mockhub.com`);

  let response = await post("/api/v1/auth/login", { email, password: user.password });
  if (!response.ok) {
    const registered = await post("/api/v1/auth/register", {
      email,
      password: user.password,
      firstName: user.first,
      lastName: user.last,
    });
    if (!registered.ok) throw new Error(`register ${email} -> ${registered.status}`);
    response = registered;
    console.error(`[demo] registered ${email}`);
  }
  const token = (await response.json()).accessToken;
  sessions.set(email, token);
  return token;
}

serveStdio(() => {
  const server = new McpServer({ name: "tickethub-naive-v1", version: "1.0.0" });

  server.registerTool(
    "searchListings",
    {
      description: "Search ticket listings by event or artist name, with an optional max price.",
      inputSchema: z.object({
        query: z.string(),
        maxPrice: z.number().optional(),
      }),
    },
    async ({ query, maxPrice }) => {
      // Filter on price here: MockHub's maxPrice also excludes at the event level.
      const page = await acpGet(`/acp/v1/listings?query=${encodeURIComponent(query)}&size=50`);
      const listings = page.content
        .map(compactListing)
        .filter((l: any) => maxPrice == null || l.price <= maxPrice);
      return asText(listings.slice(0, 8));
    },
  );

  // The naive tool, verbatim from the slide: identity is a parameter.
  server.registerTool(
    "buyTickets",
    {
      description: "Buy a listed ticket for a customer. Provide the customer's email, the listingId, and quantity.",
      inputSchema: z.object({
        userEmail: z.string(),
        listingId: z.number(),
        quantity: z.number().default(1),
      }),
    },
    async ({ userEmail, listingId }) => {
      const token = await sessionFor(userEmail);
      const added = await post("/api/v1/cart/items", { listingId }, token);
      if (!added.ok) return asText(await added.json());
      const order = await post("/api/v1/orders/checkout", { paymentMethod: "mock" }, token);
      const body = await order.json();
      return asText({
        placedFor: userEmail,
        orderNumber: body.orderNumber ?? body,
        status: body.status,
        total: body.total,
      });
    },
  );

  server.registerTool(
    "getMyOrders",
    {
      description: "List a customer's recent orders. Provide the customer's email.",
      inputSchema: z.object({ userEmail: z.string() }),
    },
    async ({ userEmail }) => {
      const token = await sessionFor(userEmail);
      const response = await fetch(`${BASE_URL}/api/v1/orders?size=5`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return asText(await response.json());
    },
  );

  return server;
});
