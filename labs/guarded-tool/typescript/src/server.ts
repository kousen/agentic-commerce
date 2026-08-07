// Scaffolding — the MCP wiring. Two tools, over stdio, using the official SDK.
//
// Count the tools. There is no approvePurchase, no raiseCeiling, no overrideMandate.
// The boundary is not something the agent negotiates with; it's the shape of its world.
//
// Run it yourself:            npm run serve
// Connect a real agent to it: claude --mcp-config mcp-config.json --strict-mcp-config

import { McpServer } from "@modelcontextprotocol/server";
import { serveStdio } from "@modelcontextprotocol/server/stdio";
import * as z from "zod/v4";
import { COURSE_MANDATE, LISTINGS, handleBuy } from "./shop.js";

serveStdio(() => {
  const server = new McpServer({ name: "guarded-ticket-shop", version: "1.0.0" });

  server.registerTool(
    "searchListings",
    {
      description:
        "List the available ticket listings with all-in pricing (subtotal plus service fee).",
      inputSchema: z.object({}),
    },
    async () => ({
      content: [{ type: "text", text: JSON.stringify(LISTINGS, null, 2) }],
    }),
  );

  server.registerTool(
    "buyTickets",
    {
      description:
        "Buy one listing on the customer's behalf under their mandate. Refusals state the " +
        "constraint. Pass a fresh idempotencyKey per purchase; reuse the SAME key when retrying.",
      inputSchema: z.object({
        listingId: z.number(),
        idempotencyKey: z.string(),
      }),
    },
    async ({ listingId, idempotencyKey }) => ({
      content: [{
        type: "text",
        text: JSON.stringify(handleBuy(COURSE_MANDATE, listingId, idempotencyKey)),
      }],
    }),
  );

  return server;
});
