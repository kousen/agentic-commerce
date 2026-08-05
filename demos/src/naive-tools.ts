// §2.4 demo: TicketHub "naive" tool surface.
//
// This server makes one architectural mistake on purpose: it exposes approvePurchase
// as a tool, backed by the human's own login session (JWT). Everything it does is a
// legitimate MockHub API call — the flaw is WHERE the approval capability lives.
// Nothing malfunctions in this demo. The architecture permits it.

import { McpServer } from "@modelcontextprotocol/server";
import { serveStdio } from "@modelcontextprotocol/server/stdio";
import * as z from "zod/v4";
import {
  acpGet,
  acpPost,
  asText,
  compactListing,
  ensureMandate,
  login,
  userPost,
  EMAIL,
} from "./mockhub.js";

const AGENT_ID = "naive-demo-agent";

const token = await login();
const mandate = await ensureMandate(token, AGENT_ID, "APPROVAL_REQUIRED");

async function bodyOf(response: Response) {
  const text = await response.text();
  try {
    return JSON.parse(text);
  } catch {
    return { raw: text };
  }
}

serveStdio(() => {
  const server = new McpServer({ name: "tickethub", version: "1.0.0" });

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
      const params = new URLSearchParams({ query, size: "8" });
      if (maxPrice != null) params.set("maxPrice", String(maxPrice));
      const page = await acpGet(`/acp/v1/listings?${params}`);
      return asText(page.content.map(compactListing));
    },
  );

  server.registerTool(
    "createCheckout",
    {
      description: "Hold a listing in a checkout under your purchase mandate. Returns a checkoutId.",
      inputSchema: z.object({ listingId: z.number() }),
    },
    async ({ listingId }) => {
      const response = await acpPost("/acp/v1/checkout", {
        buyerEmail: EMAIL,
        lineItems: [{ listingId, quantity: 1 }],
        agentId: AGENT_ID,
        mandateId: mandate.mandateId,
      });
      return asText(await bodyOf(response));
    },
  );

  server.registerTool(
    "completeCheckout",
    {
      description:
        "Complete a held checkout and place the order. If the mandate requires approval, " +
        "pass the approvalId of an approved purchase approval.",
      inputSchema: z.object({
        checkoutId: z.string(),
        approvalId: z.string().optional(),
      }),
    },
    async ({ checkoutId, approvalId }) => {
      const response = await acpPost(`/acp/v1/checkout/${checkoutId}/complete`, {
        agentId: AGENT_ID,
        mandateId: mandate.mandateId,
        ...(approvalId ? { approvalId } : {}),
      });
      return asText(await bodyOf(response));
    },
  );

  server.registerTool(
    "proposePurchase",
    {
      description:
        "Create a purchase approval request for a planned purchase. Returns an approvalId " +
        "with status PROPOSED.",
      inputSchema: z.object({
        total: z.number().describe("Total purchase amount in dollars"),
        description: z.string().describe("What is being purchased and why"),
      }),
    },
    async ({ total, description }) => {
      const response = await userPost(token, "/api/v1/agent-approvals", {
        agentId: AGENT_ID,
        mandateId: mandate.mandateId,
        proposedOrderSnapshot: description,
        agentRationale: description,
        subtotal: total,
        serviceFee: 0,
        total,
      });
      return asText(await bodyOf(response));
    },
  );

  // The mistake, stated in one line: this tool exists.
  server.registerTool(
    "approvePurchase",
    {
      description: "Approve a pending purchase approval by approvalId so the purchase can proceed.",
      inputSchema: z.object({ approvalId: z.string() }),
    },
    async ({ approvalId }) => {
      const response = await userPost(token, `/api/v1/agent-approvals/${approvalId}/approve`, {});
      return asText(await bodyOf(response));
    },
  );

  return server;
});
