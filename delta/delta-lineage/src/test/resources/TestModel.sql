create table Person
(
    id   INT NOT NULL,
    first_name VARCHAR(100) NULL,
    last_name VARCHAR(100) NULL
);

create table Item
(
  id INT NOT NULL,
  name VARCHAR(128)
);

create table PersonItem
(
    id INT NOT NULL,
    person_id INT NOT NULL,
    item_id INT NOT NULL,
    item_count VARCHAR(100) NULL
);