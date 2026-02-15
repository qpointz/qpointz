"""Integration tests — schema introspection against skymill dataset."""
from __future__ import annotations

import pytest

from mill import MillClient, MillSchema
from mill.exceptions import MillQueryError

# Expected tables in the skymill schema (UPPERCASE as returned by the server)
EXPECTED_TABLES = {
    "CITIES",
    "SEGMENTS",
    "AIRCRAFT",
    "AIRCRAFT_TYPES",
    "PASSENGER",
    "FLIGHT_INSTANCES",
    "CARGO_FLIGHTS",
    "BOOKINGS",
    "LOYALTY_EARNINGS",
    "DELAYS",
    "CANCELLATIONS",
    "TICKET_PRICES",
    "RATINGS",
    "COUNTRIES",
    "CARGO_CLIENTS",
    "CARGO_TYPES",
    "CARGO_SHIPMENTS",
}

# Expected fields for the CITIES table (UPPERCASE as returned by the server)
CITIES_FIELDS = ["ID", "STATE", "CITY", "POPULATION", "AIRPORT", "AIRPORT_IATA"]


@pytest.mark.integration
class TestListSchemas:
    """Validate list_schemas() against the live service."""

    def test_returns_at_least_one(self, mill_client: MillClient) -> None:
        schemas = mill_client.list_schemas()
        assert len(schemas) > 0, "Expected at least one schema"

    def test_contains_target_schema(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        schemas = mill_client.list_schemas()
        assert schema_name in schemas, (
            f"Expected {schema_name!r} in {schemas}"
        )


@pytest.mark.integration
class TestGetSchema:
    """Validate get_schema() returns the skymill schema structure."""

    def test_returns_mill_schema(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        schema = mill_client.get_schema(schema_name)
        assert isinstance(schema, MillSchema)

    def test_contains_expected_tables(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        schema = mill_client.get_schema(schema_name)
        actual_names = {t.name for t in schema.tables}
        missing = EXPECTED_TABLES - actual_names
        assert not missing, f"Missing tables: {missing}"

    def test_cities_field_count(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        schema = mill_client.get_schema(schema_name)
        cities = next(
            (t for t in schema.tables if t.name == "CITIES"), None,
        )
        assert cities is not None, "CITIES table not found"
        assert len(cities.fields) == len(CITIES_FIELDS), (
            f"Expected {len(CITIES_FIELDS)} fields, got {len(cities.fields)}: "
            f"{[f.name for f in cities.fields]}"
        )

    def test_cities_field_names(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        schema = mill_client.get_schema(schema_name)
        cities = next(
            (t for t in schema.tables if t.name == "CITIES"), None,
        )
        assert cities is not None
        actual_names = [f.name for f in cities.fields]
        for expected in CITIES_FIELDS:
            assert expected in actual_names, (
                f"Field {expected!r} not found in CITIES — got {actual_names}"
            )

    def test_nonexistent_schema_raises(self, mill_client: MillClient) -> None:
        with pytest.raises(MillQueryError):
            mill_client.get_schema("NO_SUCH_SCHEMA_XYZ_999")
