# Metadata greenfield reference artefacts

- **`V4__metadata_greenfield.sql`** — full DDL + seeds for `descriptive` and `concept` facet types per story SPEC §8.3–8.5. **Not** executed by Flyway in the repo until legacy metadata migrations (V4–V10) are removed and this file is copied into `persistence/mill-persistence/src/main/resources/db/migration/` as the sole metadata baseline (fresh DB only).

Use this as the source of truth when implementing **WI-122** and aligning JPA entities (§9).
