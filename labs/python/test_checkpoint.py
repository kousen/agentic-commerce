"""The setup checkpoint, run at 00:15:  pytest -k checkpoint

Paste the CHECKPOINT line it prints into the class chat.
"""

import requests


def test_checkpoint_mockhub_is_reachable_and_healthy(base_url):
    response = requests.get(f"{base_url}/actuator/health", timeout=10)

    assert response.status_code == 200, "MockHub health endpoint should answer"
    assert response.json()["status"] == "UP", "MockHub should report itself UP"

    print(f"\nCHECKPOINT OK — python — {base_url}")
