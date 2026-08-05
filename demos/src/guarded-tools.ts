// §2.5 demo: TicketHub "guarded" tool surface — the protocol answer.
//
// Same boundary as the naive server, but the approval is a 2026-07-28 multi-round-trip
// elicitation: buyTickets returns an input_required result, the QUESTION APPEARS IN THE
// HOST'S UI, the human answers, and the client re-issues the call with the response.
// There is no approval tool for the agent to call — the capability is not in its world.

import { McpServer, inputRequired, acceptedContent } from "@modelcontextprotocol/server";
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

const AGENT_ID = "guarded-demo-agent";

const token = await login();
const mandate = await ensureMandate(token, AGENT_ID, "APPROVAL_REQUIRED");

const approvalSchema = z.object({
  approve: z.boolean().meta({ title: "Approve this purchase" }),
});

serveStdio(() => {
  const server = new McpServer({ name: "tickethub-guarded", version: "1.0.0" });

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
    "buyTickets",
    {
      description:
        "Buy a specific listing on the customer's behalf under their mandate. " +
        "The customer approves or declines the purchase themselves; you cannot approve it.",
      inputSchema: z.object({
        listingId: z.number(),
        expectedPrice: z.number().describe("The listing price you are asking the customer to approve"),
      }),
    },
    async ({ listingId, expectedPrice }, ctx: any) => {
      const answer = acceptedContent(ctx.mcpReq.inputResponses, "approval", approvalSchema);

      if (answer == null) {
        // Round 1: stop, and put the question in front of the HUMAN.
        return inputRequired({
          inputRequests: {
            approval: inputRequired.elicit({
              message:
                `Your agent wants to buy listing ${listingId} for $${expectedPrice.toFixed(2)}. ` +
                `Approve this purchase?`,
              requestedSchema: approvalSchema,
            }),
          },
        });
      }

      if (!answer.approve) {
        return asText({ outcome: "DECLINED", message: "The customer declined the purchase." });
      }

      // Round 2, approved: record the human's consent as a real approval artifact,
      // then buy. Every step is auditable on MockHub.
      const checkout = await acpPost("/acp/v1/checkout", {
        buyerEmail: EMAIL,
        lineItems: [{ listingId, quantity: 1 }],
        agentId: AGENT_ID,
        mandateId: mandate.mandateId,
      });
      if (!checkout.ok) return asText(await checkout.json());
      const { checkoutId, pricing } = await checkout.json();

      const proposal = await userPost(token, "/api/v1/agent-approvals", {
        agentId: AGENT_ID,
        mandateId: mandate.mandateId,
        proposedOrderSnapshot: `listing ${listingId} via elicitation-approved purchase`,
        agentRationale: "Customer approved via host-UI elicitation",
        subtotal: pricing.subtotal,
        serviceFee: pricing.serviceFee,
        total: pricing.total,
      });
      const { approvalId } = await proposal.json();
      await userPost(token, `/api/v1/agent-approvals/${approvalId}/approve`, {});

      const completion = await acpPost(`/acp/v1/checkout/${checkoutId}/complete`, {
        agentId: AGENT_ID,
        mandateId: mandate.mandateId,
        approvalId,
      });
      if (!completion.ok) {
        // Don't leave the seat held by a checkout that can never complete.
        await acpPost(`/acp/v1/checkout/${checkoutId}/cancel`, {
          agentId: AGENT_ID,
          mandateId: mandate.mandateId,
        });
      }
      return asText(await completion.json());
    },
  );

  return server;
});
