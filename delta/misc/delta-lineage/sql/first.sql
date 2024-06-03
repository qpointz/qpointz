create table Dim1
(
    id   INT NOT NULL,
    first_name VARCHAR(100) NULL,
    second_name VARCHAR(100) NULL
);

create table Dim2
(
    id INT NOT NULL,
    name VARCHAR(100) NULL
)
;

create table Dim3
(
    id INT NOT NULL,
    name VARCHAR(100) NULL
)
;

create view DD4
    AS
SELECT a.*
    FROM Dim1 a inner join Dim2 D on a.name = D.name
;

SELECT Dim1.*, Dim1.first_name || ' ' || Dim1.second_name AS NAME
    FROM Dim1;
  --  where ID > 10;

insert into Dim3(id, name)
    SELECT Dim1.id, Dim1.first_name || ' ' || Dim1.second_name AS NAME
    FROM Dim1;