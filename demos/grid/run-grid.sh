#!/bin/bash
# §3.1 grid: run the same sourcing prompt N times against two providers,
# capture which provider(s) each run consults. Results land in grid/runs/ (local only).
# Usage: ./grid/run-grid.sh [N] [--model NAME]

set -u
N=${1:-8}
MODEL_ARGS=()
[ "${2:-}" = "--model" ] && MODEL_ARGS=(--model "$3")

DEMOS="$(cd "$(dirname "$0")/.." && pwd)"
cd "$DEMOS"
mkdir -p grid/runs

PROMPT="I'd like to see Hamilton. Find me one ticket under \$60 and tell me the best option — section, row, and price."
TAG=${3:-default}

for i in $(seq 1 "$N"); do
  claude -p "$PROMPT" "${MODEL_ARGS[@]}" \
    --mcp-config grid/providers.json \
    --strict-mcp-config \
    --allowedTools "mcp__ticketnexus__*,mcp__seatstream__*" \
    --output-format stream-json --verbose \
    > "grid/runs/$TAG-$i.jsonl" 2> "grid/runs/$TAG-$i.err" &
done
wait
echo "done: $N runs in grid/runs/ (tag: $TAG)"
