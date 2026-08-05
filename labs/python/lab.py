"""Shared plumbing for the lab tests. Students should not need to edit this file."""

import os
import uuid

import requests

BASE_URL = os.environ.get("MOCKHUB_URL", "https://mockhub.kousenit.com")
ACP_KEY = os.environ.get("MOCKHUB_ACP_KEY", "mockhub-dev-key")
EMAIL = os.environ.get("MOCKHUB_EMAIL", "buyer@mockhub.com")
PASSWORD = os.environ.get("MOCKHUB_PASSWORD", "buyer123")

AGENT_HEADERS = {"X-API-Key": ACP_KEY}  # the AGENT's credential: an API key, no user login


def login() -> str:
    """The HUMAN's credential: a JWT from the buyer's username and password."""
    response = requests.post(
        f"{BASE_URL}/api/v1/auth/login",
        json={"email": EMAIL, "password": PASSWORD},
        timeout=15,
    )
    response.raise_for_status()
    return response.json()["accessToken"]


def fresh_agent_id() -> str:
    """Unique per run, so students sharing the demo account never collide."""
    return f"lab-agent-{uuid.uuid4().hex[:12]}"


def create_mandate(token: str, agent_id: str, **fields) -> dict:
    body = {"agentId": agent_id, "scope": "PURCHASE", **fields}
    response = requests.post(
        f"{BASE_URL}/api/v1/my/mandates",
        json=body,
        headers={"Authorization": f"Bearer {token}"},
        timeout=15,
    )
    response.raise_for_status()
    return response.json()

def revoke_mandate(token: str, mandate_id: str) -> None:
    requests.delete(
        f"{BASE_URL}/api/v1/my/mandates/{mandate_id}",
        headers={"Authorization": f"Bearer {token}"},
        timeout=15,
    )


def pick_a_listing() -> dict:
    """Any listing costing more than a dollar; a random page avoids seat contention."""
    page = uuid.uuid4().int % 5
    response = requests.get(
        f"{BASE_URL}/acp/v1/listings",
        params={"size": 10, "page": page, "minPrice": 10},
        headers=AGENT_HEADERS,
        timeout=15,
    )
    response.raise_for_status()
    listings = response.json()["content"]
    assert listings, "MockHub should have listings for sale"
    return listings[uuid.uuid4().int % len(listings)]


def agent_tries_to_buy(agent_id: str, mandate_id: str, listing_id: int) -> requests.Response:
    """The agent's purchase path: ACP checkout, carrying its mandate."""
    return requests.post(
        f"{BASE_URL}/acp/v1/checkout",
        json={
            "buyerEmail": EMAIL,
            "lineItems": [{"listingId": listing_id, "quantity": 1}],
            "agentId": agent_id,
            "mandateId": mandate_id,
        },
        headers=AGENT_HEADERS,
        timeout=15,
    )


def agent_tries_to_complete(checkout_id: str, agent_id: str, mandate_id: str,
                            approval_id: str | None = None) -> requests.Response:
    body = {"agentId": agent_id, "mandateId": mandate_id}
    if approval_id:
        body["approvalId"] = approval_id
    return requests.post(
        f"{BASE_URL}/acp/v1/checkout/{checkout_id}/complete",
        json=body,
        headers={**AGENT_HEADERS, "X-Buyer-Email": EMAIL},
        timeout=15,
    )


def cancel_checkout(checkout_id: str) -> None:
    requests.post(
        f"{BASE_URL}/acp/v1/checkout/{checkout_id}/cancel",
        headers={**AGENT_HEADERS, "X-Buyer-Email": EMAIL},
        timeout=15,
    )
