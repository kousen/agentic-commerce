// Shared plumbing for the lab tests. Students should not need to edit this file.

export const BASE_URL = process.env.MOCKHUB_URL ?? "https://mockhub.kousenit.com";
export const ACP_KEY = process.env.MOCKHUB_ACP_KEY ?? "mockhub-dev-key";
export const EMAIL = process.env.MOCKHUB_EMAIL ?? "buyer@mockhub.com";
export const PASSWORD = process.env.MOCKHUB_PASSWORD ?? "buyer123";

// the AGENT's credential: an API key, no user login
export const AGENT_HEADERS = { "X-API-Key": ACP_KEY };

async function http(path: string, init?: RequestInit): Promise<Response> {
  try {
    return await fetch(BASE_URL + path, init);
  } catch (e) {
    throw new Error(
      `Could not reach MockHub at ${BASE_URL} — check your network, then ask in chat. (${e})`,
    );
  }
}

export const get = (path: string, headers: Record<string, string> = {}) =>
  http(path, { headers });

export const post = (path: string, body: unknown, headers: Record<string, string> = {}) =>
  http(path, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...headers },
    body: JSON.stringify(body),
  });

/** The HUMAN's credential: a JWT from the buyer's username and password. */
export async function login(): Promise<string> {
  const response = await post("/api/v1/auth/login", { email: EMAIL, password: PASSWORD });
  if (!response.ok) throw new Error(`login failed: ${response.status}`);
  return (await response.json()).accessToken;
}

/** Unique per run, so students sharing the demo account never collide. */
export const freshAgentId = () =>
  `lab-agent-${crypto.randomUUID().replaceAll("-", "").slice(0, 12)}`;

export async function createMandate(
  token: string,
  agentId: string,
  maxSpendPerTransaction: string,
  approvalMode: "AUTO_PURCHASE" | "APPROVAL_REQUIRED",
) {
  const response = await post(
    "/api/v1/my/mandates",
    { agentId, scope: "PURCHASE", maxSpendPerTransaction, approvalMode },
    { Authorization: `Bearer ${token}` },
  );
  if (!response.ok) throw new Error(`createMandate failed: ${response.status}`);
  return response.json();
}

export const revokeMandate = (token: string, mandateId: string) =>
  http(`/api/v1/my/mandates/${mandateId}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });

/** Any listing costing more than a dollar; a random page avoids seat contention. */
export async function pickAListing() {
  const page = Math.floor(Math.random() * 5);
  const response = await get(
    `/acp/v1/listings?size=10&minPrice=10&page=${page}`,
    AGENT_HEADERS,
  );
  if (!response.ok) throw new Error(`listings failed: ${response.status}`);
  const listings = (await response.json()).content;
  if (!listings?.length) throw new Error("MockHub should have listings for sale");
  return listings[Math.floor(Math.random() * listings.length)];
}

/** The agent's purchase path: ACP checkout, carrying its mandate. */
export const agentTriesToBuy = (agentId: string, mandateId: string, listingId: number) =>
  post(
    "/acp/v1/checkout",
    {
      buyerEmail: EMAIL,
      lineItems: [{ listingId, quantity: 1 }],
      agentId,
      mandateId,
    },
    AGENT_HEADERS,
  );

export const agentTriesToComplete = (checkoutId: string, agentId: string, mandateId: string) =>
  post(
    `/acp/v1/checkout/${checkoutId}/complete`,
    { agentId, mandateId },
    { ...AGENT_HEADERS, "X-Buyer-Email": EMAIL },
  );

export const cancelCheckout = (checkoutId: string, agentId: string, mandateId: string) =>
  post(
    `/acp/v1/checkout/${checkoutId}/cancel`,
    { agentId, mandateId },
    { ...AGENT_HEADERS, "X-Buyer-Email": EMAIL },
  );
