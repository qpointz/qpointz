drop table if exists person;

create table person (
                        id int not null ,
                        first_name varchar2(100) not null,
                        last_name varchar2(100) not null,
                        birthday date not null
);

insert into person (id, first_name, last_name, birthday) values (1, 'Marwin', 'Lamberts', '2019-12-08');
insert into person (id, first_name, last_name, birthday) values (2, 'Zandra', 'Rookledge', '2019-05-24');
insert into person (id, first_name, last_name, birthday) values (3, 'Corrianne', 'Klewer', '2019-10-13');
insert into person (id, first_name, last_name, birthday) values (4, 'Katie', 'Cesaric', '2019-11-12');
insert into person (id, first_name, last_name, birthday) values (5, 'Conn', 'Dupree', '2019-11-03');
insert into person (id, first_name, last_name, birthday) values (6, 'Neron', 'Askam', '2019-03-15');
insert into person (id, first_name, last_name, birthday) values (7, 'Maggie', 'Stile', '2019-03-19');
insert into person (id, first_name, last_name, birthday) values (8, 'Jessalin', 'Tassell', '2019-03-17');
insert into person (id, first_name, last_name, birthday) values (9, 'Mellie', 'Bertelet', '2020-01-10');
insert into person (id, first_name, last_name, birthday) values (10, 'Pia', 'Biernat', '2019-08-22');
insert into person (id, first_name, last_name, birthday) values (11, 'Flori', 'Dossettor', '2019-10-07');
insert into person (id, first_name, last_name, birthday) values (12, 'Fairlie', 'McCathy', '2019-09-06');
insert into person (id, first_name, last_name, birthday) values (13, 'Damaris', 'Heap', '2019-08-27');
insert into person (id, first_name, last_name, birthday) values (14, 'Janette', 'Grazier', '2020-02-28');
insert into person (id, first_name, last_name, birthday) values (15, 'Luci', 'Silverstone', '2020-02-23');
insert into person (id, first_name, last_name, birthday) values (16, 'Townie', 'Belfield', '2019-06-21');
insert into person (id, first_name, last_name, birthday) values (17, 'Bert', 'Summerrell', '2019-11-05');
insert into person (id, first_name, last_name, birthday) values (18, 'Felice', 'Aldis', '2019-12-19');
insert into person (id, first_name, last_name, birthday) values (19, 'Gayler', 'Peckett', '2019-05-22');
insert into person (id, first_name, last_name, birthday) values (20, 'Malena', 'Dummigan', '2019-11-03');

/*CREATE USER IF NOT EXISTS testuser password 'testpassword';
GRANT SELECT ON person TO testuser;*/