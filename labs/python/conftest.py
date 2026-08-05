import os

import pytest

BASE_URL = os.environ.get("MOCKHUB_URL", "https://mockhub.kousenit.com")


@pytest.fixture(scope="session")
def base_url():
    return BASE_URL
