# Skymill KPI Cookbook

This Appendix page provides ready-to-use SQL examples on the Skymill sample dataset used in docs and demos.
Use these as analyst starter queries, then adapt dimensions, filters, and time windows to your needs.

---

## Notes before you start

- Queries use the sample schema: `skymill`
- Identifiers are written with backticks
- Dataset is synthetic, so numbers are for example analysis patterns

---

## Passenger Revenue KPIs

### Total ticket revenue

```sql
SELECT
  SUM(tp.`total_price`) AS total_revenue
FROM `skymill`.`ticket_prices` tp
```

### Revenue by travel class

```sql
SELECT
  tp.`travel_class`,
  COUNT(*) AS tickets,
  SUM(tp.`total_price`) AS revenue,
  AVG(tp.`total_price`) AS avg_ticket_price
FROM `skymill`.`ticket_prices` tp
GROUP BY tp.`travel_class`
ORDER BY revenue DESC
```

### Revenue by route (top 10)

```sql
SELECT
  oc.`city` AS origin_city,
  dc.`city` AS destination_city,
  COUNT(*) AS bookings,
  SUM(tp.`total_price`) AS revenue
FROM `skymill`.`bookings` b
JOIN `skymill`.`ticket_prices` tp
  ON tp.`booking_id` = b.`id`
JOIN `skymill`.`flight_instances` fi
  ON fi.`id` = b.`flight_instance_id`
JOIN `skymill`.`segments` s
  ON s.`id` = fi.`segment_id`
JOIN `skymill`.`cities` oc
  ON oc.`id` = s.`origin`
JOIN `skymill`.`cities` dc
  ON dc.`id` = s.`destination`
GROUP BY oc.`city`, dc.`city`
ORDER BY revenue DESC
LIMIT 10
```

---

## Flight Operations KPIs

### Delay rate (share of flights with at least one delay event)

```sql
SELECT
  COUNT(DISTINCT d.`flight_instance_id`) AS delayed_flights,
  COUNT(DISTINCT fi.`id`) AS total_flights,
  ROUND(
    100.0 * COUNT(DISTINCT d.`flight_instance_id`) / NULLIF(COUNT(DISTINCT fi.`id`), 0),
    2
  ) AS delay_rate_pct
FROM `skymill`.`flight_instances` fi
LEFT JOIN `skymill`.`delays` d
  ON d.`flight_instance_id` = fi.`id`
```

### Average delay by reason

```sql
SELECT
  d.`reason`,
  COUNT(*) AS delay_events,
  AVG(d.`delay_minutes`) AS avg_delay_minutes
FROM `skymill`.`delays` d
GROUP BY d.`reason`
ORDER BY avg_delay_minutes DESC
```

### Cancellation rate by route (top 10 by cancellations)

```sql
SELECT
  oc.`city` AS origin_city,
  dc.`city` AS destination_city,
  COUNT(c.`id`) AS cancellations,
  COUNT(DISTINCT fi.`id`) AS scheduled_flights,
  ROUND(
    100.0 * COUNT(c.`id`) / NULLIF(COUNT(DISTINCT fi.`id`), 0),
    2
  ) AS cancellation_rate_pct
FROM `skymill`.`flight_instances` fi
JOIN `skymill`.`segments` s
  ON s.`id` = fi.`segment_id`
JOIN `skymill`.`cities` oc
  ON oc.`id` = s.`origin`
JOIN `skymill`.`cities` dc
  ON dc.`id` = s.`destination`
LEFT JOIN `skymill`.`cancellations` c
  ON c.`flight_instance_id` = fi.`id`
GROUP BY oc.`city`, dc.`city`
HAVING COUNT(c.`id`) > 0
ORDER BY cancellations DESC
LIMIT 10
```

---

## Customer Experience KPIs

### Average rating by route

