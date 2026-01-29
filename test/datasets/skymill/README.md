# Skymill Airlines — Demo Dataset Summary

Skymill Airlines is a fictional airline operating across two business lines: **passenger transport** and **cargo freight**. The dataset models a realistic airline business with shared infrastructure (aircraft, routes) and distinct commercial workflows, suited for NL2SQL demonstrations.

## Regenerating the dataset

From the `test/` directory:

```bash
make regen-skymill
```

Or run qsynth directly:

```bash
cd test
docker run -v "$(pwd):/data" qpointz/qsynth run --input-file /data/skymill.yaml -e write_csv -e write_parquet
```

Outputs are written to `datasets/skymill/csv/` and `datasets/skymill/parquet/`.

## Core schema

- **cities** — Geographic locations (airports) served by the airline.
- **segments** — Itineraries: origin-destination routes between cities.
- **aircraft_types** — Slow-moving dimension for aircraft type (narrow, wide, regional).
- **aircraft** — Fleet with registration codes (e.g. SKY123), aircraft_type_id (→ aircraft_types), capacity.
- **passenger** — Registered passengers with loyalty program; can be anonymous (walk-up) or enrolled.
- **flight_instances** — Concrete passenger flights with dates: a specific aircraft flying a specific route on given dates.
- **cargo_flights** — Concrete cargo flights with dates: a specific aircraft transporting cargo along a route on given dates.
- **bookings** — Passenger bookings linking passengers to flight instances (seat, ticket).
- **cargo_clients** — Clients who ship cargo via cargo flights (linked to countries).
- **cargo_types** — Slow-moving dimension for cargo type (containers, bulk, refrigerated, vehicles).
- **cargo_shipments** — Shipments on cargo flights: cargo_type_id (→ cargo_types), weight, client_id (→ cargo_clients), revenue.
- **loyalty_earnings** — Mileage earned per booking for loyalty members.
- **delays** — Flight delay records (duration, reason).
- **cancellations** — Cancelled flight instances with reasons.
- **ticket_prices** — Fare breakdown (base, taxes, travel_class, currency).
- **ratings** — Passenger feedback scores for completed flights.
- **countries** — Reference data (iso_code, name) for geographic categorization.

## Business lines

- **Passenger transport** — Bookings on flight instances, loyalty program, ticket pricing, ratings.
- **Cargo freight** — Cargo flights, cargo_clients (with country), and cargo shipments with type, weight, revenue.

## Schema details

| Table | Key attributes |
|-------|-----------------|
| cities | id, city, state, airport, airport_iata |
| segments | id, origin (→ cities), destination (→ cities), distance |
| aircraft_types | id, name, description (static) |
| aircraft | id, aircraft_code (SKY###), aircraft_type_id (→ aircraft_types), capacity |
| passenger | id, first_name, last_name, domicile_city_id (→ cities), loyalty_program_member, loyalty_tier |
| flight_instances | id, aircraft_id (→ aircraft), segment_id (→ segments), departure_date, arrival_date |
| cargo_flights | id, aircraft_id (→ aircraft), segment_id (→ segments), departure_date, arrival_date |
| bookings | id, passenger_id (→ passenger), flight_instance_id (→ flight_instances), seat_number |
| cargo_clients | id, client_name, address, city, country_id (→ countries), postal_code, region |
| cargo_types | id, name, description (static) |
| cargo_shipments | id, cargo_flight_id (→ cargo_flights), cargo_type_id (→ cargo_types), weight_kg, client_id (→ cargo_clients), revenue |
| loyalty_earnings | id, booking_id (→ bookings), passenger_id (→ passenger), miles_earned |
| delays | id, flight_instance_id (→ flight_instances), delay_minutes, reason |
| cancellations | id, flight_instance_id (→ flight_instances), cancellation_reason |
| ticket_prices | booking_id (→ bookings), base_price, taxes, total_price, travel_class, currency |
| ratings | id, passenger_id (→ passenger), flight_instance_id (→ flight_instances), rating, rating_comment, rated_at |
| countries | id, iso_code, name |

## Entity-relationship diagram

```mermaid
flowchart TB
    subgraph base [Base]
        cities[ cities ]
        segments[ segments ]
        aircraft_types[ aircraft_types ]
        aircraft[ aircraft ]
    end
    
    aircraft_types --> aircraft
    
    subgraph occurrences [Concrete Flights]
        flight_instances[ flight_instances ]
        cargo_flights[ cargo_flights ]
    end
    
    subgraph passenger [Passenger]
        passenger[ passenger ]
        bookings[ bookings ]
        loyalty_earnings[ loyalty_earnings ]
    end
    
    subgraph cargo [Cargo]
        countries[ countries ]
        cargo_clients[ cargo_clients ]
        cargo_types[ cargo_types ]
        cargo_shipments[ cargo_shipments ]
    end
    
    countries --> cargo_clients
    cargo_types --> cargo_shipments
    
    subgraph ops [Operations]
        delays[ delays ]
        cancellations[ cancellations ]
    end
    
    cities --> segments
    aircraft --> flight_instances
    segments --> flight_instances
    aircraft --> cargo_flights
    segments --> cargo_flights
    cargo_clients --> cargo_shipments
    cargo_flights --> cargo_shipments
    cities --> passenger
    passenger --> bookings
    flight_instances --> bookings
    bookings --> loyalty_earnings
    flight_instances --> delays
    flight_instances --> cancellations
```

## Analytical use cases

- Passenger counts and revenue by route or period
- Cargo volume and revenue by route, period, or country
- Flight delays and cancellations by reason
- Loyalty program mileage and tier distribution
- Aircraft utilization by type (narrow/wide/regional) across passenger and cargo
- Ticket pricing by class and currency
- Passenger ratings and feedback trends

The schema is intentionally "real-world" and intuitive, while complex enough to demonstrate NL2SQL capabilities: schema reasoning, conditional joins, time-based analysis, and explainable query generation.
