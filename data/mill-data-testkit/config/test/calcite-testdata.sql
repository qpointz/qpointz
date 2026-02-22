DROP TABLE IF EXISTS CITIES;

CREATE TABLE CITIES (
    id INT NOT NULL,
    name VARCHAR(100),
    state VARCHAR(50)
);

INSERT INTO CITIES (id, name, state) VALUES (1, 'Zurich', 'ZH');
INSERT INTO CITIES (id, name, state) VALUES (2, 'Bern', 'BE');
INSERT INTO CITIES (id, name, state) VALUES (3, 'Geneva', 'GE');
