"""LAB — The mandate boundary test.

Each test states a sentence of the contract between a customer and their buying agent,
then proves the platform enforces it. Read the test names out loud: they are the lesson.

Design by contract:
  precondition   — a mandate exists, and it is the ONLY authority the agent holds
  postcondition  — every purchase inside the mandate can succeed; everything outside is refused
  invariant      — nothing the agent can do changes the mandate's boundaries
"""

import pytest

from lab import (
    agent_tries_to_buy,
    agent_tries_to_complete,
    cancel_checkout,
    create_mandate,
    fresh_agent_id,
    login,
    pick_a_listing,
    revoke_mandate,
    AGENT_HEADERS,
    BASE_URL,
)
import requests


@pytest.fixture(scope="module")
def token():
    return login()


@pytest.fixture()
def agent_id():
    return fresh_agent_id()


def test_the_mandate_ceiling_holds(token, agent_id):
    """A purchase above the per-transaction limit is refused — not negotiated."""
    a_dollar_mandate = create_mandate(
        token, agent_id, maxSpendPerTransaction="1.00", approvalMode="AUTO_PURCHASE"
    )
    something_pricier = pick_a_listing()

    response = agent_tries_to_buy(agent_id, a_dollar_mandate["mandateId"],
                                  something_pricier["listingId"])

    assert response.status_code == 409, "the purchase should be refused"
    assert "Mandate does not authorize" in response.json()["detail"]

    revoke_mandate(token, a_dollar_mandate["mandateId"])


def test_a_revoked_mandate_grants_nothing(token, agent_id):
    """Revocation is immediate: yesterday's authority is not today's."""
    mandate = create_mandate(
        token, agent_id, maxSpendPerTransaction="500.00", approvalMode="AUTO_PURCHASE"
    )
    revoke_mandate(token, mandate["mandateId"])

    response = agent_tries_to_buy(agent_id, mandate["mandateId"],
                                  pick_a_listing()["listingId"])

    assert response.status_code == 409, "the purchase should be refused"
    assert "No active mandate" in response.json()["detail"]


def test_a_purchase_needing_approval_cannot_complete_without_one(token, agent_id):
    """APPROVAL_REQUIRED means a human said yes — the absence of a 'no' is not a 'yes'."""
    mandate = create_mandate(
        token, agent_id, maxSpendPerTransaction="500.00", approvalMode="APPROVAL_REQUIRED"
    )
    checkout = agent_tries_to_buy(agent_id, mandate["mandateId"],
                                  pick_a_listing()["listingId"])
    assert checkout.status_code == 201, "staging the checkout is allowed"
    checkout_id = checkout.json()["checkoutId"]

    response = agent_tries_to_complete(checkout_id, agent_id, mandate["mandateId"])

    assert response.status_code == 409, "completion should be refused"
    assert "requires an approved purchase approval" in response.json()["detail"]

    cancel_checkout(checkout_id)
    revoke_mandate(token, mandate["mandateId"])


def test_the_agent_credential_cannot_approve_a_purchase(agent_id):
    """An approval the agent can invoke is not an approval.

    The approve endpoint exists — but it accepts only the human's login, never the
    agent's API key. Same action, different door, and the agent's key does not fit.
    """
    response = requests.post(
        f"{BASE_URL}/api/v1/agent-approvals/any-proposal/approve",
        headers=AGENT_HEADERS,  # the AGENT's credential — deliberately not a user JWT
        timeout=15,
    )

    assert response.status_code == 401, "the agent's credential should not open this door"


@pytest.mark.skip(reason="YOUR TURN — delete this line and write the test (see labs.md)")
def test_the_agent_credential_cannot_mint_a_mandate(agent_id):
    """Authority comes from artifacts the model cannot mint.

    Prove it: try to CREATE a mandate (POST /api/v1/my/mandates) using only the
    agent's credential (AGENT_HEADERS), and assert the platform refuses with 401.
    The test above is the pattern; this is the same idea one level deeper.
    """
    raise NotImplementedError
