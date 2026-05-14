-- =========== skymill.cities (generic) ==========

DROP TABLE IF EXISTS cities;

CREATE TABLE cities (
  id INT NOT NULL,
  state VARCHAR(255) NOT NULL,
  city VARCHAR(255) NOT NULL,
  population INT NOT NULL,
  airport VARCHAR(255) NOT NULL,
  airport_iata VARCHAR(255) NOT NULL
);

INSERT INTO cities VALUES (452385, 'Minnesota', 'North Gregory', 3749641, 'Hamburg airport', 'VDC');
INSERT INTO cities VALUES (375963, 'New Hampshire', 'North Jessicachester', 2115012, 'Congonhas International airport', 'GVA');
INSERT INTO cities VALUES (335610, 'Colorado', 'South Bryanview', 3666221, 'Bordeaux airport', 'BKK');
INSERT INTO cities VALUES (171239, 'California', 'Perezbury', 3985644, 'General Abelardo L Rodriguez International Airpo', 'PTY');
INSERT INTO cities VALUES (224699, 'Pennsylvania', 'Lichester', 2476932, 'Fuzhou airport', 'IOS');
INSERT INTO cities VALUES (124275, 'Wyoming', 'Tinatown', 2187160, 'Edinburgh International airport', 'SAW');
INSERT INTO cities VALUES (293381, 'Missouri', 'East Oliviaport', 4120271, 'Vrazhdebna airport', 'NVT');
INSERT INTO cities VALUES (113306, 'Iowa', 'South Wendy', 4577903, 'Cologne Bonn airport', 'SCL');
INSERT INTO cities VALUES (460999, 'Hawaii', 'South Gabrielview', 18733, 'Fuzhou airport', 'OKC');
INSERT INTO cities VALUES (499654, 'Massachusetts', 'New Lisa', 2615352, 'El Calafate International airport', 'KUL');
INSERT INTO cities VALUES (139749, 'Wyoming', 'Lake Jessestad', 4642568, 'Stuttgart airport', 'LHR');
INSERT INTO cities VALUES (214351, 'South Dakota', 'North Sean', 3115441, 'Alfonso Bonilla Aragon International airport', 'TRU');
INSERT INTO cities VALUES (139963, 'Alaska', 'North Madison', 447509, 'Juana Azurduy de Padilla airport', 'CJU');
INSERT INTO cities VALUES (227902, 'Pennsylvania', 'Sharpberg', 3431514, 'Huanghua airport', 'HND');

-- =========== skymill.segments (generic) ==========

DROP TABLE IF EXISTS segments;

CREATE TABLE segments (
  id INT NOT NULL,
  origin INT NOT NULL,
  destination INT NOT NULL,
  distance INT NOT NULL
);

INSERT INTO segments VALUES (335131, 171239, 113306, 2285);
INSERT INTO segments VALUES (412625, 171239, 139963, 2100);
INSERT INTO segments VALUES (219413, 335610, 139749, 436);
INSERT INTO segments VALUES (250560, 452385, 452385, 539);
INSERT INTO segments VALUES (410727, 139749, 139963, 1169);
INSERT INTO segments VALUES (349926, 224699, 139749, 351);
INSERT INTO segments VALUES (456389, 375963, 293381, 624);
INSERT INTO segments VALUES (403209, 335610, 293381, 2173);
INSERT INTO segments VALUES (320821, 499654, 139963, 1390);
INSERT INTO segments VALUES (326795, 139749, 139749, 965);
INSERT INTO segments VALUES (387649, 293381, 224699, 1019);
INSERT INTO segments VALUES (492183, 214351, 499654, 1069);
INSERT INTO segments VALUES (373160, 171239, 124275, 1441);
INSERT INTO segments VALUES (493797, 139749, 499654, 990);
INSERT INTO segments VALUES (232414, 214351, 171239, 648);
INSERT INTO segments VALUES (185905, 293381, 460999, 2240);
INSERT INTO segments VALUES (118785, 293381, 139963, 2404);
INSERT INTO segments VALUES (227751, 124275, 214351, 1390);
INSERT INTO segments VALUES (126306, 375963, 224699, 2477);
INSERT INTO segments VALUES (375803, 335610, 224699, 1513);
INSERT INTO segments VALUES (242384, 124275, 224699, 2390);
INSERT INTO segments VALUES (214804, 375963, 214351, 643);
INSERT INTO segments VALUES (314713, 214351, 293381, 1404);
INSERT INTO segments VALUES (129782, 171239, 171239, 1341);
INSERT INTO segments VALUES (482891, 113306, 460999, 1918);
INSERT INTO segments VALUES (234001, 224699, 499654, 1301);
INSERT INTO segments VALUES (389547, 227902, 293381, 1013);
INSERT INTO segments VALUES (320444, 113306, 335610, 2023);
INSERT INTO segments VALUES (254202, 171239, 293381, 1419);
INSERT INTO segments VALUES (222532, 375963, 499654, 2360);
INSERT INTO segments VALUES (134925, 375963, 171239, 1941);
INSERT INTO segments VALUES (200726, 293381, 452385, 432);
INSERT INTO segments VALUES (494138, 171239, 452385, 1720);
INSERT INTO segments VALUES (349353, 224699, 460999, 844);
INSERT INTO segments VALUES (424311, 375963, 227902, 2438);
INSERT INTO segments VALUES (295869, 139963, 335610, 2252);
INSERT INTO segments VALUES (480025, 335610, 293381, 1083);
INSERT INTO segments VALUES (296815, 171239, 139749, 829);
INSERT INTO segments VALUES (447782, 224699, 460999, 1625);
INSERT INTO segments VALUES (272473, 460999, 452385, 801);
INSERT INTO segments VALUES (428534, 375963, 375963, 1518);
INSERT INTO segments VALUES (338854, 214351, 293381, 1632);
INSERT INTO segments VALUES (341944, 499654, 499654, 1278);
INSERT INTO segments VALUES (145650, 214351, 335610, 1518);
INSERT INTO segments VALUES (357045, 124275, 171239, 1764);
INSERT INTO segments VALUES (216072, 452385, 335610, 1264);
INSERT INTO segments VALUES (214055, 293381, 214351, 2491);
INSERT INTO segments VALUES (370748, 113306, 139749, 738);
INSERT INTO segments VALUES (279211, 499654, 214351, 1515);
INSERT INTO segments VALUES (227258, 113306, 460999, 1836);
INSERT INTO segments VALUES (344198, 452385, 171239, 1884);
INSERT INTO segments VALUES (418971, 460999, 227902, 1914);
INSERT INTO segments VALUES (175607, 214351, 375963, 1187);
INSERT INTO segments VALUES (109929, 293381, 293381, 1274);
INSERT INTO segments VALUES (189927, 224699, 452385, 569);
INSERT INTO segments VALUES (341448, 335610, 499654, 782);

-- =========== skymill.aircraft_types (generic) ==========

DROP TABLE IF EXISTS aircraft_types;

CREATE TABLE aircraft_types (
  id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL
);

INSERT INTO aircraft_types VALUES (1, 'narrow', 'Narrow-body (small) aircraft');
INSERT INTO aircraft_types VALUES (2, 'wide', 'Wide-body (large) aircraft');
INSERT INTO aircraft_types VALUES (3, 'regional', 'Regional (short-haul) aircraft');

-- =========== skymill.aircraft (generic) ==========

DROP TABLE IF EXISTS aircraft;

CREATE TABLE aircraft (
  id INT NOT NULL,
  aircraft_code VARCHAR(255) NOT NULL,
  aircraft_type_id INT NOT NULL,
  capacity INT NOT NULL
);

INSERT INTO aircraft VALUES (84275, 'SKY155', 3, 339);
INSERT INTO aircraft VALUES (35979, 'SKY192', 2, 381);
INSERT INTO aircraft VALUES (22621, 'SKY742', 3, 382);
INSERT INTO aircraft VALUES (83459, 'SKY522', 2, 397);
INSERT INTO aircraft VALUES (92725, 'SKY851', 3, 167);
INSERT INTO aircraft VALUES (38025, 'SKY593', 3, 361);
INSERT INTO aircraft VALUES (22964, 'SKY988', 1, 324);
INSERT INTO aircraft VALUES (35212, 'SKY535', 2, 330);
INSERT INTO aircraft VALUES (83612, 'SKY695', 3, 372);
INSERT INTO aircraft VALUES (95174, 'SKY054', 1, 129);
INSERT INTO aircraft VALUES (65168, 'SKY884', 1, 314);
INSERT INTO aircraft VALUES (20473, 'SKY423', 2, 122);
INSERT INTO aircraft VALUES (31389, 'SKY999', 2, 269);
INSERT INTO aircraft VALUES (72204, 'SKY635', 3, 106);
INSERT INTO aircraft VALUES (13762, 'SKY018', 2, 273);
INSERT INTO aircraft VALUES (96428, 'SKY307', 1, 330);
INSERT INTO aircraft VALUES (19647, 'SKY042', 2, 242);
INSERT INTO aircraft VALUES (39465, 'SKY415', 1, 268);
INSERT INTO aircraft VALUES (84800, 'SKY950', 1, 160);
INSERT INTO aircraft VALUES (12189, 'SKY597', 1, 136);
INSERT INTO aircraft VALUES (32040, 'SKY609', 3, 106);
INSERT INTO aircraft VALUES (39910, 'SKY163', 2, 333);
INSERT INTO aircraft VALUES (18537, 'SKY112', 3, 209);
INSERT INTO aircraft VALUES (86411, 'SKY979', 3, 314);
INSERT INTO aircraft VALUES (29934, 'SKY877', 1, 201);
INSERT INTO aircraft VALUES (36885, 'SKY404', 3, 249);
INSERT INTO aircraft VALUES (14279, 'SKY524', 1, 306);
INSERT INTO aircraft VALUES (68564, 'SKY225', 3, 169);

-- =========== skymill.passenger (generic) ==========

DROP TABLE IF EXISTS passenger;

CREATE TABLE passenger (
  id INT NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  domicile_address VARCHAR(255) NOT NULL,
  domicile_city_id INT NOT NULL,
  age INT NOT NULL,
  loyalty_program_member BOOLEAN NOT NULL,
  loyalty_tier VARCHAR(255) NOT NULL
);

INSERT INTO passenger VALUES (6651063, 'Richard', 'Bass', '02720 Rivas Meadows Suite 308
Virginiahaven, NJ 76429', 499654, 65, TRUE, 'silver');
INSERT INTO passenger VALUES (7537489, 'Roberta', 'Cook', '665 Andrew Points Suite 838
Davidville, VI 39767', 224699, 64, FALSE, 'gold');
INSERT INTO passenger VALUES (8042175, 'Thomas', 'Graves', '78946 Green Island Apt. 247
West Jamesport, CO 35776', 139963, 50, FALSE, 'platinum');
INSERT INTO passenger VALUES (7968367, 'John', 'Decker', '352 Garcia Groves
Cortezside, CO 93173', 139749, 62, TRUE, 'platinum');
INSERT INTO passenger VALUES (4332111, 'Lisa', 'Williams', '6136 Mcintyre Vista Apt. 954
Nelsonbury, WV 85168', 139963, 69, TRUE, 'silver');
INSERT INTO passenger VALUES (5109828, 'Brandon', 'Smith', '985 Thomas Bridge
Garrisonburgh, AL 99126', 499654, 74, TRUE, 'basic');
INSERT INTO passenger VALUES (1597896, 'Heidi', 'Cook', '5915 Scott Branch Apt. 470
Anneview, ME 84251', 335610, 24, FALSE, 'silver');
INSERT INTO passenger VALUES (3086160, 'Mike', 'Garcia', '112 Anderson Road
Keithtown, SD 96417', 214351, 12, FALSE, 'platinum');
INSERT INTO passenger VALUES (5520179, 'Melissa', 'Franklin', '68937 Glover Fords
South Noah, IN 81971', 227902, 42, FALSE, 'silver');
INSERT INTO passenger VALUES (1547921, 'Ryan', 'Wood', '6009 Miller Corner
East Margaret, PA 24007', 335610, 16, FALSE, 'silver');
INSERT INTO passenger VALUES (9636664, 'Sandy', 'Collins', '0367 Castro Summit Apt. 328
Fredfort, OR 67034', 171239, 54, TRUE, 'gold');
INSERT INTO passenger VALUES (1154532, 'Jessica', 'Smith', '402 Rasmussen Stravenue Apt. 563
Lake Robertchester, WV 11551', 499654, 39, FALSE, 'platinum');
INSERT INTO passenger VALUES (3603058, 'Cynthia', 'Smith', '2470 Wheeler Path
South Johnberg, IN 37968', 224699, 59, FALSE, 'basic');
INSERT INTO passenger VALUES (6405551, 'Suzanne', 'Woods', '27055 Carter Drive
Lake Courtney, FM 35376', 293381, 49, TRUE, 'basic');
INSERT INTO passenger VALUES (9992742, 'Anthony', 'Carney', '45455 Savage Inlet
West Paulville, WY 37333', 124275, 84, FALSE, 'basic');
INSERT INTO passenger VALUES (6939753, 'Joshua', 'Beard', '663 Long Tunnel Apt. 289
Ryanmouth, NJ 84937', 227902, 84, TRUE, 'gold');
INSERT INTO passenger VALUES (2858315, 'Andrea', 'Vasquez', '9033 Hudson Square Apt. 851
West Joshuaport, PR 88533', 227902, 59, TRUE, 'platinum');
INSERT INTO passenger VALUES (8218370, 'Randall', 'Mueller', '7051 Kelly Gardens Apt. 753
East Nicolechester, AL 91778', 139749, 48, TRUE, 'gold');
INSERT INTO passenger VALUES (3224135, 'Christopher', 'Franklin', 'PSC 1086, Box 3262
APO AP 49779', 171239, 43, TRUE, 'gold');
INSERT INTO passenger VALUES (6774082, 'Douglas', 'Reed', 'Unit 9145 Box 6262
DPO AA 10156', 499654, 50, FALSE, 'platinum');
INSERT INTO passenger VALUES (1892068, 'Kevin', 'Saunders', '2000 Briggs Squares Apt. 690
West Keith, NJ 44967', 227902, 81, TRUE, 'silver');
INSERT INTO passenger VALUES (8409944, 'Lauren', 'Lam', '356 Hunt Landing Suite 972
Tinaview, TN 02924', 227902, 64, TRUE, 'gold');
INSERT INTO passenger VALUES (2228727, 'Lauren', 'Perry', '60587 Jennifer Hill
South Samantha, VI 53458', 460999, 79, FALSE, 'platinum');
INSERT INTO passenger VALUES (6920459, 'Victor', 'Christensen', '89949 Marquez Roads
East Lesliefurt, SC 32608', 139749, 21, TRUE, 'basic');
INSERT INTO passenger VALUES (7220233, 'Gwendolyn', 'Hughes', '04108 Vasquez Ports
Markton, LA 33912', 293381, 70, TRUE, 'basic');
INSERT INTO passenger VALUES (5494015, 'Becky', 'Taylor', '8011 Brown Springs Apt. 329
Lake Robert, PW 57384', 139963, 44, TRUE, 'silver');
INSERT INTO passenger VALUES (1979648, 'Kristen', 'George', '669 Shannon Rapids Suite 449
Allenside, GU 93455', 171239, 20, FALSE, 'silver');
INSERT INTO passenger VALUES (3700732, 'Kevin', 'Green', '30112 Dennis Canyon Apt. 407
South Cassie, OR 66407', 139963, 84, FALSE, 'silver');
INSERT INTO passenger VALUES (2161934, 'Darlene', 'Wilson', 'USNS Green
FPO AP 48270', 335610, 31, TRUE, 'platinum');
INSERT INTO passenger VALUES (6793309, 'Cheryl', 'Johnson', '585 Bell Coves
Shannonview, IL 20930', 460999, 45, FALSE, 'platinum');
INSERT INTO passenger VALUES (3822974, 'Richard', 'Petty', '0375 Andrew Trafficway
East Matthewhaven, VT 49643', 375963, 31, TRUE, 'platinum');
INSERT INTO passenger VALUES (6383237, 'Charles', 'Brown', '7842 Stacy Prairie
Kevintown, NY 06651', 139963, 60, TRUE, 'silver');
INSERT INTO passenger VALUES (6301649, 'Anthony', 'Smith', '7296 Robert Union
West Mathewmouth, PW 99359', 139963, 67, TRUE, 'basic');
INSERT INTO passenger VALUES (6954524, 'Donald', 'Sharp', '0227 Hernandez Summit
Richardsmouth, RI 00634', 499654, 33, TRUE, 'gold');
INSERT INTO passenger VALUES (1668346, 'Amy', 'Lee', '871 Trujillo Skyway
Christophermouth, DC 21651', 113306, 90, TRUE, 'platinum');
INSERT INTO passenger VALUES (4100639, 'Rebecca', 'Carter', '4039 Noah Estate Apt. 516
Martinezton, AK 16420', 460999, 45, FALSE, 'platinum');
INSERT INTO passenger VALUES (6111066, 'Tamara', 'Anderson', '03863 Rivera Lane Suite 013
West Mandyport, WI 21513', 171239, 34, FALSE, 'platinum');
INSERT INTO passenger VALUES (6197631, 'Lindsay', 'Pham', '29378 Sydney Mission Suite 494
West Stephenborough, AS 30088', 171239, 63, FALSE, 'platinum');
INSERT INTO passenger VALUES (2162029, 'John', 'Taylor', '929 Wilson Road Suite 649
Richardburgh, GU 64120', 214351, 63, FALSE, 'silver');
INSERT INTO passenger VALUES (9868867, 'Teresa', 'Taylor', '7707 Shannon Meadow Suite 706
Gardnerstad, PA 08776', 452385, 47, TRUE, 'basic');
INSERT INTO passenger VALUES (7169430, 'Rachel', 'Mcclain', '049 Reynolds Estates Suite 046
Port Haley, HI 38376', 227902, 52, TRUE, 'basic');
INSERT INTO passenger VALUES (9362387, 'Patrick', 'Perez', '82318 Shelly Port Suite 875
New John, MN 93899', 375963, 53, FALSE, 'basic');
INSERT INTO passenger VALUES (1729245, 'Tiffany', 'Whitehead', '8322 Natasha Mountain Suite 580
Kimberlymouth, AZ 48076', 139749, 76, TRUE, 'silver');
INSERT INTO passenger VALUES (9469611, 'Jacqueline', 'Santos', '613 Diana Junction Apt. 243
Lake Maryburgh, NY 62410', 375963, 49, TRUE, 'platinum');
INSERT INTO passenger VALUES (1994077, 'Steven', 'Collins', '280 Logan Crescent
Mckinneyborough, MA 80620', 124275, 24, TRUE, 'basic');
INSERT INTO passenger VALUES (4595619, 'Heather', 'King', 'USCGC Schmidt
FPO AP 11662', 139963, 31, TRUE, 'silver');
INSERT INTO passenger VALUES (5150169, 'Jaclyn', 'Barton', '504 Michael Causeway
Lake Cassandraville, DE 98747', 293381, 86, TRUE, 'silver');
INSERT INTO passenger VALUES (7899943, 'James', 'Nielsen', '1715 Martin Common Apt. 045
Brownville, IN 58075', 224699, 74, TRUE, 'basic');
INSERT INTO passenger VALUES (9003017, 'Kurt', 'Ray', '572 Reynolds Isle Suite 317
North Christophermouth, AK 69710', 124275, 55, FALSE, 'platinum');
INSERT INTO passenger VALUES (3487415, 'Brian', 'Golden', '90730 Garcia Keys
Lake Bryanton, NC 66077', 171239, 59, FALSE, 'gold');
INSERT INTO passenger VALUES (7791393, 'Stephanie', 'Mitchell', '34588 Michael Run
Arnoldhaven, WI 99470', 224699, 52, TRUE, 'platinum');
INSERT INTO passenger VALUES (2965959, 'Sheila', 'Myers', 'Unit 7591 Box 6134
DPO AA 70905', 113306, 75, FALSE, 'gold');
INSERT INTO passenger VALUES (8991792, 'Adam', 'Powers', '3983 Rush Valley Suite 428
Dennisbury, MP 27400', 460999, 79, TRUE, 'silver');
INSERT INTO passenger VALUES (6352080, 'Douglas', 'Hunt', '1592 Walker Road Suite 614
Staceyberg, MP 78406', 499654, 33, TRUE, 'platinum');
INSERT INTO passenger VALUES (4453764, 'Jennifer', 'Buchanan', 'Unit 8374 Box 5808
DPO AP 13440', 214351, 28, FALSE, 'platinum');
INSERT INTO passenger VALUES (2961215, 'Joseph', 'Dougherty', '2506 Atkinson Ridges
Hutchinsonbury, CO 05020', 171239, 15, TRUE, 'platinum');
INSERT INTO passenger VALUES (4491384, 'Gary', 'Morales', '1886 Robert Landing
East Tammy, AS 05965', 113306, 34, FALSE, 'gold');
INSERT INTO passenger VALUES (2868562, 'David', 'Grant', 'PSC 1447, Box 3002
APO AE 79623', 124275, 70, FALSE, 'basic');
INSERT INTO passenger VALUES (6245743, 'Samantha', 'Perkins', '16662 Cole Square
Richardtown, MI 46269', 139749, 64, FALSE, 'silver');
INSERT INTO passenger VALUES (9963495, 'Lisa', 'Melton', '649 Brown View Apt. 471
North Jeffrey, IL 84839', 460999, 38, FALSE, 'platinum');
INSERT INTO passenger VALUES (4530854, 'Melanie', 'Golden', 'USCGC Davis
FPO AA 26781', 460999, 12, TRUE, 'basic');
INSERT INTO passenger VALUES (2227634, 'Mark', 'Martin', '59282 Jesus Run
New Anthony, AS 10931', 214351, 89, TRUE, 'platinum');
INSERT INTO passenger VALUES (8027778, 'Alyssa', 'Johnson', '1762 Faith River
Kimberlytown, MT 24850', 139963, 76, TRUE, 'gold');
INSERT INTO passenger VALUES (1272742, 'Jennifer', 'Murphy', '83262 Ramirez Cliffs
Andersonmouth, MH 13653', 293381, 45, FALSE, 'basic');
INSERT INTO passenger VALUES (8224728, 'Samantha', 'Hunter', 'PSC 9895, Box 8454
APO AA 55731', 139749, 27, FALSE, 'silver');
INSERT INTO passenger VALUES (7873250, 'Benjamin', 'Thomas', '2039 Matthew Meadow
Watsonburgh, ID 71326', 293381, 21, TRUE, 'basic');
INSERT INTO passenger VALUES (3986188, 'Grace', 'Schmidt', '829 Brenda Manors
Melissashire, MO 80949', 460999, 13, TRUE, 'platinum');
INSERT INTO passenger VALUES (8834504, 'Jennifer', 'Garcia', '90038 Dalton Shoal
Contrerasfort, LA 49402', 139963, 71, TRUE, 'silver');
INSERT INTO passenger VALUES (3677816, 'Charles', 'Hodge', '6482 Jones View
Port Christopher, NC 82084', 171239, 56, TRUE, 'silver');
INSERT INTO passenger VALUES (7635536, 'Dillon', 'Coleman', '6872 Baldwin Turnpike Suite 337
Whitebury, UT 29612', 224699, 28, FALSE, 'platinum');
INSERT INTO passenger VALUES (1960211, 'Stephanie', 'Johnson', '3704 Jennifer Court Apt. 819
Sarabury, KY 15217', 452385, 90, FALSE, 'platinum');
INSERT INTO passenger VALUES (1931461, 'Jimmy', 'Leonard', '9315 Jody Trafficway
Smithbury, MA 48951', 293381, 73, FALSE, 'silver');
INSERT INTO passenger VALUES (7532793, 'Michael', 'Hernandez', '8435 Antonio Hollow Suite 020
Josephside, IN 93919', 224699, 27, FALSE, 'silver');
INSERT INTO passenger VALUES (6766644, 'Jasmine', 'Leon', '03955 Cain Burgs Apt. 340
Rileychester, WA 89836', 452385, 73, FALSE, 'gold');
INSERT INTO passenger VALUES (2427690, 'Sharon', 'Richardson', '44254 Cook Ports Suite 780
Jasonshire, GA 40453', 139749, 49, FALSE, 'platinum');
INSERT INTO passenger VALUES (9514557, 'Alyssa', 'Peterson', '23231 Adrienne Track Suite 965
Tinaborough, RI 99457', 227902, 41, FALSE, 'basic');
INSERT INTO passenger VALUES (6080953, 'David', 'Ford', '035 Alexander Station
North Julieberg, SD 15603', 293381, 70, FALSE, 'basic');
INSERT INTO passenger VALUES (4530532, 'Michael', 'Adams', 'Unit 3947 Box 3259
DPO AP 98111', 124275, 78, FALSE, 'gold');
INSERT INTO passenger VALUES (5213265, 'Todd', 'Lewis', '1723 Wolfe Radial Apt. 075
Spearsfort, AS 87681', 375963, 29, TRUE, 'basic');
INSERT INTO passenger VALUES (5745431, 'Martin', 'Adams', '64173 Martin Curve Suite 197
East Charlenefort, PW 39327', 375963, 52, FALSE, 'basic');
INSERT INTO passenger VALUES (7786633, 'Robert', 'Chapman', '83082 Caroline Lock Suite 487
Robertstad, OH 63549', 499654, 76, TRUE, 'basic');
INSERT INTO passenger VALUES (7031540, 'Thomas', 'Brown', '760 Scott Stream Suite 098
Stevenstad, TN 62706', 499654, 56, FALSE, 'silver');
INSERT INTO passenger VALUES (6180741, 'Patricia', 'Jones', '33087 Gates Radial Suite 504
Lake Kaitlyn, FL 79959', 460999, 12, TRUE, 'gold');
INSERT INTO passenger VALUES (1497262, 'Joseph', 'Bauer', '4270 Garcia Plains Apt. 056
Lake Ashley, VA 96158', 335610, 43, TRUE, 'silver');
INSERT INTO passenger VALUES (7217433, 'Brandon', 'Smith', 'Unit 2324 Box 8348
DPO AP 41584', 113306, 40, TRUE, 'basic');
INSERT INTO passenger VALUES (3610348, 'Ronald', 'Simon', '19247 Alexandra Manors
Gregorymouth, AS 69694', 293381, 32, TRUE, 'basic');
INSERT INTO passenger VALUES (9092559, 'Kim', 'Hopkins', '274 Gonzalez Expressway
Gibsonberg, NY 88220', 124275, 40, TRUE, 'basic');
INSERT INTO passenger VALUES (1196601, 'Laura', 'Solomon', '09185 King Isle Apt. 104
Jenkinsfort, PR 99042', 452385, 15, TRUE, 'silver');
INSERT INTO passenger VALUES (5328741, 'Jeanette', 'Becker', '75335 Anne Circle
New Kathleenshire, TX 66069', 113306, 34, TRUE, 'platinum');
INSERT INTO passenger VALUES (2696654, 'Stephanie', 'Peterson', '904 Sanchez Port Suite 302
Mossborough, WV 17051', 214351, 66, FALSE, 'platinum');
INSERT INTO passenger VALUES (2505634, 'Theresa', 'Chambers', '64061 Barber Court Apt. 397
South Patrick, WV 53472', 452385, 64, FALSE, 'silver');
INSERT INTO passenger VALUES (1339636, 'Thomas', 'Jimenez', '987 Alexander Spur Apt. 076
Valerieburgh, OH 99193', 224699, 88, FALSE, 'silver');
INSERT INTO passenger VALUES (1888680, 'Brittany', 'Fisher', '135 Oconnell Springs
South Kristopher, WY 14597', 224699, 79, FALSE, 'basic');
INSERT INTO passenger VALUES (2168435, 'Ryan', 'Parker', '095 Holland Square Apt. 715
East Jessica, MN 17333', 124275, 72, FALSE, 'basic');
INSERT INTO passenger VALUES (2777401, 'Mary', 'Hamilton', '61992 Sabrina Radial Suite 994
Mclaughlintown, MD 87232', 139963, 20, FALSE, 'basic');
INSERT INTO passenger VALUES (3959460, 'Laura', 'Kelly', '89949 Green Mission
Knoxshire, MD 63121', 124275, 65, FALSE, 'gold');
INSERT INTO passenger VALUES (5465007, 'Keith', 'Hoffman', '3911 Christopher Place
Kingville, CO 17136', 113306, 59, TRUE, 'silver');
INSERT INTO passenger VALUES (2986199, 'Jared', 'Whitney', '7049 Anderson Trail
South Robinport, MO 16034', 499654, 31, TRUE, 'silver');
INSERT INTO passenger VALUES (9381230, 'Brooke', 'Ford', 'PSC 5044, Box 6650
APO AP 10089', 293381, 83, TRUE, 'platinum');
INSERT INTO passenger VALUES (7515968, 'Benjamin', 'Nguyen', '244 Simpson River
Dwaynefurt, NV 95639', 171239, 21, TRUE, 'platinum');
INSERT INTO passenger VALUES (4219908, 'David', 'Alexander', '42849 Bethany Estate Suite 098
Smithmouth, VT 16561', 293381, 14, FALSE, 'basic');
INSERT INTO passenger VALUES (5131170, 'Michele', 'Coleman', '7894 Michael Overpass Suite 261
East Jenniferview, PR 07553', 139963, 41, TRUE, 'gold');
INSERT INTO passenger VALUES (9925283, 'Angela', 'Aguilar', '5104 Madison Summit
New Elizabeth, VI 87628', 214351, 52, TRUE, 'silver');
INSERT INTO passenger VALUES (1432017, 'Hannah', 'Ray', '517 Danny Point
Port Jenniferburgh, DE 95541', 171239, 77, TRUE, 'silver');
INSERT INTO passenger VALUES (5734972, 'William', 'Stephens', '0484 Lori Mall
Port Ronaldshire, WY 37844', 171239, 63, TRUE, 'platinum');
INSERT INTO passenger VALUES (6156293, 'Julie', 'Mcdonald', '217 Christopher Lane Suite 283
South Alicia, MD 07519', 214351, 33, TRUE, 'gold');
INSERT INTO passenger VALUES (9027045, 'William', 'Mcgrath', '732 Cynthia River
Port Toddfort, GU 03100', 375963, 36, FALSE, 'basic');
INSERT INTO passenger VALUES (6418072, 'Rebecca', 'Cobb', '7134 Renee Inlet
Erinmouth, FM 53347', 124275, 47, FALSE, 'basic');
INSERT INTO passenger VALUES (5116160, 'Susan', 'Jensen', '213 Schmidt Terrace Suite 744
Derekberg, NH 96229', 499654, 49, TRUE, 'gold');
INSERT INTO passenger VALUES (5931547, 'David', 'Ortega', '1005 Julie Freeway Suite 349
Rickymouth, AK 56337', 227902, 29, FALSE, 'basic');
INSERT INTO passenger VALUES (4662730, 'Todd', 'Patel', '32956 Ashley Locks Suite 427
New Mirandastad, MT 25004', 224699, 84, TRUE, 'platinum');
INSERT INTO passenger VALUES (9112089, 'Donna', 'Arnold', '599 Brooks Trafficway Apt. 763
Timothyshire, OH 59981', 214351, 62, FALSE, 'silver');
INSERT INTO passenger VALUES (6124507, 'Daniel', 'Harris', '6997 Elizabeth Valleys Suite 642
North Angelaland, MH 66186', 227902, 66, TRUE, 'silver');
INSERT INTO passenger VALUES (5721829, 'Francis', 'Nielsen', 'Unit 7286 Box 8918
DPO AE 46754', 460999, 84, FALSE, 'basic');
INSERT INTO passenger VALUES (8812278, 'Michelle', 'Richardson', 'PSC 8905, Box 8216
APO AP 89818', 214351, 24, FALSE, 'silver');
INSERT INTO passenger VALUES (3998210, 'Michael', 'Bailey', '54668 Wendy Corner
South Bradton, IL 68154', 293381, 75, FALSE, 'basic');
INSERT INTO passenger VALUES (9299869, 'Michael', 'Blevins', '619 Warren Hollow
South Kimberlyshire, KY 24917', 452385, 90, TRUE, 'gold');
INSERT INTO passenger VALUES (6127004, 'Mary', 'Morton', 'PSC 0133, Box 3847
APO AP 24566', 452385, 89, TRUE, 'gold');
INSERT INTO passenger VALUES (6820491, 'Denise', 'Martinez', '7236 David Ridge
Patriciaville, AK 19143', 113306, 68, TRUE, 'gold');
INSERT INTO passenger VALUES (4998276, 'Eric', 'Walker', '19365 Vargas Meadow Apt. 227
West Jennifershire, MA 80875', 375963, 39, FALSE, 'gold');
INSERT INTO passenger VALUES (8948057, 'Christopher', 'Jensen', '49656 Vincent Skyway
Wayneshire, PA 12487', 452385, 46, FALSE, 'silver');
INSERT INTO passenger VALUES (3354650, 'Cynthia', 'Flores', '3704 Jason Groves Apt. 313
West Coreyland, PR 57979', 113306, 56, FALSE, 'silver');
INSERT INTO passenger VALUES (8189656, 'Alyssa', 'Love', '509 Rodney Squares
Port Crystalshire, RI 47541', 227902, 81, TRUE, 'platinum');
INSERT INTO passenger VALUES (1529356, 'Aaron', 'Cruz', '79792 Cruz Spur Apt. 371
North Andrew, GU 58544', 214351, 17, FALSE, 'gold');
INSERT INTO passenger VALUES (1432212, 'Tristan', 'Summers', '4345 Johnson Lock
Brianton, NH 84657', 375963, 84, TRUE, 'platinum');
INSERT INTO passenger VALUES (4246383, 'Sheila', 'Cooke', '326 Buckley Estates
Jenniferbury, MD 01852', 293381, 55, TRUE, 'silver');
INSERT INTO passenger VALUES (8173911, 'Tricia', 'Tucker', '576 Hoffman Islands Apt. 311
Williamsfort, SD 02303', 113306, 88, TRUE, 'silver');
INSERT INTO passenger VALUES (1072758, 'Cathy', 'Matthews', '7603 Andrew Brook Apt. 626
Lake Sean, UT 62436', 452385, 41, TRUE, 'basic');
INSERT INTO passenger VALUES (6447803, 'Michael', 'George', '41334 Hoover Plaza
Cuevasside, TX 59079', 375963, 42, FALSE, 'platinum');
INSERT INTO passenger VALUES (1211134, 'Alyssa', 'Moran', '8964 Hall Lodge Apt. 589
Butlerstad, PW 17218', 452385, 90, FALSE, 'platinum');
INSERT INTO passenger VALUES (9526705, 'Mark', 'Lee', '8594 Charles Ramp Apt. 745
Tannerport, DC 92433', 499654, 34, TRUE, 'gold');
INSERT INTO passenger VALUES (7563300, 'Misty', 'Austin', '510 Walsh Forks
Gilberttown, MT 54633', 499654, 81, TRUE, 'gold');
INSERT INTO passenger VALUES (8155828, 'Karen', 'Williams', '42949 Michael Ports Suite 272
North Melanieborough, AR 85408', 227902, 72, TRUE, 'platinum');
INSERT INTO passenger VALUES (3797933, 'Mark', 'Benitez', '32319 Bonilla Courts Suite 853
Russellmouth, PA 80076', 171239, 33, TRUE, 'basic');
INSERT INTO passenger VALUES (9233734, 'Christopher', 'Frazier', 'USNS Kirby
FPO AE 27880', 375963, 45, FALSE, 'platinum');
INSERT INTO passenger VALUES (2776678, 'Leah', 'Lee', '54312 Jennifer Circles Apt. 525
East Michelle, NJ 66821', 460999, 62, FALSE, 'silver');
INSERT INTO passenger VALUES (2077334, 'Brandon', 'Marshall', '8456 Johnson Stravenue
South Bethanyview, SD 57239', 113306, 13, FALSE, 'platinum');
INSERT INTO passenger VALUES (1163615, 'Devin', 'Strickland', '41256 Eric Villages Apt. 673
Port Paul, CT 40013', 171239, 59, FALSE, 'basic');
INSERT INTO passenger VALUES (3891137, 'Carolyn', 'Monroe', '377 Martinez Locks Suite 352
Lake Erica, NM 12619', 452385, 26, FALSE, 'gold');
INSERT INTO passenger VALUES (2407452, 'James', 'Sullivan', '434 Abbott Pike
Kimberlyville, HI 39194', 124275, 77, FALSE, 'basic');
INSERT INTO passenger VALUES (1741094, 'Amanda', 'Hamilton', '489 Adam Hills
East Brian, WA 35999', 227902, 51, TRUE, 'platinum');
INSERT INTO passenger VALUES (1328187, 'Victoria', 'Strickland', '250 Sharon Mills Apt. 602
Port Cassandra, IL 76462', 499654, 23, TRUE, 'gold');
INSERT INTO passenger VALUES (5317875, 'Julie', 'Hughes', '009 Burke Port Apt. 528
Geneview, ND 44723', 452385, 66, TRUE, 'platinum');
INSERT INTO passenger VALUES (9728828, 'Brad', 'Johnson', '9042 Anthony Island Suite 178
Pooleville, CT 90232', 139749, 67, TRUE, 'basic');
INSERT INTO passenger VALUES (1500606, 'Larry', 'Davis', '91855 Stephanie Ford Apt. 069
East Alexander, HI 46999', 124275, 13, TRUE, 'silver');
INSERT INTO passenger VALUES (7786316, 'Miguel', 'Jensen', '17717 Bridget Extensions
Burkeside, TX 10841', 335610, 81, TRUE, 'basic');
INSERT INTO passenger VALUES (4772680, 'Matthew', 'Kelly', '49945 Amanda Circles
Caitlyntown, DE 93079', 460999, 60, FALSE, 'silver');
INSERT INTO passenger VALUES (3231298, 'Brian', 'Tucker', '5196 Bill Prairie
West Tammymouth, AS 02305', 227902, 30, TRUE, 'gold');
INSERT INTO passenger VALUES (5682977, 'George', 'Pearson', '11151 Courtney Cliff Apt. 672
Makaylaport, FL 22070', 452385, 69, FALSE, 'gold');
INSERT INTO passenger VALUES (2294181, 'Timothy', 'Braun', '6274 Evans Forks
West Jillview, NH 72736', 335610, 53, TRUE, 'platinum');
INSERT INTO passenger VALUES (4469259, 'Michael', 'Reyes', '32765 Michelle Prairie Apt. 019
Lake Jason, AR 12759', 227902, 78, FALSE, 'gold');
INSERT INTO passenger VALUES (9331874, 'Samantha', 'Harrison', '94144 Middleton Turnpike
Clarkland, KS 65174', 113306, 35, FALSE, 'platinum');
INSERT INTO passenger VALUES (1484905, 'Andrew', 'Martinez', '3190 Ryan Springs Suite 561
Brownville, MH 74426', 227902, 42, TRUE, 'platinum');
INSERT INTO passenger VALUES (7403311, 'Sheila', 'Sullivan', '0337 Robert Estate
Benjaminport, RI 43229', 293381, 67, TRUE, 'platinum');
INSERT INTO passenger VALUES (9719088, 'Hannah', 'Chambers', '479 Maria Tunnel Apt. 633
Lake Danny, MI 48934', 452385, 82, FALSE, 'platinum');
INSERT INTO passenger VALUES (8621561, 'Mark', 'Li', '50921 Wells Inlet
Fergusonland, VI 29456', 293381, 77, FALSE, 'silver');
INSERT INTO passenger VALUES (5010487, 'Lance', 'Morgan', '51398 Alexis Alley
South Brittanyhaven, SC 19211', 171239, 16, TRUE, 'gold');
INSERT INTO passenger VALUES (8423344, 'Amanda', 'Moore', '3198 Cheyenne Glen Apt. 232
Millsborough, NC 73633', 124275, 29, TRUE, 'silver');
INSERT INTO passenger VALUES (6236965, 'Kyle', 'Benton', '95585 Miller Estate Apt. 331
East Jadestad, SC 55147', 227902, 64, FALSE, 'gold');
INSERT INTO passenger VALUES (4892788, 'Katherine', 'Olsen', '911 Russell Lodge Apt. 240
Seanborough, MI 68817', 460999, 46, TRUE, 'platinum');
INSERT INTO passenger VALUES (6992003, 'Laurie', 'Acosta', '8759 Vaughan Junctions Suite 724
Lake Richard, ID 58338', 113306, 40, TRUE, 'silver');
INSERT INTO passenger VALUES (6027472, 'Sonya', 'Fitzpatrick', '13787 Gina Hills
New Michaelshire, MS 21980', 224699, 71, TRUE, 'basic');
INSERT INTO passenger VALUES (2481283, 'Janice', 'Miller', '3168 Taylor Mission Apt. 982
North Destinystad, MS 19057', 113306, 20, TRUE, 'gold');
INSERT INTO passenger VALUES (9312273, 'David', 'Jones', 'PSC 5754, Box 5427
APO AA 76957', 375963, 35, FALSE, 'basic');
INSERT INTO passenger VALUES (2934427, 'Nicholas', 'Pierce', '64463 Brittany Lights Suite 893
Brianmouth, FM 80327', 224699, 67, TRUE, 'platinum');
INSERT INTO passenger VALUES (5576927, 'Allen', 'Long', '46852 Cindy Loop Suite 821
Lake Lisa, AZ 31107', 214351, 86, FALSE, 'silver');
INSERT INTO passenger VALUES (2470259, 'Tyler', 'Harrington', '766 Mccoy Mount Suite 902
Lake Jennaside, IN 56953', 124275, 41, TRUE, 'platinum');
INSERT INTO passenger VALUES (1010438, 'William', 'Clarke', '04943 Brian Plains Suite 144
Lake Brian, NY 45466', 452385, 20, TRUE, 'platinum');
INSERT INTO passenger VALUES (5853975, 'Brittany', 'Taylor', '3285 Johnson Pike Apt. 858
New Samantha, WA 08607', 139749, 86, TRUE, 'platinum');
INSERT INTO passenger VALUES (2959304, 'Margaret', 'Miller', '4443 Dennis Mall
Daviston, AS 62901', 375963, 27, FALSE, 'platinum');
INSERT INTO passenger VALUES (9072354, 'Christopher', 'Hart', '0315 Richard Bridge Apt. 425
Andersonchester, MP 71784', 171239, 42, FALSE, 'platinum');
INSERT INTO passenger VALUES (1988776, 'Brooke', 'Owens', '8876 Katie Alley
Lake Erin, NC 89704', 335610, 87, TRUE, 'silver');
INSERT INTO passenger VALUES (7173601, 'Wanda', 'Taylor', '2134 Powers Mount Suite 873
West Alexanderberg, IA 31898', 124275, 60, FALSE, 'basic');
INSERT INTO passenger VALUES (9793501, 'Angela', 'Stewart', '49344 Maria Square Suite 840
North Gabrielborough, WI 38685', 460999, 86, FALSE, 'platinum');
INSERT INTO passenger VALUES (3454898, 'John', 'Torres', '4328 Jennifer Garden Apt. 057
Pricefurt, IA 92450', 224699, 46, TRUE, 'gold');
INSERT INTO passenger VALUES (1718275, 'Ashley', 'Norris', '11857 Key Crest
Raymondton, NH 24084', 227902, 53, FALSE, 'basic');
INSERT INTO passenger VALUES (3482381, 'Shelly', 'Dunlap', '0169 Bailey Corner
Port Robert, SD 51786', 335610, 14, TRUE, 'gold');
INSERT INTO passenger VALUES (9538849, 'William', 'Walker', '6871 Miranda Forge
East Doris, NY 50200', 227902, 34, TRUE, 'platinum');
INSERT INTO passenger VALUES (5380927, 'Joseph', 'Robles', '6400 Glenn Harbors
Isabelview, DC 02873', 171239, 70, FALSE, 'silver');
INSERT INTO passenger VALUES (8994337, 'Brian', 'Barajas', '458 Chen Locks
Jeremytown, NM 44886', 452385, 26, TRUE, 'platinum');
INSERT INTO passenger VALUES (6776228, 'Edward', 'Cooper', 'USS Smith
FPO AE 04173', 375963, 63, FALSE, 'platinum');
INSERT INTO passenger VALUES (5526936, 'Steven', 'Love', '6819 Miles Island
Lake Douglasburgh, FL 00907', 227902, 87, FALSE, 'basic');
INSERT INTO passenger VALUES (9848880, 'Johnathan', 'Melendez', 'PSC 5000, Box 4407
APO AE 31790', 227902, 65, TRUE, 'gold');
INSERT INTO passenger VALUES (2957004, 'Rachel', 'Pratt', '119 Shannon Trace
West James, AL 80400', 124275, 52, TRUE, 'gold');
INSERT INTO passenger VALUES (3107150, 'Andrea', 'Lamb', '0363 Combs Skyway
Lake Lance, MO 29421', 227902, 33, TRUE, 'basic');
INSERT INTO passenger VALUES (9226362, 'Ryan', 'Hicks', '76830 John Unions Apt. 743
Fernandezville, IL 81456', 224699, 37, FALSE, 'gold');
INSERT INTO passenger VALUES (9243803, 'Donna', 'Wise', '28811 Cooper Run Apt. 416
Maciaston, PR 84084', 452385, 60, TRUE, 'basic');
INSERT INTO passenger VALUES (3233971, 'Lisa', 'Jackson', '354 Julie Dam
East Tiffany, NM 75218', 224699, 74, TRUE, 'basic');
INSERT INTO passenger VALUES (1610270, 'Melissa', 'Rodriguez', '538 Smith Divide
Michaelchester, NM 46052', 113306, 47, TRUE, 'silver');
INSERT INTO passenger VALUES (1264848, 'Ruben', 'Cox', '6148 Butler Trail Suite 775
Pearsonshire, WA 94101', 113306, 15, FALSE, 'gold');
INSERT INTO passenger VALUES (4059576, 'Richard', 'Blake', '76374 Yolanda Grove Apt. 012
North Ashley, NM 48867', 293381, 60, TRUE, 'gold');
INSERT INTO passenger VALUES (4073362, 'Phillip', 'Smith', 'USS Hughes
FPO AA 77739', 113306, 19, TRUE, 'gold');
INSERT INTO passenger VALUES (6329350, 'John', 'Merritt', '17920 Rhonda Trail Suite 143
Port Michelle, ME 02206', 139749, 65, FALSE, 'silver');
INSERT INTO passenger VALUES (2723241, 'Michael', 'Miller', '6788 Richardson Junctions
Laurenmouth, RI 37980', 224699, 41, TRUE, 'gold');
INSERT INTO passenger VALUES (1116912, 'Kelly', 'Rodriguez', '29996 Amanda Extensions Apt. 878
Port Anthony, WY 69512', 124275, 84, FALSE, 'gold');
INSERT INTO passenger VALUES (7383012, 'Kristen', 'Campbell', '4657 Foley Inlet
Castilloborough, MS 10965', 335610, 19, FALSE, 'platinum');
INSERT INTO passenger VALUES (2153952, 'Brian', 'King', '29246 Rodriguez Extension Apt. 971
Pricefort, AR 37433', 214351, 77, FALSE, 'silver');
INSERT INTO passenger VALUES (7496042, 'Christine', 'Norris', '702 Jason Place Apt. 164
East Jakebury, VT 52234', 452385, 67, FALSE, 'gold');
INSERT INTO passenger VALUES (8308313, 'Sarah', 'Hooper', '11864 Ronald Underpass
Thomasland, HI 74013', 460999, 87, TRUE, 'silver');
INSERT INTO passenger VALUES (4797001, 'Lori', 'Hawkins', '120 Spence Place
South Savannahshire, ID 28284', 499654, 50, TRUE, 'platinum');
INSERT INTO passenger VALUES (9383250, 'John', 'Vaughan', '8544 Ricky Summit
South Claire, LA 37807', 171239, 85, TRUE, 'silver');
INSERT INTO passenger VALUES (2911738, 'Timothy', 'Anthony', '54078 Anderson Green Suite 635
South Kyleport, WI 84527', 375963, 87, TRUE, 'basic');
INSERT INTO passenger VALUES (1083645, 'Kyle', 'Dougherty', '0566 Davis Plain Apt. 304
Christophershire, NE 88061', 139749, 72, FALSE, 'basic');
INSERT INTO passenger VALUES (6373970, 'Michael', 'Mcgee', '9075 Stuart Skyway
Evansville, SD 45422', 335610, 77, FALSE, 'basic');
INSERT INTO passenger VALUES (2084890, 'Elizabeth', 'Herrera', '60144 Hamilton Mountain
East Michaelberg, HI 57383', 499654, 35, FALSE, 'basic');
INSERT INTO passenger VALUES (4123196, 'Steven', 'Skinner', '35628 Clark Junctions
East Patriciaside, GU 39401', 293381, 90, FALSE, 'platinum');
INSERT INTO passenger VALUES (3150842, 'Kerri', 'Pennington', '064 Williamson Ramp
North Catherine, GU 74521', 139749, 88, FALSE, 'gold');
INSERT INTO passenger VALUES (9218001, 'Eric', 'Wood', '0986 Dudley Lakes
North Wendystad, ID 98837', 460999, 75, FALSE, 'gold');
INSERT INTO passenger VALUES (3307906, 'Jacob', 'Reyes', '22596 Watts Union
New Andrew, PW 33072', 214351, 72, FALSE, 'gold');
INSERT INTO passenger VALUES (8515559, 'Monica', 'Charles', '807 Blake Rapid Apt. 828
Jeanettehaven, CT 61175', 293381, 58, TRUE, 'basic');

-- =========== skymill.flight_instances (generic) ==========

DROP TABLE IF EXISTS flight_instances;

CREATE TABLE flight_instances (
  id INT NOT NULL,
  aircraft_id INT NOT NULL,
  segment_id INT NOT NULL,
  departure_date DATE NOT NULL,
  arrival_date DATE NOT NULL
);

INSERT INTO flight_instances VALUES (5282783, 12189, 412625, '2026-03-06', '2026-03-31');
INSERT INTO flight_instances VALUES (2350897, 39465, 314713, '2026-03-11', '2026-01-01');
INSERT INTO flight_instances VALUES (5762438, 13762, 482891, '2026-04-18', '2026-03-13');
INSERT INTO flight_instances VALUES (9121478, 22964, 320444, '2026-01-29', '2026-01-22');
INSERT INTO flight_instances VALUES (5072970, 92725, 375803, '2026-01-08', '2026-03-10');
INSERT INTO flight_instances VALUES (4438274, 20473, 118785, '2026-04-16', '2026-02-22');
INSERT INTO flight_instances VALUES (7168799, 22621, 403209, '2026-03-01', '2026-03-27');
INSERT INTO flight_instances VALUES (4937369, 95174, 134925, '2026-02-19', '2026-03-05');
INSERT INTO flight_instances VALUES (1430034, 96428, 295869, '2026-04-08', '2026-03-15');
INSERT INTO flight_instances VALUES (5262304, 92725, 129782, '2026-02-03', '2026-05-08');
INSERT INTO flight_instances VALUES (5986471, 84275, 295869, '2026-05-04', '2026-01-03');
INSERT INTO flight_instances VALUES (9056370, 20473, 320444, '2026-01-09', '2026-01-29');
INSERT INTO flight_instances VALUES (9011552, 68564, 242384, '2026-04-26', '2026-01-21');
INSERT INTO flight_instances VALUES (6424106, 83612, 344198, '2026-02-16', '2026-02-21');
INSERT INTO flight_instances VALUES (3891092, 35979, 232414, '2026-04-24', '2026-01-18');
INSERT INTO flight_instances VALUES (1698493, 19647, 375803, '2026-01-23', '2026-05-01');
INSERT INTO flight_instances VALUES (3541207, 38025, 109929, '2026-04-13', '2026-02-23');
INSERT INTO flight_instances VALUES (8126220, 32040, 214804, '2026-01-30', '2026-04-17');
INSERT INTO flight_instances VALUES (2838934, 35212, 480025, '2026-02-21', '2026-03-29');
INSERT INTO flight_instances VALUES (2369537, 38025, 493797, '2026-03-18', '2026-01-09');
INSERT INTO flight_instances VALUES (9513965, 86411, 232414, '2026-03-15', '2026-03-17');
INSERT INTO flight_instances VALUES (2648981, 35979, 480025, '2026-01-22', '2026-03-12');
INSERT INTO flight_instances VALUES (9550745, 72204, 480025, '2026-02-15', '2026-01-12');
INSERT INTO flight_instances VALUES (2616380, 96428, 344198, '2026-05-05', '2026-01-22');
INSERT INTO flight_instances VALUES (4661322, 68564, 234001, '2026-05-09', '2026-04-04');
INSERT INTO flight_instances VALUES (4973827, 20473, 295869, '2026-03-15', '2026-02-24');
INSERT INTO flight_instances VALUES (1099849, 35212, 370748, '2026-05-11', '2026-02-05');
INSERT INTO flight_instances VALUES (2171509, 83459, 341944, '2026-05-09', '2026-02-19');
INSERT INTO flight_instances VALUES (6746460, 31389, 296815, '2026-04-20', '2026-01-30');
INSERT INTO flight_instances VALUES (3245038, 35212, 320821, '2026-03-23', '2026-02-09');
INSERT INTO flight_instances VALUES (3243026, 84275, 134925, '2026-04-07', '2026-01-26');
INSERT INTO flight_instances VALUES (7088982, 32040, 428534, '2026-03-01', '2026-04-29');
INSERT INTO flight_instances VALUES (8911236, 92725, 242384, '2026-03-14', '2026-01-24');
INSERT INTO flight_instances VALUES (7470412, 86411, 341448, '2026-03-14', '2026-01-26');
INSERT INTO flight_instances VALUES (8180075, 83459, 200726, '2026-02-10', '2026-01-24');
INSERT INTO flight_instances VALUES (1215045, 83612, 126306, '2026-04-29', '2026-01-11');
INSERT INTO flight_instances VALUES (4999475, 39910, 254202, '2026-02-20', '2026-04-14');
INSERT INTO flight_instances VALUES (6952130, 83612, 214055, '2026-04-14', '2026-03-15');
INSERT INTO flight_instances VALUES (1209499, 22621, 447782, '2026-01-02', '2026-05-06');
INSERT INTO flight_instances VALUES (1240763, 38025, 480025, '2026-04-16', '2026-02-22');
INSERT INTO flight_instances VALUES (6025683, 95174, 219413, '2026-02-15', '2026-01-01');
INSERT INTO flight_instances VALUES (4949024, 18537, 335131, '2026-04-16', '2026-01-15');
INSERT INTO flight_instances VALUES (2677916, 36885, 118785, '2026-02-23', '2026-05-03');
INSERT INTO flight_instances VALUES (7301468, 68564, 341448, '2026-05-03', '2026-03-19');
INSERT INTO flight_instances VALUES (7140829, 84275, 387649, '2026-04-22', '2026-01-06');
INSERT INTO flight_instances VALUES (3973859, 39910, 456389, '2026-04-20', '2026-03-22');
INSERT INTO flight_instances VALUES (2087960, 29934, 109929, '2026-01-09', '2026-02-12');
INSERT INTO flight_instances VALUES (8462608, 96428, 279211, '2026-01-02', '2026-01-07');
INSERT INTO flight_instances VALUES (7450816, 18537, 341448, '2026-04-12', '2026-01-17');
INSERT INTO flight_instances VALUES (5097551, 22964, 341448, '2026-01-11', '2026-01-13');
INSERT INTO flight_instances VALUES (8778991, 83459, 242384, '2026-02-02', '2026-02-19');
INSERT INTO flight_instances VALUES (5751130, 68564, 314713, '2026-03-15', '2026-01-17');
INSERT INTO flight_instances VALUES (8258066, 32040, 370748, '2026-03-17', '2026-03-31');
INSERT INTO flight_instances VALUES (8284228, 36885, 480025, '2026-02-09', '2026-05-09');
INSERT INTO flight_instances VALUES (4982256, 22964, 134925, '2026-01-06', '2026-02-01');
INSERT INTO flight_instances VALUES (6999905, 95174, 227258, '2026-04-14', '2026-02-12');
INSERT INTO flight_instances VALUES (1760850, 22621, 118785, '2026-03-05', '2026-02-18');
INSERT INTO flight_instances VALUES (6271427, 68564, 250560, '2026-03-05', '2026-03-03');
INSERT INTO flight_instances VALUES (8323300, 14279, 335131, '2026-04-27', '2026-03-06');
INSERT INTO flight_instances VALUES (6719936, 84800, 145650, '2026-03-27', '2026-03-21');
INSERT INTO flight_instances VALUES (9886698, 95174, 428534, '2026-01-14', '2026-01-31');
INSERT INTO flight_instances VALUES (5218698, 65168, 234001, '2026-02-11', '2026-02-28');
INSERT INTO flight_instances VALUES (3731525, 14279, 335131, '2026-03-18', '2026-04-11');
INSERT INTO flight_instances VALUES (6835745, 84275, 200726, '2026-02-13', '2026-04-20');
INSERT INTO flight_instances VALUES (7463895, 83612, 480025, '2026-03-06', '2026-03-19');
INSERT INTO flight_instances VALUES (9498851, 83459, 418971, '2026-04-07', '2026-02-09');
INSERT INTO flight_instances VALUES (8607388, 35212, 222532, '2026-03-05', '2026-01-27');
INSERT INTO flight_instances VALUES (5830883, 39465, 272473, '2026-01-28', '2026-02-09');
INSERT INTO flight_instances VALUES (2092479, 31389, 227258, '2026-04-13', '2026-03-10');
INSERT INTO flight_instances VALUES (6461218, 19647, 389547, '2026-02-13', '2026-03-19');
INSERT INTO flight_instances VALUES (2993925, 36885, 389547, '2026-05-06', '2026-03-30');
INSERT INTO flight_instances VALUES (2557122, 35979, 175607, '2026-03-04', '2026-02-09');
INSERT INTO flight_instances VALUES (8021762, 83612, 200726, '2026-03-03', '2026-02-08');
INSERT INTO flight_instances VALUES (6990414, 39910, 387649, '2026-03-15', '2026-02-11');
INSERT INTO flight_instances VALUES (3491018, 84800, 227258, '2026-03-03', '2026-04-23');
INSERT INTO flight_instances VALUES (9851125, 32040, 482891, '2026-01-21', '2026-05-05');
INSERT INTO flight_instances VALUES (9218215, 96428, 109929, '2026-02-21', '2026-01-26');
INSERT INTO flight_instances VALUES (2861626, 92725, 254202, '2026-02-01', '2026-04-03');
INSERT INTO flight_instances VALUES (4610651, 86411, 341944, '2026-01-08', '2026-04-11');
INSERT INTO flight_instances VALUES (1277099, 92725, 200726, '2026-01-02', '2026-01-16');
INSERT INTO flight_instances VALUES (6004554, 36885, 185905, '2026-03-26', '2026-01-21');
INSERT INTO flight_instances VALUES (7571718, 22964, 335131, '2026-04-11', '2026-05-04');
INSERT INTO flight_instances VALUES (7203850, 31389, 214804, '2026-02-18', '2026-03-26');
INSERT INTO flight_instances VALUES (4382841, 83612, 134925, '2026-01-09', '2026-03-08');
INSERT INTO flight_instances VALUES (9343423, 39465, 254202, '2026-03-16', '2026-02-11');
INSERT INTO flight_instances VALUES (1050646, 22964, 447782, '2026-03-21', '2026-05-06');
INSERT INTO flight_instances VALUES (1374988, 18537, 418971, '2026-03-07', '2026-01-09');
INSERT INTO flight_instances VALUES (8780015, 35212, 254202, '2026-03-30', '2026-05-10');
INSERT INTO flight_instances VALUES (5833403, 13762, 185905, '2026-01-09', '2026-01-09');
INSERT INTO flight_instances VALUES (9851577, 72204, 370748, '2026-03-10', '2026-01-10');
INSERT INTO flight_instances VALUES (2702241, 68564, 341944, '2026-02-03', '2026-02-16');
INSERT INTO flight_instances VALUES (5506687, 19647, 349926, '2026-04-07', '2026-01-06');
INSERT INTO flight_instances VALUES (3941019, 39465, 389547, '2026-01-24', '2026-03-04');
INSERT INTO flight_instances VALUES (1668849, 36885, 387649, '2026-03-19', '2026-03-04');
INSERT INTO flight_instances VALUES (6980627, 18537, 314713, '2026-03-10', '2026-01-02');
INSERT INTO flight_instances VALUES (9304895, 31389, 456389, '2026-02-22', '2026-04-14');
INSERT INTO flight_instances VALUES (8659338, 12189, 232414, '2026-04-28', '2026-02-19');
INSERT INTO flight_instances VALUES (8442580, 13762, 219413, '2026-01-06', '2026-02-25');
INSERT INTO flight_instances VALUES (8865172, 35212, 375803, '2026-01-13', '2026-05-01');
INSERT INTO flight_instances VALUES (5694564, 19647, 320444, '2026-03-15', '2026-02-17');
INSERT INTO flight_instances VALUES (5984049, 84800, 375803, '2026-01-14', '2026-02-26');
INSERT INTO flight_instances VALUES (5561925, 83459, 373160, '2026-01-11', '2026-01-15');
INSERT INTO flight_instances VALUES (7046633, 32040, 412625, '2026-01-24', '2026-04-27');
INSERT INTO flight_instances VALUES (8179773, 39910, 418971, '2026-03-01', '2026-01-29');
INSERT INTO flight_instances VALUES (2278577, 18537, 214055, '2026-04-29', '2026-02-09');
INSERT INTO flight_instances VALUES (4460972, 31389, 357045, '2026-02-27', '2026-04-25');
INSERT INTO flight_instances VALUES (9962111, 22964, 232414, '2026-02-01', '2026-02-26');
INSERT INTO flight_instances VALUES (8817486, 19647, 335131, '2026-04-18', '2026-02-06');
INSERT INTO flight_instances VALUES (8920482, 31389, 216072, '2026-03-11', '2026-01-13');
INSERT INTO flight_instances VALUES (9637487, 29934, 456389, '2026-03-06', '2026-03-22');
INSERT INTO flight_instances VALUES (5037070, 22964, 279211, '2026-03-31', '2026-02-23');
INSERT INTO flight_instances VALUES (3248916, 39910, 189927, '2026-02-23', '2026-04-29');
INSERT INTO flight_instances VALUES (3070161, 12189, 349353, '2026-05-12', '2026-01-31');
INSERT INTO flight_instances VALUES (6683188, 92725, 134925, '2026-04-04', '2026-02-11');
INSERT INTO flight_instances VALUES (9910038, 32040, 118785, '2026-05-02', '2026-04-24');
INSERT INTO flight_instances VALUES (5216329, 96428, 250560, '2026-02-08', '2026-02-26');
INSERT INTO flight_instances VALUES (2281058, 84800, 242384, '2026-01-27', '2026-04-04');
INSERT INTO flight_instances VALUES (7778473, 39465, 349926, '2026-01-12', '2026-05-05');
INSERT INTO flight_instances VALUES (3985619, 92725, 357045, '2026-04-07', '2026-03-12');
INSERT INTO flight_instances VALUES (2531988, 22621, 254202, '2026-04-20', '2026-01-06');
INSERT INTO flight_instances VALUES (7128137, 13762, 214055, '2026-04-24', '2026-01-01');
INSERT INTO flight_instances VALUES (2603592, 65168, 387649, '2026-03-06', '2026-02-04');
INSERT INTO flight_instances VALUES (9808915, 68564, 480025, '2026-03-05', '2026-03-25');
INSERT INTO flight_instances VALUES (9115191, 84800, 232414, '2026-02-16', '2026-02-07');
INSERT INTO flight_instances VALUES (9977745, 20473, 341944, '2026-01-24', '2026-05-03');
INSERT INTO flight_instances VALUES (7899624, 84275, 341448, '2026-02-13', '2026-02-28');
INSERT INTO flight_instances VALUES (2481637, 22621, 493797, '2026-03-17', '2026-03-27');
INSERT INTO flight_instances VALUES (9412051, 84275, 482891, '2026-02-04', '2026-02-09');
INSERT INTO flight_instances VALUES (1293870, 96428, 375803, '2026-02-26', '2026-03-26');
INSERT INTO flight_instances VALUES (3901972, 83459, 320821, '2026-03-24', '2026-03-23');
INSERT INTO flight_instances VALUES (3168880, 84800, 344198, '2026-04-06', '2026-01-31');
INSERT INTO flight_instances VALUES (6710958, 32040, 214055, '2026-01-21', '2026-04-07');
INSERT INTO flight_instances VALUES (8066489, 86411, 493797, '2026-03-23', '2026-05-08');
INSERT INTO flight_instances VALUES (3897785, 31389, 403209, '2026-05-03', '2026-03-24');
INSERT INTO flight_instances VALUES (1454134, 83459, 418971, '2026-04-23', '2026-01-20');
INSERT INTO flight_instances VALUES (6881673, 35979, 145650, '2026-02-28', '2026-03-24');
INSERT INTO flight_instances VALUES (5660582, 35979, 482891, '2026-04-26', '2026-04-04');
INSERT INTO flight_instances VALUES (2666559, 72204, 219413, '2026-03-21', '2026-05-05');
INSERT INTO flight_instances VALUES (3989140, 20473, 480025, '2026-05-07', '2026-03-17');
INSERT INTO flight_instances VALUES (2890701, 72204, 200726, '2026-04-10', '2026-01-20');
INSERT INTO flight_instances VALUES (2751370, 36885, 216072, '2026-02-20', '2026-03-25');
INSERT INTO flight_instances VALUES (3481773, 19647, 326795, '2026-01-16', '2026-04-26');
INSERT INTO flight_instances VALUES (1150752, 22964, 185905, '2026-04-02', '2026-02-02');
INSERT INTO flight_instances VALUES (7144851, 14279, 389547, '2026-05-04', '2026-03-13');
INSERT INTO flight_instances VALUES (1487850, 13762, 320444, '2026-01-21', '2026-01-11');
INSERT INTO flight_instances VALUES (9877907, 92725, 338854, '2026-02-10', '2026-03-05');
INSERT INTO flight_instances VALUES (1274638, 13762, 314713, '2026-01-08', '2026-04-02');
INSERT INTO flight_instances VALUES (7290695, 13762, 389547, '2026-04-11', '2026-03-30');
INSERT INTO flight_instances VALUES (6109460, 96428, 214055, '2026-04-10', '2026-03-19');
INSERT INTO flight_instances VALUES (7393057, 22964, 493797, '2026-05-05', '2026-04-07');
INSERT INTO flight_instances VALUES (7459023, 68564, 373160, '2026-04-10', '2026-04-22');
INSERT INTO flight_instances VALUES (2512031, 29934, 242384, '2026-05-08', '2026-04-29');
INSERT INTO flight_instances VALUES (9958280, 83459, 428534, '2026-04-04', '2026-04-24');
INSERT INTO flight_instances VALUES (1398220, 92725, 126306, '2026-01-07', '2026-04-30');
INSERT INTO flight_instances VALUES (3946276, 65168, 389547, '2026-03-22', '2026-01-18');
INSERT INTO flight_instances VALUES (2340419, 35212, 349353, '2026-03-20', '2026-03-15');
INSERT INTO flight_instances VALUES (3891059, 83459, 428534, '2026-01-27', '2026-01-14');
INSERT INTO flight_instances VALUES (2489999, 39465, 338854, '2026-02-07', '2026-04-02');
INSERT INTO flight_instances VALUES (9999750, 72204, 214804, '2026-04-30', '2026-05-06');
INSERT INTO flight_instances VALUES (6796520, 36885, 254202, '2026-04-11', '2026-04-25');
INSERT INTO flight_instances VALUES (9517022, 36885, 492183, '2026-03-28', '2026-03-31');
INSERT INTO flight_instances VALUES (8650087, 32040, 126306, '2026-02-27', '2026-03-02');
INSERT INTO flight_instances VALUES (5841139, 22964, 341448, '2026-03-03', '2026-01-10');
INSERT INTO flight_instances VALUES (7598471, 22621, 216072, '2026-03-05', '2026-05-11');
INSERT INTO flight_instances VALUES (7588961, 38025, 250560, '2026-02-20', '2026-02-09');
INSERT INTO flight_instances VALUES (4843875, 18537, 338854, '2026-02-22', '2026-03-10');
INSERT INTO flight_instances VALUES (6775944, 20473, 492183, '2026-02-25', '2026-01-19');
INSERT INTO flight_instances VALUES (8493745, 84275, 320821, '2026-04-18', '2026-05-07');
INSERT INTO flight_instances VALUES (2248186, 39910, 145650, '2026-03-10', '2026-02-05');
INSERT INTO flight_instances VALUES (1867373, 83612, 403209, '2026-01-08', '2026-01-29');
INSERT INTO flight_instances VALUES (1671714, 68564, 129782, '2026-04-23', '2026-02-11');
INSERT INTO flight_instances VALUES (9279815, 22621, 428534, '2026-03-03', '2026-02-25');
INSERT INTO flight_instances VALUES (1558964, 83459, 410727, '2026-02-27', '2026-02-05');
INSERT INTO flight_instances VALUES (1762159, 20473, 109929, '2026-02-21', '2026-03-12');
INSERT INTO flight_instances VALUES (4915229, 83459, 222532, '2026-01-19', '2026-01-31');
INSERT INTO flight_instances VALUES (9142476, 13762, 493797, '2026-04-28', '2026-03-31');
INSERT INTO flight_instances VALUES (9724633, 92725, 295869, '2026-02-23', '2026-02-03');
INSERT INTO flight_instances VALUES (9916922, 32040, 403209, '2026-01-15', '2026-04-10');
INSERT INTO flight_instances VALUES (2847867, 86411, 145650, '2026-02-07', '2026-04-30');
INSERT INTO flight_instances VALUES (2466127, 35979, 341944, '2026-04-23', '2026-03-24');
INSERT INTO flight_instances VALUES (2374148, 72204, 387649, '2026-02-04', '2026-03-06');
INSERT INTO flight_instances VALUES (1098468, 38025, 189927, '2026-03-29', '2026-01-13');
INSERT INTO flight_instances VALUES (2067996, 22964, 296815, '2026-03-10', '2026-03-11');
INSERT INTO flight_instances VALUES (7342356, 83612, 250560, '2026-01-30', '2026-01-05');
INSERT INTO flight_instances VALUES (4448063, 20473, 222532, '2026-01-18', '2026-04-28');
INSERT INTO flight_instances VALUES (1751004, 95174, 242384, '2026-02-25', '2026-04-21');
INSERT INTO flight_instances VALUES (8728948, 20473, 214804, '2026-05-03', '2026-01-31');
INSERT INTO flight_instances VALUES (1041571, 39465, 242384, '2026-01-07', '2026-02-16');
INSERT INTO flight_instances VALUES (1028636, 20473, 272473, '2026-01-23', '2026-01-23');
INSERT INTO flight_instances VALUES (9212933, 86411, 214055, '2026-01-28', '2026-04-25');
INSERT INTO flight_instances VALUES (6432174, 68564, 200726, '2026-03-23', '2026-02-20');
INSERT INTO flight_instances VALUES (4647476, 32040, 254202, '2026-01-12', '2026-04-26');
INSERT INTO flight_instances VALUES (1846299, 65168, 272473, '2026-03-21', '2026-03-09');
INSERT INTO flight_instances VALUES (2887013, 32040, 341448, '2026-03-21', '2026-02-04');
INSERT INTO flight_instances VALUES (1131474, 83459, 357045, '2026-02-24', '2026-02-25');
INSERT INTO flight_instances VALUES (4911891, 20473, 109929, '2026-03-31', '2026-02-06');
INSERT INTO flight_instances VALUES (7955477, 20473, 456389, '2026-01-02', '2026-01-27');
INSERT INTO flight_instances VALUES (4715270, 39910, 482891, '2026-02-03', '2026-03-20');
INSERT INTO flight_instances VALUES (1736878, 65168, 214055, '2026-03-12', '2026-04-24');
INSERT INTO flight_instances VALUES (4881008, 20473, 403209, '2026-02-10', '2026-01-24');
INSERT INTO flight_instances VALUES (4236485, 84800, 341944, '2026-05-06', '2026-01-27');
INSERT INTO flight_instances VALUES (2975242, 86411, 219413, '2026-03-22', '2026-04-27');
INSERT INTO flight_instances VALUES (8651121, 68564, 349353, '2026-03-31', '2026-01-11');
INSERT INTO flight_instances VALUES (3040213, 14279, 492183, '2026-02-05', '2026-03-25');
INSERT INTO flight_instances VALUES (9889589, 35212, 403209, '2026-03-06', '2026-04-26');
INSERT INTO flight_instances VALUES (9611592, 20473, 145650, '2026-02-20', '2026-03-05');
INSERT INTO flight_instances VALUES (3203412, 22621, 314713, '2026-05-01', '2026-05-03');
INSERT INTO flight_instances VALUES (4152237, 29934, 403209, '2026-03-17', '2026-03-17');
INSERT INTO flight_instances VALUES (1532836, 31389, 373160, '2026-04-09', '2026-02-23');
INSERT INTO flight_instances VALUES (5928475, 35979, 424311, '2026-05-05', '2026-01-27');
INSERT INTO flight_instances VALUES (7755856, 20473, 118785, '2026-01-27', '2026-05-05');
INSERT INTO flight_instances VALUES (5989126, 38025, 216072, '2026-02-02', '2026-03-16');
INSERT INTO flight_instances VALUES (9792793, 83612, 189927, '2026-04-12', '2026-04-21');
INSERT INTO flight_instances VALUES (1267699, 92725, 410727, '2026-01-30', '2026-04-04');
INSERT INTO flight_instances VALUES (9108899, 83612, 389547, '2026-01-26', '2026-04-02');
INSERT INTO flight_instances VALUES (6323776, 83612, 227751, '2026-01-06', '2026-03-14');
INSERT INTO flight_instances VALUES (7727576, 65168, 338854, '2026-04-21', '2026-04-09');
INSERT INTO flight_instances VALUES (9152758, 72204, 296815, '2026-01-23', '2026-01-31');
INSERT INTO flight_instances VALUES (4010212, 39910, 412625, '2026-04-15', '2026-05-09');
INSERT INTO flight_instances VALUES (7768408, 14279, 295869, '2026-04-19', '2026-03-12');
INSERT INTO flight_instances VALUES (7854170, 83612, 375803, '2026-04-12', '2026-01-07');
INSERT INTO flight_instances VALUES (5658325, 39465, 272473, '2026-01-27', '2026-05-11');
INSERT INTO flight_instances VALUES (8295690, 35212, 296815, '2026-01-19', '2026-04-08');
INSERT INTO flight_instances VALUES (1610544, 35979, 357045, '2026-04-26', '2026-02-03');
INSERT INTO flight_instances VALUES (5635936, 86411, 418971, '2026-03-25', '2026-03-17');
INSERT INTO flight_instances VALUES (1853749, 36885, 389547, '2026-04-27', '2026-01-23');
INSERT INTO flight_instances VALUES (1758953, 65168, 295869, '2026-04-22', '2026-03-11');
INSERT INTO flight_instances VALUES (6251982, 83612, 216072, '2026-02-10', '2026-03-17');
INSERT INTO flight_instances VALUES (9248389, 14279, 349926, '2026-04-09', '2026-05-05');
INSERT INTO flight_instances VALUES (8343974, 84800, 424311, '2026-01-09', '2026-03-16');
INSERT INTO flight_instances VALUES (8175644, 31389, 145650, '2026-04-28', '2026-03-24');
INSERT INTO flight_instances VALUES (7614747, 83459, 349353, '2026-02-07', '2026-04-17');
INSERT INTO flight_instances VALUES (6086941, 96428, 320821, '2026-03-19', '2026-04-24');
INSERT INTO flight_instances VALUES (8164721, 18537, 344198, '2026-01-02', '2026-05-03');
INSERT INTO flight_instances VALUES (9162740, 22964, 295869, '2026-03-28', '2026-03-15');
INSERT INTO flight_instances VALUES (4624699, 19647, 418971, '2026-01-20', '2026-03-23');
INSERT INTO flight_instances VALUES (4795341, 14279, 134925, '2026-01-27', '2026-05-05');
INSERT INTO flight_instances VALUES (3420721, 22964, 222532, '2026-04-07', '2026-01-14');
INSERT INTO flight_instances VALUES (5109931, 83459, 418971, '2026-03-13', '2026-05-11');
INSERT INTO flight_instances VALUES (6039117, 12189, 200726, '2026-04-22', '2026-05-01');
INSERT INTO flight_instances VALUES (9651758, 13762, 389547, '2026-02-22', '2026-02-19');
INSERT INTO flight_instances VALUES (7255476, 39465, 189927, '2026-01-02', '2026-02-02');
INSERT INTO flight_instances VALUES (6431334, 95174, 295869, '2026-02-26', '2026-04-21');
INSERT INTO flight_instances VALUES (2582302, 95174, 341944, '2026-01-26', '2026-03-10');
INSERT INTO flight_instances VALUES (3284313, 12189, 227258, '2026-01-21', '2026-04-01');
INSERT INTO flight_instances VALUES (4669373, 35212, 341448, '2026-04-09', '2026-01-21');
INSERT INTO flight_instances VALUES (4291858, 84800, 185905, '2026-05-10', '2026-01-20');
INSERT INTO flight_instances VALUES (5431012, 83612, 447782, '2026-02-13', '2026-04-09');
INSERT INTO flight_instances VALUES (2181681, 32040, 456389, '2026-05-11', '2026-01-30');
INSERT INTO flight_instances VALUES (2924296, 84800, 357045, '2026-03-20', '2026-04-25');
INSERT INTO flight_instances VALUES (6988118, 13762, 389547, '2026-05-04', '2026-01-02');
INSERT INTO flight_instances VALUES (5097537, 39465, 375803, '2026-03-28', '2026-01-05');
INSERT INTO flight_instances VALUES (5862298, 22621, 492183, '2026-02-20', '2026-02-12');
INSERT INTO flight_instances VALUES (3575576, 84800, 341448, '2026-04-05', '2026-03-04');
INSERT INTO flight_instances VALUES (8876058, 29934, 349926, '2026-04-10', '2026-01-06');
INSERT INTO flight_instances VALUES (8983277, 92725, 296815, '2026-01-25', '2026-03-26');
INSERT INTO flight_instances VALUES (9455454, 39910, 494138, '2026-03-22', '2026-03-09');
INSERT INTO flight_instances VALUES (9946112, 68564, 389547, '2026-05-04', '2026-01-15');
INSERT INTO flight_instances VALUES (2011623, 32040, 189927, '2026-03-21', '2026-05-01');
INSERT INTO flight_instances VALUES (2877495, 83459, 189927, '2026-01-08', '2026-05-10');
INSERT INTO flight_instances VALUES (1299188, 86411, 338854, '2026-01-17', '2026-03-06');
INSERT INTO flight_instances VALUES (8863508, 36885, 320444, '2026-04-30', '2026-02-18');
INSERT INTO flight_instances VALUES (8040248, 31389, 219413, '2026-01-07', '2026-02-20');
INSERT INTO flight_instances VALUES (8427554, 18537, 410727, '2026-01-06', '2026-03-03');
INSERT INTO flight_instances VALUES (6357849, 12189, 344198, '2026-02-05', '2026-03-11');
INSERT INTO flight_instances VALUES (3252086, 86411, 482891, '2026-01-16', '2026-02-26');
INSERT INTO flight_instances VALUES (7277451, 14279, 222532, '2026-03-29', '2026-02-09');
INSERT INTO flight_instances VALUES (8741700, 36885, 129782, '2026-01-09', '2026-03-19');
INSERT INTO flight_instances VALUES (3262281, 84275, 418971, '2026-03-29', '2026-03-07');
INSERT INTO flight_instances VALUES (1619013, 36885, 403209, '2026-02-24', '2026-04-01');
INSERT INTO flight_instances VALUES (6406136, 95174, 109929, '2026-02-28', '2026-04-21');
INSERT INTO flight_instances VALUES (8895170, 14279, 314713, '2026-01-22', '2026-04-28');
INSERT INTO flight_instances VALUES (6233896, 22964, 403209, '2026-04-06', '2026-04-03');
INSERT INTO flight_instances VALUES (2055142, 39465, 320444, '2026-05-08', '2026-03-15');
INSERT INTO flight_instances VALUES (3739269, 12189, 118785, '2026-04-17', '2026-05-03');
INSERT INTO flight_instances VALUES (8280938, 84275, 344198, '2026-03-23', '2026-01-27');
INSERT INTO flight_instances VALUES (9796068, 72204, 109929, '2026-02-20', '2026-03-10');
INSERT INTO flight_instances VALUES (1947070, 35979, 424311, '2026-04-30', '2026-04-16');
INSERT INTO flight_instances VALUES (6638170, 31389, 242384, '2026-05-08', '2026-03-08');
INSERT INTO flight_instances VALUES (7905616, 12189, 222532, '2026-02-15', '2026-03-18');
INSERT INTO flight_instances VALUES (4572194, 20473, 344198, '2026-04-29', '2026-05-03');
INSERT INTO flight_instances VALUES (8802551, 36885, 227258, '2026-01-01', '2026-03-27');
INSERT INTO flight_instances VALUES (4851977, 92725, 389547, '2026-03-11', '2026-05-01');
INSERT INTO flight_instances VALUES (1072702, 96428, 349353, '2026-02-01', '2026-01-30');
INSERT INTO flight_instances VALUES (1290249, 14279, 295869, '2026-01-11', '2026-04-06');
INSERT INTO flight_instances VALUES (2848351, 39910, 118785, '2026-04-29', '2026-05-06');
INSERT INTO flight_instances VALUES (4763209, 35212, 492183, '2026-03-02', '2026-01-23');
INSERT INTO flight_instances VALUES (1536986, 36885, 126306, '2026-02-16', '2026-03-11');
INSERT INTO flight_instances VALUES (9153537, 14279, 295869, '2026-03-26', '2026-01-04');
INSERT INTO flight_instances VALUES (1702070, 84275, 349353, '2026-05-09', '2026-02-15');
INSERT INTO flight_instances VALUES (1739346, 29934, 222532, '2026-02-05', '2026-01-13');
INSERT INTO flight_instances VALUES (7328294, 31389, 219413, '2026-04-23', '2026-02-26');
INSERT INTO flight_instances VALUES (4512184, 39465, 272473, '2026-05-03', '2026-03-03');
INSERT INTO flight_instances VALUES (1624553, 86411, 410727, '2026-04-12', '2026-05-11');
INSERT INTO flight_instances VALUES (2587249, 36885, 389547, '2026-04-13', '2026-04-10');
INSERT INTO flight_instances VALUES (1518627, 12189, 447782, '2026-01-05', '2026-05-09');
INSERT INTO flight_instances VALUES (4344487, 86411, 424311, '2026-04-01', '2026-02-12');
INSERT INTO flight_instances VALUES (3977294, 22964, 254202, '2026-04-16', '2026-01-22');
INSERT INTO flight_instances VALUES (1639958, 68564, 175607, '2026-04-06', '2026-01-19');
INSERT INTO flight_instances VALUES (5129903, 31389, 214804, '2026-03-04', '2026-05-08');
INSERT INTO flight_instances VALUES (7512215, 72204, 214804, '2026-03-02', '2026-02-05');
INSERT INTO flight_instances VALUES (3747976, 65168, 326795, '2026-01-04', '2026-02-18');
INSERT INTO flight_instances VALUES (5848496, 35212, 403209, '2026-01-31', '2026-02-14');
INSERT INTO flight_instances VALUES (5089360, 29934, 389547, '2026-04-22', '2026-01-06');
INSERT INTO flight_instances VALUES (4343069, 38025, 373160, '2026-02-01', '2026-04-11');
INSERT INTO flight_instances VALUES (5939148, 14279, 272473, '2026-04-02', '2026-05-07');
INSERT INTO flight_instances VALUES (1950026, 68564, 189927, '2026-01-31', '2026-05-09');
INSERT INTO flight_instances VALUES (4633014, 12189, 320821, '2026-03-15', '2026-05-04');
INSERT INTO flight_instances VALUES (1347646, 86411, 338854, '2026-01-01', '2026-04-20');
INSERT INTO flight_instances VALUES (7947286, 19647, 370748, '2026-03-16', '2026-04-11');
INSERT INTO flight_instances VALUES (8899441, 86411, 214804, '2026-03-03', '2026-03-15');
INSERT INTO flight_instances VALUES (4626499, 20473, 373160, '2026-04-12', '2026-04-29');
INSERT INTO flight_instances VALUES (4318596, 13762, 227258, '2026-03-25', '2026-03-31');
INSERT INTO flight_instances VALUES (9314453, 68564, 145650, '2026-01-27', '2026-01-24');
INSERT INTO flight_instances VALUES (8886242, 29934, 492183, '2026-03-25', '2026-03-16');
INSERT INTO flight_instances VALUES (3689846, 35212, 185905, '2026-04-13', '2026-01-02');
INSERT INTO flight_instances VALUES (2914075, 22964, 250560, '2026-01-21', '2026-01-20');
INSERT INTO flight_instances VALUES (5616328, 84275, 185905, '2026-01-11', '2026-03-16');
INSERT INTO flight_instances VALUES (1389731, 72204, 338854, '2026-04-25', '2026-03-16');
INSERT INTO flight_instances VALUES (5637917, 31389, 214804, '2026-03-28', '2026-03-15');
INSERT INTO flight_instances VALUES (2037973, 36885, 145650, '2026-02-10', '2026-02-13');
INSERT INTO flight_instances VALUES (1290086, 72204, 389547, '2026-04-27', '2026-04-05');
INSERT INTO flight_instances VALUES (7991826, 31389, 410727, '2026-01-22', '2026-01-04');
INSERT INTO flight_instances VALUES (6392794, 35212, 222532, '2026-02-17', '2026-01-15');
INSERT INTO flight_instances VALUES (6445311, 19647, 295869, '2026-05-10', '2026-04-19');
INSERT INTO flight_instances VALUES (7072581, 96428, 227258, '2026-03-10', '2026-02-05');
INSERT INTO flight_instances VALUES (5842415, 19647, 480025, '2026-02-08', '2026-02-04');
INSERT INTO flight_instances VALUES (9960227, 39465, 403209, '2026-02-11', '2026-03-05');
INSERT INTO flight_instances VALUES (2077631, 13762, 335131, '2026-04-08', '2026-01-13');
INSERT INTO flight_instances VALUES (6868520, 31389, 373160, '2026-03-31', '2026-02-14');
INSERT INTO flight_instances VALUES (1085869, 32040, 424311, '2026-02-02', '2026-04-18');
INSERT INTO flight_instances VALUES (5109260, 72204, 254202, '2026-03-02', '2026-03-21');
INSERT INTO flight_instances VALUES (3506783, 96428, 428534, '2026-01-02', '2026-04-12');
INSERT INTO flight_instances VALUES (3495148, 20473, 493797, '2026-05-08', '2026-01-07');
INSERT INTO flight_instances VALUES (3790582, 35212, 145650, '2026-03-18', '2026-05-06');
INSERT INTO flight_instances VALUES (8393896, 84800, 279211, '2026-02-15', '2026-03-29');
INSERT INTO flight_instances VALUES (6777939, 29934, 320444, '2026-04-05', '2026-05-05');
INSERT INTO flight_instances VALUES (2738928, 96428, 200726, '2026-03-12', '2026-01-18');
INSERT INTO flight_instances VALUES (7151159, 35979, 428534, '2026-03-03', '2026-04-02');
INSERT INTO flight_instances VALUES (3111997, 14279, 109929, '2026-01-28', '2026-04-28');
INSERT INTO flight_instances VALUES (4704226, 13762, 227258, '2026-04-07', '2026-04-18');
INSERT INTO flight_instances VALUES (8704711, 84275, 227751, '2026-01-19', '2026-03-28');
INSERT INTO flight_instances VALUES (2117864, 35212, 389547, '2026-02-14', '2026-02-10');
INSERT INTO flight_instances VALUES (4684820, 35979, 234001, '2026-03-11', '2026-03-15');
INSERT INTO flight_instances VALUES (2059919, 86411, 403209, '2026-03-05', '2026-04-07');
INSERT INTO flight_instances VALUES (7627273, 35979, 493797, '2026-01-06', '2026-03-13');
INSERT INTO flight_instances VALUES (7358717, 29934, 185905, '2026-04-19', '2026-04-18');
INSERT INTO flight_instances VALUES (6757414, 83459, 335131, '2026-04-15', '2026-02-16');
INSERT INTO flight_instances VALUES (5380722, 14279, 227258, '2026-04-22', '2026-03-03');
INSERT INTO flight_instances VALUES (8548716, 65168, 326795, '2026-03-19', '2026-03-21');
INSERT INTO flight_instances VALUES (1497319, 95174, 109929, '2026-03-02', '2026-05-03');
INSERT INTO flight_instances VALUES (1832357, 31389, 412625, '2026-01-25', '2026-01-05');
INSERT INTO flight_instances VALUES (2531963, 36885, 447782, '2026-04-29', '2026-01-01');
INSERT INTO flight_instances VALUES (5301340, 22964, 227751, '2026-01-04', '2026-03-22');
INSERT INTO flight_instances VALUES (9501449, 86411, 219413, '2026-01-03', '2026-03-21');
INSERT INTO flight_instances VALUES (9947215, 13762, 493797, '2026-02-17', '2026-03-30');
INSERT INTO flight_instances VALUES (6861883, 65168, 234001, '2026-04-13', '2026-02-26');
INSERT INTO flight_instances VALUES (6802003, 35979, 480025, '2026-05-04', '2026-04-27');
INSERT INTO flight_instances VALUES (6093066, 19647, 338854, '2026-04-19', '2026-01-11');
INSERT INTO flight_instances VALUES (3900173, 39465, 242384, '2026-02-17', '2026-03-15');
INSERT INTO flight_instances VALUES (5784607, 96428, 227258, '2026-03-23', '2026-04-01');
INSERT INTO flight_instances VALUES (1607199, 95174, 216072, '2026-04-03', '2026-05-02');
INSERT INTO flight_instances VALUES (4193470, 95174, 250560, '2026-05-12', '2026-03-25');
INSERT INTO flight_instances VALUES (2077940, 38025, 341944, '2026-04-22', '2026-04-18');
INSERT INTO flight_instances VALUES (1965797, 14279, 118785, '2026-01-26', '2026-04-01');
INSERT INTO flight_instances VALUES (8689138, 92725, 320821, '2026-03-28', '2026-03-16');
INSERT INTO flight_instances VALUES (7613367, 14279, 320821, '2026-02-01', '2026-04-23');
INSERT INTO flight_instances VALUES (4128390, 72204, 403209, '2026-03-21', '2026-02-08');
INSERT INTO flight_instances VALUES (3928961, 86411, 424311, '2026-01-03', '2026-04-09');
INSERT INTO flight_instances VALUES (1890704, 84800, 410727, '2026-05-10', '2026-04-21');
INSERT INTO flight_instances VALUES (5826789, 84275, 189927, '2026-03-14', '2026-05-03');
INSERT INTO flight_instances VALUES (9482853, 68564, 412625, '2026-01-07', '2026-05-05');
INSERT INTO flight_instances VALUES (8185814, 12189, 344198, '2026-01-03', '2026-04-09');
INSERT INTO flight_instances VALUES (3699725, 35212, 145650, '2026-03-19', '2026-04-06');
INSERT INTO flight_instances VALUES (2378137, 12189, 214804, '2026-03-16', '2026-02-14');
INSERT INTO flight_instances VALUES (5645246, 65168, 412625, '2026-01-18', '2026-04-03');
INSERT INTO flight_instances VALUES (1546513, 29934, 326795, '2026-01-10', '2026-01-19');
INSERT INTO flight_instances VALUES (7482197, 29934, 480025, '2026-05-11', '2026-03-23');
INSERT INTO flight_instances VALUES (5098506, 86411, 227751, '2026-04-06', '2026-04-13');
INSERT INTO flight_instances VALUES (8822314, 13762, 189927, '2026-03-11', '2026-04-10');
INSERT INTO flight_instances VALUES (2201507, 18537, 482891, '2026-03-04', '2026-02-21');
INSERT INTO flight_instances VALUES (5818296, 18537, 129782, '2026-03-16', '2026-01-13');
INSERT INTO flight_instances VALUES (8308378, 13762, 344198, '2026-01-16', '2026-02-10');
INSERT INTO flight_instances VALUES (3830674, 84275, 126306, '2026-05-09', '2026-01-28');
INSERT INTO flight_instances VALUES (2188866, 35212, 232414, '2026-05-07', '2026-05-06');
INSERT INTO flight_instances VALUES (4900203, 22964, 320821, '2026-05-10', '2026-03-01');
INSERT INTO flight_instances VALUES (8599491, 83612, 214055, '2026-02-23', '2026-04-30');
INSERT INTO flight_instances VALUES (3349832, 72204, 214804, '2026-01-18', '2026-03-15');
INSERT INTO flight_instances VALUES (9462768, 86411, 341448, '2026-01-08', '2026-04-18');
INSERT INTO flight_instances VALUES (2430594, 39465, 412625, '2026-04-21', '2026-02-17');
INSERT INTO flight_instances VALUES (3958017, 36885, 216072, '2026-02-13', '2026-04-10');
INSERT INTO flight_instances VALUES (5273219, 12189, 254202, '2026-03-20', '2026-02-18');
INSERT INTO flight_instances VALUES (3967989, 14279, 279211, '2026-04-29', '2026-02-15');
INSERT INTO flight_instances VALUES (7611726, 83612, 129782, '2026-04-28', '2026-05-04');
INSERT INTO flight_instances VALUES (2549316, 19647, 250560, '2026-05-10', '2026-01-31');
INSERT INTO flight_instances VALUES (2750865, 13762, 492183, '2026-02-18', '2026-05-05');
INSERT INTO flight_instances VALUES (3172946, 83459, 492183, '2026-01-17', '2026-01-29');
INSERT INTO flight_instances VALUES (8841346, 38025, 480025, '2026-01-21', '2026-03-09');
INSERT INTO flight_instances VALUES (8670473, 13762, 482891, '2026-04-16', '2026-01-10');
INSERT INTO flight_instances VALUES (3416955, 38025, 403209, '2026-05-10', '2026-03-30');
INSERT INTO flight_instances VALUES (1862683, 19647, 126306, '2026-01-05', '2026-04-09');
INSERT INTO flight_instances VALUES (8226595, 86411, 234001, '2026-02-10', '2026-02-07');
INSERT INTO flight_instances VALUES (7938588, 36885, 126306, '2026-05-04', '2026-02-20');
INSERT INTO flight_instances VALUES (3216377, 36885, 375803, '2026-02-12', '2026-04-05');
INSERT INTO flight_instances VALUES (2050537, 84800, 219413, '2026-05-02', '2026-01-13');
INSERT INTO flight_instances VALUES (4378471, 14279, 480025, '2026-04-25', '2026-02-09');
INSERT INTO flight_instances VALUES (2949063, 68564, 403209, '2026-02-05', '2026-04-14');
INSERT INTO flight_instances VALUES (5656525, 83459, 341448, '2026-05-10', '2026-02-21');
INSERT INTO flight_instances VALUES (9509070, 39465, 250560, '2026-03-15', '2026-04-09');
INSERT INTO flight_instances VALUES (6407111, 65168, 272473, '2026-02-25', '2026-01-31');
INSERT INTO flight_instances VALUES (1957125, 84800, 349926, '2026-01-02', '2026-03-12');
INSERT INTO flight_instances VALUES (9298883, 84275, 250560, '2026-04-11', '2026-03-12');
INSERT INTO flight_instances VALUES (3511197, 20473, 185905, '2026-02-26', '2026-04-11');
INSERT INTO flight_instances VALUES (3234139, 32040, 200726, '2026-04-29', '2026-04-17');
INSERT INTO flight_instances VALUES (8969609, 35979, 296815, '2026-01-12', '2026-03-20');
INSERT INTO flight_instances VALUES (4229683, 38025, 494138, '2026-01-26', '2026-04-20');
INSERT INTO flight_instances VALUES (6096264, 95174, 175607, '2026-03-23', '2026-01-22');
INSERT INTO flight_instances VALUES (4144513, 22621, 219413, '2026-05-03', '2026-01-11');
INSERT INTO flight_instances VALUES (5975382, 20473, 357045, '2026-03-28', '2026-02-18');
INSERT INTO flight_instances VALUES (3686836, 39910, 272473, '2026-02-19', '2026-03-21');

-- =========== skymill.cargo_flights (generic) ==========

DROP TABLE IF EXISTS cargo_flights;

CREATE TABLE cargo_flights (
  id INT NOT NULL,
  aircraft_id INT NOT NULL,
  segment_id INT NOT NULL,
  departure_date DATE NOT NULL,
  arrival_date DATE NOT NULL
);

INSERT INTO cargo_flights VALUES (733236, 12189, 493797, '2026-04-14', '2026-02-19');
INSERT INTO cargo_flights VALUES (115640, 14279, 189927, '2026-03-24', '2026-05-09');
INSERT INTO cargo_flights VALUES (645334, 65168, 129782, '2026-01-25', '2026-03-01');
INSERT INTO cargo_flights VALUES (615277, 13762, 145650, '2026-04-15', '2026-04-15');
INSERT INTO cargo_flights VALUES (353497, 39910, 493797, '2026-01-26', '2026-02-25');
INSERT INTO cargo_flights VALUES (715589, 32040, 234001, '2026-01-03', '2026-02-22');
INSERT INTO cargo_flights VALUES (758072, 20473, 480025, '2026-04-17', '2026-01-24');
INSERT INTO cargo_flights VALUES (555106, 84800, 482891, '2026-02-16', '2026-02-04');
INSERT INTO cargo_flights VALUES (391814, 38025, 349353, '2026-05-05', '2026-03-25');
INSERT INTO cargo_flights VALUES (558558, 35979, 387649, '2026-05-10', '2026-01-07');
INSERT INTO cargo_flights VALUES (455699, 29934, 279211, '2026-04-29', '2026-04-08');
INSERT INTO cargo_flights VALUES (271117, 35979, 200726, '2026-04-02', '2026-05-11');
INSERT INTO cargo_flights VALUES (755059, 95174, 109929, '2026-01-03', '2026-01-01');
INSERT INTO cargo_flights VALUES (447164, 22964, 338854, '2026-03-16', '2026-04-15');
INSERT INTO cargo_flights VALUES (757508, 19647, 134925, '2026-02-18', '2026-01-24');
INSERT INTO cargo_flights VALUES (223703, 39910, 200726, '2026-03-29', '2026-02-14');
INSERT INTO cargo_flights VALUES (667591, 68564, 447782, '2026-04-19', '2026-02-06');
INSERT INTO cargo_flights VALUES (511695, 96428, 227751, '2026-04-10', '2026-04-23');
INSERT INTO cargo_flights VALUES (687019, 92725, 222532, '2026-03-14', '2026-03-03');
INSERT INTO cargo_flights VALUES (286148, 68564, 214055, '2026-02-10', '2026-01-08');
INSERT INTO cargo_flights VALUES (181455, 39910, 320444, '2026-05-12', '2026-03-30');
INSERT INTO cargo_flights VALUES (360250, 39910, 214804, '2026-04-26', '2026-04-13');
INSERT INTO cargo_flights VALUES (910188, 32040, 242384, '2026-03-16', '2026-02-03');
INSERT INTO cargo_flights VALUES (818559, 38025, 279211, '2026-03-20', '2026-03-30');
INSERT INTO cargo_flights VALUES (505917, 29934, 428534, '2026-02-18', '2026-04-12');
INSERT INTO cargo_flights VALUES (735217, 39910, 412625, '2026-01-12', '2026-02-06');
INSERT INTO cargo_flights VALUES (572228, 84800, 272473, '2026-01-12', '2026-02-21');
INSERT INTO cargo_flights VALUES (811796, 65168, 227258, '2026-02-09', '2026-04-18');
INSERT INTO cargo_flights VALUES (677816, 84275, 227751, '2026-03-07', '2026-05-05');
INSERT INTO cargo_flights VALUES (978373, 39910, 341448, '2026-03-20', '2026-01-19');
INSERT INTO cargo_flights VALUES (969502, 72204, 216072, '2026-01-29', '2026-03-27');
INSERT INTO cargo_flights VALUES (253595, 22621, 219413, '2026-02-05', '2026-04-23');
INSERT INTO cargo_flights VALUES (389460, 95174, 227258, '2026-04-22', '2026-02-17');
INSERT INTO cargo_flights VALUES (777504, 18537, 272473, '2026-05-03', '2026-05-06');
INSERT INTO cargo_flights VALUES (707099, 19647, 341448, '2026-05-09', '2026-04-05');
INSERT INTO cargo_flights VALUES (519747, 84275, 357045, '2026-03-27', '2026-05-07');
INSERT INTO cargo_flights VALUES (446354, 31389, 320444, '2026-01-21', '2026-04-18');
INSERT INTO cargo_flights VALUES (601323, 29934, 314713, '2026-03-20', '2026-03-05');
INSERT INTO cargo_flights VALUES (782915, 83612, 118785, '2026-04-25', '2026-04-19');
INSERT INTO cargo_flights VALUES (105533, 20473, 428534, '2026-03-06', '2026-01-21');
INSERT INTO cargo_flights VALUES (948750, 22621, 492183, '2026-03-02', '2026-03-29');
INSERT INTO cargo_flights VALUES (250822, 13762, 219413, '2026-04-27', '2026-01-11');
INSERT INTO cargo_flights VALUES (154036, 84275, 338854, '2026-04-14', '2026-05-10');
INSERT INTO cargo_flights VALUES (900209, 12189, 129782, '2026-05-09', '2026-02-12');
INSERT INTO cargo_flights VALUES (995244, 35212, 357045, '2026-04-30', '2026-03-17');
INSERT INTO cargo_flights VALUES (420661, 65168, 412625, '2026-02-27', '2026-03-28');
INSERT INTO cargo_flights VALUES (198178, 12189, 428534, '2026-01-03', '2026-01-14');
INSERT INTO cargo_flights VALUES (107463, 13762, 272473, '2026-05-01', '2026-01-15');
INSERT INTO cargo_flights VALUES (743993, 39465, 320821, '2026-02-12', '2026-02-28');
INSERT INTO cargo_flights VALUES (896822, 22621, 234001, '2026-01-05', '2026-04-02');
INSERT INTO cargo_flights VALUES (251048, 32040, 349353, '2026-02-26', '2026-01-19');
INSERT INTO cargo_flights VALUES (134231, 96428, 418971, '2026-04-10', '2026-05-02');
INSERT INTO cargo_flights VALUES (556487, 22964, 314713, '2026-05-03', '2026-03-25');
INSERT INTO cargo_flights VALUES (230833, 65168, 232414, '2026-03-01', '2026-01-29');
INSERT INTO cargo_flights VALUES (440172, 18537, 370748, '2026-04-14', '2026-01-11');
INSERT INTO cargo_flights VALUES (796715, 65168, 232414, '2026-04-29', '2026-04-24');
INSERT INTO cargo_flights VALUES (642804, 29934, 341448, '2026-03-09', '2026-03-31');
INSERT INTO cargo_flights VALUES (243675, 35212, 403209, '2026-01-14', '2026-04-17');
INSERT INTO cargo_flights VALUES (513506, 13762, 234001, '2026-01-19', '2026-02-27');
INSERT INTO cargo_flights VALUES (855102, 13762, 403209, '2026-01-21', '2026-02-28');
INSERT INTO cargo_flights VALUES (761485, 72204, 219413, '2026-01-02', '2026-02-25');
INSERT INTO cargo_flights VALUES (499701, 22964, 482891, '2026-01-10', '2026-04-28');
INSERT INTO cargo_flights VALUES (815933, 39910, 296815, '2026-02-26', '2026-04-12');
INSERT INTO cargo_flights VALUES (252642, 32040, 279211, '2026-05-02', '2026-04-14');
INSERT INTO cargo_flights VALUES (965863, 20473, 493797, '2026-02-25', '2026-05-04');
INSERT INTO cargo_flights VALUES (607042, 38025, 335131, '2026-04-14', '2026-03-19');
INSERT INTO cargo_flights VALUES (932235, 84275, 341944, '2026-03-02', '2026-04-18');
INSERT INTO cargo_flights VALUES (445027, 32040, 410727, '2026-03-28', '2026-03-09');
INSERT INTO cargo_flights VALUES (647082, 95174, 357045, '2026-03-11', '2026-04-10');
INSERT INTO cargo_flights VALUES (563327, 35212, 370748, '2026-04-03', '2026-01-12');
INSERT INTO cargo_flights VALUES (491415, 12189, 109929, '2026-05-12', '2026-01-26');
INSERT INTO cargo_flights VALUES (259977, 35212, 480025, '2026-04-29', '2026-01-07');
INSERT INTO cargo_flights VALUES (714581, 29934, 344198, '2026-05-01', '2026-02-23');
INSERT INTO cargo_flights VALUES (649000, 36885, 418971, '2026-01-24', '2026-02-25');
INSERT INTO cargo_flights VALUES (678301, 39910, 335131, '2026-03-01', '2026-05-11');
INSERT INTO cargo_flights VALUES (268775, 65168, 185905, '2026-01-12', '2026-03-21');
INSERT INTO cargo_flights VALUES (208914, 18537, 242384, '2026-02-18', '2026-02-04');
INSERT INTO cargo_flights VALUES (776981, 83459, 424311, '2026-03-12', '2026-04-09');
INSERT INTO cargo_flights VALUES (595048, 32040, 145650, '2026-02-09', '2026-04-20');
INSERT INTO cargo_flights VALUES (631650, 13762, 326795, '2026-01-21', '2026-04-23');
INSERT INTO cargo_flights VALUES (333386, 22621, 320444, '2026-04-08', '2026-04-26');
INSERT INTO cargo_flights VALUES (214523, 39465, 214804, '2026-03-24', '2026-02-18');
INSERT INTO cargo_flights VALUES (301037, 39465, 234001, '2026-02-25', '2026-02-08');
INSERT INTO cargo_flights VALUES (114429, 95174, 227258, '2026-03-04', '2026-05-07');
INSERT INTO cargo_flights VALUES (380476, 22964, 389547, '2026-04-05', '2026-04-11');
INSERT INTO cargo_flights VALUES (128482, 32040, 480025, '2026-01-21', '2026-04-18');
INSERT INTO cargo_flights VALUES (724842, 38025, 424311, '2026-05-08', '2026-01-04');
INSERT INTO cargo_flights VALUES (488373, 35979, 412625, '2026-03-25', '2026-04-22');
INSERT INTO cargo_flights VALUES (961801, 83612, 200726, '2026-04-06', '2026-04-13');
INSERT INTO cargo_flights VALUES (758628, 38025, 134925, '2026-04-10', '2026-04-11');
INSERT INTO cargo_flights VALUES (245326, 65168, 493797, '2026-05-11', '2026-01-13');
INSERT INTO cargo_flights VALUES (632283, 39465, 320821, '2026-02-03', '2026-04-30');
INSERT INTO cargo_flights VALUES (614175, 83612, 375803, '2026-03-19', '2026-01-30');
INSERT INTO cargo_flights VALUES (125992, 29934, 219413, '2026-01-20', '2026-05-05');
INSERT INTO cargo_flights VALUES (678322, 72204, 482891, '2026-01-29', '2026-03-27');
INSERT INTO cargo_flights VALUES (410976, 38025, 185905, '2026-04-11', '2026-01-03');
INSERT INTO cargo_flights VALUES (775508, 22964, 272473, '2026-01-14', '2026-03-02');
INSERT INTO cargo_flights VALUES (188334, 14279, 145650, '2026-02-02', '2026-04-02');
INSERT INTO cargo_flights VALUES (732193, 96428, 410727, '2026-02-17', '2026-02-26');
INSERT INTO cargo_flights VALUES (806286, 92725, 494138, '2026-04-15', '2026-03-17');
INSERT INTO cargo_flights VALUES (614681, 68564, 341448, '2026-03-09', '2026-04-23');
INSERT INTO cargo_flights VALUES (845815, 29934, 387649, '2026-02-28', '2026-01-27');
INSERT INTO cargo_flights VALUES (800049, 92725, 216072, '2026-03-12', '2026-01-24');
INSERT INTO cargo_flights VALUES (272236, 14279, 272473, '2026-04-11', '2026-03-09');
INSERT INTO cargo_flights VALUES (463436, 96428, 219413, '2026-01-12', '2026-04-24');
INSERT INTO cargo_flights VALUES (397201, 39465, 349353, '2026-01-11', '2026-01-24');
INSERT INTO cargo_flights VALUES (634792, 83459, 320821, '2026-03-07', '2026-03-22');
INSERT INTO cargo_flights VALUES (344503, 84275, 373160, '2026-02-12', '2026-01-13');
INSERT INTO cargo_flights VALUES (346445, 84800, 370748, '2026-04-05', '2026-03-23');
INSERT INTO cargo_flights VALUES (388326, 35212, 242384, '2026-04-27', '2026-02-13');
INSERT INTO cargo_flights VALUES (581397, 22964, 216072, '2026-02-15', '2026-04-10');
INSERT INTO cargo_flights VALUES (240695, 14279, 373160, '2026-03-10', '2026-02-14');
INSERT INTO cargo_flights VALUES (878046, 13762, 296815, '2026-03-14', '2026-01-12');
INSERT INTO cargo_flights VALUES (117052, 72204, 200726, '2026-02-10', '2026-02-10');
INSERT INTO cargo_flights VALUES (787243, 39910, 272473, '2026-01-14', '2026-01-21');
INSERT INTO cargo_flights VALUES (333621, 14279, 126306, '2026-04-27', '2026-01-13');
INSERT INTO cargo_flights VALUES (280979, 35212, 447782, '2026-04-23', '2026-01-08');
INSERT INTO cargo_flights VALUES (296521, 29934, 349353, '2026-01-12', '2026-02-08');
INSERT INTO cargo_flights VALUES (712175, 39910, 389547, '2026-05-11', '2026-04-08');
INSERT INTO cargo_flights VALUES (460185, 31389, 338854, '2026-02-12', '2026-04-01');
INSERT INTO cargo_flights VALUES (700177, 29934, 410727, '2026-02-17', '2026-01-03');
INSERT INTO cargo_flights VALUES (632084, 86411, 373160, '2026-03-04', '2026-01-05');
INSERT INTO cargo_flights VALUES (191136, 84275, 326795, '2026-01-15', '2026-03-09');
INSERT INTO cargo_flights VALUES (422404, 72204, 403209, '2026-01-17', '2026-05-02');
INSERT INTO cargo_flights VALUES (983159, 38025, 412625, '2026-03-11', '2026-02-07');
INSERT INTO cargo_flights VALUES (776366, 29934, 216072, '2026-05-06', '2026-04-27');
INSERT INTO cargo_flights VALUES (660589, 39910, 254202, '2026-03-27', '2026-03-11');
INSERT INTO cargo_flights VALUES (170058, 35212, 222532, '2026-01-19', '2026-01-14');
INSERT INTO cargo_flights VALUES (249856, 18537, 349926, '2026-02-26', '2026-05-01');
INSERT INTO cargo_flights VALUES (170897, 32040, 295869, '2026-04-04', '2026-02-20');
INSERT INTO cargo_flights VALUES (932118, 35979, 296815, '2026-03-14', '2026-01-22');
INSERT INTO cargo_flights VALUES (863835, 83459, 126306, '2026-02-20', '2026-01-17');
INSERT INTO cargo_flights VALUES (133800, 20473, 189927, '2026-01-28', '2026-01-10');
INSERT INTO cargo_flights VALUES (677633, 32040, 129782, '2026-04-06', '2026-01-20');
INSERT INTO cargo_flights VALUES (970881, 22964, 227258, '2026-04-05', '2026-01-20');
INSERT INTO cargo_flights VALUES (988636, 95174, 214055, '2026-02-25', '2026-03-01');
INSERT INTO cargo_flights VALUES (691232, 96428, 118785, '2026-05-07', '2026-03-05');
INSERT INTO cargo_flights VALUES (212978, 95174, 387649, '2026-03-27', '2026-04-25');
INSERT INTO cargo_flights VALUES (226404, 83612, 234001, '2026-05-03', '2026-03-20');
INSERT INTO cargo_flights VALUES (932171, 35979, 234001, '2026-03-08', '2026-03-30');
INSERT INTO cargo_flights VALUES (322704, 83612, 232414, '2026-02-28', '2026-01-19');
INSERT INTO cargo_flights VALUES (767347, 18537, 373160, '2026-03-10', '2026-04-19');
INSERT INTO cargo_flights VALUES (267291, 18537, 357045, '2026-01-04', '2026-03-15');
INSERT INTO cargo_flights VALUES (372577, 68564, 482891, '2026-03-03', '2026-04-11');
INSERT INTO cargo_flights VALUES (286320, 92725, 480025, '2026-04-17', '2026-03-24');
INSERT INTO cargo_flights VALUES (912883, 32040, 412625, '2026-05-09', '2026-01-19');
INSERT INTO cargo_flights VALUES (725323, 95174, 296815, '2026-01-29', '2026-03-01');
INSERT INTO cargo_flights VALUES (801924, 39465, 234001, '2026-02-24', '2026-04-07');
INSERT INTO cargo_flights VALUES (830748, 72204, 250560, '2026-04-10', '2026-04-19');
INSERT INTO cargo_flights VALUES (474492, 68564, 222532, '2026-02-03', '2026-04-17');
INSERT INTO cargo_flights VALUES (352768, 19647, 480025, '2026-04-09', '2026-02-03');
INSERT INTO cargo_flights VALUES (265050, 29934, 375803, '2026-03-30', '2026-03-19');
INSERT INTO cargo_flights VALUES (221189, 35212, 410727, '2026-03-30', '2026-05-10');
INSERT INTO cargo_flights VALUES (873886, 72204, 219413, '2026-02-15', '2026-04-18');
INSERT INTO cargo_flights VALUES (481738, 38025, 200726, '2026-02-03', '2026-01-14');
INSERT INTO cargo_flights VALUES (499982, 36885, 403209, '2026-02-25', '2026-03-26');
INSERT INTO cargo_flights VALUES (167992, 39465, 185905, '2026-04-02', '2026-04-24');
INSERT INTO cargo_flights VALUES (549043, 86411, 129782, '2026-01-28', '2026-02-27');
INSERT INTO cargo_flights VALUES (507134, 84275, 314713, '2026-03-08', '2026-01-09');
INSERT INTO cargo_flights VALUES (415908, 29934, 145650, '2026-02-15', '2026-01-03');
INSERT INTO cargo_flights VALUES (626357, 68564, 447782, '2026-04-27', '2026-02-03');
INSERT INTO cargo_flights VALUES (675376, 12189, 373160, '2026-03-06', '2026-02-06');
INSERT INTO cargo_flights VALUES (611911, 22964, 189927, '2026-02-24', '2026-02-11');
INSERT INTO cargo_flights VALUES (358509, 18537, 349353, '2026-04-13', '2026-04-10');
INSERT INTO cargo_flights VALUES (624235, 39465, 109929, '2026-03-27', '2026-01-25');
INSERT INTO cargo_flights VALUES (792511, 83459, 227258, '2026-04-12', '2026-04-10');
INSERT INTO cargo_flights VALUES (531537, 39910, 418971, '2026-02-11', '2026-02-09');
INSERT INTO cargo_flights VALUES (521650, 84275, 335131, '2026-03-13', '2026-02-12');
INSERT INTO cargo_flights VALUES (572361, 86411, 341448, '2026-01-17', '2026-02-16');
INSERT INTO cargo_flights VALUES (340760, 39910, 357045, '2026-01-22', '2026-02-09');
INSERT INTO cargo_flights VALUES (438748, 29934, 370748, '2026-04-07', '2026-03-12');
INSERT INTO cargo_flights VALUES (875193, 39465, 344198, '2026-01-08', '2026-04-14');
INSERT INTO cargo_flights VALUES (505854, 35979, 279211, '2026-01-24', '2026-05-05');
INSERT INTO cargo_flights VALUES (952614, 32040, 272473, '2026-01-10', '2026-05-08');
INSERT INTO cargo_flights VALUES (997802, 12189, 232414, '2026-03-09', '2026-02-07');
INSERT INTO cargo_flights VALUES (301962, 83612, 387649, '2026-02-13', '2026-04-15');
INSERT INTO cargo_flights VALUES (304639, 83612, 482891, '2026-02-24', '2026-01-02');
INSERT INTO cargo_flights VALUES (544464, 22964, 341944, '2026-01-27', '2026-04-29');
INSERT INTO cargo_flights VALUES (834600, 96428, 424311, '2026-05-06', '2026-03-30');
INSERT INTO cargo_flights VALUES (970601, 39465, 492183, '2026-04-10', '2026-01-25');
INSERT INTO cargo_flights VALUES (237199, 32040, 389547, '2026-03-09', '2026-05-06');
INSERT INTO cargo_flights VALUES (574095, 84275, 232414, '2026-05-11', '2026-03-19');
INSERT INTO cargo_flights VALUES (859274, 18537, 296815, '2026-03-24', '2026-01-18');
INSERT INTO cargo_flights VALUES (305108, 20473, 219413, '2026-04-04', '2026-03-11');
INSERT INTO cargo_flights VALUES (598078, 86411, 389547, '2026-03-12', '2026-01-26');
INSERT INTO cargo_flights VALUES (436083, 38025, 492183, '2026-03-27', '2026-04-24');
INSERT INTO cargo_flights VALUES (996823, 20473, 494138, '2026-03-06', '2026-04-17');
INSERT INTO cargo_flights VALUES (990233, 83459, 214055, '2026-05-07', '2026-03-14');
INSERT INTO cargo_flights VALUES (912153, 36885, 118785, '2026-02-23', '2026-01-23');
INSERT INTO cargo_flights VALUES (675298, 95174, 200726, '2026-01-14', '2026-04-01');
INSERT INTO cargo_flights VALUES (648680, 35212, 242384, '2026-04-04', '2026-03-02');
INSERT INTO cargo_flights VALUES (120546, 39910, 373160, '2026-01-22', '2026-04-12');
INSERT INTO cargo_flights VALUES (692445, 39465, 344198, '2026-05-03', '2026-01-24');
INSERT INTO cargo_flights VALUES (817972, 65168, 134925, '2026-01-02', '2026-01-05');
INSERT INTO cargo_flights VALUES (633887, 83459, 447782, '2026-04-23', '2026-01-06');
INSERT INTO cargo_flights VALUES (203567, 38025, 320444, '2026-01-08', '2026-03-18');
INSERT INTO cargo_flights VALUES (422244, 39910, 456389, '2026-03-07', '2026-01-11');
INSERT INTO cargo_flights VALUES (793872, 83459, 375803, '2026-02-18', '2026-04-02');
INSERT INTO cargo_flights VALUES (798926, 29934, 373160, '2026-04-19', '2026-03-11');
INSERT INTO cargo_flights VALUES (708028, 96428, 175607, '2026-01-20', '2026-05-09');
INSERT INTO cargo_flights VALUES (555899, 84275, 250560, '2026-03-22', '2026-03-01');
INSERT INTO cargo_flights VALUES (373731, 18537, 118785, '2026-02-14', '2026-02-08');
INSERT INTO cargo_flights VALUES (655713, 96428, 418971, '2026-03-02', '2026-03-20');
INSERT INTO cargo_flights VALUES (750780, 19647, 341944, '2026-03-21', '2026-04-11');
INSERT INTO cargo_flights VALUES (533295, 65168, 279211, '2026-03-02', '2026-03-11');
INSERT INTO cargo_flights VALUES (881607, 83612, 134925, '2026-01-20', '2026-05-05');
INSERT INTO cargo_flights VALUES (648848, 12189, 216072, '2026-04-12', '2026-02-24');
INSERT INTO cargo_flights VALUES (962177, 38025, 189927, '2026-01-13', '2026-01-09');
INSERT INTO cargo_flights VALUES (176495, 39465, 389547, '2026-01-01', '2026-04-03');
INSERT INTO cargo_flights VALUES (947553, 36885, 222532, '2026-04-30', '2026-03-24');
INSERT INTO cargo_flights VALUES (636347, 31389, 349926, '2026-05-01', '2026-02-18');
INSERT INTO cargo_flights VALUES (416286, 36885, 295869, '2026-03-01', '2026-01-18');
INSERT INTO cargo_flights VALUES (960137, 84275, 403209, '2026-03-03', '2026-03-14');
INSERT INTO cargo_flights VALUES (967575, 65168, 214055, '2026-02-15', '2026-04-19');
INSERT INTO cargo_flights VALUES (469053, 65168, 227751, '2026-04-16', '2026-03-27');
INSERT INTO cargo_flights VALUES (205227, 19647, 335131, '2026-04-01', '2026-04-04');
INSERT INTO cargo_flights VALUES (952278, 22964, 189927, '2026-01-24', '2026-04-03');
INSERT INTO cargo_flights VALUES (742858, 19647, 279211, '2026-02-09', '2026-05-09');
INSERT INTO cargo_flights VALUES (910151, 84800, 341944, '2026-04-22', '2026-05-12');
INSERT INTO cargo_flights VALUES (914016, 22964, 109929, '2026-03-11', '2026-02-07');
INSERT INTO cargo_flights VALUES (905715, 92725, 242384, '2026-03-26', '2026-01-31');
INSERT INTO cargo_flights VALUES (224194, 36885, 242384, '2026-02-12', '2026-01-15');
INSERT INTO cargo_flights VALUES (735633, 96428, 216072, '2026-01-08', '2026-01-16');
INSERT INTO cargo_flights VALUES (251268, 96428, 227258, '2026-02-10', '2026-02-04');
INSERT INTO cargo_flights VALUES (468598, 29934, 134925, '2026-02-05', '2026-04-03');
INSERT INTO cargo_flights VALUES (703367, 83459, 227258, '2026-02-18', '2026-01-22');
INSERT INTO cargo_flights VALUES (567241, 31389, 424311, '2026-04-13', '2026-04-21');
INSERT INTO cargo_flights VALUES (381311, 12189, 344198, '2026-02-10', '2026-04-12');
INSERT INTO cargo_flights VALUES (643092, 83612, 373160, '2026-04-01', '2026-03-11');
INSERT INTO cargo_flights VALUES (572301, 14279, 494138, '2026-03-20', '2026-01-08');
INSERT INTO cargo_flights VALUES (646600, 83459, 389547, '2026-02-17', '2026-02-22');
INSERT INTO cargo_flights VALUES (162259, 36885, 493797, '2026-02-23', '2026-03-05');
INSERT INTO cargo_flights VALUES (599483, 68564, 480025, '2026-04-30', '2026-02-27');
INSERT INTO cargo_flights VALUES (298068, 96428, 126306, '2026-04-08', '2026-04-18');
INSERT INTO cargo_flights VALUES (543693, 22964, 482891, '2026-02-22', '2026-01-01');
INSERT INTO cargo_flights VALUES (200324, 36885, 250560, '2026-03-11', '2026-02-27');
INSERT INTO cargo_flights VALUES (639436, 13762, 357045, '2026-04-10', '2026-02-21');
INSERT INTO cargo_flights VALUES (383901, 65168, 126306, '2026-02-08', '2026-04-19');
INSERT INTO cargo_flights VALUES (413138, 20473, 373160, '2026-02-04', '2026-04-27');
INSERT INTO cargo_flights VALUES (520335, 13762, 129782, '2026-03-19', '2026-01-03');
INSERT INTO cargo_flights VALUES (996048, 86411, 200726, '2026-04-12', '2026-01-15');
INSERT INTO cargo_flights VALUES (271976, 83459, 145650, '2026-01-17', '2026-03-26');
INSERT INTO cargo_flights VALUES (277004, 22964, 118785, '2026-02-19', '2026-03-31');
INSERT INTO cargo_flights VALUES (998327, 22621, 403209, '2026-01-15', '2026-05-01');
INSERT INTO cargo_flights VALUES (469804, 19647, 493797, '2026-02-12', '2026-04-22');
INSERT INTO cargo_flights VALUES (986540, 39910, 109929, '2026-03-01', '2026-02-20');
INSERT INTO cargo_flights VALUES (303778, 19647, 344198, '2026-04-28', '2026-01-31');
INSERT INTO cargo_flights VALUES (837435, 22964, 418971, '2026-01-23', '2026-02-10');
INSERT INTO cargo_flights VALUES (574737, 83612, 250560, '2026-04-17', '2026-03-07');
INSERT INTO cargo_flights VALUES (162305, 29934, 227258, '2026-02-16', '2026-04-28');
INSERT INTO cargo_flights VALUES (719236, 35212, 227258, '2026-04-26', '2026-04-19');
INSERT INTO cargo_flights VALUES (516350, 32040, 222532, '2026-03-17', '2026-02-19');
INSERT INTO cargo_flights VALUES (606002, 39465, 118785, '2026-02-17', '2026-01-11');
INSERT INTO cargo_flights VALUES (511881, 19647, 335131, '2026-03-01', '2026-02-20');
INSERT INTO cargo_flights VALUES (206705, 18537, 494138, '2026-04-14', '2026-04-13');
INSERT INTO cargo_flights VALUES (285339, 83459, 145650, '2026-04-02', '2026-03-18');
INSERT INTO cargo_flights VALUES (596094, 36885, 480025, '2026-05-10', '2026-03-28');
INSERT INTO cargo_flights VALUES (564266, 72204, 145650, '2026-05-05', '2026-02-02');
INSERT INTO cargo_flights VALUES (290811, 68564, 341448, '2026-03-27', '2026-02-23');
INSERT INTO cargo_flights VALUES (745173, 22621, 145650, '2026-02-10', '2026-01-18');
INSERT INTO cargo_flights VALUES (374226, 84800, 428534, '2026-03-30', '2026-01-17');
INSERT INTO cargo_flights VALUES (606830, 35979, 403209, '2026-03-04', '2026-04-13');
INSERT INTO cargo_flights VALUES (452680, 22621, 242384, '2026-03-28', '2026-04-16');
INSERT INTO cargo_flights VALUES (127089, 35979, 295869, '2026-05-05', '2026-04-08');
INSERT INTO cargo_flights VALUES (332039, 86411, 389547, '2026-01-17', '2026-03-10');
INSERT INTO cargo_flights VALUES (155883, 22964, 272473, '2026-03-01', '2026-03-30');
INSERT INTO cargo_flights VALUES (859272, 12189, 145650, '2026-01-14', '2026-04-26');
INSERT INTO cargo_flights VALUES (884599, 39465, 295869, '2026-02-09', '2026-01-31');
INSERT INTO cargo_flights VALUES (142736, 29934, 129782, '2026-03-01', '2026-04-04');
INSERT INTO cargo_flights VALUES (252987, 12189, 234001, '2026-01-31', '2026-02-13');
INSERT INTO cargo_flights VALUES (867984, 19647, 338854, '2026-03-24', '2026-01-31');
INSERT INTO cargo_flights VALUES (783453, 35212, 296815, '2026-03-09', '2026-02-03');
INSERT INTO cargo_flights VALUES (608026, 31389, 295869, '2026-03-26', '2026-02-01');
INSERT INTO cargo_flights VALUES (423320, 18537, 227751, '2026-02-01', '2026-04-04');
INSERT INTO cargo_flights VALUES (519222, 39465, 314713, '2026-03-11', '2026-02-21');
INSERT INTO cargo_flights VALUES (982169, 13762, 326795, '2026-02-01', '2026-01-21');
INSERT INTO cargo_flights VALUES (703920, 35979, 314713, '2026-01-25', '2026-04-16');
INSERT INTO cargo_flights VALUES (866524, 68564, 335131, '2026-02-23', '2026-03-08');
INSERT INTO cargo_flights VALUES (815951, 19647, 118785, '2026-02-26', '2026-02-22');
INSERT INTO cargo_flights VALUES (459081, 12189, 109929, '2026-01-04', '2026-04-10');

-- =========== skymill.bookings (generic) ==========

DROP TABLE IF EXISTS bookings;

CREATE TABLE bookings (
  id INT NOT NULL,
  passenger_id INT NOT NULL,
  flight_instance_id INT NOT NULL,
  seat_number VARCHAR(255) NOT NULL
);

INSERT INTO bookings VALUES (1975159, 4530854, 5928475, 'EB6');
INSERT INTO bookings VALUES (8419860, 9226362, 9501449, 'BQ4');
INSERT INTO bookings VALUES (8175377, 9112089, 5380722, 'KA1');
INSERT INTO bookings VALUES (7761831, 8991792, 5637917, 'EO6');
INSERT INTO bookings VALUES (8246948, 6080953, 8599491, 'LF6');
INSERT INTO bookings VALUES (9750719, 6992003, 5109931, 'WE5');
INSERT INTO bookings VALUES (1993933, 4332111, 3541207, 'VM6');
INSERT INTO bookings VALUES (7241642, 2407452, 8021762, 'FJ4');
INSERT INTO bookings VALUES (4244339, 9469611, 7627273, 'XZ3');
INSERT INTO bookings VALUES (2376505, 3959460, 9889589, 'GN2');
INSERT INTO bookings VALUES (6646663, 4219908, 6093066, 'FR2');
INSERT INTO bookings VALUES (5482591, 4491384, 9314453, 'VK3');
INSERT INTO bookings VALUES (1136428, 8155828, 3216377, 'EZ9');
INSERT INTO bookings VALUES (1718010, 4100639, 3973859, 'DR6');
INSERT INTO bookings VALUES (3056337, 5116160, 9889589, 'DM2');
INSERT INTO bookings VALUES (9682589, 1272742, 1098468, 'CE2');
INSERT INTO bookings VALUES (9706022, 8991792, 9248389, 'EV2');
INSERT INTO bookings VALUES (9111468, 9027045, 8780015, 'BJ2');
INSERT INTO bookings VALUES (6774020, 5931547, 3216377, 'JF3');
INSERT INTO bookings VALUES (1973322, 2723241, 9498851, 'AA1');
INSERT INTO bookings VALUES (8115927, 7217433, 6431334, 'UJ0');
INSERT INTO bookings VALUES (5492724, 9514557, 3941019, 'LC4');
INSERT INTO bookings VALUES (1089024, 4073362, 5842415, 'FQ3');
INSERT INTO bookings VALUES (3983543, 2911738, 9509070, 'AL7');
INSERT INTO bookings VALUES (9050693, 5682977, 6025683, 'LM5');
INSERT INTO bookings VALUES (3423997, 2481283, 8179773, 'FK9');
INSERT INTO bookings VALUES (7758429, 6111066, 7571718, 'VE0');
INSERT INTO bookings VALUES (4517235, 6329350, 4128390, 'UF1');
INSERT INTO bookings VALUES (7667739, 2986199, 2077940, 'KS1');
INSERT INTO bookings VALUES (9156560, 9514557, 8295690, 'HE8');
INSERT INTO bookings VALUES (9590988, 3150842, 2350897, 'XF3');
INSERT INTO bookings VALUES (2672726, 1888680, 6025683, 'QJ2');
INSERT INTO bookings VALUES (6336261, 9362387, 2378137, 'ZO8');
INSERT INTO bookings VALUES (2698721, 5745431, 5109260, 'SH5');
INSERT INTO bookings VALUES (4669281, 8621561, 1624553, 'XD9');
INSERT INTO bookings VALUES (8950869, 9027045, 8179773, 'AT0');
INSERT INTO bookings VALUES (2094084, 5131170, 3541207, 'KZ1');
INSERT INTO bookings VALUES (6810619, 5131170, 7168799, 'OL5');
INSERT INTO bookings VALUES (6565357, 8224728, 3790582, 'MK6');
INSERT INTO bookings VALUES (1515728, 3454898, 1950026, 'IX2');
INSERT INTO bookings VALUES (5984309, 1010438, 8876058, 'NM6');
INSERT INTO bookings VALUES (4312610, 7635536, 1518627, 'FO9');
INSERT INTO bookings VALUES (8795512, 2505634, 4900203, 'ID3');
INSERT INTO bookings VALUES (7498419, 7537489, 8607388, 'YE1');
INSERT INTO bookings VALUES (4344113, 9243803, 5072970, 'BC4');
INSERT INTO bookings VALUES (4923849, 5116160, 5129903, 'SL2');
INSERT INTO bookings VALUES (9695645, 7532793, 4610651, 'IN1');
INSERT INTO bookings VALUES (4283104, 1529356, 6039117, 'DA3');
INSERT INTO bookings VALUES (5067574, 4998276, 6796520, 'BX1');
INSERT INTO bookings VALUES (6754234, 2427690, 6392794, 'FM5');
INSERT INTO bookings VALUES (6001132, 9526705, 5826789, 'DB8');
INSERT INTO bookings VALUES (5261503, 1083645, 6406136, 'VI1');
INSERT INTO bookings VALUES (4264503, 1432212, 9851125, 'QI0');
INSERT INTO bookings VALUES (2922171, 2934427, 8343974, 'RJ5');
INSERT INTO bookings VALUES (8021670, 9312273, 9314453, 'WU4');
INSERT INTO bookings VALUES (4277940, 7031540, 2847867, 'IB3');
INSERT INTO bookings VALUES (8134602, 2162029, 6445311, 'SM0');
INSERT INTO bookings VALUES (8125181, 7563300, 8323300, 'OI3');
INSERT INTO bookings VALUES (8690706, 9003017, 3284313, 'XQ2');
INSERT INTO bookings VALUES (6148122, 7403311, 4010212, 'PQ9');
INSERT INTO bookings VALUES (3936076, 2868562, 5072970, 'YT6');
INSERT INTO bookings VALUES (2030092, 2934427, 5431012, 'BV8');
INSERT INTO bookings VALUES (4537593, 9233734, 4229683, 'GJ7');
INSERT INTO bookings VALUES (5608758, 2168435, 7140829, 'ON3');
INSERT INTO bookings VALUES (5007808, 3354650, 6445311, 'LS6');
INSERT INTO bookings VALUES (7836730, 8042175, 7301468, 'GC7');
INSERT INTO bookings VALUES (5297788, 1432212, 8876058, 'CQ9');
INSERT INTO bookings VALUES (1892809, 4469259, 7450816, 'BG1');
INSERT INTO bookings VALUES (4733341, 8834504, 1430034, 'AB1');
INSERT INTO bookings VALUES (3439005, 4998276, 8066489, 'KV8');
INSERT INTO bookings VALUES (5810065, 4469259, 4704226, 'LU6');
INSERT INTO bookings VALUES (5196342, 2723241, 8280938, 'SX3');
INSERT INTO bookings VALUES (1159774, 6651063, 7727576, 'EV5');
INSERT INTO bookings VALUES (7414161, 9514557, 2055142, 'UA5');
INSERT INTO bookings VALUES (2279369, 7532793, 7768408, 'EH0');
INSERT INTO bookings VALUES (9659204, 2077334, 8886242, 'IF1');
INSERT INTO bookings VALUES (4237055, 2957004, 6004554, 'ZX8');
INSERT INTO bookings VALUES (3094228, 7173601, 1558964, 'RB9');
INSERT INTO bookings VALUES (1197289, 7403311, 2666559, 'BU7');
INSERT INTO bookings VALUES (1917183, 9362387, 1072702, 'FY1');
INSERT INTO bookings VALUES (4452090, 6124507, 9121478, 'GQ7');
INSERT INTO bookings VALUES (1066367, 3354650, 2603592, 'QP6');
INSERT INTO bookings VALUES (2666535, 6820491, 7463895, 'ZK8');
INSERT INTO bookings VALUES (5206982, 7217433, 1274638, 'KM5');
INSERT INTO bookings VALUES (1871300, 3307906, 4512184, 'WF9');
INSERT INTO bookings VALUES (6283542, 6418072, 6638170, 'OS2');
INSERT INTO bookings VALUES (2626061, 6405551, 6445311, 'TF2');
INSERT INTO bookings VALUES (3257335, 8308313, 7301468, 'SC3');
INSERT INTO bookings VALUES (6750254, 5380927, 6323776, 'WZ1');
INSERT INTO bookings VALUES (7436638, 3797933, 4949024, 'VO5');
INSERT INTO bookings VALUES (2811160, 3307906, 8180075, 'CU8');
INSERT INTO bookings VALUES (1785524, 6939753, 1098468, 'QH0');
INSERT INTO bookings VALUES (6711772, 5109828, 2374148, 'FL0');
INSERT INTO bookings VALUES (3441464, 5576927, 6796520, 'QW2');
INSERT INTO bookings VALUES (4196260, 8621561, 3416955, 'WK4');
INSERT INTO bookings VALUES (5201982, 2934427, 1389731, 'QA9');
INSERT INTO bookings VALUES (5677062, 6245743, 9218215, 'KN1');
INSERT INTO bookings VALUES (8651134, 6418072, 9153537, 'YZ4');
INSERT INTO bookings VALUES (8446690, 6127004, 2890701, 'OL2');
INSERT INTO bookings VALUES (2691695, 1729245, 8599491, 'SS0');
INSERT INTO bookings VALUES (2641572, 4219908, 7955477, 'TN5');
INSERT INTO bookings VALUES (7531812, 5734972, 7393057, 'DI6');
INSERT INTO bookings VALUES (3956391, 7968367, 7072581, 'IK3');
INSERT INTO bookings VALUES (6558595, 9963495, 4010212, 'EV4');
INSERT INTO bookings VALUES (2754985, 6774082, 9498851, 'WL7');
INSERT INTO bookings VALUES (6106392, 5380927, 8780015, 'DZ5');
INSERT INTO bookings VALUES (6215604, 5131170, 3699725, 'CX0');
INSERT INTO bookings VALUES (1630227, 6027472, 6868520, 'MB1');
INSERT INTO bookings VALUES (8166452, 2858315, 1299188, 'RI1');
INSERT INTO bookings VALUES (6218598, 9331874, 1454134, 'IL8');
INSERT INTO bookings VALUES (5444623, 9514557, 8651121, 'HY1');
INSERT INTO bookings VALUES (3641027, 7383012, 2340419, 'KU7');
INSERT INTO bookings VALUES (2539748, 1668346, 7088982, 'PU5');
INSERT INTO bookings VALUES (3788607, 9526705, 2648981, 'AV6');
INSERT INTO bookings VALUES (1689962, 8515559, 8969609, 'EB0');
INSERT INTO bookings VALUES (3683955, 9218001, 2278577, 'SC3');
INSERT INTO bookings VALUES (3287489, 6156293, 1050646, 'WC7');
INSERT INTO bookings VALUES (6366140, 9381230, 2201507, 'DF1');
INSERT INTO bookings VALUES (1190426, 1729245, 5762438, 'XG4');
INSERT INTO bookings VALUES (6373622, 9868867, 9851577, 'LF9');
INSERT INTO bookings VALUES (5121506, 1432212, 3900173, 'LC1');
INSERT INTO bookings VALUES (8362204, 1988776, 3040213, 'LN0');
INSERT INTO bookings VALUES (4495174, 7031540, 4982256, 'EU8');
INSERT INTO bookings VALUES (9125675, 5150169, 4715270, 'UZ4');
INSERT INTO bookings VALUES (2464334, 9233734, 9958280, 'TJ2');
INSERT INTO bookings VALUES (6005366, 2986199, 6357849, 'OR1');
INSERT INTO bookings VALUES (4222416, 5150169, 9152758, 'BR4');
INSERT INTO bookings VALUES (8947967, 4123196, 5939148, 'VP1');
INSERT INTO bookings VALUES (9577941, 7532793, 9218215, 'RS1');
INSERT INTO bookings VALUES (5747413, 9072354, 8741700, 'VS4');
INSERT INTO bookings VALUES (9098661, 1163615, 1389731, 'ZG8');
INSERT INTO bookings VALUES (7182698, 7791393, 8911236, 'MY0');
INSERT INTO bookings VALUES (2675574, 6156293, 1760850, 'VU7');
INSERT INTO bookings VALUES (1914249, 4662730, 7277451, 'PA8');
INSERT INTO bookings VALUES (2390558, 3107150, 1072702, 'FK6');
INSERT INTO bookings VALUES (9299563, 3454898, 7905616, 'RH6');
INSERT INTO bookings VALUES (9849424, 3610348, 1639958, 'RD5');
INSERT INTO bookings VALUES (1702250, 1497262, 8863508, 'GA5');
INSERT INTO bookings VALUES (8334716, 9312273, 5660582, 'UV5');
INSERT INTO bookings VALUES (5262734, 5520179, 8295690, 'FJ1');
INSERT INTO bookings VALUES (7310858, 5317875, 9509070, 'YV4');
INSERT INTO bookings VALUES (5885445, 9072354, 7991826, 'MD9');
INSERT INTO bookings VALUES (4857263, 6124507, 9724633, 'NT1');
INSERT INTO bookings VALUES (9851126, 5213265, 9056370, 'YL6');
INSERT INTO bookings VALUES (8265136, 4491384, 8021762, 'BZ1');
INSERT INTO bookings VALUES (5185922, 7496042, 3203412, 'IR4');
INSERT INTO bookings VALUES (2983453, 6820491, 4193470, 'RB1');
INSERT INTO bookings VALUES (9960145, 2168435, 8876058, 'DC8');
INSERT INTO bookings VALUES (5748118, 6405551, 2087960, 'EO1');
INSERT INTO bookings VALUES (4105544, 1931461, 4512184, 'VU2');
INSERT INTO bookings VALUES (8208173, 9636664, 3168880, 'ZF6');
INSERT INTO bookings VALUES (3301946, 5526936, 7203850, 'DC3');
INSERT INTO bookings VALUES (1338051, 6197631, 9877907, 'PD5');
INSERT INTO bookings VALUES (2122699, 8409944, 7328294, 'XP0');
INSERT INTO bookings VALUES (3525487, 5526936, 7614747, 'JU2');
INSERT INTO bookings VALUES (8786965, 4595619, 1862683, 'KX5');
INSERT INTO bookings VALUES (7427628, 9112089, 2666559, 'QE7');
INSERT INTO bookings VALUES (7764934, 6651063, 6251982, 'IG3');
INSERT INTO bookings VALUES (8134779, 1988776, 9910038, 'ZM7');
INSERT INTO bookings VALUES (1393069, 4469259, 2037973, 'MJ0');
INSERT INTO bookings VALUES (8165692, 3307906, 2738928, 'YS1');
INSERT INTO bookings VALUES (6176256, 3231298, 2750865, 'JK6');
INSERT INTO bookings VALUES (1168134, 2228727, 1546513, 'UO5');
INSERT INTO bookings VALUES (7212597, 2777401, 8920482, 'YN4');
INSERT INTO bookings VALUES (6145468, 1888680, 2531963, 'HR8');
INSERT INTO bookings VALUES (4719837, 8409944, 6109460, 'MX4');
INSERT INTO bookings VALUES (6917413, 8027778, 1639958, 'NJ4');
INSERT INTO bookings VALUES (3686733, 6352080, 5561925, 'OL1');
INSERT INTO bookings VALUES (4626064, 1154532, 9462768, 'RX9');
INSERT INTO bookings VALUES (3010495, 6127004, 3891059, 'KU9');
INSERT INTO bookings VALUES (1878686, 8515559, 4572194, 'UG5');
INSERT INTO bookings VALUES (9023283, 9868867, 3977294, 'VT1');
INSERT INTO bookings VALUES (5743018, 4100639, 5656525, 'GO9');
INSERT INTO bookings VALUES (8614766, 3986188, 4343069, 'DH6');
INSERT INTO bookings VALUES (8790690, 6245743, 7342356, 'VQ1');
INSERT INTO bookings VALUES (6758719, 2481283, 8179773, 'HI9');
INSERT INTO bookings VALUES (6756171, 3482381, 7393057, 'MS2');
INSERT INTO bookings VALUES (6442671, 7496042, 1072702, 'GZ8');
INSERT INTO bookings VALUES (3977103, 5853975, 5989126, 'OY1');
INSERT INTO bookings VALUES (9431928, 1083645, 4715270, 'MJ7');
INSERT INTO bookings VALUES (5373112, 2427690, 6086941, 'DE6');
INSERT INTO bookings VALUES (5852557, 4453764, 2378137, 'TM0');
INSERT INTO bookings VALUES (3174616, 3677816, 4851977, 'UV8');
INSERT INTO bookings VALUES (2203629, 4332111, 2702241, 'LI3');
INSERT INTO bookings VALUES (5161393, 2961215, 1389731, 'GK0');
INSERT INTO bookings VALUES (4049970, 7220233, 2616380, 'RL8');
INSERT INTO bookings VALUES (7182766, 9226362, 5431012, 'KP4');
INSERT INTO bookings VALUES (2902282, 7403311, 2037973, 'MY6');
INSERT INTO bookings VALUES (6737581, 4100639, 1497319, 'PM8');
INSERT INTO bookings VALUES (3831712, 2294181, 7938588, 'ES8');
INSERT INTO bookings VALUES (6187299, 5328741, 7144851, 'HW2');
INSERT INTO bookings VALUES (8260979, 5150169, 6802003, 'MM8');
INSERT INTO bookings VALUES (9304185, 1610270, 6802003, 'AN1');
INSERT INTO bookings VALUES (4608722, 2168435, 6868520, 'UK0');
INSERT INTO bookings VALUES (4216040, 4892788, 7611726, 'CJ3');
INSERT INTO bookings VALUES (3602356, 7786633, 3941019, 'MG4');
INSERT INTO bookings VALUES (5051841, 8948057, 8983277, 'UQ2');
INSERT INTO bookings VALUES (8159183, 3700732, 3284313, 'PM1');
INSERT INTO bookings VALUES (8510137, 5116160, 6357849, 'TX3');
INSERT INTO bookings VALUES (5030237, 4530532, 8899441, 'CY3');
INSERT INTO bookings VALUES (1864300, 8515559, 1150752, 'HR5');
INSERT INTO bookings VALUES (6043567, 6383237, 6861883, 'GA0');
INSERT INTO bookings VALUES (5026551, 9226362, 4626499, 'GP6');
INSERT INTO bookings VALUES (2912484, 1083645, 6233896, 'JM2');
INSERT INTO bookings VALUES (1960793, 4662730, 4010212, 'ED7');
INSERT INTO bookings VALUES (8684164, 1154532, 1487850, 'NP1');
INSERT INTO bookings VALUES (1361188, 8834504, 1389731, 'YO5');
INSERT INTO bookings VALUES (5657432, 5116160, 7277451, 'FF0');
INSERT INTO bookings VALUES (3599900, 4772680, 2278577, 'CM1');
INSERT INTO bookings VALUES (1052546, 1931461, 7598471, 'DV8');
INSERT INTO bookings VALUES (9465679, 9514557, 9298883, 'UW8');
INSERT INTO bookings VALUES (3857669, 5317875, 5216329, 'SG6');
INSERT INTO bookings VALUES (6709181, 1264848, 2188866, 'IV2');
INSERT INTO bookings VALUES (7255555, 4530532, 6719936, 'JI6');
INSERT INTO bookings VALUES (4216718, 2161934, 1671714, 'TP5');
INSERT INTO bookings VALUES (2068071, 8155828, 5129903, 'HI3');
INSERT INTO bookings VALUES (3643982, 1264848, 7905616, 'GW3');
INSERT INTO bookings VALUES (9744335, 4332111, 1274638, 'KI7');
INSERT INTO bookings VALUES (1054443, 6447803, 5098506, 'QT1');
INSERT INTO bookings VALUES (7489303, 1729245, 1290249, 'WN9');
INSERT INTO bookings VALUES (4682656, 6651063, 6746460, 'VG7');
INSERT INTO bookings VALUES (9603207, 2407452, 8493745, 'CW9');
INSERT INTO bookings VALUES (1973648, 8409944, 2603592, 'BW4');
INSERT INTO bookings VALUES (6043240, 9925283, 4851977, 'JT1');
INSERT INTO bookings VALUES (6510816, 5494015, 7755856, 'DX9');
INSERT INTO bookings VALUES (8490025, 9848880, 6757414, 'JP9');
INSERT INTO bookings VALUES (8140881, 7515968, 8865172, 'XY3');
INSERT INTO bookings VALUES (1386972, 2470259, 7328294, 'DJ5');
INSERT INTO bookings VALUES (5353054, 1988776, 1028636, 'YR0');
INSERT INTO bookings VALUES (7771381, 2959304, 2512031, 'TO5');
INSERT INTO bookings VALUES (8003870, 3086160, 6757414, 'BS9');
INSERT INTO bookings VALUES (1356035, 2505634, 2751370, 'KY3');
INSERT INTO bookings VALUES (2602306, 3086160, 9462768, 'XO2');
INSERT INTO bookings VALUES (9325304, 7169430, 2949063, 'UC9');
INSERT INTO bookings VALUES (3579033, 1432212, 8969609, 'LJ5');
INSERT INTO bookings VALUES (1113922, 6301649, 1867373, 'CO8');
INSERT INTO bookings VALUES (1141592, 9514557, 3989140, 'ZH8');
INSERT INTO bookings VALUES (7267003, 6992003, 2278577, 'IH1');
INSERT INTO bookings VALUES (7849737, 8812278, 5216329, 'WS4');
INSERT INTO bookings VALUES (3809352, 5380927, 9142476, 'VS4');
INSERT INTO bookings VALUES (2457131, 2153952, 4843875, 'WE7');
INSERT INTO bookings VALUES (3072277, 9538849, 4144513, 'HL3');
INSERT INTO bookings VALUES (9442095, 9027045, 4881008, 'WF6');
INSERT INTO bookings VALUES (5116095, 4219908, 8021762, 'PW4');
INSERT INTO bookings VALUES (9331041, 8173911, 2847867, 'BK1');
INSERT INTO bookings VALUES (7797938, 8812278, 2430594, 'FX1');
INSERT INTO bookings VALUES (4075671, 5721829, 5216329, 'GH3');
INSERT INTO bookings VALUES (8529390, 4073362, 3689846, 'UH5');
INSERT INTO bookings VALUES (4165264, 9868867, 2751370, 'YR7');
INSERT INTO bookings VALUES (5107470, 7873250, 5218698, 'DM6');
INSERT INTO bookings VALUES (8883140, 3959460, 7778473, 'FC6');
INSERT INTO bookings VALUES (7510834, 1196601, 9916922, 'WB4');
INSERT INTO bookings VALUES (3809463, 3610348, 2887013, 'LL9');
INSERT INTO bookings VALUES (9793545, 6156293, 3243026, 'CB8');
INSERT INTO bookings VALUES (7680566, 4469259, 6757414, 'WS3');
INSERT INTO bookings VALUES (7764598, 2084890, 1099849, 'SP0');
INSERT INTO bookings VALUES (8504440, 9362387, 4128390, 'FG8');
INSERT INTO bookings VALUES (4407782, 3998210, 8308378, 'AZ0');
INSERT INTO bookings VALUES (5037793, 1072758, 7151159, 'EP8');
INSERT INTO bookings VALUES (2845499, 1500606, 9212933, 'HI2');
INSERT INTO bookings VALUES (9463701, 9112089, 1050646, 'WE9');
INSERT INTO bookings VALUES (8662185, 2858315, 8175644, 'DD2');
INSERT INTO bookings VALUES (8443275, 3233971, 1832357, 'NM8');
INSERT INTO bookings VALUES (1705939, 1163615, 6683188, 'VB9');
INSERT INTO bookings VALUES (6291782, 8621561, 6407111, 'DZ2');
INSERT INTO bookings VALUES (1307135, 9092559, 8704711, 'LN1');
INSERT INTO bookings VALUES (2159705, 8994337, 8343974, 'TA2');
INSERT INTO bookings VALUES (2541475, 3891137, 9212933, 'MZ9');
INSERT INTO bookings VALUES (8539530, 7532793, 8126220, 'MI0');
INSERT INTO bookings VALUES (6584934, 5116160, 2077631, 'VE5');
INSERT INTO bookings VALUES (2004972, 4491384, 2489999, 'EJ0');
INSERT INTO bookings VALUES (9510078, 7515968, 9796068, 'AA8');
INSERT INTO bookings VALUES (8406864, 1083645, 3495148, 'JF0');
INSERT INTO bookings VALUES (5233928, 2162029, 1698493, 'ZV3');
INSERT INTO bookings VALUES (2848794, 5682977, 2037973, 'NN5');
INSERT INTO bookings VALUES (7788573, 8515559, 8886242, 'KW5');
INSERT INTO bookings VALUES (3624312, 2505634, 9121478, 'OH8');
INSERT INTO bookings VALUES (4083161, 2161934, 2077631, 'JB1');
INSERT INTO bookings VALUES (4176575, 6405551, 9121478, 'DE8');
INSERT INTO bookings VALUES (8807910, 5109828, 3900173, 'PT1');
INSERT INTO bookings VALUES (9843188, 5682977, 3245038, 'SY8');
INSERT INTO bookings VALUES (3979488, 9848880, 3416955, 'EC5');
INSERT INTO bookings VALUES (4099048, 8155828, 3891092, 'FZ6');
INSERT INTO bookings VALUES (7006988, 6405551, 6039117, 'GV8');
INSERT INTO bookings VALUES (1815322, 2868562, 9248389, 'UP4');
INSERT INTO bookings VALUES (6076676, 2427690, 7463895, 'FR8');
INSERT INTO bookings VALUES (7124957, 5150169, 4647476, 'AD5');
INSERT INTO bookings VALUES (8870772, 8621561, 8689138, 'WJ6');
INSERT INTO bookings VALUES (7799687, 5465007, 9550745, 'ZS8');
INSERT INTO bookings VALUES (9054871, 1196601, 8895170, 'CF8');
INSERT INTO bookings VALUES (5373322, 3233971, 1532836, 'VZ5');
INSERT INTO bookings VALUES (5040756, 2470259, 8126220, 'NR4');
INSERT INTO bookings VALUES (8999406, 1328187, 5635936, 'AI6');
INSERT INTO bookings VALUES (5279016, 4332111, 9550745, 'OV9');
INSERT INTO bookings VALUES (3817471, 1668346, 8920482, 'RL9');
INSERT INTO bookings VALUES (7391811, 6447803, 5635936, 'CJ2');
INSERT INTO bookings VALUES (2386881, 1328187, 6004554, 'AM3');
INSERT INTO bookings VALUES (8998005, 1931461, 3168880, 'LS4');
INSERT INTO bookings VALUES (5215599, 9868867, 1290249, 'YL2');
INSERT INTO bookings VALUES (1527600, 1328187, 4999475, 'JC2');
INSERT INTO bookings VALUES (5983602, 9226362, 8295690, 'AK1');
INSERT INTO bookings VALUES (8494942, 1154532, 8040248, 'GQ9');
INSERT INTO bookings VALUES (8577033, 7532793, 8462608, 'XU1');
INSERT INTO bookings VALUES (3804691, 3233971, 2751370, 'MR0');
INSERT INTO bookings VALUES (7302640, 7217433, 8258066, 'RZ9');
INSERT INTO bookings VALUES (3065839, 2911738, 4318596, 'DE3');
INSERT INTO bookings VALUES (2007605, 5213265, 9637487, 'ZW6');
INSERT INTO bookings VALUES (3785046, 5116160, 6461218, 'MU0');
INSERT INTO bookings VALUES (6886730, 4595619, 6719936, 'NX1');
INSERT INTO bookings VALUES (7710596, 7563300, 9509070, 'WL6');
INSERT INTO bookings VALUES (1912987, 8423344, 8493745, 'JA3');
INSERT INTO bookings VALUES (8879282, 2077334, 9462768, 'HR6');
INSERT INTO bookings VALUES (2016882, 7169430, 8780015, 'KL9');
INSERT INTO bookings VALUES (6229798, 1979648, 3739269, 'RJ8');
INSERT INTO bookings VALUES (1116067, 1529356, 8258066, 'ZU9');
INSERT INTO bookings VALUES (4366075, 1547921, 3040213, 'WU1');
INSERT INTO bookings VALUES (1588511, 5317875, 1950026, 'MB2');
INSERT INTO bookings VALUES (7003855, 4123196, 6746460, 'EF6');
INSERT INTO bookings VALUES (3695369, 4332111, 7144851, 'VO4');
INSERT INTO bookings VALUES (8036188, 7786316, 1610544, 'TR5');
INSERT INTO bookings VALUES (4024318, 7537489, 4512184, 'SU2');
INSERT INTO bookings VALUES (9159559, 2911738, 5862298, 'NM9');
INSERT INTO bookings VALUES (8160670, 6405551, 2055142, 'TO7');
INSERT INTO bookings VALUES (9760088, 6383237, 6096264, 'BQ7');
INSERT INTO bookings VALUES (5873835, 5526936, 5282783, 'IS3');
INSERT INTO bookings VALUES (5197354, 9243803, 8021762, 'HQ3');
INSERT INTO bookings VALUES (9222035, 9027045, 6638170, 'SU6');
INSERT INTO bookings VALUES (9734063, 9963495, 6432174, 'YE0');
INSERT INTO bookings VALUES (8311532, 9299869, 3897785, 'VG2');
INSERT INTO bookings VALUES (7820234, 4059576, 7512215, 'TG1');
INSERT INTO bookings VALUES (2102306, 1116912, 4661322, 'UM1');
INSERT INTO bookings VALUES (3664717, 5576927, 6802003, 'OT7');
INSERT INTO bookings VALUES (1327384, 2957004, 3511197, 'GF2');
INSERT INTO bookings VALUES (1151875, 4073362, 4937369, 'SV9');
INSERT INTO bookings VALUES (3897673, 1960211, 3830674, 'QA4');
INSERT INTO bookings VALUES (9684543, 3822974, 7459023, 'IC6');
INSERT INTO bookings VALUES (8169475, 8812278, 1832357, 'PT9');
INSERT INTO bookings VALUES (5068176, 1272742, 8876058, 'MW3');
INSERT INTO bookings VALUES (6927368, 9963495, 5989126, 'YQ3');
INSERT INTO bookings VALUES (8476594, 2427690, 4128390, 'BS7');
INSERT INTO bookings VALUES (7267453, 4073362, 6323776, 'VN1');
INSERT INTO bookings VALUES (6779055, 7563300, 8442580, 'WQ4');
INSERT INTO bookings VALUES (5300181, 2934427, 6323776, 'RS7');
INSERT INTO bookings VALUES (2795819, 2470259, 4763209, 'IV9');
INSERT INTO bookings VALUES (5971778, 1597896, 7938588, 'YX9');
INSERT INTO bookings VALUES (4262048, 6236965, 2055142, 'IX6');
INSERT INTO bookings VALUES (4463303, 6954524, 5660582, 'NR8');
INSERT INTO bookings VALUES (9357543, 6418072, 1546513, 'IR8');
INSERT INTO bookings VALUES (8076992, 4797001, 8180075, 'HO0');
INSERT INTO bookings VALUES (2381088, 9072354, 7854170, 'YK3');
INSERT INTO bookings VALUES (2230259, 9963495, 7727576, 'YN6');
INSERT INTO bookings VALUES (8678382, 2228727, 5984049, 'XG9');
INSERT INTO bookings VALUES (8518583, 5494015, 2648981, 'IR3');
INSERT INTO bookings VALUES (6239863, 4246383, 3891092, 'UZ0');
INSERT INTO bookings VALUES (1684153, 2294181, 9218215, 'TQ5');
INSERT INTO bookings VALUES (2112280, 6080953, 7613367, 'MF9');
INSERT INTO bookings VALUES (7699059, 4491384, 1546513, 'CC6');
INSERT INTO bookings VALUES (6717934, 1010438, 6638170, 'UZ0');
INSERT INTO bookings VALUES (7178612, 3150842, 4982256, 'XW9');
INSERT INTO bookings VALUES (4119119, 5116160, 4843875, 'LK9');
INSERT INTO bookings VALUES (2113573, 1729245, 7128137, 'ZI3');
INSERT INTO bookings VALUES (4732178, 6954524, 7991826, 'RN7');
INSERT INTO bookings VALUES (5990996, 3610348, 6271427, 'SL6');
INSERT INTO bookings VALUES (9695794, 9226362, 3252086, 'TO5');
INSERT INTO bookings VALUES (3709835, 2696654, 9611592, 'SF1');
INSERT INTO bookings VALUES (2653056, 4219908, 9958280, 'MY0');
INSERT INTO bookings VALUES (2290399, 6111066, 2861626, 'QD4');
INSERT INTO bookings VALUES (1628369, 5150169, 2340419, 'QL6');
INSERT INTO bookings VALUES (4614602, 4123196, 2055142, 'IZ9');
INSERT INTO bookings VALUES (4521517, 5465007, 1028636, 'VM2');
INSERT INTO bookings VALUES (4504596, 7532793, 5218698, 'AQ5');
INSERT INTO bookings VALUES (4503634, 7786316, 1209499, 'HJ9');
INSERT INTO bookings VALUES (7692330, 2961215, 3985619, 'KC6');
INSERT INTO bookings VALUES (6085006, 1272742, 1668849, 'PB1');
INSERT INTO bookings VALUES (8798651, 7515968, 1290086, 'GF8');
INSERT INTO bookings VALUES (9771071, 6766644, 2378137, 'GP2');
INSERT INTO bookings VALUES (5341396, 5494015, 3830674, 'PI3');
INSERT INTO bookings VALUES (8410014, 7873250, 1965797, 'TQ1');
INSERT INTO bookings VALUES (7209569, 6124507, 4010212, 'WQ7');
INSERT INTO bookings VALUES (5892423, 9469611, 3040213, 'FM8');
INSERT INTO bookings VALUES (6618338, 2777401, 8226595, 'SZ5');
INSERT INTO bookings VALUES (5735932, 6245743, 7482197, 'ST3');
INSERT INTO bookings VALUES (3939910, 8224728, 3989140, 'SM1');
INSERT INTO bookings VALUES (9421408, 3986188, 2914075, 'XN5');
INSERT INTO bookings VALUES (2956214, 6329350, 4572194, 'YK8');
INSERT INTO bookings VALUES (8527627, 5576927, 9886698, 'BX0');
INSERT INTO bookings VALUES (4353505, 3231298, 5986471, 'JI5');
INSERT INTO bookings VALUES (8654339, 8189656, 5089360, 'WD0');
INSERT INTO bookings VALUES (8401928, 6301649, 3973859, 'PD3');
INSERT INTO bookings VALUES (4636877, 1328187, 4438274, 'UH3');
INSERT INTO bookings VALUES (2476885, 1432212, 2877495, 'MB3');
INSERT INTO bookings VALUES (7638577, 1154532, 8258066, 'PH0');
INSERT INTO bookings VALUES (6212286, 1116912, 4344487, 'YT9');
INSERT INTO bookings VALUES (1303227, 4662730, 9304895, 'OI5');
INSERT INTO bookings VALUES (3743456, 1729245, 3985619, 'AI2');
INSERT INTO bookings VALUES (8987248, 1196601, 9162740, 'UY1');
INSERT INTO bookings VALUES (6893355, 8834504, 2861626, 'PT8');
INSERT INTO bookings VALUES (6396126, 7791393, 2117864, 'KQ2');
INSERT INTO bookings VALUES (6765278, 3610348, 9412051, 'ID5');
INSERT INTO bookings VALUES (3924377, 5116160, 9792793, 'EW6');
INSERT INTO bookings VALUES (4761870, 3307906, 5089360, 'DQ2');
INSERT INTO bookings VALUES (5091401, 6447803, 8886242, 'MW1');
INSERT INTO bookings VALUES (2278269, 6373970, 6445311, 'XC2');
INSERT INTO bookings VALUES (3598706, 1718275, 1398220, 'EU7');
INSERT INTO bookings VALUES (9757055, 9027045, 9977745, 'UU6');
INSERT INTO bookings VALUES (1504954, 6776228, 8493745, 'UM0');
INSERT INTO bookings VALUES (2824011, 7173601, 5841139, 'OS2');
INSERT INTO bookings VALUES (3614633, 1272742, 9011552, 'FQ1');
INSERT INTO bookings VALUES (8684637, 6766644, 9412051, 'ZM4');
INSERT INTO bookings VALUES (9808297, 1979648, 2924296, 'KZ1');
INSERT INTO bookings VALUES (9310553, 5131170, 2914075, 'ML3');
INSERT INTO bookings VALUES (3829098, 8515559, 7598471, 'WR4');
INSERT INTO bookings VALUES (7013992, 3822974, 1751004, 'EH6');
INSERT INTO bookings VALUES (4275814, 2911738, 3901972, 'WJ4');
INSERT INTO bookings VALUES (8818406, 7515968, 6445311, 'MC3');
INSERT INTO bookings VALUES (6512178, 2227634, 2281058, 'SO0');
INSERT INTO bookings VALUES (6087178, 2161934, 1398220, 'HT6');
INSERT INTO bookings VALUES (1049770, 3487415, 8895170, 'JT2');
INSERT INTO bookings VALUES (6598273, 5116160, 7255476, 'US2');
INSERT INTO bookings VALUES (1258608, 1979648, 8393896, 'GL1');
INSERT INTO bookings VALUES (9319738, 2407452, 1398220, 'FI2');
INSERT INTO bookings VALUES (2004006, 1083645, 5830883, 'YK4');
INSERT INTO bookings VALUES (2897716, 9868867, 9792793, 'YA7');
INSERT INTO bookings VALUES (5919107, 2294181, 5218698, 'ZM1');
INSERT INTO bookings VALUES (8205394, 6329350, 4448063, 'ZG6');
INSERT INTO bookings VALUES (3003674, 6447803, 3420721, 'SH5');
INSERT INTO bookings VALUES (5690330, 6127004, 2281058, 'GY1');
INSERT INTO bookings VALUES (2484696, 5576927, 1487850, 'OA8');
INSERT INTO bookings VALUES (9075455, 7899943, 4128390, 'XI8');
INSERT INTO bookings VALUES (5235900, 6939753, 1028636, 'XJ1');
INSERT INTO bookings VALUES (3394152, 5213265, 9218215, 'DP9');
INSERT INTO bookings VALUES (1845772, 9362387, 8323300, 'WY4');
INSERT INTO bookings VALUES (8760021, 2696654, 5818296, 'SP0');
INSERT INTO bookings VALUES (7397581, 2723241, 3420721, 'LT2');
INSERT INTO bookings VALUES (6574159, 8994337, 2481637, 'FR3');
INSERT INTO bookings VALUES (4542378, 7173601, 8728948, 'XX1');
INSERT INTO bookings VALUES (1673067, 1339636, 6638170, 'BW3');
INSERT INTO bookings VALUES (8213028, 6920459, 7255476, 'MN7');
INSERT INTO bookings VALUES (4962789, 8173911, 7463895, 'ZU6');
INSERT INTO bookings VALUES (2181528, 5734972, 3977294, 'LX8');
INSERT INTO bookings VALUES (7736817, 9072354, 4881008, 'YU0');
INSERT INTO bookings VALUES (4811052, 9728828, 6086941, 'VM9');
INSERT INTO bookings VALUES (2377499, 1497262, 2924296, 'QB8');
INSERT INTO bookings VALUES (4991480, 1931461, 9115191, 'FC4');
INSERT INTO bookings VALUES (3787090, 3998210, 1098468, 'NJ1');
INSERT INTO bookings VALUES (6811678, 7515968, 7151159, 'DG4');
INSERT INTO bookings VALUES (7617005, 5328741, 4128390, 'EI8');
INSERT INTO bookings VALUES (1145922, 2777401, 9343423, 'TN9');
INSERT INTO bookings VALUES (6787359, 2961215, 1398220, 'AM1');
INSERT INTO bookings VALUES (4765828, 3677816, 1299188, 'UI4');
INSERT INTO bookings VALUES (1521824, 3086160, 1957125, 'EL9');
INSERT INTO bookings VALUES (3848688, 7786316, 5660582, 'CO7');
INSERT INTO bookings VALUES (6129697, 3482381, 5301340, 'AK0');
INSERT INTO bookings VALUES (3986898, 1163615, 4343069, 'VM4');
INSERT INTO bookings VALUES (8813465, 1729245, 9977745, 'NU1');
INSERT INTO bookings VALUES (6960741, 7515968, 6796520, 'UD1');
INSERT INTO bookings VALUES (2341251, 9963495, 5301340, 'KG9');
INSERT INTO bookings VALUES (2758436, 1339636, 5109260, 'OT3');
INSERT INTO bookings VALUES (1099646, 8189656, 2077940, 'KS6');
INSERT INTO bookings VALUES (6750815, 9072354, 2181681, 'JT7');
INSERT INTO bookings VALUES (7312771, 2776678, 1099849, 'BN3');
INSERT INTO bookings VALUES (8539538, 6027472, 5826789, 'CK1');
INSERT INTO bookings VALUES (2421272, 5494015, 9455454, 'QE2');
INSERT INTO bookings VALUES (8574290, 9469611, 2248186, 'KC6');
INSERT INTO bookings VALUES (4703264, 4073362, 4318596, 'SP4');
INSERT INTO bookings VALUES (5929437, 9793501, 3928961, 'HY0');
INSERT INTO bookings VALUES (8638856, 3986188, 2924296, 'LL1');
INSERT INTO bookings VALUES (1951269, 8027778, 2248186, 'ME7');
INSERT INTO bookings VALUES (2788615, 5328741, 6445311, 'KS6');
INSERT INTO bookings VALUES (2613937, 7383012, 3248916, 'NQ9');
INSERT INTO bookings VALUES (1979155, 7968367, 9889589, 'EO8');
INSERT INTO bookings VALUES (6288844, 6651063, 9153537, 'DA6');
INSERT INTO bookings VALUES (5001206, 9526705, 8865172, 'QO1');
INSERT INTO bookings VALUES (5371105, 3986188, 6802003, 'ZO9');
INSERT INTO bookings VALUES (5544563, 5745431, 8670473, 'CO9');
INSERT INTO bookings VALUES (2042893, 2481283, 7328294, 'FR0');
INSERT INTO bookings VALUES (1111874, 7786633, 1624553, 'SD7');
INSERT INTO bookings VALUES (7126961, 6405551, 2993925, 'LG5');
INSERT INTO bookings VALUES (9636745, 1328187, 3790582, 'OL5');
INSERT INTO bookings VALUES (3328283, 8423344, 5616328, 'QB5');
INSERT INTO bookings VALUES (3219580, 4491384, 1610544, 'HO2');
INSERT INTO bookings VALUES (7996341, 4772680, 9513965, 'YX8');
INSERT INTO bookings VALUES (3041401, 6418072, 9796068, 'XM6');
INSERT INTO bookings VALUES (1538571, 4595619, 1050646, 'HM6');
INSERT INTO bookings VALUES (5538174, 7786633, 2887013, 'WH6');
INSERT INTO bookings VALUES (5845335, 2868562, 6980627, 'LP1');
INSERT INTO bookings VALUES (4421959, 3307906, 5282783, 'DA6');
INSERT INTO bookings VALUES (1536097, 2957004, 6710958, 'QD1');
INSERT INTO bookings VALUES (4009000, 7173601, 1497319, 'NO3');
INSERT INTO bookings VALUES (9204189, 2965959, 9808915, 'UY8');
INSERT INTO bookings VALUES (4486351, 6111066, 2369537, 'WE5');
INSERT INTO bookings VALUES (2703529, 8189656, 4236485, 'JA0');
INSERT INTO bookings VALUES (6959281, 6156293, 2603592, 'WW6');
INSERT INTO bookings VALUES (7178690, 2986199, 4626499, 'WK5');
INSERT INTO bookings VALUES (7756506, 1668346, 2603592, 'BZ5');
INSERT INTO bookings VALUES (3716336, 9728828, 1454134, 'PY7');
INSERT INTO bookings VALUES (7178317, 7635536, 8040248, 'GS6');
INSERT INTO bookings VALUES (1510315, 6793309, 2648981, 'XZ6');
INSERT INTO bookings VALUES (5991028, 6992003, 1671714, 'BS2');
INSERT INTO bookings VALUES (7065558, 6774082, 8284228, 'DP9');
INSERT INTO bookings VALUES (4692500, 8948057, 9498851, 'VH0');
INSERT INTO bookings VALUES (6151320, 5465007, 8343974, 'IX5');
INSERT INTO bookings VALUES (9837411, 7383012, 3203412, 'CA8');
INSERT INTO bookings VALUES (6039800, 5494015, 5561925, 'CP5');
INSERT INTO bookings VALUES (5367651, 1718275, 4378471, 'EG9');
INSERT INTO bookings VALUES (4492837, 1010438, 9142476, 'EE9');
INSERT INTO bookings VALUES (7796733, 4059576, 5506687, 'BY5');
INSERT INTO bookings VALUES (7889117, 8189656, 1267699, 'PR0');
INSERT INTO bookings VALUES (6914575, 3482381, 1947070, 'UV0');
INSERT INTO bookings VALUES (8837629, 2868562, 4915229, 'IF3');
INSERT INTO bookings VALUES (4305778, 1154532, 1736878, 'LP1');
INSERT INTO bookings VALUES (3518512, 8994337, 6757414, 'AF3');
INSERT INTO bookings VALUES (1431180, 7899943, 2993925, 'HT5');
INSERT INTO bookings VALUES (8737669, 9331874, 5506687, 'KF7');
INSERT INTO bookings VALUES (3183782, 7563300, 7470412, 'JV9');
INSERT INTO bookings VALUES (5634334, 3482381, 3506783, 'DK4');
INSERT INTO bookings VALUES (7384931, 3959460, 1347646, 'IL6');
INSERT INTO bookings VALUES (1208366, 1154532, 2037973, 'VD1');
INSERT INTO bookings VALUES (1964975, 4453764, 1736878, 'PC3');
INSERT INTO bookings VALUES (2999648, 8409944, 8295690, 'EL4');
INSERT INTO bookings VALUES (1431245, 4797001, 8817486, 'JV4');
INSERT INTO bookings VALUES (6167319, 2934427, 3928961, 'AO9');
INSERT INTO bookings VALUES (5954034, 6027472, 9343423, 'AV1');
INSERT INTO bookings VALUES (3779626, 5682977, 3168880, 'AF0');
INSERT INTO bookings VALUES (2690461, 1729245, 4193470, 'OE9');
INSERT INTO bookings VALUES (4746635, 2961215, 3967989, 'AX8');
INSERT INTO bookings VALUES (1635916, 5494015, 3491018, 'HC9');
INSERT INTO bookings VALUES (1609490, 5745431, 8704711, 'XS1');
INSERT INTO bookings VALUES (3655349, 7786316, 4795341, 'OP6');
INSERT INTO bookings VALUES (4888508, 1272742, 5645246, 'XQ6');
INSERT INTO bookings VALUES (9632599, 6080953, 9304895, 'KE0');
INSERT INTO bookings VALUES (3264812, 3233971, 8911236, 'MI7');
INSERT INTO bookings VALUES (8739603, 1668346, 2887013, 'QQ5');
INSERT INTO bookings VALUES (1865080, 6418072, 3216377, 'TG7');
INSERT INTO bookings VALUES (2900825, 3487415, 9153537, 'FI6');
INSERT INTO bookings VALUES (9829110, 7220233, 7899624, 'BK9');
INSERT INTO bookings VALUES (9662347, 2696654, 1671714, 'VN3');
INSERT INTO bookings VALUES (9764765, 9226362, 1668849, 'RK0');
INSERT INTO bookings VALUES (8876531, 7635536, 5989126, 'VH4');
INSERT INTO bookings VALUES (5195999, 4797001, 9962111, 'NW9');
INSERT INTO bookings VALUES (6829687, 2153952, 1277099, 'QB7');
INSERT INTO bookings VALUES (7802388, 5131170, 1760850, 'ER2');
INSERT INTO bookings VALUES (2163221, 7791393, 8164721, 'BP4');
INSERT INTO bookings VALUES (7418489, 2934427, 5826789, 'HR2');
INSERT INTO bookings VALUES (2313927, 1741094, 8295690, 'SI5');
INSERT INTO bookings VALUES (6453455, 5745431, 9947215, 'EQ6');
INSERT INTO bookings VALUES (1296435, 1888680, 9947215, 'YL9');
INSERT INTO bookings VALUES (4194541, 2228727, 7512215, 'FA1');
INSERT INTO bookings VALUES (8547769, 8994337, 5842415, 'PL1');
INSERT INTO bookings VALUES (2512865, 5150169, 1762159, 'YD6');
INSERT INTO bookings VALUES (5976691, 1211134, 1832357, 'JZ7');
INSERT INTO bookings VALUES (8475534, 6774082, 9946112, 'YW7');
INSERT INTO bookings VALUES (4559812, 2965959, 3747976, 'CP8');
INSERT INTO bookings VALUES (2507666, 5931547, 6952130, 'BD2');
INSERT INTO bookings VALUES (8978098, 3700732, 5616328, 'EL9');
INSERT INTO bookings VALUES (9306204, 9072354, 8780015, 'PP8');
INSERT INTO bookings VALUES (5349297, 6774082, 7905616, 'EO3');
INSERT INTO bookings VALUES (7243928, 3822974, 1290086, 'HX2');
INSERT INTO bookings VALUES (5440509, 6352080, 6999905, 'YV1');
INSERT INTO bookings VALUES (9541641, 6793309, 3989140, 'WP8');
INSERT INTO bookings VALUES (4523702, 5010487, 1607199, 'HA8');
INSERT INTO bookings VALUES (2295632, 1432017, 5098506, 'PQ6');
INSERT INTO bookings VALUES (1036137, 1072758, 9851125, 'VB0');
INSERT INTO bookings VALUES (3416889, 3822974, 2092479, 'GT1');
INSERT INTO bookings VALUES (4800232, 7217433, 5928475, 'RU3');
INSERT INTO bookings VALUES (5390165, 6329350, 3203412, 'DP6');
INSERT INTO bookings VALUES (1895972, 6111066, 1762159, 'ZO8');
INSERT INTO bookings VALUES (2952438, 2084890, 2666559, 'AN8');
INSERT INTO bookings VALUES (4990200, 7220233, 2616380, 'VH9');
INSERT INTO bookings VALUES (3530741, 3224135, 4378471, 'OO7');
INSERT INTO bookings VALUES (5290873, 7899943, 9011552, 'YI4');
INSERT INTO bookings VALUES (2278533, 3603058, 5097537, 'TT9');
INSERT INTO bookings VALUES (1194669, 5576927, 2512031, 'BV4');
INSERT INTO bookings VALUES (7708412, 9331874, 1099849, 'CD4');
INSERT INTO bookings VALUES (3578181, 7173601, 2340419, 'OD8');
INSERT INTO bookings VALUES (1992961, 4246383, 2188866, 'UV5');
INSERT INTO bookings VALUES (8501450, 9383250, 9946112, 'YT8');
INSERT INTO bookings VALUES (6063777, 9526705, 4512184, 'RW8');
INSERT INTO bookings VALUES (2852219, 3354650, 9796068, 'LG1');
INSERT INTO bookings VALUES (5592345, 1888680, 4343069, 'PN6');
INSERT INTO bookings VALUES (7784692, 2961215, 6719936, 'FF2');
INSERT INTO bookings VALUES (2559477, 1154532, 1240763, 'UR1');
INSERT INTO bookings VALUES (6174471, 1892068, 2750865, 'MV5');
INSERT INTO bookings VALUES (2235353, 7173601, 4572194, 'GJ8');
INSERT INTO bookings VALUES (7308723, 4530854, 7571718, 'RS1');
INSERT INTO bookings VALUES (6994177, 3354650, 8280938, 'DH4');
INSERT INTO bookings VALUES (1987034, 3454898, 4229683, 'CA2');
INSERT INTO bookings VALUES (6202020, 4332111, 2077940, 'MZ5');
INSERT INTO bookings VALUES (1399338, 1072758, 1546513, 'HK4');
INSERT INTO bookings VALUES (7167852, 6027472, 2374148, 'OD9');
INSERT INTO bookings VALUES (5045110, 4453764, 8021762, 'EW7');
INSERT INTO bookings VALUES (5308411, 6124507, 8822314, 'ZY5');
INSERT INTO bookings VALUES (7641636, 9526705, 1846299, 'QR2');
INSERT INTO bookings VALUES (1333361, 6124507, 3830674, 'CH8');
INSERT INTO bookings VALUES (5053175, 1211134, 6461218, 'XB3');
INSERT INTO bookings VALUES (8012149, 9112089, 8741700, 'AK9');
INSERT INTO bookings VALUES (6872969, 3150842, 2050537, 'CM5');
INSERT INTO bookings VALUES (8463042, 9848880, 2201507, 'IM0');
INSERT INTO bookings VALUES (8556204, 5380927, 5818296, 'BO2');
INSERT INTO bookings VALUES (8390811, 6027472, 1099849, 'UD2');
INSERT INTO bookings VALUES (6719655, 9719088, 2616380, 'XI8');
INSERT INTO bookings VALUES (3226670, 5010487, 6432174, 'ZI5');
INSERT INTO bookings VALUES (9897514, 9636664, 2848351, 'FI8');
INSERT INTO bookings VALUES (3095505, 9514557, 1374988, 'IU2');
INSERT INTO bookings VALUES (1947831, 6920459, 1760850, 'BJ4');
INSERT INTO bookings VALUES (2693187, 4219908, 7450816, 'EW2');
INSERT INTO bookings VALUES (7093917, 3233971, 4343069, 'LQ9');
INSERT INTO bookings VALUES (3999564, 6127004, 6096264, 'GY1');
INSERT INTO bookings VALUES (4615800, 1994077, 2011623, 'UO3');
INSERT INTO bookings VALUES (2831053, 3610348, 9462768, 'MP5');
INSERT INTO bookings VALUES (7181951, 2077334, 4715270, 'FL4');
INSERT INTO bookings VALUES (4036119, 3107150, 7611726, 'ZR6');
INSERT INTO bookings VALUES (1936932, 3487415, 5939148, 'LM0');
INSERT INTO bookings VALUES (5110264, 5745431, 5098506, 'VR4');
INSERT INTO bookings VALUES (5454896, 9092559, 8911236, 'GQ9');
INSERT INTO bookings VALUES (5302750, 8308313, 5282783, 'CP0');
INSERT INTO bookings VALUES (7890273, 3700732, 7614747, 'QA0');
INSERT INTO bookings VALUES (5933406, 7786633, 2531988, 'HH9');
INSERT INTO bookings VALUES (5664451, 7383012, 8284228, 'IA3');
INSERT INTO bookings VALUES (9912437, 6080953, 4291858, 'ZB1');
INSERT INTO bookings VALUES (3422249, 6418072, 9056370, 'QW9');
INSERT INTO bookings VALUES (2703927, 1432017, 4684820, 'KH7');
INSERT INTO bookings VALUES (4187077, 9003017, 5637917, 'VV5');
INSERT INTO bookings VALUES (5250963, 2294181, 3928961, 'IO4');
INSERT INTO bookings VALUES (5862207, 7899943, 5089360, 'DY0');
INSERT INTO bookings VALUES (7286733, 9003017, 7938588, 'AC2');
INSERT INTO bookings VALUES (1532292, 1529356, 5975382, 'CI5');
INSERT INTO bookings VALUES (9300501, 8994337, 7991826, 'HR3');
INSERT INTO bookings VALUES (6265089, 2777401, 2011623, 'ZJ6');
INSERT INTO bookings VALUES (4544706, 3797933, 4624699, 'DX3');
INSERT INTO bookings VALUES (8562757, 5328741, 4843875, 'WM4');
INSERT INTO bookings VALUES (5681590, 5380927, 9152758, 'CK5');
INSERT INTO bookings VALUES (5483275, 4530532, 9455454, 'EI7');
INSERT INTO bookings VALUES (8290383, 6124507, 9162740, 'VA9');
INSERT INTO bookings VALUES (4857469, 4219908, 6988118, 'EI0');
INSERT INTO bookings VALUES (2764315, 5721829, 8442580, 'XE7');
INSERT INTO bookings VALUES (5226328, 2696654, 9977745, 'LI1');
INSERT INTO bookings VALUES (3107693, 2077334, 9513965, 'IN0');
INSERT INTO bookings VALUES (4472126, 1010438, 3891092, 'AM4');
INSERT INTO bookings VALUES (9319573, 2228727, 3699725, 'XJ7');
INSERT INTO bookings VALUES (2379184, 3224135, 1624553, 'LJ2');
INSERT INTO bookings VALUES (7719893, 8218370, 1619013, 'AX4');
INSERT INTO bookings VALUES (9542020, 2228727, 2702241, 'QA9');
INSERT INTO bookings VALUES (9859363, 7899943, 9947215, 'QI4');
INSERT INTO bookings VALUES (9690243, 6447803, 5037070, 'MF0');
INSERT INTO bookings VALUES (1633180, 9092559, 4633014, 'BP3');
INSERT INTO bookings VALUES (7602403, 5745431, 6796520, 'PY7');
INSERT INTO bookings VALUES (1308661, 1432212, 3891059, 'QG0');
INSERT INTO bookings VALUES (8227420, 4772680, 1518627, 'BJ3');
INSERT INTO bookings VALUES (9058143, 9728828, 1293870, 'ST9');
INSERT INTO bookings VALUES (2313136, 2481283, 7203850, 'IY9');
INSERT INTO bookings VALUES (1593813, 2084890, 6109460, 'UB4');
INSERT INTO bookings VALUES (2961201, 5317875, 6407111, 'LU5');
INSERT INTO bookings VALUES (1678074, 8515559, 3739269, 'NF6');
INSERT INTO bookings VALUES (8136247, 5213265, 9958280, 'UH4');
INSERT INTO bookings VALUES (3050769, 7968367, 5658325, 'DJ9');
INSERT INTO bookings VALUES (2842357, 9848880, 3234139, 'DW1');
INSERT INTO bookings VALUES (7010852, 2228727, 7571718, 'AF5');
INSERT INTO bookings VALUES (7008332, 7220233, 1430034, 'WM2');
INSERT INTO bookings VALUES (3165277, 4100639, 6775944, 'IG9');
INSERT INTO bookings VALUES (9532476, 5745431, 8670473, 'EL1');
INSERT INTO bookings VALUES (3375903, 4073362, 9455454, 'TG8');
INSERT INTO bookings VALUES (6716729, 7169430, 9517022, 'SZ6');
INSERT INTO bookings VALUES (2680443, 9362387, 9115191, 'KX5');
INSERT INTO bookings VALUES (7357745, 4595619, 6392794, 'XJ1');
INSERT INTO bookings VALUES (2351504, 3487415, 7588961, 'RO5');
INSERT INTO bookings VALUES (1757806, 7169430, 3900173, 'JK8');
INSERT INTO bookings VALUES (5197539, 1960211, 1853749, 'EF7');
INSERT INTO bookings VALUES (2869323, 6373970, 8670473, 'LL0');
INSERT INTO bookings VALUES (2873990, 1529356, 5658325, 'MG8');
INSERT INTO bookings VALUES (1732248, 2911738, 9121478, 'IL4');
INSERT INTO bookings VALUES (6908826, 1960211, 2887013, 'BO2');
INSERT INTO bookings VALUES (4916481, 6920459, 2582302, 'CN0');
INSERT INTO bookings VALUES (4238719, 9719088, 2011623, 'YG2');
INSERT INTO bookings VALUES (9113331, 2084890, 3686836, 'UY0');
INSERT INTO bookings VALUES (7682748, 6236965, 7450816, 'XW0');
INSERT INTO bookings VALUES (5377767, 6920459, 7072581, 'QH8');
INSERT INTO bookings VALUES (3314264, 4595619, 2378137, 'IC6');
INSERT INTO bookings VALUES (4978640, 9003017, 3243026, 'MK2');
INSERT INTO bookings VALUES (8833475, 2965959, 4378471, 'AD9');
INSERT INTO bookings VALUES (2717650, 1668346, 9343423, 'YV8');
INSERT INTO bookings VALUES (3535754, 3797933, 5694564, 'OX3');
INSERT INTO bookings VALUES (5624426, 3822974, 4193470, 'IW3');
INSERT INTO bookings VALUES (9044370, 1072758, 5282783, 'FA3');
INSERT INTO bookings VALUES (8984601, 2777401, 4715270, 'GJ1');
INSERT INTO bookings VALUES (8650575, 7873250, 1293870, 'EH0');
INSERT INTO bookings VALUES (7175871, 4100639, 3977294, 'FB6');
INSERT INTO bookings VALUES (2147415, 8812278, 4795341, 'LT1');
INSERT INTO bookings VALUES (8658873, 2777401, 7588961, 'ED7');
INSERT INTO bookings VALUES (6627102, 7383012, 1487850, 'TU1');
INSERT INTO bookings VALUES (3178558, 5721829, 7611726, 'PG8');
INSERT INTO bookings VALUES (9209331, 7532793, 5818296, 'EC7');
INSERT INTO bookings VALUES (1901452, 8621561, 1751004, 'BQ7');
INSERT INTO bookings VALUES (1662128, 9963495, 4291858, 'RS4');
INSERT INTO bookings VALUES (9923856, 1888680, 4669373, 'WC5');
INSERT INTO bookings VALUES (3154105, 7786633, 7755856, 'MX1');
INSERT INTO bookings VALUES (1968620, 3231298, 2648981, 'NK7');
INSERT INTO bookings VALUES (9800207, 7217433, 3946276, 'HZ4');
INSERT INTO bookings VALUES (5451310, 4246383, 6952130, 'LQ2');
INSERT INTO bookings VALUES (9900238, 9848880, 6775944, 'PR2');
INSERT INTO bookings VALUES (3236623, 7217433, 6746460, 'YY0');
INSERT INTO bookings VALUES (3579905, 4059576, 1290249, 'KW6');
INSERT INTO bookings VALUES (5431269, 6127004, 2067996, 'WF9');
INSERT INTO bookings VALUES (2368655, 3700732, 8865172, 'IO7');
INSERT INTO bookings VALUES (5503219, 3107150, 1751004, 'FL2');
INSERT INTO bookings VALUES (1868312, 8515559, 7470412, 'IS5');
INSERT INTO bookings VALUES (1248456, 1888680, 5635936, 'LR5');
INSERT INTO bookings VALUES (8582013, 8189656, 3731525, 'BV7');
INSERT INTO bookings VALUES (1389979, 3677816, 7450816, 'JO0');
INSERT INTO bookings VALUES (9594950, 1339636, 5818296, 'NR6');
INSERT INTO bookings VALUES (3792401, 6080953, 8393896, 'SU6');
INSERT INTO bookings VALUES (5041240, 7899943, 6233896, 'XT6');
INSERT INTO bookings VALUES (7204967, 7791393, 4999475, 'QQ9');
INSERT INTO bookings VALUES (3943663, 3231298, 9455454, 'ZC3');
INSERT INTO bookings VALUES (7728809, 9243803, 6109460, 'HP9');
INSERT INTO bookings VALUES (4657770, 6124507, 2702241, 'JX2');
INSERT INTO bookings VALUES (8871228, 1888680, 5939148, 'DZ1');
INSERT INTO bookings VALUES (2624417, 2986199, 8741700, 'MC3');
INSERT INTO bookings VALUES (5398963, 3677816, 9142476, 'UA5');
INSERT INTO bookings VALUES (4008721, 9793501, 2861626, 'LS8');
INSERT INTO bookings VALUES (9609632, 7537489, 1209499, 'DL3');
INSERT INTO bookings VALUES (1731082, 1718275, 3203412, 'XD4');
INSERT INTO bookings VALUES (7803146, 4123196, 9152758, 'WI7');
INSERT INTO bookings VALUES (3451260, 5380927, 1267699, 'LD5');
INSERT INTO bookings VALUES (2564401, 5576927, 7899624, 'DH1');
INSERT INTO bookings VALUES (2792797, 1072758, 1890704, 'WY0');
INSERT INTO bookings VALUES (4372779, 9925283, 1277099, 'KC0');
INSERT INTO bookings VALUES (2259747, 6776228, 7768408, 'OW4');
INSERT INTO bookings VALUES (1109729, 4772680, 9455454, 'YU7');
INSERT INTO bookings VALUES (9121363, 6651063, 5694564, 'HX5');
INSERT INTO bookings VALUES (4864622, 5213265, 3541207, 'OB5');
INSERT INTO bookings VALUES (8521337, 3700732, 7393057, 'XP3');
INSERT INTO bookings VALUES (6064693, 3487415, 5431012, 'XU3');
INSERT INTO bookings VALUES (7252564, 1010438, 1347646, 'ID1');
INSERT INTO bookings VALUES (4720672, 7383012, 4647476, 'PQ1');
INSERT INTO bookings VALUES (5238197, 2084890, 3575576, 'MX2');
INSERT INTO bookings VALUES (6374168, 6766644, 5694564, 'XG2');
INSERT INTO bookings VALUES (5554039, 5010487, 1290249, 'JW1');
INSERT INTO bookings VALUES (4352162, 7786633, 2648981, 'ZA0');
INSERT INTO bookings VALUES (7067625, 7563300, 2037973, 'YC8');
INSERT INTO bookings VALUES (6535324, 9636664, 7255476, 'GY8');
INSERT INTO bookings VALUES (1036544, 6124507, 2531963, 'UM1');
INSERT INTO bookings VALUES (3284657, 7791393, 1215045, 'VE9');
INSERT INTO bookings VALUES (2218331, 7786316, 4318596, 'GM4');
INSERT INTO bookings VALUES (3233962, 7791393, 8295690, 'CZ5');
INSERT INTO bookings VALUES (8793765, 7515968, 9462768, 'CV6');
INSERT INTO bookings VALUES (2954465, 8224728, 5262304, 'GB8');
INSERT INTO bookings VALUES (4961976, 7873250, 7571718, 'UC6');
INSERT INTO bookings VALUES (1701192, 1888680, 3790582, 'EK5');
INSERT INTO bookings VALUES (6428264, 7791393, 9916922, 'FK6');
INSERT INTO bookings VALUES (4539858, 9072354, 2557122, 'HC5');
INSERT INTO bookings VALUES (3461672, 9728828, 8226595, 'IU5');
INSERT INTO bookings VALUES (5307731, 7169430, 6861883, 'SC3');
INSERT INTO bookings VALUES (6620913, 2776678, 3747976, 'YL4');
INSERT INTO bookings VALUES (1587102, 5150169, 2603592, 'LK9');
INSERT INTO bookings VALUES (7936822, 2168435, 2489999, 'RR1');
INSERT INTO bookings VALUES (8272382, 6080953, 8780015, 'HZ5');
INSERT INTO bookings VALUES (6393897, 6156293, 2489999, 'BV5');
INSERT INTO bookings VALUES (3056420, 2723241, 9498851, 'YJ2');
INSERT INTO bookings VALUES (9303510, 9381230, 8780015, 'RL7');
INSERT INTO bookings VALUES (3068327, 6774082, 4715270, 'MW7');
INSERT INTO bookings VALUES (4210848, 9848880, 7301468, 'UQ2');
INSERT INTO bookings VALUES (7625760, 6301649, 6710958, 'UZ3');
INSERT INTO bookings VALUES (5847938, 6776228, 9482853, 'SH2');
INSERT INTO bookings VALUES (2914274, 1211134, 2430594, 'PZ1');
INSERT INTO bookings VALUES (4162115, 1116912, 8179773, 'HB5');
INSERT INTO bookings VALUES (7380779, 3307906, 2248186, 'FE7');
INSERT INTO bookings VALUES (3594207, 2077334, 7328294, 'PJ3');
INSERT INTO bookings VALUES (8439787, 6127004, 3070161, 'RH3');
INSERT INTO bookings VALUES (1074994, 9233734, 3506783, 'PV9');
INSERT INTO bookings VALUES (3016306, 9469611, 3172946, 'SK7');
INSERT INTO bookings VALUES (7470490, 1979648, 3790582, 'GI1');
INSERT INTO bookings VALUES (6568737, 9636664, 8886242, 'UK4');
INSERT INTO bookings VALUES (8075935, 6920459, 1518627, 'MD5');
INSERT INTO bookings VALUES (8421927, 1211134, 1668849, 'NN9');
INSERT INTO bookings VALUES (8047880, 2868562, 1536986, 'OF6');
INSERT INTO bookings VALUES (3326133, 5109828, 5109260, 'BK9');
INSERT INTO bookings VALUES (3520916, 2427690, 6251982, 'UN1');
INSERT INTO bookings VALUES (4859088, 3482381, 7947286, 'RR4');
INSERT INTO bookings VALUES (3817252, 1960211, 3284313, 'KN6');
INSERT INTO bookings VALUES (1467706, 5494015, 6086941, 'GE3');
INSERT INTO bookings VALUES (1623781, 9383250, 6990414, 'IH5');
INSERT INTO bookings VALUES (4787491, 2961215, 1607199, 'UH0');
INSERT INTO bookings VALUES (1517845, 5116160, 3575576, 'GA9');
INSERT INTO bookings VALUES (7238602, 6766644, 2603592, 'WL4');
INSERT INTO bookings VALUES (8037895, 7563300, 9889589, 'RQ5');
INSERT INTO bookings VALUES (4008564, 9925283, 2848351, 'NC0');
INSERT INTO bookings VALUES (1907906, 8834504, 2055142, 'ZU6');
INSERT INTO bookings VALUES (5764911, 2934427, 4128390, 'EE0');
INSERT INTO bookings VALUES (7296919, 7563300, 2549316, 'GU4');
INSERT INTO bookings VALUES (6010216, 6124507, 1050646, 'IV2');
INSERT INTO bookings VALUES (8929791, 6180741, 9886698, 'QN0');
INSERT INTO bookings VALUES (7653389, 5520179, 1867373, 'DL1');
INSERT INTO bookings VALUES (4012314, 3986188, 3891092, 'TS4');
INSERT INTO bookings VALUES (3324596, 6027472, 1619013, 'KL6');
INSERT INTO bookings VALUES (9352451, 9226362, 4152237, 'CJ9');
INSERT INTO bookings VALUES (8211288, 6156293, 2887013, 'CD9');
INSERT INTO bookings VALUES (3949560, 3107150, 7140829, 'FR1');
INSERT INTO bookings VALUES (1432793, 2294181, 6777939, 'QP5');
INSERT INTO bookings VALUES (7449605, 1196601, 2340419, 'XW6');
INSERT INTO bookings VALUES (3377609, 2505634, 9796068, 'UW2');
INSERT INTO bookings VALUES (9223238, 9383250, 1751004, 'VH3');
INSERT INTO bookings VALUES (9387579, 4530532, 1299188, 'ZN4');
INSERT INTO bookings VALUES (6301695, 9636664, 4973827, 'GC4');
INSERT INTO bookings VALUES (1384399, 3307906, 3575576, 'SK2');
INSERT INTO bookings VALUES (7768654, 5734972, 1041571, 'LK9');
INSERT INTO bookings VALUES (3132572, 1432212, 8865172, 'GK7');
INSERT INTO bookings VALUES (7787960, 1610270, 9121478, 'TD6');
INSERT INTO bookings VALUES (6377160, 3998210, 4661322, 'HR8');
INSERT INTO bookings VALUES (2254391, 9526705, 2374148, 'OM2');
INSERT INTO bookings VALUES (7048967, 1072758, 2975242, 'GL7');
INSERT INTO bookings VALUES (7589791, 1994077, 2350897, 'TR8');
INSERT INTO bookings VALUES (5511933, 1610270, 7755856, 'GO3');
INSERT INTO bookings VALUES (7701442, 8991792, 1497319, 'CF4');
INSERT INTO bookings VALUES (9457407, 6651063, 7459023, 'RH2');
INSERT INTO bookings VALUES (9863068, 1432017, 2993925, 'CG2');
INSERT INTO bookings VALUES (1478186, 9362387, 6802003, 'FY2');
INSERT INTO bookings VALUES (8354406, 9072354, 5989126, 'KT6');
INSERT INTO bookings VALUES (6854841, 2084890, 7328294, 'JG4');
INSERT INTO bookings VALUES (5932802, 9469611, 9962111, 'DA4');
INSERT INTO bookings VALUES (3595306, 9848880, 2466127, 'PN3');
INSERT INTO bookings VALUES (8673497, 5853975, 1277099, 'QN6');
INSERT INTO bookings VALUES (4189265, 7383012, 1862683, 'QX0');
INSERT INTO bookings VALUES (1309610, 2077334, 7482197, 'NB4');
INSERT INTO bookings VALUES (5858697, 6992003, 4229683, 'WS3');
INSERT INTO bookings VALUES (3005312, 6793309, 9960227, 'TH6');
INSERT INTO bookings VALUES (9921563, 9793501, 4236485, 'UL5');
INSERT INTO bookings VALUES (3925907, 7563300, 2077940, 'YE4');
INSERT INTO bookings VALUES (4528017, 1432017, 5282783, 'WT7');
INSERT INTO bookings VALUES (6785085, 2427690, 8670473, 'HS2');
INSERT INTO bookings VALUES (4872956, 1339636, 8599491, 'GY9');
INSERT INTO bookings VALUES (2568406, 9243803, 1209499, 'OL7');
INSERT INTO bookings VALUES (1182373, 9793501, 5109260, 'ZY8');
INSERT INTO bookings VALUES (2132777, 8155828, 6424106, 'YL5');
INSERT INTO bookings VALUES (5290996, 4123196, 5431012, 'KR4');
INSERT INTO bookings VALUES (6286856, 8994337, 2092479, 'ER2');
INSERT INTO bookings VALUES (6886463, 2965959, 3900173, 'SO0');
INSERT INTO bookings VALUES (7886125, 1072758, 1290249, 'OE0');
INSERT INTO bookings VALUES (8416548, 6329350, 2050537, 'IW7');
INSERT INTO bookings VALUES (7953361, 9383250, 9916922, 'SE3');
INSERT INTO bookings VALUES (1282611, 8409944, 1099849, 'NS5');
INSERT INTO bookings VALUES (8819841, 2161934, 9248389, 'VI0');
INSERT INTO bookings VALUES (2701028, 1988776, 3172946, 'YE7');
INSERT INTO bookings VALUES (9788698, 7635536, 8226595, 'NZ8');
INSERT INTO bookings VALUES (1066466, 6651063, 5282783, 'KC2');
INSERT INTO bookings VALUES (2783563, 7899943, 7328294, 'NP4');
INSERT INTO bookings VALUES (5632501, 6954524, 2011623, 'UM0');
INSERT INTO bookings VALUES (7104185, 3797933, 4152237, 'RJ6');
INSERT INTO bookings VALUES (9298319, 9299869, 7255476, 'TC2');
INSERT INTO bookings VALUES (3094250, 3233971, 7277451, 'AB5');
INSERT INTO bookings VALUES (1372524, 1729245, 1947070, 'AO3');
INSERT INTO bookings VALUES (1848426, 1718275, 5826789, 'YC4');
INSERT INTO bookings VALUES (1262137, 9072354, 2616380, 'MQ7');
INSERT INTO bookings VALUES (1508517, 7968367, 1624553, 'VE7');
INSERT INTO bookings VALUES (9786264, 7635536, 8650087, 'HS6');
INSERT INTO bookings VALUES (8749003, 1597896, 3284313, 'KH9');
INSERT INTO bookings VALUES (7376714, 7791393, 5989126, 'VQ4');
INSERT INTO bookings VALUES (4729305, 3150842, 7768408, 'RU2');
INSERT INTO bookings VALUES (1299400, 5116160, 1607199, 'UO4');
INSERT INTO bookings VALUES (8385087, 5328741, 1487850, 'CE8');
INSERT INTO bookings VALUES (4300350, 9381230, 7588961, 'RU5');
INSERT INTO bookings VALUES (2348979, 7791393, 1947070, 'TU5');
INSERT INTO bookings VALUES (8231218, 1116912, 8817486, 'FM1');
INSERT INTO bookings VALUES (7956642, 8189656, 3511197, 'FU5');
INSERT INTO bookings VALUES (1678049, 3610348, 7301468, 'TJ6');
INSERT INTO bookings VALUES (2579214, 9925283, 2677916, 'MM4');
INSERT INTO bookings VALUES (4063257, 3677816, 2281058, 'VX5');
INSERT INTO bookings VALUES (1684189, 8409944, 9108899, 'AD8');
INSERT INTO bookings VALUES (9082648, 1010438, 7459023, 'RY3');
INSERT INTO bookings VALUES (9801730, 9072354, 3245038, 'EC4');
INSERT INTO bookings VALUES (2031560, 5328741, 2877495, 'UJ0');
INSERT INTO bookings VALUES (7672443, 3797933, 5784607, 'GV1');
INSERT INTO bookings VALUES (8808690, 5131170, 9304895, 'IC0');
INSERT INTO bookings VALUES (1425174, 9514557, 3172946, 'CX1');
INSERT INTO bookings VALUES (6620412, 4059576, 5841139, 'LO6');
INSERT INTO bookings VALUES (5870313, 8994337, 5273219, 'YR9');
INSERT INTO bookings VALUES (2384364, 9514557, 7778473, 'MA5');
INSERT INTO bookings VALUES (1951295, 1163615, 2092479, 'GS2');
INSERT INTO bookings VALUES (6239727, 6820491, 6086941, 'AB4');
INSERT INTO bookings VALUES (5059358, 7173601, 7991826, 'DJ1');
INSERT INTO bookings VALUES (5669521, 2934427, 2914075, 'HM8');
INSERT INTO bookings VALUES (9573827, 8991792, 4915229, 'WM5');
INSERT INTO bookings VALUES (4939990, 1729245, 5751130, 'PV1');
INSERT INTO bookings VALUES (9323560, 6329350, 8343974, 'VY1');
INSERT INTO bookings VALUES (4545850, 6405551, 2582302, 'SG2');
INSERT INTO bookings VALUES (8332529, 3487415, 6109460, 'ZD4');
INSERT INTO bookings VALUES (8700875, 3307906, 3491018, 'PE8');
INSERT INTO bookings VALUES (8799279, 5380927, 7393057, 'ZH6');
INSERT INTO bookings VALUES (8068393, 5328741, 7128137, 'VF8');
INSERT INTO bookings VALUES (2044101, 7496042, 5694564, 'UB8');
INSERT INTO bookings VALUES (2358908, 1960211, 4460972, 'GI2');
INSERT INTO bookings VALUES (5197731, 9925283, 5037070, 'EK4');
INSERT INTO bookings VALUES (9862436, 2965959, 5986471, 'YG9');
INSERT INTO bookings VALUES (8472292, 6197631, 2914075, 'PP5');
INSERT INTO bookings VALUES (6557492, 3307906, 5216329, 'YQ8');
INSERT INTO bookings VALUES (2272649, 9027045, 9298883, 'OA8');
INSERT INTO bookings VALUES (6261780, 9312273, 1607199, 'VQ6');
INSERT INTO bookings VALUES (2947666, 2986199, 2378137, 'OK1');
INSERT INTO bookings VALUES (3132795, 6373970, 2677916, 'LI5');
INSERT INTO bookings VALUES (9487529, 5328741, 1890704, 'PM1');
INSERT INTO bookings VALUES (7565191, 7791393, 3495148, 'ZM7');
INSERT INTO bookings VALUES (8110051, 9514557, 4448063, 'VJ8');
INSERT INTO bookings VALUES (5761338, 6418072, 7342356, 'ZN5');
INSERT INTO bookings VALUES (8583895, 1328187, 8886242, 'ED7');
INSERT INTO bookings VALUES (6424569, 9793501, 1240763, 'XV2');
INSERT INTO bookings VALUES (3061202, 7220233, 1536986, 'AZ5');
INSERT INTO bookings VALUES (8832147, 5150169, 3262281, 'OB0');
INSERT INTO bookings VALUES (4506688, 9868867, 5037070, 'SB3');
INSERT INTO bookings VALUES (8968179, 9218001, 7203850, 'PU1');
INSERT INTO bookings VALUES (9763574, 6329350, 3967989, 'UX6');
INSERT INTO bookings VALUES (5354806, 5317875, 4378471, 'PT1');
INSERT INTO bookings VALUES (4333078, 9963495, 2077940, 'WV8');
INSERT INTO bookings VALUES (5077999, 9312273, 5431012, 'RZ8');
INSERT INTO bookings VALUES (9661014, 8994337, 5660582, 'BS4');
INSERT INTO bookings VALUES (6394448, 4059576, 5751130, 'SK9');
INSERT INTO bookings VALUES (9955288, 6405551, 1347646, 'JA3');
INSERT INTO bookings VALUES (8568894, 9226362, 8599491, 'SQ8');
INSERT INTO bookings VALUES (4386814, 3231298, 7755856, 'HB2');
INSERT INTO bookings VALUES (8230145, 1484905, 6638170, 'BZ8');
INSERT INTO bookings VALUES (7339840, 5526936, 4624699, 'ZF3');
INSERT INTO bookings VALUES (2987692, 5109828, 8393896, 'YE7');
INSERT INTO bookings VALUES (8937043, 3487415, 5833403, 'GB8');
INSERT INTO bookings VALUES (4012596, 7496042, 9162740, 'KX3');
INSERT INTO bookings VALUES (1127504, 9469611, 6093066, 'AB7');
INSERT INTO bookings VALUES (9932521, 9538849, 7459023, 'OM6');
INSERT INTO bookings VALUES (8709181, 9992742, 5989126, 'NA6');
INSERT INTO bookings VALUES (3415052, 9218001, 6432174, 'WT4');
INSERT INTO bookings VALUES (9257451, 1610270, 2848351, 'CH7');
INSERT INTO bookings VALUES (9009625, 2868562, 1832357, 'UP7');
INSERT INTO bookings VALUES (1337378, 5380927, 1853749, 'RY0');
INSERT INTO bookings VALUES (9626498, 6793309, 3989140, 'EF9');
INSERT INTO bookings VALUES (2695400, 8423344, 3416955, 'EC5');
INSERT INTO bookings VALUES (5577893, 9848880, 7627273, 'OA8');
INSERT INTO bookings VALUES (8180107, 6776228, 6999905, 'HX1');
INSERT INTO bookings VALUES (2645318, 6352080, 1846299, 'QA2');
INSERT INTO bookings VALUES (4398930, 6766644, 6323776, 'BB6');
INSERT INTO bookings VALUES (9538785, 8155828, 9153537, 'PI3');
INSERT INTO bookings VALUES (4305137, 4059576, 6757414, 'CS3');
INSERT INTO bookings VALUES (4105945, 2153952, 7144851, 'VO5');
INSERT INTO bookings VALUES (9124371, 7383012, 4982256, 'WS5');
INSERT INTO bookings VALUES (2761510, 4246383, 8876058, 'QD0');
INSERT INTO bookings VALUES (2750638, 5109828, 5218698, 'AY0');
INSERT INTO bookings VALUES (7661075, 8027778, 2616380, 'ZW5');
INSERT INTO bookings VALUES (7795633, 5520179, 5660582, 'KY9');
INSERT INTO bookings VALUES (3213297, 7899943, 1624553, 'DR9');
INSERT INTO bookings VALUES (4126049, 5931547, 1639958, 'MZ7');
INSERT INTO bookings VALUES (5787044, 2162029, 6802003, 'VC6');
INSERT INTO bookings VALUES (7199382, 2959304, 1950026, 'DL3');
INSERT INTO bookings VALUES (7970883, 6651063, 8911236, 'VG8');
INSERT INTO bookings VALUES (9863466, 5116160, 5645246, 'XH3');
INSERT INTO bookings VALUES (4034235, 9469611, 7938588, 'YW1');
INSERT INTO bookings VALUES (3637382, 9719088, 7168799, 'YS2');
INSERT INTO bookings VALUES (6646700, 2153952, 8066489, 'FQ9');
INSERT INTO bookings VALUES (4843602, 3482381, 3491018, 'ZV0');
INSERT INTO bookings VALUES (7561579, 9331874, 2949063, 'AM6');
INSERT INTO bookings VALUES (5964236, 4059576, 5762438, 'CK8');
INSERT INTO bookings VALUES (8257366, 9112089, 9218215, 'KC4');
INSERT INTO bookings VALUES (6361531, 8027778, 1607199, 'LG6');
INSERT INTO bookings VALUES (2012674, 8515559, 4144513, 'JC7');
INSERT INTO bookings VALUES (8553669, 9728828, 2350897, 'QB0');
INSERT INTO bookings VALUES (6315863, 2162029, 8607388, 'DI7');
INSERT INTO bookings VALUES (4212767, 1960211, 2616380, 'ES4');
INSERT INTO bookings VALUES (8701300, 9514557, 5658325, 'ZI9');
INSERT INTO bookings VALUES (4925346, 5213265, 7482197, 'IC0');
INSERT INTO bookings VALUES (3824887, 1610270, 7128137, 'HY2');
INSERT INTO bookings VALUES (9511613, 9243803, 6461218, 'UG6');
INSERT INTO bookings VALUES (2202618, 7873250, 2751370, 'AL9');
INSERT INTO bookings VALUES (2506033, 9728828, 9886698, 'ZV6');
INSERT INTO bookings VALUES (3366919, 3700732, 8670473, 'RF7');
INSERT INTO bookings VALUES (6132404, 8621561, 9637487, 'MM4');
INSERT INTO bookings VALUES (1302224, 8994337, 5762438, 'BM4');
INSERT INTO bookings VALUES (7247048, 5465007, 8040248, 'XF2');
INSERT INTO bookings VALUES (7048130, 6197631, 8427554, 'MB1');
INSERT INTO bookings VALUES (7364876, 6027472, 8126220, 'HA4');
INSERT INTO bookings VALUES (3551594, 1083645, 1758953, 'YZ5');
INSERT INTO bookings VALUES (9334615, 1010438, 2092479, 'ID4');
INSERT INTO bookings VALUES (1782369, 8042175, 5762438, 'WW3');
INSERT INTO bookings VALUES (8587405, 5150169, 8280938, 'VG5');
INSERT INTO bookings VALUES (8019586, 3307906, 9056370, 'ZE2');
INSERT INTO bookings VALUES (4206189, 5745431, 7571718, 'MD9');
INSERT INTO bookings VALUES (3930094, 7220233, 9011552, 'BG8');
INSERT INTO bookings VALUES (7844689, 2505634, 1099849, 'VQ2');
INSERT INTO bookings VALUES (4960922, 2168435, 8841346, 'MJ0');
INSERT INTO bookings VALUES (3932363, 9072354, 8886242, 'AF1');
INSERT INTO bookings VALUES (3935580, 5465007, 7450816, 'UC0');
INSERT INTO bookings VALUES (9236212, 1500606, 7991826, 'YT5');
INSERT INTO bookings VALUES (3807440, 3233971, 3349832, 'PU3');
INSERT INTO bookings VALUES (4778792, 5010487, 4949024, 'CV9');
INSERT INTO bookings VALUES (3217759, 4123196, 9482853, 'JW9');
INSERT INTO bookings VALUES (4197992, 2470259, 2092479, 'LM0');
INSERT INTO bookings VALUES (5211411, 8173911, 2489999, 'VQ5');
INSERT INTO bookings VALUES (5800268, 8991792, 8180075, 'AG8');
INSERT INTO bookings VALUES (2282988, 4772680, 1558964, 'AX9');
INSERT INTO bookings VALUES (8081512, 6954524, 1832357, 'TY4');
INSERT INTO bookings VALUES (1782627, 6776228, 9916922, 'LJ4');
INSERT INTO bookings VALUES (6419364, 9299869, 8180075, 'IG4');
INSERT INTO bookings VALUES (5901056, 9793501, 1867373, 'DC9');
INSERT INTO bookings VALUES (8239156, 1484905, 5637917, 'LL6');
INSERT INTO bookings VALUES (3686549, 1072758, 9152758, 'CU2');
INSERT INTO bookings VALUES (9025546, 7899943, 8778991, 'ID7');
INSERT INTO bookings VALUES (8450214, 1988776, 3967989, 'KQ6');
INSERT INTO bookings VALUES (8178330, 9925283, 9121478, 'FL8');
INSERT INTO bookings VALUES (8794425, 8027778, 9212933, 'DB7');
INSERT INTO bookings VALUES (5334196, 5526936, 1758953, 'TI5');
INSERT INTO bookings VALUES (2370489, 1718275, 2751370, 'PB0');
INSERT INTO bookings VALUES (5901712, 7031540, 9304895, 'OI9');
INSERT INTO bookings VALUES (8439052, 2407452, 1862683, 'JS4');
INSERT INTO bookings VALUES (4156177, 9381230, 9724633, 'ZP7');
INSERT INTO bookings VALUES (1332121, 5576927, 2374148, 'VJ4');
INSERT INTO bookings VALUES (1649816, 3354650, 1050646, 'IB8');
INSERT INTO bookings VALUES (5653660, 9092559, 7613367, 'EV3');
INSERT INTO bookings VALUES (4047157, 2696654, 2466127, 'QR7');
INSERT INTO bookings VALUES (3645895, 9003017, 6881673, 'EQ9');
INSERT INTO bookings VALUES (2733791, 4453764, 5109260, 'NB2');
INSERT INTO bookings VALUES (7871858, 7217433, 1518627, 'VN3');
INSERT INTO bookings VALUES (3137140, 6766644, 8164721, 'MT5');
INSERT INTO bookings VALUES (3940539, 9868867, 1497319, 'NM4');
INSERT INTO bookings VALUES (2656242, 9514557, 6445311, 'LN0');
INSERT INTO bookings VALUES (7074356, 7169430, 7358717, 'KR6');
INSERT INTO bookings VALUES (4075671, 2777401, 3941019, 'KF1');
INSERT INTO bookings VALUES (4605003, 7173601, 2430594, 'TO9');
INSERT INTO bookings VALUES (4264342, 3986188, 4633014, 'YX3');
INSERT INTO bookings VALUES (8428829, 1597896, 2369537, 'GX5');
INSERT INTO bookings VALUES (4568343, 1668346, 9796068, 'UT8');
INSERT INTO bookings VALUES (6276151, 4530532, 6775944, 'AV3');
INSERT INTO bookings VALUES (4291922, 5150169, 2887013, 'TP1');
INSERT INTO bookings VALUES (9548914, 1196601, 5216329, 'VN8');
INSERT INTO bookings VALUES (2983965, 7217433, 8226595, 'RF6');
INSERT INTO bookings VALUES (6853677, 6245743, 3495148, 'YN3');
INSERT INTO bookings VALUES (9414542, 8027778, 7144851, 'IS1');
INSERT INTO bookings VALUES (6892531, 2162029, 4911891, 'RN2');
INSERT INTO bookings VALUES (6001708, 4797001, 4684820, 'BI5');
INSERT INTO bookings VALUES (6684946, 6197631, 8599491, 'MW5');
INSERT INTO bookings VALUES (5748609, 7173601, 1751004, 'NK2');
INSERT INTO bookings VALUES (4758320, 7532793, 3897785, 'GR7');
INSERT INTO bookings VALUES (7174126, 1010438, 7255476, 'WZ1');
INSERT INTO bookings VALUES (6222472, 8308313, 8670473, 'TQ2');
INSERT INTO bookings VALUES (4810034, 2077334, 2738928, 'SB6');
INSERT INTO bookings VALUES (3223081, 2858315, 6109460, 'KB6');
INSERT INTO bookings VALUES (5782227, 2777401, 6406136, 'AS7');
INSERT INTO bookings VALUES (6163337, 2470259, 5098506, 'XF0');
INSERT INTO bookings VALUES (2739438, 8423344, 9513965, 'FA5');
INSERT INTO bookings VALUES (6193857, 5745431, 4610651, 'UV8');
INSERT INTO bookings VALUES (2058234, 6992003, 9724633, 'AZ1');
INSERT INTO bookings VALUES (8379974, 1729245, 7072581, 'CA5');
INSERT INTO bookings VALUES (6479415, 2407452, 5939148, 'YT4');
INSERT INTO bookings VALUES (4991658, 7791393, 6039117, 'WD1');
INSERT INTO bookings VALUES (6814766, 8218370, 4949024, 'AS3');
INSERT INTO bookings VALUES (6713902, 7403311, 8066489, 'QH1');
INSERT INTO bookings VALUES (5962128, 5931547, 4236485, 'FC0');

-- =========== skymill.loyalty_earnings (generic) ==========

DROP TABLE IF EXISTS loyalty_earnings;

CREATE TABLE loyalty_earnings (
  id INT NOT NULL,
  booking_id INT NOT NULL,
  passenger_id INT NOT NULL,
  miles_earned INT NOT NULL,
  earning_date DATE NOT NULL
);

INSERT INTO loyalty_earnings VALUES (2382259, 2693187, 7635536, 3652, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (5780512, 8012149, 6992003, 3309, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (6993325, 2278269, 9003017, 2955, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (4650917, 4961976, 9003017, 2311, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (4347180, 9684543, 6373970, 607, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (9885452, 5932802, 2294181, 811, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (1068608, 7796733, 4219908, 2906, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (3243332, 9352451, 2481283, 2704, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (5622226, 8749003, 2470259, 788, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (1656234, 1732248, 6766644, 4307, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (3157233, 7701442, 8224728, 1582, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (5375509, 3804691, 1083645, 3715, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (1400232, 5577893, 2776678, 1161, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (6461423, 7699059, 1211134, 4280, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (9039574, 7339840, 2959304, 2921, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (7880231, 7886125, 5931547, 927, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (2459133, 5040756, 4772680, 3352, '2026-01-07');
INSERT INTO loyalty_earnings VALUES (1111519, 4528017, 2934427, 4563, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (5955686, 9632599, 8812278, 3924, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (5820572, 5847938, 7496042, 4497, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (5803429, 4888508, 2911738, 946, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (9511257, 5451310, 2911738, 1742, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (2899049, 9706022, 1083645, 4603, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (8150528, 9594950, 2228727, 2879, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (5913300, 6129697, 1741094, 4921, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (1723413, 2653056, 3482381, 1421, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (5989084, 5068176, 6776228, 2869, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (8799189, 9695794, 9299869, 4961, '2026-01-29');
INSERT INTO loyalty_earnings VALUES (3253079, 5984309, 7031540, 1395, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (7979988, 2044101, 9226362, 4102, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (7299949, 6286856, 3603058, 1327, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (6253230, 9257451, 2427690, 1162, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (3901110, 9304185, 9243803, 736, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (6588925, 4305137, 8515559, 4015, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (3398029, 5870313, 5116160, 2372, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (9530913, 1478186, 2911738, 1901, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (5296976, 4216040, 9868867, 4282, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (6026424, 3939910, 4772680, 4794, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (6244257, 8260979, 3150842, 1999, '2026-02-01');
INSERT INTO loyalty_earnings VALUES (2652697, 2680443, 4772680, 4455, '2026-03-27');
INSERT INTO loyalty_earnings VALUES (5136737, 2358908, 4662730, 841, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (1370112, 4857263, 1154532, 3021, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (1247719, 8662185, 6027472, 2636, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (3575208, 7768654, 2961215, 3247, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (1901993, 3377609, 2868562, 4748, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (7078262, 1145922, 4772680, 2084, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (5108963, 4187077, 9963495, 4300, '2026-02-01');
INSERT INTO loyalty_earnings VALUES (2479885, 7589791, 3307906, 2938, '2026-01-07');
INSERT INTO loyalty_earnings VALUES (2485970, 8690706, 2162029, 920, '2026-05-10');
INSERT INTO loyalty_earnings VALUES (8830295, 4398930, 7899943, 2435, '2026-04-07');
INSERT INTO loyalty_earnings VALUES (4668581, 5810065, 7563300, 3117, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (7950584, 2691695, 7791393, 1101, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (4701502, 7013992, 5116160, 1597, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (4782874, 5051841, 1610270, 2748, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (1134430, 6010216, 7873250, 3784, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (4944122, 8166452, 7220233, 4295, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (8011010, 3683955, 4491384, 2967, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (4560668, 5933406, 6418072, 3991, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (2518529, 6574159, 6447803, 4167, '2026-04-04');
INSERT INTO loyalty_earnings VALUES (1924745, 5664451, 1668346, 1029, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (2049893, 9111468, 1484905, 3161, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (7560647, 1538571, 6156293, 2788, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (3672165, 9788698, 2481283, 4190, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (9260560, 8818406, 9312273, 2492, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (9885767, 2464334, 4530854, 4856, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (1198728, 2961201, 7968367, 1378, '2026-04-14');
INSERT INTO loyalty_earnings VALUES (6719693, 5901712, 1741094, 3752, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (4161366, 3377609, 6793309, 4411, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (8630144, 1282611, 1888680, 1032, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (3495233, 8231218, 6405551, 2213, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (3706160, 8521337, 7217433, 593, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (7184585, 3956391, 9538849, 899, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (6428532, 7692330, 7532793, 3139, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (6347756, 2421272, 9003017, 2859, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (7611514, 6315863, 8409944, 1210, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (4985502, 5764911, 2168435, 4657, '2026-02-06');
INSERT INTO loyalty_earnings VALUES (8776889, 8311532, 3487415, 2760, '2026-03-30');
INSERT INTO loyalty_earnings VALUES (9342918, 5107470, 5682977, 4115, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (2412348, 9156560, 5745431, 2976, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (3471660, 2764315, 9092559, 744, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (3001301, 9771071, 8027778, 3087, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (5324709, 1116067, 3107150, 4988, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (3150911, 5632501, 7031540, 573, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (6834671, 4811052, 6197631, 2680, '2026-04-19');
INSERT INTO loyalty_earnings VALUES (5808584, 7470490, 1597896, 4701, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (4152697, 2541475, 3150842, 4159, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (3805029, 2042893, 3454898, 2803, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (4900207, 9431928, 5150169, 1996, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (6304727, 8416548, 2505634, 1277, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (5933752, 3645895, 3603058, 4255, '2026-04-13');
INSERT INTO loyalty_earnings VALUES (9779038, 6620913, 3107150, 2251, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (9588920, 2717650, 2168435, 2084, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (7308664, 7199382, 9793501, 3751, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (3448610, 7126961, 1960211, 2431, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (5507406, 7302640, 6373970, 1963, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (8293313, 1127504, 5682977, 1551, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (8196216, 9661014, 7173601, 2436, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (5528043, 8446690, 9003017, 3148, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (5104550, 6646700, 4530532, 1445, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (1691315, 1099646, 3700732, 521, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (5058937, 2947666, 3797933, 1084, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (9058214, 4344113, 6776228, 825, '2026-01-05');
INSERT INTO loyalty_earnings VALUES (7725683, 3422249, 7968367, 2430, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (6986667, 3314264, 2961215, 4038, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (7345193, 9923856, 4892788, 673, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (1411280, 3551594, 1163615, 3097, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (6218943, 3986898, 9514557, 1373, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (1924569, 6886730, 8027778, 3210, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (1732715, 1992961, 3454898, 2029, '2026-03-30');
INSERT INTO loyalty_earnings VALUES (6327349, 8385087, 8409944, 2621, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (2566359, 5632501, 1979648, 1895, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (7272302, 2030092, 7532793, 2747, '2026-04-13');
INSERT INTO loyalty_earnings VALUES (1872518, 3010495, 9719088, 2573, '2026-01-25');
INSERT INTO loyalty_earnings VALUES (6651410, 7771381, 2957004, 3321, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (1613546, 9862436, 2228727, 996, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (2900310, 1992961, 1931461, 1147, '2026-03-30');
INSERT INTO loyalty_earnings VALUES (4047601, 1864300, 5380927, 757, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (3738430, 3314264, 8812278, 2413, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (5126979, 9764765, 2227634, 4322, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (5260474, 4463303, 2965959, 4857, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (3252062, 6106392, 9362387, 3825, '2026-04-19');
INSERT INTO loyalty_earnings VALUES (1604978, 8140881, 2481283, 2493, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (9067347, 1532292, 6651063, 4147, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (4976746, 5748609, 3107150, 1325, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (1004451, 2680443, 6383237, 2626, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (8343424, 6854841, 8515559, 4507, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (7143569, 8518583, 3959460, 4501, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (8346786, 2698721, 6939753, 2027, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (5687462, 4197992, 6027472, 3016, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (4945612, 4398930, 6124507, 2295, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (9157680, 2956214, 1979648, 891, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (3960290, 1303227, 2077334, 535, '2026-04-01');
INSERT INTO loyalty_earnings VALUES (2229558, 7589791, 4530854, 1586, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (1519134, 1478186, 2227634, 3407, '2026-01-28');
INSERT INTO loyalty_earnings VALUES (5320537, 4528017, 2427690, 3542, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (7865458, 6872969, 2407452, 3981, '2026-01-28');
INSERT INTO loyalty_earnings VALUES (1745008, 9923856, 9218001, 2182, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (2254264, 1432793, 6127004, 2194, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (6250890, 6064693, 8218370, 1935, '2026-02-12');
INSERT INTO loyalty_earnings VALUES (8096214, 3932363, 1729245, 1365, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (3492227, 8837629, 4332111, 4593, '2026-04-28');
INSERT INTO loyalty_earnings VALUES (4823831, 8450214, 9992742, 3749, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (8280753, 3983543, 3959460, 4765, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (5423151, 6283542, 6373970, 1203, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (9842932, 2313136, 9992742, 3067, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (3946667, 6064693, 1960211, 4625, '2026-05-10');
INSERT INTO loyalty_earnings VALUES (2527806, 3451260, 1741094, 2197, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (2946742, 6085006, 2776678, 781, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (2194962, 3137140, 9299869, 2709, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (9415061, 5211411, 2986199, 2663, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (8842673, 6336261, 5526936, 1749, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (4487486, 3094228, 2294181, 943, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (2684500, 8494942, 3307906, 3629, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (1655162, 2831053, 9362387, 1397, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (8307387, 5669521, 9218001, 2606, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (2745668, 2376505, 4469259, 4799, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (3183294, 1182373, 2723241, 2667, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (6812989, 6442671, 5131170, 3495, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (9133287, 1074994, 8042175, 2548, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (2038158, 5077999, 2427690, 3576, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (4314685, 5053175, 7537489, 2968, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (1923492, 9594950, 9092559, 3785, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (2443795, 8272382, 5494015, 3197, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (1255728, 4615800, 5931547, 812, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (3114942, 5677062, 1668346, 644, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (4606803, 8385087, 1500606, 3130, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (4181762, 7784692, 1072758, 645, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (3784747, 3056420, 1718275, 2401, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (2128937, 4212767, 2077334, 4517, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (9938708, 7074356, 9963495, 3074, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (3178615, 1701192, 6245743, 867, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (6390453, 2848794, 6027472, 626, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (3818474, 3132795, 9992742, 1646, '2026-04-15');
INSERT INTO loyalty_earnings VALUES (1350025, 9156560, 7873250, 4063, '2026-04-07');
INSERT INTO loyalty_earnings VALUES (8789483, 7653389, 5734972, 2643, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (2832667, 1399338, 5931547, 990, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (2168588, 2341251, 2776678, 3417, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (9483124, 8230145, 1729245, 2787, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (7866402, 4291922, 2153952, 2583, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (9902310, 7561579, 6080953, 4198, '2026-01-07');
INSERT INTO loyalty_earnings VALUES (3459735, 6684946, 1196601, 1956, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (5097224, 8075935, 8155828, 3124, '2026-03-27');
INSERT INTO loyalty_earnings VALUES (9441379, 3003674, 2868562, 1761, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (3002082, 4495174, 6405551, 1704, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (3186876, 1386972, 8948057, 1539, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (3442835, 5624426, 5150169, 4648, '2026-04-28');
INSERT INTO loyalty_earnings VALUES (7571219, 3709835, 4998276, 3475, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (3138165, 2376505, 7217433, 2965, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (1441089, 2202618, 5131170, 3878, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (5227774, 1517845, 3998210, 1678, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (4611212, 2313136, 1597896, 2242, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (2775811, 7178317, 7786633, 2574, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (4262985, 7756506, 8155828, 2200, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (4472151, 7199382, 6124507, 3610, '2026-01-29');
INSERT INTO loyalty_earnings VALUES (1108769, 1338051, 4662730, 2902, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (5066225, 2842357, 2986199, 3031, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (6350399, 6176256, 1931461, 4431, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (8522597, 3686549, 7786316, 4344, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (8488990, 7212597, 8189656, 928, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (1543610, 4194541, 8994337, 1745, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (5849278, 2539748, 1597896, 2216, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (5362857, 6646700, 9636664, 3733, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (3572606, 5353054, 6127004, 4164, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (9926070, 2506033, 3891137, 4412, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (8715500, 6758719, 8515559, 2642, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (6403012, 5068176, 3797933, 4163, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (4289714, 7692330, 8155828, 1575, '2026-02-06');
INSERT INTO loyalty_earnings VALUES (7999998, 8987248, 6774082, 3304, '2026-03-27');
INSERT INTO loyalty_earnings VALUES (3427696, 3324596, 2162029, 859, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (5037714, 5440509, 5931547, 4081, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (4724312, 7680566, 5931547, 4030, '2026-04-01');
INSERT INTO loyalty_earnings VALUES (8535525, 8110051, 8409944, 4093, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (1972514, 3785046, 6301649, 4698, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (8459351, 3643982, 9925283, 3565, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (8408764, 1914249, 5734972, 3705, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (6092802, 2675574, 7169430, 1970, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (9081495, 2507666, 8155828, 3720, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (3486946, 7489303, 3487415, 2626, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (5287162, 2390558, 3224135, 3161, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (9337206, 7178690, 6766644, 2236, '2026-03-29');
INSERT INTO loyalty_earnings VALUES (6176046, 7764598, 9526705, 1036, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (6357038, 2613937, 4332111, 3262, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (3765869, 8950869, 9469611, 807, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (1055373, 7996341, 2696654, 2430, '2026-04-15');
INSERT INTO loyalty_earnings VALUES (9500393, 6754234, 4797001, 1371, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (8647770, 5440509, 6156293, 4921, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (3627768, 6001132, 9218001, 991, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (3650395, 4703264, 2168435, 4135, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (2710495, 8334716, 2153952, 4864, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (1079798, 2113573, 6766644, 3958, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (2726694, 9577941, 5010487, 2958, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (1550640, 8208173, 2723241, 1899, '2026-01-28');
INSERT INTO loyalty_earnings VALUES (2767664, 7124957, 7786633, 4291, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (4414278, 8421927, 9072354, 3123, '2026-04-14');
INSERT INTO loyalty_earnings VALUES (7372011, 4888508, 9299869, 4407, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (1543883, 4857263, 7220233, 1810, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (2832828, 3956391, 1264848, 1021, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (2496354, 8501450, 3603058, 3182, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (6602419, 3107693, 6373970, 1323, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (6358604, 4237055, 5721829, 3796, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (9955090, 8416548, 6352080, 1477, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (1359380, 8476594, 6954524, 2224, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (1188464, 9511613, 6992003, 770, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (7143200, 6908826, 6651063, 2314, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (2573254, 5592345, 2911738, 2424, '2026-01-09');
INSERT INTO loyalty_earnings VALUES (2032230, 4262048, 1668346, 1850, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (3666432, 5398963, 1500606, 4478, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (9503055, 1127504, 2407452, 2253, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (7361122, 9900238, 7635536, 3983, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (8131740, 8760021, 6301649, 3332, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (9925792, 6286856, 7563300, 4290, '2026-02-26');
INSERT INTO loyalty_earnings VALUES (5927311, 9548914, 5010487, 2410, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (8720111, 2693187, 1718275, 1117, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (6733139, 4669281, 9092559, 4519, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (9110095, 5964236, 5150169, 2789, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (8889045, 3986898, 1892068, 4413, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (1463856, 8871228, 4059576, 4159, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (7158301, 1338051, 3454898, 2481, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (2368584, 4719837, 8812278, 4834, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (2074054, 7820234, 4073362, 4276, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (5174581, 5962128, 4219908, 589, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (5390436, 3061202, 6373970, 2221, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (6371550, 7167852, 1610270, 4207, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (4732673, 3326133, 6939753, 1977, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (8217743, 6366140, 6111066, 1867, '2026-01-28');
INSERT INTO loyalty_earnings VALUES (5149346, 5870313, 3603058, 3283, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (3990578, 4283104, 6127004, 3475, '2026-04-15');
INSERT INTO loyalty_earnings VALUES (6965427, 3743456, 9381230, 2040, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (6630839, 7174126, 3822974, 4044, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (5650481, 2641572, 4246383, 1209, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (1293438, 4872956, 6954524, 2001, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (7574046, 8673497, 9383250, 3987, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (5075405, 7364876, 7169430, 3477, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (2804449, 3451260, 4892788, 1014, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (8092163, 9659204, 3107150, 1473, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (7727982, 4810034, 2777401, 4082, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (9878386, 3219580, 7173601, 2685, '2026-04-14');
INSERT INTO loyalty_earnings VALUES (3316253, 8947967, 3224135, 3000, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (2044214, 3219580, 5494015, 747, '2026-03-29');
INSERT INTO loyalty_earnings VALUES (3325336, 6814766, 5494015, 516, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (3264646, 3932363, 9992742, 2220, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (4080780, 5933406, 5380927, 3674, '2026-01-17');
INSERT INTO loyalty_earnings VALUES (6596910, 2733791, 4073362, 755, '2026-01-02');
INSERT INTO loyalty_earnings VALUES (8629923, 7167852, 4998276, 4263, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (9224362, 9306204, 7899943, 4262, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (5171507, 9125675, 5116160, 3691, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (9093055, 3072277, 5494015, 1929, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (3457329, 7449605, 8812278, 3346, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (1149187, 8947967, 7031540, 4970, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (4634529, 8166452, 5317875, 2056, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (1581786, 2845499, 1888680, 1598, '2026-04-04');
INSERT INTO loyalty_earnings VALUES (9230916, 7699059, 9072354, 1934, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (1587591, 3423997, 2153952, 1118, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (9243433, 7238602, 3231298, 3442, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (4283238, 1145922, 5734972, 807, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (1511015, 4216040, 1010438, 4807, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (9713071, 8290383, 6954524, 2710, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (2649613, 6557492, 1432212, 4675, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (7665621, 9058143, 6197631, 846, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (4785239, 9334615, 8834504, 3008, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (6504871, 3219580, 3150842, 4443, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (3810009, 5482591, 8991792, 3528, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (2002247, 3165277, 9312273, 1269, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (5903785, 7339840, 1888680, 502, '2026-01-28');
INSERT INTO loyalty_earnings VALUES (3685154, 3683955, 7786316, 4881, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (5702779, 7013992, 2228727, 4402, '2026-04-11');
INSERT INTO loyalty_earnings VALUES (2030375, 5852557, 1597896, 3803, '2026-03-03');
INSERT INTO loyalty_earnings VALUES (2114904, 9843188, 9003017, 1763, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (7035268, 6646663, 5131170, 3901, '2026-03-21');
INSERT INTO loyalty_earnings VALUES (3184201, 9421408, 5213265, 776, '2026-04-28');
INSERT INTO loyalty_earnings VALUES (8750076, 7199382, 3150842, 797, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (2307361, 1194669, 3224135, 2077, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (6042546, 7844689, 7899943, 2968, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (8899312, 8334716, 6329350, 791, '2026-01-02');
INSERT INTO loyalty_earnings VALUES (6186589, 5873835, 5109828, 899, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (8115278, 6394448, 7217433, 2769, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (3890538, 1532292, 1610270, 4601, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (7440656, 2457131, 9526705, 1712, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (4102677, 2680443, 2077334, 2805, '2026-01-09');
INSERT INTO loyalty_earnings VALUES (8550961, 3441464, 9868867, 3746, '2026-01-02');
INSERT INTO loyalty_earnings VALUES (2699111, 5929437, 3487415, 851, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (4647921, 5991028, 7968367, 2659, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (8784796, 5608758, 4595619, 4472, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (5691432, 3779626, 7169430, 4838, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (9760479, 5197539, 7403311, 2506, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (4836244, 6754234, 2934427, 963, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (3821466, 2897716, 7873250, 1157, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (2162159, 1333361, 3233971, 569, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (6154031, 2386881, 7496042, 1289, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (6110404, 8760021, 4246383, 3728, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (8682657, 6787359, 2868562, 4267, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (8814642, 9684543, 3986188, 4524, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (5721344, 5077999, 2227634, 945, '2026-04-01');
INSERT INTO loyalty_earnings VALUES (3743605, 1914249, 1339636, 571, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (1266789, 5211411, 6236965, 3484, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (2286876, 5847938, 2084890, 4591, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (7258399, 6959281, 4530854, 4681, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (5292251, 9352451, 8423344, 724, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (3778986, 4083161, 2162029, 4194, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (6631541, 9661014, 6156293, 789, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (4927191, 5544563, 4530854, 3407, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (2943320, 4162115, 3891137, 3798, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (4517222, 4162115, 5317875, 3037, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (2340757, 5984309, 1163615, 1305, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (7275701, 9849424, 6127004, 2047, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (3891538, 8701300, 6954524, 4017, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (9697655, 6222472, 4662730, 2990, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (6563274, 9050693, 1729245, 754, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (4083147, 1628369, 3603058, 764, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (6301146, 1865080, 6111066, 1428, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (3789301, 1630227, 5317875, 2224, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (2739696, 5196342, 3150842, 3188, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (4694968, 3579033, 9092559, 2636, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (7217764, 3219580, 6920459, 756, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (9295012, 2739438, 5010487, 3099, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (4615741, 7890273, 4100639, 1345, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (8293356, 9421408, 1328187, 3969, '2026-02-01');
INSERT INTO loyalty_earnings VALUES (5102504, 3930094, 2777401, 4274, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (3828173, 1208366, 1272742, 2834, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (9729364, 9862436, 2162029, 1049, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (6296099, 1731082, 1211134, 1303, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (9410830, 1510315, 9072354, 2824, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (9442623, 7756506, 2959304, 1877, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (7807204, 6829687, 9526705, 4969, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (9931409, 3107693, 7169430, 1704, '2026-04-19');
INSERT INTO loyalty_earnings VALUES (5042176, 6758719, 4797001, 4093, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (8434751, 3578181, 1083645, 4069, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (8101829, 1947831, 7635536, 2669, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (5361673, 1609490, 6774082, 1663, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (6902869, 9025546, 6939753, 1877, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (5860304, 1308661, 4453764, 1543, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (1672042, 1673067, 5576927, 2721, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (2238725, 1141592, 4100639, 4818, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (7594779, 3056420, 7786633, 2922, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (2091093, 4733341, 3107150, 3178, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (9981966, 9357543, 9963495, 2494, '2026-04-28');
INSERT INTO loyalty_earnings VALUES (7914687, 1467706, 7220233, 2213, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (7634035, 8582013, 7169430, 4809, '2026-03-21');
INSERT INTO loyalty_earnings VALUES (5416787, 5592345, 3086160, 2277, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (1158431, 6960741, 8948057, 2168, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (4069102, 7661075, 8189656, 3486, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (4320662, 2811160, 7496042, 3493, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (3491226, 3518512, 7873250, 3950, '2026-03-30');
INSERT INTO loyalty_earnings VALUES (8030137, 3897673, 7873250, 1771, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (4507466, 7470490, 3487415, 1974, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (1058005, 5444623, 5109828, 2532, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (8334886, 4729305, 2858315, 3874, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (1880518, 6886463, 9469611, 3386, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (3117733, 5681590, 5010487, 4578, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (1123355, 4083161, 6383237, 1767, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (3350240, 6886463, 8991792, 1538, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (3917222, 8799279, 2427690, 1462, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (9297203, 5748118, 1892068, 575, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (6583327, 2004972, 4123196, 4844, '2026-04-04');
INSERT INTO loyalty_earnings VALUES (1362800, 9859363, 6776228, 4142, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (1662820, 3664717, 2776678, 3041, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (5075776, 4165264, 8027778, 1086, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (4802247, 2831053, 1500606, 4105, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (8801744, 3284657, 8224728, 1805, '2026-04-07');
INSERT INTO loyalty_earnings VALUES (9884396, 3709835, 4530854, 548, '2026-01-29');
INSERT INTO loyalty_earnings VALUES (2764029, 7602403, 2723241, 3996, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (8962825, 3094228, 7383012, 4541, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (5350550, 1845772, 1339636, 4841, '2026-03-30');
INSERT INTO loyalty_earnings VALUES (3734066, 5451310, 1547921, 1008, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (5840132, 8638856, 8991792, 2421, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (8857690, 4961976, 8155828, 4031, '2026-03-03');
INSERT INTO loyalty_earnings VALUES (1124623, 8036188, 6992003, 899, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (5523343, 5657432, 2227634, 2242, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (1066926, 8047880, 4073362, 4813, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (5048061, 9793545, 1741094, 1054, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (4376188, 9331041, 6236965, 3581, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (6500960, 6129697, 8812278, 1460, '2026-04-14');
INSERT INTO loyalty_earnings VALUES (9849462, 8494942, 9848880, 4189, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (1411743, 5657432, 2957004, 1634, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (9578864, 4216718, 2227634, 3912, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (9314466, 2672726, 4073362, 3209, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (9773583, 4991658, 1718275, 3887, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (7765632, 1337378, 7786316, 1124, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (9570386, 4216718, 9719088, 3428, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (3026671, 5110264, 9218001, 3447, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (3126574, 8390811, 2077334, 1214, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (5822445, 6039800, 2162029, 1393, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (8199058, 8260979, 8834504, 4853, '2026-01-07');
INSERT INTO loyalty_earnings VALUES (1301738, 3174616, 3354650, 4872, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (7308637, 5810065, 1432212, 1855, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (2289573, 5196342, 3231298, 3805, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (1503893, 6176256, 1211134, 3658, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (6763724, 5482591, 9243803, 4179, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (7982616, 8450214, 2986199, 3475, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (4198901, 1914249, 7786316, 2076, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (9568361, 6709181, 9092559, 2290, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (6970371, 3061202, 2505634, 3244, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (6787292, 8362204, 9233734, 1897, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (9141949, 3829098, 7873250, 3068, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (1671200, 1258608, 2934427, 1541, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (6354674, 8115927, 5526936, 3468, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (4083125, 5735932, 9538849, 2929, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (8887307, 6087178, 8621561, 3352, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (4571172, 5041240, 5494015, 1140, '2026-04-01');
INSERT INTO loyalty_earnings VALUES (4172314, 1848426, 2407452, 4602, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (3421006, 5440509, 4530532, 1174, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (9975129, 1327384, 2957004, 1114, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (5053561, 8494942, 4453764, 4683, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (8565206, 2952438, 9728828, 532, '2026-03-03');
INSERT INTO loyalty_earnings VALUES (7834185, 6063777, 5682977, 3331, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (4304137, 2348979, 6418072, 4191, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (6107963, 7498419, 2858315, 3746, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (7084661, 9744335, 7496042, 3384, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (1724456, 7641636, 6992003, 1977, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (4377617, 2691695, 2227634, 4488, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (8357675, 7286733, 7873250, 4146, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (4887859, 7719893, 5734972, 2306, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (3658604, 6886463, 9243803, 1878, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (2322254, 3439005, 9925283, 2628, '2026-04-19');
INSERT INTO loyalty_earnings VALUES (7199924, 9542020, 6124507, 1767, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (2944689, 4008564, 4797001, 647, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (9652505, 1036544, 3998210, 2897, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (9618949, 3441464, 5116160, 3326, '2026-04-07');
INSERT INTO loyalty_earnings VALUES (2647067, 3641027, 7899943, 4986, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (3051111, 4528017, 1010438, 1138, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (6055963, 6557492, 2294181, 1790, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (8185642, 3439005, 1888680, 4856, '2026-02-19');
INSERT INTO loyalty_earnings VALUES (4624774, 2313136, 9233734, 1881, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (1011841, 3804691, 4772680, 4467, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (9708850, 9695645, 3700732, 1608, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (3821091, 7380779, 3354650, 1063, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (6740722, 7820234, 5010487, 4483, '2026-02-19');
INSERT INTO loyalty_earnings VALUES (7608700, 8180107, 1154532, 3167, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (7262931, 7391811, 7217433, 1909, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (8843520, 5577893, 8173911, 2112, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (4272368, 8115927, 9383250, 4013, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (7023860, 6315863, 9243803, 938, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (6059541, 8428829, 1272742, 2644, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (2297546, 4568343, 6820491, 4664, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (3954059, 9204189, 7403311, 3222, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (7391537, 4291922, 3231298, 2896, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (3132383, 2313136, 3959460, 4792, '2026-03-30');
INSERT INTO loyalty_earnings VALUES (6496982, 6756171, 9362387, 1623, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (4503321, 6646700, 4892788, 3195, '2026-02-19');
INSERT INTO loyalty_earnings VALUES (2934666, 4012314, 6245743, 3347, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (5204738, 8475534, 3224135, 3380, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (9405928, 6627102, 3307906, 2714, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (8570365, 7489303, 3959460, 678, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (8361260, 4545850, 5526936, 1650, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (7223784, 8638856, 3998210, 4000, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (5794347, 6814766, 1328187, 3850, '2026-03-27');
INSERT INTO loyalty_earnings VALUES (4726388, 8749003, 1339636, 3025, '2026-02-26');
INSERT INTO loyalty_earnings VALUES (4187691, 4034235, 8994337, 1703, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (6398082, 6785085, 6766644, 3415, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (8476713, 4778792, 3891137, 949, '2026-03-21');
INSERT INTO loyalty_earnings VALUES (4501604, 2358908, 1729245, 3190, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (3801668, 1111874, 8621561, 4096, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (7814836, 4216040, 9469611, 4401, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (1283961, 1248456, 9383250, 3685, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (3464998, 6005366, 1994077, 3921, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (1450272, 3817471, 5526936, 556, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (8341420, 4991658, 7383012, 598, '2026-04-01');
INSERT INTO loyalty_earnings VALUES (4244427, 6750254, 4662730, 570, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (7727853, 5873835, 4892788, 4612, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (2186146, 3326133, 5465007, 4609, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (8920669, 9532476, 9719088, 4757, '2026-03-21');
INSERT INTO loyalty_earnings VALUES (2614864, 9457407, 4453764, 4701, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (5013947, 4398930, 2965959, 2532, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (6316478, 8019586, 1163615, 3262, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (2834579, 8272382, 2911738, 4067, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (4194905, 1609490, 7786633, 599, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (2296381, 8813465, 9383250, 682, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (4630467, 7247048, 9331874, 3748, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (1831756, 3655349, 3107150, 1068, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (3853334, 5373322, 9299869, 3351, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (8769560, 4216040, 7220233, 2034, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (4907847, 4008721, 8423344, 1665, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (2337518, 7889117, 1497262, 1084, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (3797078, 8081512, 1888680, 696, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (8926972, 8879282, 3998210, 2297, '2026-01-09');
INSERT INTO loyalty_earnings VALUES (5657708, 7178612, 4892788, 3012, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (1522903, 3016306, 1931461, 2677, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (3143496, 9577941, 7532793, 2580, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (5427524, 7209569, 9469611, 2751, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (5318337, 5664451, 2723241, 1315, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (4722385, 3779626, 7873250, 1257, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (7462234, 7761831, 6245743, 2228, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (5026632, 8246948, 7031540, 4832, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (1980632, 1425174, 3610348, 633, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (8488780, 8701300, 3233971, 2545, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (9210807, 1871300, 7403311, 2314, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (4906649, 9603207, 2294181, 3259, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (3300343, 2068071, 8409944, 4815, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (8799022, 2645318, 9226362, 2041, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (6879935, 5873835, 6418072, 4632, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (4361453, 8807910, 5931547, 3760, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (1028052, 4939990, 3797933, 3309, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (1342382, 6557492, 9226362, 4377, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (3604925, 1964975, 3959460, 3512, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (1754440, 7074356, 9992742, 3151, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (5940351, 4421959, 1328187, 2143, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (6447215, 4291922, 7786633, 3809, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (2770005, 9763574, 5109828, 635, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (7523608, 8012149, 2957004, 3575, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (9926896, 9590988, 2959304, 2018, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (7675207, 2004972, 4219908, 960, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (2406645, 7682748, 6197631, 3778, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (4787170, 8003870, 6373970, 1233, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (7019958, 2750638, 6329350, 1430, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (2005312, 6361531, 7383012, 832, '2026-03-27');
INSERT INTO loyalty_earnings VALUES (2534567, 9695645, 6920459, 2214, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (2948264, 4189265, 9868867, 706, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (1723757, 3236623, 9526705, 1884, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (1608414, 1912987, 4246383, 3295, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (6849684, 4559812, 4332111, 1937, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (1961893, 1630227, 6180741, 2863, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (8221884, 2341251, 2911738, 1538, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (8529774, 2313136, 6793309, 2832, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (8241985, 3287489, 9728828, 4596, '2026-04-15');
INSERT INTO loyalty_earnings VALUES (3544404, 1194669, 2168435, 1066, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (6874461, 7936822, 5131170, 2792, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (3900714, 5847938, 1264848, 4576, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (5291031, 6396126, 1328187, 3039, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (7250809, 2983965, 7791393, 4595, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (5827971, 1127504, 9243803, 3934, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (7455318, 6176256, 7563300, 3948, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (1832277, 2795819, 9331874, 2738, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (3452213, 1895972, 1163615, 1692, '2026-04-13');
INSERT INTO loyalty_earnings VALUES (1077776, 5300181, 4332111, 4774, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (2654307, 4719837, 2696654, 2571, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (1905502, 7589791, 2227634, 3474, '2026-05-10');
INSERT INTO loyalty_earnings VALUES (4146862, 3236623, 2084890, 4656, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (9295518, 2484696, 2228727, 1997, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (3372165, 2653056, 5721829, 1517, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (9436111, 8205394, 6027472, 4676, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (7362907, 4305778, 9027045, 3364, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (1842607, 5858697, 1339636, 1771, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (4556149, 2564401, 7169430, 1177, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (4876197, 7178612, 1718275, 3408, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (2598328, 6039800, 6383237, 4020, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (1990290, 8798651, 1154532, 4252, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (2901771, 5690330, 3822974, 2536, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (9546035, 4495174, 6992003, 3368, '2026-02-20');
INSERT INTO loyalty_earnings VALUES (6999125, 9431928, 8948057, 1029, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (7275698, 7241642, 7217433, 3171, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (5922037, 3328283, 9312273, 4157, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (1641571, 5059358, 1718275, 4840, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (6636971, 3936076, 1154532, 2199, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (4161249, 6276151, 9112089, 1465, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (6246941, 7708412, 5328741, 1317, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (4129646, 7380779, 4530532, 866, '2026-01-17');
INSERT INTO loyalty_earnings VALUES (6657176, 9801730, 5734972, 2016, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (9510722, 1066466, 1196601, 1173, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (1953099, 8410014, 8423344, 1898, '2026-05-10');
INSERT INTO loyalty_earnings VALUES (6344805, 7802388, 1729245, 641, '2026-02-26');
INSERT INTO loyalty_earnings VALUES (8290934, 7672443, 2696654, 2978, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (8094141, 7710596, 7496042, 2816, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (6971286, 3132795, 2965959, 4287, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (4720393, 8037895, 4219908, 566, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (5077728, 8690706, 6766644, 1286, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (5964778, 1356035, 5721829, 2754, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (6008660, 4452090, 1988776, 820, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (7965982, 1538571, 9538849, 2422, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (7243069, 7710596, 7786633, 1789, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (4497877, 9156560, 2481283, 1295, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (5220120, 4165264, 1264848, 853, '2026-04-13');
INSERT INTO loyalty_earnings VALUES (9174358, 8510137, 5526936, 588, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (3024516, 2852219, 5010487, 4754, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (8644989, 9298319, 6954524, 3406, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (6266614, 9744335, 5734972, 3800, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (5369444, 4542378, 5380927, 4659, '2026-02-12');
INSERT INTO loyalty_earnings VALUES (3445312, 8978098, 2505634, 3600, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (5200279, 1504954, 9027045, 2511, '2026-01-25');
INSERT INTO loyalty_earnings VALUES (2598647, 1684189, 6405551, 3157, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (8266903, 9310553, 2168435, 4057, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (6497279, 9786264, 7515968, 633, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (8550472, 3016306, 6920459, 2823, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (1161776, 5367651, 5465007, 3676, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (4178190, 4119119, 7786316, 3419, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (4928788, 3415052, 5494015, 2591, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (5032258, 6129697, 3700732, 550, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (2673663, 4047157, 2505634, 2245, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (9192092, 6283542, 9331874, 1762, '2026-05-10');
INSERT INTO loyalty_earnings VALUES (1701341, 7010852, 9925283, 4710, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (5475156, 9661014, 6651063, 1092, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (7526387, 2272649, 8189656, 4192, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (5495715, 8428829, 6383237, 3823, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (6385111, 2042893, 9092559, 4124, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (3134393, 6187299, 6776228, 754, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (2897119, 3154105, 3986188, 2682, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (1787729, 5053175, 4662730, 4498, '2026-01-02');
INSERT INTO loyalty_earnings VALUES (7408872, 1782369, 3797933, 2236, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (6309318, 8136247, 6992003, 686, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (9251411, 7178317, 2961215, 3456, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (9298579, 2564401, 9383250, 3671, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (3039896, 7796733, 3224135, 895, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (4118253, 8081512, 2153952, 3157, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (3810253, 4262048, 1668346, 1294, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (7351305, 2703529, 8189656, 789, '2026-01-02');
INSERT INTO loyalty_earnings VALUES (6880216, 7936822, 7532793, 3989, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (4892472, 5373322, 4662730, 4277, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (8250697, 1960793, 2153952, 4368, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (3064381, 1309610, 2470259, 796, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (5972759, 2384364, 3482381, 3320, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (6344138, 3551594, 3891137, 4016, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (4209882, 3785046, 1211134, 4333, '2026-04-07');
INSERT INTO loyalty_earnings VALUES (8632792, 5161393, 1083645, 2133, '2026-01-05');
INSERT INTO loyalty_earnings VALUES (2858149, 8870772, 8621561, 3683, '2026-04-19');
INSERT INTO loyalty_earnings VALUES (9510971, 9159559, 9243803, 4053, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (1682308, 8978098, 4123196, 4657, '2026-04-19');
INSERT INTO loyalty_earnings VALUES (1451203, 5845335, 9848880, 4074, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (4367054, 6001708, 8409944, 3879, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (4260649, 6129697, 5745431, 550, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (4300961, 1536097, 8423344, 3406, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (6097149, 6785085, 9538849, 3489, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (3330054, 8421927, 7496042, 1068, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (9852056, 2758436, 4469259, 1538, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (5944010, 6717934, 5328741, 723, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (2672938, 7436638, 3677816, 2558, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (3842267, 1049770, 5380927, 4984, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (8158871, 2381088, 3482381, 2390, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (2055185, 6927368, 9383250, 832, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (2358393, 1116067, 9027045, 1301, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (7932679, 3930094, 9112089, 1555, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (8343245, 5690330, 4491384, 1077, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (1215480, 4517235, 5682977, 3754, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (2030176, 1296435, 2168435, 2335, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (5139421, 3686549, 8515559, 4517, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (3080646, 7296919, 9526705, 3778, '2026-03-29');
INSERT INTO loyalty_earnings VALUES (2307577, 6758719, 3454898, 4929, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (2855568, 2218331, 4797001, 523, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (6158078, 7364876, 4491384, 1224, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (2198037, 9662347, 8409944, 2084, '2026-01-28');
INSERT INTO loyalty_earnings VALUES (3782099, 2912484, 5576927, 1259, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (3256637, 7768654, 9072354, 4913, '2026-04-13');
INSERT INTO loyalty_earnings VALUES (1124588, 8003870, 9027045, 2429, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (3633548, 7764598, 7873250, 2173, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (6319842, 6917413, 6301649, 2476, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (6752792, 4244339, 1741094, 2700, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (3023298, 2386881, 8155828, 780, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (3051122, 8690706, 9112089, 2212, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (3720457, 3535754, 5317875, 3413, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (8037844, 2203629, 8834504, 601, '2026-04-04');
INSERT INTO loyalty_earnings VALUES (1223591, 5735932, 4772680, 3782, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (1375727, 5261503, 6954524, 609, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (4786706, 9223238, 8948057, 4487, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (5418483, 4344113, 1339636, 3440, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (6552960, 4990200, 7496042, 2869, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (1094577, 3301946, 2162029, 1330, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (6351345, 8521337, 9299869, 655, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (2418740, 8036188, 4595619, 2188, '2026-04-09');
INSERT INTO loyalty_earnings VALUES (1938379, 3183782, 6383237, 2493, '2026-01-17');
INSERT INTO loyalty_earnings VALUES (8454948, 8650575, 1163615, 4754, '2026-02-12');
INSERT INTO loyalty_earnings VALUES (8391664, 3284657, 2168435, 2254, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (2618191, 3956391, 3107150, 1051, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (7369015, 7761831, 8173911, 2377, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (3482899, 8518583, 9112089, 4878, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (8374635, 6711772, 4797001, 4404, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (6816383, 8650575, 9793501, 4697, '2026-01-15');
INSERT INTO loyalty_earnings VALUES (1569708, 6286856, 2294181, 3009, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (1017773, 5971778, 5931547, 2298, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (4361086, 2044101, 3233971, 3287, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (7209448, 6212286, 9072354, 2610, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (1750774, 7589791, 9381230, 3535, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (4037980, 3065839, 8409944, 2672, '2026-02-04');

-- =========== skymill.delays (generic) ==========

DROP TABLE IF EXISTS delays;

CREATE TABLE delays (
  id INT NOT NULL,
  flight_instance_id INT NOT NULL,
  delay_minutes INT NOT NULL,
  reason VARCHAR(255) NOT NULL
);

INSERT INTO delays VALUES (4057692, 7470412, 89, 'crew');
INSERT INTO delays VALUES (3561072, 2350897, 15, 'technical');
INSERT INTO delays VALUES (7911227, 4715270, 73, 'crew');
INSERT INTO delays VALUES (5574750, 6086941, 99, 'crew');
INSERT INTO delays VALUES (6385590, 1209499, 131, 'technical');
INSERT INTO delays VALUES (8842321, 8427554, 83, 'crew');
INSERT INTO delays VALUES (1436407, 6431334, 84, 'technical');
INSERT INTO delays VALUES (5357136, 9513965, 131, 'weather');
INSERT INTO delays VALUES (2330627, 3416955, 151, 'crew');
INSERT INTO delays VALUES (9125434, 8175644, 28, 'weather');
INSERT INTO delays VALUES (9144168, 3891092, 5, 'technical');
INSERT INTO delays VALUES (3790831, 4999475, 57, 'crew');
INSERT INTO delays VALUES (9741538, 8284228, 136, 'other');
INSERT INTO delays VALUES (3595068, 7255476, 72, 'weather');
INSERT INTO delays VALUES (9222418, 1041571, 160, 'other');
INSERT INTO delays VALUES (4536103, 9962111, 177, 'weather');
INSERT INTO delays VALUES (9011200, 6777939, 84, 'weather');
INSERT INTO delays VALUES (1527143, 4684820, 166, 'crew');
INSERT INTO delays VALUES (2675703, 4661322, 103, 'technical');
INSERT INTO delays VALUES (7861592, 7727576, 84, 'weather');
INSERT INTO delays VALUES (9038676, 4229683, 67, 'technical');
INSERT INTO delays VALUES (2673491, 3900173, 29, 'crew');
INSERT INTO delays VALUES (6852041, 9218215, 174, 'weather');
INSERT INTO delays VALUES (4239121, 9298883, 141, 'crew');
INSERT INTO delays VALUES (8663825, 3790582, 78, 'crew');
INSERT INTO delays VALUES (3175585, 8704711, 88, 'other');
INSERT INTO delays VALUES (4477375, 3495148, 56, 'technical');
INSERT INTO delays VALUES (5370653, 6999905, 138, 'other');
INSERT INTO delays VALUES (9317707, 7290695, 104, 'technical');
INSERT INTO delays VALUES (4971394, 1702070, 104, 'other');
INSERT INTO delays VALUES (3077520, 6710958, 130, 'technical');
INSERT INTO delays VALUES (4939199, 4460972, 52, 'technical');
INSERT INTO delays VALUES (1737812, 7450816, 152, 'other');
INSERT INTO delays VALUES (4494663, 3989140, 172, 'weather');
INSERT INTO delays VALUES (7703983, 6746460, 89, 'other');
INSERT INTO delays VALUES (6728809, 5109931, 37, 'other');
INSERT INTO delays VALUES (7291309, 9851577, 70, 'weather');
INSERT INTO delays VALUES (5131085, 7755856, 23, 'crew');
INSERT INTO delays VALUES (1048593, 2077940, 175, 'technical');
INSERT INTO delays VALUES (2029754, 3216377, 149, 'crew');
INSERT INTO delays VALUES (5480809, 1290249, 111, 'other');
INSERT INTO delays VALUES (2879788, 2278577, 121, 'crew');
INSERT INTO delays VALUES (2678370, 6424106, 135, 'other');
INSERT INTO delays VALUES (4638205, 7144851, 134, 'technical');
INSERT INTO delays VALUES (4190919, 8983277, 13, 'weather');
INSERT INTO delays VALUES (9208382, 6777939, 34, 'other');
INSERT INTO delays VALUES (1406786, 8911236, 77, 'other');
INSERT INTO delays VALUES (6237312, 3928961, 35, 'weather');
INSERT INTO delays VALUES (6703297, 6407111, 160, 'weather');
INSERT INTO delays VALUES (9787342, 7611726, 101, 'weather');
INSERT INTO delays VALUES (8311146, 8066489, 154, 'weather');
INSERT INTO delays VALUES (7428330, 1862683, 22, 'technical');
INSERT INTO delays VALUES (1582060, 1274638, 129, 'crew');
INSERT INTO delays VALUES (8446227, 2750865, 178, 'other');
INSERT INTO delays VALUES (6254199, 1518627, 69, 'technical');
INSERT INTO delays VALUES (5002092, 3699725, 175, 'crew');
INSERT INTO delays VALUES (1537169, 3262281, 82, 'other');
INSERT INTO delays VALUES (1861042, 1072702, 177, 'other');
INSERT INTO delays VALUES (5606441, 1293870, 138, 'technical');
INSERT INTO delays VALUES (4015896, 7072581, 29, 'crew');
INSERT INTO delays VALUES (7275963, 2603592, 77, 'crew');
INSERT INTO delays VALUES (1882154, 9611592, 70, 'crew');
INSERT INTO delays VALUES (8182102, 5109260, 105, 'weather');
INSERT INTO delays VALUES (9922369, 7938588, 111, 'weather');
INSERT INTO delays VALUES (9067374, 3495148, 165, 'weather');
INSERT INTO delays VALUES (2948293, 6086941, 101, 'technical');
INSERT INTO delays VALUES (8136628, 6990414, 5, 'weather');
INSERT INTO delays VALUES (6686491, 3262281, 101, 'other');
INSERT INTO delays VALUES (9486121, 5431012, 165, 'weather');
INSERT INTO delays VALUES (4754931, 4763209, 14, 'crew');
INSERT INTO delays VALUES (2171324, 1558964, 35, 'technical');
INSERT INTO delays VALUES (6547101, 8180075, 162, 'technical');
INSERT INTO delays VALUES (7023920, 6093066, 151, 'technical');
INSERT INTO delays VALUES (2182458, 2587249, 42, 'technical');
INSERT INTO delays VALUES (1129412, 7301468, 147, 'weather');
INSERT INTO delays VALUES (1802255, 8895170, 86, 'technical');
INSERT INTO delays VALUES (7148238, 8442580, 7, 'weather');
INSERT INTO delays VALUES (7065546, 6719936, 119, 'weather');
INSERT INTO delays VALUES (3026378, 8841346, 146, 'other');
INSERT INTO delays VALUES (1974396, 7727576, 74, 'weather');
INSERT INTO delays VALUES (6578287, 3168880, 161, 'technical');
INSERT INTO delays VALUES (5974380, 7727576, 162, 'other');
INSERT INTO delays VALUES (5518445, 8393896, 116, 'weather');
INSERT INTO delays VALUES (7432355, 4715270, 177, 'technical');
INSERT INTO delays VALUES (8372084, 9958280, 30, 'technical');
INSERT INTO delays VALUES (7043705, 9611592, 118, 'crew');
INSERT INTO delays VALUES (1908423, 5561925, 177, 'technical');
INSERT INTO delays VALUES (1677548, 4193470, 63, 'weather');
INSERT INTO delays VALUES (8830087, 6710958, 90, 'other');
INSERT INTO delays VALUES (2119540, 3731525, 114, 'other');
INSERT INTO delays VALUES (2315307, 1965797, 176, 'technical');
INSERT INTO delays VALUES (5905848, 9724633, 55, 'other');
INSERT INTO delays VALUES (4742004, 1099849, 48, 'other');
INSERT INTO delays VALUES (7139147, 7140829, 124, 'other');
INSERT INTO delays VALUES (9191052, 5975382, 17, 'weather');
INSERT INTO delays VALUES (2905715, 2350897, 17, 'other');
INSERT INTO delays VALUES (3542213, 1619013, 172, 'technical');
INSERT INTO delays VALUES (8146954, 2993925, 179, 'crew');
INSERT INTO delays VALUES (2188272, 3245038, 150, 'weather');
INSERT INTO delays VALUES (4622225, 1099849, 128, 'weather');
INSERT INTO delays VALUES (7858396, 7072581, 24, 'other');
INSERT INTO delays VALUES (7200063, 9218215, 162, 'technical');
INSERT INTO delays VALUES (1801047, 4010212, 60, 'weather');
INSERT INTO delays VALUES (2514610, 1965797, 151, 'other');
INSERT INTO delays VALUES (8203047, 5616328, 60, 'crew');
INSERT INTO delays VALUES (5164802, 4900203, 143, 'technical');
INSERT INTO delays VALUES (6700043, 5842415, 165, 'crew');
INSERT INTO delays VALUES (3316699, 7598471, 111, 'crew');
INSERT INTO delays VALUES (7683944, 4193470, 47, 'crew');
INSERT INTO delays VALUES (5027416, 5862298, 72, 'crew');
INSERT INTO delays VALUES (4561772, 3252086, 36, 'other');
INSERT INTO delays VALUES (5851809, 4610651, 122, 'crew');
INSERT INTO delays VALUES (6592926, 3243026, 68, 'technical');
INSERT INTO delays VALUES (4455790, 8462608, 106, 'crew');
INSERT INTO delays VALUES (4174523, 8284228, 78, 'technical');
INSERT INTO delays VALUES (8554480, 8899441, 24, 'crew');
INSERT INTO delays VALUES (5290333, 9412051, 8, 'weather');
INSERT INTO delays VALUES (8894187, 5282783, 83, 'technical');
INSERT INTO delays VALUES (9954937, 7450816, 45, 'crew');
INSERT INTO delays VALUES (1368416, 1374988, 136, 'other');
INSERT INTO delays VALUES (1055836, 7144851, 42, 'weather');
INSERT INTO delays VALUES (2446075, 2369537, 10, 'weather');
INSERT INTO delays VALUES (1636797, 5784607, 90, 'other');
INSERT INTO delays VALUES (9618020, 5751130, 147, 'technical');
INSERT INTO delays VALUES (7122079, 5848496, 127, 'weather');
INSERT INTO delays VALUES (2650883, 6093066, 91, 'crew');
INSERT INTO delays VALUES (4320272, 1671714, 12, 'weather');
INSERT INTO delays VALUES (9881712, 6710958, 100, 'weather');
INSERT INTO delays VALUES (3466837, 1293870, 133, 'crew');
INSERT INTO delays VALUES (2027587, 9152758, 83, 'weather');
INSERT INTO delays VALUES (4874143, 6835745, 120, 'other');
INSERT INTO delays VALUES (2365021, 7512215, 114, 'weather');
INSERT INTO delays VALUES (6482284, 3739269, 92, 'weather');
INSERT INTO delays VALUES (8026744, 9142476, 120, 'technical');
INSERT INTO delays VALUES (2367151, 3891059, 144, 'weather');
INSERT INTO delays VALUES (5805284, 2092479, 54, 'technical');
INSERT INTO delays VALUES (9622812, 7613367, 139, 'crew');
INSERT INTO delays VALUES (4325538, 5818296, 25, 'technical');
INSERT INTO delays VALUES (2038949, 5830883, 33, 'crew');
INSERT INTO delays VALUES (5203815, 5109260, 119, 'technical');
INSERT INTO delays VALUES (5543288, 2037973, 151, 'crew');
INSERT INTO delays VALUES (6316897, 8066489, 111, 'weather');
INSERT INTO delays VALUES (1885215, 1487850, 144, 'technical');
INSERT INTO delays VALUES (8896649, 8983277, 48, 'other');
INSERT INTO delays VALUES (4975731, 8689138, 17, 'other');
INSERT INTO delays VALUES (9109841, 2750865, 144, 'weather');
INSERT INTO delays VALUES (5711126, 2011623, 175, 'crew');
INSERT INTO delays VALUES (8047575, 1739346, 149, 'other');
INSERT INTO delays VALUES (7021923, 2512031, 171, 'crew');
INSERT INTO delays VALUES (6362406, 5216329, 43, 'crew');
INSERT INTO delays VALUES (1232366, 8741700, 27, 'other');
INSERT INTO delays VALUES (6653395, 8670473, 35, 'crew');
INSERT INTO delays VALUES (1222379, 3491018, 103, 'crew');
INSERT INTO delays VALUES (6118006, 1947070, 99, 'crew');
INSERT INTO delays VALUES (1743585, 4949024, 103, 'technical');
INSERT INTO delays VALUES (9769624, 9808915, 28, 'technical');
INSERT INTO delays VALUES (1341665, 7342356, 33, 'technical');
INSERT INTO delays VALUES (5673598, 5637917, 109, 'other');
INSERT INTO delays VALUES (7366566, 8493745, 18, 'crew');
INSERT INTO delays VALUES (8919274, 1546513, 87, 'technical');
INSERT INTO delays VALUES (7483790, 5784607, 65, 'other');
INSERT INTO delays VALUES (7012654, 7588961, 12, 'other');
INSERT INTO delays VALUES (5973990, 9152758, 139, 'weather');
INSERT INTO delays VALUES (1010496, 4851977, 91, 'crew');
INSERT INTO delays VALUES (8930650, 6424106, 111, 'other');
INSERT INTO delays VALUES (8068378, 1347646, 124, 'technical');
INSERT INTO delays VALUES (9353628, 3747976, 82, 'other');
INSERT INTO delays VALUES (5970271, 4647476, 40, 'crew');
INSERT INTO delays VALUES (8543488, 9011552, 47, 'other');
INSERT INTO delays VALUES (7864091, 7128137, 69, 'technical');
INSERT INTO delays VALUES (3047839, 8280938, 24, 'weather');
INSERT INTO delays VALUES (8635183, 9851577, 54, 'other');
INSERT INTO delays VALUES (5669213, 1454134, 179, 'technical');
INSERT INTO delays VALUES (6072926, 3168880, 125, 'other');
INSERT INTO delays VALUES (7843272, 4512184, 163, 'other');
INSERT INTO delays VALUES (6973802, 4647476, 47, 'technical');
INSERT INTO delays VALUES (2592347, 8066489, 9, 'crew');
INSERT INTO delays VALUES (2736416, 2466127, 167, 'crew');
INSERT INTO delays VALUES (3096729, 9513965, 31, 'crew');
INSERT INTO delays VALUES (4859568, 6757414, 51, 'other');
INSERT INTO delays VALUES (9194861, 8650087, 76, 'technical');
INSERT INTO delays VALUES (7096876, 9412051, 17, 'weather');
INSERT INTO delays VALUES (7235870, 8323300, 102, 'other');
INSERT INTO delays VALUES (3907591, 3416955, 118, 'other');
INSERT INTO delays VALUES (3361434, 2248186, 120, 'weather');
INSERT INTO delays VALUES (9175188, 2557122, 176, 'crew');
INSERT INTO delays VALUES (9804194, 6392794, 150, 'other');
INSERT INTO delays VALUES (3457078, 5089360, 27, 'weather');
INSERT INTO delays VALUES (2732538, 6710958, 69, 'technical');
INSERT INTO delays VALUES (3585989, 3172946, 69, 'weather');
INSERT INTO delays VALUES (8799946, 1890704, 85, 'crew');
INSERT INTO delays VALUES (5941173, 5616328, 58, 'technical');
INSERT INTO delays VALUES (2883130, 2077940, 80, 'other');
INSERT INTO delays VALUES (7314788, 8066489, 45, 'crew');
INSERT INTO delays VALUES (7598396, 6357849, 6, 'technical');
INSERT INTO delays VALUES (3886682, 9947215, 113, 'crew');
INSERT INTO delays VALUES (6335054, 3941019, 17, 'other');
INSERT INTO delays VALUES (8908269, 4344487, 83, 'technical');
INSERT INTO delays VALUES (6078469, 5431012, 37, 'weather');
INSERT INTO delays VALUES (1216149, 8841346, 58, 'technical');
INSERT INTO delays VALUES (4884289, 9343423, 7, 'other');
INSERT INTO delays VALUES (2815431, 4973827, 179, 'weather');
INSERT INTO delays VALUES (7296077, 4763209, 108, 'technical');
INSERT INTO delays VALUES (4265762, 9279815, 29, 'crew');
INSERT INTO delays VALUES (3224864, 8983277, 157, 'crew');
INSERT INTO delays VALUES (2407536, 5784607, 114, 'crew');
INSERT INTO delays VALUES (7246482, 1532836, 149, 'technical');
INSERT INTO delays VALUES (6803215, 5109260, 65, 'other');
INSERT INTO delays VALUES (5084570, 2861626, 28, 'technical');
INSERT INTO delays VALUES (8505513, 7450816, 35, 'technical');
INSERT INTO delays VALUES (2000112, 4229683, 172, 'technical');
INSERT INTO delays VALUES (4714297, 4128390, 92, 'weather');
INSERT INTO delays VALUES (1212872, 6093066, 23, 'other');
INSERT INTO delays VALUES (5104194, 1890704, 118, 'weather');
INSERT INTO delays VALUES (9427437, 3234139, 64, 'technical');
INSERT INTO delays VALUES (6276944, 1624553, 149, 'technical');
INSERT INTO delays VALUES (1957588, 1274638, 116, 'crew');
INSERT INTO delays VALUES (4915660, 2587249, 123, 'weather');
INSERT INTO delays VALUES (7430818, 9916922, 52, 'crew');
INSERT INTO delays VALUES (7296902, 2887013, 139, 'other');
INSERT INTO delays VALUES (2468218, 9792793, 119, 'other');
INSERT INTO delays VALUES (1893436, 8066489, 96, 'weather');
INSERT INTO delays VALUES (3545142, 8126220, 55, 'weather');
INSERT INTO delays VALUES (9856037, 5826789, 102, 'other');
INSERT INTO delays VALUES (9187270, 5218698, 151, 'technical');
INSERT INTO delays VALUES (3904857, 6638170, 151, 'technical');
INSERT INTO delays VALUES (8828560, 6835745, 58, 'technical');
INSERT INTO delays VALUES (8631215, 4318596, 101, 'technical');
INSERT INTO delays VALUES (3446261, 8126220, 175, 'technical');
INSERT INTO delays VALUES (4359492, 1957125, 18, 'other');
INSERT INTO delays VALUES (8339358, 2077631, 62, 'crew');
INSERT INTO delays VALUES (9904193, 3686836, 153, 'other');
INSERT INTO delays VALUES (7231812, 4010212, 160, 'technical');
INSERT INTO delays VALUES (4921484, 8778991, 45, 'weather');
INSERT INTO delays VALUES (4607407, 4318596, 88, 'weather');
INSERT INTO delays VALUES (2823563, 3790582, 64, 'crew');
INSERT INTO delays VALUES (2753545, 5262304, 36, 'other');
INSERT INTO delays VALUES (3536278, 1762159, 79, 'weather');
INSERT INTO delays VALUES (1085966, 5830883, 96, 'technical');
INSERT INTO delays VALUES (1795521, 5089360, 47, 'crew');
INSERT INTO delays VALUES (1375047, 1098468, 135, 'technical');
INSERT INTO delays VALUES (6997290, 4715270, 126, 'technical');
INSERT INTO delays VALUES (3312405, 9115191, 50, 'technical');
INSERT INTO delays VALUES (5244577, 4937369, 147, 'technical');
INSERT INTO delays VALUES (7287216, 4152237, 75, 'technical');
INSERT INTO delays VALUES (1700431, 4378471, 118, 'other');
INSERT INTO delays VALUES (4999211, 1965797, 112, 'other');
INSERT INTO delays VALUES (7022535, 4144513, 141, 'other');
INSERT INTO delays VALUES (2534323, 1041571, 76, 'other');
INSERT INTO delays VALUES (1658588, 1041571, 151, 'other');
INSERT INTO delays VALUES (7083562, 6988118, 74, 'weather');
INSERT INTO delays VALUES (8869525, 5109260, 122, 'other');
INSERT INTO delays VALUES (1723831, 5818296, 78, 'other');
INSERT INTO delays VALUES (4143555, 5072970, 164, 'weather');
INSERT INTO delays VALUES (9842941, 8983277, 135, 'other');
INSERT INTO delays VALUES (5799813, 3245038, 33, 'technical');
INSERT INTO delays VALUES (6458168, 4633014, 148, 'crew');
INSERT INTO delays VALUES (1202745, 3941019, 6, 'weather');
INSERT INTO delays VALUES (1475537, 3575576, 153, 'crew');
INSERT INTO delays VALUES (7077281, 1558964, 103, 'other');
INSERT INTO delays VALUES (7799161, 7947286, 31, 'weather');
INSERT INTO delays VALUES (7233619, 7277451, 125, 'technical');
INSERT INTO delays VALUES (9033627, 6999905, 149, 'technical');
INSERT INTO delays VALUES (5231676, 5380722, 149, 'technical');
INSERT INTO delays VALUES (7093748, 6638170, 79, 'other');
INSERT INTO delays VALUES (8153096, 9977745, 47, 'crew');
INSERT INTO delays VALUES (6919268, 2861626, 169, 'technical');
INSERT INTO delays VALUES (1070247, 2011623, 90, 'weather');
INSERT INTO delays VALUES (8134610, 8780015, 101, 'other');
INSERT INTO delays VALUES (5979546, 8308378, 88, 'crew');
INSERT INTO delays VALUES (6539336, 2466127, 175, 'other');
INSERT INTO delays VALUES (1411986, 6025683, 147, 'other');
INSERT INTO delays VALUES (7322499, 9792793, 79, 'weather');
INSERT INTO delays VALUES (9847372, 2378137, 69, 'weather');
INSERT INTO delays VALUES (1506143, 8651121, 80, 'other');
INSERT INTO delays VALUES (9139928, 1215045, 75, 'crew');
INSERT INTO delays VALUES (9725478, 1050646, 73, 'other');
INSERT INTO delays VALUES (5934256, 6271427, 13, 'other');
INSERT INTO delays VALUES (5326136, 6777939, 141, 'crew');
INSERT INTO delays VALUES (7289248, 3491018, 107, 'crew');

-- =========== skymill.cancellations (generic) ==========

DROP TABLE IF EXISTS cancellations;

CREATE TABLE cancellations (
  id INT NOT NULL,
  flight_instance_id INT NOT NULL,
  cancellation_reason VARCHAR(255) NOT NULL,
  cancellation_time DATE NOT NULL
);

INSERT INTO cancellations VALUES (5447939, 3248916, 'low_demand', '2026-05-01');
INSERT INTO cancellations VALUES (8881246, 5561925, 'weather', '2026-03-23');
INSERT INTO cancellations VALUES (7337282, 1085869, 'low_demand', '2026-04-03');
INSERT INTO cancellations VALUES (4040944, 3967989, 'low_demand', '2026-03-13');
INSERT INTO cancellations VALUES (4519630, 5751130, 'low_demand', '2026-02-22');
INSERT INTO cancellations VALUES (3776990, 6461218, 'low_demand', '2026-05-06');
INSERT INTO cancellations VALUES (1755378, 6796520, 'operational', '2026-01-14');
INSERT INTO cancellations VALUES (9098283, 3416955, 'low_demand', '2026-02-02');
INSERT INTO cancellations VALUES (7699949, 8607388, 'operational', '2026-02-18');
INSERT INTO cancellations VALUES (7446129, 6431334, 'operational', '2026-03-06');
INSERT INTO cancellations VALUES (3248949, 4010212, 'operational', '2026-03-15');
INSERT INTO cancellations VALUES (9460119, 6746460, 'operational', '2026-03-15');
INSERT INTO cancellations VALUES (2637513, 8899441, 'technical', '2026-04-09');
INSERT INTO cancellations VALUES (8606961, 1536986, 'operational', '2026-04-03');
INSERT INTO cancellations VALUES (1430625, 9142476, 'low_demand', '2026-03-06');
INSERT INTO cancellations VALUES (2101545, 2489999, 'operational', '2026-04-04');
INSERT INTO cancellations VALUES (2815756, 1098468, 'operational', '2026-02-19');
INSERT INTO cancellations VALUES (2226896, 5645246, 'technical', '2026-04-06');
INSERT INTO cancellations VALUES (6855081, 5089360, 'technical', '2026-05-01');
INSERT INTO cancellations VALUES (4546816, 2838934, 'operational', '2026-03-16');
INSERT INTO cancellations VALUES (7288281, 4647476, 'low_demand', '2026-03-25');
INSERT INTO cancellations VALUES (2182717, 8295690, 'low_demand', '2026-01-29');
INSERT INTO cancellations VALUES (7764586, 3481773, 'low_demand', '2026-02-23');
INSERT INTO cancellations VALUES (5181986, 8295690, 'technical', '2026-03-22');
INSERT INTO cancellations VALUES (8970805, 8841346, 'technical', '2026-02-23');
INSERT INTO cancellations VALUES (7657770, 4343069, 'operational', '2026-05-12');
INSERT INTO cancellations VALUES (9961305, 4229683, 'technical', '2026-03-31');
INSERT INTO cancellations VALUES (2814003, 7144851, 'low_demand', '2026-05-04');
INSERT INTO cancellations VALUES (9790179, 1374988, 'low_demand', '2026-03-09');
INSERT INTO cancellations VALUES (1528404, 7614747, 'weather', '2026-01-21');
INSERT INTO cancellations VALUES (2551982, 6432174, 'low_demand', '2026-02-19');
INSERT INTO cancellations VALUES (8605660, 5989126, 'low_demand', '2026-04-05');
INSERT INTO cancellations VALUES (2652658, 1950026, 'technical', '2026-02-07');
INSERT INTO cancellations VALUES (6955829, 8865172, 'technical', '2026-02-20');
INSERT INTO cancellations VALUES (7032467, 3495148, 'weather', '2026-03-16');
INSERT INTO cancellations VALUES (9936900, 2677916, 'weather', '2026-02-14');
INSERT INTO cancellations VALUES (6767802, 2838934, 'low_demand', '2026-01-15');
INSERT INTO cancellations VALUES (5307710, 9153537, 'low_demand', '2026-05-01');
INSERT INTO cancellations VALUES (9359096, 8126220, 'low_demand', '2026-01-19');
INSERT INTO cancellations VALUES (7191402, 2512031, 'operational', '2026-02-04');
INSERT INTO cancellations VALUES (1604859, 9962111, 'weather', '2026-02-16');
INSERT INTO cancellations VALUES (3454974, 1862683, 'operational', '2026-02-18');
INSERT INTO cancellations VALUES (6321230, 8164721, 'low_demand', '2026-01-19');
INSERT INTO cancellations VALUES (8493499, 2466127, 'low_demand', '2026-03-09');
INSERT INTO cancellations VALUES (2680458, 2011623, 'operational', '2026-01-24');
INSERT INTO cancellations VALUES (1390778, 9958280, 'operational', '2026-04-10');
INSERT INTO cancellations VALUES (2367743, 8802551, 'weather', '2026-01-07');
INSERT INTO cancellations VALUES (2650022, 2340419, 'technical', '2026-04-23');
INSERT INTO cancellations VALUES (2576287, 8969609, 'technical', '2026-02-20');
INSERT INTO cancellations VALUES (9975851, 7046633, 'low_demand', '2026-05-05');
INSERT INTO cancellations VALUES (8281043, 6980627, 'operational', '2026-04-06');
INSERT INTO cancellations VALUES (1337643, 7168799, 'technical', '2026-02-24');
INSERT INTO cancellations VALUES (3239262, 4661322, 'low_demand', '2026-03-14');
INSERT INTO cancellations VALUES (4641867, 7046633, 'technical', '2026-05-12');
INSERT INTO cancellations VALUES (6994372, 6233896, 'technical', '2026-03-31');
INSERT INTO cancellations VALUES (2605946, 6683188, 'low_demand', '2026-01-04');
INSERT INTO cancellations VALUES (4054004, 2531988, 'operational', '2026-02-22');
INSERT INTO cancellations VALUES (7037856, 8179773, 'technical', '2026-01-23');
INSERT INTO cancellations VALUES (3922960, 7755856, 'weather', '2026-05-08');
INSERT INTO cancellations VALUES (4213702, 5301340, 'low_demand', '2026-03-28');
INSERT INTO cancellations VALUES (9179158, 5561925, 'operational', '2026-04-19');
INSERT INTO cancellations VALUES (9164268, 2067996, 'weather', '2026-01-05');
INSERT INTO cancellations VALUES (8309749, 6392794, 'operational', '2026-01-12');
INSERT INTO cancellations VALUES (5426669, 9115191, 'operational', '2026-03-09');
INSERT INTO cancellations VALUES (4604016, 4795341, 'weather', '2026-04-09');
INSERT INTO cancellations VALUES (5438192, 2481637, 'low_demand', '2026-04-18');
INSERT INTO cancellations VALUES (8947432, 6392794, 'weather', '2026-04-08');
INSERT INTO cancellations VALUES (7432499, 1497319, 'operational', '2026-03-09');
INSERT INTO cancellations VALUES (7342124, 4229683, 'technical', '2026-01-10');
INSERT INTO cancellations VALUES (4155370, 1072702, 'operational', '2026-02-13');

-- =========== skymill.ticket_prices (generic) ==========

DROP TABLE IF EXISTS ticket_prices;

CREATE TABLE ticket_prices (
  booking_id INT NOT NULL,
  base_price DECIMAL(15, 4) NOT NULL,
  taxes DECIMAL(15, 4) NOT NULL,
  total_price DECIMAL(15, 4) NOT NULL,
  travel_class VARCHAR(255) NOT NULL,
  currency VARCHAR(255) NOT NULL
);

INSERT INTO ticket_prices VALUES (8987248, 714.452649781144, 192.68252067365944, 414.6012731519655, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5444623, 757.7135946841503, 289.63168956754754, 875.0397305322604, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2703927, 898.3818513479157, 249.69867087627, 620.7213006950307, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3132795, 676.4251465293503, 276.50787860522064, 476.75260155621646, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2007605, 159.22583660787376, 134.67391726866725, 676.5503575155742, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3817252, 1321.7199602590981, 79.1309131096611, 117.42817428213293, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5492724, 729.8623671797919, 256.45205198834435, 387.15943735511206, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6756171, 652.1949838324715, 160.89741655407056, 225.03667076969558, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6377160, 1446.3683863880492, 126.82495523802581, 878.493438137992, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3807440, 424.28523234426626, 229.42450922638912, 587.2709296656377, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9124371, 1328.5412586123123, 119.11717473136922, 154.13795908244455, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6085006, 1101.2536498385607, 124.6363553382999, 155.38868693446372, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1951295, 429.02793958427037, 67.4140747849138, 644.8303408039188, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4916481, 1273.1360576593227, 44.210287719500556, 502.8738020533807, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2390558, 738.2230684327327, 24.21619760284949, 24.174721837231793, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9323560, 322.6896840294573, 23.76736162065462, 95.78595838254034, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4119119, 1429.769872119452, 40.787712974990356, 180.06122064789443, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8401928, 1194.5920771851622, 106.18878034577861, 339.34877506017915, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3213297, 569.9646579900232, 252.66663324089473, 593.0067877237138, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3935580, 1102.7701276422345, 224.76868828115107, 225.0416069791782, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6215604, 238.80318774261573, 150.99372720842854, 533.5942977252145, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2811160, 537.123200697362, 44.275137495745, 864.8342047742427, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7093917, 826.5398425191497, 99.25144598677504, 848.3793189388161, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5235900, 1242.9785828503918, 164.06414455667777, 991.2442832492593, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5250963, 1013.6506270681141, 35.988469824334686, 628.7360011409838, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5440509, 1465.4630272943398, 169.3092717685214, 88.06938130559384, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6187299, 1274.7832600156785, 287.79428538307263, 579.6997718028182, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1845772, 892.4022270404646, 78.02737190549374, 962.9721424688364, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2758436, 112.40236661989027, 53.41034264827283, 466.819991143311, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1296435, 252.84565471873353, 101.9951176793093, 504.2269773077527, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7212597, 73.44676649503913, 93.04857777311679, 555.7565885243974, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5919107, 1283.2911333456834, 98.03570199019191, 430.0150234962716, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7886125, 1169.6796190396174, 134.72131942617008, 146.7991564314669, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2376505, 739.3357837167114, 228.89630406954865, 479.66541492736525, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3641027, 1463.6556822518571, 259.9998567456828, 719.0545091276, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4523702, 1277.1992614186026, 160.64513627823817, 408.1254545777202, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3939910, 1363.9280557066809, 237.39767785331625, 705.9654217724116, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4991480, 819.7237544323998, 93.81915310287268, 906.2572091993221, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9125675, 957.0163797520482, 148.00903379462812, 715.5148043544369, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3423997, 562.749232376715, 27.259537264448326, 486.98721959483606, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3579033, 570.4794828708119, 116.45977491752258, 413.3697126252434, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8650575, 388.9324532600475, 54.02159835285133, 959.1169042702332, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7602403, 306.99595750263506, 264.68286948519057, 841.2082086237444, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5764911, 347.13389486988154, 36.486754322528384, 362.8342250758715, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2987692, 1396.021241197088, 38.09652443790212, 283.29348645341145, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2163221, 1045.907188104271, 168.7521818982802, 478.25181533017656, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1327384, 238.49216431114198, 156.59143967220325, 443.9058685468648, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3986898, 1111.163187122315, 121.12347361946269, 958.9602596230902, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9800207, 221.90193958216022, 187.11989594595988, 768.1259675059182, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1993933, 846.1530442338972, 22.373801395678797, 31.832038667784612, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7761831, 1409.1795793149317, 163.68998044061908, 624.9007706605012, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3716336, 320.2280573192243, 192.35635943254076, 516.0604849087981, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5233928, 883.4065380692823, 159.97565527811514, 709.3197571242952, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7784692, 724.6901665805083, 88.7776932742676, 795.9046854679582, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6479415, 1033.018285402226, 286.94943875086585, 671.3614936331022, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9706022, 1288.5602347447264, 180.15505168107254, 569.7604011853404, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6129697, 1347.2154397693469, 243.29444963112778, 110.85679917789292, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9695645, 362.621718058353, 245.8358896319337, 428.5972566601477, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8562757, 1244.645669812406, 299.1116606094429, 577.0721194884094, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5892423, 187.29768504244794, 17.98943910594885, 895.865614161016, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6187299, 1270.3306068444117, 133.92161889261126, 977.49231550219, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1960793, 942.0573485816826, 268.20041440807387, 467.50819176126555, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3686733, 1148.079048528951, 189.9425164097868, 929.3636130972923, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8037895, 1145.7302399407845, 70.36241017067377, 185.42591323368907, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3983543, 900.4978604664011, 225.54270354940837, 853.6671760912967, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6535324, 493.73525751831386, 39.595097403566854, 664.3904231352069, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7510834, 414.3182415618695, 78.63263485958801, 959.5786351173749, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2112280, 1239.2828974096408, 23.393666998549875, 317.72893000571236, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5608758, 82.45593600780863, 287.64004459925644, 366.3132375268484, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5197539, 1308.2565430139866, 235.21074264896384, 364.25698832297337, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3716336, 225.26793853009573, 114.14480060327111, 393.5610544374578, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5279016, 116.9195220109724, 222.5678170799741, 332.41610948891196, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8678382, 444.49333631761965, 205.89740031611794, 666.469014317451, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1151875, 203.84156001368734, 152.4400387930968, 547.3454211812842, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8701300, 648.9563922876748, 148.3338867968226, 822.6554359789681, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4778792, 436.23325353180917, 80.85397553221773, 393.54518719219266, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3154105, 90.94598989148147, 20.636236540483342, 724.2165877632001, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1673067, 1273.8819773686898, 84.06937359357867, 226.23986636054104, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3183782, 1069.349066543808, 222.7069954234825, 859.5849083622227, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3094228, 745.8111363024742, 114.56636312669438, 827.9631617762542, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9690243, 1494.1815128588146, 156.58410172992524, 869.6376234921204, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1066466, 1110.4866787289072, 25.403532781315583, 188.94511801449121, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1878686, 842.2431095390233, 166.24535915046508, 899.4575804734968, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4238719, 447.8135668840774, 193.41018071143702, 762.300066609949, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3183782, 756.8582837658915, 175.10567671277508, 549.2894328161642, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3394152, 96.34988796027054, 32.73526914830913, 562.7783783388114, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4472126, 118.50572618205261, 39.10549201687938, 840.8327191510338, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7736817, 878.8176607306094, 123.94399237622778, 894.7001242706172, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8987248, 1229.0657772973232, 197.14092605479, 630.5232890295415, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3061202, 816.8568859128305, 289.18948025265155, 637.1809797020745, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7680566, 462.92136285290525, 46.52476898370941, 1.3478221981689797, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5761338, 981.4755120960682, 220.43128916949672, 498.25300798713477, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6646700, 570.6052450905787, 198.309278631072, 342.08111517376005, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5743018, 626.021200969868, 231.68110579905388, 684.6146854878986, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5377767, 1038.0851517648503, 169.48442129190784, 589.4789668944873, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1089024, 1304.1169800640096, 280.81192587478677, 929.3345727800994, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2733791, 721.8627973087824, 136.57869028553174, 1.426774089800631, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1159774, 644.7900299301868, 38.79342945147934, 466.7692008609269, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9542020, 947.9069022093488, 87.64120362083207, 578.2718874390475, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9771071, 1396.1219263913476, 110.9693621330271, 599.5642239603676, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8290383, 1249.8523551010867, 222.62229881524203, 361.1410160049119, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9932521, 1297.8012250666668, 103.767644696543, 249.82557747544564, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3520916, 1301.4576584083072, 92.31316970618556, 768.9370006942804, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9859363, 621.2289542324381, 42.593630283972594, 181.88153840752463, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6584934, 503.0347346039575, 239.81079989786863, 504.0549044848084, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6374168, 260.18110218042966, 270.52431108442613, 279.6778110850533, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6085006, 82.55218944754463, 13.84360939158292, 402.2700896650198, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4657770, 1030.9382015675715, 180.98948588357166, 857.8243635231202, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9541641, 225.8193692116785, 220.1994098559535, 875.9780949089426, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4197992, 313.2144841265181, 87.02179602811803, 448.6493449647565, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1361188, 680.759757059497, 288.3422463731866, 522.7723916398664, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2758436, 1214.0281110129274, 169.37749300179945, 574.829390072666, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4720672, 209.886688189045, 161.17840378467713, 290.9842886391312, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5862207, 388.1531075886608, 233.3932951834539, 940.3262293356453, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6717934, 411.14025673128555, 188.60167965168242, 432.42302912069783, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9075455, 1105.729781734292, 68.40879863446266, 959.861192155361, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3804691, 746.1160685056864, 142.28024703036274, 251.10798282319547, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6872969, 672.7071130125165, 51.05586657486977, 581.3097904097946, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4049970, 1221.2731425763213, 169.5904473092092, 952.0403725099413, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6716729, 1184.940485123536, 89.84739565563927, 895.2690505122529, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3010495, 672.8472878402914, 287.06149737428944, 516.3326424010631, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2666535, 265.4282457527747, 175.27850697209797, 673.6006234906013, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5901056, 616.6243187602, 69.05127316023044, 550.4602840847762, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7384931, 926.1207400675933, 35.864200340548166, 740.5043320268384, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8662185, 1386.2084957866098, 58.64920698753394, 71.80257256562483, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7126961, 1100.0659964633387, 264.0007965892113, 440.52893707349597, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1731082, 645.3038360890638, 112.66003977640648, 705.9790516272585, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1393069, 1080.8551578727136, 23.8774543401941, 914.3724092270755, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3219580, 582.2269613084841, 279.83372177677535, 304.68846009564476, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7625760, 575.5435813552855, 258.41410869113605, 16.453693364327894, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5349297, 555.5343004728609, 139.02326649742122, 937.2707834302992, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4636877, 1069.7052917476099, 146.7262985505323, 674.4610873403769, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5608758, 973.5889586140023, 212.27180730844356, 272.8427274787677, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8583895, 1316.8982522217002, 213.36911797189038, 958.9826073932355, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8547769, 1059.383237060044, 156.39855384606753, 78.47507312571977, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9771071, 1222.1125728409459, 201.7197592324683, 162.90369013742534, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7124957, 139.61190146489392, 77.38741749976568, 59.76388063532057, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9310553, 649.6563872671176, 68.70613118060837, 917.3245818296356, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3939910, 536.5426737961714, 107.78571236375234, 67.99401350473954, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8701300, 683.8034438839884, 298.8187752502387, 314.22036099324345, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9851126, 1380.4423254200697, 243.75154932233696, 43.82633732154484, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8876531, 379.26981614055603, 110.91553643463823, 489.63020090419207, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6001708, 731.4797438347439, 253.4682428972561, 937.6514281021209, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5592345, 901.903372256411, 162.11227129676863, 824.9071281861518, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8521337, 1112.214238239439, 27.60926862104611, 526.3870132548582, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5206982, 478.76578237878255, 168.375652401132, 188.81246394572992, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1425174, 760.9403383966923, 138.60435105463807, 234.10806696244103, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6361531, 445.36031214294263, 72.32391128784093, 265.1124283222781, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7641636, 868.9028630867531, 230.93645987432123, 890.3760934258669, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6779055, 354.5397563515576, 182.37156938571792, 693.690786407223, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4176575, 658.2279668467751, 27.118881176315163, 992.1595806484476, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5454896, 1079.8057786036982, 241.83164671172463, 324.12536550926507, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1951269, 328.7052060754513, 157.75344959750296, 690.4955136608938, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3016306, 512.6251848087552, 224.6956358383958, 365.8187974882525, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3695369, 1184.6493042934308, 165.60518132568941, 883.3913828181036, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3439005, 1491.2902832728753, 225.83388616170518, 173.73305451275056, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7010852, 487.55344659365164, 196.06803966659172, 288.8262043383941, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4608722, 211.25911393832018, 139.10090323793938, 565.3635512012697, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4990200, 639.4863156793938, 204.4978855057184, 357.4976896427361, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3599900, 722.6849054330208, 282.71176388916643, 222.61045797708167, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1308661, 162.30202725210972, 262.78892414187305, 746.7054017489595, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7093917, 381.2032470155093, 77.6013326648386, 832.108843422244, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6129697, 1341.9228267369424, 204.20688645481886, 492.99541459035544, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5787044, 796.5944611983039, 32.51064286053369, 619.6486937679834, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4615800, 67.4303719989977, 204.80071471115266, 623.1829435476191, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4283104, 1008.9072663449866, 38.399310447858525, 234.870619332629, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9082648, 246.24525069599224, 172.04668316991328, 842.7759633178518, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7010852, 859.2232842834626, 186.2479547295316, 572.0343829661823, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8036188, 962.841560614648, 53.49216369938373, 808.6324700714501, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8818406, 642.0360806423784, 56.63932442803699, 172.278172738799, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7048967, 1059.4655197831867, 264.7636364592379, 902.5414395209727, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8504440, 1230.8326621255094, 253.60001717170275, 118.47649241934666, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8205394, 1358.0942838380927, 226.07254392504453, 797.9880056847888, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4421959, 1309.4923397887321, 229.76776371973375, 651.8728047907698, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5669521, 104.98262057610194, 14.509223927842354, 244.01305985234168, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4492837, 1253.3368885509853, 209.25064215311858, 698.7439200768157, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1399338, 1072.586745391321, 140.74702219915252, 858.2788557899539, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4386814, 344.7472050780504, 88.69184762339944, 619.3641158109184, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1633180, 984.5147800444536, 119.69011094703463, 689.3172363837629, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7380779, 450.81315953501525, 150.84152660058137, 171.16373601109936, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5185922, 304.6982267398611, 18.131276251834127, 47.69570973490633, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4506688, 1346.1692324849143, 142.45213682395084, 272.8291704459921, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6646700, 1081.3664602318474, 249.6109969973877, 224.22852670568938, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3831712, 1420.2262400074583, 212.98034303823127, 394.60296765942826, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2376505, 1411.4846577210553, 182.13908445071166, 378.11749073687895, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6960741, 788.9658954560055, 137.68754847004328, 422.2268683017556, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7756506, 785.589358584747, 182.3973596571418, 768.8326247043076, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4012596, 1382.506519354016, 208.79278367253536, 218.30299781251873, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9159559, 362.8905634606589, 134.45484751704288, 895.2503581044346, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1907906, 1369.1544114347962, 217.66564965605056, 754.6059809299278, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9306204, 202.22079020502053, 109.08755199323788, 34.01106666593279, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4372779, 796.8818900269716, 279.27316665931755, 725.4492289523545, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4758320, 391.1653867368527, 24.664078392646744, 502.4950098438862, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4506688, 934.8522943551402, 18.373075317997106, 161.03681143741332, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8110051, 868.6147772464329, 187.14761401515426, 898.2689467181226, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1333361, 51.37011711770265, 50.82515267589089, 938.6115870836746, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9025546, 952.9099629785931, 156.42736667120462, 961.1119385563748, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4075671, 478.7721561554454, 130.8147732513375, 559.8990577555462, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8807910, 339.6918071836082, 128.0860868043628, 847.6075944417071, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3284657, 86.73716678854302, 70.69431954821174, 761.5852183116217, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1145922, 1416.9930510446711, 261.37919711751493, 283.17365403282105, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8984601, 1388.510194219311, 58.75035530020032, 430.73209540747746, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6202020, 790.363156676319, 262.1386278499052, 242.418288244867, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1136428, 170.12338365782418, 85.26481814009495, 140.08928062582538, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8159183, 718.1018847666861, 245.38015904246603, 298.2338612755736, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6428264, 621.9395786061418, 223.8895800758721, 53.16921241317984, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5238197, 1376.6671218081024, 121.58550521791936, 408.56669720805985, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8450214, 558.9469457031214, 281.6790593781997, 745.3327260673162, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2279369, 740.1567510889805, 144.5924830790711, 30.259859071683827, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1871300, 873.8571976017648, 73.88677956736274, 295.50766741029764, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1623781, 969.2154775734886, 54.778936346467844, 234.06421829269053, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3183782, 130.6334566234345, 23.125669221270456, 122.46143733027593, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5624426, 333.15826461610175, 92.55767402001877, 134.05843319355958, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1515728, 800.6706020497892, 44.5173757874685, 972.983257197447, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5577893, 946.2816780863703, 125.00148922398613, 25.431925499892927, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6010216, 372.94830956080995, 222.63941396312902, 774.9219394049593, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6810619, 211.6736262971206, 96.2978237174655, 69.75355048207665, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3686733, 1207.372962718121, 30.700167664773204, 120.8224347437431, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3785046, 380.8754106684288, 187.86113402143712, 441.84545328887646, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4305137, 1241.2453685582402, 246.04391404057117, 629.4596201649579, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8984601, 571.8213156814036, 160.5176900473396, 381.0488384395904, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2703927, 262.44620326676016, 52.24014400185756, 947.8509321643659, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3829098, 1217.0840124743202, 191.96288259386745, 103.44201833790989, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8390811, 1177.8892153194413, 101.67414371661629, 704.8584720828659, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4545850, 1065.4113994516638, 14.275928627144808, 231.5964441211673, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8795512, 842.9909095236384, 27.485869571776835, 342.3667036958745, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1089024, 853.1792525011007, 245.34767536315874, 241.44198638133773, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4216718, 255.9791209064996, 118.28346201119834, 762.0654438890581, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4264342, 188.3740716100513, 163.16216307624163, 871.0383930631378, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7212597, 947.5818696569615, 273.6323001591111, 378.26159503225796, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6215604, 1079.5562761063022, 116.9377871988666, 956.1693605303053, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5761338, 809.7314943042043, 17.78895967353799, 811.2382055247551, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7384931, 1499.5269376471228, 55.452898629167784, 965.9199218530938, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2961201, 330.08213989166535, 285.2688744906723, 684.3485203412204, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7003855, 468.8365036215887, 12.62683347236716, 994.8853603028747, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2044101, 1087.6797035952125, 210.93705892967023, 36.534538718009266, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3525487, 101.64603110850993, 88.71531508270758, 366.92712566400223, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3598706, 1482.8718624029104, 292.0483642313581, 521.1152625266241, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8205394, 319.2812894907945, 267.0010468049372, 736.2955947994724, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5041240, 598.9655861842615, 146.8641922934133, 545.0367770245172, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9299563, 99.55967617845289, 66.71596934980678, 711.5053354855945, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8019586, 236.8801990583916, 230.24349680142996, 915.5296996150153, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2602306, 92.1801652177113, 123.51615192455328, 968.8392190655928, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8446690, 1380.324814825973, 133.06922855704642, 43.505446607955854, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5847938, 50.248062977328466, 280.17480120333175, 731.1144436143646, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8870772, 534.0377730481633, 216.45299140980197, 301.68670334388713, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4800232, 1312.4268707579915, 252.27729893200618, 240.93891737981355, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3525487, 72.62942504967653, 194.9495010805304, 456.82198949144157, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4238719, 870.6277117280902, 235.68506544299518, 558.9007892406632, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1623781, 858.5923154437197, 293.6407266590783, 672.0278110383616, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7692330, 833.864839002103, 173.77174716318268, 787.0876948729889, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8260979, 294.9134161314533, 15.899849897868474, 953.6570159636793, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9632599, 338.2477364556577, 122.1497195673501, 494.2200893905736, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3551594, 921.0292012913341, 278.38978703449106, 159.22392587940536, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1785524, 760.8064432942556, 276.8021853307388, 557.9537876388167, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3178558, 175.6053346852675, 212.21236643863494, 841.5649817667411, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8406864, 246.13751032028378, 13.751321454457706, 262.3042833713152, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4407782, 1119.136587493876, 85.61504792100045, 687.3358771719373, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1871300, 1065.6836930704303, 293.954350680574, 674.0937826314757, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7682748, 1413.6216405019936, 279.8448211442019, 119.57167628632692, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7178612, 1366.0448616797335, 39.83896396203417, 849.5416260055973, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6512178, 140.59880076543251, 42.53455681373052, 341.60214836426053, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4386814, 640.819328927819, 95.73134830679291, 626.8734544768549, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6374168, 1346.3946598478585, 134.3557685779764, 178.68434105532793, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2754985, 975.8502146405527, 189.10595938552885, 854.3819416849345, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6193857, 571.5706636453526, 234.5791735077942, 25.586691617920266, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6785085, 681.6291838506044, 220.931090169361, 815.5184090212756, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9465679, 1142.5533309709328, 156.9960071620894, 824.9676765787423, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8227420, 1174.6014712712179, 269.6240333097991, 473.40296323510364, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3940539, 264.1708342103444, 108.65310293091531, 94.65982187077215, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5919107, 1358.9805683416894, 192.2371979713636, 125.58431258131797, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7384931, 336.7227903610278, 24.740084206927754, 904.670954374343, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3809463, 1168.784700966215, 52.44907334226043, 316.0485892806001, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4047157, 308.29111713736006, 33.94700981128756, 708.3530426713263, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6222472, 216.89846210799266, 230.33565321534977, 15.634854083850236, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3983543, 859.1647350608082, 196.3316752833188, 986.7883859006403, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8463042, 525.5366676175856, 13.430390861506268, 618.1710241023778, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1973648, 771.4203839148854, 195.76141845414801, 54.99227303082777, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2690461, 628.2504757175637, 284.76385197793905, 265.75628722502256, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1299400, 596.3777726222432, 295.40000177235424, 492.55744284150705, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9548914, 104.24032278122786, 287.9996864071983, 873.1770043201969, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1333361, 142.600267498326, 276.08052890007264, 945.7899243887201, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4703264, 194.86163730672826, 272.1586107279489, 65.62985343696093, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2691695, 583.7714117357103, 58.31213689752365, 699.5069499488728, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7243928, 186.36845758635556, 150.4482378507126, 85.82967746920956, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1689962, 1458.7676306700014, 217.23169188955526, 724.6489218499536, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4787491, 724.7712481185912, 222.08244196758372, 126.07762788604427, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2476885, 943.4166623038099, 269.9596055995586, 861.6104645373491, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8501450, 130.93421324300905, 93.1634091323907, 208.06270983804188, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5040756, 419.22772603954064, 64.77888573671228, 710.9097239694828, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2512865, 555.769506421584, 296.6538254331288, 525.1375619786259, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9955288, 435.49235275167376, 194.69167452931654, 372.45335744785615, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5862207, 839.4513987492563, 192.15248108645824, 392.37719889012055, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5451310, 274.3908126024319, 281.10213866416717, 139.19760059185947, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7178612, 1271.7261194269774, 209.62019559504682, 145.6038543474505, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1649816, 65.64830596564128, 57.22893041790578, 495.0095102839548, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7641636, 1271.3692992193542, 53.05654595649299, 425.9284527911938, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2761510, 335.5263479320445, 142.67116468285454, 768.6176032228376, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1848426, 489.83810630000664, 28.525862903954287, 397.2544796469728, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8012149, 1149.2356496122443, 218.5052166874391, 883.1133820916066, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9609632, 163.81614787673115, 57.26506096370721, 823.8668173363316, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8529390, 136.4290813904443, 211.92073059717774, 604.4116441062532, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4262048, 167.22332459671443, 234.47321369854504, 116.01855454706, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5743018, 1006.1957972937771, 88.19131547306299, 541.1461918920193, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3578181, 105.2403489724613, 77.93564469578992, 944.1715967759449, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7175871, 608.5049354405588, 54.96310664904429, 865.98221546457, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4765828, 896.6241162366609, 196.849074510898, 86.33437027253154, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2922171, 643.7626996244559, 70.97165677886699, 742.7092212045557, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9863068, 581.2704212902739, 133.8509992104781, 787.7714144550049, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6811678, 50.009794777218126, 279.71851813950633, 124.05372875153431, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3226670, 1486.782117477376, 25.349500235623516, 465.01093592851083, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1338051, 327.40656482531494, 16.28242627287585, 605.581429184501, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7418489, 1036.6238994588352, 115.23669207208452, 3.757803056657738, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3213297, 686.5026764349907, 14.935346788763914, 509.1309906370968, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2703927, 843.8781524721106, 219.68709580034073, 467.172390124303, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3949560, 430.86234733376153, 32.38453502563375, 956.9259206116985, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8583895, 647.5817416729384, 116.1310331558388, 20.855038867198527, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9222035, 673.6869156533445, 254.12906048509902, 724.9929829675785, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2848794, 172.31959587025693, 276.5394103521668, 965.0532810087475, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1684189, 1056.7593449694632, 206.66983952915365, 697.2108549079189, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2568406, 907.4206046907764, 154.6920299204357, 799.8814252114613, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5377767, 589.8637123736131, 176.63554636132884, 287.4661634946133, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9862436, 578.4671940070103, 186.50906124478655, 559.7946894685082, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6756171, 1009.4816074806881, 55.84757636775223, 317.08885500152076, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6785085, 726.9610256385367, 102.01499418516326, 689.4253146008264, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7764934, 1068.1383125992174, 65.76388632561346, 411.63493633368864, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4165264, 443.4883593269331, 117.5723496027926, 862.5773522529063, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5761338, 365.90261881385936, 224.94328627457415, 697.4452952470635, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9257451, 367.61814185639287, 279.9071156037754, 608.8164125365462, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5929437, 855.8249056421753, 134.79129716556335, 353.36186832535645, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3301946, 210.3189997230157, 17.904644558504703, 908.8302570561538, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4353505, 1359.370033470808, 245.11382377707702, 777.7829807112618, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9075455, 637.0950081319905, 71.54803725147548, 908.8251646853076, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7680566, 373.6211734597104, 119.44308336745797, 543.925161903529, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2613937, 1192.7885147180355, 54.25885569988611, 457.39825373837317, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3005312, 949.4614557093333, 123.60124433082224, 92.32134471368613, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9684543, 1208.147496488001, 257.10685739623864, 910.1934190453019, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6151320, 775.7572571025592, 267.69523065960084, 187.94169396051575, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3977103, 578.7326897044331, 129.11288732967932, 819.4754778390501, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8332529, 471.6161157402281, 206.25671092184476, 657.3736201291146, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7174126, 222.47429821812625, 97.12627052302821, 727.4144196460088, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1587102, 900.2364541695323, 41.04663929972459, 527.7208313673073, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2259747, 1065.329755977069, 129.76674191243887, 290.7542189491916, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7756506, 608.2801562509419, 33.18287495527244, 266.6296504496497, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6361531, 1143.72269401873, 270.2431617907096, 733.4914206235603, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9900238, 569.7401888893296, 219.06739912585235, 160.51810451217042, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4761870, 805.0631176099486, 39.467702170099386, 453.63053526656813, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5892423, 431.8521145770813, 105.24029361559528, 756.2529033084388, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8231218, 1213.0390243774843, 141.69373359352278, 726.1321432485963, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7013992, 1295.1653916794373, 278.3910569136763, 966.7526401019452, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6717934, 1247.0782141120721, 275.20649797877707, 699.6908708410494, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4545850, 740.748770712676, 173.25327115911801, 246.9601344664022, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7436638, 224.52437065107983, 102.50694229405882, 797.139957841925, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5197731, 181.2597176975124, 29.377804201189573, 709.2254822399668, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8178330, 234.1392309640996, 146.27555556215844, 723.8281751795744, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8968179, 332.2588113846701, 209.1813045511665, 161.69580106931426, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6442671, 811.8925888339039, 50.99831998195068, 848.0542315000979, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4012314, 729.3629392875713, 165.38863725078792, 945.2202592823071, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9626498, 1375.217153891687, 33.36127708558533, 765.4944289012807, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2680443, 1488.710560918739, 70.00173958852571, 397.6562656842589, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9859363, 1096.6037339809263, 230.1758290464589, 601.9231597802818, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7067625, 578.6762764184676, 135.64029110803403, 36.375704812657176, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7204967, 1078.7046960521998, 263.4546950090154, 165.64825556908713, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2961201, 167.76282740224457, 284.159494596969, 226.4939815446766, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6087178, 1110.0981954673175, 227.87080098979493, 466.3984009280389, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1089024, 1495.6828590187408, 159.55780974324034, 275.186551120281, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7212597, 1487.4318150253534, 182.97271608174873, 224.72739211147675, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8654339, 738.6021596961938, 237.482196562381, 395.41230182987374, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8638856, 1248.5356714486, 275.1564565382741, 75.23734732161924, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7008332, 708.0793876556775, 10.066327458709504, 200.2421866673385, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4486351, 600.0051035018269, 19.28264947107352, 218.58334983780014, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5743018, 731.7217474787573, 107.27889653026652, 656.3643223935879, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6568737, 374.6383862501148, 160.48631288619288, 507.17125106232186, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6598273, 480.6614561277951, 276.1885145613309, 613.9456999079338, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8654339, 560.279691432433, 39.18580979688764, 790.7168881939687, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1099646, 112.33341656841228, 228.9077744678045, 330.29894284723724, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9786264, 785.9330887222127, 196.25796698633133, 776.4691511464653, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8568894, 1338.8999497582731, 240.2004633962005, 128.05856785156033, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7638577, 1201.2479499734459, 83.35299768877564, 783.1414677704105, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6039800, 230.77848665487926, 27.052847023286265, 31.927661255296556, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8539530, 1146.6097860325485, 247.34400439838686, 618.1041817682994, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3940539, 55.15334628830085, 219.53122678462603, 316.9752840170037, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6239727, 849.3152997264763, 53.80702421755881, 229.0409114255255, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5211411, 245.95994423763028, 228.40483705650317, 504.53983132267075, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7970883, 901.6561941853704, 82.77725884164849, 284.6710663872284, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7710596, 216.50762332614485, 181.60063724698196, 562.5425696885379, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1623781, 1167.2482675104482, 262.6680106954329, 605.7159071051774, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5053175, 857.5844347730934, 38.25960453475959, 999.709635889115, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9487529, 1383.8951560080964, 174.2620856893346, 895.9776837881155, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2672726, 294.04953993805555, 269.7736361246913, 639.612706191004, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2384364, 665.3739353816982, 204.22354256309242, 362.1806979670333, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9859363, 286.262682223081, 252.1852182094185, 795.2383041401548, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5037793, 696.2649167499425, 211.5405043802967, 594.5714233618642, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3787090, 365.5336728856616, 127.17483291870116, 241.3601194927508, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4264342, 1476.9964849437551, 177.8033655485638, 544.1661275773714, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5983602, 875.4434087830294, 45.63923253212922, 766.2510425616318, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8140881, 393.73411159138374, 298.89224027568525, 181.29406913154256, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6361531, 1199.1032501555703, 138.21147015326034, 930.4810391984295, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4732178, 864.9914581521813, 191.10340634812368, 269.50317855745595, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5735932, 1063.3475048688506, 11.335588054674746, 870.1658413859019, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6853677, 1495.9223499160687, 205.06586404966856, 344.5813243961069, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8401928, 1453.7086939288758, 184.64555392850892, 9.560362068304794, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6261780, 523.3641307901271, 134.6371984866704, 282.21229710763475, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2641572, 567.7751592658389, 281.4743549887675, 23.754366059240972, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2030092, 219.4773858400854, 30.964487516152513, 894.4790703034904, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8175377, 274.1289348011668, 123.45513835591946, 435.12799019101635, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5901712, 1141.294544747643, 128.149141790741, 944.8421255924802, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3107693, 341.1606190961968, 237.5730233802761, 396.5514118994733, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7761831, 1317.169531020194, 39.23319422593732, 289.212182507143, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4212767, 1064.9951028666803, 102.64274633021574, 938.8874537967878, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5041240, 1132.8946595555915, 157.84136910972052, 304.06786376399043, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8421927, 831.3467252947312, 193.12341402139498, 736.5579744511521, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4283104, 520.4191798238276, 111.32666204843447, 628.0445597042906, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6064693, 735.7388883991135, 255.02812552991017, 33.40731335831026, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2384364, 713.2601407193903, 120.85852143750884, 300.59706978676803, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3094250, 536.7465834414372, 31.886539037591184, 141.40229393195824, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8760021, 675.5319532959501, 142.50833288786882, 220.29933728180794, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3165277, 1331.7609885290838, 112.13500458740457, 349.1026650418014, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8165692, 1022.3191305652532, 286.395173886653, 899.0264142524031, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8650575, 1208.4399099992183, 64.88869435410551, 604.1764621127915, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7820234, 609.9282481513404, 269.28277815158964, 28.500964386415582, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9609632, 1365.1706860728082, 11.264571478341464, 720.1508602310175, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1111874, 335.9134815112083, 40.62340821384421, 320.24864996554624, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9075455, 1440.2540128076112, 26.433757943320405, 140.35630441031677, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2113573, 405.0460744959107, 208.49132570482948, 889.5322167699532, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3643982, 198.84920574845307, 14.508321472700628, 91.96336871529154, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9912437, 309.1756378745722, 189.25630389963587, 467.37307200834147, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8390811, 1309.5703053186223, 289.6647972236914, 978.0285938869926, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3068327, 561.0711364510187, 186.4979080561129, 485.97794360014467, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1356035, 1405.8184703017196, 93.47318038821751, 863.5228094915351, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6717934, 231.1740336926011, 253.24493761980912, 322.3748146779625, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4503634, 597.2082405875326, 287.4548768411522, 246.35493722390956, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4372779, 252.765043241822, 53.45391272169316, 961.2291516184898, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2675574, 229.96186046815816, 228.54759636298803, 372.80626900006877, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7886125, 611.5986806875803, 16.53284407485817, 358.8747560279176, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4264503, 621.8985654028926, 253.3893546171791, 919.8224380063019, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3422249, 1076.5676878294764, 29.85845681150445, 6.879951954022578, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8819841, 380.25624045701625, 276.203713091158, 317.8062853272737, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4991480, 1105.2403251992448, 10.691097335424452, 958.1544697819636, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3999564, 113.54377716920298, 61.93783314368671, 717.9896921264875, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3154105, 1390.745896015019, 268.7620240143232, 734.4143111390733, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6453455, 1227.2744771850448, 21.84388867137323, 466.53320314996813, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2613937, 1370.3660542128312, 230.07157357867501, 917.1660816393245, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1689962, 1210.0662704792705, 65.84161555371563, 419.6604985311844, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5919107, 1047.9054609039827, 25.582262844369268, 851.0262835720077, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4372779, 368.47767263168254, 126.53556675747423, 177.95076953720078, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5743018, 1359.922431186487, 283.30926907900715, 117.36803248822858, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4012596, 1492.3355654711559, 142.2137512977484, 837.9411373705909, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4008721, 452.26947951475137, 78.4611277986236, 100.06463230485352, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1431180, 1083.0244871730335, 106.90667030965243, 235.4922285334692, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7708412, 1113.3403456763162, 32.248284676344724, 851.6673615405552, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2163221, 911.5594768328172, 225.18573243643257, 468.09425641577576, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3165277, 262.04236733121337, 171.2986611466433, 271.2731589107953, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4126049, 992.5175772243668, 118.79016692062044, 310.7698146834822, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5045110, 1469.2999155789203, 131.60450412477968, 455.41260931512096, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7182698, 239.47231095523813, 59.41268123180253, 49.06935938653933, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7889117, 73.6381956556649, 66.01572286440077, 875.5989945330504, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5250963, 1008.1743162423222, 93.04893760770172, 924.5560233690676, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1309610, 1195.0997993297406, 180.3538123972729, 190.07346795310397, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3578181, 1310.2284359081705, 185.34931785152864, 124.15839786024429, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5349297, 1198.1188831800143, 176.46548371294082, 707.5861005618251, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8678382, 1197.3533498244378, 109.76809610801949, 142.3461782048958, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4277940, 1309.4207749232583, 177.35371419145193, 78.30801501092589, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8527627, 1226.313258510418, 100.16274605543619, 546.5560343925067, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2795819, 775.930650340775, 219.15913922290173, 977.959434102401, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6064693, 471.8800124454731, 290.8764121435855, 236.5632694248093, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2568406, 1237.085865070993, 246.42706708840214, 236.81373180887522, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9511613, 989.7672499524244, 161.6947946468073, 333.71284653845856, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1248456, 658.7680042718915, 122.57946103779632, 470.10525851984994, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1662128, 189.26478414950301, 193.47006412831655, 172.44843762226148, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8504440, 674.4309707223962, 163.42652428228345, 853.1795137128594, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7006988, 1376.5396013335826, 253.45008203969184, 428.26031222559516, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2897716, 1408.741521093105, 135.24273174783363, 580.7618567166099, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2541475, 311.6727281080056, 281.74208508813666, 178.6969804702808, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7414161, 1138.5548728537244, 295.0818656062461, 126.28632212077574, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1951269, 735.3054253329348, 173.4708909019694, 984.1281695147387, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2568406, 766.8259154916061, 248.50091784128787, 809.6574645292324, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6085006, 347.2744690115131, 153.95043998750842, 134.37278870211367, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5077999, 1252.929375856929, 190.46970151215712, 307.9764695138777, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2386881, 1420.0863245465032, 179.34888665313284, 38.87400665183116, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9352451, 378.4389192001042, 179.12400081173465, 48.64449710955632, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2983965, 1025.3056802191313, 87.08116598909778, 759.5670346370296, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6584934, 959.4053763390804, 38.98020932071182, 289.5788412345416, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5373112, 1327.2712887835357, 59.02592385340323, 999.3514608141165, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2068071, 1434.9139191621575, 270.01519362547333, 759.0602947014967, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9156560, 977.6424569704799, 207.890021196875, 506.3012078288608, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1393069, 456.21255797893014, 273.2236184012412, 678.3916725121627, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4517235, 1169.3461755103958, 50.61869496213113, 357.70864959099237, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7638577, 749.4260513315445, 284.72413052982864, 297.2523976073128, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9204189, 546.4040450316965, 151.5337335159239, 408.4520506464955, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9684543, 899.2575375896631, 113.40080713291441, 532.9736616040852, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5681590, 1114.43622172912, 102.04190166361437, 978.5804046071314, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8230145, 1422.9882083278003, 249.77375556383538, 804.7317054975979, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4238719, 875.2916109595884, 63.55346922222295, 694.9744501245985, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5933406, 479.38864852231217, 151.29129457980312, 501.89250303918607, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7178690, 1356.501125300586, 32.050196260744315, 850.2241572308224, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3439005, 821.0925616801663, 157.9669596206747, 702.2762568557721, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1701192, 276.6456283548779, 226.8627073284682, 282.85446450953043, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7238602, 836.1198709952495, 82.95794489837277, 122.93574724681555, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6291782, 881.6325168342823, 178.38714691038547, 982.5163138407371, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5051841, 987.3357330919424, 179.86195772368842, 762.7687624427897, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1338051, 92.6849437275743, 31.792498428829614, 313.1481117192716, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2750638, 1209.0913146965559, 70.50733046284827, 187.45112302174104, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5885445, 1487.9805895717457, 200.82530346364197, 635.9374146686482, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7124957, 1044.8606263796296, 260.47497681689896, 615.9785316087026, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2680443, 521.5998419096602, 169.50373305860455, 622.8230828453839, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3743456, 849.8529827373616, 278.6118470525077, 365.94380635673264, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6960741, 637.5232360279406, 60.80455069026937, 208.9527236998704, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5454896, 829.4009510635206, 146.27765706677553, 782.0635332028018, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1718010, 1130.1688384652884, 90.3695134498816, 393.8367824225436, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7010852, 635.7784725211504, 176.4815793440714, 560.6412438252884, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8553669, 419.33069744920135, 52.774360097518034, 125.268705000796, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1372524, 128.3115617551746, 275.4536782677627, 292.51919950708594, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5954034, 932.0951645188579, 254.53877433622586, 375.4277926772277, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8684164, 1390.3320073865148, 253.7297011789632, 927.8834908467134, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4099048, 538.5298821679967, 179.5698773656901, 666.5724263416294, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6717934, 1387.8895539562461, 283.8448132625028, 193.01931719563115, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1478186, 282.96434852072105, 182.3494295743238, 682.8239646109292, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9548914, 1215.6572976465106, 158.1654212327083, 544.370362837211, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5185922, 1257.805537344149, 282.88811663960655, 885.778988013106, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7391811, 1068.7351464073588, 146.03780544116037, 579.0568850094169, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3461672, 1418.8539606835334, 96.34918597688052, 199.94492113703222, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5196342, 437.1377966134104, 178.59563971087698, 147.3403538594499, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4008564, 735.9313382241822, 23.35144117757192, 185.92086724395062, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5483275, 1478.3070500193367, 145.57769577475648, 730.5120195047164, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8265136, 719.6818300043542, 76.8208149692223, 715.4121658718869, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8160670, 116.97651908018081, 246.96156939808748, 276.1187465464835, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1718010, 1384.2881204432804, 287.14905904052347, 34.8528847745202, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7376714, 680.1070152993829, 204.84686593424263, 51.42076879119828, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3643982, 1465.534136997706, 136.69923251546072, 993.0527364931866, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3594207, 865.5355028853476, 120.35916809925764, 384.11799492639267, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8749003, 1213.174815087208, 237.70741699794337, 207.6577691764374, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9609632, 1185.9030995978967, 171.09971781198547, 22.03667528878672, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4746635, 194.94850893531793, 208.38633794250836, 692.5827936958326, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6222472, 1100.8171080844563, 178.9951581391926, 922.3613452623869, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1538571, 960.5503836340774, 79.80126128474262, 506.05477997879535, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3328283, 1358.5484170370873, 177.90150535984807, 123.04872848535409, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3264812, 415.720133945544, 253.64093566988723, 962.258740775694, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2203629, 618.5115854279935, 57.121284382120095, 88.33315451829927, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9050693, 395.4354669664432, 87.25713232993107, 199.91171368585114, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7692330, 649.2272581561185, 68.33861499121818, 45.60314523439413, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4126049, 1181.6829369545346, 198.50719279143632, 502.0219884709659, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2956214, 1411.9251299392479, 14.63169778808053, 117.57032766014063, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6756171, 184.7526458407517, 250.6021781904358, 213.07969544427053, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6374168, 280.01390176486075, 205.8828861309757, 770.1038987030839, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9662347, 278.1270358952247, 230.78160986690304, 268.856483856157, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6646700, 663.7413066276047, 33.855683001656466, 930.0335280375489, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1936932, 730.9218108640331, 139.2461232228302, 996.110035055814, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1432793, 373.4865672152221, 73.64566219905214, 949.590243735814, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2279369, 869.4196293625542, 187.63156205344265, 887.1198035608508, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8556204, 90.39783633955989, 127.39251566856957, 636.8865624454403, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5290873, 592.6405648704836, 262.039820740423, 593.4892094971857, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4472126, 1238.9389011547823, 237.11878026655452, 532.6110124906866, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8968179, 1351.887852659905, 196.63716557795482, 783.2186821668705, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1197289, 644.5113555807393, 153.4919921685689, 677.4713744272299, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8175377, 1150.6795338149057, 13.028047507244615, 168.60505817072934, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7820234, 1304.3296448454375, 175.49088874765764, 984.6029522645276, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4523702, 124.22380938321042, 261.3575944892626, 522.8686631031799, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6716729, 654.1718908912385, 160.9281632763328, 249.4496773602073, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7799687, 426.3297614789565, 206.04732662731632, 94.75845396038729, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3366919, 868.8246661269545, 35.77579700946705, 441.19942505839714, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8798651, 1297.3206367012183, 35.254652077813816, 554.4357422660884, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5962128, 1031.9774266423717, 135.02000097220423, 401.32609014022, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7796733, 909.6270852443411, 139.13430771086777, 716.5599072735578, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5870313, 884.4284836637705, 155.18449896879235, 142.04611572781778, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4960922, 348.92301559714315, 51.32618435805286, 275.5354062402874, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3641027, 286.64899874490476, 59.4494268498662, 102.06511337323786, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7167852, 423.66554327803743, 63.82016173712634, 161.68371872512478, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2579214, 1433.1636844264979, 201.56809651111354, 726.7281240148454, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5624426, 743.0366768153775, 112.47337462860648, 389.72400373624794, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5990996, 596.6959524937613, 146.71692573258292, 15.246951057409364, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3956391, 613.972468744849, 62.51708161474198, 28.53929812888756, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1208366, 1384.9249313088133, 197.36282634984116, 25.59841804292695, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6914575, 814.5313319849886, 287.15224750620746, 977.5560662258828, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8134779, 1484.9960211194746, 49.40026650596249, 707.7805827350797, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7470490, 703.3112537579593, 209.54869351590858, 313.865976500884, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8136247, 760.3652940464546, 103.70475195010097, 178.779091500912, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5262734, 1267.21141394994, 282.05693528988024, 555.6613365059872, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1960793, 813.1744697353337, 208.42042761711815, 831.9839564505662, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2675574, 269.7219010339013, 259.2274504375125, 588.0403287730232, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6005366, 365.2256072650095, 242.16357660312653, 440.2556925090525, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9771071, 292.0749373546294, 146.35507404798304, 249.11923730582743, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9690243, 755.275091808277, 111.82855506157979, 134.76476426926465, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8047880, 1009.078450289081, 64.60014582948165, 961.9202956951378, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1684153, 1309.5358345451516, 280.1023858950988, 630.5276305194631, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2641572, 577.3951925974316, 118.68893085021615, 480.19786805718354, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2848794, 580.5128200269298, 214.838557271507, 37.737413946034025, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2202618, 826.5184002304682, 157.01107099759065, 358.5232903095151, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4119119, 136.95899455740192, 56.4125317419252, 131.23810223290255, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9859363, 202.973785859083, 130.4975297626126, 526.2998263796205, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7871858, 1493.9788963243955, 234.32486336839955, 136.25603148475264, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6558595, 767.7587112952983, 38.34992875822435, 993.4485776211586, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4012596, 370.846057037726, 159.56967647774582, 788.1228971680576, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5261503, 1048.9538201003722, 19.43457671522752, 119.14587619570017, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1701192, 564.3324115994811, 161.81906231752595, 618.6908355926889, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7728809, 479.153059577058, 203.80386702328397, 552.5471444274152, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5353054, 189.60934736322022, 252.73533389592092, 84.7172296779437, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5007808, 678.3463012068405, 92.52046506877365, 380.9324466671864, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4733341, 133.98285388621716, 108.39246415446969, 570.3055583511189, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6829687, 718.6394808194149, 39.52641837594483, 654.5926080806724, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1327384, 406.79276897636805, 20.577996500817623, 854.5191883212609, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3056420, 1377.7910964248117, 170.70760731481982, 478.1460168949575, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8036188, 269.39133624366326, 253.5925084258012, 171.53520035271308, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8813465, 650.2973327337028, 165.79997206504493, 842.3049824518106, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3416889, 1430.7197056098719, 124.73226021339083, 662.7732254689705, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9863466, 922.4985952380539, 141.5281572829103, 287.96587671860084, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7252564, 747.8097270725951, 232.24868423001018, 635.3803075320521, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9603207, 1246.9026201179806, 56.04779669876205, 346.61808307059374, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2113573, 466.3386261714848, 74.50337738098027, 837.478773741186, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4305778, 1020.5151597065037, 67.58486594535623, 625.8675490995016, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9310553, 238.96173146179154, 182.44131202681035, 572.623381762007, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3943663, 1432.0734428935473, 263.36213501571865, 234.10817629854895, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7788573, 868.2938979775153, 242.55206871215339, 227.2636225240745, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3213297, 1020.4660339815448, 164.8910982284259, 258.42784857099167, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9319573, 596.0087938899238, 274.89322519745633, 80.25946342437173, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5068176, 1198.8725566338942, 45.275442606528884, 302.8262822545973, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5349297, 483.5158419100773, 23.60398106229615, 630.5553940464387, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4238719, 764.4279537867928, 91.3405036616922, 337.4973416396756, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6584934, 1325.7492965218769, 289.43148912048946, 508.8414854811294, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5116095, 1295.8745087024622, 86.85924315629856, 846.2867677512105, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9331041, 622.3168402177203, 147.200056337443, 111.3939055286478, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9632599, 1398.007219181672, 61.6669790444613, 145.17237014965323, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8760021, 692.8839851622982, 41.73825204838023, 929.560861624588, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9300501, 587.6258852144709, 30.54674229604337, 328.8285686259689, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5215599, 1097.803085079424, 73.2974633957464, 595.2609853848484, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5634334, 1420.5968108300883, 201.45896848612608, 610.4018619768945, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2042893, 717.3800605470868, 98.45911828034882, 960.8037297088189, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5690330, 1125.126281823481, 39.48217390907901, 1.4651514912580321, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6756171, 639.2699055104839, 142.10614275164247, 473.69605207619213, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6557492, 1223.182246408742, 22.326808613586365, 162.09714077582382, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8175377, 240.7993898229286, 90.67913577487694, 643.1702674605306, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5390165, 1441.981154344797, 13.200663468188946, 21.013933910466132, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1593813, 1311.5570436484327, 172.06131676114623, 18.1493660459463, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5116095, 949.9552910759356, 122.48812062338781, 611.6850692027879, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1089024, 1470.0328111517072, 128.3453019710398, 604.4870561324412, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3986898, 698.947903667838, 197.50293359826992, 693.6258554839626, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4105945, 470.4796984065396, 279.8721140721185, 69.7652938929314, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8227420, 1334.0745773937297, 213.2246178177559, 676.9567323399077, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6085006, 672.743500404042, 232.30113793256407, 69.88880767526805, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4888508, 405.73291880481906, 125.18031893284468, 312.3166328968622, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4352162, 259.9452832072302, 189.7493251030827, 721.7787005002166, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9457407, 178.5203146954843, 75.75279738477109, 322.0138642911876, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9734063, 692.9357447088466, 153.2626159245787, 97.41151394813474, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7008332, 694.6892457053596, 34.22345193884442, 559.3105848128886, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9829110, 1353.1094190933954, 142.24868784144246, 430.01458060126987, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6218598, 155.3711400512035, 112.20446456530432, 261.59147902488553, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3284657, 67.42984141789785, 246.65733150273576, 692.1097494149656, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2897716, 275.73426973849087, 30.628715366859936, 663.0647032722198, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7799687, 730.6946661520063, 21.276259164674904, 630.7476334428089, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9859363, 499.88914270281464, 216.26328261329243, 375.8494432920285, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5690330, 539.543495218091, 201.44561813044916, 917.6503326323212, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9808297, 614.9261107332561, 78.65104467897383, 572.0209561621152, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1089024, 707.0846260586175, 131.7895275018899, 872.537023273348, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5197731, 945.4027105304816, 235.83270784172237, 758.043690291194, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9912437, 720.5783868527868, 96.6027936121955, 790.483685029258, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8807910, 488.81317571717534, 220.90046542952794, 573.628893289884, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9300501, 633.8773340418206, 170.13655379606595, 117.31447801372718, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1878686, 782.7657325899496, 94.33465432800975, 882.1371890800656, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3551594, 1438.7411512705496, 51.24336961009586, 659.6630920262944, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5300181, 313.3320542622325, 272.7518197842803, 402.79435314416867, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8311532, 1100.830741737104, 72.63690548423479, 945.0931319376617, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3132572, 737.6790801122722, 229.04175131370673, 4.612157927104232, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6959281, 240.217733277582, 148.7992613568184, 544.8118852356131, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2754985, 1290.602553053001, 123.88520168172224, 133.65557315400255, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3809352, 718.5162481984853, 72.37123815472467, 258.89382665248075, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4075671, 1456.9075510962298, 197.89305688892597, 987.5352804827936, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5608758, 695.2211995731641, 275.2581248720728, 732.4265805651911, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2163221, 67.03649369811653, 54.095234430988626, 218.15973720163473, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7243928, 229.6756471554106, 40.48572166011668, 312.28253073965936, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5297788, 112.76381421142135, 283.5453106597255, 919.7231362940186, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3579905, 991.3055678356623, 149.68901794673044, 67.81968077995326, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5215599, 1299.1276558055088, 73.80928837702788, 487.07584236084557, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9310553, 246.95211568314863, 102.07419191080596, 74.29334983862468, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6222472, 1362.9841200262485, 16.950640652112984, 443.6230756965156, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1467706, 391.8831862630794, 44.55747721114494, 476.1430621155858, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2159705, 1487.652037731406, 263.266633085937, 543.3305013945901, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6087178, 321.3733600036097, 113.47740932865598, 838.6487690168295, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9786264, 1210.5956650483606, 13.48734061189177, 164.63652358412529, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4962789, 1291.1686861951353, 281.0101565853833, 930.3715331052788, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2102306, 1293.778223486203, 97.05915149517199, 969.6889724017204, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1197289, 620.2229638611536, 210.43412522738495, 786.6423254466108, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2457131, 855.9013902193501, 263.19178842307235, 116.55098498795391, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7844689, 462.1699958879667, 168.95154864498977, 218.03395579627394, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7565191, 1368.733290324557, 186.75149062255494, 70.76912503971921, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3518512, 1346.4606579918004, 28.937738574556246, 359.942941845529, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8529390, 427.8748018345176, 269.6976172609114, 997.7546829005804, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5211411, 554.7815020393996, 269.2006565029616, 594.3027466048446, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9863068, 806.5425608866209, 165.90633237657912, 630.7907107979751, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6787359, 318.88097803021327, 120.33587387546358, 774.4357558196645, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8818406, 828.9256684747519, 166.34257095387485, 54.1428801737388, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1901452, 771.8370449240267, 113.02809851386134, 523.5388186114874, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6393897, 164.05788890403218, 189.35932138154465, 767.4967848934298, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1116067, 989.8891483447431, 253.60916194662963, 283.9396897979191, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2869323, 92.94218734230319, 213.85485859195896, 858.6243279870687, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6167319, 1271.5236269210845, 68.15351973474117, 442.4761691847858, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8180107, 233.53428565267907, 179.36850536288242, 9.630448233031498, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3328283, 513.4627296197987, 162.63153725994297, 146.69162537484436, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9786264, 1028.3713491491362, 243.16778978861757, 459.30205274182157, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4626064, 70.69807524369585, 29.56877019292069, 983.6845534545082, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9661014, 993.3662394352839, 57.0358478612592, 488.7303371174211, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8463042, 710.1013413890598, 235.5278453956019, 8.639738839453347, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7788573, 123.926138325107, 263.8479498454829, 523.6145022702638, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2701028, 1180.9029495441669, 60.90294989094106, 720.2191857083184, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9298319, 1071.5598704795043, 136.47002875619862, 906.1050926464022, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6163337, 1355.1119106203537, 170.13006233263565, 432.338493653083, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4105945, 953.7204347082921, 262.1791823282513, 252.96471056636506, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9325304, 283.6402174086445, 18.148063308169434, 151.57994437367273, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3050769, 929.7545489655269, 151.28462743787898, 583.4759028046419, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5544563, 667.5381202282839, 183.55951359755528, 762.2877698903607, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1684189, 1067.4820647447, 131.02251578963808, 660.9369632184524, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8311532, 638.0426865924215, 197.7334448343818, 630.064679014752, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7286733, 761.2223034299386, 83.5414735781928, 952.0901768884071, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3817471, 890.5302171608671, 219.79416283513842, 408.47314791000287, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7489303, 118.90906538911959, 157.29299494383318, 998.3021975311158, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8539538, 478.8651738372375, 83.12056486769433, 8.898314776346727, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5511933, 78.73285997811335, 260.1873036973462, 833.0467429849551, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9387579, 489.2405258419314, 38.29059719162449, 9.907577689333703, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2390558, 1048.1142328107564, 218.73148520954985, 676.2379871728143, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9236212, 548.6566028725167, 136.55938829033047, 216.49057433429553, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2564401, 542.9681103143241, 118.55544767304383, 737.574240990962, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6288844, 620.7390133352297, 297.06447012166024, 897.5764046770696, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4925346, 1472.5612110723134, 194.59756063981828, 898.9434185316294, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1109729, 703.6893451194807, 145.9884435537187, 523.0091784358829, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5235900, 471.4167522251262, 299.990036724151, 230.24783769244218, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1338051, 1269.5518400171782, 15.314118821260983, 247.45905977107284, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4960922, 70.80937716077676, 277.586684749831, 675.6573381735946, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1992961, 724.2912890496485, 65.37514597518431, 502.20976093199874, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5197539, 1079.1737333114127, 234.2062399357604, 814.9446187462272, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8737669, 837.507221393141, 94.84415055742042, 731.9862467539431, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5341396, 784.3782651175688, 123.99175417717511, 47.954346419825214, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2701028, 244.57004456653442, 247.64286239372998, 153.17623875597297, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7380779, 81.87147594274403, 215.16671437089724, 566.2687244329345, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5367651, 1148.6292597886022, 256.4898292739581, 18.690840930975728, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8760021, 154.17211791075536, 165.3218414421546, 855.0800290698894, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1515728, 748.7520756228743, 64.09204397240978, 963.3394196014568, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3804691, 545.5511072959534, 155.71411840224573, 476.7824340753212, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7936822, 335.75989612951946, 151.84982690219863, 664.4495012055485, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3010495, 745.8412639402511, 165.61821542382307, 914.6417581297696, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5211411, 659.8610730092422, 260.9594512996817, 946.9682775950056, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7175871, 923.2177577257909, 32.40124165207796, 996.5463279005456, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3010495, 1022.4540285677584, 207.12568151860498, 61.86829679482986, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8798651, 1190.4037681876118, 262.0472176787117, 172.08753683375022, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5334196, 1162.296342953905, 293.90380984854755, 757.291551438208, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5279016, 1491.6109284542035, 198.2097721431785, 625.7482642558534, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1705939, 632.6967450835277, 238.70706165178194, 84.77115287461035, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4787491, 1280.9507847940881, 243.82022754658294, 263.58992874296547, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4486351, 464.74213215569637, 137.1077935963216, 379.13246239697315, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7241642, 973.7376952696491, 223.8049607232015, 978.5731156394176, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1111874, 216.96380536039976, 33.070073715619074, 890.4522672222606, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1049770, 809.3092569294114, 45.096412920652256, 667.4088358962796, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2421272, 887.1387151253133, 206.0636495394565, 750.6519846639316, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3366919, 90.76949262265413, 116.0954643200361, 125.30747442361012, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4344113, 205.93948439967716, 299.7547503974285, 970.2624661777911, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8562757, 410.2092295619938, 65.46892743244388, 823.8725992044172, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8553669, 449.3418584415376, 33.89116724826138, 3.003500864524389, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8428829, 909.9439779811473, 223.07287908805313, 894.1580586644011, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1609490, 525.026944081706, 288.0800689729085, 430.55396732864426, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6756171, 585.0654541118724, 22.387224868149488, 297.44022922568445, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4864622, 1060.7498238792318, 282.1152931944917, 88.53720479492144, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3804691, 1340.596417744845, 53.00145122378204, 744.4195630476027, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3925907, 1428.1736934521732, 277.7926312286898, 230.63222168170904, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4262048, 667.1864973508525, 19.89293588900068, 790.2107046800103, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1684189, 798.5253441388486, 136.91368526065327, 161.0273386454173, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2377499, 695.2804550900315, 292.285753116618, 584.6593288583288, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8140881, 1321.1944317372236, 170.09412237388744, 941.4223368109008, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8808690, 1158.7971018179028, 89.27841332400962, 741.1089770576385, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2733791, 1428.4898097806201, 43.47188106217115, 405.27629587351277, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8739603, 1011.4003254216365, 228.44509235763294, 313.86087033123187, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8175377, 907.4991872889516, 237.38212510925953, 117.55447824505738, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9334615, 1378.9982411966166, 298.1947545747737, 557.9890543951685, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7175871, 1119.1590362667116, 145.10858693833526, 810.7334651733755, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6286856, 832.2740953071373, 164.91223967740476, 517.5243403245566, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2739438, 937.8377806334796, 179.12531056496016, 158.3271142805559, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1731082, 1232.8669988348547, 189.13052972285388, 952.7488729350756, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4537593, 1413.6406518513747, 70.214221024479, 782.29764747474, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2113573, 370.95044591997976, 135.65116559256847, 937.7654712576843, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3132572, 816.9654626052283, 215.2157694171405, 225.77585459604165, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1036137, 75.17443622536021, 184.5821236959301, 903.1219160096142, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4888508, 544.9508522248, 56.30624851627455, 177.2341698587635, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5398963, 1359.4751710862206, 220.00464987408097, 897.5611941701303, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5195999, 317.4159258652028, 247.67168867255558, 553.463187771752, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5852557, 860.3401614758726, 287.8073967455861, 471.43694797831984, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6785085, 1165.7303488457082, 263.5377304018647, 893.703489801858, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2641572, 535.8394128213367, 32.75076262896489, 102.35168172420272, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9023283, 519.962417359669, 19.44691841048156, 48.921399353553326, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8501450, 1375.3385469420245, 142.52471202868185, 608.5361403395756, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9659204, 1257.4554246582352, 208.24673075543546, 577.8925130783496, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8690706, 1020.384172202677, 267.14453455728557, 1.055455433688457, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7641636, 433.1787679788186, 260.21745056640964, 895.8399055887846, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1951269, 322.83989380794014, 211.91754088319396, 684.3124243581501, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6377160, 1328.3172698124101, 256.3407701330633, 325.4445835573142, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7339840, 673.4859503361135, 282.02198499096136, 115.89007634743986, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4197992, 727.9432754741165, 171.34872703470188, 79.78360880554791, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6568737, 973.1126168531856, 169.07161500719326, 941.5781983509255, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3695369, 1219.2235199257298, 47.937872392914606, 297.98479411600965, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7890273, 302.4179231143364, 189.85088275806083, 836.5775273490503, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8159183, 842.9385330832698, 277.36831239738984, 565.5326254577554, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7764598, 540.9289696325475, 286.3305818281023, 108.28388618891327, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5971778, 703.3007788021102, 250.10117135740222, 418.4693191064441, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3817471, 965.0663733854907, 122.30651110171365, 144.81427859859664, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7890273, 1067.8176373600622, 66.1957338928449, 222.62636097111564, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3535754, 771.1207580559369, 50.79314331931875, 618.3603238856012, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6646663, 99.94898363323509, 150.9568821895225, 300.99688616183704, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3003674, 417.24391001670824, 143.05069762016348, 319.73623855905385, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6620913, 1372.9945837584155, 79.66565882070532, 275.5210116669996, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9863466, 416.79395178329446, 289.1659974162735, 319.36519393506734, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2947666, 696.3345717432129, 172.45496864221622, 382.4781895273044, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7470490, 1427.7740901928732, 182.5853155413468, 475.4757517612892, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3598706, 1044.6684986354246, 22.639990380400377, 887.8168502212693, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7357745, 480.527237236395, 184.42487121950325, 359.70001064499155, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7048967, 392.686167643364, 240.8492584730055, 170.96149458368438, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4452090, 764.0206258127333, 276.7561136711271, 192.36131545700508, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2656242, 732.6358351328066, 83.60496333617147, 518.4255848508315, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4165264, 475.5524572890068, 237.614572277462, 955.7038218145867, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1914249, 449.86957281196896, 52.62961606210704, 275.7238209673809, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4262048, 1012.8893118135702, 31.867302670550494, 711.1949569511529, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1145922, 1087.6496290593445, 286.5183016437038, 283.0906253522163, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6893355, 370.79758042476044, 182.89269375206425, 49.91076820819207, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8494942, 1174.2387598868982, 155.74030301083695, 442.4937784338014, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1689962, 1100.2579222621, 254.60169883437004, 156.45940286476701, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4212767, 1283.869688996948, 208.93806586798578, 498.39966897101016, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9626498, 1159.156061170836, 38.665179646525765, 614.4371289796973, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1951295, 290.92101230240223, 31.16767558769125, 742.8370147011619, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3579033, 1273.4164755006057, 217.67798374271555, 223.60221136269288, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7661075, 155.652623850809, 163.5414824526635, 663.8182821812369, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2831053, 522.0385381596043, 87.87582348134899, 572.7286046882803, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2012674, 174.1306248891333, 26.83694428714112, 512.7077293726084, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9851126, 266.8436250081098, 297.4480988156904, 291.67647887612003, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6276151, 172.15427786490355, 90.18649139120505, 221.51002437212654, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8165692, 916.3979068438458, 87.8932768872736, 624.0163636410094, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6286856, 892.5709033754949, 135.7627053936623, 479.5467127776354, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1337378, 1262.2275581922402, 269.3163935185589, 764.1998692840513, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2068071, 810.2777265023644, 140.6562759559101, 142.31694949591855, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7126961, 937.9106566523197, 202.36841059564236, 680.5998487307436, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5492724, 1021.9536212057934, 54.50278256293809, 932.595269715456, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6618338, 834.9585160744838, 195.6652405486062, 700.8546640275313, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8539530, 1268.030528299126, 45.90278302175372, 628.8602397728031, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8385087, 143.23723065305296, 23.8020922327676, 687.4253033272571, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3695369, 1066.9612861874907, 258.3834508250029, 341.92588806154845, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5743018, 1422.362909849092, 179.17480994526056, 154.59891041405083, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2044101, 733.5564880132941, 61.59997139355118, 281.86485341295753, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6442671, 783.5370152910518, 161.3865496061034, 657.640023128327, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6394448, 1268.117831951028, 28.672931651527414, 344.85325471866014, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8673497, 321.96892329081044, 49.25807760675614, 185.31440318707948, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5197731, 300.6063963883501, 98.59140513219423, 762.0727056902869, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1947831, 805.9995203707658, 186.2280630679128, 223.4299298685428, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2764315, 567.8430076392698, 103.22753898247466, 582.1541375129062, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5290873, 103.83419134925222, 98.15784712049322, 321.9844993678889, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6886730, 690.9393908799614, 63.23988226404601, 973.6759918960583, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4657770, 939.1448690180778, 158.56739402544318, 628.2557344242286, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2102306, 1299.1997839243336, 179.90625817433875, 6.304049879123919, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2764315, 683.9223250537402, 93.70153891606859, 216.65909434614173, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3695369, 657.8386871774761, 10.138254241705015, 333.1602346310274, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6711772, 1140.3886775007713, 82.30955813567427, 333.78407305280246, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6709181, 922.8834732871635, 31.443484863326194, 179.02416817381194, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6202020, 977.6270280387143, 127.8986394756541, 226.4599065824615, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4733341, 208.22155426113758, 146.28313583270244, 42.08575530241876, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8180107, 371.933135773802, 288.1725849575374, 21.24271188842364, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5503219, 590.9524538383366, 127.39581245955745, 991.618327671047, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3520916, 1395.1830655587721, 118.21030352969221, 758.6230603776374, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2564401, 177.52380692311056, 167.8552815661287, 821.7503127778923, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2693187, 1398.9219472293858, 71.47473487558588, 808.5575497699217, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1587102, 1198.28845808336, 136.1187446545997, 444.53653336845423, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9058143, 1440.570194705146, 62.104346236238435, 330.34501665283665, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4300350, 287.95433254631496, 243.01765835261526, 112.02764900114359, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7561579, 603.4977579915051, 192.71640535699635, 476.23146436332786, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3056420, 716.862033936435, 247.89121602579795, 621.5671472826406, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1864300, 1320.0030190182883, 187.84412939667112, 926.2454550302169, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3287489, 244.2442099660619, 30.641646388102806, 754.3939450813275, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3178558, 1360.4323248492765, 77.68804032725016, 165.71516073277803, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5653660, 1034.320603008986, 270.1342013931293, 580.3709652491287, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8390811, 1117.6533318896643, 182.93210972830488, 86.35064210328503, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4559812, 406.19855297711456, 87.23226930600293, 283.3493760620212, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6557492, 251.87846230996726, 99.83799564975523, 560.1113842352659, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1914249, 1063.197452763262, 31.907291927268506, 682.7255969570537, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8794425, 649.9184918636988, 256.04669329098243, 438.7877243387936, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4083161, 434.4488041670902, 74.60955763498136, 778.3420368326238, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7953361, 133.34224279619264, 47.37433486867761, 886.1218809775669, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3602356, 1476.5479723614974, 136.0211306012526, 227.04363040931352, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8159183, 887.2780271393366, 128.00467143321836, 287.8314318349947, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7788573, 974.4151077693194, 197.45442878145244, 106.66174209393908, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6750815, 113.89820494952691, 208.48934745811457, 12.76662692398045, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4916481, 146.8562515136205, 30.22549148832417, 411.83403874750115, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9808297, 797.9667728819061, 219.6029435321368, 47.134843038663554, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9442095, 774.870517571435, 166.3531322490042, 197.80230635182272, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9897514, 920.1248316054537, 102.1519646167655, 347.8135230926018, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4187077, 465.33385242717605, 280.3159110845357, 258.3530719072936, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2376505, 1405.7015077147598, 180.06942956572425, 523.7937613642885, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2656242, 492.65159671925267, 245.4912212969429, 947.2997014791557, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5976691, 568.8853390309985, 267.3544290609245, 401.5605195603977, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8739603, 1327.7071036748082, 183.22329005656593, 213.28579226569678, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2351504, 628.7958607606768, 298.2976767647676, 643.1612856328729, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6174471, 555.0114870340368, 175.06590817681453, 957.8245125423211, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1975159, 70.78178545823887, 281.15940879091426, 766.3730870446501, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7638577, 130.52677187216568, 260.3126255357887, 574.3885922519981, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9897514, 905.1637975934048, 142.63709372186497, 865.976571784437, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2351504, 571.3583216710316, 25.02656642766433, 313.6745941446308, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6479415, 1082.388814588217, 56.220774105533806, 689.6443617644343, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4503634, 422.2541377629797, 165.43361207797545, 917.4698951871862, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3624312, 888.9919000172703, 75.4488966420519, 807.502900254394, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3595306, 843.6756557024561, 89.68585204664106, 703.7343378836114, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6756171, 1151.4995598031414, 210.34492538937323, 762.1486109356673, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9695794, 1357.1585602637583, 24.485401720404663, 983.8755947095835, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3461672, 592.1140096176076, 142.54347504605647, 999.6346157050542, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6419364, 1384.331741896079, 80.59491622492543, 642.2640405957993, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1036544, 613.5274772648224, 188.8591023580465, 583.5830232218972, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1393069, 846.4130186774607, 273.4958181673592, 755.9275131911761, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9590988, 979.4725187890155, 118.2477143588951, 157.24256892115108, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3935580, 103.42672435778165, 245.20004427281944, 477.58962301696914, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8813465, 1392.4101312152413, 22.601694716222646, 788.3914931028827, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9577941, 1232.9647039340132, 80.84074323834271, 708.3764348177813, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4719837, 244.44974136149528, 222.83129146000184, 689.3808804928668, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5051841, 847.2227643921395, 180.54421668682053, 338.9265548901723, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8175377, 198.75903363642368, 48.85975646960574, 13.186936271537109, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1673067, 1269.1767464169432, 219.12554902680506, 941.3065055038097, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4719837, 469.2073376608076, 210.5507874280187, 146.3872222342737, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1182373, 893.5574015963185, 29.44768200352012, 140.3295187017045, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5297788, 81.14918050210353, 160.94663374627478, 768.6475224787488, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5040756, 1192.339554016574, 191.79808432508915, 934.6160238376762, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1731082, 317.63296225548794, 148.67644382297294, 857.2157520324663, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4291922, 239.59628650442676, 285.0130281149781, 792.0282905565911, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3219580, 327.78497625879953, 44.84321775679385, 284.6607333172384, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5690330, 494.24068847308445, 24.293984974250897, 15.033759599384467, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4636877, 803.8258905648734, 230.30018638876865, 917.1105975843637, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8651134, 406.15110390434654, 104.61444171402842, 955.9426490594104, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8265136, 363.0178102583271, 89.0128060989207, 248.87425691724852, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5858697, 501.26270843344776, 14.485196928124852, 544.3542100673756, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5954034, 1144.8841551036392, 201.67528500482376, 438.15817879002583, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9808297, 937.1958703505081, 89.17729446826226, 948.0988143973757, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5852557, 433.5040103164591, 171.94191622552628, 736.8271834099415, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3641027, 922.4797778533821, 222.82911542293755, 530.3871723814987, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7243928, 997.9392328012817, 85.8214072646886, 145.17402224469066, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5748609, 90.0773552459282, 108.05851872080873, 759.242391793369, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3518512, 367.7904744901032, 270.3385056340945, 454.3681178402612, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8614766, 527.1442067574626, 122.17174983986169, 123.84155508564554, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6558595, 1479.584463264128, 153.3005025501552, 506.63719099503714, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5492724, 758.8810045702741, 135.46930088945078, 667.9243668595894, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3056337, 1292.5150928299672, 138.49016461382698, 641.0736092249458, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3817252, 492.60110430195084, 58.14911180543297, 363.86778555956965, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2341251, 315.2337263104111, 242.70475665008772, 141.3408675048955, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4024318, 623.5183910959364, 11.98675288472625, 557.012909443385, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2132777, 690.0316128809535, 86.44816675879038, 276.8819578216521, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6584934, 714.2113148335882, 51.830363968557805, 309.04922607129714, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3068327, 1090.4881297272432, 261.37092032385755, 539.4292872227388, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4991658, 601.4436627585453, 35.569672669248575, 527.8724591072427, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8166452, 1124.4684699768738, 143.08525078094198, 118.49376055457917, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8614766, 1077.0391453949376, 206.3774940544547, 61.945310745581516, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6428264, 791.0877748823626, 33.68452265712215, 692.2502518061157, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6132404, 183.53195346098414, 163.04499094994077, 717.9626925729568, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2698721, 130.29161516543326, 172.918044960314, 685.7317163758499, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5454896, 571.7057101616826, 154.4008015353509, 660.7544835830317, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9319573, 97.01628631685114, 126.12557314219892, 287.06380616865937, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7391811, 1226.9520850483893, 190.67848578160184, 887.6679250073408, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4063257, 1201.552083234372, 148.7775029306388, 211.4742814796252, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7764598, 993.2290852425414, 198.25366882882506, 567.2384840864512, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3226670, 1370.2000538185275, 55.037215569646385, 840.8876510086974, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1066367, 881.1621019311351, 43.9856764993739, 469.9697568336458, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7531812, 310.903611652529, 245.9625338943609, 383.3798844601363, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5681590, 153.82472509890025, 57.895121627746924, 38.210007157173706, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1431245, 1210.0425888879604, 287.7692485958535, 556.0474538609463, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6510816, 473.686441959458, 115.90794661418785, 524.8481426527209, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6315863, 1097.8167041307072, 24.879265963588985, 621.7637314259413, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5577893, 373.7113763003134, 191.60797886862161, 689.3071455013578, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9548914, 322.75803702987974, 166.05483209786044, 891.6192307825934, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8527627, 949.8458716622057, 56.9710591601926, 21.14034354911254, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5091401, 1419.8635299475638, 297.9785518282466, 844.670824680509, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3375903, 205.08910716162322, 39.58714692808739, 759.0731606487041, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3641027, 912.1683588432602, 286.5370475207289, 384.28837852465006, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2954465, 760.0650457638029, 64.04476612505186, 0.4456608486570479, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5592345, 1409.3678940039333, 258.88621579228544, 72.56507771186527, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8654339, 945.6374195495366, 17.574402917568285, 984.5552689297677, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2912484, 369.2113551034743, 74.15293168386982, 762.2894183692574, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3932363, 1358.56093505999, 262.08742293810707, 353.24724778556237, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1302224, 356.6252750417065, 255.8446926279469, 36.48457001124372, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2873990, 965.1944385723216, 248.78814043429998, 691.4803250678859, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9487529, 721.1671937337665, 297.1673257295889, 427.98973509112926, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3936076, 1315.531188248487, 18.179610441900813, 30.644838548248178, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9209331, 571.3575992871056, 287.1291803877744, 11.932699777208455, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4778792, 838.2628034433361, 91.12284275220374, 392.42971355079027, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7682748, 195.38307486428795, 290.95555805773387, 281.00306772479865, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8178330, 420.3962605426072, 172.29810864354747, 29.638859085057035, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1917183, 404.18640843809106, 31.942269051364573, 850.6931734480711, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6959281, 984.2371858496256, 275.6703439657837, 972.6840990347482, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2624417, 553.4790605592977, 39.95188465350598, 848.3977538839715, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5483275, 1062.7227796896618, 243.72141166174328, 971.1016418759505, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8807910, 1001.8857139372726, 14.132617972364915, 915.3938773701016, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3595306, 822.4242658949051, 45.87535727207131, 962.0066469560376, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1901452, 1276.7661252570988, 69.14542324553767, 523.990535576842, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7267003, 164.19409710427755, 20.398602379852655, 206.59164758232328, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2690461, 1196.9332661645233, 253.7766513794944, 374.8829081632702, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8169475, 1079.8007953790298, 48.62942563578572, 802.8843015785425, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2278533, 1095.4941893370474, 87.68286005833869, 390.3171139220092, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5632501, 1297.3917119421294, 82.20636316870416, 762.4464000243651, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5544563, 1388.5947427594344, 288.9415612048554, 108.53812337429657, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5367651, 873.0543242206588, 241.85298464158487, 891.1068032329302, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9159559, 1324.7060964847965, 226.27916490428348, 954.1836957977569, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8231218, 874.0806968627176, 247.5886509654266, 152.38742036172837, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8047880, 404.2239106002857, 271.6729360023811, 297.02828255852245, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2693187, 981.6222266033092, 110.48177899474999, 294.3004607550963, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1878686, 140.92507815165447, 147.2945529526488, 925.25303277233, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5201982, 417.7940610516808, 261.91538783014755, 298.19200524122505, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9801730, 613.8327625315991, 295.7941852083699, 909.6314511442815, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3518512, 1496.959924979066, 174.18096710570006, 990.9299280827555, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5341396, 1380.499785910684, 298.63131285484957, 512.7387021934339, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4703264, 159.51169251271477, 76.00238632398455, 257.6237393836559, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3328283, 1490.6467880158527, 179.24607911898423, 935.7743485792763, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9843188, 720.6472965653261, 63.33393684018113, 355.15256480805925, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9510078, 434.3375853458881, 284.25724137120187, 271.79994099373386, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6453455, 199.1195723825568, 161.2679339920798, 649.7796102002119, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5250963, 1108.7099633095052, 235.85052238656462, 561.0012264928913, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9044370, 876.3521591372981, 222.07751870644898, 157.523532443408, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8021670, 1084.8536708167408, 216.35545724390664, 828.0295282396991, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3551594, 1341.7835736721568, 26.172056240208505, 523.3638062691255, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1109729, 314.81636653558036, 228.43557215627, 247.59052615371579, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3072277, 1108.3759086789328, 93.1835526345445, 338.2121729964027, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3614633, 1426.586132130733, 201.44972701780966, 136.06838569863166, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8539530, 213.8663133699827, 283.12117707026727, 143.3205030125435, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2666535, 498.1165685077355, 99.49309682538535, 501.9142620261373, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7247048, 718.0537651969381, 64.29682423920752, 980.2367366093474, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6886463, 942.2329382473755, 49.1882270145315, 771.4321808204463, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9793545, 1074.3843886969269, 129.30158126158534, 647.7980167142033, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3375903, 1496.460887192449, 166.76872192085906, 924.0516991659554, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7243928, 1415.8462358745226, 182.2476543777255, 636.6153653819948, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5431269, 880.1222414874335, 50.798948518245275, 514.0495245069225, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2758436, 539.263575979272, 241.85489692236928, 186.08479196209515, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1532292, 1000.8076220236732, 263.1529181979257, 670.1047302141494, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8476594, 548.9163914209148, 170.50950570810764, 447.3015342355557, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9113331, 1094.3620899743487, 112.87540221848285, 142.99991473789163, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1532292, 451.47573445756166, 22.343820495793114, 796.4480160374717, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4194541, 964.1541654968161, 94.82145895487305, 761.2768832067287, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3932363, 309.81003888998345, 196.31185309184497, 68.57466688308511, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5901056, 101.84449993875211, 229.91555038362554, 370.8065695388332, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1113922, 1084.4222543889746, 89.4570166815564, 785.3677439339676, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1197289, 142.1462354044617, 298.3059835087641, 443.03605200869964, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8786965, 1341.7175057230481, 36.56692673642068, 527.1035660933516, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7710596, 1075.0810750918495, 188.17788652258338, 659.9053810588673, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2358908, 1096.6301893619989, 34.80655556414877, 819.5727552823766, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1968620, 137.6613349979928, 194.49362685703204, 842.0363768948317, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5211411, 789.8686346430912, 187.07016295991704, 965.327205688636, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4923849, 1436.2880114350503, 82.00498804984586, 631.6388393531819, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9121363, 225.8092889404732, 269.50108248235983, 844.5097696923024, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9921563, 799.701068279323, 179.95702600621308, 927.2222971399166, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7682748, 965.6063247691224, 152.33078267334128, 60.80539045195776, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2539748, 611.8940427774988, 227.4963222100344, 906.1283786116232, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4961976, 1459.3725868154456, 133.80230041761956, 113.57663494548898, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4244339, 246.26438526669244, 231.19735172849016, 780.3622230963492, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1089024, 1090.1422812763851, 55.874481898202255, 366.8248400553573, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1136428, 1354.9149938186779, 145.95713962101752, 138.4008872241471, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8562757, 787.0857304723803, 168.64828780125626, 239.39811559001578, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1731082, 1296.7645363378285, 77.14932875841988, 292.7280328427349, 'first', 'EUR');

-- =========== skymill.ratings (generic) ==========

DROP TABLE IF EXISTS ratings;

CREATE TABLE ratings (
  id INT NOT NULL,
  passenger_id INT NOT NULL,
  flight_instance_id INT NOT NULL,
  rating INT NOT NULL,
  rating_comment VARCHAR(255) NOT NULL,
  rated_at DATE NOT NULL
);

INSERT INTO ratings VALUES (7587049, 9719088, 7512215, 5, 'Democrat cup shake figure too finally place hand. Result one top half.', '2026-04-26');
INSERT INTO ratings VALUES (8869867, 6180741, 4900203, 5, 'Begin plan front here.
Well American find large into interesting. New music painting certainly try. Drug during run laugh. On player we fast rest next.', '2026-03-25');
INSERT INTO ratings VALUES (4465116, 6329350, 8179773, 5, 'Expert hot often others particular lot. Deal office old interest guess save citizen. Data out tell building foreign compare base.', '2026-05-01');
INSERT INTO ratings VALUES (2018884, 2696654, 8741700, 3, 'Bring her than sure. When study glass throughout. Relate where significant language.
Compare avoid least among analysis yeah mother. War yard well yard current data. Short six sure.', '2026-03-24');
INSERT INTO ratings VALUES (3350844, 3224135, 4291858, 5, 'Save break daughter positive conference food. Career camera concern ten weight cost would partner. Wrong federal someone believe have hot drug.', '2026-05-05');
INSERT INTO ratings VALUES (2151847, 7873250, 9509070, 5, 'Decade evening color director their office. Enough bring drug important suggest field. Series move its to yet politics beat.
Peace report century. Later buy character able wind behind film.', '2026-05-05');
INSERT INTO ratings VALUES (4762117, 9003017, 3541207, 1, 'Color rich remain. Institution again job public teacher.
Low word day goal decade speak cold. Interest course human western relate. Activity security later result themselves.', '2026-03-21');
INSERT INTO ratings VALUES (5402403, 8042175, 1454134, 1, 'Allow line ok card avoid tax position for.
Participant worry research seek factor.
Themselves ball garden drug anything lay or above. Themselves possible set president. Exactly opportunity attorney.', '2026-03-14');
INSERT INTO ratings VALUES (9278448, 4772680, 7598471, 4, 'Feel eat anything share. Almost idea to television democratic.', '2026-04-26');
INSERT INTO ratings VALUES (9330050, 6776228, 1290249, 1, 'Five claim water boy. Score product computer pretty.
Tonight respond key simply age. Hard leave grow him loss Democrat look. Throughout final off.', '2026-05-10');
INSERT INTO ratings VALUES (4457864, 4892788, 1290249, 1, 'Blue store approach itself agency. Man voice join it. Go matter alone century source development someone personal. Claim adult his determine have early.', '2026-01-17');
INSERT INTO ratings VALUES (3244421, 1484905, 9946112, 2, 'Smile energy strong consider yes. Myself here recently meet. Federal spend interest ever nice occur far. She sell want watch he religious drive Mrs.', '2026-04-07');
INSERT INTO ratings VALUES (3477514, 8812278, 8920482, 3, 'Executive might born practice season house. Record yeah surface finish offer avoid. Create local top heart. Address spring magazine throughout billion measure.', '2026-01-16');
INSERT INTO ratings VALUES (4997500, 1729245, 9279815, 4, 'Between care car him answer pick. Hundred require store off control. Perhaps less author what blue.', '2026-02-15');
INSERT INTO ratings VALUES (9979516, 6127004, 6406136, 4, 'Because conference other threat memory ball price. Player good tend.
Into themselves whom always. Vote together laugh arrive find outside under. Easy current down sort treatment.', '2026-04-20');
INSERT INTO ratings VALUES (5548436, 6776228, 8886242, 3, 'Different play couple group someone smile. Yeah hope job four glass American interview. Wait drop answer time bar house make.', '2026-03-01');
INSERT INTO ratings VALUES (1786786, 2965959, 4684820, 3, 'Gas book walk general couple. Necessary analysis into take.
Hold green place thing source live. Time state name movement deal.', '2026-04-01');
INSERT INTO ratings VALUES (5733202, 1718275, 4949024, 4, 'Live research join trade thousand key treat.
Garden quite indicate live begin poor. Activity experience different.', '2026-01-09');
INSERT INTO ratings VALUES (9431247, 1931461, 3689846, 5, 'May serious travel around. Person party sing trouble either add. Senior too treat response glass box.
Doctor life thousand quite. Safe into huge friend floor. Along wrong decade system direction.', '2026-05-01');
INSERT INTO ratings VALUES (4040001, 8173911, 4438274, 2, 'She lawyer care catch rather apply experience. Left democratic everything low seem glass design.', '2026-01-11');
INSERT INTO ratings VALUES (1447191, 6992003, 5098506, 2, 'Explain affect couple. Important notice weight serious level. Film finally big no information each.', '2026-03-16');
INSERT INTO ratings VALUES (4117064, 4530532, 6039117, 2, 'A line successful citizen. His nation growth. Leg individual would sit matter type always.
Kitchen attack too shoulder or really policy.', '2026-04-19');
INSERT INTO ratings VALUES (8770818, 2228727, 8323300, 4, 'Support hundred contain. Congress war level remember sell. Machine student picture by family.', '2026-03-13');
INSERT INTO ratings VALUES (6187951, 3986188, 7342356, 5, 'Still deal church risk else product other. West tax outside serve say inside both. Manage room place expect.
Both month career mission special realize technology. Include suffer visit wrong perhaps.', '2026-05-03');
INSERT INTO ratings VALUES (5050325, 9992742, 5506687, 5, 'Spring my half by.
Must quite never paper far ever. Rich gun follow fight same alone.
Which house last herself staff can stand. Put almost suffer amount. Worker region life size seek less.', '2026-02-24');
INSERT INTO ratings VALUES (9968610, 1432017, 4949024, 1, 'Protect ask hospital billion. Successful radio time drug customer. Type experience station give free. Technology agency wonder number area.', '2026-04-09');
INSERT INTO ratings VALUES (1584829, 4797001, 9011552, 4, 'Week cell establish former edge executive.
Him course entire person. Loss financial hundred if. Management I interview another field stop.', '2026-01-19');
INSERT INTO ratings VALUES (8013343, 4530532, 8659338, 1, 'Either early public this fill memory major kind. Radio deep care professor grow. Skill nature gun now tax agreement fish.
Institution cold large matter least. Own bad official area.', '2026-02-22');
INSERT INTO ratings VALUES (6488097, 6180741, 8817486, 5, 'Concern painting girl cold. Assume growth whether particular system. Painting growth common brother gas issue tree piece. White political between bill long green without sport.', '2026-01-09');
INSERT INTO ratings VALUES (6539527, 2161934, 4647476, 1, 'Attention say matter minute coach threat save. Professional pay court despite interview piece.
Relate can less little. College senior three answer discuss. Very catch suffer join total.', '2026-01-25');
INSERT INTO ratings VALUES (1327522, 1718275, 4982256, 3, 'Off TV central.
Pick last yard since speak environmental participant product. Forget fire rather because thing here. Anyone movie kid however west fly experience.', '2026-04-08');
INSERT INTO ratings VALUES (2785778, 7403311, 4193470, 1, 'Suggest evidence then Mr. Expert area less note I else race.
Who movie tough sister oil. Stage team else source water. Among by office.', '2026-05-01');
INSERT INTO ratings VALUES (3118724, 8621561, 9314453, 4, 'Nearly enough trial history parent daughter. Note seven black chance even. White black crime life treatment material surface item.', '2026-01-13');
INSERT INTO ratings VALUES (5832605, 4246383, 5818296, 4, 'Dream believe author safe size choice hour answer. Rich information officer doctor. Project too senior benefit.', '2026-02-28');
INSERT INTO ratings VALUES (1437836, 6651063, 7328294, 4, 'Beat culture care detail turn. Attorney for measure parent.
Something fly about arrive remain education. Ask agree ready whose.
Drop board imagine however. Street blood gun fund guess big.', '2026-04-10');
INSERT INTO ratings VALUES (4649109, 6245743, 2201507, 2, 'Hotel wife unit month town claim. Leg best whose. Staff leg continue three stop.
Involve three lead many. Guy keep price together.
Little industry reflect rate purpose television whole.', '2026-05-04');
INSERT INTO ratings VALUES (6268955, 3482381, 3897785, 3, 'Sea outside but participant science admit. Wait management cost smile per industry. Do from at send long hotel.
Office painting require any stop role whose. New threat pressure sound certainly.', '2026-01-11');
INSERT INTO ratings VALUES (1247555, 7786316, 1867373, 4, 'Allow argue matter suggest they. Simply rule east per or. Drug strong star recognize collection.
Vote recent anything for room me so surface. Happen eye matter mission. Plan region find call life.', '2026-03-28');
INSERT INTO ratings VALUES (3301447, 3677816, 7955477, 1, 'True chance politics tonight much often determine. Democratic step less interesting never blue cause.', '2026-01-16');
INSERT INTO ratings VALUES (5811162, 1500606, 8780015, 1, 'Set win rather person how. Ready matter week seven author.
Pay simple term. Event quality cause others.
Former very level professional. Others add try door.', '2026-02-13');
INSERT INTO ratings VALUES (2476046, 1931461, 6039117, 3, 'Culture draw enter forward director on special cost. Paper cost son recent rate type. Help that see maybe manager decision put project.
Age young nor among tonight manage. Late cold standard job.', '2026-04-14');
INSERT INTO ratings VALUES (2709928, 5721829, 9724633, 5, 'Avoid wide force capital marriage into foot. Surface camera possible attention indeed herself. Issue possible type run throw kind political on.
Power article seven compare song. Whose wall it money.', '2026-03-23');
INSERT INTO ratings VALUES (6654300, 6197631, 8164721, 3, 'Idea say quickly special popular develop would.
Unit sister difference middle. Establish field try important. Country woman thank sense available trouble drug.', '2026-02-05');
INSERT INTO ratings VALUES (1704581, 9992742, 2666559, 3, 'Study education environment traditional rate morning film determine. Rest kind down decide say police follow artist.
Among reach head skill fly raise no in. This economic industry dark.', '2026-04-07');
INSERT INTO ratings VALUES (5372111, 8224728, 2924296, 3, 'Budget section center garden worker identify. Hand you town more meet top across.
Role four TV fact way box. Pretty dark six show near politics professional.', '2026-02-28');
INSERT INTO ratings VALUES (4730139, 3231298, 4900203, 4, 'Myself surface professional mouth center another PM late. Method building suddenly kitchen since age.
Film have note yet ball task state. Law ago create her true skin full which.', '2026-05-11');
INSERT INTO ratings VALUES (4823231, 2965959, 1957125, 4, 'Alone most bag upon nothing such. Information yet today describe positive newspaper. Natural response night away happen.', '2026-03-21');
INSERT INTO ratings VALUES (2181585, 3233971, 7938588, 4, 'Build pick environmental than message room form. Community stuff only them situation. Single season clearly experience her yes.
Hospital offer international scene player room skin.', '2026-03-14');
INSERT INTO ratings VALUES (9661251, 3086160, 5656525, 5, 'Foot call friend go reach. Employee president open crime money place when medical. Every reason evening maintain finally.', '2026-01-23');
INSERT INTO ratings VALUES (2335855, 6383237, 8548716, 4, 'Bit quite good attention material through. Weight health nothing foreign guy require themselves.
Support truth participant describe amount.
Lay idea party exist. Energy too color energy stay.', '2026-04-04');
INSERT INTO ratings VALUES (5780649, 1432212, 8179773, 5, 'Sure increase to agreement any process. Community light inside his. Son common meeting manage.
Including price admit fast or yet really cover. Check eye teacher real population sense.', '2026-01-19');
INSERT INTO ratings VALUES (7664391, 2723241, 1290086, 2, 'Middle success finish water.
Then study hard only. Again relationship particular political recognize. Pull word everything marriage then travel represent follow. It indeed site rich allow.', '2026-05-08');
INSERT INTO ratings VALUES (7163460, 4797001, 9724633, 4, 'Science few shoulder wide three follow. Bill finally least occur fire letter part. Board baby everyone no specific.
Data husband everybody recently tonight. West large program magazine once along.', '2026-05-07');
INSERT INTO ratings VALUES (7091030, 6301649, 8343974, 3, 'Project series cell. Suddenly magazine main mean. Operation actually establish.
Forget appear apply different.', '2026-01-15');
INSERT INTO ratings VALUES (5646363, 8027778, 4010212, 1, 'Cost movement action region. Issue former process thousand card wear late.
Dinner area method money. Treatment character thus Congress spring. Trial beautiful feeling.
Risk share marriage plan.', '2026-03-17');
INSERT INTO ratings VALUES (2986963, 1988776, 5826789, 2, 'Human oil fund south close available upon. Also trouble watch. Charge college water garden cup.
Style guy responsibility quickly. Set season financial it particularly range place.', '2026-01-24');
INSERT INTO ratings VALUES (7141069, 2427690, 8841346, 4, 'Weight wide war out weight. How himself laugh affect north. Race raise quite guy view. You cell arrive garden some.', '2026-04-15');
INSERT INTO ratings VALUES (7759401, 5682977, 4915229, 3, 'Before suggest help open use drive quality could. Hard list clear believe nearly we position system. Push early politics likely defense region.', '2026-02-05');
INSERT INTO ratings VALUES (1180241, 1729245, 6096264, 5, 'Investment serve fine. Democratic perform these body energy since director call.
Seat than less seat. Popular always current case its.', '2026-03-19');
INSERT INTO ratings VALUES (6647557, 2294181, 9977745, 1, 'Her need economy research prove though two. Many performance article religious table.', '2026-04-04');
INSERT INTO ratings VALUES (9857526, 2470259, 2838934, 4, 'Window friend world wonder. Top significant under probably will attack of.', '2026-03-29');
INSERT INTO ratings VALUES (4781229, 8155828, 6432174, 5, 'Officer media second be continue. These speak amount because worry art model.
Become follow sister house approach century.
Rather eight two husband of hospital. Example large need energy worry.', '2026-01-30');
INSERT INTO ratings VALUES (3956234, 3603058, 7955477, 3, 'How school safe standard. Vote few should focus. Kid page fast enter art still.
Night number structure follow spring. At similar drive prove. Ability may beyond little whatever.', '2026-01-22');
INSERT INTO ratings VALUES (7420834, 6245743, 1299188, 5, 'My what specific like try defense find friend. Perhaps federal keep cut.', '2026-02-04');
INSERT INTO ratings VALUES (8135990, 9003017, 7470412, 5, 'Hospital start picture inside figure free morning. Wait mother language knowledge. Effect sit generation left blood water.', '2026-04-26');
INSERT INTO ratings VALUES (4319404, 6180741, 8323300, 2, 'Street wind seem stop control put society. Use picture make current within newspaper these.', '2026-01-17');
INSERT INTO ratings VALUES (7713697, 3150842, 6802003, 5, 'Source anyone work son. Result hair exist safe dream well.
Society own care administration.
Decide walk gun edge. Business brother but man artist easy man ask.', '2026-03-30');
INSERT INTO ratings VALUES (5797498, 1529356, 3985619, 5, 'Three their rate five hope. Public rich decision.
Town hotel them check often your police much.
Soon yard TV day. It individual respond simple short leg eight.', '2026-01-09');
INSERT INTO ratings VALUES (5928196, 1211134, 2171509, 2, 'Dinner home his occur. Put any pick brother. Perhaps evidence law style. Wind ask hundred he professional.
Modern a decide. Reflect land out media treatment prevent.', '2026-04-25');
INSERT INTO ratings VALUES (2152736, 2986199, 6775944, 3, 'Author truth phone home your each voice. Such himself imagine ground ask blue sound. Behavior foreign down plant hair indeed.', '2026-04-04');
INSERT INTO ratings VALUES (1020985, 1988776, 2738928, 5, 'Pay agree read court four foot. Full religious thus nation four player oil.
Each professional yes budget think. Make oil current support section relate. Home ten operation any.', '2026-01-13');
INSERT INTO ratings VALUES (1212504, 2858315, 5848496, 5, 'Such throughout hair life her though dream. Save around her heavy reach.
Process environmental share sister. Technology actually region particularly. Too opportunity reach talk way doctor crime.', '2026-03-01');
INSERT INTO ratings VALUES (8894873, 9381230, 3747976, 3, 'Though professional woman task so. Effect continue computer language worry visit tax.
Describe notice nor idea strategy heart. Recently available may ready art. Some move president among.', '2026-03-18');
INSERT INTO ratings VALUES (8745535, 9112089, 7459023, 5, 'Factor art deal relationship see third. Wind say now alone mother result teach. Least toward together either win.', '2026-04-13');
INSERT INTO ratings VALUES (4501442, 1497262, 4669373, 5, 'Turn discuss employee summer it suffer PM. Mind thus arm couple reach price.', '2026-03-15');
INSERT INTO ratings VALUES (4991539, 9312273, 2340419, 4, 'Scientist wall interview either hotel cost. Now will expect forward guess society.
Culture party stock southern owner group body I. Partner enjoy have assume language represent be.', '2026-01-30');
INSERT INTO ratings VALUES (7656141, 9868867, 1610544, 3, 'Image address blood professional fine accept its. There attention suggest. Not defense set sell you former trip why.', '2026-04-19');
INSERT INTO ratings VALUES (7124187, 8834504, 5129903, 1, 'Thing real star laugh later. Sea building official yard win. Computer nor enough west sense hair.', '2026-01-10');
INSERT INTO ratings VALUES (9157247, 7403311, 6775944, 3, 'Institution not will oil already suggest. Hand instead explain safe end turn kind physical. Character general heart significant.', '2026-05-11');
INSERT INTO ratings VALUES (2238708, 4595619, 5561925, 5, 'Sure course movie either. Church box through go enough rock perhaps.
Ground yard wife election. All movement however.', '2026-04-21');
INSERT INTO ratings VALUES (4561476, 9112089, 1736878, 3, 'Size sister police spend care purpose. Positive Mr most course would time stock.
Knowledge if behavior into stop fly prove. Above economic million.
Save store system some leave.', '2026-04-01');
INSERT INTO ratings VALUES (6404464, 2168435, 9279815, 2, 'Successful no today difficult management make. Religious able force about well firm.
Soon southern scene indicate. That blue yeah only truth.', '2026-02-01');
INSERT INTO ratings VALUES (7656497, 2986199, 6980627, 4, 'Teach political American him. Move five wait evidence discussion join high responsibility. Young like education scene there.', '2026-02-04');
INSERT INTO ratings VALUES (5658047, 7968367, 3973859, 2, 'Contain marriage begin. Table religious reduce couple management black though.
Section real thought consumer trial. Enjoy sure figure catch find yes.', '2026-04-18');
INSERT INTO ratings VALUES (3102737, 4469259, 9501449, 2, 'Peace least cup successful to unit. Wrong peace eat western control away could. Require director future economy home red defense.', '2026-04-22');
INSERT INTO ratings VALUES (6813354, 1729245, 2050537, 2, 'Court item bill owner firm imagine. Party itself past former small of general. After line without new list visit.', '2026-03-05');
INSERT INTO ratings VALUES (1395909, 3891137, 2201507, 5, 'Hope first exactly step. Great of assume health huge. Letter color culture necessary simple consumer close special. Energy use military job quite.', '2026-01-31');
INSERT INTO ratings VALUES (8758554, 5734972, 8021762, 5, 'Class according perform item apply image hair. Ready run rule brother newspaper. Finish travel family teacher. Rock medical late sure newspaper hot.', '2026-01-13');
INSERT INTO ratings VALUES (6985241, 7403311, 9792793, 1, 'Local one media simply just place. Appear ready view too company research arm friend.
Owner much operation look later those. Size choice source job.', '2026-02-23');
INSERT INTO ratings VALUES (8167885, 3354650, 1619013, 4, 'Investment send these. Somebody miss decade mind notice shake.
Man treat later mention our become wonder. Investment forget again old under deep risk.
Do my case physical.', '2026-01-24');
INSERT INTO ratings VALUES (2298470, 4469259, 5656525, 5, 'Politics door care coach suddenly member poor bring. Charge nor move unit chair skill particular. Second when how.
Theory hot Mr act he admit. Begin run should appear involve you.', '2026-03-19');
INSERT INTO ratings VALUES (1361864, 7173601, 3203412, 3, 'Eat upon keep. Employee improve unit against respond his.
This fast cold assume short. Production she available small politics. Season actually would guy series strong.', '2026-03-10');
INSERT INTO ratings VALUES (2022936, 3354650, 2557122, 2, 'Cause apply above community return team. Finally fish find seat this school task attack.', '2026-02-01');
INSERT INTO ratings VALUES (5048618, 9299869, 9343423, 3, 'Local TV Mrs.
Age money worker light bar after. Guess finally price television chair. Put glass sell door red property if.', '2026-04-20');
INSERT INTO ratings VALUES (9908224, 1500606, 9611592, 5, 'Just apply partner. Long road great would official write. Treatment source event executive director most face site. Professor life event baby official.', '2026-05-01');
INSERT INTO ratings VALUES (9268757, 2294181, 7482197, 3, 'Join will cover. Away discuss human black knowledge go.
Shoulder professor add lot either pull. Check term worry. Dog because place although out.', '2026-04-05');
INSERT INTO ratings VALUES (2360403, 4100639, 4610651, 1, 'Report fund need rest claim.
Whole study morning.
Camera surface at everything sound really enough.
Husband I grow than current. Surface cover look increase class. Listen behind good what.', '2026-02-23');
INSERT INTO ratings VALUES (4257203, 5682977, 5862298, 5, 'Meeting down hope else method entire. Us than know detail stuff pressure. Field drop want by small movie raise thank.', '2026-04-23');
INSERT INTO ratings VALUES (1281417, 6127004, 6406136, 3, 'Him much religious population large suddenly. Social serve most lawyer common few. Newspaper stage most question newspaper common get.', '2026-02-07');
INSERT INTO ratings VALUES (5747505, 4123196, 7277451, 4, 'Gun old station keep close bank. School policy spring discover who Congress. Little finally sit trip majority may defense.', '2026-01-29');
INSERT INTO ratings VALUES (9849622, 9381230, 4982256, 1, 'Sit forget time image all somebody industry. Window go herself attorney become feeling sign. Red within against important east manager help.', '2026-03-28');
INSERT INTO ratings VALUES (3180249, 2986199, 4915229, 1, 'Least game yes food its. North notice develop letter. Investment citizen because third decade film. Since red real foot base color per.', '2026-03-08');
INSERT INTO ratings VALUES (2910591, 7899943, 1832357, 3, 'Gun although item walk. Camera themselves program wife past want rate.
Garden old after technology. Our could statement degree. Couple bank goal.', '2026-03-20');
INSERT INTO ratings VALUES (1108187, 4453764, 7128137, 4, 'Fish indeed successful drop.
Case full surface quality town maybe. War public follow huge feel throw enough.', '2026-04-21');
INSERT INTO ratings VALUES (3535242, 7515968, 1215045, 3, 'More build worry operation treatment interesting subject. Determine high prove.
Now child blue move cup decision himself. Specific person better involve within.', '2026-04-03');
INSERT INTO ratings VALUES (4838203, 6383237, 1950026, 1, 'Third great four camera dark day financial major. American physical painting fill standard process court. Exactly responsibility enough such. Statement receive campaign once other anything example.', '2026-01-09');
INSERT INTO ratings VALUES (5317123, 3482381, 1454134, 3, 'Land serious they language more minute hand. Continue policy never join collection huge pretty.
Agree anything forward card. Six face race way down.', '2026-03-14');
INSERT INTO ratings VALUES (9265088, 2228727, 6777939, 5, 'Mention son loss also. Nothing half door nation why.
Exist light bring. Institution write food employee head road. Century four sound soldier smile letter learn.', '2026-04-29');
INSERT INTO ratings VALUES (4539041, 9226362, 7140829, 2, 'Edge paper Democrat less whatever will would.
Soon spend miss. Future rate listen give visit cup. Fine heart new cause. Dog too why woman west high.', '2026-02-27');
INSERT INTO ratings VALUES (9840495, 2961215, 4128390, 2, 'Into way foot arrive standard. Place there activity out. Material sense sometimes. Series decision admit agent professional society.', '2026-04-27');
INSERT INTO ratings VALUES (7279237, 1072758, 5262304, 5, 'Mother deep huge voice under here. Why political system outside perhaps meet exist impact.
Course claim style then own position federal with. Seven successful middle relate writer ball protect owner.', '2026-03-05');
INSERT INTO ratings VALUES (5610915, 1547921, 6775944, 5, 'More hospital position both. Number close fear move child.
All clear national somebody. Cost glass strategy ask.
Send clear recognize. Issue past information.', '2026-02-01');
INSERT INTO ratings VALUES (5136099, 6111066, 2603592, 2, 'Through box certain. Knowledge coach later list I follow.
Too toward new. Benefit couple for hair. Member eight space never positive popular debate.', '2026-01-12');
INSERT INTO ratings VALUES (3324086, 6418072, 3070161, 2, 'Series along skin increase community what practice. Where maybe prevent share positive his so. Statement reflect far chair stay white.
Rest bag available door reason.', '2026-04-14');
INSERT INTO ratings VALUES (5639337, 5682977, 3985619, 2, 'Admit your second pay significant picture. Him story artist. Understand wonder thank professional building article officer.', '2026-03-28');
INSERT INTO ratings VALUES (9952104, 4219908, 5097537, 2, 'Student power sense laugh state attention treatment. Read news measure between property tend help.', '2026-04-09');
INSERT INTO ratings VALUES (5435070, 3482381, 7938588, 4, 'Poor offer teach. Population against industry skill wish moment deep. Business report together management a record.
Sell wide about from hit first maintain.
Now run arrive learn draw.', '2026-01-11');
INSERT INTO ratings VALUES (4537089, 6301649, 2838934, 4, 'Hard training public. Article no run trip factor skin.
Enter report attorney fall message. Admit tell though. Of continue rest want guy must main.', '2026-04-08');
INSERT INTO ratings VALUES (8658978, 1529356, 8983277, 5, 'Why identify somebody write natural special stock. Car special manage form matter. Individual son past full deal hundred building.
Claim reflect smile affect show really very.', '2026-05-08');
INSERT INTO ratings VALUES (8547966, 1741094, 1671714, 3, 'Beautiful design war boy her method issue. First us market prove.
Mrs just daughter significant soldier reduce great. Learn say heart watch. Several seek another maintain however oil.', '2026-04-04');
INSERT INTO ratings VALUES (5094410, 1529356, 1751004, 4, 'Couple fear may adult the himself major. Present article budget middle. Money quality campaign miss approach month.', '2026-04-27');
INSERT INTO ratings VALUES (6890016, 9514557, 5637917, 2, 'Prevent tell concern image item above. Real Mrs still impact deep national. Product form operation popular certainly certainly something include. Entire as eye push much kid.', '2026-01-18');
INSERT INTO ratings VALUES (5413576, 2961215, 6719936, 5, 'Item loss offer movement positive remember. Plant think indicate. Operation building wife statement area treatment glass.', '2026-03-14');
INSERT INTO ratings VALUES (8059263, 2777401, 6990414, 1, 'Tough third evidence quickly structure somebody. Public prevent network hand ready either too.', '2026-02-18');
INSERT INTO ratings VALUES (3621671, 9312273, 4661322, 2, 'Agreement especially notice give live. Add never various reality especially. Fish everything each product item term establish word.', '2026-01-26');
INSERT INTO ratings VALUES (4895483, 5213265, 2171509, 4, 'Notice require spring available doctor imagine current. Mind collection as writer more accept price. Culture arrive sure often.', '2026-04-29');
INSERT INTO ratings VALUES (6373658, 1010438, 1671714, 4, 'Minute which century movie where Democrat always put. Drug else case she. Quite woman participant enough collection of PM.
Would travel size population home buy. Hard begin go.', '2026-03-20');
INSERT INTO ratings VALUES (1994649, 9233734, 1293870, 5, 'Hot wait source local return about. Heart condition just lay floor career never.
Remain mention Congress different.', '2026-02-10');
INSERT INTO ratings VALUES (1934784, 3107150, 3349832, 3, 'Feel fly send. Often fine physical next would approach.
Part he serious across official. Majority what imagine thought. War theory catch almost someone.', '2026-03-17');
INSERT INTO ratings VALUES (9472786, 3307906, 4378471, 5, 'Star trial fast specific should. Be book opportunity help current loss. Central both at become. Now less lot onto building talk strategy himself.', '2026-04-28');
INSERT INTO ratings VALUES (2510746, 4100639, 6407111, 1, 'It board resource bit trial. Near commercial center center audience person action past.
Gas president option year. Suddenly school capital size authority. She tonight before owner center me.', '2026-05-09');
INSERT INTO ratings VALUES (1668224, 1547921, 5928475, 1, 'Rock last employee reduce. Question growth design back total off. Relationship this woman.
Drug tree bad degree. Buy those of room decision. College member thought before play.', '2026-03-14');
INSERT INTO ratings VALUES (1637048, 2858315, 3243026, 4, 'Serve if goal when common manage evening. Require cell each much increase toward store. Interesting bar forget able.
Hotel return establish prepare poor thing.', '2026-04-06');
INSERT INTO ratings VALUES (1402014, 2965959, 7151159, 3, 'Prove answer so American determine.
Friend city want billion paper federal.
From threat today hair become half. Public source true window once reality. Ask if necessary condition environmental.', '2026-04-13');
INSERT INTO ratings VALUES (5925067, 4246383, 8284228, 1, 'Air participant sort east market wide. Author like memory Mrs. Lead door protect also why follow career.
Tv wife hold. Available low chair power. Happen feel mother avoid special leg itself.', '2026-04-10');
INSERT INTO ratings VALUES (8564997, 6776228, 2738928, 2, 'Environmental million off design. Tv kitchen go fast summer economic.
Finish boy she board business top hospital. Successful four poor charge. Exist toward board seven product dream experience.', '2026-04-15');
INSERT INTO ratings VALUES (4773797, 8173911, 1028636, 2, 'True son enter line section why late focus. Artist product director require pull space focus.
Sign receive evidence read. Member eight answer artist identify agree.', '2026-04-07');
INSERT INTO ratings VALUES (5771761, 1729245, 4460972, 4, 'Build I evidence process maybe least. Decision itself south increase name catch.
Surface section need certainly that worker. Everybody resource why economy contain popular.', '2026-01-26');
INSERT INTO ratings VALUES (5685171, 3603058, 1947070, 1, 'Past visit smile. Yourself explain argue whatever range news wonder section.
Less past join fire. Reality quite after wear total. Seem store type foreign wonder small.', '2026-03-09');
INSERT INTO ratings VALUES (5896574, 4530532, 8689138, 1, 'Blue run indicate speech. Myself grow economy movie ability half.
Adult pattern citizen.
Trouble glass budget loss on often throughout. Position glass worry very card.', '2026-02-16');
INSERT INTO ratings VALUES (2390681, 4892788, 8841346, 2, 'Common professor nation environmental some me. As according might red. Part treat fish move leader.
Growth star month anyone leader. Force between play doctor represent wife. Cost road shake church.', '2026-01-25');
INSERT INTO ratings VALUES (9246581, 1729245, 3689846, 4, 'Decide business doctor red talk. Who performance phone treat environment statement group. Onto southern series always easy.
Own our heavy pressure race manage.', '2026-02-28');
INSERT INTO ratings VALUES (5097123, 6373970, 9916922, 5, 'Push leg almost. Drop increase civil now sure.
Blood actually near market loss step. Source big save country simply yeah. Myself trial amount.
Thing hit society itself real.', '2026-02-16');
INSERT INTO ratings VALUES (7531653, 3610348, 7512215, 3, 'Almost recent travel month better school. Agree her unit visit wish. Listen of husband weight seat skin week. Book major building.', '2026-02-01');
INSERT INTO ratings VALUES (6522335, 1432017, 3416955, 3, 'Street only weight short practice. Decision avoid reality quite. Employee approach western attention reality.
Including assume though recent. Hotel its physical top notice old.', '2026-04-22');
INSERT INTO ratings VALUES (6812209, 5116160, 8548716, 2, 'Film do current practice. Central fish huge bit PM lot. Series continue boy threat.', '2026-03-29');
INSERT INTO ratings VALUES (2715785, 9925283, 4144513, 2, 'Person politics may notice movie television suddenly. Reduce race today prevent. Full prevent six hit whatever view.
Federal field view senior night yet. Community new need for.', '2026-04-12');
INSERT INTO ratings VALUES (1081349, 8515559, 4881008, 4, 'Think listen represent draw. Cultural significant marriage international close.
Course peace race magazine choice off letter. Reach kind participant south administration group call.', '2026-01-04');
INSERT INTO ratings VALUES (4107940, 1083645, 5037070, 4, 'Large increase price world high. Later believe compare size drug pattern month. Heavy thought conference player.', '2026-03-10');
INSERT INTO ratings VALUES (6604482, 7496042, 1267699, 3, 'Standard including black table catch today physical. Easy fear pay they nice eight section. Hotel low speak raise.', '2026-03-03');
INSERT INTO ratings VALUES (3317970, 1729245, 6638170, 4, 'Simply agree activity maybe such wonder. Mr window wear his.
More do then determine beat. Rich manage serious on goal rate.', '2026-03-09');
INSERT INTO ratings VALUES (9501963, 3700732, 1532836, 5, 'Bag simple performance past understand blue will. Magazine thousand wind begin suffer water total artist. Door song pattern seek need city dream.', '2026-02-01');
INSERT INTO ratings VALUES (4567825, 6776228, 9509070, 4, 'Per play popular should side goal.
Anything appear believe. Several official help. Point because significant interesting.', '2026-04-21');
INSERT INTO ratings VALUES (7202310, 8948057, 4911891, 1, 'Recognize action subject. Make else catch management high. Likely soldier system although.', '2026-03-24');
INSERT INTO ratings VALUES (2253728, 1888680, 5928475, 1, 'White very easy gun even Republican. Eight morning threat person be.
Deal term arm director thought check far. Election point design sea father arm weight.', '2026-02-15');
INSERT INTO ratings VALUES (8623462, 3224135, 4624699, 1, 'Remain less kind race without final responsibility. Around win red summer tax discuss character. Bit task approach music through onto writer. Meet least support sign second pattern budget.', '2026-03-22');
INSERT INTO ratings VALUES (6525297, 2153952, 1546513, 3, 'Too possible personal imagine view so everybody. Artist onto watch benefit. Which bit believe want seem treat.
Top its perform wrong friend small card many. Hospital report first attorney.', '2026-02-15');
INSERT INTO ratings VALUES (5823923, 5931547, 5637917, 2, 'Chance garden cold vote opportunity nice method space. Bank later including score debate field. Public others reduce contain send might one. Congress protect recognize set memory.', '2026-03-20');
INSERT INTO ratings VALUES (9178041, 7496042, 1215045, 5, 'Heavy tax music nice figure hotel. Local house certain term value. Our reflect since father political them bar.
Behavior single television admit. Friend more start for.', '2026-03-26');
INSERT INTO ratings VALUES (7688641, 5380927, 2666559, 1, 'Attention see series news together cover two current. Tv natural son education size. Generation join us care what career.', '2026-05-10');
INSERT INTO ratings VALUES (2853617, 2957004, 2531963, 3, 'Sell yes condition letter work discover. Machine here produce among. Ask federal middle meeting table police.', '2026-02-05');
INSERT INTO ratings VALUES (8816472, 9636664, 3989140, 3, 'Right food evidence yourself add stage difference. No player hour company body. Not next address hope.
Peace ago dinner here his. Tree cup write describe short word rest.', '2026-04-28');
INSERT INTO ratings VALUES (5093890, 1072758, 8462608, 4, 'Article evidence want fire air foot. Address quickly thing newspaper and both.', '2026-03-14');
INSERT INTO ratings VALUES (7583640, 2696654, 5098506, 4, 'Seat hope now young let new describe. Other response party seek like north. Apply respond build amount fly.', '2026-03-16');
INSERT INTO ratings VALUES (8685279, 9362387, 8427554, 3, 'Bad knowledge baby ball it mission relate. Network information must either sister.
Raise toward necessary. Billion sea factor discover animal figure.', '2026-01-08');
INSERT INTO ratings VALUES (2954573, 9469611, 1668849, 4, 'Entire central we task window skin. Fact add dream area age environment south.
Live tonight their like claim assume. Behind rise most late.
Deep tax positive hospital. Only she actually.', '2026-02-15');
INSERT INTO ratings VALUES (9876071, 2957004, 4144513, 4, 'Listen poor raise total class. Happen center describe run operation movie.
Perhaps material student travel style able. Both society fall woman happy claim perform.', '2026-03-16');
INSERT INTO ratings VALUES (4649754, 1597896, 4633014, 5, 'Evidence attack world. Operation human program half present record. Marriage network grow.
About let movie indeed. Traditional wall religious can list resource million Republican. Cup available bill.', '2026-03-10');
INSERT INTO ratings VALUES (4810379, 1211134, 8280938, 5, 'Close clear relate remain. Short clearly trial play someone peace. Among relate nor image newspaper mother hour. Expert already decade seem month concern baby.', '2026-01-24');
INSERT INTO ratings VALUES (8609132, 3487415, 1739346, 3, 'Risk place yet in. Sea development about something state task church. Measure responsibility whether.', '2026-02-28');
INSERT INTO ratings VALUES (4566206, 6352080, 5282783, 4, 'Open product together probably. Fund area prove factor student design. Watch talk detail human.
Test with nature police decide. Nice door available tend moment their.', '2026-04-12');
INSERT INTO ratings VALUES (4625688, 3354650, 2587249, 1, 'Room arrive discussion teach network. Design short sort wall ball.
Prove actually indicate sea later. Section second walk network central fine edge.', '2026-05-09');
INSERT INTO ratings VALUES (5502240, 9868867, 2531963, 2, 'Will computer treatment plan. So kitchen so wish sign statement each.
Campaign herself marriage our grow group each. Face fear table individual city note side. Low serve star strong.', '2026-01-09');
INSERT INTO ratings VALUES (7823670, 5010487, 8778991, 3, 'Who return get face statement hard.
Field no argue student where attorney be. Speak sometimes mother continue.', '2026-04-01');
INSERT INTO ratings VALUES (7957714, 4530532, 3830674, 2, 'College lawyer chair local term expert Republican. Chance right go one.
Indeed defense call drug imagine head. Current before well Republican size rather. Number present hold road affect fire.', '2026-02-16');
INSERT INTO ratings VALUES (1778465, 1328187, 1098468, 4, 'Similar himself common she exactly four this. Fast former once keep or together find another. Heavy dark official.', '2026-04-26');
INSERT INTO ratings VALUES (8318336, 6373970, 6990414, 3, 'Into sense stuff example Democrat arrive nor recently. Similar play defense catch seven relationship. Certainly focus him represent.', '2026-04-11');
INSERT INTO ratings VALUES (8690581, 1500606, 4128390, 2, 'List woman plan door common up sort. Audience anyone experience source office.
Three describe for something behavior.', '2026-02-09');
INSERT INTO ratings VALUES (3376781, 5520179, 5282783, 5, 'Area military address guess run politics. Character age final development themselves. Fund leave central score. We or day practice upon.
Realize administration market. Yourself medical loss view.', '2026-02-01');
INSERT INTO ratings VALUES (6835047, 9963495, 2077631, 2, 'But indeed condition create well before decide. His hard rock civil cost experience budget ok. Democrat believe lead per.', '2026-01-07');
INSERT INTO ratings VALUES (4372799, 3454898, 4572194, 5, 'Local material past single value vote. Who customer career risk hundred.
Available program change be all. Choose their with responsibility.', '2026-02-27');
INSERT INTO ratings VALUES (9538604, 5682977, 9498851, 2, 'Score whatever ten example body pattern citizen. Information believe everything door station message. Training hard in federal member collection.
Stage tax also say mind. Occur law close.', '2026-03-31');
INSERT INTO ratings VALUES (1898089, 1484905, 5109260, 5, 'Quite tend number prevent wait bit through. Account ok just couple only. Exactly mean sense large son deep.', '2026-03-22');
INSERT INTO ratings VALUES (9555925, 4530532, 5098506, 2, 'House talk theory number pull stock everything. Town week skin indeed evidence century. Exist skill car word government TV establish cover.', '2026-04-25');
INSERT INTO ratings VALUES (3014780, 1979648, 3946276, 3, 'Charge that hand.
Family court before owner spend stage position. Current interest dark clearly sort father situation. Article purpose huge performance season risk agreement.', '2026-03-07');
INSERT INTO ratings VALUES (7352432, 2986199, 3689846, 2, 'Media contain against under site next open. Should toward he mind.
Direction say seat today explain. Time candidate lawyer laugh.', '2026-03-08');
INSERT INTO ratings VALUES (1447492, 8155828, 8822314, 4, 'More better wear bag state. Company bag threat manager effect. Be final culture.
Station process clearly toward. Candidate size special training. Capital you rate project must soon popular note.', '2026-04-15');
INSERT INTO ratings VALUES (2947591, 4797001, 2350897, 2, 'Dream game decide result hour of. Research hope realize degree behavior finally official debate. Impact direction talk statement.
Thing exist study degree. Point itself along.', '2026-01-15');
INSERT INTO ratings VALUES (5608161, 7791393, 7598471, 3, 'Call fund civil. Generation lot low back final fact admit. Film fight since young point performance.
It during party recent. Then no present media clearly leave. Charge as look.', '2026-03-06');
INSERT INTO ratings VALUES (2552184, 8991792, 7938588, 5, 'Inside much guess result dream brother cold. Camera open building each national instead.', '2026-04-25');
INSERT INTO ratings VALUES (6724656, 6776228, 2067996, 4, 'Hear appear near home. Few change face commercial smile decide benefit effect. Education production song.
Mean language maybe believe.', '2026-02-19');
INSERT INTO ratings VALUES (5661021, 6197631, 4900203, 5, 'Else entire music report white. Today total long market. Item claim before list.', '2026-05-12');
INSERT INTO ratings VALUES (1955093, 9362387, 2751370, 2, 'Remain executive simply magazine defense option laugh. Process now other western foot there my.', '2026-01-08');
INSERT INTO ratings VALUES (8551928, 9112089, 2340419, 4, 'Research change fast wall lot. Side bill those defense seek.
Manage although her. Standard fast attack pay fast.
Firm east hard international someone table. Moment skill their letter better mind.', '2026-04-09');
INSERT INTO ratings VALUES (8669020, 5131170, 9412051, 5, 'Indicate moment everyone blue but create federal. Hold exist great truth. Green miss near require else police generation election. Edge become her feel question.', '2026-04-19');
INSERT INTO ratings VALUES (3870393, 6418072, 3243026, 5, 'Data magazine behavior property forward town. Organization effect leave itself.
Mean have sell crime agent blood ahead. Day summer ago top out.', '2026-01-05');
INSERT INTO ratings VALUES (1419957, 5131170, 9343423, 5, 'Standard hospital statement themselves. Money tend out energy cold opportunity region.
Born any economy reveal program. Character attorney occur fear.', '2026-05-10');
INSERT INTO ratings VALUES (7027259, 8173911, 6746460, 2, 'Term production dark newspaper professor focus. Better we laugh light water rule.
Station doctor beat no. Near the space young fish.', '2026-02-02');
INSERT INTO ratings VALUES (6366604, 4246383, 3168880, 5, 'Fill moment impact since night party. Boy institution whose people movie process board.
Argue what difficult notice become pull. Mean occur model market.', '2026-05-11');
INSERT INTO ratings VALUES (6783388, 4530854, 3897785, 4, 'Best opportunity staff. Onto of receive one. Since treatment seem likely practice.
End laugh fast key little police job certainly. Grow meet accept base store safe call.', '2026-01-13');
INSERT INTO ratings VALUES (9917200, 4059576, 8343974, 2, 'Operation together particularly notice myself half. Law issue avoid than pretty traditional.', '2026-02-19');
INSERT INTO ratings VALUES (8677984, 3959460, 1389731, 4, 'Create room watch state. Debate we economic record.
Task eight black although group space letter. Role security young likely eye while. Themselves woman image short.', '2026-01-27');
INSERT INTO ratings VALUES (8684643, 5853975, 7342356, 3, 'Shake pressure off city. Use him likely more agency minute high bit.
Item deal tough sure. Six and argue lead shoulder. Traditional available cup.', '2026-04-06');
INSERT INTO ratings VALUES (5006172, 1196601, 2861626, 1, 'Small media own professor painting color. Ahead weight set century.
Name thought type next born. Really can road hard up should but.', '2026-04-02');
INSERT INTO ratings VALUES (6580811, 2986199, 7512215, 5, 'Story few tend eight east either ok race. Best loss line assume court sing officer.', '2026-01-13');
INSERT INTO ratings VALUES (4120973, 8409944, 9851577, 5, 'Money look play really most simply.
Pull capital line modern. Or environmental daughter buy at deal.', '2026-03-01');
INSERT INTO ratings VALUES (5559541, 1196601, 7358717, 3, 'Challenge someone interest benefit still assume thank. Tax would future without floor himself. For no also particular. Class reflect exactly project few occur safe.', '2026-01-13');
INSERT INTO ratings VALUES (7817068, 1272742, 5282783, 2, 'Policy vote owner and sort deep. Must consumer during account. Such represent off natural.
Per lawyer media decade whether. Pm kitchen parent house many how.', '2026-03-08');
INSERT INTO ratings VALUES (1279778, 3891137, 2847867, 1, 'Mention where authority. Grow involve better day unit. Our and take woman rather stop run poor. Serious seat production beat west glass reduce half.', '2026-02-13');
INSERT INTO ratings VALUES (6749932, 5853975, 2890701, 1, 'Scientist have camera thousand project season itself. Available forget term I.', '2026-03-16');
INSERT INTO ratings VALUES (7720297, 1892068, 6093066, 3, 'Mention street argue beautiful billion general. Popular science success home it likely. Within hotel capital per suddenly large.', '2026-04-09');
INSERT INTO ratings VALUES (5941142, 9362387, 4881008, 4, 'College term surface local whom mind. Beat two rest tell but very thank.', '2026-02-25');
INSERT INTO ratings VALUES (4621442, 2934427, 3243026, 2, 'Fill physical smile else name pay give. Strategy have pass. Citizen measure pull star.
Kind movement game staff speak personal. Side speech purpose. Focus that eat now.', '2026-03-24');
INSERT INTO ratings VALUES (1223007, 6301649, 9218215, 3, 'Increase very worry however. Beautiful professional conference whether per picture. Whose leg power charge wonder catch run.
Difference about style we. Break goal special share actually.', '2026-02-03');
INSERT INTO ratings VALUES (7594687, 3610348, 9218215, 4, 'Hear Mr put partner. Sure medical bank foot area president. Pressure visit nice include alone matter law.
It fire girl drive according compare. Free turn through street clear course.', '2026-03-18');
INSERT INTO ratings VALUES (3786953, 9469611, 1947070, 2, 'Cell around even agent. Baby benefit else until politics none authority.
Woman state realize ever rise growth building oil. Dog so anyone think yeah food.
Surface subject serve system expert.', '2026-05-01');
INSERT INTO ratings VALUES (3306097, 2153952, 5826789, 5, 'Person glass study news education fire.
Machine set process treat purpose. Within natural feel on successful mouth full.
Dark myself air. Difficult media life indicate. Dream no month throughout.', '2026-04-16');
INSERT INTO ratings VALUES (3008296, 1497262, 7168799, 1, 'Modern buy sometimes time debate television. Conference modern according area forward upon both.
Manager main seven attack scientist. Listen life finally employee yeah yard if.', '2026-01-16');
INSERT INTO ratings VALUES (9297108, 3700732, 7613367, 2, 'Return then mention someone. Become either door site challenge herself stay. Lay operation cultural.
Meeting task environment sense meet. Guess sound board hour economic able.', '2026-02-04');
INSERT INTO ratings VALUES (1567780, 1432017, 1546513, 3, 'She process again us final where. Particularly red answer between benefit ahead.
Rule speak later almost board close performance positive. Let determine medical. Affect term soldier husband eight.', '2026-01-02');
INSERT INTO ratings VALUES (8501457, 4332111, 5986471, 2, 'Good charge marriage finish tend so. Half week business suffer health suffer rock. Resource source money standard pull decide specific.', '2026-02-17');
INSERT INTO ratings VALUES (2780344, 3891137, 9142476, 4, 'Traditional measure style policy admit.
Officer sell decision where prepare weight five son. Trial heart between watch town party parent accept. Democratic positive fear than society site again.', '2026-04-22');
INSERT INTO ratings VALUES (1359643, 1888680, 3985619, 5, 'Property write make try data. Administration act bed cut build music particularly.
Later though foreign score big manager. Rich ask understand sister often up four allow. Court oil build speak.', '2026-03-22');
INSERT INTO ratings VALUES (2340628, 8224728, 2603592, 4, 'Character almost fight win. Parent film medical business ability. When discover rate reveal us. Defense marriage husband picture born.', '2026-04-17');
INSERT INTO ratings VALUES (6319606, 1072758, 8865172, 5, 'Employee wish next investment attack court. During able prevent knowledge toward speech. Loss current you if teacher simply.', '2026-01-14');
INSERT INTO ratings VALUES (1829200, 6766644, 7290695, 5, 'Reduce card ever. Six after Democrat it area matter. Despite billion model everyone region yeah safe.
For tough prepare professor yourself break tough. Book first process case hold machine onto this.', '2026-04-17');
INSERT INTO ratings VALUES (1854554, 6954524, 8343974, 5, 'Culture little space institution. Agency institution address the issue form.
Through behavior meeting final. Idea whether stop glass. Myself wide writer certainly night seat mean.', '2026-02-13');
INSERT INTO ratings VALUES (3173002, 3482381, 1050646, 1, 'Shoulder international produce consider to claim. You everybody above fall.
Picture other poor have great until. Sometimes task expect save institution. Still on wide sing.', '2026-01-01');
INSERT INTO ratings VALUES (7580660, 9112089, 7512215, 4, 'Our four of skin. Store win why add position ask. Health gun law.
Short change effort color language instead. Read party relationship really western simple.', '2026-05-06');
INSERT INTO ratings VALUES (2133179, 1163615, 2171509, 1, 'Pull bring effort local change time kid method. Student new read trouble ability star. Range glass pattern yes economy.', '2026-01-08');
INSERT INTO ratings VALUES (7554618, 6766644, 2489999, 2, 'Back meeting mention reason investment pick. Time stuff keep water.
Size trouble base concern paper throughout. Peace site suddenly read above.', '2026-01-28');
INSERT INTO ratings VALUES (4742713, 1484905, 4881008, 5, 'Activity tree foot he. Medical per vote doctor. Small guess newspaper environment across bag hand.
Reality until collection ten leader since debate. Hit PM issue home join responsibility toward.', '2026-02-06');
INSERT INTO ratings VALUES (4145176, 3482381, 8393896, 4, 'Product service recently strong media century their. Present forward within science executive. Month college decision behind prevent piece win.', '2026-01-11');
INSERT INTO ratings VALUES (8947108, 2407452, 7991826, 2, 'Center behind impact lay number food coach paper. Ten start war.
Enter anything source dog. Notice make fund allow generation do.', '2026-02-13');
INSERT INTO ratings VALUES (6266694, 6939753, 7255476, 1, 'One majority foot American wall.
Seat maybe agency course. Physical reveal drive network step management through. Usually hold thus opportunity.', '2026-02-03');
INSERT INTO ratings VALUES (2156412, 3677816, 8899441, 2, 'Mean run situation still around walk politics. If pay to outside.
General movement need late shoulder reveal concern during. Serve lay wind gun event imagine more.', '2026-01-23');
INSERT INTO ratings VALUES (2675043, 3354650, 8323300, 5, 'Whom wife raise school. Sense beyond management. Full letter against like hard.
Mouth by section ball. Talk until fast.', '2026-03-21');
INSERT INTO ratings VALUES (7193153, 1083645, 4900203, 2, 'South reveal find include ready. Do too choose station later pay. Picture white within wrong chance each administration.', '2026-04-20');
INSERT INTO ratings VALUES (3750750, 5526936, 6980627, 3, 'Mind book official you drive pretty reduce finish. Economy want seek painting challenge I write. Kid success line prevent speak her sure still.', '2026-05-09');
INSERT INTO ratings VALUES (4619753, 4332111, 9011552, 5, 'Cut civil even person. Only author country against rise hair discuss make.
Today pressure turn should hotel. Standard admit agency. Point thought green project.', '2026-02-15');
INSERT INTO ratings VALUES (2417109, 2227634, 4704226, 2, 'Fact do material activity. Close beat painting also mission about approach. President brother unit spring alone spring different main.
Public participant enjoy site. Artist student husband.', '2026-04-13');
INSERT INTO ratings VALUES (2957850, 4332111, 2430594, 2, 'News true enjoy peace. Word carry able respond. Class begin well show skill.
Religious remain choose drop allow. Loss reflect serious together other network.', '2026-03-13');
INSERT INTO ratings VALUES (5509365, 8621561, 2087960, 5, 'Parent poor beat turn material number ten.
Hour like center follow method. Will catch their.', '2026-03-28');
INSERT INTO ratings VALUES (8230753, 4595619, 8802551, 1, 'Game can would court next. Matter east dog old capital defense bed large. Record effect letter.
American yard design ever special. Analysis bad success plan.', '2026-04-10');
INSERT INTO ratings VALUES (4928226, 5150169, 9412051, 1, 'Western feeling article first along purpose her. Put his room. Want assume could him have throw push.
Carry help plant market benefit interest fall. West provide similar western policy top big.', '2026-04-23');
INSERT INTO ratings VALUES (9189023, 7383012, 1760850, 3, 'Weight remain vote when thought future above. Good interesting peace newspaper surface structure myself prevent. Continue kitchen require wife.', '2026-02-12');
INSERT INTO ratings VALUES (1286000, 3482381, 5862298, 4, 'Entire soon least visit mouth glass consider. Require after huge. Clear force contain night fact become.
Skill ball there collection main. Issue catch data thus keep.', '2026-03-21');
INSERT INTO ratings VALUES (2430195, 4073362, 3070161, 4, 'Four father you wonder effort. Lot great part watch. Including area these leg radio kid center.
Whose bank such worry PM food. Power yes expect somebody consider.', '2026-03-31');
INSERT INTO ratings VALUES (4931611, 1339636, 1610544, 3, 'Lawyer citizen adult crime wonder save. We serve example boy. Event green I threat good. Weight drug carry hospital north.', '2026-01-09');
INSERT INTO ratings VALUES (3904345, 6329350, 5928475, 3, 'History from people including choice expect like. Garden law official impact. Seven describe establish data human business alone.
Field up line serve.
Spring thank other. Her war after hot.', '2026-02-27');
INSERT INTO ratings VALUES (1146248, 2777401, 6004554, 5, 'Film authority upon safe. Over think even occur. None accept lay walk.
Suddenly value that early attention foreign prove. Manager buy the maintain should. Dinner prepare thank between become.', '2026-02-28');
INSERT INTO ratings VALUES (7307058, 1272742, 3420721, 3, 'Where whatever may movement. Office condition strong direction series over.
Play arrive home fine indeed foot. Meeting big accept doctor natural learn phone. Property service return according.', '2026-02-14');
INSERT INTO ratings VALUES (5860262, 5328741, 7328294, 5, 'Hotel enough peace around consider culture society. Though friend he effort him foreign. Produce size reveal not.
View item particular door. Him future blue indeed.', '2026-03-02');
INSERT INTO ratings VALUES (1444249, 9868867, 8164721, 4, 'Possible create difference feeling air. Identify thought program whatever more reduce information writer.
Fine act woman hospital reason line. Degree reduce crime. Tax rate people.', '2026-01-25');
INSERT INTO ratings VALUES (8766279, 1729245, 4763209, 4, 'Experience enter over. Note across suggest also bit.
Crime east gas discover head. Two guess community finally. Reach cover wrong teach.
So side today across security star. Would finish strategy.', '2026-01-11');
INSERT INTO ratings VALUES (7407328, 9514557, 5072970, 2, 'Reflect effect fly. Son store range begin place increase. Rate baby heavy.
Success score evening answer hear nature. Hot Democrat among per out.', '2026-03-17');
INSERT INTO ratings VALUES (2198171, 9383250, 7277451, 3, 'Which share trial exactly throughout south receive either. Administration enter economy social surface protect far practice.
Debate record box. Particular word toward attention movie which.', '2026-01-23');
INSERT INTO ratings VALUES (3779561, 1979648, 4448063, 4, 'Provide high despite truth. Describe hit term eat surface building them. High sit upon detail.
Ever some high amount.', '2026-05-06');
INSERT INTO ratings VALUES (4252228, 2481283, 1758953, 2, 'Fall study total produce. Day card he sport natural sister. Yeah forward wall rise far act next hour. Again investment maintain investment food.', '2026-03-04');
INSERT INTO ratings VALUES (7205470, 3354650, 4763209, 5, 'Fine nor trial remain scientist.
Stand ball coach fire street term top. Receive according knowledge majority protect country.', '2026-03-12');
INSERT INTO ratings VALUES (7444732, 3354650, 5830883, 4, 'Suddenly pass rise international opportunity. My hand general son guess television various forward.
Them career second church economy risk quickly.', '2026-04-17');
INSERT INTO ratings VALUES (5965089, 6329350, 8323300, 2, 'Available history wind oil their. Per most cost behavior skin science another fly.
Father country whole where maintain however onto.
Dark report blood knowledge.', '2026-01-16');
INSERT INTO ratings VALUES (9356776, 6080953, 4911891, 4, 'Sign side oil simply. But five possible. These else write candidate today.
Young present production tell. Memory company minute.', '2026-04-15');
INSERT INTO ratings VALUES (6795255, 7532793, 5282783, 5, 'Somebody street today just hour end. Sell wish dog win method character. Everything majority sure learn describe surface receive.', '2026-03-25');
INSERT INTO ratings VALUES (8629456, 1931461, 4661322, 2, 'Minute seven do agent. Mother stop result establish hour artist community.
Ahead to investment true. Much trade check wear coach him. Yes see store rock happy stuff.', '2026-04-19');
INSERT INTO ratings VALUES (4794477, 5131170, 1487850, 4, 'History alone artist station. Although serve fill school.
Control young here hear response institution security. Religious top again everyone program couple race.', '2026-02-07');
INSERT INTO ratings VALUES (8050451, 7532793, 9248389, 3, 'Building single deep her. Quite power property dinner city technology alone.
Save choose agent big. Skill law mind fact. Themselves discussion pay street adult street professor because.', '2026-05-11');
INSERT INTO ratings VALUES (2193151, 8042175, 4193470, 2, 'Range fire tell study should forward time. Field close side put. Director moment suggest deep shoulder call. Since either school enter.
Goal I success staff law order. Product just nature drug.', '2026-04-04');
INSERT INTO ratings VALUES (6287923, 7403311, 7482197, 4, 'According blood care character who. Bad senior including party.
Event war money leader.
Check mouth specific garden hope letter year. Can machine story story over. For cause turn cause.', '2026-02-15');
INSERT INTO ratings VALUES (3333786, 1072758, 9501449, 4, 'Case democratic life. Focus billion pass decide nearly watch.
Yeah TV treat. Together garden never up pattern teacher teacher.', '2026-01-18');
INSERT INTO ratings VALUES (5805768, 5465007, 8659338, 2, 'Somebody product simple adult fire media. Wide arrive Congress maintain risk technology. Tend hotel affect personal vote prepare grow ability.', '2026-01-12');
INSERT INTO ratings VALUES (8966462, 8027778, 4633014, 3, 'To return tell listen meet person. Yet camera push pass where road. Mention report wrong better authority major painting.
Real economy believe candidate well. Form white star for he agent.', '2026-03-06');
INSERT INTO ratings VALUES (8522345, 8948057, 5262304, 5, 'Price wife generation. Where professor its bit but capital song. Office serve wife thus before agree sign.', '2026-04-14');
INSERT INTO ratings VALUES (1366730, 6111066, 6432174, 4, 'Science all student movie technology time. Voice cell in network conference fine test.
Street own product same happy material avoid. Produce fact score.', '2026-01-27');
INSERT INTO ratings VALUES (6058477, 4662730, 6096264, 5, 'Cold view good lot thousand event. Without side reduce focus apply. Almost traditional song quite stock deal five.
Listen evidence every. Sure bring research north specific suffer approach.', '2026-04-24');
INSERT INTO ratings VALUES (5800970, 4595619, 9977745, 1, 'East hand sea around attack together wear. Thought debate bill above.
Skin whose admit where our contain. Build father road end.', '2026-03-17');
INSERT INTO ratings VALUES (8521261, 1083645, 5109931, 5, 'Want fear professional yeah avoid. House player and pay type week quickly choose.', '2026-05-11');
INSERT INTO ratings VALUES (2490876, 7383012, 5645246, 3, 'Trial relationship capital no meeting. Speak project wrong our. Service against wall time attorney town start.
Imagine above one sea left. Think fish and pass play meet work.', '2026-03-17');
INSERT INTO ratings VALUES (1890991, 4123196, 2055142, 5, 'Former lawyer defense treatment hour join. Off yourself some tough media morning so.
Floor mind six. Really detail business become professional modern cause.', '2026-03-31');
INSERT INTO ratings VALUES (8851060, 2723241, 6407111, 5, 'Yourself painting business sense bad model. Challenge create shake month pressure than although. Imagine table challenge once bill long picture he.', '2026-01-21');
INSERT INTO ratings VALUES (8684101, 2776678, 8899441, 4, 'Place day police. Candidate treat great.
Lay quality war safe. Candidate however require reason lawyer best want attorney. Standard set message coach well way professor.', '2026-05-03');
INSERT INTO ratings VALUES (9686963, 4998276, 7947286, 3, 'Talk imagine dog partner why leader realize old. Inside data rise American clear.
Happen tend thank sound soon catch above director.', '2026-02-24');
INSERT INTO ratings VALUES (5137996, 9526705, 7328294, 3, 'Someone radio already fill between seat. Bring approach across child themselves player smile mouth.', '2026-03-02');
INSERT INTO ratings VALUES (2463883, 6156293, 8179773, 1, 'Probably interview example dog how. Subject price yeah believe. Really government every of run deep when.
Discuss capital pay.', '2026-05-12');
INSERT INTO ratings VALUES (5175097, 2084890, 4010212, 3, 'Sit article nice move program culture. South actually move market billion.
Body include maintain senior design recently. Surface bag among statement study.', '2026-04-08');
INSERT INTO ratings VALUES (9354373, 7532793, 3252086, 5, 'Economy daughter college radio important fine will mother. Sense three future yes Republican design.
Son important this simple. North art activity school new now hold.', '2026-01-14');
INSERT INTO ratings VALUES (6874533, 8423344, 5658325, 3, 'Action citizen serve. Public measure budget modern kind. Play space game tough fire.
Nothing look official hotel among. Explain able listen along investment. True contain before oil deep.', '2026-02-02');
INSERT INTO ratings VALUES (3090181, 2427690, 9886698, 3, 'Theory another these entire star cold. Result themselves already show.
Material push might bar both partner. Of Democrat candidate involve grow fire quickly.', '2026-05-04');
INSERT INTO ratings VALUES (8299596, 6329350, 4843875, 4, 'Maintain author size follow college. Year mission some professor note arm exist. Do American blue sport.', '2026-03-14');
INSERT INTO ratings VALUES (6956806, 1196601, 6746460, 2, 'No have every great able financial. Mission father teach current throughout human. Happy particularly upon I prepare safe.
Five north sure wish near door Mrs. Tend bring ground.', '2026-05-06');
INSERT INTO ratings VALUES (1792782, 4123196, 4152237, 4, 'Method blood husband car wall whole. Sense city fine member especially. Style mention fire.
Although material together against me administration. Kitchen now firm.', '2026-03-10');
INSERT INTO ratings VALUES (7651341, 1328187, 5762438, 4, 'Seven bring direction know. Two research determine language. Meeting play important boy open between once face.
Bring unit agreement animal reflect office. Continue protect skill and hour look.', '2026-01-30');
INSERT INTO ratings VALUES (5991999, 5734972, 7938588, 1, 'Your effect number my activity indicate result. Maintain window baby explain. Ever final pick pressure. Into wife remember sister method.', '2026-03-09');
INSERT INTO ratings VALUES (3329707, 3150842, 9960227, 1, 'Tend stage any hotel summer everybody. Student reason participant future address nearly understand. Mr more commercial politics fall tree establish.', '2026-02-20');
INSERT INTO ratings VALUES (8544999, 2776678, 4973827, 2, 'Game first medical main sure live. Worker Mr size.
Give his sort thing list election you quality. Memory receive news almost song drop floor. Give senior list interest wall number.', '2026-04-07');
INSERT INTO ratings VALUES (3712336, 1196601, 9011552, 3, 'A off produce move. Painting statement campaign hotel medical pretty. Seat a discover knowledge. Stuff Republican after heavy shake.', '2026-03-24');
INSERT INTO ratings VALUES (8681181, 9728828, 3985619, 2, 'Story unit business organization. Important professional guy form.
Approach let rock able bad design fact chair. Product doctor return last teacher movie piece. Exist animal cost ok.', '2026-04-19');
INSERT INTO ratings VALUES (8755606, 1668346, 7727576, 5, 'Everybody test step lose.
Much office avoid impact. Open life view TV away. Job exactly lot PM meeting. Instead before free character I.', '2026-03-12');
INSERT INTO ratings VALUES (4081176, 1979648, 9482853, 3, 'Any poor media sense growth could. Parent with letter worker father our. Seem almost research safe ready the whatever.', '2026-01-22');
INSERT INTO ratings VALUES (2359532, 6447803, 7778473, 5, 'Your military whether. Reflect just drop.
Gas herself ability drug sell. Walk summer high anything to player. Write international she.', '2026-05-08');
INSERT INTO ratings VALUES (6345540, 9963495, 3830674, 4, 'Practice early much really attack. Call three second from example discover I. Cultural pick anyone information.
Different employee capital dream per inside as. Crime such ability sometimes.', '2026-04-23');
INSERT INTO ratings VALUES (6847312, 2965959, 6407111, 3, 'Help mind able recognize teach. My happy care member.
Sound defense window family almost. At again sort save church write increase.', '2026-03-20');
INSERT INTO ratings VALUES (4353301, 9112089, 6424106, 2, 'A offer new form. Then growth human others per successful. Time southern mind operation.
Reality what person himself fall hundred body. Focus front experience direction whether involve season.', '2026-03-06');
INSERT INTO ratings VALUES (6986783, 8515559, 5129903, 1, 'Laugh throw lay feeling weight. Them question leg chair fight economy. These would assume sometimes toward card.
Might health common mention make. Enough professor Democrat or. News piece local.', '2026-04-10');
INSERT INTO ratings VALUES (1557153, 9233734, 2949063, 3, 'Himself nature bring political early me her. Each end fine state war. Walk option Mr born three red.
Voice exactly too. Three mind second my front body.', '2026-01-24');
INSERT INTO ratings VALUES (8830657, 3454898, 1671714, 1, 'Pay west kid hundred affect field discover. Teach behavior cultural front. Tell left however yes crime.
Him baby chair question positive. Middle style baby your pressure.', '2026-03-16');
INSERT INTO ratings VALUES (3956221, 8155828, 1150752, 1, 'Than cold shoulder from address. Network strategy media about last standard culture. Article however determine five game officer two. Central government education.', '2026-05-02');
INSERT INTO ratings VALUES (8360235, 2470259, 6777939, 3, 'Attack I return top southern could return. Also defense idea part standard might professional.', '2026-02-19');
INSERT INTO ratings VALUES (8068494, 9218001, 5862298, 3, 'Indeed book century region station eye baby water. More science site ahead music yard once.
Hundred foot represent. Suffer increase kind thus others live.', '2026-05-02');
INSERT INTO ratings VALUES (4423962, 7383012, 9889589, 4, 'Arrive traditional people forward their back. Exist law front that college.
Bag interview represent. Scientist major on. Free happy live close party instead.
Building test low billion discover grow.', '2026-04-16');
INSERT INTO ratings VALUES (6779589, 5131170, 6757414, 4, 'Role wait by big gas result. Ahead protect true style. Law think save people street determine would suggest.
Four wind system world identify. Admit very news of more short town compare.', '2026-04-26');
INSERT INTO ratings VALUES (3793315, 3233971, 6432174, 3, 'Down economic drive song behind improve. Season all customer mother finally agency enjoy open.
Century eat chance walk finish father.', '2026-04-14');
INSERT INTO ratings VALUES (6585389, 2153952, 3891059, 2, 'Year form wind poor decision and source. White face improve major.
Myself air lawyer finish theory still call. Call they water. Store girl around range compare.', '2026-03-03');
INSERT INTO ratings VALUES (3029909, 1741094, 8179773, 5, 'Foot beat even. Figure book including. Send glass use too age region answer.', '2026-03-15');
INSERT INTO ratings VALUES (7409488, 9331874, 3506783, 4, 'Have instead detail mind data sure. Century word compare role property quite would he. Toward peace face anyone mission her expert.', '2026-04-16');
INSERT INTO ratings VALUES (7414992, 3307906, 4949024, 3, 'Step daughter nature major. Him stand prevent capital. Play whole American.
Conference thank win others structure true.', '2026-03-16');
INSERT INTO ratings VALUES (6494336, 4797001, 9108899, 1, 'North one hotel feel. Movie show civil feeling style close theory build.
Important race little question. Raise note animal name speech learn.', '2026-01-12');
INSERT INTO ratings VALUES (8759511, 5526936, 2666559, 2, 'Time accept beat effort rise yourself all conference. Trip animal interesting recent floor while.
Cell guy get put simple fall house. Mrs structure fine research read. Run least pay democratic whole.', '2026-04-16');
INSERT INTO ratings VALUES (8878172, 3822974, 8462608, 5, 'Majority into remain. Chair others gas choose. Reality rather language system risk crime.
Money think yard picture example. Ever usually if city seem. Oil imagine pull realize.', '2026-02-06');
INSERT INTO ratings VALUES (8319775, 2696654, 8343974, 3, 'Detail mean far. Them green foreign behavior. Type second city person six could gas fast.', '2026-02-24');
INSERT INTO ratings VALUES (8368363, 7515968, 7140829, 1, 'Worry prevent political dog. Actually wife Republican support government station join. Around for after bill doctor.', '2026-04-21');
INSERT INTO ratings VALUES (3940951, 4892788, 4236485, 1, 'Source bad much whole end skill. Specific society threat face.
Exist west husband. Same place officer beautiful ok.
Loss property whose policy. Choose service loss range much include.', '2026-02-24');
INSERT INTO ratings VALUES (1132533, 1729245, 2848351, 5, 'Send difficult general author seven summer.
Check which according design describe ground fly sing. Probably little job parent discuss.', '2026-05-10');
INSERT INTO ratings VALUES (1801574, 6405551, 4344487, 4, 'We close dinner director describe yet building reality. Former majority get more ok attorney. Concern miss top nature general.', '2026-05-10');
INSERT INTO ratings VALUES (8872298, 5213265, 5072970, 5, 'Art consider until kitchen. Ability good president condition plan. Officer finally smile.
Poor phone will pass. Trip lot suggest owner parent radio value.', '2026-05-09');
INSERT INTO ratings VALUES (6396894, 7217433, 7168799, 3, 'Late process fast Republican dog respond mission she. Last possible bar ahead baby.
Position meet smile around determine authority. Party nature if interest moment bring realize.', '2026-01-02');
INSERT INTO ratings VALUES (9673086, 1432212, 4915229, 1, 'Think school per even thousand improve. Air year within imagine strong picture sure wait. Write how ready before decade yeah support occur.', '2026-02-22');
INSERT INTO ratings VALUES (8010078, 7383012, 7991826, 1, 'Relate detail west remain. Home administration us president others as. Sit official forget rise draw affect meeting.', '2026-05-04');
INSERT INTO ratings VALUES (1431054, 9925283, 2489999, 5, 'Part my meet future organization for. Somebody player reflect.
Sort girl even today hear song. Degree writer memory sure cost defense. Serve college year respond take national.', '2026-03-14');
INSERT INTO ratings VALUES (7496896, 7873250, 9651758, 4, 'Nice new information somebody process enter. Community name fire can democratic dog anything camera.', '2026-01-22');
INSERT INTO ratings VALUES (3766209, 5150169, 1347646, 3, 'Family lay describe teach. Protect not speak piece range explain source.
Law alone tell central green. Perform leave evidence among we born itself. Mean green certain speech as.', '2026-05-07');
INSERT INTO ratings VALUES (2216888, 1892068, 6796520, 2, 'Require black step. Conference year evidence account common American significant. Per produce color personal business require.', '2026-03-25');
INSERT INTO ratings VALUES (1365715, 8173911, 9462768, 5, 'Collection test term consumer turn. Program still senior break subject.
Every often owner paper song approach. Picture too view hospital world mean reflect.', '2026-01-25');
INSERT INTO ratings VALUES (6787300, 3797933, 1607199, 2, 'Build though nothing account.
Yourself wrong feel difficult. Wear paper charge social art just.
Have everything well travel. Product hot view occur field all main.', '2026-02-20');
INSERT INTO ratings VALUES (7041708, 3482381, 5660582, 3, 'Note yeah plan court indeed system election. Woman special cold form idea. To fear indeed defense.
From commercial few baby commercial. Trouble alone point. Current me hold value too without.', '2026-03-05');
INSERT INTO ratings VALUES (9797383, 8173911, 4763209, 2, 'Future reason plant south event.
Rule show goal prove. Ok rise different goal career energy none.
Number water nation dinner approach. Fill themselves lose poor activity ten.', '2026-04-07');
INSERT INTO ratings VALUES (8466358, 1083645, 1293870, 3, 'Quality interesting us physical young industry manager through. Agency southern success least role he.
Nature pay executive serve whatever age. Morning resource later direction dog firm by.', '2026-01-19');
INSERT INTO ratings VALUES (2558494, 4059576, 3506783, 5, 'Huge civil understand. Language write ahead any institution fear. Through strategy bar become.', '2026-02-02');
INSERT INTO ratings VALUES (5141389, 2077334, 2077631, 5, 'Strong TV upon federal possible could. About lawyer up tax while development. Like impact stay build amount long later relationship.', '2026-05-09');
INSERT INTO ratings VALUES (4136208, 9092559, 2924296, 5, 'Type call number open. Page start develop. Continue top girl yet situation face go.
Want to page realize. Create whatever sometimes beyond career general some. Thank own item.', '2026-05-05');
INSERT INTO ratings VALUES (9267210, 9381230, 9851125, 1, 'Such successful front alone. Also alone shoulder.
Others can join. Opportunity open stage ready mean along must.
Her majority rock other star. Until others free center player.', '2026-04-03');
INSERT INTO ratings VALUES (6319329, 9925283, 6775944, 1, 'Technology activity professional star avoid past back eye. Fire million contain teach. Us couple whom success.
System later phone success. Dog along manager know seven spring. Road second image.', '2026-02-26');
INSERT INTO ratings VALUES (6119447, 6405551, 7611726, 4, 'Little whole approach degree. Detail probably step relationship. Form gun food fish social your.', '2026-01-23');
INSERT INTO ratings VALUES (7298363, 6373970, 3901972, 5, 'Sing career kind. There citizen morning go. Left Mrs author heart popular.
Already option eat finally. Into determine order someone TV again.', '2026-04-19');
INSERT INTO ratings VALUES (5938795, 9218001, 6039117, 3, 'Important off start push animal realize possible. Heart man fall heavy.', '2026-03-28');
INSERT INTO ratings VALUES (4834937, 5682977, 6746460, 1, 'Tax experience data maintain. Sure sea glass home place arm ground. Tough smile effort where full cause especially.
Respond option of fish. Near forward ok each.', '2026-03-22');
INSERT INTO ratings VALUES (5155323, 2294181, 2887013, 3, 'Other key society report.
Modern must discuss easy. Herself make step who doctor within medical. Policy hair and none involve.', '2026-02-20');
INSERT INTO ratings VALUES (8916781, 1610270, 7613367, 2, 'Where message drop weight voice do. Mind computer child answer keep somebody see.
Own main image try. Which against chair management fight. Smile poor weight ball political.', '2026-02-22');
INSERT INTO ratings VALUES (1400762, 8994337, 6999905, 4, 'Soldier owner plan month office. Who through week skill manager nice sign. Indicate actually father.
Sometimes common very. Traditional possible six real.', '2026-01-16');
INSERT INTO ratings VALUES (7604697, 5213265, 7128137, 5, 'Bank write citizen phone option actually. Find able girl line. Away generation age better as leg station ask. Example second able middle painting bank just several.', '2026-04-11');
INSERT INTO ratings VALUES (6487098, 5520179, 9011552, 2, 'Similar leg provide maintain serious. Decide century list maybe market trade throughout. Fire actually not these upon audience talk.
Sign say American top yourself. Indeed future trade base free.', '2026-01-03');
INSERT INTO ratings VALUES (2884583, 8621561, 5097551, 5, 'Create piece chair occur lot recent. Rich style can popular left work. Life property son growth book.', '2026-04-19');
INSERT INTO ratings VALUES (8960152, 2153952, 8983277, 4, 'Light physical possible kind somebody. Pass price system impact fight. Conference tell marriage first.
Report Mr certain think perform anything music civil. Pick recently answer fish agent crime.', '2026-03-03');
INSERT INTO ratings VALUES (3727118, 6111066, 2037973, 5, 'Water model back down. Officer option concern office win middle. Government catch seek single.', '2026-01-26');
INSERT INTO ratings VALUES (1975447, 2294181, 4193470, 1, 'Walk crime such nearly few. Church charge since.
Check manage turn instead develop remain oil. Discussion compare answer foot bank hair. Friend program scene professional thing relate.', '2026-02-21');
INSERT INTO ratings VALUES (3864855, 6776228, 3946276, 2, 'Benefit west study interesting yet mission these. While future plant while picture leader who. Focus chair effort everything animal.', '2026-02-22');
INSERT INTO ratings VALUES (6261415, 9526705, 6999905, 3, 'Price onto when never really relationship suddenly. Morning recognize bit through hope responsibility follow. Tax force appear season.', '2026-01-17');
INSERT INTO ratings VALUES (9923746, 8027778, 9115191, 3, 'Feel raise Democrat your key spend support. Generation near wife some guy case. Recently amount up population course maintain that trouble.', '2026-04-16');
INSERT INTO ratings VALUES (2033760, 9027045, 6980627, 4, 'Relationship difficult dinner. Pay improve adult care study cover. Head yourself reflect above.
Determine health when he board. To top might industry son little form.', '2026-04-08');
INSERT INTO ratings VALUES (9643196, 2227634, 7727576, 3, 'Meet bring American few time certain he little. Both major give night single. Knowledge certain institution during any would left.', '2026-04-13');
INSERT INTO ratings VALUES (9171349, 2858315, 8548716, 3, 'Real question law someone anyone. Bring carry character coach rock explain play.
Adult drop heavy size city media beyond least. Floor deal rule subject. Outside financial likely task have second.', '2026-04-10');
INSERT INTO ratings VALUES (5901600, 6793309, 9314453, 4, 'Others structure half. Especially cell yard crime contain war cup. Arm lay professor. Bag late section allow.', '2026-04-13');
INSERT INTO ratings VALUES (1977712, 9538849, 4291858, 2, 'Tend share practice. Town treat table billion common tonight born. Produce eight government money possible.
Ball money time allow close pattern. Play tend become country Republican turn soon.', '2026-02-17');
INSERT INTO ratings VALUES (6755880, 1718275, 2188866, 1, 'Beat probably option student story help history challenge. Really operation toward international. Item figure result end week life authority buy.', '2026-02-24');
INSERT INTO ratings VALUES (9269426, 2696654, 1760850, 5, 'International mother cold. Short think person idea young drop.
Member feeling military treatment choose entire shake. Instead myself opportunity stuff.', '2026-03-18');
INSERT INTO ratings VALUES (6676784, 4332111, 6407111, 1, 'Bank Republican purpose will rate bar. Fall result far edge consider always. Hotel leader risk sell. Various certain actually me.
Attorney vote may. All campaign machine.', '2026-01-31');
INSERT INTO ratings VALUES (9792893, 3610348, 9958280, 3, 'Weight prepare hope mouth style accept each. Machine cover drive offer recently nothing. Mrs imagine box firm player new price.', '2026-02-08');
INSERT INTO ratings VALUES (4485577, 1597896, 8180075, 3, 'Gas mention first leader almost fast PM two. Baby property right approach single leg bill.
Against control new song traditional without. She region page serve. Market else plant into.', '2026-02-25');
INSERT INTO ratings VALUES (2844863, 8155828, 6271427, 5, 'Air look couple debate kitchen. System risk firm make doctor.
Candidate hard right. Simple left break garden hold performance. During report hard some single be like.', '2026-03-31');
INSERT INTO ratings VALUES (8095174, 4772680, 4881008, 3, 'Term call to scene trial option. Worry door general only writer future safe.
Evening issue guy trip should. Adult good maintain might.', '2026-03-30');
INSERT INTO ratings VALUES (6632622, 7031540, 1546513, 1, 'Face change lay office feel its. Realize population bit their wall theory.
Bad too consider. Significant doctor require community. Health democratic fund minute.', '2026-03-11');
INSERT INTO ratings VALUES (2096630, 2965959, 3897785, 5, 'Lawyer your town watch form college decade front. Claim traditional clear father tonight top imagine.', '2026-05-07');
INSERT INTO ratings VALUES (3295539, 4469259, 6461218, 5, 'Pattern positive crime I us dinner. Energy total relationship unit before old. Require everything central hot create box.', '2026-05-04');
INSERT INTO ratings VALUES (2284321, 1339636, 3830674, 4, 'Read heavy man statement site. Sign government near rock provide course television.
By reveal house see human. Protect bed pattern.
Commercial international local difficult. Leg hold model.', '2026-05-07');
INSERT INTO ratings VALUES (3067971, 9469611, 5762438, 2, 'After accept stay. Tree dog wife college stand career.
Task hear near interesting expert. Wife put indeed trip most something approach very.', '2026-03-18');
INSERT INTO ratings VALUES (7839364, 6373970, 9011552, 2, 'Amount add operation past anyone pattern resource yeah. Generation positive data camera tree practice.
Action stand star else easy past difficult. Pull network shoulder soldier central.', '2026-04-06');
INSERT INTO ratings VALUES (3042608, 3797933, 6323776, 4, 'Start figure last have current side. Need cold building animal tonight statement discuss turn.', '2026-02-17');
INSERT INTO ratings VALUES (9950885, 8308313, 8175644, 2, 'Decide student computer early law training.
Understand allow half effect. Fish several perform understand three oil. Over staff save soldier hold simply though force.', '2026-04-17');
INSERT INTO ratings VALUES (6217240, 7786316, 5561925, 1, 'Positive difference weight almost person most low dark. Plant player property major break American lose. Son perform last real per nature travel.', '2026-04-28');
INSERT INTO ratings VALUES (6572999, 8423344, 6096264, 1, 'Bank media no. Sea can offer focus culture appear bed though. In door firm physical.', '2026-03-28');
INSERT INTO ratings VALUES (6950951, 5109828, 6357849, 5, 'Skill say short business. Fight boy out themselves instead rather.
Matter whom section. Not realize city other. There collection certain where learn. Finally able meet real.', '2026-02-21');
INSERT INTO ratings VALUES (4924598, 1264848, 5928475, 5, 'Administration home situation trouble third. Trade allow interesting speak my attack. Century ok forget minute lawyer.
Administration reveal fund have that capital religious. Great share customer.', '2026-04-30');
INSERT INTO ratings VALUES (2890995, 6124507, 4973827, 2, 'Responsibility nearly most either blue customer year. Beautiful Mr generation.
Carry bring market serious weight better water. Its day science program.', '2026-04-06');
INSERT INTO ratings VALUES (7589618, 9728828, 9056370, 1, 'Season government fear run away. Any hotel student firm. Off doctor same debate quickly system check student.
Certain describe old single and.', '2026-04-12');
INSERT INTO ratings VALUES (7074338, 1211134, 3973859, 3, 'Rate body per learn operation. Product prove notice ask would. Read safe light remember finally.
Be just raise once five read indeed. Give heavy visit effort. It yourself music chance.', '2026-05-01');
INSERT INTO ratings VALUES (7804589, 2961215, 7588961, 3, 'Game dog through dog idea expert administration describe. Third sing reality close.
Answer issue take traditional end participant. Religious cell raise dinner identify good institution.', '2026-01-17');
INSERT INTO ratings VALUES (5858085, 6329350, 1131474, 5, 'Soon born school popular. Strong their all. Present effect power.', '2026-03-21');
INSERT INTO ratings VALUES (5993355, 1979648, 1277099, 5, 'Dream foot democratic piece site front standard. Study people push choose method. Whatever plan couple TV.
Arm data method small. Painting president hear indicate them skill science.', '2026-03-23');
INSERT INTO ratings VALUES (2908426, 4332111, 3111997, 4, 'Sort into good mission could along. Authority force natural successful after begin. Pressure book she soon.', '2026-02-17');
INSERT INTO ratings VALUES (8162527, 1497262, 4144513, 5, 'Dinner subject while exist live. Else among show crime. Result book now side life.
Short force well see something. Almost call upon less myself war happen. Least mission democratic street hour than.', '2026-02-24');
INSERT INTO ratings VALUES (7611591, 9514557, 6980627, 2, 'Member as bar adult. Travel garden ok recognize magazine month wind. Test girl community center north.', '2026-02-17');
INSERT INTO ratings VALUES (7943130, 1432017, 2861626, 5, 'Her stage different partner admit whether meeting. Nature measure woman simply animal.
Be cause modern anyone father moment message shake. Add new their believe.', '2026-02-01');
INSERT INTO ratings VALUES (7714209, 6127004, 7140829, 2, 'Star local professor condition significant minute. Enjoy fall wife strategy suddenly blood. Become race dark bad chair guy.', '2026-03-15');
INSERT INTO ratings VALUES (2222488, 4662730, 7463895, 4, 'Their able garden. However occur story. Summer school arrive entire American.
Attorney particularly choice activity kind. Us maybe relate across open public. Item attack same eight while hard.', '2026-01-04');
INSERT INTO ratings VALUES (9894181, 9925283, 9947215, 5, 'Lose hope single wrong school claim themselves. Seven nor development.
Lay unit even history pretty cold instead. Sister professor leave ask positive process. Her nothing far phone group eat young.', '2026-01-14');
INSERT INTO ratings VALUES (8808257, 2227634, 1374988, 2, 'Threat talk husband road international exactly approach. Sea evening reflect trial. Possible claim teacher wait treatment she sea.', '2026-02-05');
INSERT INTO ratings VALUES (4856504, 6383237, 7151159, 2, 'Feeling reflect upon risk interesting hear into. Want bed notice. Up small simply recently protect. Might together kind identify.
List may similar somebody agreement plan. Campaign power she.', '2026-03-17');
INSERT INTO ratings VALUES (5124957, 7873250, 3731525, 2, 'Cup office design these or read. Himself place reach purpose state. Raise heart everything subject yeah candidate beyond wrong. Wife month discussion save population spring.', '2026-04-11');
INSERT INTO ratings VALUES (4877215, 1668346, 2914075, 3, 'Myself very support myself raise language best police. Citizen key single newspaper hear not like.
New student rock above administration thousand add.', '2026-05-02');
INSERT INTO ratings VALUES (5598188, 3822974, 8280938, 5, 'Sit still sound cover song loss. College chance who exist design.
Find some describe up. Sense plant assume series remain.
Call produce authority maybe glass always write set. Stop bring hour.', '2026-03-31');
INSERT INTO ratings VALUES (1322040, 1729245, 3739269, 3, 'Interesting kitchen doctor change air outside. Spend challenge those word walk move system. Road left second prevent market garden.', '2026-04-23');
INSERT INTO ratings VALUES (9004022, 1328187, 5109931, 2, 'Pass step very door large fire dog. Imagine best create magazine sort. World discover office education significant. Gas really court local significant catch cultural.', '2026-04-22');
INSERT INTO ratings VALUES (9486808, 4892788, 7598471, 5, 'Teach class western degree off chance effect. First spend church east quality year pick. Large care quality leg.', '2026-03-12');
INSERT INTO ratings VALUES (3942008, 8994337, 8548716, 1, 'Level case choice half inside include despite. Sometimes total station then late.
Nature today south grow. Bank court wonder imagine season.', '2026-01-06');
INSERT INTO ratings VALUES (7311400, 1931461, 1624553, 2, 'Mouth anything behind blue they compare clear. Wall red paper require answer.
Among option unit poor life every. Wrong quality lay class. Stock small avoid than.', '2026-03-13');
INSERT INTO ratings VALUES (4046452, 1988776, 1267699, 1, 'Decide whose move determine. Machine while and thus hour. Since behavior agree oil.
Common window allow. Room have dog kid thought second after.', '2026-02-25');
INSERT INTO ratings VALUES (9868169, 5734972, 1041571, 2, 'Public range best same.
Method wish much ok. Degree body grow woman challenge return.
World we feeling option factor event ball. First with people remain help develop develop. Modern top space.', '2026-01-28');
INSERT INTO ratings VALUES (4583584, 5931547, 9999750, 5, 'Protect rich type animal tough. Final site own activity main. Describe community box anything either true condition.', '2026-01-18');
INSERT INTO ratings VALUES (6358285, 6774082, 4229683, 1, 'Entire hotel seek. Later stop front yard base game build rather.
Water magazine member. Across what example big technology floor.', '2026-01-13');
INSERT INTO ratings VALUES (4034393, 7899943, 1150752, 3, 'Part modern together kind. Such company answer time reach. Seek try city evidence.
Movie cut threat tell political commercial. News crime three edge be gun.', '2026-04-04');
INSERT INTO ratings VALUES (2340206, 9027045, 7203850, 1, 'Lot share stuff billion send newspaper. Agency ground better drive mother by need. Action light degree add. If lay card city it.', '2026-03-13');
INSERT INTO ratings VALUES (8551668, 6651063, 1610544, 4, 'City market marriage present one travel. Medical last lose result fill himself. Care spend piece play leave stage federal.', '2026-01-20');
INSERT INTO ratings VALUES (3806361, 8834504, 7778473, 1, 'Will production office recognize. Factor brother particularly stay.
Your task though material group democratic behind.
Night job act low. Act major return mean into skin increase.', '2026-05-08');
INSERT INTO ratings VALUES (9070036, 2858315, 8462608, 2, 'Mention she coach production these increase staff. Contain past discussion my long trouble. Hold past house say.', '2026-05-02');
INSERT INTO ratings VALUES (5748696, 2227634, 7854170, 4, 'Bad simple eye suggest during rise. Live word society she. Alone home arrive face. Hot employee after structure recent.', '2026-03-12');
INSERT INTO ratings VALUES (8761329, 6111066, 9651758, 5, 'Long positive ready once crime. Pull machine lead indicate first shake eye. Toward pull student worker behind.', '2026-05-08');
INSERT INTO ratings VALUES (7324454, 6080953, 8670473, 3, 'Art act western my pull clearly. Sister floor north computer them upon safe.', '2026-03-20');
INSERT INTO ratings VALUES (1368264, 9383250, 5830883, 2, 'Decade consider development prepare allow prevent. Will large remain test scientist entire response.
Raise ahead affect into. Mouth help commercial affect board past discuss.', '2026-03-10');
INSERT INTO ratings VALUES (8186410, 4892788, 2847867, 1, 'Fast act town born former. Against most different report everything character notice. Down do property ability project wish.
Happen teacher appear most movie total. Ground new media determine.', '2026-03-26');
INSERT INTO ratings VALUES (5463897, 9636664, 2877495, 2, 'Remain stuff shake raise career future defense. We pick final miss see term left first.
Our site thousand suddenly. Class source actually care including yet food no.', '2026-02-12');
INSERT INTO ratings VALUES (9886519, 2153952, 3928961, 3, 'Modern card four house require could. Trouble energy performance she crime might another but.
Professor check same. Young month must beat above.', '2026-04-13');
INSERT INTO ratings VALUES (9700890, 5576927, 9611592, 4, 'Him study speak. Bank skin keep fine. Life music too piece study agency.
Unit lot way heavy partner fact nothing. Community respond travel these federal travel.', '2026-04-10');
INSERT INTO ratings VALUES (4422820, 5116160, 5097537, 3, 'Majority beautiful crime officer whatever dream people mention. Structure environmental artist military performance avoid. Forward lay none employee phone until material.', '2026-01-24');
INSERT INTO ratings VALUES (4904099, 4100639, 6999905, 1, 'Responsibility treat city soldier reality. Though ever hand institution property ok push. Wait she executive section network.', '2026-04-24');
INSERT INTO ratings VALUES (4543621, 5465007, 3941019, 4, 'Face design one clear. Write indicate social business national floor road.', '2026-01-10');
INSERT INTO ratings VALUES (3035741, 6992003, 3234139, 5, 'Successful wife economy establish particularly above financial. Decade office suddenly structure ask goal. Five cultural kid deep else. Why marriage short law.', '2026-02-23');
INSERT INTO ratings VALUES (5371446, 2986199, 1487850, 5, 'Affect as behind give main society important attorney. Whether single community professor often scene them.
Service of occur our him. Human war low foreign. Along against particularly girl.', '2026-01-11');
INSERT INTO ratings VALUES (5345082, 9526705, 5380722, 4, 'Difficult yard who TV organization record. Source pay cup account federal will. Religious enter information quite learn. Door national individual brother.', '2026-03-10');
INSERT INTO ratings VALUES (8860072, 1988776, 2059919, 2, 'Policy maintain pattern than history four know. Per quickly production there risk. Under change clearly name. Much fear seat benefit.
Consider bag explain some because. Social I live.', '2026-04-29');
INSERT INTO ratings VALUES (1294739, 2227634, 3168880, 4, 'Serve view manage above middle. Tax body weight education position.
Authority seek include learn. Authority usually third themselves interest result billion. Which material so without.', '2026-03-10');
INSERT INTO ratings VALUES (2991598, 6027472, 6109460, 3, 'Allow truth herself listen. Move street act positive site social age few. Assume continue check claim class exist.
Former play those discover may build. Appear happen himself realize study.', '2026-03-13');
INSERT INTO ratings VALUES (9043481, 9312273, 8607388, 2, 'Bank exist drug I.
Likely commercial no item store enter owner. Still theory old crime. Increase class attention.', '2026-01-28');
INSERT INTO ratings VALUES (4219657, 2934427, 4715270, 3, 'Model easy western night inside plan. Performance impact record keep.', '2026-04-21');
INSERT INTO ratings VALUES (7413371, 9112089, 1215045, 5, 'Heart control pass job change reason minute music. Any red leg fear. Itself sit six of spend once bring. Perhaps letter some office each big laugh.', '2026-01-09');
INSERT INTO ratings VALUES (4346844, 3150842, 2603592, 1, 'Really world animal home happen first or. Back none political just.
Yes street why individual then majority product. Hour much better though beautiful big player catch.', '2026-01-09');
INSERT INTO ratings VALUES (1481704, 6774082, 3575576, 3, 'Yeah talk someone community. Stand record audience suggest free authority help. Example raise pay mother.', '2026-03-14');
INSERT INTO ratings VALUES (6345899, 2470259, 2092479, 1, 'Section author lot. Tax food firm democratic phone class indicate. Safe small imagine explain.
Serious thought generation value travel inside just. Indeed carry represent if.', '2026-01-30');
INSERT INTO ratings VALUES (3876809, 3959460, 9513965, 2, 'Fine open until seem. Night plan scientist it pressure. Resource thank great quickly right technology.', '2026-01-14');
INSERT INTO ratings VALUES (3603531, 3307906, 2281058, 1, 'Enough tend state scientist attention class toward them. Read economy house behavior.
Business like study main. Different detail air take stop. List yourself break continue bring.', '2026-03-26');
INSERT INTO ratings VALUES (4995831, 9218001, 2248186, 2, 'Maintain than claim gun. Cell lay book method discover show seek. Receive leave rest serve nice response.
Cut beautiful position oil sister ground some. Weight head education.', '2026-05-10');
INSERT INTO ratings VALUES (1589379, 8812278, 1209499, 1, 'During add feeling final. Today air popular generation major.
Your choice newspaper Congress. Organization past better western eat.
Manage test strong child wait. Smile leave hope ten.', '2026-05-03');
INSERT INTO ratings VALUES (1943993, 2228727, 4900203, 2, 'Yet mean air protect practice east. Card dog action father house class that likely. Staff rate learn.', '2026-01-16');
INSERT INTO ratings VALUES (1447840, 9003017, 7588961, 2, 'Matter chair sea wonder current film. Career rate child owner.
Break boy manager age within hot. Young writer win likely pretty. Look himself provide south indeed they.', '2026-02-09');
INSERT INTO ratings VALUES (6189116, 9092559, 1957125, 3, 'Operation whom care when evening who world. Skin machine authority newspaper answer partner.', '2026-04-28');
INSERT INTO ratings VALUES (5819224, 1892068, 1209499, 4, 'Time generation task most anything. Final open onto call more.
Full lot address business treat south different. Move trouble middle story begin single consumer.', '2026-03-09');
INSERT INTO ratings VALUES (2571508, 6954524, 9517022, 5, 'Individual worry environment Mrs Republican west let. Political provide here design stop fund.', '2026-05-11');
INSERT INTO ratings VALUES (5190558, 6352080, 3985619, 1, 'Industry during cut. Financial conference north office. Time lay open know us media himself.', '2026-03-14');
INSERT INTO ratings VALUES (2176329, 2077334, 8280938, 5, 'Sense door not blood. Media however direction past lot.
Son power garden throughout drop figure reach price. Everything section admit boy happy worry action.', '2026-03-20');
INSERT INTO ratings VALUES (5282115, 3797933, 3495148, 5, 'New up vote generation assume officer real sport. Visit hit office travel necessary record.', '2026-04-01');
INSERT INTO ratings VALUES (1950715, 5520179, 4438274, 4, 'Rise again avoid leg. Outside affect nation fly. Peace fire order to. Account kitchen her consumer fly there.
Sport rather same become report ability. Our final foot do believe above.', '2026-02-15');
INSERT INTO ratings VALUES (2824005, 3603058, 5841139, 5, 'Appear food about plant think. Sing trade into appear. Car player expert.
Recently accept again debate. Behavior institution way listen hold computer on. Over last own growth.', '2026-02-11');
INSERT INTO ratings VALUES (8017150, 9226362, 3416955, 4, 'Charge close city six character performance. The modern hot simple up task size.', '2026-05-12');
INSERT INTO ratings VALUES (2355212, 4332111, 2666559, 1, 'Suddenly money play must office. Analysis seat college majority officer feeling especially.', '2026-05-09');
INSERT INTO ratings VALUES (8683552, 1892068, 3739269, 1, 'National do radio through study road. Research mission simple involve other himself.
Growth serious practice expect someone. Eight country general region herself gas. Add eye practice.', '2026-04-17');
INSERT INTO ratings VALUES (1334979, 2294181, 9808915, 2, 'Commercial name type fall goal painting. Skin better father newspaper service listen. Whatever star represent apply space manager never.
Report dinner morning. Any executive trouble.', '2026-04-08');
INSERT INTO ratings VALUES (6833789, 4332111, 6999905, 5, 'Whom while fire. People least represent democratic color enough. I painting name picture.
Ability popular study agency. Hope education scientist process until list.', '2026-01-17');
INSERT INTO ratings VALUES (2283919, 4772680, 1150752, 2, 'Conference least probably out culture. Sister able guy ten successful. Subject group security international somebody join establish claim.', '2026-01-23');
INSERT INTO ratings VALUES (5174656, 4530532, 5762438, 2, 'Without if realize focus both environment. Likely baby tree race agency throw in.
Four perhaps participant share. May TV middle compare professor nothing.', '2026-01-01');
INSERT INTO ratings VALUES (3140269, 2911738, 2750865, 3, 'Offer issue production major activity same. Blood general maintain result do.
Each believe wrong soon rule computer. Finally bit pattern doctor.', '2026-03-07');
INSERT INTO ratings VALUES (2482161, 1272742, 5862298, 1, 'Hear catch produce now. Expert field hit despite. Financial rich into box report either speech help. Few write specific point raise decision.', '2026-01-09');
INSERT INTO ratings VALUES (6965146, 3959460, 3928961, 5, 'Total boy year level letter surface. Any national including couple game rise everyone wrong.', '2026-02-05');
INSERT INTO ratings VALUES (9674725, 3150842, 6868520, 3, 'Most project that yes sort community meet situation.
Wonder staff address now bed. Heart north perform public.', '2026-01-11');
INSERT INTO ratings VALUES (1166594, 1888680, 6980627, 2, 'Strong true college policy activity. Ten information service American.
Building hear he seem against capital member. Move artist fire body until. Send hour movie man behind crime.', '2026-03-03');
INSERT INTO ratings VALUES (8084783, 8991792, 1299188, 3, 'Number her deal movie special ball since.
Eye smile boy child present assume. Oil sound her eye might create.
Want air happy believe.', '2026-02-26');
INSERT INTO ratings VALUES (8331589, 9925283, 2924296, 5, 'Partner space around run computer within.
Production score treat. Technology represent put run the card pass sort.
Story mind break provide cut maybe. Value miss sound.', '2026-02-19');
INSERT INTO ratings VALUES (9818230, 5131170, 2188866, 4, 'Spend if back series whose support.
Ten cost compare wrong strategy effect amount. Network fine role popular right role rule. Decision during whom rise.
Discover also painting present room.', '2026-02-26');
INSERT INTO ratings VALUES (6128046, 4530854, 2975242, 4, 'Camera type age from. Staff movie individual forward central source full.
Man either military less. Ago very partner data none middle available.', '2026-04-19');
INSERT INTO ratings VALUES (1182742, 1994077, 1277099, 5, 'Fear benefit good writer him sport prevent. Up it use trial feeling country. Trouble line fill doctor from he skill.
Movie cultural purpose special themselves. Open face call.', '2026-03-04');
INSERT INTO ratings VALUES (7527774, 5721829, 1454134, 3, 'Idea network pressure water city. Use debate along wish lay current. Analysis thing write for also step sea audience. Girl weight rest argue future feeling political world.', '2026-01-05');
INSERT INTO ratings VALUES (2783918, 8155828, 4624699, 2, 'Should there over off of the population. Run smile offer treat six lay girl.
Could while huge she onto action help. Close item late down skin modern.', '2026-03-18');
INSERT INTO ratings VALUES (9200868, 7217433, 6039117, 4, 'Couple almost since reduce provide. Network story material move activity. Thank rock sometimes billion. Appear never special and return less loss.', '2026-01-24');
INSERT INTO ratings VALUES (8464565, 7383012, 9142476, 4, 'Pressure music single wall. Nor hope cause traditional offer seven.
Can court report. Main every avoid small society. Visit card next avoid ground control.', '2026-04-28');
INSERT INTO ratings VALUES (9582702, 6111066, 3897785, 4, 'Camera find above. Series call fear every want. Successful deep inside challenge some.
Group necessary conference garden. Everything day indeed just task.', '2026-01-20');
INSERT INTO ratings VALUES (3611100, 5576927, 7991826, 4, 'Trip reality door blue. Whole south between.
Important sea sea industry which most explain. Move first clear inside guess space garden. No young which street instead do.', '2026-02-18');
INSERT INTO ratings VALUES (7827546, 9636664, 2549316, 2, 'We reduce they these bill specific yeah sort. Life require your I. Soon charge require.
A open property easy question behind huge offer.', '2026-03-06');
INSERT INTO ratings VALUES (5649224, 2965959, 1290086, 5, 'Magazine not house forward. Somebody remember leg late go wait interesting military. Analysis past receive site assume.
Term during by ability fall.', '2026-03-22');
INSERT INTO ratings VALUES (6175041, 9925283, 8650087, 5, 'Others good real when degree herself.
Even specific ago born.
Structure hard account memory attention. Education condition receive. Along myself ahead.', '2026-03-17');
INSERT INTO ratings VALUES (1677126, 3487415, 2378137, 4, 'Trip husband project between take as wife. Heavy eye up wind.
Defense morning consider process political the. Them set final energy. With yard clearly both.', '2026-03-25');
INSERT INTO ratings VALUES (6987215, 3891137, 2975242, 1, 'Particularly back single catch nor pressure. Best our animal charge teacher finish particular.
Need thank role television rich debate strategy. Wear model growth network wait give stock.', '2026-02-19');
INSERT INTO ratings VALUES (3733136, 6180741, 5660582, 1, 'Free take try data put six. Else ahead detail decade change where everybody yes. Whom work parent sit knowledge feel green.
Whole half allow avoid thing account note. Face either myself approach.', '2026-04-06');
INSERT INTO ratings VALUES (6461582, 4332111, 6431334, 4, 'Win door certain.
Call can man current age recently around. Place manager father subject. Pm carry huge establish light.
Head step shake. Whatever allow enjoy amount especially purpose pay.', '2026-02-04');
INSERT INTO ratings VALUES (2295378, 9514557, 1957125, 5, 'Ground pick quite fund. Order whom teach. Under prepare under away trip generation.', '2026-04-13');
INSERT INTO ratings VALUES (1730671, 3986188, 4763209, 2, 'Pay region space blue else fish. Team ask happy soldier design. Identify her check wide north. Rather him set natural side life among.', '2026-03-30');
INSERT INTO ratings VALUES (3693360, 6383237, 9946112, 4, 'Production watch boy bad same month whose. Bed late bring popular test. Age never look apply.
Network citizen growth drive get. Situation may collection without.', '2026-05-12');
INSERT INTO ratings VALUES (4890065, 7031540, 9162740, 3, 'Feel care individual surface. Time radio eat none approach himself.
Former already artist appear. Oil technology national return.', '2026-04-11');
INSERT INTO ratings VALUES (7467448, 7383012, 4937369, 5, 'Fund last war event by name surface. Century itself level simply note. Interview mention here whom then adult. Behavior future little summer collection citizen resource real.', '2026-03-23');
INSERT INTO ratings VALUES (7303642, 8515559, 7151159, 2, 'Sort assume reflect both. Left then score mention remain everything. Until head after main always involve song.', '2026-01-31');
INSERT INTO ratings VALUES (8711302, 3610348, 4911891, 3, 'Fight deal range else. Line government nearly.
Ground technology real gun.
Indicate fly than. His seek price something hand light trial. Customer series matter evidence speak year.', '2026-02-12');
INSERT INTO ratings VALUES (2143996, 7537489, 3420721, 3, 'Score view full. Choice through decide responsibility some must someone.
Section image member inside support serve your. Third push group nothing.', '2026-04-04');
INSERT INTO ratings VALUES (8858239, 5494015, 8865172, 5, 'Perform almost trade ground program anything run. American even vote country short.
Response nice image skill note PM likely. Wear environment ready film almost happen.', '2026-02-27');
INSERT INTO ratings VALUES (6464945, 3822974, 9142476, 5, 'Performance it around water level. Bar probably case. They finish young through boy.
Kitchen involve bank thousand kind family. Sometimes upon over might. Card stock million guy nature left.', '2026-01-09');
INSERT INTO ratings VALUES (6267667, 8224728, 8180075, 4, 'Likely others rock court would direction. Responsibility thus toward.
Court leg relationship town less piece. Town one hospital weight team. Collection specific nothing various move.', '2026-03-19');
INSERT INTO ratings VALUES (9797088, 1116912, 2067996, 1, 'Simple three form hope participant space writer art. Staff series foreign inside. Executive truth tell score eight reduce born.', '2026-01-13');
INSERT INTO ratings VALUES (1179961, 9719088, 8179773, 5, 'Now stop former all red alone. Policy individual series. Per role actually garden.
East change range. Physical kid seat strong still trip.', '2026-05-04');
INSERT INTO ratings VALUES (9181963, 1154532, 4937369, 1, 'Small event I vote easy why. Show remember research national capital apply the.
Themselves modern third wall chair. Power better number major base. Fill while ten street.', '2026-01-11');
INSERT INTO ratings VALUES (3928997, 6301649, 3111997, 2, 'Feeling memory town accept herself finish. Follow country continue. Ten place across field. That treatment so knowledge age.
Group nice concern. Point join far safe push question attorney.', '2026-01-10');
INSERT INTO ratings VALUES (6493459, 1529356, 8548716, 2, 'Chance up effect school collection interesting I. Almost task stop trouble half use win.
Plan matter somebody alone. However though month poor yard. Seem during for half.', '2026-05-10');
INSERT INTO ratings VALUES (2154715, 7173601, 1762159, 1, 'Challenge fact lay firm.
Gun around condition visit six serious because. Center federal painting. Try charge by kitchen place.', '2026-01-11');
INSERT INTO ratings VALUES (8106690, 5109828, 3731525, 4, 'Where fall pressure goal paper.
Budget follow thus walk show. Sound house free many maybe. Certainly gas true free arm.', '2026-03-16');
INSERT INTO ratings VALUES (8114654, 7899943, 3248916, 5, 'City in shoulder despite than focus fill. Loss bag arrive capital left. Quite nice section past. Play spend attention seven staff.', '2026-03-26');
INSERT INTO ratings VALUES (4527007, 6651063, 3891059, 4, 'Letter realize lot brother treatment structure represent. Space high center raise.
Make movie safe talk several wife. Similar similar hold religious similar television.', '2026-02-23');
INSERT INTO ratings VALUES (2975538, 4073362, 4900203, 2, 'Continue reveal wear marriage. Town Congress Mrs money wear authority with show. Adult nice responsibility base.
A how kitchen true type walk.', '2026-02-10');
INSERT INTO ratings VALUES (4920699, 8409944, 2666559, 4, 'Create such year food drive message detail. Method probably show as theory admit. Brother so change compare thus. Whether station but own rise where.', '2026-01-25');
INSERT INTO ratings VALUES (1089340, 8812278, 9796068, 2, 'This pattern water decade continue billion law method. Spend if so.
Performance world party clear seven thousand.
Different individual article study apply. Miss mission almost adult before.', '2026-04-06');
INSERT INTO ratings VALUES (2787834, 8218370, 7588961, 4, 'Again hair eight. Fire bit same agreement street consider area can.
Fact she meeting dream. Accept use risk owner owner. Look type key level throw detail.', '2026-03-27');
INSERT INTO ratings VALUES (2675976, 6197631, 4378471, 5, 'Under role let throughout may perhaps. Concern add trip box itself big. Project recently true industry lose.
Spend them music. Likely have follow situation resource at.', '2026-01-05');
INSERT INTO ratings VALUES (1268637, 1272742, 5694564, 2, 'Small serve base analysis inside rock. There shake view prevent reality three few. No coach story successful sure.
Place model serve hundred. Produce beautiful send.', '2026-03-10');
INSERT INTO ratings VALUES (5732155, 7635536, 2050537, 4, 'Beat him only class carry. Special list out let foreign past whether deep. Expert suddenly subject party believe ago suggest.', '2026-03-03');
INSERT INTO ratings VALUES (3143501, 1729245, 9916922, 3, 'Music customer prevent letter half number participant begin. Health with forget forget.
Also me purpose wrong entire allow.
Section crime stage politics. Pattern reach language.', '2026-02-08');
INSERT INTO ratings VALUES (1766399, 9027045, 8308378, 5, 'Away many you put. Feeling movement middle field already husband pressure recent.
Central history decide matter. Laugh around customer occur movement.', '2026-02-23');
INSERT INTO ratings VALUES (7668547, 3700732, 7947286, 3, 'Place somebody skill president live whatever. Author top they look everything fly couple.
Page natural expect official. Discussion central course.', '2026-03-06');
INSERT INTO ratings VALUES (2281604, 7532793, 1736878, 5, 'Game country rest of knowledge give. Way of but movement.
Law care politics media sister and. Different trade decision professional.
Affect boy call television down with.', '2026-02-18');
INSERT INTO ratings VALUES (3087674, 1484905, 6796520, 2, 'Tend real financial despite. All able report guy resource.
Too step small. Bar success data kind.', '2026-05-10');
INSERT INTO ratings VALUES (3392778, 8027778, 6999905, 4, 'Congress teach put agree. Behind audience laugh smile three. Inside theory then attorney its.', '2026-03-09');
INSERT INTO ratings VALUES (3251360, 1339636, 3891092, 2, 'Travel stock answer gas almost determine. Sometimes trouble project travel article school various.', '2026-05-05');
INSERT INTO ratings VALUES (3703822, 9514557, 1518627, 1, 'High beautiful itself significant easy. Even can service politics choice hot. Probably vote policy team grow sort write.', '2026-01-04');
INSERT INTO ratings VALUES (3925723, 7873250, 4669373, 2, 'Share available reduce change late remember. Fire past necessary another thought edge.
Truth commercial listen others push what. Financial continue find.', '2026-01-05');
INSERT INTO ratings VALUES (7018722, 9719088, 1736878, 1, 'Painting range himself bill whose Mrs. Support career large summer. Authority year upon every result somebody defense.
Such how food campaign feeling laugh sell. Exist determine simple blue imagine.', '2026-02-04');
INSERT INTO ratings VALUES (9145882, 1931461, 9611592, 5, 'Training might father sing I. Skill answer particularly term. Think while price involve thank. Approach certainly condition seat.', '2026-02-19');
INSERT INTO ratings VALUES (9806687, 1163615, 4010212, 5, 'Eat over agent while either marriage law. Green structure finish necessary treat provide if. Sometimes hope we than.', '2026-01-26');
INSERT INTO ratings VALUES (8337055, 1154532, 5097537, 1, 'Personal time fish feel. As hold next contain subject key direction collection. Member none why party serious so.', '2026-03-11');
INSERT INTO ratings VALUES (7549762, 3482381, 2887013, 4, 'Cut value long scene floor size. Morning improve control rise campaign break.
Year result office notice piece road everything south.', '2026-01-26');
INSERT INTO ratings VALUES (9120809, 2294181, 2011623, 1, 'Listen assume hand mind in main. Morning think enough base. Agency threat experience.
Call right magazine Democrat civil. Quite over service central hot.', '2026-01-25');
INSERT INTO ratings VALUES (1915323, 6920459, 3989140, 3, 'Happy old read image there subject concern. Religious woman or tax. Quite thought change skill including.
Part property drive tree site remember. Smile system bit fact protect eye lose.', '2026-03-17');
INSERT INTO ratings VALUES (7559883, 7786633, 3262281, 2, 'Color become product enough once environment speak. Town commercial trial whom however energy task. Now key business job few.', '2026-03-25');
INSERT INTO ratings VALUES (6538648, 9027045, 8185814, 2, 'Dog Democrat draw provide. Executive reality adult watch very too. Score could kid enough book bring ground.
Too up live score size although. Democrat how treat article keep. Use wear gun glass.', '2026-02-08');
INSERT INTO ratings VALUES (1813191, 2777401, 1950026, 4, 'Exist more plant again interest bring. Language himself raise travel so say writer.
Character stand down simply produce everything little. Situation long item. Dark in each plan soldier.', '2026-04-11');
INSERT INTO ratings VALUES (9643726, 1328187, 4152237, 1, 'Or happy tax actually one few. Kid executive certain professional.
Threat fast resource hot growth. Likely this cultural performance. Hospital television country movie part.', '2026-02-14');
INSERT INTO ratings VALUES (5771810, 9362387, 9153537, 4, 'Long value which day. Smile dinner his. Itself add herself kitchen later international.', '2026-05-11');
INSERT INTO ratings VALUES (1313889, 4059576, 4193470, 4, 'Data season college. Happen rather chance fast why true however.
Instead point year hope chair. Whatever always treat on eat. Each other tree argue new growth.', '2026-04-22');
INSERT INTO ratings VALUES (8848092, 1163615, 7301468, 2, 'Day military page forget. Early grow north firm price.
Organization realize Mrs color.', '2026-02-26');
INSERT INTO ratings VALUES (3697371, 6197631, 4438274, 5, 'Like look speech point former staff. Night available management. Issue energy pass generation.
Become ahead pressure radio experience trouble. Seem into product voice push evening.', '2026-02-16');
INSERT INTO ratings VALUES (6288788, 4662730, 9946112, 2, 'Him among measure window avoid far. Late mother degree trial beautiful best. Try industry clear clear stand peace agent. Night instead end.', '2026-01-17');
INSERT INTO ratings VALUES (9315162, 2505634, 8822314, 2, 'Meet drive husband and. Difficult meet design character civil miss hard. Scene hear make staff north theory.
Ever listen more assume week. Spend oil eye.', '2026-04-23');
INSERT INTO ratings VALUES (6299394, 9719088, 5830883, 1, 'Same sort stop fight someone. Who southern law participant suddenly. Should government project listen whatever subject understand.
System because cup fire. Provide scene friend half.', '2026-05-08');
INSERT INTO ratings VALUES (2689567, 2168435, 9611592, 3, 'Feeling until join discussion. Himself outside soldier building focus. Behind safe how section young hour.
Already contain next fly federal travel.', '2026-04-23');
INSERT INTO ratings VALUES (7355652, 6236965, 6233896, 3, 'Standard popular stop possible.
Lot beautiful again including system experience third. Others very garden east Democrat long.', '2026-03-19');
INSERT INTO ratings VALUES (9535332, 1339636, 2557122, 1, 'Service page somebody American happy. Everyone attorney improve community admit. Eye first gas.
Hotel entire standard without. Learn and despite. Goal above economy glass nor.', '2026-02-18');
INSERT INTO ratings VALUES (5012937, 1888680, 3245038, 4, 'Single daughter tree. Eat growth six foot ground stage imagine.
Want peace section herself them. Lawyer sea pressure character.', '2026-02-23');
INSERT INTO ratings VALUES (4975031, 9848880, 9651758, 2, 'Up guy through. Score want team itself thing race quickly. Fear dog tough situation.
Upon today matter set. Word could effect education. Walk within subject discussion behavior.', '2026-05-04');
INSERT INTO ratings VALUES (8249705, 7899943, 7459023, 5, 'Well hold design.
Land American control state blood I.
Standard cell lot common. Individual deep whose.', '2026-01-22');
INSERT INTO ratings VALUES (6680782, 6127004, 4382841, 2, 'Seat wait heavy of anyone. Rule protect our sit. Again during hot. Mind staff medical by cup positive.', '2026-02-25');
INSERT INTO ratings VALUES (3591755, 3610348, 8393896, 4, 'Lay improve culture. Pick land age within.
Team century off moment among prepare industry themselves. Raise market then per boy guy.', '2026-04-15');
INSERT INTO ratings VALUES (9671461, 2161934, 8911236, 5, 'Account foot up general offer water record number. Quickly line feel stop choose box.
Including live manager center. Week reality fall exist walk.', '2026-04-27');
INSERT INTO ratings VALUES (9640712, 7532793, 9851125, 4, 'Improve company PM. Morning true piece project seem safe. Field shake agent about.
System include agree free hospital service. Sure usually through still marriage nice. Special tend season.', '2026-02-18');
INSERT INTO ratings VALUES (2045765, 4073362, 1277099, 2, 'Population player thing series spend property. Society parent along protect drug heavy ability. Name paper green authority only question fact.', '2026-04-15');
INSERT INTO ratings VALUES (7753420, 4123196, 4193470, 2, 'Coach better argue us parent. Staff picture where minute throughout story. Poor rather lay treat edge. Sing well Mrs enough west central hour item.', '2026-04-07');
INSERT INTO ratings VALUES (8064394, 6236965, 1758953, 2, 'Available detail ground. Likely again per. Debate nation condition foot.
There remain his manager try general trial north. Process structure new Congress arm born discover. Drug both challenge whole.', '2026-04-04');
INSERT INTO ratings VALUES (7779834, 9514557, 4669373, 5, 'Garden person article. Whose address argue approach pattern sign. Someone final pretty land where. Memory every east through born lead.', '2026-01-22');
INSERT INTO ratings VALUES (3722783, 9728828, 3977294, 1, 'Society ok level clearly fly assume. Old everybody eight benefit.
That box think purpose stay avoid hair. Order air seven white several allow action remain.', '2026-03-23');
INSERT INTO ratings VALUES (6184851, 2961215, 2369537, 4, 'Season security foot clearly people phone above. Several character control assume TV develop agent.', '2026-01-02');
INSERT INTO ratings VALUES (3345507, 8812278, 1098468, 2, 'Arrive address miss wrong cover attention. Story writer form dream economy perform. Song message building term reduce great phone.
Include air investment heart bit.', '2026-04-17');
INSERT INTO ratings VALUES (9770636, 3107150, 7450816, 4, 'Enough represent own better. Task present bill religious ability treat design.
Happen thought city worker. Activity score maintain important common.', '2026-04-05');
INSERT INTO ratings VALUES (5106577, 6820491, 1299188, 4, 'Quickly agreement everyone list capital. Information particularly board body however indeed pull.
Idea pressure major rich section debate. Some ten main enjoy market himself.', '2026-01-14');
INSERT INTO ratings VALUES (6145300, 9003017, 9509070, 1, 'None dream enjoy process. Scene maintain participant direction college. Response gun away special.', '2026-02-01');
INSERT INTO ratings VALUES (8202107, 3891137, 2877495, 2, 'Assume notice place window although. Into cultural since future. Can pull think care never five speak full.', '2026-02-01');
INSERT INTO ratings VALUES (5927405, 6447803, 8308378, 2, 'Measure he activity these probably song cause. Time respond number democratic community heavy memory. Defense speak character stop glass.', '2026-02-09');
INSERT INTO ratings VALUES (5118602, 1211134, 2861626, 1, 'Growth meeting suggest. Learn stage very research five guess two blue. Surface your safe she color spend sell last.', '2026-02-20');
INSERT INTO ratings VALUES (4534753, 1328187, 1639958, 3, 'Glass add traditional seven spring me.
Help participant benefit pick start. Knowledge nation term put leg site how must.', '2026-03-16');
INSERT INTO ratings VALUES (9152681, 5853975, 8462608, 1, 'Year medical important ahead fight. Herself magazine I include season.
Vote center pretty see such action bit. Now former owner. Budget response usually light six hand.', '2026-03-23');

-- =========== skymill.countries (generic) ==========

DROP TABLE IF EXISTS countries;

CREATE TABLE countries (
  id INT NOT NULL,
  iso_code VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL
);

INSERT INTO countries VALUES (336, 'PS', 'Benin');
INSERT INTO countries VALUES (359, 'MM', 'Congo');
INSERT INTO countries VALUES (201, 'AZ', 'Qatar');
INSERT INTO countries VALUES (60, 'KW', 'Thailand');
INSERT INTO countries VALUES (221, 'DK', 'Bolivia');
INSERT INTO countries VALUES (316, 'AZ', 'Maldives');
INSERT INTO countries VALUES (7, 'KM', 'Greenland');
INSERT INTO countries VALUES (359, 'CV', 'Mongolia');
INSERT INTO countries VALUES (115, 'FM', 'Equatorial Guinea');
INSERT INTO countries VALUES (326, 'GD', 'Turks and Caicos Islands');
INSERT INTO countries VALUES (61, 'MR', 'Congo');
INSERT INTO countries VALUES (382, 'NI', 'United Arab Emirates');
INSERT INTO countries VALUES (328, 'DJ', 'Sierra Leone');
INSERT INTO countries VALUES (434, 'FR', 'Georgia');
INSERT INTO countries VALUES (243, 'SY', 'Pitcairn Islands');
INSERT INTO countries VALUES (99, 'DJ', 'Kenya');
INSERT INTO countries VALUES (311, 'ZW', 'Ukraine');
INSERT INTO countries VALUES (251, 'IL', 'Ghana');
INSERT INTO countries VALUES (363, 'BT', 'Andorra');
INSERT INTO countries VALUES (93, 'AD', 'Dominican Republic');
INSERT INTO countries VALUES (152, 'BO', 'Northern Mariana Islands');
INSERT INTO countries VALUES (274, 'VN', 'Botswana');
INSERT INTO countries VALUES (462, 'SD', 'Saint Martin');
INSERT INTO countries VALUES (299, 'GY', 'Northern Mariana Islands');
INSERT INTO countries VALUES (252, 'TW', 'Argentina');
INSERT INTO countries VALUES (8, 'CH', 'Samoa');
INSERT INTO countries VALUES (44, 'SA', 'Bermuda');
INSERT INTO countries VALUES (143, 'IE', 'Slovakia (Slovak Republic)');
INSERT INTO countries VALUES (94, 'ML', 'Comoros');
INSERT INTO countries VALUES (31, 'NO', 'Estonia');
INSERT INTO countries VALUES (413, 'GA', 'Dominican Republic');
INSERT INTO countries VALUES (271, 'IL', 'Hong Kong');
INSERT INTO countries VALUES (182, 'GY', 'Antigua and Barbuda');
INSERT INTO countries VALUES (40, 'BR', 'Brazil');
INSERT INTO countries VALUES (412, 'JP', 'Antigua and Barbuda');

-- =========== skymill.cargo_clients (generic) ==========

DROP TABLE IF EXISTS cargo_clients;

CREATE TABLE cargo_clients (
  id INT NOT NULL,
  client_name VARCHAR(255) NOT NULL,
  address VARCHAR(255) NOT NULL,
  city VARCHAR(255) NOT NULL,
  country_id INT NOT NULL,
  postal_code VARCHAR(255) NOT NULL,
  region VARCHAR(255) NOT NULL
);

INSERT INTO cargo_clients VALUES (73640, 'Riley, Matthews and Garcia', '53256 Adams Station', 'Tiffanyborough', 462, '11847', 'EMEA');
INSERT INTO cargo_clients VALUES (68168, 'Compton-Myers', '987 Wells Corner Suite 871', 'New Brandonport', 299, '23166', 'EMEA');
INSERT INTO cargo_clients VALUES (77171, 'Hamilton-Willis', '507 Amber Way', 'West Cathyburgh', 44, '07495', 'APAC');
INSERT INTO cargo_clients VALUES (95504, 'Jacobs, Romero and Frank', '08240 James Lodge Apt. 099', 'Jenniferhaven', 201, '87373', 'APAC');
INSERT INTO cargo_clients VALUES (34668, 'Cain PLC', '22652 Lewis Plaza Apt. 996', 'Sharonhaven', 359, '37139', 'EMEA');
INSERT INTO cargo_clients VALUES (32518, 'Gregory Ltd', '7711 Duarte Oval', 'South Matthewmouth', 363, '35215', 'EMEA');
INSERT INTO cargo_clients VALUES (70479, 'Fox-Pollard', '501 Patricia Corner', 'Kathrynstad', 115, '61008', 'APAC');
INSERT INTO cargo_clients VALUES (11178, 'Salazar Group', '6453 Mcdaniel Burgs', 'West Eileen', 8, '08310', 'AM');
INSERT INTO cargo_clients VALUES (18819, 'Gray and Sons', '107 Schultz Lodge', 'Moraport', 382, '17348', 'APAC');
INSERT INTO cargo_clients VALUES (22957, 'Padilla Ltd', '27134 Compton Village', 'Deannaland', 271, '02749', 'APAC');
INSERT INTO cargo_clients VALUES (23317, 'Garner, Jones and Bates', '6202 Allen Crossroad', 'East Marcus', 359, '96542', 'AM');
INSERT INTO cargo_clients VALUES (74735, 'Howard Group', '12168 Reyes Centers Suite 699', 'Codyland', 201, '28886', 'EMEA');
INSERT INTO cargo_clients VALUES (78092, 'Lee, Johnson and Evans', '19207 Foley Courts Suite 134', 'North Nicholasbury', 271, '42463', 'AM');
INSERT INTO cargo_clients VALUES (17145, 'Kent-Richard', '03683 Anderson Manor', 'Lake Markhaven', 60, '03171', 'APAC');
INSERT INTO cargo_clients VALUES (46702, 'Shields, Graves and Fields', '87953 Angela Prairie Apt. 588', 'Benjaminburgh', 326, '25656', 'APAC');
INSERT INTO cargo_clients VALUES (16362, 'Meyer-Robinson', '964 Lewis Squares Suite 119', 'Paynetown', 271, '89574', 'EMEA');
INSERT INTO cargo_clients VALUES (35306, 'Edwards, Armstrong and Moore', '2865 Donaldson Ridges Suite 584', 'Toddtown', 271, '41446', 'AM');
INSERT INTO cargo_clients VALUES (84370, 'Sanders-Ortega', '628 Sharp Summit Apt. 697', 'Jaredbury', 316, '69405', 'AM');
INSERT INTO cargo_clients VALUES (73217, 'Armstrong-Scott', '0755 Michelle Ramp', 'West Brianhaven', 7, '87647', 'APAC');
INSERT INTO cargo_clients VALUES (78897, 'Ruiz, Tucker and Rose', '523 Santana Motorway Suite 747', 'Jamesburgh', 413, '27333', 'AM');
INSERT INTO cargo_clients VALUES (42830, 'Sanders LLC', '14967 Gregory Square Apt. 890', 'Geraldfurt', 115, '55044', 'EMEA');
INSERT INTO cargo_clients VALUES (61780, 'Crawford and Sons', '5390 Anderson Lake', 'Dylanborough', 143, '78277', 'EMEA');
INSERT INTO cargo_clients VALUES (99845, 'Walker, Keller and Mendoza', '3717 Smith Rapids', 'Donnaland', 221, '49002', 'AM');
INSERT INTO cargo_clients VALUES (20032, 'Smith, Anderson and Turner', '37744 Ball Place Apt. 224', 'Port Margaretmouth', 382, '05971', 'EMEA');
INSERT INTO cargo_clients VALUES (73357, 'Jackson, Collins and Miller', '621 Cunningham Place', 'Hayneston', 271, '64563', 'AM');
INSERT INTO cargo_clients VALUES (63674, 'Gonzalez-Willis', '96708 Susan Mission Apt. 715', 'South Michael', 182, '52470', 'EMEA');
INSERT INTO cargo_clients VALUES (37665, 'Moses Group', '2674 Jamie Highway Suite 793', 'Christophershire', 326, '70267', 'AM');
INSERT INTO cargo_clients VALUES (75571, 'Price Group', '186 Ford Gardens Suite 971', 'Kellychester', 412, '17292', 'APAC');
INSERT INTO cargo_clients VALUES (46879, 'Sanchez-Simmons', '07697 Julia Ports', 'Sandraview', 462, '14895', 'AM');
INSERT INTO cargo_clients VALUES (85299, 'Green-Hill', '599 Brian Lane Apt. 203', 'New Keith', 316, '96326', 'EMEA');
INSERT INTO cargo_clients VALUES (85128, 'Perez, Horton and Morales', '25922 Catherine Streets', 'West Brenda', 251, '89707', 'AM');
INSERT INTO cargo_clients VALUES (56046, 'Ball-Smith', '281 Brandon Springs Apt. 734', 'East Meagan', 252, '46511', 'EMEA');
INSERT INTO cargo_clients VALUES (37865, 'Soto, Hughes and Graham', '629 Gardner Skyway', 'Port Erin', 94, '05119', 'AM');
INSERT INTO cargo_clients VALUES (66672, 'Wagner-Bush', '592 Michael Harbor Suite 963', 'East Nicole', 152, '25413', 'AM');
INSERT INTO cargo_clients VALUES (58401, 'Sanchez-Pennington', '69801 Herring Spurs Apt. 939', 'Jamieland', 93, '87877', 'APAC');
INSERT INTO cargo_clients VALUES (38582, 'Parker, Massey and Martin', '90512 Laura Coves Suite 889', 'East Anna', 462, '83680', 'EMEA');
INSERT INTO cargo_clients VALUES (81142, 'Davis-Heath', '22394 Bell Port', 'South Julieville', 311, '13444', 'APAC');
INSERT INTO cargo_clients VALUES (95020, 'Arroyo and Sons', '20987 Brandon Plain', 'Jenniferhaven', 252, '54283', 'APAC');
INSERT INTO cargo_clients VALUES (87624, 'Perez-Fox', '29596 Joyce Shores Suite 160', 'Jillianmouth', 40, '44149', 'EMEA');
INSERT INTO cargo_clients VALUES (29828, 'Chung, Hernandez and Weaver', '81512 Cynthia Lodge Suite 214', 'Williamfurt', 382, '07458', 'AM');
INSERT INTO cargo_clients VALUES (11136, 'Garcia Ltd', '70617 Acevedo Circles Suite 783', 'East Andrewstad', 336, '97639', 'AM');
INSERT INTO cargo_clients VALUES (34263, 'Brooks, Garcia and Vasquez', '1188 Henderson Rapid', 'Feliciastad', 251, '39833', 'AM');
INSERT INTO cargo_clients VALUES (67067, 'Wallace LLC', '99340 Lauren Mount Apt. 709', 'Lake Elaine', 31, '21759', 'AM');
INSERT INTO cargo_clients VALUES (41156, 'Rich Ltd', '50269 Schmitt Knoll', 'Smithton', 143, '23527', 'EMEA');
INSERT INTO cargo_clients VALUES (69973, 'Rios, Lewis and Tucker', '9834 Solis Prairie', 'Mooreberg', 143, '85314', 'EMEA');
INSERT INTO cargo_clients VALUES (87478, 'Lewis, Evans and Reynolds', '538 Davis Mission', 'Wardmouth', 252, '72789', 'APAC');
INSERT INTO cargo_clients VALUES (45027, 'Stark Ltd', '6973 Laura Lock', 'Joneschester', 40, '14712', 'AM');
INSERT INTO cargo_clients VALUES (69183, 'Dalton LLC', '1509 Robertson Shores', 'Kimberlyborough', 359, '11634', 'AM');
INSERT INTO cargo_clients VALUES (66374, 'Freeman, Chapman and Adkins', '0891 Jasmine Cape', 'Lake Beckymouth', 412, '79136', 'EMEA');
INSERT INTO cargo_clients VALUES (95509, 'Young PLC', '113 Jessica Underpass', 'Mathewstown', 44, '96050', 'AM');
INSERT INTO cargo_clients VALUES (51197, 'Macdonald-Wilcox', '98018 Cameron Coves Apt. 836', 'Port Kara', 7, '45340', 'AM');
INSERT INTO cargo_clients VALUES (37468, 'Brown, Tapia and Woods', '20212 Jones Lakes', 'West Leah', 94, '60655', 'APAC');
INSERT INTO cargo_clients VALUES (30603, 'Randall PLC', '223 Allen View', 'Port Timothy', 412, '28519', 'AM');
INSERT INTO cargo_clients VALUES (31584, 'Smith Inc', '94556 Marie Trafficway Suite 929', 'Reginaview', 40, '70242', 'AM');
INSERT INTO cargo_clients VALUES (83314, 'Christensen, Collier and Johnson', '372 Phillip Glen', 'Port Katrinafurt', 336, '09944', 'EMEA');
INSERT INTO cargo_clients VALUES (38903, 'Rollins and Sons', '84014 Eric Lake Apt. 064', 'East Dianebury', 7, '60316', 'EMEA');
INSERT INTO cargo_clients VALUES (90584, 'Hodges-Medina', '949 Cruz Station', 'West Jasonberg', 115, '19131', 'AM');
INSERT INTO cargo_clients VALUES (52750, 'Fuller-Bailey', '607 Cynthia Centers Suite 433', 'Dannyside', 44, '01510', 'AM');
INSERT INTO cargo_clients VALUES (11056, 'Turner-Santos', '2907 Smith Way Suite 961', 'Lake Brianberg', 94, '19976', 'APAC');
INSERT INTO cargo_clients VALUES (64620, 'Montgomery-Harrell', '92771 Michael Via Suite 434', 'Amandaborough', 243, '02782', 'EMEA');
INSERT INTO cargo_clients VALUES (12096, 'Page LLC', '968 Stewart Parkways Apt. 236', 'Christineview', 316, '44997', 'AM');
INSERT INTO cargo_clients VALUES (93903, 'Hughes Inc', '4380 Davis Mall Suite 886', 'East Alejandraburgh', 93, '73504', 'APAC');
INSERT INTO cargo_clients VALUES (84900, 'Thomas, Mclaughlin and King', '8619 Brian Oval', 'East Christopherbury', 243, '28869', 'AM');
INSERT INTO cargo_clients VALUES (81696, 'Moore Group', '60805 Robert Walk', 'Andrewhaven', 61, '23392', 'APAC');
INSERT INTO cargo_clients VALUES (27794, 'Davis PLC', '109 Andrea Drive', 'Ramirezshire', 316, '86042', 'APAC');
INSERT INTO cargo_clients VALUES (31016, 'Maynard, Ramirez and Perez', '785 Merritt Crossing', 'Suttonhaven', 271, '49528', 'AM');
INSERT INTO cargo_clients VALUES (75270, 'Hall LLC', '2555 Pollard Creek', 'Gravesview', 326, '26212', 'AM');
INSERT INTO cargo_clients VALUES (49609, 'Davis-Chan', '012 Smith Spur', 'Kennethshire', 60, '12977', 'AM');
INSERT INTO cargo_clients VALUES (81855, 'Williams and Sons', '690 Kaitlin Mountains', 'Port Cynthiaview', 359, '12101', 'AM');
INSERT INTO cargo_clients VALUES (13552, 'Bennett, May and Vasquez', '35383 Megan Fort', 'East Brianna', 7, '97416', 'EMEA');
INSERT INTO cargo_clients VALUES (26625, 'Hutchinson, Castillo and Barnett', '10015 Christopher Trafficway', 'Strongland', 363, '50208', 'EMEA');
INSERT INTO cargo_clients VALUES (76275, 'Jones, Galvan and Hall', '832 Kathleen Mount', 'Gonzalesberg', 311, '81997', 'EMEA');
INSERT INTO cargo_clients VALUES (40203, 'Young, Johns and Aguirre', '008 Henderson Greens Apt. 317', 'Toddton', 359, '51200', 'AM');
INSERT INTO cargo_clients VALUES (51067, 'Murphy, Clark and Hodges', '320 Lisa River', 'Daleview', 382, '50497', 'EMEA');
INSERT INTO cargo_clients VALUES (20438, 'Henderson PLC', '46180 Brooks Vista Apt. 329', 'North Christophermouth', 462, '19381', 'EMEA');
INSERT INTO cargo_clients VALUES (54150, 'Allison Group', '18586 Cody Inlet', 'East Taylor', 31, '34189', 'APAC');
INSERT INTO cargo_clients VALUES (22999, 'Thompson and Sons', '8428 Tracy Pike Apt. 361', 'Krystaltown', 44, '44321', 'AM');
INSERT INTO cargo_clients VALUES (21786, 'Boyd, Sanchez and Stewart', '3798 Richard Pines Suite 253', 'Chambershaven', 115, '75471', 'AM');
INSERT INTO cargo_clients VALUES (36421, 'Everett Group', '09532 Bryant Rapids Apt. 014', 'East Kayla', 7, '25434', 'AM');
INSERT INTO cargo_clients VALUES (35963, 'Collins LLC', '44031 Shelby Center', 'East Brianna', 31, '91419', 'EMEA');
INSERT INTO cargo_clients VALUES (14388, 'Hoffman-Medina', '1884 Lewis Villages', 'Randallmouth', 31, '73375', 'APAC');
INSERT INTO cargo_clients VALUES (85047, 'Hammond, Goodman and Tran', '461 Carter Spurs', 'New Kristenchester', 326, '13129', 'AM');
INSERT INTO cargo_clients VALUES (21481, 'Carr, Todd and Trevino', '2642 Wheeler Tunnel Suite 447', 'Richardsonborough', 243, '15176', 'AM');
INSERT INTO cargo_clients VALUES (50739, 'Miller-Irwin', '40470 Daugherty Course Apt. 632', 'Joanburgh', 243, '10710', 'EMEA');
INSERT INTO cargo_clients VALUES (74533, 'Davis LLC', '9127 Dale Forks Suite 140', 'Melissaville', 412, '99047', 'EMEA');
INSERT INTO cargo_clients VALUES (88271, 'Bailey LLC', '589 Kristine Forge Suite 015', 'Toddland', 251, '60338', 'AM');
INSERT INTO cargo_clients VALUES (31694, 'Long-Clark', '230 Maria Tunnel Suite 382', 'Hansonborough', 359, '08866', 'AM');
INSERT INTO cargo_clients VALUES (71907, 'Lewis, White and Cisneros', '50055 Mcdaniel Fords Suite 566', 'Ryanmouth', 434, '97656', 'EMEA');
INSERT INTO cargo_clients VALUES (88944, 'Green Inc', '77023 Christensen Tunnel', 'South Josephview', 271, '32156', 'AM');
INSERT INTO cargo_clients VALUES (42117, 'Byrd Ltd', '58523 Stewart Unions Suite 787', 'Taylorland', 8, '13708', 'APAC');
INSERT INTO cargo_clients VALUES (73315, 'James-Cherry', '0406 Jonathan Garden Apt. 508', 'Terrimouth', 413, '27380', 'APAC');
INSERT INTO cargo_clients VALUES (25571, 'Delacruz, Anderson and Kelley', '679 Victoria Pines Apt. 299', 'South Gary', 382, '81012', 'APAC');
INSERT INTO cargo_clients VALUES (19125, 'Hill LLC', '72120 Kline Land Apt. 738', 'Cassandraside', 152, '43441', 'EMEA');
INSERT INTO cargo_clients VALUES (32608, 'Smith, Parsons and Schmidt', '70540 Raymond Walk', 'Port Seanport', 115, '13803', 'APAC');
INSERT INTO cargo_clients VALUES (67410, 'Sims-Williams', '93864 Johnson Street', 'New Danielton', 412, '74962', 'AM');
INSERT INTO cargo_clients VALUES (93319, 'Hobbs, Hampton and Phelps', '80496 Davis Burg', 'East Deniseton', 31, '04566', 'AM');
INSERT INTO cargo_clients VALUES (64986, 'Jones, Roberson and Williams', '256 Troy Springs Suite 252', 'East Robin', 363, '30042', 'AM');
INSERT INTO cargo_clients VALUES (26822, 'Williams LLC', '71601 Jacob View Suite 076', 'Knightland', 363, '04462', 'AM');
INSERT INTO cargo_clients VALUES (96914, 'Collins-Gillespie', '7645 Renee Turnpike Suite 937', 'South Austinberg', 44, '71768', 'APAC');
INSERT INTO cargo_clients VALUES (55608, 'Murray-Mccullough', '36745 Kimberly Corners', 'East Jennifer', 299, '72236', 'EMEA');
INSERT INTO cargo_clients VALUES (32861, 'Brown PLC', '2182 Mata Burgs Suite 077', 'Port David', 412, '34555', 'EMEA');
INSERT INTO cargo_clients VALUES (28008, 'Montgomery, Brown and Nguyen', '85328 Duran Isle Apt. 720', 'Navarroborough', 31, '10352', 'AM');
INSERT INTO cargo_clients VALUES (30534, 'Williams, Anderson and Vasquez', '0217 Gray Hills Apt. 403', 'Port Vanessafort', 274, '71846', 'EMEA');
INSERT INTO cargo_clients VALUES (48241, 'Gonzalez Ltd', '630 Hughes Streets Suite 100', 'Port Jennifer', 359, '76237', 'APAC');
INSERT INTO cargo_clients VALUES (34984, 'Ortiz, Daniel and Smith', '8506 Fritz Divide Suite 381', 'Lindaview', 201, '48471', 'APAC');
INSERT INTO cargo_clients VALUES (87094, 'Maynard, Hayes and Santiago', '31638 Justin Glen Apt. 977', 'West Michael', 328, '11828', 'EMEA');
INSERT INTO cargo_clients VALUES (41503, 'Howell, Prince and Mccoy', '445 Susan Field', 'Smithport', 61, '39123', 'APAC');
INSERT INTO cargo_clients VALUES (69028, 'Powell PLC', '446 Karen Centers Suite 878', 'Christineville', 143, '14811', 'AM');
INSERT INTO cargo_clients VALUES (14955, 'Wyatt Ltd', '771 Hart Views', 'South Matthew', 311, '42132', 'APAC');
INSERT INTO cargo_clients VALUES (66620, 'Lopez-Richardson', '750 Monroe Ports', 'Kimberlyborough', 316, '45895', 'AM');
INSERT INTO cargo_clients VALUES (65481, 'Griffin, Wagner and Padilla', '90326 Alexander Manor', 'North Jaredville', 359, '77743', 'AM');
INSERT INTO cargo_clients VALUES (47786, 'Vazquez Inc', '4437 Byrd Ferry Suite 898', 'Rubenbury', 93, '47668', 'EMEA');
INSERT INTO cargo_clients VALUES (89117, 'Garcia Inc', '37525 Miller Prairie Apt. 948', 'Port Michaelberg', 252, '79138', 'EMEA');
INSERT INTO cargo_clients VALUES (63186, 'Marquez, Lopez and Durham', '235 Rodriguez Rapids', 'New Stephanieburgh', 299, '61380', 'AM');
INSERT INTO cargo_clients VALUES (58358, 'Carrillo Inc', '74962 Amanda Loaf Apt. 902', 'Vasquezfurt', 115, '46526', 'APAC');
INSERT INTO cargo_clients VALUES (48435, 'Porter-Bolton', '975 Andrew Port', 'Josephview', 311, '60019', 'AM');
INSERT INTO cargo_clients VALUES (61748, 'Hunt-Marquez', '1382 Boyle Terrace Suite 067', 'Port Ana', 336, '69807', 'EMEA');
INSERT INTO cargo_clients VALUES (11754, 'Moore Group', '2355 Johnson Fork Apt. 877', 'West John', 182, '02168', 'EMEA');
INSERT INTO cargo_clients VALUES (16790, 'Miller LLC', '59754 Ramos Place', 'Riverstown', 359, '48783', 'AM');
INSERT INTO cargo_clients VALUES (18630, 'Griffin Inc', '3158 Richmond Parks Apt. 899', 'East Codyside', 359, '36229', 'AM');
INSERT INTO cargo_clients VALUES (40856, 'Hudson Ltd', '5309 Ryan Highway', 'East Jason', 316, '81731', 'EMEA');
INSERT INTO cargo_clients VALUES (25806, 'Rubio PLC', '266 Hall Plaza Suite 440', 'West Carlabury', 413, '49173', 'AM');
INSERT INTO cargo_clients VALUES (43313, 'Jones-Thomas', '21132 Johnson Knolls', 'West Shane', 462, '22847', 'APAC');
INSERT INTO cargo_clients VALUES (55037, 'Mathis Group', '860 Justin Lakes', 'Port Donna', 316, '25481', 'APAC');
INSERT INTO cargo_clients VALUES (35881, 'Martin Group', '2288 John Shoal Apt. 283', 'North Jennifer', 143, '60339', 'EMEA');
INSERT INTO cargo_clients VALUES (78382, 'Ramos LLC', '696 Barnett Forges Suite 518', 'Hannahton', 182, '30902', 'APAC');
INSERT INTO cargo_clients VALUES (67867, 'Wilson, Flores and Petty', '88892 Terry Land', 'Aaronland', 221, '06653', 'APAC');
INSERT INTO cargo_clients VALUES (48910, 'Booth, Ray and Carr', '59078 Cunningham Port Apt. 341', 'Josephview', 94, '79023', 'AM');
INSERT INTO cargo_clients VALUES (43047, 'Brown Inc', '7783 Dawn Plains', 'West Caroline', 363, '92704', 'AM');
INSERT INTO cargo_clients VALUES (74354, 'Burton and Sons', '0161 Martin Walk Apt. 942', 'Michaelbury', 7, '87716', 'AM');
INSERT INTO cargo_clients VALUES (63996, 'Benson, Dennis and Richardson', '995 Jacob Tunnel Apt. 486', 'Thomashaven', 326, '26074', 'AM');
INSERT INTO cargo_clients VALUES (66975, 'Williamson-Lopez', '13253 Baker Village', 'East Cheryl', 252, '13817', 'APAC');
INSERT INTO cargo_clients VALUES (97714, 'Foley Group', '915 Ramos Springs Apt. 858', 'Longside', 94, '53911', 'APAC');
INSERT INTO cargo_clients VALUES (53546, 'Chung-Lawson', '40447 Glass Mountains', 'Robertton', 152, '85615', 'APAC');
INSERT INTO cargo_clients VALUES (27543, 'Shelton-Reyes', '082 Alexander Locks Suite 943', 'East Mary', 299, '79107', 'AM');
INSERT INTO cargo_clients VALUES (13533, 'Clarke-Newman', '27761 Ray Shore', 'Jonesstad', 7, '92287', 'EMEA');
INSERT INTO cargo_clients VALUES (80764, 'Flowers-Wilson', '66388 Koch Manors Suite 177', 'Murphyton', 359, '16687', 'EMEA');
INSERT INTO cargo_clients VALUES (17062, 'Turner-Wiggins', '762 Jones Center', 'North Ellenbury', 336, '87581', 'EMEA');
INSERT INTO cargo_clients VALUES (88059, 'Mitchell and Sons', '83107 Lindsay Islands', 'Lindsaytown', 40, '59832', 'AM');
INSERT INTO cargo_clients VALUES (68759, 'Johnson-Mccoy', '732 Mark Gateway Apt. 497', 'Bryantfort', 201, '95081', 'APAC');

-- =========== skymill.cargo_types (generic) ==========

DROP TABLE IF EXISTS cargo_types;

CREATE TABLE cargo_types (
  id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL
);

INSERT INTO cargo_types VALUES (1, 'containers', 'Standard shipping containers (boxes, pallets)');
INSERT INTO cargo_types VALUES (2, 'bulk', 'Loose bulk goods (grains, minerals)');
INSERT INTO cargo_types VALUES (3, 'refrigerated', 'Temperature-controlled perishables');
INSERT INTO cargo_types VALUES (4, 'vehicles', 'Cars, trucks, and other vehicles');

-- =========== skymill.cargo_shipments (generic) ==========

DROP TABLE IF EXISTS cargo_shipments;

CREATE TABLE cargo_shipments (
  id INT NOT NULL,
  cargo_flight_id INT NOT NULL,
  cargo_type_id INT NOT NULL,
  weight_kg INT NOT NULL,
  client_id INT NOT NULL,
  revenue DECIMAL(15, 4) NOT NULL
);

INSERT INTO cargo_shipments VALUES (2628060, 787243, 1, 14641, 49609, 826.8578226465092);
INSERT INTO cargo_shipments VALUES (3170042, 267291, 3, 31718, 53546, 484.41142972825526);
INSERT INTO cargo_shipments VALUES (2187460, 881607, 1, 22561, 34984, 456.8377843659576);
INSERT INTO cargo_shipments VALUES (1938435, 301037, 4, 5030, 88271, 671.1673074241995);
INSERT INTO cargo_shipments VALUES (3680613, 267291, 3, 34505, 68759, 232.5061491532152);
INSERT INTO cargo_shipments VALUES (1464792, 878046, 4, 28034, 29828, 323.7473785893439);
INSERT INTO cargo_shipments VALUES (5068413, 782915, 4, 42428, 18819, 967.2910377994446);
INSERT INTO cargo_shipments VALUES (1755439, 572361, 4, 17199, 11056, 213.29079771075544);
INSERT INTO cargo_shipments VALUES (4043283, 678322, 4, 26528, 31584, 236.99975969270804);
INSERT INTO cargo_shipments VALUES (9185474, 572361, 1, 16570, 45027, 400.7740296501223);
INSERT INTO cargo_shipments VALUES (8172581, 162259, 3, 43348, 55037, 555.9214725022752);
INSERT INTO cargo_shipments VALUES (6494350, 304639, 3, 8642, 76275, 166.8528253790521);
INSERT INTO cargo_shipments VALUES (4294050, 675298, 1, 1469, 73315, 42.78208025580332);
INSERT INTO cargo_shipments VALUES (7290496, 878046, 2, 48160, 29828, 118.51780850424365);
INSERT INTO cargo_shipments VALUES (8651230, 581397, 4, 33879, 73357, 197.0955411563401);
INSERT INTO cargo_shipments VALUES (4287670, 815951, 2, 7375, 40203, 432.48745559408763);
INSERT INTO cargo_shipments VALUES (4631261, 446354, 2, 10469, 75571, 48.461704037486506);
INSERT INTO cargo_shipments VALUES (7568312, 614681, 3, 48577, 20438, 689.8013893099204);
INSERT INTO cargo_shipments VALUES (4831908, 170897, 2, 34310, 12096, 410.0395371186141);
INSERT INTO cargo_shipments VALUES (5215068, 866524, 4, 36683, 48241, 257.7885181113007);
INSERT INTO cargo_shipments VALUES (7483690, 948750, 1, 7066, 25571, 361.4772666182952);
INSERT INTO cargo_shipments VALUES (5490503, 837435, 3, 26524, 14955, 18.979618651587703);
INSERT INTO cargo_shipments VALUES (5605941, 655713, 1, 22973, 37865, 205.20962601995674);
INSERT INTO cargo_shipments VALUES (7262877, 373731, 1, 39984, 69973, 67.22512695439309);
INSERT INTO cargo_shipments VALUES (3964297, 272236, 4, 9707, 35881, 551.0367300905255);
INSERT INTO cargo_shipments VALUES (7717478, 290811, 4, 24244, 17145, 860.4736612023809);
INSERT INTO cargo_shipments VALUES (1141956, 995244, 4, 45646, 41156, 764.1297547375497);
INSERT INTO cargo_shipments VALUES (8930039, 817972, 2, 8570, 73315, 162.37202907972758);
INSERT INTO cargo_shipments VALUES (6811259, 447164, 3, 11127, 14955, 903.6902746361194);
INSERT INTO cargo_shipments VALUES (1683399, 830748, 4, 29864, 61748, 613.4687996276316);
INSERT INTO cargo_shipments VALUES (8017257, 230833, 3, 2710, 64986, 958.5358896547822);
INSERT INTO cargo_shipments VALUES (5002226, 223703, 3, 18607, 34668, 412.88704096629283);
INSERT INTO cargo_shipments VALUES (5891536, 415908, 4, 3000, 88059, 598.2693087646473);
INSERT INTO cargo_shipments VALUES (4222258, 873886, 2, 45338, 42830, 338.2754342487638);
INSERT INTO cargo_shipments VALUES (9976676, 224194, 1, 15721, 78897, 568.7386688861407);
INSERT INTO cargo_shipments VALUES (2357085, 286320, 4, 3980, 31694, 765.9864052228568);
INSERT INTO cargo_shipments VALUES (1635189, 555106, 4, 41555, 78897, 206.965158028689);
INSERT INTO cargo_shipments VALUES (9131682, 633887, 1, 38628, 78382, 353.19370229612514);
INSERT INTO cargo_shipments VALUES (6841667, 415908, 3, 39769, 71907, 914.4288723604722);
INSERT INTO cargo_shipments VALUES (8838511, 649000, 3, 25546, 70479, 361.89891405317474);
INSERT INTO cargo_shipments VALUES (2567309, 787243, 4, 31805, 58358, 189.2334874408964);
INSERT INTO cargo_shipments VALUES (1315125, 373731, 2, 47720, 61780, 21.02599792707316);
INSERT INTO cargo_shipments VALUES (2891768, 667591, 4, 26617, 99845, 760.5647833412563);
INSERT INTO cargo_shipments VALUES (3632193, 198178, 1, 17254, 81696, 524.9351967181658);
INSERT INTO cargo_shipments VALUES (9800791, 932235, 4, 35349, 11136, 258.4388750195421);
INSERT INTO cargo_shipments VALUES (6516917, 460185, 3, 22711, 69973, 995.6661967258393);
INSERT INTO cargo_shipments VALUES (2160680, 105533, 4, 30399, 87624, 807.4518334088091);
INSERT INTO cargo_shipments VALUES (5921034, 176495, 4, 13008, 88271, 348.68796843362725);
INSERT INTO cargo_shipments VALUES (9933115, 346445, 1, 1286, 28008, 910.2578785842225);
INSERT INTO cargo_shipments VALUES (4809613, 691232, 2, 30625, 18819, 301.6297373197504);
INSERT INTO cargo_shipments VALUES (9563194, 301037, 4, 28506, 27543, 97.4839689590813);
INSERT INTO cargo_shipments VALUES (5072562, 997802, 2, 26317, 95504, 125.31527882566506);
INSERT INTO cargo_shipments VALUES (5371752, 388326, 4, 49085, 23317, 929.9191812122724);
INSERT INTO cargo_shipments VALUES (4028793, 742858, 2, 13742, 73315, 94.39832837606954);
INSERT INTO cargo_shipments VALUES (3338284, 358509, 3, 10843, 40203, 842.913704725122);
INSERT INTO cargo_shipments VALUES (8089755, 932171, 4, 14526, 28008, 420.87582748942884);
INSERT INTO cargo_shipments VALUES (4168350, 374226, 1, 17538, 38903, 134.64381247846836);
INSERT INTO cargo_shipments VALUES (7596362, 743993, 3, 34143, 73357, 652.4800446825908);
INSERT INTO cargo_shipments VALUES (5731697, 905715, 2, 1018, 48241, 461.6283260264428);
INSERT INTO cargo_shipments VALUES (3558723, 340760, 3, 9994, 88059, 763.9573621083008);
INSERT INTO cargo_shipments VALUES (1700974, 203567, 1, 5706, 37865, 150.5598264805912);
INSERT INTO cargo_shipments VALUES (2561744, 240695, 4, 32989, 13533, 877.701832656575);
INSERT INTO cargo_shipments VALUES (6309003, 154036, 1, 20963, 64620, 479.45350619611946);
INSERT INTO cargo_shipments VALUES (3057796, 687019, 2, 34051, 30534, 573.8371638613024);
INSERT INTO cargo_shipments VALUES (9923411, 776366, 3, 46445, 69973, 216.47866913404712);
INSERT INTO cargo_shipments VALUES (9057713, 815951, 1, 1884, 48241, 494.7406956580591);
INSERT INTO cargo_shipments VALUES (9681567, 373731, 4, 21212, 42830, 791.3998341550677);
INSERT INTO cargo_shipments VALUES (8509887, 910151, 2, 49228, 27543, 967.046935073804);
INSERT INTO cargo_shipments VALUES (9794478, 787243, 2, 32483, 17062, 923.3694802405125);
INSERT INTO cargo_shipments VALUES (7354133, 932235, 1, 40988, 88944, 47.53674412092235);
INSERT INTO cargo_shipments VALUES (3819068, 488373, 1, 47888, 71907, 792.6882430694658);
INSERT INTO cargo_shipments VALUES (7017298, 900209, 2, 28160, 35881, 236.76057442399056);
INSERT INTO cargo_shipments VALUES (3911455, 268775, 1, 41008, 12096, 629.4253909031518);
INSERT INTO cargo_shipments VALUES (4589354, 505854, 1, 11249, 93903, 998.34102178657);
INSERT INTO cargo_shipments VALUES (2943867, 170897, 4, 45285, 26822, 970.9474189609631);
INSERT INTO cargo_shipments VALUES (3205933, 481738, 1, 35916, 96914, 209.60591432876961);
INSERT INTO cargo_shipments VALUES (5434460, 787243, 4, 2719, 81142, 27.758901591117713);
INSERT INTO cargo_shipments VALUES (3597848, 162305, 4, 30101, 34263, 674.1738901575213);
INSERT INTO cargo_shipments VALUES (2499447, 639436, 2, 40690, 87624, 909.6592994268032);
INSERT INTO cargo_shipments VALUES (7290964, 127089, 2, 37122, 30603, 605.3688346207493);
INSERT INTO cargo_shipments VALUES (2254089, 340760, 4, 31558, 52750, 730.934752921515);
INSERT INTO cargo_shipments VALUES (8449169, 353497, 2, 8392, 73357, 387.42574540858834);
INSERT INTO cargo_shipments VALUES (1351529, 970881, 3, 24669, 46702, 270.1661249275101);
INSERT INTO cargo_shipments VALUES (7672738, 251048, 2, 31750, 23317, 240.30073261752494);
INSERT INTO cargo_shipments VALUES (9152209, 792511, 3, 11642, 21786, 261.3249459956072);
INSERT INTO cargo_shipments VALUES (7100882, 777504, 2, 13305, 85299, 276.92715017069224);
INSERT INTO cargo_shipments VALUES (1162777, 413138, 4, 46384, 27794, 944.8455986426102);
INSERT INTO cargo_shipments VALUES (2357622, 505854, 2, 31346, 56046, 390.6325836580831);
INSERT INTO cargo_shipments VALUES (8779612, 558558, 2, 4288, 65481, 923.7333883401401);
INSERT INTO cargo_shipments VALUES (8287180, 961801, 3, 30261, 63674, 580.389910630944);
INSERT INTO cargo_shipments VALUES (5871218, 447164, 4, 25664, 67867, 911.850717426519);
INSERT INTO cargo_shipments VALUES (9767903, 776366, 2, 43632, 48241, 49.98676315720385);
INSERT INTO cargo_shipments VALUES (2108841, 912883, 3, 45387, 58401, 185.43130476635073);
INSERT INTO cargo_shipments VALUES (7038790, 280979, 3, 6072, 18819, 623.6892601055321);
INSERT INTO cargo_shipments VALUES (8306517, 881607, 4, 25125, 26822, 309.50843481949806);
INSERT INTO cargo_shipments VALUES (6894576, 648848, 1, 13661, 83314, 700.7088922671653);
INSERT INTO cargo_shipments VALUES (1016198, 647082, 4, 49491, 88944, 962.911388538372);
INSERT INTO cargo_shipments VALUES (8160950, 374226, 1, 1524, 51067, 68.90425123438915);
INSERT INTO cargo_shipments VALUES (4939959, 986540, 2, 3453, 55608, 461.53799355061795);
INSERT INTO cargo_shipments VALUES (8618856, 845815, 4, 16281, 31694, 212.10429644139418);
INSERT INTO cargo_shipments VALUES (2553422, 191136, 3, 49674, 49609, 273.6553386294821);
INSERT INTO cargo_shipments VALUES (6742018, 420661, 4, 14494, 66620, 706.8809568103453);
INSERT INTO cargo_shipments VALUES (5208412, 998327, 2, 29259, 73217, 226.06985465929807);
INSERT INTO cargo_shipments VALUES (7101831, 978373, 2, 34252, 66672, 416.04944875179064);
INSERT INTO cargo_shipments VALUES (3330692, 305108, 4, 10684, 65481, 892.8165712516981);
INSERT INTO cargo_shipments VALUES (4961292, 176495, 4, 11664, 73640, 561.9503474904573);
INSERT INTO cargo_shipments VALUES (2673270, 952278, 2, 49129, 64986, 552.9612946104762);
INSERT INTO cargo_shipments VALUES (2957832, 574095, 2, 24367, 78382, 359.0510414320537);
INSERT INTO cargo_shipments VALUES (8624217, 636347, 2, 21930, 13533, 658.9323054365805);
INSERT INTO cargo_shipments VALUES (1490286, 675376, 4, 9736, 78382, 68.79811757415011);
INSERT INTO cargo_shipments VALUES (2092926, 549043, 2, 46899, 73315, 974.2110437102278);
INSERT INTO cargo_shipments VALUES (2634517, 212978, 4, 33582, 85299, 6.4105538608429935);
INSERT INTO cargo_shipments VALUES (9041888, 615277, 3, 47167, 67067, 282.57043764560075);
INSERT INTO cargo_shipments VALUES (3766084, 452680, 3, 45664, 13552, 528.931809803566);
INSERT INTO cargo_shipments VALUES (9717634, 574095, 4, 46663, 93319, 986.5903377055649);
INSERT INTO cargo_shipments VALUES (6254860, 970601, 2, 3016, 69183, 110.51269163080602);
INSERT INTO cargo_shipments VALUES (2440713, 268775, 4, 24955, 61780, 189.46006435048614);
INSERT INTO cargo_shipments VALUES (4004018, 240695, 4, 10076, 42117, 386.7957593007094);
INSERT INTO cargo_shipments VALUES (5723834, 267291, 2, 21669, 42830, 347.97627707757226);
INSERT INTO cargo_shipments VALUES (5555552, 374226, 3, 43845, 67067, 339.15580379717323);
INSERT INTO cargo_shipments VALUES (5186796, 290811, 2, 2505, 93903, 196.3433463758988);
INSERT INTO cargo_shipments VALUES (1842832, 633887, 4, 42180, 73315, 47.55861085574775);
INSERT INTO cargo_shipments VALUES (1205125, 574737, 3, 19720, 48241, 282.1819423251216);
INSERT INTO cargo_shipments VALUES (2707153, 200324, 3, 5823, 96914, 465.06184517487327);
INSERT INTO cargo_shipments VALUES (1766454, 240695, 3, 44570, 35881, 864.5261521661729);
INSERT INTO cargo_shipments VALUES (2723687, 531537, 2, 18072, 66975, 733.171523380342);
INSERT INTO cargo_shipments VALUES (5553225, 410976, 2, 6301, 61780, 490.92860278502195);
INSERT INTO cargo_shipments VALUES (9626721, 614175, 3, 25724, 38903, 43.83560475121584);
INSERT INTO cargo_shipments VALUES (7989370, 214523, 3, 36924, 87094, 230.46177037155445);
INSERT INTO cargo_shipments VALUES (7592062, 636347, 2, 46215, 43047, 254.20073050542547);
INSERT INTO cargo_shipments VALUES (4154829, 855102, 2, 17706, 25806, 992.3393856992245);
INSERT INTO cargo_shipments VALUES (3776125, 750780, 4, 15583, 71907, 491.3046376997032);
INSERT INTO cargo_shipments VALUES (8687472, 755059, 4, 46078, 12096, 154.3065235710729);
INSERT INTO cargo_shipments VALUES (1838319, 837435, 4, 28742, 90584, 548.2702103695749);
INSERT INTO cargo_shipments VALUES (3870857, 881607, 1, 45405, 67867, 784.4534418289608);
INSERT INTO cargo_shipments VALUES (5951511, 614681, 3, 5098, 65481, 175.429595629657);
INSERT INTO cargo_shipments VALUES (8360019, 127089, 2, 31028, 22999, 800.0554672476595);
INSERT INTO cargo_shipments VALUES (1110772, 286148, 3, 28524, 32608, 765.2055158312214);
INSERT INTO cargo_shipments VALUES (2624181, 611911, 2, 40404, 55037, 469.24885587849263);
INSERT INTO cargo_shipments VALUES (8686212, 624235, 1, 20938, 64620, 586.2705185295515);
INSERT INTO cargo_shipments VALUES (3854671, 212978, 3, 27324, 50739, 203.81395174348648);
INSERT INTO cargo_shipments VALUES (9161042, 162259, 4, 12800, 63996, 383.3139302895294);
INSERT INTO cargo_shipments VALUES (5029166, 743993, 4, 14443, 40203, 860.2631331125391);
INSERT INTO cargo_shipments VALUES (3241604, 614175, 2, 2455, 77171, 937.6173939288345);
INSERT INTO cargo_shipments VALUES (8777975, 556487, 3, 13061, 95509, 194.0313545391562);
INSERT INTO cargo_shipments VALUES (6852136, 176495, 1, 6829, 47786, 207.03897234485734);
INSERT INTO cargo_shipments VALUES (5336948, 995244, 4, 33434, 28008, 242.02938899339932);
INSERT INTO cargo_shipments VALUES (2678675, 389460, 2, 8801, 65481, 698.505667214336);
INSERT INTO cargo_shipments VALUES (5656209, 655713, 2, 6971, 11178, 201.21883220989167);
INSERT INTO cargo_shipments VALUES (9990040, 212978, 1, 4088, 95504, 704.0197956776827);
INSERT INTO cargo_shipments VALUES (9802860, 200324, 3, 25118, 35963, 986.0457692756191);
INSERT INTO cargo_shipments VALUES (5651511, 614681, 3, 42459, 58358, 651.2522151556633);
INSERT INTO cargo_shipments VALUES (9332288, 271117, 3, 38930, 66672, 802.9620640565255);
INSERT INTO cargo_shipments VALUES (5661742, 896822, 1, 34806, 31584, 622.5661645355275);
INSERT INTO cargo_shipments VALUES (7968150, 438748, 4, 48004, 27543, 203.1655671376027);
INSERT INTO cargo_shipments VALUES (8040740, 634792, 2, 48017, 76275, 536.2391169717683);
INSERT INTO cargo_shipments VALUES (2855793, 459081, 3, 7107, 55037, 501.4504482337579);
INSERT INTO cargo_shipments VALUES (8936448, 646600, 4, 47778, 66374, 277.236427110645);
INSERT INTO cargo_shipments VALUES (8703649, 792511, 3, 21107, 40203, 879.47225177059);
INSERT INTO cargo_shipments VALUES (8170257, 859272, 3, 45801, 27794, 38.31544557940647);
INSERT INTO cargo_shipments VALUES (4079205, 601323, 2, 40221, 47786, 544.5373022331241);
INSERT INTO cargo_shipments VALUES (6802899, 117052, 1, 4721, 64620, 847.4044977971723);
INSERT INTO cargo_shipments VALUES (8825591, 380476, 3, 37553, 51067, 159.69263701745805);
INSERT INTO cargo_shipments VALUES (4385980, 792511, 3, 34216, 35306, 184.5636740411034);
INSERT INTO cargo_shipments VALUES (3609954, 988636, 3, 42891, 66620, 209.4343750680684);
INSERT INTO cargo_shipments VALUES (3786721, 776366, 2, 3650, 32608, 777.2796363773933);
INSERT INTO cargo_shipments VALUES (1216836, 703920, 3, 23104, 18630, 907.005559402551);
INSERT INTO cargo_shipments VALUES (5298982, 801924, 1, 45420, 88059, 649.2195472832443);
INSERT INTO cargo_shipments VALUES (5976887, 735633, 3, 35195, 51197, 919.3429604656409);
INSERT INTO cargo_shipments VALUES (9923393, 948750, 2, 18105, 13552, 265.70818850590473);
INSERT INTO cargo_shipments VALUES (2303480, 460185, 3, 48786, 21481, 966.5682067185378);
INSERT INTO cargo_shipments VALUES (4937100, 271976, 1, 44105, 95504, 351.3925794677404);
INSERT INTO cargo_shipments VALUES (1849973, 574095, 1, 28706, 93319, 537.2180550685484);
INSERT INTO cargo_shipments VALUES (8398383, 389460, 1, 1376, 38582, 911.0806970482538);
INSERT INTO cargo_shipments VALUES (6091484, 724842, 2, 11993, 88944, 638.6739951786934);
INSERT INTO cargo_shipments VALUES (9806886, 127089, 3, 20890, 50739, 824.9797306297643);
INSERT INTO cargo_shipments VALUES (7690061, 675376, 3, 5626, 69183, 324.8305053093501);
INSERT INTO cargo_shipments VALUES (9033771, 960137, 4, 39589, 42117, 972.4173177746546);
INSERT INTO cargo_shipments VALUES (4838018, 205227, 1, 38903, 64620, 504.536955999189);
INSERT INTO cargo_shipments VALUES (5462049, 947553, 3, 33817, 11136, 401.23572282695164);
INSERT INTO cargo_shipments VALUES (8100452, 507134, 2, 18030, 83314, 366.7903605796352);
INSERT INTO cargo_shipments VALUES (9502890, 970881, 4, 38895, 31584, 513.422061975565);
INSERT INTO cargo_shipments VALUES (3483812, 549043, 3, 40823, 74533, 457.83633198195486);
INSERT INTO cargo_shipments VALUES (6164303, 815951, 2, 47249, 87094, 354.4864300980912);
INSERT INTO cargo_shipments VALUES (8293833, 932171, 1, 44103, 11754, 92.26060435358153);
INSERT INTO cargo_shipments VALUES (4693383, 750780, 4, 5610, 88271, 774.9542196886754);
INSERT INTO cargo_shipments VALUES (1346508, 120546, 1, 15928, 63674, 949.9875223709704);
INSERT INTO cargo_shipments VALUES (2326976, 380476, 1, 1718, 81855, 687.505578623991);
INSERT INTO cargo_shipments VALUES (5493970, 633887, 2, 42611, 95504, 996.8334203327311);
INSERT INTO cargo_shipments VALUES (1458676, 212978, 1, 7173, 27794, 172.72460501286324);
INSERT INTO cargo_shipments VALUES (1704751, 533295, 4, 44573, 13533, 926.8137065253057);
INSERT INTO cargo_shipments VALUES (6829777, 125992, 2, 40079, 34668, 667.215777841762);
INSERT INTO cargo_shipments VALUES (5735469, 648848, 4, 7203, 66975, 471.8785877829686);
INSERT INTO cargo_shipments VALUES (8177561, 761485, 1, 39812, 18819, 453.4636663780045);
INSERT INTO cargo_shipments VALUES (6373244, 643092, 3, 14927, 70479, 888.5484412880188);
INSERT INTO cargo_shipments VALUES (3451075, 660589, 3, 40356, 67410, 509.3365115590698);
INSERT INTO cargo_shipments VALUES (8481173, 265050, 2, 29913, 37468, 965.5752617693598);
INSERT INTO cargo_shipments VALUES (3536820, 996048, 3, 43939, 37665, 36.061429523406076);
INSERT INTO cargo_shipments VALUES (7047931, 374226, 3, 21058, 58401, 227.23344620633245);
INSERT INTO cargo_shipments VALUES (4511947, 519222, 4, 30984, 19125, 669.9833243066176);
INSERT INTO cargo_shipments VALUES (1718511, 223703, 2, 8564, 22999, 982.255216369049);
INSERT INTO cargo_shipments VALUES (1899212, 615277, 4, 1442, 80764, 652.1000525373044);
INSERT INTO cargo_shipments VALUES (2618753, 615277, 4, 17279, 11178, 21.18369932363151);
INSERT INTO cargo_shipments VALUES (3066912, 574095, 2, 16002, 40856, 936.905723899398);
INSERT INTO cargo_shipments VALUES (4615385, 755059, 2, 47218, 30603, 469.7892678549156);
INSERT INTO cargo_shipments VALUES (5694111, 845815, 4, 35971, 14955, 656.473088163465);
INSERT INTO cargo_shipments VALUES (1415854, 855102, 3, 15666, 35881, 642.6732811365672);
INSERT INTO cargo_shipments VALUES (7348757, 724842, 2, 26253, 75270, 373.6866484390221);
INSERT INTO cargo_shipments VALUES (7441888, 265050, 3, 4311, 77171, 283.6144850546518);
INSERT INTO cargo_shipments VALUES (3574791, 170058, 1, 17225, 26625, 300.88461103022536);
INSERT INTO cargo_shipments VALUES (2156109, 397201, 3, 31760, 52750, 757.6959960993312);
INSERT INTO cargo_shipments VALUES (5549179, 675298, 4, 28642, 87478, 644.8569987700808);
INSERT INTO cargo_shipments VALUES (4263221, 519222, 2, 37393, 81142, 366.98111083263296);
INSERT INTO cargo_shipments VALUES (2983119, 420661, 3, 31008, 80764, 658.0776356670236);
INSERT INTO cargo_shipments VALUES (4222678, 380476, 3, 16176, 45027, 458.1590079307675);
INSERT INTO cargo_shipments VALUES (7881340, 967575, 4, 22266, 30603, 662.5292613282861);
INSERT INTO cargo_shipments VALUES (4582279, 631650, 1, 37164, 32861, 961.2516679561228);
INSERT INTO cargo_shipments VALUES (3809516, 606002, 4, 20386, 88059, 957.0098707117974);
INSERT INTO cargo_shipments VALUES (6562186, 572361, 4, 30543, 78092, 128.94170191236975);
INSERT INTO cargo_shipments VALUES (7330198, 636347, 2, 44289, 29828, 108.42326737235908);
INSERT INTO cargo_shipments VALUES (2947877, 634792, 2, 47769, 23317, 387.184515007585);
INSERT INTO cargo_shipments VALUES (5427838, 910188, 1, 36103, 35963, 368.2926091573071);
INSERT INTO cargo_shipments VALUES (4436729, 775508, 2, 3786, 23317, 308.96467147327786);
INSERT INTO cargo_shipments VALUES (2859499, 970881, 4, 37426, 46702, 383.226991158292);
INSERT INTO cargo_shipments VALUES (6524739, 245326, 4, 45923, 88059, 303.55186396519383);
INSERT INTO cargo_shipments VALUES (6378670, 252987, 4, 6152, 32518, 852.4053357271908);
INSERT INTO cargo_shipments VALUES (2710785, 910151, 1, 13782, 81142, 740.3691674562108);
INSERT INTO cargo_shipments VALUES (3431396, 271976, 1, 21559, 28008, 529.1863798836857);
INSERT INTO cargo_shipments VALUES (1026486, 715589, 1, 10071, 81696, 58.27333369362564);
INSERT INTO cargo_shipments VALUES (1898298, 533295, 4, 44638, 32608, 473.43501357566663);
INSERT INTO cargo_shipments VALUES (5006554, 914016, 2, 23569, 38903, 0.8234200076425724);
INSERT INTO cargo_shipments VALUES (4687609, 830748, 3, 10146, 88271, 339.1497792119923);
INSERT INTO cargo_shipments VALUES (9035217, 214523, 4, 33026, 55608, 654.8839222726801);
INSERT INTO cargo_shipments VALUES (4269336, 463436, 3, 7530, 13552, 537.5490375081954);
INSERT INTO cargo_shipments VALUES (1064131, 905715, 1, 26862, 78897, 995.9549384063891);
INSERT INTO cargo_shipments VALUES (5659539, 572301, 4, 38797, 23317, 718.2164908671398);
INSERT INTO cargo_shipments VALUES (6698249, 438748, 1, 44087, 40203, 316.7193945825513);
INSERT INTO cargo_shipments VALUES (5064325, 415908, 3, 4369, 37665, 10.954105273963343);
INSERT INTO cargo_shipments VALUES (6027545, 252987, 3, 44072, 81142, 475.1057959295719);
INSERT INTO cargo_shipments VALUES (8483858, 830748, 3, 10585, 93319, 95.42472242269062);
INSERT INTO cargo_shipments VALUES (3501068, 707099, 2, 48459, 49609, 463.6451142438902);
INSERT INTO cargo_shipments VALUES (9676610, 563327, 4, 33785, 74354, 300.30459790105533);
INSERT INTO cargo_shipments VALUES (7674406, 268775, 4, 20464, 32608, 128.3614940376484);
INSERT INTO cargo_shipments VALUES (7677681, 875193, 3, 27361, 40203, 787.6310316321169);
INSERT INTO cargo_shipments VALUES (4598447, 105533, 2, 1061, 51197, 950.8200054591246);
INSERT INTO cargo_shipments VALUES (2518083, 474492, 3, 28126, 81696, 236.3582768574497);
INSERT INTO cargo_shipments VALUES (6526186, 990233, 4, 37770, 45027, 11.036451578594342);
INSERT INTO cargo_shipments VALUES (4661787, 460185, 2, 16237, 47786, 924.2451478501222);
INSERT INTO cargo_shipments VALUES (6811125, 252642, 3, 21107, 30603, 459.71275006323043);
INSERT INTO cargo_shipments VALUES (5910121, 511881, 2, 9432, 36421, 713.9462133859988);
INSERT INTO cargo_shipments VALUES (9765662, 707099, 1, 13695, 81696, 191.8980801994029);
INSERT INTO cargo_shipments VALUES (1285576, 224194, 2, 13997, 37665, 848.7153296132956);
INSERT INTO cargo_shipments VALUES (6158405, 333621, 2, 38460, 66374, 410.7205440435224);
INSERT INTO cargo_shipments VALUES (4854254, 415908, 1, 28533, 74735, 849.0203299993909);
INSERT INTO cargo_shipments VALUES (6363760, 373731, 2, 47563, 38582, 136.15535687412495);
INSERT INTO cargo_shipments VALUES (5574027, 558558, 4, 5729, 42830, 80.48114947710283);
INSERT INTO cargo_shipments VALUES (4527635, 380476, 1, 11957, 69028, 410.30157265574974);
INSERT INTO cargo_shipments VALUES (6781358, 252987, 3, 16241, 83314, 266.77024657371953);
INSERT INTO cargo_shipments VALUES (2846480, 296521, 3, 16832, 11136, 853.3145676166689);
INSERT INTO cargo_shipments VALUES (7665124, 948750, 2, 25841, 12096, 820.7450928563859);
INSERT INTO cargo_shipments VALUES (3140982, 446354, 1, 43808, 35963, 307.4691346701839);
INSERT INTO cargo_shipments VALUES (6369007, 505917, 4, 13979, 37665, 992.7332546037655);
INSERT INTO cargo_shipments VALUES (9356064, 556487, 4, 38599, 47786, 657.9877488804945);
INSERT INTO cargo_shipments VALUES (4184866, 286320, 4, 28819, 14388, 498.7203737748558);
INSERT INTO cargo_shipments VALUES (6814646, 793872, 2, 15757, 27794, 212.99359281505448);
INSERT INTO cargo_shipments VALUES (9956367, 249856, 4, 37236, 66975, 914.8467608886631);
INSERT INTO cargo_shipments VALUES (7616306, 601323, 4, 12070, 16362, 493.53705783017085);
INSERT INTO cargo_shipments VALUES (3190255, 646600, 2, 38255, 32518, 418.98606691771533);
INSERT INTO cargo_shipments VALUES (4187251, 725323, 3, 35391, 29828, 526.7489596801747);
INSERT INTO cargo_shipments VALUES (2533035, 986540, 4, 21279, 18819, 112.81652101021011);
INSERT INTO cargo_shipments VALUES (2542691, 801924, 4, 41586, 77171, 886.6643062257394);
INSERT INTO cargo_shipments VALUES (5440068, 413138, 4, 36328, 73357, 9.368334391446243);
INSERT INTO cargo_shipments VALUES (8408687, 725323, 1, 10680, 74735, 406.34885476210616);
INSERT INTO cargo_shipments VALUES (6349826, 271976, 4, 25005, 84900, 17.045269725949996);
INSERT INTO cargo_shipments VALUES (7756150, 563327, 4, 43258, 14955, 670.1669090288041);
INSERT INTO cargo_shipments VALUES (2773481, 505917, 4, 4696, 84900, 133.59202577814168);
INSERT INTO cargo_shipments VALUES (3472338, 352768, 1, 27853, 37865, 857.2909316825509);
INSERT INTO cargo_shipments VALUES (7568108, 995244, 2, 48750, 73217, 907.2184435044875);
INSERT INTO cargo_shipments VALUES (3661720, 558558, 2, 48595, 63674, 655.0588145035966);
INSERT INTO cargo_shipments VALUES (3036976, 563327, 1, 26441, 69183, 327.09619876637396);
INSERT INTO cargo_shipments VALUES (7454615, 271117, 4, 40222, 95020, 497.67289638103904);
INSERT INTO cargo_shipments VALUES (1865858, 607042, 2, 46109, 47786, 992.4419332684652);
INSERT INTO cargo_shipments VALUES (4865374, 914016, 2, 37579, 47786, 69.64294773459979);
INSERT INTO cargo_shipments VALUES (4237614, 221189, 3, 2755, 85299, 698.0686868134733);
INSERT INTO cargo_shipments VALUES (3145308, 332039, 1, 41893, 51197, 363.8570829123048);
INSERT INTO cargo_shipments VALUES (9238895, 251048, 3, 18999, 66620, 518.9120350173682);
INSERT INTO cargo_shipments VALUES (4626456, 995244, 3, 17324, 83314, 204.74212615647946);
INSERT INTO cargo_shipments VALUES (2339551, 556487, 4, 47222, 54150, 753.9671031696081);
INSERT INTO cargo_shipments VALUES (2643607, 714581, 2, 29598, 74354, 209.7289984427413);
INSERT INTO cargo_shipments VALUES (5806511, 237199, 2, 42681, 37665, 955.9370002247235);
INSERT INTO cargo_shipments VALUES (3247853, 755059, 4, 8125, 31694, 242.8728706041987);
INSERT INTO cargo_shipments VALUES (1306187, 787243, 1, 5237, 85047, 61.93443691272626);
INSERT INTO cargo_shipments VALUES (7665038, 290811, 4, 40261, 53546, 657.9672078366831);
INSERT INTO cargo_shipments VALUES (3242350, 952278, 4, 7814, 75270, 533.8125220911395);
INSERT INTO cargo_shipments VALUES (4669918, 947553, 4, 47441, 74533, 325.3018768582079);
INSERT INTO cargo_shipments VALUES (4266762, 128482, 2, 46297, 80764, 352.5168693702746);
INSERT INTO cargo_shipments VALUES (3544845, 574095, 4, 45466, 46879, 934.3365606571476);
INSERT INTO cargo_shipments VALUES (1997149, 170058, 2, 14814, 83314, 949.3430756168823);
INSERT INTO cargo_shipments VALUES (3697565, 167992, 2, 42061, 73315, 430.4985656680068);
INSERT INTO cargo_shipments VALUES (8234384, 463436, 2, 25142, 21481, 668.1572333034168);
INSERT INTO cargo_shipments VALUES (2210998, 252642, 1, 35982, 21786, 401.0922276020422);
INSERT INTO cargo_shipments VALUES (9889808, 643092, 1, 40425, 81696, 860.609600915061);
INSERT INTO cargo_shipments VALUES (8703491, 643092, 4, 47356, 55037, 116.34079827341715);
INSERT INTO cargo_shipments VALUES (7003880, 818559, 2, 7522, 96914, 269.9623218733467);
INSERT INTO cargo_shipments VALUES (2438846, 265050, 4, 37020, 81696, 861.0559589094955);
INSERT INTO cargo_shipments VALUES (4466585, 996823, 4, 14932, 83314, 58.333238901934315);
INSERT INTO cargo_shipments VALUES (5814130, 995244, 4, 16154, 48241, 329.212031092212);
INSERT INTO cargo_shipments VALUES (4351910, 606002, 4, 30670, 64620, 331.22110453406253);
INSERT INTO cargo_shipments VALUES (1569226, 767347, 4, 24498, 63674, 815.3061621404192);
INSERT INTO cargo_shipments VALUES (8854088, 298068, 2, 28649, 11056, 282.58566111029637);
INSERT INTO cargo_shipments VALUES (7658736, 863835, 2, 4360, 69973, 351.25070652707836);
INSERT INTO cargo_shipments VALUES (3319961, 226404, 2, 48507, 61780, 318.01948799099966);
INSERT INTO cargo_shipments VALUES (2592646, 677816, 2, 10248, 67410, 452.92979082305806);
INSERT INTO cargo_shipments VALUES (3200821, 995244, 2, 8432, 20438, 405.71257852926635);
INSERT INTO cargo_shipments VALUES (5259308, 855102, 2, 48303, 58358, 515.876478526744);
INSERT INTO cargo_shipments VALUES (6309786, 796715, 3, 6089, 63186, 136.72798793985453);
INSERT INTO cargo_shipments VALUES (8237258, 750780, 2, 3541, 54150, 32.699292405988786);
INSERT INTO cargo_shipments VALUES (3611962, 639436, 1, 34703, 31016, 157.60767618443637);
INSERT INTO cargo_shipments VALUES (3792237, 881607, 4, 35079, 73357, 830.9375035058489);
INSERT INTO cargo_shipments VALUES (1984250, 374226, 1, 45021, 48241, 955.4661145542183);
INSERT INTO cargo_shipments VALUES (1926132, 468598, 2, 20605, 66374, 463.30919099797774);
INSERT INTO cargo_shipments VALUES (1803803, 114429, 3, 31856, 53546, 272.2537582122838);
INSERT INTO cargo_shipments VALUES (8970984, 511881, 1, 8431, 75270, 491.02398721839313);
INSERT INTO cargo_shipments VALUES (3166771, 286148, 1, 28657, 37865, 820.6904161592807);
INSERT INTO cargo_shipments VALUES (5317409, 715589, 3, 38038, 81696, 570.9223072335992);
INSERT INTO cargo_shipments VALUES (7983370, 777504, 2, 4572, 41156, 655.6754449564509);
INSERT INTO cargo_shipments VALUES (4508454, 724842, 4, 31648, 66374, 22.588490019380014);
INSERT INTO cargo_shipments VALUES (7738283, 776366, 1, 5276, 85299, 709.8896013873415);
INSERT INTO cargo_shipments VALUES (7669284, 499982, 4, 44845, 58401, 67.34337158490133);
INSERT INTO cargo_shipments VALUES (6124673, 755059, 2, 21377, 26822, 767.4975806746004);
INSERT INTO cargo_shipments VALUES (9141744, 176495, 4, 40385, 67067, 389.28716508037763);
INSERT INTO cargo_shipments VALUES (5408571, 374226, 1, 16666, 93319, 117.31219967458573);
INSERT INTO cargo_shipments VALUES (2275518, 675298, 2, 42421, 55608, 185.82990109653807);
INSERT INTO cargo_shipments VALUES (1842213, 422404, 4, 14832, 81142, 824.9852560995167);
INSERT INTO cargo_shipments VALUES (4996483, 632283, 1, 31901, 52750, 920.2119632980525);
INSERT INTO cargo_shipments VALUES (7344525, 200324, 4, 12676, 20438, 491.66577298747825);
INSERT INTO cargo_shipments VALUES (6575241, 983159, 4, 49784, 55608, 292.11195063011564);
INSERT INTO cargo_shipments VALUES (1607150, 416286, 3, 21223, 34984, 219.10212500403836);
INSERT INTO cargo_shipments VALUES (1020048, 413138, 2, 31785, 43047, 547.2001255911975);
INSERT INTO cargo_shipments VALUES (4046201, 782915, 3, 32625, 20438, 431.53247232467885);
INSERT INTO cargo_shipments VALUES (6539724, 358509, 1, 9976, 34668, 258.96615648931265);
INSERT INTO cargo_shipments VALUES (3732595, 555899, 2, 35342, 87478, 109.94459785592814);
INSERT INTO cargo_shipments VALUES (8192710, 167992, 1, 21187, 53546, 67.67495797078304);
INSERT INTO cargo_shipments VALUES (5036095, 445027, 1, 49006, 53546, 185.62014066563847);
INSERT INTO cargo_shipments VALUES (5237431, 997802, 4, 23856, 13552, 807.6380519651027);
INSERT INTO cargo_shipments VALUES (7535852, 381311, 2, 12855, 85299, 289.25074748533086);
INSERT INTO cargo_shipments VALUES (4190404, 555106, 2, 10570, 49609, 177.8582094905574);
INSERT INTO cargo_shipments VALUES (3740753, 735633, 4, 20263, 85299, 748.611899747742);
INSERT INTO cargo_shipments VALUES (7257528, 520335, 3, 17951, 64620, 213.19839135624642);
INSERT INTO cargo_shipments VALUES (4069578, 735633, 4, 12189, 48241, 868.2050743072041);
INSERT INTO cargo_shipments VALUES (3149709, 817972, 4, 23147, 63996, 176.9890893899615);
INSERT INTO cargo_shipments VALUES (4014145, 212978, 1, 11597, 48435, 551.97610786634);
INSERT INTO cargo_shipments VALUES (3826004, 755059, 4, 25054, 64986, 667.9508276805191);
INSERT INTO cargo_shipments VALUES (8633278, 237199, 1, 8284, 48435, 156.5812715185375);
INSERT INTO cargo_shipments VALUES (9329375, 675298, 3, 16804, 11056, 657.0694494896925);
INSERT INTO cargo_shipments VALUES (2650009, 572228, 4, 19581, 87478, 524.2994479548667);
INSERT INTO cargo_shipments VALUES (3915183, 912883, 2, 43023, 75270, 680.0258661255858);
INSERT INTO cargo_shipments VALUES (7779188, 252642, 3, 36931, 69183, 233.53756333719133);
INSERT INTO cargo_shipments VALUES (3701170, 221189, 1, 34187, 32518, 955.4758624620176);
INSERT INTO cargo_shipments VALUES (3420938, 912153, 2, 9453, 88059, 168.6569911255499);
INSERT INTO cargo_shipments VALUES (9602001, 290811, 2, 1819, 85047, 359.7659841318561);
INSERT INTO cargo_shipments VALUES (7876538, 910188, 4, 20766, 16362, 33.85857832458794);
INSERT INTO cargo_shipments VALUES (7929398, 845815, 3, 10115, 16790, 743.7864714819843);
INSERT INTO cargo_shipments VALUES (9396650, 614681, 1, 35918, 67867, 993.3882891155804);
INSERT INTO cargo_shipments VALUES (2351017, 910188, 1, 19234, 26822, 81.06271347943006);
INSERT INTO cargo_shipments VALUES (6200276, 595048, 1, 18548, 35306, 52.38083937444971);
INSERT INTO cargo_shipments VALUES (5076699, 952278, 3, 21065, 68759, 694.5432853881996);
INSERT INTO cargo_shipments VALUES (6283610, 304639, 3, 13767, 88271, 820.6640638384916);
INSERT INTO cargo_shipments VALUES (1556149, 572301, 3, 25528, 65481, 371.55194728152117);
INSERT INTO cargo_shipments VALUES (3314935, 782915, 3, 4293, 17062, 229.9887345498709);
INSERT INTO cargo_shipments VALUES (4776407, 634792, 2, 11029, 17145, 474.25562934135945);
INSERT INTO cargo_shipments VALUES (1244916, 572301, 4, 13533, 88944, 806.743755111401);
INSERT INTO cargo_shipments VALUES (2968736, 277004, 4, 28175, 69183, 992.4199924357187);
INSERT INTO cargo_shipments VALUES (7679767, 447164, 1, 7739, 63674, 733.7029642372559);
INSERT INTO cargo_shipments VALUES (8400427, 240695, 4, 6812, 37468, 199.10910035110984);
INSERT INTO cargo_shipments VALUES (5403171, 677816, 2, 6526, 30603, 680.0339608419607);
INSERT INTO cargo_shipments VALUES (9350254, 572361, 4, 47220, 31016, 873.8993928018275);
INSERT INTO cargo_shipments VALUES (7920878, 952278, 4, 32680, 78897, 363.8545738368417);
INSERT INTO cargo_shipments VALUES (5247406, 531537, 1, 4104, 34263, 50.663778465622535);
INSERT INTO cargo_shipments VALUES (6839298, 511881, 3, 25125, 56046, 501.8413617468918);
INSERT INTO cargo_shipments VALUES (4591748, 732193, 2, 34322, 38582, 608.7521394372842);
INSERT INTO cargo_shipments VALUES (3389921, 990233, 1, 48850, 31584, 497.64150662363573);
INSERT INTO cargo_shipments VALUES (1997042, 422404, 2, 4761, 50739, 995.2360545186514);
INSERT INTO cargo_shipments VALUES (1270157, 967575, 1, 32877, 27543, 527.4995924038061);
INSERT INTO cargo_shipments VALUES (6360686, 692445, 2, 25561, 73217, 16.2555186132326);
INSERT INTO cargo_shipments VALUES (3052481, 998327, 3, 26166, 87094, 32.64034714681263);
INSERT INTO cargo_shipments VALUES (9562418, 296521, 1, 47329, 55037, 391.03616022176845);
INSERT INTO cargo_shipments VALUES (5459347, 544464, 1, 8587, 77171, 519.093736953087);
INSERT INTO cargo_shipments VALUES (3550685, 206705, 2, 2581, 47786, 581.2708636054974);
INSERT INTO cargo_shipments VALUES (1086766, 859272, 3, 27138, 78092, 818.1277358330944);
INSERT INTO cargo_shipments VALUES (4419622, 240695, 1, 19644, 40203, 819.1799062733227);
INSERT INTO cargo_shipments VALUES (9626239, 226404, 4, 12925, 73315, 174.86407961489482);
INSERT INTO cargo_shipments VALUES (2929031, 675376, 1, 33752, 70479, 988.8347637305658);
INSERT INTO cargo_shipments VALUES (1810053, 678322, 4, 12450, 85128, 857.2158354021763);
INSERT INTO cargo_shipments VALUES (1175508, 649000, 3, 34741, 90584, 726.5534261520711);
INSERT INTO cargo_shipments VALUES (6059961, 251048, 1, 22226, 93903, 195.1613083737148);
INSERT INTO cargo_shipments VALUES (1352478, 438748, 1, 11133, 77171, 746.5362880565253);
INSERT INTO cargo_shipments VALUES (7950899, 555106, 2, 1023, 17145, 29.68722492384357);
INSERT INTO cargo_shipments VALUES (6702092, 761485, 1, 5498, 85047, 223.8615904013669);
INSERT INTO cargo_shipments VALUES (1620001, 117052, 1, 10450, 74735, 832.1436345182353);
INSERT INTO cargo_shipments VALUES (1316556, 468598, 3, 36654, 73217, 741.8777429463775);
INSERT INTO cargo_shipments VALUES (7006579, 280979, 4, 22262, 29828, 261.4917504848523);
INSERT INTO cargo_shipments VALUES (7693864, 775508, 4, 21489, 97714, 904.4003430518034);
INSERT INTO cargo_shipments VALUES (3661829, 469804, 3, 22371, 69183, 706.8754391063673);
INSERT INTO cargo_shipments VALUES (2606476, 745173, 3, 36946, 19125, 856.3345722843877);
INSERT INTO cargo_shipments VALUES (6365868, 240695, 1, 20452, 73315, 284.5486951108441);
INSERT INTO cargo_shipments VALUES (2094104, 142736, 1, 14631, 18630, 515.7449457492312);
INSERT INTO cargo_shipments VALUES (1884461, 251268, 1, 37708, 40203, 620.4516077278728);
INSERT INTO cargo_shipments VALUES (2864992, 787243, 4, 23251, 84370, 269.71149522360247);
INSERT INTO cargo_shipments VALUES (1499142, 978373, 2, 31463, 50739, 846.683387930226);
INSERT INTO cargo_shipments VALUES (8068948, 574095, 4, 27938, 63996, 522.0092362941751);
INSERT INTO cargo_shipments VALUES (3366006, 724842, 1, 46430, 52750, 831.2464406641376);
INSERT INTO cargo_shipments VALUES (3689419, 381311, 4, 32863, 28008, 7.306417691740563);
INSERT INTO cargo_shipments VALUES (8899709, 162259, 1, 34723, 18819, 347.00295650593296);
INSERT INTO cargo_shipments VALUES (8528404, 447164, 4, 49637, 97714, 423.7190810407125);
INSERT INTO cargo_shipments VALUES (3115578, 970601, 2, 1602, 88944, 950.2941543749638);
INSERT INTO cargo_shipments VALUES (4526021, 223703, 4, 42944, 99845, 920.7790882243771);
INSERT INTO cargo_shipments VALUES (6013888, 513506, 3, 9621, 41503, 633.9279492376048);
INSERT INTO cargo_shipments VALUES (1370951, 733236, 1, 21047, 17062, 81.74849822866992);
INSERT INTO cargo_shipments VALUES (8863192, 572361, 3, 5336, 32861, 29.1510454408902);
INSERT INTO cargo_shipments VALUES (8733173, 712175, 4, 31263, 22999, 737.9810884119865);
INSERT INTO cargo_shipments VALUES (7948584, 251048, 1, 3977, 27543, 898.1843963780753);
INSERT INTO cargo_shipments VALUES (5474509, 224194, 2, 45849, 81142, 107.98886990281076);
INSERT INTO cargo_shipments VALUES (8614735, 198178, 3, 17046, 38903, 994.869510601698);
INSERT INTO cargo_shipments VALUES (3161500, 271117, 3, 34278, 11754, 364.35014859552496);
INSERT INTO cargo_shipments VALUES (3694545, 675376, 4, 43311, 25571, 260.4064613628797);
INSERT INTO cargo_shipments VALUES (4221571, 817972, 2, 44073, 66672, 725.4685323013771);
INSERT INTO cargo_shipments VALUES (6484698, 615277, 3, 32377, 93903, 524.0665442089648);
INSERT INTO cargo_shipments VALUES (5575129, 626357, 3, 40788, 45027, 260.19971845457826);
INSERT INTO cargo_shipments VALUES (7526479, 632283, 1, 47096, 63674, 669.3329073543202);
INSERT INTO cargo_shipments VALUES (7126953, 115640, 2, 30123, 29828, 617.5486906340244);
INSERT INTO cargo_shipments VALUES (4600868, 834600, 4, 26265, 76275, 366.0905455173818);
INSERT INTO cargo_shipments VALUES (7955346, 910188, 4, 45903, 87094, 572.7143476044629);
INSERT INTO cargo_shipments VALUES (3309101, 155883, 4, 42688, 95020, 233.21964741813184);
INSERT INTO cargo_shipments VALUES (9091209, 606002, 1, 24038, 93903, 503.19531175900244);
INSERT INTO cargo_shipments VALUES (1127770, 678301, 1, 3828, 25806, 500.5679551812635);
INSERT INTO cargo_shipments VALUES (5264644, 413138, 2, 41968, 17062, 96.39490484669378);
INSERT INTO cargo_shipments VALUES (9713686, 997802, 3, 27403, 61780, 155.52886794574826);
INSERT INTO cargo_shipments VALUES (9291117, 422404, 3, 28427, 56046, 308.2117257102166);
INSERT INTO cargo_shipments VALUES (6257873, 647082, 3, 24940, 30534, 26.488545297054912);
INSERT INTO cargo_shipments VALUES (8118534, 558558, 1, 23732, 19125, 563.4659225350715);
INSERT INTO cargo_shipments VALUES (8392241, 633887, 3, 28278, 90584, 669.9485031759924);
INSERT INTO cargo_shipments VALUES (4872535, 513506, 4, 18498, 30603, 503.8021404905302);
INSERT INTO cargo_shipments VALUES (2231678, 445027, 3, 1618, 46879, 612.7875297736084);
INSERT INTO cargo_shipments VALUES (1970078, 601323, 3, 26550, 22999, 441.15649451358775);
INSERT INTO cargo_shipments VALUES (3707328, 636347, 1, 16217, 52750, 159.26247114277703);
INSERT INTO cargo_shipments VALUES (6300206, 750780, 4, 2248, 73357, 409.145690105597);
INSERT INTO cargo_shipments VALUES (2824548, 469804, 1, 18794, 40856, 96.49614285043762);
INSERT INTO cargo_shipments VALUES (9722109, 859274, 4, 41130, 58401, 193.49122564705024);
INSERT INTO cargo_shipments VALUES (9004835, 303778, 2, 20401, 31016, 887.5276859042625);
INSERT INTO cargo_shipments VALUES (6318152, 214523, 2, 15922, 43313, 121.03905731476638);
INSERT INTO cargo_shipments VALUES (2071137, 692445, 3, 34655, 29828, 42.855859874135646);
INSERT INTO cargo_shipments VALUES (7411128, 900209, 4, 42517, 38903, 361.15834006799264);
INSERT INTO cargo_shipments VALUES (5198532, 558558, 1, 30855, 48435, 515.2911589974597);
INSERT INTO cargo_shipments VALUES (9216871, 777504, 1, 1128, 83314, 834.4826240089513);
INSERT INTO cargo_shipments VALUES (9479742, 896822, 1, 46419, 34984, 908.175065121267);
INSERT INTO cargo_shipments VALUES (2169692, 251268, 1, 12082, 93903, 1.3156798088089783);
INSERT INTO cargo_shipments VALUES (4859588, 873886, 2, 32255, 87094, 958.3012679917026);
INSERT INTO cargo_shipments VALUES (9222685, 154036, 2, 48238, 53546, 435.34693461253806);
INSERT INTO cargo_shipments VALUES (2160139, 188334, 3, 36967, 11136, 631.0677741623477);
INSERT INTO cargo_shipments VALUES (2690441, 724842, 4, 40910, 55608, 248.55148437921693);
INSERT INTO cargo_shipments VALUES (9306530, 459081, 1, 10682, 27794, 277.1170796413447);
INSERT INTO cargo_shipments VALUES (6900713, 208914, 3, 32786, 73640, 709.8352752659899);
INSERT INTO cargo_shipments VALUES (9707289, 750780, 3, 38332, 13533, 835.3227582543157);
INSERT INTO cargo_shipments VALUES (4998399, 703367, 3, 18238, 54150, 590.0311967772964);
INSERT INTO cargo_shipments VALUES (1262569, 544464, 1, 45604, 99845, 354.75031231667765);
INSERT INTO cargo_shipments VALUES (4853230, 511881, 1, 49473, 31584, 527.9209424924765);
INSERT INTO cargo_shipments VALUES (9600305, 990233, 3, 46999, 48435, 90.79563109833566);
INSERT INTO cargo_shipments VALUES (8710119, 203567, 1, 23862, 34668, 367.2073075915818);
INSERT INTO cargo_shipments VALUES (4509716, 601323, 1, 34955, 27543, 427.963153784886);
INSERT INTO cargo_shipments VALUES (7146876, 420661, 3, 48859, 53546, 696.9831700168544);
INSERT INTO cargo_shipments VALUES (2566916, 128482, 4, 39592, 78382, 976.5124151543903);
INSERT INTO cargo_shipments VALUES (9738602, 167992, 2, 23567, 53546, 683.6510424744977);
INSERT INTO cargo_shipments VALUES (5543527, 107463, 2, 19455, 75571, 9.065821971751209);
INSERT INTO cargo_shipments VALUES (5695041, 574737, 1, 23828, 30534, 493.51463185545094);
INSERT INTO cargo_shipments VALUES (3308406, 572361, 1, 43350, 73640, 114.56152020054422);
INSERT INTO cargo_shipments VALUES (6289399, 410976, 1, 31763, 58358, 649.4736967371608);
INSERT INTO cargo_shipments VALUES (7245724, 806286, 2, 26627, 85047, 568.2825742935305);
INSERT INTO cargo_shipments VALUES (1591082, 996823, 2, 2143, 70479, 971.4174763313432);
INSERT INTO cargo_shipments VALUES (9599983, 572361, 4, 39136, 38582, 12.838730382105034);
INSERT INTO cargo_shipments VALUES (8720752, 624235, 2, 21502, 83314, 354.3307229898748);
INSERT INTO cargo_shipments VALUES (8883082, 272236, 2, 18288, 68759, 501.89573857812366);
INSERT INTO cargo_shipments VALUES (3319070, 296521, 4, 27964, 56046, 347.95700373345886);
INSERT INTO cargo_shipments VALUES (3842160, 881607, 1, 47928, 85047, 594.5132350388191);
INSERT INTO cargo_shipments VALUES (7400085, 125992, 2, 15044, 66374, 293.64622851719366);
INSERT INTO cargo_shipments VALUES (8622418, 581397, 4, 18593, 16362, 608.3965344053895);
INSERT INTO cargo_shipments VALUES (5519198, 719236, 4, 20929, 63186, 372.4189655844562);
INSERT INTO cargo_shipments VALUES (3888915, 815951, 3, 43020, 95504, 355.0192075012111);
INSERT INTO cargo_shipments VALUES (5644046, 533295, 2, 19615, 16362, 789.4940917318344);
INSERT INTO cargo_shipments VALUES (9915835, 463436, 2, 23217, 26625, 928.1035638848764);
INSERT INTO cargo_shipments VALUES (8718252, 677816, 1, 5285, 38582, 762.0913538260397);
INSERT INTO cargo_shipments VALUES (4613644, 678322, 3, 49678, 32861, 512.4155738537307);
INSERT INTO cargo_shipments VALUES (7258159, 815951, 3, 10480, 81142, 397.97287539591395);
INSERT INTO cargo_shipments VALUES (5856658, 117052, 2, 27822, 38903, 654.0505555535183);
INSERT INTO cargo_shipments VALUES (7399251, 265050, 2, 14526, 37865, 825.7752718519918);
INSERT INTO cargo_shipments VALUES (7354190, 881607, 4, 46136, 31584, 125.33075181218867);
INSERT INTO cargo_shipments VALUES (7615216, 647082, 3, 33759, 11056, 138.2942693295055);
INSERT INTO cargo_shipments VALUES (7470736, 491415, 1, 18432, 21786, 646.2112904882484);
INSERT INTO cargo_shipments VALUES (1514837, 815951, 4, 3644, 99845, 80.68871780134145);
INSERT INTO cargo_shipments VALUES (8765762, 271976, 4, 3330, 31584, 990.0231250840986);
INSERT INTO cargo_shipments VALUES (2286149, 452680, 2, 2606, 84900, 454.9998169764724);
INSERT INTO cargo_shipments VALUES (9371988, 511695, 1, 5114, 63186, 433.98190114648185);
INSERT INTO cargo_shipments VALUES (6523226, 783453, 4, 48523, 31584, 476.1348892425147);
INSERT INTO cargo_shipments VALUES (6117827, 900209, 3, 29599, 89117, 589.6401687235574);
INSERT INTO cargo_shipments VALUES (4239949, 647082, 1, 42281, 31016, 862.4996101331113);
INSERT INTO cargo_shipments VALUES (2095784, 176495, 4, 3035, 38903, 544.8471882663694);
INSERT INTO cargo_shipments VALUES (8522111, 360250, 3, 7378, 73217, 608.7849835339912);
INSERT INTO cargo_shipments VALUES (2327334, 252642, 2, 46517, 13533, 452.95646850423833);
INSERT INTO cargo_shipments VALUES (2905995, 960137, 1, 39901, 95504, 524.8274412678563);
INSERT INTO cargo_shipments VALUES (7317439, 687019, 4, 11660, 21481, 781.9191794689269);
INSERT INTO cargo_shipments VALUES (2043248, 422404, 2, 18705, 61780, 688.7600735837296);
INSERT INTO cargo_shipments VALUES (4140456, 867984, 1, 46498, 56046, 531.9726303009011);
INSERT INTO cargo_shipments VALUES (9350041, 474492, 2, 28404, 73217, 20.42612727151294);
INSERT INTO cargo_shipments VALUES (9505123, 855102, 4, 47145, 43313, 620.0206492163937);
INSERT INTO cargo_shipments VALUES (8888165, 481738, 3, 39087, 16362, 651.7820628530629);
INSERT INTO cargo_shipments VALUES (4568107, 598078, 1, 29012, 22957, 649.5207678664183);
INSERT INTO cargo_shipments VALUES (8488913, 105533, 1, 20768, 99845, 850.6142382160693);
INSERT INTO cargo_shipments VALUES (9824473, 900209, 4, 16376, 73217, 237.53747327838792);
INSERT INTO cargo_shipments VALUES (3217251, 558558, 4, 33105, 87094, 960.0927374826484);
INSERT INTO cargo_shipments VALUES (4478526, 735633, 2, 35761, 40203, 199.88745262098485);
INSERT INTO cargo_shipments VALUES (7357308, 896822, 1, 7584, 46702, 15.724972483888378);
INSERT INTO cargo_shipments VALUES (6503533, 245326, 2, 49856, 69028, 352.7103220641171);
INSERT INTO cargo_shipments VALUES (3995498, 188334, 1, 26040, 95504, 715.4380388246094);
INSERT INTO cargo_shipments VALUES (2384636, 511881, 2, 19392, 66975, 997.659107013284);
INSERT INTO cargo_shipments VALUES (9099165, 304639, 2, 45890, 74354, 577.2861606556663);
INSERT INTO cargo_shipments VALUES (9384207, 251048, 2, 32958, 51197, 448.0636827766442);
INSERT INTO cargo_shipments VALUES (1332546, 280979, 3, 45603, 20032, 663.7048964057935);
INSERT INTO cargo_shipments VALUES (2033762, 655713, 1, 8641, 61748, 331.7301343925172);
INSERT INTO cargo_shipments VALUES (5057890, 947553, 4, 33468, 56046, 866.8875593216029);
INSERT INTO cargo_shipments VALUES (1106482, 251268, 1, 11352, 66620, 136.18970103245752);
INSERT INTO cargo_shipments VALUES (6941821, 253595, 3, 22860, 41503, 809.4104552736266);
INSERT INTO cargo_shipments VALUES (8679719, 305108, 1, 19205, 84370, 898.8315924628675);
INSERT INTO cargo_shipments VALUES (9073737, 978373, 4, 9191, 21481, 672.0167400670142);
INSERT INTO cargo_shipments VALUES (8620102, 445027, 2, 36790, 68168, 843.8346839732644);
INSERT INTO cargo_shipments VALUES (3415572, 446354, 4, 18079, 78092, 276.2021405800237);
INSERT INTO cargo_shipments VALUES (4392628, 549043, 4, 9330, 58358, 166.16974610108437);
INSERT INTO cargo_shipments VALUES (1619395, 358509, 4, 38026, 38903, 761.6374400334738);
INSERT INTO cargo_shipments VALUES (6450514, 286148, 3, 27392, 18819, 546.7524245290955);
INSERT INTO cargo_shipments VALUES (9742990, 758072, 3, 49081, 11136, 289.4403066830351);
INSERT INTO cargo_shipments VALUES (5629442, 875193, 3, 12686, 84900, 777.1687116297863);
INSERT INTO cargo_shipments VALUES (6769170, 777504, 3, 14957, 50739, 331.05273042452745);
INSERT INTO cargo_shipments VALUES (1817243, 353497, 4, 35509, 16790, 578.2861499545289);
INSERT INTO cargo_shipments VALUES (7785693, 198178, 2, 6880, 87624, 310.16808344159443);
INSERT INTO cargo_shipments VALUES (9054329, 859274, 3, 15789, 20438, 363.1561038959213);
INSERT INTO cargo_shipments VALUES (6707738, 995244, 2, 17902, 34263, 314.4933389899264);
INSERT INTO cargo_shipments VALUES (7402796, 948750, 1, 34475, 40203, 827.6103457077885);
INSERT INTO cargo_shipments VALUES (9569608, 167992, 2, 9896, 68168, 411.1612710126776);
INSERT INTO cargo_shipments VALUES (5337673, 648848, 2, 46745, 75571, 419.7206078934402);
INSERT INTO cargo_shipments VALUES (7889655, 596094, 4, 5380, 31016, 343.9194376036342);
INSERT INTO cargo_shipments VALUES (2896178, 446354, 1, 18742, 88059, 570.6901840139019);
INSERT INTO cargo_shipments VALUES (5448678, 595048, 3, 22035, 83314, 803.5485723253679);
INSERT INTO cargo_shipments VALUES (4246590, 675376, 2, 47066, 30534, 483.34165209442136);
INSERT INTO cargo_shipments VALUES (2940234, 667591, 1, 17039, 63674, 167.34012851143675);
INSERT INTO cargo_shipments VALUES (1146245, 252987, 1, 47900, 74533, 244.0426490953502);
INSERT INTO cargo_shipments VALUES (6959612, 114429, 1, 18570, 69183, 973.5661207450781);
INSERT INTO cargo_shipments VALUES (3361939, 521650, 3, 49290, 11136, 497.6989841862274);
INSERT INTO cargo_shipments VALUES (3413665, 301037, 1, 42328, 48435, 914.1259323230423);
INSERT INTO cargo_shipments VALUES (9617567, 388326, 1, 18501, 32861, 50.819185868674175);
INSERT INTO cargo_shipments VALUES (6658173, 352768, 4, 46164, 35306, 613.6121118875527);
INSERT INTO cargo_shipments VALUES (4187635, 107463, 3, 33719, 71907, 613.6895060374546);
INSERT INTO cargo_shipments VALUES (2111139, 982169, 4, 30531, 48910, 239.33089579517042);
INSERT INTO cargo_shipments VALUES (1402303, 455699, 1, 5166, 64620, 245.64005363870623);
INSERT INTO cargo_shipments VALUES (1932309, 745173, 1, 31197, 96914, 338.71979673487175);
INSERT INTO cargo_shipments VALUES (6912910, 519747, 1, 31775, 84900, 800.9472142699653);
INSERT INTO cargo_shipments VALUES (3739817, 967575, 1, 14061, 68168, 492.22326183524814);
INSERT INTO cargo_shipments VALUES (3536888, 608026, 3, 33081, 17062, 500.52259187537106);
INSERT INTO cargo_shipments VALUES (2608030, 133800, 1, 4256, 74533, 460.18772477106796);
INSERT INTO cargo_shipments VALUES (7130610, 648680, 3, 19989, 18630, 778.0750981955965);
INSERT INTO cargo_shipments VALUES (4261507, 259977, 1, 13405, 14955, 956.2151840629159);
INSERT INTO cargo_shipments VALUES (6245057, 544464, 2, 9198, 71907, 521.113056981628);
INSERT INTO cargo_shipments VALUES (8737597, 491415, 3, 24575, 26625, 213.76942748892114);
INSERT INTO cargo_shipments VALUES (6691471, 301962, 4, 4645, 55608, 525.2459338683706);
INSERT INTO cargo_shipments VALUES (2848705, 645334, 4, 49914, 34984, 969.0093108307749);
INSERT INTO cargo_shipments VALUES (4459652, 761485, 4, 21314, 36421, 717.4887663147101);
INSERT INTO cargo_shipments VALUES (1262039, 947553, 2, 17567, 17145, 354.301852288465);
INSERT INTO cargo_shipments VALUES (8598219, 438748, 2, 2339, 20438, 675.1179151600593);
INSERT INTO cargo_shipments VALUES (8486947, 125992, 1, 34033, 87478, 246.04841731667327);
INSERT INTO cargo_shipments VALUES (7949197, 866524, 2, 36010, 63186, 747.4805207871646);
INSERT INTO cargo_shipments VALUES (8042641, 947553, 4, 15168, 26822, 506.61677745779053);
INSERT INTO cargo_shipments VALUES (7472702, 250822, 1, 5674, 88059, 45.070262304164885);
INSERT INTO cargo_shipments VALUES (9581714, 691232, 2, 47119, 16362, 190.23832351936986);
INSERT INTO cargo_shipments VALUES (3288516, 268775, 3, 3661, 40856, 156.72533815230793);
INSERT INTO cargo_shipments VALUES (8146846, 967575, 1, 1677, 96914, 242.3990471095042);
INSERT INTO cargo_shipments VALUES (8137136, 188334, 1, 31510, 42830, 957.5191509432141);
INSERT INTO cargo_shipments VALUES (9323052, 301037, 3, 34417, 37665, 436.8208751688196);
INSERT INTO cargo_shipments VALUES (5686087, 212978, 1, 16681, 51197, 8.209718781670649);
INSERT INTO cargo_shipments VALUES (3078853, 884599, 3, 15032, 14388, 922.19168971083);
INSERT INTO cargo_shipments VALUES (1610680, 413138, 2, 14229, 95509, 193.8215670676644);
INSERT INTO cargo_shipments VALUES (1366185, 837435, 1, 31565, 81142, 228.1268566669463);
INSERT INTO cargo_shipments VALUES (4917402, 519222, 1, 11816, 56046, 356.4514093949116);
INSERT INTO cargo_shipments VALUES (2700417, 818559, 3, 36663, 16790, 247.2521527280448);
INSERT INTO cargo_shipments VALUES (2876420, 884599, 4, 9003, 68759, 198.0356692839592);
INSERT INTO cargo_shipments VALUES (7082985, 692445, 4, 34562, 45027, 259.5692168304627);
INSERT INTO cargo_shipments VALUES (2611781, 555899, 4, 5376, 35881, 729.8723831700623);
INSERT INTO cargo_shipments VALUES (3127538, 436083, 3, 45221, 42117, 225.4420577968157);
INSERT INTO cargo_shipments VALUES (9826797, 982169, 4, 9907, 63996, 998.6898060350816);
INSERT INTO cargo_shipments VALUES (4327087, 611911, 2, 28972, 95020, 851.0845324034195);
INSERT INTO cargo_shipments VALUES (2049773, 488373, 4, 23786, 90584, 975.3187609833301);
INSERT INTO cargo_shipments VALUES (8935324, 636347, 1, 7785, 32861, 204.13628040959685);
INSERT INTO cargo_shipments VALUES (2974858, 373731, 2, 1270, 32861, 917.8678556317124);
INSERT INTO cargo_shipments VALUES (5484461, 626357, 2, 10620, 18630, 597.353932394547);
INSERT INTO cargo_shipments VALUES (9506002, 436083, 1, 20157, 37468, 691.1490455844423);
INSERT INTO cargo_shipments VALUES (9919636, 678301, 2, 34442, 78897, 669.5118947253185);
INSERT INTO cargo_shipments VALUES (5738539, 845815, 3, 5817, 25806, 695.2252070062706);
INSERT INTO cargo_shipments VALUES (7305505, 333621, 4, 47790, 14388, 915.9461974411672);
INSERT INTO cargo_shipments VALUES (1783690, 986540, 4, 23658, 19125, 769.0367153555153);
INSERT INTO cargo_shipments VALUES (8635690, 265050, 3, 37576, 69183, 706.3856791386762);
INSERT INTO cargo_shipments VALUES (5832598, 910151, 3, 5932, 66374, 223.83496563590643);
INSERT INTO cargo_shipments VALUES (9044934, 250822, 2, 5181, 47786, 68.62538064556279);
INSERT INTO cargo_shipments VALUES (7322108, 932171, 4, 8538, 69183, 366.24310256604485);
INSERT INTO cargo_shipments VALUES (1781486, 815933, 3, 49061, 85299, 491.3542143369216);
INSERT INTO cargo_shipments VALUES (4439436, 352768, 3, 10863, 36421, 778.4049210937443);
INSERT INTO cargo_shipments VALUES (1142640, 817972, 2, 39390, 47786, 920.6390162590577);
INSERT INTO cargo_shipments VALUES (7754824, 447164, 2, 42909, 32608, 400.08238061498815);
INSERT INTO cargo_shipments VALUES (8878926, 531537, 1, 18232, 87094, 585.3092798132296);
INSERT INTO cargo_shipments VALUES (2652660, 692445, 1, 43134, 85047, 745.3811253339677);
INSERT INTO cargo_shipments VALUES (8246332, 436083, 3, 43309, 58401, 98.4175546977476);
INSERT INTO cargo_shipments VALUES (8152489, 719236, 4, 21523, 74354, 716.0416627205461);
INSERT INTO cargo_shipments VALUES (7577913, 280979, 1, 17357, 93319, 698.2806671500763);
INSERT INTO cargo_shipments VALUES (3082454, 636347, 3, 19634, 77171, 600.4010076724824);
INSERT INTO cargo_shipments VALUES (2000537, 900209, 3, 19834, 87094, 82.3862425226235);
INSERT INTO cargo_shipments VALUES (7228105, 380476, 1, 26586, 46879, 424.77501472313486);
INSERT INTO cargo_shipments VALUES (3753921, 298068, 4, 40622, 16362, 577.1192298295988);
INSERT INTO cargo_shipments VALUES (7303112, 636347, 1, 35959, 61748, 27.66314567035577);
INSERT INTO cargo_shipments VALUES (5262026, 601323, 2, 27092, 84900, 753.5016897533909);
INSERT INTO cargo_shipments VALUES (6259723, 574737, 3, 6913, 37468, 564.0129985185838);
INSERT INTO cargo_shipments VALUES (3103174, 170058, 1, 45746, 30603, 675.8864750634775);
INSERT INTO cargo_shipments VALUES (5257373, 960137, 2, 31143, 58358, 705.7857745670302);
INSERT INTO cargo_shipments VALUES (8812088, 223703, 1, 32179, 34668, 484.008691065957);
INSERT INTO cargo_shipments VALUES (5565524, 388326, 4, 30436, 87094, 417.63899916014293);
INSERT INTO cargo_shipments VALUES (9226165, 114429, 2, 27503, 67867, 432.0855480242717);
INSERT INTO cargo_shipments VALUES (3911560, 606002, 2, 20309, 20032, 928.5010948809146);
INSERT INTO cargo_shipments VALUES (3605985, 743993, 4, 43261, 34263, 32.47024367743312);
INSERT INTO cargo_shipments VALUES (7938014, 798926, 4, 8724, 66620, 155.79581843841572);
INSERT INTO cargo_shipments VALUES (4781962, 162259, 1, 38835, 84370, 94.91951632154583);
INSERT INTO cargo_shipments VALUES (7262059, 459081, 1, 44366, 31016, 568.9541951129469);
INSERT INTO cargo_shipments VALUES (6124005, 574095, 4, 46562, 43047, 391.8942819970549);
INSERT INTO cargo_shipments VALUES (3182010, 372577, 1, 28780, 43047, 728.8745088884914);
INSERT INTO cargo_shipments VALUES (9621662, 162305, 2, 30975, 31016, 508.40912170865636);
INSERT INTO cargo_shipments VALUES (4502576, 491415, 3, 7363, 48435, 876.7782554935268);
INSERT INTO cargo_shipments VALUES (3719201, 513506, 2, 23080, 20032, 698.7515175865143);
INSERT INTO cargo_shipments VALUES (5665851, 655713, 2, 26526, 34263, 638.4952328328501);
INSERT INTO cargo_shipments VALUES (2070241, 574737, 3, 9470, 53546, 128.58542748179357);
INSERT INTO cargo_shipments VALUES (3119276, 380476, 4, 14157, 16362, 700.1491087826797);
INSERT INTO cargo_shipments VALUES (2204783, 703920, 2, 28677, 69973, 934.3518798600485);
INSERT INTO cargo_shipments VALUES (4375130, 961801, 4, 14222, 40203, 707.8061871471627);
INSERT INTO cargo_shipments VALUES (6484755, 505917, 4, 10114, 51197, 114.90801026645336);
INSERT INTO cargo_shipments VALUES (8432730, 271976, 1, 39743, 38903, 390.4320303591787);
INSERT INTO cargo_shipments VALUES (7312259, 452680, 3, 24075, 11754, 42.14008845757722);
INSERT INTO cargo_shipments VALUES (1127273, 624235, 4, 32965, 43047, 457.7031427208881);
INSERT INTO cargo_shipments VALUES (4664798, 996823, 2, 19253, 52750, 628.6303421644408);
INSERT INTO cargo_shipments VALUES (5809551, 755059, 3, 9151, 95504, 345.7059713450501);
INSERT INTO cargo_shipments VALUES (4933649, 572361, 1, 30969, 37665, 557.1195523653879);
INSERT INTO cargo_shipments VALUES (2643161, 142736, 4, 13699, 73315, 363.8201664685825);
INSERT INTO cargo_shipments VALUES (6659690, 420661, 2, 20358, 83314, 382.9937002293668);
INSERT INTO cargo_shipments VALUES (5599132, 598078, 4, 41798, 20438, 352.1425743699862);
INSERT INTO cargo_shipments VALUES (7796885, 714581, 2, 14075, 32608, 978.7472581756555);
INSERT INTO cargo_shipments VALUES (1870914, 782915, 1, 15046, 89117, 987.744221262681);
INSERT INTO cargo_shipments VALUES (3708217, 859274, 2, 18418, 64620, 316.12825938983326);
INSERT INTO cargo_shipments VALUES (8629504, 446354, 1, 11229, 73217, 314.28931969710794);
INSERT INTO cargo_shipments VALUES (7720484, 742858, 3, 10529, 61780, 794.6443818846669);
INSERT INTO cargo_shipments VALUES (9570419, 643092, 4, 12812, 89117, 612.2767993065994);
INSERT INTO cargo_shipments VALUES (8019037, 533295, 3, 24827, 40856, 413.2799314513023);
INSERT INTO cargo_shipments VALUES (1102249, 120546, 4, 42544, 84370, 855.8420227770953);
INSERT INTO cargo_shipments VALUES (9653054, 353497, 3, 26771, 75270, 65.18334652775404);
INSERT INTO cargo_shipments VALUES (1837007, 796715, 1, 23740, 88944, 35.28321927076317);
INSERT INTO cargo_shipments VALUES (3332591, 986540, 4, 25435, 11178, 50.76285575382966);
INSERT INTO cargo_shipments VALUES (6623510, 878046, 2, 9825, 87094, 260.0140887955256);
INSERT INTO cargo_shipments VALUES (2232581, 996048, 4, 43360, 53546, 746.1920007411751);
INSERT INTO cargo_shipments VALUES (8911150, 304639, 1, 3296, 46879, 762.0368133766402);
INSERT INTO cargo_shipments VALUES (8525727, 499982, 2, 34361, 34668, 65.13308299623665);
INSERT INTO cargo_shipments VALUES (3000198, 474492, 3, 23866, 78897, 142.278899067786);
INSERT INTO cargo_shipments VALUES (7768768, 142736, 3, 6409, 43313, 515.8939399620192);
INSERT INTO cargo_shipments VALUES (3235547, 660589, 1, 23434, 73357, 568.6166516609733);
INSERT INTO cargo_shipments VALUES (2733813, 761485, 3, 1040, 31584, 694.0549981615121);
INSERT INTO cargo_shipments VALUES (5772234, 469804, 1, 15172, 68168, 101.4497362495651);
INSERT INTO cargo_shipments VALUES (3040208, 978373, 1, 38359, 34263, 366.25892205122835);
INSERT INTO cargo_shipments VALUES (8165863, 389460, 2, 11060, 74735, 704.3669724125884);
INSERT INTO cargo_shipments VALUES (7280530, 948750, 4, 43278, 63996, 443.5567027702084);
INSERT INTO cargo_shipments VALUES (8837373, 558558, 1, 36115, 73640, 664.3613463281575);
INSERT INTO cargo_shipments VALUES (1579616, 633887, 1, 33732, 99845, 792.5790293123306);
INSERT INTO cargo_shipments VALUES (9016279, 353497, 2, 30121, 38582, 73.51107868804252);
INSERT INTO cargo_shipments VALUES (1867937, 655713, 4, 44767, 66975, 804.6971784539121);
INSERT INTO cargo_shipments VALUES (9979294, 212978, 3, 23886, 69973, 389.56417994692583);
INSERT INTO cargo_shipments VALUES (5611198, 513506, 2, 15862, 41156, 916.1508658858838);
INSERT INTO cargo_shipments VALUES (2520205, 200324, 1, 42937, 26822, 61.15603095819666);
INSERT INTO cargo_shipments VALUES (1159764, 645334, 4, 26028, 37665, 496.57740462765906);
INSERT INTO cargo_shipments VALUES (1958844, 596094, 3, 12023, 48435, 214.07609430862607);
INSERT INTO cargo_shipments VALUES (9881812, 775508, 4, 18180, 35963, 895.4221336498663);
INSERT INTO cargo_shipments VALUES (5297093, 296521, 2, 19328, 74354, 879.7759877829154);
INSERT INTO cargo_shipments VALUES (8660753, 133800, 3, 1345, 65481, 74.32800079335189);
INSERT INTO cargo_shipments VALUES (8800207, 905715, 1, 37320, 76275, 964.6584308537463);
INSERT INTO cargo_shipments VALUES (6511460, 626357, 3, 38107, 11178, 370.05105267906356);
INSERT INTO cargo_shipments VALUES (2195876, 817972, 2, 24395, 48241, 914.0908032512424);
INSERT INTO cargo_shipments VALUES (3985021, 932171, 2, 13919, 88944, 822.6686252511951);
INSERT INTO cargo_shipments VALUES (3610057, 280979, 4, 20886, 85047, 69.71435242158286);
INSERT INTO cargo_shipments VALUES (1755796, 742858, 4, 43033, 48241, 686.4934686430048);
INSERT INTO cargo_shipments VALUES (7508428, 272236, 1, 36357, 36421, 766.2912242093471);
INSERT INTO cargo_shipments VALUES (7527806, 520335, 2, 11593, 55608, 498.58948915139746);
INSERT INTO cargo_shipments VALUES (2755826, 170897, 1, 3758, 51197, 912.6543979825768);
INSERT INTO cargo_shipments VALUES (5140649, 416286, 4, 39083, 54150, 805.3775371896795);
INSERT INTO cargo_shipments VALUES (7140426, 645334, 1, 43248, 95504, 176.75480555505908);
INSERT INTO cargo_shipments VALUES (1724852, 513506, 4, 38677, 66975, 835.6597293313356);
INSERT INTO cargo_shipments VALUES (7527003, 606002, 2, 29390, 71907, 920.8367912504539);
INSERT INTO cargo_shipments VALUES (4162004, 703920, 3, 29230, 66620, 737.4368562678859);
INSERT INTO cargo_shipments VALUES (9050646, 301962, 2, 30336, 65481, 160.66390162794698);
INSERT INTO cargo_shipments VALUES (3318294, 212978, 4, 49709, 88271, 522.2937320873771);
INSERT INTO cargo_shipments VALUES (3564411, 374226, 4, 45415, 11178, 729.6203426725991);
INSERT INTO cargo_shipments VALUES (5039757, 776981, 2, 32464, 26625, 861.6640936203914);
INSERT INTO cargo_shipments VALUES (9582459, 988636, 1, 23762, 51067, 668.4710075898801);
INSERT INTO cargo_shipments VALUES (8484139, 436083, 2, 35078, 22999, 37.85316529958682);
INSERT INTO cargo_shipments VALUES (4448715, 965863, 4, 29361, 66620, 929.372199120859);
INSERT INTO cargo_shipments VALUES (8868385, 296521, 4, 10564, 41503, 842.8884184502186);
INSERT INTO cargo_shipments VALUES (1918444, 595048, 4, 18652, 90584, 379.3928255186203);
INSERT INTO cargo_shipments VALUES (7741009, 649000, 1, 2295, 55608, 263.56455795895437);
INSERT INTO cargo_shipments VALUES (7631788, 383901, 4, 36699, 37665, 589.2389252456842);
INSERT INTO cargo_shipments VALUES (2469670, 988636, 2, 42789, 64986, 851.4227320140611);
INSERT INTO cargo_shipments VALUES (3383880, 649000, 1, 15492, 31584, 120.56815954400557);
INSERT INTO cargo_shipments VALUES (9286258, 491415, 3, 11083, 22957, 497.10149534393076);
INSERT INTO cargo_shipments VALUES (6107050, 952614, 4, 18373, 88944, 525.3587424375884);
INSERT INTO cargo_shipments VALUES (2622017, 757508, 4, 21628, 66672, 779.0116637794072);
INSERT INTO cargo_shipments VALUES (5243526, 301037, 1, 6824, 36421, 979.1087113556085);
INSERT INTO cargo_shipments VALUES (7691463, 634792, 2, 39002, 87624, 228.01220002665735);
INSERT INTO cargo_shipments VALUES (3863370, 735633, 2, 1949, 64620, 377.51724335975456);
INSERT INTO cargo_shipments VALUES (8365923, 632084, 1, 33293, 41156, 282.59673017896824);
INSERT INTO cargo_shipments VALUES (1175823, 724842, 3, 20439, 11136, 327.31078263435944);
INSERT INTO cargo_shipments VALUES (3917652, 793872, 1, 12089, 18630, 565.582587602547);
INSERT INTO cargo_shipments VALUES (2826875, 303778, 3, 10624, 87094, 215.1800124683265);
INSERT INTO cargo_shipments VALUES (5815321, 703367, 3, 27846, 90584, 864.0599273337788);
INSERT INTO cargo_shipments VALUES (4655724, 859274, 4, 33919, 22999, 390.1900970853428);
INSERT INTO cargo_shipments VALUES (7677713, 581397, 1, 12302, 32861, 435.2571659758867);
INSERT INTO cargo_shipments VALUES (4521156, 914016, 2, 9151, 38582, 853.9478942949681);
INSERT INTO cargo_shipments VALUES (5596351, 601323, 4, 42148, 63674, 324.13705102213544);
INSERT INTO cargo_shipments VALUES (7527637, 389460, 2, 27437, 73640, 999.2027926352489);
INSERT INTO cargo_shipments VALUES (8507306, 970881, 3, 23086, 22999, 952.444219812655);
INSERT INTO cargo_shipments VALUES (3797501, 252987, 1, 11824, 29828, 493.7590030450323);
INSERT INTO cargo_shipments VALUES (7984672, 223703, 1, 13665, 85299, 122.19029381587943);
INSERT INTO cargo_shipments VALUES (2770230, 608026, 3, 36831, 37468, 39.487491334456394);
INSERT INTO cargo_shipments VALUES (4538686, 128482, 1, 22789, 26822, 106.17049816128299);
INSERT INTO cargo_shipments VALUES (3008785, 572301, 4, 25009, 51197, 348.11625501758147);
INSERT INTO cargo_shipments VALUES (8646539, 162259, 3, 18101, 46879, 759.1304819628333);
INSERT INTO cargo_shipments VALUES (1866833, 606002, 3, 39038, 16362, 38.186909033974146);
INSERT INTO cargo_shipments VALUES (7567129, 358509, 4, 15939, 67067, 4.903922101455738);
INSERT INTO cargo_shipments VALUES (7171174, 859272, 4, 25074, 19125, 281.2288298075558);
INSERT INTO cargo_shipments VALUES (7763660, 505854, 3, 33289, 38903, 176.36182379938958);
INSERT INTO cargo_shipments VALUES (6105767, 642804, 2, 2473, 50739, 585.8101076419103);
INSERT INTO cargo_shipments VALUES (3298090, 815933, 1, 33072, 32861, 436.7632537154168);
INSERT INTO cargo_shipments VALUES (5562673, 648848, 3, 12644, 64620, 248.22629814176557);
INSERT INTO cargo_shipments VALUES (4650021, 120546, 2, 3160, 42117, 569.1580222799397);
INSERT INTO cargo_shipments VALUES (4534318, 230833, 1, 37816, 35963, 353.9945493224156);
INSERT INTO cargo_shipments VALUES (2523523, 793872, 1, 6346, 89117, 742.6202275796314);
INSERT INTO cargo_shipments VALUES (9119051, 420661, 4, 10663, 76275, 332.496147044952);
INSERT INTO cargo_shipments VALUES (3150858, 381311, 2, 23116, 95020, 103.31331311582747);
INSERT INTO cargo_shipments VALUES (7568481, 286320, 2, 24824, 68759, 772.6911053875182);
INSERT INTO cargo_shipments VALUES (3420606, 646600, 3, 30750, 37665, 972.4797067255155);
INSERT INTO cargo_shipments VALUES (3677462, 205227, 4, 11290, 76275, 900.1056538817753);
INSERT INTO cargo_shipments VALUES (2963699, 452680, 1, 16310, 43047, 358.93157724017544);
INSERT INTO cargo_shipments VALUES (4491446, 114429, 3, 20099, 61780, 52.3073874089468);
INSERT INTO cargo_shipments VALUES (3689945, 544464, 1, 9504, 31694, 370.53095437607277);
INSERT INTO cargo_shipments VALUES (3138156, 863835, 1, 3999, 93903, 708.3911270312399);
INSERT INTO cargo_shipments VALUES (2908162, 352768, 4, 6798, 50739, 585.0285467529064);
INSERT INTO cargo_shipments VALUES (3389229, 574095, 1, 25429, 14955, 675.5235211590289);
INSERT INTO cargo_shipments VALUES (8106373, 678322, 1, 7754, 93319, 800.7837020214595);
INSERT INTO cargo_shipments VALUES (8401695, 358509, 4, 31414, 38582, 457.01541758946405);
INSERT INTO cargo_shipments VALUES (9743779, 170058, 3, 39733, 37865, 964.0637359460733);
INSERT INTO cargo_shipments VALUES (5779534, 226404, 3, 43264, 69028, 276.7404090717933);
INSERT INTO cargo_shipments VALUES (3459300, 555899, 4, 48578, 95509, 305.7312126029802);
INSERT INTO cargo_shipments VALUES (6542826, 332039, 4, 41535, 26625, 105.03725055550295);
INSERT INTO cargo_shipments VALUES (8351908, 948750, 3, 7471, 37665, 696.7224349576983);
INSERT INTO cargo_shipments VALUES (8173832, 505854, 4, 16296, 32608, 403.1325837276387);
INSERT INTO cargo_shipments VALUES (7655974, 127089, 4, 5761, 63186, 780.3105588435623);
INSERT INTO cargo_shipments VALUES (8625431, 505917, 1, 7263, 87094, 973.0362056184655);
INSERT INTO cargo_shipments VALUES (8997193, 252642, 4, 44085, 37468, 604.4780449148665);
INSERT INTO cargo_shipments VALUES (1027368, 268775, 2, 22726, 36421, 755.6089351806273);
INSERT INTO cargo_shipments VALUES (9688054, 687019, 4, 39782, 41156, 320.4307280962564);
INSERT INTO cargo_shipments VALUES (8448104, 397201, 2, 3712, 51067, 708.4066670512556);
INSERT INTO cargo_shipments VALUES (2035845, 775508, 3, 11604, 18819, 188.49537304053044);
INSERT INTO cargo_shipments VALUES (2505688, 259977, 2, 40151, 66975, 15.939435421843173);
INSERT INTO cargo_shipments VALUES (9712343, 520335, 3, 27418, 20032, 196.12052152594762);
INSERT INTO cargo_shipments VALUES (5819910, 223703, 2, 31176, 93319, 72.08418357283041);
INSERT INTO cargo_shipments VALUES (9061965, 280979, 3, 20453, 46879, 680.9742173247068);
INSERT INTO cargo_shipments VALUES (5651658, 463436, 1, 14432, 74533, 747.0184061972813);
INSERT INTO cargo_shipments VALUES (1054590, 296521, 4, 21654, 31016, 93.05450105864821);
INSERT INTO cargo_shipments VALUES (6355089, 912153, 3, 8863, 34984, 519.3117687594724);
INSERT INTO cargo_shipments VALUES (7258789, 388326, 4, 42720, 32608, 729.2144207532813);
INSERT INTO cargo_shipments VALUES (5757821, 793872, 3, 24630, 55608, 934.4943440501995);
INSERT INTO cargo_shipments VALUES (5493937, 678301, 1, 15472, 68759, 714.2423669379132);
INSERT INTO cargo_shipments VALUES (2267192, 181455, 4, 21928, 27794, 874.4797553340609);
INSERT INTO cargo_shipments VALUES (2344620, 859274, 2, 29308, 23317, 604.4989532723865);
INSERT INTO cargo_shipments VALUES (8692707, 127089, 2, 2415, 61748, 98.7299210614817);
INSERT INTO cargo_shipments VALUES (7920743, 742858, 1, 28080, 99845, 732.9213004509496);
INSERT INTO cargo_shipments VALUES (4659769, 881607, 2, 15416, 36421, 972.2316360598895);
INSERT INTO cargo_shipments VALUES (7696956, 932235, 4, 14425, 38582, 25.200551469443777);
INSERT INTO cargo_shipments VALUES (9537245, 198178, 3, 10743, 53546, 884.7854664042954);
INSERT INTO cargo_shipments VALUES (2386626, 558558, 4, 33262, 69973, 988.6755947779085);
INSERT INTO cargo_shipments VALUES (6882109, 567241, 2, 21521, 88944, 568.4582572234248);
INSERT INTO cargo_shipments VALUES (4600685, 298068, 2, 37584, 34263, 140.00389139006165);
INSERT INTO cargo_shipments VALUES (8290578, 983159, 1, 2938, 58358, 217.42672555915797);
INSERT INTO cargo_shipments VALUES (4145365, 743993, 4, 31838, 27794, 561.6414054411681);
INSERT INTO cargo_shipments VALUES (1887818, 511695, 3, 49349, 40203, 958.636278831417);
INSERT INTO cargo_shipments VALUES (3273689, 817972, 3, 42747, 74354, 463.1583023634437);
INSERT INTO cargo_shipments VALUES (1288704, 563327, 4, 32225, 49609, 123.00700365394978);
INSERT INTO cargo_shipments VALUES (9717849, 677816, 2, 46883, 30534, 5.303259551434625);
INSERT INTO cargo_shipments VALUES (6765424, 962177, 1, 12688, 61748, 785.3112377779773);
INSERT INTO cargo_shipments VALUES (5757059, 481738, 4, 18869, 54150, 747.7925520854163);
INSERT INTO cargo_shipments VALUES (6330447, 914016, 3, 33456, 83314, 352.708732878776);
INSERT INTO cargo_shipments VALUES (5637563, 703920, 3, 44470, 45027, 942.9421328469335);
INSERT INTO cargo_shipments VALUES (9159823, 777504, 1, 18430, 17145, 34.59658210548522);
INSERT INTO cargo_shipments VALUES (9157918, 995244, 1, 46657, 22999, 5.257094702372456);
INSERT INTO cargo_shipments VALUES (9601996, 277004, 2, 47101, 11136, 776.4167892319987);
INSERT INTO cargo_shipments VALUES (5024607, 511695, 2, 12657, 25806, 87.8371927621997);
INSERT INTO cargo_shipments VALUES (4188021, 624235, 3, 27106, 34668, 926.6726013596439);
INSERT INTO cargo_shipments VALUES (8367942, 543693, 3, 20071, 25571, 939.2508386075937);
INSERT INTO cargo_shipments VALUES (1566116, 707099, 4, 1022, 31584, 709.7834807417094);
INSERT INTO cargo_shipments VALUES (9251547, 208914, 2, 5624, 21481, 430.85497704002364);
INSERT INTO cargo_shipments VALUES (1257865, 777504, 4, 18127, 87094, 288.32708154677766);
INSERT INTO cargo_shipments VALUES (9489204, 626357, 1, 17727, 35963, 731.5593549308254);
INSERT INTO cargo_shipments VALUES (2251812, 800049, 3, 36541, 70479, 508.31232890963804);
INSERT INTO cargo_shipments VALUES (3564282, 271117, 2, 29143, 38582, 575.3022532769794);
INSERT INTO cargo_shipments VALUES (8806753, 960137, 1, 17674, 74533, 598.0151668508835);
INSERT INTO cargo_shipments VALUES (7624047, 250822, 2, 44855, 69183, 728.0373783029413);
INSERT INTO cargo_shipments VALUES (4587979, 346445, 2, 31476, 63186, 689.7681118269489);
INSERT INTO cargo_shipments VALUES (2820950, 155883, 2, 29692, 37665, 338.3460775482656);
INSERT INTO cargo_shipments VALUES (3565066, 646600, 3, 44091, 16362, 658.8250660615987);
INSERT INTO cargo_shipments VALUES (9205479, 998327, 4, 30886, 74354, 834.2975881457787);
INSERT INTO cargo_shipments VALUES (4162130, 867984, 2, 34366, 17145, 263.6160986201456);
INSERT INTO cargo_shipments VALUES (2161134, 253595, 3, 39862, 58358, 812.5898510989678);
INSERT INTO cargo_shipments VALUES (4538805, 170897, 3, 5729, 41156, 848.5656993944748);
INSERT INTO cargo_shipments VALUES (4166792, 626357, 1, 17284, 68168, 844.6708519086357);
INSERT INTO cargo_shipments VALUES (7135941, 633887, 2, 21602, 32608, 683.4037153059651);
INSERT INTO cargo_shipments VALUES (1235189, 767347, 1, 23174, 97714, 822.2557235172274);
INSERT INTO cargo_shipments VALUES (5530354, 543693, 4, 48927, 64986, 657.2882192705828);
INSERT INTO cargo_shipments VALUES (5362873, 606002, 3, 17238, 30603, 568.6821111238798);
INSERT INTO cargo_shipments VALUES (6531090, 815951, 4, 42363, 64620, 358.41779264265097);
INSERT INTO cargo_shipments VALUES (9878192, 105533, 3, 29268, 36421, 357.02920963677485);
INSERT INTO cargo_shipments VALUES (3440650, 660589, 1, 6443, 81855, 253.22481040482893);
INSERT INTO cargo_shipments VALUES (1813724, 691232, 3, 40284, 54150, 412.31238415485194);
INSERT INTO cargo_shipments VALUES (2765035, 834600, 2, 36455, 32861, 594.5219582739938);
INSERT INTO cargo_shipments VALUES (1305019, 615277, 4, 27247, 52750, 415.8758659238515);
INSERT INTO cargo_shipments VALUES (1503433, 692445, 1, 39535, 84900, 788.9246921584863);
INSERT INTO cargo_shipments VALUES (5434959, 388326, 2, 40110, 22999, 181.9043265824205);
INSERT INTO cargo_shipments VALUES (1852578, 469053, 1, 41171, 77171, 375.9434236475546);
INSERT INTO cargo_shipments VALUES (8844539, 581397, 2, 23864, 51067, 899.080547444749);
INSERT INTO cargo_shipments VALUES (7610467, 643092, 1, 34140, 95020, 225.7363904260764);
INSERT INTO cargo_shipments VALUES (5317741, 567241, 4, 32587, 18819, 669.3836559222657);
INSERT INTO cargo_shipments VALUES (1814935, 544464, 2, 6676, 34668, 529.6747674057331);
INSERT INTO cargo_shipments VALUES (4800073, 859274, 1, 1249, 55037, 25.205498040847374);
INSERT INTO cargo_shipments VALUES (9126043, 646600, 4, 14299, 67867, 41.158719643041294);
INSERT INTO cargo_shipments VALUES (3145037, 481738, 4, 3451, 58358, 601.4030046596889);
INSERT INTO cargo_shipments VALUES (6736854, 624235, 1, 11113, 90584, 797.7427537227732);
INSERT INTO cargo_shipments VALUES (6765874, 198178, 1, 4636, 35306, 134.14091953535322);
INSERT INTO cargo_shipments VALUES (8401805, 511881, 4, 36321, 43313, 809.5541311347569);
INSERT INTO cargo_shipments VALUES (6682329, 986540, 4, 33264, 81855, 830.586126431401);
INSERT INTO cargo_shipments VALUES (3564724, 107463, 1, 8981, 35963, 649.2143299537432);
INSERT INTO cargo_shipments VALUES (6173010, 614681, 3, 21991, 37865, 825.375687760906);
INSERT INTO cargo_shipments VALUES (5903569, 988636, 1, 12875, 85299, 453.32758380947604);
INSERT INTO cargo_shipments VALUES (1200426, 481738, 2, 28346, 61748, 495.9986800478613);
INSERT INTO cargo_shipments VALUES (3666646, 205227, 1, 24568, 67867, 997.9554260358618);
INSERT INTO cargo_shipments VALUES (1048925, 251268, 4, 1373, 78092, 279.2869973141289);
INSERT INTO cargo_shipments VALUES (3491767, 436083, 1, 16571, 11178, 144.47971039195596);
INSERT INTO cargo_shipments VALUES (6909419, 675298, 4, 2551, 26822, 794.7917715327387);
INSERT INTO cargo_shipments VALUES (7162401, 214523, 2, 33663, 89117, 203.12932427988162);
INSERT INTO cargo_shipments VALUES (9693908, 513506, 1, 24158, 36421, 372.53684175797196);
INSERT INTO cargo_shipments VALUES (7648563, 251048, 2, 25415, 61780, 649.9208632597071);
INSERT INTO cargo_shipments VALUES (3767428, 655713, 3, 16509, 78897, 730.872388077539);
INSERT INTO cargo_shipments VALUES (5392149, 544464, 3, 43357, 96914, 802.7235290843435);
INSERT INTO cargo_shipments VALUES (1176301, 358509, 2, 36892, 17145, 926.8453996834153);
INSERT INTO cargo_shipments VALUES (1841740, 286320, 1, 8982, 75571, 518.9478685470042);
INSERT INTO cargo_shipments VALUES (6260848, 468598, 4, 46318, 77171, 945.7462392407974);
INSERT INTO cargo_shipments VALUES (3941594, 240695, 1, 22240, 20032, 736.0540650494754);
INSERT INTO cargo_shipments VALUES (8277090, 678322, 4, 44182, 41156, 22.314775838142342);
INSERT INTO cargo_shipments VALUES (2878119, 286148, 3, 17014, 32608, 863.3548199592353);
INSERT INTO cargo_shipments VALUES (2198849, 667591, 2, 13784, 43313, 219.21351804574905);
INSERT INTO cargo_shipments VALUES (1293098, 866524, 1, 30133, 67867, 246.86469493880648);
INSERT INTO cargo_shipments VALUES (6528044, 712175, 3, 21862, 42830, 369.5442278508091);
INSERT INTO cargo_shipments VALUES (7414975, 154036, 3, 24523, 32608, 524.0421945524512);
INSERT INTO cargo_shipments VALUES (7244755, 859274, 3, 35449, 87624, 736.7417511205196);
INSERT INTO cargo_shipments VALUES (5908832, 715589, 4, 21978, 36421, 99.4504757486666);
INSERT INTO cargo_shipments VALUES (9647224, 250822, 3, 35872, 66620, 345.2637399896965);
INSERT INTO cargo_shipments VALUES (9480600, 646600, 3, 47856, 96914, 85.34422708513101);
INSERT INTO cargo_shipments VALUES (6137998, 380476, 4, 15461, 73640, 334.40035367595334);
INSERT INTO cargo_shipments VALUES (9083829, 900209, 1, 39670, 75571, 681.1577463985398);
INSERT INTO cargo_shipments VALUES (2368282, 127089, 1, 17776, 66672, 446.4672914529155);
INSERT INTO cargo_shipments VALUES (6993803, 198178, 2, 25087, 63674, 481.64251555202844);
INSERT INTO cargo_shipments VALUES (6316895, 488373, 3, 36948, 42117, 422.70420242927264);
INSERT INTO cargo_shipments VALUES (9799031, 606830, 4, 26792, 42830, 304.2660385393863);
INSERT INTO cargo_shipments VALUES (4938925, 301037, 3, 15680, 63186, 344.4440002184853);
INSERT INTO cargo_shipments VALUES (2426058, 793872, 4, 14179, 55608, 992.5753307711004);
INSERT INTO cargo_shipments VALUES (3538053, 452680, 1, 28970, 18819, 337.5342478494239);
INSERT INTO cargo_shipments VALUES (2958193, 285339, 3, 32912, 66975, 548.2630360369387);
INSERT INTO cargo_shipments VALUES (5809650, 167992, 3, 45987, 31584, 199.41223280312147);
INSERT INTO cargo_shipments VALUES (4574965, 692445, 3, 49606, 12096, 322.95350306710804);
INSERT INTO cargo_shipments VALUES (2315195, 251268, 2, 24663, 13533, 753.030536468032);
INSERT INTO cargo_shipments VALUES (4653109, 947553, 3, 33942, 70479, 331.0683367821418);
INSERT INTO cargo_shipments VALUES (7719866, 162305, 1, 48017, 64986, 859.0240844280485);
INSERT INTO cargo_shipments VALUES (7062396, 208914, 4, 13521, 69028, 126.79733484052014);
INSERT INTO cargo_shipments VALUES (5452653, 252642, 3, 3933, 37468, 702.4428278521594);
INSERT INTO cargo_shipments VALUES (6769760, 198178, 4, 21695, 85128, 290.1156740471411);
INSERT INTO cargo_shipments VALUES (3037109, 499982, 1, 40732, 31584, 617.6641698083008);
INSERT INTO cargo_shipments VALUES (8584768, 881607, 1, 34458, 85047, 742.4436081623872);
INSERT INTO cargo_shipments VALUES (8623401, 420661, 3, 27726, 97714, 27.244454518576376);
INSERT INTO cargo_shipments VALUES (8619183, 818559, 1, 45262, 41156, 887.0076940000014);
INSERT INTO cargo_shipments VALUES (6853316, 912153, 1, 9481, 73357, 148.0807854886046);
INSERT INTO cargo_shipments VALUES (1790383, 358509, 1, 24770, 36421, 281.6819649384931);
INSERT INTO cargo_shipments VALUES (6523782, 303778, 2, 20578, 64620, 985.7114036831651);
INSERT INTO cargo_shipments VALUES (4447018, 715589, 2, 19577, 68168, 4.531940715820304);
INSERT INTO cargo_shipments VALUES (6914301, 188334, 1, 17321, 43047, 296.79821870051006);
INSERT INTO cargo_shipments VALUES (2940604, 170058, 2, 1781, 49609, 511.21869840582326);
INSERT INTO cargo_shipments VALUES (9693423, 817972, 1, 8001, 68759, 660.3255684616603);
INSERT INTO cargo_shipments VALUES (4998829, 303778, 4, 46906, 69183, 939.4742107530858);
INSERT INTO cargo_shipments VALUES (3060577, 170058, 4, 6117, 74354, 866.9083402990539);
INSERT INTO cargo_shipments VALUES (4062158, 410976, 3, 11894, 16790, 425.5146989733285);
INSERT INTO cargo_shipments VALUES (2611738, 549043, 4, 32636, 48910, 159.61223843403238);
INSERT INTO cargo_shipments VALUES (2619433, 333386, 3, 34319, 42117, 698.7465358754921);
INSERT INTO cargo_shipments VALUES (6921433, 277004, 2, 22819, 27543, 305.7935777493805);
INSERT INTO cargo_shipments VALUES (1885555, 206705, 4, 19649, 65481, 203.96772105204585);
INSERT INTO cargo_shipments VALUES (4359085, 280979, 1, 47098, 28008, 740.8173686892942);
INSERT INTO cargo_shipments VALUES (8312000, 423320, 4, 43807, 30534, 938.1063144471415);
INSERT INTO cargo_shipments VALUES (3889957, 516350, 3, 6189, 31584, 605.0352299307783);
INSERT INTO cargo_shipments VALUES (9256468, 798926, 4, 34068, 35306, 707.151391612642);
INSERT INTO cargo_shipments VALUES (6289317, 469804, 3, 39558, 37865, 675.4165223858762);
INSERT INTO cargo_shipments VALUES (1626405, 965863, 4, 33307, 74533, 787.9691695009913);
INSERT INTO cargo_shipments VALUES (4255452, 978373, 4, 4818, 85128, 69.91467933446837);
INSERT INTO cargo_shipments VALUES (8936038, 249856, 1, 40714, 69183, 625.0770795958065);
INSERT INTO cargo_shipments VALUES (4120450, 614175, 4, 3820, 45027, 880.6618504069072);
INSERT INTO cargo_shipments VALUES (9789477, 208914, 1, 25966, 34263, 573.488707881237);
INSERT INTO cargo_shipments VALUES (6629540, 873886, 1, 17444, 20438, 555.9176803490543);
INSERT INTO cargo_shipments VALUES (9301903, 970881, 4, 5889, 78092, 897.0076284891321);
INSERT INTO cargo_shipments VALUES (4826976, 800049, 1, 13196, 23317, 256.2899552553547);
INSERT INTO cargo_shipments VALUES (9520657, 986540, 1, 18717, 49609, 204.94335629327398);
INSERT INTO cargo_shipments VALUES (5513077, 208914, 2, 43724, 11754, 925.6426082816406);
INSERT INTO cargo_shipments VALUES (1505319, 332039, 1, 35429, 95509, 704.6627383125);
INSERT INTO cargo_shipments VALUES (1818819, 811796, 3, 41260, 81696, 643.0910619612532);
INSERT INTO cargo_shipments VALUES (1631600, 932171, 4, 41274, 67867, 113.35475543102925);
INSERT INTO cargo_shipments VALUES (3880954, 373731, 4, 42555, 40856, 192.00276585400889);
INSERT INTO cargo_shipments VALUES (9431367, 724842, 4, 24294, 11178, 990.6863540182923);
INSERT INTO cargo_shipments VALUES (1916907, 410976, 4, 43190, 48241, 213.0851939402526);
INSERT INTO cargo_shipments VALUES (4358082, 272236, 3, 48304, 66620, 693.880254287388);
INSERT INTO cargo_shipments VALUES (9398364, 410976, 4, 32299, 48910, 603.5408502837117);
INSERT INTO cargo_shipments VALUES (5305004, 416286, 4, 44365, 36421, 516.7758900140058);
INSERT INTO cargo_shipments VALUES (7716916, 243675, 4, 47800, 78382, 451.3365245145433);
INSERT INTO cargo_shipments VALUES (3611267, 170058, 3, 11788, 81855, 432.7537833033562);
INSERT INTO cargo_shipments VALUES (5897995, 983159, 4, 6684, 90584, 897.1831719281614);
INSERT INTO cargo_shipments VALUES (5869193, 344503, 1, 49391, 43047, 600.3447540749866);
INSERT INTO cargo_shipments VALUES (9505227, 252987, 1, 3968, 21786, 705.870472262611);
INSERT INTO cargo_shipments VALUES (9766422, 115640, 3, 22485, 25806, 84.6079261448277);
INSERT INTO cargo_shipments VALUES (6179918, 511695, 4, 35394, 40203, 817.042250527918);
INSERT INTO cargo_shipments VALUES (9451400, 606002, 4, 49272, 37865, 964.0081585858702);
INSERT INTO cargo_shipments VALUES (1298852, 742858, 1, 35036, 71907, 426.31484703053104);
INSERT INTO cargo_shipments VALUES (2381364, 389460, 4, 39814, 69028, 538.9095250044211);
INSERT INTO cargo_shipments VALUES (6116152, 691232, 3, 24569, 12096, 384.6752582386519);
INSERT INTO cargo_shipments VALUES (1135848, 626357, 1, 25544, 29828, 931.9255628480206);
INSERT INTO cargo_shipments VALUES (9324545, 733236, 1, 24942, 45027, 331.7107422879485);
INSERT INTO cargo_shipments VALUES (8988945, 505917, 2, 39831, 34984, 737.1725024442703);
INSERT INTO cargo_shipments VALUES (8285227, 468598, 4, 17448, 26822, 445.7798521584497);
INSERT INTO cargo_shipments VALUES (4727787, 237199, 1, 40534, 41156, 429.4206835347294);
INSERT INTO cargo_shipments VALUES (3230744, 388326, 1, 45654, 26822, 52.19243045825639);
INSERT INTO cargo_shipments VALUES (8776603, 563327, 4, 2612, 81142, 216.8618095428544);
INSERT INTO cargo_shipments VALUES (2364165, 863835, 2, 21428, 26625, 953.6960788196787);
INSERT INTO cargo_shipments VALUES (9474437, 648680, 4, 15689, 14955, 208.44497572380826);
INSERT INTO cargo_shipments VALUES (5304115, 961801, 4, 29740, 88944, 94.21154385822628);
INSERT INTO cargo_shipments VALUES (4220934, 205227, 2, 47634, 43313, 172.7977152448461);
INSERT INTO cargo_shipments VALUES (9193180, 558558, 3, 8506, 29828, 37.43250981452961);
INSERT INTO cargo_shipments VALUES (9060697, 162259, 1, 10905, 70479, 371.20116233544366);
INSERT INTO cargo_shipments VALUES (1746073, 286320, 4, 8356, 30603, 389.1021906065687);
INSERT INTO cargo_shipments VALUES (7529400, 596094, 2, 4992, 20438, 283.7482357866955);
INSERT INTO cargo_shipments VALUES (6967097, 221189, 4, 39369, 16362, 854.9882599312722);
INSERT INTO cargo_shipments VALUES (4554138, 440172, 4, 49885, 85128, 845.8125389706881);
INSERT INTO cargo_shipments VALUES (5803786, 996823, 4, 11649, 63186, 849.3233125549489);
INSERT INTO cargo_shipments VALUES (9462359, 167992, 3, 6771, 26822, 677.1593564458132);
INSERT INTO cargo_shipments VALUES (7346569, 459081, 3, 17198, 36421, 888.0753780476992);
INSERT INTO cargo_shipments VALUES (8853330, 460185, 2, 49492, 32861, 993.5632384212637);
INSERT INTO cargo_shipments VALUES (2030772, 978373, 1, 43856, 84900, 712.6360311865755);
INSERT INTO cargo_shipments VALUES (2142944, 982169, 1, 36695, 37865, 195.82897795097787);
INSERT INTO cargo_shipments VALUES (2370131, 912883, 2, 2449, 41156, 773.6232883900809);
INSERT INTO cargo_shipments VALUES (1021832, 558558, 3, 28634, 47786, 7.087039464626632);
INSERT INTO cargo_shipments VALUES (3150920, 301962, 3, 34362, 43047, 948.5524822776833);
INSERT INTO cargo_shipments VALUES (7392953, 572361, 4, 19286, 83314, 797.2003332371426);
INSERT INTO cargo_shipments VALUES (3529331, 800049, 2, 8012, 67410, 760.5761765016246);
INSERT INTO cargo_shipments VALUES (1576416, 452680, 2, 34031, 34263, 195.37915541165384);
INSERT INTO cargo_shipments VALUES (3127741, 978373, 2, 9011, 95509, 292.2076298324622);
INSERT INTO cargo_shipments VALUES (1682252, 167992, 3, 37926, 70479, 98.36679749003518);
INSERT INTO cargo_shipments VALUES (6599443, 574095, 1, 12036, 81696, 305.20652592692966);
INSERT INTO cargo_shipments VALUES (8130629, 344503, 4, 8863, 64986, 84.8403380701236);
INSERT INTO cargo_shipments VALUES (1611215, 372577, 4, 22470, 48241, 51.11785523274026);
INSERT INTO cargo_shipments VALUES (6237223, 884599, 2, 22606, 40203, 539.4882587756);
INSERT INTO cargo_shipments VALUES (3436620, 675376, 1, 24447, 58358, 84.90292446573189);
INSERT INTO cargo_shipments VALUES (1345934, 373731, 1, 11432, 84900, 769.5919486114259);
INSERT INTO cargo_shipments VALUES (9549608, 735633, 1, 7206, 14388, 552.815690824366);
INSERT INTO cargo_shipments VALUES (3076172, 642804, 4, 20733, 69183, 244.75368520119355);
INSERT INTO cargo_shipments VALUES (3093040, 240695, 2, 35240, 28008, 817.7179475097054);
INSERT INTO cargo_shipments VALUES (6088817, 960137, 1, 23118, 67410, 339.8723812313662);
INSERT INTO cargo_shipments VALUES (1835865, 757508, 4, 40500, 38582, 366.40724346929545);
INSERT INTO cargo_shipments VALUES (5020813, 912883, 4, 2478, 73357, 939.9540382282214);
INSERT INTO cargo_shipments VALUES (6037645, 815933, 1, 15674, 53546, 537.7857589962919);
INSERT INTO cargo_shipments VALUES (8099765, 277004, 3, 6201, 89117, 125.69174844846953);
INSERT INTO cargo_shipments VALUES (3587643, 511881, 1, 47677, 95509, 777.091804196232);
INSERT INTO cargo_shipments VALUES (2660594, 206705, 2, 16384, 70479, 484.29440558505934);
INSERT INTO cargo_shipments VALUES (2573365, 703367, 3, 21371, 69183, 729.0306131065591);
INSERT INTO cargo_shipments VALUES (5116231, 775508, 3, 7356, 38582, 414.4812244140149);
INSERT INTO cargo_shipments VALUES (3467754, 128482, 4, 21070, 43313, 438.01548113198396);
INSERT INTO cargo_shipments VALUES (2940549, 543693, 4, 14179, 67867, 921.5339976295761);
INSERT INTO cargo_shipments VALUES (5028878, 783453, 4, 32313, 51067, 113.01187134264146);
INSERT INTO cargo_shipments VALUES (1792941, 556487, 4, 31033, 68759, 266.6531789533072);
INSERT INTO cargo_shipments VALUES (5883969, 962177, 2, 32932, 96914, 681.1782336453641);
INSERT INTO cargo_shipments VALUES (4039006, 521650, 3, 40250, 90584, 460.9333769050976);
INSERT INTO cargo_shipments VALUES (3563393, 205227, 4, 15558, 73315, 806.0592524814468);
INSERT INTO cargo_shipments VALUES (9209372, 884599, 3, 17669, 95504, 243.653877212064);
INSERT INTO cargo_shipments VALUES (5545633, 608026, 4, 20682, 64986, 978.9485726898298);
INSERT INTO cargo_shipments VALUES (6606330, 208914, 4, 1296, 47786, 811.5719756372159);
INSERT INTO cargo_shipments VALUES (3587971, 436083, 4, 1885, 34263, 270.0937406804732);
INSERT INTO cargo_shipments VALUES (6937691, 900209, 1, 27432, 18819, 452.56516539618565);
INSERT INTO cargo_shipments VALUES (2159673, 296521, 3, 45024, 84370, 425.11503081572556);
INSERT INTO cargo_shipments VALUES (3722823, 271117, 4, 6458, 32608, 931.0707303313161);
INSERT INTO cargo_shipments VALUES (1755549, 305108, 3, 47298, 61748, 543.5781380371131);
INSERT INTO cargo_shipments VALUES (9733744, 761485, 1, 23244, 22999, 853.5490150662408);
INSERT INTO cargo_shipments VALUES (9661583, 639436, 4, 22802, 42830, 189.5866635779755);
INSERT INTO cargo_shipments VALUES (3687415, 796715, 4, 40189, 74354, 624.8373057216695);
INSERT INTO cargo_shipments VALUES (4598322, 383901, 3, 11750, 73217, 184.85217770781358);
INSERT INTO cargo_shipments VALUES (5835865, 608026, 1, 31991, 76275, 894.2868808553209);
INSERT INTO cargo_shipments VALUES (4630783, 606002, 4, 33116, 38903, 182.80879594886258);
INSERT INTO cargo_shipments VALUES (8743298, 381311, 1, 20829, 63674, 715.0132710746592);
INSERT INTO cargo_shipments VALUES (3234138, 624235, 1, 47545, 38903, 947.6359043004497);
INSERT INTO cargo_shipments VALUES (2002177, 214523, 3, 8288, 73315, 250.1106825466468);
INSERT INTO cargo_shipments VALUES (6598324, 632084, 2, 29331, 75270, 812.7804780917364);
INSERT INTO cargo_shipments VALUES (1867505, 649000, 2, 17216, 66374, 215.87006113854622);
INSERT INTO cargo_shipments VALUES (8537150, 777504, 4, 37783, 35306, 227.34636471078574);
INSERT INTO cargo_shipments VALUES (9996787, 572301, 4, 35894, 80764, 325.5536089101139);
INSERT INTO cargo_shipments VALUES (2980204, 488373, 4, 45596, 63996, 783.8171186729277);
INSERT INTO cargo_shipments VALUES (2369549, 286148, 3, 18586, 31016, 806.710990217041);
INSERT INTO cargo_shipments VALUES (3195010, 389460, 3, 3692, 84370, 66.76753049793838);
INSERT INTO cargo_shipments VALUES (2790414, 208914, 1, 29478, 48435, 884.1474683897181);
INSERT INTO cargo_shipments VALUES (2367531, 272236, 4, 47420, 41503, 946.7804099234215);
INSERT INTO cargo_shipments VALUES (1597828, 607042, 1, 8890, 73217, 476.4366477539289);
INSERT INTO cargo_shipments VALUES (5702243, 253595, 3, 27659, 97714, 801.610480638045);
INSERT INTO cargo_shipments VALUES (2586574, 380476, 3, 35774, 64986, 949.4373572386169);
INSERT INTO cargo_shipments VALUES (7736634, 410976, 3, 45901, 66374, 626.7197192127704);
INSERT INTO cargo_shipments VALUES (2400429, 708028, 1, 28621, 67410, 10.66287380571762);
INSERT INTO cargo_shipments VALUES (6581095, 867984, 1, 1108, 54150, 620.6544045919998);
INSERT INTO cargo_shipments VALUES (6117905, 170058, 1, 27079, 45027, 112.54971623112131);
INSERT INTO cargo_shipments VALUES (6542775, 259977, 3, 39480, 63674, 383.4052622146733);
INSERT INTO cargo_shipments VALUES (5992804, 997802, 3, 42111, 37665, 928.227302460656);
INSERT INTO cargo_shipments VALUES (1914142, 346445, 3, 44085, 54150, 340.36850787442086);
INSERT INTO cargo_shipments VALUES (7528175, 422404, 1, 26374, 66620, 73.68583506146587);
INSERT INTO cargo_shipments VALUES (8692888, 513506, 4, 29193, 28008, 845.4326800835576);
INSERT INTO cargo_shipments VALUES (3801343, 208914, 4, 36535, 31694, 509.6545496115901);
INSERT INTO cargo_shipments VALUES (6476481, 127089, 4, 29656, 88944, 240.43266574522727);
INSERT INTO cargo_shipments VALUES (4198991, 787243, 3, 9827, 20438, 439.04114989485157);
INSERT INTO cargo_shipments VALUES (2623209, 881607, 1, 9510, 95509, 907.5186444436014);
INSERT INTO cargo_shipments VALUES (4547120, 249856, 3, 22737, 95020, 418.2574613524863);
INSERT INTO cargo_shipments VALUES (8520137, 374226, 1, 23404, 27794, 723.5335169562496);
INSERT INTO cargo_shipments VALUES (3649336, 154036, 1, 22294, 75571, 470.7574764258691);
INSERT INTO cargo_shipments VALUES (1642240, 511695, 2, 5850, 55608, 409.8254653142913);
INSERT INTO cargo_shipments VALUES (5167613, 253595, 4, 17209, 37665, 205.39930078563518);
INSERT INTO cargo_shipments VALUES (9815992, 614175, 3, 44285, 41503, 299.35883820567943);
INSERT INTO cargo_shipments VALUES (2746349, 811796, 3, 46303, 56046, 22.956835916359775);
INSERT INTO cargo_shipments VALUES (5841374, 910151, 4, 26903, 87478, 883.2572968003194);
INSERT INTO cargo_shipments VALUES (8859680, 634792, 1, 29918, 51197, 595.3044425000118);
INSERT INTO cargo_shipments VALUES (6842185, 970881, 1, 29691, 14955, 350.6294279862342);
INSERT INTO cargo_shipments VALUES (4440278, 632283, 3, 13853, 25806, 347.85986812649725);
INSERT INTO cargo_shipments VALUES (3927468, 801924, 2, 32207, 11136, 647.8480640328227);
INSERT INTO cargo_shipments VALUES (4292636, 224194, 3, 1293, 97714, 359.0065133726282);
INSERT INTO cargo_shipments VALUES (3097331, 601323, 4, 33393, 96914, 892.6533682524274);
INSERT INTO cargo_shipments VALUES (2140160, 866524, 2, 39756, 67067, 258.5143865414897);
INSERT INTO cargo_shipments VALUES (9704503, 817972, 3, 27601, 26822, 574.5725334787331);
INSERT INTO cargo_shipments VALUES (9302745, 965863, 1, 4519, 13533, 58.24732700633373);
INSERT INTO cargo_shipments VALUES (7499431, 415908, 1, 40503, 20032, 170.97205705171547);
INSERT INTO cargo_shipments VALUES (9573835, 413138, 3, 45028, 64620, 60.05497919685454);
INSERT INTO cargo_shipments VALUES (3507532, 224194, 3, 20563, 64620, 235.8262342012668);
INSERT INTO cargo_shipments VALUES (6332032, 969502, 4, 46561, 95504, 428.09407451219397);
INSERT INTO cargo_shipments VALUES (5598936, 516350, 2, 49127, 73217, 642.0307442087488);
INSERT INTO cargo_shipments VALUES (5281131, 633887, 3, 28493, 95509, 523.1471075943246);
INSERT INTO cargo_shipments VALUES (2557988, 452680, 3, 25563, 37665, 739.6674804195109);
INSERT INTO cargo_shipments VALUES (7195926, 469804, 1, 20927, 67867, 649.9354414971826);
INSERT INTO cargo_shipments VALUES (4805913, 837435, 2, 2231, 22957, 440.1167349515603);
INSERT INTO cargo_shipments VALUES (6170916, 867984, 2, 47559, 12096, 137.63180881239455);
INSERT INTO cargo_shipments VALUES (6439152, 633887, 2, 31820, 95509, 669.4020859141744);
INSERT INTO cargo_shipments VALUES (3930394, 896822, 3, 4829, 64986, 91.12665001474474);
INSERT INTO cargo_shipments VALUES (6171033, 703920, 1, 9945, 97714, 864.9129901793697);
INSERT INTO cargo_shipments VALUES (7386918, 511695, 2, 33232, 26625, 838.1543374585218);
INSERT INTO cargo_shipments VALUES (9686336, 120546, 4, 14274, 99845, 175.25243019424164);
INSERT INTO cargo_shipments VALUES (8206030, 170058, 3, 43149, 29828, 233.6881395453635);
INSERT INTO cargo_shipments VALUES (9384903, 969502, 4, 41022, 38903, 187.00433960728836);
INSERT INTO cargo_shipments VALUES (4072395, 775508, 2, 20223, 23317, 392.4473761021345);
INSERT INTO cargo_shipments VALUES (5385129, 563327, 3, 43049, 26822, 906.460515246191);
INSERT INTO cargo_shipments VALUES (6496936, 855102, 3, 24356, 41503, 436.44743614610536);
INSERT INTO cargo_shipments VALUES (7808310, 567241, 1, 28148, 16362, 761.7777156336263);
INSERT INTO cargo_shipments VALUES (7430904, 777504, 2, 21456, 95020, 234.54719681841584);
INSERT INTO cargo_shipments VALUES (4646218, 595048, 3, 4866, 45027, 583.7122167520872);
INSERT INTO cargo_shipments VALUES (7590630, 519747, 1, 33568, 17145, 410.2750493705947);
INSERT INTO cargo_shipments VALUES (2078553, 208914, 2, 21779, 73217, 81.79233296055743);
INSERT INTO cargo_shipments VALUES (3781559, 226404, 2, 21358, 34263, 28.23110289595665);
INSERT INTO cargo_shipments VALUES (3850071, 572361, 3, 12095, 11136, 143.78400648900114);
INSERT INTO cargo_shipments VALUES (7938562, 167992, 1, 27066, 78897, 799.579556103815);
INSERT INTO cargo_shipments VALUES (4265995, 675298, 4, 9314, 11056, 783.8141477335689);
INSERT INTO cargo_shipments VALUES (1488315, 125992, 1, 40332, 37865, 454.7868805185863);
INSERT INTO cargo_shipments VALUES (2223200, 206705, 4, 47626, 32861, 614.9737318740539);
INSERT INTO cargo_shipments VALUES (2059343, 675376, 4, 48668, 41156, 490.3724708392676);
INSERT INTO cargo_shipments VALUES (9161222, 251268, 3, 8185, 77171, 906.8283374454072);
INSERT INTO cargo_shipments VALUES (9599548, 700177, 3, 17701, 29828, 399.08853012647626);
INSERT INTO cargo_shipments VALUES (5870134, 507134, 2, 25443, 23317, 373.93135858782534);
INSERT INTO cargo_shipments VALUES (9299769, 732193, 4, 22744, 17145, 496.9681138487437);
INSERT INTO cargo_shipments VALUES (4801531, 380476, 4, 1938, 69973, 982.8624847022472);
INSERT INTO cargo_shipments VALUES (8774142, 191136, 2, 22912, 25806, 849.4742089090989);
INSERT INTO cargo_shipments VALUES (4229943, 468598, 1, 17958, 61748, 89.21769412723035);
INSERT INTO cargo_shipments VALUES (8454581, 381311, 3, 27142, 77171, 737.9822732016754);
INSERT INTO cargo_shipments VALUES (6402527, 420661, 3, 33652, 81855, 555.7221277001082);
INSERT INTO cargo_shipments VALUES (4562699, 678301, 4, 35767, 55608, 360.0168859108972);
INSERT INTO cargo_shipments VALUES (8132420, 614681, 4, 23870, 16362, 251.55887441761826);
INSERT INTO cargo_shipments VALUES (4683020, 105533, 3, 31133, 78897, 913.7653345132271);
INSERT INTO cargo_shipments VALUES (5574137, 188334, 1, 19456, 41156, 426.42561266891323);
INSERT INTO cargo_shipments VALUES (4149584, 595048, 2, 1470, 73217, 74.16124879585185);
INSERT INTO cargo_shipments VALUES (4664091, 134231, 1, 34160, 17062, 587.3832901216251);
INSERT INTO cargo_shipments VALUES (9977508, 866524, 4, 16586, 69183, 38.78268423284703);
INSERT INTO cargo_shipments VALUES (4512992, 599483, 2, 32630, 74533, 685.5840982311671);
INSERT INTO cargo_shipments VALUES (4690848, 372577, 1, 20674, 11754, 126.2734436230517);
INSERT INTO cargo_shipments VALUES (3829162, 389460, 4, 27642, 32518, 135.40044678572883);
INSERT INTO cargo_shipments VALUES (7811353, 817972, 2, 40496, 41503, 449.56383291429415);
INSERT INTO cargo_shipments VALUES (1316697, 413138, 1, 32735, 36421, 494.52154179794695);
INSERT INTO cargo_shipments VALUES (9203008, 519222, 4, 27108, 53546, 888.7809364767002);
INSERT INTO cargo_shipments VALUES (5465617, 643092, 4, 27136, 73357, 357.1768546100588);
INSERT INTO cargo_shipments VALUES (5393748, 511695, 2, 43733, 25806, 208.55780332130192);
INSERT INTO cargo_shipments VALUES (5418561, 798926, 4, 48494, 31584, 216.85915022655865);
INSERT INTO cargo_shipments VALUES (9348426, 745173, 2, 3813, 35306, 560.9636400608819);
INSERT INTO cargo_shipments VALUES (9619339, 413138, 2, 31190, 63996, 922.1213985716748);
INSERT INTO cargo_shipments VALUES (5138833, 422404, 2, 14154, 67067, 382.2508906081915);
INSERT INTO cargo_shipments VALUES (4954967, 181455, 4, 22282, 85128, 102.26120232674585);
INSERT INTO cargo_shipments VALUES (1002053, 133800, 4, 23651, 81142, 562.9571894490509);
INSERT INTO cargo_shipments VALUES (8402749, 787243, 2, 35103, 35881, 388.59988243449715);
INSERT INTO cargo_shipments VALUES (8499354, 286320, 1, 10056, 21481, 997.9723558324285);
INSERT INTO cargo_shipments VALUES (7707957, 511881, 2, 24646, 50739, 503.5434903341154);
INSERT INTO cargo_shipments VALUES (9560170, 572361, 3, 12729, 48435, 466.884966787296);
INSERT INTO cargo_shipments VALUES (4630102, 952614, 3, 48648, 75270, 714.1063484254555);
INSERT INTO cargo_shipments VALUES (5873798, 296521, 3, 33303, 55037, 807.0121267945232);
INSERT INTO cargo_shipments VALUES (1933789, 286148, 2, 23081, 67867, 450.39768898427434);
INSERT INTO cargo_shipments VALUES (5296707, 777504, 3, 26128, 11056, 501.75527434728406);
INSERT INTO cargo_shipments VALUES (6471352, 280979, 1, 22634, 78382, 343.62738201182555);
INSERT INTO cargo_shipments VALUES (3448069, 986540, 1, 13810, 70479, 475.2957848049134);
INSERT INTO cargo_shipments VALUES (3053867, 845815, 1, 16853, 41156, 154.93688321037158);
INSERT INTO cargo_shipments VALUES (7161412, 155883, 4, 24675, 11754, 888.2787891289589);
INSERT INTO cargo_shipments VALUES (8876587, 544464, 1, 8227, 38582, 244.16561316080987);
INSERT INTO cargo_shipments VALUES (1591519, 910151, 2, 16807, 12096, 686.0187543833333);
INSERT INTO cargo_shipments VALUES (2979781, 572228, 1, 21220, 25806, 273.9917726471697);
INSERT INTO cargo_shipments VALUES (1338448, 970601, 2, 28891, 17145, 82.63732980890926);
INSERT INTO cargo_shipments VALUES (7217752, 474492, 1, 1804, 32861, 875.1934580433966);
INSERT INTO cargo_shipments VALUES (6123993, 469053, 3, 12084, 51067, 114.09737379887075);
INSERT INTO cargo_shipments VALUES (3721890, 505917, 3, 4883, 63674, 437.6578955956757);
INSERT INTO cargo_shipments VALUES (9622020, 252642, 1, 26723, 50739, 157.62811207791384);
INSERT INTO cargo_shipments VALUES (9715385, 352768, 1, 37503, 32518, 837.5301894456);
INSERT INTO cargo_shipments VALUES (3536731, 170058, 2, 44536, 37865, 330.25972402865233);
INSERT INTO cargo_shipments VALUES (2070576, 440172, 4, 43967, 95504, 814.4995899788905);
INSERT INTO cargo_shipments VALUES (6255095, 691232, 4, 46044, 49609, 613.1933867656257);
INSERT INTO cargo_shipments VALUES (8853080, 725323, 2, 24955, 11754, 313.7982764239584);
INSERT INTO cargo_shipments VALUES (7267031, 381311, 2, 48463, 42830, 85.14873612158557);
INSERT INTO cargo_shipments VALUES (5323051, 555899, 1, 14619, 68759, 497.476313539876);
INSERT INTO cargo_shipments VALUES (6558026, 776366, 4, 37943, 97714, 369.247906431872);
INSERT INTO cargo_shipments VALUES (9008080, 692445, 1, 26026, 85128, 617.0763243341001);
INSERT INTO cargo_shipments VALUES (1134391, 601323, 2, 4440, 11056, 640.1509818098515);
INSERT INTO cargo_shipments VALUES (4591472, 677816, 2, 26039, 75270, 794.9381960922383);
INSERT INTO cargo_shipments VALUES (1920337, 181455, 1, 47594, 75571, 972.9216686647777);
INSERT INTO cargo_shipments VALUES (3137542, 643092, 4, 27073, 63996, 307.2473584954888);
INSERT INTO cargo_shipments VALUES (1246812, 608026, 2, 5726, 11056, 111.18430818975168);
INSERT INTO cargo_shipments VALUES (5302778, 677816, 2, 30961, 85047, 646.7222082571152);
INSERT INTO cargo_shipments VALUES (7379876, 415908, 4, 11587, 84370, 631.3729442570724);
INSERT INTO cargo_shipments VALUES (4655023, 188334, 3, 25757, 30603, 912.4821523952816);
INSERT INTO cargo_shipments VALUES (4823858, 932171, 4, 30832, 55608, 826.3007037479871);
INSERT INTO cargo_shipments VALUES (7604893, 206705, 1, 6942, 31584, 979.3200930349813);
INSERT INTO cargo_shipments VALUES (2674722, 782915, 4, 14396, 31016, 996.9017117210179);
INSERT INTO cargo_shipments VALUES (8857617, 155883, 1, 19991, 89117, 14.97358332527976);
INSERT INTO cargo_shipments VALUES (7214428, 735633, 1, 17845, 61748, 385.04366630198626);
INSERT INTO cargo_shipments VALUES (4732508, 305108, 1, 24086, 90584, 707.7723549508727);
INSERT INTO cargo_shipments VALUES (5718664, 420661, 1, 24500, 73315, 615.6157221175953);
INSERT INTO cargo_shipments VALUES (4536001, 978373, 2, 10060, 73217, 171.77256453651756);
INSERT INTO cargo_shipments VALUES (6418517, 181455, 4, 46679, 87094, 70.98822586790399);
INSERT INTO cargo_shipments VALUES (1014638, 633887, 1, 38261, 37468, 110.71347005712906);
INSERT INTO cargo_shipments VALUES (1870275, 127089, 3, 3466, 50739, 930.7675314113068);
INSERT INTO cargo_shipments VALUES (1285782, 574737, 2, 35787, 81142, 462.42876122914487);
INSERT INTO cargo_shipments VALUES (2821708, 800049, 1, 21472, 51067, 658.8066157569402);
INSERT INTO cargo_shipments VALUES (7438811, 422404, 1, 10378, 65481, 996.5315406534396);
INSERT INTO cargo_shipments VALUES (7338391, 912153, 3, 8442, 96914, 576.8096539669756);
INSERT INTO cargo_shipments VALUES (9433735, 990233, 2, 30691, 69028, 674.1866753407542);
INSERT INTO cargo_shipments VALUES (7284048, 380476, 4, 27166, 50739, 879.7143774218138);
INSERT INTO cargo_shipments VALUES (2890889, 176495, 2, 12101, 74533, 469.88284855634123);
INSERT INTO cargo_shipments VALUES (1086623, 932118, 2, 5746, 31584, 309.1506724730277);
INSERT INTO cargo_shipments VALUES (4001053, 511881, 4, 1535, 69973, 897.7798807093186);
INSERT INTO cargo_shipments VALUES (1797729, 120546, 3, 21094, 68168, 434.7683205963423);
INSERT INTO cargo_shipments VALUES (2793759, 519222, 1, 22917, 30534, 290.07360106220835);
INSERT INTO cargo_shipments VALUES (1308487, 250822, 1, 9031, 32861, 815.4685262057889);
INSERT INTO cargo_shipments VALUES (9732183, 544464, 4, 16513, 93319, 559.7961374155019);
INSERT INTO cargo_shipments VALUES (8507832, 397201, 1, 40189, 20438, 179.85732193777494);
INSERT INTO cargo_shipments VALUES (9669651, 875193, 2, 36748, 45027, 816.639999582308);
INSERT INTO cargo_shipments VALUES (1694286, 632283, 4, 3896, 46879, 361.77576372329423);
INSERT INTO cargo_shipments VALUES (3332771, 128482, 3, 49149, 68168, 727.4886559283478);
INSERT INTO cargo_shipments VALUES (1230058, 445027, 2, 17396, 74354, 812.7963404979039);
INSERT INTO cargo_shipments VALUES (8217560, 117052, 3, 31164, 67867, 254.43048293289328);
INSERT INTO cargo_shipments VALUES (9486347, 271976, 2, 22535, 32518, 31.102256493284575);
INSERT INTO cargo_shipments VALUES (8522294, 214523, 1, 13549, 48435, 818.1332866506128);
INSERT INTO cargo_shipments VALUES (8560924, 758628, 2, 15514, 69183, 811.0605934117507);
INSERT INTO cargo_shipments VALUES (2142043, 250822, 1, 11551, 47786, 819.1772769771153);
INSERT INTO cargo_shipments VALUES (8913781, 859274, 4, 16869, 41503, 346.5959485182191);
INSERT INTO cargo_shipments VALUES (2701900, 544464, 2, 1186, 88059, 135.87746123703016);
INSERT INTO cargo_shipments VALUES (3340498, 223703, 4, 13333, 18630, 371.66942600362887);
INSERT INTO cargo_shipments VALUES (3730512, 381311, 1, 15754, 47786, 925.1343418539079);
INSERT INTO cargo_shipments VALUES (9004421, 675298, 2, 28354, 14955, 867.5456299374398);
INSERT INTO cargo_shipments VALUES (2450479, 286148, 3, 24212, 52750, 627.704301863811);
INSERT INTO cargo_shipments VALUES (3197855, 271976, 4, 41851, 69028, 447.43885937832107);
INSERT INTO cargo_shipments VALUES (7939264, 776981, 3, 10099, 66374, 826.2084987658116);
INSERT INTO cargo_shipments VALUES (3085333, 743993, 1, 44338, 66975, 303.6418648833119);
INSERT INTO cargo_shipments VALUES (6615506, 743993, 4, 27237, 35963, 122.33334254060479);
INSERT INTO cargo_shipments VALUES (8200118, 660589, 1, 33538, 85047, 840.3477086753063);
INSERT INTO cargo_shipments VALUES (4434408, 998327, 3, 17253, 23317, 414.2947723752378);
INSERT INTO cargo_shipments VALUES (4844046, 896822, 3, 15819, 63996, 454.2028701923292);
INSERT INTO cargo_shipments VALUES (7793116, 286320, 3, 8101, 47786, 468.4355272894124);
INSERT INTO cargo_shipments VALUES (1287357, 127089, 3, 46445, 50739, 503.0235720918453);
INSERT INTO cargo_shipments VALUES (4749622, 700177, 1, 43725, 40856, 760.9222869449576);
INSERT INTO cargo_shipments VALUES (7670805, 988636, 4, 12254, 66975, 545.1145013727527);
INSERT INTO cargo_shipments VALUES (4823779, 397201, 1, 18965, 68168, 996.7793119959127);
INSERT INTO cargo_shipments VALUES (2001761, 986540, 1, 48632, 95504, 709.568957494196);
INSERT INTO cargo_shipments VALUES (8516737, 633887, 2, 25570, 40856, 173.5968959838745);
INSERT INTO cargo_shipments VALUES (2074061, 389460, 1, 47259, 13533, 457.256162001871);
INSERT INTO cargo_shipments VALUES (2901903, 373731, 2, 11238, 31584, 666.5906726279898);
INSERT INTO cargo_shipments VALUES (3565886, 983159, 4, 6919, 63996, 144.3773220630251);
INSERT INTO cargo_shipments VALUES (3684914, 200324, 3, 6454, 64986, 730.733357527567);
INSERT INTO cargo_shipments VALUES (4128143, 267291, 1, 8840, 32608, 473.8423867226832);
INSERT INTO cargo_shipments VALUES (2559415, 678301, 1, 15633, 58358, 393.18520324679895);
INSERT INTO cargo_shipments VALUES (5912195, 422404, 4, 42052, 65481, 607.0177558375862);
INSERT INTO cargo_shipments VALUES (4206396, 912883, 1, 6976, 19125, 235.68449526378177);
INSERT INTO cargo_shipments VALUES (1242966, 712175, 2, 7132, 48910, 951.4496050536148);
INSERT INTO cargo_shipments VALUES (4291691, 912153, 1, 48280, 25571, 86.10332850601587);
INSERT INTO cargo_shipments VALUES (5284199, 932118, 3, 20921, 14955, 485.61091605538553);
INSERT INTO cargo_shipments VALUES (3703029, 142736, 1, 37280, 46879, 125.60212800529413);
INSERT INTO cargo_shipments VALUES (9663263, 900209, 3, 34259, 73315, 787.0113544236201);
INSERT INTO cargo_shipments VALUES (5831411, 445027, 2, 3363, 88944, 864.9264038673205);
INSERT INTO cargo_shipments VALUES (3433519, 642804, 4, 41076, 35881, 599.6300368059766);
INSERT INTO cargo_shipments VALUES (1282861, 708028, 3, 43813, 53546, 231.1665389756452);
INSERT INTO cargo_shipments VALUES (8627619, 230833, 2, 25948, 49609, 115.81391171810395);
INSERT INTO cargo_shipments VALUES (5135476, 996048, 1, 39195, 81142, 493.09581027085324);
INSERT INTO cargo_shipments VALUES (3427097, 667591, 1, 11800, 20032, 249.0085714749467);
INSERT INTO cargo_shipments VALUES (9005034, 998327, 2, 36795, 87624, 328.4147806317121);
INSERT INTO cargo_shipments VALUES (7126762, 648680, 3, 45000, 11136, 733.8003069556912);
INSERT INTO cargo_shipments VALUES (1339119, 191136, 2, 44923, 68168, 171.5001885272833);
INSERT INTO cargo_shipments VALUES (1349405, 983159, 4, 26701, 66620, 858.8023044250141);
INSERT INTO cargo_shipments VALUES (2662758, 818559, 4, 46976, 69028, 31.667843768830473);
INSERT INTO cargo_shipments VALUES (8483078, 499701, 2, 47774, 64620, 397.82871389857456);
INSERT INTO cargo_shipments VALUES (2565337, 615277, 3, 23380, 22957, 893.8889583165876);
INSERT INTO cargo_shipments VALUES (3338480, 469053, 4, 9929, 37865, 467.66258399789496);
INSERT INTO cargo_shipments VALUES (9662040, 507134, 1, 46367, 78897, 278.099549472822);
INSERT INTO cargo_shipments VALUES (8135569, 543693, 4, 39868, 41503, 334.2769372792242);
INSERT INTO cargo_shipments VALUES (8179207, 986540, 2, 34914, 74354, 587.5057788705808);
INSERT INTO cargo_shipments VALUES (6775911, 798926, 1, 38726, 35306, 858.9057500660014);
INSERT INTO cargo_shipments VALUES (3041189, 267291, 4, 8158, 13533, 631.2162357017711);
INSERT INTO cargo_shipments VALUES (9524246, 564266, 2, 1836, 47786, 934.2992847372353);
INSERT INTO cargo_shipments VALUES (6552146, 667591, 4, 8202, 38903, 206.60472080454872);
INSERT INTO cargo_shipments VALUES (2763168, 947553, 2, 42703, 37665, 479.3358497454965);
INSERT INTO cargo_shipments VALUES (1391649, 188334, 4, 41808, 71907, 480.38165851948725);
INSERT INTO cargo_shipments VALUES (1762801, 758072, 4, 32046, 95020, 58.13050842477552);
INSERT INTO cargo_shipments VALUES (2352155, 383901, 3, 25843, 13552, 778.7717545569132);
INSERT INTO cargo_shipments VALUES (5278458, 253595, 2, 29472, 78897, 320.23910049309654);
INSERT INTO cargo_shipments VALUES (4427798, 340760, 4, 26470, 29828, 318.23301233257206);
INSERT INTO cargo_shipments VALUES (6669780, 649000, 2, 48185, 95020, 904.2125474079173);
INSERT INTO cargo_shipments VALUES (9237188, 353497, 4, 35841, 88944, 417.8455974118024);
INSERT INTO cargo_shipments VALUES (3616820, 445027, 3, 5555, 69183, 73.37136017515955);
INSERT INTO cargo_shipments VALUES (6735914, 438748, 3, 21430, 81142, 923.3665711711773);
INSERT INTO cargo_shipments VALUES (3819244, 914016, 4, 22249, 25806, 894.290101992894);
INSERT INTO cargo_shipments VALUES (8627211, 817972, 2, 23835, 26625, 157.00068353476436);
INSERT INTO cargo_shipments VALUES (8905816, 322704, 4, 14856, 29828, 924.4568069953116);
INSERT INTO cargo_shipments VALUES (4553598, 446354, 1, 36900, 22957, 872.8903769978864);
INSERT INTO cargo_shipments VALUES (2698681, 167992, 3, 14945, 80764, 622.6875964696317);
INSERT INTO cargo_shipments VALUES (3860656, 782915, 3, 30090, 88944, 509.34707485811504);
INSERT INTO cargo_shipments VALUES (1901491, 970881, 4, 26343, 50739, 717.5272310343958);
INSERT INTO cargo_shipments VALUES (1421647, 667591, 3, 11562, 87094, 104.32861292472029);
INSERT INTO cargo_shipments VALUES (2792870, 303778, 1, 40268, 58358, 479.4737058415798);
INSERT INTO cargo_shipments VALUES (7746079, 372577, 4, 4750, 87478, 743.2021588701067);
INSERT INTO cargo_shipments VALUES (3630582, 397201, 4, 18687, 76275, 34.779531650090064);
INSERT INTO cargo_shipments VALUES (8139478, 792511, 2, 36020, 35306, 940.4330059805899);
INSERT INTO cargo_shipments VALUES (1700957, 800049, 1, 16629, 71907, 51.38758536645038);
INSERT INTO cargo_shipments VALUES (2859509, 606002, 2, 15421, 35306, 123.3949218860887);
INSERT INTO cargo_shipments VALUES (9859368, 212978, 4, 7210, 81696, 249.32249442597555);
INSERT INTO cargo_shipments VALUES (5837086, 776366, 4, 39366, 31694, 406.2730117033556);
INSERT INTO cargo_shipments VALUES (7210589, 761485, 1, 32111, 76275, 584.8296714821522);
INSERT INTO cargo_shipments VALUES (5060703, 631650, 3, 9849, 81142, 457.33737779912207);
INSERT INTO cargo_shipments VALUES (4904201, 643092, 2, 46013, 27543, 935.0820933782488);
INSERT INTO cargo_shipments VALUES (6889764, 970881, 3, 42441, 13533, 982.239775538563);
INSERT INTO cargo_shipments VALUES (2303896, 878046, 2, 13041, 85047, 967.0051411760243);
INSERT INTO cargo_shipments VALUES (3073560, 519222, 2, 9287, 11136, 147.03915786566213);
INSERT INTO cargo_shipments VALUES (4579258, 134231, 2, 30721, 83314, 451.3618090446345);
INSERT INTO cargo_shipments VALUES (2967146, 206705, 1, 41749, 52750, 288.7120400249755);
INSERT INTO cargo_shipments VALUES (9275729, 962177, 4, 7084, 63996, 326.0321687028512);
INSERT INTO cargo_shipments VALUES (7820532, 563327, 2, 12019, 21481, 469.66609356836443);
INSERT INTO cargo_shipments VALUES (2832496, 191136, 3, 22551, 88059, 52.41833376646821);
INSERT INTO cargo_shipments VALUES (2025397, 678301, 4, 36000, 20032, 592.2778114880158);
INSERT INTO cargo_shipments VALUES (1651456, 703920, 2, 12178, 38582, 2.4681012213852993);
INSERT INTO cargo_shipments VALUES (7352558, 301037, 4, 16549, 22957, 764.21863836923);
INSERT INTO cargo_shipments VALUES (4489497, 703920, 3, 3493, 85128, 205.0989765027359);
INSERT INTO cargo_shipments VALUES (6871910, 884599, 2, 28610, 73217, 733.3608291340165);
INSERT INTO cargo_shipments VALUES (3238261, 125992, 4, 41618, 32608, 658.6240690932646);
INSERT INTO cargo_shipments VALUES (1308385, 358509, 1, 45230, 78092, 464.5403042876347);
INSERT INTO cargo_shipments VALUES (6977692, 900209, 4, 7626, 63674, 252.0911540112918);
INSERT INTO cargo_shipments VALUES (9636534, 162259, 3, 38335, 17145, 59.40099553624656);
INSERT INTO cargo_shipments VALUES (6003423, 777504, 3, 43090, 31584, 413.99049514010835);
INSERT INTO cargo_shipments VALUES (7126088, 170058, 3, 28005, 74735, 680.8107896559337);
INSERT INTO cargo_shipments VALUES (3301281, 251268, 3, 22045, 17145, 834.3219007564007);
INSERT INTO cargo_shipments VALUES (2833821, 415908, 2, 44272, 27543, 856.936349025741);
INSERT INTO cargo_shipments VALUES (9808291, 162305, 3, 41370, 71907, 385.0208753828356);
INSERT INTO cargo_shipments VALUES (1588372, 259977, 2, 40682, 95020, 349.68460145366123);
INSERT INTO cargo_shipments VALUES (6879119, 127089, 4, 25483, 35306, 515.4286467194465);
INSERT INTO cargo_shipments VALUES (7919908, 226404, 3, 36019, 87624, 62.425861906167725);
INSERT INTO cargo_shipments VALUES (1973477, 460185, 4, 11633, 56046, 596.8642548180994);
INSERT INTO cargo_shipments VALUES (3366933, 692445, 3, 15512, 46879, 314.1678186289728);
INSERT INTO cargo_shipments VALUES (5267695, 519747, 1, 44395, 74533, 158.56871409227736);
INSERT INTO cargo_shipments VALUES (1938390, 388326, 1, 6874, 45027, 387.20203584406977);
INSERT INTO cargo_shipments VALUES (6300806, 732193, 2, 6562, 67410, 882.8519533644393);
INSERT INTO cargo_shipments VALUES (8062892, 733236, 3, 26260, 50739, 924.913779819164);
INSERT INTO cargo_shipments VALUES (9687618, 438748, 2, 26583, 35881, 458.2230633670799);
INSERT INTO cargo_shipments VALUES (5835104, 383901, 2, 5604, 34263, 244.38637564832632);
INSERT INTO cargo_shipments VALUES (5119184, 634792, 1, 42278, 48241, 660.1536933859767);
INSERT INTO cargo_shipments VALUES (4164332, 675298, 3, 38198, 78897, 475.3477203866061);
INSERT INTO cargo_shipments VALUES (5517282, 333621, 1, 12937, 48910, 149.54274864328488);
INSERT INTO cargo_shipments VALUES (3954348, 397201, 4, 17590, 52750, 920.894288213455);
INSERT INTO cargo_shipments VALUES (3912205, 626357, 4, 49857, 42117, 460.1493228834883);
INSERT INTO cargo_shipments VALUES (5718322, 962177, 2, 18493, 43047, 226.3014877255225);
INSERT INTO cargo_shipments VALUES (4990777, 128482, 1, 33940, 22999, 710.8504755389947);
INSERT INTO cargo_shipments VALUES (5829681, 415908, 4, 48696, 19125, 132.7153891272471);
INSERT INTO cargo_shipments VALUES (1552750, 859274, 1, 2372, 34263, 632.5039405029545);
INSERT INTO cargo_shipments VALUES (6898921, 692445, 2, 22960, 77171, 454.76835553688886);
INSERT INTO cargo_shipments VALUES (5909483, 296521, 3, 36377, 89117, 10.276403564885461);
INSERT INTO cargo_shipments VALUES (5727480, 223703, 3, 43660, 68759, 339.2486668135457);
INSERT INTO cargo_shipments VALUES (4698357, 574095, 1, 6861, 48435, 851.0316202977111);
INSERT INTO cargo_shipments VALUES (5308397, 226404, 2, 22764, 53546, 525.0182606105731);
INSERT INTO cargo_shipments VALUES (5338739, 631650, 2, 46595, 58401, 364.4012095909813);
INSERT INTO cargo_shipments VALUES (7060109, 237199, 1, 21707, 18819, 406.348261818537);
INSERT INTO cargo_shipments VALUES (8453276, 611911, 2, 46317, 87624, 258.74264365780766);
INSERT INTO cargo_shipments VALUES (3980311, 203567, 4, 26024, 32518, 92.51044785150087);
INSERT INTO cargo_shipments VALUES (6122388, 460185, 4, 20127, 21786, 475.84259419432186);
INSERT INTO cargo_shipments VALUES (7017362, 655713, 1, 17235, 70479, 309.9970408998836);
INSERT INTO cargo_shipments VALUES (8608013, 533295, 1, 3392, 73357, 639.0344459167316);
INSERT INTO cargo_shipments VALUES (7328189, 499701, 3, 6067, 37665, 612.3986072309124);
INSERT INTO cargo_shipments VALUES (6297312, 626357, 3, 46278, 76275, 860.6232767836375);
INSERT INTO cargo_shipments VALUES (6283010, 114429, 3, 49748, 88944, 846.4893858766255);
INSERT INTO cargo_shipments VALUES (3490735, 447164, 3, 26937, 36421, 596.8145472633117);
INSERT INTO cargo_shipments VALUES (2742470, 733236, 2, 42565, 52750, 511.4990672311386);
INSERT INTO cargo_shipments VALUES (7043030, 446354, 1, 16240, 81142, 509.3928031836227);
INSERT INTO cargo_shipments VALUES (5444532, 380476, 3, 6711, 75571, 544.0142866941225);
INSERT INTO cargo_shipments VALUES (9934840, 205227, 4, 1067, 18819, 66.39562928472742);
INSERT INTO cargo_shipments VALUES (2385418, 212978, 4, 27084, 87094, 625.4345032426759);
INSERT INTO cargo_shipments VALUES (4136965, 757508, 3, 39226, 75571, 449.554572287404);
INSERT INTO cargo_shipments VALUES (3877379, 142736, 2, 19036, 27543, 685.2342033106025);
INSERT INTO cargo_shipments VALUES (9421927, 549043, 3, 8524, 70479, 376.8380517334322);
INSERT INTO cargo_shipments VALUES (4640926, 912883, 3, 7353, 84370, 980.5107102148673);
INSERT INTO cargo_shipments VALUES (7243052, 272236, 3, 24100, 42117, 813.5362982234998);
INSERT INTO cargo_shipments VALUES (5413122, 533295, 1, 31053, 53546, 416.7385041992404);
INSERT INTO cargo_shipments VALUES (2212201, 624235, 3, 31931, 35963, 998.1674633180613);
INSERT INTO cargo_shipments VALUES (4912587, 352768, 4, 38397, 95020, 989.2551863719466);
INSERT INTO cargo_shipments VALUES (5685651, 648680, 4, 13839, 51197, 204.0127173681049);
INSERT INTO cargo_shipments VALUES (1038892, 817972, 4, 33493, 76275, 184.91509451173982);
INSERT INTO cargo_shipments VALUES (2971558, 322704, 4, 30004, 21786, 759.7408329299714);
INSERT INTO cargo_shipments VALUES (8313416, 792511, 3, 14749, 66374, 772.0398326117673);
INSERT INTO cargo_shipments VALUES (3220085, 555106, 1, 14716, 67410, 692.1836343008722);
INSERT INTO cargo_shipments VALUES (2606163, 505917, 4, 5589, 67867, 285.7439442164461);
INSERT INTO cargo_shipments VALUES (4418845, 758628, 2, 12491, 81696, 162.62328532472247);
INSERT INTO cargo_shipments VALUES (1940127, 632283, 1, 34626, 37665, 728.4338899038476);
INSERT INTO cargo_shipments VALUES (9075236, 511881, 4, 46540, 95020, 349.1994180418026);
INSERT INTO cargo_shipments VALUES (5137434, 875193, 3, 39104, 58358, 324.99220203523225);
INSERT INTO cargo_shipments VALUES (2022175, 967575, 3, 13096, 58358, 761.6918603419641);
INSERT INTO cargo_shipments VALUES (4867565, 632283, 1, 47904, 66975, 571.2800164525303);
INSERT INTO cargo_shipments VALUES (8186489, 818559, 4, 34393, 67410, 892.5477825439161);
INSERT INTO cargo_shipments VALUES (8748848, 543693, 4, 45022, 66975, 278.52328318905097);
INSERT INTO cargo_shipments VALUES (5042867, 422404, 4, 30132, 81696, 130.9507846136293);
INSERT INTO cargo_shipments VALUES (2445053, 303778, 4, 15451, 17145, 999.6735820759129);
INSERT INTO cargo_shipments VALUES (3557315, 440172, 3, 34758, 76275, 14.727257212160062);
INSERT INTO cargo_shipments VALUES (3849916, 531537, 1, 27680, 87094, 923.8557091716785);
INSERT INTO cargo_shipments VALUES (2988685, 162305, 1, 28090, 34984, 690.414039836362);
INSERT INTO cargo_shipments VALUES (4905041, 230833, 4, 11051, 45027, 439.13309530933566);
INSERT INTO cargo_shipments VALUES (8673584, 224194, 1, 44369, 47786, 964.5435266411463);
INSERT INTO cargo_shipments VALUES (4524859, 798926, 4, 19953, 32518, 527.8687629993692);
INSERT INTO cargo_shipments VALUES (2322661, 596094, 4, 49579, 95509, 903.3940644372389);
INSERT INTO cargo_shipments VALUES (6052422, 253595, 3, 1622, 67067, 295.76099648911304);
INSERT INTO cargo_shipments VALUES (7966140, 115640, 2, 40606, 64620, 241.46568806046466);
INSERT INTO cargo_shipments VALUES (5041706, 601323, 3, 19697, 87624, 386.9809148545781);
INSERT INTO cargo_shipments VALUES (7973692, 440172, 4, 28506, 42117, 842.4719469032918);
INSERT INTO cargo_shipments VALUES (2865972, 675298, 4, 19006, 69183, 99.10711105260994);
INSERT INTO cargo_shipments VALUES (4635105, 624235, 1, 21542, 32861, 731.2521615691477);
INSERT INTO cargo_shipments VALUES (3004702, 970601, 2, 28453, 17145, 636.406409627081);
INSERT INTO cargo_shipments VALUES (8206294, 881607, 1, 21247, 66374, 833.6487451445003);
INSERT INTO cargo_shipments VALUES (3753781, 967575, 3, 9588, 22957, 690.319659278811);
INSERT INTO cargo_shipments VALUES (5725549, 200324, 2, 33178, 95504, 59.38557875957462);
INSERT INTO cargo_shipments VALUES (3235915, 422404, 4, 21964, 81696, 408.38269282564744);
INSERT INTO cargo_shipments VALUES (8456296, 878046, 1, 21363, 31584, 641.714087397877);
INSERT INTO cargo_shipments VALUES (6649745, 422244, 4, 35354, 56046, 69.64848801095336);
INSERT INTO cargo_shipments VALUES (9104156, 614681, 1, 13183, 13552, 829.5270902118152);
INSERT INTO cargo_shipments VALUES (5991501, 678301, 2, 48498, 26822, 591.2147702872804);
INSERT INTO cargo_shipments VALUES (2231322, 776366, 1, 28668, 87094, 334.534647679764);
INSERT INTO cargo_shipments VALUES (5079335, 353497, 2, 27057, 61780, 850.1536962070651);
INSERT INTO cargo_shipments VALUES (3970782, 555106, 3, 5640, 69028, 315.61053504849036);
INSERT INTO cargo_shipments VALUES (7218491, 272236, 1, 36828, 84900, 789.2097314488236);
INSERT INTO cargo_shipments VALUES (3287083, 912153, 1, 33361, 73217, 787.1720132088606);
INSERT INTO cargo_shipments VALUES (1618622, 115640, 3, 33914, 11754, 909.3398549742612);
INSERT INTO cargo_shipments VALUES (9959825, 599483, 4, 19536, 27543, 407.5238188082232);
INSERT INTO cargo_shipments VALUES (9162476, 912883, 1, 12915, 22957, 982.3714311606933);
INSERT INTO cargo_shipments VALUES (3173281, 948750, 2, 20817, 41156, 551.6850677591573);
