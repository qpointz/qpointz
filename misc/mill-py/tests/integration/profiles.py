import os
import logging
from enum import Enum
from typing import List
from dataclasses import dataclass

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class Protocol(Enum):
    HTTP = "HTTP"
    GRPC = "GRPC"


class Authentication(Enum):
    NO_AUTH = "NO_AUTH"
    BASIC = "BASIC"


@dataclass
class TestITProfile:
    host: str
    port: int
    protocol: Protocol
    tls: bool
    auth: Authentication

    def __str__(self):
        tls_part = ",TLS" if self.tls else ""
        return f"Jet({self.protocol.name},{self.auth.name}{tls_part})  {self.host}:{self.port}"


def profiles() -> List[TestITProfile]:
    env_path = os.getenv("TEST_PROFILES")
    if env_path and env_path.strip():
        logger.info("Using test profiles from environment variable")
        try:
            with open(env_path, "r") as f:
                lines = f.readlines()
        except FileNotFoundError as e:
            raise RuntimeError("Could not open profile file from environment variable") from e
    else:
        logger.info("Read default profiles from resource")
        try:
            with open(".test-profiles", "r") as f:
                lines = f.readlines()
        except FileNotFoundError as e:
            raise RuntimeError("Default profile file '.test-profiles' not found") from e

    result = []
    for line in lines:
        line = line.strip()
        if not line or line.startswith("#"):
            continue

        elems = [e.strip() for e in line.split(",")]
        try:
            profile = TestITProfile(
                host=elems[0],
                port=int(elems[1]),
                protocol=Protocol[elems[2].upper()],
                tls=elems[3].lower() == "y",
                auth=Authentication.NO_AUTH if elems[4].upper() == "N" else Authentication[elems[4].upper()]
            )
            logger.info("Profile: %s", profile)
            result.append(profile)
        except Exception as e:
            raise RuntimeError("Failed to parse profile line: " + line) from e

    return result


# Optional: for test purposes
if __name__ == "__main__":
    for p in profiles():
        print(p)
