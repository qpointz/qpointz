-- WI-143: Rewrite legacy flat entity URNs to typed model URNs.
-- Converts: urn:mill/metadata/entity:<id>  →  urn:mill/model/<kind>:<id>
-- Requires entity_kind to be set; rows without a kind are left unchanged.
UPDATE metadata_entity
SET entity_res = 'urn:mill/model/' || entity_kind || ':' || SUBSTRING(entity_res, 26)
WHERE entity_res LIKE 'urn:mill/metadata/entity:%'
  AND entity_kind IS NOT NULL;
