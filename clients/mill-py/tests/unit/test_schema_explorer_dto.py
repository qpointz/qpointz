"""Schema explorer DTO parsing."""
from __future__ import annotations

import json

from mill.schema_explorer.dto import model_root_from_dict, schema_context_from_dict


def test_schema_context_roundtrip() -> None:
    d = json.loads(
        '{"selectedContext":"x","availableScopes":[{"id":"a","slug":"s","displayName":"D"}]}'
    )
    c = schema_context_from_dict(d)
    assert c.available_scopes[0].display_name == "D"


def test_model_root() -> None:
    d = {
        "id": "model",
        "entityType": "MODEL",
        "metadataEntityId": "urn:mill/model:root",
    }
    m = model_root_from_dict(d)
    assert m.metadata_entity_id == "urn:mill/model:root"
