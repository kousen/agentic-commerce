// Shared MockHub plumbing for the demo MCP servers.

export const BASE_URL = process.env.MOCKHUB_URL ?? "https://mockhub.kousenit.com";
export const ACP_KEY = process.env.MOCKHUB_ACP_KEY ?? "mockhub-dev-key";
export const EMAIL = process.env.MOCKHUB_EMAIL ?? "buyer@mockhub.com";
export const PASSWORD = process.env.MOCKHUB_PASSWORD ?? "buyer123";

const ACP_HEADERS = { "X-API-Key": ACP_KEY };

async function http(path: string, init: RequestInit = {}): Promise<Response> {
  return fetch(BASE_URL + path, init);
}

export async function acpGet(path: string): Promise<any> {
  const response = await http(path, { headers: ACP_HEADERS });
  if (!response.ok) throw new Error(`${path} -> ${response.status}`);
  return response.json();
}

export async function acpPost(path: string, body: unknown): Promise<Response> {
  return http(path, {
    method: "POST",
    headers: { ...ACP_HEADERS, "Content-Type": "application/json", "X-Buyer-Email": EMAIL },
    body: JSON.stringify(body),
  });
}

/** Log in as the demo buyer; returns the human's JWT. */
export async function login(): Promise<string> {
  const response = await http("/api/v1/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: EMAIL, password: PASSWORD }),
  });
  if (!response.ok) throw new Error(`login -> ${response.status}`);
  return (await response.json()).accessToken;
}

export async function userGet(token: string, path: string): Promise<any> {
  const response = await http(path, { headers: { Authorization: `Bearer ${token}` } });
  if (!response.ok) throw new Error(`${path} -> ${response.status}`);
  return response.json();
}

export async function userPost(token: string, path: string, body: unknown): Promise<Response> {
  return http(path, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

/** Find or create an ACTIVE mandate for this agent (demo bootstrap, printed to stderr). */
export async function ensureMandate(
  token: string,
  agentId: string,
  approvalMode: "AUTO_PURCHASE" | "APPROVAL_REQUIRED",
  ceiling = "500.00",
): Promise<{ mandateId: string }> {
  const mandates = await userGet(token, "/api/v1/my/mandates");
  const existing = mandates.find(
    (m: any) => m.agentId === agentId && m.status === "ACTIVE" && m.approvalMode === approvalMode,
  );
  if (existing) {
    console.error(`[demo] using existing mandate ${existing.mandateId} for ${agentId}`);
    return existing;
  }
  const response = await userPost(token, "/api/v1/my/mandates", {
    agentId,
    scope: "PURCHASE",
    maxSpendPerTransaction: ceiling,
    approvalMode,
  });
  if (!response.ok) throw new Error(`create mandate -> ${response.status}`);
  const created = await response.json();
  console.error(`[demo] created mandate ${created.mandateId} for ${agentId} (${approvalMode}, ceiling $${ceiling})`);
  return created;
}

/** Compact listing shape so tool results stay small. */
export function compactListing(l: any) {
  return {
    listingId: l.listingId,
    event: l.eventName,
    date: l.eventDate,
    venue: l.venue,
    city: l.city,
    section: l.sectionName ?? l.section,
    row: l.rowLabel ?? l.row,
    seat: l.seatNumber ?? l.seat,
    price: l.price ?? l.unitPrice,
  };
}

export function asText(value: unknown) {
  return { content: [{ type: "text" as const, text: JSON.stringify(value, null, 1) }] };
}
