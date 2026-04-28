"""Tests for mill.metadata.dto — JSON parsing and aliases."""
from __future__ import annotations

import json

from mill.metadata.dto import (
    FacetInstanceDto,
    FacetMergeTraceResponseDto,
    FacetTypeManifestDto,
    ImportResultDto,
    MetadataAuditRecordDto,
    MetadataEntityDto,
    MetadataScopeDto,
    SearchResultDto,
    TreeNodeDto,
    facet_instance_from_dict,
    facet_merge_trace_from_dict,
    facet_type_manifest_from_dict,
    import_result_from_dict,
    metadata_audit_record_from_dict,
    metadata_entity_from_dict,
    metadata_scope_from_dict,
    search_result_from_dict,
    tree_node_from_dict,
)


def test_metadata_scope_aliases() -> None:
    d = json.loads(
        '{"scopeId":"urn:mill/metadata/scope:global","displayName":"Global"}'
    )
    s = metadata_scope_from_dict(d)
    assert isinstance(s, MetadataScopeDto)
    assert s.scope_urn == "urn:mill/metadata/scope:global"
    assert s.display_name == "Global"


def test_metadata_entity_id_alias() -> None:
    d = {"id": "urn:mill/model/table:a.b", "kind": "table"}
    e = metadata_entity_from_dict(d)
    assert isinstance(e, MetadataEntityDto)
    assert e.entity_urn == "urn:mill/model/table:a.b"


def test_facet_instance_aliases() -> None:
    d = {
        "uid": "u1",
        "facetType": "urn:mill/facet:t",
        "scope": "urn:mill/metadata/scope:global",
        "origin": "CAPTURED",
        "originId": "repo",
        "assignmentUid": None,
        "payload": {"x": 1},
        "createdAt": "2025-01-01T00:00:00Z",
        "lastModifiedAt": "2025-01-02T00:00:00Z",
    }
    f = facet_instance_from_dict(d)
    assert isinstance(f, FacetInstanceDto)
    assert f.facet_type_urn == "urn:mill/facet:t"
    assert f.scope_urn == "urn:mill/metadata/scope:global"
    assert f.payload == {"x": 1}


def test_import_result() -> None:
    d = {"entitiesImported": 2, "facetTypesImported": 1, "errors": ["e1"]}
    r = import_result_from_dict(d)
    assert isinstance(r, ImportResultDto)
    assert r.entities_imported == 2
    assert r.errors == ["e1"]


def test_merge_trace_context_alias() -> None:
    d = {
        "context": ["urn:mill/metadata/scope:global"],
        "entries": [
            {
                "uid": "u",
                "facetType": "urn:mill/facet:t",
                "scope": "urn:mill/metadata/scope:global",
                "mergeAction": "SET",
                "payload": {},
                "contributesToEffectiveView": True,
            }
        ],
    }
    m = facet_merge_trace_from_dict(d)
    assert isinstance(m, FacetMergeTraceResponseDto)
    assert m.scopes == ["urn:mill/metadata/scope:global"]
    assert len(m.entries) == 1
    assert m.entries[0].merge_action == "SET"


def test_audit_record_aliases() -> None:
    d = {
        "auditId": "a1",
        "operationType": "FACET_SET",
        "entityId": "urn:e",
        "facetType": "urn:f",
        "scopeKey": "urn:s",
        "actorId": "actor",
        "occurredAt": "2025-03-01T12:00:00+00:00",
        "payloadBefore": None,
        "payloadAfter": "{}",
        "changeSummary": "set",
    }
    a = metadata_audit_record_from_dict(d)
    assert isinstance(a, MetadataAuditRecordDto)
    assert a.entity_urn == "urn:e"
    assert a.facet_type_urn == "urn:f"
    assert a.scope_urn == "urn:s"


def test_facet_type_manifest_aliases() -> None:
    d = {
        "typeKey": "urn:mill/metadata/facet-type:descriptive",
        "displayName": "Desc",
        "description": "d",
        "contentSchema": {"type": "OBJECT", "title": "t", "description": "x"},
    }
    m = facet_type_manifest_from_dict(d)
    assert isinstance(m, FacetTypeManifestDto)
    assert m.type_key == "urn:mill/metadata/facet-type:descriptive"
    assert m.title == "Desc"
    assert m.payload["type"] == "OBJECT"


def test_search_result() -> None:
    d = {"id": "1", "score": 0.5}
    s = search_result_from_dict(d)
    assert isinstance(s, SearchResultDto)
    assert s.score == 0.5


def test_tree_node_nested() -> None:
    d = {
        "id": "root",
        "hasChildren": True,
        "children": [{"id": "c1", "hasChildren": False}],
    }
    t = tree_node_from_dict(d)
    assert isinstance(t, TreeNodeDto)
    assert t.children is not None
    assert t.children[0].id == "c1"
