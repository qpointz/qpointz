# Data Quality Rule Catalog

**Metadata facets (L1/L2):** Rules expressible as a single relational plan are documented as platform facet
types in [`metadata/dq-rule-facet-types.md`](../metadata/dq-rule-facet-types.md).  
L1 seeds: [`platform-dq-l1-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l1-facet-types.yaml).  
L2 seeds: [`platform-dq-l2-facet-types.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-dq-l2-facet-types.yaml).  
Relational plan sketches (pseudo SQL): [`dq-rule-relplan-sketches.md`](../metadata/dq-rule-relplan-sketches.md).  
Delivery story: [`dqm-metadata-facets`](../../workitems/in-progress/dqm-metadata-facets/STORY.md).

### L1 catalog → facet type

| Catalog rule | Facet type URN |
|--------------|----------------|
| Null Check | `urn:mill/metadata/facet-type:dq-null-check` |
| Empty Value Check | `urn:mill/metadata/facet-type:dq-empty-value-check` |
| Unique Value Check | `urn:mill/metadata/facet-type:dq-unique-value-check` |
| Allowed Values Check | `urn:mill/metadata/facet-type:dq-allowed-values-check` |
| Pattern Check | `urn:mill/metadata/facet-type:dq-pattern-check` |
| Min/Max Check | `urn:mill/metadata/facet-type:dq-min-max-check` |
| Data Age Check | `urn:mill/metadata/facet-type:dq-data-age-check` |
| Foreign Key Check | `dq-referential-integrity`, `dq-referential-source`, `dq-referential-target` |

### L2 catalog → facet type

| Catalog rule | Facet type URN |
|--------------|----------------|
| Cross-Column Consistency | `urn:mill/metadata/facet-type:dq-predicate` |
| Derived Value Validation | `urn:mill/metadata/facet-type:dq-predicate` |
| Conditional Completeness | `urn:mill/metadata/facet-type:dq-predicate` |
| Composite Uniqueness | `urn:mill/metadata/facet-type:dq-composite-uniqueness` |
| Parent-Child Reconciliation | `urn:mill/metadata/facet-type:dq-parent-child-reconciliation` |
| Cross-Table Reconciliation | `urn:mill/metadata/facet-type:dq-cross-table-reconciliation` |
| SLA Compliance Check | `urn:mill/metadata/facet-type:dq-sla-compliance-check` |
| Semantic Validation | `urn:mill/metadata/facet-type:dq-predicate` |

Four catalog L2 rows share **`dq-predicate`** (different `predicate` values). Full sketches: design doc § L2.

| Level | Category | Rule Name | Scope | Description | Example |
|---------|----------|------------|---------|-------------|----------|
| L1 | Completeness | Null Check | Column | Required values must be present | `customer_id IS NOT NULL` |
| L1 | Completeness | Empty Value Check | Column | String values must not be empty or blank | `TRIM(name) <> ''` |
| L1 | Uniqueness | Unique Value Check | Column | Values must be unique within dataset | `customer_id` unique |
| L1 | Validity | Allowed Values Check | Column | Value must belong to predefined domain | `status IN ('ACTIVE','INACTIVE')` |
| L1 | Validity | Pattern Check | Column | Value must match expected format or regex | Email, phone, UUID |
| L1 | Referential Integrity | Foreign Key Check | Column | Referenced record must exist | `orders.customer_id -> customers.customer_id` |
| L1 | Freshness | Data Age Check | Table | Data must be updated within expected SLA | Last update within maxAge of now or of expected schedule tick |
| L1 | Range | Min/Max Check | Column | Numeric or date value must be within bounds | `age BETWEEN 0 AND 120` |

| L2 | Consistency | Cross-Column Consistency | Table | Related columns must satisfy business rule | `start_date <= end_date` |
| L2 | Consistency | Derived Value Validation | Table | Calculated values must match source values | `total = subtotal + tax` |
| L2 | Completeness | Conditional Completeness | Table | Field required when condition is true | `termination_date` required when status='TERMINATED' |
| L2 | Uniqueness | Composite Uniqueness | Table | Combination of columns must be unique | `(country, tax_id)` unique |
| L2 | Reconciliation | Parent-Child Reconciliation | Table | Aggregated detail records must match summary record | Invoice lines = invoice total |
| L2 | Reconciliation | Cross-Table Reconciliation | Tables | Totals between related tables must match | Orders total = Shipments total |
| L2 | Timeliness | SLA Compliance Check | Table | Data arrival must meet expected schedule | Daily feed before 06:00 |
| L2 | Validity | Semantic Validation | Column | Value is logically valid beyond format | Birth date not in future |

| L3 | Volume | Row Count Anomaly Detection | Table | Detect unexpected volume changes | Daily rows drop by 80% |
| L3 | Distribution | Distribution Drift Detection | Column | Detect significant changes in value distribution | Country mix changes dramatically |
| L3 | Distribution | Statistical Outlier Detection | Column | Detect extreme values | Unusually large transaction amount |
| L3 | Accuracy | Master Data Validation | Column | Validate against authoritative reference data | Product code exists in catalog |
| L3 | Accuracy | External Reference Validation | Column | Validate against external authoritative source | Postal code exists in registry |
| L3 | Referential Integrity | Temporal Integrity Check | Table | Relationship valid for corresponding time period | Order date within customer active period |
| L3 | Duplicate Detection | Fuzzy Duplicate Detection | Table | Detect probable duplicate entities | "IBM Corp" vs "I.B.M Corporation" |
| L3 | Reconciliation | Cross-System Reconciliation | Systems | Metrics match across systems | CRM customer count ≈ ERP customer count |
| L3 | Business Rule | Lifecycle Validation | Table | Entity lifecycle follows business process | Shipment cannot precede order |
| L3 | Business Rule | State Transition Validation | Table | State changes follow allowed transitions | Closed order cannot return to Draft |

## Recommended Implementation Roadmap

### L1 (Core DQ)
Implement first. Covers the majority of common data quality issues.

- Null Check
- Empty Value Check
- Unique Value Check
- Allowed Values Check
- Pattern Check
- Foreign Key Check
- Data Age Check
- Min/Max Check

### L2 (Business DQ)
Implement after L1. Captures business correctness.

- Cross-Column Consistency
- Derived Value Validation
- Conditional Completeness
- Composite Uniqueness
- Reconciliation Checks
- SLA Compliance
- Semantic Validation

### L3 (Advanced DQ)
Implement when historical data and monitoring infrastructure are available.

- Anomaly Detection
- Drift Detection
- Outlier Detection
- Master Data Validation
- External Validation
- Temporal Integrity
- Fuzzy Duplicate Detection
- Cross-System Reconciliation
- Lifecycle Validation
- State Transition Validation