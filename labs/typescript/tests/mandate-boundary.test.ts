// LAB — The mandate boundary test.
//
// Each test states a sentence of the contract between a customer and their buying agent,
// then proves the platform enforces it. Read the test names out loud: they are the lesson.
//
// Design by contract:
//   precondition   — a mandate exists, and it is the ONLY authority the agent holds
//   postcondition  — every purchase inside the mandate can succeed; everything outside is refused
//   invariant      — nothing the agent can do changes the mandate's boundaries

import { beforeAll, expect, test } from "vitest";
import {
  AGENT_HEADERS,
  agentTriesToBuy,
  agentTriesToComplete,
  cancelCheckout,
  createMandate,
  freshAgentId,
  login,
  pickAListing,
  post,
  revokeMandate,
} from "./lab";

let token: string;

beforeAll(async () => {
  token = await login();
});

test("the mandate ceiling holds — a purchase above the limit is refused, not negotiated", async () => {
  const agentId = freshAgentId();
  const aDollarMandate = await createMandate(token, agentId, "1.00", "AUTO_PURCHASE");
  const somethingPricier = await pickAListing();

  const response = await agentTriesToBuy(
    agentId,
    aDollarMandate.mandateId,
    somethingPricier.listingId,
  );

  expect(response.status, "the purchase should be refused").toBe(409);
  expect((await response.json()).detail).toContain("Mandate does not authorize");

  await revokeMandate(token, aDollarMandate.mandateId);
});

test("a revoked mandate grants nothing — yesterday's authority is not today's", async () => {
  const agentId = freshAgentId();
  const mandate = await createMandate(token, agentId, "500.00", "AUTO_PURCHASE");
  await revokeMandate(token, mandate.mandateId);

  const listing = await pickAListing();
  const response = await agentTriesToBuy(agentId, mandate.mandateId, listing.listingId);

  expect(response.status, "the purchase should be refused").toBe(409);
  expect((await response.json()).detail).toContain("No active mandate");
});

test("a purchase needing approval cannot complete without one — no 'no' is not a 'yes'", async () => {
  const agentId = freshAgentId();
  const mandate = await createMandate(token, agentId, "500.00", "APPROVAL_REQUIRED");

  // A listing can be momentarily held by a classmate's run; try a few.
  let checkout!: Response;
  for (let attempt = 0; attempt < 3; attempt++) {
    const listing = await pickAListing();
    checkout = await agentTriesToBuy(agentId, mandate.mandateId, listing.listingId);
    if (checkout.status === 201) break;
  }
  expect(checkout.status, "staging the checkout is allowed").toBe(201);
  const { checkoutId } = await checkout.json();

  const response = await agentTriesToComplete(checkoutId, agentId, mandate.mandateId);

  expect(response.status, "completion should be refused").toBe(409);
  expect((await response.json()).detail).toContain("requires an approved purchase approval");

  await cancelCheckout(checkoutId, agentId, mandate.mandateId);
  await revokeMandate(token, mandate.mandateId);
});

test("the agent's credential cannot approve a purchase — an approval the agent can invoke is not an approval", async () => {
  // The approve endpoint exists — but it accepts only the human's login, never the
  // agent's API key. Same action, different door, and the agent's key does not fit.
  const response = await post(
    "/api/v1/agent-approvals/any-proposal/approve",
    {},
    AGENT_HEADERS, // the AGENT's credential — deliberately not a user JWT
  );

  expect(response.status, "the agent's credential should not open this door").toBe(401);
});

test.skip("YOUR TURN: the agent's credential cannot mint a mandate — authority comes from artifacts the model cannot mint", async () => {
  // Prove it: try to CREATE a mandate (POST /api/v1/my/mandates) using only the
  // agent's credential (AGENT_HEADERS), and assert the platform refuses with 401.
  // The test above is the pattern; this is the same idea one level deeper.
  throw new Error("write me");
});
