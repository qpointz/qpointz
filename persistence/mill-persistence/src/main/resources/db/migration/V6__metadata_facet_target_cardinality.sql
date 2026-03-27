-- V6: Add facet target cardinality to platform manifest JSON.
-- structural/descriptive/relation/concept are single-value per target, value-mapping is multi-value.

UPDATE metadata_facet_type
SET manifest_json =
    '{"typeKey":"urn:mill/metadata/facet-type:structural","title":"Structural","description":"Physical schema binding","enabled":true,"mandatory":true,"targetCardinality":"SINGLE","applicableTo":["urn:mill/metadata/entity-type:table","urn:mill/metadata/entity-type:attribute"],"schemaVersion":"1.0","payload":{"type":"object","title":"Structural payload","description":"Structural facet payload schema.","fields":[],"required":[]}}'
WHERE type_key = 'urn:mill/metadata/facet-type:structural';

UPDATE metadata_facet_type
SET manifest_json =
    '{"typeKey":"urn:mill/metadata/facet-type:descriptive","title":"Descriptive","description":"Human-readable metadata","enabled":true,"mandatory":true,"targetCardinality":"SINGLE","applicableTo":["urn:mill/metadata/entity-type:schema","urn:mill/metadata/entity-type:table","urn:mill/metadata/entity-type:attribute"],"schemaVersion":"1.0","payload":{"type":"object","title":"Descriptive payload","description":"Descriptive facet payload schema.","fields":[],"required":[]}}'
WHERE type_key = 'urn:mill/metadata/facet-type:descriptive';

UPDATE metadata_facet_type
SET manifest_json =
    '{"typeKey":"urn:mill/metadata/facet-type:relation","title":"Relation","description":"Cross-entity relationships","enabled":true,"mandatory":true,"targetCardinality":"SINGLE","applicableTo":["urn:mill/metadata/entity-type:table"],"schemaVersion":"1.0","payload":{"type":"object","title":"Relation payload","description":"Relation facet payload schema.","fields":[],"required":[]}}'
WHERE type_key = 'urn:mill/metadata/facet-type:relation';

UPDATE metadata_facet_type
SET manifest_json =
    '{"typeKey":"urn:mill/metadata/facet-type:concept","title":"Concept","description":"Business concept definitions","enabled":true,"mandatory":false,"targetCardinality":"SINGLE","applicableTo":["urn:mill/metadata/entity-type:concept"],"schemaVersion":"1.0","payload":{"type":"object","title":"Concept payload","description":"Concept facet payload schema.","fields":[],"required":[]}}'
WHERE type_key = 'urn:mill/metadata/facet-type:concept';

UPDATE metadata_facet_type
SET manifest_json =
    '{"typeKey":"urn:mill/metadata/facet-type:value-mapping","title":"Value Mapping","description":"Attribute value mappings for NL-to-SQL","enabled":true,"mandatory":false,"targetCardinality":"MULTIPLE","applicableTo":["urn:mill/metadata/entity-type:attribute"],"schemaVersion":"1.0","payload":{"type":"object","title":"Value mapping payload","description":"Value mapping facet payload schema.","fields":[],"required":[]}}'
WHERE type_key = 'urn:mill/metadata/facet-type:value-mapping';

