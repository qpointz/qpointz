CREATE SCHEMA IF NOT EXISTS ts;

DROP TABLE IF EXISTS ts.TEST;

CREATE TABLE ts.TEST (
    id INT NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    amount INT
);

INSERT INTO ts.TEST (id, first_name, last_name, amount) VALUES (1, 'Alice', 'Smith', 100);
INSERT INTO ts.TEST (id, first_name, last_name, amount) VALUES (2, 'Bob', 'Jones', 200);
INSERT INTO ts.TEST (id, first_name, last_name, amount) VALUES (3, 'Charlie', 'Brown', 300);
