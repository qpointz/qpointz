create table Dim1
(
    id   INT NOT NULL,
    name VARCHAR(100) NULL
);

create table Dim2
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

SELECT *
    FROM Dim1
    where ID > 10;