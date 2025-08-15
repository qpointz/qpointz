
-- H2 multi-schema sample (2 schemas, 2 tables each) with cross-schema joins
-- Run with: jdbc:h2:mem:multi;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
-- Or file DB: jdbc:h2:file:./multi;MODE=PostgreSQL

SET MODE PostgreSQL;

DROP SCHEMA IF EXISTS schema2 CASCADE;
DROP SCHEMA IF EXISTS schema1 CASCADE;

CREATE SCHEMA schema1;
CREATE SCHEMA schema2;

-- =======================
-- schema1
-- =======================
CREATE TABLE schema1.customers (
    customer_id INT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    country     VARCHAR(50)  NOT NULL
);

CREATE TABLE schema1.orders (
    order_id    INT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id  INT NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    order_date  DATE NOT NULL
);

-- =======================
-- schema2
-- =======================
CREATE TABLE schema2.products (
    product_id INT PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    category   VARCHAR(60)  NOT NULL,
    price      DECIMAL(10,2) NOT NULL
);

CREATE TABLE schema2.shipments (
    shipment_id INT PRIMARY KEY,
    order_id    INT NOT NULL,
    shipped_at  DATE,
    carrier     VARCHAR(60)
);

-- =======================
-- DATA
-- =======================

-- customers (12 rows)
INSERT INTO schema1.customers (customer_id, name, country) VALUES
 (1,'Alice','CH'),
 (2,'Bob','DE'),
 (3,'Charlie','FR'),
 (4,'Diana','IT'),
 (5,'Ethan','ES'),
 (6,'Fiona','NL'),
 (7,'George','AT'),
 (8,'Hana','CZ'),
 (9,'Ivan','PL'),
 (10,'Julia','SE'),
 (11,'Karl','NO'),
 (12,'Lena','DK');

-- products (15 rows)
INSERT INTO schema2.products (product_id, name, category, price) VALUES
 (101,'Widget A','Gadgets',19.90),
 (102,'Widget B','Gadgets',24.50),
 (103,'Widget C','Gadgets',29.00),
 (104,'Cable 1m','Accessories',4.99),
 (105,'Cable 2m','Accessories',6.49),
 (106,'Charger 15W','Power',14.90),
 (107,'Charger 30W','Power',24.90),
 (108,'Power Bank 10k','Power',29.90),
 (109,'Headphones','Audio',39.00),
 (110,'Earbuds','Audio',34.00),
 (111,'Speaker Mini','Audio',25.00),
 (112,'Case Small','Accessories',8.90),
 (113,'Case Large','Accessories',12.90),
 (114,'Mount Stand','Accessories',18.00),
 (115,'Smart Light','Home',22.00);

-- orders (20 rows) referencing customers and products
INSERT INTO schema1.orders (order_id, customer_id, product_id, amount, order_date) VALUES
 (1001, 1, 101, 19.90, DATE '2025-07-01'),
 (1002, 2, 109, 39.00, DATE '2025-07-02'),
 (1003, 3, 104,  4.99, DATE '2025-07-03'),
 (1004, 4, 106, 14.90, DATE '2025-07-04'),
 (1005, 5, 108, 29.90, DATE '2025-07-05'),
 (1006, 6, 110, 34.00, DATE '2025-07-06'),
 (1007, 7, 112,  8.90, DATE '2025-07-07'),
 (1008, 8, 115, 22.00, DATE '2025-07-08'),
 (1009, 9, 103, 29.00, DATE '2025-07-09'),
 (1010,10, 105,  6.49, DATE '2025-07-10'),
 (1011,11, 107, 24.90, DATE '2025-07-11'),
 (1012,12, 111, 25.00, DATE '2025-07-12'),
 (1013, 1, 113, 12.90, DATE '2025-07-13'),
 (1014, 2, 114, 18.00, DATE '2025-07-14'),
 (1015, 3, 102, 24.50, DATE '2025-07-15'),
 (1016, 4, 101, 19.90, DATE '2025-07-16'),
 (1017, 5, 109, 39.00, DATE '2025-07-17'),
 (1018, 6, 110, 34.00, DATE '2025-07-18'),
 (1019, 7, 112,  8.90, DATE '2025-07-19'),
 (1020, 8, 115, 22.00, DATE '2025-07-20');

-- shipments (18 rows) referencing orders (some not shipped yet)
INSERT INTO schema2.shipments (shipment_id, order_id, shipped_at, carrier) VALUES
 (5001,1001, DATE '2025-07-02','DHL'),
 (5002,1002, DATE '2025-07-03','DHL'),
 (5003,1003, DATE '2025-07-04','UPS'),
 (5004,1004, DATE '2025-07-05','UPS'),
 (5005,1005, DATE '2025-07-06','SwissPost'),
 (5006,1006, DATE '2025-07-07','SwissPost'),
 (5007,1007, DATE '2025-07-08','DHL'),
 (5008,1008, DATE '2025-07-09','DHL'),
 (5009,1009, DATE '2025-07-10','UPS'),
 (5010,1010, DATE '2025-07-11','UPS'),
 (5011,1011, DATE '2025-07-12','SwissPost'),
 (5012,1012, DATE '2025-07-13','SwissPost'),
 (5013,1013, DATE '2025-07-14','DHL'),
 (5014,1014, DATE '2025-07-15','DHL'),
 (5015,1015, DATE '2025-07-16','UPS'),
 (5016,1016, DATE '2025-07-17','UPS'),
 (5017,1017, NULL, NULL),
 (5018,1018, NULL, NULL);

-- =======================
-- SAMPLE QUERIES
-- =======================

-- List tables per schema
-- SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES ORDER BY 1,2;

-- Cross-schema join: customers -> orders -> products
-- SELECT c.name AS customer, p.name AS product, o.amount, o.order_date
-- FROM schema1.customers c
-- JOIN schema1.orders o   ON o.customer_id = c.customer_id
-- JOIN schema2.products p ON p.product_id  = o.product_id
-- ORDER BY o.order_date;

-- Cross-schema join: orders -> shipments
-- SELECT o.order_id, o.order_date, s.shipped_at, s.carrier
-- FROM schema1.orders o
-- LEFT JOIN schema2.shipments s ON s.order_id = o.order_id
-- ORDER BY o.order_id;
