"""Metadata REST DTOs and JSON parsers with server field-alias support.

Shapes mirror Kotlin types under ``metadata/mill-metadata-service/.../api/dto``
and :class:`FacetTypeManifest` from ``mill-metadata-core``.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Mapping, cast


def _first_str(d: Mapping[str, Any], *keys: str) -> str | None:
    for k in keys:
        v = d.get(k)
        if v is not None and v != "":
            return str(v)
    return None


def _req_str(d: Mapping[str, Any], *keys: str) -> str:
    s = _first_str(d, *keys)
    if s is None:
        raise ValueError(f"expected one of keys {keys!r} in object")
    return s


def _parse_instant(v: Any) -> datetime | None:
    if v is None:
        return None
    if isinstance(v, datetime):
        return v
    text = str(v).replace("Z", "+00:00")
    return datetime.fromisoformat(text)


def _opt_instant(d: Mapping[str, Any], key: str) -> datetime | None:
    return _parse_instant(d.get(key))


def _req_instant(d: Mapping[str, Any], key: str) -> datetime:
    t = _parse_instant(d.get(key))
    if t is None:
        raise ValueError(f"missing or invalid instant {key!r}")
    return t


@dataclass
class MetadataScopeDto:
    """``GET/POST /api/v1/metadata/scopes`` row."""

    scope_urn: str
    display_name: str | None = None
    owner_id: str | None = None
    created_at: datetime | None = None


def metadata_scope_from_dict(d: Mapping[str, Any]) -> MetadataScopeDto:
    return MetadataScopeDto(
        scope_urn=_req_str(d, "scopeUrn", "scopeId"),
        display_name=_first_str(d, "displayName"),
        owner_id=_first_str(d, "ownerId"),
        created_at=_opt_instant(d, "createdAt"),
    )


@dataclass
class MetadataEntityDto:
    """Entity identity (no embedded facets on list/get entity)."""

    entity_urn: str | None = None
    kind: str | None = None
    created_at: datetime | None = None
    last_modified_at: datetime | None = None
    created_by: str | None = None
    last_modified_by: str | None = None


def metadata_entity_from_dict(d: Mapping[str, Any]) -> MetadataEntityDto:
    return MetadataEntityDto(
        entity_urn=_first_str(d, "entityUrn", "id"),
        kind=_first_str(d, "kind"),
        created_at=_opt_instant(d, "createdAt"),
        last_modified_at=_opt_instant(d, "lastModifiedAt"),
        created_by=_first_str(d, "createdBy"),
        last_modified_by=_first_str(d, "lastModifiedBy"),
    )


@dataclass
class FacetInstanceDto:
    """One facet assignment row."""

    uid: str
    facet_type_urn: str
    scope_urn: str
    origin: str
    origin_id: str
    assignment_uid: str | None
    payload: dict[str, Any]
    created_at: datetime
    last_modified_at: datetime


def facet_instance_from_dict(d: Mapping[str, Any]) -> FacetInstanceDto:
    raw_payload = d.get("payload")
    if not isinstance(raw_payload, dict):
        raise ValueError("facet instance requires object payload")
    return FacetInstanceDto(
        uid=_req_str(d, "uid"),
        facet_type_urn=_req_str(d, "facetTypeUrn", "facetType"),
        scope_urn=_req_str(d, "scopeUrn", "scope"),
        origin=_req_str(d, "origin"),
        origin_id=_req_str(d, "originId"),
        assignment_uid=_first_str(d, "assignmentUid"),
        payload=cast(dict[str, Any], raw_payload),
        created_at=_req_instant(d, "createdAt"),
        last_modified_at=_req_instant(d, "lastModifiedAt"),
    )


@dataclass
class ImportResultDto:
    """``POST /api/v1/metadata/import`` summary."""

    entities_imported: int
    facet_types_imported: int
    errors: list[str] = field(default_factory=list)


def import_result_from_dict(d: Mapping[str, Any]) -> ImportResultDto:
    errs = d.get("errors")
    if errs is None:
        el: list[str] = []
    elif isinstance(errs, list):
        el = [str(x) for x in errs]
    else:
        raise ValueError("errors must be a list")
    return ImportResultDto(
        entities_imported=int(d.get("entitiesImported", 0)),
        facet_types_imported=int(d.get("facetTypesImported", 0)),
        errors=el,
    )


@dataclass
class FacetMergeTraceEntryDto:
    """Single row in merge-trace."""

    uid: str
    facet_type_urn: str
    scope_urn: str
    merge_action: str
    payload: dict[str, Any]
    contributes_to_effective_view: bool


def facet_merge_trace_entry_from_dict(d: Mapping[str, Any]) -> FacetMergeTraceEntryDto:
    raw_payload = d.get("payload")
    if not isinstance(raw_payload, dict):
        raise ValueError("merge-trace entry requires object payload")
    return FacetMergeTraceEntryDto(
        uid=_req_str(d, "uid"),
        facet_type_urn=_req_str(d, "facetTypeUrn", "facetType"),
        scope_urn=_req_str(d, "scopeUrn", "scope"),
        merge_action=_req_str(d, "mergeAction"),
        payload=cast(dict[str, Any], raw_payload),
        contributes_to_effective_view=bool(d.get("contributesToEffectiveView", False)),
    )


@dataclass
class FacetMergeTraceResponseDto:
    """``GET …/facets/merge-trace`` body."""

    scopes: list[str]
    entries: list[FacetMergeTraceEntryDto]


def facet_merge_trace_from_dict(d: Mapping[str, Any]) -> FacetMergeTraceResponseDto:
    raw_scopes = d.get("scopes")
    if raw_scopes is None:
        raw_scopes = d.get("context")
    if not isinstance(raw_scopes, list):
        raise ValueError("merge-trace requires scopes (or context alias) list")
    scopes = [str(x) for x in raw_scopes]
    raw_entries = d.get("entries")
    if not isinstance(raw_entries, list):
        raise ValueError("merge-trace requires entries list")
    entries = [facet_merge_trace_entry_from_dict(cast(Mapping[str, Any], e)) for e in raw_entries]
    return FacetMergeTraceResponseDto(scopes=scopes, entries=entries)


@dataclass
class MetadataAuditRecordDto:
    """One history / audit row."""

    audit_id: str
    operation_type: str
    entity_urn: str | None
    facet_type_urn: str | None
    scope_urn: str | None
    actor_id: str
    occurred_at: datetime
    payload_before: str | None
    payload_after: str | None
    change_summary: str | None


def metadata_audit_record_from_dict(d: Mapping[str, Any]) -> MetadataAuditRecordDto:
    return MetadataAuditRecordDto(
        audit_id=_req_str(d, "auditId"),
        operation_type=_req_str(d, "operationType"),
        entity_urn=_first_str(d, "entityUrn", "entityId"),
        facet_type_urn=_first_str(d, "facetTypeUrn", "facetType"),
        scope_urn=_first_str(d, "scopeUrn", "scopeKey"),
        actor_id=_req_str(d, "actorId"),
        occurred_at=_req_instant(d, "occurredAt"),
        payload_before=_first_str(d, "payloadBefore"),
        payload_after=_first_str(d, "payloadAfter"),
        change_summary=_first_str(d, "changeSummary"),
    )


@dataclass
class SearchResultDto:
    """Search hit (if exposed by metadata search APIs)."""

    id: str | None = None
    name: str | None = None
    kind: str | None = None
    display_name: str | None = None
    description: str | None = None
    location: str | None = None
    score: float | None = None


def search_result_from_dict(d: Mapping[str, Any]) -> SearchResultDto:
    sc = d.get("score")
    score = float(sc) if sc is not None else None
    return SearchResultDto(
        id=_first_str(d, "id"),
        name=_first_str(d, "name"),
        kind=_first_str(d, "kind"),
        display_name=_first_str(d, "displayName"),
        description=_first_str(d, "description"),
        location=_first_str(d, "location"),
        score=score,
    )


@dataclass
class TreeNodeDto:
    """Hierarchical metadata browser node."""

    id: str | None = None
    name: str | None = None
    kind: str | None = None
    display_name: str | None = None
    description: str | None = None
    children: list[TreeNodeDto] | None = None
    has_children: bool = False


def tree_node_from_dict(d: Mapping[str, Any]) -> TreeNodeDto:
    raw_children = d.get("children")
    children: list[TreeNodeDto] | None = None
    if isinstance(raw_children, list):
        children = [tree_node_from_dict(cast(Mapping[str, Any], c)) for c in raw_children]
    return TreeNodeDto(
        id=_first_str(d, "id"),
        name=_first_str(d, "name"),
        kind=_first_str(d, "kind"),
        display_name=_first_str(d, "displayName"),
        description=_first_str(d, "description"),
        children=children,
        has_children=bool(d.get("hasChildren", False)),
    )


@dataclass
class FacetTypeManifestDto:
    """Facet catalog descriptor (:class:`FacetTypeManifest` wire shape)."""

    type_key: str
    title: str
    description: str
    category: str | None = None
    enabled: bool = True
    mandatory: bool = False
    target_cardinality: str = "SINGLE"
    applicable_to: list[str] | None = None
    schema_version: str | None = None
    payload: dict[str, Any] = field(default_factory=dict)


def facet_type_manifest_from_dict(d: Mapping[str, Any]) -> FacetTypeManifestDto:
    raw_schema = d.get("contentSchema")
    if raw_schema is None:
        raw_schema = d.get("payload")
    if raw_schema is not None and not isinstance(raw_schema, dict):
        raise ValueError("contentSchema / payload must be an object when present")
    applicable = d.get("applicableTo")
    app_list: list[str] | None = None
    if isinstance(applicable, list):
        app_list = [str(x) for x in applicable]
    card = d.get("targetCardinality")
    return FacetTypeManifestDto(
        type_key=_req_str(d, "facetTypeUrn", "typeKey", "typeRes"),
        title=_req_str(d, "title", "displayName"),
        description=_req_str(d, "description"),
        category=_first_str(d, "category"),
        enabled=bool(d.get("enabled", True)),
        mandatory=bool(d.get("mandatory", False)),
        target_cardinality=str(card) if card is not None else "SINGLE",
        applicable_to=app_list,
        schema_version=_first_str(d, "schemaVersion"),
        payload=cast(dict[str, Any], raw_schema) if isinstance(raw_schema, dict) else {},
    )
