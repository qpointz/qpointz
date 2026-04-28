"""Schema explorer REST DTOs and JSON parsers."""
from __future__ import annotations

from dataclasses import dataclass
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
        raise ValueError(f"expected one of keys {keys!r}")
    return s


def _parse_instant(v: Any) -> datetime | None:
    if v is None:
        return None
    if isinstance(v, datetime):
        return v
    text = str(v).replace("Z", "+00:00")
    return datetime.fromisoformat(text)


def _req_instant(d: Mapping[str, Any], key: str) -> datetime:
    t = _parse_instant(d.get(key))
    if t is None:
        raise ValueError(f"missing instant {key!r}")
    return t


@dataclass
class ScopeOptionDto:
    id: str
    slug: str
    display_name: str


def scope_option_from_dict(d: Mapping[str, Any]) -> ScopeOptionDto:
    return ScopeOptionDto(
        id=_req_str(d, "id"),
        slug=_req_str(d, "slug"),
        display_name=_req_str(d, "displayName"),
    )


@dataclass
class SchemaContextDto:
    selected_context: str
    available_scopes: list[ScopeOptionDto]


def schema_context_from_dict(d: Mapping[str, Any]) -> SchemaContextDto:
    raw = d.get("availableScopes")
    if not isinstance(raw, list):
        raise ValueError("availableScopes must be a list")
    scopes = [scope_option_from_dict(cast(Mapping[str, Any], x)) for x in raw]
    return SchemaContextDto(
        selected_context=_req_str(d, "selectedContext"),
        available_scopes=scopes,
    )


@dataclass
class DataTypeDescriptor:
    type: str
    nullable: bool
    precision: int | None = None
    scale: int | None = None


def data_type_descriptor_from_dict(d: Mapping[str, Any]) -> DataTypeDescriptor:
    return DataTypeDescriptor(
        type=_req_str(d, "type"),
        nullable=bool(d.get("nullable", False)),
        precision=int(d["precision"]) if d.get("precision") is not None else None,
        scale=int(d["scale"]) if d.get("scale") is not None else None,
    )


@dataclass
class FacetEnvelopeDto:
    facet_type: str
    payload: Any


def facet_envelope_from_dict(d: Mapping[str, Any]) -> FacetEnvelopeDto:
    return FacetEnvelopeDto(
        facet_type=_req_str(d, "facetType"),
        payload=d.get("payload"),
    )


@dataclass
class FacetResolvedRowDto:
    uid: str
    facet_type_urn: str
    scope_urn: str
    origin: str
    origin_id: str
    assignment_uid: str | None
    payload: dict[str, Any]
    created_at: datetime
    last_modified_at: datetime


def facet_resolved_row_from_dict(d: Mapping[str, Any]) -> FacetResolvedRowDto:
    raw_pl = d.get("payload")
    if not isinstance(raw_pl, dict):
        raise ValueError("facet row payload must be object")
    return FacetResolvedRowDto(
        uid=_req_str(d, "uid"),
        facet_type_urn=_req_str(d, "facetTypeUrn"),
        scope_urn=_req_str(d, "scopeUrn"),
        origin=_req_str(d, "origin"),
        origin_id=_req_str(d, "originId"),
        assignment_uid=_first_str(d, "assignmentUid"),
        payload=cast(dict[str, Any], raw_pl),
        created_at=_req_instant(d, "createdAt"),
        last_modified_at=_req_instant(d, "lastModifiedAt"),
    )


def _facets_map(raw: Any) -> dict[str, FacetEnvelopeDto] | None:
    if raw is None:
        return None
    if not isinstance(raw, dict):
        raise ValueError("facets must be object or null")
    out: dict[str, FacetEnvelopeDto] = {}
    for k, v in raw.items():
        if isinstance(v, dict):
            out[str(k)] = facet_envelope_from_dict(cast(Mapping[str, Any], v))
    return out


def _facets_resolved(raw: Any) -> list[FacetResolvedRowDto] | None:
    if raw is None:
        return None
    if not isinstance(raw, list):
        raise ValueError("facetsResolved must be list or null")
    return [facet_resolved_row_from_dict(cast(Mapping[str, Any], x)) for x in raw]


@dataclass
class ModelRootDto:
    id: str
    entity_type: str
    metadata_entity_id: str
    facets: dict[str, FacetEnvelopeDto] | None = None
    facets_resolved: list[FacetResolvedRowDto] | None = None


def model_root_from_dict(d: Mapping[str, Any]) -> ModelRootDto:
    return ModelRootDto(
        id=_req_str(d, "id"),
        entity_type=_req_str(d, "entityType"),
        metadata_entity_id=_req_str(d, "metadataEntityId"),
        facets=_facets_map(d.get("facets")),
        facets_resolved=_facets_resolved(d.get("facetsResolved")),
    )


@dataclass
class SchemaListItemDto:
    id: str
    entity_type: str
    schema_name: str
    metadata_entity_id: str | None = None
    facets: dict[str, FacetEnvelopeDto] | None = None
    facets_resolved: list[FacetResolvedRowDto] | None = None


