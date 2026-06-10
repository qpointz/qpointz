#!/usr/bin/env python3
"""One-off helper: legacy dataset YAML -> CANONICAL envelope + URN facet/scope keys."""
from __future__ import annotations

import sys
from typing import Any, Dict

import yaml

FACET_DESC = "urn:mill/metadata/facet-type:descriptive"
FACET_RELATION = "urn:mill/metadata/facet-type:relation"
F_SOURCE_TABLE = "urn:mill/metadata/facet-type:source-table"
F_SOURCE_COLUMN = "urn:mill/metadata/facet-type:source-column"
SCOPE_GLOBAL = "urn:mill/metadata/scope:global"


def _norm_scope_map(scope_map: Any) -> Dict[str, Any]:
    if not isinstance(scope_map, dict):
        return {SCOPE_GLOBAL: scope_map}
    out: Dict[str, Any] = {}
    for sk, pl in scope_map.items():
        if sk == "global":
            out[SCOPE_GLOBAL] = pl
        elif sk.startswith("urn:mill/metadata/scope:"):
            out[sk] = pl
        elif sk.startswith("user:"):
            out[f"urn:mill/metadata/scope:{sk}"] = pl
        elif sk.startswith("team:"):
            out[f"urn:mill/metadata/scope:{sk}"] = pl
        elif sk.startswith("role:"):
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
    return {"sourceType": st, "package": "", "name": name}


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


def _transform_facets(facets: Any, entity_type: Any) -> Any:
    if not facets or not isinstance(facets, dict):
        return facets
    new: Dict[str, Any] = {}
    et = str(entity_type) if entity_type is not None else ""
    for fk, scope_map in facets.items():
        if fk == "descriptive":
            new[FACET_DESC] = _norm_scope_map(scope_map)
        elif fk == "relation":
            new[FACET_RELATION] = _norm_scope_map(scope_map)
        elif fk == "structural":
            sm = scope_map if isinstance(scope_map, dict) else {}
            inner = sm.get("global") if "global" in sm else sm.get(SCOPE_GLOBAL)
            if et == "TABLE":
                new[F_SOURCE_TABLE] = {SCOPE_GLOBAL: _structural_to_source_table(inner)}
            elif et == "ATTRIBUTE":
                new[F_SOURCE_COLUMN] = {SCOPE_GLOBAL: _structural_to_source_column(inner)}
            else:
                new["urn:mill/metadata/facet-type:structural"] = _norm_scope_map(scope_map)
        elif fk in ("concept", "value-mapping"):
            urn = {
                "concept": "urn:mill/metadata/facet-type:concept",
                "value-mapping": "urn:mill/metadata/facet-type:value-mapping",
            }[fk]
            new[urn] = _norm_scope_map(scope_map)
        else:
            new[fk] = scope_map
    return new


def convert_root(data: Dict[str, Any]) -> Dict[str, Any]:
    out: Dict[str, Any] = {
        "metadataFormat": "CANONICAL",
        "formatVersion": 1,
    }
    for k, v in data.items():
        if k == "entities" and isinstance(v, list):
            ents = []
            for e in v:
                if not isinstance(e, dict):
                    ents.append(e)
                    continue
                e2 = dict(e)
                et = e2.get("type")
                if "facets" in e2:
                    e2["facets"] = _transform_facets(e2["facets"], et)
                ents.append(e2)
            out["entities"] = ents
        else:
            out[k] = v
    return out


def main() -> None:
    path_in = sys.argv[1]
    path_out = sys.argv[2]
    with open(path_in, encoding="utf-8") as f:
        data = yaml.safe_load(f)
    if not isinstance(data, dict):
        raise SystemExit("expected root mapping")
    conv = convert_root(data)
    with open(path_out, "w", encoding="utf-8") as f:
        yaml.dump(
            conv,
            f,
            default_flow_style=False,
            allow_unicode=True,
            sort_keys=False,
            width=120,
        )


if __name__ == "__main__":
    main()
