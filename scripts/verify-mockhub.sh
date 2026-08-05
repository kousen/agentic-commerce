#!/bin/bash
# Post-deploy verification for the course platform. Checks the five fixed defects and the
# behavior the course depends on. Safe to re-run: creates its own agent and cleans up.
# Usage: ./scripts/verify-mockhub.sh

set -u
BASE=${MOCKHUB_URL:-https://mockhub.kousenit.com}
KEY=${MOCKHUB_ACP_KEY:-mockhub-dev-key}
EMAIL=${MOCKHUB_EMAIL:-buyer@mockhub.com}
PASSWORD=${MOCKHUB_PASSWORD:-buyer123}
pass=0; fail=0
check() { # check <name> <actual> <expected>
  if [ "$2" = "$3" ]; then echo "  PASS  $1"; pass=$((pass+1));
  else echo "  FAIL  $1 (got '$2', wanted '$3')"; fail=$((fail+1)); fi
}

echo "Verifying $BASE"

check "health is UP" \
  "$(curl -s --max-time 15 "$BASE/actuator/health" | python3 -c 'import sys,json;print(json.load(sys.stdin)["status"])')" \
  "UP"

# Fix 1: price filter must not drop whole events
n=$(curl -s --max-time 15 -H "X-API-Key: $KEY" "$BASE/acp/v1/listings?query=Monster&maxPrice=40&size=5" \
      | python3 -c 'import sys,json;print(len(json.load(sys.stdin)["content"]))')
[ "$n" -gt 0 ] && r=yes || r=no
check "price filter returns affordable seats at pricier events" "$r" "yes"

# Fix 2: missing request body is a client error, not a server error
check "missing request body returns 400" \
  "$(curl -s --max-time 15 -o /dev/null -w '%{http_code}' -X POST "$BASE/acp/v1/checkout/MH-NOPE/complete" \
      -H "X-API-Key: $KEY" -H "X-Buyer-Email: $EMAIL" -H 'Content-Type: application/json')" \
  "400"

TOKEN=$(curl -s --max-time 15 -X POST "$BASE/api/v1/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])')
[ -n "$TOKEN" ] && r=yes || r=no
check "demo buyer can log in" "$r" "yes"

# Fix 3 (the big one): a ceiling must cover the fee, not just the ticket price.
# Pick a listing priced just under a ceiling whose fee-inclusive total exceeds it.
AGENT="verify-$RANDOM"
LISTING=$(curl -s --max-time 15 -H "X-API-Key: $KEY" "$BASE/acp/v1/listings?query=Monster&size=50" \
  | python3 -c '
import sys, json
ls = sorted(json.load(sys.stdin)["content"], key=lambda l: l["price"])
# want price < 35 but price*1.1 > 35
for l in ls:
    if l["price"] < 35 and l["price"] * 1.1 > 35:
        print(l["listingId"]); break')
MID=$(curl -s --max-time 15 -X POST "$BASE/api/v1/my/mandates" -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"agentId\":\"$AGENT\",\"scope\":\"PURCHASE\",\"maxSpendPerTransaction\":35.00,\"approvalMode\":\"AUTO_PURCHASE\"}" \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["mandateId"])')

if [ -n "$LISTING" ]; then
  code=$(curl -s --max-time 15 -o /dev/null -w '%{http_code}' -X POST "$BASE/acp/v1/checkout" -H "X-API-Key: $KEY" \
    -H 'Content-Type: application/json' \
    -d "{\"buyerEmail\":\"$EMAIL\",\"lineItems\":[{\"listingId\":$LISTING,\"quantity\":1}],\"agentId\":\"$AGENT\",\"mandateId\":\"$MID\"}")
  check "ticket under the ceiling but over it with fees is refused" "$code" "409"
else
  echo "  SKIP  no listing in the fee-boundary range today"
fi

curl -s --max-time 15 -o /dev/null -X DELETE "$BASE/api/v1/my/mandates/$MID" -H "Authorization: Bearer $TOKEN"

# Course dependency: the agent's credential must not open human-only doors
check "agent credential cannot approve a purchase" \
  "$(curl -s --max-time 15 -o /dev/null -w '%{http_code}' -X POST "$BASE/api/v1/agent-approvals/any/approve" -H "X-API-Key: $KEY")" \
  "401"

echo
echo "$pass passed, $fail failed"
[ "$fail" -eq 0 ]