def schema_list_item_from_dict(d: Mapping[str, Any]) -> SchemaListItemDto:
    # schemaName may be empty for MODEL rows in list responses
    raw_sn = d.get("schemaName")
    schema_name = "" if raw_sn is None else str(raw_sn)
    return SchemaListItemDto(
        id=_req_str(d, "id"),
        entity_type=_req_str(d, "entityType"),
        schema_name=schema_name,
        metadata_entity_id=_first_str(d, "metadataEntityId"),
        facets=_facets_map(d.get("facets")),
        facets_resolved=_facets_resolved(d.get("facetsResolved")),
    )


@dataclass
class TableSummaryDto:
    id: str
    entity_type: str
    schema_name: str
    table_name: str
    metadata_entity_id: str | None = None
    facets: dict[str, FacetEnvelopeDto] | None = None
    facets_resolved: list[FacetResolvedRowDto] | None = None


def table_summary_from_dict(d: Mapping[str, Any]) -> TableSummaryDto:
    return TableSummaryDto(
        id=_req_str(d, "id"),
        entity_type=_req_str(d, "entityType"),
        schema_name=_req_str(d, "schemaName"),
        table_name=_req_str(d, "tableName"),
        metadata_entity_id=_first_str(d, "metadataEntityId"),
        facets=_facets_map(d.get("facets")),
        facets_resolved=_facets_resolved(d.get("facetsResolved")),
    )


@dataclass
class SchemaDto:
    id: str
    entity_type: str
    schema_name: str
    tables: list[TableSummaryDto]
    metadata_entity_id: str | None = None
    facets: dict[str, FacetEnvelopeDto] | None = None
    facets_resolved: list[FacetResolvedRowDto] | None = None


def schema_dto_from_dict(d: Mapping[str, Any]) -> SchemaDto:
    raw_tables = d.get("tables")
    if not isinstance(raw_tables, list):
        raise ValueError("tables must be a list")
    tables = [table_summary_from_dict(cast(Mapping[str, Any], t)) for t in raw_tables]
    return SchemaDto(
        id=_req_str(d, "id"),
        entity_type=_req_str(d, "entityType"),
        schema_name=_req_str(d, "schemaName"),
        tables=tables,
        metadata_entity_id=_first_str(d, "metadataEntityId"),
        facets=_facets_map(d.get("facets")),
        facets_resolved=_facets_resolved(d.get("facetsResolved")),
    )


@dataclass
class ColumnDto:
    id: str
    entity_type: str
    schema_name: str
    table_name: str
    column_name: str
    field_index: int
    type: DataTypeDescriptor
    metadata_entity_id: str | None = None
    facets: dict[str, FacetEnvelopeDto] | None = None
    facets_resolved: list[FacetResolvedRowDto] | None = None


def column_dto_from_dict(d: Mapping[str, Any]) -> ColumnDto:
    raw_t = d.get("type")
    if not isinstance(raw_t, dict):
        raise ValueError("column type must be object")
    return ColumnDto(
        id=_req_str(d, "id"),
        entity_type=_req_str(d, "entityType"),
        schema_name=_req_str(d, "schemaName"),
        table_name=_req_str(d, "tableName"),
        column_name=_req_str(d, "columnName"),
        field_index=int(d.get("fieldIndex", 0)),
        type=data_type_descriptor_from_dict(cast(Mapping[str, Any], raw_t)),
        metadata_entity_id=_first_str(d, "metadataEntityId"),
        facets=_facets_map(d.get("facets")),
        facets_resolved=_facets_resolved(d.get("facetsResolved")),
    )


@dataclass
class TableDto:
    id: str
    entity_type: str
    schema_name: str
    table_name: str
    table_type: str
    columns: list[ColumnDto]
    metadata_entity_id: str | None = None
    facets: dict[str, FacetEnvelopeDto] | None = None
    facets_resolved: list[FacetResolvedRowDto] | None = None


def table_dto_from_dict(d: Mapping[str, Any]) -> TableDto:
    raw_cols = d.get("columns")
    if not isinstance(raw_cols, list):
        raise ValueError("columns must be a list")
    cols = [column_dto_from_dict(cast(Mapping[str, Any], c)) for c in raw_cols]
    return TableDto(
        id=_req_str(d, "id"),
        entity_type=_req_str(d, "entityType"),
        schema_name=_req_str(d, "schemaName"),
        table_name=_req_str(d, "tableName"),
        table_type=_req_str(d, "tableType"),
        columns=cols,
        metadata_entity_id=_first_str(d, "metadataEntityId"),
        facets=_facets_map(d.get("facets")),
        facets_resolved=_facets_resolved(d.get("facetsResolved")),
    )


@dataclass
class SchemaExplorerTreeDto:
    model_root: ModelRootDto
    schemas: list[SchemaDto]


def schema_explorer_tree_from_dict(d: Mapping[str, Any]) -> SchemaExplorerTreeDto:
    raw_mr = d.get("modelRoot")
    raw_sc = d.get("schemas")
    if not isinstance(raw_mr, dict):
        raise ValueError("modelRoot must be object")
    if not isinstance(raw_sc, list):
        raise ValueError("schemas must be list")
    return SchemaExplorerTreeDto(
        model_root=model_root_from_dict(cast(Mapping[str, Any], raw_mr)),
        schemas=[schema_dto_from_dict(cast(Mapping[str, Any], s)) for s in raw_sc],
    )
