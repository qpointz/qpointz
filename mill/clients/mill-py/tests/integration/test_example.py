import unittest

import pytest

from tests.integration.profiles import profiles, TestITProfile


@pytest.mark.parametrize("profile", profiles())
def test_profile_fields(profile: TestITProfile):
    assert isinstance(profile.host, str)
    assert isinstance(profile.port, int)
    assert profile.protocol in [profile.protocol.HTTP, profile.protocol.GRPC]
    assert isinstance(profile.tls, bool)
    assert profile.auth in [profile.auth.NO_AUTH, profile.auth.BASIC]
    print(f"Running test for: {profile}")

if __name__ == '__main__':
    unittest.main()
