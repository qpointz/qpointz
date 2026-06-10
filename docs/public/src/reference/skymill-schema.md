# Skymill Schema Reference

This page documents the **example dataset** used in Mill tutorials, quickstarts, and query samples.
It is supplemental reference material for examples, not product configuration/reference documentation.

Skymill is a synthetic airline analytics dataset designed for business and operational analysis.
It mixes passenger and cargo domains so analysts can answer questions about revenue, demand, operations, punctuality, and customer experience.

---

## How to read this schema

- Passenger flow: `passenger` -> `bookings` -> `flight_instances`
- Revenue flow: `bookings` -> `ticket_prices`, and `cargo_shipments` -> `revenue`
- Network flow: `segments` links origin/destination using `cities`
- Fleet flow: `aircraft` + `aircraft_types` tied to both passenger and cargo flights
- Operations quality: `delays`, `cancellations`, `ratings`

---

## Core relationship map

- `segments.origin` -> `cities.id`
- `segments.destination` -> `cities.id`
- `flight_instances.segment_id` -> `segments.id`
- `flight_instances.aircraft_id` -> `aircraft.id`
- `cargo_flights.segment_id` -> `segments.id`
- `cargo_flights.aircraft_id` -> `aircraft.id`
- `bookings.passenger_id` -> `passenger.id`
- `bookings.flight_instance_id` -> `flight_instances.id`
- `ticket_prices.booking_id` -> `bookings.id`
- `loyalty_earnings.booking_id` -> `bookings.id`
- `loyalty_earnings.passenger_id` -> `passenger.id`
- `delays.flight_instance_id` -> `flight_instances.id`
- `cancellations.flight_instance_id` -> `flight_instances.id`
- `ratings.passenger_id` -> `passenger.id`
- `ratings.flight_instance_id` -> `flight_instances.id`
- `cargo_clients.country_id` -> `countries.id`
- `cargo_shipments.cargo_flight_id` -> `cargo_flights.id`
- `cargo_shipments.cargo_type_id` -> `cargo_types.id`
- `cargo_shipments.client_id` -> `cargo_clients.id`

---

## Table catalog

### `cities`

Reference list of airport cities used in routes and passenger residence.

- `id`: city key used in route and domicile joins
- `state`: state/region
- `city`: city name
- `population`: city population
- `airport`: airport full name
- `airport_iata`: 3-letter IATA code

### `segments`

Route master table; one row per direct city pair.

- `id`: route key
- `origin`: origin city (`cities.id`)
- `destination`: destination city (`cities.id`)
- `distance`: route distance in km

### `aircraft_types`

Small static dimension for fleet class.

- `id`: aircraft type key
- `name`: `narrow`, `wide`, `regional`
- `description`: business meaning of type

### `aircraft`

Fleet inventory table.

- `id`: aircraft key
- `aircraft_code`: registration-like code
- `aircraft_type_id`: fleet type (`aircraft_types.id`)
- `capacity`: seat capacity

### `passenger`

Passenger master table with demographics and loyalty attributes.

- `id`: passenger key
- `first_name`, `last_name`: passenger name
- `domicile_address`: home address
- `domicile_city_id`: home city (`cities.id`)
- `age`: age in years
- `loyalty_program_member`: loyalty enrollment flag
- `loyalty_tier`: `basic`, `silver`, `gold`, `platinum`

### `flight_instances`

Passenger flight facts; one row per flown passenger flight instance.

- `id`: flight instance key
- `aircraft_id`: operating aircraft (`aircraft.id`)
- `segment_id`: route (`segments.id`)
- `departure_date`: departure date
- `arrival_date`: arrival date

### `cargo_flights`

Cargo flight facts; structure mirrors passenger flights.

- `id`: cargo flight key
- `aircraft_id`: operating aircraft (`aircraft.id`)
- `segment_id`: route (`segments.id`)
- `departure_date`: departure date
- `arrival_date`: arrival date

### `bookings`

Passenger booking facts; one row per booked seat.

- `id`: booking key
- `passenger_id`: booking passenger (`passenger.id`)
- `flight_instance_id`: booked flight (`flight_instances.id`)
- `seat_number`: seat assignment

### `ticket_prices`

Commercial ticket metrics by booking.

- `booking_id`: booking (`bookings.id`)
- `base_price`: fare before taxes
- `taxes`: taxes and fees
- `total_price`: paid amount
- `travel_class`: `economy`, `business`, `first`
- `currency`: `USD`, `EUR`, `CHF`

### `loyalty_earnings`

Miles accrual fact table.

- `id`: earning record key
- `booking_id`: source booking (`bookings.id`)
- `passenger_id`: passenger (`passenger.id`)
- `miles_earned`: credited miles
- `earning_date`: posting date

### `delays`

Delay events for passenger flights.

- `id`: delay event key
- `flight_instance_id`: delayed flight (`flight_instances.id`)
- `delay_minutes`: delay duration
- `reason`: `weather`, `technical`, `crew`, `other`

### `cancellations`

Cancellation events for passenger flights.

- `id`: cancellation event key
- `flight_instance_id`: cancelled flight (`flight_instances.id`)
- `cancellation_reason`: `weather`, `technical`, `operational`, `low_demand`
- `cancellation_time`: cancellation timestamp/date

### `ratings`

Customer feedback and service quality signals.

- `id`: rating record key
- `passenger_id`: reviewing passenger (`passenger.id`)
- `flight_instance_id`: reviewed flight (`flight_instances.id`)
- `rating`: satisfaction score from 1 to 5
- `rating_comment`: free-text feedback
- `rated_at`: rating date/time

### `countries`

Country reference dimension used by cargo clients.

- `id`: country key
- `iso_code`: ISO 2-letter code
- `name`: country name

### `cargo_clients`

Shipper/customer master for cargo business.

- `id`: client key
- `client_name`: company name
- `address`: street address
- `city`: city
- `country_id`: country (`countries.id`)
- `postal_code`: postal code
- `region`: `EMEA`, `APAC`, `AM`

### `cargo_types`

Static cargo category dimension.

- `id`: cargo type key
- `name`: `containers`, `bulk`, `refrigerated`, `vehicles`
- `description`: cargo type meaning

### `cargo_shipments`

Cargo shipment fact table and core freight revenue source.

- `id`: shipment key
- `cargo_flight_id`: transport flight (`cargo_flights.id`)
- `cargo_type_id`: cargo category (`cargo_types.id`)
- `weight_kg`: shipment weight in kg
- `client_id`: shipper (`cargo_clients.id`)
- `revenue`: shipment revenue

---

## Typical analyst use cases

- Network analysis: busiest origin-destination pairs and long-haul mix
- Commercial analysis: ticket revenue by class, route, and period
- Loyalty analysis: miles earned by tier and repeat traveler behavior
- Operational quality: delay and cancellation rates by route/aircraft
- Cargo performance: revenue and weight by client, region, and cargo type
- Experience analysis: rating trends by route, aircraft, and disruption events

---

## Data notes

- This dataset is synthetic and intended for demos and testing.
- Row counts are generated and may vary by environment.
- Keys behave as business identifiers for joins but are generated values.
