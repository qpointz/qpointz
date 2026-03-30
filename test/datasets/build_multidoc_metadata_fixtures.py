#!/usr/bin/env python3
"""
Build SPEC §15.2 multi-document YAML fixtures from:
  - legacy *-meta-repository.yaml (entities + short facet keys)
  - metadata/mill-metadata-core/.../platform-facet-types.json (facet type definitions)

Output: facet definitions + global scope + MetadataEntity documents with URN ids and
full facet type / scope URNs (same mapping rules as convert_to_canonical_yaml.py).

Usage:
  ./test/datasets/build_multidoc_metadata_fixtures.py \\
      test/datasets/skymill/skymill-meta-repository.yaml \\
      test/datasets/skymill/skymill-meta-multidoc-v1.yaml
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, List

import yaml

FACET_DESC = "urn:mill/metadata/facet-type:descriptive"
FACET_RELATION = "urn:mill/metadata/facet-type:relation"
F_SOURCE_TABLE = "urn:mill/metadata/facet-type:source-table"
F_SOURCE_COLUMN = "urn:mill/metadata/facet-type:source-column"
F_STRUCTURAL = "urn:mill/metadata/facet-type:structural"
SCOPE_GLOBAL = "urn:mill/metadata/scope:global"

FIXTURE_NS = __import__("uuid").uuid5(__import__("uuid").NAMESPACE_DNS, "io.qpointz.mill.metadata.multidoc")


def _norm_scope_map(scope_map: Any) -> Dict[str, Any]:
    if not isinstance(scope_map, dict):
        return {SCOPE_GLOBAL: scope_map}
    out: Dict[str, Any] = {}
    for sk, pl in scope_map.items():
        if sk == "global":
            out[SCOPE_GLOBAL] = pl
        elif sk.startswith("urn:mill/metadata/scope:"):
            out[sk] = pl
        elif sk.startswith(("user:", "team:", "role:")):
            out[f"urn:mill/metadata/scope:{sk}"] = pl
        else:
            out[sk] = pl
    return out


def _structural_to_source_table(payload: Any) -> Dict[str, Any]:
    if not isinstance(payload, dict):
        return {}
    name = payload.get("physicalName", "")
    st = "UNKNOWN"
    bt = str(payload.get("backendType", "")).lower()
    if bt == "jdbc":
        st = "JDBC"
    elif bt == "flow":
        st = "FLOW"
    elif bt == "calcite":
        st = "CALCITE"
    pkg = payload.get("package", "")
    if not pkg and payload.get("schemaName"):
        pkg = str(payload.get("schemaName", ""))
    return {"sourceType": st, "package": pkg, "name": name}


def _structural_to_source_column(payload: Any) -> Dict[str, Any]:
    if not isinstance(payload, dict):
        return {}
    return {
        "name": payload.get("physicalName", ""),
        "type": payload.get("physicalType", ""),
        "nullable": bool(payload.get("nullable", False)),
        "isPK": bool(payload.get("isPrimaryKey", False)),
        "isFK": bool(payload.get("isForeignKey", False)),
    }


def _legacy_relations_payload(relations: Any) -> Dict[str, Any]:
    if not isinstance(relations, list):
        return {"relations": []}
    out_list: List[Dict[str, Any]] = []
    for r in relations:
        if not isinstance(r, dict):
            continue
        st = r.get("sourceTable") or {}
        tt = r.get("targetTable") or {}
        out_list.append(
            {
                "name": r.get("name"),
                "description": r.get("description"),
                "cardinality": r.get("cardinality"),
                "type": r.get("type"),
                "expression": r.get("joinSql") or r.get("expression", ""),
                "source": {
                    "schema": st.get("schema"),
                    "table": st.get("table"),
                    "columns": list(r.get("sourceAttributes") or []),
                },
                "target": {
                    "schema": tt.get("schema"),
                    "table": tt.get("table"),
                    "columns": list(r.get("targetAttributes") or []),
                },
            }
        )
    return {"relations": out_list}


def _transform_facets_to_assignments(
    facets: Any, entity_type: str, entity_res: str
) -> List[Dict[str, Any]]:
    if not facets or not isinstance(facets, dict):
        return []
    et = str(entity_type) if entity_type is not None else ""
    assignments: List[Dict[str, Any]] = []
    ordinal = 0

    def add(ft: str, scope: str, payload: Any) -> None:
        nonlocal ordinal
        ordinal += 1
        uid = str(__import__("uuid").uuid5(FIXTURE_NS, f"{entity_res}|{ft}|{scope}|{ordinal}"))
        assignments.append(
            {
                "uid": uid,
                "facetTypeUrn": ft,
                "scopeUrn": scope,
                "payload": payload if isinstance(payload, dict) else {},
            }
        )

    for fk, scope_map in facets.items():
        if fk == "descriptive":
            sm = _norm_scope_map(scope_map)
            for scope, pl in sm.items():
                add(FACET_DESC, scope, pl if isinstance(pl, dict) else {})
        elif fk == "relation":
            sm = _norm_scope_map(scope_map)
            for scope, pl in sm.items():
                rels = pl.get("relations") if isinstance(pl, dict) else []
                add(FACET_RELATION, scope, _legacy_relations_payload(rels))
        elif fk == "structural":
            sm = scope_map if isinstance(scope_map, dict) else {}
            inner = sm.get("global") if "global" in sm else sm.get(SCOPE_GLOBAL)
            if et == "TABLE":
                add(F_SOURCE_TABLE, SCOPE_GLOBAL, _structural_to_source_table(inner))
            elif et == "ATTRIBUTE":
                add(F_SOURCE_COLUMN, SCOPE_GLOBAL, _structural_to_source_column(inner))
            else:
                for scope, pl in _norm_scope_map(scope_map).items():
                    add(F_STRUCTURAL, scope, pl if isinstance(pl, dict) else {})
        else:
            urn = {
                "concept": "urn:mill/metadata/facet-type:concept",
                "value-mapping": "urn:mill/metadata/facet-type:value-mapping",
            }.get(fk)
            if urn:
                sm = _norm_scope_map(scope_map)
                for scope, pl in sm.items():
                    add(urn, scope, pl if isinstance(pl, dict) else {})
            else:
                sm = _norm_scope_map(scope_map)
                for scope, pl in sm.items():
                    add(fk, scope, pl if isinstance(pl, dict) else {})

    return assignments


def entity_urn(e: Dict[str, Any]) -> str:
    t = e.get("type")
    sn = (e.get("schemaName") or "").lower()
    tn = (e.get("tableName") or "").lower()
    an = (e.get("attributeName") or "").lower()
    if t == "SCHEMA":
        return f"urn:mill/model/schema:{sn}"
    if t == "TABLE":
        return f"urn:mill/model/table:{sn}.{tn}"
    if t == "ATTRIBUTE":
        return f"urn:mill/model/attribute:{sn}.{tn}.{an}"
    if t == "CONCEPT":
        raw = str(e.get("id") or "concept").lower()
        if raw.startswith("concept_"):
            raw = raw[len("concept_") :]
        return f"urn:mill/model/concept:{raw}"
    raise ValueError(f"unknown entity type: {t!r}")


def entity_kind(e: Dict[str, Any]) -> str:
    return {
        "SCHEMA": "schema",
        "TABLE": "table",
        "ATTRIBUTE": "attribute",
        "CONCEPT": "concept",
    }[str(e["type"])]


def _applicable_to_yaml(json_val: Any) -> List[str]:
    if not json_val:
        return []
    out: List[str] = []
    for x in json_val:
        xs = str(x)
        if "entity-type:table" in xs:
            out.append("table")
        elif "entity-type:attribute" in xs:
            out.append("attribute")
        elif "entity-type:schema" in xs:
            out.append("schema")
        else:
            out.append(xs)
    return out


def _content_schema_descriptive() -> Dict[str, Any]:
    return {
        "type": "object",
        "properties": {
            "displayName": {"type": "string"},
            "description": {"type": "string"},
        },
    }


def _content_schema_loose() -> Dict[str, Any]:
    return {"type": "object", "additionalProperties": True}


def facet_definition_docs(platform_path: Path) -> List[Dict[str, Any]]:
    raw = json.loads(platform_path.read_text(encoding="utf-8"))
    docs: List[Dict[str, Any]] = []
    for entry in raw:
        if not isinstance(entry, dict):
            continue
        type_key = str(entry.get("facetTypeUrn") or entry.get("typeKey") or "")
        tc = str(entry.get("targetCardinality", "SINGLE")).upper()
        if tc not in ("SINGLE", "MULTIPLE"):
            tc = "SINGLE"
        display = entry.get("displayName") if entry.get("displayName") is not None else entry.get("title")
        doc: Dict[str, Any] = {
            "kind": "FacetTypeDefinition",
            "facetTypeUrn": type_key,
            "displayName": display,
            "description": entry.get("description"),
            "mandatory": bool(entry.get("mandatory", False)),
            "enabled": bool(entry.get("enabled", True)),
            "targetCardinality": tc,
            "applicableTo": _applicable_to_yaml(entry.get("applicableTo")),
            "schemaVersion": entry.get("schemaVersion"),
        }
        cat = entry.get("category")
        if cat:
            doc["category"] = cat
        cs = entry.get("contentSchema") or entry.get("payload")
        if cs and isinstance(cs, dict):
            doc["contentSchema"] = cs
        elif type_key == FACET_DESC:
            doc["contentSchema"] = _content_schema_descriptive()
        else:
            doc["contentSchema"] = _content_schema_loose()
        docs.append(doc)
    return docs


def scope_global_doc() -> Dict[str, Any]:
    return {
        "kind": "MetadataScope",
        "scopeUrn": SCOPE_GLOBAL,
        "scopeType": "GLOBAL",
        "referenceId": None,
        "displayName": "Global",
        "visibility": "PUBLIC",
    }


def build_documents(repo_path: Path, platform_path: Path) -> List[Dict[str, Any]]:
    data = yaml.safe_load(repo_path.read_text(encoding="utf-8"))
    entities = data.get("entities") or []
    if not isinstance(entities, list):
        entities = []

    docs: List[Dict[str, Any]] = []
    docs.extend(facet_definition_docs(platform_path))
    docs.append(scope_global_doc())

    for e in entities:
        if not isinstance(e, dict):
            continue
        er = entity_urn(e)
        ek = entity_kind(e)
        facets = _transform_facets_to_assignments(e.get("facets"), str(e.get("type")), er)
        docs.append(
            {
                "kind": "MetadataEntity",
                "entityUrn": er,
                "entityKind": ek,
                "facets": facets,
            }
        )
    return docs


def dump_multidoc(documents: List[Dict[str, Any]], out_path: Path) -> None:
    parts: List[str] = []
    header = (
        "# Multi-document metadata fixture (SPEC §15.2).\n"
        "# Generated by test/datasets/build_multidoc_metadata_fixtures.py — do not hand-edit;\n"
        "# re-run the script after changing the legacy repository YAML or platform-facet-types.json.\n"
    )
    parts.append(header.rstrip())
    for i, doc in enumerate(documents):
        parts.append("---")
        parts.append(yaml.dump(doc, default_flow_style=False, allow_unicode=True, sort_keys=False).rstrip())
    out_path.write_text("\n".join(parts) + "\n", encoding="utf-8")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("legacy_yaml", type=Path, help="Legacy *-meta-repository.yaml")
    ap.add_argument("out_yaml", type=Path, help="Output multidoc v1 YAML")
    ap.add_argument(
        "--platform-json",
        type=Path,
        default=None,
        help="platform-facet-types.json (default: mill-metadata-core resource)",
    )
    args = ap.parse_args()
    root = Path(__file__).resolve().parents[2]
    platform = args.platform_json or (
        root / "metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json"
    )
    if not platform.is_file():
        print(f"Missing platform JSON: {platform}", file=sys.stderr)
        sys.exit(1)
    if not args.legacy_yaml.is_file():
        print(f"Missing legacy YAML: {args.legacy_yaml}", file=sys.stderr)
        sys.exit(1)

    docs = build_documents(args.legacy_yaml, platform)
    args.out_yaml.parent.mkdir(parents=True, exist_ok=True)
    dump_multidoc(docs, args.out_yaml)
    print(f"Wrote {len(docs)} documents to {args.out_yaml}")


if __name__ == "__main__":
    main()