```sql
SELECT
  oc.`city` AS origin_city,
  dc.`city` AS destination_city,
  COUNT(r.`id`) AS ratings_count,
  AVG(r.`rating`) AS avg_rating
FROM `skymill`.`ratings` r
JOIN `skymill`.`flight_instances` fi
  ON fi.`id` = r.`flight_instance_id`
JOIN `skymill`.`segments` s
  ON s.`id` = fi.`segment_id`
JOIN `skymill`.`cities` oc
  ON oc.`id` = s.`origin`
JOIN `skymill`.`cities` dc
  ON dc.`id` = s.`destination`
GROUP BY oc.`city`, dc.`city`
ORDER BY avg_rating DESC, ratings_count DESC
```

### Rating by loyalty tier

```sql
SELECT
  p.`loyalty_tier`,
  COUNT(r.`id`) AS ratings_count,
  AVG(r.`rating`) AS avg_rating
FROM `skymill`.`ratings` r
JOIN `skymill`.`passenger` p
  ON p.`id` = r.`passenger_id`
GROUP BY p.`loyalty_tier`
ORDER BY avg_rating DESC
```

---

## Cargo KPIs

### Cargo revenue by region

```sql
SELECT
  cc.`region`,
  COUNT(cs.`id`) AS shipments,
  SUM(cs.`revenue`) AS total_revenue,
  AVG(cs.`revenue`) AS avg_revenue_per_shipment
FROM `skymill`.`cargo_shipments` cs
JOIN `skymill`.`cargo_clients` cc
  ON cc.`id` = cs.`client_id`
GROUP BY cc.`region`
ORDER BY total_revenue DESC
```

### Revenue per kg by cargo type

```sql
SELECT
  ct.`name` AS cargo_type,
  SUM(cs.`weight_kg`) AS total_weight_kg,
  SUM(cs.`revenue`) AS total_revenue,
  ROUND(SUM(cs.`revenue`) / NULLIF(SUM(cs.`weight_kg`), 0), 4) AS revenue_per_kg
FROM `skymill`.`cargo_shipments` cs
JOIN `skymill`.`cargo_types` ct
  ON ct.`id` = cs.`cargo_type_id`
GROUP BY ct.`name`
ORDER BY revenue_per_kg DESC
```

### Top cargo clients by revenue

```sql
SELECT
  cc.`client_name`,
  c.`name` AS country,
  COUNT(cs.`id`) AS shipments,
  SUM(cs.`revenue`) AS total_revenue
FROM `skymill`.`cargo_shipments` cs
JOIN `skymill`.`cargo_clients` cc
  ON cc.`id` = cs.`client_id`
LEFT JOIN `skymill`.`countries` c
  ON c.`id` = cc.`country_id`
GROUP BY cc.`client_name`, c.`name`
ORDER BY total_revenue DESC
LIMIT 20
```

---

## Combined Passenger + Operations KPI

### Revenue impact of delays

Compare average ticket revenue for flights with delays vs without delays.

```sql
SELECT
  CASE WHEN d.`flight_instance_id` IS NULL THEN 'on_time' ELSE 'delayed' END AS flight_status,
  COUNT(*) AS tickets,
  AVG(tp.`total_price`) AS avg_ticket_price,
  SUM(tp.`total_price`) AS total_revenue
FROM `skymill`.`bookings` b
JOIN `skymill`.`ticket_prices` tp
  ON tp.`booking_id` = b.`id`
LEFT JOIN `skymill`.`delays` d
  ON d.`flight_instance_id` = b.`flight_instance_id`
GROUP BY CASE WHEN d.`flight_instance_id` IS NULL THEN 'on_time' ELSE 'delayed' END
ORDER BY total_revenue DESC
```

---

## Next steps

- Add date filters (`departure_date`, `earning_date`, `rated_at`) for period analysis
- Slice by dimensions (`travel_class`, `region`, `loyalty_tier`, route)
- Turn KPI queries into dashboards (trend, distribution, top-N, and anomaly views)
