# Lab 1 solutions — instructor reference

The **YOUR TURN** test is test 4 one level deeper: take the *agent's* credential to a door
only the human's login opens — this time the one that mints mandates — and assert **401**.
Remove the skip/disabled marker, then replace the body. All three verified green against
hosted MockHub. (Lab 2 solutions: [`guarded-tool/SOLUTIONS.md`](guarded-tool/SOLUTIONS.md).)

## Python — `python/test_mandate_boundary.py`

```python
def test_the_agent_credential_cannot_mint_a_mandate(agent_id):
    response = requests.post(
        f"{BASE_URL}/api/v1/my/mandates",
        json={"agentId": agent_id, "scope": "PURCHASE",
              "maxSpendPerTransaction": "500.00", "approvalMode": "AUTO_PURCHASE"},
        headers=AGENT_HEADERS,  # the AGENT's credential — deliberately not a user JWT
        timeout=15,
    )

    assert response.status_code == 401, "the agent's credential should not mint a mandate"
```

## TypeScript — `typescript/tests/mandate-boundary.test.ts`

```typescript
test("the agent's credential cannot mint a mandate — authority comes from artifacts the model cannot mint", async () => {
  const response = await post(
    "/api/v1/my/mandates",
    { agentId: freshAgentId(), scope: "PURCHASE", maxSpendPerTransaction: "500.00", approvalMode: "AUTO_PURCHASE" },
    AGENT_HEADERS, // the AGENT's credential — deliberately not a user JWT
  );

  expect(response.status, "the agent's credential should not mint a mandate").toBe(401);
});
```

## Java — `java/src/test/java/lab/MandateBoundaryLabTest.java`

```java
@Test
@DisplayName("The agent's credential cannot mint a mandate — authority comes from artifacts the model cannot mint")
void theAgentCredentialCannotMintAMandate() {
    var response = Lab.post("/api/v1/my/mandates",
            """
            {"agentId": "%s", "scope": "PURCHASE",
             "maxSpendPerTransaction": 500.00, "approvalMode": "AUTO_PURCHASE"}"""
                    .formatted(Lab.freshAgentId()),
            "X-API-Key", Lab.ACP_KEY);  // the AGENT's credential — deliberately not a JWT

    assertThat(response.statusCode())
            .as("the agent's credential should not mint a mandate")
            .isEqualTo(401);
}
```

## Debrief notes

- **The payload barely matters.** Authentication is checked before the body is read, so
  `{}` earns the same 401. A realistic payload is the stronger proof, though: it shows the
  refusal is about *who is asking*, not a malformed request.
- **401, not 403.** The platform isn't saying "you may not" — it's saying "I don't know
  who you are." An API key identifies the agent's integration, never a user, and only a
  user can grant authority.
- **If a student gets 400 or 404,** check the path (`/api/v1/my/mandates`) and that they
  sent the agent headers rather than no headers at all — both still fail closed, but the
  test should exercise the agent's key specifically.
