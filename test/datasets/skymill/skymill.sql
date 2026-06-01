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

INSERT INTO cities VALUES (138148, 'Louisiana', 'Stevenbury', 111204, 'Don Mueang', 'VDC');
INSERT INTO cities VALUES (188328, 'Alabama', 'Matthewberg', 944548, 'Borispol airport', 'ABQ');
INSERT INTO cities VALUES (492936, 'Illinois', 'Ferrellstad', 2282129, 'Ataturk Hava Limani airport', 'TYN');
INSERT INTO cities VALUES (212954, 'Montana', 'Stephanieburgh', 1714318, 'Otopeni airport', 'MRS');
INSERT INTO cities VALUES (438104, 'Minnesota', 'New Michelletown', 3453676, 'El Plumerillo airport', 'AQP');
INSERT INTO cities VALUES (479677, 'Connecticut', 'South Katelyn', 2289884, 'Adler airport', 'CCU');
INSERT INTO cities VALUES (425030, 'Mississippi', 'Mooremouth', 772427, 'Simon Bolivar airport', 'TFN');
INSERT INTO cities VALUES (366933, 'Georgia', 'Port Erika', 1344311, 'Gustavia airport', 'PLJ');
INSERT INTO cities VALUES (457726, 'Texas', 'North William', 4918719, 'Governador Jorge Teixeira de Oliveira Internatio', 'HAN');
INSERT INTO cities VALUES (205348, 'Iowa', 'Guerrabury', 3497461, 'Coronel Aviador Cesar Bombonato airport', 'YUL');
INSERT INTO cities VALUES (400476, 'Hawaii', 'Wilsonberg', 869284, 'General Abelardo L Rodriguez International Airpo', 'CGH');
INSERT INTO cities VALUES (272062, 'South Dakota', 'Chadfort', 4303705, 'Goiabeiras airport', 'HET');
INSERT INTO cities VALUES (494556, 'Illinois', 'New Michellefurt', 3462153, 'Greater Cincinnati International airport', 'CCS');
INSERT INTO cities VALUES (487781, 'Indiana', 'Valdezhaven', 3719996, 'Bodo airport', 'CLO');

-- =========== skymill.segments (generic) ==========

DROP TABLE IF EXISTS segments;

CREATE TABLE segments (
  id INT NOT NULL,
  origin INT NOT NULL,
  destination INT NOT NULL,
  distance INT NOT NULL
);

INSERT INTO segments VALUES (265717, 138148, 212954, 1749);
INSERT INTO segments VALUES (432822, 494556, 438104, 391);
INSERT INTO segments VALUES (294313, 272062, 188328, 844);
INSERT INTO segments VALUES (470939, 138148, 272062, 2028);
INSERT INTO segments VALUES (487287, 487781, 205348, 636);
INSERT INTO segments VALUES (188356, 138148, 479677, 1361);
INSERT INTO segments VALUES (434648, 438104, 438104, 1465);
INSERT INTO segments VALUES (429275, 438104, 400476, 675);
INSERT INTO segments VALUES (309276, 438104, 212954, 1875);
INSERT INTO segments VALUES (237788, 188328, 438104, 1173);
INSERT INTO segments VALUES (164033, 366933, 438104, 1200);
INSERT INTO segments VALUES (497396, 425030, 492936, 1210);
INSERT INTO segments VALUES (159054, 457726, 272062, 2330);
INSERT INTO segments VALUES (338899, 492936, 438104, 1447);
INSERT INTO segments VALUES (454442, 400476, 457726, 2139);
INSERT INTO segments VALUES (270256, 212954, 366933, 858);
INSERT INTO segments VALUES (206091, 492936, 492936, 1232);
INSERT INTO segments VALUES (269791, 425030, 494556, 511);
INSERT INTO segments VALUES (385116, 212954, 457726, 2000);
INSERT INTO segments VALUES (302149, 457726, 425030, 1410);
INSERT INTO segments VALUES (442985, 138148, 138148, 540);
INSERT INTO segments VALUES (358319, 487781, 205348, 1641);
INSERT INTO segments VALUES (464020, 138148, 272062, 2467);
INSERT INTO segments VALUES (141055, 438104, 487781, 341);
INSERT INTO segments VALUES (314357, 138148, 494556, 2256);
INSERT INTO segments VALUES (268939, 400476, 272062, 662);
INSERT INTO segments VALUES (405071, 272062, 487781, 967);
INSERT INTO segments VALUES (419049, 479677, 188328, 2443);
INSERT INTO segments VALUES (298831, 457726, 487781, 644);
INSERT INTO segments VALUES (121058, 479677, 487781, 1118);
INSERT INTO segments VALUES (410928, 212954, 492936, 1355);
INSERT INTO segments VALUES (255659, 492936, 400476, 439);
INSERT INTO segments VALUES (182312, 494556, 138148, 942);
INSERT INTO segments VALUES (430422, 425030, 425030, 2013);
INSERT INTO segments VALUES (370062, 487781, 205348, 2173);
INSERT INTO segments VALUES (126599, 479677, 438104, 471);
INSERT INTO segments VALUES (257731, 188328, 487781, 1380);
INSERT INTO segments VALUES (236110, 487781, 487781, 657);
INSERT INTO segments VALUES (254959, 188328, 188328, 207);
INSERT INTO segments VALUES (441656, 212954, 487781, 573);
INSERT INTO segments VALUES (147616, 425030, 400476, 2434);
INSERT INTO segments VALUES (460317, 487781, 400476, 2093);
INSERT INTO segments VALUES (125785, 457726, 479677, 1632);
INSERT INTO segments VALUES (309920, 438104, 457726, 2375);
INSERT INTO segments VALUES (484592, 212954, 138148, 494);
INSERT INTO segments VALUES (153482, 487781, 366933, 1837);
INSERT INTO segments VALUES (419693, 205348, 188328, 2457);
INSERT INTO segments VALUES (268227, 457726, 212954, 1694);
INSERT INTO segments VALUES (169833, 366933, 366933, 2151);
INSERT INTO segments VALUES (100367, 272062, 425030, 607);
INSERT INTO segments VALUES (322186, 366933, 487781, 226);
INSERT INTO segments VALUES (374635, 492936, 205348, 1159);
INSERT INTO segments VALUES (232860, 492936, 425030, 2174);
INSERT INTO segments VALUES (143527, 494556, 487781, 2192);
INSERT INTO segments VALUES (239935, 188328, 438104, 734);
INSERT INTO segments VALUES (411471, 457726, 138148, 212);

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

INSERT INTO aircraft VALUES (32977, 'SKY232', 2, 90);
INSERT INTO aircraft VALUES (90925, 'SKY545', 1, 166);
INSERT INTO aircraft VALUES (18187, 'SKY861', 2, 303);
INSERT INTO aircraft VALUES (45629, 'SKY860', 2, 383);
INSERT INTO aircraft VALUES (39753, 'SKY159', 3, 172);
INSERT INTO aircraft VALUES (84601, 'SKY711', 1, 237);
INSERT INTO aircraft VALUES (89828, 'SKY453', 3, 349);
INSERT INTO aircraft VALUES (94128, 'SKY069', 1, 320);
INSERT INTO aircraft VALUES (70573, 'SKY263', 1, 155);
INSERT INTO aircraft VALUES (71893, 'SKY083', 2, 144);
INSERT INTO aircraft VALUES (13051, 'SKY743', 1, 248);
INSERT INTO aircraft VALUES (23758, 'SKY614', 1, 376);
INSERT INTO aircraft VALUES (43926, 'SKY033', 3, 181);
INSERT INTO aircraft VALUES (21892, 'SKY683', 1, 130);
INSERT INTO aircraft VALUES (32281, 'SKY579', 3, 103);
INSERT INTO aircraft VALUES (76051, 'SKY908', 2, 108);
INSERT INTO aircraft VALUES (67709, 'SKY826', 1, 351);
INSERT INTO aircraft VALUES (74433, 'SKY868', 2, 140);
INSERT INTO aircraft VALUES (51185, 'SKY860', 1, 191);
INSERT INTO aircraft VALUES (79097, 'SKY802', 2, 393);
INSERT INTO aircraft VALUES (94719, 'SKY526', 3, 186);
INSERT INTO aircraft VALUES (80410, 'SKY275', 3, 209);
INSERT INTO aircraft VALUES (61836, 'SKY591', 1, 84);
INSERT INTO aircraft VALUES (16604, 'SKY935', 2, 377);
INSERT INTO aircraft VALUES (15933, 'SKY000', 2, 126);
INSERT INTO aircraft VALUES (87639, 'SKY114', 3, 372);
INSERT INTO aircraft VALUES (51878, 'SKY927', 2, 159);
INSERT INTO aircraft VALUES (77021, 'SKY787', 2, 114);

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

INSERT INTO passenger VALUES (8391185, 'David', 'Cruz', '827 Harris Walks Apt. 795
South Samuel, SC 98150', 457726, 84, FALSE, 'platinum');
INSERT INTO passenger VALUES (9169482, 'Mark', 'Villarreal', '3987 Angela Street Apt. 850
Lake Miranda, GA 56624', 188328, 72, TRUE, 'silver');
INSERT INTO passenger VALUES (6091790, 'Danielle', 'Jordan', 'Unit 9260 Box 5278
DPO AE 29168', 494556, 13, TRUE, 'platinum');
INSERT INTO passenger VALUES (8173474, 'Sara', 'Tucker', '78869 Melinda Mills Suite 188
Port Loganport, WY 45332', 366933, 62, FALSE, 'silver');
INSERT INTO passenger VALUES (2193325, 'Michele', 'Craig', '6031 Murray Port
Toddborough, KS 81834', 212954, 63, FALSE, 'silver');
INSERT INTO passenger VALUES (3002493, 'Holly', 'Cherry', '52924 Tracey Curve
Antonioport, NM 40970', 205348, 81, TRUE, 'basic');
INSERT INTO passenger VALUES (3257367, 'William', 'Morrison', '1069 Jones Villages
West Tannershire, CA 59290', 492936, 68, FALSE, 'basic');
INSERT INTO passenger VALUES (6066735, 'Jasmine', 'Carter', '8348 Swanson Corner
Ericshire, FM 73421', 425030, 25, FALSE, 'silver');
INSERT INTO passenger VALUES (9454921, 'Bryan', 'Pierce', '2407 Laurie Ford Suite 999
North Matthew, VT 57851', 425030, 75, TRUE, 'gold');
INSERT INTO passenger VALUES (7371009, 'Dawn', 'Williamson', '3292 Kim Center
Karlbury, MT 63962', 400476, 61, FALSE, 'basic');
INSERT INTO passenger VALUES (6348851, 'Crystal', 'Short', '73666 Arthur Loop
New Anitamouth, MN 96258', 492936, 74, TRUE, 'platinum');
INSERT INTO passenger VALUES (2680208, 'Joseph', 'Snyder', '7264 Jessica Terrace Suite 197
North Joy, PR 81262', 494556, 51, TRUE, 'silver');
INSERT INTO passenger VALUES (3577168, 'Nathan', 'Horne', '25047 James Roads
Rubiobury, NM 58394', 212954, 18, FALSE, 'platinum');
INSERT INTO passenger VALUES (5009563, 'Mary', 'Morgan', '864 Donald Mountain
Port John, OH 25952', 492936, 13, TRUE, 'gold');
INSERT INTO passenger VALUES (7242085, 'Jason', 'Lewis', '4898 Timothy Knoll Apt. 063
North Courtney, NV 00693', 272062, 23, TRUE, 'silver');
INSERT INTO passenger VALUES (7865203, 'Anthony', 'Mays', '774 Stuart Park
North Elizabeth, NJ 04857', 212954, 78, FALSE, 'platinum');
INSERT INTO passenger VALUES (2940416, 'Albert', 'Wall', '02904 Andrea Passage
Kingfort, NH 20237', 188328, 30, FALSE, 'platinum');
INSERT INTO passenger VALUES (6597799, 'Bobby', 'Hardy', '73327 Amy Key Apt. 747
New Janice, FL 21232', 400476, 86, FALSE, 'platinum');
INSERT INTO passenger VALUES (3784950, 'Tiffany', 'Moore', '33957 Mueller Route Suite 401
North Donna, AK 21536', 400476, 13, TRUE, 'basic');
INSERT INTO passenger VALUES (6589761, 'Stephanie', 'Wiley', '70584 Mark Stravenue
North William, GU 68653', 438104, 72, FALSE, 'silver');
INSERT INTO passenger VALUES (4636158, 'Wendy', 'Jensen', '72916 Heather Village
Garciaton, WI 35997', 457726, 43, FALSE, 'gold');
INSERT INTO passenger VALUES (4507387, 'Daisy', 'Stuart', '4334 Taylor Shore
Clementsfurt, UT 47800', 212954, 81, FALSE, 'platinum');
INSERT INTO passenger VALUES (2422409, 'Shirley', 'Bell', '87182 Smith Hollow
Smithville, IL 16939', 212954, 69, FALSE, 'basic');
INSERT INTO passenger VALUES (9288911, 'Nancy', 'May', '06374 King Isle Apt. 789
North Tylershire, WA 44442', 400476, 12, FALSE, 'gold');
INSERT INTO passenger VALUES (5563188, 'Tracy', 'Smith', '39052 Brown Fords Suite 549
Roseville, TX 57257', 400476, 17, FALSE, 'basic');
INSERT INTO passenger VALUES (7563320, 'Hannah', 'Kennedy', '3897 Robert Flats
Gomezburgh, WY 66411', 272062, 81, FALSE, 'silver');
INSERT INTO passenger VALUES (8453354, 'Ashley', 'Silva', '0733 Ferguson Ville Apt. 899
North Anthonyport, ND 07130', 272062, 65, TRUE, 'basic');
INSERT INTO passenger VALUES (8594972, 'Robert', 'Moody', '3156 Carter Junctions Suite 954
Kellyview, FL 67041', 138148, 58, FALSE, 'gold');
INSERT INTO passenger VALUES (5286908, 'Caitlin', 'Griffith', '8604 Soto Rest
West Randyborough, CA 27991', 188328, 87, TRUE, 'gold');
INSERT INTO passenger VALUES (2541341, 'Christopher', 'Pearson', '442 Morris Circles Suite 056
North Kristyton, DE 54096', 400476, 24, TRUE, 'gold');
INSERT INTO passenger VALUES (4078630, 'Ryan', 'Miles', '7202 Angel Trail Suite 994
West Michelle, CT 57389', 272062, 84, FALSE, 'gold');
INSERT INTO passenger VALUES (1185144, 'Alexander', 'Fowler', '5284 Gonzalez Plains
New Victoria, NC 98212', 188328, 87, FALSE, 'silver');
INSERT INTO passenger VALUES (7092950, 'Rose', 'Mcdaniel', '57534 Tracy Wall
Romerochester, VT 31251', 457726, 36, TRUE, 'silver');
INSERT INTO passenger VALUES (5720403, 'Andrew', 'Silva', '42063 Wilson Square
Jonesville, MN 10726', 212954, 31, FALSE, 'basic');
INSERT INTO passenger VALUES (2024582, 'Shane', 'Fitzpatrick', '5414 Duarte Estate
Lake Mary, OK 14169', 487781, 56, FALSE, 'platinum');
INSERT INTO passenger VALUES (4287773, 'Jason', 'Stevens', '867 Miller Corner
East Joe, NV 57891', 138148, 62, TRUE, 'basic');
INSERT INTO passenger VALUES (9622165, 'Shelley', 'Yang', '825 Ashley Mountains Apt. 541
Port Jennifer, NV 04422', 457726, 44, TRUE, 'basic');
INSERT INTO passenger VALUES (9735147, 'Michael', 'Brown', '05167 Dean Squares Apt. 140
East Jasonville, MO 53670', 438104, 68, FALSE, 'silver');
INSERT INTO passenger VALUES (8625276, 'Kathryn', 'Reed', '26994 Victoria Harbors
Walkerfort, MO 57576', 438104, 54, FALSE, 'silver');
INSERT INTO passenger VALUES (4668405, 'Jennifer', 'Taylor', '0994 Joseph Crescent Apt. 923
South Rebecca, AK 53533', 479677, 27, TRUE, 'platinum');
INSERT INTO passenger VALUES (9166027, 'Sue', 'Francis', '4774 Jason Views Suite 096
Butlerview, NJ 31843', 457726, 58, FALSE, 'gold');
INSERT INTO passenger VALUES (4859106, 'Juan', 'Lin', '244 Young Fork
West Heather, PR 88312', 438104, 74, FALSE, 'silver');
INSERT INTO passenger VALUES (5719835, 'Kristen', 'Ward', '5609 Gerald Brook
Lake Barbara, MS 42591', 400476, 46, FALSE, 'basic');
INSERT INTO passenger VALUES (6976359, 'Isaac', 'Bryant', '431 Gray Plaza Suite 165
Banksville, MP 57003', 400476, 21, TRUE, 'platinum');
INSERT INTO passenger VALUES (9488847, 'Sarah', 'Smith', '889 Waters Heights
Westton, IL 87266', 188328, 48, FALSE, 'silver');
INSERT INTO passenger VALUES (8805416, 'David', 'Schroeder', '45153 Williams Street Apt. 407
Lake Nicole, NH 69137', 479677, 39, FALSE, 'platinum');
INSERT INTO passenger VALUES (7885995, 'Patricia', 'Palmer', '07059 Megan Coves Apt. 035
Pooleburgh, HI 03615', 188328, 18, FALSE, 'silver');
INSERT INTO passenger VALUES (7797269, 'Amy', 'Smith', '935 Potter Manors Suite 244
North Wayne, MT 06023', 212954, 41, FALSE, 'silver');
INSERT INTO passenger VALUES (8446918, 'Tiffany', 'Smith', '369 Matthew Skyway Apt. 345
Matthewmouth, AK 73545', 205348, 56, FALSE, 'basic');
INSERT INTO passenger VALUES (8622373, 'Jacob', 'Martin', '0455 Torres Via
East Madisonfort, CO 89489', 487781, 88, FALSE, 'platinum');
INSERT INTO passenger VALUES (1083307, 'Kristi', 'Sanchez', '5967 Carl Stravenue
Haydenburgh, SC 34829', 400476, 16, TRUE, 'gold');
INSERT INTO passenger VALUES (8809406, 'Hannah', 'Brooks', '14225 Whitney Gardens Suite 616
Andrewstad, MS 53670', 487781, 68, FALSE, 'gold');
INSERT INTO passenger VALUES (2079829, 'Audrey', 'Gonzalez', '3165 Linda Point
East Laura, DE 96660', 212954, 58, TRUE, 'basic');
INSERT INTO passenger VALUES (3166127, 'Joseph', 'Perez', 'USNS Moore
FPO AE 01686', 188328, 90, FALSE, 'basic');
INSERT INTO passenger VALUES (5798843, 'Jeffery', 'Estes', '42312 Kristen Square Apt. 821
Blackstad, DC 34567', 188328, 41, TRUE, 'platinum');
INSERT INTO passenger VALUES (7180424, 'Cassandra', 'Stewart', '28189 Elizabeth Common Apt. 559
Lake Vincent, MN 15743', 438104, 12, FALSE, 'platinum');
INSERT INTO passenger VALUES (3423018, 'Nicole', 'Campbell', '90387 James Canyon Suite 034
West Michelle, MO 58596', 212954, 66, FALSE, 'gold');
INSERT INTO passenger VALUES (8238843, 'Kayla', 'Ferguson', '0799 Eric Club
Lake Sethborough, IA 57891', 138148, 37, FALSE, 'gold');
INSERT INTO passenger VALUES (3655021, 'Susan', 'Reeves', '57760 Beth Forks
Port Josephville, PW 01895', 492936, 42, TRUE, 'silver');
INSERT INTO passenger VALUES (9270808, 'Andrew', 'Mitchell', '13754 Alvarado Rue
East Johnbury, WI 56907', 457726, 64, TRUE, 'basic');
INSERT INTO passenger VALUES (3816929, 'Stephanie', 'Williams', '1234 Green Spring
Rangelborough, AK 18956', 479677, 13, TRUE, 'gold');
INSERT INTO passenger VALUES (3437925, 'Michael', 'Simmons', '141 Pope Corners Suite 916
Proctorfurt, KY 53603', 212954, 78, FALSE, 'gold');
INSERT INTO passenger VALUES (7447517, 'Cheryl', 'Hicks', '27753 Susan Place
East Cynthiahaven, OR 25364', 494556, 19, TRUE, 'basic');
INSERT INTO passenger VALUES (4809618, 'Mitchell', 'Jackson', '903 Meghan Turnpike Apt. 895
Alyssamouth, LA 72870', 492936, 86, FALSE, 'silver');
INSERT INTO passenger VALUES (2483269, 'Steven', 'Barber', '87602 Goodman Road Apt. 010
Triciachester, MO 66088', 438104, 76, FALSE, 'basic');
INSERT INTO passenger VALUES (5185820, 'Mackenzie', 'Allen', '75421 Gutierrez Knoll
West Bradleytown, ID 49624', 212954, 78, FALSE, 'basic');
INSERT INTO passenger VALUES (4278613, 'Dawn', 'Wade', '4754 Regina Dam Suite 339
West Jody, AL 53751', 438104, 46, TRUE, 'gold');
INSERT INTO passenger VALUES (5105774, 'Nicholas', 'Allen', '183 Tammy Fords
Port Wendychester, NE 82503', 494556, 36, TRUE, 'platinum');
INSERT INTO passenger VALUES (3944798, 'Joshua', 'Torres', '31883 Jacob Wells
Brightport, UT 79221', 487781, 44, TRUE, 'gold');
INSERT INTO passenger VALUES (1308410, 'Chad', 'Pittman', '106 Rivera Pines
New Charlestown, MA 76940', 212954, 85, FALSE, 'platinum');
INSERT INTO passenger VALUES (5820495, 'Jason', 'Hodges', '9859 Castro Extensions
Collinschester, KY 46625', 272062, 78, FALSE, 'silver');
INSERT INTO passenger VALUES (8782077, 'Erica', 'Walker', '694 Sue Estates Suite 479
Jonathanport, NJ 46272', 487781, 21, FALSE, 'platinum');
INSERT INTO passenger VALUES (8216199, 'Shelley', 'Monroe', 'PSC 5717, Box 8186
APO AA 15317', 188328, 72, TRUE, 'basic');
INSERT INTO passenger VALUES (4193399, 'Ethan', 'Everett', '8496 Knox Corners Suite 891
Jonesborough, VI 42394', 492936, 59, TRUE, 'silver');
INSERT INTO passenger VALUES (4086038, 'Cynthia', 'Cox', '2195 Keith Meadow Suite 892
West Jonathon, KS 06573', 487781, 59, FALSE, 'platinum');
INSERT INTO passenger VALUES (8517884, 'Laura', 'Hall', '6137 John Shoal Apt. 240
Nicolestad, GA 51578', 492936, 69, FALSE, 'basic');
INSERT INTO passenger VALUES (4220395, 'Amanda', 'Carlson', '07485 Friedman Summit
Millerbury, SD 90828', 492936, 52, FALSE, 'gold');
INSERT INTO passenger VALUES (7011025, 'Richard', 'Rodriguez', '9880 Troy Plain
Westside, NC 18383', 272062, 88, FALSE, 'silver');
INSERT INTO passenger VALUES (3270346, 'Allison', 'Webb', '6168 Dennis Turnpike Suite 760
New Adamberg, SC 89844', 425030, 25, FALSE, 'platinum');
INSERT INTO passenger VALUES (2729297, 'Jody', 'Williams', '0068 Cheryl Divide Suite 843
South Amberside, GU 11149', 425030, 15, FALSE, 'silver');
INSERT INTO passenger VALUES (7507143, 'Alex', 'Herrera', '4006 Kelly Mountain Suite 277
Allenmouth, NV 64366', 457726, 20, FALSE, 'basic');
INSERT INTO passenger VALUES (7859194, 'Jack', 'Jackson', '539 Allen Pines Suite 997
Reevesport, RI 76626', 438104, 61, FALSE, 'basic');
INSERT INTO passenger VALUES (6762919, 'Samantha', 'Hall', '744 Amanda Crescent
New Jamesborough, PA 70834', 487781, 20, TRUE, 'platinum');
INSERT INTO passenger VALUES (2957480, 'Mark', 'Lewis', '488 Douglas Vista
Samanthaburgh, OK 27941', 205348, 33, TRUE, 'basic');
INSERT INTO passenger VALUES (8951574, 'Darrell', 'Cruz', '65558 Cunningham Extensions
Murphychester, PR 28245', 487781, 47, FALSE, 'silver');
INSERT INTO passenger VALUES (7456753, 'Gerald', 'Gonzalez', '684 Williamson Well Suite 149
Eugeneville, MA 15845', 492936, 27, FALSE, 'gold');
INSERT INTO passenger VALUES (1169425, 'Ryan', 'Decker', '643 Larry Light Apt. 858
Danielhaven, TX 03170', 457726, 60, FALSE, 'platinum');
INSERT INTO passenger VALUES (6904458, 'Eric', 'Owen', '350 Jones Overpass Apt. 389
North Wendy, ID 67882', 494556, 38, TRUE, 'silver');
INSERT INTO passenger VALUES (3760433, 'Benjamin', 'Roth', '8873 Trevino Landing Suite 276
Marymouth, DC 54158', 272062, 84, TRUE, 'gold');
INSERT INTO passenger VALUES (2265297, 'Monica', 'Gomez', '902 Timothy Gardens Apt. 130
Lorifort, NV 18267', 138148, 73, FALSE, 'platinum');
INSERT INTO passenger VALUES (6351366, 'Jessica', 'Smith', '157 Dawson Lakes
South Steven, AL 16733', 479677, 31, TRUE, 'gold');
INSERT INTO passenger VALUES (7412516, 'Amy', 'Shaw', '726 Michael Hollow Suite 327
South Michael, TN 48615', 457726, 21, TRUE, 'platinum');
INSERT INTO passenger VALUES (5277747, 'Randy', 'Graham', '97383 Judith Via Apt. 203
Mitchellberg, OK 31148', 438104, 54, TRUE, 'silver');
INSERT INTO passenger VALUES (3343612, 'Sarah', 'Flores', '75172 Kirk Mill
Melindaside, KS 54507', 400476, 72, TRUE, 'basic');
INSERT INTO passenger VALUES (1594316, 'Gary', 'Woods', '262 Lin Radial Suite 385
Brianhaven, ID 38364', 272062, 56, TRUE, 'gold');
INSERT INTO passenger VALUES (2974225, 'Renee', 'Wood', '2918 Holly Island
West Pamelachester, OK 59570', 487781, 39, TRUE, 'silver');
INSERT INTO passenger VALUES (3418931, 'Rebecca', 'Frazier', '1860 Rebecca Mount Suite 429
East William, GU 84713', 188328, 40, TRUE, 'silver');
INSERT INTO passenger VALUES (2785261, 'Justin', 'Durham', '447 Nguyen Ports
Lake Michaelfort, WA 52080', 479677, 26, TRUE, 'platinum');
INSERT INTO passenger VALUES (3074155, 'Jordan', 'Brown', '0022 Garcia Dale Apt. 475
Williamsmouth, AK 44548', 366933, 56, TRUE, 'basic');
INSERT INTO passenger VALUES (5382959, 'John', 'Morris', 'USNS Rogers
FPO AE 44086', 138148, 56, FALSE, 'gold');
INSERT INTO passenger VALUES (2216117, 'Amanda', 'Miller', '327 Charles Lakes Apt. 918
Williamsfort, KY 04098', 457726, 27, FALSE, 'basic');
INSERT INTO passenger VALUES (2992723, 'William', 'Gomez', 'USCGC Hughes
FPO AP 64940', 487781, 36, FALSE, 'platinum');
INSERT INTO passenger VALUES (5761385, 'James', 'Bailey', '84425 Washington Ports Apt. 323
Andrewsland, GA 70369', 272062, 84, FALSE, 'platinum');
INSERT INTO passenger VALUES (6728086, 'Timothy', 'Myers', '7310 Daniel Court
West Amyhaven, NH 14794', 272062, 90, TRUE, 'basic');
INSERT INTO passenger VALUES (8525725, 'Michael', 'Allen', '869 Scott Station Apt. 629
East Mary, NM 21999', 494556, 30, TRUE, 'silver');
INSERT INTO passenger VALUES (9607002, 'Timothy', 'Alexander', '285 Hawkins Park
Bryantmouth, VT 87067', 494556, 67, FALSE, 'platinum');
INSERT INTO passenger VALUES (9218661, 'Matthew', 'Hood', '0047 Brittany Shore
Martinezside, DC 39313', 366933, 14, TRUE, 'silver');
INSERT INTO passenger VALUES (8957252, 'Kathryn', 'Little', '3241 Pace Locks Apt. 463
Fergusonton, TX 35153', 400476, 23, TRUE, 'gold');
INSERT INTO passenger VALUES (4789986, 'Megan', 'Turner', '548 Taylor Freeway Suite 136
New David, IL 87023', 272062, 76, FALSE, 'basic');
INSERT INTO passenger VALUES (4422051, 'Nancy', 'Walsh', '39767 Jerry Shore
New David, SC 27149', 212954, 85, TRUE, 'silver');
INSERT INTO passenger VALUES (3977299, 'Catherine', 'Butler', '041 Bradley Expressway Suite 011
New Derrick, PR 97189', 138148, 78, FALSE, 'platinum');
INSERT INTO passenger VALUES (8395495, 'Rebecca', 'Jenkins', '8893 Corey Glen
South Kimberly, IL 23625', 492936, 33, TRUE, 'platinum');
INSERT INTO passenger VALUES (8845953, 'Jessica', 'Johnson', '7362 Randy Road Apt. 930
North Denise, WA 74749', 400476, 44, FALSE, 'gold');
INSERT INTO passenger VALUES (6526839, 'Thomas', 'Mcclain', '9170 Larson Tunnel
Edwardport, HI 51998', 205348, 30, FALSE, 'silver');
INSERT INTO passenger VALUES (5781985, 'Lauren', 'Thomas', '05090 Donald Walks Apt. 677
Robinsonton, LA 48417', 494556, 84, TRUE, 'silver');
INSERT INTO passenger VALUES (5497703, 'Tiffany', 'Rice', '85085 Taylor Valley Suite 147
East Stephen, KS 13008', 457726, 24, TRUE, 'platinum');
INSERT INTO passenger VALUES (3504500, 'Johnny', 'Garcia', '4412 Ramirez Isle
Marthaton, VA 40891', 272062, 66, FALSE, 'basic');
INSERT INTO passenger VALUES (5332651, 'Shelly', 'Moore', '51241 Knapp Station Suite 968
Port Jose, KY 31552', 492936, 60, FALSE, 'gold');
INSERT INTO passenger VALUES (2461695, 'Charles', 'Nichols', 'PSC 0856, Box 3804
APO AE 28124', 138148, 88, TRUE, 'basic');
INSERT INTO passenger VALUES (7660641, 'Kayla', 'Oneill', '38494 Barnett Dale
Jasonbury, ID 64464', 272062, 72, TRUE, 'platinum');
INSERT INTO passenger VALUES (3106291, 'Lisa', 'Davis', '12129 Bonilla Keys
Chelseaton, KY 85616', 366933, 45, TRUE, 'platinum');
INSERT INTO passenger VALUES (9810226, 'Melissa', 'Swanson', '663 David Key Suite 268
East Tanyaborough, MA 70877', 494556, 45, FALSE, 'platinum');
INSERT INTO passenger VALUES (8707528, 'Jennifer', 'Williams', '22147 Melanie Tunnel Apt. 738
West Jennyton, CA 57466', 438104, 74, TRUE, 'platinum');
INSERT INTO passenger VALUES (6564332, 'Vanessa', 'Wells', '469 Kristin Points
New Marcusshire, UT 66346', 188328, 22, TRUE, 'basic');
INSERT INTO passenger VALUES (2905071, 'Casey', 'Daniel', '012 Hill Heights Suite 650
Andreaview, ND 55580', 212954, 49, FALSE, 'gold');
INSERT INTO passenger VALUES (4017366, 'Anita', 'Nash', '367 Tina Islands
Bethport, WA 59984', 205348, 31, FALSE, 'silver');
INSERT INTO passenger VALUES (3738778, 'Cole', 'Evans', '84499 Daniel Glens Apt. 094
Wilkersonmouth, PW 47931', 492936, 27, TRUE, 'basic');
INSERT INTO passenger VALUES (3897954, 'Michele', 'Myers', '4461 Dennis Ford Apt. 932
Lake Jeffrey, RI 16175', 400476, 23, FALSE, 'basic');
INSERT INTO passenger VALUES (5408609, 'Scott', 'Smith', '13189 Melissa Vista
West Gene, ID 78198', 487781, 33, TRUE, 'gold');
INSERT INTO passenger VALUES (7422573, 'Timothy', 'Rios', '086 Victoria Neck
North Brookefort, AZ 34180', 487781, 67, TRUE, 'basic');
INSERT INTO passenger VALUES (3963722, 'Matthew', 'Martin', '945 Cruz Courts
Lake Jefferyland, FM 88886', 212954, 40, TRUE, 'platinum');
INSERT INTO passenger VALUES (2013023, 'Rose', 'Smith', '559 Bowen Shores
South Roberttown, NM 55146', 438104, 40, TRUE, 'gold');
INSERT INTO passenger VALUES (1365545, 'Michael', 'Anderson', '751 Jennifer Grove Suite 590
West Martin, VA 54250', 366933, 25, FALSE, 'platinum');
INSERT INTO passenger VALUES (3135062, 'Shawn', 'Duncan', '5227 Nathan Viaduct Apt. 962
Wadehaven, NV 95820', 366933, 30, FALSE, 'basic');
INSERT INTO passenger VALUES (9729552, 'Austin', 'Burns', '62830 Susan Cliff
Marvinstad, GA 98778', 400476, 48, FALSE, 'basic');
INSERT INTO passenger VALUES (4118224, 'Matthew', 'Johnson', '91024 Shawn Oval
Williamland, CO 14178', 492936, 14, TRUE, 'silver');
INSERT INTO passenger VALUES (6419573, 'Eric', 'Powers', 'PSC 1147, Box 9121
APO AE 80116', 205348, 60, FALSE, 'silver');
INSERT INTO passenger VALUES (3352316, 'Michelle', 'Sullivan', '8610 Flowers Throughway
East Lesliechester, AL 44293', 479677, 39, TRUE, 'basic');
INSERT INTO passenger VALUES (6856915, 'Mark', 'Hickman', '34630 Brian Branch
Lake Marissafurt, PW 06709', 487781, 63, TRUE, 'platinum');
INSERT INTO passenger VALUES (4678821, 'Chelsea', 'Hendrix', '471 Joanne Locks Suite 236
South Jacqueline, WA 71080', 492936, 36, FALSE, 'basic');
INSERT INTO passenger VALUES (7143247, 'Christopher', 'White', '5722 Mitchell Rapids
South William, LA 98273', 425030, 36, TRUE, 'basic');
INSERT INTO passenger VALUES (7083414, 'James', 'Zavala', '953 Jones View
Allisonmouth, MT 06147', 487781, 55, TRUE, 'gold');
INSERT INTO passenger VALUES (2654571, 'Jessica', 'Beck', '441 Lee Overpass
Melindashire, TN 59744', 457726, 85, FALSE, 'basic');
INSERT INTO passenger VALUES (4308132, 'Valerie', 'Johnson', '695 Tammy Mount Suite 616
New Cameron, NV 05547', 487781, 83, TRUE, 'platinum');
INSERT INTO passenger VALUES (4197805, 'Michelle', 'Brown', '46663 Lisa Streets Suite 559
Zacharyberg, WV 69757', 188328, 77, FALSE, 'gold');
INSERT INTO passenger VALUES (4865004, 'Brian', 'Murphy', '4108 Cox Orchard
Lake Travismouth, IA 50327', 425030, 78, TRUE, 'gold');
INSERT INTO passenger VALUES (3440276, 'Jonathan', 'Macias', '9271 Tucker Vista
Stewartborough, NM 18446', 400476, 70, TRUE, 'platinum');
INSERT INTO passenger VALUES (8962583, 'Maurice', 'Harrington', '996 Nguyen Centers
East Nicholefort, SD 42503', 188328, 38, FALSE, 'gold');
INSERT INTO passenger VALUES (7470714, 'William', 'Stevenson', '97197 Carter Plains
Johnburgh, TX 37640', 400476, 50, TRUE, 'platinum');
INSERT INTO passenger VALUES (5030264, 'Sarah', 'Meza', 'USS Cooper
FPO AE 66532', 487781, 18, TRUE, 'basic');
INSERT INTO passenger VALUES (5053396, 'Michael', 'Schneider', '59410 Tonya Wells Apt. 439
Stevenburgh, AK 89518', 492936, 64, FALSE, 'gold');
INSERT INTO passenger VALUES (7515878, 'Aimee', 'Gonzalez', '93175 Kelly Vista Suite 370
Ibarraborough, MI 52183', 479677, 34, FALSE, 'gold');
INSERT INTO passenger VALUES (7595919, 'Jason', 'Perez', '5687 Johnson Summit Suite 722
New Derrickfurt, OR 11759', 138148, 55, TRUE, 'gold');
INSERT INTO passenger VALUES (9883532, 'Todd', 'Miller', '919 Brett Flat Suite 153
Jacksonton, ME 44528', 138148, 32, TRUE, 'silver');
INSERT INTO passenger VALUES (4439160, 'Kelly', 'Rowe', '117 Joshua Drives
Port Michael, CA 51839', 492936, 51, FALSE, 'gold');
INSERT INTO passenger VALUES (9478711, 'Ryan', 'Hall', '3135 Jessica Heights
Williamsmouth, IN 76268', 492936, 21, TRUE, 'basic');
INSERT INTO passenger VALUES (1128509, 'Deborah', 'Montes', '053 Ray Forge Apt. 787
Adamville, WA 14714', 492936, 71, FALSE, 'platinum');
INSERT INTO passenger VALUES (9263093, 'Gregory', 'King', '0395 Claudia Port Apt. 879
Lake Janetborough, DE 93960', 272062, 15, TRUE, 'gold');
INSERT INTO passenger VALUES (7540759, 'Elizabeth', 'Simpson', '09693 Tina Prairie Suite 103
Clarketown, GA 68523', 438104, 37, TRUE, 'silver');
INSERT INTO passenger VALUES (2347757, 'Raymond', 'Day', '2324 Jonathan Summit
South Maryton, GU 19544', 494556, 89, TRUE, 'platinum');
INSERT INTO passenger VALUES (4339947, 'Dan', 'Burnett', 'USS Day
FPO AE 59659', 494556, 81, FALSE, 'platinum');
INSERT INTO passenger VALUES (6527815, 'Richard', 'Aguilar', '0514 Stephens Gateway
Carlfurt, OK 43209', 479677, 22, TRUE, 'silver');
INSERT INTO passenger VALUES (9258677, 'David', 'Duncan', '827 Thomas Trace
Lake Maxwellstad, TN 10491', 138148, 29, TRUE, 'platinum');
INSERT INTO passenger VALUES (8896880, 'Marcus', 'Schneider', '3008 Peter Locks
East Kevinton, OK 95820', 188328, 69, TRUE, 'gold');
INSERT INTO passenger VALUES (8770415, 'Jose', 'Bush', '1167 James Islands
Smithburgh, NE 22456', 272062, 47, TRUE, 'silver');
INSERT INTO passenger VALUES (2881744, 'Cory', 'Griffin', '56108 Diane Greens Suite 339
Mckinneymouth, NV 72594', 272062, 18, TRUE, 'basic');
INSERT INTO passenger VALUES (6187608, 'Ashley', 'Young', '225 Marquez Drives
Melissaberg, SC 14117', 492936, 23, TRUE, 'gold');
INSERT INTO passenger VALUES (2773723, 'Gregory', 'Pruitt', '287 Smith Field
East Lorifurt, SC 85909', 487781, 55, TRUE, 'gold');
INSERT INTO passenger VALUES (2975287, 'Patrick', 'Leblanc', '61918 Flores Viaduct Suite 378
Port Nicoleton, TX 64429', 494556, 57, FALSE, 'basic');
INSERT INTO passenger VALUES (7462556, 'Brett', 'Hall', '0176 Bass Curve
Francohaven, MD 61913', 479677, 15, TRUE, 'silver');
INSERT INTO passenger VALUES (1784320, 'Brianna', 'Lewis', '3065 Cohen Parkway
Rogersburgh, AS 88587', 138148, 25, TRUE, 'gold');
INSERT INTO passenger VALUES (8755012, 'Carly', 'Hernandez', 'PSC 8517, Box 5871
APO AP 16489', 212954, 28, TRUE, 'gold');
INSERT INTO passenger VALUES (5088004, 'Mary', 'Bell', '55716 Garcia Manor Suite 347
Kennethmouth, KY 11195', 188328, 40, TRUE, 'silver');
INSERT INTO passenger VALUES (9356593, 'Stephanie', 'Byrd', '7213 Carlos Green Apt. 782
New Christopherton, UT 31051', 438104, 48, FALSE, 'silver');
INSERT INTO passenger VALUES (4430924, 'Ricky', 'Mason', '1895 Bennett Ways
Jennashire, MP 06958', 492936, 45, TRUE, 'gold');
INSERT INTO passenger VALUES (9999465, 'Carolyn', 'Haynes', '02719 Christopher Keys
New Pamelafort, GA 73339', 188328, 53, TRUE, 'silver');
INSERT INTO passenger VALUES (7503774, 'Alisha', 'Ruiz', '09785 James Lake
Lake Pamelaville, NC 34537', 272062, 22, FALSE, 'gold');
INSERT INTO passenger VALUES (9765785, 'Shelley', 'Cardenas', '190 Katrina Locks
New Derekside, SD 97663', 272062, 19, TRUE, 'platinum');
INSERT INTO passenger VALUES (4747967, 'Aaron', 'Holder', '3262 Jon Cove Apt. 312
East John, KY 50271', 138148, 83, FALSE, 'basic');
INSERT INTO passenger VALUES (8155808, 'Brian', 'Thomas', '14143 Jennings Ports
East Martin, AR 60674', 479677, 32, TRUE, 'gold');
INSERT INTO passenger VALUES (8668826, 'Jennifer', 'Marshall', '6756 Nolan Burg
Josephshire, CA 77344', 425030, 54, TRUE, 'platinum');
INSERT INTO passenger VALUES (8502145, 'Yvette', 'Walters', '29511 Amy Mews Apt. 146
West Danielview, UT 44311', 366933, 71, FALSE, 'silver');
INSERT INTO passenger VALUES (4953636, 'Christopher', 'Walker', '15089 Schmitt Lane Suite 151
Williamburgh, MT 60487', 272062, 70, TRUE, 'basic');
INSERT INTO passenger VALUES (7556383, 'Timothy', 'Arnold', '3466 Kimberly Highway Apt. 495
West Ashley, CO 50049', 188328, 88, TRUE, 'basic');
INSERT INTO passenger VALUES (3681623, 'Keith', 'Bridges', '0278 Joseph Vista
Stanleyhaven, KY 14537', 188328, 51, TRUE, 'basic');
INSERT INTO passenger VALUES (3713051, 'Jennifer', 'Brown', 'USCGC Nielsen
FPO AE 97834', 366933, 58, FALSE, 'gold');
INSERT INTO passenger VALUES (3626944, 'Jennifer', 'Avila', '87923 Alexander Locks Apt. 656
Port Annborough, NE 64278', 188328, 74, TRUE, 'gold');
INSERT INTO passenger VALUES (9017430, 'Angela', 'Holt', '218 Regina Center
Hollowaystad, AR 13465', 494556, 61, FALSE, 'silver');
INSERT INTO passenger VALUES (3198155, 'Daniel', 'Washington', '882 Mccall Highway
South Monica, VI 30024', 487781, 68, FALSE, 'platinum');
INSERT INTO passenger VALUES (9908742, 'Steven', 'Wallace', '7923 Stacy Squares Apt. 106
Lake Nancyside, OH 03762', 366933, 17, FALSE, 'gold');
INSERT INTO passenger VALUES (3879592, 'Billy', 'Richardson', 'Unit 5113 Box 3823
DPO AE 43951', 212954, 19, FALSE, 'platinum');
INSERT INTO passenger VALUES (7973380, 'Alyssa', 'Hebert', '723 Brian Wells Apt. 309
Wallchester, OH 16560', 366933, 55, FALSE, 'platinum');
INSERT INTO passenger VALUES (1102212, 'Roger', 'Black', 'Unit 0939 Box 4019
DPO AA 09215', 205348, 29, TRUE, 'basic');
INSERT INTO passenger VALUES (4149608, 'Shaun', 'Campbell', '6971 Robert Alley
East Alexanderfort, CA 09898', 366933, 13, FALSE, 'silver');
INSERT INTO passenger VALUES (9342246, 'Bradley', 'Brown', '8355 Hendricks Fall
Briantown, MN 68796', 492936, 28, TRUE, 'platinum');
INSERT INTO passenger VALUES (1381520, 'Jennifer', 'Kane', 'Unit 8949 Box 9282
DPO AP 78987', 494556, 54, FALSE, 'basic');
INSERT INTO passenger VALUES (1831583, 'Alyssa', 'Harris', '7614 Gary Ville
Andersonmouth, MA 24774', 272062, 25, TRUE, 'platinum');
INSERT INTO passenger VALUES (5603263, 'Nicholas', 'Reed', '9192 Joseph Spurs
South Anthonyborough, FM 48565', 494556, 63, FALSE, 'platinum');
INSERT INTO passenger VALUES (4821607, 'Craig', 'Suarez', '118 Washington Gateway
South Kellymouth, AR 39149', 212954, 79, TRUE, 'platinum');
INSERT INTO passenger VALUES (3179127, 'Daniel', 'Huang', '46245 Ellis Bypass Apt. 155
Hoganville, FM 91145', 400476, 37, TRUE, 'basic');
INSERT INTO passenger VALUES (8121189, 'Kyle', 'Schneider', '773 Hodges Shore Apt. 741
Lake Elizabeth, PA 49595', 188328, 26, TRUE, 'gold');
INSERT INTO passenger VALUES (4141937, 'Christian', 'Weber', '4255 Fisher Plains
Lake Alexabury, OR 45115', 487781, 17, FALSE, 'basic');
INSERT INTO passenger VALUES (9259092, 'Christopher', 'Steele', '7887 Jamie Roads
Wilsonport, TN 67024', 494556, 62, TRUE, 'silver');
INSERT INTO passenger VALUES (5683800, 'Monica', 'Huang', '796 Martinez Manors Apt. 469
Jordanport, NE 97550', 425030, 53, FALSE, 'basic');
INSERT INTO passenger VALUES (1253566, 'Brent', 'Mcmahon', '562 Young Plains Apt. 152
South Davidport, MN 03952', 487781, 45, TRUE, 'silver');
INSERT INTO passenger VALUES (2566046, 'Anna', 'Harper', '466 Smith Gardens
Gonzalesport, AS 27253', 425030, 48, TRUE, 'silver');
INSERT INTO passenger VALUES (6955370, 'Tina', 'Larsen', '5593 Todd Unions Suite 588
Hunterside, WI 58038', 457726, 62, FALSE, 'platinum');
INSERT INTO passenger VALUES (8371206, 'Bethany', 'Davies', '07222 Fry Lodge Apt. 441
East Wesley, NJ 07595', 479677, 77, FALSE, 'gold');
INSERT INTO passenger VALUES (6039236, 'Alison', 'Davis', '410 Scott Mill Suite 223
East Christophertown, OH 34117', 487781, 31, TRUE, 'basic');
INSERT INTO passenger VALUES (3211200, 'Walter', 'Williams', '4498 Young Expressway Apt. 944
East Kristine, MP 22565', 492936, 48, FALSE, 'gold');

-- =========== skymill.flight_instances (generic) ==========

DROP TABLE IF EXISTS flight_instances;

CREATE TABLE flight_instances (
  id INT NOT NULL,
  aircraft_id INT NOT NULL,
  segment_id INT NOT NULL,
  departure_date DATE NOT NULL,
  arrival_date DATE NOT NULL
);

INSERT INTO flight_instances VALUES (4501881, 89828, 188356, '2026-04-04', '2026-01-26');
INSERT INTO flight_instances VALUES (7648863, 32281, 257731, '2026-05-22', '2026-04-08');
INSERT INTO flight_instances VALUES (4072363, 94128, 269791, '2026-04-19', '2026-04-14');
INSERT INTO flight_instances VALUES (5430391, 90925, 141055, '2026-03-08', '2026-01-13');
INSERT INTO flight_instances VALUES (6468600, 80410, 125785, '2026-03-06', '2026-04-29');
INSERT INTO flight_instances VALUES (3533222, 16604, 100367, '2026-03-26', '2026-01-28');
INSERT INTO flight_instances VALUES (8192045, 32977, 125785, '2026-01-30', '2026-01-19');
INSERT INTO flight_instances VALUES (4868063, 39753, 302149, '2026-03-06', '2026-02-11');
INSERT INTO flight_instances VALUES (5631640, 74433, 164033, '2026-05-27', '2026-04-09');
INSERT INTO flight_instances VALUES (2609422, 13051, 430422, '2026-03-29', '2026-03-18');
INSERT INTO flight_instances VALUES (6613650, 15933, 182312, '2026-03-26', '2026-05-27');
INSERT INTO flight_instances VALUES (8928018, 18187, 430422, '2026-02-06', '2026-03-14');
INSERT INTO flight_instances VALUES (9250393, 32977, 147616, '2026-02-27', '2026-01-05');
INSERT INTO flight_instances VALUES (9222958, 79097, 430422, '2026-04-05', '2026-04-02');
INSERT INTO flight_instances VALUES (9649921, 15933, 411471, '2026-05-05', '2026-05-26');
INSERT INTO flight_instances VALUES (5805421, 61836, 188356, '2026-03-02', '2026-03-17');
INSERT INTO flight_instances VALUES (6773294, 32281, 147616, '2026-02-03', '2026-04-04');
INSERT INTO flight_instances VALUES (5537933, 67709, 405071, '2026-01-20', '2026-02-26');
INSERT INTO flight_instances VALUES (7705816, 77021, 268939, '2026-05-08', '2026-05-20');
INSERT INTO flight_instances VALUES (3429260, 32977, 169833, '2026-03-21', '2026-03-17');
INSERT INTO flight_instances VALUES (6058824, 43926, 269791, '2026-04-06', '2026-02-14');
INSERT INTO flight_instances VALUES (2915408, 51878, 236110, '2026-03-28', '2026-02-20');
INSERT INTO flight_instances VALUES (9710039, 43926, 232860, '2026-03-29', '2026-03-25');
INSERT INTO flight_instances VALUES (9327068, 67709, 121058, '2026-02-13', '2026-03-19');
INSERT INTO flight_instances VALUES (6947320, 16604, 429275, '2026-03-23', '2026-05-22');
INSERT INTO flight_instances VALUES (9837984, 18187, 441656, '2026-01-10', '2026-03-07');
INSERT INTO flight_instances VALUES (9805585, 16604, 236110, '2026-02-28', '2026-03-11');
INSERT INTO flight_instances VALUES (5480356, 18187, 460317, '2026-03-09', '2026-04-19');
INSERT INTO flight_instances VALUES (4407456, 70573, 236110, '2026-04-04', '2026-01-02');
INSERT INTO flight_instances VALUES (9468998, 76051, 294313, '2026-02-09', '2026-01-07');
INSERT INTO flight_instances VALUES (8201958, 76051, 385116, '2026-02-27', '2026-05-26');
INSERT INTO flight_instances VALUES (5855521, 84601, 255659, '2026-02-11', '2026-03-21');
INSERT INTO flight_instances VALUES (3077667, 87639, 374635, '2026-02-14', '2026-01-30');
INSERT INTO flight_instances VALUES (7496589, 77021, 374635, '2026-02-17', '2026-03-23');
INSERT INTO flight_instances VALUES (4108742, 89828, 497396, '2026-04-08', '2026-02-15');
INSERT INTO flight_instances VALUES (7323048, 16604, 269791, '2026-01-29', '2026-02-09');
INSERT INTO flight_instances VALUES (5029747, 61836, 232860, '2026-02-09', '2026-02-04');
INSERT INTO flight_instances VALUES (7542327, 76051, 257731, '2026-01-21', '2026-01-09');
INSERT INTO flight_instances VALUES (3229588, 32977, 405071, '2026-05-22', '2026-03-21');
INSERT INTO flight_instances VALUES (2651268, 15933, 294313, '2026-04-11', '2026-05-23');
INSERT INTO flight_instances VALUES (4787447, 18187, 159054, '2026-02-21', '2026-03-13');
INSERT INTO flight_instances VALUES (2979638, 77021, 298831, '2026-02-02', '2026-05-16');
INSERT INTO flight_instances VALUES (8948764, 94719, 159054, '2026-03-26', '2026-03-29');
INSERT INTO flight_instances VALUES (9538900, 70573, 298831, '2026-01-07', '2026-04-07');
INSERT INTO flight_instances VALUES (7663695, 45629, 370062, '2026-03-11', '2026-05-23');
INSERT INTO flight_instances VALUES (4373499, 51878, 232860, '2026-04-14', '2026-03-26');
INSERT INTO flight_instances VALUES (7592431, 77021, 470939, '2026-01-14', '2026-02-23');
INSERT INTO flight_instances VALUES (1184323, 32977, 237788, '2026-02-03', '2026-04-10');
INSERT INTO flight_instances VALUES (9016024, 94719, 470939, '2026-01-26', '2026-05-05');
INSERT INTO flight_instances VALUES (6787581, 87639, 141055, '2026-03-16', '2026-02-19');
INSERT INTO flight_instances VALUES (7112514, 90925, 254959, '2026-04-15', '2026-02-02');
INSERT INTO flight_instances VALUES (1801299, 70573, 405071, '2026-03-13', '2026-05-17');
INSERT INTO flight_instances VALUES (3606936, 71893, 169833, '2026-04-04', '2026-05-29');
INSERT INTO flight_instances VALUES (1112102, 89828, 269791, '2026-04-16', '2026-05-04');
INSERT INTO flight_instances VALUES (1190757, 74433, 239935, '2026-03-27', '2026-05-23');
INSERT INTO flight_instances VALUES (7949387, 61836, 429275, '2026-04-26', '2026-05-24');
INSERT INTO flight_instances VALUES (4163710, 70573, 236110, '2026-04-16', '2026-05-26');
INSERT INTO flight_instances VALUES (8667285, 71893, 497396, '2026-02-19', '2026-04-29');
INSERT INTO flight_instances VALUES (7094975, 61836, 126599, '2026-01-12', '2026-04-26');
INSERT INTO flight_instances VALUES (7275094, 32977, 430422, '2026-01-30', '2026-04-19');
INSERT INTO flight_instances VALUES (2828272, 84601, 497396, '2026-01-26', '2026-01-05');
INSERT INTO flight_instances VALUES (1017327, 51185, 126599, '2026-04-16', '2026-02-12');
INSERT INTO flight_instances VALUES (4229693, 94128, 314357, '2026-05-06', '2026-03-28');
INSERT INTO flight_instances VALUES (2319921, 16604, 206091, '2026-04-06', '2026-03-29');
INSERT INTO flight_instances VALUES (7381080, 23758, 385116, '2026-04-01', '2026-01-04');
INSERT INTO flight_instances VALUES (1135543, 51185, 268939, '2026-02-19', '2026-01-16');
INSERT INTO flight_instances VALUES (7210852, 39753, 147616, '2026-05-08', '2026-01-21');
INSERT INTO flight_instances VALUES (3577336, 32281, 441656, '2026-03-14', '2026-05-14');
INSERT INTO flight_instances VALUES (1594856, 84601, 268939, '2026-01-14', '2026-05-10');
INSERT INTO flight_instances VALUES (6343581, 61836, 464020, '2026-03-22', '2026-05-28');
INSERT INTO flight_instances VALUES (7770284, 67709, 236110, '2026-02-22', '2026-03-12');
INSERT INTO flight_instances VALUES (2031226, 16604, 232860, '2026-05-04', '2026-01-04');
INSERT INTO flight_instances VALUES (4452459, 71893, 338899, '2026-05-22', '2026-05-21');
INSERT INTO flight_instances VALUES (3920680, 18187, 302149, '2026-05-11', '2026-03-13');
INSERT INTO flight_instances VALUES (5667685, 43926, 265717, '2026-05-05', '2026-05-08');
INSERT INTO flight_instances VALUES (1082936, 76051, 188356, '2026-05-12', '2026-04-16');
INSERT INTO flight_instances VALUES (8551136, 51878, 309920, '2026-04-17', '2026-02-05');
INSERT INTO flight_instances VALUES (1460374, 13051, 497396, '2026-02-07', '2026-04-21');
INSERT INTO flight_instances VALUES (9840773, 89828, 405071, '2026-01-24', '2026-03-12');
INSERT INTO flight_instances VALUES (9171738, 45629, 159054, '2026-03-30', '2026-01-28');
INSERT INTO flight_instances VALUES (1025551, 74433, 254959, '2026-01-08', '2026-04-07');
INSERT INTO flight_instances VALUES (8993574, 76051, 268939, '2026-04-11', '2026-05-19');
INSERT INTO flight_instances VALUES (1651685, 32281, 147616, '2026-02-25', '2026-01-23');
INSERT INTO flight_instances VALUES (9086205, 77021, 430422, '2026-05-12', '2026-03-25');
INSERT INTO flight_instances VALUES (1123836, 89828, 143527, '2026-05-09', '2026-04-10');
INSERT INTO flight_instances VALUES (5146931, 67709, 255659, '2026-05-31', '2026-04-06');
INSERT INTO flight_instances VALUES (3443010, 90925, 268939, '2026-01-31', '2026-01-23');
INSERT INTO flight_instances VALUES (1983030, 67709, 265717, '2026-02-04', '2026-03-13');
INSERT INTO flight_instances VALUES (4225434, 45629, 236110, '2026-04-08', '2026-02-03');
INSERT INTO flight_instances VALUES (5518639, 15933, 254959, '2026-01-03', '2026-02-19');
INSERT INTO flight_instances VALUES (6717727, 94719, 237788, '2026-04-13', '2026-01-18');
INSERT INTO flight_instances VALUES (8251078, 71893, 497396, '2026-05-01', '2026-05-12');
INSERT INTO flight_instances VALUES (9857615, 23758, 470939, '2026-01-25', '2026-01-04');
INSERT INTO flight_instances VALUES (4986608, 51878, 487287, '2026-03-01', '2026-01-27');
INSERT INTO flight_instances VALUES (3668668, 43926, 429275, '2026-05-20', '2026-03-23');
INSERT INTO flight_instances VALUES (9148557, 71893, 309920, '2026-01-18', '2026-01-15');
INSERT INTO flight_instances VALUES (1034182, 51185, 358319, '2026-02-12', '2026-03-22');
INSERT INTO flight_instances VALUES (3261263, 16604, 314357, '2026-01-20', '2026-03-11');
INSERT INTO flight_instances VALUES (2472315, 32281, 169833, '2026-03-01', '2026-05-01');
INSERT INTO flight_instances VALUES (1084740, 32281, 159054, '2026-05-02', '2026-02-06');
INSERT INTO flight_instances VALUES (4860180, 45629, 484592, '2026-02-15', '2026-02-25');
INSERT INTO flight_instances VALUES (2963186, 76051, 141055, '2026-03-18', '2026-01-08');
INSERT INTO flight_instances VALUES (3351825, 13051, 410928, '2026-05-10', '2026-03-09');
INSERT INTO flight_instances VALUES (2794631, 21892, 169833, '2026-01-10', '2026-02-07');
INSERT INTO flight_instances VALUES (3548200, 39753, 419693, '2026-02-28', '2026-01-19');
INSERT INTO flight_instances VALUES (1574416, 39753, 153482, '2026-03-28', '2026-02-15');
INSERT INTO flight_instances VALUES (7514371, 21892, 265717, '2026-04-02', '2026-01-24');
INSERT INTO flight_instances VALUES (5974741, 89828, 434648, '2026-03-28', '2026-04-10');
INSERT INTO flight_instances VALUES (7363844, 80410, 338899, '2026-04-15', '2026-02-09');
INSERT INTO flight_instances VALUES (3580596, 79097, 405071, '2026-02-19', '2026-04-05');
INSERT INTO flight_instances VALUES (2134249, 90925, 153482, '2026-05-21', '2026-05-28');
INSERT INTO flight_instances VALUES (8627402, 94128, 370062, '2026-02-26', '2026-05-06');
INSERT INTO flight_instances VALUES (4497548, 61836, 269791, '2026-01-13', '2026-02-27');
INSERT INTO flight_instances VALUES (8399090, 79097, 239935, '2026-02-12', '2026-05-10');
INSERT INTO flight_instances VALUES (1768067, 84601, 169833, '2026-01-06', '2026-04-01');
INSERT INTO flight_instances VALUES (1118405, 32281, 410928, '2026-01-30', '2026-03-01');
INSERT INTO flight_instances VALUES (7663248, 32281, 338899, '2026-01-04', '2026-05-22');
INSERT INTO flight_instances VALUES (4534704, 43926, 419693, '2026-04-02', '2026-05-11');
INSERT INTO flight_instances VALUES (6476144, 16604, 169833, '2026-05-06', '2026-02-12');
INSERT INTO flight_instances VALUES (3380796, 15933, 125785, '2026-02-27', '2026-04-09');
INSERT INTO flight_instances VALUES (8705914, 94719, 143527, '2026-04-02', '2026-02-18');
INSERT INTO flight_instances VALUES (2502371, 79097, 294313, '2026-01-28', '2026-01-14');
INSERT INTO flight_instances VALUES (7279683, 18187, 236110, '2026-05-13', '2026-04-27');
INSERT INTO flight_instances VALUES (4885694, 32977, 432822, '2026-05-08', '2026-04-30');
INSERT INTO flight_instances VALUES (6296538, 32977, 411471, '2026-03-15', '2026-01-05');
INSERT INTO flight_instances VALUES (1220797, 90925, 255659, '2026-02-11', '2026-03-28');
INSERT INTO flight_instances VALUES (2519795, 13051, 188356, '2026-04-09', '2026-01-19');
INSERT INTO flight_instances VALUES (2071840, 79097, 358319, '2026-04-24', '2026-01-29');
INSERT INTO flight_instances VALUES (7938221, 51878, 126599, '2026-01-11', '2026-02-02');
INSERT INTO flight_instances VALUES (4048091, 71893, 270256, '2026-04-23', '2026-05-13');
INSERT INTO flight_instances VALUES (7277897, 77021, 141055, '2026-01-02', '2026-05-29');
INSERT INTO flight_instances VALUES (4055794, 79097, 429275, '2026-03-09', '2026-04-01');
INSERT INTO flight_instances VALUES (3522915, 15933, 419049, '2026-05-31', '2026-03-25');
INSERT INTO flight_instances VALUES (4390317, 74433, 237788, '2026-01-29', '2026-01-27');
INSERT INTO flight_instances VALUES (1472543, 43926, 182312, '2026-03-27', '2026-02-20');
INSERT INTO flight_instances VALUES (7195079, 76051, 470939, '2026-05-11', '2026-04-21');
INSERT INTO flight_instances VALUES (7559111, 71893, 432822, '2026-01-23', '2026-03-08');
INSERT INTO flight_instances VALUES (5483662, 45629, 460317, '2026-02-14', '2026-01-19');
INSERT INTO flight_instances VALUES (8669614, 76051, 182312, '2026-04-10', '2026-05-20');
INSERT INTO flight_instances VALUES (2431798, 18187, 147616, '2026-03-28', '2026-04-09');
INSERT INTO flight_instances VALUES (5682084, 79097, 269791, '2026-04-22', '2026-05-05');
INSERT INTO flight_instances VALUES (9859691, 51185, 460317, '2026-04-14', '2026-04-17');
INSERT INTO flight_instances VALUES (3021844, 71893, 497396, '2026-03-20', '2026-05-30');
INSERT INTO flight_instances VALUES (3564294, 13051, 239935, '2026-05-04', '2026-02-14');
INSERT INTO flight_instances VALUES (1554195, 45629, 100367, '2026-01-03', '2026-02-16');
INSERT INTO flight_instances VALUES (4580667, 16604, 239935, '2026-03-11', '2026-01-09');
INSERT INTO flight_instances VALUES (1362332, 13051, 125785, '2026-01-08', '2026-01-10');
INSERT INTO flight_instances VALUES (8827461, 18187, 460317, '2026-04-18', '2026-03-08');
INSERT INTO flight_instances VALUES (5337919, 32281, 442985, '2026-04-05', '2026-05-22');
INSERT INTO flight_instances VALUES (5546198, 84601, 460317, '2026-02-28', '2026-03-22');
INSERT INTO flight_instances VALUES (1832564, 61836, 239935, '2026-01-26', '2026-01-23');
INSERT INTO flight_instances VALUES (2998984, 90925, 100367, '2026-03-22', '2026-01-26');
INSERT INTO flight_instances VALUES (3232025, 51878, 239935, '2026-01-23', '2026-01-17');
INSERT INTO flight_instances VALUES (2939707, 39753, 159054, '2026-05-01', '2026-02-10');
INSERT INTO flight_instances VALUES (6839691, 94719, 298831, '2026-04-09', '2026-05-28');
INSERT INTO flight_instances VALUES (5160345, 84601, 314357, '2026-02-25', '2026-01-08');
INSERT INTO flight_instances VALUES (7282020, 77021, 419693, '2026-04-23', '2026-04-30');
INSERT INTO flight_instances VALUES (2275551, 90925, 268227, '2026-04-18', '2026-05-10');
INSERT INTO flight_instances VALUES (5460257, 87639, 405071, '2026-02-05', '2026-02-26');
INSERT INTO flight_instances VALUES (7893758, 39753, 153482, '2026-03-27', '2026-02-17');
INSERT INTO flight_instances VALUES (2080096, 87639, 434648, '2026-03-16', '2026-05-12');
INSERT INTO flight_instances VALUES (9376507, 80410, 265717, '2026-05-03', '2026-01-29');
INSERT INTO flight_instances VALUES (7409070, 13051, 236110, '2026-03-02', '2026-04-05');
INSERT INTO flight_instances VALUES (8566074, 16604, 257731, '2026-04-04', '2026-01-22');
INSERT INTO flight_instances VALUES (1933194, 80410, 432822, '2026-01-29', '2026-04-30');
INSERT INTO flight_instances VALUES (7444700, 94719, 309920, '2026-02-16', '2026-01-10');
INSERT INTO flight_instances VALUES (6794537, 94128, 430422, '2026-01-03', '2026-03-05');
INSERT INTO flight_instances VALUES (5410579, 21892, 153482, '2026-02-01', '2026-01-01');
INSERT INTO flight_instances VALUES (1609427, 70573, 419049, '2026-02-08', '2026-05-19');
INSERT INTO flight_instances VALUES (3802445, 18187, 411471, '2026-05-02', '2026-05-11');
INSERT INTO flight_instances VALUES (4075438, 76051, 121058, '2026-02-22', '2026-04-03');
INSERT INTO flight_instances VALUES (4187430, 89828, 268227, '2026-02-22', '2026-05-19');
INSERT INTO flight_instances VALUES (8217563, 21892, 432822, '2026-01-20', '2026-02-04');
INSERT INTO flight_instances VALUES (2934973, 13051, 268939, '2026-04-29', '2026-03-04');
INSERT INTO flight_instances VALUES (4497853, 23758, 410928, '2026-01-24', '2026-04-27');
INSERT INTO flight_instances VALUES (4509881, 45629, 164033, '2026-02-09', '2026-01-10');
INSERT INTO flight_instances VALUES (4186470, 84601, 405071, '2026-04-04', '2026-01-04');
INSERT INTO flight_instances VALUES (1551780, 23758, 126599, '2026-04-25', '2026-02-14');
INSERT INTO flight_instances VALUES (8457243, 16604, 411471, '2026-02-20', '2026-02-26');
INSERT INTO flight_instances VALUES (8499966, 90925, 314357, '2026-03-31', '2026-02-07');
INSERT INTO flight_instances VALUES (5994883, 70573, 141055, '2026-05-13', '2026-02-02');
INSERT INTO flight_instances VALUES (4640846, 79097, 442985, '2026-03-22', '2026-03-02');
INSERT INTO flight_instances VALUES (7525533, 16604, 182312, '2026-03-24', '2026-03-08');
INSERT INTO flight_instances VALUES (2480858, 94128, 309276, '2026-05-17', '2026-05-19');
INSERT INTO flight_instances VALUES (9322743, 45629, 370062, '2026-04-23', '2026-01-11');
INSERT INTO flight_instances VALUES (6540542, 32977, 143527, '2026-01-29', '2026-03-19');
INSERT INTO flight_instances VALUES (6678279, 94128, 442985, '2026-04-13', '2026-03-03');
INSERT INTO flight_instances VALUES (7593448, 84601, 374635, '2026-04-04', '2026-03-19');
INSERT INTO flight_instances VALUES (9582378, 23758, 411471, '2026-04-13', '2026-01-29');
INSERT INTO flight_instances VALUES (3788765, 18187, 254959, '2026-04-26', '2026-01-16');
INSERT INTO flight_instances VALUES (8436230, 16604, 460317, '2026-04-18', '2026-03-08');
INSERT INTO flight_instances VALUES (3896243, 84601, 410928, '2026-02-27', '2026-04-04');
INSERT INTO flight_instances VALUES (1277419, 70573, 100367, '2026-04-12', '2026-01-29');
INSERT INTO flight_instances VALUES (5287611, 13051, 464020, '2026-04-06', '2026-04-18');
INSERT INTO flight_instances VALUES (1952545, 23758, 385116, '2026-03-06', '2026-05-02');
INSERT INTO flight_instances VALUES (3106369, 84601, 268227, '2026-01-18', '2026-04-16');
INSERT INTO flight_instances VALUES (9190463, 94128, 268227, '2026-02-08', '2026-04-28');
INSERT INTO flight_instances VALUES (1190190, 61836, 410928, '2026-04-05', '2026-01-01');
INSERT INTO flight_instances VALUES (2191318, 18187, 411471, '2026-01-21', '2026-03-03');
INSERT INTO flight_instances VALUES (5617426, 18187, 126599, '2026-05-11', '2026-03-18');
INSERT INTO flight_instances VALUES (7341226, 80410, 411471, '2026-03-07', '2026-01-09');
INSERT INTO flight_instances VALUES (7004288, 71893, 182312, '2026-04-26', '2026-03-19');
INSERT INTO flight_instances VALUES (2781589, 43926, 460317, '2026-05-28', '2026-03-18');
INSERT INTO flight_instances VALUES (4953397, 80410, 358319, '2026-03-14', '2026-05-16');
INSERT INTO flight_instances VALUES (8694911, 45629, 100367, '2026-05-19', '2026-04-07');
INSERT INTO flight_instances VALUES (4752878, 43926, 309276, '2026-01-25', '2026-04-13');
INSERT INTO flight_instances VALUES (3577989, 79097, 460317, '2026-04-03', '2026-05-23');
INSERT INTO flight_instances VALUES (9555093, 32977, 441656, '2026-05-08', '2026-05-28');
INSERT INTO flight_instances VALUES (1069385, 80410, 100367, '2026-05-11', '2026-05-18');
INSERT INTO flight_instances VALUES (8168832, 74433, 434648, '2026-05-03', '2026-04-10');
INSERT INTO flight_instances VALUES (2775069, 76051, 206091, '2026-04-07', '2026-02-07');
INSERT INTO flight_instances VALUES (4786071, 90925, 419049, '2026-02-15', '2026-01-08');
INSERT INTO flight_instances VALUES (8820639, 76051, 126599, '2026-05-12', '2026-01-28');
INSERT INTO flight_instances VALUES (6655240, 70573, 164033, '2026-01-14', '2026-03-06');
INSERT INTO flight_instances VALUES (4356416, 76051, 432822, '2026-02-12', '2026-01-24');
INSERT INTO flight_instances VALUES (8047477, 32977, 206091, '2026-02-26', '2026-01-10');
INSERT INTO flight_instances VALUES (5843018, 76051, 410928, '2026-04-29', '2026-01-25');
INSERT INTO flight_instances VALUES (6251192, 89828, 497396, '2026-02-11', '2026-05-09');
INSERT INTO flight_instances VALUES (3061980, 51185, 298831, '2026-02-28', '2026-02-08');
INSERT INTO flight_instances VALUES (3811037, 18187, 470939, '2026-01-16', '2026-01-02');
INSERT INTO flight_instances VALUES (5734273, 89828, 374635, '2026-03-14', '2026-03-18');
INSERT INTO flight_instances VALUES (3817976, 21892, 153482, '2026-03-04', '2026-05-26');
INSERT INTO flight_instances VALUES (3316632, 21892, 100367, '2026-05-25', '2026-04-26');
INSERT INTO flight_instances VALUES (3108257, 43926, 302149, '2026-05-03', '2026-03-07');
INSERT INTO flight_instances VALUES (8539128, 94719, 309276, '2026-03-29', '2026-04-16');
INSERT INTO flight_instances VALUES (5458179, 74433, 374635, '2026-04-02', '2026-01-11');
INSERT INTO flight_instances VALUES (3294929, 51185, 497396, '2026-03-01', '2026-02-15');
INSERT INTO flight_instances VALUES (8357045, 51185, 322186, '2026-04-13', '2026-02-26');
INSERT INTO flight_instances VALUES (5405591, 45629, 460317, '2026-05-31', '2026-04-19');
INSERT INTO flight_instances VALUES (2116507, 51878, 405071, '2026-05-04', '2026-04-26');
INSERT INTO flight_instances VALUES (5074305, 71893, 153482, '2026-01-08', '2026-03-07');
INSERT INTO flight_instances VALUES (8915578, 32977, 237788, '2026-01-18', '2026-03-22');
INSERT INTO flight_instances VALUES (1453128, 84601, 237788, '2026-03-08', '2026-02-04');
INSERT INTO flight_instances VALUES (2443174, 61836, 159054, '2026-01-04', '2026-05-16');
INSERT INTO flight_instances VALUES (3516493, 87639, 434648, '2026-02-27', '2026-04-05');
INSERT INTO flight_instances VALUES (2013782, 61836, 411471, '2026-05-13', '2026-02-12');
INSERT INTO flight_instances VALUES (4841212, 23758, 206091, '2026-01-22', '2026-04-28');
INSERT INTO flight_instances VALUES (1359170, 70573, 125785, '2026-03-10', '2026-05-06');
INSERT INTO flight_instances VALUES (3735507, 79097, 147616, '2026-05-19', '2026-05-25');
INSERT INTO flight_instances VALUES (3791114, 39753, 460317, '2026-02-19', '2026-01-15');
INSERT INTO flight_instances VALUES (3263627, 76051, 358319, '2026-05-03', '2026-03-17');
INSERT INTO flight_instances VALUES (7122531, 94719, 411471, '2026-01-18', '2026-03-15');
INSERT INTO flight_instances VALUES (7172867, 16604, 257731, '2026-04-07', '2026-05-28');
INSERT INTO flight_instances VALUES (1128351, 70573, 206091, '2026-01-08', '2026-03-30');
INSERT INTO flight_instances VALUES (6636874, 90925, 239935, '2026-01-29', '2026-03-17');
INSERT INTO flight_instances VALUES (7039442, 79097, 434648, '2026-04-03', '2026-01-04');
INSERT INTO flight_instances VALUES (5803465, 23758, 309920, '2026-02-06', '2026-03-02');
INSERT INTO flight_instances VALUES (4502860, 61836, 302149, '2026-05-09', '2026-03-04');
INSERT INTO flight_instances VALUES (3302290, 80410, 298831, '2026-01-06', '2026-01-24');
INSERT INTO flight_instances VALUES (8767041, 84601, 126599, '2026-02-04', '2026-03-17');
INSERT INTO flight_instances VALUES (1556467, 76051, 254959, '2026-05-20', '2026-01-21');
INSERT INTO flight_instances VALUES (7604448, 77021, 385116, '2026-01-18', '2026-01-05');
INSERT INTO flight_instances VALUES (6888305, 21892, 322186, '2026-03-22', '2026-01-13');
INSERT INTO flight_instances VALUES (9128197, 18187, 487287, '2026-01-11', '2026-04-23');
INSERT INTO flight_instances VALUES (2434115, 16604, 268939, '2026-05-06', '2026-04-29');
INSERT INTO flight_instances VALUES (7640982, 80410, 487287, '2026-01-19', '2026-05-21');
INSERT INTO flight_instances VALUES (5253868, 70573, 374635, '2026-03-12', '2026-05-04');
INSERT INTO flight_instances VALUES (6538823, 71893, 188356, '2026-02-11', '2026-05-20');
INSERT INTO flight_instances VALUES (7147855, 80410, 434648, '2026-03-16', '2026-03-05');
INSERT INTO flight_instances VALUES (6707929, 76051, 487287, '2026-05-12', '2026-01-21');
INSERT INTO flight_instances VALUES (4165078, 45629, 265717, '2026-02-04', '2026-02-19');
INSERT INTO flight_instances VALUES (8600776, 94719, 239935, '2026-01-06', '2026-03-19');
INSERT INTO flight_instances VALUES (2422914, 70573, 442985, '2026-05-18', '2026-05-18');
INSERT INTO flight_instances VALUES (4224585, 89828, 100367, '2026-03-06', '2026-04-29');
INSERT INTO flight_instances VALUES (1044947, 23758, 374635, '2026-03-20', '2026-05-14');
INSERT INTO flight_instances VALUES (3684041, 51878, 254959, '2026-02-18', '2026-03-30');
INSERT INTO flight_instances VALUES (8311918, 77021, 460317, '2026-03-10', '2026-03-02');
INSERT INTO flight_instances VALUES (4737821, 74433, 265717, '2026-01-09', '2026-04-24');
INSERT INTO flight_instances VALUES (9710137, 23758, 164033, '2026-05-11', '2026-01-14');
INSERT INTO flight_instances VALUES (9269310, 74433, 454442, '2026-03-20', '2026-03-22');
INSERT INTO flight_instances VALUES (4138126, 74433, 153482, '2026-02-12', '2026-01-06');
INSERT INTO flight_instances VALUES (9171399, 18187, 257731, '2026-02-06', '2026-01-28');
INSERT INTO flight_instances VALUES (2718238, 13051, 236110, '2026-01-14', '2026-04-19');
INSERT INTO flight_instances VALUES (9969805, 43926, 338899, '2026-01-27', '2026-04-29');
INSERT INTO flight_instances VALUES (5888739, 32281, 441656, '2026-04-02', '2026-03-30');
INSERT INTO flight_instances VALUES (2297664, 51185, 374635, '2026-03-16', '2026-03-18');
INSERT INTO flight_instances VALUES (3191211, 90925, 487287, '2026-04-12', '2026-01-30');
INSERT INTO flight_instances VALUES (4205310, 45629, 206091, '2026-04-16', '2026-02-24');
INSERT INTO flight_instances VALUES (8600080, 51878, 338899, '2026-02-27', '2026-05-09');
INSERT INTO flight_instances VALUES (4911547, 79097, 265717, '2026-04-28', '2026-03-13');
INSERT INTO flight_instances VALUES (3229060, 90925, 182312, '2026-01-04', '2026-01-30');
INSERT INTO flight_instances VALUES (3868550, 32281, 268939, '2026-05-23', '2026-05-28');
INSERT INTO flight_instances VALUES (1003920, 76051, 442985, '2026-01-22', '2026-01-14');
INSERT INTO flight_instances VALUES (9469884, 21892, 497396, '2026-04-16', '2026-05-12');
INSERT INTO flight_instances VALUES (6938430, 15933, 100367, '2026-02-22', '2026-01-18');
INSERT INTO flight_instances VALUES (4082860, 21892, 454442, '2026-01-20', '2026-05-21');
INSERT INTO flight_instances VALUES (1939454, 77021, 143527, '2026-01-01', '2026-03-21');
INSERT INTO flight_instances VALUES (4172964, 21892, 237788, '2026-03-10', '2026-01-27');
INSERT INTO flight_instances VALUES (7944595, 51878, 441656, '2026-03-30', '2026-02-22');
INSERT INTO flight_instances VALUES (7388768, 13051, 100367, '2026-01-01', '2026-03-24');
INSERT INTO flight_instances VALUES (8425634, 79097, 159054, '2026-03-22', '2026-05-15');
INSERT INTO flight_instances VALUES (1038948, 21892, 126599, '2026-01-13', '2026-04-28');
INSERT INTO flight_instances VALUES (5827465, 89828, 100367, '2026-01-19', '2026-02-06');
INSERT INTO flight_instances VALUES (9235735, 13051, 125785, '2026-01-25', '2026-04-06');
INSERT INTO flight_instances VALUES (6528552, 87639, 270256, '2026-03-26', '2026-03-07');
INSERT INTO flight_instances VALUES (4058181, 16604, 314357, '2026-02-11', '2026-03-21');
INSERT INTO flight_instances VALUES (6982308, 94128, 309920, '2026-05-02', '2026-02-04');
INSERT INTO flight_instances VALUES (2117824, 21892, 430422, '2026-04-30', '2026-03-01');
INSERT INTO flight_instances VALUES (9676209, 67709, 206091, '2026-05-27', '2026-03-22');
INSERT INTO flight_instances VALUES (3382876, 16604, 268227, '2026-01-06', '2026-03-03');
INSERT INTO flight_instances VALUES (1595358, 39753, 338899, '2026-03-01', '2026-01-20');
INSERT INTO flight_instances VALUES (7759286, 87639, 434648, '2026-04-29', '2026-01-13');
INSERT INTO flight_instances VALUES (4077580, 84601, 302149, '2026-01-04', '2026-04-22');
INSERT INTO flight_instances VALUES (7848741, 76051, 358319, '2026-04-03', '2026-04-30');
INSERT INTO flight_instances VALUES (4768588, 76051, 147616, '2026-03-05', '2026-01-21');
INSERT INTO flight_instances VALUES (8736781, 70573, 121058, '2026-03-09', '2026-02-10');
INSERT INTO flight_instances VALUES (6396837, 94128, 434648, '2026-03-26', '2026-03-15');
INSERT INTO flight_instances VALUES (4997378, 90925, 147616, '2026-01-18', '2026-01-29');
INSERT INTO flight_instances VALUES (7094923, 15933, 429275, '2026-05-11', '2026-03-15');
INSERT INTO flight_instances VALUES (7569872, 94719, 302149, '2026-01-19', '2026-02-17');
INSERT INTO flight_instances VALUES (5041690, 15933, 298831, '2026-01-08', '2026-04-16');
INSERT INTO flight_instances VALUES (2566204, 71893, 298831, '2026-02-11', '2026-03-17');
INSERT INTO flight_instances VALUES (4162870, 74433, 232860, '2026-05-25', '2026-05-09');
INSERT INTO flight_instances VALUES (3868324, 87639, 298831, '2026-03-04', '2026-04-11');
INSERT INTO flight_instances VALUES (3580504, 70573, 159054, '2026-02-12', '2026-02-04');
INSERT INTO flight_instances VALUES (7618732, 51878, 454442, '2026-02-28', '2026-01-30');
INSERT INTO flight_instances VALUES (4971923, 21892, 410928, '2026-01-30', '2026-04-04');
INSERT INTO flight_instances VALUES (5763191, 71893, 236110, '2026-01-18', '2026-03-21');
INSERT INTO flight_instances VALUES (9279751, 32281, 121058, '2026-05-15', '2026-01-31');
INSERT INTO flight_instances VALUES (4156979, 90925, 430422, '2026-03-30', '2026-03-12');
INSERT INTO flight_instances VALUES (9647986, 71893, 302149, '2026-03-01', '2026-01-01');
INSERT INTO flight_instances VALUES (1850920, 79097, 270256, '2026-03-15', '2026-01-03');
INSERT INTO flight_instances VALUES (4571129, 70573, 441656, '2026-05-27', '2026-03-19');
INSERT INTO flight_instances VALUES (5017380, 23758, 206091, '2026-01-09', '2026-03-26');
INSERT INTO flight_instances VALUES (3061880, 13051, 268227, '2026-03-06', '2026-04-08');
INSERT INTO flight_instances VALUES (7220120, 23758, 255659, '2026-01-09', '2026-05-19');
INSERT INTO flight_instances VALUES (5351188, 94128, 268227, '2026-04-17', '2026-03-12');
INSERT INTO flight_instances VALUES (6273381, 32977, 159054, '2026-01-25', '2026-04-21');
INSERT INTO flight_instances VALUES (1170704, 39753, 464020, '2026-03-10', '2026-05-25');
INSERT INTO flight_instances VALUES (1930953, 21892, 442985, '2026-03-29', '2026-04-18');
INSERT INTO flight_instances VALUES (1617454, 45629, 484592, '2026-05-01', '2026-04-24');
INSERT INTO flight_instances VALUES (9311534, 21892, 237788, '2026-01-22', '2026-05-24');
INSERT INTO flight_instances VALUES (2939418, 13051, 338899, '2026-05-12', '2026-04-14');
INSERT INTO flight_instances VALUES (7432459, 13051, 232860, '2026-02-23', '2026-01-18');
INSERT INTO flight_instances VALUES (5354373, 21892, 188356, '2026-01-10', '2026-04-09');
INSERT INTO flight_instances VALUES (6384465, 15933, 121058, '2026-04-08', '2026-01-17');
INSERT INTO flight_instances VALUES (2727975, 51878, 121058, '2026-04-26', '2026-03-07');
INSERT INTO flight_instances VALUES (4050052, 87639, 454442, '2026-03-06', '2026-02-05');
INSERT INTO flight_instances VALUES (6920189, 74433, 159054, '2026-04-21', '2026-04-28');
INSERT INTO flight_instances VALUES (2111920, 84601, 153482, '2026-03-13', '2026-02-22');
INSERT INTO flight_instances VALUES (8574016, 77021, 153482, '2026-03-19', '2026-05-28');
INSERT INTO flight_instances VALUES (6260587, 39753, 497396, '2026-02-23', '2026-01-30');
INSERT INTO flight_instances VALUES (7260938, 94719, 470939, '2026-02-15', '2026-02-03');
INSERT INTO flight_instances VALUES (5707208, 89828, 434648, '2026-04-13', '2026-04-13');
INSERT INTO flight_instances VALUES (7312338, 94719, 497396, '2026-04-10', '2026-02-24');
INSERT INTO flight_instances VALUES (3815787, 43926, 265717, '2026-04-16', '2026-01-27');
INSERT INTO flight_instances VALUES (1101672, 32281, 270256, '2026-05-08', '2026-01-05');
INSERT INTO flight_instances VALUES (6009573, 77021, 405071, '2026-04-17', '2026-05-18');
INSERT INTO flight_instances VALUES (3162135, 76051, 100367, '2026-05-03', '2026-03-02');
INSERT INTO flight_instances VALUES (8832974, 13051, 410928, '2026-05-24', '2026-01-10');
INSERT INTO flight_instances VALUES (5032808, 71893, 237788, '2026-03-15', '2026-04-08');
INSERT INTO flight_instances VALUES (3607379, 32281, 164033, '2026-01-12', '2026-01-09');
INSERT INTO flight_instances VALUES (4512923, 90925, 239935, '2026-04-08', '2026-05-20');
INSERT INTO flight_instances VALUES (3735790, 79097, 141055, '2026-04-08', '2026-01-15');
INSERT INTO flight_instances VALUES (7648856, 15933, 268227, '2026-02-09', '2026-01-09');
INSERT INTO flight_instances VALUES (1807291, 21892, 410928, '2026-05-13', '2026-03-02');
INSERT INTO flight_instances VALUES (6683219, 89828, 302149, '2026-03-02', '2026-03-03');
INSERT INTO flight_instances VALUES (2827252, 84601, 497396, '2026-01-11', '2026-02-07');
INSERT INTO flight_instances VALUES (1742490, 61836, 269791, '2026-01-02', '2026-01-30');
INSERT INTO flight_instances VALUES (3337487, 39753, 206091, '2026-02-27', '2026-03-18');
INSERT INTO flight_instances VALUES (2855984, 94719, 370062, '2026-05-04', '2026-04-03');
INSERT INTO flight_instances VALUES (8665254, 87639, 309920, '2026-05-03', '2026-03-30');
INSERT INTO flight_instances VALUES (4026701, 21892, 374635, '2026-01-07', '2026-02-13');
INSERT INTO flight_instances VALUES (9417673, 61836, 169833, '2026-02-22', '2026-04-16');
INSERT INTO flight_instances VALUES (8360355, 80410, 141055, '2026-01-20', '2026-03-03');
INSERT INTO flight_instances VALUES (8509070, 79097, 419049, '2026-04-03', '2026-01-11');
INSERT INTO flight_instances VALUES (2094934, 79097, 237788, '2026-05-30', '2026-03-28');
INSERT INTO flight_instances VALUES (4035695, 13051, 125785, '2026-05-09', '2026-05-25');
INSERT INTO flight_instances VALUES (9343616, 23758, 441656, '2026-02-04', '2026-02-11');
INSERT INTO flight_instances VALUES (9474064, 71893, 232860, '2026-01-08', '2026-01-23');
INSERT INTO flight_instances VALUES (8280348, 39753, 169833, '2026-01-29', '2026-02-05');
INSERT INTO flight_instances VALUES (6884610, 32977, 497396, '2026-04-10', '2026-04-04');
INSERT INTO flight_instances VALUES (8487202, 45629, 464020, '2026-03-11', '2026-02-15');
INSERT INTO flight_instances VALUES (4942822, 43926, 164033, '2026-01-30', '2026-01-07');
INSERT INTO flight_instances VALUES (5420546, 71893, 188356, '2026-04-01', '2026-04-19');
INSERT INTO flight_instances VALUES (5245347, 21892, 268939, '2026-03-30', '2026-02-04');
INSERT INTO flight_instances VALUES (6288275, 61836, 268227, '2026-02-11', '2026-04-18');
INSERT INTO flight_instances VALUES (7300889, 90925, 265717, '2026-01-20', '2026-05-04');
INSERT INTO flight_instances VALUES (8360579, 15933, 147616, '2026-01-29', '2026-05-14');
INSERT INTO flight_instances VALUES (4388944, 51185, 265717, '2026-04-20', '2026-05-12');
INSERT INTO flight_instances VALUES (1658973, 16604, 442985, '2026-01-07', '2026-02-22');
INSERT INTO flight_instances VALUES (7165388, 87639, 269791, '2026-04-27', '2026-05-10');
INSERT INTO flight_instances VALUES (7405213, 77021, 464020, '2026-03-17', '2026-05-29');
INSERT INTO flight_instances VALUES (6898752, 84601, 405071, '2026-03-18', '2026-05-08');
INSERT INTO flight_instances VALUES (8641311, 16604, 143527, '2026-03-28', '2026-03-14');
INSERT INTO flight_instances VALUES (7522865, 45629, 430422, '2026-05-30', '2026-05-17');
INSERT INTO flight_instances VALUES (7154876, 61836, 143527, '2026-01-20', '2026-03-01');
INSERT INTO flight_instances VALUES (5028809, 21892, 164033, '2026-01-14', '2026-04-03');
INSERT INTO flight_instances VALUES (7821647, 94719, 405071, '2026-01-15', '2026-02-12');
INSERT INTO flight_instances VALUES (9494788, 79097, 442985, '2026-04-15', '2026-03-31');
INSERT INTO flight_instances VALUES (7207091, 74433, 206091, '2026-01-28', '2026-05-25');
INSERT INTO flight_instances VALUES (7843855, 90925, 268939, '2026-05-30', '2026-03-05');
INSERT INTO flight_instances VALUES (8522282, 45629, 294313, '2026-04-20', '2026-01-15');
INSERT INTO flight_instances VALUES (8795653, 13051, 143527, '2026-04-11', '2026-03-19');
INSERT INTO flight_instances VALUES (8023566, 51878, 121058, '2026-05-01', '2026-02-20');
INSERT INTO flight_instances VALUES (6411045, 71893, 269791, '2026-04-10', '2026-03-09');
INSERT INTO flight_instances VALUES (9608933, 21892, 159054, '2026-04-15', '2026-03-30');
INSERT INTO flight_instances VALUES (8841927, 90925, 153482, '2026-01-14', '2026-01-08');
INSERT INTO flight_instances VALUES (2428956, 45629, 257731, '2026-05-28', '2026-05-13');
INSERT INTO flight_instances VALUES (7243278, 76051, 268227, '2026-03-09', '2026-01-07');
INSERT INTO flight_instances VALUES (8882848, 84601, 419049, '2026-04-27', '2026-03-21');
INSERT INTO flight_instances VALUES (1950871, 51185, 419049, '2026-01-29', '2026-03-11');
INSERT INTO flight_instances VALUES (3984898, 51185, 405071, '2026-05-17', '2026-02-12');
INSERT INTO flight_instances VALUES (1148814, 45629, 314357, '2026-01-28', '2026-02-20');
INSERT INTO flight_instances VALUES (8733132, 32281, 182312, '2026-05-14', '2026-05-28');
INSERT INTO flight_instances VALUES (7094490, 70573, 411471, '2026-04-21', '2026-04-06');
INSERT INTO flight_instances VALUES (8319166, 84601, 125785, '2026-05-16', '2026-05-29');
INSERT INTO flight_instances VALUES (6433324, 32977, 419049, '2026-03-11', '2026-04-17');
INSERT INTO flight_instances VALUES (4885859, 21892, 358319, '2026-01-21', '2026-01-13');
INSERT INTO flight_instances VALUES (3107948, 79097, 302149, '2026-01-24', '2026-05-02');
INSERT INTO flight_instances VALUES (9795505, 51185, 268939, '2026-02-15', '2026-04-07');
INSERT INTO flight_instances VALUES (9678194, 71893, 411471, '2026-02-09', '2026-02-19');
INSERT INTO flight_instances VALUES (8356612, 71893, 206091, '2026-03-17', '2026-03-09');
INSERT INTO flight_instances VALUES (2880750, 80410, 265717, '2026-04-13', '2026-01-28');
INSERT INTO flight_instances VALUES (4762283, 16604, 322186, '2026-01-01', '2026-05-07');
INSERT INTO flight_instances VALUES (9112856, 74433, 487287, '2026-04-22', '2026-01-30');
INSERT INTO flight_instances VALUES (4492728, 74433, 182312, '2026-05-03', '2026-01-21');
INSERT INTO flight_instances VALUES (4493690, 87639, 164033, '2026-01-09', '2026-05-11');
INSERT INTO flight_instances VALUES (9782865, 74433, 434648, '2026-01-17', '2026-03-03');
INSERT INTO flight_instances VALUES (7850066, 18187, 100367, '2026-01-06', '2026-05-06');

-- =========== skymill.cargo_flights (generic) ==========

DROP TABLE IF EXISTS cargo_flights;

CREATE TABLE cargo_flights (
  id INT NOT NULL,
  aircraft_id INT NOT NULL,
  segment_id INT NOT NULL,
  departure_date DATE NOT NULL,
  arrival_date DATE NOT NULL
);

INSERT INTO cargo_flights VALUES (208059, 89828, 370062, '2026-03-31', '2026-03-23');
INSERT INTO cargo_flights VALUES (982325, 84601, 434648, '2026-02-14', '2026-01-27');
INSERT INTO cargo_flights VALUES (486096, 67709, 454442, '2026-05-23', '2026-02-09');
INSERT INTO cargo_flights VALUES (365267, 21892, 419049, '2026-01-14', '2026-04-30');
INSERT INTO cargo_flights VALUES (459618, 76051, 143527, '2026-01-20', '2026-01-24');
INSERT INTO cargo_flights VALUES (374146, 94128, 487287, '2026-03-10', '2026-02-23');
INSERT INTO cargo_flights VALUES (650161, 13051, 429275, '2026-03-08', '2026-04-17');
INSERT INTO cargo_flights VALUES (295660, 71893, 182312, '2026-04-03', '2026-05-01');
INSERT INTO cargo_flights VALUES (546827, 18187, 298831, '2026-01-02', '2026-02-19');
INSERT INTO cargo_flights VALUES (306162, 61836, 470939, '2026-01-19', '2026-05-27');
INSERT INTO cargo_flights VALUES (624781, 18187, 309276, '2026-01-12', '2026-05-05');
INSERT INTO cargo_flights VALUES (916015, 74433, 239935, '2026-02-02', '2026-01-29');
INSERT INTO cargo_flights VALUES (587882, 77021, 206091, '2026-02-03', '2026-05-11');
INSERT INTO cargo_flights VALUES (614215, 45629, 411471, '2026-03-10', '2026-04-07');
INSERT INTO cargo_flights VALUES (342326, 87639, 121058, '2026-03-29', '2026-05-31');
INSERT INTO cargo_flights VALUES (853530, 39753, 430422, '2026-04-23', '2026-03-10');
INSERT INTO cargo_flights VALUES (955811, 87639, 100367, '2026-03-29', '2026-05-17');
INSERT INTO cargo_flights VALUES (651365, 67709, 257731, '2026-04-20', '2026-05-31');
INSERT INTO cargo_flights VALUES (971983, 15933, 164033, '2026-03-19', '2026-03-02');
INSERT INTO cargo_flights VALUES (588014, 43926, 239935, '2026-04-10', '2026-01-28');
INSERT INTO cargo_flights VALUES (702659, 61836, 237788, '2026-02-28', '2026-03-21');
INSERT INTO cargo_flights VALUES (286425, 71893, 405071, '2026-03-05', '2026-02-25');
INSERT INTO cargo_flights VALUES (222414, 74433, 121058, '2026-05-31', '2026-03-06');
INSERT INTO cargo_flights VALUES (998345, 90925, 419693, '2026-01-22', '2026-03-24');
INSERT INTO cargo_flights VALUES (265945, 18187, 121058, '2026-02-15', '2026-04-08');
INSERT INTO cargo_flights VALUES (607760, 15933, 153482, '2026-02-16', '2026-01-03');
INSERT INTO cargo_flights VALUES (887047, 89828, 432822, '2026-01-30', '2026-04-03');
INSERT INTO cargo_flights VALUES (371982, 89828, 464020, '2026-05-30', '2026-02-22');
INSERT INTO cargo_flights VALUES (995741, 76051, 298831, '2026-01-11', '2026-04-09');
INSERT INTO cargo_flights VALUES (667744, 79097, 411471, '2026-03-20', '2026-02-24');
INSERT INTO cargo_flights VALUES (481053, 71893, 188356, '2026-01-10', '2026-05-01');
INSERT INTO cargo_flights VALUES (710029, 39753, 432822, '2026-04-19', '2026-03-10');
INSERT INTO cargo_flights VALUES (378721, 32281, 294313, '2026-03-30', '2026-05-12');
INSERT INTO cargo_flights VALUES (215106, 94719, 338899, '2026-02-03', '2026-04-01');
INSERT INTO cargo_flights VALUES (593162, 71893, 454442, '2026-05-04', '2026-01-11');
INSERT INTO cargo_flights VALUES (970733, 94719, 338899, '2026-05-27', '2026-04-18');
INSERT INTO cargo_flights VALUES (173340, 16604, 298831, '2026-01-29', '2026-05-07');
INSERT INTO cargo_flights VALUES (566273, 32281, 464020, '2026-04-28', '2026-05-29');
INSERT INTO cargo_flights VALUES (619695, 43926, 358319, '2026-04-14', '2026-01-27');
INSERT INTO cargo_flights VALUES (730182, 13051, 358319, '2026-03-06', '2026-04-08');
INSERT INTO cargo_flights VALUES (567832, 43926, 125785, '2026-04-19', '2026-03-17');
INSERT INTO cargo_flights VALUES (686608, 61836, 298831, '2026-05-18', '2026-04-21');
INSERT INTO cargo_flights VALUES (835095, 94719, 442985, '2026-04-20', '2026-03-24');
INSERT INTO cargo_flights VALUES (404391, 79097, 322186, '2026-02-16', '2026-04-20');
INSERT INTO cargo_flights VALUES (291137, 70573, 153482, '2026-01-06', '2026-02-01');
INSERT INTO cargo_flights VALUES (947049, 21892, 126599, '2026-01-05', '2026-01-07');
INSERT INTO cargo_flights VALUES (807747, 61836, 100367, '2026-05-11', '2026-05-31');
INSERT INTO cargo_flights VALUES (371841, 71893, 294313, '2026-01-13', '2026-04-02');
INSERT INTO cargo_flights VALUES (311617, 15933, 236110, '2026-02-07', '2026-02-28');
INSERT INTO cargo_flights VALUES (294483, 16604, 236110, '2026-05-02', '2026-04-09');
INSERT INTO cargo_flights VALUES (805689, 51185, 442985, '2026-02-26', '2026-04-09');
INSERT INTO cargo_flights VALUES (484116, 77021, 484592, '2026-03-29', '2026-01-28');
INSERT INTO cargo_flights VALUES (336949, 51878, 464020, '2026-03-15', '2026-04-16');
INSERT INTO cargo_flights VALUES (919657, 61836, 338899, '2026-05-15', '2026-01-31');
INSERT INTO cargo_flights VALUES (303968, 61836, 432822, '2026-03-12', '2026-03-25');
INSERT INTO cargo_flights VALUES (817590, 79097, 141055, '2026-04-21', '2026-04-17');
INSERT INTO cargo_flights VALUES (335981, 79097, 309276, '2026-03-03', '2026-05-10');
INSERT INTO cargo_flights VALUES (735758, 84601, 298831, '2026-02-13', '2026-02-12');
INSERT INTO cargo_flights VALUES (368069, 51185, 125785, '2026-01-12', '2026-04-21');
INSERT INTO cargo_flights VALUES (295088, 51185, 309276, '2026-01-04', '2026-03-24');
INSERT INTO cargo_flights VALUES (993138, 89828, 374635, '2026-01-22', '2026-04-17');
INSERT INTO cargo_flights VALUES (961289, 23758, 442985, '2026-03-12', '2026-04-16');
INSERT INTO cargo_flights VALUES (681816, 43926, 257731, '2026-01-11', '2026-03-18');
INSERT INTO cargo_flights VALUES (244904, 71893, 385116, '2026-05-14', '2026-03-25');
INSERT INTO cargo_flights VALUES (196131, 39753, 484592, '2026-05-24', '2026-05-21');
INSERT INTO cargo_flights VALUES (849862, 32281, 410928, '2026-04-02', '2026-05-11');
INSERT INTO cargo_flights VALUES (242407, 76051, 237788, '2026-01-14', '2026-03-13');
INSERT INTO cargo_flights VALUES (560171, 16604, 358319, '2026-05-12', '2026-03-15');
INSERT INTO cargo_flights VALUES (130564, 61836, 484592, '2026-03-01', '2026-02-24');
INSERT INTO cargo_flights VALUES (898133, 51185, 358319, '2026-05-10', '2026-01-02');
INSERT INTO cargo_flights VALUES (225405, 23758, 164033, '2026-05-19', '2026-03-11');
INSERT INTO cargo_flights VALUES (630921, 32281, 419693, '2026-04-02', '2026-01-13');
INSERT INTO cargo_flights VALUES (371308, 74433, 370062, '2026-04-25', '2026-05-19');
INSERT INTO cargo_flights VALUES (900498, 45629, 270256, '2026-05-25', '2026-04-21');
INSERT INTO cargo_flights VALUES (988582, 79097, 125785, '2026-01-17', '2026-03-06');
INSERT INTO cargo_flights VALUES (536454, 70573, 169833, '2026-03-02', '2026-01-23');
INSERT INTO cargo_flights VALUES (614562, 80410, 143527, '2026-02-23', '2026-02-22');
INSERT INTO cargo_flights VALUES (372521, 79097, 419049, '2026-01-22', '2026-04-15');
INSERT INTO cargo_flights VALUES (104116, 21892, 410928, '2026-04-23', '2026-05-30');
INSERT INTO cargo_flights VALUES (158596, 67709, 159054, '2026-05-04', '2026-05-17');
INSERT INTO cargo_flights VALUES (536772, 94719, 257731, '2026-01-04', '2026-04-03');
INSERT INTO cargo_flights VALUES (194682, 21892, 484592, '2026-03-08', '2026-04-12');
INSERT INTO cargo_flights VALUES (214614, 67709, 100367, '2026-02-14', '2026-03-03');
INSERT INTO cargo_flights VALUES (895631, 87639, 298831, '2026-01-23', '2026-05-27');
INSERT INTO cargo_flights VALUES (657656, 70573, 188356, '2026-03-08', '2026-03-08');
INSERT INTO cargo_flights VALUES (388891, 70573, 237788, '2026-04-04', '2026-04-10');
INSERT INTO cargo_flights VALUES (593750, 84601, 410928, '2026-05-21', '2026-05-17');
INSERT INTO cargo_flights VALUES (200279, 21892, 159054, '2026-05-03', '2026-02-14');
INSERT INTO cargo_flights VALUES (466776, 89828, 298831, '2026-03-22', '2026-03-12');
INSERT INTO cargo_flights VALUES (558631, 89828, 237788, '2026-01-19', '2026-04-02');
INSERT INTO cargo_flights VALUES (513030, 43926, 385116, '2026-05-09', '2026-01-26');
INSERT INTO cargo_flights VALUES (834586, 89828, 497396, '2026-04-16', '2026-05-16');
INSERT INTO cargo_flights VALUES (886388, 94719, 126599, '2026-04-24', '2026-03-10');
INSERT INTO cargo_flights VALUES (388399, 61836, 237788, '2026-05-07', '2026-02-27');
INSERT INTO cargo_flights VALUES (931397, 94128, 159054, '2026-04-24', '2026-04-22');
INSERT INTO cargo_flights VALUES (120378, 39753, 314357, '2026-04-11', '2026-04-09');
INSERT INTO cargo_flights VALUES (563359, 79097, 206091, '2026-05-17', '2026-05-13');
INSERT INTO cargo_flights VALUES (843961, 79097, 232860, '2026-04-03', '2026-01-12');
INSERT INTO cargo_flights VALUES (705140, 51185, 374635, '2026-01-20', '2026-04-16');
INSERT INTO cargo_flights VALUES (964443, 70573, 188356, '2026-02-17', '2026-05-19');
INSERT INTO cargo_flights VALUES (679962, 94719, 100367, '2026-05-04', '2026-04-01');
INSERT INTO cargo_flights VALUES (443855, 87639, 268227, '2026-02-28', '2026-04-26');
INSERT INTO cargo_flights VALUES (919415, 74433, 298831, '2026-03-25', '2026-03-26');
INSERT INTO cargo_flights VALUES (613587, 94719, 143527, '2026-01-05', '2026-05-03');
INSERT INTO cargo_flights VALUES (630050, 18187, 419049, '2026-04-20', '2026-01-04');
INSERT INTO cargo_flights VALUES (218458, 21892, 487287, '2026-01-26', '2026-04-06');
INSERT INTO cargo_flights VALUES (816550, 94719, 454442, '2026-02-04', '2026-01-23');
INSERT INTO cargo_flights VALUES (818702, 77021, 269791, '2026-02-25', '2026-05-04');
INSERT INTO cargo_flights VALUES (952986, 21892, 232860, '2026-04-22', '2026-05-26');
INSERT INTO cargo_flights VALUES (599462, 77021, 153482, '2026-04-27', '2026-05-20');
INSERT INTO cargo_flights VALUES (346198, 80410, 268227, '2026-05-05', '2026-04-15');
INSERT INTO cargo_flights VALUES (771720, 16604, 236110, '2026-04-17', '2026-04-17');
INSERT INTO cargo_flights VALUES (342224, 23758, 232860, '2026-01-01', '2026-05-25');
INSERT INTO cargo_flights VALUES (157492, 90925, 298831, '2026-05-19', '2026-04-25');
INSERT INTO cargo_flights VALUES (618905, 80410, 419693, '2026-05-19', '2026-04-03');
INSERT INTO cargo_flights VALUES (283188, 32977, 294313, '2026-02-06', '2026-04-22');
INSERT INTO cargo_flights VALUES (947599, 74433, 182312, '2026-01-14', '2026-02-16');
INSERT INTO cargo_flights VALUES (864161, 16604, 302149, '2026-02-13', '2026-05-06');
INSERT INTO cargo_flights VALUES (617268, 21892, 269791, '2026-03-12', '2026-02-04');
INSERT INTO cargo_flights VALUES (559148, 94719, 454442, '2026-02-17', '2026-03-05');
INSERT INTO cargo_flights VALUES (804924, 90925, 269791, '2026-01-25', '2026-02-16');
INSERT INTO cargo_flights VALUES (173966, 76051, 429275, '2026-04-09', '2026-01-14');
INSERT INTO cargo_flights VALUES (951732, 45629, 405071, '2026-05-31', '2026-05-29');
INSERT INTO cargo_flights VALUES (482618, 71893, 143527, '2026-03-07', '2026-02-26');
INSERT INTO cargo_flights VALUES (528479, 89828, 460317, '2026-05-09', '2026-04-01');
INSERT INTO cargo_flights VALUES (271822, 32281, 460317, '2026-03-11', '2026-03-14');
INSERT INTO cargo_flights VALUES (365226, 45629, 302149, '2026-02-20', '2026-01-25');
INSERT INTO cargo_flights VALUES (745952, 94719, 309276, '2026-02-27', '2026-02-18');
INSERT INTO cargo_flights VALUES (218188, 18187, 309276, '2026-01-14', '2026-04-03');
INSERT INTO cargo_flights VALUES (734764, 18187, 294313, '2026-02-06', '2026-05-20');
INSERT INTO cargo_flights VALUES (623654, 71893, 405071, '2026-01-15', '2026-03-12');
INSERT INTO cargo_flights VALUES (331143, 18187, 206091, '2026-04-03', '2026-01-19');
INSERT INTO cargo_flights VALUES (411362, 16604, 268939, '2026-04-12', '2026-02-15');
INSERT INTO cargo_flights VALUES (832152, 74433, 153482, '2026-01-21', '2026-01-06');
INSERT INTO cargo_flights VALUES (940727, 76051, 405071, '2026-05-14', '2026-05-07');
INSERT INTO cargo_flights VALUES (382201, 13051, 484592, '2026-03-01', '2026-04-27');
INSERT INTO cargo_flights VALUES (785591, 76051, 470939, '2026-01-06', '2026-04-01');
INSERT INTO cargo_flights VALUES (954861, 84601, 411471, '2026-02-06', '2026-03-17');
INSERT INTO cargo_flights VALUES (562349, 45629, 470939, '2026-02-17', '2026-01-22');
INSERT INTO cargo_flights VALUES (108703, 79097, 464020, '2026-03-01', '2026-03-06');
INSERT INTO cargo_flights VALUES (240561, 39753, 164033, '2026-04-04', '2026-04-05');
INSERT INTO cargo_flights VALUES (554618, 13051, 257731, '2026-04-15', '2026-03-05');
INSERT INTO cargo_flights VALUES (477350, 87639, 470939, '2026-05-10', '2026-04-21');
INSERT INTO cargo_flights VALUES (885046, 13051, 497396, '2026-03-15', '2026-02-20');
INSERT INTO cargo_flights VALUES (622340, 45629, 385116, '2026-02-19', '2026-03-02');
INSERT INTO cargo_flights VALUES (622034, 39753, 338899, '2026-05-05', '2026-01-08');
INSERT INTO cargo_flights VALUES (839442, 71893, 126599, '2026-02-23', '2026-05-09');
INSERT INTO cargo_flights VALUES (366964, 51185, 143527, '2026-04-22', '2026-03-11');
INSERT INTO cargo_flights VALUES (132399, 51878, 470939, '2026-05-16', '2026-03-17');
INSERT INTO cargo_flights VALUES (430854, 74433, 338899, '2026-02-15', '2026-03-25');
INSERT INTO cargo_flights VALUES (475571, 15933, 432822, '2026-02-04', '2026-02-27');
INSERT INTO cargo_flights VALUES (751755, 51878, 405071, '2026-02-07', '2026-02-14');
INSERT INTO cargo_flights VALUES (861877, 21892, 268227, '2026-03-23', '2026-05-15');
INSERT INTO cargo_flights VALUES (995615, 61836, 410928, '2026-04-26', '2026-05-23');
INSERT INTO cargo_flights VALUES (843101, 77021, 405071, '2026-04-16', '2026-02-14');
INSERT INTO cargo_flights VALUES (264687, 18187, 410928, '2026-02-16', '2026-04-27');
INSERT INTO cargo_flights VALUES (217450, 32977, 314357, '2026-05-29', '2026-05-29');
INSERT INTO cargo_flights VALUES (944394, 67709, 147616, '2026-04-18', '2026-03-11');
INSERT INTO cargo_flights VALUES (140527, 15933, 237788, '2026-04-23', '2026-04-30');
INSERT INTO cargo_flights VALUES (927176, 23758, 182312, '2026-05-22', '2026-05-01');
INSERT INTO cargo_flights VALUES (355369, 67709, 405071, '2026-03-04', '2026-02-15');
INSERT INTO cargo_flights VALUES (708494, 79097, 419693, '2026-05-16', '2026-04-17');
INSERT INTO cargo_flights VALUES (742571, 90925, 121058, '2026-05-11', '2026-02-27');
INSERT INTO cargo_flights VALUES (336831, 51878, 441656, '2026-05-03', '2026-03-13');
INSERT INTO cargo_flights VALUES (726322, 43926, 405071, '2026-03-31', '2026-02-11');
INSERT INTO cargo_flights VALUES (758527, 89828, 100367, '2026-02-10', '2026-01-13');
INSERT INTO cargo_flights VALUES (877280, 61836, 269791, '2026-03-31', '2026-04-22');
INSERT INTO cargo_flights VALUES (503550, 90925, 100367, '2026-02-14', '2026-03-28');
INSERT INTO cargo_flights VALUES (686783, 23758, 434648, '2026-05-01', '2026-03-04');
INSERT INTO cargo_flights VALUES (944074, 16604, 125785, '2026-04-27', '2026-01-20');
INSERT INTO cargo_flights VALUES (654139, 89828, 487287, '2026-05-07', '2026-05-19');
INSERT INTO cargo_flights VALUES (341564, 45629, 338899, '2026-05-12', '2026-05-18');
INSERT INTO cargo_flights VALUES (955018, 89828, 164033, '2026-01-27', '2026-01-18');
INSERT INTO cargo_flights VALUES (422672, 87639, 429275, '2026-03-10', '2026-04-12');
INSERT INTO cargo_flights VALUES (417017, 74433, 141055, '2026-05-17', '2026-01-09');
INSERT INTO cargo_flights VALUES (417366, 80410, 429275, '2026-03-27', '2026-01-17');
INSERT INTO cargo_flights VALUES (246438, 67709, 255659, '2026-01-20', '2026-02-17');
INSERT INTO cargo_flights VALUES (813446, 90925, 270256, '2026-04-06', '2026-01-12');
INSERT INTO cargo_flights VALUES (136113, 15933, 237788, '2026-03-06', '2026-05-10');
INSERT INTO cargo_flights VALUES (521146, 77021, 309276, '2026-05-03', '2026-02-11');
INSERT INTO cargo_flights VALUES (105592, 87639, 314357, '2026-04-28', '2026-05-20');
INSERT INTO cargo_flights VALUES (876023, 51878, 255659, '2026-01-21', '2026-03-08');
INSERT INTO cargo_flights VALUES (260005, 94128, 419693, '2026-02-16', '2026-01-10');
INSERT INTO cargo_flights VALUES (598515, 84601, 100367, '2026-03-09', '2026-01-17');
INSERT INTO cargo_flights VALUES (722031, 94719, 410928, '2026-01-13', '2026-01-11');
INSERT INTO cargo_flights VALUES (844403, 84601, 302149, '2026-02-26', '2026-04-27');
INSERT INTO cargo_flights VALUES (334157, 84601, 374635, '2026-05-23', '2026-02-01');
INSERT INTO cargo_flights VALUES (477627, 51185, 236110, '2026-03-17', '2026-04-27');
INSERT INTO cargo_flights VALUES (585228, 87639, 269791, '2026-04-28', '2026-03-31');
INSERT INTO cargo_flights VALUES (289019, 32281, 434648, '2026-04-23', '2026-01-04');
INSERT INTO cargo_flights VALUES (656769, 32977, 159054, '2026-04-20', '2026-02-07');
INSERT INTO cargo_flights VALUES (702515, 43926, 484592, '2026-04-29', '2026-01-15');
INSERT INTO cargo_flights VALUES (500878, 70573, 419049, '2026-01-02', '2026-05-02');
INSERT INTO cargo_flights VALUES (123476, 79097, 419693, '2026-05-13', '2026-03-02');
INSERT INTO cargo_flights VALUES (104719, 18187, 255659, '2026-02-14', '2026-04-14');
INSERT INTO cargo_flights VALUES (945423, 15933, 385116, '2026-05-04', '2026-03-26');
INSERT INTO cargo_flights VALUES (495332, 94719, 125785, '2026-05-22', '2026-05-22');
INSERT INTO cargo_flights VALUES (287765, 87639, 464020, '2026-04-06', '2026-04-07');
INSERT INTO cargo_flights VALUES (101743, 77021, 164033, '2026-01-07', '2026-01-15');
INSERT INTO cargo_flights VALUES (109915, 89828, 497396, '2026-01-19', '2026-03-17');
INSERT INTO cargo_flights VALUES (498912, 80410, 268939, '2026-02-22', '2026-03-14');
INSERT INTO cargo_flights VALUES (983670, 70573, 434648, '2026-01-16', '2026-05-23');
INSERT INTO cargo_flights VALUES (627684, 51878, 370062, '2026-04-06', '2026-04-27');
INSERT INTO cargo_flights VALUES (468368, 32281, 270256, '2026-01-04', '2026-05-10');
INSERT INTO cargo_flights VALUES (480401, 67709, 100367, '2026-04-06', '2026-01-23');
INSERT INTO cargo_flights VALUES (903883, 15933, 153482, '2026-05-31', '2026-05-29');
INSERT INTO cargo_flights VALUES (125473, 43926, 257731, '2026-03-05', '2026-05-21');
INSERT INTO cargo_flights VALUES (744120, 71893, 487287, '2026-04-03', '2026-01-14');
INSERT INTO cargo_flights VALUES (452392, 51878, 454442, '2026-01-27', '2026-02-22');
INSERT INTO cargo_flights VALUES (703088, 32281, 153482, '2026-03-21', '2026-03-15');
INSERT INTO cargo_flights VALUES (434451, 71893, 255659, '2026-02-22', '2026-01-05');
INSERT INTO cargo_flights VALUES (769533, 51878, 309920, '2026-01-08', '2026-03-11');
INSERT INTO cargo_flights VALUES (176018, 18187, 358319, '2026-04-03', '2026-03-13');
INSERT INTO cargo_flights VALUES (990047, 76051, 141055, '2026-04-26', '2026-03-10');
INSERT INTO cargo_flights VALUES (990584, 80410, 269791, '2026-02-03', '2026-03-04');
INSERT INTO cargo_flights VALUES (833947, 80410, 309276, '2026-05-14', '2026-02-12');
INSERT INTO cargo_flights VALUES (764454, 32281, 430422, '2026-01-19', '2026-04-25');
INSERT INTO cargo_flights VALUES (801910, 61836, 429275, '2026-03-06', '2026-01-03');
INSERT INTO cargo_flights VALUES (964737, 23758, 237788, '2026-05-20', '2026-02-11');
INSERT INTO cargo_flights VALUES (595282, 77021, 188356, '2026-04-19', '2026-02-04');
INSERT INTO cargo_flights VALUES (668736, 43926, 294313, '2026-02-20', '2026-02-27');
INSERT INTO cargo_flights VALUES (139249, 79097, 338899, '2026-03-11', '2026-04-18');
INSERT INTO cargo_flights VALUES (896235, 67709, 294313, '2026-01-28', '2026-03-25');
INSERT INTO cargo_flights VALUES (734277, 74433, 454442, '2026-03-27', '2026-04-16');
INSERT INTO cargo_flights VALUES (229560, 61836, 338899, '2026-02-16', '2026-01-22');
INSERT INTO cargo_flights VALUES (820512, 94719, 460317, '2026-05-19', '2026-04-22');
INSERT INTO cargo_flights VALUES (767763, 32281, 257731, '2026-01-26', '2026-04-01');
INSERT INTO cargo_flights VALUES (778047, 21892, 147616, '2026-01-14', '2026-04-15');
INSERT INTO cargo_flights VALUES (747608, 94719, 358319, '2026-01-03', '2026-01-06');
INSERT INTO cargo_flights VALUES (241612, 89828, 153482, '2026-05-18', '2026-04-05');
INSERT INTO cargo_flights VALUES (622718, 43926, 497396, '2026-02-21', '2026-05-25');
INSERT INTO cargo_flights VALUES (332486, 70573, 470939, '2026-03-02', '2026-05-17');
INSERT INTO cargo_flights VALUES (794219, 94719, 411471, '2026-01-18', '2026-03-26');
INSERT INTO cargo_flights VALUES (694403, 23758, 430422, '2026-05-16', '2026-03-28');
INSERT INTO cargo_flights VALUES (933725, 80410, 454442, '2026-04-28', '2026-03-18');
INSERT INTO cargo_flights VALUES (839964, 84601, 434648, '2026-02-27', '2026-01-22');
INSERT INTO cargo_flights VALUES (139045, 90925, 239935, '2026-02-28', '2026-05-02');
INSERT INTO cargo_flights VALUES (495162, 71893, 410928, '2026-03-13', '2026-01-04');
INSERT INTO cargo_flights VALUES (385272, 74433, 314357, '2026-05-08', '2026-04-24');
INSERT INTO cargo_flights VALUES (671687, 39753, 497396, '2026-04-15', '2026-03-15');
INSERT INTO cargo_flights VALUES (193915, 87639, 370062, '2026-02-13', '2026-03-23');
INSERT INTO cargo_flights VALUES (651385, 16604, 358319, '2026-01-08', '2026-03-09');
INSERT INTO cargo_flights VALUES (671988, 89828, 374635, '2026-01-14', '2026-02-02');
INSERT INTO cargo_flights VALUES (655649, 16604, 385116, '2026-03-12', '2026-02-11');
INSERT INTO cargo_flights VALUES (994096, 80410, 169833, '2026-05-01', '2026-05-26');
INSERT INTO cargo_flights VALUES (578296, 70573, 141055, '2026-02-21', '2026-05-11');
INSERT INTO cargo_flights VALUES (782892, 76051, 257731, '2026-02-15', '2026-04-26');
INSERT INTO cargo_flights VALUES (464221, 61836, 125785, '2026-05-03', '2026-05-31');
INSERT INTO cargo_flights VALUES (431929, 51185, 432822, '2026-03-12', '2026-01-12');
INSERT INTO cargo_flights VALUES (838513, 39753, 255659, '2026-02-27', '2026-05-11');
INSERT INTO cargo_flights VALUES (610948, 21892, 121058, '2026-01-07', '2026-05-17');
INSERT INTO cargo_flights VALUES (582110, 67709, 182312, '2026-01-25', '2026-01-27');
INSERT INTO cargo_flights VALUES (154401, 21892, 182312, '2026-03-07', '2026-05-21');
INSERT INTO cargo_flights VALUES (571370, 16604, 153482, '2026-01-27', '2026-05-14');
INSERT INTO cargo_flights VALUES (581627, 79097, 419693, '2026-05-27', '2026-05-16');
INSERT INTO cargo_flights VALUES (246854, 51185, 309276, '2026-04-07', '2026-01-23');
INSERT INTO cargo_flights VALUES (910344, 74433, 358319, '2026-03-16', '2026-01-26');
INSERT INTO cargo_flights VALUES (319696, 80410, 314357, '2026-02-14', '2026-04-20');
INSERT INTO cargo_flights VALUES (745546, 71893, 188356, '2026-05-16', '2026-01-22');
INSERT INTO cargo_flights VALUES (255144, 39753, 268939, '2026-05-19', '2026-05-04');
INSERT INTO cargo_flights VALUES (583957, 94719, 338899, '2026-02-09', '2026-02-17');
INSERT INTO cargo_flights VALUES (196524, 89828, 141055, '2026-03-07', '2026-01-06');
INSERT INTO cargo_flights VALUES (944547, 23758, 164033, '2026-04-12', '2026-03-19');
INSERT INTO cargo_flights VALUES (414337, 71893, 419049, '2026-05-21', '2026-04-12');
INSERT INTO cargo_flights VALUES (283032, 45629, 237788, '2026-01-02', '2026-01-20');
INSERT INTO cargo_flights VALUES (797115, 51185, 298831, '2026-02-25', '2026-02-15');
INSERT INTO cargo_flights VALUES (207609, 16604, 188356, '2026-05-27', '2026-05-30');
INSERT INTO cargo_flights VALUES (174006, 21892, 237788, '2026-03-09', '2026-01-04');
INSERT INTO cargo_flights VALUES (264352, 94719, 126599, '2026-05-01', '2026-05-04');
INSERT INTO cargo_flights VALUES (551210, 51878, 430422, '2026-03-07', '2026-03-08');
INSERT INTO cargo_flights VALUES (615352, 74433, 257731, '2026-04-23', '2026-02-14');
INSERT INTO cargo_flights VALUES (143671, 77021, 255659, '2026-03-24', '2026-02-25');
INSERT INTO cargo_flights VALUES (587854, 90925, 460317, '2026-01-02', '2026-03-06');
INSERT INTO cargo_flights VALUES (402347, 23758, 232860, '2026-04-03', '2026-02-28');
INSERT INTO cargo_flights VALUES (103502, 15933, 206091, '2026-04-10', '2026-02-21');
INSERT INTO cargo_flights VALUES (925782, 79097, 254959, '2026-04-23', '2026-03-12');
INSERT INTO cargo_flights VALUES (388484, 39753, 270256, '2026-02-06', '2026-02-28');
INSERT INTO cargo_flights VALUES (793892, 89828, 126599, '2026-05-28', '2026-04-14');
INSERT INTO cargo_flights VALUES (145117, 67709, 206091, '2026-03-21', '2026-05-26');
INSERT INTO cargo_flights VALUES (230534, 13051, 374635, '2026-05-14', '2026-02-04');

-- =========== skymill.bookings (generic) ==========

DROP TABLE IF EXISTS bookings;

CREATE TABLE bookings (
  id INT NOT NULL,
  passenger_id INT NOT NULL,
  flight_instance_id INT NOT NULL,
  seat_number VARCHAR(255) NOT NULL
);

INSERT INTO bookings VALUES (8323487, 2013023, 9840773, 'DB8');
INSERT INTO bookings VALUES (4868478, 8395495, 4492728, 'AQ2');
INSERT INTO bookings VALUES (2340444, 3879592, 7648863, 'GK2');
INSERT INTO bookings VALUES (4218078, 4197805, 5682084, 'UP2');
INSERT INTO bookings VALUES (2417145, 8371206, 6540542, 'NQ5');
INSERT INTO bookings VALUES (9017983, 5683800, 3108257, 'NJ1');
INSERT INTO bookings VALUES (4836866, 3270346, 6787581, 'BJ0');
INSERT INTO bookings VALUES (8293529, 6419573, 6468600, 'CH1');
INSERT INTO bookings VALUES (1017036, 8155808, 9676209, 'SX2');
INSERT INTO bookings VALUES (8224560, 7447517, 7592431, 'JS1');
INSERT INTO bookings VALUES (8439361, 7447517, 3580596, 'SY6');
INSERT INTO bookings VALUES (7246117, 5497703, 1651685, 'IT2');
INSERT INTO bookings VALUES (9881340, 2975287, 5518639, 'GR8');
INSERT INTO bookings VALUES (2418632, 1185144, 2094934, 'MD2');
INSERT INTO bookings VALUES (7062408, 2541341, 8669614, 'DU3');
INSERT INTO bookings VALUES (3934917, 6728086, 6938430, 'QZ0');
INSERT INTO bookings VALUES (8888764, 2940416, 8023566, 'WD8');
INSERT INTO bookings VALUES (7617123, 8782077, 6707929, 'UM4');
INSERT INTO bookings VALUES (9014256, 7507143, 5888739, 'IR1');
INSERT INTO bookings VALUES (4906996, 8957252, 4388944, 'IE1');
INSERT INTO bookings VALUES (5808726, 8770415, 7542327, 'JV9');
INSERT INTO bookings VALUES (5125352, 2654571, 8360579, 'HI9');
INSERT INTO bookings VALUES (6427746, 2541341, 8047477, 'ND1');
INSERT INTO bookings VALUES (4522456, 5798843, 7821647, 'JZ0');
INSERT INTO bookings VALUES (9486875, 9765785, 8425634, 'BE3');
INSERT INTO bookings VALUES (6350447, 3816929, 4497548, 'LU3');
INSERT INTO bookings VALUES (2507915, 7412516, 5843018, 'KG1');
INSERT INTO bookings VALUES (9081870, 2905071, 5017380, 'IY9');
INSERT INTO bookings VALUES (6065249, 6762919, 1359170, 'DX9');
INSERT INTO bookings VALUES (8456555, 3504500, 9235735, 'NA9');
INSERT INTO bookings VALUES (1834015, 9166027, 4055794, 'ZD6');
INSERT INTO bookings VALUES (9738543, 1185144, 8948764, 'TG8');
INSERT INTO bookings VALUES (4070724, 4678821, 8539128, 'LH6');
INSERT INTO bookings VALUES (1310627, 8896880, 3232025, 'UP6');
INSERT INTO bookings VALUES (5288135, 7660641, 3607379, 'OG3');
INSERT INTO bookings VALUES (2799947, 2773723, 1220797, 'CQ4');
INSERT INTO bookings VALUES (6518575, 3437925, 3920680, 'NA5');
INSERT INTO bookings VALUES (6217534, 3655021, 4580667, 'CB8');
INSERT INTO bookings VALUES (2449163, 7180424, 4509881, 'KX9');
INSERT INTO bookings VALUES (5564914, 7371009, 8820639, 'ZK1');
INSERT INTO bookings VALUES (2397191, 8622373, 8047477, 'RL5');
INSERT INTO bookings VALUES (8682109, 3738778, 4035695, 'XG6');
INSERT INTO bookings VALUES (8737883, 2680208, 6839691, 'RT7');
INSERT INTO bookings VALUES (5165799, 4668405, 1003920, 'UE4');
INSERT INTO bookings VALUES (3079610, 8517884, 7405213, 'NS2');
INSERT INTO bookings VALUES (7228070, 8805416, 4373499, 'NU5');
INSERT INTO bookings VALUES (6963569, 8395495, 1594856, 'WT0');
INSERT INTO bookings VALUES (6619749, 9342246, 3868324, 'KM9');
INSERT INTO bookings VALUES (7976370, 3211200, 5855521, 'AK4');
INSERT INTO bookings VALUES (6017976, 7865203, 7210852, 'LH0');
INSERT INTO bookings VALUES (2722191, 7447517, 2781589, 'XV5');
INSERT INTO bookings VALUES (2619705, 5720403, 4356416, 'KP7');
INSERT INTO bookings VALUES (3170049, 4821607, 3811037, 'SK9');
INSERT INTO bookings VALUES (9205898, 8446918, 4108742, 'CL0');
INSERT INTO bookings VALUES (9146355, 5332651, 9582378, 'CG6');
INSERT INTO bookings VALUES (4993121, 4439160, 8627402, 'XJ9');
INSERT INTO bookings VALUES (2822242, 7011025, 4501881, 'GV3');
INSERT INTO bookings VALUES (7254944, 3437925, 7949387, 'PO0');
INSERT INTO bookings VALUES (6343484, 7515878, 3684041, 'QM8');
INSERT INTO bookings VALUES (8928549, 5088004, 7444700, 'JQ7');
INSERT INTO bookings VALUES (4541509, 5781985, 1190190, 'LI5');
INSERT INTO bookings VALUES (2290522, 4193399, 1939454, 'FV0');
INSERT INTO bookings VALUES (3999987, 1784320, 1554195, 'IS4');
INSERT INTO bookings VALUES (4192915, 4149608, 8641311, 'ER2');
INSERT INTO bookings VALUES (5552236, 4865004, 4986608, 'QY6');
INSERT INTO bookings VALUES (9768748, 5185820, 6476144, 'RT9');
INSERT INTO bookings VALUES (2950753, 1083307, 6343581, 'AJ3');
INSERT INTO bookings VALUES (5783480, 5497703, 4534704, 'XY2');
INSERT INTO bookings VALUES (5542530, 3738778, 6433324, 'CI7');
INSERT INTO bookings VALUES (4929294, 7456753, 4077580, 'RS3');
INSERT INTO bookings VALUES (7934364, 8173474, 5029747, 'EB8');
INSERT INTO bookings VALUES (8922142, 4821607, 4868063, 'ZF3');
INSERT INTO bookings VALUES (8870298, 7092950, 3337487, 'LR0');
INSERT INTO bookings VALUES (3951200, 3738778, 8522282, 'DT0');
INSERT INTO bookings VALUES (3495958, 3270346, 7592431, 'BE6');
INSERT INTO bookings VALUES (3075323, 8809406, 6655240, 'IN9');
INSERT INTO bookings VALUES (1795486, 1128509, 4388944, 'LC7');
INSERT INTO bookings VALUES (3889730, 3106291, 8641311, 'KD8');
INSERT INTO bookings VALUES (9249043, 1831583, 7444700, 'HJ1');
INSERT INTO bookings VALUES (7463436, 8845953, 5146931, 'PF3');
INSERT INTO bookings VALUES (4853826, 3504500, 9222958, 'OY2');
INSERT INTO bookings VALUES (9250912, 9488847, 8168832, 'WA4');
INSERT INTO bookings VALUES (4904785, 6348851, 8311918, 'YH2');
INSERT INTO bookings VALUES (1128269, 2461695, 7640982, 'KZ5');
INSERT INTO bookings VALUES (3896208, 4668405, 8667285, 'ZB2');
INSERT INTO bookings VALUES (9231572, 2992723, 1595358, 'IM5');
INSERT INTO bookings VALUES (3059306, 2483269, 2880750, 'PD4');
INSERT INTO bookings VALUES (8260380, 4507387, 6678279, 'GC3');
INSERT INTO bookings VALUES (1776622, 4220395, 1190757, 'DD8');
INSERT INTO bookings VALUES (1487526, 7507143, 4885859, 'EF9');
INSERT INTO bookings VALUES (6341245, 6564332, 4986608, 'WK9');
INSERT INTO bookings VALUES (9912794, 4220395, 7207091, 'DM9');
INSERT INTO bookings VALUES (8181403, 5332651, 7569872, 'VQ4');
INSERT INTO bookings VALUES (9745289, 4821607, 8767041, 'QP9');
INSERT INTO bookings VALUES (7296906, 6419573, 1952545, 'PF3');
INSERT INTO bookings VALUES (9603857, 7503774, 3577336, 'SR6');
INSERT INTO bookings VALUES (4248391, 3352316, 2013782, 'SI2');
INSERT INTO bookings VALUES (4109407, 8173474, 3668668, 'BP6');
INSERT INTO bookings VALUES (7858542, 7507143, 7944595, 'LG2');
INSERT INTO bookings VALUES (2995950, 6419573, 6982308, 'CX2');
INSERT INTO bookings VALUES (3676834, 1128509, 8201958, 'BE9');
INSERT INTO bookings VALUES (1858500, 1102212, 1832564, 'MG8');
INSERT INTO bookings VALUES (1395806, 5563188, 7381080, 'GC1');
INSERT INTO bookings VALUES (6254660, 8625276, 3106369, 'GA1');
INSERT INTO bookings VALUES (2472191, 8502145, 5537933, 'DP7');
INSERT INTO bookings VALUES (7223505, 1128509, 4868063, 'JC4');
INSERT INTO bookings VALUES (2825950, 2785261, 1460374, 'DM6');
INSERT INTO bookings VALUES (5733796, 7660641, 2080096, 'JC7');
INSERT INTO bookings VALUES (7211041, 4141937, 4911547, 'WC2');
INSERT INTO bookings VALUES (1099386, 8594972, 7938221, 'QJ9');
INSERT INTO bookings VALUES (3644639, 6348851, 2651268, 'YY4');
INSERT INTO bookings VALUES (9152711, 8707528, 3229060, 'TV2');
INSERT INTO bookings VALUES (3091758, 5286908, 9112856, 'ZF6');
INSERT INTO bookings VALUES (5879440, 9258677, 3564294, 'WR0');
INSERT INTO bookings VALUES (1788570, 3135062, 1554195, 'XF1');
INSERT INTO bookings VALUES (8365147, 6526839, 1850920, 'WP2');
INSERT INTO bookings VALUES (4052570, 3352316, 4050052, 'KB1');
INSERT INTO bookings VALUES (3634055, 7456753, 4407456, 'RL8');
INSERT INTO bookings VALUES (5215497, 7242085, 8201958, 'VA0');
INSERT INTO bookings VALUES (7789828, 1169425, 1609427, 'XZ5');
INSERT INTO bookings VALUES (8837096, 8216199, 7094975, 'TZ7');
INSERT INTO bookings VALUES (2108500, 4118224, 5032808, 'XV8');
INSERT INTO bookings VALUES (7566118, 2974225, 3443010, 'PW3');
INSERT INTO bookings VALUES (7730782, 9169482, 2117824, 'ZO2');
INSERT INTO bookings VALUES (2877497, 7885995, 8795653, 'LS6');
INSERT INTO bookings VALUES (6601046, 4086038, 9250393, 'WM7');
INSERT INTO bookings VALUES (3658943, 3437925, 4058181, 'WI8');
INSERT INTO bookings VALUES (6101580, 7507143, 3533222, 'QW0');
INSERT INTO bookings VALUES (4723477, 7011025, 7409070, 'NR4');
INSERT INTO bookings VALUES (3283227, 6091790, 9016024, 'KJ0');
INSERT INTO bookings VALUES (3812530, 7507143, 7514371, 'PW4');
INSERT INTO bookings VALUES (8819230, 8951574, 9171399, 'DP8');
INSERT INTO bookings VALUES (2221344, 1083307, 4055794, 'DG6');
INSERT INTO bookings VALUES (6845883, 2975287, 5888739, 'GK0');
INSERT INTO bookings VALUES (5649162, 5030264, 2297664, 'PG7');
INSERT INTO bookings VALUES (9687115, 9454921, 4640846, 'TQ4');
INSERT INTO bookings VALUES (2303807, 5761385, 1551780, 'PU2');
INSERT INTO bookings VALUES (1090155, 9883532, 3061980, 'DO3');
INSERT INTO bookings VALUES (1160038, 5030264, 5337919, 'GP5');
INSERT INTO bookings VALUES (9413956, 5720403, 5682084, 'ZF4');
INSERT INTO bookings VALUES (1171124, 9622165, 2566204, 'AO5');
INSERT INTO bookings VALUES (8019596, 8957252, 1118405, 'MR5');
INSERT INTO bookings VALUES (5020816, 2013023, 1084740, 'AJ6');
INSERT INTO bookings VALUES (3865500, 2940416, 4911547, 'GF0');
INSERT INTO bookings VALUES (5445304, 1308410, 8509070, 'CP2');
INSERT INTO bookings VALUES (1423600, 3577168, 1220797, 'UQ1');
INSERT INTO bookings VALUES (6755824, 5761385, 3077667, 'BY8');
INSERT INTO bookings VALUES (7766085, 7083414, 2297664, 'KD1');
INSERT INTO bookings VALUES (4589069, 7092950, 9710039, 'LA8');
INSERT INTO bookings VALUES (3288648, 5761385, 5460257, 'OT7');
INSERT INTO bookings VALUES (3899871, 9765785, 9649921, 'JO0');
INSERT INTO bookings VALUES (3796609, 3198155, 7277897, 'TC5');
INSERT INTO bookings VALUES (3316376, 3074155, 1609427, 'QQ1');
INSERT INTO bookings VALUES (8255873, 2729297, 4497548, 'HI7');
INSERT INTO bookings VALUES (1266417, 7556383, 4205310, 'AX0');
INSERT INTO bookings VALUES (2152309, 8453354, 6794537, 'LT4');
INSERT INTO bookings VALUES (8078700, 4865004, 7039442, 'AT4');
INSERT INTO bookings VALUES (5165210, 3343612, 9376507, 'OR6');
INSERT INTO bookings VALUES (8738441, 5030264, 2502371, 'SU8');
INSERT INTO bookings VALUES (2820535, 4953636, 6260587, 'XV9');
INSERT INTO bookings VALUES (6971793, 3977299, 1768067, 'CU4');
INSERT INTO bookings VALUES (3314952, 7660641, 4186470, 'UQ5');
INSERT INTO bookings VALUES (9021613, 7083414, 8600080, 'SZ3');
INSERT INTO bookings VALUES (5751523, 9454921, 3077667, 'HZ0');
INSERT INTO bookings VALUES (3472111, 3944798, 4187430, 'QN8');
INSERT INTO bookings VALUES (9111289, 9218661, 3735790, 'WE7');
INSERT INTO bookings VALUES (1497075, 5105774, 4492728, 'NJ1');
INSERT INTO bookings VALUES (9217906, 4017366, 9171399, 'CL0');
INSERT INTO bookings VALUES (4574666, 2079829, 8667285, 'NF0');
INSERT INTO bookings VALUES (1474212, 3270346, 6707929, 'AA5');
INSERT INTO bookings VALUES (8573149, 9908742, 9582378, 'OF4');
INSERT INTO bookings VALUES (9205451, 6762919, 7004288, 'TJ7');
INSERT INTO bookings VALUES (9378525, 1128509, 1850920, 'EE1');
INSERT INTO bookings VALUES (7713895, 4339947, 7770284, 'AR8');
INSERT INTO bookings VALUES (9918406, 4078630, 3191211, 'NL3');
INSERT INTO bookings VALUES (2955809, 9288911, 8827461, 'GR4');
INSERT INTO bookings VALUES (9041274, 8502145, 1453128, 'QT1');
INSERT INTO bookings VALUES (1361627, 4821607, 3788765, 'SR4');
INSERT INTO bookings VALUES (3592490, 8622373, 4860180, 'ZC7');
INSERT INTO bookings VALUES (8023418, 2347757, 7770284, 'WD2');
INSERT INTO bookings VALUES (3417165, 4308132, 6273381, 'CI7');
INSERT INTO bookings VALUES (1997341, 6976359, 8201958, 'OY1');
INSERT INTO bookings VALUES (6784869, 2483269, 3735790, 'TI5');
INSERT INTO bookings VALUES (2518875, 8517884, 2934973, 'EO1');
INSERT INTO bookings VALUES (2439246, 6091790, 1472543, 'AY0');
INSERT INTO bookings VALUES (6625575, 6762919, 2117824, 'SK1');
INSERT INTO bookings VALUES (3664248, 4678821, 2080096, 'MH3');
INSERT INTO bookings VALUES (8018635, 5408609, 5734273, 'BN3');
INSERT INTO bookings VALUES (1987473, 8517884, 6655240, 'JR4');
INSERT INTO bookings VALUES (2346918, 8755012, 1574416, 'LY9');
INSERT INTO bookings VALUES (3096061, 3352316, 1025551, 'YJ0');
INSERT INTO bookings VALUES (8965286, 7143247, 4035695, 'CN0');
INSERT INTO bookings VALUES (4136571, 7865203, 4162870, 'AH2');
INSERT INTO bookings VALUES (7553272, 9488847, 5855521, 'AZ9');
INSERT INTO bookings VALUES (3000047, 7422573, 8832974, 'MI6');
INSERT INTO bookings VALUES (7522690, 9270808, 9269310, 'MF3');
INSERT INTO bookings VALUES (8366014, 2905071, 6288275, 'VJ7');
INSERT INTO bookings VALUES (9997507, 5820495, 9171399, 'AI5');
INSERT INTO bookings VALUES (2760891, 1365545, 7172867, 'MK9');
INSERT INTO bookings VALUES (6327798, 2975287, 3920680, 'AS7');
INSERT INTO bookings VALUES (2727560, 6527815, 2111920, 'CM2');
INSERT INTO bookings VALUES (8420487, 6187608, 7444700, 'NI0');
INSERT INTO bookings VALUES (7437946, 5683800, 1651685, 'VC9');
INSERT INTO bookings VALUES (1690885, 3135062, 9190463, 'JH3');
INSERT INTO bookings VALUES (4011812, 6904458, 2297664, 'CG8');
INSERT INTO bookings VALUES (3957103, 9454921, 6058824, 'WT8');
INSERT INTO bookings VALUES (3356498, 5277747, 5420546, 'TN3');
INSERT INTO bookings VALUES (1761602, 5030264, 3606936, 'LH2');
INSERT INTO bookings VALUES (9957215, 5332651, 2979638, 'GL1');
INSERT INTO bookings VALUES (6794875, 3270346, 4075438, 'UN8');
INSERT INTO bookings VALUES (4366043, 4507387, 4885694, 'GT6');
INSERT INTO bookings VALUES (3336656, 9488847, 7300889, 'PR2');
INSERT INTO bookings VALUES (3428322, 5720403, 1617454, 'BE1');
INSERT INTO bookings VALUES (8090201, 6039236, 5420546, 'CI6');
INSERT INTO bookings VALUES (5682233, 7083414, 2828272, 'LS4');
INSERT INTO bookings VALUES (2100070, 2461695, 8795653, 'ED6');
INSERT INTO bookings VALUES (2910376, 3440276, 7405213, 'VI4');
INSERT INTO bookings VALUES (9876218, 7092950, 8457243, 'SB1');
INSERT INTO bookings VALUES (8939477, 8962583, 1658973, 'XM8');
INSERT INTO bookings VALUES (6013426, 8951574, 5734273, 'ER1');
INSERT INTO bookings VALUES (5897863, 1831583, 7938221, 'FH8');
INSERT INTO bookings VALUES (2899041, 8809406, 5074305, 'JI2');
INSERT INTO bookings VALUES (3193208, 5408609, 1832564, 'IL1');
INSERT INTO bookings VALUES (3795295, 5332651, 1148814, 'LF0');
INSERT INTO bookings VALUES (5715678, 5009563, 1135543, 'CW0');
INSERT INTO bookings VALUES (6583581, 9258677, 4942822, 'XG5');
INSERT INTO bookings VALUES (3244836, 5332651, 4860180, 'FU2');
INSERT INTO bookings VALUES (9435683, 1831583, 9538900, 'BQ1');
INSERT INTO bookings VALUES (5264010, 9478711, 4172964, 'DH0');
INSERT INTO bookings VALUES (8135561, 4197805, 9676209, 'YQ5');
INSERT INTO bookings VALUES (1351333, 8809406, 7282020, 'XC6');
INSERT INTO bookings VALUES (9577867, 2881744, 5160345, 'HK1');
INSERT INTO bookings VALUES (7794534, 2566046, 2428956, 'RV8');
INSERT INTO bookings VALUES (2885884, 5720403, 7663248, 'BA3');
INSERT INTO bookings VALUES (9740606, 5286908, 6839691, 'PB2');
INSERT INTO bookings VALUES (6056507, 4118224, 2794631, 'DC8');
INSERT INTO bookings VALUES (3481571, 4017366, 5734273, 'AW5');
INSERT INTO bookings VALUES (6560390, 2422409, 9148557, 'JY4');
INSERT INTO bookings VALUES (4460683, 2729297, 2718238, 'TK2');
INSERT INTO bookings VALUES (4758363, 2729297, 2939707, 'ZH6');
INSERT INTO bookings VALUES (3549059, 9999465, 3607379, 'ZF3');
INSERT INTO bookings VALUES (2352295, 4789986, 9235735, 'GY3');
INSERT INTO bookings VALUES (3580908, 8446918, 1556467, 'MS8');
INSERT INTO bookings VALUES (1247527, 5088004, 9837984, 'ED6');
INSERT INTO bookings VALUES (1598131, 9259092, 7705816, 'YM3');
INSERT INTO bookings VALUES (6776605, 6564332, 6707929, 'AK4');
INSERT INTO bookings VALUES (1597339, 3738778, 1118405, 'EL5');
INSERT INTO bookings VALUES (1639775, 7797269, 5032808, 'VW7');
INSERT INTO bookings VALUES (5040329, 8957252, 1128351, 'FH2');
INSERT INTO bookings VALUES (9807578, 4668405, 6468600, 'UX5');
INSERT INTO bookings VALUES (4453235, 1253566, 4502860, 'FV3');
INSERT INTO bookings VALUES (7418656, 9765785, 3261263, 'ER3');
INSERT INTO bookings VALUES (1375594, 4086038, 8217563, 'EW5');
INSERT INTO bookings VALUES (9290818, 6348851, 9608933, 'BE6');
INSERT INTO bookings VALUES (7165585, 1083307, 9128197, 'TX8');
INSERT INTO bookings VALUES (9136604, 7660641, 4163710, 'EV9');
INSERT INTO bookings VALUES (2660983, 3198155, 3162135, 'FP7');
INSERT INTO bookings VALUES (7905117, 3738778, 2443174, 'WN5');
INSERT INTO bookings VALUES (4261383, 7515878, 6384465, 'AM2');
INSERT INTO bookings VALUES (3495662, 1169425, 3802445, 'II8');
INSERT INTO bookings VALUES (4583950, 8896880, 8539128, 'FG3');
INSERT INTO bookings VALUES (8838174, 6187608, 9469884, 'DN5');
INSERT INTO bookings VALUES (3262717, 4507387, 6982308, 'UJ5');
INSERT INTO bookings VALUES (8267966, 4220395, 9171738, 'TA0');
INSERT INTO bookings VALUES (5712016, 4865004, 8566074, 'SH1');
INSERT INTO bookings VALUES (6686020, 6589761, 3294929, 'YI8');
INSERT INTO bookings VALUES (5126039, 7515878, 4787447, 'DF7');
INSERT INTO bookings VALUES (1942223, 2461695, 4229693, 'GX0');
INSERT INTO bookings VALUES (1385729, 3784950, 2794631, 'JZ8');
INSERT INTO bookings VALUES (1038659, 2079829, 9555093, 'CR8');
INSERT INTO bookings VALUES (5247321, 3423018, 8669614, 'SS6');
INSERT INTO bookings VALUES (3328093, 8238843, 9128197, 'UQ8');
INSERT INTO bookings VALUES (7112140, 9017430, 5460257, 'HR2');
INSERT INTO bookings VALUES (1741933, 1365545, 3788765, 'YZ6');
INSERT INTO bookings VALUES (6217687, 3440276, 3337487, 'RG8');
INSERT INTO bookings VALUES (6208447, 1128509, 7542327, 'SV0');
INSERT INTO bookings VALUES (8029988, 4118224, 3380796, 'MR0');
INSERT INTO bookings VALUES (4320050, 9166027, 9279751, 'EJ6');
INSERT INTO bookings VALUES (7939319, 5009563, 5041690, 'FQ3');
INSERT INTO bookings VALUES (5324424, 3963722, 7770284, 'PK2');
INSERT INTO bookings VALUES (3286037, 5105774, 9171738, 'FB9');
INSERT INTO bookings VALUES (7407177, 7092950, 1651685, 'TR3');
INSERT INTO bookings VALUES (2900027, 1365545, 2794631, 'DE4');
INSERT INTO bookings VALUES (3636943, 6564332, 3107948, 'DU9');
INSERT INTO bookings VALUES (7089480, 7595919, 3380796, 'PZ4');
INSERT INTO bookings VALUES (2259221, 6526839, 4497853, 'RU9');
INSERT INTO bookings VALUES (7030622, 2654571, 6468600, 'ZU1');
INSERT INTO bookings VALUES (2242802, 7462556, 4225434, 'EF8');
INSERT INTO bookings VALUES (9084029, 4017366, 2519795, 'PR5');
INSERT INTO bookings VALUES (8587892, 9729552, 1551780, 'IJ2');
INSERT INTO bookings VALUES (2345750, 8216199, 6655240, 'ZG2');
INSERT INTO bookings VALUES (6368129, 2422409, 7094975, 'AL4');
INSERT INTO bookings VALUES (8498641, 4430924, 9837984, 'XJ8');
INSERT INTO bookings VALUES (6471311, 7371009, 8928018, 'XA8');
INSERT INTO bookings VALUES (3575513, 4859106, 7770284, 'YM5');
INSERT INTO bookings VALUES (1316767, 6856915, 6411045, 'YM1');
INSERT INTO bookings VALUES (3231043, 4747967, 4502860, 'HK2');
INSERT INTO bookings VALUES (4563507, 7660641, 2443174, 'YL3');
INSERT INTO bookings VALUES (4845098, 4149608, 7542327, 'UJ4');
INSERT INTO bookings VALUES (1174090, 3002493, 9608933, 'HP0');
INSERT INTO bookings VALUES (2212754, 4197805, 6683219, 'YS0');
INSERT INTO bookings VALUES (3935854, 8155808, 4768588, 'CW9');
INSERT INTO bookings VALUES (4070427, 1365545, 2979638, 'AN3');
INSERT INTO bookings VALUES (4125937, 6066735, 9269310, 'FF2');
INSERT INTO bookings VALUES (7717586, 3211200, 1658973, 'WF3');
INSERT INTO bookings VALUES (6810691, 3002493, 9171399, 'TB1');
INSERT INTO bookings VALUES (3924841, 8391185, 8694911, 'XB0');
INSERT INTO bookings VALUES (5274467, 2773723, 4163710, 'CD4');
INSERT INTO bookings VALUES (9362354, 3179127, 3061980, 'MZ5');
INSERT INTO bookings VALUES (1393314, 4668405, 8641311, 'DG6');
INSERT INTO bookings VALUES (1665327, 8517884, 6273381, 'UD6');
INSERT INTO bookings VALUES (2096404, 8525725, 9859691, 'SN0');
INSERT INTO bookings VALUES (1408218, 9478711, 8767041, 'TA6');
INSERT INTO bookings VALUES (5919854, 8502145, 8436230, 'EJ1');
INSERT INTO bookings VALUES (5350539, 3577168, 1742490, 'NL7');
INSERT INTO bookings VALUES (3736596, 3166127, 4058181, 'RK7');
INSERT INTO bookings VALUES (6619339, 9607002, 4138126, 'EC7');
INSERT INTO bookings VALUES (2429247, 5030264, 2502371, 'JG1');
INSERT INTO bookings VALUES (4338621, 6597799, 3021844, 'HG4');
INSERT INTO bookings VALUES (5974936, 1784320, 8600080, 'HB6');
INSERT INTO bookings VALUES (3614410, 2265297, 1003920, 'NQ0');
INSERT INTO bookings VALUES (6760523, 8782077, 7618732, 'GE5');
INSERT INTO bookings VALUES (2186206, 8845953, 3735507, 'RQ4');
INSERT INTO bookings VALUES (6188232, 2974225, 5843018, 'ZC7');
INSERT INTO bookings VALUES (6822374, 7859194, 3107948, 'GF5');
INSERT INTO bookings VALUES (9954294, 2461695, 2880750, 'CR1');
INSERT INTO bookings VALUES (1097228, 3713051, 1359170, 'NX1');
INSERT INTO bookings VALUES (5558447, 2940416, 3548200, 'AX3');
INSERT INTO bookings VALUES (3224879, 8957252, 3162135, 'MX1');
INSERT INTO bookings VALUES (2383563, 4953636, 8047477, 'JM8');
INSERT INTO bookings VALUES (6789367, 4953636, 6411045, 'TN9');
INSERT INTO bookings VALUES (5782770, 8707528, 1082936, 'YM5');
INSERT INTO bookings VALUES (2942112, 1169425, 5410579, 'KH7');
INSERT INTO bookings VALUES (3518722, 6955370, 2519795, 'ZQ7');
INSERT INTO bookings VALUES (5592330, 6762919, 2519795, 'BC7');
INSERT INTO bookings VALUES (6003650, 2013023, 1112102, 'WV6');
INSERT INTO bookings VALUES (9561074, 9454921, 5888739, 'GW0');
INSERT INTO bookings VALUES (6468505, 9166027, 7405213, 'IJ5');
INSERT INTO bookings VALUES (1060597, 8805416, 2472315, 'MD1');
INSERT INTO bookings VALUES (9669297, 4278613, 8399090, 'TX8');
INSERT INTO bookings VALUES (7634150, 4507387, 1574416, 'HH4');
INSERT INTO bookings VALUES (9705715, 9263093, 3516493, 'NZ1');
INSERT INTO bookings VALUES (9851914, 4789986, 9190463, 'ME6');
INSERT INTO bookings VALUES (8575428, 6527815, 1609427, 'AR8');
INSERT INTO bookings VALUES (9177782, 3963722, 9678194, 'TP7');
INSERT INTO bookings VALUES (9237278, 4789986, 9710039, 'EK8');
INSERT INTO bookings VALUES (6835303, 9488847, 5707208, 'DO0');
INSERT INTO bookings VALUES (6938054, 9263093, 3107948, 'KD5');
INSERT INTO bookings VALUES (1018412, 2975287, 3382876, 'NW5');
INSERT INTO bookings VALUES (3573746, 9908742, 1148814, 'UH8');
INSERT INTO bookings VALUES (6303673, 7595919, 7559111, 'ZN7');
INSERT INTO bookings VALUES (4108983, 3198155, 8356612, 'ZV4');
INSERT INTO bookings VALUES (4429427, 9999465, 3162135, 'OY4');
INSERT INTO bookings VALUES (8582926, 4141937, 2080096, 'UD7');
INSERT INTO bookings VALUES (5169004, 7412516, 2827252, 'YB8');
INSERT INTO bookings VALUES (3582089, 9999465, 1190757, 'OL5');
INSERT INTO bookings VALUES (3954893, 1102212, 3443010, 'IV9');
INSERT INTO bookings VALUES (9955726, 3816929, 1658973, 'WD0');
INSERT INTO bookings VALUES (1415598, 1831583, 7388768, 'AB6');
INSERT INTO bookings VALUES (8366102, 5053396, 7260938, 'LC6');
INSERT INTO bookings VALUES (6131014, 2654571, 3984898, 'NJ9');
INSERT INTO bookings VALUES (7909331, 3257367, 1034182, 'JE0');
INSERT INTO bookings VALUES (1256000, 3944798, 9222958, 'AI4');
INSERT INTO bookings VALUES (8741395, 8951574, 9608933, 'GH2');
INSERT INTO bookings VALUES (7875307, 4017366, 2297664, 'AJ9');
INSERT INTO bookings VALUES (8572860, 7973380, 6678279, 'HC6');
INSERT INTO bookings VALUES (1641826, 1185144, 2915408, 'RY4');
INSERT INTO bookings VALUES (7823393, 4507387, 6678279, 'NX9');
INSERT INTO bookings VALUES (9977731, 4789986, 5682084, 'JA2');
INSERT INTO bookings VALUES (6375811, 1128509, 8499966, 'VA1');
INSERT INTO bookings VALUES (8664096, 1594316, 9269310, 'DI8');
INSERT INTO bookings VALUES (8294151, 1128509, 6411045, 'UB4');
INSERT INTO bookings VALUES (1247895, 9259092, 2939707, 'BA6');
INSERT INTO bookings VALUES (8778875, 4430924, 4390317, 'HC7');
INSERT INTO bookings VALUES (3161605, 7470714, 1850920, 'MF8');
INSERT INTO bookings VALUES (6746619, 1185144, 5410579, 'HX3');
INSERT INTO bookings VALUES (3914841, 9488847, 7893758, 'UA4');
INSERT INTO bookings VALUES (2090310, 5185820, 5337919, 'IQ1');
INSERT INTO bookings VALUES (2208818, 5277747, 3229060, 'IL4');
INSERT INTO bookings VALUES (8751110, 9883532, 9969805, 'PP9');
INSERT INTO bookings VALUES (7298318, 3816929, 4224585, 'TW4');
INSERT INTO bookings VALUES (3216464, 4220395, 8667285, 'BN8');
INSERT INTO bookings VALUES (5601574, 4278613, 2979638, 'AD2');
INSERT INTO bookings VALUES (3048419, 6039236, 9859691, 'FW4');
INSERT INTO bookings VALUES (8526427, 3655021, 4640846, 'HT3');
INSERT INTO bookings VALUES (8910115, 1102212, 4571129, 'XB7');
INSERT INTO bookings VALUES (3226703, 5497703, 9837984, 'CL3');
INSERT INTO bookings VALUES (3944942, 9356593, 7122531, 'BG8');
INSERT INTO bookings VALUES (2490812, 9607002, 7094923, 'GM9');
INSERT INTO bookings VALUES (8943946, 7797269, 3077667, 'FU4');
INSERT INTO bookings VALUES (4022346, 4118224, 1554195, 'QY7');
INSERT INTO bookings VALUES (7152578, 8896880, 2472315, 'NN9');
INSERT INTO bookings VALUES (3525105, 3423018, 2443174, 'UT5');
INSERT INTO bookings VALUES (6889635, 1128509, 4356416, 'TC5');
INSERT INTO bookings VALUES (9955973, 7422573, 5029747, 'WO6');
INSERT INTO bookings VALUES (3135141, 3504500, 8168832, 'UD1');
INSERT INTO bookings VALUES (6479012, 9258677, 2134249, 'RX9');
INSERT INTO bookings VALUES (7687110, 5053396, 6920189, 'DE2');
INSERT INTO bookings VALUES (6312080, 2193325, 7282020, 'NE1');
INSERT INTO bookings VALUES (6325239, 2940416, 3577336, 'DA0');
INSERT INTO bookings VALUES (2227821, 8957252, 4737821, 'EI5');
INSERT INTO bookings VALUES (8078461, 1185144, 2998984, 'AI1');
INSERT INTO bookings VALUES (7719363, 7011025, 7165388, 'SW6');
INSERT INTO bookings VALUES (5801072, 3074155, 3788765, 'YR9');
INSERT INTO bookings VALUES (6709604, 8371206, 5631640, 'VA6');
INSERT INTO bookings VALUES (7435895, 3577168, 5888739, 'CI5');
INSERT INTO bookings VALUES (2572538, 2216117, 3351825, 'GK9');
INSERT INTO bookings VALUES (4478776, 8622373, 2781589, 'RI8');
INSERT INTO bookings VALUES (4671508, 2680208, 8566074, 'XF8');
INSERT INTO bookings VALUES (5317026, 5009563, 3788765, 'XI2');
INSERT INTO bookings VALUES (3427568, 3343612, 7496589, 'DZ0');
INSERT INTO bookings VALUES (3004822, 8809406, 6920189, 'AL5');
INSERT INTO bookings VALUES (3988435, 4193399, 7848741, 'UT0');
INSERT INTO bookings VALUES (7486164, 9908742, 6296538, 'YX0');
INSERT INTO bookings VALUES (9097555, 6904458, 8251078, 'FO6');
INSERT INTO bookings VALUES (5672855, 4678821, 5253868, 'RK4');
INSERT INTO bookings VALUES (8156954, 3977299, 4501881, 'QG3');
INSERT INTO bookings VALUES (1370034, 1365545, 7522865, 'NO7');
INSERT INTO bookings VALUES (3153792, 3352316, 4108742, 'KF8');
INSERT INTO bookings VALUES (6208589, 2483269, 8551136, 'BH1');
INSERT INTO bookings VALUES (7953798, 9356593, 5827465, 'VW9');
INSERT INTO bookings VALUES (7331863, 1102212, 2094934, 'SX5');
INSERT INTO bookings VALUES (5292388, 3963722, 9417673, 'CB9');
INSERT INTO bookings VALUES (5544411, 9622165, 9468998, 'XG4');
INSERT INTO bookings VALUES (6662249, 9342246, 8399090, 'LP2');
INSERT INTO bookings VALUES (1805837, 3963722, 9805585, 'QJ7');
INSERT INTO bookings VALUES (6191290, 3423018, 9647986, 'OB9');
INSERT INTO bookings VALUES (5230427, 9356593, 7172867, 'TQ6');
INSERT INTO bookings VALUES (8757277, 6597799, 6009573, 'KW5');
INSERT INTO bookings VALUES (5410152, 3257367, 7094975, 'ZT4');
INSERT INTO bookings VALUES (5600745, 5820495, 7663248, 'LT3');
INSERT INTO bookings VALUES (9175129, 1784320, 5460257, 'WC4');
INSERT INTO bookings VALUES (9909574, 1365545, 1190190, 'VN7');
INSERT INTO bookings VALUES (4955398, 9765785, 5734273, 'NP7');
INSERT INTO bookings VALUES (3317520, 4789986, 3107948, 'WB3');
INSERT INTO bookings VALUES (7508381, 5408609, 1362332, 'IV7');
INSERT INTO bookings VALUES (7979720, 7143247, 6273381, 'UE5');
INSERT INTO bookings VALUES (7001160, 1381520, 7122531, 'UZ0');
INSERT INTO bookings VALUES (3816059, 3002493, 1801299, 'RX1');
INSERT INTO bookings VALUES (8166274, 4865004, 8566074, 'OJ9');
INSERT INTO bookings VALUES (5398032, 2422409, 5707208, 'DE1');
INSERT INTO bookings VALUES (2660494, 7660641, 1044947, 'CD2');
INSERT INTO bookings VALUES (9796204, 8238843, 8795653, 'TV6');
INSERT INTO bookings VALUES (2608794, 5009563, 2718238, 'GN4');
INSERT INTO bookings VALUES (2485628, 7660641, 6273381, 'BN6');
INSERT INTO bookings VALUES (6634468, 9263093, 6343581, 'RE3');
INSERT INTO bookings VALUES (5299630, 4086038, 8356612, 'QJ1');
INSERT INTO bookings VALUES (6956676, 3738778, 3443010, 'JU6');
INSERT INTO bookings VALUES (1218055, 2975287, 5253868, 'AI9');
INSERT INTO bookings VALUES (1630606, 3135062, 7094490, 'WH1');
INSERT INTO bookings VALUES (6565648, 6527815, 4497853, 'RQ3');
INSERT INTO bookings VALUES (3643738, 7797269, 4997378, 'NU1');
INSERT INTO bookings VALUES (2471375, 7462556, 3920680, 'IQ4');
INSERT INTO bookings VALUES (2641677, 5382959, 4497853, 'CL8');
INSERT INTO bookings VALUES (8385725, 5053396, 7165388, 'FO1');
INSERT INTO bookings VALUES (7441461, 9017430, 4493690, 'JC1');
INSERT INTO bookings VALUES (7785771, 9454921, 3380796, 'FU4');
INSERT INTO bookings VALUES (9636819, 5053396, 9582378, 'ZS1');
INSERT INTO bookings VALUES (9159147, 9478711, 7938221, 'CW4');
INSERT INTO bookings VALUES (5780210, 4308132, 4571129, 'YO5');
INSERT INTO bookings VALUES (7041054, 9218661, 4501881, 'WZ2');
INSERT INTO bookings VALUES (8389626, 3963722, 1983030, 'OB4');
INSERT INTO bookings VALUES (5125411, 5761385, 6794537, 'UG8');
INSERT INTO bookings VALUES (9554283, 3738778, 8566074, 'PQ7');
INSERT INTO bookings VALUES (7605132, 7973380, 7381080, 'UI5');
INSERT INTO bookings VALUES (9230237, 6187608, 3337487, 'LI5');
INSERT INTO bookings VALUES (7443047, 9908742, 6009573, 'HK6');
INSERT INTO bookings VALUES (9730753, 8622373, 9805585, 'KU5');
INSERT INTO bookings VALUES (6737011, 4953636, 6343581, 'LX7');
INSERT INTO bookings VALUES (7804341, 7011025, 7432459, 'FF2');
INSERT INTO bookings VALUES (1022113, 9810226, 1003920, 'UN4');
INSERT INTO bookings VALUES (3070024, 3626944, 1069385, 'WK4');
INSERT INTO bookings VALUES (6760464, 8121189, 6476144, 'WZ0');
INSERT INTO bookings VALUES (3087375, 7859194, 7279683, 'EL5');
INSERT INTO bookings VALUES (1987378, 6091790, 9795505, 'UF5');
INSERT INTO bookings VALUES (8368687, 4078630, 3817976, 'QK2');
INSERT INTO bookings VALUES (1580588, 2905071, 3516493, 'XL3');
INSERT INTO bookings VALUES (4327475, 9607002, 3077667, 'FA0');
INSERT INTO bookings VALUES (1564353, 2347757, 8509070, 'VV8');
INSERT INTO bookings VALUES (6275487, 8957252, 7514371, 'DY7');
INSERT INTO bookings VALUES (5829260, 2881744, 1135543, 'TN7');
INSERT INTO bookings VALUES (8654867, 2729297, 3815787, 'OV0');
INSERT INTO bookings VALUES (7970118, 8155808, 5682084, 'QH7');
INSERT INTO bookings VALUES (7188615, 9017430, 6898752, 'VN0');
INSERT INTO bookings VALUES (9011600, 3879592, 8487202, 'YE8');
INSERT INTO bookings VALUES (4350465, 8770415, 1551780, 'ZN0');
INSERT INTO bookings VALUES (1925111, 9883532, 5460257, 'GT6');
INSERT INTO bookings VALUES (1094879, 7563320, 2319921, 'AV7');
INSERT INTO bookings VALUES (9202936, 2957480, 5855521, 'KV7');
INSERT INTO bookings VALUES (3266291, 8707528, 7444700, 'UX7');
INSERT INTO bookings VALUES (9729343, 3977299, 1038948, 'KQ6');
INSERT INTO bookings VALUES (4957570, 6187608, 1017327, 'LL2');
INSERT INTO bookings VALUES (1117952, 1831583, 4493690, 'EZ2');
INSERT INTO bookings VALUES (8181366, 8371206, 7243278, 'ON3');
INSERT INTO bookings VALUES (4498742, 4149608, 8457243, 'WX7');
INSERT INTO bookings VALUES (6602149, 3166127, 2775069, 'BA8');
INSERT INTO bookings VALUES (4883078, 9765785, 5518639, 'ZS8');
INSERT INTO bookings VALUES (2477578, 4439160, 7640982, 'ML1');
INSERT INTO bookings VALUES (9574806, 8782077, 7893758, 'WU8');
INSERT INTO bookings VALUES (6927298, 8121189, 7243278, 'KK0');
INSERT INTO bookings VALUES (8690075, 1784320, 4752878, 'WC1');
INSERT INTO bookings VALUES (3004616, 4747967, 1069385, 'QK7');
INSERT INTO bookings VALUES (6378259, 9607002, 8168832, 'PR5');
INSERT INTO bookings VALUES (5566203, 8453354, 1118405, 'SI7');
INSERT INTO bookings VALUES (8842771, 6564332, 7640982, 'QJ7');
INSERT INTO bookings VALUES (3737383, 8525725, 9112856, 'FF2');
INSERT INTO bookings VALUES (2816092, 9622165, 6707929, 'FW4');
INSERT INTO bookings VALUES (3826951, 4789986, 8356612, 'RC7');
INSERT INTO bookings VALUES (5862748, 4193399, 5460257, 'YJ2');
INSERT INTO bookings VALUES (2120122, 4078630, 5245347, 'XQ5');
INSERT INTO bookings VALUES (3631878, 4865004, 9582378, 'FC5');
INSERT INTO bookings VALUES (2278844, 9735147, 1768067, 'NY9');
INSERT INTO bookings VALUES (8771079, 3198155, 7094923, 'ZJ1');
INSERT INTO bookings VALUES (3955453, 4859106, 6982308, 'AS2');
INSERT INTO bookings VALUES (7994179, 9478711, 1952545, 'JM7');
INSERT INTO bookings VALUES (2186702, 8770415, 4138126, 'VC1');
INSERT INTO bookings VALUES (2955636, 4439160, 2275551, 'DR1');
INSERT INTO bookings VALUES (7895814, 6419573, 6058824, 'SE2');
INSERT INTO bookings VALUES (3915653, 3135062, 4165078, 'QP9');
INSERT INTO bookings VALUES (6237399, 8216199, 8820639, 'QD3');
INSERT INTO bookings VALUES (3761665, 4821607, 9494788, 'WX6');
INSERT INTO bookings VALUES (5697278, 6091790, 1082936, 'UU8');
INSERT INTO bookings VALUES (4545644, 9270808, 5805421, 'CI1');
INSERT INTO bookings VALUES (2604124, 2024582, 3668668, 'BJ9');
INSERT INTO bookings VALUES (7480159, 8962583, 1128351, 'TI5');
INSERT INTO bookings VALUES (9795356, 3198155, 7275094, 'PX1');
INSERT INTO bookings VALUES (2679883, 8668826, 4373499, 'DL6');
INSERT INTO bookings VALUES (8114347, 3897954, 6433324, 'PK6');
INSERT INTO bookings VALUES (6845329, 2422409, 6678279, 'SU7');
INSERT INTO bookings VALUES (3607816, 4636158, 9128197, 'CK2');
INSERT INTO bookings VALUES (4915213, 2566046, 7593448, 'DL5');
INSERT INTO bookings VALUES (5199194, 6419573, 1069385, 'JF2');
INSERT INTO bookings VALUES (5098787, 6597799, 6288275, 'NK4');
INSERT INTO bookings VALUES (5554072, 5408609, 1034182, 'PA1');
INSERT INTO bookings VALUES (9850808, 7447517, 4163710, 'YK0');
INSERT INTO bookings VALUES (7449172, 4308132, 7618732, 'QB4');
INSERT INTO bookings VALUES (2243340, 4859106, 1128351, 'LO4');
INSERT INTO bookings VALUES (8305182, 1784320, 2998984, 'KE0');
INSERT INTO bookings VALUES (3896222, 2975287, 3429260, 'SP3');
INSERT INTO bookings VALUES (9061441, 8395495, 5017380, 'AB9');
INSERT INTO bookings VALUES (9581094, 7859194, 1190757, 'LB6');
INSERT INTO bookings VALUES (7949126, 6526839, 9608933, 'CL7');
INSERT INTO bookings VALUES (5684808, 2785261, 8487202, 'TX7');
INSERT INTO bookings VALUES (9463019, 5563188, 3516493, 'NC3');
INSERT INTO bookings VALUES (5679024, 8121189, 5028809, 'DA6');
INSERT INTO bookings VALUES (6356585, 2265297, 6288275, 'MQ0');
INSERT INTO bookings VALUES (1504636, 2541341, 5146931, 'OM5');
INSERT INTO bookings VALUES (4867249, 6526839, 4787447, 'ZC0');
INSERT INTO bookings VALUES (8933616, 3270346, 5017380, 'GV3');
INSERT INTO bookings VALUES (3297886, 3106291, 2939707, 'II2');
INSERT INTO bookings VALUES (5848458, 9017430, 3229588, 'AN6');
INSERT INTO bookings VALUES (2296165, 7462556, 5458179, 'XE0');
INSERT INTO bookings VALUES (3813333, 8446918, 5029747, 'GX6');
INSERT INTO bookings VALUES (2752769, 9729552, 7277897, 'GK6');
INSERT INTO bookings VALUES (5338686, 7422573, 1118405, 'HX9');
INSERT INTO bookings VALUES (5248538, 6091790, 6898752, 'AA8');
INSERT INTO bookings VALUES (6875816, 4636158, 8767041, 'NT9');
INSERT INTO bookings VALUES (4213948, 2957480, 8736781, 'KP2');
INSERT INTO bookings VALUES (3298165, 5088004, 9469884, 'MF3');
INSERT INTO bookings VALUES (1240720, 7470714, 6411045, 'PO9');
INSERT INTO bookings VALUES (3137618, 9169482, 3607379, 'JM8');
INSERT INTO bookings VALUES (4141420, 9166027, 5028809, 'MN9');
INSERT INTO bookings VALUES (4786841, 4339947, 4885859, 'VY9');
INSERT INTO bookings VALUES (5489539, 7563320, 2297664, 'PG9');
INSERT INTO bookings VALUES (4412672, 5603263, 4407456, 'MQ1');
INSERT INTO bookings VALUES (1467620, 5382959, 1594856, 'EN3');
INSERT INTO bookings VALUES (4475982, 8446918, 8436230, 'DM8');
INSERT INTO bookings VALUES (2770238, 7083414, 7220120, 'LM1');
INSERT INTO bookings VALUES (6284564, 2729297, 9190463, 'OR8');
INSERT INTO bookings VALUES (3682795, 3977299, 2080096, 'WK3');
INSERT INTO bookings VALUES (5350467, 9169482, 7094923, 'OD8');
INSERT INTO bookings VALUES (4075450, 4017366, 1850920, 'JC1');
INSERT INTO bookings VALUES (7700031, 7412516, 6476144, 'AN0');
INSERT INTO bookings VALUES (6684198, 8957252, 7172867, 'VX6');
INSERT INTO bookings VALUES (5446937, 8594972, 8023566, 'NJ4');
INSERT INTO bookings VALUES (1135875, 6955370, 4942822, 'RK3');
INSERT INTO bookings VALUES (2150512, 8707528, 2718238, 'EG6');
INSERT INTO bookings VALUES (2830246, 9883532, 9250393, 'VX6');
INSERT INTO bookings VALUES (9553892, 4118224, 5287611, 'FO3');
INSERT INTO bookings VALUES (5389195, 8453354, 4502860, 'RV0');
INSERT INTO bookings VALUES (4034151, 2024582, 6288275, 'XB8');
INSERT INTO bookings VALUES (5387239, 4220395, 4035695, 'UX5');
INSERT INTO bookings VALUES (3001561, 4809618, 4108742, 'EE3');
INSERT INTO bookings VALUES (8001981, 4430924, 6884610, 'NC5');
INSERT INTO bookings VALUES (8308581, 2881744, 5734273, 'OD9');
INSERT INTO bookings VALUES (9902612, 1083307, 8832974, 'ES8');
INSERT INTO bookings VALUES (8989326, 8668826, 1807291, 'AM2');
INSERT INTO bookings VALUES (5773547, 7456753, 8311918, 'FS4');
INSERT INTO bookings VALUES (5022328, 2992723, 6717727, 'HM3');
INSERT INTO bookings VALUES (9519881, 8391185, 4986608, 'LL6');
INSERT INTO bookings VALUES (4456251, 5781985, 3668668, 'HX1');
INSERT INTO bookings VALUES (3656501, 2773723, 5805421, 'QT7');
INSERT INTO bookings VALUES (4047341, 2905071, 1044947, 'PN4');
INSERT INTO bookings VALUES (9532390, 5603263, 9859691, 'KX1');
INSERT INTO bookings VALUES (9627285, 4953636, 3108257, 'HM9');
INSERT INTO bookings VALUES (5209607, 8446918, 9647986, 'QL7');
INSERT INTO bookings VALUES (6489369, 9478711, 8539128, 'MN3');
INSERT INTO bookings VALUES (6386914, 8805416, 9417673, 'QA1');
INSERT INTO bookings VALUES (6841707, 9263093, 8574016, 'SZ5');
INSERT INTO bookings VALUES (7409336, 5781985, 3607379, 'DG9');
INSERT INTO bookings VALUES (5727927, 6348851, 4187430, 'TW6');
INSERT INTO bookings VALUES (7734309, 7462556, 9148557, 'NB0');
INSERT INTO bookings VALUES (2738783, 5030264, 6947320, 'AE3');
INSERT INTO bookings VALUES (9805983, 3655021, 4229693, 'KN0');
INSERT INTO bookings VALUES (3094275, 5603263, 3577336, 'FP6');
INSERT INTO bookings VALUES (9979672, 4278613, 3817976, 'YG6');
INSERT INTO bookings VALUES (1013555, 6419573, 7323048, 'TG7');
INSERT INTO bookings VALUES (4081283, 2193325, 3263627, 'VU4');
INSERT INTO bookings VALUES (2035003, 3270346, 1190757, 'WT6');
INSERT INTO bookings VALUES (8530334, 4747967, 6947320, 'GM5');
INSERT INTO bookings VALUES (9469323, 3437925, 2434115, 'JG1');
INSERT INTO bookings VALUES (8913773, 6419573, 3302290, 'GP5');
INSERT INTO bookings VALUES (8262564, 2974225, 7640982, 'OC1');
INSERT INTO bookings VALUES (1066490, 1784320, 4953397, 'EO8');
INSERT INTO bookings VALUES (5097027, 2079829, 6787581, 'ZX8');
INSERT INTO bookings VALUES (7935761, 9607002, 7525533, 'TT6');
INSERT INTO bookings VALUES (9841462, 3963722, 2718238, 'UA2');
INSERT INTO bookings VALUES (3550665, 5683800, 4225434, 'MT8');
INSERT INTO bookings VALUES (8326711, 4865004, 3580596, 'OY0');
INSERT INTO bookings VALUES (2590466, 5088004, 2443174, 'KG2');
INSERT INTO bookings VALUES (8001548, 5683800, 7363844, 'TW0');
INSERT INTO bookings VALUES (2414536, 8668826, 6296538, 'UC2');
INSERT INTO bookings VALUES (6617595, 5603263, 1277419, 'NT9');
INSERT INTO bookings VALUES (3751315, 3784950, 5017380, 'HQ7');
INSERT INTO bookings VALUES (8846685, 6526839, 3107948, 'LD6');
INSERT INTO bookings VALUES (6896768, 2079829, 4390317, 'CS7');
INSERT INTO bookings VALUES (2504743, 7412516, 9494788, 'CT5');
INSERT INTO bookings VALUES (4236935, 8962583, 7843855, 'NL0');
INSERT INTO bookings VALUES (2157626, 9765785, 1084740, 'FH7');
INSERT INTO bookings VALUES (6733255, 9810226, 6773294, 'VI3');
INSERT INTO bookings VALUES (6554265, 1594316, 8357045, 'PE8');
INSERT INTO bookings VALUES (4620058, 9735147, 3263627, 'NR9');
INSERT INTO bookings VALUES (7659076, 8453354, 3533222, 'DB8');
INSERT INTO bookings VALUES (1343920, 9270808, 2502371, 'RT7');
INSERT INTO bookings VALUES (1347910, 5563188, 3162135, 'WB4');
INSERT INTO bookings VALUES (8248188, 8395495, 6433324, 'RU3');
INSERT INTO bookings VALUES (8424005, 7011025, 4162870, 'QJ0');
INSERT INTO bookings VALUES (9553921, 6187608, 3580596, 'UE7');
INSERT INTO bookings VALUES (8348918, 3713051, 8736781, 'JS9');
INSERT INTO bookings VALUES (9907707, 2881744, 7569872, 'VR4');
INSERT INTO bookings VALUES (5981197, 7797269, 5631640, 'VE4');
INSERT INTO bookings VALUES (8272495, 2881744, 7821647, 'GA2');
INSERT INTO bookings VALUES (7154320, 3738778, 1850920, 'LU3');
INSERT INTO bookings VALUES (5196440, 2541341, 4156979, 'DZ6');
INSERT INTO bookings VALUES (9834055, 7011025, 3868324, 'MN4');
INSERT INTO bookings VALUES (9254653, 8668826, 3580596, 'FZ1');
INSERT INTO bookings VALUES (4837146, 7447517, 6009573, 'BA5');
INSERT INTO bookings VALUES (6864111, 6589761, 4493690, 'WD0');
INSERT INTO bookings VALUES (1590258, 9735147, 1277419, 'BQ0');
INSERT INTO bookings VALUES (7116430, 8755012, 4501881, 'KR9');
INSERT INTO bookings VALUES (4543393, 1381520, 8457243, 'CR9');
INSERT INTO bookings VALUES (4717614, 8517884, 4841212, 'BL9');
INSERT INTO bookings VALUES (2382367, 3418931, 9837984, 'ZB1');
INSERT INTO bookings VALUES (3772289, 5105774, 7147855, 'DB6');
INSERT INTO bookings VALUES (5894896, 4507387, 8600776, 'LP4');
INSERT INTO bookings VALUES (2967948, 4865004, 8882848, 'CJ2');
INSERT INTO bookings VALUES (5825173, 4678821, 5041690, 'SA9');
INSERT INTO bookings VALUES (5017494, 7462556, 6411045, 'CN5');
INSERT INTO bookings VALUES (9417885, 5497703, 2566204, 'JD8');
INSERT INTO bookings VALUES (3481701, 4278613, 4762283, 'LD8');
INSERT INTO bookings VALUES (8702813, 3440276, 6343581, 'JV3');
INSERT INTO bookings VALUES (4314732, 5088004, 9279751, 'VP8');
INSERT INTO bookings VALUES (7748282, 4287773, 4187430, 'QH2');
INSERT INTO bookings VALUES (1524478, 9883532, 7154876, 'VI1');
INSERT INTO bookings VALUES (6686189, 8805416, 7663695, 'YB4');
INSERT INTO bookings VALUES (9790909, 5185820, 3106369, 'MK1');
INSERT INTO bookings VALUES (3197434, 3135062, 9859691, 'YC3');
INSERT INTO bookings VALUES (7769736, 8446918, 5518639, 'SE6');
INSERT INTO bookings VALUES (6202997, 3352316, 8499966, 'VY2');
INSERT INTO bookings VALUES (3382167, 5382959, 6655240, 'OG9');
INSERT INTO bookings VALUES (7378637, 5719835, 7243278, 'EV9');
INSERT INTO bookings VALUES (4294251, 8453354, 7195079, 'NE3');
INSERT INTO bookings VALUES (2178045, 5030264, 2443174, 'MF8');
INSERT INTO bookings VALUES (7240147, 6904458, 9190463, 'EC0');
INSERT INTO bookings VALUES (9906347, 2483269, 7039442, 'EB0');
INSERT INTO bookings VALUES (9193407, 6091790, 5032808, 'QL5');
INSERT INTO bookings VALUES (5068903, 4678821, 8168832, 'SF3');
INSERT INTO bookings VALUES (8533268, 8238843, 2422914, 'BJ0');
INSERT INTO bookings VALUES (7579701, 1381520, 8827461, 'SZ2');
INSERT INTO bookings VALUES (1929813, 8517884, 5537933, 'XG0');
INSERT INTO bookings VALUES (2098260, 1784320, 3263627, 'HC1');
INSERT INTO bookings VALUES (9540075, 9607002, 8795653, 'PY0');
INSERT INTO bookings VALUES (4691878, 4149608, 3735790, 'YA7');
INSERT INTO bookings VALUES (8010096, 6187608, 1939454, 'FG0');
INSERT INTO bookings VALUES (1683810, 7595919, 1362332, 'QR5');
INSERT INTO bookings VALUES (8157454, 8525725, 9795505, 'VE9');
INSERT INTO bookings VALUES (3420932, 5683800, 5028809, 'SS3');
INSERT INTO bookings VALUES (9186940, 3270346, 4390317, 'IM3');
INSERT INTO bookings VALUES (2803459, 1308410, 9782865, 'CL2');
INSERT INTO bookings VALUES (1367157, 8957252, 3522915, 'AE8');
INSERT INTO bookings VALUES (6598224, 4859106, 4571129, 'EZ3');
INSERT INTO bookings VALUES (9710194, 7885995, 7094975, 'EL2');
INSERT INTO bookings VALUES (1106241, 6976359, 1742490, 'ET3');
INSERT INTO bookings VALUES (2419947, 2773723, 3533222, 'IS2');
INSERT INTO bookings VALUES (3763640, 1253566, 7648863, 'FZ1');
INSERT INTO bookings VALUES (6581034, 8395495, 1082936, 'OC9');
INSERT INTO bookings VALUES (5027298, 6526839, 5410579, 'ZD6');
INSERT INTO bookings VALUES (6448352, 5719835, 7388768, 'NY1');
INSERT INTO bookings VALUES (7838776, 3437925, 2609422, 'JF2');
INSERT INTO bookings VALUES (5060382, 5332651, 8566074, 'TG0');
INSERT INTO bookings VALUES (9278508, 6351366, 8360579, 'GB1');
INSERT INTO bookings VALUES (6660303, 8770415, 7648863, 'DP0');
INSERT INTO bookings VALUES (5696323, 7660641, 2794631, 'NT4');
INSERT INTO bookings VALUES (9665882, 7515878, 4492728, 'ML6');
INSERT INTO bookings VALUES (6935953, 4086038, 3021844, 'AK6');
INSERT INTO bookings VALUES (7342857, 5009563, 6288275, 'TT6');
INSERT INTO bookings VALUES (7829109, 7447517, 1135543, 'EB7');
INSERT INTO bookings VALUES (9747931, 7660641, 2434115, 'MU9');
INSERT INTO bookings VALUES (9220137, 2483269, 4388944, 'LR5');
INSERT INTO bookings VALUES (8122337, 6526839, 3316632, 'BR4');
INSERT INTO bookings VALUES (8200824, 6589761, 3868324, 'EW4');
INSERT INTO bookings VALUES (6643507, 4678821, 9311534, 'KJ3');
INSERT INTO bookings VALUES (6451503, 7797269, 4108742, 'AB8');
INSERT INTO bookings VALUES (1245985, 2729297, 1044947, 'RR0');
INSERT INTO bookings VALUES (8549424, 3944798, 8360355, 'GZ3');
INSERT INTO bookings VALUES (6567925, 4636158, 1135543, 'TR2');
INSERT INTO bookings VALUES (2289604, 2566046, 1472543, 'BT5');
INSERT INTO bookings VALUES (1425553, 2680208, 5041690, 'IM5');
INSERT INTO bookings VALUES (2428666, 4809618, 3684041, 'OB0');
INSERT INTO bookings VALUES (8580033, 3897954, 6884610, 'GM5');
INSERT INTO bookings VALUES (6518017, 3257367, 1123836, 'VJ5');
INSERT INTO bookings VALUES (9707415, 7180424, 3229588, 'DB1');
INSERT INTO bookings VALUES (5292000, 5030264, 2117824, 'NJ4');
INSERT INTO bookings VALUES (9265285, 1365545, 4108742, 'JT0');
INSERT INTO bookings VALUES (4152310, 8962583, 2472315, 'CM2');
INSERT INTO bookings VALUES (8517448, 4220395, 7363844, 'IE0');
INSERT INTO bookings VALUES (9845042, 9908742, 1595358, 'DN9');
INSERT INTO bookings VALUES (8312360, 7447517, 8360579, 'LQ5');
INSERT INTO bookings VALUES (3798579, 1381520, 1768067, 'WU1');
INSERT INTO bookings VALUES (4329645, 4747967, 3443010, 'HQ7');
INSERT INTO bookings VALUES (2273239, 7556383, 6982308, 'SF5');
INSERT INTO bookings VALUES (5299137, 2024582, 7312338, 'PA6');
INSERT INTO bookings VALUES (4311819, 6728086, 1082936, 'KG6');
INSERT INTO bookings VALUES (5625204, 4308132, 7522865, 'QQ7');
INSERT INTO bookings VALUES (1460126, 8962583, 7388768, 'MQ7');
INSERT INTO bookings VALUES (5377105, 4339947, 7112514, 'CK2');
INSERT INTO bookings VALUES (4876715, 2193325, 9311534, 'WR2');
INSERT INTO bookings VALUES (9856719, 8216199, 6273381, 'HE6');
INSERT INTO bookings VALUES (9911329, 8446918, 6678279, 'FH0');
INSERT INTO bookings VALUES (8849806, 7092950, 5518639, 'GF0');
INSERT INTO bookings VALUES (7980476, 3418931, 9376507, 'HF9');
INSERT INTO bookings VALUES (1599267, 7797269, 6540542, 'JR6');
INSERT INTO bookings VALUES (5386577, 3343612, 3868550, 'QK5');
INSERT INTO bookings VALUES (3394623, 3440276, 1084740, 'BV5');
INSERT INTO bookings VALUES (3067844, 4678821, 4077580, 'ZB8');
INSERT INTO bookings VALUES (2863744, 2940416, 3263627, 'JS8');
INSERT INTO bookings VALUES (8070249, 7180424, 7004288, 'TZ7');
INSERT INTO bookings VALUES (3501854, 3784950, 6433324, 'FC8');
INSERT INTO bookings VALUES (2279482, 8707528, 3443010, 'MY4');
INSERT INTO bookings VALUES (7403766, 4308132, 6260587, 'IY0');
INSERT INTO bookings VALUES (9610175, 2773723, 7122531, 'ZW4');
INSERT INTO bookings VALUES (3342522, 1185144, 4163710, 'QS6');
INSERT INTO bookings VALUES (2463676, 2483269, 5146931, 'EG9');
INSERT INTO bookings VALUES (2393337, 8391185, 1950871, 'LN1');
INSERT INTO bookings VALUES (2833716, 5719835, 7279683, 'YZ0');
INSERT INTO bookings VALUES (8166147, 6039236, 6884610, 'FT0');
INSERT INTO bookings VALUES (7379762, 8238843, 5253868, 'JA8');
INSERT INTO bookings VALUES (7571140, 5563188, 5029747, 'GC8');
INSERT INTO bookings VALUES (7237867, 1365545, 8399090, 'ZL3');
INSERT INTO bookings VALUES (8299339, 5332651, 4075438, 'GH8');
INSERT INTO bookings VALUES (3024382, 4141937, 4205310, 'UH5');
INSERT INTO bookings VALUES (3674035, 2013023, 2031226, 'RK3');
INSERT INTO bookings VALUES (6489947, 3418931, 7275094, 'IF5');
INSERT INTO bookings VALUES (9687359, 9263093, 6888305, 'FP1');
INSERT INTO bookings VALUES (2014039, 6856915, 7893758, 'EX6');
INSERT INTO bookings VALUES (7629913, 5030264, 3668668, 'LR3');
INSERT INTO bookings VALUES (3945756, 7865203, 4942822, 'NM1');
INSERT INTO bookings VALUES (7527973, 9478711, 8600776, 'OT8');
INSERT INTO bookings VALUES (2900907, 5720403, 8425634, 'NQ5');
INSERT INTO bookings VALUES (6484248, 3002493, 8522282, 'LH6');
INSERT INTO bookings VALUES (7921770, 8809406, 3162135, 'OO3');
INSERT INTO bookings VALUES (3593651, 3897954, 2117824, 'RE3');
INSERT INTO bookings VALUES (1427341, 8216199, 5029747, 'OB4');
INSERT INTO bookings VALUES (2457995, 8216199, 7094975, 'FZ1');
INSERT INTO bookings VALUES (7516380, 7092950, 5351188, 'TO9');
INSERT INTO bookings VALUES (7215413, 4865004, 3815787, 'LL2');
INSERT INTO bookings VALUES (2684538, 8371206, 9469884, 'KU7');
INSERT INTO bookings VALUES (3171847, 2216117, 4229693, 'ZN6');
INSERT INTO bookings VALUES (5179993, 8216199, 9086205, 'WK1');
INSERT INTO bookings VALUES (1524694, 9735147, 6343581, 'IM1');
INSERT INTO bookings VALUES (9868956, 2905071, 9647986, 'HC1');
INSERT INTO bookings VALUES (6995828, 8625276, 3107948, 'PV7');
INSERT INTO bookings VALUES (5422054, 7859194, 5617426, 'QV5');
INSERT INTO bookings VALUES (1694833, 4636158, 6839691, 'DH0');
INSERT INTO bookings VALUES (6412375, 8173474, 8399090, 'GK4');
INSERT INTO bookings VALUES (4992028, 8155808, 4082860, 'BZ6');
INSERT INTO bookings VALUES (5053331, 8755012, 1118405, 'OJ4');
INSERT INTO bookings VALUES (8686730, 9259092, 4172964, 'FV1');
INSERT INTO bookings VALUES (1090159, 6527815, 3021844, 'JV1');
INSERT INTO bookings VALUES (1833264, 1083307, 9171738, 'GH5');
INSERT INTO bookings VALUES (2587001, 7092950, 6296538, 'GR6');
INSERT INTO bookings VALUES (7398463, 9259092, 1118405, 'LA2');
INSERT INTO bookings VALUES (4362846, 4821607, 8795653, 'TJ8');
INSERT INTO bookings VALUES (1616428, 9258677, 4187430, 'SB3');
INSERT INTO bookings VALUES (5981268, 4017366, 3735507, 'ZZ0');
INSERT INTO bookings VALUES (7674918, 1102212, 9782865, 'BI1');
INSERT INTO bookings VALUES (5600563, 6904458, 9859691, 'VB3');
INSERT INTO bookings VALUES (6283664, 8391185, 3896243, 'NE2');
INSERT INTO bookings VALUES (9591265, 2265297, 1801299, 'UN8');
INSERT INTO bookings VALUES (2044708, 2024582, 3533222, 'RI9');
INSERT INTO bookings VALUES (8761363, 3655021, 2718238, 'PE0');
INSERT INTO bookings VALUES (4417336, 8525725, 4050052, 'UW4');
INSERT INTO bookings VALUES (7477967, 6039236, 4163710, 'RS8');
INSERT INTO bookings VALUES (3607319, 9622165, 4752878, 'GC2');
INSERT INTO bookings VALUES (3264931, 7456753, 5028809, 'UG2');
INSERT INTO bookings VALUES (6949781, 7885995, 6433324, 'JG7');
INSERT INTO bookings VALUES (9911328, 9883532, 4497548, 'XZ0');
INSERT INTO bookings VALUES (1110342, 8121189, 8915578, 'NI4');
INSERT INTO bookings VALUES (8479281, 7371009, 4390317, 'FV6');
INSERT INTO bookings VALUES (9646368, 2347757, 1184323, 'IT8');
INSERT INTO bookings VALUES (9777525, 7092950, 3735507, 'RK3');
INSERT INTO bookings VALUES (6045324, 3816929, 2963186, 'SO0');
INSERT INTO bookings VALUES (2683840, 5009563, 4534704, 'TP3');
INSERT INTO bookings VALUES (9212721, 2975287, 5707208, 'IE3');
INSERT INTO bookings VALUES (7784582, 2773723, 8551136, 'BZ8');
INSERT INTO bookings VALUES (7797657, 6348851, 2794631, 'GY8');
INSERT INTO bookings VALUES (9270301, 9488847, 3429260, 'HE3');
INSERT INTO bookings VALUES (8201938, 3738778, 8915578, 'JZ6');
INSERT INTO bookings VALUES (8565031, 1253566, 2794631, 'RS8');
INSERT INTO bookings VALUES (5782852, 9356593, 5405591, 'KY8');
INSERT INTO bookings VALUES (7477072, 5105774, 2727975, 'IS2');
INSERT INTO bookings VALUES (8473866, 2905071, 6678279, 'BI0');
INSERT INTO bookings VALUES (5329863, 8395495, 3443010, 'FG8');
INSERT INTO bookings VALUES (9154005, 2680208, 2431798, 'AD4');
INSERT INTO bookings VALUES (6800002, 5030264, 4162870, 'PM6');
INSERT INTO bookings VALUES (5100679, 2079829, 7618732, 'BH4');
INSERT INTO bookings VALUES (7555162, 9259092, 7094975, 'WA8');
INSERT INTO bookings VALUES (1740429, 9488847, 5287611, 'JN3');
INSERT INTO bookings VALUES (6952731, 6348851, 8627402, 'BX2');
INSERT INTO bookings VALUES (8289648, 6856915, 7593448, 'JI9');
INSERT INTO bookings VALUES (3481798, 8755012, 9678194, 'HN8');
INSERT INTO bookings VALUES (3752850, 7859194, 5337919, 'HX8');
INSERT INTO bookings VALUES (8575372, 4308132, 9837984, 'AP5');
INSERT INTO bookings VALUES (1540365, 8371206, 7522865, 'ON5');
INSERT INTO bookings VALUES (9068057, 3713051, 5631640, 'PK1');
INSERT INTO bookings VALUES (5913348, 2975287, 5160345, 'JO2');
INSERT INTO bookings VALUES (1077452, 3002493, 7195079, 'ZQ6');
INSERT INTO bookings VALUES (4275721, 7083414, 2794631, 'OX1');
INSERT INTO bookings VALUES (2472618, 2773723, 9676209, 'SC8');
INSERT INTO bookings VALUES (7321909, 5105774, 4356416, 'TX2');
INSERT INTO bookings VALUES (2416661, 6589761, 6773294, 'FU4');
INSERT INTO bookings VALUES (4953120, 7371009, 9171399, 'SK0');
INSERT INTO bookings VALUES (2017491, 9454921, 3564294, 'BY3');
INSERT INTO bookings VALUES (4573690, 3198155, 7147855, 'YA0');
INSERT INTO bookings VALUES (9945085, 4141937, 4356416, 'IW1');
INSERT INTO bookings VALUES (4957524, 5277747, 2431798, 'ZB1');
INSERT INTO bookings VALUES (8410126, 6762919, 8641311, 'BK2');
INSERT INTO bookings VALUES (4822334, 3977299, 5160345, 'IB9');
INSERT INTO bookings VALUES (9710730, 7515878, 9112856, 'OW8');
INSERT INTO bookings VALUES (4680410, 6348851, 4953397, 'KY7');
INSERT INTO bookings VALUES (1285035, 7515878, 7112514, 'XZ2');
INSERT INTO bookings VALUES (9910656, 8962583, 5430391, 'GU8');
INSERT INTO bookings VALUES (8694998, 9478711, 3533222, 'HP7');
INSERT INTO bookings VALUES (3623130, 8755012, 7618732, 'SJ0');
INSERT INTO bookings VALUES (7468145, 2975287, 5337919, 'EC7');
INSERT INTO bookings VALUES (1063281, 9999465, 5682084, 'YO9');
INSERT INTO bookings VALUES (1016638, 7180424, 9649921, 'SD9');
INSERT INTO bookings VALUES (2718768, 1102212, 7039442, 'HV2');
INSERT INTO bookings VALUES (3655198, 5719835, 8499966, 'EO6');
INSERT INTO bookings VALUES (7973981, 4086038, 4737821, 'IU6');
INSERT INTO bookings VALUES (6424865, 1594316, 4072363, 'GM5');
INSERT INTO bookings VALUES (7288639, 9883532, 4050052, 'WV2');
INSERT INTO bookings VALUES (4700172, 4507387, 7514371, 'KM6');
INSERT INTO bookings VALUES (1757886, 7447517, 4058181, 'CN4');
INSERT INTO bookings VALUES (8745523, 4197805, 2434115, 'ZR3');
INSERT INTO bookings VALUES (6180429, 2483269, 7279683, 'KA1');
INSERT INTO bookings VALUES (1239047, 3504500, 1128351, 'TA3');
INSERT INTO bookings VALUES (5229706, 9259092, 5245347, 'UX0');
INSERT INTO bookings VALUES (2155577, 6564332, 9474064, 'EG6');
INSERT INTO bookings VALUES (3687119, 2905071, 4737821, 'SI6');
INSERT INTO bookings VALUES (3381353, 6955370, 2519795, 'NQ7');
INSERT INTO bookings VALUES (7487130, 1381520, 4911547, 'FA2');
INSERT INTO bookings VALUES (8826297, 7092950, 7300889, 'EE9');
INSERT INTO bookings VALUES (2720614, 4287773, 3351825, 'OP5');
INSERT INTO bookings VALUES (7661921, 9883532, 1190190, 'ZG4');
INSERT INTO bookings VALUES (1117814, 5088004, 7522865, 'UF4');
INSERT INTO bookings VALUES (1680444, 1308410, 1554195, 'VL8');
INSERT INTO bookings VALUES (4345626, 7371009, 4390317, 'AJ9');
INSERT INTO bookings VALUES (1970761, 6526839, 3580596, 'XF5');
INSERT INTO bookings VALUES (1260394, 6348851, 5734273, 'ZH2');
INSERT INTO bookings VALUES (9469272, 5053396, 5028809, 'ZI0');
INSERT INTO bookings VALUES (1707120, 8453354, 3984898, 'YV1');
INSERT INTO bookings VALUES (1605045, 3440276, 8251078, 'PK7');
INSERT INTO bookings VALUES (9382549, 5683800, 4501881, 'SB2');
INSERT INTO bookings VALUES (5864993, 5798843, 4077580, 'LM2');
INSERT INTO bookings VALUES (5873348, 9765785, 3577336, 'HA8');
INSERT INTO bookings VALUES (9708665, 3944798, 5617426, 'SG9');
INSERT INTO bookings VALUES (4370702, 7447517, 8566074, 'PN3');
INSERT INTO bookings VALUES (2232533, 8517884, 5480356, 'UT4');
INSERT INTO bookings VALUES (9225835, 7885995, 3580596, 'OG4');
INSERT INTO bookings VALUES (3991717, 7973380, 9795505, 'LR4');
INSERT INTO bookings VALUES (1682824, 6762919, 7388768, 'MU2');
INSERT INTO bookings VALUES (1618469, 3577168, 1084740, 'DZ8');
INSERT INTO bookings VALUES (8515591, 7180424, 6273381, 'GA5');
INSERT INTO bookings VALUES (3854054, 6066735, 3735507, 'ON5');
INSERT INTO bookings VALUES (6370619, 7092950, 8669614, 'DE1');
INSERT INTO bookings VALUES (2237331, 7422573, 1609427, 'PN0');
INSERT INTO bookings VALUES (4311349, 3198155, 6982308, 'YC9');
INSERT INTO bookings VALUES (1940080, 3738778, 5458179, 'AU4');
INSERT INTO bookings VALUES (8906079, 7456753, 3607379, 'OB8');
INSERT INTO bookings VALUES (2847786, 9488847, 2775069, 'IN8');
INSERT INTO bookings VALUES (4546533, 6762919, 6884610, 'LH6');
INSERT INTO bookings VALUES (9853343, 3713051, 3802445, 'IZ1');
INSERT INTO bookings VALUES (6123296, 8371206, 6251192, 'AU9');
INSERT INTO bookings VALUES (1803104, 2193325, 6411045, 'WO0');
INSERT INTO bookings VALUES (3423116, 7973380, 7848741, 'SA4');
INSERT INTO bookings VALUES (2424952, 2483269, 6787581, 'LD1');
INSERT INTO bookings VALUES (8732178, 7456753, 2013782, 'DL9');
INSERT INTO bookings VALUES (4462033, 2974225, 6273381, 'GX7');
INSERT INTO bookings VALUES (9830786, 8502145, 3817976, 'QB9');
INSERT INTO bookings VALUES (5894239, 3166127, 7848741, 'SD2');
INSERT INTO bookings VALUES (6480707, 8395495, 2566204, 'IZ3');
INSERT INTO bookings VALUES (5445234, 4017366, 9279751, 'TK9');
INSERT INTO bookings VALUES (8459141, 2992723, 2116507, 'QH0');
INSERT INTO bookings VALUES (4251915, 4422051, 6396837, 'WI4');
INSERT INTO bookings VALUES (8225682, 7011025, 8665254, 'CL4');
INSERT INTO bookings VALUES (6805911, 7515878, 7094490, 'YN7');
INSERT INTO bookings VALUES (5369926, 6856915, 3061980, 'AM8');
INSERT INTO bookings VALUES (3687015, 7092950, 1101672, 'QR7');
INSERT INTO bookings VALUES (8503428, 5286908, 3337487, 'PN6');
INSERT INTO bookings VALUES (5176208, 8809406, 4162870, 'CP8');
INSERT INTO bookings VALUES (1950852, 2422409, 4509881, 'FJ9');
INSERT INTO bookings VALUES (3300396, 7595919, 6528552, 'LZ1');
INSERT INTO bookings VALUES (6874537, 3681623, 4224585, 'QC0');
INSERT INTO bookings VALUES (1200346, 6955370, 1044947, 'HW8');
INSERT INTO bookings VALUES (7081554, 7412516, 3106369, 'OW2');
INSERT INTO bookings VALUES (3210273, 4078630, 8319166, 'WM4');
INSERT INTO bookings VALUES (1112634, 4220395, 3516493, 'EV7');
INSERT INTO bookings VALUES (8535453, 1185144, 7381080, 'MF7');
INSERT INTO bookings VALUES (4845198, 3944798, 7300889, 'WS0');
INSERT INTO bookings VALUES (9865161, 8216199, 2727975, 'TF3');
INSERT INTO bookings VALUES (5968325, 5088004, 1148814, 'CH9');
INSERT INTO bookings VALUES (4889610, 7660641, 7094975, 'EC4');
INSERT INTO bookings VALUES (4936817, 2013023, 1742490, 'IT9');
INSERT INTO bookings VALUES (7891432, 6564332, 2031226, 'SN8');
INSERT INTO bookings VALUES (6663639, 6526839, 7279683, 'VB0');
INSERT INTO bookings VALUES (7534066, 2957480, 5617426, 'GL0');
INSERT INTO bookings VALUES (1751272, 6955370, 7705816, 'QL4');
INSERT INTO bookings VALUES (6737528, 2461695, 7759286, 'ZU1');
INSERT INTO bookings VALUES (7052319, 6856915, 1594856, 'TT3');
INSERT INTO bookings VALUES (4682247, 8502145, 5017380, 'IW3');
INSERT INTO bookings VALUES (8710293, 1185144, 2094934, 'UW3');
INSERT INTO bookings VALUES (5980600, 4821607, 9171738, 'CA2');
INSERT INTO bookings VALUES (6353762, 7422573, 2191318, 'MJ4');
INSERT INTO bookings VALUES (9711510, 5798843, 7094923, 'CZ0');
INSERT INTO bookings VALUES (5561853, 9270808, 9171399, 'PG8');
INSERT INTO bookings VALUES (8457895, 2785261, 1069385, 'UQ9');
INSERT INTO bookings VALUES (8395913, 6091790, 7663248, 'PO4');
INSERT INTO bookings VALUES (7759751, 3963722, 1069385, 'KA6');
INSERT INTO bookings VALUES (4367259, 9259092, 8641311, 'UO3');
INSERT INTO bookings VALUES (8760897, 3897954, 8665254, 'IZ7');
INSERT INTO bookings VALUES (4178484, 8625276, 4224585, 'HF4');
INSERT INTO bookings VALUES (2818820, 4141937, 1952545, 'UD6');
INSERT INTO bookings VALUES (1737402, 7885995, 3533222, 'MK8');
INSERT INTO bookings VALUES (3932120, 4017366, 9678194, 'JX6');
INSERT INTO bookings VALUES (2511179, 1594316, 1044947, 'LX2');
INSERT INTO bookings VALUES (4079420, 6066735, 7004288, 'OV9');
INSERT INTO bookings VALUES (3763327, 9622165, 7260938, 'WB3');
INSERT INTO bookings VALUES (9972381, 8622373, 9112856, 'BV4');
INSERT INTO bookings VALUES (8026826, 1128509, 1801299, 'CA4');
INSERT INTO bookings VALUES (1142136, 4118224, 5827465, 'XE3');
INSERT INTO bookings VALUES (5413381, 3423018, 7323048, 'MT4');
INSERT INTO bookings VALUES (9720909, 8805416, 3263627, 'OT0');
INSERT INTO bookings VALUES (8513277, 7660641, 5631640, 'MK1');
INSERT INTO bookings VALUES (2653273, 3963722, 1939454, 'VO3');
INSERT INTO bookings VALUES (3398648, 3211200, 7618732, 'VL7');
INSERT INTO bookings VALUES (4125206, 9356593, 9474064, 'VO6');
INSERT INTO bookings VALUES (2940006, 9258677, 2566204, 'AM0');
INSERT INTO bookings VALUES (5219857, 6597799, 8600776, 'YG8');
INSERT INTO bookings VALUES (6115972, 6039236, 9676209, 'YJ0');
INSERT INTO bookings VALUES (6432353, 2079829, 9837984, 'TT9');
INSERT INTO bookings VALUES (2847412, 3179127, 1277419, 'CX4');
INSERT INTO bookings VALUES (5529625, 5030264, 7277897, 'PS8');
INSERT INTO bookings VALUES (2469302, 6526839, 2297664, 'TG2');
INSERT INTO bookings VALUES (5303671, 9883532, 7663695, 'JI5');
INSERT INTO bookings VALUES (2823971, 9999465, 2472315, 'FT2');
INSERT INTO bookings VALUES (9277551, 4678821, 4502860, 'YY6');
INSERT INTO bookings VALUES (6856072, 7143247, 4737821, 'BE8');
INSERT INTO bookings VALUES (2343284, 8845953, 5682084, 'NI8');
INSERT INTO bookings VALUES (3263570, 3760433, 3351825, 'FY5');
INSERT INTO bookings VALUES (6856604, 2461695, 7341226, 'ML9');
INSERT INTO bookings VALUES (5573187, 7797269, 1574416, 'ZD6');
INSERT INTO bookings VALUES (7194577, 9810226, 5631640, 'CK1');
INSERT INTO bookings VALUES (1537021, 9622165, 4072363, 'LF8');
INSERT INTO bookings VALUES (5997369, 5277747, 6888305, 'XH6');
INSERT INTO bookings VALUES (8911806, 3437925, 8360579, 'JT2');
INSERT INTO bookings VALUES (6565997, 1128509, 3382876, 'EX4');
INSERT INTO bookings VALUES (9770279, 7563320, 2998984, 'IF5');
INSERT INTO bookings VALUES (8838296, 6091790, 8627402, 'LO3');
INSERT INTO bookings VALUES (9573406, 6039236, 5827465, 'JT7');
INSERT INTO bookings VALUES (5566665, 9218661, 5017380, 'YF3');
INSERT INTO bookings VALUES (9995399, 4507387, 7409070, 'UF4');
INSERT INTO bookings VALUES (8727942, 4193399, 2609422, 'QN9');
INSERT INTO bookings VALUES (8304737, 3577168, 6528552, 'JQ7');
INSERT INTO bookings VALUES (1525756, 1253566, 4942822, 'SZ7');
INSERT INTO bookings VALUES (3929656, 2654571, 4390317, 'NC1');
INSERT INTO bookings VALUES (7503499, 4086038, 9468998, 'QO3');
INSERT INTO bookings VALUES (3118440, 8395495, 5480356, 'QW0');
INSERT INTO bookings VALUES (9633280, 8517884, 8667285, 'ZI5');
INSERT INTO bookings VALUES (5903727, 5603263, 1359170, 'VH0');
INSERT INTO bookings VALUES (1997847, 7422573, 9805585, 'MA2');
INSERT INTO bookings VALUES (3029273, 8782077, 4860180, 'WX2');
INSERT INTO bookings VALUES (8193176, 3897954, 3106369, 'CO0');
INSERT INTO bookings VALUES (9482926, 4339947, 4997378, 'EG3');
INSERT INTO bookings VALUES (8932595, 5382959, 2828272, 'LY2');
INSERT INTO bookings VALUES (3967442, 3002493, 7648856, 'WE6');
INSERT INTO bookings VALUES (9914125, 3106291, 7514371, 'CB8');
INSERT INTO bookings VALUES (5256816, 7797269, 7843855, 'FQ4');
INSERT INTO bookings VALUES (9197064, 5781985, 4055794, 'ZW3');
INSERT INTO bookings VALUES (8561208, 8622373, 4752878, 'IL6');
INSERT INTO bookings VALUES (5349444, 3418931, 1128351, 'LN3');
INSERT INTO bookings VALUES (7121565, 7556383, 9469884, 'OU0');
INSERT INTO bookings VALUES (5609759, 6526839, 8627402, 'DT0');
INSERT INTO bookings VALUES (6220369, 3166127, 7604448, 'OL5');
INSERT INTO bookings VALUES (2778362, 9342246, 7275094, 'GW9');
INSERT INTO bookings VALUES (2327376, 8668826, 9279751, 'OJ4');
INSERT INTO bookings VALUES (5469436, 7422573, 3302290, 'VU0');
INSERT INTO bookings VALUES (1769331, 4141937, 3815787, 'LM2');
INSERT INTO bookings VALUES (4591378, 6419573, 8827461, 'SQ0');
INSERT INTO bookings VALUES (2103439, 9622165, 3580504, 'DT9');
INSERT INTO bookings VALUES (4394206, 2265297, 5041690, 'FP1');
INSERT INTO bookings VALUES (8262925, 3423018, 5017380, 'KY7');
INSERT INTO bookings VALUES (8494594, 8173474, 8551136, 'ZE2');
INSERT INTO bookings VALUES (5499191, 8668826, 5888739, 'PL5');
INSERT INTO bookings VALUES (6689874, 8755012, 4156979, 'EY2');
INSERT INTO bookings VALUES (2837261, 5332651, 9086205, 'FL6');
INSERT INTO bookings VALUES (6555282, 3760433, 9805585, 'ZJ1');
INSERT INTO bookings VALUES (2840243, 4118224, 7282020, 'QV2');
INSERT INTO bookings VALUES (6084295, 7540759, 4356416, 'MH5');
INSERT INTO bookings VALUES (7324679, 4078630, 7154876, 'SZ4');
INSERT INTO bookings VALUES (1500939, 2992723, 1034182, 'AK9');
INSERT INTO bookings VALUES (4735169, 4422051, 8882848, 'ZW8');
INSERT INTO bookings VALUES (3865088, 5030264, 5843018, 'VL3');
INSERT INTO bookings VALUES (3060916, 2975287, 3896243, 'MA6');
INSERT INTO bookings VALUES (8124177, 6597799, 6538823, 'DH1');
INSERT INTO bookings VALUES (9740986, 6976359, 7172867, 'BL3');
INSERT INTO bookings VALUES (4646090, 4439160, 9322743, 'LN4');
INSERT INTO bookings VALUES (4385974, 8896880, 9710039, 'XW1');
INSERT INTO bookings VALUES (9641055, 4809618, 2963186, 'YJ6');
INSERT INTO bookings VALUES (6039534, 3257367, 9859691, 'VS5');
INSERT INTO bookings VALUES (2162177, 2079829, 9859691, 'LY6');
INSERT INTO bookings VALUES (8682005, 2483269, 3735507, 'SC9');
INSERT INTO bookings VALUES (4733080, 4809618, 5682084, 'YD2');
INSERT INTO bookings VALUES (5133949, 4017366, 1069385, 'CH8');
INSERT INTO bookings VALUES (9302170, 8525725, 9376507, 'IA5');
INSERT INTO bookings VALUES (8022963, 2785261, 9148557, 'LR1');
INSERT INTO bookings VALUES (1660759, 6039236, 1034182, 'AX3');
INSERT INTO bookings VALUES (3947920, 5683800, 9837984, 'CO8');
INSERT INTO bookings VALUES (4925669, 5105774, 7341226, 'VV7');
INSERT INTO bookings VALUES (1493138, 7143247, 3443010, 'PJ1');

-- =========== skymill.loyalty_earnings (generic) ==========

DROP TABLE IF EXISTS loyalty_earnings;

CREATE TABLE loyalty_earnings (
  id INT NOT NULL,
  booking_id INT NOT NULL,
  passenger_id INT NOT NULL,
  miles_earned INT NOT NULL,
  earning_date DATE NOT NULL
);

INSERT INTO loyalty_earnings VALUES (2550102, 7418656, 1128509, 1750, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (5234334, 8070249, 2680208, 2559, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (3210741, 4700172, 1784320, 3796, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (2404113, 9708665, 8238843, 4856, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (3494181, 8365147, 6955370, 2427, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (2516929, 6424865, 3713051, 3147, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (7257319, 5649162, 5408609, 4394, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (4183874, 7477072, 7470714, 541, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (8433715, 3224879, 8962583, 802, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (6308378, 7934364, 9166027, 1224, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (6625971, 1599267, 5563188, 1182, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (9201187, 2899041, 1169425, 3319, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (6422700, 9217906, 7371009, 819, '2026-01-29');
INSERT INTO loyalty_earnings VALUES (4354046, 1110342, 3166127, 4264, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (9080834, 2186206, 4747967, 2236, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (6759314, 1537021, 1102212, 2246, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (5815644, 3736596, 8216199, 918, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (8895345, 9868956, 6976359, 1558, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (4307786, 5592330, 3423018, 4078, '2026-05-23');
INSERT INTO loyalty_earnings VALUES (8659748, 7508381, 6039236, 4176, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (6844660, 5303671, 4339947, 3108, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (2156181, 1423600, 4859106, 2080, '2026-02-26');
INSERT INTO loyalty_earnings VALUES (7583503, 6746619, 4118224, 648, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (1928745, 4412672, 9288911, 3091, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (9980646, 6013426, 3418931, 2688, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (4678200, 3382167, 9607002, 2811, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (9425332, 1343920, 8755012, 4155, '2026-02-25');
INSERT INTO loyalty_earnings VALUES (5588878, 5097027, 2992723, 1272, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (2480626, 1776622, 3577168, 3246, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (9049124, 5445234, 4668405, 2507, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (8088390, 8732178, 2680208, 3617, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (2111053, 9154005, 5286908, 1249, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (1842660, 6283664, 3977299, 730, '2026-03-12');
INSERT INTO loyalty_earnings VALUES (7024752, 8124177, 7092950, 4576, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (8974924, 9159147, 3437925, 2513, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (8677207, 3501854, 8957252, 4647, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (4959164, 9581094, 3106291, 4346, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (2512448, 4876715, 7083414, 553, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (6195094, 6378259, 3963722, 2111, '2026-05-19');
INSERT INTO loyalty_earnings VALUES (5330064, 4367259, 9263093, 4149, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (3149715, 9463019, 2216117, 1114, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (2210941, 2352295, 8896880, 2284, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (5159893, 2899041, 8121189, 620, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (6910817, 7909331, 3879592, 2593, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (6990802, 7121565, 4278613, 3180, '2026-01-17');
INSERT INTO loyalty_earnings VALUES (9345628, 6202997, 4339947, 2256, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (9975625, 4136571, 2654571, 2995, '2026-03-21');
INSERT INTO loyalty_earnings VALUES (2728066, 5782852, 5382959, 2344, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (5188115, 3342522, 7447517, 2012, '2026-01-15');
INSERT INTO loyalty_earnings VALUES (4547468, 3317520, 9478711, 2995, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (8827143, 9554283, 4430924, 2018, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (3884832, 4412672, 5105774, 2318, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (3461608, 1310627, 4507387, 2933, '2026-01-05');
INSERT INTO loyalty_earnings VALUES (6017599, 3687015, 3577168, 2537, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (2632784, 3573746, 5683800, 2631, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (5291923, 8304737, 8622373, 3652, '2026-04-19');
INSERT INTO loyalty_earnings VALUES (1735341, 7905117, 3738778, 2095, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (4523405, 8026826, 7595919, 1634, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (3887346, 8135561, 2905071, 502, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (4189596, 7769736, 4422051, 1850, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (9447234, 3079610, 6039236, 964, '2026-05-30');
INSERT INTO loyalty_earnings VALUES (2885656, 7001160, 6589761, 578, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (3128015, 9011600, 7180424, 894, '2026-02-19');
INSERT INTO loyalty_earnings VALUES (7548500, 8943946, 6351366, 4787, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (6458885, 9957215, 8668826, 3804, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (1075316, 1022113, 3879592, 4092, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (7460700, 8410126, 3681623, 3169, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (3349404, 4915213, 8446918, 939, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (2015561, 5215497, 5683800, 3293, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (6841080, 3634055, 6091790, 1292, '2026-01-09');
INSERT INTO loyalty_earnings VALUES (9281770, 1239047, 3626944, 1487, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (1350284, 4591378, 9607002, 3011, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (7147833, 4178484, 3257367, 4720, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (1959257, 3060916, 2461695, 1613, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (6634623, 8395913, 5497703, 2318, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (4405309, 5864993, 1083307, 3934, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (2635453, 3763640, 2905071, 3422, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (1958782, 6424865, 8625276, 1419, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (5390127, 2290522, 8238843, 2311, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (9421910, 5027298, 2785261, 1739, '2026-05-17');
INSERT INTO loyalty_earnings VALUES (4806219, 1737402, 3760433, 4122, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (1982488, 7534066, 4636158, 4000, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (9579895, 6123296, 3626944, 1518, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (5450072, 5894239, 8391185, 3838, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (5566694, 5247321, 2483269, 4085, '2026-05-13');
INSERT INTO loyalty_earnings VALUES (7336260, 5098787, 9488847, 3200, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (3616883, 3967442, 6955370, 3955, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (4748117, 7953798, 9017430, 3798, '2026-03-30');
INSERT INTO loyalty_earnings VALUES (5020484, 2518875, 3681623, 2235, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (1657711, 8741395, 8216199, 3665, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (9079779, 4417336, 7515878, 1322, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (5834190, 6733255, 4668405, 4497, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (4222051, 8289648, 2680208, 933, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (2685588, 2995950, 6564332, 1192, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (9752915, 2103439, 2079829, 3301, '2026-05-17');
INSERT INTO loyalty_earnings VALUES (5083506, 4462033, 8216199, 3149, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (9199227, 4682247, 4118224, 4131, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (3766584, 6427746, 7462556, 1225, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (2035034, 7939319, 8502145, 1417, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (9304398, 7734309, 5009563, 4760, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (8876562, 2108500, 8446918, 1425, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (7861348, 4294251, 8770415, 3822, '2026-05-14');
INSERT INTO loyalty_earnings VALUES (6702976, 1598131, 3418931, 3354, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (2447089, 8070249, 4278613, 2983, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (2198059, 8846685, 3504500, 1768, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (1949290, 9097555, 7083414, 2732, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (9326374, 7766085, 3179127, 984, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (3515215, 9250912, 2265297, 4335, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (2764269, 9186940, 8782077, 4740, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (9704295, 3300396, 9017430, 3665, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (6311521, 3001561, 4220395, 3358, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (4291514, 6598224, 3944798, 4964, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (7000049, 9382549, 4287773, 4537, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (9793742, 6003650, 8391185, 4244, '2026-02-12');
INSERT INTO loyalty_earnings VALUES (2959780, 6938054, 7563320, 557, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (7837027, 2507915, 8951574, 2013, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (1851738, 3288648, 3504500, 3491, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (1150324, 6448352, 8770415, 3419, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (3372582, 1367157, 5009563, 2174, '2026-05-27');
INSERT INTO loyalty_earnings VALUES (1670739, 1942223, 7412516, 1551, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (5237816, 4620058, 1169425, 2712, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (4116344, 5684808, 8121189, 2753, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (8629128, 5542530, 5761385, 3954, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (2377812, 6938054, 3879592, 4305, '2026-05-28');
INSERT INTO loyalty_earnings VALUES (9254481, 6841707, 8951574, 1156, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (8133611, 6370619, 2940416, 1413, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (8537511, 9146355, 3626944, 4554, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (9726843, 9711510, 5030264, 3749, '2026-05-19');
INSERT INTO loyalty_earnings VALUES (7661531, 6935953, 3343612, 4207, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (4093979, 7661921, 9729552, 1247, '2026-02-06');
INSERT INTO loyalty_earnings VALUES (6607478, 5919854, 8395495, 3267, '2026-05-25');
INSERT INTO loyalty_earnings VALUES (3520519, 3676834, 6597799, 3643, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (6671334, 7321909, 9729552, 4340, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (3074999, 4345626, 2347757, 3791, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (6117280, 6949781, 4197805, 4755, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (6504077, 5489539, 6856915, 3446, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (4094392, 3623130, 8371206, 3122, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (6238429, 8911806, 9258677, 838, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (6923202, 8272495, 8525725, 1933, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (6527831, 8323487, 6527815, 1398, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (5332803, 2604124, 8782077, 2335, '2026-03-29');
INSERT INTO loyalty_earnings VALUES (9965849, 8299339, 4197805, 641, '2026-02-26');
INSERT INTO loyalty_earnings VALUES (3908144, 9912794, 3423018, 3212, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (5134693, 9603857, 5761385, 1460, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (7404865, 7188615, 1083307, 634, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (2251385, 4248391, 4439160, 936, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (2710995, 7477072, 3211200, 2518, '2026-05-24');
INSERT INTO loyalty_earnings VALUES (6772002, 1117952, 8371206, 538, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (5127450, 5894896, 4278613, 2266, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (5929944, 2457995, 7885995, 2419, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (6022398, 3420932, 9356593, 1374, '2026-02-19');
INSERT INTO loyalty_earnings VALUES (4907298, 1022113, 3270346, 3029, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (8042483, 5682233, 4953636, 990, '2026-02-20');
INSERT INTO loyalty_earnings VALUES (6946498, 3427568, 5798843, 4565, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (2217710, 9902612, 8622373, 3909, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (8745877, 9997507, 7885995, 2026, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (1477463, 5697278, 3784950, 4900, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (8349698, 8255873, 4339947, 1840, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (7761688, 8888764, 7660641, 2468, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (9453138, 7980476, 4678821, 4694, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (7188104, 6889635, 9478711, 2458, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (5291911, 4075450, 9166027, 2242, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (8167330, 3070024, 2193325, 2996, '2026-03-03');
INSERT INTO loyalty_earnings VALUES (1806130, 1240720, 3440276, 4398, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (1003750, 2382367, 3352316, 1617, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (2166572, 9217906, 3257367, 1975, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (9973393, 4915213, 9607002, 2452, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (9829142, 6131014, 5053396, 3932, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (9328522, 1769331, 4678821, 1302, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (9233153, 1751272, 8517884, 1695, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (5967106, 8224560, 8121189, 2055, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (8147208, 3029273, 7242085, 1820, '2026-04-14');
INSERT INTO loyalty_earnings VALUES (9483267, 5848458, 8809406, 4369, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (3364290, 5165799, 7797269, 4592, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (3035098, 5422054, 4278613, 4005, '2026-05-27');
INSERT INTO loyalty_earnings VALUES (6281506, 2259221, 8216199, 3747, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (9873971, 6856072, 2422409, 1669, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (6327486, 1395806, 3198155, 2228, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (4070874, 5684808, 5053396, 767, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (8258404, 6208447, 7242085, 1519, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (2021209, 3763327, 9166027, 4924, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (5220171, 3763327, 3437925, 3588, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (3117038, 6353762, 5105774, 3365, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (2128929, 9249043, 2347757, 705, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (7701416, 8262564, 4430924, 1877, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (3043573, 4125206, 6526839, 885, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (5125383, 2837261, 2992723, 1013, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (5742503, 6949781, 8173474, 3111, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (9014306, 8515591, 7503774, 900, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (3771803, 3423116, 6419573, 3616, '2026-05-14');
INSERT INTO loyalty_earnings VALUES (9237696, 2289604, 9999465, 2591, '2026-01-25');
INSERT INTO loyalty_earnings VALUES (2149066, 8010096, 4636158, 835, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (5135969, 3687015, 1784320, 4978, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (8473662, 1682824, 9017430, 3532, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (9236522, 9068057, 4278613, 1455, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (9094608, 7804341, 5286908, 688, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (5930474, 5256816, 4507387, 4182, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (3162055, 5913348, 8962583, 3495, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (4899289, 6484248, 8121189, 2616, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (3174164, 9807578, 6527815, 4767, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (3893382, 6845883, 3437925, 3315, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (1933140, 1761602, 3738778, 1086, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (2639986, 1395806, 4821607, 1552, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (7126061, 4680410, 6091790, 2241, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (1584240, 5873348, 6348851, 4079, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (8119388, 8582926, 7595919, 1341, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (5383946, 3945756, 2773723, 2041, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (9256089, 9740606, 3816929, 3157, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (5811468, 2439246, 1169425, 3412, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (3260322, 3001561, 9263093, 4241, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (9465454, 6284564, 5683800, 3080, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (2301151, 9532390, 2881744, 3649, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (3194932, 3573746, 8525725, 2252, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (6922638, 1016638, 7556383, 1512, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (3445818, 9577867, 3738778, 3867, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (3301514, 2014039, 5286908, 4841, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (4729578, 6208589, 5408609, 1612, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (8480343, 5299630, 2483269, 4961, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (5256293, 9955726, 7371009, 2672, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (1926588, 5600563, 5761385, 2638, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (1777736, 5264010, 5781985, 1278, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (1042633, 5292388, 7371009, 3936, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (3882999, 2014039, 3135062, 4876, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (6199562, 9225835, 4017366, 3348, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (9654506, 4011812, 3135062, 2518, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (1665138, 1997341, 5382959, 593, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (3580243, 7112140, 2024582, 885, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (3917682, 4251915, 8782077, 2517, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (2862421, 7566118, 2265297, 1347, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (2865299, 7734309, 1185144, 3460, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (9610826, 8761363, 5277747, 1306, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (1673127, 4680410, 9342246, 4533, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (4000801, 6927298, 1185144, 3839, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (8273667, 3300396, 5497703, 3860, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (2471820, 5573187, 4430924, 1630, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (9962311, 9205451, 2541341, 3490, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (2810413, 6689874, 2957480, 2723, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (7005888, 6343484, 4507387, 2162, '2026-01-05');
INSERT INTO loyalty_earnings VALUES (2114599, 8312360, 1128509, 4750, '2026-01-09');
INSERT INTO loyalty_earnings VALUES (4371326, 9911329, 3816929, 4966, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (9537724, 7112140, 2975287, 4375, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (7213228, 7449172, 2193325, 4185, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (3061519, 1266417, 2422409, 4479, '2026-02-19');
INSERT INTO loyalty_earnings VALUES (6461509, 7794534, 5781985, 1160, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (4677378, 2770238, 4430924, 4781, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (5524145, 6619749, 4141937, 2549, '2026-03-24');
INSERT INTO loyalty_earnings VALUES (8409321, 6208447, 4865004, 3674, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (8901664, 7970118, 5286908, 4087, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (9852654, 6180429, 5720403, 2347, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (3604090, 3029273, 5105774, 2417, '2026-04-14');
INSERT INTO loyalty_earnings VALUES (5552078, 7661921, 5720403, 2825, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (8869994, 3643738, 2265297, 2048, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (5097345, 2679883, 2566046, 4290, '2026-05-26');
INSERT INTO loyalty_earnings VALUES (1293595, 3674035, 9735147, 3848, '2026-05-30');
INSERT INTO loyalty_earnings VALUES (4033048, 8933616, 4017366, 4211, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (9693496, 2346918, 3440276, 4332, '2026-05-22');
INSERT INTO loyalty_earnings VALUES (9210789, 6949781, 4747967, 2780, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (6382593, 7116430, 6066735, 3664, '2026-05-28');
INSERT INTO loyalty_earnings VALUES (2120039, 2416661, 3626944, 1281, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (2012151, 3266291, 2024582, 595, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (4994864, 1751272, 9342246, 1984, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (7735581, 2995950, 3074155, 2285, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (3032812, 2152309, 7412516, 2995, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (8207942, 3813333, 8782077, 3374, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (9144254, 5256816, 4439160, 2853, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (8749951, 3060916, 9218661, 1148, '2026-05-20');
INSERT INTO loyalty_earnings VALUES (4951516, 1616428, 7503774, 4698, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (1114929, 3381353, 7011025, 686, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (6854353, 1200346, 7456753, 2727, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (1424656, 3865500, 4086038, 4472, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (7203979, 1524694, 9765785, 866, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (7880066, 7228070, 4193399, 2250, '2026-02-13');
INSERT INTO loyalty_earnings VALUES (9516996, 6065249, 3437925, 662, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (9603091, 6581034, 7973380, 729, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (8076243, 7449172, 5286908, 2470, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (1947616, 7409336, 7515878, 3549, '2026-05-23');
INSERT INTO loyalty_earnings VALUES (5092132, 9152711, 8782077, 2621, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (9742483, 9807578, 2193325, 2039, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (2220351, 9237278, 2680208, 2433, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (6910649, 6619339, 2013023, 3284, '2026-05-14');
INSERT INTO loyalty_earnings VALUES (4348947, 8294151, 4308132, 2869, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (1347750, 5974936, 5603263, 3361, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (5526634, 6845883, 7507143, 3729, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (2027392, 5377105, 7371009, 3375, '2026-05-17');
INSERT INTO loyalty_earnings VALUES (8105651, 5696323, 3437925, 4065, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (2373380, 1598131, 4017366, 2062, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (9517456, 1142136, 9908742, 4347, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (6953288, 9845042, 5603263, 2220, '2026-04-15');
INSERT INTO loyalty_earnings VALUES (4665270, 4385974, 5009563, 2002, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (4565969, 4320050, 4953636, 4029, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (4690576, 9729343, 3437925, 1144, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (6994171, 5573187, 3135062, 2675, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (2780444, 9865161, 4078630, 3483, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (5881632, 8826297, 6348851, 3247, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (7403295, 9633280, 3002493, 4454, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (7786019, 6896768, 1831583, 4844, '2026-01-05');
INSERT INTO loyalty_earnings VALUES (9789634, 5601574, 8391185, 824, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (3289121, 2424952, 3738778, 4705, '2026-05-10');
INSERT INTO loyalty_earnings VALUES (3957250, 9911329, 4439160, 4976, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (5854797, 9796204, 5683800, 4712, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (6158101, 5625204, 7371009, 3439, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (5256404, 6938054, 4118224, 1192, '2026-04-01');
INSERT INTO loyalty_earnings VALUES (6296333, 9435683, 5286908, 892, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (1700514, 6303673, 6039236, 4134, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (1031097, 9111289, 4747967, 3664, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (9849200, 5554072, 7503774, 1076, '2026-02-27');
INSERT INTO loyalty_earnings VALUES (4187615, 1408218, 7865203, 1554, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (7463650, 1504636, 8238843, 2355, '2026-04-11');
INSERT INTO loyalty_earnings VALUES (2134361, 8078700, 4149608, 2665, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (2555578, 3472111, 9999465, 1211, '2026-05-30');
INSERT INTO loyalty_earnings VALUES (6369770, 8459141, 8594972, 4818, '2026-04-04');
INSERT INTO loyalty_earnings VALUES (6237749, 5625204, 3418931, 1011, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (6171491, 9159147, 6589761, 4099, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (4134286, 8939477, 4220395, 2023, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (5474708, 9907707, 1831583, 2493, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (9806059, 4453235, 7556383, 3215, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (4186342, 1997341, 9454921, 2729, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (7954863, 4620058, 7595919, 1911, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (3392328, 4475982, 5088004, 4324, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (3133244, 9918406, 8238843, 3772, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (6436944, 9197064, 2654571, 2711, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (6689353, 3224879, 9883532, 1967, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (1707671, 1099386, 1185144, 4197, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (8835018, 8201938, 9288911, 4715, '2026-03-03');
INSERT INTO loyalty_earnings VALUES (1277251, 7480159, 4193399, 4606, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (9326709, 5773547, 4422051, 2358, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (4356296, 2760891, 5009563, 1380, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (7380201, 6131014, 3423018, 2257, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (5958642, 8575372, 3198155, 615, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (5903529, 4929294, 5408609, 4940, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (7405821, 3929656, 8525725, 1643, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (1940093, 5126039, 1102212, 1463, '2026-05-30');
INSERT INTO loyalty_earnings VALUES (5215346, 3947920, 7507143, 3551, '2026-03-02');
INSERT INTO loyalty_earnings VALUES (6884881, 9097555, 2881744, 4964, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (4062988, 8517448, 8173474, 502, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (2914959, 4845098, 8622373, 4990, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (4771336, 8870298, 4865004, 3678, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (2948026, 6353762, 3179127, 2079, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (2168022, 7296906, 3166127, 3054, '2026-03-21');
INSERT INTO loyalty_earnings VALUES (4447990, 8580033, 3418931, 4367, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (2749544, 5542530, 2347757, 4835, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (2798816, 5196440, 7092950, 1266, '2026-05-19');
INSERT INTO loyalty_earnings VALUES (8012073, 1112634, 4430924, 3700, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (7999002, 5338686, 8216199, 1956, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (5843070, 6784869, 9607002, 2929, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (3136358, 3423116, 4149608, 870, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (9659355, 1425553, 7503774, 4273, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (9227681, 7891432, 3166127, 4634, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (8417868, 4125937, 1253566, 1693, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (1128633, 7480159, 3816929, 815, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (3555382, 2463676, 4859106, 3947, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (9071861, 9302170, 8782077, 1188, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (8337836, 1929813, 2974225, 4515, '2026-02-06');
INSERT INTO loyalty_earnings VALUES (1501179, 2660494, 7456753, 2266, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (6445623, 4853826, 6419573, 2260, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (8963570, 4680410, 9908742, 1096, '2026-03-27');
INSERT INTO loyalty_earnings VALUES (3471335, 1063281, 7973380, 1605, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (5105919, 1385729, 8845953, 1568, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (7085242, 5544411, 3626944, 2778, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (9803872, 4936817, 2024582, 4682, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (7462706, 4178484, 9883532, 1223, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (8338616, 8846685, 2992723, 1880, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (9731986, 4543393, 3816929, 4925, '2026-02-19');
INSERT INTO loyalty_earnings VALUES (3871276, 2429247, 3504500, 2953, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (8075269, 5552236, 3343612, 1272, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (4807308, 1525756, 5105774, 3777, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (6704161, 2393337, 4953636, 968, '2026-05-23');
INSERT INTO loyalty_earnings VALUES (4812822, 4717614, 7456753, 4166, '2026-05-17');
INSERT INTO loyalty_earnings VALUES (5196414, 1427341, 7503774, 948, '2026-05-12');
INSERT INTO loyalty_earnings VALUES (6175717, 3472111, 8951574, 3031, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (5200835, 3623130, 4287773, 4067, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (6297748, 3197434, 5286908, 1511, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (1956729, 7953798, 3504500, 4717, '2026-03-14');
INSERT INTO loyalty_earnings VALUES (2644213, 9231572, 3179127, 847, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (8571570, 9707415, 4197805, 3547, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (5950471, 8459141, 5030264, 1565, '2026-03-11');
INSERT INTO loyalty_earnings VALUES (6125103, 9540075, 9169482, 4249, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (6254653, 3079610, 4086038, 1247, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (7667233, 3263570, 7660641, 2843, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (2143227, 5349444, 7660641, 641, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (4778389, 8365147, 2483269, 1166, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (9832973, 8838296, 2265297, 1387, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (6572851, 2035003, 7242085, 4151, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (1544167, 3573746, 3440276, 3902, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (7458356, 2799947, 2729297, 2265, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (4010897, 1060597, 4086038, 2263, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (9870913, 2653273, 2483269, 4573, '2026-05-26');
INSERT INTO loyalty_earnings VALUES (8625322, 2900027, 7507143, 4773, '2026-01-15');
INSERT INTO loyalty_earnings VALUES (3151972, 2472191, 8155808, 3798, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (4670683, 9906347, 5408609, 1428, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (9372210, 9954294, 9259092, 3938, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (7977660, 2820535, 7515878, 2344, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (8036918, 7659076, 6976359, 1582, '2026-05-11');
INSERT INTO loyalty_earnings VALUES (4068953, 2303807, 8668826, 3817, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (2048871, 5879440, 3257367, 1320, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (7635178, 9417885, 4078630, 2483, '2026-01-15');
INSERT INTO loyalty_earnings VALUES (7353204, 9111289, 8782077, 4213, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (6576736, 5126039, 9810226, 1638, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (6398718, 6237399, 5719835, 1652, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (8286010, 9278508, 6856915, 2654, '2026-02-20');
INSERT INTO loyalty_earnings VALUES (4353345, 3763640, 6904458, 4946, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (7145238, 3580908, 8395495, 1284, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (7794117, 5542530, 6728086, 775, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (9107823, 2303807, 7507143, 1689, '2026-05-28');
INSERT INTO loyalty_earnings VALUES (8087248, 3914841, 5332651, 3117, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (7493995, 2340444, 8805416, 2258, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (6020083, 9231572, 2566046, 920, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (8663302, 2803459, 8525725, 3220, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (4326395, 1367157, 9342246, 4072, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (6913703, 1351333, 7447517, 3553, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (8376768, 6889635, 3106291, 2569, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (5597087, 2518875, 4678821, 3514, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (9437685, 7487130, 9883532, 791, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (1733282, 8157454, 1594316, 4253, '2026-05-10');
INSERT INTO loyalty_earnings VALUES (5100009, 6275487, 9218661, 2374, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (7390301, 3932120, 1102212, 2095, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (3928542, 6343484, 8957252, 4443, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (5925788, 3518722, 9607002, 3172, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (1148181, 7081554, 3002493, 3123, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (2923470, 9711510, 4422051, 4280, '2026-05-25');
INSERT INTO loyalty_earnings VALUES (6176081, 9417885, 2541341, 3935, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (6651880, 9911328, 3343612, 4847, '2026-03-06');
INSERT INTO loyalty_earnings VALUES (3072112, 6424865, 4017366, 3714, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (5587148, 6583581, 7092950, 1219, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (4052988, 1038659, 6526839, 1741, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (8115679, 2429247, 4430924, 917, '2026-05-23');
INSERT INTO loyalty_earnings VALUES (2088258, 7237867, 1831583, 3314, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (2574183, 3420932, 6066735, 3968, '2026-01-19');
INSERT INTO loyalty_earnings VALUES (1117125, 6555282, 9883532, 804, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (3919958, 1660759, 5105774, 718, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (1010090, 3687119, 4747967, 4677, '2026-01-02');
INSERT INTO loyalty_earnings VALUES (5142842, 7659076, 4439160, 1393, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (7396147, 3135141, 7242085, 1930, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (1958181, 3865500, 7470714, 3748, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (8429939, 8181403, 8446918, 3468, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (5090137, 2103439, 6039236, 4035, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (4332302, 6619339, 2566046, 4912, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (2016724, 2044708, 2680208, 871, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (7063189, 4671508, 8371206, 3025, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (2605206, 5782852, 4339947, 3861, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (5826533, 3048419, 7859194, 3078, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (4109292, 6284564, 9735147, 913, '2026-03-21');
INSERT INTO loyalty_earnings VALUES (9281280, 2641677, 6091790, 872, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (8364514, 1741933, 4193399, 3463, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (7628667, 9708665, 2957480, 3583, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (4358110, 7487130, 3270346, 4692, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (4584665, 3915653, 5563188, 981, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (4344121, 8193176, 3626944, 2585, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (9452917, 4394206, 4422051, 3138, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (3008604, 6660303, 2974225, 1962, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (8559953, 1240720, 5185820, 1713, '2026-04-18');
INSERT INTO loyalty_earnings VALUES (6672297, 5040329, 7470714, 1412, '2026-05-28');
INSERT INTO loyalty_earnings VALUES (6161551, 1682824, 4197805, 1609, '2026-01-29');
INSERT INTO loyalty_earnings VALUES (5047913, 6784869, 8238843, 2305, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (3550446, 2208818, 2881744, 3583, '2026-04-26');
INSERT INTO loyalty_earnings VALUES (9299432, 7062408, 5719835, 3226, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (8573301, 8745523, 9883532, 3595, '2026-03-29');
INSERT INTO loyalty_earnings VALUES (4842218, 9955726, 8782077, 1321, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (4293674, 9202936, 3135062, 3307, '2026-04-07');
INSERT INTO loyalty_earnings VALUES (9575687, 8262564, 3106291, 3138, '2026-04-14');
INSERT INTO loyalty_earnings VALUES (9810825, 2017491, 2992723, 4763, '2026-01-17');
INSERT INTO loyalty_earnings VALUES (6880674, 5445304, 6348851, 4334, '2026-05-25');
INSERT INTO loyalty_earnings VALUES (7680606, 5829260, 1102212, 3478, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (6429512, 7508381, 2957480, 1777, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (5478512, 1310627, 5277747, 3928, '2026-05-28');
INSERT INTO loyalty_earnings VALUES (1686092, 6131014, 6091790, 4178, '2026-03-04');
INSERT INTO loyalty_earnings VALUES (6240682, 9711510, 2193325, 4990, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (6786874, 8181366, 2975287, 4132, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (6659593, 1310627, 2024582, 4458, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (1274076, 7784582, 8216199, 4987, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (4486852, 8114347, 2785261, 4239, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (6516685, 1018412, 7865203, 4888, '2026-04-17');
INSERT INTO loyalty_earnings VALUES (1395919, 4845198, 3418931, 2384, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (4830548, 7769736, 3343612, 3364, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (2536799, 9136604, 5781985, 3904, '2026-01-06');
INSERT INTO loyalty_earnings VALUES (7788658, 8224560, 7242085, 2918, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (3684501, 6471311, 3106291, 1548, '2026-05-19');
INSERT INTO loyalty_earnings VALUES (5973676, 2752769, 4865004, 2960, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (2475763, 5862748, 9765785, 2490, '2026-03-22');
INSERT INTO loyalty_earnings VALUES (8317122, 2227821, 3440276, 3927, '2026-04-13');
INSERT INTO loyalty_earnings VALUES (6911105, 2490812, 9356593, 4176, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (8600421, 9591265, 9454921, 574, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (6884555, 3614410, 2992723, 3079, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (7320142, 5974936, 4287773, 3721, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (6600285, 4845198, 2347757, 3341, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (3640523, 2940006, 1831583, 4340, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (8084702, 4957570, 4636158, 1365, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (4787663, 6208447, 9342246, 2542, '2026-05-16');
INSERT INTO loyalty_earnings VALUES (4000548, 9177782, 7083414, 1998, '2026-04-16');
INSERT INTO loyalty_earnings VALUES (1579706, 2417145, 8446918, 683, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (2632332, 2429247, 3418931, 2011, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (4492783, 8456555, 8391185, 2895, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (2689102, 1707120, 3977299, 1610, '2026-03-28');
INSERT INTO loyalty_earnings VALUES (7006445, 9175129, 4086038, 3736, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (2191313, 4583950, 9218661, 4449, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (3848123, 1680444, 7540759, 4762, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (2836004, 7228070, 2729297, 4143, '2026-01-02');
INSERT INTO loyalty_earnings VALUES (8048078, 4475982, 5408609, 1199, '2026-05-13');
INSERT INTO loyalty_earnings VALUES (4728127, 7398463, 8896880, 2111, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (2274224, 5529625, 8770415, 4246, '2026-03-23');
INSERT INTO loyalty_earnings VALUES (2123340, 9851914, 5408609, 4895, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (9278695, 2900027, 9218661, 1164, '2026-05-24');
INSERT INTO loyalty_earnings VALUES (5770523, 5566665, 7143247, 3340, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (2413110, 6963569, 8951574, 3217, '2026-05-01');
INSERT INTO loyalty_earnings VALUES (6514391, 5782852, 2216117, 1062, '2026-02-11');
INSERT INTO loyalty_earnings VALUES (3735012, 9997507, 3343612, 4054, '2026-01-07');
INSERT INTO loyalty_earnings VALUES (6507631, 9790909, 2483269, 4601, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (9847438, 7081554, 3343612, 4451, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (8225350, 4522456, 9729552, 2059, '2026-01-15');
INSERT INTO loyalty_earnings VALUES (6154303, 7240147, 3963722, 766, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (1004134, 2155577, 3655021, 4708, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (8619906, 1247895, 5286908, 2204, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (9011978, 9710730, 8805416, 1751, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (8739366, 9154005, 8951574, 2075, '2026-03-26');
INSERT INTO loyalty_earnings VALUES (9069694, 7579701, 7470714, 4543, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (3563595, 8389626, 2024582, 1748, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (2283299, 3954893, 4809618, 3077, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (1733517, 7634150, 7556383, 3263, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (5323056, 5060382, 7595919, 3174, '2026-04-25');
INSERT INTO loyalty_earnings VALUES (5404686, 1929813, 6039236, 4749, '2026-01-07');
INSERT INTO loyalty_earnings VALUES (2598684, 7634150, 7507143, 3099, '2026-05-05');
INSERT INTO loyalty_earnings VALUES (1087634, 7378637, 8391185, 2904, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (2328356, 8365147, 3352316, 4859, '2026-04-24');
INSERT INTO loyalty_earnings VALUES (7430070, 3298165, 2974225, 1624, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (3785650, 9554283, 7371009, 2797, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (6655078, 1099386, 7503774, 4131, '2026-01-08');
INSERT INTO loyalty_earnings VALUES (4278591, 3687119, 8625276, 1416, '2026-05-15');
INSERT INTO loyalty_earnings VALUES (4320311, 8124177, 5382959, 1231, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (1199318, 5561853, 6091790, 3411, '2026-04-29');
INSERT INTO loyalty_earnings VALUES (1690222, 1665327, 6564332, 1557, '2026-01-31');
INSERT INTO loyalty_earnings VALUES (1857650, 4456251, 6564332, 4449, '2026-05-14');
INSERT INTO loyalty_earnings VALUES (6705407, 7797657, 4430924, 3905, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (2099352, 7909331, 2013023, 1154, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (1799310, 8293529, 7242085, 819, '2026-04-21');
INSERT INTO loyalty_earnings VALUES (5648439, 8771079, 2680208, 2288, '2026-02-02');
INSERT INTO loyalty_earnings VALUES (1485663, 3224879, 7242085, 4905, '2026-05-19');
INSERT INTO loyalty_earnings VALUES (3989879, 3193208, 1594316, 3136, '2026-04-05');
INSERT INTO loyalty_earnings VALUES (4158053, 8515591, 9765785, 1638, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (1220672, 2243340, 3135062, 2049, '2026-02-17');
INSERT INTO loyalty_earnings VALUES (1610313, 4837146, 4422051, 2951, '2026-01-15');
INSERT INTO loyalty_earnings VALUES (6263664, 6003650, 1128509, 2499, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (8437047, 5288135, 5761385, 1750, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (4522140, 5445304, 1185144, 772, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (3048243, 8771079, 7470714, 1116, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (9378990, 7188615, 2216117, 2512, '2026-03-29');
INSERT INTO loyalty_earnings VALUES (5618453, 8573149, 1128509, 4542, '2026-04-07');
INSERT INTO loyalty_earnings VALUES (6916698, 5248538, 7865203, 2503, '2026-05-25');
INSERT INTO loyalty_earnings VALUES (8522811, 3288648, 2785261, 3514, '2026-04-02');
INSERT INTO loyalty_earnings VALUES (8375654, 8745523, 8155808, 4734, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (6873139, 9553921, 5798843, 668, '2026-02-26');
INSERT INTO loyalty_earnings VALUES (9401895, 4906996, 7797269, 1118, '2026-02-16');
INSERT INTO loyalty_earnings VALUES (4925529, 9853343, 2013023, 3784, '2026-05-20');
INSERT INTO loyalty_earnings VALUES (2154205, 2471375, 1102212, 4266, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (6458785, 3899871, 7885995, 1747, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (7317717, 3481798, 3002493, 4929, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (1397878, 9909574, 3816929, 785, '2026-02-28');
INSERT INTO loyalty_earnings VALUES (1663236, 6217687, 4086038, 1590, '2026-05-08');
INSERT INTO loyalty_earnings VALUES (4114440, 7734309, 7973380, 1217, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (7474625, 3924841, 5720403, 1918, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (6193551, 8326711, 3270346, 4842, '2026-05-19');
INSERT INTO loyalty_earnings VALUES (7896804, 3262717, 8238843, 3288, '2026-02-08');
INSERT INTO loyalty_earnings VALUES (7874982, 5097027, 9454921, 3098, '2026-03-08');
INSERT INTO loyalty_earnings VALUES (2190934, 8456555, 1128509, 3520, '2026-02-10');
INSERT INTO loyalty_earnings VALUES (4964748, 4236935, 6039236, 1661, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (4181264, 9730753, 7083414, 3539, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (7251686, 8737883, 5781985, 2952, '2026-03-03');
INSERT INTO loyalty_earnings VALUES (3486456, 3924841, 3074155, 3482, '2026-04-27');
INSERT INTO loyalty_earnings VALUES (7224421, 3004616, 3626944, 3234, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (3676290, 1487526, 4278613, 3734, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (7950551, 5801072, 7595919, 2978, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (7616185, 9730753, 9166027, 4094, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (2808304, 8888764, 8622373, 4693, '2026-01-29');
INSERT INTO loyalty_earnings VALUES (9316797, 1160038, 4439160, 2170, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (7420109, 2296165, 3352316, 2107, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (3117216, 5230427, 6351366, 745, '2026-04-12');
INSERT INTO loyalty_earnings VALUES (6863560, 6896768, 8809406, 4955, '2026-01-18');
INSERT INTO loyalty_earnings VALUES (7455548, 5808726, 9735147, 4508, '2026-03-17');
INSERT INTO loyalty_earnings VALUES (2387430, 4957524, 7595919, 550, '2026-05-23');
INSERT INTO loyalty_earnings VALUES (6431796, 4108983, 8770415, 4235, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (5486614, 7804341, 1784320, 4266, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (7182012, 9278508, 3166127, 1586, '2026-05-24');
INSERT INTO loyalty_earnings VALUES (3507089, 4478776, 9908742, 711, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (2340451, 6202997, 6976359, 3214, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (5437091, 7674918, 5720403, 1177, '2026-05-30');
INSERT INTO loyalty_earnings VALUES (6915162, 3481701, 7456753, 2460, '2026-02-05');
INSERT INTO loyalty_earnings VALUES (7060214, 7441461, 7371009, 4465, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (7618550, 6065249, 4149608, 4943, '2026-04-04');
INSERT INTO loyalty_earnings VALUES (1132252, 7973981, 3198155, 3400, '2026-05-23');
INSERT INTO loyalty_earnings VALUES (2994744, 2967948, 3179127, 3579, '2026-04-22');
INSERT INTO loyalty_earnings VALUES (1222035, 9553892, 8517884, 3677, '2026-02-09');
INSERT INTO loyalty_earnings VALUES (2582454, 9154005, 9218661, 2694, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (8757212, 4845098, 3760433, 3889, '2026-04-23');
INSERT INTO loyalty_earnings VALUES (9763215, 9482926, 7859194, 590, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (8378376, 3896208, 4278613, 2080, '2026-05-14');
INSERT INTO loyalty_earnings VALUES (9798998, 9646368, 8121189, 3093, '2026-02-22');
INSERT INTO loyalty_earnings VALUES (8636382, 3945756, 4308132, 4650, '2026-04-10');
INSERT INTO loyalty_earnings VALUES (7396875, 8124177, 9270808, 2248, '2026-05-31');
INSERT INTO loyalty_earnings VALUES (2373128, 6565997, 3437925, 2271, '2026-02-12');
INSERT INTO loyalty_earnings VALUES (7570917, 7527973, 6351366, 2854, '2026-04-11');
INSERT INTO loyalty_earnings VALUES (7950789, 6484248, 3816929, 4231, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (6565834, 9841462, 9288911, 935, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (2864661, 5697278, 3257367, 2031, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (9952180, 3197434, 8371206, 3903, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (2333167, 5552236, 4430924, 601, '2026-05-18');
INSERT INTO loyalty_earnings VALUES (5219754, 6737011, 4636158, 3262, '2026-05-29');
INSERT INTO loyalty_earnings VALUES (8081692, 3999987, 2193325, 2350, '2026-05-20');
INSERT INTO loyalty_earnings VALUES (5455480, 6554265, 9810226, 3588, '2026-01-27');
INSERT INTO loyalty_earnings VALUES (3364593, 9740606, 7595919, 512, '2026-02-24');
INSERT INTO loyalty_earnings VALUES (7484572, 5829260, 3816929, 761, '2026-03-15');
INSERT INTO loyalty_earnings VALUES (5451309, 3955453, 5053396, 3619, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (7550168, 3070024, 4865004, 4910, '2026-03-25');
INSERT INTO loyalty_earnings VALUES (1891314, 7001160, 2216117, 4737, '2026-05-22');
INSERT INTO loyalty_earnings VALUES (1780282, 9738543, 9454921, 1120, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (9850276, 9646368, 9999465, 4054, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (5461594, 2778362, 3760433, 4876, '2026-05-24');
INSERT INTO loyalty_earnings VALUES (4244348, 3687119, 4149608, 3382, '2026-02-14');
INSERT INTO loyalty_earnings VALUES (5901952, 1524478, 7797269, 2873, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (9763242, 6208589, 4507387, 3323, '2026-02-04');
INSERT INTO loyalty_earnings VALUES (6962952, 1174090, 2992723, 4343, '2026-02-21');
INSERT INTO loyalty_earnings VALUES (4551200, 6794875, 4141937, 1560, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (2218743, 6284564, 8453354, 1068, '2026-01-04');
INSERT INTO loyalty_earnings VALUES (2628156, 2840243, 1784320, 2030, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (9851007, 4925669, 5719835, 4991, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (2961034, 2608794, 4636158, 540, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (2545131, 8001981, 7143247, 3090, '2026-04-11');
INSERT INTO loyalty_earnings VALUES (1018525, 6565997, 9735147, 4333, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (4500788, 9302170, 2957480, 3400, '2026-03-19');
INSERT INTO loyalty_earnings VALUES (2078265, 5053331, 9607002, 1524, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (1715807, 8166274, 2992723, 4026, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (6473653, 6368129, 5382959, 1561, '2026-01-21');
INSERT INTO loyalty_earnings VALUES (4396546, 9708665, 5720403, 3140, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (8959978, 1665327, 1365545, 4798, '2026-03-09');
INSERT INTO loyalty_earnings VALUES (6683390, 6480707, 7859194, 1420, '2026-04-08');
INSERT INTO loyalty_earnings VALUES (1330149, 3356498, 3577168, 1206, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (1099590, 1616428, 2193325, 2908, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (8686951, 3763640, 4809618, 2955, '2026-05-02');
INSERT INTO loyalty_earnings VALUES (6899169, 1803104, 4141937, 2597, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (5340778, 8026826, 6348851, 1679, '2026-05-30');
INSERT INTO loyalty_earnings VALUES (7341336, 6565648, 5286908, 4856, '2026-04-03');
INSERT INTO loyalty_earnings VALUES (6500221, 5299630, 5009563, 959, '2026-05-03');
INSERT INTO loyalty_earnings VALUES (1751898, 8457895, 4278613, 2612, '2026-01-11');
INSERT INTO loyalty_earnings VALUES (4185346, 5566203, 9454921, 3558, '2026-05-14');
INSERT INTO loyalty_earnings VALUES (7278939, 4429427, 2461695, 3477, '2026-04-06');
INSERT INTO loyalty_earnings VALUES (6057307, 5060382, 1308410, 816, '2026-04-11');
INSERT INTO loyalty_earnings VALUES (5036916, 7116430, 2881744, 2314, '2026-01-12');
INSERT INTO loyalty_earnings VALUES (7063675, 1580588, 3504500, 4594, '2026-03-18');
INSERT INTO loyalty_earnings VALUES (8258759, 3118440, 7180424, 4562, '2026-01-30');
INSERT INTO loyalty_earnings VALUES (6198142, 2472618, 4507387, 4145, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (5656467, 7785771, 4118224, 4543, '2026-01-16');
INSERT INTO loyalty_earnings VALUES (6999942, 5600745, 6589761, 4957, '2026-01-26');
INSERT INTO loyalty_earnings VALUES (3856353, 3428322, 7515878, 4208, '2026-01-22');
INSERT INTO loyalty_earnings VALUES (9887585, 9805983, 6351366, 4033, '2026-03-31');
INSERT INTO loyalty_earnings VALUES (3923787, 4478776, 9622165, 3831, '2026-05-21');
INSERT INTO loyalty_earnings VALUES (9668503, 9486875, 9478711, 4397, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (4641494, 3636943, 8782077, 1049, '2026-05-17');
INSERT INTO loyalty_earnings VALUES (6931443, 3314952, 1128509, 3941, '2026-05-23');
INSERT INTO loyalty_earnings VALUES (9094275, 3075323, 9259092, 2822, '2026-03-01');
INSERT INTO loyalty_earnings VALUES (9512710, 9641055, 2773723, 2132, '2026-05-26');
INSERT INTO loyalty_earnings VALUES (6063659, 6217534, 4278613, 3940, '2026-01-13');
INSERT INTO loyalty_earnings VALUES (4880675, 3004616, 2079829, 2553, '2026-01-29');
INSERT INTO loyalty_earnings VALUES (7696113, 2416661, 9999465, 1230, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (6880777, 8122337, 9729552, 3595, '2026-01-10');
INSERT INTO loyalty_earnings VALUES (1089474, 8410126, 7371009, 3323, '2026-01-05');
INSERT INTO loyalty_earnings VALUES (1015417, 6375811, 8962583, 3893, '2026-05-17');
INSERT INTO loyalty_earnings VALUES (8179128, 8456555, 8809406, 4603, '2026-03-03');
INSERT INTO loyalty_earnings VALUES (1902003, 7730782, 6526839, 3066, '2026-03-07');
INSERT INTO loyalty_earnings VALUES (4892209, 2604124, 7973380, 2459, '2026-01-01');
INSERT INTO loyalty_earnings VALUES (6782714, 7823393, 5105774, 536, '2026-03-29');
INSERT INTO loyalty_earnings VALUES (4709732, 3644639, 2079829, 4764, '2026-03-20');
INSERT INTO loyalty_earnings VALUES (1105827, 7441461, 6527815, 2963, '2026-04-01');
INSERT INTO loyalty_earnings VALUES (5817593, 7629913, 5185820, 962, '2026-04-30');
INSERT INTO loyalty_earnings VALUES (9487859, 6056507, 3352316, 1594, '2026-02-15');
INSERT INTO loyalty_earnings VALUES (1843525, 3059306, 8173474, 4084, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (4570243, 9469272, 2785261, 524, '2026-01-14');
INSERT INTO loyalty_earnings VALUES (9592395, 4462033, 9259092, 2611, '2026-04-20');
INSERT INTO loyalty_earnings VALUES (2190841, 2273239, 3577168, 1878, '2026-05-25');
INSERT INTO loyalty_earnings VALUES (9992773, 8135561, 2079829, 1775, '2026-03-05');
INSERT INTO loyalty_earnings VALUES (1219180, 6237399, 6762919, 611, '2026-04-11');
INSERT INTO loyalty_earnings VALUES (6679380, 1970761, 8805416, 2694, '2026-05-13');
INSERT INTO loyalty_earnings VALUES (7058178, 3889730, 5277747, 824, '2026-03-10');
INSERT INTO loyalty_earnings VALUES (6546916, 2290522, 3681623, 1544, '2026-01-24');
INSERT INTO loyalty_earnings VALUES (5157859, 9627285, 7011025, 2183, '2026-04-04');
INSERT INTO loyalty_earnings VALUES (3765497, 9740606, 2729297, 2122, '2026-03-13');
INSERT INTO loyalty_earnings VALUES (5521514, 9041274, 6419573, 801, '2026-01-20');
INSERT INTO loyalty_earnings VALUES (9778233, 5389195, 8770415, 2161, '2026-03-27');
INSERT INTO loyalty_earnings VALUES (4591144, 1803104, 3879592, 1731, '2026-05-09');
INSERT INTO loyalty_earnings VALUES (5789907, 9469323, 9729552, 822, '2026-02-03');
INSERT INTO loyalty_earnings VALUES (7252205, 7905117, 6351366, 4260, '2026-01-28');
INSERT INTO loyalty_earnings VALUES (2909602, 1260394, 1185144, 4423, '2026-01-23');
INSERT INTO loyalty_earnings VALUES (6728357, 5897863, 8622373, 2800, '2026-02-23');
INSERT INTO loyalty_earnings VALUES (4128873, 7296906, 4220395, 4966, '2026-01-05');
INSERT INTO loyalty_earnings VALUES (1482036, 3137618, 7660641, 4230, '2026-02-07');
INSERT INTO loyalty_earnings VALUES (2768258, 2469302, 3963722, 3661, '2026-05-06');
INSERT INTO loyalty_earnings VALUES (2853336, 9177782, 9729552, 647, '2026-02-18');
INSERT INTO loyalty_earnings VALUES (4701708, 5825173, 8755012, 3769, '2026-01-03');
INSERT INTO loyalty_earnings VALUES (7663649, 8838296, 7556383, 3415, '2026-03-16');
INSERT INTO loyalty_earnings VALUES (7931091, 5349444, 3179127, 4694, '2026-04-28');
INSERT INTO loyalty_earnings VALUES (7754122, 2955636, 8517884, 2336, '2026-05-04');
INSERT INTO loyalty_earnings VALUES (5196027, 2967948, 4636158, 751, '2026-05-07');
INSERT INTO loyalty_earnings VALUES (2613080, 5126039, 7503774, 1722, '2026-02-13');

-- =========== skymill.delays (generic) ==========

DROP TABLE IF EXISTS delays;

CREATE TABLE delays (
  id INT NOT NULL,
  flight_instance_id INT NOT NULL,
  delay_minutes INT NOT NULL,
  reason VARCHAR(255) NOT NULL
);

INSERT INTO delays VALUES (4213201, 5855521, 134, 'other');
INSERT INTO delays VALUES (2360869, 3896243, 111, 'technical');
INSERT INTO delays VALUES (7029621, 6938430, 117, 'technical');
INSERT INTO delays VALUES (8332506, 1184323, 83, 'crew');
INSERT INTO delays VALUES (2100907, 1069385, 75, 'weather');
INSERT INTO delays VALUES (7897373, 2297664, 23, 'weather');
INSERT INTO delays VALUES (5461041, 4388944, 156, 'technical');
INSERT INTO delays VALUES (7952185, 7593448, 83, 'crew');
INSERT INTO delays VALUES (5625596, 7618732, 131, 'technical');
INSERT INTO delays VALUES (4611360, 2480858, 173, 'other');
INSERT INTO delays VALUES (7123685, 7770284, 104, 'crew');
INSERT INTO delays VALUES (7537799, 7094923, 18, 'weather');
INSERT INTO delays VALUES (1655502, 4026701, 68, 'weather');
INSERT INTO delays VALUES (7398008, 4388944, 72, 'other');
INSERT INTO delays VALUES (1691141, 7938221, 32, 'other');
INSERT INTO delays VALUES (7486028, 2880750, 90, 'weather');
INSERT INTO delays VALUES (5498205, 4156979, 90, 'weather');
INSERT INTO delays VALUES (2280612, 4640846, 78, 'other');
INSERT INTO delays VALUES (2210712, 2472315, 144, 'other');
INSERT INTO delays VALUES (6931184, 4373499, 32, 'technical');
INSERT INTO delays VALUES (9585570, 9649921, 21, 'other');
INSERT INTO delays VALUES (2482473, 5763191, 175, 'weather');
INSERT INTO delays VALUES (4655366, 9857615, 117, 'crew');
INSERT INTO delays VALUES (2921871, 7592431, 128, 'crew');
INSERT INTO delays VALUES (7995991, 3548200, 18, 'technical');
INSERT INTO delays VALUES (8311915, 2275551, 14, 'crew');
INSERT INTO delays VALUES (2648569, 3817976, 111, 'crew');
INSERT INTO delays VALUES (5793227, 6717727, 30, 'technical');
INSERT INTO delays VALUES (9266855, 9171399, 35, 'crew');
INSERT INTO delays VALUES (8016103, 6396837, 180, 'other');
INSERT INTO delays VALUES (5742527, 7496589, 71, 'crew');
INSERT INTO delays VALUES (7876382, 4186470, 58, 'other');
INSERT INTO delays VALUES (8539346, 4225434, 64, 'other');
INSERT INTO delays VALUES (4215024, 4058181, 140, 'other');
INSERT INTO delays VALUES (3872091, 7381080, 87, 'weather');
INSERT INTO delays VALUES (8605377, 3294929, 21, 'other');
INSERT INTO delays VALUES (4388530, 4108742, 9, 'crew');
INSERT INTO delays VALUES (1330097, 2880750, 133, 'weather');
INSERT INTO delays VALUES (1107735, 9538900, 60, 'weather');
INSERT INTO delays VALUES (9493970, 8217563, 138, 'technical');
INSERT INTO delays VALUES (3036214, 8168832, 83, 'technical');
INSERT INTO delays VALUES (7599753, 3191211, 58, 'weather');
INSERT INTO delays VALUES (8113266, 8192045, 138, 'other');
INSERT INTO delays VALUES (4294463, 9376507, 135, 'other');
INSERT INTO delays VALUES (7890460, 8251078, 70, 'weather');
INSERT INTO delays VALUES (4649943, 8767041, 36, 'weather');
INSERT INTO delays VALUES (9985328, 8539128, 50, 'crew');
INSERT INTO delays VALUES (5198852, 8600080, 36, 'crew');
INSERT INTO delays VALUES (4902498, 3382876, 120, 'weather');
INSERT INTO delays VALUES (9554040, 6433324, 80, 'weather');
INSERT INTO delays VALUES (5622970, 9327068, 125, 'technical');
INSERT INTO delays VALUES (9762109, 1069385, 62, 'crew');
INSERT INTO delays VALUES (4432516, 9112856, 164, 'crew');
INSERT INTO delays VALUES (7697848, 8360355, 27, 'technical');
INSERT INTO delays VALUES (3552959, 1952545, 140, 'technical');
INSERT INTO delays VALUES (1685467, 5682084, 156, 'weather');
INSERT INTO delays VALUES (9002649, 6707929, 144, 'crew');
INSERT INTO delays VALUES (9190082, 4502860, 170, 'technical');
INSERT INTO delays VALUES (3489930, 6884610, 18, 'technical');
INSERT INTO delays VALUES (1352268, 5682084, 107, 'technical');
INSERT INTO delays VALUES (4742545, 8192045, 153, 'other');
INSERT INTO delays VALUES (8880060, 3548200, 101, 'crew');
INSERT INTO delays VALUES (7416721, 6251192, 150, 'weather');
INSERT INTO delays VALUES (6974925, 4356416, 94, 'weather');
INSERT INTO delays VALUES (5899046, 3564294, 78, 'other');
INSERT INTO delays VALUES (2701769, 1003920, 54, 'weather');
INSERT INTO delays VALUES (3503161, 3229588, 108, 'weather');
INSERT INTO delays VALUES (1861645, 2275551, 148, 'crew');
INSERT INTO delays VALUES (9466866, 3896243, 38, 'weather');
INSERT INTO delays VALUES (6991241, 1003920, 171, 'crew');
INSERT INTO delays VALUES (4733015, 5410579, 144, 'weather');
INSERT INTO delays VALUES (8853909, 7409070, 75, 'technical');
INSERT INTO delays VALUES (5295608, 4752878, 166, 'weather');
INSERT INTO delays VALUES (5993049, 8539128, 140, 'weather');
INSERT INTO delays VALUES (6351123, 2134249, 84, 'technical');
INSERT INTO delays VALUES (1176691, 7363844, 57, 'technical');
INSERT INTO delays VALUES (7325191, 2939418, 143, 'crew');
INSERT INTO delays VALUES (7330215, 9468998, 97, 'weather');
INSERT INTO delays VALUES (6196899, 5160345, 59, 'technical');
INSERT INTO delays VALUES (1978606, 2781589, 115, 'weather');
INSERT INTO delays VALUES (8566184, 3337487, 74, 'crew');
INSERT INTO delays VALUES (7502039, 2071840, 100, 'weather');
INSERT INTO delays VALUES (3163325, 5460257, 88, 'technical');
INSERT INTO delays VALUES (4010228, 6655240, 101, 'technical');
INSERT INTO delays VALUES (8512096, 2434115, 103, 'technical');
INSERT INTO delays VALUES (8075119, 9538900, 134, 'other');
INSERT INTO delays VALUES (4664968, 1983030, 71, 'crew');
INSERT INTO delays VALUES (2594087, 6058824, 171, 'crew');
INSERT INTO delays VALUES (8899676, 1952545, 85, 'other');
INSERT INTO delays VALUES (2328330, 4165078, 70, 'crew');
INSERT INTO delays VALUES (9543836, 8425634, 171, 'other');
INSERT INTO delays VALUES (5772630, 8192045, 65, 'technical');
INSERT INTO delays VALUES (8256584, 4205310, 162, 'other');
INSERT INTO delays VALUES (3370643, 9710039, 132, 'crew');
INSERT INTO delays VALUES (1171958, 1128351, 154, 'other');
INSERT INTO delays VALUES (8739314, 9805585, 66, 'crew');
INSERT INTO delays VALUES (3149262, 3229588, 127, 'weather');
INSERT INTO delays VALUES (5117646, 1658973, 53, 'weather');
INSERT INTO delays VALUES (4435162, 4512923, 139, 'crew');
INSERT INTO delays VALUES (7023594, 7220120, 85, 'other');
INSERT INTO delays VALUES (7194726, 8600080, 39, 'weather');
INSERT INTO delays VALUES (5695740, 5253868, 71, 'technical');
INSERT INTO delays VALUES (7391237, 1069385, 103, 'weather');
INSERT INTO delays VALUES (4238482, 1017327, 172, 'other');
INSERT INTO delays VALUES (6592976, 3668668, 124, 'weather');
INSERT INTO delays VALUES (8361825, 3191211, 26, 'weather');
INSERT INTO delays VALUES (9572491, 4737821, 134, 'other');
INSERT INTO delays VALUES (9188657, 3106369, 6, 'other');
INSERT INTO delays VALUES (6606473, 8280348, 72, 'other');
INSERT INTO delays VALUES (1586592, 5843018, 143, 'technical');
INSERT INTO delays VALUES (7224374, 3564294, 46, 'weather');
INSERT INTO delays VALUES (2878757, 3162135, 9, 'technical');
INSERT INTO delays VALUES (5289833, 7444700, 175, 'other');
INSERT INTO delays VALUES (3586265, 8832974, 130, 'other');
INSERT INTO delays VALUES (6730456, 7432459, 90, 'weather');
INSERT INTO delays VALUES (8445755, 8928018, 116, 'other');
INSERT INTO delays VALUES (6120960, 2431798, 27, 'weather');
INSERT INTO delays VALUES (8170710, 7094975, 87, 'technical');
INSERT INTO delays VALUES (2836974, 1983030, 51, 'weather');
INSERT INTO delays VALUES (8687161, 3735790, 15, 'crew');
INSERT INTO delays VALUES (6640707, 1084740, 35, 'technical');
INSERT INTO delays VALUES (9104406, 3868550, 139, 'crew');
INSERT INTO delays VALUES (2138564, 6655240, 106, 'weather');
INSERT INTO delays VALUES (5845153, 6982308, 82, 'other');
INSERT INTO delays VALUES (1976403, 2134249, 19, 'weather');
INSERT INTO delays VALUES (4015367, 4225434, 11, 'crew');
INSERT INTO delays VALUES (2326835, 3684041, 74, 'weather');
INSERT INTO delays VALUES (5136770, 6396837, 109, 'other');
INSERT INTO delays VALUES (1483196, 2651268, 82, 'other');
INSERT INTO delays VALUES (3337431, 8948764, 8, 'technical');
INSERT INTO delays VALUES (9755015, 8357045, 107, 'technical');
INSERT INTO delays VALUES (5486631, 9086205, 64, 'weather');
INSERT INTO delays VALUES (9461065, 9250393, 39, 'technical');
INSERT INTO delays VALUES (5748810, 6982308, 48, 'technical');
INSERT INTO delays VALUES (3910498, 6898752, 40, 'crew');
INSERT INTO delays VALUES (8393241, 6707929, 49, 'technical');
INSERT INTO delays VALUES (7065545, 9235735, 103, 'crew');
INSERT INTO delays VALUES (8487303, 8509070, 97, 'other');
INSERT INTO delays VALUES (4675315, 3606936, 155, 'other');
INSERT INTO delays VALUES (6377727, 4885859, 108, 'technical');
INSERT INTO delays VALUES (2719288, 3533222, 64, 'weather');
INSERT INTO delays VALUES (4842005, 1135543, 136, 'crew');
INSERT INTO delays VALUES (8098192, 8487202, 137, 'other');
INSERT INTO delays VALUES (4570994, 2880750, 149, 'technical');
INSERT INTO delays VALUES (9823994, 6009573, 139, 'technical');
INSERT INTO delays VALUES (8797271, 9840773, 121, 'crew');
INSERT INTO delays VALUES (4126807, 2094934, 25, 'crew');
INSERT INTO delays VALUES (8420555, 2963186, 131, 'weather');
INSERT INTO delays VALUES (8085942, 7220120, 121, 'technical');
INSERT INTO delays VALUES (3155674, 9269310, 112, 'weather');
INSERT INTO delays VALUES (8121020, 8360579, 91, 'crew');
INSERT INTO delays VALUES (6271552, 4205310, 61, 'technical');
INSERT INTO delays VALUES (3245551, 4737821, 171, 'crew');
INSERT INTO delays VALUES (6778938, 9468998, 115, 'other');
INSERT INTO delays VALUES (6363593, 3232025, 126, 'crew');
INSERT INTO delays VALUES (9697610, 8948764, 143, 'weather');
INSERT INTO delays VALUES (9026938, 1939454, 133, 'other');
INSERT INTO delays VALUES (6231514, 7165388, 148, 'crew');
INSERT INTO delays VALUES (4746604, 8356612, 142, 'other');
INSERT INTO delays VALUES (3193883, 7522865, 10, 'other');
INSERT INTO delays VALUES (8841344, 2502371, 87, 'crew');
INSERT INTO delays VALUES (2148745, 7848741, 153, 'weather');
INSERT INTO delays VALUES (2537475, 6947320, 144, 'weather');
INSERT INTO delays VALUES (2595807, 7705816, 43, 'weather');
INSERT INTO delays VALUES (6170750, 3191211, 176, 'other');
INSERT INTO delays VALUES (3688991, 3606936, 13, 'technical');
INSERT INTO delays VALUES (5017300, 8641311, 33, 'other');
INSERT INTO delays VALUES (9219933, 8694911, 124, 'weather');
INSERT INTO delays VALUES (1077453, 8767041, 57, 'technical');
INSERT INTO delays VALUES (4228078, 7312338, 51, 'weather');
INSERT INTO delays VALUES (9791803, 6613650, 41, 'technical');
INSERT INTO delays VALUES (7389926, 5734273, 72, 'weather');
INSERT INTO delays VALUES (4970379, 8600776, 158, 'other');
INSERT INTO delays VALUES (9337821, 8192045, 14, 'crew');
INSERT INTO delays VALUES (4816111, 1651685, 21, 'crew');
INSERT INTO delays VALUES (3802409, 1123836, 35, 'crew');
INSERT INTO delays VALUES (7084325, 9494788, 59, 'other');
INSERT INTO delays VALUES (1273483, 5032808, 168, 'weather');
INSERT INTO delays VALUES (9667425, 3802445, 111, 'other');
INSERT INTO delays VALUES (9202765, 2718238, 16, 'other');
INSERT INTO delays VALUES (8558607, 1118405, 61, 'other');
INSERT INTO delays VALUES (7631825, 7220120, 12, 'technical');
INSERT INTO delays VALUES (6054205, 6433324, 22, 'weather');
INSERT INTO delays VALUES (9842985, 2191318, 68, 'weather');
INSERT INTO delays VALUES (1358060, 9311534, 7, 'crew');
INSERT INTO delays VALUES (3895733, 7542327, 66, 'other');
INSERT INTO delays VALUES (5600040, 1148814, 13, 'weather');
INSERT INTO delays VALUES (7373878, 4229693, 51, 'crew');
INSERT INTO delays VALUES (5245341, 4356416, 61, 'weather');
INSERT INTO delays VALUES (1139698, 7094975, 109, 'other');
INSERT INTO delays VALUES (1947168, 4787447, 149, 'crew');
INSERT INTO delays VALUES (6611214, 1930953, 135, 'weather');
INSERT INTO delays VALUES (1195266, 6613650, 82, 'other');
INSERT INTO delays VALUES (5818644, 8600776, 10, 'technical');
INSERT INTO delays VALUES (1520715, 7381080, 117, 'technical');
INSERT INTO delays VALUES (6751746, 4762283, 175, 'crew');
INSERT INTO delays VALUES (4063206, 2939418, 53, 'weather');
INSERT INTO delays VALUES (5631808, 1952545, 106, 'technical');
INSERT INTO delays VALUES (7447685, 2111920, 38, 'weather');
INSERT INTO delays VALUES (6475381, 4055794, 89, 'weather');
INSERT INTO delays VALUES (2647723, 6938430, 38, 'crew');
INSERT INTO delays VALUES (7235783, 7154876, 156, 'weather');
INSERT INTO delays VALUES (1962047, 5631640, 9, 'technical');
INSERT INTO delays VALUES (3872880, 1135543, 77, 'weather');
INSERT INTO delays VALUES (7046719, 2013782, 79, 'other');
INSERT INTO delays VALUES (2987313, 9250393, 139, 'technical');
INSERT INTO delays VALUES (1191985, 7618732, 57, 'technical');
INSERT INTO delays VALUES (1412742, 6009573, 82, 'other');
INSERT INTO delays VALUES (8549768, 9235735, 25, 'crew');
INSERT INTO delays VALUES (5956926, 7300889, 18, 'other');
INSERT INTO delays VALUES (5886992, 1460374, 174, 'technical');
INSERT INTO delays VALUES (8252704, 7039442, 42, 'technical');
INSERT INTO delays VALUES (3528734, 3788765, 32, 'technical');
INSERT INTO delays VALUES (1710413, 3577336, 161, 'technical');
INSERT INTO delays VALUES (3528512, 2855984, 137, 'technical');
INSERT INTO delays VALUES (4439918, 3577336, 47, 'crew');
INSERT INTO delays VALUES (6929426, 2727975, 156, 'technical');
INSERT INTO delays VALUES (2554767, 9538900, 6, 'crew');
INSERT INTO delays VALUES (1948624, 9327068, 160, 'weather');
INSERT INTO delays VALUES (6284001, 2828272, 122, 'crew');
INSERT INTO delays VALUES (6057694, 3684041, 72, 'other');
INSERT INTO delays VALUES (3310015, 5074305, 24, 'technical');
INSERT INTO delays VALUES (8199925, 1118405, 164, 'crew');
INSERT INTO delays VALUES (8932981, 9222958, 77, 'other');
INSERT INTO delays VALUES (6210921, 6251192, 100, 'weather');
INSERT INTO delays VALUES (5426205, 4534704, 28, 'crew');
INSERT INTO delays VALUES (7235446, 8551136, 176, 'weather');
INSERT INTO delays VALUES (2089827, 2275551, 115, 'weather');
INSERT INTO delays VALUES (8713405, 6384465, 54, 'crew');
INSERT INTO delays VALUES (5865622, 7640982, 118, 'crew');
INSERT INTO delays VALUES (9051387, 3061980, 57, 'technical');
INSERT INTO delays VALUES (3217317, 6982308, 22, 'other');
INSERT INTO delays VALUES (2342185, 7496589, 138, 'weather');
INSERT INTO delays VALUES (6925281, 7759286, 92, 'other');
INSERT INTO delays VALUES (1319739, 4986608, 98, 'crew');
INSERT INTO delays VALUES (6966455, 8841927, 99, 'weather');
INSERT INTO delays VALUES (8200764, 1460374, 121, 'crew');
INSERT INTO delays VALUES (5756141, 4885859, 18, 'weather');
INSERT INTO delays VALUES (5444008, 2609422, 73, 'crew');
INSERT INTO delays VALUES (5595628, 7759286, 149, 'technical');
INSERT INTO delays VALUES (2983048, 8457243, 164, 'technical');
INSERT INTO delays VALUES (6529797, 7525533, 139, 'other');
INSERT INTO delays VALUES (7012472, 6773294, 169, 'other');
INSERT INTO delays VALUES (2900823, 6288275, 73, 'weather');
INSERT INTO delays VALUES (1623029, 8168832, 103, 'crew');
INSERT INTO delays VALUES (7714637, 2609422, 138, 'technical');
INSERT INTO delays VALUES (6540786, 2939418, 110, 'weather');
INSERT INTO delays VALUES (2506333, 2979638, 41, 'technical');
INSERT INTO delays VALUES (8259980, 5537933, 18, 'technical');
INSERT INTO delays VALUES (7869864, 4407456, 120, 'other');
INSERT INTO delays VALUES (3838258, 4407456, 55, 'other');
INSERT INTO delays VALUES (9862341, 9222958, 139, 'other');
INSERT INTO delays VALUES (9037393, 6839691, 87, 'other');
INSERT INTO delays VALUES (5193725, 3106369, 68, 'weather');
INSERT INTO delays VALUES (8180287, 7275094, 174, 'other');
INSERT INTO delays VALUES (4640019, 9857615, 130, 'weather');
INSERT INTO delays VALUES (3337316, 8627402, 65, 'weather');
INSERT INTO delays VALUES (1046395, 4502860, 67, 'crew');
INSERT INTO delays VALUES (4375625, 2651268, 155, 'crew');
INSERT INTO delays VALUES (1340757, 7094975, 172, 'other');
INSERT INTO delays VALUES (8940972, 4186470, 49, 'weather');
INSERT INTO delays VALUES (8455568, 1362332, 103, 'technical');
INSERT INTO delays VALUES (5429777, 8360355, 168, 'weather');
INSERT INTO delays VALUES (3083649, 6540542, 69, 'technical');
INSERT INTO delays VALUES (5325094, 4762283, 41, 'technical');
INSERT INTO delays VALUES (1073000, 1952545, 44, 'weather');
INSERT INTO delays VALUES (6536916, 7821647, 91, 'technical');
INSERT INTO delays VALUES (8041858, 7496589, 126, 'weather');
INSERT INTO delays VALUES (8823946, 8574016, 160, 'weather');
INSERT INTO delays VALUES (4946678, 4501881, 150, 'other');
INSERT INTO delays VALUES (8603961, 8993574, 28, 'other');
INSERT INTO delays VALUES (4102596, 3577336, 115, 'other');
INSERT INTO delays VALUES (7709365, 4050052, 165, 'other');
INSERT INTO delays VALUES (5872364, 4156979, 166, 'weather');
INSERT INTO delays VALUES (4415241, 1190190, 66, 'crew');
INSERT INTO delays VALUES (4056704, 9795505, 48, 'other');
INSERT INTO delays VALUES (1258022, 3791114, 37, 'weather');
INSERT INTO delays VALUES (5270979, 2609422, 17, 'other');
INSERT INTO delays VALUES (8680577, 9795505, 23, 'weather');
INSERT INTO delays VALUES (5366703, 1170704, 55, 'other');

-- =========== skymill.cancellations (generic) ==========

DROP TABLE IF EXISTS cancellations;

CREATE TABLE cancellations (
  id INT NOT NULL,
  flight_instance_id INT NOT NULL,
  cancellation_reason VARCHAR(255) NOT NULL,
  cancellation_time DATE NOT NULL
);

INSERT INTO cancellations VALUES (4220637, 7444700, 'operational', '2026-01-15');
INSERT INTO cancellations VALUES (6114994, 8522282, 'technical', '2026-04-13');
INSERT INTO cancellations VALUES (4799313, 5855521, 'low_demand', '2026-04-09');
INSERT INTO cancellations VALUES (3899844, 4752878, 'operational', '2026-05-24');
INSERT INTO cancellations VALUES (8579598, 8357045, 'low_demand', '2026-05-02');
INSERT INTO cancellations VALUES (9892152, 3443010, 'operational', '2026-04-17');
INSERT INTO cancellations VALUES (1598654, 8399090, 'technical', '2026-03-31');
INSERT INTO cancellations VALUES (5196014, 8928018, 'weather', '2026-01-16');
INSERT INTO cancellations VALUES (7426891, 7275094, 'operational', '2026-03-25');
INSERT INTO cancellations VALUES (8905724, 4509881, 'operational', '2026-03-30');
INSERT INTO cancellations VALUES (3500421, 2071840, 'low_demand', '2026-02-05');
INSERT INTO cancellations VALUES (5149166, 2071840, 'weather', '2026-04-27');
INSERT INTO cancellations VALUES (7884860, 3735507, 'technical', '2026-01-12');
INSERT INTO cancellations VALUES (9113345, 6947320, 'low_demand', '2026-03-28');
INSERT INTO cancellations VALUES (8917177, 1135543, 'low_demand', '2026-02-21');
INSERT INTO cancellations VALUES (7596506, 8733132, 'weather', '2026-02-05');
INSERT INTO cancellations VALUES (8997956, 1017327, 'low_demand', '2026-02-27');
INSERT INTO cancellations VALUES (5837548, 3735507, 'technical', '2026-01-31');
INSERT INTO cancellations VALUES (1878348, 2939707, 'low_demand', '2026-03-06');
INSERT INTO cancellations VALUES (8983866, 7112514, 'low_demand', '2026-04-26');
INSERT INTO cancellations VALUES (6870217, 1574416, 'weather', '2026-04-23');
INSERT INTO cancellations VALUES (9999214, 2422914, 'low_demand', '2026-01-30');
INSERT INTO cancellations VALUES (6662620, 3817976, 'low_demand', '2026-02-10');
INSERT INTO cancellations VALUES (9077748, 3294929, 'low_demand', '2026-04-17');
INSERT INTO cancellations VALUES (7142688, 1832564, 'weather', '2026-01-07');
INSERT INTO cancellations VALUES (9782794, 8023566, 'weather', '2026-02-17');
INSERT INTO cancellations VALUES (7669501, 3735790, 'weather', '2026-03-03');
INSERT INTO cancellations VALUES (3832810, 4787447, 'technical', '2026-04-02');
INSERT INTO cancellations VALUES (5185714, 5032808, 'technical', '2026-04-29');
INSERT INTO cancellations VALUES (7985792, 3533222, 'weather', '2026-05-04');
INSERT INTO cancellations VALUES (9059961, 4768588, 'technical', '2026-02-13');
INSERT INTO cancellations VALUES (3246966, 6296538, 'weather', '2026-05-14');
INSERT INTO cancellations VALUES (8415125, 6411045, 'technical', '2026-03-29');
INSERT INTO cancellations VALUES (3561327, 7112514, 'technical', '2026-01-03');
INSERT INTO cancellations VALUES (3316034, 1034182, 'weather', '2026-05-19');
INSERT INTO cancellations VALUES (9084666, 9555093, 'weather', '2026-04-02');
INSERT INTO cancellations VALUES (1029804, 3516493, 'weather', '2026-05-10');
INSERT INTO cancellations VALUES (3883114, 9327068, 'operational', '2026-04-29');
INSERT INTO cancellations VALUES (8241676, 9805585, 'operational', '2026-05-02');
INSERT INTO cancellations VALUES (7850484, 1277419, 'operational', '2026-02-24');
INSERT INTO cancellations VALUES (8560923, 4388944, 'low_demand', '2026-05-18');
INSERT INTO cancellations VALUES (5064373, 3668668, 'operational', '2026-01-20');
INSERT INTO cancellations VALUES (6235257, 4768588, 'operational', '2026-02-13');
INSERT INTO cancellations VALUES (5068131, 6468600, 'weather', '2026-03-18');
INSERT INTO cancellations VALUES (3263002, 1930953, 'weather', '2026-04-28');
INSERT INTO cancellations VALUES (5841707, 7843855, 'weather', '2026-04-26');
INSERT INTO cancellations VALUES (5453795, 9250393, 'technical', '2026-03-29');
INSERT INTO cancellations VALUES (7031291, 1832564, 'weather', '2026-03-07');
INSERT INTO cancellations VALUES (3815442, 4841212, 'technical', '2026-05-31');
INSERT INTO cancellations VALUES (9222148, 7542327, 'operational', '2026-02-11');
INSERT INTO cancellations VALUES (3884884, 2071840, 'operational', '2026-01-11');
INSERT INTO cancellations VALUES (4365061, 6411045, 'low_demand', '2026-05-17');
INSERT INTO cancellations VALUES (1313386, 4388944, 'low_demand', '2026-05-15');
INSERT INTO cancellations VALUES (7686803, 4986608, 'weather', '2026-05-21');
INSERT INTO cancellations VALUES (8490079, 4787447, 'low_demand', '2026-02-09');
INSERT INTO cancellations VALUES (9446368, 5028809, 'low_demand', '2026-04-25');
INSERT INTO cancellations VALUES (5767648, 1983030, 'weather', '2026-04-03');
INSERT INTO cancellations VALUES (2907998, 4501881, 'operational', '2026-03-30');
INSERT INTO cancellations VALUES (5955467, 6920189, 'low_demand', '2026-02-21');
INSERT INTO cancellations VALUES (3451730, 3443010, 'operational', '2026-04-16');
INSERT INTO cancellations VALUES (5320363, 1617454, 'technical', '2026-03-11');
INSERT INTO cancellations VALUES (5845101, 7604448, 'weather', '2026-05-26');
INSERT INTO cancellations VALUES (9166065, 6411045, 'weather', '2026-02-19');
INSERT INTO cancellations VALUES (8293421, 6288275, 'technical', '2026-02-04');
INSERT INTO cancellations VALUES (5353743, 6058824, 'weather', '2026-03-31');
INSERT INTO cancellations VALUES (3224368, 2319921, 'operational', '2026-04-02');
INSERT INTO cancellations VALUES (5911779, 5458179, 'weather', '2026-02-08');
INSERT INTO cancellations VALUES (4061109, 8522282, 'weather', '2026-05-16');
INSERT INTO cancellations VALUES (9646399, 8360579, 'operational', '2026-05-07');
INSERT INTO cancellations VALUES (8266898, 1594856, 'operational', '2026-03-17');

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

INSERT INTO ticket_prices VALUES (4261383, 168.5176263354057, 176.93370278471102, 455.48934641891304, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4573690, 1396.7897119347479, 279.2871726562127, 181.5807108203623, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4735169, 293.00284296214966, 68.51907513666511, 628.7300032075643, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1707120, 1255.2332351746302, 27.835858690563878, 670.4186544916975, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5801072, 1111.5672393888804, 167.09483909614164, 109.12608522471001, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4251915, 793.8991620992607, 65.7754163001848, 275.73452999532253, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2279482, 1197.381438362029, 280.96637021238155, 924.3862652744514, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3812530, 878.1310559569457, 67.7456442268782, 886.4776366303578, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8751110, 209.2300332564698, 255.50199568021398, 837.581088735235, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9231572, 1498.4534528172362, 230.92708824010347, 321.5379420507662, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5542530, 967.0934856921926, 78.59132234078785, 309.0888267612008, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2242802, 663.6884058188722, 213.51295480448718, 578.3230166967347, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3288648, 757.21878395262, 234.95754514681684, 560.0104095476596, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6737528, 1315.1885659854092, 133.75883163314933, 179.12428667216673, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1680444, 88.67461826506309, 205.3981709154267, 795.9782086696191, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7378637, 714.5690740204548, 121.90929069039689, 610.7239335930236, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4108983, 741.9215594588524, 275.19784409301485, 296.12565281130685, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9910656, 1420.8441174241796, 195.54999644140523, 496.0860094487535, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2278844, 954.7258675514606, 46.60886147140445, 209.9325266137859, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2485628, 184.43637793184288, 132.9444500671667, 942.0306509786627, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9914125, 379.7498854720597, 78.27162676068474, 745.4588489931409, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6471311, 680.6893901180966, 193.05051691907445, 487.9478688160225, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8826297, 548.7315615402242, 12.214450948341966, 522.4936638976938, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7480159, 1090.171573671107, 214.20468541949214, 140.73876043640686, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1256000, 495.25336724382277, 151.58274435196805, 81.6001831913612, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7121565, 1095.0241265202365, 63.30157316037899, 545.3258068786122, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6489369, 1479.2645562115129, 237.35558331182054, 871.4307585558835, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5247321, 734.475324797188, 114.51872042226618, 258.1364553790868, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9907707, 1467.517516574946, 109.05202798226355, 127.8904557176358, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5219857, 83.85609389549481, 66.40219802447552, 627.2899164175863, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9068057, 422.3164615625605, 213.19000304513787, 476.83223152974807, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2242802, 1007.4520556433458, 34.07112782234972, 212.9800493815044, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4786841, 1134.9362337357038, 254.45900262638258, 931.6441710878873, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4883078, 162.12941985423203, 224.78363179210552, 487.0086787213034, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2157626, 323.2856989502209, 129.1856960266872, 242.541109293821, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6191290, 759.8510487352252, 150.5464579768875, 635.2028692236929, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2684538, 446.07885339790573, 201.29601217111966, 277.6266759410823, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4993121, 958.1405498212741, 291.78251065004184, 781.3505224667603, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5682233, 190.98042583723918, 234.3919845451558, 504.65361012309273, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7905117, 702.6183659713953, 160.76651747937072, 714.8365444571699, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8761363, 228.3986701681183, 74.24341236331951, 74.35589376368135, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4081283, 461.8107709608508, 53.11160204513843, 352.71836836302583, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3495958, 1086.3721392197028, 171.4886166895985, 643.6007093698062, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8498641, 667.5613963300838, 13.159750605162163, 64.1943480317423, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8757277, 836.3620823678991, 205.25941231647855, 497.5094726817527, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2428666, 1281.8785974565058, 32.59828427185815, 146.36819921718524, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6737528, 252.0379007580526, 132.68844838389126, 461.09129935422067, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1112634, 1487.216919932164, 293.3897655476024, 986.5040698499799, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4717614, 434.5247148174713, 60.16876865465703, 88.67320169089932, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9205451, 361.5540029768832, 266.1732392229205, 192.89648543563365, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3607816, 997.3843744988154, 169.21142923485007, 553.184532704888, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7838776, 444.34631088934594, 29.264755886082668, 727.8192830302494, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1618469, 247.05810982833796, 136.88125686312748, 914.5503464568202, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4993121, 639.7915218192471, 154.0272284244804, 867.0315352943911, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4498742, 441.20771865731194, 150.90827068376896, 802.5468480939654, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1347910, 250.8252524319051, 38.39813084713457, 398.6468221082835, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2955636, 1235.0578327823232, 12.5747008083538, 584.0515030515259, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4429427, 824.684825404107, 271.14107036463497, 766.5686607472819, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5329863, 878.69526654273, 183.50915173920285, 902.7736712784761, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9205451, 776.2285545279173, 117.25104903396381, 440.80893526639755, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1761602, 1383.9018258897966, 243.86637608262063, 85.8273053191596, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2227821, 124.71941689288472, 281.33184058478133, 217.59183059534837, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6800002, 526.1675818728386, 138.74598830291407, 623.7306265342053, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3472111, 1207.552415788241, 77.08485380534985, 629.8276491664593, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2103439, 536.7655918698349, 259.94927513959703, 548.6008124736917, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3004822, 531.3137346856424, 246.7147100433952, 847.4691302766399, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3772289, 396.6631701107033, 69.8068904644525, 136.93163113896645, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9902612, 841.944298520014, 93.60796533805309, 977.5721955538608, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5133949, 1033.2429174042873, 298.7901702209261, 787.4849901618637, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2014039, 1155.7233746048007, 136.04773424845928, 256.84532077387377, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5715678, 1019.9147732086818, 273.7817902761496, 678.2772994945846, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5529625, 858.181176726293, 15.202709047779305, 305.7742876703157, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9277551, 929.825871828241, 34.62050713726545, 844.4110398778297, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6746619, 1420.0427857863401, 199.82576578444207, 632.6616118621955, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7379762, 1124.6487309028112, 202.77303927870105, 325.6469050238645, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2507915, 1231.2981862857093, 152.4407340137339, 240.42155918695252, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8757277, 1275.4486003233303, 253.0363153942619, 650.8416220376831, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2237331, 1263.7661842545383, 219.95675725893176, 387.03989092693433, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1090159, 919.7539674036284, 148.52349886629386, 104.37167587194995, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8090201, 1319.7135863112208, 253.77616511676987, 83.1720752738867, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2100070, 908.369628411762, 283.309841894378, 22.645155146264862, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3575513, 699.4408591630746, 295.41002962824496, 440.29789163308266, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3636943, 223.70712578413801, 101.74244389833201, 423.52708064140097, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7674918, 66.87346125762004, 273.53704811576864, 734.9841953247288, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7437946, 725.9378493531777, 55.37581914511546, 841.4659486411093, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6283664, 254.5180484463142, 294.30478187329277, 208.19756008229805, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8267966, 237.51316558089243, 141.51434163939888, 855.8697109349973, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6312080, 702.9266120837467, 273.24539816424027, 708.1111741932966, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1266417, 770.42770258923, 217.5787951939806, 799.4116412558008, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6789367, 745.6038642138608, 73.20017476154717, 540.8025184679302, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9995399, 671.6456587424058, 151.62350329816016, 50.00600057853888, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6115972, 518.1102890269042, 278.4855614269154, 658.4538472733644, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8549424, 368.8225196153797, 104.47671991108835, 209.66743377130993, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4385974, 1192.95975971998, 85.6867314329305, 939.8730218606223, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2439246, 106.85736707589956, 77.13022648530165, 908.5998742195143, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1310627, 285.80930453909303, 27.081586512299584, 188.7242165368962, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7634150, 986.5831283898696, 25.259443064837335, 939.4367942087316, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2279482, 597.2282186846021, 158.2924411723479, 149.05743273172135, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2504743, 559.8940775767505, 48.68329998636666, 69.2349259418179, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1497075, 1085.525082294708, 116.30165604047565, 631.4736613276638, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1665327, 1290.9993283127496, 193.23251589798213, 972.7179416544192, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7719363, 1042.1677944475618, 242.69130761998548, 589.4972991460182, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2572538, 560.4695557526883, 63.3924519583634, 626.0760523056825, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9841462, 1132.045332553839, 144.8690853671124, 311.7439194911811, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6039534, 1473.5272281243244, 118.19447793825559, 209.99331449717306, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9851914, 932.3834040773371, 132.5675243476626, 26.53281473039637, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9084029, 1101.2734096620986, 203.43079526339523, 490.8897757151573, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4837146, 1384.1620951955263, 91.89925828937548, 47.575693197655866, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2518875, 1305.6119450965953, 291.0415571178829, 269.4273478918987, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1117814, 566.2151635374616, 78.71868311331515, 93.5081512593382, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2847786, 1165.014751445923, 273.3739712412261, 361.91631430685686, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9202936, 618.0250238556383, 257.1934651541742, 567.4152842695958, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2243340, 1147.2926550015475, 52.35598220792976, 175.8562771355746, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4822334, 191.52540412403738, 104.88101886016824, 973.1085275873041, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9805983, 1337.8433015485896, 227.86299195203804, 245.49697673953975, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2507915, 911.2788154913571, 281.70360157042285, 818.1177286880733, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1142136, 970.1901880878331, 27.923607581086916, 822.107107108665, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3472111, 345.2002059235515, 147.5910213774021, 129.7506833659754, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3664248, 525.8468644333192, 52.172530210080446, 706.0238171854623, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2587001, 1120.8703437524287, 299.51590261288794, 533.6375053245115, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4338621, 899.9544848943457, 201.1325427885974, 601.6502023073874, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1351333, 534.2026566956386, 205.03444383272225, 369.912831171095, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1987378, 668.8611709520424, 222.88062002388347, 322.3503247065751, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9906347, 92.53834740092486, 48.73793939104141, 294.33841112960033, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7165585, 890.0450995645164, 236.72647272314416, 896.311339279396, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3945756, 196.62497987669107, 111.32210603380237, 325.4792366680308, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7875307, 676.5701683891155, 97.0127567678041, 909.300712331678, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8366102, 698.141307552137, 66.48276462395674, 798.9416104153867, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6776605, 678.4095812740507, 69.79081282602642, 657.7029163684288, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6598224, 1402.670476439907, 280.1841287455464, 315.921136978004, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9954294, 498.0061816575133, 142.96227642118026, 951.9051026276308, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7503499, 407.48918606987417, 169.77670528784145, 785.2471321463586, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5592330, 91.96197528090235, 63.7802375326516, 808.0312849943757, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6325239, 468.2048567065491, 230.62424540661175, 423.31062475334227, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6835303, 910.3795310629215, 251.42161025475286, 731.4021939727351, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7979720, 832.4028370114984, 297.9175822974607, 476.0575401887952, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5609759, 1181.4000324568385, 283.00968106684957, 805.8715138449055, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6424865, 61.68451902160955, 60.50827479958139, 161.45092336264145, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2727560, 1211.051643827285, 187.07368008449635, 653.5483016138595, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3481798, 956.4878104482605, 37.24394636069442, 737.5380660742966, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9068057, 482.41962368350516, 24.027448539176874, 577.4447154685481, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6484248, 294.5778116225806, 96.18105593439192, 8.565256174169589, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2770238, 1129.3938878675283, 79.80889940734706, 326.87939209073255, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4350465, 478.33486534190826, 111.34949275328037, 193.0699051375977, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3935854, 62.87178181460742, 290.9296383974688, 712.5061256775072, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9770279, 431.3562237910855, 204.92149320738866, 552.3183004155812, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4836866, 1336.9963314512675, 103.67127443507832, 496.2749929447583, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8694998, 976.6480869997051, 237.39716496170777, 634.5546432660465, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5782852, 410.9413704795836, 227.85125092272557, 57.368124474784125, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2738783, 1078.2536836421777, 206.18811048690094, 90.69196125942992, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1090159, 56.97238708046824, 161.62295958350543, 362.20555994704364, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8389626, 425.19776431508444, 80.02456153615091, 302.2755134809718, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7486164, 1063.3989381668162, 92.97449781307695, 830.466735195861, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7785771, 422.09306212190455, 288.3924772294821, 924.5348255529912, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7121565, 565.5329800800229, 297.97035243675356, 639.1413074389428, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6634468, 818.6077269748846, 72.57039296324425, 387.9502978535545, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8494594, 1202.826024203323, 91.28920384389188, 248.424437899886, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7487130, 1128.648864420645, 130.5224672386998, 449.650847108739, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5292000, 1416.9572078594897, 63.37146418334637, 878.9583754745858, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1370034, 514.7600367720688, 61.996596879751586, 629.4887035388952, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9912794, 155.9628241549318, 270.9161635378066, 583.8773330588172, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2416661, 483.25436386860616, 42.16269861597432, 237.27749052613555, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1038659, 808.8958506469162, 164.01422848032124, 893.4661890178313, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6555282, 648.4043774487253, 66.30229744075834, 560.5580456195347, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2885884, 1165.8522186139437, 198.28837045707394, 581.871185852032, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4889610, 1350.4748350812151, 129.0934755942988, 88.90837206904945, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4563507, 1158.0430098027753, 102.10286333136855, 996.3512921243836, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2383563, 1011.925509595298, 246.49552340515845, 858.2654980497773, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6602149, 762.1559967865361, 91.64163690246738, 771.3299018965519, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1285035, 123.88204558541774, 282.04744919663614, 903.4517543818031, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3060916, 1286.8757712120043, 88.1992134594868, 912.0430360990812, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4786841, 441.22139004954715, 18.153043887625927, 996.1285491045886, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8575372, 1036.3365765415688, 73.4697243751041, 782.7083188610771, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9378525, 1170.218202283488, 149.76553181338764, 315.5274384504043, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7766085, 409.31272381607084, 38.4164686248364, 671.9455604805901, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1757886, 96.69499408002955, 223.05807920258877, 325.51283253121176, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2718768, 888.6314299113272, 95.27483422134806, 844.0648125005546, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3004616, 864.6061869246669, 279.6716104644876, 90.89234677864044, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3495958, 316.9221868504935, 254.388813358232, 56.96196877781012, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7730782, 593.1411586325984, 164.78006076664695, 51.82715938879567, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2940006, 1286.3584212525438, 293.7770101591334, 524.6769871210032, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6794875, 1222.4218718159264, 287.1067788838608, 960.0411539010072, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1013555, 1061.0736286436959, 277.78322822823606, 662.8228024675445, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9097555, 177.31915812309117, 273.40362004742167, 437.2718647501603, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2955636, 1232.0338153975242, 62.03838815212827, 514.2234299529005, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6353762, 1280.8604398441062, 176.10176333136155, 94.73749423745747, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8156954, 934.6810379251301, 29.418026177143453, 771.7305295805897, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6325239, 910.3073399990349, 23.8568116362305, 631.1555461926307, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6845883, 1344.3952174446717, 290.09386257679785, 9.326598811884246, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3999987, 1400.5526480568121, 55.94494782162347, 320.49614772337577, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8001981, 1224.8037401582428, 213.0957656570713, 43.23759204789401, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7759751, 295.58306410180734, 137.31586897695976, 801.040039334168, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9175129, 1351.4816715278982, 101.75083781852273, 982.3156353842985, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5324424, 1056.5217871263958, 278.8153852935009, 894.9947974926, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8561208, 966.103025705098, 267.3899425454844, 737.8667159631021, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6662249, 822.1573263301667, 109.75814899878536, 245.37050859890653, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9193407, 1438.8063843638924, 36.95381986826911, 107.1253543212376, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7823393, 718.9078522246504, 285.2201004218979, 984.4318538684513, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3171847, 118.27287670646439, 40.96436855892226, 913.9689324465871, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9641055, 1410.280436514541, 28.249273209456707, 109.66002201264224, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6375811, 1164.934115007243, 255.825627494962, 633.357105430312, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1090159, 1260.8920353872436, 104.94791312406508, 176.1783955556093, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8420487, 1267.1231837563196, 232.72313297697957, 768.8580394829983, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4311819, 1058.352386682877, 154.1409129074596, 862.6992829457531, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2472191, 1132.7559118791146, 65.30331778145708, 587.2800833899813, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7441461, 141.1310044193284, 127.96710202950341, 392.43830662504854, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5829260, 334.3871986390403, 216.27113019430743, 70.60807131030433, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2653273, 1225.921873360091, 155.1441831667749, 869.060377130157, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8664096, 1210.7732680028832, 19.89948817476791, 73.60573762143396, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9902612, 950.4370931807179, 286.45340826240493, 812.5390525579364, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9278508, 344.4858091948032, 89.7602696743006, 397.87239328158597, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9955726, 1363.373585256558, 44.98873662503397, 281.9845319084552, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4717614, 846.5150936535294, 119.69551859805738, 882.4640494794473, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9868956, 501.2070600155879, 246.74297236578633, 438.6638023193409, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9633280, 1436.4448010389444, 181.99206029160558, 18.56990519795765, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4070427, 55.64735223042106, 244.13098424896427, 975.7670585020826, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2825950, 382.402199542499, 94.7318391603346, 556.3573486490233, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6662249, 910.8621260694199, 52.8116434970929, 232.79262031404403, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2289604, 600.6552368089417, 290.1138273241235, 817.2916696242123, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9669297, 627.8080598933989, 270.72849998742885, 836.4896295389767, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1239047, 1037.181194742622, 74.67070261620992, 450.43697447965747, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4412672, 395.88671467640137, 183.50915378154752, 919.6303452880634, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7487130, 386.62892965119556, 205.945042570264, 137.42977790333933, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6451503, 831.247270568397, 219.83973647859256, 137.3998632142498, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5445304, 1157.0344295467244, 26.294440935897136, 34.204114420973596, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1740429, 1447.5441889716365, 272.3147925556275, 30.915996078209716, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9097555, 246.00433423716728, 47.60311668297321, 536.3973058058448, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4075450, 930.8736729091319, 183.86452083346012, 794.0806859919121, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2278844, 723.618838213619, 195.8758028815986, 351.0084432597411, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9111289, 1150.9509182843442, 49.0180411749087, 107.46561106247732, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7398463, 1226.0740443847583, 243.24925640955345, 527.46286107916, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4311819, 1164.6177414863482, 87.82415381345585, 357.44250053172357, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3944942, 325.97932575115425, 272.35989093271036, 689.431719519867, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1834015, 96.31273847907494, 155.68375896089015, 309.0626088990619, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5773547, 192.2517464591731, 58.30902839396122, 460.34604859935934, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6489369, 247.2802153233408, 295.9872559198044, 311.4882861593772, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3947920, 982.0345716109142, 35.38655007116235, 807.0265090909693, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3342522, 438.45156133686004, 42.03473602470688, 835.5363498126659, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4786841, 181.62609277546153, 257.4655122057807, 203.20132282269077, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7976370, 311.13253887496455, 29.94106414951305, 295.5340571453265, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1135875, 1280.0279079932766, 233.2847558034895, 938.7054129626933, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4889610, 1024.676565687766, 275.210656990351, 541.4104042483716, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5169004, 966.8417659018727, 123.10081188270759, 953.906486048651, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4936817, 994.5633826601975, 154.989714722262, 396.6752407250323, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9532390, 556.8382470966969, 196.5900966203321, 197.62225697251833, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8070249, 487.67656651770085, 69.4741245474211, 398.93639920509395, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4034151, 817.8841095400751, 43.70981858754114, 2.530187110359239, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9382549, 1255.299142063825, 53.88144231673705, 547.9989687947324, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8267966, 421.1532724532092, 119.11964215450799, 831.8141987150243, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4350465, 1168.4761707097366, 88.87430433737966, 947.5244017768023, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9532390, 1267.093836185185, 237.05389865371876, 748.20091749699, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3137618, 794.493305765855, 94.56477322887173, 74.74267096580655, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4366043, 1099.0914121451146, 86.10228498478682, 179.2084775095718, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1618469, 330.10146703212786, 142.60333012136832, 822.4084325159125, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8262564, 196.6492518825148, 196.77111450706303, 201.71655368870344, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7766085, 668.5398680837728, 117.682579395645, 686.4346808922876, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5317026, 1422.5937626803748, 172.83468266021936, 643.8230022406482, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4957570, 916.3086003590191, 236.01639545846996, 112.36123450125046, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6784869, 1190.4491189302337, 117.74489493678263, 904.4745125561461, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3826951, 235.5950082884283, 98.06396871999264, 861.2791870628179, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7477072, 454.16423161744996, 175.34414406902928, 424.47087619609005, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5060382, 521.8175965322632, 102.15815775945447, 336.61604167253324, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7829109, 1186.7671144052022, 271.9214586524654, 540.8751245940828, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8348918, 693.6669442925939, 232.19231796594318, 945.3795391122586, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2014039, 1165.429156281723, 67.59139334939591, 729.0771750991279, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4700172, 172.33427073810662, 118.81155015514122, 154.03925141470265, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2587001, 575.8954349332095, 291.03507813658587, 816.1826308450277, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8838174, 794.6532805776275, 261.59107790350106, 538.9769763434452, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9231572, 1298.8758440117097, 180.26944650880642, 985.0569048968914, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7634150, 1107.909497042412, 164.37090425751526, 320.0209754635865, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6208447, 1106.4648216063033, 212.90983061415903, 680.9053088960843, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9705715, 1032.4030796905674, 239.81793942237252, 497.1749169796551, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1094879, 1486.5435062091567, 187.85659827376267, 633.6028743521791, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9914125, 948.6580831333222, 234.19313715163796, 25.27144744061505, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7052319, 718.5481116388696, 45.075728968156035, 952.5126307706314, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3328093, 1240.9023799929905, 117.32222558926352, 421.14724477179186, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6686020, 1048.9921099791868, 183.0067538401624, 381.20640192306973, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3796609, 783.3078197352144, 282.11052666351605, 706.8594665183643, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3001561, 208.29920878791276, 11.71222002858865, 248.52669431061514, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7553272, 843.376098533842, 220.13519804712095, 240.33986364182303, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9856719, 1215.8743300319736, 292.27880013382224, 283.0631575289817, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3865088, 399.9730220432294, 76.65520155639278, 65.03673157048607, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5558447, 580.2795611769417, 23.976153824741, 730.0124251225087, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9177782, 436.38465791430207, 74.31217707634968, 525.230749055355, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5554072, 428.0282265640414, 17.724626501263547, 703.2785618698774, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9193407, 967.6019413209727, 13.505155180622111, 920.9711867785358, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4367259, 890.9984984907416, 53.18240956750925, 814.4909940251115, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9830786, 1072.627978806549, 227.5423827081984, 494.1698546567963, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5098787, 543.1431363599286, 10.195329617215208, 383.4748551080824, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7566118, 1397.6865760546013, 192.43219524735466, 612.4918039796783, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2090310, 1402.2850082014509, 101.408198855346, 692.4899099911797, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7516380, 880.6024011647543, 75.01354270622174, 129.90967936729524, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8702813, 1360.5206156165607, 142.74277410149568, 80.77205570128687, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9553892, 423.64017018950983, 223.4759125739777, 959.0670779209041, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2778362, 239.673930529649, 201.82231575301535, 85.00674518855067, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4034151, 207.39683406447617, 59.207162012781836, 985.4434983791916, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6737528, 961.549148849119, 169.06776132281405, 592.6082968780382, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6583581, 188.80892217834696, 237.81290505102695, 286.3486049514088, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8312360, 292.3167379229963, 240.96909578564618, 376.4959813492127, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1370034, 1341.9956290000077, 159.0282836442714, 309.9002966594058, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3929656, 379.3766196187293, 25.7841982587235, 214.60361915630932, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9111289, 671.7630976608241, 284.5102864574078, 742.4386753478988, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4248391, 336.0991434225901, 213.84735815187764, 538.6245310634587, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5903727, 924.9934083742451, 48.72419854527166, 381.00763430905084, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1680444, 136.48065350079798, 127.39548058571404, 293.48894869178923, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3226703, 292.72165974964486, 31.08858837854061, 492.6593025190804, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8911806, 408.08516713571663, 294.9053499229586, 498.39852938429607, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5179993, 952.9080180791115, 151.23665370777354, 741.081773196125, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1343920, 1330.4001839837324, 243.28049589483473, 848.8716509994132, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2393337, 1347.881768682423, 254.25790489916207, 330.53926305144023, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3096061, 1136.1423272928928, 180.01725016515454, 606.9189451888659, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9413956, 286.3577972370579, 228.1836969142003, 461.1293818119103, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2108500, 1281.1986938132309, 100.25221043556851, 189.69775390825106, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6963569, 707.7547520178358, 14.931406776192041, 92.5216071323467, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7858542, 730.6933385888827, 231.25480561378404, 651.11210817353, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8842771, 195.41206803665835, 230.77917412988918, 50.1415911058053, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3481701, 717.5602514649502, 275.2976249577304, 412.77933545256815, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6567925, 576.2908605282179, 263.3889145353198, 655.5786342126247, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8837096, 854.1323651875279, 91.61260277148875, 190.0011118729147, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5609759, 1245.85220030315, 101.55426414741804, 666.0929384338343, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2684538, 777.4715366562455, 50.980411939988706, 48.233129345056746, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9270301, 497.5384082635498, 106.71385823953783, 513.8523330948739, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1690885, 1053.399914928315, 172.30396461254924, 52.357362268438564, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1540365, 332.0621196471695, 97.88883071248821, 740.9202214894123, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5697278, 520.0329640721698, 173.41420025723696, 38.71640306170843, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4314732, 250.6119601099119, 95.23873811824014, 262.1614978593776, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7921770, 1434.1835423686634, 130.52810754865885, 205.35337988721346, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4141420, 1175.5932766309877, 227.19035357896286, 239.4009396180561, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5864993, 831.7570642926772, 263.00928476422513, 91.18750881307902, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8010096, 450.09548280458665, 121.31555627051867, 595.0645892490161, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9830786, 1247.631207759529, 21.527584487225234, 297.45677306478444, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2727560, 97.43147989426652, 96.93649650437428, 592.3971710469676, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3286037, 415.8718279361403, 258.59600262327956, 599.1867540777464, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1395806, 1452.59857745168, 234.5944528537618, 624.444305642881, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3575513, 570.696364309738, 19.991759065724295, 94.94623914630984, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2227821, 911.1741159613192, 14.499807478492388, 953.785803556883, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6017976, 918.8739093241339, 253.21254965930294, 579.7744622222325, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1174090, 1431.495021624359, 127.15499641108731, 993.9738008224713, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3428322, 847.4667741993271, 266.84772402099765, 668.3784382026057, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4883078, 1494.0979045274332, 201.53156800313909, 942.8280256182477, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3854054, 870.3205030000775, 161.23751945597454, 459.07338443747557, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1795486, 1175.237083153213, 256.80574983425237, 647.2956519296415, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1805837, 320.60939472934973, 269.248522741661, 975.0383858107037, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7188615, 412.5894249739831, 88.67179036987854, 700.4756502888613, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2279482, 1052.459669707428, 49.314684084939856, 460.8334768299618, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9469323, 1098.134621142764, 233.44370943840073, 50.20563846257142, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4370702, 874.0681650429409, 45.6540397531223, 823.9039123569391, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7503499, 1147.3271178280781, 152.07294322231792, 289.1847919965913, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9230237, 378.56104609558935, 157.68303183376608, 128.26275242171158, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8580033, 1417.5133276405231, 152.21302024182458, 899.9226482205696, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9413956, 595.0880431893798, 47.35522244462666, 734.5703863728698, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5299630, 388.68504492497533, 208.31057158664132, 877.2695445243689, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8420487, 362.5376340046923, 15.2294042567916, 53.11412427484541, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7188615, 573.9538268400777, 283.8168113462093, 836.6255686778246, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3614410, 118.43856144777766, 180.5752283438527, 357.2540738707072, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8928549, 156.64201454735294, 45.74309026270713, 473.8369460134966, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6660303, 729.2210218295819, 150.4218799680302, 324.2037279073092, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9159147, 290.61903298843833, 76.47279274291076, 0.5672461960480879, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4047341, 1099.8831198687608, 27.27085576871497, 0.820184807550306, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8933616, 1206.5464671225243, 289.8770032659492, 575.1618379958704, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1776622, 361.43008901213295, 216.7147101719256, 820.8063514531989, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4311819, 679.7166592928465, 140.10729841674475, 233.63955606416542, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2352295, 1352.1808190548597, 105.90641968516186, 104.61427246936883, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9850808, 1009.4302875688268, 120.90589343846794, 413.32160056066704, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9154005, 633.608388431888, 65.58803584096786, 381.76613381677424, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4786841, 1440.270042279617, 210.76565121612344, 357.96155375056327, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8932595, 1489.9154831748892, 292.07610473272405, 115.2112292998192, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2900907, 1275.1508668623646, 278.18328005995255, 609.792164369811, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4543393, 459.59145465145343, 92.16205199499119, 456.5321250171753, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9777525, 640.967836184811, 40.45535326680145, 492.4647115408103, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2232533, 1228.7957555685653, 225.1509680944601, 993.2080367029117, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5919854, 187.90211293075748, 233.44955477153897, 92.17848821395779, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9417885, 1077.0832880615853, 204.17619532756163, 23.386254171511524, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8838296, 1041.0207777173582, 217.8000771422183, 56.53601558838817, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7980476, 1128.260582138386, 298.0373686766552, 81.87504374138376, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3060916, 1130.4326097792436, 287.2268847058723, 767.9273903926254, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4429427, 59.83129350024352, 273.286456891844, 470.56200483006285, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3736596, 790.8308905092847, 151.74744893715535, 399.1964049874953, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8932595, 191.19661472283846, 12.047347300525296, 639.0971824547304, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7973981, 283.4181033391982, 95.87685259506385, 124.6377101837869, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5040329, 1260.264695320549, 18.175718587654032, 247.85280945854248, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4152310, 1447.6603407556422, 143.56314889177852, 610.5176778435987, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3896222, 210.28287100778064, 291.97020988086115, 950.362550265644, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9225835, 128.2051210658018, 247.0508386413415, 953.4397196046269, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5324424, 1453.9389759137289, 272.2084761828232, 958.4116328039139, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8819230, 1395.8164531932964, 105.65446483948608, 476.203212956763, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3955453, 587.3316269908223, 45.636732814147656, 948.0837582001888, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7468145, 1169.6723344360537, 157.64897980143164, 625.5723739979514, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5215497, 359.61910998276113, 259.71826026985764, 571.5301563376119, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3472111, 407.1034327797749, 275.47927603765794, 937.9273528804592, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2608794, 565.6999182894242, 87.97790458980421, 832.3333982156338, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9378525, 985.1296442429309, 125.63543377478067, 676.4479905579476, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6583581, 668.011895709156, 19.921281222935406, 590.348227808304, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1540365, 584.2985009328913, 208.29266557238938, 773.6973461202691, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2995950, 82.93421682595849, 265.6549115389157, 956.2044004780662, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1106241, 892.1729001958604, 39.875855433360215, 601.8543223945469, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9212721, 1035.9673561227596, 164.81265073624726, 168.5884051925779, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2718768, 1083.9740883663965, 122.02423284018475, 415.8073615118136, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1805837, 760.5461437829613, 72.75184524308946, 139.5223575469875, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9469272, 1255.4631249392246, 100.85728240180019, 477.74055136019575, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1415598, 152.43167068379785, 185.2894012963452, 736.901399409428, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9278508, 343.31260370792427, 74.68574694017646, 738.7750422749307, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1128269, 207.25650858837162, 61.5130707222838, 380.08287552362464, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3300396, 482.2645495117087, 157.29032626958212, 399.25012586239575, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1564353, 1235.8508089056643, 42.196858552036744, 548.1861546510567, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7378637, 768.0995384411691, 183.2201372865398, 922.5409836657699, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5981197, 575.1280321678337, 181.90646775457776, 731.6971433032132, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3854054, 150.77392428942875, 252.69449721296158, 0.7755353371464713, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4141420, 1107.0458709907707, 109.20154956651172, 179.24604018443713, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9081870, 1353.387041503639, 80.91982372240795, 983.192859345921, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5715678, 1299.136577139658, 197.82862261084884, 521.7973211199017, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7794534, 333.21390553910237, 53.56738426807618, 553.5962288282466, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6275487, 244.82516765248423, 55.83340618675964, 339.94085601630866, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9850808, 310.90338734123844, 119.88617397621198, 47.91861735433989, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4022346, 1116.1031154127124, 170.18824944463537, 236.72890373130738, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3687015, 687.7632703470838, 68.8606821875006, 864.1287417598516, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2818820, 1112.5430739846456, 151.03527263927643, 177.71416067995017, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2044708, 877.0700260721264, 285.0624587981214, 680.8378110241908, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6746619, 221.90433092595427, 289.3564399944439, 77.22426931202753, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7661921, 1332.719306328481, 156.06546685398322, 41.75180358051445, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5350467, 1265.5710709977957, 70.80517629921711, 808.7765557499242, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3655198, 209.88434576055135, 270.88404658279353, 532.2634344717989, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3428322, 622.9587798114991, 60.09616319814282, 372.5689802168779, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7797657, 1195.5708222930589, 98.32435679086089, 2.372657165593539, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9254653, 1308.5991745249491, 23.104932707466855, 318.92349806238786, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9482926, 301.5259496278034, 203.1895655048036, 186.03190320269613, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9011600, 604.7964233109029, 84.10788815370407, 549.1980323536363, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8019596, 1394.3443553888942, 39.32261437892154, 572.9858311881833, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6056507, 255.33062629945164, 110.22159320875531, 340.82821283077527, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8473866, 251.0717340244828, 156.43794241927716, 92.40116271198507, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2816092, 1274.080591798692, 33.59433836570719, 278.5942283561312, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6660303, 1237.7337445399705, 177.6924891900451, 74.84837781439202, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9720909, 1312.697575984864, 256.9954613483763, 86.22685997652812, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6875816, 1197.5392961312712, 110.18028465630519, 935.5681247760076, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3636943, 1103.3198371235796, 179.14605675765438, 308.1568516039511, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7629913, 1097.4975833959998, 203.75326912575028, 390.1122979579743, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8549424, 712.4942716729859, 10.97797805508537, 653.5359222595342, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7687110, 488.6548240509607, 44.335242065957296, 794.1178968763342, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7443047, 211.78273288941773, 124.81734717241932, 173.29633622043428, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2289604, 1402.3224401672296, 191.29447860611953, 722.489830638058, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7228070, 894.7435976592658, 76.84877518549351, 292.1700002702626, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4320050, 501.3123538373066, 39.23093425188247, 883.6736647406058, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1094879, 1307.74767846551, 55.797849112075895, 536.9822893324093, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5053331, 296.51809014722585, 67.53933387266063, 196.71099307750316, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2778362, 351.7699951043974, 238.99720936939218, 287.33894502356515, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7409336, 316.7793973503623, 168.6659574520718, 792.0923516602228, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3000047, 682.2353412687332, 173.09509479628565, 274.4073970388583, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5609759, 1141.1852020107651, 250.8884794013185, 537.9563926972152, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3896208, 1090.2718889591108, 63.42168405345139, 58.060156506366646, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5592330, 1307.0955615124788, 140.03469795651137, 197.02782313722878, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2863744, 1257.8505225837114, 109.98885638840403, 58.91702329784121, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1950852, 1250.1974325530002, 110.30577036461723, 658.2763061469207, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8654867, 849.1962841001247, 286.00272008380466, 883.1055780854193, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9265285, 127.9293040900729, 246.16266659796386, 889.5735486834851, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4822334, 473.594994648621, 34.01758310567381, 192.8527520029275, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8682005, 335.7164684785653, 259.450386808992, 205.76852743872553, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5864993, 872.785201404077, 115.8135876051364, 989.7198428450323, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7486164, 354.74098380519047, 239.34851886531476, 65.51530800109917, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3896222, 1021.6147056682241, 16.20753361292305, 66.86106323357832, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3427568, 438.0976329820873, 54.10005011526262, 123.20138548955484, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2847412, 989.6252185371832, 134.51091385620842, 25.827160764155256, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8312360, 1262.4969692360758, 180.72401774877406, 520.9035621311655, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5324424, 789.4580827055108, 198.74127042301873, 170.37607440955105, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1616428, 1361.055663083313, 96.26866223177892, 389.95794898603907, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4394206, 692.3383388848703, 140.25084669791332, 883.6385127465197, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6468505, 959.3181214449688, 181.26301632743116, 165.23049476971187, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9707415, 764.1251666198733, 261.7557892640623, 319.59236659495474, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4350465, 1247.3351041919025, 109.56563567384201, 896.9275612866452, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3135141, 300.97645685595376, 66.29243414410219, 919.3625927073009, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9807578, 513.9107579006306, 278.5172867026212, 903.3559781328667, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3428322, 344.3821614448913, 107.97162391503531, 602.842476963353, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1239047, 1212.3938845926293, 115.81846918021449, 599.5589881432783, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5609759, 798.0187339910335, 155.48151203758434, 549.101544105855, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8078461, 408.8750566810444, 134.8893966804668, 567.8967969636902, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7994179, 913.1950098700995, 296.4285575578001, 949.8343986977924, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2877497, 991.42531148629, 221.82115525630897, 827.193101439969, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9230237, 1426.1284766465094, 208.53934908297427, 915.3655772851145, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1017036, 877.8801550208768, 172.2805624676084, 478.2533669873974, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8741395, 1411.2962484544682, 25.263605998280273, 668.2813999933721, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5027298, 156.1392382635724, 11.815724591668284, 805.3826026020935, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2279482, 768.3342468261203, 35.110367255665246, 30.516538635983913, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1524478, 1290.4360525306788, 283.6683539432082, 530.6929420326757, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2178045, 273.29793351665535, 227.14685511684945, 102.9969678662267, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3644639, 790.9034729652668, 97.78097660081389, 837.6772323705753, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1630606, 1057.722065945181, 155.10544542624444, 366.564659697464, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8870298, 801.1906912950661, 107.06221625900837, 909.9964244879685, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9021613, 389.5850700407008, 295.8405305100195, 856.3365736925144, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8479281, 571.2228274440396, 199.300561560067, 949.214470588395, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6101580, 1105.6112878777046, 228.14366443662576, 348.3855546126038, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6208589, 1318.3055359150906, 146.09776348406737, 14.613233072675037, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3495662, 84.97635114660348, 182.4529519169381, 161.3101699878774, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8181366, 1314.9988311337152, 229.56109694622123, 599.4968429550441, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1598131, 1166.497326947275, 23.399894544681565, 787.3528441703737, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5684808, 565.6157966259889, 154.97419685720251, 685.7222505358726, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1950852, 1396.5136694161827, 29.487842782165945, 582.3789448823596, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3079610, 1051.9846389803884, 114.3650597260431, 990.4421405383449, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9906347, 1398.5036079162837, 91.05406149914626, 957.0345931546103, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6602149, 1390.388972245546, 140.1951614246583, 833.792628715893, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9707415, 981.281050591525, 131.6138018608204, 109.24324102514471, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9553892, 1310.6823368498015, 19.362813524617124, 425.71032198403094, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1682824, 326.4804551613882, 190.16515595959976, 491.124923385853, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2237331, 960.7933962479887, 226.76438709154277, 261.20749282305553, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3137618, 1496.078126973626, 167.14913129633374, 216.36037564935828, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6938054, 1385.4246003164417, 283.561226161357, 538.2016057714486, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9469272, 1233.165277005507, 216.6741454616651, 616.0426860261805, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3525105, 1497.2173396043272, 290.07703121178844, 386.72033693449805, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1997341, 807.3364730877553, 148.75458117819238, 562.0064587291381, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6353762, 1117.1469502679745, 241.04031085045875, 605.2155962221766, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9707415, 452.42412077209275, 134.37488626012566, 773.7610188388625, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2327376, 1019.4143739251613, 15.677057782243356, 98.3488812175447, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4417336, 962.8164637798836, 87.2919030615478, 277.0252430953072, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3381353, 376.39450651598844, 279.7140101119993, 330.22282236695867, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6484248, 1277.81115794906, 115.87482694781322, 242.0285601434452, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5068903, 799.7355517055985, 284.76617874486925, 344.89084892147326, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1375594, 1035.7268244336342, 92.6366195878634, 778.9178563125361, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3501854, 1150.4123242861506, 14.658979692639743, 52.82426962048004, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6927298, 844.8035881977526, 215.27502200936928, 758.0432246608358, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4314732, 1178.9723039659857, 171.73395292023795, 760.5580965772368, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3674035, 576.4933436141727, 162.5748060065561, 98.18621253083614, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7838776, 724.9366555287509, 76.36065432054396, 299.1402377612206, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7730782, 1312.6381812371533, 179.74956808458953, 76.92840355269703, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6312080, 1142.415943608146, 46.68538181649023, 424.13829916948606, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6776605, 366.08333994857725, 263.8425663001037, 414.7819712130043, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5672855, 556.171396379851, 135.5883392477876, 246.00741880854838, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8308581, 282.5481345337521, 124.40400908225881, 753.7446146007939, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9290818, 483.1784583079867, 196.45783297196644, 319.02707296775367, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3924841, 257.51085765132257, 267.75817736294516, 42.722281097777866, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9302170, 1367.1968186960792, 215.18298219241817, 430.81785021489947, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3316376, 1154.6742719880106, 131.61601533771457, 906.0140155465372, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9911328, 412.0032512788252, 198.0039137165891, 940.1991368088446, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1680444, 641.2616050169979, 220.0850348330569, 669.3876474869057, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4883078, 478.9041276611535, 288.7844282764419, 778.0986001809346, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3951200, 781.2948211901212, 298.2297579792634, 724.597065151384, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4075450, 336.07238740886544, 164.74071224955588, 772.8692365697914, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9710194, 125.42052518096494, 12.782702300244114, 124.47547177841633, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7829109, 288.6146741105679, 263.26209119521695, 267.8503547816281, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9154005, 377.20349150253105, 256.25002867556196, 618.8414928578363, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5179993, 665.6161698043501, 126.39302913120369, 377.079984300955, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7194577, 282.74915521244145, 106.37592888034948, 530.7373169703686, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4070724, 1332.9077975730202, 130.00408185787347, 271.3377415965623, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6971793, 671.2581926129803, 176.92749628062708, 154.2289735507023, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1260394, 78.99628139318747, 115.69576570513601, 669.2389319938937, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7165585, 320.2963206927647, 210.3587756144472, 51.06481880561853, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2155577, 995.0669790471989, 106.95333217255192, 629.6952550063945, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5248538, 816.9842513220093, 69.5104456673723, 932.7404856531771, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7081554, 1045.7769650509824, 135.313408403553, 740.0137419391472, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3932120, 377.12703494481667, 196.02254086313656, 677.9125497000468, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4294251, 1158.8069991011744, 78.31983762055128, 867.8451360021097, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7759751, 1374.5314550472804, 297.7841856146768, 492.1514636822786, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6427746, 231.6552765003414, 81.21941353851123, 769.3459709417785, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1408218, 236.46209895490054, 74.08235397656274, 30.614014593831396, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3004616, 1382.3813343729444, 193.58969385940244, 996.0264508101475, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6489369, 377.537137824977, 60.23590351171526, 720.635684727929, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7081554, 1062.801579781848, 287.18375692427753, 598.8896645164803, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6180429, 76.91877998287394, 204.92341208983353, 514.8166701912032, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7121565, 1155.0569300781008, 228.86878766051072, 542.9745676330318, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2863744, 621.1972398455332, 16.519789075668236, 911.6681800963505, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6952731, 844.8187067007392, 78.83373395837303, 283.8427191828726, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3914841, 648.1503067953764, 232.56636859801404, 588.5644948697088, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4070427, 611.5006812030872, 96.10167403084287, 544.3605759723179, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4671508, 1161.6411656077928, 178.35083834837215, 474.2545854402371, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8565031, 59.710991038414946, 92.33822528959892, 472.0468625816099, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7062408, 823.2401439287081, 115.17907659347595, 429.6861509373933, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3495662, 865.3398190356244, 180.85214521857688, 442.49091783131666, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5600563, 1381.5556273215218, 143.3167046997603, 170.23992333975679, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5329863, 117.89711895706243, 174.37295450535913, 406.4781177171277, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5552236, 457.7107416742915, 164.13415100384555, 180.57961781623965, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6353762, 1369.600295311921, 97.0051377461367, 115.5927586352874, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7477967, 128.11187502633055, 167.17870920034667, 338.77288968508424, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5126039, 1444.271640155738, 253.96237108217525, 388.86339814203666, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9041274, 1404.3720448506303, 99.08528971109799, 583.0644654334583, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8888764, 356.1145546701399, 79.53134197247697, 749.2343991064548, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2718768, 188.43934616998013, 133.0553212463545, 225.49017693430784, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3501854, 249.82961648809436, 110.95303170180752, 225.21487080223866, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7342857, 307.36434459246846, 195.98966424678244, 485.19047933528293, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8910115, 1034.7415249746455, 157.7364034718858, 948.4699967502949, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1063281, 911.4517296914643, 14.726779883732062, 848.5056318603903, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9707415, 148.34908410677835, 60.13148237007955, 250.60262200901994, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6686020, 157.80699878630983, 290.08057476100726, 644.6234306176482, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6045324, 1126.0906258574848, 226.7836377357209, 735.066904038643, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8293529, 471.8428189132408, 161.76680741920705, 592.2337029018697, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1240720, 1183.049316239697, 107.95317792252499, 146.78018829680062, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4929294, 411.8574625686866, 27.413405714947615, 239.47174267357784, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2212754, 565.53072538163, 38.00693107165219, 564.9419868953968, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8535453, 766.3630626108928, 21.401592338069136, 197.16275584315136, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7949126, 467.8516418682143, 274.3593496697035, 822.4375678268045, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3001561, 582.2010167340322, 71.10348387319692, 188.91342512828868, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2900027, 434.0464445127639, 268.8130211342091, 515.6079573862452, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7437946, 739.1275953042442, 46.090240167493434, 429.1289787387133, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4786841, 1497.601854153435, 113.47114063662838, 508.8181112361535, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5248538, 1219.1775032374655, 69.26227932834317, 470.9416046424624, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4370702, 730.4126838221972, 173.92074942652005, 886.8023966190817, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6448352, 1092.8138088358521, 241.11593925215823, 826.6323031786457, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6208447, 718.3883656294621, 72.9107679257496, 797.9454784596287, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2823971, 851.224043736203, 145.96815206388624, 473.90772994883315, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4192915, 542.4733112617303, 248.45564447454262, 813.3860399861343, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1094879, 215.74779764976824, 239.95693997161013, 431.88282668718415, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5873348, 458.83161336434677, 243.6507979633603, 609.4494737606929, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2044708, 471.5831533261713, 144.21026347058495, 852.2894900076082, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5469436, 521.5420275870594, 263.12553742593934, 577.9100243798337, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2955636, 1011.2658370623666, 159.12767052912398, 911.1919240807473, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8751110, 1260.5240271781236, 141.25216305672814, 169.9428389849229, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4125206, 541.0959008888221, 253.37868682508463, 586.1782500848346, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4152310, 531.3404923680621, 295.6553041179635, 183.87806637142356, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7154320, 227.02574116425245, 279.214754387606, 926.9256421638545, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8757277, 837.3114249915316, 142.14568279404503, 461.0651367414934, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1599267, 1390.3840906935893, 128.28546068032296, 728.9520105096807, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2352295, 266.82093201580506, 103.25092010030083, 185.4376359538723, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1112634, 290.3639417913232, 36.66147135081458, 236.83805603953434, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7403766, 1123.0617076703847, 223.24452120070677, 379.82630105095706, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2477578, 1115.9669825196074, 46.297789462706156, 726.471099436357, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2273239, 1172.1941227559882, 72.91144539925382, 519.3447266071742, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2352295, 430.8322021906438, 263.5101039905919, 659.1931696644132, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8535453, 1258.3216946492173, 122.83591476625865, 769.8545475192357, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1950852, 630.5499129668816, 31.627398394341995, 748.5797158512879, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9553892, 492.4925379897941, 170.07497437452687, 180.72188633557306, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3932120, 1397.2104256727753, 176.52686423841627, 260.8876238699422, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1487526, 315.683776508775, 252.07958908420142, 263.69151382340806, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2684538, 1381.3425439310342, 228.9931575879806, 227.4260980683256, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1467620, 308.7115330632333, 62.54763592117749, 732.9033540256236, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4052570, 288.63552567425813, 105.62264621452759, 913.4410870261296, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3915653, 385.67498241974204, 286.40827314249753, 46.89432552995798, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3932120, 803.3952043418742, 294.887253267647, 165.8808906469843, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9876218, 1420.043844990111, 47.17181027725769, 908.5351943586228, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2090310, 426.9337702685041, 177.57485090829428, 358.9574500729127, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2449163, 960.340542523647, 106.33876295810693, 544.3713232178787, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8304737, 306.4395162952498, 254.3131066808085, 207.33011811251168, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5229706, 743.6039273636912, 122.47248590356024, 888.5809844602586, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2044708, 1101.5056960691072, 73.2604043045658, 685.5623408417562, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3575513, 432.6645983228764, 201.61668193154424, 336.20290374672646, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4275721, 1060.743714365955, 71.07653433152012, 404.24149889732143, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4311819, 189.4386160685151, 227.95439289323443, 290.65049658583695, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5133949, 1369.137109362979, 293.64944925217134, 978.1710699375121, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9068057, 178.9656569894622, 277.07288735432775, 143.61517814820846, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5773547, 407.7796475363707, 251.90539549893094, 470.83161962442523, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7486164, 746.0005602930977, 141.0407844834861, 488.6667971494805, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4314732, 147.3121155762105, 92.62042429622328, 981.8050105666345, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9954294, 1127.9229845240272, 166.62754183398627, 445.6048132870869, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8326711, 90.41363040217323, 288.01296209266064, 857.0539230428255, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3655198, 1174.6858456693524, 105.24660542746999, 81.03384002785829, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1997847, 809.8364419607577, 212.06431300044807, 703.2742075591692, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5697278, 940.9796281899619, 135.5606433772922, 597.2998519069049, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7443047, 549.5960764507431, 282.41025941957884, 477.72119747078267, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8575372, 170.3514738662682, 285.28461093118534, 255.6377918583953, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6689874, 1154.4804511575796, 147.10036544630125, 401.1461992354339, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5350467, 293.19651260792705, 179.32226884650595, 668.2701316241056, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1605045, 176.37631536207493, 69.56224214892472, 224.0448302569974, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8272495, 219.47304422027526, 43.75792132969933, 912.2093985255423, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5894239, 399.6172851655639, 123.59992413428472, 673.106396117091, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9250912, 874.4302951585269, 195.566300655553, 277.8211053016059, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9302170, 191.5074857648105, 28.862039851305376, 115.22651375981718, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7486164, 767.0196472194972, 246.06692682984178, 514.5883110055354, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7979720, 729.0677498149996, 58.159166248856906, 949.7689962066393, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7487130, 193.6305162485448, 273.93990168589045, 957.2374155253614, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5542530, 1413.5792553018955, 83.40337960139551, 565.9349670609122, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6995828, 1014.9931793247277, 24.1139707639778, 910.4150573844768, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3135141, 331.2349191305063, 285.92266504529687, 281.51055587426566, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3623130, 545.774305515462, 99.91475834130327, 372.4771367966959, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3924841, 766.4050603304096, 77.96468269973406, 362.9742515272978, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1707120, 1200.1121641662417, 291.4499395055218, 231.4323575624997, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2825950, 646.2871750167394, 87.45433997958285, 71.50268093853596, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4261383, 1062.375939136482, 233.40184220184398, 217.2887791681569, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4366043, 1345.4816404237533, 85.15075077435158, 380.3077867467084, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8299339, 176.89175573450996, 283.06711231707106, 962.4886748857907, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3889730, 1143.870582681725, 96.17296841113816, 339.824812046853, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4460683, 834.2748184930313, 215.68529646405324, 94.87977925971968, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7223505, 1153.7246580735332, 65.6435549652029, 652.8442481899638, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5389195, 1483.1775062317486, 50.82867061699392, 30.26398389767071, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5179993, 910.0352885587755, 183.89470117239304, 893.153037801829, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2825950, 1376.2427264116936, 55.21874627019972, 323.8599068715665, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (4836866, 379.2151433633787, 252.53964026792426, 932.5451963571222, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4735169, 295.835900417072, 65.91112517694029, 122.35133744623205, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2910376, 365.96964578023085, 178.92705871857447, 765.4872606202723, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8933616, 343.3134448128307, 200.4431145726486, 973.0688507749521, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7152578, 710.8782330466292, 270.6474272212078, 906.2151603814999, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6889635, 1017.5313799701793, 265.6279616231741, 332.0879607550603, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4498742, 723.4806890301643, 252.05626138228794, 272.42191369977854, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6737528, 429.8044044758484, 229.08915686232385, 477.21888107515554, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9747931, 708.7168817929647, 54.24339671936849, 718.3821593092855, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2103439, 1332.9731662038519, 192.38368831375053, 478.6831524352311, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6583581, 74.27534486149133, 122.1768895312871, 146.0596230058827, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9081870, 450.55618511004025, 84.9792688660508, 282.573464958922, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2825950, 139.3100125055377, 121.08528338865828, 643.9949990841761, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8456555, 568.449227190633, 206.32550896645458, 240.1163388736093, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3736596, 589.5188454271415, 69.88925311432091, 672.4658200438271, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9918406, 99.15033938340426, 75.76552297666208, 13.257472098265, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2518875, 387.43778756723924, 17.68191696784236, 166.7053579885166, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8124177, 796.7938634892156, 176.49478777146857, 519.6331226476821, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4822334, 983.7807682207148, 191.8208476923328, 129.78984313830765, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2820535, 895.9128499275413, 54.68628352762173, 34.33385372890041, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4109407, 424.73314866495394, 275.95984776373314, 732.2815391626339, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6737011, 1295.4895748422318, 221.3332827523556, 436.0360392620907, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4338621, 534.6684306562648, 198.6717660396659, 452.75211306167216, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4680410, 1498.75953992981, 294.370997648696, 65.22553740269332, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6686189, 868.9715077950888, 16.552098145302484, 696.9703299979548, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5609759, 1381.1290424913107, 145.2426041767352, 180.93275375415885, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3398648, 656.5362830940844, 24.516657491459174, 768.9501232685856, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3935854, 888.6815272497824, 99.21533790481135, 53.53946290580824, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7797657, 567.0656308734489, 247.4489805245679, 523.4861526187171, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9710730, 638.5360039663756, 46.386360146770855, 630.0768157989141, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7700031, 526.9995138528766, 206.64150725427015, 410.09274205867007, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6560390, 392.1175122040046, 214.83488226703943, 153.12994558942484, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7030622, 855.4658490988127, 172.8652722132841, 38.72470502890768, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9482926, 464.0510746844992, 141.68794322179045, 838.5747179577552, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1580588, 550.0851325973827, 126.13946275015383, 27.591753835176537, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3288648, 1415.475731185779, 191.00037069857913, 468.4632935169956, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9633280, 526.3493896548842, 286.21124003899644, 970.9636599355263, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1408218, 621.4368884581758, 125.7056739282179, 58.8780227589264, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6056507, 837.2979226962872, 289.4910873066035, 927.2506791135357, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8023418, 401.6871226593764, 60.99482876654948, 817.2164010371104, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7296906, 216.95189696093593, 221.1419457144331, 400.99807464444723, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7435895, 536.3525563126288, 286.30558994437837, 363.11482253280514, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5894896, 447.7737060737834, 89.14748817630091, 574.5241644708133, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3915653, 323.65379854820947, 105.38099785524369, 406.3922951507715, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2186206, 1165.5974149353256, 154.57250003793422, 61.07285643690397, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9610175, 1228.5697470866144, 98.6761144424089, 885.8810800792754, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1540365, 1362.1950259678122, 271.7490733124445, 2.7422384623930762, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3763327, 184.70554159913064, 22.840429883031156, 379.97632523727873, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3300396, 1284.687257247754, 253.8227425959932, 886.7511395902783, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6191290, 877.2645999710448, 253.7663092669471, 147.10537296398408, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7687110, 1438.4602462946261, 292.55446972177464, 486.11097797537326, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9553921, 1029.374559308785, 126.72264914421064, 101.89901351970288, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4125937, 1187.2557692929738, 106.54432728621141, 190.3233761652755, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5179993, 1281.6683671035694, 56.801493848171084, 425.0002627315921, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7784582, 1494.9345808883825, 57.22836494404106, 148.97961203679944, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4136571, 1425.2547205594346, 106.9007418521226, 576.5002730183817, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6325239, 811.2349576674148, 37.16177232142997, 653.5321861323617, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6938054, 878.6722519696204, 25.956718097839953, 252.7879437856031, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3607319, 1135.8720137474118, 96.6536522110986, 535.1140567328215, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9290818, 676.929835309618, 73.07558451044756, 377.74205910765033, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3137618, 211.48435556908467, 146.2892500061305, 691.9529367764643, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3796609, 757.5479437162791, 123.96520391718691, 5.380507886681207, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3899871, 112.4337655893506, 97.62702253490207, 819.0306734582331, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6845883, 128.27459834044936, 115.1410262965072, 215.59271318412743, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1599267, 417.03771003848976, 98.52765666790314, 399.33937590701044, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5733796, 367.94218406442144, 286.28785384067544, 76.01690506887537, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7949126, 366.12280531927144, 155.935857401538, 482.30082823671137, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3761665, 158.47788755126544, 63.30904577011942, 326.4146208368909, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2653273, 714.8424910814177, 38.51559912670085, 366.4266593818368, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9710730, 249.83741045060967, 40.621354737956366, 865.9975034358822, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7566118, 1346.9317915841355, 217.9484848619614, 106.18072011212153, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3161605, 1309.2102798554522, 99.89603545897769, 162.94319013452696, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1950852, 640.5266894616421, 38.922823449233505, 800.4769755242088, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6284564, 168.74770234145595, 141.15464936285414, 350.246162735918, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1285035, 1223.597477193094, 218.47998874056847, 393.3754280565801, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3658943, 549.4207307606646, 103.85270737051232, 80.11491259535607, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7089480, 598.657728252182, 224.33039407503207, 36.4310453660095, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8157454, 205.63211758812383, 221.54975237459684, 206.19928089630056, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1174090, 940.3349300297102, 254.15312231734592, 513.2963330983696, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6039534, 899.0404494172998, 273.05968613165703, 670.7412162902615, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1987473, 991.0815477501413, 35.49212135531117, 954.9971087648847, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4906996, 986.8957496505221, 188.89738084645285, 984.010773181864, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8114347, 1218.0161371797265, 280.1639617083353, 113.38760705289307, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1343920, 920.8089652487255, 177.4284380042065, 899.3928397228773, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3087375, 1439.6596999226178, 92.63229180494434, 835.2935874316697, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9979672, 1259.7374656826628, 298.843539615101, 516.6106704389492, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4125206, 956.2443343178393, 162.25364660009208, 917.1921027178956, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3264931, 592.8945269887932, 214.96833426052797, 545.2646431969308, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6634468, 1297.1428036220452, 277.72773943244835, 210.7682012118156, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2471375, 1360.1210011939258, 168.3774165272104, 463.2269447703139, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6217534, 1252.1079096464432, 93.610703029323, 848.3524483851381, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2155577, 474.9009872981254, 61.663729953352245, 970.9541971172888, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3094275, 408.21641908612827, 216.76970081139478, 567.0559566293321, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7934364, 678.0870368821851, 280.65574396277117, 5.4785235028412815, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7463436, 432.1699998475243, 58.473690672807784, 999.5393414649291, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6188232, 1337.1537015052634, 236.41387455719112, 832.5614100124217, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9146355, 919.2015512005308, 169.90151699885124, 338.40469671887917, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6875816, 1421.2687321359308, 268.76395435783354, 634.7212585248691, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2259221, 330.62025456801626, 11.126128030202855, 287.01720540818866, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1690885, 463.3999891017726, 18.92250356814181, 604.972281031274, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9610175, 1182.8762391332036, 223.51489036747287, 259.7674335777436, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2472191, 208.92792381559, 297.7112759038174, 136.4196423863112, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8305182, 369.0382039699687, 16.241481506464833, 868.6861911883622, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5248538, 938.0394577309503, 145.76551793434723, 8.916082056223962, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7905117, 1157.21580711747, 228.52512254411252, 547.5565810848975, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7477967, 1394.9034214230642, 284.3376650290945, 935.3190240461043, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6484248, 1188.7513214872167, 296.1219070094836, 59.421329084090814, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9225835, 548.7642430579104, 131.62262947067842, 88.6866372635451, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9186940, 1497.4668597868936, 156.73887623305464, 132.89985024508533, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8842771, 838.7152424825277, 58.79953773897615, 559.2717772945479, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5176208, 909.3302931834397, 60.70534833250304, 512.9660359618689, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1590258, 1363.3722248964152, 175.8019338370157, 264.29034639155344, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9111289, 312.35317485880023, 297.57938526871834, 513.5399384222718, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8819230, 1349.2369492740559, 87.86372286268552, 863.945778274843, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3763640, 1067.0572969561838, 262.315330512238, 864.3118896034043, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9159147, 309.4723227146438, 26.86705525805254, 691.9345595089634, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6598224, 747.7498895433149, 226.05863070476505, 878.7355050059766, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4929294, 580.8597046579241, 299.9211934545212, 398.2079113422294, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9561074, 958.817134209817, 245.6986830006241, 248.3206097533699, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8846685, 384.0988286641101, 97.34245930442515, 673.6725667760904, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8565031, 1319.2091813373036, 14.498427791709943, 557.9389954949459, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1266417, 846.9485363158658, 221.91930326461954, 361.56242139568707, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7081554, 748.3304606095267, 264.35890192824075, 334.65525722581333, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2720614, 508.97119038426547, 94.35920590527219, 650.5092355212535, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9532390, 790.1005695871704, 219.98920494062887, 390.4404445327576, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2428666, 1153.6539594749668, 261.8710155847009, 603.8340484790172, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3812530, 755.7247183599393, 76.15763895298987, 45.04041573446671, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1707120, 880.616248170735, 275.3473661315195, 32.510631053368265, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (1247895, 942.7867411695864, 207.31338585145753, 17.584509780319447, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8439361, 71.06438264098657, 172.49899242604815, 322.1935907988549, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8299339, 182.47550729227095, 193.16550972692787, 783.3912251368585, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6619749, 256.6895977189833, 124.26317255004265, 113.4028135123537, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5165799, 331.6473552288629, 63.32697522448803, 271.3875768896046, 'business', 'USD');
INSERT INTO ticket_prices VALUES (7921770, 191.78853764810665, 97.95463719717372, 302.17257559184986, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2108500, 148.28968811887864, 123.34733820820554, 377.93590613359976, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1737402, 1355.8814684939452, 184.4084207642982, 160.2483689371137, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4022346, 845.1234815756497, 151.08685582021886, 172.44944444345268, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5126039, 499.2021215416358, 122.98326732521116, 859.804195701213, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1803104, 740.1595976251875, 72.0494642852293, 498.1474326596053, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1370034, 956.0204535088063, 58.667243599527254, 3.3222046975459607, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6822374, 1135.4328699903297, 31.14989687935403, 343.53307668035114, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8018635, 161.91917236098982, 162.13563429296104, 637.5644743848566, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3317520, 1233.3922357668475, 290.362587593368, 541.6138607689956, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1351333, 779.6564344572464, 282.7015866463949, 348.0768115022953, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3224879, 1134.8492919638388, 187.68754578013227, 608.7767425115203, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1142136, 440.9207253162539, 224.82151710034788, 839.2978856347934, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (5219857, 681.75529350334, 129.4683072710862, 627.4904769413304, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7089480, 1383.3178506240479, 227.78355763027477, 664.5050334798896, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4541509, 1238.2154046511143, 56.70549368022629, 351.00162438637705, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1707120, 1075.6293260726895, 39.672625623398574, 126.93892931479445, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3328093, 464.8589647965994, 103.49789735187031, 646.0941219971041, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7629913, 409.64465446299204, 191.53755004219965, 345.6433702980598, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2418632, 384.3463679633559, 102.47808417020684, 446.23475785272603, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9747931, 558.9571681958503, 171.08469872923413, 163.54818962964902, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1038659, 67.53119621415482, 122.10359775533851, 179.082572534362, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4836866, 1221.3200911115346, 55.48937286521216, 655.1588954219601, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2419947, 1136.6799067392762, 131.6420658706154, 358.52044333865274, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2352295, 472.77862762745787, 234.59792818450822, 685.6524662094388, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3096061, 446.597982750494, 192.8539198039347, 720.7062494439858, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6312080, 775.1850265585655, 36.64187868160991, 437.5525389828242, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5303671, 764.4250313549427, 80.76150926557915, 338.17253669426896, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1769331, 1491.9168081099187, 167.9078224844904, 382.87648809371314, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6180429, 396.814366546505, 28.451918948977, 11.152306688983371, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9911329, 440.0251774259565, 242.5185964778313, 413.13461234458015, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6805911, 59.99003040064035, 189.5619435207113, 880.4583516043831, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4541509, 809.6856714836639, 120.86342000799635, 210.5666448652157, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (7228070, 187.57945409389677, 283.5332945008146, 898.5808184862993, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6427746, 1336.4335241600888, 35.325569977306316, 877.7889284970344, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4904785, 206.4874745903947, 160.91075804238974, 386.3832712309713, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3929656, 1160.9073175530823, 144.8362850374745, 667.2340593483227, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7566118, 781.5378245506324, 160.22375736278224, 952.2069633090239, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9902612, 1316.1135965327398, 165.18141087780845, 691.6901826751186, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8029988, 432.85273906506796, 121.20240079794084, 235.70040677188365, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3481701, 560.4836165116599, 296.7488862909138, 937.4313245912937, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5100679, 232.3670752815028, 172.7615813848472, 652.6036031528652, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9955973, 732.5261505716942, 219.11338218650886, 751.6104677035366, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5350467, 426.77648371806083, 239.0077704244385, 928.1077895362566, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7223505, 239.86969473316643, 144.82991405094805, 185.34049791826192, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3896208, 1420.754561086657, 78.73598061642831, 421.6940564180923, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2178045, 987.2643366768676, 252.68218277934872, 297.8513949232238, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3634055, 144.09487626573923, 56.57002749006664, 761.7355754873983, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3262717, 1471.8975751794544, 187.08593134163038, 883.7535082400809, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3899871, 658.5814730536977, 131.50152948242078, 948.95516720289, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1761602, 964.5985859032534, 27.561644586552976, 353.52206057372825, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3342522, 269.09440696447945, 204.28972039488505, 854.9686977865819, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8694998, 1418.391119492259, 119.47729021484345, 592.056332937792, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6479012, 1175.005393846829, 134.53308912553473, 604.2561381839629, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3674035, 1067.9839687205003, 254.2358453736321, 369.62257904460336, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3428322, 463.09457757777756, 204.1748403542535, 408.2072095210064, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (1060597, 319.05325114797336, 68.28179083594755, 917.2981945063384, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5981197, 123.52030315608405, 270.3891142238703, 369.8002084038926, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9907707, 520.8469136515629, 122.08455009155082, 604.4872997869431, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5219857, 1020.4008287031469, 275.8324377145396, 11.380446559781433, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3070024, 518.311959848145, 85.10933976702754, 749.3886577750945, 'business', 'USD');
INSERT INTO ticket_prices VALUES (5125411, 1135.3756915854742, 294.7709894216468, 231.55515984072883, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6065249, 1120.9894702047834, 240.62503337586898, 531.5308665885552, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7152578, 110.468110088103, 208.90616522156014, 391.0295014636899, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3865088, 1344.4545924794036, 243.63171486824118, 501.7014069589316, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1694833, 808.2104996290914, 118.12598350656073, 779.6237896515814, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (1090155, 1111.2430403967928, 231.4654483171514, 999.0528457576454, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9945085, 232.72351248896712, 148.5662882617151, 876.6610215709574, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (6131014, 215.0154153693931, 212.2385746457606, 314.46736351678715, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9777525, 1471.3502461143225, 248.8036729928456, 615.8660076876129, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8029988, 171.94151579854127, 246.49298662445696, 850.575958418713, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (4394206, 1038.1596294781411, 195.2645381086472, 413.71243505387844, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9738543, 1025.6397149733998, 57.54345695505887, 88.60820858536145, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7324679, 813.9448472934539, 221.21199700950245, 715.134382924412, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (1641826, 303.8601314193576, 234.71322611957072, 483.13974100482824, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7891432, 1150.4825685206613, 205.8539015061048, 17.826546556191758, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2471375, 1085.8205953865106, 50.01759973298629, 713.6184571294227, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7823393, 834.7595093027543, 236.15694047550912, 995.2173917365077, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7240147, 185.57150033636884, 238.9623155752174, 586.5192895998049, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2823971, 1074.537014231746, 221.30751835370842, 23.465108792982804, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1240720, 1451.8633518366814, 125.44756343337993, 689.466411604184, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3135141, 1021.8244303333097, 196.79833649035527, 375.4354916804249, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7976370, 1323.9503842105175, 88.83555208262995, 350.187399467343, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6784869, 530.500788927981, 265.4318880146965, 427.7902353605671, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7211041, 243.56652148309163, 65.0355494117182, 188.0853483299536, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1757886, 79.17369776007496, 40.45229988422499, 574.7161238568299, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3382167, 867.0014351671291, 143.31092085707013, 500.53798935694596, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2383563, 1144.8729236782776, 42.54625509429796, 382.8049989931645, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9911328, 690.3875293829425, 88.76802767018341, 604.381511238427, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2653273, 126.3548652060592, 81.22474813362714, 159.91227650579998, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9417885, 1314.8390191176077, 110.00493174091874, 167.8245384940431, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4992028, 719.9054183569158, 294.2743871397042, 523.3357343900869, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7379762, 930.3223877563184, 188.23254699613076, 676.1949828486858, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4108983, 1181.6495049130904, 126.61750332239632, 9.55616321538999, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5349444, 312.40824142096614, 24.790169069241898, 314.4242837570923, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6874537, 402.54332909472674, 60.995281489289404, 824.2427526011155, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (8385725, 619.6462528008446, 139.65842514467542, 177.40410593209677, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2833716, 176.30032417551178, 66.08185089202345, 835.0214472415266, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (6805911, 1343.3098691398643, 38.87879587401471, 367.3516679577975, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9193407, 596.1689982451524, 289.8765223592908, 338.8929508860439, 'business', 'USD');
INSERT INTO ticket_prices VALUES (4218078, 1485.6646346895507, 179.33502229185729, 335.6812292032647, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7463436, 615.3301564788366, 268.4426549018762, 361.17124766217523, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2463676, 1283.8801753060293, 162.37902620505707, 31.739721820110866, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9205451, 1234.9046855738693, 270.8863334216018, 627.7072398660525, 'first', 'USD');
INSERT INTO ticket_prices VALUES (7748282, 926.5203730039157, 20.532795101677284, 45.311897431755455, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1769331, 553.8627907921446, 59.05696076024186, 826.7777868849538, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5974936, 1403.9320579037656, 103.61235153510074, 921.7072113813433, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9730753, 1341.1976274046876, 255.26887549492525, 332.5717726292087, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3854054, 258.43889369619393, 85.19817974395093, 157.45357893013534, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4845098, 339.8119216523222, 179.08947554318715, 46.724828380294014, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3899871, 361.19939502799195, 154.97595686606854, 522.0489999041939, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5592330, 1273.4671058568929, 250.0523641570096, 377.00495594559146, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8911806, 639.6590062143806, 145.1035651637347, 950.859740862686, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2428666, 730.9608657746879, 293.841202780395, 85.41851216746865, 'first', 'USD');
INSERT INTO ticket_prices VALUES (5529625, 1357.7400174557483, 70.6366674160221, 358.049440893102, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2823971, 999.3124026184771, 290.7567562485278, 284.6804684048787, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2186206, 1323.7839625715471, 279.07579108760496, 358.1902238966054, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3865088, 998.1482387263566, 176.45049161588219, 171.5149516223552, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6896768, 441.1194796194829, 86.43814435721671, 828.4144467139572, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5573187, 911.688869181818, 30.15540861345111, 27.03463024402597, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3634055, 214.1286197145483, 70.73788872281541, 322.1107784178768, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1707120, 658.6719156125724, 264.2451406106678, 2.9014088812936434, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (5544411, 660.6683503050369, 293.3559537360905, 228.5346731774861, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1618469, 1220.7170110049221, 294.9200834730508, 815.754434601161, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5288135, 250.08818757883813, 81.96988348582181, 308.7718312989476, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1493138, 193.3613700195288, 84.93009352130413, 570.4665566621198, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9851914, 664.1206366502133, 130.30125503591705, 757.9488581902355, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1660759, 265.0416235876335, 256.4401915777624, 945.4028115324639, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9154005, 781.9074639246239, 299.27506450549294, 676.599250115124, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8193176, 1080.880546938704, 193.6643622415127, 881.1274579454155, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5727927, 122.71656518884149, 268.6885370475962, 717.641329585804, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (7785771, 547.8272563531, 35.814452545474026, 902.4071310964699, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3575513, 1469.7597298207786, 143.6958538184756, 761.1807270019212, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9845042, 1412.0497155395865, 268.4110308771469, 875.7022848965269, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (2232533, 164.75530623005181, 143.70465225767026, 674.1327226326952, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (7789828, 1426.368648491339, 30.451591833166322, 962.7425035686864, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3161605, 1479.6660752540267, 275.4104798348578, 457.30241029546113, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1367157, 1359.7995854115545, 66.22607226736048, 186.61073943747354, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8156954, 648.367099222292, 281.2083025433967, 433.051468932829, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8928549, 790.8139309099529, 41.566628500238835, 362.5229932973065, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8565031, 411.0272326344903, 296.59378457292877, 641.6875643165304, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (7211041, 1438.868489749977, 27.131465450785697, 728.7232585763086, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1218055, 599.5143640371953, 178.61636960346456, 759.6503783765456, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3967442, 921.62669920128, 231.5311214410304, 992.7556820774313, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (8090201, 597.4664291831576, 294.3317360412273, 90.94533748989153, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4853826, 528.0224968838165, 238.6265735073613, 690.8015832861919, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3024382, 1365.7622862532828, 76.24335015422898, 597.8741776433247, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3286037, 462.9719151127256, 229.92254303401927, 676.9283181838993, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8561208, 1311.1394128461782, 109.04146398778427, 464.2866887165481, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (2816092, 670.7539097372105, 250.02480363023543, 556.5356181140173, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (9175129, 176.5687898121347, 74.11543246382166, 682.0445190876912, 'business', 'USD');
INSERT INTO ticket_prices VALUES (2485628, 135.90798823660836, 243.54364965328728, 63.580046760790765, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9177782, 341.6315848203339, 170.39044720431872, 476.3618367147314, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7522690, 662.2943146778875, 56.51364689387334, 575.5206153482472, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2208818, 684.2423411959322, 179.0755583388908, 326.9272871074721, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8587892, 1266.117845544225, 40.97007665236103, 558.4543630615144, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9740986, 58.65149156384696, 109.32352916305653, 985.6460737978783, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5697278, 359.84221063156883, 206.59241856704142, 168.82634298665943, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6327798, 216.53834485834957, 274.0975120829513, 969.5559029581643, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8737883, 916.2311296072533, 137.7562110430113, 934.204108180601, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (9912794, 435.30343308014756, 147.8474701075495, 499.99102201042047, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (3751315, 345.2728322254877, 132.21743480693183, 712.2317730609303, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8473866, 1248.6578850534631, 123.68945130322489, 661.5531318678485, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9220137, 368.43006103016313, 219.80349753546508, 184.3843971611383, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5398032, 1217.74352474683, 242.64035989267146, 988.9689576289588, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (8761363, 1424.8226374828628, 44.71825792373634, 988.4670331718697, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4251915, 843.3113448227391, 59.142840546110676, 469.04043325489306, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3193208, 72.83163802201477, 26.537384486741082, 474.11870337384653, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6598224, 1017.3450162201669, 232.62352824061657, 178.54018203063794, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (4412672, 523.2606567424735, 232.80092876797605, 564.0523188855724, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (2393337, 487.2087109697892, 194.03451042766963, 230.2669414269668, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2955636, 1218.3624450439117, 289.3184213787269, 476.92578220781235, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1415598, 692.3645551462163, 121.46272207142415, 152.29851524601722, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (5229706, 568.6353641286128, 69.5469480897481, 122.5291903126502, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (8439361, 204.12000942355266, 281.74158357543064, 208.9450283522689, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1751272, 1184.0652554887617, 289.2147185418917, 741.2658345107702, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (3687119, 572.4475677462277, 22.871304850453345, 406.07664079508197, 'first', 'USD');
INSERT INTO ticket_prices VALUES (6424865, 85.21562532501592, 200.094037219115, 512.4855601809938, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9469272, 952.9644471653373, 44.84954331355736, 928.320342587799, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9954294, 730.3993557423338, 197.55928679206568, 246.88061101673864, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1351333, 805.1533635490745, 17.141463740003744, 628.9782298544379, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5165210, 1248.838820517478, 116.78809422947052, 905.9625425732363, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (5981197, 71.21128057746593, 159.51158179695835, 854.5435705494319, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (8572860, 191.69409648176975, 112.94479460318648, 678.3602415931156, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (9646368, 538.0214864706368, 61.769043115429064, 290.92705793853133, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6065249, 690.7812779127569, 94.50332494390719, 911.4026036719322, 'first', 'USD');
INSERT INTO ticket_prices VALUES (8572860, 1347.823696199261, 15.315859854881332, 452.90348309528093, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5292000, 300.45214564791297, 134.6003948630223, 403.72127191731676, 'business', 'USD');
INSERT INTO ticket_prices VALUES (1117814, 528.6073390604954, 191.50827105360412, 862.1758251027637, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (7407177, 368.31727884548604, 165.17674912371857, 884.171685466361, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (4367259, 1364.522766591892, 235.96214934271183, 441.7562037486301, 'business', 'USD');
INSERT INTO ticket_prices VALUES (3593651, 806.5920219418439, 138.65563101740193, 240.7965905399261, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (6841707, 169.53282063922245, 203.51442164870124, 121.16516217531014, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2345750, 1123.9928515054628, 59.75853901777409, 642.5172140123979, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (2660494, 374.1430488856841, 228.57572582604806, 802.8815386251272, 'first', 'USD');
INSERT INTO ticket_prices VALUES (9881340, 808.4009535860141, 70.07695402493647, 691.586100945821, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9205451, 946.3716100204691, 81.99964140483168, 523.080151827045, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (3283227, 759.8223733742088, 212.80840138055854, 298.66783501140526, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (7324679, 627.9849572921038, 144.20662471447716, 113.628081629907, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6115972, 528.1342725778431, 167.85141367751004, 600.0930308838733, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2885884, 1116.9285693259685, 203.57581726978984, 665.1604969015939, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (3087375, 523.8772793368609, 35.86984041081993, 581.0788965780744, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2504743, 1070.78657969579, 211.19682044328113, 980.0922790969607, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6312080, 186.3155759370693, 68.91992136921516, 328.34752896640305, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (3991717, 1164.8641599070193, 94.92785867041775, 383.51947454115276, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (9362354, 1176.716388223386, 48.39581251620366, 782.1828224700238, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8026826, 484.52412459648264, 56.44047000395386, 519.2285389088113, 'business', 'USD');
INSERT INTO ticket_prices VALUES (9136604, 1289.0142583357272, 265.70248537226524, 532.404591656082, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6370619, 106.75846870330783, 178.44385198516918, 681.9515999124702, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2414536, 1004.4363818391435, 182.21188197734463, 22.822328388996894, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (5469436, 1444.2103015834825, 112.24541535696137, 670.7626588678426, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1063281, 1388.8059801955494, 199.09059651814468, 984.7384826847896, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5264010, 878.8671269861019, 69.8730285726693, 686.0840031838339, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4867249, 256.8389237339097, 18.156402855296776, 264.9904582708229, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6356585, 1251.0517969864304, 78.79234942460023, 274.9273441983294, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (4429427, 1345.691781960146, 74.35096858492822, 272.6691711472112, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (6794875, 395.5465975930207, 117.3330451089521, 108.55837310719862, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (3687015, 1341.0549322505187, 187.04566046122144, 623.9578381876397, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6448352, 739.7431232026834, 41.03360489657091, 533.9293274020032, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (1016638, 206.01808239325774, 73.78472778863566, 593.153116637416, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (8965286, 1224.6029448971515, 65.21989102104325, 391.1614163388881, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9265285, 1154.680408238848, 165.76549397668725, 266.19866500732724, 'first', 'USD');
INSERT INTO ticket_prices VALUES (3481701, 1079.7092472255802, 158.22309389247542, 840.0983834133361, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8575372, 1166.101418445281, 130.89676699183576, 91.0432231938263, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (1580588, 937.8070681110462, 139.4170472084113, 318.3988851724697, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (3381353, 1421.4696536919541, 293.90709270326613, 521.7042360742697, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (1256000, 342.70758140040334, 178.7180716414525, 396.0357017969844, 'first', 'USD');
INSERT INTO ticket_prices VALUES (2157626, 895.0831707590726, 277.89528627385124, 757.445221882912, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (8389626, 1259.5797744125002, 52.197493444001935, 569.4940352749495, 'economy', 'CHF');
INSERT INTO ticket_prices VALUES (9740986, 104.12629438167738, 75.25739066911191, 967.3683267568031, 'first', 'USD');
INSERT INTO ticket_prices VALUES (4453235, 1281.9195750929725, 123.24391786603437, 378.13156344941825, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (2760891, 975.5344837293765, 200.42658657447745, 230.77162382574946, 'business', 'USD');
INSERT INTO ticket_prices VALUES (6283664, 320.06102005350243, 168.8844046707153, 971.9251329191362, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (4079420, 121.58865387984162, 137.5125694901036, 58.79930534751632, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (6709604, 674.7347801765115, 255.9809067559421, 198.41917772643902, 'first', 'USD');
INSERT INTO ticket_prices VALUES (1343920, 714.3016784464478, 170.6393960639362, 818.2778755555022, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (2014039, 504.95124438014545, 191.96201929780298, 588.8246557654975, 'economy', 'USD');
INSERT INTO ticket_prices VALUES (5801072, 1434.0303880807885, 24.161992496445805, 554.1147246468886, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (7980476, 1350.0393322234188, 26.02011112051082, 706.4655238420927, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (6368129, 321.22971104052124, 73.13166420562978, 885.6696950194267, 'first', 'EUR');
INSERT INTO ticket_prices VALUES (6805911, 1223.413881353295, 228.36535825687898, 611.3656971669537, 'first', 'CHF');
INSERT INTO ticket_prices VALUES (9197064, 661.8337104394445, 224.25670028659982, 313.8335683980158, 'business', 'EUR');
INSERT INTO ticket_prices VALUES (4853826, 600.6407471945647, 178.04823424964226, 55.37817031423342, 'business', 'CHF');
INSERT INTO ticket_prices VALUES (2152309, 370.68753719051506, 141.33361056934024, 350.57665526627557, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (9641055, 699.9781942752563, 113.71452709495975, 339.71309253025305, 'economy', 'EUR');
INSERT INTO ticket_prices VALUES (5068903, 1163.5478790602492, 294.71647032251286, 623.2466689307668, 'business', 'USD');
INSERT INTO ticket_prices VALUES (8933616, 186.4077179532533, 37.57786497581601, 960.0092191167444, 'first', 'CHF');

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

INSERT INTO ratings VALUES (3338781, 1102212, 3606936, 4, 'Action road tend through away top. Health appear technology like capital message.
Sit wish name property. Capital pattern cultural her on. Question analysis situation discussion generation.', '2026-05-04');
INSERT INTO ratings VALUES (1815987, 2461695, 5682084, 2, 'Against season response court budget company. Church sure hundred us improve speak still. Management end class behind visit.', '2026-02-09');
INSERT INTO ratings VALUES (3386191, 4821607, 5041690, 4, 'Fine happen sort better what everybody. Property perhaps physical low inside half approach.', '2026-01-09');
INSERT INTO ratings VALUES (5695030, 1185144, 2134249, 3, 'Understand talk know turn me edge get. Third add hope drive response old.
Small when wait myself suffer.
Democrat event data morning identify wait. Else test under action.', '2026-02-21');
INSERT INTO ratings VALUES (2428203, 2541341, 1832564, 3, 'Station rich morning interesting sea pull. Idea road quality seek score without. Group create experience choose but skill hard law. Talk yet industry radio which cell.', '2026-03-05');
INSERT INTO ratings VALUES (3656630, 3257367, 9969805, 1, 'Million well interesting speech serious movement often. Law ten pretty little score only.
A choose where growth entire radio raise. Difficult church help ready choose of. Event much movie red.', '2026-02-05');
INSERT INTO ratings VALUES (6916146, 6526839, 1801299, 2, 'Recognize third nice civil subject than. Report majority sing remember specific break.', '2026-05-09');
INSERT INTO ratings VALUES (7700521, 2483269, 1082936, 4, 'North election better nothing. Assume generation call discuss certainly.
Specific later business plan else could consumer.
Office song ability go deep. Consider enough where so model turn bit size.', '2026-01-27');
INSERT INTO ratings VALUES (6557064, 8755012, 1170704, 1, 'Ten how plant spend. Term boy political civil religious pressure.
Allow card policy decade will goal practice. Information together season end.', '2026-05-10');
INSERT INTO ratings VALUES (5823246, 9908742, 7381080, 5, 'Feel program ground form none walk pattern. Five can require significant loss action baby. Prepare anything draw method girl support popular.', '2026-05-20');
INSERT INTO ratings VALUES (2311830, 3135062, 8522282, 2, 'Star political be third recently decision ask speak. Still now suddenly religious debate heart still.', '2026-05-31');
INSERT INTO ratings VALUES (4739824, 2940416, 1595358, 2, 'Chair imagine rate provide could culture. Drug model pretty plan take majority. Stand Republican quality picture whatever visit strong.
Subject fact state.', '2026-05-16');
INSERT INTO ratings VALUES (8695262, 6955370, 8665254, 5, 'Air popular newspaper especially happen word.
High huge walk account. Than space feeling. Area truth fear similar.', '2026-04-05');
INSERT INTO ratings VALUES (5663759, 9735147, 3920680, 2, 'American season social new indeed raise. Claim total executive section beat.
Source later success decade. Player already industry education agreement.', '2026-03-06');
INSERT INTO ratings VALUES (2283024, 9735147, 2915408, 1, 'Community respond white respond cut music. Here wrong cut. Since big perhaps just woman figure color.
Away hope threat behavior mention report heart. Reduce its machine stock.', '2026-01-20');
INSERT INTO ratings VALUES (6663410, 7563320, 4885859, 2, 'Lay region decide relate. Then top recognize main fear.
Building magazine gun can particular fear room people. Hour style population serious different operation sure today. Middle kid likely art.', '2026-04-01');
INSERT INTO ratings VALUES (3512596, 5185820, 4186470, 1, 'Keep lead plant Republican.
Employee man compare appear nature on. Experience pay game even everybody site my.
Power matter particularly listen wind only exactly small.', '2026-05-31');
INSERT INTO ratings VALUES (8661263, 2992723, 7312338, 3, 'All address perform beyond. Government information direction style word before event.
Kind may safe test may. There season civil. Everyone rest feeling life campaign so nearly.', '2026-02-05');
INSERT INTO ratings VALUES (2051226, 5408609, 5994883, 2, 'New low produce identify writer to contain. Picture value music begin.
Whatever life never film event Democrat work. Serious simply price cost. See somebody since show however clearly.', '2026-02-26');
INSERT INTO ratings VALUES (3101701, 8238843, 5160345, 3, 'Interesting study money two consider customer significant. Bad vote notice federal fill west.
Strategy sound time. Particular moment they evening indeed.', '2026-03-17');
INSERT INTO ratings VALUES (2810998, 7422573, 1003920, 5, 'Discussion practice light itself seat new analysis. Prevent cell see sit. Charge cause exist water.', '2026-05-08');
INSERT INTO ratings VALUES (9347674, 3270346, 5803465, 1, 'Age up for single fight rule result.
Leader meet set floor on fire. Next religious bed knowledge especially experience former baby. No clearly vote figure north.', '2026-05-02');
INSERT INTO ratings VALUES (9015925, 8155808, 3443010, 3, 'Around until with heart off. Prevent economy year time.
Yourself officer allow nearly name weight. Draw store election record. Everything particularly those natural involve.', '2026-02-25');
INSERT INTO ratings VALUES (2586556, 6419573, 5354373, 2, 'A society woman skill his test continue stock. Scientist thing nice suddenly health town.', '2026-01-24');
INSERT INTO ratings VALUES (9533421, 5719835, 2775069, 1, 'Form help treat either one social give. Child full real back. Herself mention event yourself concern far.
Free mean security radio. Station art talk sort.', '2026-05-29');
INSERT INTO ratings VALUES (5613127, 8216199, 2794631, 2, 'Dream worker agreement difficult bank house painting. Bed character father skin state.
Live hear teacher strong. Throughout must figure customer air. Piece live mention learn south medical school.', '2026-01-21');
INSERT INTO ratings VALUES (5675247, 1381520, 1472543, 2, 'Easy artist walk soldier skin television. Theory because our professional.
Notice establish draw. Its history yes issue forward.', '2026-05-18');
INSERT INTO ratings VALUES (7554289, 5185820, 4058181, 4, 'Third head add. Him factor much something.
Car team pull executive teacher step carry. Though minute art pay sing scientist million. Account single woman around consider should religious.', '2026-01-27');
INSERT INTO ratings VALUES (2056510, 9263093, 2775069, 3, 'Right concern true own camera finish. Money likely fund one about action church. From billion blood.
Can look appear approach peace ability modern indeed. Create admit forget short either up.', '2026-04-01');
INSERT INTO ratings VALUES (4283176, 9263093, 9710137, 4, 'Matter cold remain dark. Community what nation hear.
Road special wide respond believe physical opportunity. College role so several certain. Responsibility reach race sell her change fill.', '2026-01-30');
INSERT INTO ratings VALUES (9415822, 4747967, 9608933, 4, 'Call kitchen whole cause clearly win. Add check happy her cup.
Suddenly about industry about sort. Popular industry identify world build. Source weight green tough thus direction less.', '2026-02-09');
INSERT INTO ratings VALUES (8063008, 2422409, 3191211, 2, 'Not race security trial before affect four herself. Thing bad water yourself special traditional call certainly. Cup director several past.', '2026-04-05');
INSERT INTO ratings VALUES (2659411, 3784950, 4388944, 3, 'Teacher value fly democratic those set. Lot public whatever whom.
Pattern task news black action air. East yeah dinner operation statement hope. Heavy parent or do discover grow usually.', '2026-04-18');
INSERT INTO ratings VALUES (6996066, 9999465, 1617454, 1, 'But baby smile develop move energy. Even prepare detail decide chance themselves issue.
Meet civil officer budget. Government cover design cell sing final.', '2026-04-10');
INSERT INTO ratings VALUES (7740391, 2975287, 3577336, 3, 'Community under ever price bit performance scientist. Eye own assume away customer. Measure half either get.', '2026-05-04');
INSERT INTO ratings VALUES (7774185, 3437925, 1983030, 3, 'Congress speech long. Past section girl speak this high back. Hundred happen ago hospital professor.
Order quality then student others hair despite. Step heavy chair who.', '2026-03-28');
INSERT INTO ratings VALUES (3843771, 3655021, 2443174, 5, 'Stay population federal culture certain wall.
Church live new. Home history success. One available which seven people. Physical our tax artist despite.', '2026-02-23');
INSERT INTO ratings VALUES (4913975, 2773723, 7312338, 1, 'Work toward available outside early. Simple current despite wall career study after. Long mission expect present lose international.', '2026-02-13');
INSERT INTO ratings VALUES (7696029, 2992723, 7648856, 3, 'Benefit above police dinner half win open western. There anything PM play care effect need. On west above product center of.
Base election beyond. Down box magazine.', '2026-03-13');
INSERT INTO ratings VALUES (5842773, 5277747, 1034182, 2, 'Direction marriage rest life. Possible assume visit. Wish instead painting manager. Indeed stand respond allow data board have record.', '2026-04-18');
INSERT INTO ratings VALUES (6907694, 7180424, 2727975, 5, 'Summer health parent school.
Major week great alone. Rock whatever maybe animal region clear.
Land win second spring stock interview. Community boy assume training establish.', '2026-02-20');
INSERT INTO ratings VALUES (3476787, 5761385, 9608933, 1, 'Reality weight you back heart message school. Network best performance manage hand guy. Indicate movie alone recognize particularly rate follow bank.', '2026-03-31');
INSERT INTO ratings VALUES (9525833, 5030264, 8457243, 3, 'Property what most letter. Type cold fear value. Form blue especially benefit cold draw two treat.
Pay foreign practice one officer. Black idea get.', '2026-03-30');
INSERT INTO ratings VALUES (4683689, 4422051, 7409070, 5, 'Everyone career thousand draw space seem. Successful any ahead main teach learn.', '2026-02-13');
INSERT INTO ratings VALUES (1404479, 8371206, 2502371, 4, 'Radio show once nor home pay. Opportunity this language only.
Class do financial up agency blood eye respond. He probably parent onto office. Either down project evening relationship radio.', '2026-05-23');
INSERT INTO ratings VALUES (2254094, 4078630, 6288275, 4, 'Partner treatment college walk later month for. Inside weight near traditional.
Rate according public however ability.
Pressure own protect pattern management think. Cell lose fear after few various.', '2026-01-25');
INSERT INTO ratings VALUES (1813340, 3681623, 8795653, 3, 'Moment eye movement. Box design most American.
Summer together account stage although almost. Admit trial recognize may opportunity hold study.', '2026-02-11');
INSERT INTO ratings VALUES (1360187, 4859106, 3920680, 4, 'Have law question. Rise degree agreement first story relationship.', '2026-03-03');
INSERT INTO ratings VALUES (3500470, 7371009, 8356612, 4, 'Nothing data society eye on money decide under. Believe education only option shake expert. Discuss professional face reduce whether. Especially herself staff choose model.', '2026-02-21');
INSERT INTO ratings VALUES (8446602, 8391185, 9969805, 2, 'Congress music message same attorney up. Lose important opportunity property any.
Nature range teacher fast firm late station. Memory office involve. Adult stuff agree direction.', '2026-01-03');
INSERT INTO ratings VALUES (7229644, 2975287, 5146931, 1, 'Certainly recognize kind cold foreign. Garden direction amount consumer each. Decade pull idea wrong cup coach.
Pressure already group least argue receive sit difference. Apply TV always base hard.', '2026-04-12');
INSERT INTO ratings VALUES (8561288, 5382959, 7663695, 4, 'For agency most pretty.
Lose could remember choose western subject address. Cup establish just new likely every impact meet.
Of even evening water official. None wife network stand answer course.', '2026-05-03');
INSERT INTO ratings VALUES (4538541, 3816929, 6009573, 2, 'Outside left tough set main her. Discuss account participant newspaper without. World especially school raise address culture.', '2026-05-21');
INSERT INTO ratings VALUES (9036653, 3713051, 9837984, 3, 'Try century next cut must blue close machine. Subject world anything detail.
Travel member resource society field keep. Near environmental everything beyond interest.', '2026-04-24');
INSERT INTO ratings VALUES (6342677, 9488847, 7323048, 5, 'Set like thing season heart forward local. Need speech play region note main hotel. Country party late while out trial near.', '2026-01-05');
INSERT INTO ratings VALUES (6335876, 8216199, 7207091, 5, 'Anyone continue maybe start pick find. Herself opportunity out cold own owner. Leg bring foreign.
Couple form form such. Reason break music wear impact. End keep the technology together star.', '2026-04-10');
INSERT INTO ratings VALUES (8573555, 2483269, 7154876, 4, 'Property technology available break defense statement general. Ahead unit door pay sure prepare. Mention board voice mean century history.', '2026-05-03');
INSERT INTO ratings VALUES (6954086, 8594972, 4497548, 4, 'Drop job sometimes value throw improve field. Sometimes raise join discussion. Shake occur wind approach college. Character difficult believe partner able.', '2026-01-29');
INSERT INTO ratings VALUES (2961857, 3760433, 8841927, 5, 'Back require let college eat agent picture. May morning up so.', '2026-02-21');
INSERT INTO ratings VALUES (5444129, 3418931, 1190190, 3, 'Have room western interest stage. Identify particular civil data cultural away nature any. Second home tough like this.', '2026-04-05');
INSERT INTO ratings VALUES (3907502, 8395495, 8574016, 5, 'Today far interesting rest so recently. Exist not account start. Popular how conference believe short place.', '2026-02-02');
INSERT INTO ratings VALUES (6031691, 4422051, 3533222, 1, 'Gas ahead thousand wind believe over. Better there whose idea nearly Congress sport summer. Movie floor hard program policy.', '2026-01-15');
INSERT INTO ratings VALUES (9584578, 9258677, 5480356, 4, 'Appear institution physical future. Region bed floor test successful carry almost vote. Trial simply big message lead bed rather.', '2026-04-19');
INSERT INTO ratings VALUES (3048065, 2541341, 5994883, 2, 'Hundred miss less certainly carry. Clear inside break change ahead cause bag. Value country media officer toward vote.', '2026-04-17');
INSERT INTO ratings VALUES (1699975, 6526839, 3380796, 5, 'Citizen five coach people matter call president develop. Yes us itself night life pressure station spend. Day young authority section boy last.', '2026-05-23');
INSERT INTO ratings VALUES (2327980, 6526839, 1017327, 2, 'Back national pretty. Case right produce staff defense check. Second institution they college save.
Popular kitchen room increase yeah almost director. Attack admit maybe should behavior water happy.', '2026-05-12');
INSERT INTO ratings VALUES (1816610, 3352316, 3229060, 4, 'Everyone scientist save record. Teacher gun strong mouth kid turn natural involve.', '2026-04-07');
INSERT INTO ratings VALUES (8821492, 7092950, 2502371, 1, 'Perform property officer glass claim.
Trouble loss clear forget move either either. Those month government politics up carry industry. Inside increase less think we.', '2026-03-12');
INSERT INTO ratings VALUES (6038986, 1594316, 3061980, 5, 'International party policy. Defense nothing buy create player us first manager. Newspaper record reflect decade letter.
Current girl call employee garden score. Role half our who source movie show.', '2026-02-14');
INSERT INTO ratings VALUES (4196745, 4821607, 3533222, 2, 'Me teach enjoy issue little would.
Finish technology garden office prepare adult field. Stuff international themselves team. Why interview hair mention when which.', '2026-01-07');
INSERT INTO ratings VALUES (1103650, 4789986, 3577336, 1, 'Space call risk stay much. Property care suddenly father any.
Discussion several few bill serve each not. Receive rock scientist road.', '2026-05-01');
INSERT INTO ratings VALUES (2039938, 2905071, 2117824, 5, 'It here rise push provide. Reality task let increase specific. Pass some a mother before arrive play true.
Learn policy or wrong painting. Crime admit moment west treat hard.', '2026-02-16');
INSERT INTO ratings VALUES (9178748, 5719835, 3548200, 1, 'Risk everyone admit contain seek good paper. Voice sister until someone year prove.
For white nearly place room.
Lot certainly without particular experience high its. Prevent civil between become.', '2026-05-06');
INSERT INTO ratings VALUES (3208274, 5382959, 8168832, 3, 'Mission not picture fact into admit loss. Subject security first Mr region side unit. Ball situation fact into. Professor development piece edge born resource season particularly.', '2026-02-22');
INSERT INTO ratings VALUES (3688819, 1253566, 7542327, 4, 'Take magazine hotel month tax piece name glass. Investment picture whose recently camera alone season raise. American instead most option.', '2026-01-30');
INSERT INTO ratings VALUES (3338851, 4859106, 9250393, 3, 'Thank may option size learn actually. Necessary write class production cut whose value. Crime point protect family would idea leave.
Still agent board form owner customer.', '2026-01-07');
INSERT INTO ratings VALUES (5880116, 7556383, 7525533, 2, 'His participant population yet. Condition apply though.
Republican dark apply someone end. Trade on pretty force. Point later test rest.', '2026-04-01');
INSERT INTO ratings VALUES (5227776, 3784950, 5287611, 5, 'Watch minute room ability. Service threat they detail shake. Sometimes people arrive lead according check play.
Two and nothing occur church prevent including thank. High south fear room author her.', '2026-03-11');
INSERT INTO ratings VALUES (6972455, 4865004, 7770284, 1, 'She simply create teacher help indeed fine trip. Design health tell them two. Republican prepare more direction card gun.
Tend degree never after language. West store beyond forward serve avoid.', '2026-01-03');
INSERT INTO ratings VALUES (2644817, 7412516, 4048091, 4, 'Involve source kitchen produce meet however common. Job include people poor dark health. Medical seven news.', '2026-04-22');
INSERT INTO ratings VALUES (8927419, 7470714, 2609422, 3, 'Control face pay perhaps again. Either night choose thus over foreign easy.
Summer article here state. Them husband close money meeting pretty effort.', '2026-02-19');
INSERT INTO ratings VALUES (4754702, 7885995, 7147855, 2, 'Determine impact section thus food under. Tell letter act number month subject black.', '2026-05-25');
INSERT INTO ratings VALUES (8658275, 9166027, 1933194, 4, 'New quite size plant explain can sing. New reveal production.
Mouth which particularly finally exist. Choice past information anything. Gas letter see defense media song.', '2026-04-19');
INSERT INTO ratings VALUES (4917676, 7422573, 7496589, 2, 'World type if. Pay education sign particular guy help.
Level life together base government on room enjoy. Whatever many bring food student. Leader statement energy.', '2026-02-28');
INSERT INTO ratings VALUES (6581056, 1102212, 5029747, 4, 'Sell customer down accept make budget. Finish evening go debate than. Laugh born its leg beat.', '2026-02-15');
INSERT INTO ratings VALUES (7547949, 3963722, 4762283, 2, 'I discussion science imagine painting research toward fear. Space behavior bed east.
Our wear evidence avoid exist bag public. Home together someone fact sport send should.', '2026-03-24');
INSERT INTO ratings VALUES (6829869, 7422573, 1554195, 3, 'Mrs consumer pass ball again land the. Involve conference two note.
After structure what medical. Cover option describe education measure reveal.', '2026-02-27');
INSERT INTO ratings VALUES (7479796, 3577168, 9128197, 1, 'Truth price future have drug improve. Mean nothing eight.
Long yourself really nearly behind why network. Real what white win.', '2026-01-02');
INSERT INTO ratings VALUES (8056095, 6597799, 7593448, 5, 'Increase service significant whose. Field floor spend.
Avoid animal mention environmental spring. Young foot person theory attention describe. Ever sister next majority change state participant.', '2026-05-17');
INSERT INTO ratings VALUES (6184008, 1169425, 4786071, 2, 'Week thus charge life spring reality. Stay board visit language wife someone. Last effect everyone. Issue military everybody most smile system individual.', '2026-03-17');
INSERT INTO ratings VALUES (6763544, 2265297, 4077580, 3, 'Health high which computer star forget. Thank alone build country. Chance with country play lot score politics.
Deep remember skin identify.', '2026-05-06');
INSERT INTO ratings VALUES (4946880, 3352316, 3106369, 1, 'Station too for natural forget author. Benefit religious chance total. Eat act mean write inside agree. Seem walk lay technology.', '2026-02-27');
INSERT INTO ratings VALUES (2121858, 7563320, 3606936, 4, 'Wear none past side card provide. Seat agreement his age medical treat short. May because system.
Community however campaign do because them tonight Democrat. End live southern real church.', '2026-04-29');
INSERT INTO ratings VALUES (6392163, 6187608, 4373499, 1, 'Why business thus write. Responsibility game recognize. Bar skin however society citizen still.
Between assume of side campaign. Street center test somebody blood grow situation back.', '2026-04-18');
INSERT INTO ratings VALUES (9507227, 8155808, 8360355, 5, 'Accept pattern doctor plan thing hot. Technology effort ahead he television. Perform half interest American country write.
Arrive concern win military. Health maybe but big her. Rock tree enjoy two.', '2026-01-27');
INSERT INTO ratings VALUES (9484771, 9622165, 7821647, 5, 'Board PM heart soon past exactly area operation. Alone she woman full degree world can budget.', '2026-03-31');
INSERT INTO ratings VALUES (7886423, 3626944, 3162135, 4, 'Mission science point also street. Until talk thus story hand leader audience hard.
Daughter today positive able have human amount. Able operation you gas two.
Suddenly seek language.', '2026-03-15');
INSERT INTO ratings VALUES (2153991, 3343612, 4534704, 2, 'Door right wish avoid benefit to political. Thousand how check late else red thought. State five family professor yard someone agree happy. Water draw great establish.', '2026-01-16');
INSERT INTO ratings VALUES (2579166, 3977299, 7944595, 4, 'Bed news fish pretty detail mother. Form high particularly magazine appear lose control feel. Friend guess end first rest nothing wind.', '2026-03-25');
INSERT INTO ratings VALUES (7346924, 3760433, 7542327, 5, 'Information hot fear resource serve never move. Responsibility account difficult the board. Kid painting political PM force.', '2026-02-18');
INSERT INTO ratings VALUES (3522802, 8446918, 3580596, 1, 'Good challenge evening reality. When turn cultural act environment. Artist employee sport ask why push.
Thing involve father season focus specific foreign. Over them not either no.', '2026-01-27');
INSERT INTO ratings VALUES (3724201, 4339947, 9086205, 4, 'Floor form account simply year. Economic money consumer fish father many animal. Admit control throw religious course cell. Bill theory speak drop college.', '2026-03-03');
INSERT INTO ratings VALUES (6717914, 8951574, 5518639, 3, 'Pick order thus. Join edge defense.
Floor plant top serve yourself property water. Good respond power school story someone.
Bank want discussion skin. Sister cover at. Dark risk window particularly.', '2026-04-10');
INSERT INTO ratings VALUES (8435915, 4789986, 8574016, 2, 'Mission able great make almost attention likely. Though like current child condition country relate.', '2026-03-25');
INSERT INTO ratings VALUES (9260635, 4308132, 9086205, 1, 'Like century something know marriage. Many best stand trip quickly. Coach trade yes.
Seat raise notice possible score center. Suddenly cup effort.', '2026-05-26');
INSERT INTO ratings VALUES (7354588, 6728086, 5253868, 2, 'International science line hospital agree anything statement. Many put provide hot chance. Position environment minute movement anyone whom.', '2026-04-03');
INSERT INTO ratings VALUES (8863251, 4439160, 1453128, 2, 'Sense while may must. Four their sound hundred visit deep glass take.
House community certain probably. Win focus return see move.', '2026-03-19');
INSERT INTO ratings VALUES (8470603, 3270346, 4082860, 1, 'Realize might expert machine. State develop drug quickly though. Table throw send four soldier case clear region.
For notice anything that. Remain tough fund same argue modern race.', '2026-04-14');
INSERT INTO ratings VALUES (4784665, 4149608, 2116507, 3, 'Worry although city series. Wish production message edge threat word recognize line. Back record attention usually require rise federal.', '2026-05-01');
INSERT INTO ratings VALUES (8918229, 4308132, 6396837, 3, 'City require for glass. Government factor fast center man street cold. Fire truth line group huge owner already cause.
Local low which find player. Trial family break street care guess field.', '2026-05-21');
INSERT INTO ratings VALUES (5742529, 1253566, 5855521, 1, 'Herself evidence conference young. Part down sing much purpose black this.', '2026-05-04');
INSERT INTO ratings VALUES (7999985, 4809618, 9710039, 5, 'Public fast experience chance his collection note. Her officer contain crime part image.', '2026-04-26');
INSERT INTO ratings VALUES (7232835, 7556383, 7172867, 4, 'Our stay difficult carry.
Main national themselves foot now same. Because watch usually claim they whatever entire.
International admit peace game hour now. Chance others part throw home follow such.', '2026-01-29');
INSERT INTO ratings VALUES (4603781, 3002493, 4492728, 3, 'Each assume become impact research technology able. Film interest government dog others wind thousand available. Act suggest son board station art south.', '2026-04-01');
INSERT INTO ratings VALUES (8635146, 7422573, 3684041, 1, 'Already over picture democratic entire though. Five less six past market set.
Remain day manage this.
Budget cut half piece note enjoy page house. Back partner start partner.', '2026-01-09');
INSERT INTO ratings VALUES (6627794, 8962583, 2422914, 5, 'Management model various possible me clear about. Somebody road stand rise evidence throw. Pick toward stage tax strong.', '2026-01-09');
INSERT INTO ratings VALUES (6505496, 3352316, 8457243, 4, 'Around take organization. Determine keep spend market day whose daughter else. Move customer wonder party stuff run air. Hour important site reach dog list next may.', '2026-03-16');
INSERT INTO ratings VALUES (7352848, 6856915, 2191318, 4, 'Whatever suddenly listen exist. Difference scene already half color. Dream but catch send.
Spend factor land exactly participant hot. Fill serious rock specific crime. Control never surface deep.', '2026-01-23');
INSERT INTO ratings VALUES (7997851, 8594972, 4501881, 1, 'People religious peace law. Oil religious early skill on. Mention as any week building control.
How hot identify floor. Reality old Democrat someone happen management.', '2026-02-07');
INSERT INTO ratings VALUES (2444818, 8957252, 1017327, 3, 'Billion citizen officer bit. Feeling read various represent. Century population machine rock economy seat he. Finally discussion foot recent even state require.', '2026-01-08');
INSERT INTO ratings VALUES (4319952, 4507387, 7592431, 5, 'Another three become choose ever give child. Science seem one newspaper that year each. Thus military investment.
Kid name bad despite. Best economy decade over. Detail issue news.', '2026-03-15');
INSERT INTO ratings VALUES (6656426, 4678821, 8522282, 2, 'Grow pull our probably why no billion. American kid color. Coach admit win report edge why activity.
Control positive under tend third else. Better whatever recently agreement worker might.', '2026-03-19');
INSERT INTO ratings VALUES (8450092, 3166127, 2827252, 5, 'National interview could soldier series indeed international to. Effect behavior woman president live wait.
Significant open painting without recently would listen. Old natural sport effect.', '2026-03-31');
INSERT INTO ratings VALUES (1939626, 5185820, 3564294, 1, 'Ago though education guy student customer. Way practice team since ten mean political life. Tough one team report.', '2026-01-15');
INSERT INTO ratings VALUES (1182626, 8896880, 5287611, 5, 'Share parent describe off less look. Reach matter something able inside score that suffer.', '2026-02-04');
INSERT INTO ratings VALUES (1640088, 5720403, 9148557, 5, 'Room store a get. Technology laugh single draw letter prepare anyone. Happy available majority take consumer nearly.', '2026-04-27');
INSERT INTO ratings VALUES (4878044, 9017430, 7275094, 1, 'Even spring nice ago draw direction. Full front simply full ground enough. Too right less usually team lose.
Various piece set make. Share most record treatment position author.', '2026-04-06');
INSERT INTO ratings VALUES (2897738, 7865203, 8251078, 2, 'Yet other rule baby. Bill mention situation beat event plan later house. Make feeling break method society.', '2026-05-05');
INSERT INTO ratings VALUES (1691213, 3897954, 2117824, 1, 'Middle and teacher nice. Will direction imagine significant yeah party.
Hard partner more artist production recent build necessary. Seem data involve fight here shake.', '2026-01-05');
INSERT INTO ratings VALUES (4161246, 4747967, 9859691, 2, 'City foreign role their manager unit class. Run play control wear though less he. Wall once call health situation eight.', '2026-03-28');
INSERT INTO ratings VALUES (8633618, 5030264, 9222958, 5, 'City notice success about detail. West task center hair culture list.
Address whatever season hair. Material guess something. Water chair major remain scene.', '2026-02-26');
INSERT INTO ratings VALUES (8747157, 9017430, 1044947, 2, 'Fine describe easy it others bag. Necessary of on artist factor out under.', '2026-04-06');
INSERT INTO ratings VALUES (7070644, 5781985, 1453128, 3, 'Second discover sing environmental ability. Newspaper share ok against last many.
Old participant above ability situation out wife. Southern fear opportunity offer degree interesting though.', '2026-01-20');
INSERT INTO ratings VALUES (1329222, 6589761, 3106369, 5, 'Until other candidate four remember same it keep. Be role surface participant later keep perform. Senior old laugh marriage.', '2026-03-28');
INSERT INTO ratings VALUES (8812788, 4865004, 4452459, 3, 'Drive charge these point glass. Company respond agent decide boy because example. Day huge instead accept.', '2026-02-25');
INSERT INTO ratings VALUES (7481553, 7447517, 8487202, 1, 'Sure return statement economic last professional yet. Million enter sister total sort woman more your. Indicate say identify soon can Republican production.', '2026-04-28');
INSERT INTO ratings VALUES (4696094, 9169482, 4752878, 4, 'Light mission each really character choice. Protect some health series onto. Pull knowledge hard clear top never a.
Hold fill professor off you. Smile character threat boy security skin.', '2026-04-25');
INSERT INTO ratings VALUES (8141691, 7462556, 3522915, 5, 'South way since. Scientist long network evening inside walk. Positive natural ball decade change listen.
Then machine seek author rest party. Tend matter leader like drug paper.', '2026-05-22');
INSERT INTO ratings VALUES (1499497, 4439160, 8399090, 1, 'Create long may issue check. Bring walk five half bank including beautiful.
Happen skill bed discuss great learn feel. Despite art let. Suggest computer option wait maintain travel easy street.', '2026-05-29');
INSERT INTO ratings VALUES (3162373, 7865203, 1362332, 2, 'College soon research different pass southern ok. Reveal rate fire task when century city seat.', '2026-01-05');
INSERT INTO ratings VALUES (6545335, 8668826, 9795505, 2, 'Edge cultural behavior but. Name as foreign sister. Use must toward Republican.
Act watch me only animal bar hear. Official represent however against watch necessary team.', '2026-03-11');
INSERT INTO ratings VALUES (4363816, 2024582, 5017380, 3, 'Tell day under. Long job price arm artist. Hour design thousand hit cover.
Budget hotel fact over. Next fight rich series this water class. Series himself of this speech begin realize note.', '2026-03-20');
INSERT INTO ratings VALUES (9243709, 8216199, 3984898, 2, 'Since couple keep future life size.
Structure avoid offer stand know church. Fire prevent language stock water reach Mrs ok. Pretty say list believe. Either surface this part.', '2026-03-30');
INSERT INTO ratings VALUES (8853311, 7371009, 7210852, 4, 'Force position recognize reflect. Cultural phone street wind while. College enough read create.
Shoulder free beyond second answer action. Management sit recently upon. Consider short event series.', '2026-02-13');
INSERT INTO ratings VALUES (2845365, 6066735, 8356612, 3, 'Me everyone through similar ready own peace. Television then enough scientist. System however attention feel.
Place tree where. Southern most push wrong.', '2026-03-21');
INSERT INTO ratings VALUES (6842244, 5286908, 5253868, 5, 'Entire entire page personal yourself crime action.
Role way general seat plan. Parent for president suddenly effort everyone.', '2026-05-03');
INSERT INTO ratings VALUES (5757362, 5105774, 8251078, 4, 'History participant total wall. Safe all federal end hair table so.
Expect occur seek how key your decision rise. Process option off senior girl its above. While suggest action throughout.', '2026-04-16');
INSERT INTO ratings VALUES (6923469, 7092950, 2117824, 5, 'Hour bag day play according. Maybe pretty strong painting address field task.
Fight education official. Exactly entire give.
Rule left kind operation house within. Fine run fear seem total long.', '2026-04-24');
INSERT INTO ratings VALUES (5599478, 7456753, 6343581, 3, 'Purpose expect right each lay off loss. Itself director memory dog describe. Important site student avoid whole hot still identify.
Own table truth forward value budget.
Head suggest group with unit.', '2026-01-13');
INSERT INTO ratings VALUES (9103022, 2461695, 7663248, 5, 'Practice child cut upon. Daughter soldier night step believe. Dream me view throughout manager.
Similar budget young top sit less. Whole character against employee. Green second because.', '2026-01-23');
INSERT INTO ratings VALUES (5515005, 5497703, 9710137, 5, 'Opportunity style may west indicate. There firm form field late build sure performance.
Term network all story act family small. Player ten truth as community have card eight.', '2026-03-06');
INSERT INTO ratings VALUES (5247319, 2992723, 8357045, 3, 'Find hundred agreement hotel wrong by quickly. Actually environment card road age.
Consumer teacher bring produce away. Of possible notice sport music that. Still high now room use.', '2026-02-28');
INSERT INTO ratings VALUES (4369248, 3198155, 3382876, 1, 'Fill type writer case manage. Population million once wrong hope writer last. Maintain within charge senior boy black. Such character stand staff too.', '2026-05-29');
INSERT INTO ratings VALUES (3578563, 3179127, 4580667, 4, 'The position space person world really.
Management commercial successful stop treatment few be all. Feeling many pressure office by. Positive action time respond structure understand.', '2026-05-27');
INSERT INTO ratings VALUES (3384787, 6527815, 5803465, 2, 'Stand go fast score available great sing. Right friend into stage left party ask. Exist could white that away operation.', '2026-04-24');
INSERT INTO ratings VALUES (1275618, 3897954, 5028809, 5, 'Play born account like. Fill arrive another debate or.', '2026-03-25');
INSERT INTO ratings VALUES (5702845, 5408609, 4373499, 2, 'Manage some stay role. Too hold although hit style group different.
Arm simple here want whatever. Long later design natural common bag world.
Word opportunity fund president phone security anything.', '2026-03-11');
INSERT INTO ratings VALUES (2208220, 9735147, 9311534, 4, 'Fly role he rock soon attention operation. Season team notice including course next both nature.
Watch movement force past glass just use. Or stock continue short executive mind each.', '2026-01-14');
INSERT INTO ratings VALUES (4667963, 8371206, 9311534, 3, 'If third chance also child. Together above almost player treatment his.
May despite identify imagine it structure inside. Yeah best future amount push anything discover.', '2026-03-03');
INSERT INTO ratings VALUES (8721339, 2957480, 6273381, 5, 'Production difficult where room. Civil bad present must total body quality. Other son field enjoy together.
Institution effort finish ball us. Out rule whole moment.', '2026-03-19');
INSERT INTO ratings VALUES (6745698, 3440276, 9795505, 2, 'Easy trial answer green include beat middle. Arm should policy establish process. Away allow suddenly to spend about wear.', '2026-01-15');
INSERT INTO ratings VALUES (2550253, 4636158, 8795653, 5, 'Worry write various order. Fish good may evidence benefit recently nor.
Sea deep mission build deal population.', '2026-02-12');
INSERT INTO ratings VALUES (6064469, 6589761, 4162870, 4, 'Pattern question activity less strategy. Represent direction ability clear notice hotel would. Investment matter draw house body window.', '2026-01-05');
INSERT INTO ratings VALUES (5037221, 3211200, 6009573, 5, 'Health check according determine team. Computer happen pretty opportunity Democrat bring many wrong.
Resource begin want foreign.', '2026-03-13');
INSERT INTO ratings VALUES (3746592, 8782077, 7843855, 4, 'Wrong right seat president real message. Fact account pull politics help. Nice around or price stay live around commercial.', '2026-03-01');
INSERT INTO ratings VALUES (5023451, 5497703, 5805421, 3, 'Debate news then theory off. Themselves pass trip. Crime develop billion care watch ask miss.', '2026-01-18');
INSERT INTO ratings VALUES (3576019, 6091790, 8820639, 1, 'Take appear how professor two allow. Product realize shoulder event most own recent. Hope itself quite nothing seem church knowledge gun.', '2026-04-10');
INSERT INTO ratings VALUES (2447000, 7540759, 9837984, 3, 'Describe idea foot note wear stuff treat. Key ok how.
Us prevent hope study his back society agreement. Buy collection local. Letter whether physical media home mind usually fear.', '2026-05-23');
INSERT INTO ratings VALUES (7754820, 6419573, 4942822, 3, 'Mission write capital own everything security word. Fact event treat nice event off yard. Mission bad plant now.', '2026-05-03');
INSERT INTO ratings VALUES (6069101, 5408609, 3735507, 1, 'Science song floor. Little type yard establish professor quite coach.
Small arm source positive read author already. Star may since resource.', '2026-04-28');
INSERT INTO ratings VALUES (6423399, 9258677, 1742490, 2, 'Step fast place itself. Between respond somebody others.
Structure treat realize apply form enter executive. System notice provide front.', '2026-02-05');
INSERT INTO ratings VALUES (5213846, 7447517, 4172964, 2, 'Agency western reality guess dinner use per. Stop president add type other group. Evidence behind continue family.', '2026-04-10');
INSERT INTO ratings VALUES (8868568, 6976359, 5458179, 3, 'Choice follow other move various. President week dog night.
Instead question difference.
Dream hold way similar. Job myself will treatment benefit free check science.', '2026-03-18');
INSERT INTO ratings VALUES (4837349, 8957252, 6888305, 2, 'Civil especially building Mr. Open knowledge Republican.
Land ask able pass it goal. Assume party pretty might only example hot.', '2026-04-09');
INSERT INTO ratings VALUES (4305907, 3504500, 7405213, 5, 'Thank approach hospital artist. Same manage quite peace weight mean organization.
Nation owner full top at. Dream prepare beautiful affect model speech movement believe.', '2026-05-23');
INSERT INTO ratings VALUES (9623789, 4308132, 8820639, 2, 'Type international team especially camera quite should. East price major. Month two current onto tonight two.', '2026-04-25');
INSERT INTO ratings VALUES (6710886, 6351366, 7432459, 1, 'Continue college year popular tell. Laugh low almost mean discussion eat stock likely. Modern six four later.
Reality politics movement hotel lay east range. Part goal center become return father.', '2026-04-28');
INSERT INTO ratings VALUES (1040414, 7797269, 9235735, 3, 'Production either she clearly bed value. Great floor middle group although. Include simple possible yard edge.
Maybe different mention traditional. Wish manage short democratic site minute.', '2026-02-18');
INSERT INTO ratings VALUES (9954071, 5408609, 1617454, 1, 'Step forward story. There right involve give nothing statement general. Expert knowledge scene list.
Century there report pay even. Color large night source notice shake account.', '2026-05-01');
INSERT INTO ratings VALUES (2162508, 7556383, 8539128, 3, 'Doctor program face travel sister although remain. Last age late hour feeling ball simple.
Hospital enjoy clear friend. Mean city general week.', '2026-02-20');
INSERT INTO ratings VALUES (3009193, 4193399, 2111920, 4, 'Job how hair. Event look really bar learn notice. Lot short religious professor.
Author party Congress specific someone poor. Marriage year class red great building.', '2026-05-11');
INSERT INTO ratings VALUES (4709733, 9810226, 4452459, 1, 'Stand successful live here. Four character modern sing travel decade happen. Source skin station price.', '2026-03-18');
INSERT INTO ratings VALUES (7660652, 8782077, 5420546, 3, 'Smile begin their outside prevent. See development toward then west.
Play medical choice everybody option scene. Mission paper might camera understand enjoy. Couple pick available to product really.', '2026-03-12');
INSERT INTO ratings VALUES (1917193, 8395495, 8928018, 5, 'Any adult policy compare deal paper chair. Road beyond art place indicate president. Reveal his tell whatever hard.', '2026-03-19');
INSERT INTO ratings VALUES (3710959, 9263093, 3577989, 3, 'Cell rise little cover. Market to political.
Worry happy give by case. Clear huge believe.
Camera their government. Stay able Mrs effort science.', '2026-02-18');
INSERT INTO ratings VALUES (1052799, 7447517, 8795653, 2, 'Into weight pretty kid. Wish Mrs indicate type chair material. Account practice son everything.
Network season close skill Mr rich response.
Figure involve do training.', '2026-03-04');
INSERT INTO ratings VALUES (8420813, 5009563, 4953397, 3, 'Assume perform either cell five. Wife Mr view source such.
World scene morning factor. Raise can especially. Begin thus husband old oil.
Set two store.', '2026-03-14');
INSERT INTO ratings VALUES (4150179, 8755012, 3577336, 3, 'Choice most paper hour operation service usually. Check red fish read always meet exist.
Girl reality she report tax point term myself. Guy you election expert in coach.', '2026-03-05');
INSERT INTO ratings VALUES (3374768, 2654571, 4942822, 2, 'Medical summer recent tonight. Window crime safe. Girl sort think song approach machine subject heavy.', '2026-01-21');
INSERT INTO ratings VALUES (2658754, 6856915, 8522282, 1, 'Next parent station pay. Current spend couple have.
Add ground myself employee. Generation ground contain ago already ground every front. Air month PM.', '2026-04-11');
INSERT INTO ratings VALUES (6632724, 1128509, 9710137, 5, 'Concern eye public land put everyone writer. Board quality forward. Sea it Democrat year contain several. Religious next personal office responsibility strategy building.', '2026-05-04');
INSERT INTO ratings VALUES (7531205, 1594316, 9311534, 4, 'With rise must customer Mrs.
Seem choose he successful subject middle. Simple where now option foreign. After spring green agree investment. Prevent cold rather information meeting threat condition.', '2026-05-29');
INSERT INTO ratings VALUES (9054166, 7470714, 3261263, 2, 'Democratic analysis recognize hard. Run activity positive almost but.
Beyond sport none. Rock civil win eat.
Live part science themselves. Anything car government own half.', '2026-02-10');
INSERT INTO ratings VALUES (3531912, 9999465, 8600080, 2, 'Support rock father imagine memory shoulder. Station fill player ask.
Describe discover with somebody. Research explain parent yes trial. Kind court recognize whether his.', '2026-02-22');
INSERT INTO ratings VALUES (5278797, 7507143, 3868324, 4, 'Religious area rate student spring item. Ability behavior project message financial skill.', '2026-04-24');
INSERT INTO ratings VALUES (9034270, 4809618, 5480356, 3, 'War simply sit unit truth once. Reflect deep they especially.
Fire improve list consumer determine same. Good home during seat. Glass government purpose inside audience.', '2026-01-25');
INSERT INTO ratings VALUES (7610322, 2974225, 7147855, 1, 'Fly model indicate between sound hard. Among maintain carry. Force your relationship three yourself remain.
Power table then onto foot enough. Anyone set remember matter second power law.', '2026-03-17');
INSERT INTO ratings VALUES (8181284, 8805416, 9676209, 2, 'Product quickly firm need star. Affect attack tough money.
Someone standard half movie network hundred. Mrs performance little sign. Here much even pass teach everything section.', '2026-03-04');
INSERT INTO ratings VALUES (9801614, 5030264, 9608933, 5, 'Us hit scientist like statement. Prepare story road fall peace past. Soldier half sound reason upon parent.
Cell bag card history machine cultural question. About summer decision save as radio.', '2026-02-15');
INSERT INTO ratings VALUES (1540975, 9169482, 4497853, 4, 'Anyone treatment from front control. Decision where may card employee.
Chair campaign employee mother region. Friend require exist sign help. Together magazine usually administration concern style.', '2026-02-01');
INSERT INTO ratings VALUES (5720464, 8962583, 1952545, 4, 'Child effect answer state down social never. Foot mouth health eight particularly according. New audience service subject could strong throw.', '2026-01-15');
INSERT INTO ratings VALUES (8075068, 7660641, 3580504, 4, 'Remain must dinner media go particular plan. Community allow degree manage role interesting important.
Morning audience whatever action. Shoulder past call space help.', '2026-04-27');
INSERT INTO ratings VALUES (6126649, 1253566, 9417673, 2, 'Study leg country leg ok keep stop she. Also list scene activity happy race.', '2026-03-24');
INSERT INTO ratings VALUES (8622459, 2483269, 9494788, 3, 'Next eye whether.
Let significant owner. Best continue information step drive paper.', '2026-05-18');
INSERT INTO ratings VALUES (6012970, 1831583, 6707929, 3, 'Some class memory bit nation ever report. Mouth past research. Fast player result sport local put page.', '2026-01-22');
INSERT INTO ratings VALUES (9915609, 8371206, 3077667, 4, 'Serious subject spend miss. Something simply group or lead stay.
You wonder those purpose believe. Senior to summer plant spend.', '2026-04-25');
INSERT INTO ratings VALUES (6583619, 7083414, 5843018, 1, 'View reveal under onto. Newspaper on give study rich mission increase. Over instead indicate rate poor.
Financial national option do green appear save.', '2026-02-06');
INSERT INTO ratings VALUES (9658499, 8594972, 9795505, 1, 'Soldier blue world party detail produce work. Human common mention dinner safe we first. Choice behind weight lot beyond.', '2026-02-27');
INSERT INTO ratings VALUES (4820459, 3074155, 4035695, 5, 'But him direction arm upon key. Growth three same administration. Research interesting appear main should majority land.', '2026-01-02');
INSERT INTO ratings VALUES (9404832, 3423018, 9269310, 3, 'Happen treatment writer series letter pick. Pretty why near cover current. He probably admit discussion sea compare.
Pick statement blue lead.
Because him main support.', '2026-02-20');
INSERT INTO ratings VALUES (7563589, 1365545, 7207091, 2, 'Her degree notice owner. Room good test collection east choice cultural. Hope scene education another very who huge. Trial practice arm truth country.', '2026-01-17');
INSERT INTO ratings VALUES (4957913, 5030264, 1118405, 3, 'Per prepare bad. Great already idea offer.
Hold TV key. With clear administration through station story. Live six account deal.
Election sign him inside. Service author clear bed protect run nation.', '2026-05-11');
INSERT INTO ratings VALUES (1546981, 7503774, 7405213, 5, 'Write evidence drop we himself. Paper simple national guy. Since theory full.
Skill affect fall road film want.
Address attention war perhaps young.', '2026-04-05');
INSERT INTO ratings VALUES (7022613, 4668405, 9279751, 3, 'Someone security consumer score.
Already report why avoid. Participant play American six at life scientist. Although building fast.', '2026-04-15');
INSERT INTO ratings VALUES (2287160, 3437925, 4841212, 4, 'By pressure soon nation. Major compare provide wrong matter well.
Me image manage increase month performance finish issue. Ground property drug move may. Peace not age keep.', '2026-02-12');
INSERT INTO ratings VALUES (1063256, 8395495, 9782865, 5, 'Financial just end identify camera total same several. Carry protect level probably fire people.', '2026-02-07');
INSERT INTO ratings VALUES (9886617, 2461695, 4026701, 2, 'Great really car family red despite practice. General perform commercial police staff mother. One rise investment arm suddenly.', '2026-03-20');
INSERT INTO ratings VALUES (7165902, 9729552, 5017380, 5, 'Whole detail nature beat save mention instead beautiful. Building drop wrong.', '2026-05-21');
INSERT INTO ratings VALUES (2435219, 8155808, 7569872, 5, 'Compare bit class decade Democrat wide trade group.
Perform off project same traditional without statement other. Indeed pay simply north report require. May so mouth.', '2026-05-21');
INSERT INTO ratings VALUES (3165896, 9810226, 7648856, 2, 'Once parent few ahead total each. Else song she majority party. Position eat deal expert word store health.', '2026-05-06');
INSERT INTO ratings VALUES (3383848, 8238843, 1128351, 2, 'Every else establish surface. Especially political discussion sell begin local. Today available newspaper to kitchen.', '2026-04-19');
INSERT INTO ratings VALUES (4215962, 2773723, 5017380, 4, 'Class different poor both forward step past. Very church also born necessary population. Still budget amount smile water difficult south.', '2026-04-28');
INSERT INTO ratings VALUES (3718967, 9883532, 3429260, 3, 'Might training show water discussion wide red. Second story together factor answer interesting.
Enter poor per. Plan community enter purpose alone bad boy. These appear each under feel thing hair.', '2026-03-24');
INSERT INTO ratings VALUES (1110476, 9166027, 3817976, 4, 'Nearly everyone fire hear relate service. Machine affect nice news American.
Sometimes off pick five far tell. Green reflect floor always will dinner.', '2026-05-28');
INSERT INTO ratings VALUES (9404818, 8391185, 2319921, 4, 'Media agent carry down.
Season reality buy hair mission. Day mean then for. Own enough full development generation specific. Southern tough exist somebody key carry.', '2026-02-06');
INSERT INTO ratings VALUES (4644463, 4439160, 7770284, 3, 'Fly forward treat piece. Value Republican never perform.
Success investment country degree. Kitchen turn city most would.
Send direction station serve sing customer. Help movie boy truth.', '2026-04-11');
INSERT INTO ratings VALUES (3600775, 9166027, 1742490, 3, 'Sound crime education now health sure. Establish which line industry. Our board sort pattern create other always of.', '2026-01-08');
INSERT INTO ratings VALUES (5076041, 8707528, 1453128, 3, 'Plan less administration clearly treat. Many free bill course. Far majority early network relate from paper. Yourself to decide product player cell wind.', '2026-02-24');
INSERT INTO ratings VALUES (1899510, 8951574, 5631640, 3, 'Rich international dog remember address doctor. Work recently agency center movement recognize read simple.
Up public well them who help back.', '2026-01-08');
INSERT INTO ratings VALUES (8269942, 4422051, 2939418, 2, 'Three big city. Once yeah ready data.
Young person important radio. Thousand face every identify.
Service wait success ever special important.', '2026-03-31');
INSERT INTO ratings VALUES (4839423, 1253566, 1038948, 2, 'Often turn past know major either. Morning local contain yard letter blood. Space a future friend plan thought source.
Least truth treat across road.', '2026-03-09');
INSERT INTO ratings VALUES (7849864, 9478711, 3191211, 1, 'Voice child third heavy debate always.
Onto whatever nothing child partner. Age Republican four contain though. Participant computer film read forget them.', '2026-02-25');
INSERT INTO ratings VALUES (1160323, 6728086, 3061980, 2, 'President money discussion simply able window. Collection from even they air. Avoid inside trade ground low fall.
Note argue boy response can guess. Bag theory fast hand.', '2026-01-19');
INSERT INTO ratings VALUES (1334754, 5408609, 1170704, 1, 'Particularly society almost benefit. Quite wait similar away.
Picture world box idea type too including. Above boy sit data system process. Work wall card.', '2026-03-09');
INSERT INTO ratings VALUES (1739124, 4339947, 5480356, 2, 'Yes cultural say phone thousand ready accept worker. Head want energy man girl whose. South account current per.', '2026-04-29');
INSERT INTO ratings VALUES (4036008, 9729552, 1939454, 5, 'Central institution worry fight gas.
Over draw become can.
Clearly part machine heavy story anyone. Effect baby various trade represent mother. Anything catch they factor.', '2026-03-11');
INSERT INTO ratings VALUES (9502100, 5497703, 4162870, 1, 'Lawyer citizen whole spring story floor. Moment believe age respond none drop state. Six truth management full chance action national.
Evening admit wear same. Best church long cut.', '2026-01-30');
INSERT INTO ratings VALUES (8517530, 8668826, 7207091, 3, 'Try be describe feel. Quickly discuss describe product. Second artist its whose century himself writer. Fast safe open human.', '2026-01-26');
INSERT INTO ratings VALUES (8400320, 7660641, 4997378, 2, 'When letter art though him here. Identify six prepare bank pretty discussion. Theory culture member with involve seat month.', '2026-01-10');
INSERT INTO ratings VALUES (1515651, 5105774, 1801299, 3, 'Order society home physical.
Traditional finish so official public. Shake study put nothing certainly.
Discover computer each. Win order something officer. Moment author race require politics idea.', '2026-01-01');
INSERT INTO ratings VALUES (8144659, 2461695, 4512923, 3, 'Shoulder focus soldier food lose heavy term. Always any expect view smile Congress. Medical president woman term decide capital who various.', '2026-05-10');
INSERT INTO ratings VALUES (8691388, 3944798, 3802445, 4, 'Deal young itself why. Section I free court standard ok name. As red civil peace.
Not night eight can use item. High approach herself. Mean everybody top peace government owner.', '2026-02-09');
INSERT INTO ratings VALUES (1528962, 6762919, 8600776, 3, 'The avoid already stand boy develop. Both last compare produce newspaper agree kitchen. Maybe learn area feel bar.', '2026-02-06');
INSERT INTO ratings VALUES (6985973, 4859106, 9222958, 3, 'Give total already admit great.
Smile huge music hour choose cold. Until million inside let month. Protect report always public father process statement.', '2026-01-08');
INSERT INTO ratings VALUES (5923188, 5030264, 7604448, 2, 'Minute pull writer finally simply involve. Perform protect compare. Sing thank like that place she. Paper seven PM try.', '2026-01-30');
INSERT INTO ratings VALUES (3833062, 3879592, 1594856, 5, 'Time know take society buy. Lead director decade to grow center. Without fly assume common college east natural.', '2026-04-30');
INSERT INTO ratings VALUES (1213057, 5719835, 9112856, 1, 'Head success important hotel. Suffer power eight draw article try everything.
Hour drop law. Happen together class difference me fact war.
Growth possible even hotel. We toward leave capital.', '2026-05-17');
INSERT INTO ratings VALUES (9683379, 2773723, 6888305, 2, 'Bring Mr eat focus perform. Western book finally soon control. Exactly person guess. Fast activity purpose song rock.', '2026-01-25');
INSERT INTO ratings VALUES (8203608, 5382959, 2939707, 5, 'Majority amount end any require. Everyone offer remember peace.
Set figure anything member. Former early if result soldier. Natural order show positive history court.', '2026-05-25');
INSERT INTO ratings VALUES (7152530, 2422409, 3516493, 1, 'Character somebody blood wear. Probably pick democratic point be more today.
Win sell foreign debate. Its prove that. Believe book not.
If this trial couple whose positive. Skin skin gun.', '2026-01-11');
INSERT INTO ratings VALUES (5341465, 7083414, 8736781, 5, 'Defense southern ok serve. New manager state total Republican. Admit from whose reality.
Sit itself drive career leave perform today. Share state try former institution media into.', '2026-02-18');
INSERT INTO ratings VALUES (6264858, 6762919, 2117824, 2, 'Serious finally that political stage. Who foot official just half reflect him. Part evening control place indeed ten.
Reach report situation relationship air. Son group government.', '2026-01-29');
INSERT INTO ratings VALUES (8654645, 4118224, 2915408, 4, 'These run song finally single. Stock sometimes score now themselves study firm. Mrs plant time attack.
Speak inside dream material include. Learn prove create believe far two tend sea.', '2026-04-22');
INSERT INTO ratings VALUES (4414353, 5277747, 3232025, 3, 'Everything it claim throw purpose management. Brother need Democrat chance bed young. Natural market current in thing series.
Several adult center. Newspaper late manager foreign them hospital.', '2026-01-06');
INSERT INTO ratings VALUES (8865418, 2541341, 9649921, 3, 'Store identify become parent physical special. Long point east himself staff. Page culture third exactly impact.
Color store our. Note beat inside bill eye simple. Seat analysis those.', '2026-02-02');
INSERT INTO ratings VALUES (3720084, 8625276, 8357045, 1, 'Capital say exactly probably trouble. Painting position she fly service major.
Cup owner raise likely firm daughter beat per. Animal simply outside adult attack full physical.', '2026-03-13');
INSERT INTO ratings VALUES (6806469, 9218661, 2781589, 3, 'Television which range collection I near. Investment upon her play such. His watch let technology part keep Republican. Property hundred game computer reflect.', '2026-02-17');
INSERT INTO ratings VALUES (8610522, 6039236, 7496589, 5, 'Responsibility run green word. Single bar sign.
Book hour the finish policy impact party career. Out poor movie two state until.', '2026-05-12');
INSERT INTO ratings VALUES (7636056, 3352316, 8356612, 3, 'Design resource road affect. Help mention let provide oil four. Face lay plant common.', '2026-02-07');
INSERT INTO ratings VALUES (7019301, 7083414, 2297664, 4, 'Up enter at rich majority success light. Between how form either tough fight senior. Believe system capital kind item allow.', '2026-04-04');
INSERT INTO ratings VALUES (1939897, 6348851, 9795505, 3, 'Crime Mr section can. Poor sort because national about.
Fly possible hand nor catch. Start book produce fire laugh. Picture money institution practice current floor vote.', '2026-03-27');
INSERT INTO ratings VALUES (2998681, 5603263, 3061880, 5, 'Include network week care show.
College easy off. Seem ever practice since discussion though lot. Southern job senior want thought southern sell. First that service skin.', '2026-04-26');
INSERT INTO ratings VALUES (8994746, 3270346, 9279751, 3, 'Mrs situation heart place able watch. Eight everything resource first they goal. Various child wait quite eat build phone.', '2026-02-11');
INSERT INTO ratings VALUES (8195793, 2461695, 8820639, 3, 'As usually happen picture property lose will. Inside light after management. Enter fish way girl show music would heart. Per hour north center.', '2026-01-05');
INSERT INTO ratings VALUES (9477004, 4278613, 7759286, 4, 'Wife call compare where edge born. Program actually especially field as check. Second character dream town.', '2026-02-15');
INSERT INTO ratings VALUES (1728883, 7470714, 1939454, 5, 'Charge personal prove want gas drop place. Affect model effort any. Mission low people say side stop material allow.
Size say enter degree skin ever. Give hold might lead religious daughter himself.', '2026-02-01');
INSERT INTO ratings VALUES (7020326, 2654571, 4224585, 5, 'Amount by lay skin science. Child change possible hotel particularly apply degree. Senior blue with development.', '2026-04-15');
INSERT INTO ratings VALUES (8787767, 5683800, 4186470, 1, 'Oil picture chance garden. Small medical energy away by herself box reflect.
Half same growth technology attorney less start.', '2026-04-02');
INSERT INTO ratings VALUES (9301486, 3655021, 7094490, 3, 'Speech American where story.
Site model wife.
Skin size your arrive particular blood. Necessary garden blue region others contain ahead. Force include prepare son sure professional.', '2026-01-18');
INSERT INTO ratings VALUES (7218201, 6597799, 1556467, 5, 'Piece clear truth traditional different. Yet air player. Key he Republican site great half common. Positive back president order treat a.', '2026-01-02');
INSERT INTO ratings VALUES (2196371, 2974225, 9647986, 5, 'Marriage cut woman cold. Southern near trade fast let believe white. Activity learn they between music focus improve.
Hot state prove middle safe pay. Product recognize blood loss man beyond.', '2026-02-14');
INSERT INTO ratings VALUES (1235957, 7180424, 8192045, 5, 'Factor son role experience then sea hundred find.
Often base country his life. Would economic plant commercial may. Everything set change red usually two.', '2026-04-13');
INSERT INTO ratings VALUES (5398041, 4953636, 4497853, 5, 'Leave art rich floor staff. High glass all how production month. Hit figure up call.
Look part wife from risk. Marriage federal school hotel pass also view. Firm on factor.', '2026-04-28');
INSERT INTO ratings VALUES (3676812, 3179127, 8820639, 5, 'Leg entire most. Product teacher claim time there growth she. Try accept door admit we large.
Else dinner PM fish. Want interest region likely. Item group home.', '2026-02-26');
INSERT INTO ratings VALUES (9147637, 5286908, 9969805, 3, 'Land popular each nearly friend form. Partner employee situation reality gas.
Under natural appear another speech. Chance region raise top happy between.', '2026-02-19');
INSERT INTO ratings VALUES (7841652, 8371206, 2117824, 3, 'Shake piece different PM. Table then describe of.
Car life outside and player. Food who up establish ask close later.', '2026-02-17');
INSERT INTO ratings VALUES (6617598, 3211200, 7850066, 4, 'Somebody PM attorney oil. Television list prevent response old manage under.
Whole high professor rock use ask. Field available sing four not tell decide. Toward all financial.', '2026-01-22');
INSERT INTO ratings VALUES (9066531, 4141937, 5430391, 2, 'Break next yes necessary let onto common. Per national believe whole wrong.
On cost use reason foreign. Already wait foot yeah various campaign. Mrs suffer seat out for life quite garden.', '2026-05-29');
INSERT INTO ratings VALUES (4273634, 7865203, 6433324, 3, 'Child together politics better game keep imagine. Rule what include all present. Note own attention direction low.
Former fact fund herself beat century child true. Myself live force sport ground.', '2026-04-30');
INSERT INTO ratings VALUES (1743948, 9999465, 5146931, 1, 'Suffer most next group performance. Pay color two fall into soldier. Heart think team method much spring yourself whom.', '2026-01-27');
INSERT INTO ratings VALUES (5281562, 6762919, 9582378, 5, 'That drop federal former skin investment. Administration medical treatment. Think after piece TV project.', '2026-05-28');
INSERT INTO ratings VALUES (7962488, 8517884, 9376507, 2, 'Bit challenge our song century question rich. Student country talk as figure. Else assume he ever some let no.', '2026-04-12');
INSERT INTO ratings VALUES (9031244, 9166027, 5855521, 1, 'Interview his law five car. Possible garden line everything without.
Child hold leg list cultural interesting. Ok fact wife guy sport. Glass truth later tonight staff sometimes.', '2026-02-21');
INSERT INTO ratings VALUES (2262336, 4809618, 9649921, 2, 'Send face table reduce argue skin. Than drug power service side.
Wear prove address. Protect last pay less standard dream.
Dark specific environmental mission.', '2026-03-03');
INSERT INTO ratings VALUES (2841499, 7540759, 3294929, 4, 'Compare wonder country but. Large PM but part environmental prepare. Piece generation sea floor including campaign prevent recognize.', '2026-01-21');
INSERT INTO ratings VALUES (5139912, 4678821, 5160345, 4, 'Herself bank prepare sign. Republican seem already different.', '2026-03-13');
INSERT INTO ratings VALUES (2408440, 6066735, 6433324, 1, 'Help office skill movement ask group. Dog fish gas drug control skill. Wrong beat strategy save. Boy natural coach officer or put.
Conference charge total bag.', '2026-01-17');
INSERT INTO ratings VALUES (9052779, 8951574, 3382876, 1, 'Foreign cell option training. Analysis option reason conference huge store. Number last check large while organization common.', '2026-05-23');
INSERT INTO ratings VALUES (9951816, 7503774, 7260938, 4, 'Administration staff international expect. End such question remain conference teach bed. Idea sound beautiful seem.', '2026-02-02');
INSERT INTO ratings VALUES (1145112, 7011025, 4841212, 4, 'Thousand impact they yet win suddenly. Film last note treat two. When southern huge wall Republican.', '2026-04-29');
INSERT INTO ratings VALUES (8509900, 5720403, 7220120, 4, 'Option store south you. I hospital front move represent service agreement.
Time war source my to television road. Behavior key us participant.', '2026-05-23');
INSERT INTO ratings VALUES (6394291, 5761385, 4911547, 2, 'Anyone similar reflect mouth form. Mr such able government both media.
Think respond good night. Whose over benefit prove likely. Still forward deal itself scientist easy.', '2026-01-02');
INSERT INTO ratings VALUES (5713103, 3738778, 6787581, 1, 'Consider attorney my where major important. Suddenly recently describe mouth top moment benefit thought. Pressure personal describe civil similar lay.', '2026-01-22');
INSERT INTO ratings VALUES (5664862, 2541341, 4580667, 5, 'But throw guess her decision. Although town not onto claim. Of artist final.
Least rest main night act. Thousand big yard or old cost. Middle radio quite recognize prove network.', '2026-04-12');
INSERT INTO ratings VALUES (8813707, 1128509, 3429260, 5, 'Little add production plant recently ball away. Door blood nature according. If dark president learn certain.', '2026-02-17');
INSERT INTO ratings VALUES (5858596, 8173474, 7821647, 3, 'Financial glass cause drive job. Social fire two thus. Resource budget class tax.
Deal television claim generation. Tree right case. During training hold write tonight.', '2026-01-19');
INSERT INTO ratings VALUES (8293542, 4197805, 8539128, 4, 'Maintain various friend message purpose rich. Sound similar risk writer friend.
Reach sense show. Finally manager head cup image section.', '2026-03-20');
INSERT INTO ratings VALUES (4689952, 3816929, 5843018, 4, 'Crime movement future everything.
Recently everything room least. Model exist middle drop miss. Arm money themselves husband. Any visit research thousand yeah very require.', '2026-03-31');
INSERT INTO ratings VALUES (5205097, 4809618, 8600080, 1, 'For across page agree will. Than Mr light age soon.
Order two great course financial ago. Indicate win everybody fear knowledge.', '2026-02-24');
INSERT INTO ratings VALUES (5613431, 3977299, 1084740, 5, 'Statement technology treat federal local evidence. Civil nor experience he so himself. Bar it ground yes.
Before condition follow. Involve hand example concern stand thousand note.', '2026-03-31');
INSERT INTO ratings VALUES (6713349, 2974225, 3788765, 4, 'Serve defense prevent. Too back statement tend story away plan fish. Old choice half put end. Material free fast though yes church tell.', '2026-03-13');
INSERT INTO ratings VALUES (5526215, 2566046, 8457243, 1, 'Community doctor price recent safe.
Enough compare put official of there none. In would from source high occur task within. Off fund any.
Couple crime all usually prepare write act.', '2026-03-17');
INSERT INTO ratings VALUES (1651648, 6564332, 9840773, 4, 'Meet yeah full operation. Success cold difference wife. Practice participant involve probably.', '2026-02-24');
INSERT INTO ratings VALUES (3240380, 5781985, 5245347, 1, 'Network get safe. Before should summer from or high. Administration floor strategy military never cultural.
Ground kid yourself ok step.
Game magazine they yes small figure lose.', '2026-01-26');
INSERT INTO ratings VALUES (8260571, 6856915, 4165078, 1, 'About hotel home enough. Expert last cell field quality. Say scene mission cover wife. Shake Mr piece shoulder.
Thus culture look maybe fact while. Green almost manage how.', '2026-05-27');
INSERT INTO ratings VALUES (3943084, 5761385, 7618732, 1, 'Food leader music people. Range put crime hair. Seat wear development can defense customer. Sea product along ball herself should.
Property high other show appear. Man treat project clear.', '2026-05-24');
INSERT INTO ratings VALUES (7055875, 1831583, 7300889, 1, 'Back into event fear after. Performance add pay within rest.
When there half rather girl stuff relationship. Into respond perform deal. Station notice fight option service again.', '2026-03-13');
INSERT INTO ratings VALUES (9739183, 8395495, 7648856, 1, 'Yard nor beat force bed. Financial decide floor. Feeling tonight soon unit sister whom remain.
Box huge finish. And through behavior benefit weight main sport. Alone smile fund.', '2026-03-06');
INSERT INTO ratings VALUES (5904952, 3423018, 4138126, 5, 'Whole week your speak phone walk happy only. And want management.
Several summer age growth Mr voice why. Hotel item project would.', '2026-05-03');
INSERT INTO ratings VALUES (4068864, 5820495, 4229693, 2, 'Story but her adult better stock wear force. Believe above around woman fall kid. Pull mention hour notice long market fund.', '2026-03-09');
INSERT INTO ratings VALUES (6308162, 9166027, 5994883, 2, 'Option movie produce can remain or. Risk why center door while. Attack buy meeting serious five building.', '2026-01-19');
INSERT INTO ratings VALUES (4290181, 3784950, 8356612, 1, 'Medical environmental question write much best remain. Feeling color yard part back. Success owner check land language more mind against. Window city ago later interest standard opportunity.', '2026-04-03');
INSERT INTO ratings VALUES (7690174, 1128509, 2094934, 2, 'Expect them whatever company describe trip. Pm party look current drive. Produce pull eight within night education once.', '2026-01-31');
INSERT INTO ratings VALUES (2914357, 8525725, 1362332, 2, 'Policy city blood billion. Assume agreement avoid the end letter. South eat blood perhaps learn test whether.', '2026-02-27');
INSERT INTO ratings VALUES (3017944, 5053396, 5253868, 1, 'Build old thus group. Interest mission officer prevent start. Treatment consider we low reveal.', '2026-03-14');
INSERT INTO ratings VALUES (9842773, 2940416, 8357045, 4, 'Gas account relationship on whatever. Later meet identify condition goal power.
Staff no across out agreement lead. Interest attention four national air east. Hear ahead health old girl.', '2026-04-10');
INSERT INTO ratings VALUES (3433187, 6955370, 3337487, 5, 'Reason citizen than seem. They friend court senior along parent. Analysis fill street task phone want set.', '2026-05-20');
INSERT INTO ratings VALUES (5018787, 2024582, 4501881, 5, 'Program relate or explain respond short. Change allow large station. Performance tonight deep almost woman one. Thus president receive cup nice work rise.', '2026-01-21');
INSERT INTO ratings VALUES (2251695, 3166127, 1101672, 2, 'Spend continue discussion participant. Senior seat human serious vote. Tell rise two cultural notice artist authority.', '2026-05-04');
INSERT INTO ratings VALUES (1647561, 2461695, 1025551, 5, 'View sign specific entire performance. Still throw doctor. Former notice describe.
Close it TV send know out soldier available. Yard rather ahead message. Place amount section policy.', '2026-04-19');
INSERT INTO ratings VALUES (5427292, 4017366, 1801299, 2, 'Art week upon will statement. Especially tell allow lay door culture. Movement she go current everyone analysis glass during.
Pm try beautiful message. Ahead own whether quickly worker toward.', '2026-01-20');
INSERT INTO ratings VALUES (2103263, 3816929, 9269310, 1, 'Claim crime baby cause effort. Market public make practice sell.
Billion successful whose travel dinner drive. While artist research.
Style garden gun. Near accept officer clear accept.', '2026-03-25');
INSERT INTO ratings VALUES (5168979, 9810226, 3788765, 5, 'Strong current traditional without husband organization.
Anything authority financial show mean than. Eye finally arrive. Than fish court.
Difference factor ability structure speak newspaper.', '2026-04-21');
INSERT INTO ratings VALUES (2733662, 8391185, 7409070, 2, 'Doctor much future most picture leave wide. Maybe social threat or sister place. Mr make ahead low. Boy home debate indicate attorney phone fund work.', '2026-02-02');
INSERT INTO ratings VALUES (2714571, 2654571, 8669614, 4, 'President lawyer special remember foot win already. Difference why great best natural pass everything.
Office herself bring theory article describe card.', '2026-03-14');
INSERT INTO ratings VALUES (7761893, 7092950, 3302290, 3, 'Listen once behavior four I. Interview top best.
One finally care no heart truth. Create section go. Wall machine general head.', '2026-01-03');
INSERT INTO ratings VALUES (1302157, 2881744, 4356416, 3, 'Hard debate total finally reason work. Describe fish road scene themselves could. Control American administration positive matter behind. Small design return during reality.', '2026-02-13');
INSERT INTO ratings VALUES (2448991, 8845953, 1768067, 1, 'High cultural light loss campaign probably until. Smile threat forward suffer believe.
System maybe car hear series. Stop possible candidate fine parent center issue value. Little citizen loss.', '2026-03-16');
INSERT INTO ratings VALUES (1284796, 6351366, 3232025, 1, 'Sort forget together thus television case. Audience protect what both difficult. Value least family lose election.', '2026-01-09');
INSERT INTO ratings VALUES (5028226, 3977299, 9327068, 3, 'Relate include world sure tell might we. Him agency speech among we fund under.
Same science go mother until. Outside environment trade generation dog ago although.', '2026-01-07');
INSERT INTO ratings VALUES (9283203, 5798843, 8832974, 2, 'Next fine last truth better baby. Organization discussion yourself full case.
Song against student often. According project truth. Seem significant language wife participant.', '2026-05-28');
INSERT INTO ratings VALUES (9610905, 4636158, 5017380, 3, 'In note ground professor but. Nice become data cultural region. Name figure doctor score film agree.', '2026-01-28');
INSERT INTO ratings VALUES (2503954, 8155808, 8360355, 5, 'Bar matter edge bed finish worker. Physical we water word director morning movement. Every end color education cover.', '2026-01-24');
INSERT INTO ratings VALUES (1424073, 7456753, 8425634, 5, 'Office include impact student realize find. Surface fire here.', '2026-02-07');
INSERT INTO ratings VALUES (8846052, 7242085, 7282020, 4, 'On order accept again office pressure strategy. Ten court address few real couple western.
Surface consider focus notice author feeling. White least responsibility.', '2026-04-27');
INSERT INTO ratings VALUES (3104746, 5277747, 3229060, 1, 'Our fire fish concern he employee sport. Than chance attorney reveal four quickly view.
Front rather popular director war for book spring. Way across range behavior tend miss mouth.', '2026-02-25');
INSERT INTO ratings VALUES (6150268, 3179127, 5617426, 2, 'Stay get voice back section. Budget decade authority office. Central go indicate.
Air yard player shake suddenly turn. Concern might analysis both care claim.', '2026-05-17');
INSERT INTO ratings VALUES (8647681, 5332651, 1768067, 2, 'Reach teach high participant property.
Listen per actually result oil. Seat toward force building recently rich different. Heavy sister feeling. Hear seven agent Congress.', '2026-05-29');
INSERT INTO ratings VALUES (3522769, 9263093, 7592431, 4, 'When fly born ok really during. Husband base simple reflect affect effect.
Realize help catch base trouble ground. Cover power system institution majority. Still small last act federal great at.', '2026-02-09');
INSERT INTO ratings VALUES (7904362, 2957480, 9417673, 4, 'Sort central much including moment. Those support exist majority ten herself.', '2026-04-26');
INSERT INTO ratings VALUES (4304515, 8525725, 1128351, 5, 'Walk lay above free start former. Form forward blue yourself less. Break under necessary. Remember law recently per skin try they.', '2026-04-26');
INSERT INTO ratings VALUES (1924687, 3106291, 6433324, 5, 'Increase seven such scene how. Trip rather mention million leader. International it minute always official decision.', '2026-04-06');
INSERT INTO ratings VALUES (9089852, 8446918, 2775069, 4, 'Power discover despite recognize thousand. Fire cost use newspaper represent. Around heart nothing over perform talk. I simple matter push.
Material do color here.', '2026-01-01');
INSERT INTO ratings VALUES (8006846, 2483269, 2979638, 1, 'Serve low me husband weight none. Seek sing say enter situation.
Son marriage operation all scene against.
Build enough watch goal. Set also century sign. Inside door possible sister.', '2026-05-08');
INSERT INTO ratings VALUES (4471652, 4197805, 5805421, 2, 'News me pressure same sound arrive. Cup standard technology fire reduce. Forget environmental fund else she here without.', '2026-01-15');
INSERT INTO ratings VALUES (8852990, 8805416, 6288275, 5, 'Discuss true address clearly consider. Force either six program sort. Father us answer teach sense stop.', '2026-02-23');
INSERT INTO ratings VALUES (2951491, 8155808, 7770284, 1, 'Culture national tonight. Community those resource read drive final open. Father reduce itself we under hit.', '2026-01-24');
INSERT INTO ratings VALUES (5761562, 4141937, 8820639, 4, 'Pattern set about teacher outside. Local parent through between include federal approach beyond. Cold section public report but.
Want with outside give partner. Plan even new decide.', '2026-01-24');
INSERT INTO ratings VALUES (9904624, 1831583, 2718238, 4, 'His plant season election day area. Foot by best single he newspaper. Let style short feel list son.
Social page fill. Always might box film peace test hope. Fire trial western.', '2026-05-21');
INSERT INTO ratings VALUES (2001783, 5408609, 3984898, 1, 'Score almost generation radio herself car stage. Wear table coach somebody capital pattern. Finally join power sister stay.', '2026-02-20');
INSERT INTO ratings VALUES (5472116, 5720403, 5017380, 4, 'Around get technology thought try lose near. She alone movie paper debate owner artist someone.', '2026-04-27');
INSERT INTO ratings VALUES (7403859, 4865004, 3382876, 4, 'The enough stage cause late. Him take than expect be effort be under. Than instead chance city street dark discover.', '2026-05-11');
INSERT INTO ratings VALUES (9574049, 5408609, 7640982, 3, 'Entire discuss he expert can executive. Successful buy seat start night tax.', '2026-01-10');
INSERT INTO ratings VALUES (6037103, 2347757, 1003920, 2, 'Sort public heart fast where western hear discussion. Nearly Mr worry decision environment just.', '2026-03-17');
INSERT INTO ratings VALUES (8546292, 8453354, 9837984, 3, 'Any article may operation ever within either. Charge sort group bit. Card resource state its could year region.', '2026-03-28');
INSERT INTO ratings VALUES (2258919, 3440276, 2111920, 3, 'Newspaper it force foot. Scene why list worker. Party pay old dark mind view.
Mr let consider truth network. Say different line peace right there. Turn still interest baby.', '2026-05-13');
INSERT INTO ratings VALUES (3748702, 2347757, 2134249, 5, 'Under direction account along story.
Heart reveal just air capital. Operation apply rock character.
Concern whether push low market itself in. Couple do sort room do someone trade trial.', '2026-01-07');
INSERT INTO ratings VALUES (7788122, 5497703, 3548200, 3, 'Medical gas finally question. Few listen which water its.
Music word clear good minute group. National image cut part staff.', '2026-02-16');
INSERT INTO ratings VALUES (1601875, 8845953, 5888739, 5, 'Night traditional state understand. Them idea news tough national product experience.
Today particularly operation remain Mr argue. Already activity news cup.', '2026-01-21');
INSERT INTO ratings VALUES (1997094, 9999465, 5888739, 1, 'Class parent water consumer none well series. Study well Republican later discuss hard our manage.', '2026-04-30');
INSERT INTO ratings VALUES (6937032, 5683800, 1939454, 4, 'Your them spring arm common she majority story.
Want myself write situation business end feeling.
Skill red new nation. Yet common hair remember. Tonight once continue play Congress hard above.', '2026-02-05');
INSERT INTO ratings VALUES (8551838, 4278613, 9837984, 1, 'Important will live life. Treat stock perform I city rock. Bad night man to rest or force.', '2026-05-19');
INSERT INTO ratings VALUES (2803835, 9258677, 1017327, 5, 'Material improve nice in member collection. Seat travel TV evidence too here.', '2026-02-04');
INSERT INTO ratings VALUES (5997098, 7797269, 4841212, 5, 'Whether threat old right sort. Senior land when your why visit position job. Skin house region receive tough line make.
Choose surface deal wrong yes writer. Action security hear mother Mrs only.', '2026-02-24');
INSERT INTO ratings VALUES (5901914, 8395495, 6260587, 2, 'Right weight with. Hear which economic detail science group. Management modern go structure positive director.', '2026-04-11');
INSERT INTO ratings VALUES (6471341, 9258677, 8600776, 4, 'Amount response husband that century role language difference. Real deep charge party again staff quite thus.
Case trial try enjoy president. Fly instead person agree establish without your war.', '2026-02-12');
INSERT INTO ratings VALUES (1420540, 3440276, 3107948, 3, 'Hand each home one paper city.
Answer resource senior media prevent success anything. Look than sense society particular. Imagine church treat hotel ever sport player.', '2026-04-05');
INSERT INTO ratings VALUES (2055114, 3135062, 8539128, 4, 'Yourself out ever. Ten worry month. Learn effect call together.
Your radio tough. First place because popular federal whether. Reason bag contain base.', '2026-02-25');
INSERT INTO ratings VALUES (6886766, 7412516, 1118405, 2, 'Economy fish south music turn. Stage question information forward lot hotel. Watch hand election defense anything long.
Half country yet Congress. Dinner during authority.', '2026-01-12');
INSERT INTO ratings VALUES (6944381, 4678821, 3232025, 4, 'Prevent way budget condition his. In tonight start company. Current along player again seek performance.
Treat nothing time care beyond matter window. Gun floor increase serious individual candidate.', '2026-04-13');
INSERT INTO ratings VALUES (9733086, 5563188, 3548200, 4, 'Boy deal cultural cut.
Human news hotel hair race someone light. Seem its less boy whatever. Rock real economy together film water.', '2026-05-25');
INSERT INTO ratings VALUES (6342755, 6597799, 3229060, 2, 'Forget able alone. Style song control skin which. Democratic food turn.
Agency rule experience. It recently fire.', '2026-05-05');
INSERT INTO ratings VALUES (7266330, 5053396, 9235735, 1, 'Onto focus coach after nice travel piece. Use smile decide bring near become activity provide.', '2026-03-30');
INSERT INTO ratings VALUES (1613744, 4953636, 3261263, 4, 'Real even realize of memory along tend. Kind shoulder ago line big condition though. Suffer go meeting such.', '2026-01-16');
INSERT INTO ratings VALUES (3387859, 4287773, 7444700, 2, 'Training man check then during kitchen whatever. Specific federal friend we. Deep man remember get tend mouth.', '2026-02-23');
INSERT INTO ratings VALUES (4561293, 6856915, 3229588, 2, 'Night get answer bank more act while. Me stuff side help.
Majority no fast late table. Drive quality heart. Near vote audience PM.
Cost with else week professor. Participant career could level.', '2026-02-24');
INSERT INTO ratings VALUES (5473702, 4859106, 3380796, 4, 'Space side democratic where especially be her. Who second expect language piece fast physical.
Stand toward song. Arrive six ten attention gas. Push phone four answer.', '2026-02-19');
INSERT INTO ratings VALUES (8458783, 7083414, 6884610, 4, 'Car like radio fill agree next week. Describe expert take sing. Half fire way whom.
Memory north Congress suddenly style development our. Child main owner later paper. Religious by play lose trip.', '2026-05-28');
INSERT INTO ratings VALUES (8551246, 4747967, 2071840, 4, 'Story size police short food research who. Act identify bad magazine media glass nor suggest. Will second also oil social side stock kid. Term unit hot citizen enjoy.', '2026-04-10');
INSERT INTO ratings VALUES (7232974, 3343612, 4885859, 5, 'Yourself little hear measure science. Late without discussion message fish mother.
Material trouble reflect sit hold field local serious. Week only film region walk foreign.', '2026-04-14');
INSERT INTO ratings VALUES (8310816, 7083414, 2651268, 1, 'Baby since individual short. Young father put worker answer far picture size.
Home provide seat fund behavior decide. Land box a ago ready remain Congress.', '2026-01-16');
INSERT INTO ratings VALUES (5274464, 9288911, 3668668, 2, 'Above last just contain. Standard information candidate decade perhaps economic national image.', '2026-04-22');
INSERT INTO ratings VALUES (1333250, 7422573, 8566074, 2, 'Near bank chance beyond save experience. Power hard what although perhaps. Office eat upon fear.', '2026-05-04');
INSERT INTO ratings VALUES (7377524, 3681623, 5410579, 3, 'Least daughter see think finally. Quality open country be military as tonight.
Those foreign traditional performance just. Word style very identify employee.
Cause two military school.', '2026-04-07');
INSERT INTO ratings VALUES (7885150, 9218661, 2775069, 3, 'Feeling picture unit play ok.
State street listen create message bar actually.', '2026-01-02');
INSERT INTO ratings VALUES (8548493, 3423018, 4497548, 1, 'Example if chance pick also know cut. Hot compare ago fine model crime prove. Prove third today important.', '2026-01-19');
INSERT INTO ratings VALUES (5507097, 7242085, 2080096, 5, 'Kid sound tough as. Various anyone while father need. Ability think position relationship.', '2026-01-18');
INSERT INTO ratings VALUES (6469218, 2483269, 4187430, 1, 'Job condition business forward window. Practice soon especially what. Save condition positive child where candidate line.', '2026-04-19');
INSERT INTO ratings VALUES (6480906, 9622165, 1277419, 3, 'Hour for character structure gas. Kind car real fund clear win current. Now reduce television environment tell scientist.', '2026-05-30');
INSERT INTO ratings VALUES (6651860, 3577168, 4885694, 1, 'Show international themselves impact affect management per suggest. Similar people simple response may.', '2026-05-28');
INSERT INTO ratings VALUES (1357648, 3577168, 7648856, 4, 'Item suggest attention least. Movie man middle crime. Head eat pretty according.
How contain wrong condition pick head.', '2026-03-17');
INSERT INTO ratings VALUES (7118445, 8594972, 8827461, 2, 'Production ago job include west operation work town. Try customer company physical. Century serve probably specific gun.
Media stop party pass respond. Imagine whose standard news mother feeling.', '2026-02-12');
INSERT INTO ratings VALUES (7834849, 8668826, 6468600, 1, 'Give participant of record main western. Chance light past everything why option. Pass week foot up.
Whatever have themselves responsibility think just include door.', '2026-03-29');
INSERT INTO ratings VALUES (1378521, 3418931, 1983030, 4, 'Professional recognize run often.
Son social forget high. Top set recognize tax.
Movement some economy issue human. Threat show her environment. Exactly first recognize bit yeah cover rest.', '2026-04-02');
INSERT INTO ratings VALUES (7939139, 5009563, 7663248, 3, 'Bit computer pretty believe section try. Cultural across world probably. Address nearly its live keep paper.', '2026-03-12');
INSERT INTO ratings VALUES (8377168, 1381520, 2428956, 5, 'Avoid church pretty baby law. Power red suffer.
Street bar money cut. Star election explain total accept thought. Beat certain expect.', '2026-03-20');
INSERT INTO ratings VALUES (9760523, 2566046, 8928018, 5, 'School candidate wish student political goal politics nearly. Voice find us remain organization.
End include which base. Say story car particular want culture. Even ground store very who experience.', '2026-04-05');
INSERT INTO ratings VALUES (8051356, 5563188, 9857615, 4, 'Matter improve vote difficult degree Democrat. Because arrive whatever significant.
Get skill usually develop arm care. Away degree little.
Beat watch score street. Protect career join back he skill.', '2026-05-12');
INSERT INTO ratings VALUES (5664777, 5185820, 2013782, 5, 'Cost simply little front reflect thank east TV. Wind environment firm you. Care bag government according long.', '2026-05-15');
INSERT INTO ratings VALUES (3852569, 8622373, 9647986, 2, 'Hope institution national his reality power cold. Administration similar sister. Anything mission religious create style reduce box beat.', '2026-03-05');
INSERT INTO ratings VALUES (7310702, 2013023, 4787447, 4, 'Sense change baby name song. Provide begin I responsibility south rich traditional. Yourself technology firm health. Physical along yard general discuss.', '2026-04-20');
INSERT INTO ratings VALUES (7257032, 7011025, 7207091, 2, 'Policy right mission up customer fish. Notice color side note mouth newspaper. Pull here reveal everything green truth.
From raise hope care pay power get. Score through window every bit.', '2026-04-16');
INSERT INTO ratings VALUES (4860923, 6976359, 6839691, 5, 'Support hit million middle. Court require reflect eight produce recent. What take fall forward we race.
Walk name use often style matter. Look offer level. Attorney this someone rest something admit.', '2026-01-20');
INSERT INTO ratings VALUES (2508961, 4747967, 7648856, 3, 'Example suffer control available scientist reason. Various society behind leg all government. Democratic reduce thing argue.', '2026-02-14');
INSERT INTO ratings VALUES (1153998, 2975287, 7559111, 2, 'Top create name fast money. Marriage today stage.
His physical soon. A member theory offer. Skin success huge which story.', '2026-04-24');
INSERT INTO ratings VALUES (6257625, 6856915, 3316632, 3, 'Campaign age serious detail technology lose. Within scientist husband method.
Tax action reach message build unit film. See individual however similar point. Example yet after scene great.', '2026-05-24');
INSERT INTO ratings VALUES (8474266, 7470714, 7569872, 3, 'Final present cup movement treat agree building. Indeed security affect pay happy significant.
Course thank use send physical debate thing general. Wife card human black.', '2026-03-14');
INSERT INTO ratings VALUES (1710327, 2422409, 4050052, 1, 'Evidence reduce Republican happen little.
Only building product help may poor Republican. Animal allow relationship author compare ball care.', '2026-05-07');
INSERT INTO ratings VALUES (3788317, 2940416, 3380796, 2, 'All service executive federal. Main wait feel each. Rich so among home recognize decade picture live. Avoid thank quality.', '2026-05-01');
INSERT INTO ratings VALUES (6959013, 6597799, 4205310, 1, 'Magazine home be amount. Both deep hot hand. Low owner administration research traditional benefit. Science poor cover rest far receive.
Or boy individual interview large laugh cover.', '2026-04-04');
INSERT INTO ratings VALUES (3913976, 8668826, 5337919, 3, 'Face fall how individual politics get their drug. Argue man record turn stay maybe culture month.', '2026-05-05');
INSERT INTO ratings VALUES (4287351, 8896880, 2828272, 4, 'Establish consumer home administration government case say determine. College look wear voice think. House local surface especially analysis kid. Buy toward fast such too.', '2026-03-14');
INSERT INTO ratings VALUES (4482792, 4678821, 1939454, 4, 'Guess stay finally commercial exactly recently amount. Service culture garden best where determine. Act sense such station morning industry.', '2026-01-19');
INSERT INTO ratings VALUES (9440327, 6091790, 4868063, 2, 'Car this way usually. Time TV debate.
Government determine someone movement challenge. Large boy play onto.
Mother continue effort population door.', '2026-03-27');
INSERT INTO ratings VALUES (5919018, 7660641, 1554195, 4, 'Group particular dark cause different similar sure. Situation suffer walk large.
Little leave some may note. Level have entire probably history. Tonight floor age grow attention sit itself.', '2026-04-22');
INSERT INTO ratings VALUES (4161322, 3963722, 4055794, 3, 'From computer police true life possible.
Often eight dark mind letter or about. Tv response recognize article professor center side chance. She total figure item.', '2026-04-14');
INSERT INTO ratings VALUES (5303894, 2729297, 6260587, 1, 'Bring such purpose artist hit something.
Information eye maybe wish drug accept south. Necessary dog country region war strategy situation though. Road trial method own beat material.', '2026-04-09');
INSERT INTO ratings VALUES (7591130, 2265297, 4058181, 3, 'Wall dark term evening boy attention section there. Process hour buy sea practice various.
Enjoy deal news design onto capital hand specific.', '2026-04-07');
INSERT INTO ratings VALUES (8569950, 7412516, 1277419, 5, 'Red off say hotel model reality family. Hope several bed serious lead.
Local magazine body agency space this scientist. Go local budget yeah PM begin politics.', '2026-04-27');
INSERT INTO ratings VALUES (5312265, 9356593, 5041690, 4, 'Anyone image include sister to. Different tend town prove audience. Money adult learn foreign civil defense success.
Step try food food list behind. Adult debate until spring push.', '2026-04-20');
INSERT INTO ratings VALUES (6079107, 5497703, 1184323, 3, 'Writer eat time two after stuff nor. Individual night right wife. Base go mother Democrat.
Often yet quality look material clearly.', '2026-03-29');
INSERT INTO ratings VALUES (9803372, 7503774, 4860180, 4, 'Recent reflect among professional quickly central exactly. Kid commercial turn financial possible go test wish. Seem space perform.', '2026-03-16');
INSERT INTO ratings VALUES (2159456, 1083307, 6288275, 2, 'Option school open use give foreign. Name water week student surface film. Behind green health occur mother. Operation despite hand interest though history should laugh.', '2026-01-12');
INSERT INTO ratings VALUES (2527430, 5053396, 5160345, 4, 'Allow church sing improve listen political one last. Such of strong summer. Thought style true people mother.', '2026-05-15');
INSERT INTO ratings VALUES (9807828, 6527815, 9190463, 3, 'Can maintain him. Local people everything land staff moment. Half third win return.
Beautiful why cup talk whatever. Trial must friend table Democrat themselves foot.', '2026-05-13');
INSERT INTO ratings VALUES (7234557, 8594972, 8694911, 1, 'House behind defense keep instead little. Painting since threat choice future.
Worker manager friend contain close together opportunity. Girl gun movie check plant organization.', '2026-02-07');
INSERT INTO ratings VALUES (7002149, 6589761, 9190463, 5, 'Back hundred ok whether since. Answer mention worry get wonder ready. Hotel develop next structure mind. Early thought rest week those.', '2026-04-15');
INSERT INTO ratings VALUES (4735977, 6597799, 8627402, 5, 'Guess office beat modern.
Memory finally analysis past of lay. Fill hot friend degree push guess could.', '2026-02-01');
INSERT INTO ratings VALUES (5594445, 3577168, 3580504, 3, 'Quality tough year visit. Answer certain up ever trouble study.
Notice raise find rise left. Spring wall structure. Sense positive include care.
Daughter why song politics article.', '2026-05-04');
INSERT INTO ratings VALUES (6442024, 3074155, 1554195, 4, 'Reveal continue student because out language age. Dog campaign eye foot. Democrat during now black test choose participant.', '2026-01-29');
INSERT INTO ratings VALUES (6568546, 8755012, 2434115, 2, 'Ability become soon city hold discuss. Week bed situation consumer pull smile. Rich discussion charge art learn enter.', '2026-01-22');
INSERT INTO ratings VALUES (3507798, 9999465, 1359170, 5, 'Market order economic land but program read know. Trouble table happy best particular at seven.
Claim fill street under suddenly without.', '2026-03-02');
INSERT INTO ratings VALUES (1163424, 8809406, 2775069, 4, 'Board heavy nation yeah avoid hit produce together. Claim former so company life.
Without receive above world receive. Top artist state simply. Recent more however leave system rate.', '2026-04-02');
INSERT INTO ratings VALUES (9693318, 4865004, 7243278, 5, 'Anyone until skin catch between town. Its others white crime toward end. Than all realize force.', '2026-01-10');
INSERT INTO ratings VALUES (9755297, 2216117, 7663248, 2, 'Staff here throughout provide range practice language. Seat race to place people born.
Job fast picture city school. Concern wife young spring. Share edge radio glass.', '2026-01-25');
INSERT INTO ratings VALUES (1046056, 4953636, 6717727, 4, 'Season several bit practice language responsibility. Science doctor central pay charge worker his certain. His while fund fly.', '2026-02-05');
INSERT INTO ratings VALUES (4958347, 2680208, 7559111, 5, 'Pattern but or must guess thousand. Ground in although rock. Degree hot power southern. Make management may factor street how.
Painting tough become why most science. Pm to should idea.', '2026-01-03');
INSERT INTO ratings VALUES (1595182, 3944798, 2979638, 2, 'Keep guess set project class. Leave attention option anything address important turn. Machine report who site through despite.', '2026-04-12');
INSERT INTO ratings VALUES (3127379, 3738778, 9322743, 3, 'Guy into ever. Likely glass really point kid audience. Any meet half opportunity.', '2026-01-21');
INSERT INTO ratings VALUES (2622467, 2680208, 8457243, 1, 'Focus reality soon region still. Price today station inside.
Event what drop thus general film through. Significant general true example themselves career. Claim because chair issue.', '2026-04-01');
INSERT INTO ratings VALUES (1808658, 3352316, 2502371, 4, 'Pretty artist want. Early have go cost peace run possible.', '2026-02-07');
INSERT INTO ratings VALUES (5395102, 3106291, 8399090, 2, 'Police executive become. Top painting group start always wear.
Push could east. Figure spring difference like. Receive black expert short professor.', '2026-04-13');
INSERT INTO ratings VALUES (4560598, 2905071, 4737821, 3, 'Skill wish like than. Likely own away deal look.
Dark on want modern plant early and. Heavy half blood now arrive lose. Agreement appear wind be take cover police.', '2026-03-12');
INSERT INTO ratings VALUES (6369098, 5719835, 6288275, 3, 'General scientist sometimes yet. Everything small institution better effect become.', '2026-02-17');
INSERT INTO ratings VALUES (2860582, 2654571, 4162870, 5, 'Live tough whatever letter. Base his several room. College particular throw throw give book.
Interesting remain some Republican. Kid industry Democrat edge computer senior myself.', '2026-05-17');
INSERT INTO ratings VALUES (7617667, 3074155, 1148814, 2, 'Itself director maintain for still stop choose evidence. Visit yard college act the once begin.
Instead understand shake throughout worker future. Teach stock everything body.', '2026-03-27');
INSERT INTO ratings VALUES (8486580, 1253566, 5354373, 3, 'High peace right wind mission technology product. Small sister woman step. Response song drop party.
Fire work thus return top. Network particular Republican back leader never couple lead.', '2026-03-02');
INSERT INTO ratings VALUES (7858574, 6526839, 4971923, 4, 'Source though court study. Guy prepare thousand whom case film like.
Decision per enough interest six strong watch. Recently daughter red last high.', '2026-01-10');
INSERT INTO ratings VALUES (2484495, 3270346, 2998984, 4, 'Specific it receive name risk however. Watch there only happy now gas still.', '2026-05-29');
INSERT INTO ratings VALUES (9221699, 9810226, 4885859, 3, 'Despite sort way even trade case. Near cause expect stay. Increase into night none among far.
Central theory role light central network kid discover. Nation wife writer there back catch wall.', '2026-02-10');
INSERT INTO ratings VALUES (3954116, 1102212, 8600776, 4, 'Edge level response travel its ability. Book everyone team customer site easy whose.
Value key admit certain huge. Me with year.', '2026-04-29');
INSERT INTO ratings VALUES (8058429, 3944798, 2080096, 5, 'Particular evening power chair high story billion.
Act worker world project measure. Clearly much second carry increase. Ahead red information debate on most care.', '2026-03-02');
INSERT INTO ratings VALUES (9189708, 2461695, 4156979, 2, 'Consider key feeling know hour huge class reflect. Center clear nation white civil.
Cell today mind exactly investment note. Design toward agent road that phone late.', '2026-02-13');
INSERT INTO ratings VALUES (1946879, 1083307, 7592431, 1, 'Street group west practice current day either. Edge occur ready. School gun ever actually modern general three.
Nor recognize and no might deep. Choose throughout personal.', '2026-01-02');
INSERT INTO ratings VALUES (1515525, 6527815, 2080096, 1, 'Continue service challenge future. Huge assume smile serve.
Audience time service summer. Whom end dark body without likely either.', '2026-05-13');
INSERT INTO ratings VALUES (2535396, 4668405, 1807291, 2, 'Cell beat finish light. Him article real resource. While door stuff pattern able lose brother.', '2026-04-10');
INSERT INTO ratings VALUES (9546449, 8957252, 3684041, 2, 'Bill artist knowledge chance. Room appear find machine middle. Our analysis beautiful floor across democratic.
Relationship south then live early. Practice subject school walk.', '2026-05-13');
INSERT INTO ratings VALUES (9620150, 7180424, 7944595, 2, 'Recognize technology individual mind evening go option population. Low husband remember under rock use question station.
Production agency various likely third decide. Ahead stuff similar case focus.', '2026-04-11');
INSERT INTO ratings VALUES (5040584, 7515878, 7207091, 5, 'Condition free back every beautiful religious free.
After admit first traditional. Door summer off attack design two deep. Usually who day worker away science woman. About instead rich cell.', '2026-01-01');
INSERT INTO ratings VALUES (8310422, 8525725, 4997378, 2, 'Gun third student half any. Cultural tonight camera game.
Positive put actually kitchen.
Surface eight pressure land. Both main body democratic. Real law manager similar idea chance nice.', '2026-04-08');
INSERT INTO ratings VALUES (6929731, 9169482, 3061880, 2, 'Matter data stay. When safe product pattern chance.
Because within whatever coach member from community indeed. Once maintain better boy phone. Itself memory alone oil quality business full.', '2026-05-01');
INSERT INTO ratings VALUES (6405778, 3418931, 1025551, 5, 'Face five lay. Television realize best admit seem artist. Senior assume necessary remain travel hear.
Structure certain another agency. Significant walk bank else. Either here news exist bed.', '2026-01-29');
INSERT INTO ratings VALUES (8782658, 8809406, 3061880, 3, 'History might agreement writer her investment. Book nature military. Lawyer adult old security senior put where.', '2026-04-08');
INSERT INTO ratings VALUES (3151930, 5563188, 2013782, 3, 'Ground business fact between family cut. Ask for cup sign whatever see add. Current mouth six today politics throughout woman they.', '2026-05-27');
INSERT INTO ratings VALUES (7358946, 2992723, 4911547, 2, 'Choice reveal color wind science. Course bit bill home source free.
Ten join partner law. Response meeting recently particular simple yeah.', '2026-01-18');
INSERT INTO ratings VALUES (1466041, 4287773, 6411045, 3, 'Woman bank radio measure product financial agree. Woman building be watch add.
Mind term power possible staff still either. Draw kid their. Sing run set defense.', '2026-02-04');
INSERT INTO ratings VALUES (5722628, 3437925, 5017380, 1, 'Claim state probably central great. Upon attorney situation seek increase. Performance Mr form fall.', '2026-03-04');
INSERT INTO ratings VALUES (3553074, 1083307, 7559111, 4, 'Big detail song write defense. From give debate certain attention. Offer inside wall your might.
Another card long bring.', '2026-02-20');
INSERT INTO ratings VALUES (1339685, 2483269, 3817976, 5, 'Hope evening spend market then. Four phone require. Close through clear suggest measure present far.
Hand together have off involve real tend. Wrong wear staff better important clear.', '2026-02-20');
INSERT INTO ratings VALUES (5840237, 6348851, 7542327, 3, 'Again clearly every offer. Too window near agree even.
Want music conference yard project him. Ahead goal name make energy with. Assume address recently.', '2026-02-11');
INSERT INTO ratings VALUES (2382987, 8525725, 7300889, 4, 'Long much grow staff board team check. Style politics their against.
School price skill phone sing. Relate mention value special some its.', '2026-02-23');
INSERT INTO ratings VALUES (1245012, 3211200, 4172964, 1, 'Military computer southern pattern week begin. Like consumer draw who girl state.', '2026-03-10');
INSERT INTO ratings VALUES (8314751, 3423018, 4077580, 4, 'Particular mouth everybody light hear it. Learn hospital look clearly.
Author law sell admit hard who. Above ok key want.', '2026-01-08');
INSERT INTO ratings VALUES (3018459, 8809406, 4501881, 1, 'Able page city receive. Adult region conference kid. Join radio open kind million would moment.', '2026-04-19');
INSERT INTO ratings VALUES (1375324, 5719835, 4172964, 4, 'Could moment enjoy more put. Agreement interest occur their.
Include husband each final establish watch out try.', '2026-01-09');
INSERT INTO ratings VALUES (9142480, 3713051, 3564294, 4, 'Simple word bar or. Stuff eye black minute late nature. Music job responsibility important quality.
Blood huge maintain. Free guy economy individual sell.', '2026-03-09');
INSERT INTO ratings VALUES (2728842, 2680208, 8311918, 5, 'Help director kitchen last particular many trade. Important explain region near military office.', '2026-03-07');
INSERT INTO ratings VALUES (6244219, 2216117, 5337919, 3, 'State commercial resource west herself place central. Yeah play ever add participant oil real. Why ten person art PM.
Note enter school would head. Relate policy buy similar who director use.', '2026-01-18');
INSERT INTO ratings VALUES (3816670, 9218661, 7821647, 4, 'Rock would be out network behind third since.
Win new space bar. Itself interview large people event others. Buy method so suggest current.', '2026-05-25');
INSERT INTO ratings VALUES (3354434, 2422409, 1801299, 4, 'Officer region middle themselves international teacher require. Worker party medical ago doctor miss style. Even throughout so middle argue someone wrong southern.', '2026-01-03');
INSERT INTO ratings VALUES (7538646, 1128509, 4055794, 4, 'Position raise value election professor. Include responsibility southern none help parent TV pattern.', '2026-03-24');
INSERT INTO ratings VALUES (8219971, 5603263, 4580667, 4, 'Center growth discussion you free alone. When everyone other me door. Much TV now. Draw certain weight necessary player truth system.', '2026-03-06');
INSERT INTO ratings VALUES (1750531, 7859194, 3548200, 3, 'Middle none particularly three. Catch in check all. To shake hard coach.
Shoulder water use. Firm cost sit manager soon southern all. Out radio able administration knowledge small.', '2026-03-14');
INSERT INTO ratings VALUES (9629055, 7507143, 8499966, 4, 'Tend could page throughout sport building. Four husband add despite.
Computer behavior short beat while system never. Full anything nor population least work fund. After else series.', '2026-05-11');
INSERT INTO ratings VALUES (3359339, 9270808, 2094934, 1, 'Indeed box personal quality letter. Herself detail east style letter operation think world.', '2026-02-28');
INSERT INTO ratings VALUES (1629517, 3352316, 1574416, 1, 'Something national myself sort itself half stand. Crime drive forget structure quickly. Cost mention water however everything. Low community customer rather front end.', '2026-01-08');
INSERT INTO ratings VALUES (2049077, 2881744, 7220120, 4, 'Its actually value serious capital four. Education case top. Hot appear measure discussion.
Full newspaper TV have. Six seven set country. Measure list expect cultural back.', '2026-02-18');
INSERT INTO ratings VALUES (9841655, 8525725, 1101672, 4, 'Will theory five executive born speak fact. Yeah measure hospital happy.
Oil star hit standard. Very purpose act west maybe.', '2026-04-10');
INSERT INTO ratings VALUES (7613093, 7011025, 1220797, 2, 'Throw continue stop turn opportunity prevent. Top receive reach people.
Brother cost kind person. At claim rich suffer.', '2026-01-12');
INSERT INTO ratings VALUES (1746310, 7092950, 3263627, 3, 'Answer police sort cell sister. Offer less force its amount wear perhaps. Grow myself level song throughout agency three. Day energy go new magazine defense.', '2026-05-07');
INSERT INTO ratings VALUES (3576489, 3074155, 3108257, 2, 'Air product ever record employee per herself manager. Boy involve television near population economic goal science. Serve though strategy catch need yeah.', '2026-02-22');
INSERT INTO ratings VALUES (3547900, 8173474, 6009573, 2, 'Even PM able sound southern think career. Hit from far expert series because expert.
Now care service truth. World amount whom agree research present morning large.', '2026-02-23');
INSERT INTO ratings VALUES (8643500, 4193399, 2191318, 1, 'Institution economic television accept reveal born. Board collection part positive possible remember walk.', '2026-02-05');
INSERT INTO ratings VALUES (1897991, 7470714, 3516493, 1, 'Into fund class too quality everybody ok.
Hope minute star everyone middle. Goal process more just main show lot leg.
Price prove live thus whole need. Agreement already go thousand.', '2026-05-04');
INSERT INTO ratings VALUES (4607456, 2940416, 2297664, 3, 'Cell road voice information. Wait six national cover.
Board speech win step.
Forget poor man ahead. Adult property author explain thought officer. Whom resource upon food.', '2026-03-31');
INSERT INTO ratings VALUES (4136600, 7885995, 5667685, 1, 'Society kind change.
Including certain recognize subject development case condition. Big hear Mrs address type write light. Lawyer foot surface family yet race.', '2026-05-17');
INSERT INTO ratings VALUES (9133614, 9765785, 9190463, 5, 'Follow with term local. Plant modern enough decide fly trade left attack.
Read catch deep coach approach. Necessary officer certainly leg. Million medical among example.', '2026-02-13');
INSERT INTO ratings VALUES (9006692, 5277747, 6938430, 4, 'Book spend serve sell sense nor conference. Industry eight pretty condition certainly concern hope. Government head deep put why result already agreement.', '2026-03-11');
INSERT INTO ratings VALUES (4002938, 4439160, 7363844, 4, 'Remember amount rich. Find half beautiful society nor way believe.
Cell put off senior. Girl enter three could near ready. Physical picture impact set south.', '2026-01-15');
INSERT INTO ratings VALUES (6946857, 8957252, 2727975, 4, 'Analysis future either guy measure. Economic final plan whom along toward another.
Cold staff matter evening. Affect small though of adult. Coach bad former wall visit.', '2026-03-10');
INSERT INTO ratings VALUES (8843083, 9735147, 1184323, 3, 'Identify school tax sometimes they pick best young. Understand all some raise too health. Live interesting save father next firm yeah.', '2026-05-10');
INSERT INTO ratings VALUES (2095948, 9735147, 8928018, 3, 'Professional play ahead real around drug factor scene. Reveal these military building there.
Artist realize build. Then economic industry process. Other human now remember single himself that remain.', '2026-05-23');
INSERT INTO ratings VALUES (3808993, 3166127, 6773294, 3, 'Low affect big meet. Challenge two both heart back. Indeed at garden size agency.
Receive fire indeed watch. Knowledge education do seek there. Poor member kind me.', '2026-04-01');
INSERT INTO ratings VALUES (3484217, 4865004, 1453128, 4, 'Church lead fine result participant half. Event different heavy together east vote arm. Its plan audience perform especially since.', '2026-01-15');
INSERT INTO ratings VALUES (2910990, 7470714, 7569872, 3, 'Still local key although. Catch concern final blue. President science language model catch rather song feeling.', '2026-02-15');
INSERT INTO ratings VALUES (1338816, 5105774, 4571129, 2, 'Chance recently Congress left low. Kid evidence man position. Could open message.', '2026-02-03');
INSERT INTO ratings VALUES (7733929, 4078630, 2855984, 1, 'Knowledge under probably miss. Computer fight we order.
Natural test level since owner. Member seek hear yourself where include.', '2026-03-21');
INSERT INTO ratings VALUES (9258261, 8453354, 8600776, 3, 'Every value local inside. Themselves traditional church stage. Point travel factor act employee skill quality resource. Computer resource season send foreign page find.', '2026-03-23');
INSERT INTO ratings VALUES (6664308, 3106291, 7112514, 1, 'Chance animal fear prepare effort sister sure during. Recognize skin standard.', '2026-04-18');
INSERT INTO ratings VALUES (2834799, 2216117, 1069385, 1, 'Country me develop near hot receive today ability. Address him we eye key. Culture road stop today.', '2026-01-13');
INSERT INTO ratings VALUES (1271723, 8962583, 7341226, 2, 'Per always worry especially process who. Either receive believe difficult lay to author face. Weight might heavy citizen sea account.', '2026-02-08');
INSERT INTO ratings VALUES (5214110, 2729297, 2963186, 2, 'Challenge music bad call. Space run suddenly something miss work herself. Door character there leader western. Thank institution exactly deep wife.', '2026-02-16');
INSERT INTO ratings VALUES (7316412, 5603263, 4390317, 5, 'Mouth value animal size stuff conference under which. Surface always red above age either key. Push both director affect.', '2026-04-12');
INSERT INTO ratings VALUES (6800434, 3760433, 8948764, 3, 'Either such room newspaper raise identify. Statement drive where service miss.
Reflect food would who. Question these shoulder line social sing.', '2026-03-14');
INSERT INTO ratings VALUES (2119818, 5105774, 3380796, 3, 'Director father even carry. Believe network in account. Yard choice board TV several bad national.', '2026-05-17');
INSERT INTO ratings VALUES (1616315, 3135062, 2111920, 4, 'Report another quickly collection music. Leg significant bill who research identify around. Result reach while center.
Piece it believe prevent. Season across education create. Chair above floor.', '2026-05-05');
INSERT INTO ratings VALUES (9946766, 3352316, 4942822, 2, 'Lose parent eight. Tough off office herself.
Threat for hit threat accept. Like positive perform.', '2026-02-07');
INSERT INTO ratings VALUES (3616066, 4308132, 3564294, 4, 'Though week early off part.
Physical foot under human. Approach day film large sing. Stop onto thank great raise hand stay.', '2026-04-13');
INSERT INTO ratings VALUES (8678383, 6527815, 1190757, 5, 'Together building professor people agreement. Specific society staff effect nearly agreement. Light new world subject.
Write happen ability likely say.
Say concern early. Yard fear where arrive.', '2026-05-13');
INSERT INTO ratings VALUES (2767417, 4339947, 7172867, 5, 'Happen usually fight training situation series. Entire several exist story situation must.
Treatment tree look somebody window under player. Relate to attack require school above Republican.', '2026-04-12');
INSERT INTO ratings VALUES (2516306, 7797269, 1472543, 2, 'Amount individual society research language from ability. Most manage push outside assume participant fund reality. Success force employee knowledge must accept human whatever.
Build hit boy social.', '2026-04-19');
INSERT INTO ratings VALUES (3950932, 3179127, 4075438, 2, 'Force fine down notice prove vote site. Husband just room sure.
Provide serve southern.
Education brother kind task. Respond above next entire begin. Art young set color race might.', '2026-03-18');
INSERT INTO ratings VALUES (6063264, 3784950, 5351188, 2, 'Seat car power. Pretty great tonight general hold house raise. Myself bag either under.
Over suffer manager reduce. Tell dark accept responsibility eight ask we its.', '2026-03-10');
INSERT INTO ratings VALUES (2459600, 2566046, 2727975, 3, 'House dark Mrs choice. Authority else street rich current.
Church her quite leg project also. Both white evening inside.', '2026-05-23');
INSERT INTO ratings VALUES (2601990, 3504500, 5667685, 3, 'Develop window practice through become. Real parent spring author contain find success.
Without help yet letter through model when. First him work keep. Discussion oil art provide me.', '2026-01-22');
INSERT INTO ratings VALUES (3283475, 8622373, 6707929, 5, 'Benefit Congress such how chair ball. Science peace religious anything ready suggest. Close before cause set morning compare. Believe focus campaign situation unit.', '2026-03-22');
INSERT INTO ratings VALUES (4463293, 3504500, 1359170, 3, 'Behind knowledge religious reach a any of accept. Evening matter suddenly Republican respond show success. Every individual piece then modern crime third born.', '2026-04-15');
INSERT INTO ratings VALUES (2730431, 3655021, 3607379, 3, 'Must offer population pass. Employee ever several however strong major hour probably.
Particularly democratic two edge quickly smile exist.
Relationship physical draw. Customer station foot.', '2026-04-28');
INSERT INTO ratings VALUES (9333475, 8121189, 1069385, 1, 'My front candidate option general magazine easy wall. Turn word member institution interview new oil.', '2026-04-08');
INSERT INTO ratings VALUES (4227043, 8391185, 9710137, 4, 'Rich learn well all heart hotel thousand. Professional budget sometimes.
Example painting huge draw her particular. Group open thing indicate process. West through skin stay.', '2026-05-22');
INSERT INTO ratings VALUES (5551258, 7503774, 7279683, 5, 'Peace term memory six expert player sea. Some defense land sometimes base describe consider special.
Late tonight air to any smile design present. Today defense all plan.', '2026-01-30');
INSERT INTO ratings VALUES (8706384, 8173474, 2794631, 3, 'How area all tough surface. Six dark son after skill admit half. Others kitchen loss hour summer thing through.
Skill participant animal police. Car either customer rich. Strong company color.', '2026-03-03');
INSERT INTO ratings VALUES (8412853, 9488847, 2111920, 2, 'Myself common should board much sort. Care able second design nice century.
Baby step design anyone production. I value box federal seven. Family walk exist debate ago degree shake.', '2026-01-12');
INSERT INTO ratings VALUES (3735809, 7083414, 6260587, 5, 'Minute position technology sea history. Ability art once interesting protect Congress present. Parent address field and growth.', '2026-04-03');
INSERT INTO ratings VALUES (5232633, 8755012, 4373499, 5, 'Sell not street see. Decade Republican political reflect marriage worker side. Turn conference respond approach space.', '2026-04-26');
INSERT INTO ratings VALUES (5817638, 9999465, 3229588, 3, 'Final address have list final. Pay hundred arrive brother hold civil step. House perhaps help middle these shoulder huge.
Coach writer many race case sing people. Life draw option foot.', '2026-03-25');
INSERT INTO ratings VALUES (4131076, 5820495, 8023566, 3, 'Certainly media audience what record though tonight. Grow be such trade.
Second early agreement. Care school well social for media effect sell. Difference plan build break mission machine war.', '2026-02-08');
INSERT INTO ratings VALUES (2762242, 4430924, 2428956, 2, 'Within seven focus let concern huge letter. Experience money ability note under before.
Particular international room continue best. View west ok money everything animal this.', '2026-03-25');
INSERT INTO ratings VALUES (7066885, 2265297, 3191211, 5, 'Represent involve myself possible me generation end teacher. Gun since leader.
Minute any require report with agent. Police though bed security. Agreement fast against test.', '2026-04-05');
INSERT INTO ratings VALUES (7121815, 6351366, 9649921, 5, 'Task coach little nothing data quickly. Who response consumer performance. If say those Mrs executive day development.', '2026-01-27');
INSERT INTO ratings VALUES (5287452, 8395495, 8251078, 5, 'Follow before movement garden back trial night. Sit hear positive into too simple call short.
Weight agent own movement certainly. Room top develop chance on chance read. Image Mr important major.', '2026-05-11');
INSERT INTO ratings VALUES (2360421, 3655021, 2013782, 4, 'Stand forward during morning nice young. Sell month religious stand let top enough. Well spend each feel nearly though official.', '2026-02-26');
INSERT INTO ratings VALUES (7123414, 9259092, 9222958, 1, 'Charge write drug pull full lay material. Heavy mention black.
Travel red answer do production it. Talk prepare watch coach care member. Site affect decade movement know.', '2026-01-11');
INSERT INTO ratings VALUES (9249495, 5720403, 4388944, 2, 'Possible hot those perform. Miss society be partner myself including just. Pass agent evidence water anyone member seem.', '2026-03-17');
INSERT INTO ratings VALUES (5357065, 6728086, 6794537, 1, 'Truth six south base hard decade.
Ten figure act price yard a sign research. Between girl through.
Nice very career. Write general still serve economic know. Man site at why study truth.', '2026-05-20');
INSERT INTO ratings VALUES (4940545, 4287773, 9555093, 3, 'Surface weight story young age song music. Prove support pattern write way may six. Because process source.', '2026-01-04');
INSERT INTO ratings VALUES (3683381, 1594316, 3817976, 5, 'Your per personal although develop cost those. Glass color food affect size.
Anyone trade less.
Course walk new parent quite issue. Option hit well able.', '2026-04-27');
INSERT INTO ratings VALUES (2852385, 8622373, 4186470, 4, 'Walk sense ever actually art TV. Act far expect similar debate officer hour. Power than necessary court cell report nothing.', '2026-01-17');
INSERT INTO ratings VALUES (7027079, 9218661, 3868324, 1, 'Light teach special education few maybe. Report baby teach allow TV finish scene.
Former here must serve would establish protect. Way tell apply treatment. Enjoy day sometimes those station.', '2026-04-23');
INSERT INTO ratings VALUES (3951565, 7885995, 1220797, 5, 'Great state sell. Among give particularly shoulder. Call mouth agree power information. Voice day every stand reflect son onto quality.', '2026-03-21');
INSERT INTO ratings VALUES (2936431, 7973380, 9837984, 3, 'Consumer special skin pay. Watch drug gun scientist.
Save then brother fine dog. Act identify low bad. Type huge a test population road.', '2026-04-25');
INSERT INTO ratings VALUES (6231630, 8962583, 3735790, 5, 'Part daughter exist exist. Sing base performance say. Television base out Mrs personal.
Society down house marriage experience four.', '2026-01-07');
INSERT INTO ratings VALUES (2919603, 9729552, 8574016, 2, 'During listen enjoy. Step impact already key process. Pretty evening affect player.
Even likely argue agree memory. Special cup officer popular difference treatment while.', '2026-01-25');
INSERT INTO ratings VALUES (1832227, 2974225, 8795653, 5, 'Policy voice nice detail space piece. Answer cover sound.
Establish single those performance. Record until work president dark which important.', '2026-04-08');
INSERT INTO ratings VALUES (7855257, 4308132, 3920680, 2, 'Environmental star author. Turn industry including. Article similar food some beyond identify into summer. General set learn account floor adult.', '2026-05-30');
INSERT INTO ratings VALUES (9363892, 7859194, 7432459, 5, 'Thank little wear music worry glass. Book bed here can green table us.
With whether their front level. Various impact crime already theory. Bad now force present hear.', '2026-05-24');
INSERT INTO ratings VALUES (2706841, 7859194, 9805585, 2, 'Serve send discussion serve name above. Tend both federal hour run production.
Professional than must laugh. Allow guy phone attention color interest matter.
Allow wonder up old customer bad truth.', '2026-02-26');
INSERT INTO ratings VALUES (6676077, 3211200, 2718238, 2, 'Must ten nearly. Least everyone simply tax ten admit. Enough skin over least investment. Social deep half author sell.', '2026-04-12');
INSERT INTO ratings VALUES (3099459, 3166127, 1651685, 1, 'Determine more agency these international meet. School offer fish measure.', '2026-02-18');
INSERT INTO ratings VALUES (8781199, 6351366, 4752878, 3, 'Term discussion enjoy morning. Usually many air approach game. Take drive act energy subject better go.
Wait that that buy your agent fine health. Board land open.', '2026-04-25');

-- =========== skymill.countries (generic) ==========

DROP TABLE IF EXISTS countries;

CREATE TABLE countries (
  id INT NOT NULL,
  iso_code VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL
);

INSERT INTO countries VALUES (161, 'LA', 'Bahamas');
INSERT INTO countries VALUES (290, 'TV', 'Guinea-Bissau');
INSERT INTO countries VALUES (191, 'LR', 'Tunisia');
INSERT INTO countries VALUES (467, 'AZ', 'South Georgia and the South Sandwich Islands');
INSERT INTO countries VALUES (394, 'GE', 'Zambia');
INSERT INTO countries VALUES (479, 'MY', 'Holy See (Vatican City State)');
INSERT INTO countries VALUES (44, 'CA', 'Syrian Arab Republic');
INSERT INTO countries VALUES (482, 'LR', 'Lao People''s Democratic Republic');
INSERT INTO countries VALUES (467, 'PS', 'Vietnam');
INSERT INTO countries VALUES (102, 'TT', 'Bahamas');
INSERT INTO countries VALUES (284, 'AR', 'Brunei Darussalam');
INSERT INTO countries VALUES (111, 'TR', 'Bolivia');
INSERT INTO countries VALUES (339, 'VA', 'Saint Martin');
INSERT INTO countries VALUES (363, 'NP', 'Turkey');
INSERT INTO countries VALUES (260, 'GT', 'Bermuda');
INSERT INTO countries VALUES (139, 'LA', 'New Caledonia');
INSERT INTO countries VALUES (429, 'MH', 'Chad');
INSERT INTO countries VALUES (56, 'KM', 'Guinea-Bissau');
INSERT INTO countries VALUES (207, 'KG', 'Monaco');
INSERT INTO countries VALUES (163, 'FR', 'Brunei Darussalam');
INSERT INTO countries VALUES (159, 'BW', 'Sierra Leone');
INSERT INTO countries VALUES (475, 'TD', 'Uzbekistan');
INSERT INTO countries VALUES (450, 'ST', 'Poland');
INSERT INTO countries VALUES (344, 'DO', 'New Zealand');
INSERT INTO countries VALUES (158, 'NI', 'Jamaica');
INSERT INTO countries VALUES (93, 'BI', 'Niue');
INSERT INTO countries VALUES (19, 'BI', 'Cote d''Ivoire');
INSERT INTO countries VALUES (343, 'TZ', 'Central African Republic');
INSERT INTO countries VALUES (73, 'LV', 'Switzerland');
INSERT INTO countries VALUES (237, 'NL', 'Cook Islands');
INSERT INTO countries VALUES (485, 'PL', 'Saint Helena');
INSERT INTO countries VALUES (131, 'HT', 'Lesotho');
INSERT INTO countries VALUES (329, 'FI', 'Sierra Leone');
INSERT INTO countries VALUES (59, 'IS', 'Lao People''s Democratic Republic');
INSERT INTO countries VALUES (115, 'CL', 'Paraguay');

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

INSERT INTO cargo_clients VALUES (52508, 'Le and Sons', '6746 White Dale Suite 643', 'Staceyfort', 344, '33339', 'EMEA');
INSERT INTO cargo_clients VALUES (73292, 'Mclaughlin-Hodge', '343 Sabrina Square Apt. 271', 'Michellehaven', 329, '12374', 'APAC');
INSERT INTO cargo_clients VALUES (38314, 'Nichols PLC', '67313 Cassandra Drive Suite 855', 'South Nicole', 93, '39513', 'AM');
INSERT INTO cargo_clients VALUES (92389, 'Allen, Rivas and Reynolds', '263 Jones Squares', 'Port Shawn', 159, '43482', 'APAC');
INSERT INTO cargo_clients VALUES (57557, 'Williams Ltd', '0150 Garcia Freeway Apt. 275', 'Donaldfort', 191, '04576', 'AM');
INSERT INTO cargo_clients VALUES (58544, 'Potter Group', '20988 Butler Estates', 'Pamelaside', 191, '57937', 'AM');
INSERT INTO cargo_clients VALUES (46825, 'Martinez, Dalton and Wong', '789 William Crossing Apt. 025', 'Lake Andrewton', 93, '19566', 'AM');
INSERT INTO cargo_clients VALUES (10309, 'Donaldson Inc', '67097 Watson Union', 'Lake Kaylaside', 485, '93830', 'AM');
INSERT INTO cargo_clients VALUES (11060, 'Jordan Inc', '410 Smith Inlet', 'South David', 343, '86703', 'APAC');
INSERT INTO cargo_clients VALUES (91113, 'Garcia, Mccall and Davis', '440 Nguyen Plaza Suite 466', 'North Robert', 394, '01894', 'EMEA');
INSERT INTO cargo_clients VALUES (40002, 'Williams-Sharp', '28252 Guerra Views', 'Williamsside', 115, '21126', 'EMEA');
INSERT INTO cargo_clients VALUES (41926, 'Martin and Sons', '130 Jones Garden Suite 526', 'Fitzgeraldville', 343, '28707', 'AM');
INSERT INTO cargo_clients VALUES (97073, 'Kelley, Nguyen and Martin', '437 Derek Run', 'South Laurie', 479, '65163', 'AM');
INSERT INTO cargo_clients VALUES (97095, 'Rogers PLC', '799 Keith Route', 'Pamburgh', 207, '82518', 'AM');
INSERT INTO cargo_clients VALUES (87026, 'Lopez LLC', '4382 Daniel Points Apt. 778', 'North Meganland', 139, '61973', 'AM');
INSERT INTO cargo_clients VALUES (53234, 'Guerra, Daniels and Oconnor', '0329 Brent Harbor Suite 360', 'Phelpsland', 339, '93868', 'APAC');
INSERT INTO cargo_clients VALUES (40390, 'Mosley LLC', '5621 Anna Summit', 'Port Kenneth', 450, '13377', 'AM');
INSERT INTO cargo_clients VALUES (53758, 'Fisher, Fox and Stevenson', '1247 Ricky Village Apt. 492', 'Lake Nathan', 139, '46879', 'EMEA');
INSERT INTO cargo_clients VALUES (35671, 'Richardson and Sons', '6680 Dickson Wells', 'Richardfort', 363, '83340', 'EMEA');
INSERT INTO cargo_clients VALUES (12323, 'Park, Allen and Ochoa', '843 Stewart Vista Suite 439', 'Raymondfurt', 475, '80432', 'AM');
INSERT INTO cargo_clients VALUES (13852, 'Holden LLC', '27448 Corey Court', 'Garyhaven', 207, '64485', 'APAC');
INSERT INTO cargo_clients VALUES (41547, 'Lopez-Banks', '46886 James Village Suite 744', 'Spencerhaven', 207, '11871', 'EMEA');
INSERT INTO cargo_clients VALUES (46381, 'Campbell-Perez', '497 Yang Station', 'New Cassidychester', 73, '98012', 'AM');
INSERT INTO cargo_clients VALUES (64330, 'Phillips and Sons', '778 Stanley Street Apt. 693', 'Evanchester', 73, '97603', 'APAC');
INSERT INTO cargo_clients VALUES (15898, 'Day Inc', '9502 Frances Stravenue Suite 594', 'South Isabellamouth', 163, '20278', 'AM');
INSERT INTO cargo_clients VALUES (59636, 'Gray-Smith', '28452 Jennifer Extension Suite 190', 'Elliottfort', 93, '24177', 'AM');
INSERT INTO cargo_clients VALUES (82660, 'Doyle-Christensen', '56967 Thompson Springs Suite 296', 'North Melissaborough', 450, '07714', 'APAC');
INSERT INTO cargo_clients VALUES (50303, 'Hernandez-Joseph', '397 Austin Causeway Apt. 793', 'Jeffland', 343, '49346', 'AM');
INSERT INTO cargo_clients VALUES (57468, 'Irwin, Haynes and Jones', '7978 Richard Fall', 'Lake Donna', 59, '99854', 'APAC');
INSERT INTO cargo_clients VALUES (90765, 'Garza and Sons', '138 Adkins Road Apt. 099', 'Kennethtown', 482, '89427', 'EMEA');
INSERT INTO cargo_clients VALUES (87702, 'Graves, Hill and Wallace', '23140 Fitzpatrick Roads Apt. 467', 'Juliafort', 139, '87795', 'EMEA');
INSERT INTO cargo_clients VALUES (56173, 'Ward-Dougherty', '604 Norris View', 'South Terryfort', 290, '61135', 'APAC');
INSERT INTO cargo_clients VALUES (69330, 'Friedman Inc', '829 Lane Corners Suite 538', 'Pettystad', 139, '07146', 'APAC');
INSERT INTO cargo_clients VALUES (32643, 'Perez Ltd', '588 Johnson Station', 'Perezmouth', 339, '44568', 'AM');
INSERT INTO cargo_clients VALUES (90980, 'Hansen, Thomas and Anderson', '702 Paula Forges', 'South Robertchester', 290, '13211', 'EMEA');
INSERT INTO cargo_clients VALUES (60373, 'Ramirez-King', '015 Bonilla Ports', 'South Robert', 482, '22130', 'EMEA');
INSERT INTO cargo_clients VALUES (94072, 'Howe, Lewis and Page', '122 Christopher Land', 'Monicastad', 56, '64285', 'APAC');
INSERT INTO cargo_clients VALUES (72190, 'Johnston-Romero', '97806 Adams Mills', 'Victorbury', 450, '09863', 'EMEA');
INSERT INTO cargo_clients VALUES (19686, 'Howell-Norman', '655 Stephanie Estate', 'New April', 111, '90915', 'APAC');
INSERT INTO cargo_clients VALUES (66598, 'Kim-Norton', '0486 Cox Row Suite 985', 'West Gabriel', 450, '56902', 'AM');
INSERT INTO cargo_clients VALUES (65140, 'Castillo, Olsen and Grant', '1667 Tina Station', 'Lake Carriefort', 467, '84082', 'EMEA');
INSERT INTO cargo_clients VALUES (82368, 'Fields Inc', '46049 Benjamin Forks Suite 882', 'North Jasonside', 485, '01823', 'EMEA');
INSERT INTO cargo_clients VALUES (48653, 'Weaver Inc', '3675 Aguilar Camp Suite 720', 'Port Allenside', 339, '46377', 'AM');
INSERT INTO cargo_clients VALUES (88516, 'Grant-Beasley', '0391 Pope Divide', 'Cabreraside', 158, '59978', 'APAC');
INSERT INTO cargo_clients VALUES (57235, 'Hawkins-Harris', '90828 Stone Crossing Suite 295', 'West Michaelberg', 44, '02023', 'AM');
INSERT INTO cargo_clients VALUES (41410, 'Curry PLC', '50272 Cynthia Junctions Suite 903', 'Johnsonfort', 475, '17674', 'AM');
INSERT INTO cargo_clients VALUES (56241, 'Acosta, Rivera and Wilson', '46745 Nielsen Villages Suite 494', 'East Lisabury', 159, '47256', 'AM');
INSERT INTO cargo_clients VALUES (59459, 'Hoover LLC', '694 James Fall', 'Heidiberg', 159, '44247', 'EMEA');
INSERT INTO cargo_clients VALUES (13913, 'Burton Group', '42924 Brown Meadows', 'Guerreroville', 260, '99355', 'APAC');
INSERT INTO cargo_clients VALUES (67231, 'Young Ltd', '59407 Deborah Land Apt. 889', 'North Jeffreyland', 131, '84589', 'APAC');
INSERT INTO cargo_clients VALUES (56962, 'Harvey PLC', '9012 Amanda Overpass', 'New Amyville', 290, '47707', 'APAC');
INSERT INTO cargo_clients VALUES (42611, 'Rodriguez Group', '761 Erik Shore Suite 482', 'Lake Carolineport', 44, '56336', 'APAC');
INSERT INTO cargo_clients VALUES (50782, 'Mcmillan, Jacobson and Patterson', '48876 Adams Fork Apt. 470', 'Gregoryport', 394, '24201', 'AM');
INSERT INTO cargo_clients VALUES (53482, 'Wilson-Ryan', '50783 Lawrence Unions Suite 566', 'Kingmouth', 56, '60772', 'EMEA');
INSERT INTO cargo_clients VALUES (84275, 'Miranda, Lin and Henson', '91163 Julie Mountains', 'Fordchester', 479, '79269', 'APAC');
INSERT INTO cargo_clients VALUES (32322, 'Rose-Olson', '7058 Hampton Camp Suite 688', 'Gibbsburgh', 139, '48106', 'APAC');
INSERT INTO cargo_clients VALUES (56621, 'Rose PLC', '0611 Latoya Haven Suite 810', 'North Vincent', 59, '97173', 'AM');
INSERT INTO cargo_clients VALUES (79895, 'Garrett-Flores', '730 Oliver Parkways', 'Griffinhaven', 329, '03820', 'APAC');
INSERT INTO cargo_clients VALUES (21780, 'Haley Inc', '39105 Diaz Expressway Apt. 282', 'Lake Mark', 115, '37689', 'EMEA');
INSERT INTO cargo_clients VALUES (34532, 'Huff-Johnson', '94874 Anderson Trace Apt. 689', 'North Daniellechester', 191, '71638', 'APAC');
INSERT INTO cargo_clients VALUES (58567, 'Johnson Ltd', '5204 Herrera Isle Suite 744', 'East Timothy', 191, '13733', 'AM');
INSERT INTO cargo_clients VALUES (50226, 'Cervantes-Tate', '7295 Jose Summit Apt. 955', 'Lake Jasmine', 131, '59557', 'EMEA');
INSERT INTO cargo_clients VALUES (56615, 'Guzman-Parker', '55863 Fisher Views', 'West Christopherberg', 139, '80489', 'AM');
INSERT INTO cargo_clients VALUES (42021, 'Brown LLC', '03615 Amanda Squares', 'New Oliviaville', 115, '42697', 'EMEA');
INSERT INTO cargo_clients VALUES (50581, 'Michael-Wilson', '38615 Melissa Rest', 'Smithstad', 59, '98067', 'APAC');
INSERT INTO cargo_clients VALUES (60646, 'Huff and Sons', '573 Nicolas Street Apt. 007', 'Lake Dylanhaven', 475, '36764', 'APAC');
INSERT INTO cargo_clients VALUES (61652, 'Hawkins Ltd', '836 John Road', 'Parkerland', 467, '56539', 'AM');
INSERT INTO cargo_clients VALUES (85779, 'Torres Inc', '0414 Smith Union', 'Robertburgh', 163, '71042', 'AM');
INSERT INTO cargo_clients VALUES (69140, 'Brown-Lowe', '057 Isaac Falls', 'Brianfurt', 44, '78665', 'EMEA');
INSERT INTO cargo_clients VALUES (44873, 'Brady-Strong', '4397 Long Wall Suite 546', 'Wandaport', 163, '86328', 'APAC');
INSERT INTO cargo_clients VALUES (86201, 'Bernard, Ellis and Thompson', '15855 Tapia Circle Apt. 016', 'East Shelleyton', 19, '90156', 'AM');
INSERT INTO cargo_clients VALUES (38536, 'Hodges-Briggs', '294 Santos Branch', 'Heatherfort', 59, '85551', 'EMEA');
INSERT INTO cargo_clients VALUES (32577, 'Weeks LLC', '87161 Vanessa Squares', 'Dennisbury', 394, '37014', 'EMEA');
INSERT INTO cargo_clients VALUES (50718, 'Rodriguez-Cox', '04228 Denise Estate', 'Grantfurt', 394, '25833', 'EMEA');
INSERT INTO cargo_clients VALUES (13500, 'Fowler-Mccoy', '2805 Stevenson Court Suite 923', 'Hubbardport', 429, '53510', 'EMEA');
INSERT INTO cargo_clients VALUES (34595, 'Jacobs, Anderson and Delacruz', '65240 Rose Port Suite 243', 'Lake Brittany', 111, '84704', 'EMEA');
INSERT INTO cargo_clients VALUES (69007, 'Parker, Grant and Ramsey', '783 Benson Haven Suite 050', 'South Peter', 59, '30181', 'APAC');
INSERT INTO cargo_clients VALUES (17590, 'Garrett-Durham', '5054 Bates Islands Apt. 271', 'Johnstad', 284, '04443', 'APAC');
INSERT INTO cargo_clients VALUES (42690, 'Kerr, Charles and Graves', '5830 Hodges Street Apt. 229', 'North Patrickmouth', 131, '96518', 'EMEA');
INSERT INTO cargo_clients VALUES (41786, 'Stone-Cline', '6027 Smith Court', 'Codyberg', 475, '64980', 'APAC');
INSERT INTO cargo_clients VALUES (52470, 'Valentine, Edwards and Whitehead', '36282 Anthony Rest', 'North Christopher', 207, '65395', 'AM');
INSERT INTO cargo_clients VALUES (49548, 'Koch-Gross', '5685 Vincent Ford', 'Schwartzborough', 363, '91653', 'APAC');
INSERT INTO cargo_clients VALUES (84733, 'Shelton and Sons', '8168 Taylor Parks Suite 920', 'Lake Stephen', 139, '98051', 'AM');
INSERT INTO cargo_clients VALUES (31827, 'Snyder LLC', '6332 Wallace Islands', 'West Amber', 56, '90652', 'AM');
INSERT INTO cargo_clients VALUES (12385, 'Harris-Obrien', '02368 Valerie Neck', 'Benjaminmouth', 329, '31308', 'EMEA');
INSERT INTO cargo_clients VALUES (93450, 'Moore Ltd', '3113 Alison Coves Apt. 481', 'Turnertown', 163, '20196', 'APAC');
INSERT INTO cargo_clients VALUES (36126, 'Gray-Savage', '8431 Bryant Islands', 'Alexanderhaven', 260, '48176', 'EMEA');
INSERT INTO cargo_clients VALUES (41631, 'Walker-Peters', '2894 Bryant Glens', 'New Kimberlyshire', 163, '85165', 'AM');
INSERT INTO cargo_clients VALUES (41477, 'Humphrey-Riley', '7438 Hall Road Apt. 508', 'Shaneshire', 343, '41497', 'EMEA');
INSERT INTO cargo_clients VALUES (91899, 'Mcdonald, Flores and Collins', '902 Isaac Walks', 'Fieldsfort', 467, '76290', 'EMEA');
INSERT INTO cargo_clients VALUES (73560, 'Nichols, Wilson and Larson', '6478 Joshua Extension', 'Jamesmouth', 111, '30593', 'AM');
INSERT INTO cargo_clients VALUES (94338, 'Cruz, Paul and Prince', '8272 Chase Flat', 'Joshuaville', 479, '89531', 'APAC');
INSERT INTO cargo_clients VALUES (15015, 'Hawkins LLC', '85411 Debra Hollow', 'East Aaron', 207, '80235', 'APAC');
INSERT INTO cargo_clients VALUES (17666, 'Hall PLC', '5638 Fox Inlet Apt. 666', 'Alexandrastad', 56, '29078', 'APAC');
INSERT INTO cargo_clients VALUES (50820, 'Carpenter-Mitchell', '64558 Cassandra Fork', 'Port Samuelchester', 115, '89434', 'EMEA');
INSERT INTO cargo_clients VALUES (42260, 'Griffith and Sons', '562 Tina Camp', 'Taylorview', 284, '47550', 'EMEA');
INSERT INTO cargo_clients VALUES (84090, 'Smith-Ramsey', '61132 Becker Spurs', 'Tanyachester', 290, '54985', 'AM');
INSERT INTO cargo_clients VALUES (55085, 'Carroll Group', '83895 Bryant Village', 'Port Kenneth', 163, '28947', 'EMEA');
INSERT INTO cargo_clients VALUES (45811, 'Smith, Rivers and Gross', '8026 Berry Station Suite 645', 'Port Terrance', 363, '33566', 'APAC');
INSERT INTO cargo_clients VALUES (41898, 'Lewis and Sons', '13644 James Greens', 'Jeremiahberg', 363, '09194', 'EMEA');
INSERT INTO cargo_clients VALUES (31036, 'Stewart-Brown', '989 Blake Dam Suite 366', 'North Dawnbury', 467, '24886', 'EMEA');
INSERT INTO cargo_clients VALUES (84690, 'Dean-Oliver', '471 Castro Ports', 'Hodgesmouth', 44, '52333', 'APAC');
INSERT INTO cargo_clients VALUES (33485, 'Moore LLC', '077 Jennifer Fork', 'New Crystal', 482, '58240', 'APAC');
INSERT INTO cargo_clients VALUES (53684, 'Holmes, Cohen and Ingram', '06553 Javier Drive', 'North Jameston', 260, '53557', 'APAC');
INSERT INTO cargo_clients VALUES (99271, 'Jones LLC', '320 Andrew Flats Apt. 697', 'East Suzannemouth', 363, '79077', 'APAC');
INSERT INTO cargo_clients VALUES (16955, 'Vazquez Group', '43140 Ryan Summit', 'Port Rachelport', 56, '13948', 'APAC');
INSERT INTO cargo_clients VALUES (94669, 'Fitzpatrick, Conner and Foster', '9104 Johnson Creek', 'Port Tarashire', 93, '36842', 'EMEA');
INSERT INTO cargo_clients VALUES (62432, 'Rios PLC', '2164 Evans Isle', 'Port Joel', 19, '66910', 'AM');
INSERT INTO cargo_clients VALUES (31202, 'Simmons Group', '290 Angela Prairie Apt. 995', 'West Jasonchester', 73, '13821', 'APAC');
INSERT INTO cargo_clients VALUES (66509, 'Martinez, Gray and Ford', '4008 Karen Land Apt. 756', 'Suzanneside', 450, '22405', 'APAC');
INSERT INTO cargo_clients VALUES (65134, 'Gutierrez Group', '82376 Mercer Squares', 'Mendezmouth', 59, '51366', 'AM');
INSERT INTO cargo_clients VALUES (30478, 'Santos and Sons', '2460 Meyer Mountain', 'North Bradshire', 339, '68835', 'AM');
INSERT INTO cargo_clients VALUES (93452, 'Snyder-Jimenez', '293 Taylor Inlet Apt. 148', 'Villarrealview', 161, '93358', 'AM');
INSERT INTO cargo_clients VALUES (18549, 'Stanley LLC', '68525 Mason Village Apt. 572', 'Bankstown', 339, '36987', 'EMEA');
INSERT INTO cargo_clients VALUES (65303, 'Holmes PLC', '192 Robert Burgs Apt. 635', 'West Steven', 191, '31729', 'EMEA');
INSERT INTO cargo_clients VALUES (59681, 'Williams Ltd', '03881 Murphy Plaza Apt. 978', 'East Edwardfurt', 44, '25523', 'APAC');
INSERT INTO cargo_clients VALUES (27704, 'Murray and Sons', '24104 Johnson Forge', 'Deniseberg', 344, '57156', 'AM');
INSERT INTO cargo_clients VALUES (11007, 'Humphrey-Dunn', '9354 Ruiz Wall Apt. 885', 'Batesville', 207, '34051', 'AM');
INSERT INTO cargo_clients VALUES (54679, 'Castillo-Ortiz', '4959 Rodriguez Summit Apt. 881', 'Lake Debbie', 163, '33468', 'AM');
INSERT INTO cargo_clients VALUES (12118, 'Obrien PLC', '4738 Hensley River Apt. 283', 'Garzamouth', 161, '12123', 'AM');
INSERT INTO cargo_clients VALUES (36453, 'Johnson LLC', '803 Chase Alley', 'Port Susanland', 115, '52696', 'APAC');
INSERT INTO cargo_clients VALUES (28848, 'Curry, Smith and Moore', '358 Jennifer Stream Suite 702', 'South Jesus', 329, '89343', 'APAC');
INSERT INTO cargo_clients VALUES (38161, 'Powell, Campbell and Murray', '073 White Neck', 'Lake Holly', 207, '09988', 'AM');
INSERT INTO cargo_clients VALUES (47423, 'Russell-Murray', '321 Michael Crescent', 'New Jesusfort', 111, '93661', 'APAC');
INSERT INTO cargo_clients VALUES (87279, 'Brown-Cordova', '190 Greene Views Suite 737', 'Shawnfort', 159, '65320', 'EMEA');
INSERT INTO cargo_clients VALUES (11051, 'Hays Inc', '27851 Marshall Mountains Suite 714', 'Nicholasborough', 163, '92346', 'APAC');
INSERT INTO cargo_clients VALUES (88864, 'Weaver Group', '2330 Matthew Lakes', 'Juliehaven', 344, '63533', 'AM');
INSERT INTO cargo_clients VALUES (40977, 'Bauer-Nguyen', '732 Edwards Lights', 'Charlesshire', 343, '79540', 'APAC');
INSERT INTO cargo_clients VALUES (13937, 'Jones-Baker', '755 Zoe Forge', 'Bradleyborough', 363, '28454', 'APAC');
INSERT INTO cargo_clients VALUES (25322, 'Davis Ltd', '43853 Raymond Greens', 'West Katherineberg', 44, '78061', 'APAC');
INSERT INTO cargo_clients VALUES (76581, 'Fry, Duncan and Garcia', '676 William River Suite 407', 'Annebury', 329, '55887', 'EMEA');
INSERT INTO cargo_clients VALUES (58284, 'Torres and Sons', '517 Smith Centers Apt. 965', 'South Colleen', 102, '24300', 'EMEA');
INSERT INTO cargo_clients VALUES (76084, 'Murphy-Wagner', '8100 Matthew Pines', 'Ricardochester', 56, '26508', 'APAC');
INSERT INTO cargo_clients VALUES (50177, 'Clark Inc', '394 Kevin Trace Apt. 303', 'North Loribury', 93, '38210', 'EMEA');
INSERT INTO cargo_clients VALUES (86258, 'Thomas-Cruz', '5732 Daniel Causeway', 'West Dawn', 111, '25970', 'AM');
INSERT INTO cargo_clients VALUES (40197, 'Warner-White', '80466 Sophia Islands', 'Johnsonton', 73, '82309', 'APAC');
INSERT INTO cargo_clients VALUES (70715, 'Weaver, Smith and Marquez', '29446 Smith Manor', 'West Brandishire', 59, '23613', 'EMEA');
INSERT INTO cargo_clients VALUES (96305, 'Sandoval, Fuentes and Roberts', '80840 Robinson Mission Apt. 123', 'Wilcoxview', 73, '94363', 'AM');
INSERT INTO cargo_clients VALUES (81659, 'Saunders-Barnes', '932 Hill Ridges', 'South Richardburgh', 102, '20688', 'APAC');
INSERT INTO cargo_clients VALUES (33265, 'Miller-Coleman', '56851 Monroe Fields', 'Kathleenberg', 93, '15881', 'EMEA');

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

INSERT INTO cargo_shipments VALUES (1716500, 551210, 4, 48402, 59681, 985.6564423493788);
INSERT INTO cargo_shipments VALUES (5747488, 336831, 3, 37085, 25322, 830.9132428859464);
INSERT INTO cargo_shipments VALUES (6812495, 745546, 3, 33852, 81659, 195.71580541528667);
INSERT INTO cargo_shipments VALUES (3826439, 464221, 1, 12526, 41547, 563.0817268511876);
INSERT INTO cargo_shipments VALUES (8209719, 595282, 3, 19265, 69330, 3.602397639146604);
INSERT INTO cargo_shipments VALUES (5385381, 861877, 1, 46177, 40390, 354.9924767532694);
INSERT INTO cargo_shipments VALUES (2749803, 371308, 2, 8020, 18549, 130.55775332030893);
INSERT INTO cargo_shipments VALUES (4338904, 104116, 3, 39128, 57557, 901.0551089885527);
INSERT INTO cargo_shipments VALUES (5555798, 294483, 2, 39245, 57557, 360.17060800963264);
INSERT INTO cargo_shipments VALUES (7858731, 771720, 1, 4011, 31036, 815.7451838372058);
INSERT INTO cargo_shipments VALUES (5578437, 291137, 4, 26949, 56241, 532.1835617876527);
INSERT INTO cargo_shipments VALUES (9504098, 694403, 2, 39797, 59681, 341.2454371574508);
INSERT INTO cargo_shipments VALUES (8704845, 843961, 4, 7249, 52470, 935.2080531629041);
INSERT INTO cargo_shipments VALUES (7627372, 208059, 2, 22383, 13852, 116.1575278136202);
INSERT INTO cargo_shipments VALUES (8540141, 742571, 1, 4007, 38536, 202.87073089284246);
INSERT INTO cargo_shipments VALUES (4751719, 484116, 2, 6789, 50177, 665.9140035438877);
INSERT INTO cargo_shipments VALUES (9844811, 944394, 3, 48685, 11060, 570.1165119878057);
INSERT INTO cargo_shipments VALUES (7314984, 947599, 1, 27504, 11051, 999.1458691516067);
INSERT INTO cargo_shipments VALUES (1986666, 745952, 4, 17326, 73560, 940.3641509199399);
INSERT INTO cargo_shipments VALUES (9657736, 747608, 1, 16067, 30478, 876.487122131517);
INSERT INTO cargo_shipments VALUES (4415904, 919657, 1, 37895, 50303, 919.875964763733);
INSERT INTO cargo_shipments VALUES (8021619, 374146, 2, 5659, 16955, 360.4678972695715);
INSERT INTO cargo_shipments VALUES (7885786, 264352, 3, 19714, 33265, 65.28533445928642);
INSERT INTO cargo_shipments VALUES (3760377, 193915, 2, 18254, 36126, 265.3110790599961);
INSERT INTO cargo_shipments VALUES (5678893, 998345, 1, 25511, 87702, 624.0692988738222);
INSERT INTO cargo_shipments VALUES (4834736, 481053, 3, 9877, 50303, 528.4816138268941);
INSERT INTO cargo_shipments VALUES (7361443, 241612, 4, 47418, 70715, 462.85486218462427);
INSERT INTO cargo_shipments VALUES (8722514, 108703, 1, 45733, 66598, 475.6192572041978);
INSERT INTO cargo_shipments VALUES (3284769, 196131, 4, 2049, 34595, 757.5249420260309);
INSERT INTO cargo_shipments VALUES (1475033, 970733, 4, 8925, 31827, 204.40606832361087);
INSERT INTO cargo_shipments VALUES (7942837, 668736, 4, 22999, 56621, 364.53681961555975);
INSERT INTO cargo_shipments VALUES (1824476, 477350, 3, 22245, 69007, 11.703777140292694);
INSERT INTO cargo_shipments VALUES (2726298, 207609, 1, 14185, 90980, 978.633913846596);
INSERT INTO cargo_shipments VALUES (6229444, 610948, 4, 10312, 64330, 571.9537185820552);
INSERT INTO cargo_shipments VALUES (9993116, 193915, 3, 6667, 53234, 578.8561372708137);
INSERT INTO cargo_shipments VALUES (6545616, 686608, 2, 9128, 99271, 551.0243583084351);
INSERT INTO cargo_shipments VALUES (4716864, 650161, 3, 12083, 53684, 612.8797058306299);
INSERT INTO cargo_shipments VALUES (7998130, 838513, 1, 23547, 97073, 452.5498552680164);
INSERT INTO cargo_shipments VALUES (5285393, 735758, 4, 28020, 56962, 32.10402349993091);
INSERT INTO cargo_shipments VALUES (6273253, 404391, 4, 11820, 49548, 374.7238408792516);
INSERT INTO cargo_shipments VALUES (4727034, 726322, 3, 40541, 15898, 529.8736547689616);
INSERT INTO cargo_shipments VALUES (4018754, 839442, 4, 28178, 40977, 223.06621433768692);
INSERT INTO cargo_shipments VALUES (1330796, 617268, 3, 36712, 41547, 27.449130638134033);
INSERT INTO cargo_shipments VALUES (8233919, 742571, 3, 40957, 73560, 221.65651295865274);
INSERT INTO cargo_shipments VALUES (4838130, 366964, 1, 23858, 42611, 921.0144641906123);
INSERT INTO cargo_shipments VALUES (6373999, 919657, 3, 40461, 99271, 417.8771541391959);
INSERT INTO cargo_shipments VALUES (7156554, 365267, 4, 29424, 53758, 159.2032077529858);
INSERT INTO cargo_shipments VALUES (5465114, 566273, 4, 32419, 84690, 506.1451379915892);
INSERT INTO cargo_shipments VALUES (4523252, 434451, 2, 26517, 31036, 843.1985647345397);
INSERT INTO cargo_shipments VALUES (2907599, 140527, 3, 22268, 50782, 682.2042523926295);
INSERT INTO cargo_shipments VALUES (6142919, 895631, 4, 37924, 21780, 28.066788233734318);
INSERT INTO cargo_shipments VALUES (1616379, 566273, 2, 8848, 41477, 402.66428419558554);
INSERT INTO cargo_shipments VALUES (9389605, 291137, 1, 11211, 53684, 294.9415430689036);
INSERT INTO cargo_shipments VALUES (4026873, 200279, 3, 40274, 90765, 778.8783936361946);
INSERT INTO cargo_shipments VALUES (9805091, 877280, 2, 20763, 70715, 850.5987518065591);
INSERT INTO cargo_shipments VALUES (2960173, 651365, 4, 41022, 17666, 126.98728423817774);
INSERT INTO cargo_shipments VALUES (5586411, 734764, 3, 13046, 67231, 170.10963763404106);
INSERT INTO cargo_shipments VALUES (7231204, 264687, 3, 42309, 35671, 162.9809429853054);
INSERT INTO cargo_shipments VALUES (6700032, 217450, 1, 1413, 16955, 109.65424641519827);
INSERT INTO cargo_shipments VALUES (4885297, 104116, 3, 20685, 69140, 483.46186264993275);
INSERT INTO cargo_shipments VALUES (3838016, 864161, 2, 49854, 15898, 934.5074920382151);
INSERT INTO cargo_shipments VALUES (7290141, 208059, 3, 36763, 86258, 865.089725240103);
INSERT INTO cargo_shipments VALUES (2272169, 402347, 4, 26553, 58284, 999.325007496997);
INSERT INTO cargo_shipments VALUES (5956950, 877280, 3, 43990, 36453, 601.1865345982757);
INSERT INTO cargo_shipments VALUES (4646635, 835095, 4, 16686, 91899, 859.3560747797908);
INSERT INTO cargo_shipments VALUES (2082227, 374146, 4, 48196, 46381, 782.3154713623353);
INSERT INTO cargo_shipments VALUES (9500947, 193915, 1, 7179, 56962, 471.09879233898477);
INSERT INTO cargo_shipments VALUES (5533320, 964737, 4, 13181, 32577, 516.8421058442451);
INSERT INTO cargo_shipments VALUES (7647275, 656769, 4, 13496, 58284, 437.38336292839864);
INSERT INTO cargo_shipments VALUES (3546140, 599462, 2, 8032, 33265, 899.2652861667083);
INSERT INTO cargo_shipments VALUES (8063124, 886388, 3, 14593, 76084, 407.6417243905902);
INSERT INTO cargo_shipments VALUES (4065941, 747608, 4, 27136, 64330, 758.0465883408422);
INSERT INTO cargo_shipments VALUES (3542985, 295088, 1, 30799, 66509, 971.0240501773554);
INSERT INTO cargo_shipments VALUES (6043939, 443855, 4, 28161, 41547, 606.3359548348236);
INSERT INTO cargo_shipments VALUES (4317204, 422672, 3, 15353, 18549, 399.3735312831198);
INSERT INTO cargo_shipments VALUES (1023443, 864161, 4, 16724, 40977, 961.4254177849057);
INSERT INTO cargo_shipments VALUES (7889565, 417366, 4, 37174, 64330, 873.3874700481587);
INSERT INTO cargo_shipments VALUES (1453211, 767763, 2, 49956, 36126, 8.420319348102169);
INSERT INTO cargo_shipments VALUES (3328445, 546827, 3, 29195, 15015, 811.8166697949949);
INSERT INTO cargo_shipments VALUES (7576669, 139249, 3, 16900, 59681, 183.05244634949182);
INSERT INTO cargo_shipments VALUES (3259611, 651365, 4, 10151, 79895, 361.08407200728607);
INSERT INTO cargo_shipments VALUES (2364277, 769533, 3, 39352, 12323, 716.8660860436397);
INSERT INTO cargo_shipments VALUES (1226210, 730182, 1, 1290, 28848, 635.0038523651298);
INSERT INTO cargo_shipments VALUES (7379877, 571370, 4, 47621, 50820, 340.4273053980985);
INSERT INTO cargo_shipments VALUES (7112174, 623654, 2, 9878, 58544, 340.24547808277197);
INSERT INTO cargo_shipments VALUES (7269172, 566273, 3, 21469, 52508, 346.9461712225429);
INSERT INTO cargo_shipments VALUES (4098677, 242407, 1, 40413, 11060, 245.06667237276213);
INSERT INTO cargo_shipments VALUES (3417214, 615352, 4, 6924, 15015, 430.854214314769);
INSERT INTO cargo_shipments VALUES (6953457, 244904, 3, 14323, 91113, 654.5387296699831);
INSERT INTO cargo_shipments VALUES (3485187, 702515, 1, 6048, 46381, 180.42431961088346);
INSERT INTO cargo_shipments VALUES (9977719, 218188, 4, 33945, 52508, 331.4839335416838);
INSERT INTO cargo_shipments VALUES (6864162, 311617, 2, 34546, 41547, 164.79007039075987);
INSERT INTO cargo_shipments VALUES (3572712, 961289, 1, 24555, 85779, 649.1018441744301);
INSERT INTO cargo_shipments VALUES (2640179, 838513, 2, 33565, 15015, 878.7515539737101);
INSERT INTO cargo_shipments VALUES (5333249, 995741, 4, 40218, 92389, 926.0357677295156);
INSERT INTO cargo_shipments VALUES (3353192, 109915, 3, 13942, 50177, 451.5038688040097);
INSERT INTO cargo_shipments VALUES (1185719, 998345, 4, 8903, 15015, 107.98799977080532);
INSERT INTO cargo_shipments VALUES (2981138, 747608, 1, 13560, 97073, 531.6653069467259);
INSERT INTO cargo_shipments VALUES (2481264, 459618, 4, 12030, 57235, 739.1551851246904);
INSERT INTO cargo_shipments VALUES (4330206, 587854, 3, 48764, 31827, 836.8960303138784);
INSERT INTO cargo_shipments VALUES (4742565, 797115, 3, 9480, 56615, 523.8053570827279);
INSERT INTO cargo_shipments VALUES (6366709, 452392, 1, 2942, 87702, 606.2057181349855);
INSERT INTO cargo_shipments VALUES (3063638, 702659, 4, 43018, 15898, 477.9046385341259);
INSERT INTO cargo_shipments VALUES (4867344, 927176, 4, 29369, 70715, 164.6898675470413);
INSERT INTO cargo_shipments VALUES (1338891, 241612, 4, 46895, 82660, 61.42927538344944);
INSERT INTO cargo_shipments VALUES (3690254, 961289, 3, 3665, 87702, 141.57116176350092);
INSERT INTO cargo_shipments VALUES (4365921, 853530, 4, 4901, 32322, 524.2008549677577);
INSERT INTO cargo_shipments VALUES (9978065, 832152, 2, 41599, 13913, 397.3913535520089);
INSERT INTO cargo_shipments VALUES (6902411, 710029, 2, 40435, 17666, 628.4369257039998);
INSERT INTO cargo_shipments VALUES (5853412, 495162, 2, 6581, 10309, 959.4866941958045);
INSERT INTO cargo_shipments VALUES (8952741, 468368, 2, 35817, 70715, 265.1191962606233);
INSERT INTO cargo_shipments VALUES (6288914, 945423, 4, 8288, 45811, 156.61081046103052);
INSERT INTO cargo_shipments VALUES (5130053, 196524, 1, 4116, 69140, 838.1868090041488);
INSERT INTO cargo_shipments VALUES (4922801, 255144, 2, 38893, 13913, 524.640439869144);
INSERT INTO cargo_shipments VALUES (6710274, 832152, 3, 26061, 67231, 338.89273029146295);
INSERT INTO cargo_shipments VALUES (8966507, 402347, 3, 19702, 91113, 942.0289235865841);
INSERT INTO cargo_shipments VALUES (6501466, 910344, 1, 35369, 47423, 724.460651105565);
INSERT INTO cargo_shipments VALUES (3142169, 287765, 3, 34021, 50581, 409.0440744610707);
INSERT INTO cargo_shipments VALUES (5153919, 816550, 4, 29181, 66509, 665.6855702621375);
INSERT INTO cargo_shipments VALUES (1026409, 382201, 3, 1573, 70715, 981.4531462795226);
INSERT INTO cargo_shipments VALUES (8040461, 374146, 4, 8345, 13913, 596.0107828271327);
INSERT INTO cargo_shipments VALUES (1048082, 464221, 3, 37061, 97095, 241.59671976395413);
INSERT INTO cargo_shipments VALUES (7083006, 614215, 1, 49515, 58284, 352.95055246302655);
INSERT INTO cargo_shipments VALUES (4209708, 332486, 4, 7596, 50581, 85.64346440373616);
INSERT INTO cargo_shipments VALUES (3173922, 140527, 3, 49288, 59459, 859.5615700251387);
INSERT INTO cargo_shipments VALUES (2561622, 294483, 1, 35611, 58544, 613.9466830435574);
INSERT INTO cargo_shipments VALUES (5397388, 947049, 2, 9794, 91899, 195.41752656845102);
INSERT INTO cargo_shipments VALUES (8977961, 229560, 3, 29995, 87279, 124.38204463706948);
INSERT INTO cargo_shipments VALUES (1035236, 104719, 4, 44860, 32643, 990.6328454920654);
INSERT INTO cargo_shipments VALUES (2484360, 655649, 4, 46520, 84090, 319.40538436386254);
INSERT INTO cargo_shipments VALUES (5380178, 895631, 4, 1368, 27704, 465.51017601482414);
INSERT INTO cargo_shipments VALUES (4107102, 656769, 3, 25290, 27704, 198.02489851938344);
INSERT INTO cargo_shipments VALUES (4977126, 708494, 1, 14609, 38314, 515.2804018673105);
INSERT INTO cargo_shipments VALUES (5360189, 681816, 4, 35332, 40197, 425.39735282556956);
INSERT INTO cargo_shipments VALUES (7257486, 877280, 4, 9249, 12385, 471.8356054692685);
INSERT INTO cargo_shipments VALUES (1884613, 722031, 4, 27917, 79895, 95.65641826556137);
INSERT INTO cargo_shipments VALUES (3881298, 260005, 2, 7537, 50226, 92.85697324179453);
INSERT INTO cargo_shipments VALUES (7093682, 844403, 4, 40776, 40977, 772.6866131291862);
INSERT INTO cargo_shipments VALUES (5446236, 820512, 3, 17588, 87026, 436.90570098247184);
INSERT INTO cargo_shipments VALUES (3023991, 336831, 1, 28315, 42021, 239.02227099363526);
INSERT INTO cargo_shipments VALUES (6533788, 207609, 2, 26170, 15898, 97.44746874430365);
INSERT INTO cargo_shipments VALUES (8974297, 123476, 4, 44036, 34532, 270.29545196640316);
INSERT INTO cargo_shipments VALUES (5124539, 140527, 2, 9999, 11060, 454.14898526036893);
INSERT INTO cargo_shipments VALUES (8862822, 477627, 2, 11482, 66509, 257.2635344559762);
INSERT INTO cargo_shipments VALUES (6052373, 145117, 2, 27842, 33485, 562.9002874551297);
INSERT INTO cargo_shipments VALUES (8531766, 498912, 3, 25352, 10309, 627.046306401621);
INSERT INTO cargo_shipments VALUES (9027114, 264352, 1, 48491, 47423, 44.072775302093305);
INSERT INTO cargo_shipments VALUES (5521807, 214614, 2, 27342, 32577, 526.2674399404808);
INSERT INTO cargo_shipments VALUES (5310474, 813446, 2, 43272, 96305, 405.56490840531313);
INSERT INTO cargo_shipments VALUES (3206827, 726322, 2, 17038, 56621, 491.36629748870786);
INSERT INTO cargo_shipments VALUES (7051288, 294483, 3, 30269, 48653, 555.2370521175221);
INSERT INTO cargo_shipments VALUES (6757245, 995741, 2, 34041, 36126, 972.4824694820337);
INSERT INTO cargo_shipments VALUES (9705273, 342224, 4, 28538, 21780, 260.14651682944555);
INSERT INTO cargo_shipments VALUES (1210549, 388891, 4, 21678, 34532, 195.54754478235103);
INSERT INTO cargo_shipments VALUES (5293596, 813446, 3, 42726, 58567, 269.81138518169337);
INSERT INTO cargo_shipments VALUES (4355247, 157492, 1, 43099, 42021, 57.54383859696921);
INSERT INTO cargo_shipments VALUES (4484636, 998345, 1, 32891, 82368, 286.0462973307627);
INSERT INTO cargo_shipments VALUES (8342258, 196131, 1, 36900, 94669, 119.32324080996126);
INSERT INTO cargo_shipments VALUES (3892691, 366964, 2, 9026, 56962, 897.5189534360131);
INSERT INTO cargo_shipments VALUES (8053549, 995615, 1, 20051, 38314, 759.0703221912763);
INSERT INTO cargo_shipments VALUES (9900307, 679962, 3, 41653, 41477, 261.6904859776572);
INSERT INTO cargo_shipments VALUES (1396012, 332486, 2, 23445, 53684, 577.572803774279);
INSERT INTO cargo_shipments VALUES (5129189, 218458, 3, 37928, 13937, 213.95580419605852);
INSERT INTO cargo_shipments VALUES (5067636, 365267, 2, 21214, 97073, 311.2731420690171);
INSERT INTO cargo_shipments VALUES (4530591, 630921, 3, 20516, 69140, 771.5642995702527);
INSERT INTO cargo_shipments VALUES (9780978, 655649, 3, 47023, 69140, 380.40986129319856);
INSERT INTO cargo_shipments VALUES (7934876, 671988, 3, 31067, 65303, 30.75449075820391);
INSERT INTO cargo_shipments VALUES (1977655, 993138, 1, 17501, 73292, 827.0122035467139);
INSERT INTO cargo_shipments VALUES (5322111, 103502, 3, 1033, 94669, 167.42028173605615);
INSERT INTO cargo_shipments VALUES (9461863, 702659, 2, 2523, 53482, 820.3456687776606);
INSERT INTO cargo_shipments VALUES (1301940, 244904, 3, 9388, 13913, 451.59887913941276);
INSERT INTO cargo_shipments VALUES (2778270, 443855, 4, 31515, 91113, 243.7384420769444);
INSERT INTO cargo_shipments VALUES (8071331, 745952, 4, 13083, 56241, 176.50637191147666);
INSERT INTO cargo_shipments VALUES (1792151, 382201, 1, 26197, 50718, 603.168709882473);
INSERT INTO cargo_shipments VALUES (8306751, 242407, 1, 4157, 42690, 989.1731875970878);
INSERT INTO cargo_shipments VALUES (3983286, 804924, 4, 10583, 62432, 900.639349997214);
INSERT INTO cargo_shipments VALUES (2046366, 655649, 2, 10986, 60646, 966.0821532343199);
INSERT INTO cargo_shipments VALUES (3346007, 651365, 4, 13918, 46825, 378.8054794702619);
INSERT INTO cargo_shipments VALUES (6526034, 143671, 3, 32848, 50177, 836.6482869015908);
INSERT INTO cargo_shipments VALUES (3480218, 295660, 4, 17202, 33485, 169.7761068050323);
INSERT INTO cargo_shipments VALUES (5289822, 222414, 3, 18432, 41898, 699.1743812075032);
INSERT INTO cargo_shipments VALUES (6316540, 311617, 4, 11018, 36126, 917.9822027513945);
INSERT INTO cargo_shipments VALUES (7629792, 342326, 4, 30640, 53684, 411.92347915994753);
INSERT INTO cargo_shipments VALUES (8655935, 562349, 2, 36565, 10309, 614.570349054406);
INSERT INTO cargo_shipments VALUES (5547204, 368069, 3, 47847, 17590, 942.5800303378453);
INSERT INTO cargo_shipments VALUES (9503159, 622718, 1, 44149, 60373, 602.9321494700853);
INSERT INTO cargo_shipments VALUES (9528790, 306162, 1, 45094, 42260, 29.113440142132795);
INSERT INTO cargo_shipments VALUES (6972140, 970733, 3, 12752, 13913, 221.19306444743026);
INSERT INTO cargo_shipments VALUES (6356467, 157492, 1, 46721, 50581, 582.6978774046491);
INSERT INTO cargo_shipments VALUES (9750331, 839442, 4, 39676, 57557, 197.2904554367514);
INSERT INTO cargo_shipments VALUES (4318324, 951732, 4, 49682, 41926, 981.4190646519019);
INSERT INTO cargo_shipments VALUES (9191128, 459618, 2, 16278, 60646, 281.9481150103674);
INSERT INTO cargo_shipments VALUES (3795178, 342326, 2, 20837, 47423, 267.84599648153653);
INSERT INTO cargo_shipments VALUES (1681789, 241612, 1, 31173, 50177, 876.6247235367775);
INSERT INTO cargo_shipments VALUES (3813270, 931397, 2, 29251, 17666, 164.06517134669772);
INSERT INTO cargo_shipments VALUES (8956789, 944394, 3, 12176, 11060, 556.7993116083666);
INSERT INTO cargo_shipments VALUES (3333147, 368069, 4, 45837, 31202, 843.974122137536);
INSERT INTO cargo_shipments VALUES (7385108, 925782, 1, 12492, 33485, 961.7341616019899);
INSERT INTO cargo_shipments VALUES (5796341, 843101, 1, 49028, 25322, 0.462621034866606);
INSERT INTO cargo_shipments VALUES (5520196, 355369, 2, 44469, 60646, 103.18386419931569);
INSERT INTO cargo_shipments VALUES (2720964, 365267, 3, 24016, 92389, 190.48493268466015);
INSERT INTO cargo_shipments VALUES (4527126, 218458, 2, 45798, 88864, 809.6381858030929);
INSERT INTO cargo_shipments VALUES (7915560, 366964, 3, 1379, 41786, 344.81658921069214);
INSERT INTO cargo_shipments VALUES (6758736, 331143, 1, 23049, 99271, 211.1489006818942);
INSERT INTO cargo_shipments VALUES (5242212, 303968, 1, 34791, 96305, 994.963422963438);
INSERT INTO cargo_shipments VALUES (9330561, 876023, 1, 2387, 84090, 135.27778102417355);
INSERT INTO cargo_shipments VALUES (7867103, 764454, 1, 39642, 31827, 366.8590432292408);
INSERT INTO cargo_shipments VALUES (6999663, 578296, 2, 30301, 21780, 613.1262375771498);
INSERT INTO cargo_shipments VALUES (3586124, 287765, 3, 39025, 64330, 188.32932583032436);
INSERT INTO cargo_shipments VALUES (1023298, 618905, 3, 18602, 38536, 251.10276384892515);
INSERT INTO cargo_shipments VALUES (3193291, 583957, 2, 16420, 41631, 486.69973693217185);
INSERT INTO cargo_shipments VALUES (7416917, 372521, 3, 4433, 90765, 433.9885976386519);
INSERT INTO cargo_shipments VALUES (1618479, 767763, 2, 22027, 93450, 417.06670829151903);
INSERT INTO cargo_shipments VALUES (8890742, 843961, 4, 44891, 97095, 324.4839922620182);
INSERT INTO cargo_shipments VALUES (4316176, 567832, 1, 35579, 31036, 273.78185377698736);
INSERT INTO cargo_shipments VALUES (4215620, 919415, 3, 48770, 53684, 77.10531628658867);
INSERT INTO cargo_shipments VALUES (3659341, 431929, 1, 33900, 21780, 271.7910566725232);
INSERT INTO cargo_shipments VALUES (8088176, 940727, 1, 42515, 12323, 575.3835489032223);
INSERT INTO cargo_shipments VALUES (5298876, 778047, 3, 3853, 64330, 53.932166111451906);
INSERT INTO cargo_shipments VALUES (3634719, 464221, 4, 31439, 50226, 557.7796904474815);
INSERT INTO cargo_shipments VALUES (1454997, 341564, 3, 47416, 93450, 383.25032196739716);
INSERT INTO cargo_shipments VALUES (6448107, 702515, 3, 9571, 58544, 701.9744405714838);
INSERT INTO cargo_shipments VALUES (9943995, 196131, 2, 36601, 13500, 255.54783833940442);
INSERT INTO cargo_shipments VALUES (2388287, 388399, 3, 46983, 17666, 423.68560784373176);
INSERT INTO cargo_shipments VALUES (4413312, 816550, 4, 27296, 72190, 137.23850970572892);
INSERT INTO cargo_shipments VALUES (9844125, 910344, 3, 19744, 66509, 806.4121866650422);
INSERT INTO cargo_shipments VALUES (2143498, 422672, 4, 30200, 90980, 474.66424378714623);
INSERT INTO cargo_shipments VALUES (1503565, 306162, 2, 42683, 54679, 701.6624062011292);
INSERT INTO cargo_shipments VALUES (8036757, 196524, 4, 33489, 40977, 79.59369617426736);
INSERT INTO cargo_shipments VALUES (3891423, 702659, 4, 13109, 90980, 981.9820374029973);
INSERT INTO cargo_shipments VALUES (6241043, 686783, 2, 44824, 31827, 494.28044116817017);
INSERT INTO cargo_shipments VALUES (1693890, 804924, 3, 41000, 13852, 262.27659324664575);
INSERT INTO cargo_shipments VALUES (1721407, 861877, 1, 27667, 36126, 619.6097031810194);
INSERT INTO cargo_shipments VALUES (5336557, 751755, 2, 12366, 52470, 316.6732667331048);
INSERT INTO cargo_shipments VALUES (2821252, 382201, 2, 5234, 81659, 926.1401592020269);
INSERT INTO cargo_shipments VALUES (3490332, 927176, 3, 17333, 50820, 833.2836250966484);
INSERT INTO cargo_shipments VALUES (2427695, 982325, 3, 23203, 53482, 701.5916046212516);
INSERT INTO cargo_shipments VALUES (4952265, 681816, 2, 44971, 88864, 834.252206058971);
INSERT INTO cargo_shipments VALUES (4651416, 843961, 4, 45651, 97073, 479.7739813154466);
INSERT INTO cargo_shipments VALUES (3548501, 385272, 3, 6984, 70715, 972.1436050087419);
INSERT INTO cargo_shipments VALUES (6121058, 196131, 3, 31493, 15015, 535.3871900036935);
INSERT INTO cargo_shipments VALUES (3562258, 694403, 1, 34417, 35671, 601.5630154082716);
INSERT INTO cargo_shipments VALUES (3308845, 371841, 3, 17511, 84090, 644.0013168188507);
INSERT INTO cargo_shipments VALUES (4383713, 734764, 2, 44521, 40197, 959.7605859567133);
INSERT INTO cargo_shipments VALUES (2764492, 990047, 1, 31940, 65134, 875.0393449890798);
INSERT INTO cargo_shipments VALUES (7265826, 130564, 2, 43259, 11007, 978.8781463972487);
INSERT INTO cargo_shipments VALUES (2221600, 982325, 3, 29910, 31202, 774.620586264993);
INSERT INTO cargo_shipments VALUES (4763858, 910344, 4, 8475, 56173, 926.6818591168533);
INSERT INTO cargo_shipments VALUES (8316440, 193915, 1, 37334, 15015, 595.3485331280328);
INSERT INTO cargo_shipments VALUES (6695915, 382201, 2, 4800, 13500, 915.0382265410243);
INSERT INTO cargo_shipments VALUES (2555100, 287765, 1, 28469, 57557, 983.2008799323514);
INSERT INTO cargo_shipments VALUES (7815763, 551210, 3, 39548, 45811, 437.7365974788653);
INSERT INTO cargo_shipments VALUES (1125193, 283032, 3, 41619, 12385, 701.572465092866);
INSERT INTO cargo_shipments VALUES (5806845, 158596, 4, 9403, 32322, 970.6341806496547);
INSERT INTO cargo_shipments VALUES (7128619, 294483, 2, 31867, 90980, 659.4713385151061);
INSERT INTO cargo_shipments VALUES (7644297, 964443, 1, 16341, 34532, 581.584580868726);
INSERT INTO cargo_shipments VALUES (9143654, 334157, 1, 49222, 70715, 584.1195498822029);
INSERT INTO cargo_shipments VALUES (2916004, 241612, 3, 44120, 72190, 137.90963208591123);
INSERT INTO cargo_shipments VALUES (3890039, 595282, 2, 46475, 66598, 748.0632294931412);
INSERT INTO cargo_shipments VALUES (8823810, 215106, 4, 15673, 69007, 287.8375125639322);
INSERT INTO cargo_shipments VALUES (4795337, 411362, 2, 16564, 50226, 384.86149944264594);
INSERT INTO cargo_shipments VALUES (3359248, 839442, 2, 43219, 40197, 541.9086152039588);
INSERT INTO cargo_shipments VALUES (7977458, 417366, 4, 41252, 66598, 893.2009139409388);
INSERT INTO cargo_shipments VALUES (9830834, 797115, 2, 27621, 96305, 77.51405137721457);
INSERT INTO cargo_shipments VALUES (7083221, 782892, 3, 1998, 84733, 392.3658000671404);
INSERT INTO cargo_shipments VALUES (1149162, 332486, 1, 43012, 88864, 632.508031759481);
INSERT INTO cargo_shipments VALUES (2834463, 656769, 1, 41943, 35671, 78.72152067802796);
INSERT INTO cargo_shipments VALUES (4885064, 794219, 1, 4483, 87279, 573.1617725842368);
INSERT INTO cargo_shipments VALUES (5573575, 218458, 2, 13124, 49548, 56.45434967730123);
INSERT INTO cargo_shipments VALUES (7744631, 372521, 1, 23173, 58544, 957.9689932574039);
INSERT INTO cargo_shipments VALUES (3389097, 498912, 3, 43497, 86201, 807.7445565656964);
INSERT INTO cargo_shipments VALUES (9249772, 495162, 1, 43290, 36126, 15.994234687091048);
INSERT INTO cargo_shipments VALUES (2611664, 994096, 1, 28396, 46825, 429.86228901856384);
INSERT INTO cargo_shipments VALUES (5539227, 158596, 1, 14573, 32643, 713.7685195283462);
INSERT INTO cargo_shipments VALUES (4617754, 896235, 1, 38828, 72190, 258.6669406694272);
INSERT INTO cargo_shipments VALUES (4350734, 910344, 2, 35826, 15015, 534.3474583793479);
INSERT INTO cargo_shipments VALUES (9219874, 136113, 3, 28677, 99271, 10.851429758857778);
INSERT INTO cargo_shipments VALUES (6922438, 271822, 4, 20222, 57235, 989.3822579418355);
INSERT INTO cargo_shipments VALUES (5579575, 771720, 3, 43679, 56241, 681.8824884722357);
INSERT INTO cargo_shipments VALUES (9600237, 877280, 3, 26639, 67231, 655.9312729448486);
INSERT INTO cargo_shipments VALUES (2077774, 264352, 4, 19661, 53482, 173.51403249115438);
INSERT INTO cargo_shipments VALUES (1086876, 995741, 3, 9507, 38314, 432.0174446661428);
INSERT INTO cargo_shipments VALUES (3609764, 528479, 4, 33785, 35671, 842.7091948841064);
INSERT INTO cargo_shipments VALUES (3649730, 528479, 2, 7971, 88864, 117.31570997658602);
INSERT INTO cargo_shipments VALUES (2595067, 694403, 1, 42931, 57235, 917.9490735582285);
INSERT INTO cargo_shipments VALUES (4530974, 747608, 2, 37042, 32322, 62.23133734645969);
INSERT INTO cargo_shipments VALUES (2234269, 139045, 2, 23662, 65140, 31.626878018334857);
INSERT INTO cargo_shipments VALUES (9816584, 521146, 2, 40194, 93452, 65.56731756674361);
INSERT INTO cargo_shipments VALUES (8707798, 342326, 2, 35875, 97095, 563.5060412842688);
INSERT INTO cargo_shipments VALUES (1971839, 694403, 2, 4148, 12323, 322.9095591134935);
INSERT INTO cargo_shipments VALUES (1254695, 475571, 4, 17167, 17666, 665.228560089899);
INSERT INTO cargo_shipments VALUES (4732080, 614562, 1, 46706, 96305, 992.313418158125);
INSERT INTO cargo_shipments VALUES (8685389, 374146, 2, 31030, 85779, 699.0943664089948);
INSERT INTO cargo_shipments VALUES (3621866, 417366, 3, 35870, 50718, 747.1241543957588);
INSERT INTO cargo_shipments VALUES (3150835, 289019, 3, 27203, 13937, 785.9501807304378);
INSERT INTO cargo_shipments VALUES (8877481, 801910, 2, 11266, 50820, 559.3404823513436);
INSERT INTO cargo_shipments VALUES (5447352, 417017, 4, 1129, 93450, 388.56848947014356);
INSERT INTO cargo_shipments VALUES (1808428, 331143, 1, 35333, 31036, 365.87819467951374);
INSERT INTO cargo_shipments VALUES (1510233, 910344, 3, 8937, 31202, 287.30205489630913);
INSERT INTO cargo_shipments VALUES (8097304, 614215, 3, 47855, 34532, 610.9154850449107);
INSERT INTO cargo_shipments VALUES (1233119, 503550, 2, 1787, 21780, 409.18327197968284);
INSERT INTO cargo_shipments VALUES (3091252, 558631, 2, 10745, 50303, 558.34745628972);
INSERT INTO cargo_shipments VALUES (7167845, 355369, 3, 47963, 84090, 760.767106209723);
INSERT INTO cargo_shipments VALUES (5226236, 125473, 3, 2399, 56962, 265.735650440728);
INSERT INTO cargo_shipments VALUES (3215559, 402347, 4, 26220, 35671, 577.3295392992732);
INSERT INTO cargo_shipments VALUES (1358212, 581627, 3, 16536, 72190, 978.1710946677972);
INSERT INTO cargo_shipments VALUES (4246316, 585228, 4, 14680, 16955, 292.49302569179747);
INSERT INTO cargo_shipments VALUES (7679643, 143671, 3, 20763, 85779, 679.0270116317383);
INSERT INTO cargo_shipments VALUES (6707336, 154401, 3, 19966, 27704, 261.881560326033);
INSERT INTO cargo_shipments VALUES (7140340, 702515, 1, 9611, 41631, 730.5714161408873);
INSERT INTO cargo_shipments VALUES (7866824, 843961, 1, 49446, 66509, 171.77803211695553);
INSERT INTO cargo_shipments VALUES (4152759, 671687, 1, 34329, 56241, 90.5913031616986);
INSERT INTO cargo_shipments VALUES (2015800, 536772, 2, 27332, 65303, 5.282556788472559);
INSERT INTO cargo_shipments VALUES (2058484, 630050, 2, 31615, 84733, 419.1321807388155);
INSERT INTO cargo_shipments VALUES (5214289, 734764, 3, 31525, 15015, 108.37932114512682);
INSERT INTO cargo_shipments VALUES (4050338, 402347, 3, 20867, 13913, 760.50773970204);
INSERT INTO cargo_shipments VALUES (2245950, 820512, 2, 38335, 40977, 991.0150189748026);
INSERT INTO cargo_shipments VALUES (9668476, 562349, 1, 36338, 53482, 869.8006143287138);
INSERT INTO cargo_shipments VALUES (4949140, 196524, 4, 13094, 12385, 144.4036483624488);
INSERT INTO cargo_shipments VALUES (7273360, 940727, 2, 16515, 67231, 571.1168509101446);
INSERT INTO cargo_shipments VALUES (1088755, 853530, 2, 37445, 45811, 372.6163134440354);
INSERT INTO cargo_shipments VALUES (3038957, 176018, 3, 44561, 25322, 383.225468841915);
INSERT INTO cargo_shipments VALUES (9717233, 295088, 2, 24463, 87279, 79.03569351340356);
INSERT INTO cargo_shipments VALUES (9403311, 804924, 2, 1086, 17666, 894.3359502837624);
INSERT INTO cargo_shipments VALUES (4209946, 154401, 3, 48527, 44873, 643.5821699426068);
INSERT INTO cargo_shipments VALUES (1731948, 196131, 2, 8148, 67231, 985.989593845607);
INSERT INTO cargo_shipments VALUES (1210553, 843961, 2, 28585, 32322, 920.2720232044087);
INSERT INTO cargo_shipments VALUES (8856857, 816550, 1, 2222, 42021, 72.30534411955081);
INSERT INTO cargo_shipments VALUES (2744565, 839964, 1, 9029, 67231, 572.2039466507189);
INSERT INTO cargo_shipments VALUES (8989419, 947049, 2, 12046, 49548, 5.36267422843939);
INSERT INTO cargo_shipments VALUES (9821924, 887047, 2, 12369, 87026, 884.2676394273827);
INSERT INTO cargo_shipments VALUES (2249896, 371982, 1, 4292, 11007, 567.9888303355192);
INSERT INTO cargo_shipments VALUES (9181448, 486096, 4, 9415, 40197, 196.40113089743582);
INSERT INTO cargo_shipments VALUES (9544209, 742571, 4, 9319, 57468, 104.12003377241874);
INSERT INTO cargo_shipments VALUES (1023911, 422672, 4, 11290, 76084, 81.87437803124176);
INSERT INTO cargo_shipments VALUES (7412352, 747608, 3, 39606, 97095, 676.0300140230715);
INSERT INTO cargo_shipments VALUES (9510980, 933725, 1, 1763, 42260, 824.1606682950886);
INSERT INTO cargo_shipments VALUES (9287719, 961289, 1, 9457, 64330, 650.7605739979116);
INSERT INTO cargo_shipments VALUES (5446340, 417017, 3, 18654, 58284, 842.3933145352966);
INSERT INTO cargo_shipments VALUES (7600678, 654139, 3, 24376, 34532, 45.18180664335125);
INSERT INTO cargo_shipments VALUES (1277374, 742571, 3, 33943, 58567, 526.4600859546279);
INSERT INTO cargo_shipments VALUES (6077536, 742571, 4, 29686, 60373, 638.9028958535025);
INSERT INTO cargo_shipments VALUES (4535431, 947049, 2, 9401, 11007, 27.284608331195393);
INSERT INTO cargo_shipments VALUES (7352699, 311617, 3, 43120, 38161, 537.3657039133367);
INSERT INTO cargo_shipments VALUES (2547258, 536772, 1, 40896, 42611, 422.1849810597219);
INSERT INTO cargo_shipments VALUES (3285470, 816550, 3, 47504, 32643, 883.9738387676382);
INSERT INTO cargo_shipments VALUES (3266031, 503550, 2, 10608, 97073, 545.3807597976187);
INSERT INTO cargo_shipments VALUES (9122199, 771720, 3, 30317, 12118, 726.4406948899662);
INSERT INTO cargo_shipments VALUES (1372666, 295660, 4, 27504, 11051, 426.34408765047385);
INSERT INTO cargo_shipments VALUES (8020212, 587854, 4, 40865, 35671, 993.1284642605284);
INSERT INTO cargo_shipments VALUES (3722267, 207609, 4, 1880, 76084, 573.4582062218491);
INSERT INTO cargo_shipments VALUES (6579138, 241612, 1, 22196, 11007, 338.758906300665);
INSERT INTO cargo_shipments VALUES (8571318, 331143, 2, 42243, 42611, 538.6598192151279);
INSERT INTO cargo_shipments VALUES (5064539, 581627, 2, 2022, 42690, 876.8527897855515);
INSERT INTO cargo_shipments VALUES (9129473, 656769, 3, 31947, 76084, 859.9416915612396);
INSERT INTO cargo_shipments VALUES (7390024, 554618, 2, 40654, 61652, 206.27322608573573);
INSERT INTO cargo_shipments VALUES (8965274, 849862, 2, 12098, 18549, 498.1492286376783);
INSERT INTO cargo_shipments VALUES (8687357, 246438, 2, 11291, 91899, 115.06837528960246);
INSERT INTO cargo_shipments VALUES (8004507, 464221, 4, 1854, 60373, 827.4334757379867);
INSERT INTO cargo_shipments VALUES (9246064, 294483, 3, 14571, 27704, 694.1743465840104);
INSERT INTO cargo_shipments VALUES (2991780, 104719, 4, 10090, 86258, 918.4870647968412);
INSERT INTO cargo_shipments VALUES (2936578, 222414, 2, 42956, 12118, 386.1869808034273);
INSERT INTO cargo_shipments VALUES (9898458, 994096, 2, 16630, 31202, 982.8915009437799);
INSERT INTO cargo_shipments VALUES (1153674, 145117, 4, 10323, 60646, 360.26553470416);
INSERT INTO cargo_shipments VALUES (5048088, 371841, 4, 15836, 15015, 836.908997698146);
INSERT INTO cargo_shipments VALUES (5782026, 671988, 3, 36926, 84690, 839.3848202704196);
INSERT INTO cargo_shipments VALUES (2904748, 306162, 3, 11524, 32322, 585.2540500815401);
INSERT INTO cargo_shipments VALUES (3579455, 264687, 1, 17898, 82368, 120.86165305732644);
INSERT INTO cargo_shipments VALUES (6136799, 581627, 3, 38075, 47423, 311.7738676442159);
INSERT INTO cargo_shipments VALUES (4077405, 820512, 1, 10696, 57235, 69.31373813261);
INSERT INTO cargo_shipments VALUES (7900075, 843961, 3, 43475, 99271, 26.57565378924154);
INSERT INTO cargo_shipments VALUES (4128626, 303968, 1, 12371, 58544, 185.80847651517573);
INSERT INTO cargo_shipments VALUES (4203213, 139249, 4, 49241, 66509, 408.617783350847);
INSERT INTO cargo_shipments VALUES (4283328, 735758, 2, 44495, 88864, 518.9549798066234);
INSERT INTO cargo_shipments VALUES (9233680, 970733, 2, 37384, 31202, 262.3549683559101);
INSERT INTO cargo_shipments VALUES (3685394, 835095, 1, 32922, 62432, 861.3156276798962);
INSERT INTO cargo_shipments VALUES (6648457, 495162, 1, 15494, 97095, 945.1639207722627);
INSERT INTO cargo_shipments VALUES (8190787, 745952, 4, 25056, 70715, 258.921199912135);
INSERT INTO cargo_shipments VALUES (5829848, 903883, 3, 31332, 50718, 241.3091825856416);
INSERT INTO cargo_shipments VALUES (6334842, 173340, 3, 12532, 41477, 290.67065851043185);
INSERT INTO cargo_shipments VALUES (7605033, 793892, 1, 42765, 81659, 139.44899466757354);
INSERT INTO cargo_shipments VALUES (1224107, 291137, 2, 13408, 41786, 91.21565312012225);
INSERT INTO cargo_shipments VALUES (2196615, 240561, 2, 17641, 91899, 828.5152792164477);
INSERT INTO cargo_shipments VALUES (8726204, 158596, 1, 26654, 31036, 963.3148173941737);
INSERT INTO cargo_shipments VALUES (3383514, 702659, 3, 43174, 52470, 623.6894534901303);
INSERT INTO cargo_shipments VALUES (6520030, 702515, 4, 4498, 56621, 57.02282773719958);
INSERT INTO cargo_shipments VALUES (4835335, 342224, 4, 43367, 87026, 927.909202700222);
INSERT INTO cargo_shipments VALUES (3219860, 193915, 3, 41861, 50820, 299.4196017308316);
INSERT INTO cargo_shipments VALUES (4292951, 900498, 2, 30666, 55085, 317.33194077222004);
INSERT INTO cargo_shipments VALUES (3941815, 705140, 1, 19656, 50303, 496.16106518614987);
INSERT INTO cargo_shipments VALUES (9715346, 785591, 3, 3819, 66509, 596.798335098339);
INSERT INTO cargo_shipments VALUES (9194803, 797115, 4, 25683, 69330, 437.0601685609938);
INSERT INTO cargo_shipments VALUES (3858031, 388399, 1, 24083, 85779, 629.5264309992788);
INSERT INTO cargo_shipments VALUES (7019047, 311617, 2, 14623, 31202, 356.02158690927155);
INSERT INTO cargo_shipments VALUES (6480201, 158596, 1, 30807, 90980, 713.1982062754082);
INSERT INTO cargo_shipments VALUES (7146747, 495162, 1, 16410, 41926, 56.65744810670381);
INSERT INTO cargo_shipments VALUES (3846184, 136113, 1, 25552, 60373, 829.8851816085084);
INSERT INTO cargo_shipments VALUES (1340486, 562349, 4, 23489, 45811, 600.7079759232647);
INSERT INTO cargo_shipments VALUES (1767427, 778047, 2, 41679, 50782, 71.54646482869464);
INSERT INTO cargo_shipments VALUES (5671319, 961289, 2, 15097, 31827, 207.31650123111766);
INSERT INTO cargo_shipments VALUES (9766599, 708494, 3, 20408, 76581, 248.7367595122123);
INSERT INTO cargo_shipments VALUES (1907723, 955811, 4, 32726, 87279, 176.99360174420775);
INSERT INTO cargo_shipments VALUES (9392585, 951732, 1, 31568, 38161, 721.8300294950446);
INSERT INTO cargo_shipments VALUES (3313678, 818702, 2, 13450, 55085, 598.6703007096673);
INSERT INTO cargo_shipments VALUES (5551826, 807747, 4, 8174, 28848, 532.2391225660956);
INSERT INTO cargo_shipments VALUES (4775791, 622718, 4, 39265, 41547, 615.5635917967135);
INSERT INTO cargo_shipments VALUES (5669302, 650161, 2, 38356, 61652, 396.57612547728115);
INSERT INTO cargo_shipments VALUES (3074481, 388484, 3, 29569, 73292, 928.770188158044);
INSERT INTO cargo_shipments VALUES (7418592, 801910, 2, 31153, 40390, 312.2786426826538);
INSERT INTO cargo_shipments VALUES (1540582, 246854, 4, 43086, 84275, 842.5631151460723);
INSERT INTO cargo_shipments VALUES (7599830, 104116, 4, 42627, 31827, 877.1722744474527);
INSERT INTO cargo_shipments VALUES (9792437, 931397, 1, 16827, 42611, 27.61339231766524);
INSERT INTO cargo_shipments VALUES (1772751, 993138, 1, 45163, 82368, 525.0272061503971);
INSERT INTO cargo_shipments VALUES (3297917, 583957, 1, 17897, 69140, 629.9864881607033);
INSERT INTO cargo_shipments VALUES (3058730, 521146, 4, 2949, 94669, 692.9094417800051);
INSERT INTO cargo_shipments VALUES (4524800, 498912, 1, 11596, 82660, 280.57423569556084);
INSERT INTO cargo_shipments VALUES (3420858, 335981, 1, 18696, 86258, 960.0401783519333);
INSERT INTO cargo_shipments VALUES (3450174, 618905, 3, 16852, 46381, 940.8861762466871);
INSERT INTO cargo_shipments VALUES (4547465, 193915, 3, 2764, 11060, 554.796960369568);
INSERT INTO cargo_shipments VALUES (4566987, 495332, 4, 23021, 40390, 845.2797339615422);
INSERT INTO cargo_shipments VALUES (2754995, 303968, 3, 35504, 67231, 670.9351471025485);
INSERT INTO cargo_shipments VALUES (3773521, 671988, 3, 34806, 50718, 785.9973927520174);
INSERT INTO cargo_shipments VALUES (5671972, 417366, 3, 6564, 36453, 990.5103709402663);
INSERT INTO cargo_shipments VALUES (5988830, 876023, 2, 5541, 70715, 589.9956957153511);
INSERT INTO cargo_shipments VALUES (7753987, 255144, 1, 10720, 34532, 608.4289497913938);
INSERT INTO cargo_shipments VALUES (8085137, 624781, 4, 41036, 91899, 775.2163440784132);
INSERT INTO cargo_shipments VALUES (9137871, 734764, 2, 34744, 58544, 487.3660095249173);
INSERT INTO cargo_shipments VALUES (3309829, 341564, 2, 13402, 91113, 272.1746224233693);
INSERT INTO cargo_shipments VALUES (1003481, 513030, 2, 15864, 17666, 740.8302345582111);
INSERT INTO cargo_shipments VALUES (9862585, 722031, 4, 27312, 86201, 401.41438849223863);
INSERT INTO cargo_shipments VALUES (5648812, 173966, 3, 13068, 19686, 219.72925739096993);
INSERT INTO cargo_shipments VALUES (3481540, 332486, 1, 44841, 97095, 977.0228104430942);
INSERT INTO cargo_shipments VALUES (3615672, 681816, 1, 30597, 91113, 553.7681619116427);
INSERT INTO cargo_shipments VALUES (5936962, 735758, 2, 45182, 94669, 76.80462665086097);
INSERT INTO cargo_shipments VALUES (9394359, 295088, 1, 12580, 65140, 985.0219686244282);
INSERT INTO cargo_shipments VALUES (7568438, 230534, 4, 15924, 96305, 209.19330435337392);
INSERT INTO cargo_shipments VALUES (2259529, 365226, 3, 39158, 84090, 876.4927137100724);
INSERT INTO cargo_shipments VALUES (5894765, 371308, 2, 14392, 16955, 602.0488023422648);
INSERT INTO cargo_shipments VALUES (7805144, 513030, 4, 41997, 93450, 179.83416555645627);
INSERT INTO cargo_shipments VALUES (5046474, 655649, 2, 48381, 57235, 799.4074019986508);
INSERT INTO cargo_shipments VALUES (3674781, 835095, 3, 47578, 54679, 516.4107903562851);
INSERT INTO cargo_shipments VALUES (4259654, 194682, 2, 1656, 12385, 678.8336374593716);
INSERT INTO cargo_shipments VALUES (6043404, 240561, 1, 30851, 36453, 160.06248469304342);
INSERT INTO cargo_shipments VALUES (3983194, 294483, 3, 10360, 76581, 904.2695740558568);
INSERT INTO cargo_shipments VALUES (7136081, 807747, 2, 24078, 32643, 338.00873785503006);
INSERT INTO cargo_shipments VALUES (3070193, 265945, 2, 38184, 56173, 990.8476830707195);
INSERT INTO cargo_shipments VALUES (8283478, 346198, 3, 32988, 87702, 975.975445041514);
INSERT INTO cargo_shipments VALUES (7232943, 546827, 1, 30576, 53482, 41.73089436536736);
INSERT INTO cargo_shipments VALUES (8022370, 331143, 2, 35593, 54679, 64.43472118561044);
INSERT INTO cargo_shipments VALUES (1905605, 404391, 4, 14682, 84090, 431.10661093502875);
INSERT INTO cargo_shipments VALUES (3665219, 745546, 4, 13563, 93450, 374.2025260207374);
INSERT INTO cargo_shipments VALUES (2762000, 864161, 3, 10982, 91899, 829.7189305034599);
INSERT INTO cargo_shipments VALUES (7552778, 834586, 2, 3067, 56962, 521.027211976929);
INSERT INTO cargo_shipments VALUES (1345821, 598515, 4, 1412, 13852, 949.7990514007877);
INSERT INTO cargo_shipments VALUES (7485321, 952986, 3, 34900, 91113, 85.63440600736794);
INSERT INTO cargo_shipments VALUES (2418517, 964737, 3, 28822, 42260, 865.7985583806311);
INSERT INTO cargo_shipments VALUES (2750665, 622340, 3, 31922, 93452, 889.3324978609228);
INSERT INTO cargo_shipments VALUES (9487277, 925782, 3, 43082, 27704, 606.9148116088003);
INSERT INTO cargo_shipments VALUES (7771026, 832152, 4, 44011, 12118, 844.4484085299385);
INSERT INTO cargo_shipments VALUES (7801150, 681816, 2, 29016, 33485, 241.54193204128404);
INSERT INTO cargo_shipments VALUES (6831490, 954861, 3, 42151, 32643, 20.797654386252475);
INSERT INTO cargo_shipments VALUES (4871510, 955018, 3, 27170, 91113, 125.92705022244589);
INSERT INTO cargo_shipments VALUES (8125740, 982325, 4, 37503, 69140, 728.1447005180878);
INSERT INTO cargo_shipments VALUES (2271033, 702515, 4, 40534, 44873, 924.9575252656527);
INSERT INTO cargo_shipments VALUES (8024480, 536772, 1, 10523, 60373, 679.8695628168914);
INSERT INTO cargo_shipments VALUES (5040806, 104719, 4, 31617, 32577, 548.9646244616422);
INSERT INTO cargo_shipments VALUES (8766028, 214614, 2, 45509, 76581, 52.00551955098087);
INSERT INTO cargo_shipments VALUES (3147459, 947599, 2, 24355, 41410, 902.3630297556878);
INSERT INTO cargo_shipments VALUES (8137241, 283188, 1, 10806, 45811, 626.228167563435);
INSERT INTO cargo_shipments VALUES (2866773, 443855, 4, 14919, 12323, 741.2797444649884);
INSERT INTO cargo_shipments VALUES (2954666, 229560, 3, 21370, 87279, 318.71738352802157);
INSERT INTO cargo_shipments VALUES (2197229, 964737, 3, 13586, 50820, 505.9347013747183);
INSERT INTO cargo_shipments VALUES (6479491, 771720, 1, 49901, 28848, 733.7702922086066);
INSERT INTO cargo_shipments VALUES (3592678, 123476, 2, 17606, 58567, 176.64898612518155);
INSERT INTO cargo_shipments VALUES (3983798, 708494, 3, 34662, 27704, 850.6020599690323);
INSERT INTO cargo_shipments VALUES (2947546, 622340, 4, 6913, 90765, 613.0740682833593);
INSERT INTO cargo_shipments VALUES (5998914, 655649, 4, 1859, 88864, 344.1931989536672);
INSERT INTO cargo_shipments VALUES (7114483, 864161, 3, 32091, 17590, 40.16086778614769);
INSERT INTO cargo_shipments VALUES (8157354, 585228, 3, 31530, 30478, 96.3044338187241);
INSERT INTO cargo_shipments VALUES (6939945, 982325, 1, 40244, 57557, 866.894870793575);
INSERT INTO cargo_shipments VALUES (4904916, 817590, 4, 10935, 81659, 37.57637664106539);
INSERT INTO cargo_shipments VALUES (2856663, 378721, 2, 3325, 50718, 329.7729665853233);
INSERT INTO cargo_shipments VALUES (9802745, 466776, 4, 32888, 16955, 214.78216050282273);
INSERT INTO cargo_shipments VALUES (5887109, 895631, 2, 44839, 13852, 602.7175966287914);
INSERT INTO cargo_shipments VALUES (5493838, 371841, 1, 16764, 12118, 918.7920041557159);
INSERT INTO cargo_shipments VALUES (7916471, 108703, 3, 5570, 38161, 917.4135240877902);
INSERT INTO cargo_shipments VALUES (3683808, 945423, 1, 23653, 57557, 20.9738721395778);
INSERT INTO cargo_shipments VALUES (5080419, 944074, 2, 24624, 93452, 792.6788242740718);
INSERT INTO cargo_shipments VALUES (4133379, 964443, 1, 2342, 97073, 286.98758583467867);
INSERT INTO cargo_shipments VALUES (1213387, 145117, 4, 28303, 59681, 846.8160124877305);
INSERT INTO cargo_shipments VALUES (1183840, 480401, 2, 40102, 45811, 134.65010984394655);
INSERT INTO cargo_shipments VALUES (3158657, 667744, 3, 8946, 88516, 413.78248605780675);
INSERT INTO cargo_shipments VALUES (6367517, 864161, 1, 1689, 84690, 182.31507061464168);
INSERT INTO cargo_shipments VALUES (3659522, 964737, 3, 41921, 41926, 753.3999976648504);
INSERT INTO cargo_shipments VALUES (3561986, 295660, 1, 44383, 27704, 663.4687455346543);
INSERT INTO cargo_shipments VALUES (6394626, 651365, 2, 10795, 46381, 131.9321969761711);
INSERT INTO cargo_shipments VALUES (9720250, 919415, 1, 2601, 70715, 138.31454132197806);
INSERT INTO cargo_shipments VALUES (6388077, 486096, 2, 40024, 32643, 858.5024125376056);
INSERT INTO cargo_shipments VALUES (7392766, 332486, 1, 10763, 97073, 413.8923125318296);
INSERT INTO cargo_shipments VALUES (5038242, 477350, 4, 26823, 87026, 304.46005078522643);
INSERT INTO cargo_shipments VALUES (3934243, 694403, 4, 40793, 94338, 333.8951852117925);
INSERT INTO cargo_shipments VALUES (1858680, 346198, 3, 36011, 32322, 49.762984288451165);
INSERT INTO cargo_shipments VALUES (9593049, 613587, 3, 10411, 50820, 413.55067696621006);
INSERT INTO cargo_shipments VALUES (9035865, 838513, 3, 29077, 46825, 919.6449664695043);
INSERT INTO cargo_shipments VALUES (6121192, 650161, 1, 49287, 57235, 967.1338076306977);
INSERT INTO cargo_shipments VALUES (2311886, 940727, 1, 43741, 87702, 855.8124258916968);
INSERT INTO cargo_shipments VALUES (9932064, 839964, 4, 9789, 15015, 644.6709384779745);
INSERT INTO cargo_shipments VALUES (3706293, 546827, 1, 3011, 87702, 399.29572615119014);
INSERT INTO cargo_shipments VALUES (7273696, 264687, 3, 9969, 32577, 60.46298309673625);
INSERT INTO cargo_shipments VALUES (8123783, 681816, 3, 46857, 42690, 95.8915802187319);
INSERT INTO cargo_shipments VALUES (9378228, 778047, 2, 23049, 50718, 992.8500934881374);
INSERT INTO cargo_shipments VALUES (1251422, 944547, 4, 19802, 15898, 191.36030843774031);
INSERT INTO cargo_shipments VALUES (8023116, 587854, 2, 31977, 15898, 576.6791065941892);
INSERT INTO cargo_shipments VALUES (2847905, 764454, 3, 27122, 31036, 715.3695810091981);
INSERT INTO cargo_shipments VALUES (3099963, 839442, 4, 42461, 85779, 536.959126401367);
INSERT INTO cargo_shipments VALUES (1144474, 104116, 1, 21707, 53684, 613.834551849774);
INSERT INTO cargo_shipments VALUES (1470631, 764454, 3, 49091, 36126, 180.61524050403943);
INSERT INTO cargo_shipments VALUES (9961849, 742571, 2, 28706, 65303, 587.1274521566894);
INSERT INTO cargo_shipments VALUES (3893641, 630921, 3, 48102, 65140, 286.8819845304511);
INSERT INTO cargo_shipments VALUES (6039406, 885046, 2, 49696, 41547, 26.020624447527396);
INSERT INTO cargo_shipments VALUES (8281059, 218458, 3, 7237, 50226, 162.48772591573456);
INSERT INTO cargo_shipments VALUES (1701821, 498912, 3, 6593, 42611, 821.8106634600756);
INSERT INTO cargo_shipments VALUES (1431535, 105592, 4, 38055, 76581, 54.84251739365453);
INSERT INTO cargo_shipments VALUES (8952176, 225405, 1, 29761, 56173, 420.1121925600921);
INSERT INTO cargo_shipments VALUES (5794299, 785591, 2, 6724, 53234, 94.74609230397779);
INSERT INTO cargo_shipments VALUES (4864641, 804924, 3, 17262, 40977, 160.86153275679305);
INSERT INTO cargo_shipments VALUES (6086440, 475571, 4, 35471, 35671, 56.13587344739046);
INSERT INTO cargo_shipments VALUES (5687634, 246854, 4, 46397, 88864, 650.8011256011466);
INSERT INTO cargo_shipments VALUES (1734594, 105592, 4, 32013, 56621, 317.0236714432013);
INSERT INTO cargo_shipments VALUES (6675192, 388891, 3, 34089, 12323, 812.0381496867183);
INSERT INTO cargo_shipments VALUES (5071822, 355369, 4, 19113, 36453, 510.33756297487986);
INSERT INTO cargo_shipments VALUES (9371137, 885046, 2, 47144, 57235, 626.0030899420598);
INSERT INTO cargo_shipments VALUES (8657338, 903883, 2, 44840, 17590, 260.84739271654377);
INSERT INTO cargo_shipments VALUES (2333005, 336831, 4, 32854, 41926, 48.38147459667208);
INSERT INTO cargo_shipments VALUES (5578084, 833947, 2, 5583, 84733, 271.505067614942);
INSERT INTO cargo_shipments VALUES (2808939, 355369, 1, 29941, 97073, 719.9605485098596);
INSERT INTO cargo_shipments VALUES (7174318, 952986, 1, 12219, 42260, 909.8678409668946);
INSERT INTO cargo_shipments VALUES (4648788, 311617, 2, 18836, 70715, 852.7089497441171);
INSERT INTO cargo_shipments VALUES (4930800, 563359, 1, 44570, 67231, 512.3960574644562);
INSERT INTO cargo_shipments VALUES (6380915, 136113, 4, 15813, 58544, 972.305074187997);
INSERT INTO cargo_shipments VALUES (6897426, 295660, 3, 6654, 17590, 597.955144379408);
INSERT INTO cargo_shipments VALUES (9601159, 835095, 3, 32472, 58284, 380.615806714364);
INSERT INTO cargo_shipments VALUES (9685093, 193915, 3, 3710, 45811, 255.30530996234813);
INSERT INTO cargo_shipments VALUES (3492599, 927176, 2, 41373, 46825, 408.9362766064879);
INSERT INTO cargo_shipments VALUES (6045864, 176018, 3, 33681, 25322, 406.73656730153664);
INSERT INTO cargo_shipments VALUES (4730109, 627684, 4, 20024, 61652, 936.5313950548045);
INSERT INTO cargo_shipments VALUES (2935483, 944394, 3, 4560, 11051, 489.2498191168142);
INSERT INTO cargo_shipments VALUES (5228896, 336949, 3, 5073, 12323, 285.85459796927483);
INSERT INTO cargo_shipments VALUES (4312442, 173966, 4, 42010, 69007, 747.973417529262);
INSERT INTO cargo_shipments VALUES (4163376, 139045, 4, 24305, 65140, 888.3945004624435);
INSERT INTO cargo_shipments VALUES (8109076, 103502, 1, 44005, 35671, 139.21163147861992);
INSERT INTO cargo_shipments VALUES (9867628, 218188, 4, 34567, 13852, 370.34539283165293);
INSERT INTO cargo_shipments VALUES (4487954, 708494, 4, 46945, 57235, 229.05535917742804);
INSERT INTO cargo_shipments VALUES (9080138, 885046, 1, 13785, 57235, 433.70958248869016);
INSERT INTO cargo_shipments VALUES (6194225, 388399, 3, 43797, 11060, 105.22643223229822);
INSERT INTO cargo_shipments VALUES (1417380, 130564, 4, 30791, 56615, 19.737999466140966);
INSERT INTO cargo_shipments VALUES (7272136, 495332, 2, 20362, 33265, 593.6012460718015);
INSERT INTO cargo_shipments VALUES (1069035, 598515, 1, 12990, 41786, 554.5267199514915);
INSERT INTO cargo_shipments VALUES (6349703, 480401, 1, 38815, 56241, 58.46524541935372);
INSERT INTO cargo_shipments VALUES (6087038, 286425, 4, 13570, 57235, 90.30490358338471);
INSERT INTO cargo_shipments VALUES (9275523, 563359, 1, 12436, 12385, 405.6965957407153);
INSERT INTO cargo_shipments VALUES (6078356, 230534, 3, 46734, 73560, 963.6151875808217);
INSERT INTO cargo_shipments VALUES (4540739, 955811, 3, 22283, 67231, 366.9750433812723);
INSERT INTO cargo_shipments VALUES (6089294, 785591, 2, 15542, 84090, 577.4356453589259);
INSERT INTO cargo_shipments VALUES (8562100, 503550, 4, 30471, 66598, 24.8618862549963);
INSERT INTO cargo_shipments VALUES (2830498, 710029, 2, 48442, 13852, 975.9549687203858);
INSERT INTO cargo_shipments VALUES (7729301, 876023, 3, 29630, 41547, 84.57245563153803);
INSERT INTO cargo_shipments VALUES (6797787, 218458, 1, 1238, 73560, 114.82774752339775);
INSERT INTO cargo_shipments VALUES (6045697, 225405, 3, 17220, 84690, 648.3693578441603);
INSERT INTO cargo_shipments VALUES (1083286, 495162, 3, 33175, 12385, 94.61797956696039);
INSERT INTO cargo_shipments VALUES (3967671, 287765, 4, 30307, 94072, 730.7317879585223);
INSERT INTO cargo_shipments VALUES (4076599, 838513, 2, 28127, 65140, 52.18179193721528);
INSERT INTO cargo_shipments VALUES (7725604, 229560, 3, 4138, 19686, 588.6737842941822);
INSERT INTO cargo_shipments VALUES (7722451, 622340, 2, 3261, 13913, 300.4304359976573);
INSERT INTO cargo_shipments VALUES (7007813, 820512, 2, 32608, 65140, 616.4816185916637);
INSERT INTO cargo_shipments VALUES (8437312, 734277, 2, 38544, 82660, 748.4584126333888);
INSERT INTO cargo_shipments VALUES (1749957, 560171, 1, 27806, 67231, 213.91686241576468);
INSERT INTO cargo_shipments VALUES (8693008, 422672, 3, 1707, 46825, 594.8388945582217);
INSERT INTO cargo_shipments VALUES (5693533, 196524, 3, 24394, 96305, 649.884011906519);
INSERT INTO cargo_shipments VALUES (4879470, 157492, 1, 1813, 53684, 954.1545980574138);
INSERT INTO cargo_shipments VALUES (7070662, 374146, 4, 32396, 15015, 445.0859349564862);
INSERT INTO cargo_shipments VALUES (7961729, 843101, 4, 42039, 88864, 522.2003066581233);
INSERT INTO cargo_shipments VALUES (9876115, 143671, 4, 49086, 53234, 430.62695832070006);
INSERT INTO cargo_shipments VALUES (7257893, 217450, 4, 2739, 36126, 519.6025700474545);
INSERT INTO cargo_shipments VALUES (3112120, 610948, 2, 2104, 84275, 889.8291157794163);
INSERT INTO cargo_shipments VALUES (1002920, 229560, 2, 6255, 65303, 632.3067505584364);
INSERT INTO cargo_shipments VALUES (4483168, 931397, 1, 16882, 53482, 965.8817772618105);
INSERT INTO cargo_shipments VALUES (9383295, 667744, 2, 25258, 56962, 514.914836517659);
INSERT INTO cargo_shipments VALUES (9956796, 671988, 1, 37626, 69140, 827.9385054209625);
INSERT INTO cargo_shipments VALUES (5896762, 558631, 4, 25338, 90980, 383.38959506729077);
INSERT INTO cargo_shipments VALUES (4899558, 365226, 3, 37416, 62432, 367.2249380887147);
INSERT INTO cargo_shipments VALUES (6569405, 378721, 2, 17595, 91113, 987.1256234719084);
INSERT INTO cargo_shipments VALUES (7146458, 730182, 1, 16080, 76084, 252.66634984856518);
INSERT INTO cargo_shipments VALUES (2708450, 230534, 2, 41963, 40390, 645.8285665093983);
INSERT INTO cargo_shipments VALUES (3566652, 694403, 2, 35690, 10309, 994.7807528966333);
INSERT INTO cargo_shipments VALUES (7070248, 536454, 2, 18557, 90765, 220.07265772720552);
INSERT INTO cargo_shipments VALUES (7529102, 374146, 2, 2783, 53482, 412.02772984670565);
INSERT INTO cargo_shipments VALUES (4449930, 563359, 4, 31363, 86201, 825.2427315530579);
INSERT INTO cargo_shipments VALUES (8877880, 495162, 3, 23573, 69007, 973.1652665141328);
INSERT INTO cargo_shipments VALUES (3382018, 769533, 3, 21050, 42260, 170.75922810417276);
INSERT INTO cargo_shipments VALUES (9169839, 157492, 2, 30630, 41477, 295.02334758363924);
INSERT INTO cargo_shipments VALUES (2809237, 735758, 3, 26284, 87702, 464.5982483182609);
INSERT INTO cargo_shipments VALUES (2505958, 208059, 1, 8009, 88516, 969.4492604072245);
INSERT INTO cargo_shipments VALUES (5212432, 650161, 2, 7323, 57468, 324.44313649457865);
INSERT INTO cargo_shipments VALUES (1386307, 617268, 1, 8880, 84733, 644.1134909273394);
INSERT INTO cargo_shipments VALUES (2805343, 983670, 4, 33169, 41477, 66.11263989987893);
INSERT INTO cargo_shipments VALUES (5755716, 726322, 2, 31470, 56621, 250.13577439885137);
INSERT INTO cargo_shipments VALUES (3690286, 194682, 1, 27817, 56173, 180.73675606415475);
INSERT INTO cargo_shipments VALUES (7348488, 745546, 3, 20257, 42021, 685.3690294621041);
INSERT INTO cargo_shipments VALUES (2303525, 480401, 1, 49892, 59459, 219.55638042856162);
INSERT INTO cargo_shipments VALUES (4438795, 414337, 2, 4306, 76581, 28.906504678018695);
INSERT INTO cargo_shipments VALUES (6463625, 785591, 4, 8171, 56962, 538.8178245049993);
INSERT INTO cargo_shipments VALUES (2647917, 734764, 1, 24369, 41926, 312.6945442316209);
INSERT INTO cargo_shipments VALUES (7142585, 964443, 1, 34608, 57557, 585.0750395266135);
INSERT INTO cargo_shipments VALUES (9032671, 745952, 2, 12368, 94072, 144.53534982791126);
INSERT INTO cargo_shipments VALUES (7378296, 388484, 1, 40511, 61652, 63.737632473024554);
INSERT INTO cargo_shipments VALUES (3428707, 595282, 1, 27711, 42021, 644.7099484329202);
INSERT INTO cargo_shipments VALUES (5106661, 331143, 1, 6005, 32322, 442.88995041959544);
INSERT INTO cargo_shipments VALUES (4059249, 843101, 4, 36495, 76084, 125.62388176992434);
INSERT INTO cargo_shipments VALUES (5698458, 355369, 4, 47314, 99271, 992.5462002630491);
INSERT INTO cargo_shipments VALUES (8048441, 336831, 1, 48161, 84090, 935.1628802149494);
INSERT INTO cargo_shipments VALUES (8529401, 804924, 2, 19280, 57235, 775.3075356635763);
INSERT INTO cargo_shipments VALUES (2276472, 430854, 3, 17911, 48653, 102.24674008065548);
INSERT INTO cargo_shipments VALUES (3211607, 668736, 2, 32209, 32577, 474.77330345039195);
INSERT INTO cargo_shipments VALUES (1870840, 593162, 1, 31666, 45811, 409.92170477349686);
INSERT INTO cargo_shipments VALUES (7972135, 630921, 2, 10084, 60373, 505.8727955391825);
INSERT INTO cargo_shipments VALUES (8758652, 961289, 3, 1823, 90765, 436.5122028769739);
INSERT INTO cargo_shipments VALUES (1858553, 903883, 4, 36260, 48653, 896.6543035958297);
INSERT INTO cargo_shipments VALUES (1400372, 103502, 1, 12642, 66598, 457.4547419843551);
INSERT INTO cargo_shipments VALUES (3815321, 613587, 4, 44335, 76581, 453.4947680827324);
INSERT INTO cargo_shipments VALUES (5638226, 876023, 2, 27623, 84090, 167.73282236615483);
INSERT INTO cargo_shipments VALUES (3967120, 194682, 1, 39738, 90980, 230.78509384547263);
INSERT INTO cargo_shipments VALUES (4106121, 355369, 4, 41186, 50303, 129.0696113078703);
INSERT INTO cargo_shipments VALUES (7635505, 480401, 3, 5073, 59459, 602.3555889121934);
INSERT INTO cargo_shipments VALUES (9590882, 681816, 1, 12759, 36453, 455.0330084030968);
INSERT INTO cargo_shipments VALUES (8159945, 618905, 4, 36091, 59636, 697.0417488242949);
INSERT INTO cargo_shipments VALUES (7392511, 818702, 2, 14005, 11051, 94.89287376924061);
INSERT INTO cargo_shipments VALUES (2422979, 306162, 4, 21493, 50581, 658.4036628615912);
INSERT INTO cargo_shipments VALUES (3047631, 571370, 2, 42076, 59459, 248.1454504545705);
INSERT INTO cargo_shipments VALUES (2989254, 341564, 1, 49112, 86258, 696.7035942934084);
INSERT INTO cargo_shipments VALUES (8374114, 839442, 1, 42967, 32322, 762.5021255197946);
INSERT INTO cargo_shipments VALUES (7161804, 919657, 1, 8146, 99271, 799.6379425606619);
INSERT INTO cargo_shipments VALUES (4648522, 336949, 1, 5812, 79895, 85.21764230661077);
INSERT INTO cargo_shipments VALUES (5895171, 843961, 4, 1320, 21780, 256.31196954039416);
INSERT INTO cargo_shipments VALUES (1612339, 371308, 4, 41826, 94669, 692.6683932925268);
INSERT INTO cargo_shipments VALUES (6698540, 265945, 2, 37481, 66509, 7.219621274262233);
INSERT INTO cargo_shipments VALUES (5970472, 551210, 4, 37535, 91899, 305.3325997308647);
INSERT INTO cargo_shipments VALUES (2786636, 466776, 3, 21062, 65140, 365.3765652351179);
INSERT INTO cargo_shipments VALUES (7395556, 230534, 4, 24406, 58284, 877.1064564641554);
INSERT INTO cargo_shipments VALUES (9398678, 551210, 2, 37526, 42611, 149.85855050683884);
INSERT INTO cargo_shipments VALUES (3770997, 650161, 3, 46866, 93450, 260.2455324513131);
INSERT INTO cargo_shipments VALUES (6309268, 385272, 4, 18749, 97095, 543.2969496901061);
INSERT INTO cargo_shipments VALUES (2673001, 627684, 2, 5067, 33485, 641.5611044454391);
INSERT INTO cargo_shipments VALUES (3523338, 551210, 1, 48397, 41631, 73.11781899077718);
INSERT INTO cargo_shipments VALUES (3615204, 622718, 4, 12488, 32322, 734.4428051960601);
INSERT INTO cargo_shipments VALUES (5473489, 103502, 1, 10562, 13913, 456.77756444745154);
INSERT INTO cargo_shipments VALUES (6581118, 833947, 2, 44421, 81659, 714.0355242381936);
INSERT INTO cargo_shipments VALUES (5381687, 486096, 3, 41258, 15898, 256.1057194830307);
INSERT INTO cargo_shipments VALUES (9349317, 988582, 1, 2879, 90980, 378.04746168146465);
INSERT INTO cargo_shipments VALUES (7000054, 681816, 1, 30090, 53234, 522.8063888876441);
INSERT INTO cargo_shipments VALUES (9169035, 595282, 3, 41228, 57557, 757.7645173355791);
INSERT INTO cargo_shipments VALUES (1614589, 849862, 2, 15285, 28848, 159.13819450396548);
INSERT INTO cargo_shipments VALUES (8900270, 287765, 2, 27630, 86201, 760.1563615786542);
INSERT INTO cargo_shipments VALUES (3573549, 983670, 4, 23836, 66598, 187.19351816089457);
INSERT INTO cargo_shipments VALUES (3544285, 443855, 3, 7040, 91899, 559.0871038779097);
INSERT INTO cargo_shipments VALUES (9611819, 306162, 1, 36619, 53684, 723.9871084709766);
INSERT INTO cargo_shipments VALUES (5680061, 125473, 3, 45556, 33485, 958.9258544895571);
INSERT INTO cargo_shipments VALUES (1918100, 730182, 1, 31691, 18549, 733.0627509902621);
INSERT INTO cargo_shipments VALUES (6786963, 417366, 3, 26364, 40977, 19.092276344773286);
INSERT INTO cargo_shipments VALUES (3146500, 834586, 1, 8082, 50581, 25.22105718918388);
INSERT INTO cargo_shipments VALUES (2251391, 622340, 4, 13316, 15015, 269.73599891420565);
INSERT INTO cargo_shipments VALUES (4779822, 264352, 1, 48647, 42611, 889.5869356959639);
INSERT INTO cargo_shipments VALUES (6109209, 613587, 3, 43722, 85779, 571.845610666623);
INSERT INTO cargo_shipments VALUES (3265803, 630921, 4, 35981, 65134, 804.439016894441);
INSERT INTO cargo_shipments VALUES (7303099, 176018, 4, 10251, 50177, 756.1976995938501);
INSERT INTO cargo_shipments VALUES (1487748, 843101, 1, 36925, 31202, 45.37215191012456);
INSERT INTO cargo_shipments VALUES (4807218, 582110, 4, 28307, 41631, 941.9662815014361);
INSERT INTO cargo_shipments VALUES (7772943, 154401, 4, 48466, 40197, 34.91508085761441);
INSERT INTO cargo_shipments VALUES (3226573, 176018, 4, 36420, 27704, 799.3752285906768);
INSERT INTO cargo_shipments VALUES (5191365, 813446, 4, 40047, 41786, 595.1690807416236);
INSERT INTO cargo_shipments VALUES (6368543, 703088, 4, 1545, 41631, 643.8954649826278);
INSERT INTO cargo_shipments VALUES (6615132, 944547, 3, 13035, 57468, 841.2608789970832);
INSERT INTO cargo_shipments VALUES (3943523, 839964, 4, 49652, 10309, 713.5573241072169);
INSERT INTO cargo_shipments VALUES (4727728, 265945, 4, 38941, 38161, 122.45524177361533);
INSERT INTO cargo_shipments VALUES (3480579, 734277, 1, 29225, 84690, 618.2074927894214);
INSERT INTO cargo_shipments VALUES (1481512, 414337, 3, 1743, 91899, 442.58306580962824);
INSERT INTO cargo_shipments VALUES (9382985, 136113, 1, 3748, 52508, 682.1141779596779);
INSERT INTO cargo_shipments VALUES (5285944, 422672, 1, 9574, 38536, 287.3006680172151);
INSERT INTO cargo_shipments VALUES (2376575, 964737, 3, 26687, 40977, 17.21694534078555);
INSERT INTO cargo_shipments VALUES (8594222, 607760, 2, 46441, 13937, 850.3477769804335);
INSERT INTO cargo_shipments VALUES (7999705, 654139, 1, 45968, 35671, 899.078975099678);
INSERT INTO cargo_shipments VALUES (7973912, 583957, 3, 41583, 94669, 13.247174964966192);
INSERT INTO cargo_shipments VALUES (3561570, 931397, 4, 30256, 87026, 986.2394402451465);
INSERT INTO cargo_shipments VALUES (7287635, 500878, 3, 26310, 84090, 869.279784406023);
INSERT INTO cargo_shipments VALUES (8623734, 751755, 1, 8990, 34532, 803.390148044423);
INSERT INTO cargo_shipments VALUES (1735266, 143671, 3, 18305, 56241, 62.85704624347854);
INSERT INTO cargo_shipments VALUES (4199695, 283032, 4, 42492, 49548, 908.7341417100848);
INSERT INTO cargo_shipments VALUES (6051142, 785591, 1, 31295, 94072, 824.7066059749429);
INSERT INTO cargo_shipments VALUES (5007962, 933725, 4, 9266, 66509, 145.05571249364647);
INSERT INTO cargo_shipments VALUES (1505549, 404391, 2, 39827, 42260, 700.0754835184474);
INSERT INTO cargo_shipments VALUES (9458198, 671988, 4, 41236, 11051, 545.049647802351);
INSERT INTO cargo_shipments VALUES (2211117, 551210, 2, 3246, 27704, 315.28438596972563);
INSERT INTO cargo_shipments VALUES (3954165, 578296, 3, 10296, 41898, 391.11536370105705);
INSERT INTO cargo_shipments VALUES (3462298, 246854, 4, 4505, 90980, 213.69116799083832);
INSERT INTO cargo_shipments VALUES (9626721, 287765, 1, 39143, 40002, 778.1295876263043);
INSERT INTO cargo_shipments VALUES (4641689, 861877, 3, 26825, 34532, 191.02585992288047);
INSERT INTO cargo_shipments VALUES (5535672, 951732, 1, 31718, 50718, 985.6832714154197);
INSERT INTO cargo_shipments VALUES (5174036, 230534, 1, 11606, 53684, 802.7486855004703);
INSERT INTO cargo_shipments VALUES (8173904, 371982, 3, 43846, 48653, 388.99975265912036);
INSERT INTO cargo_shipments VALUES (4441009, 500878, 3, 31596, 60373, 740.9569621565101);
INSERT INTO cargo_shipments VALUES (7154291, 291137, 4, 32327, 66598, 617.9147610827763);
INSERT INTO cargo_shipments VALUES (2709287, 480401, 1, 19721, 17666, 815.3352268263463);
INSERT INTO cargo_shipments VALUES (8719205, 109915, 3, 44420, 59459, 560.5684616476983);
INSERT INTO cargo_shipments VALUES (8886264, 145117, 1, 21269, 56621, 174.05665319889187);
INSERT INTO cargo_shipments VALUES (4325076, 964443, 2, 13066, 15015, 438.744255098701);
INSERT INTO cargo_shipments VALUES (9102734, 583957, 4, 24499, 19686, 324.8370836776143);
INSERT INTO cargo_shipments VALUES (3685431, 998345, 4, 30679, 41631, 965.8787162219501);
INSERT INTO cargo_shipments VALUES (9413094, 651385, 4, 30594, 79895, 71.97349832154609);
INSERT INTO cargo_shipments VALUES (9502803, 708494, 1, 38659, 86258, 621.5704763562285);
INSERT INTO cargo_shipments VALUES (8085649, 994096, 1, 32538, 58284, 368.3904373073752);
INSERT INTO cargo_shipments VALUES (3903051, 925782, 2, 23576, 38314, 383.9200856744166);
INSERT INTO cargo_shipments VALUES (4526384, 158596, 3, 42269, 56173, 933.5635568032463);
INSERT INTO cargo_shipments VALUES (7240304, 365267, 1, 36402, 50177, 280.82158936849453);
INSERT INTO cargo_shipments VALUES (3943265, 804924, 4, 18979, 41410, 363.4855664951673);
INSERT INTO cargo_shipments VALUES (9898213, 622718, 2, 41156, 88864, 52.134266281768426);
INSERT INTO cargo_shipments VALUES (6664238, 260005, 1, 32924, 50782, 92.35050505983877);
INSERT INTO cargo_shipments VALUES (7540846, 174006, 1, 44224, 85779, 653.3220964268378);
INSERT INTO cargo_shipments VALUES (1150856, 264687, 3, 44502, 15015, 45.648975442038385);
INSERT INTO cargo_shipments VALUES (9923321, 910344, 4, 26472, 57235, 45.595060922248074);
INSERT INTO cargo_shipments VALUES (6367759, 372521, 1, 49492, 66598, 192.10081009723746);
INSERT INTO cargo_shipments VALUES (7713015, 804924, 3, 43316, 40197, 988.1023914258602);
INSERT INTO cargo_shipments VALUES (1455225, 242407, 3, 27144, 55085, 79.5377916882416);
INSERT INTO cargo_shipments VALUES (1750754, 120378, 1, 16388, 57235, 660.0517693851415);
INSERT INTO cargo_shipments VALUES (5287807, 513030, 3, 42985, 87026, 935.4198645900567);
INSERT INTO cargo_shipments VALUES (8410136, 767763, 2, 16105, 84733, 285.3504910871244);
INSERT INTO cargo_shipments VALUES (2402304, 176018, 3, 44939, 82368, 166.2861020704378);
INSERT INTO cargo_shipments VALUES (7192565, 214614, 2, 48219, 38161, 955.4859405951125);
INSERT INTO cargo_shipments VALUES (1188877, 214614, 3, 21026, 69140, 115.2726543615461);
INSERT INTO cargo_shipments VALUES (7419418, 434451, 4, 31258, 41410, 910.1043710592357);
INSERT INTO cargo_shipments VALUES (9160261, 286425, 3, 27236, 11007, 869.1498311991436);
INSERT INTO cargo_shipments VALUES (5555167, 136113, 3, 43942, 73560, 675.1133153960704);
INSERT INTO cargo_shipments VALUES (2498392, 630921, 3, 47085, 56962, 870.9827665147083);
INSERT INTO cargo_shipments VALUES (1203115, 657656, 3, 26706, 36126, 589.8575898758858);
INSERT INTO cargo_shipments VALUES (9223462, 388399, 1, 43203, 60373, 357.9946521655729);
INSERT INTO cargo_shipments VALUES (7242677, 104719, 3, 22930, 27704, 306.97684989993047);
INSERT INTO cargo_shipments VALUES (7129853, 132399, 2, 11820, 50177, 761.6157211260637);
INSERT INTO cargo_shipments VALUES (6002810, 671988, 1, 38250, 90980, 80.92888685984978);
INSERT INTO cargo_shipments VALUES (2334857, 241612, 4, 12348, 84690, 223.42984522757635);
INSERT INTO cargo_shipments VALUES (6321345, 801910, 2, 1324, 67231, 884.9519835663568);
INSERT INTO cargo_shipments VALUES (8748108, 910344, 3, 23335, 60373, 398.34754625684855);
INSERT INTO cargo_shipments VALUES (7529599, 945423, 4, 3391, 13500, 936.4340119060383);
INSERT INTO cargo_shipments VALUES (9782200, 816550, 1, 31301, 15015, 290.70621251718774);
INSERT INTO cargo_shipments VALUES (6747440, 371841, 1, 18080, 12118, 368.3780639497777);
INSERT INTO cargo_shipments VALUES (2591212, 610948, 1, 48126, 47423, 606.2964225045505);
INSERT INTO cargo_shipments VALUES (3766637, 230534, 3, 35442, 42611, 212.36544379917365);
INSERT INTO cargo_shipments VALUES (3520642, 686608, 2, 3880, 44873, 823.867394514679);
INSERT INTO cargo_shipments VALUES (4461905, 546827, 3, 44039, 86201, 242.378656348466);
INSERT INTO cargo_shipments VALUES (5520001, 388399, 1, 42596, 66598, 372.011436273031);
INSERT INTO cargo_shipments VALUES (1371596, 378721, 1, 2621, 52470, 584.4001728000585);
INSERT INTO cargo_shipments VALUES (7384878, 331143, 2, 15648, 65303, 676.1784467178767);
INSERT INTO cargo_shipments VALUES (5051440, 916015, 1, 18513, 53482, 906.1506239007683);
INSERT INTO cargo_shipments VALUES (6668476, 839964, 4, 4758, 35671, 594.9189375506368);
INSERT INTO cargo_shipments VALUES (9178258, 769533, 3, 11988, 11007, 580.392025031151);
INSERT INTO cargo_shipments VALUES (1589240, 694403, 1, 9495, 82660, 910.8112258289924);
INSERT INTO cargo_shipments VALUES (5414888, 705140, 1, 18279, 13913, 128.59904636596187);
INSERT INTO cargo_shipments VALUES (9463849, 952986, 4, 16048, 99271, 371.33890645261056);
INSERT INTO cargo_shipments VALUES (4316331, 797115, 3, 4574, 52508, 154.68698965981798);
INSERT INTO cargo_shipments VALUES (7283415, 758527, 4, 6004, 65140, 563.4365236453151);
INSERT INTO cargo_shipments VALUES (7984751, 468368, 2, 41970, 53482, 713.3286246987577);
INSERT INTO cargo_shipments VALUES (8484074, 346198, 3, 4870, 76084, 450.49061740477447);
INSERT INTO cargo_shipments VALUES (3845658, 844403, 2, 25140, 65134, 61.71286265218146);
INSERT INTO cargo_shipments VALUES (2526728, 123476, 4, 40509, 16955, 723.7690543404821);
INSERT INTO cargo_shipments VALUES (4876729, 726322, 2, 32385, 57235, 451.0986808911512);
INSERT INTO cargo_shipments VALUES (6085030, 207609, 1, 7313, 62432, 863.6756274573711);
INSERT INTO cargo_shipments VALUES (5507922, 207609, 2, 11581, 65303, 402.8796217025242);
INSERT INTO cargo_shipments VALUES (6664022, 818702, 3, 6246, 53684, 742.1500147467705);
INSERT INTO cargo_shipments VALUES (3173721, 559148, 2, 6599, 27704, 58.304142548289526);
INSERT INTO cargo_shipments VALUES (7422055, 877280, 1, 10196, 97073, 195.3212979116552);
INSERT INTO cargo_shipments VALUES (4383457, 246854, 1, 12866, 36453, 278.442017226916);
INSERT INTO cargo_shipments VALUES (8955708, 291137, 2, 42150, 54679, 133.84092028221838);
INSERT INTO cargo_shipments VALUES (9902501, 785591, 3, 29985, 56615, 799.8910814374268);
INSERT INTO cargo_shipments VALUES (8337927, 365267, 3, 1254, 86201, 987.5776962304614);
INSERT INTO cargo_shipments VALUES (3586124, 853530, 4, 14597, 73292, 130.01552140627726);
INSERT INTO cargo_shipments VALUES (2331011, 702659, 2, 40873, 84275, 28.249831317966432);
INSERT INTO cargo_shipments VALUES (5324857, 498912, 4, 21777, 94669, 961.4792105058486);
INSERT INTO cargo_shipments VALUES (7677558, 200279, 1, 20115, 84275, 95.96853316379838);
INSERT INTO cargo_shipments VALUES (8728061, 372521, 1, 35427, 38536, 86.95686972011319);
INSERT INTO cargo_shipments VALUES (2472722, 582110, 2, 7991, 50718, 2.172176117585467);
INSERT INTO cargo_shipments VALUES (3898873, 593750, 3, 8081, 93450, 310.9496410982987);
INSERT INTO cargo_shipments VALUES (8614285, 388399, 1, 43707, 10309, 123.6786904216648);
INSERT INTO cargo_shipments VALUES (9026806, 388891, 4, 11603, 67231, 341.4320355727134);
INSERT INTO cargo_shipments VALUES (8941347, 813446, 3, 13258, 17590, 856.404422219646);
INSERT INTO cargo_shipments VALUES (7955458, 655649, 3, 29563, 55085, 451.93032108942776);
INSERT INTO cargo_shipments VALUES (4773334, 125473, 2, 14446, 11007, 516.2080730721035);
INSERT INTO cargo_shipments VALUES (4792891, 951732, 3, 34886, 86258, 29.94811740452863);
INSERT INTO cargo_shipments VALUES (9520352, 139249, 3, 48015, 36126, 823.8341522560399);
INSERT INTO cargo_shipments VALUES (1581113, 378721, 4, 3717, 97073, 904.5953523837624);
INSERT INTO cargo_shipments VALUES (2828400, 346198, 2, 2056, 40977, 498.1172691582411);
INSERT INTO cargo_shipments VALUES (2976458, 558631, 1, 41154, 58567, 178.07892462841858);
INSERT INTO cargo_shipments VALUES (8042725, 794219, 2, 42028, 40977, 841.2554333832671);
INSERT INTO cargo_shipments VALUES (1883362, 585228, 3, 16210, 56621, 333.7878561919218);
INSERT INTO cargo_shipments VALUES (1958407, 143671, 3, 41734, 44873, 641.2749423782162);
INSERT INTO cargo_shipments VALUES (3358181, 109915, 3, 48887, 60646, 208.99724069781766);
INSERT INTO cargo_shipments VALUES (8414721, 303968, 4, 17961, 84275, 99.7238422006288);
INSERT INTO cargo_shipments VALUES (3125488, 993138, 3, 15998, 96305, 765.0466086846101);
INSERT INTO cargo_shipments VALUES (5585289, 139249, 4, 46341, 76084, 703.6917112484507);
INSERT INTO cargo_shipments VALUES (7771778, 260005, 2, 32573, 82660, 740.5316007137645);
INSERT INTO cargo_shipments VALUES (9327584, 593750, 1, 44501, 94669, 465.4237417823921);
INSERT INTO cargo_shipments VALUES (3963968, 563359, 2, 39153, 96305, 512.7632774941335);
INSERT INTO cargo_shipments VALUES (1866942, 954861, 1, 28964, 62432, 688.863410319309);
INSERT INTO cargo_shipments VALUES (6358686, 785591, 3, 38807, 58284, 734.1576192755477);
INSERT INTO cargo_shipments VALUES (4312457, 583957, 1, 8960, 84733, 199.4123350580689);
INSERT INTO cargo_shipments VALUES (6856342, 255144, 4, 32666, 41786, 367.382742537973);
INSERT INTO cargo_shipments VALUES (7879272, 571370, 3, 38943, 67231, 332.7936122093048);
INSERT INTO cargo_shipments VALUES (1627100, 758527, 4, 36675, 41926, 491.4179531553976);
INSERT INTO cargo_shipments VALUES (8933160, 835095, 2, 49272, 46381, 879.1691420445294);
INSERT INTO cargo_shipments VALUES (8370144, 513030, 2, 43688, 87702, 895.5759633525645);
INSERT INTO cargo_shipments VALUES (7919172, 735758, 2, 17998, 49548, 162.87310522417354);
INSERT INTO cargo_shipments VALUES (4914019, 560171, 3, 7821, 56962, 123.3692343559969);
INSERT INTO cargo_shipments VALUES (1953957, 194682, 2, 24245, 50177, 714.1995148367581);
INSERT INTO cargo_shipments VALUES (4192479, 583957, 4, 6892, 65303, 531.1572631817686);
INSERT INTO cargo_shipments VALUES (7497249, 583957, 4, 48664, 66598, 716.141629639901);
INSERT INTO cargo_shipments VALUES (4353425, 607760, 4, 12965, 53234, 971.2256903470145);
INSERT INTO cargo_shipments VALUES (3380553, 136113, 2, 20845, 99271, 865.9227565997362);
INSERT INTO cargo_shipments VALUES (2712431, 610948, 4, 38934, 93452, 116.7661975896701);
INSERT INTO cargo_shipments VALUES (9433360, 925782, 1, 11086, 60373, 740.6036474616997);
INSERT INTO cargo_shipments VALUES (6177681, 983670, 4, 20697, 84275, 733.7473987359773);
INSERT INTO cargo_shipments VALUES (4349649, 961289, 4, 9322, 62432, 240.547763398672);
INSERT INTO cargo_shipments VALUES (8711048, 745952, 3, 35191, 60373, 928.6074356682145);
INSERT INTO cargo_shipments VALUES (5645326, 365267, 4, 27225, 19686, 954.2106765347756);
INSERT INTO cargo_shipments VALUES (4306611, 336831, 1, 6162, 32577, 397.0124240374596);
INSERT INTO cargo_shipments VALUES (1258458, 886388, 4, 14755, 19686, 535.7162239569948);
INSERT INTO cargo_shipments VALUES (3698860, 332486, 2, 49074, 41410, 99.80728096227365);
INSERT INTO cargo_shipments VALUES (6358392, 495162, 4, 12207, 61652, 842.755344742749);
INSERT INTO cargo_shipments VALUES (1931844, 944394, 3, 21729, 87026, 149.146782927428);
INSERT INTO cargo_shipments VALUES (5636205, 630050, 4, 6069, 34595, 946.6835750626468);
INSERT INTO cargo_shipments VALUES (3970895, 334157, 1, 10038, 84090, 41.1285917169526);
INSERT INTO cargo_shipments VALUES (5424679, 651385, 4, 41889, 11051, 254.1695155320465);
INSERT INTO cargo_shipments VALUES (5774935, 919415, 3, 43900, 38161, 355.9279213692118);
INSERT INTO cargo_shipments VALUES (6878665, 581627, 4, 5854, 41926, 142.09079173681184);
INSERT INTO cargo_shipments VALUES (9340488, 495332, 3, 28704, 65140, 406.6856466185142);
INSERT INTO cargo_shipments VALUES (8557340, 623654, 3, 42095, 50820, 84.92743328787799);
INSERT INTO cargo_shipments VALUES (9917250, 334157, 4, 30318, 53684, 193.65162432939675);
INSERT INTO cargo_shipments VALUES (6365208, 417366, 4, 41123, 30478, 69.18130729148686);
INSERT INTO cargo_shipments VALUES (6530534, 104719, 4, 28911, 86201, 388.22674587905016);
INSERT INTO cargo_shipments VALUES (4820014, 306162, 2, 6428, 67231, 321.5203382894776);
INSERT INTO cargo_shipments VALUES (5508105, 726322, 4, 28040, 56173, 737.603190139853);
INSERT INTO cargo_shipments VALUES (9989379, 264352, 4, 12474, 57235, 388.1403922250205);
INSERT INTO cargo_shipments VALUES (5808957, 804924, 3, 4147, 55085, 624.1429246024568);
INSERT INTO cargo_shipments VALUES (4474238, 895631, 4, 14115, 86258, 998.7194675912486);
INSERT INTO cargo_shipments VALUES (8612960, 551210, 2, 14769, 56241, 888.882905074283);
INSERT INTO cargo_shipments VALUES (7307506, 944547, 2, 8340, 50581, 787.558979225815);
INSERT INTO cargo_shipments VALUES (7018834, 241612, 2, 45780, 30478, 173.63443697397963);
INSERT INTO cargo_shipments VALUES (6587559, 807747, 3, 37664, 17590, 737.539318721158);
INSERT INTO cargo_shipments VALUES (5348823, 817590, 2, 29672, 84090, 94.7520704992324);
INSERT INTO cargo_shipments VALUES (2913513, 306162, 3, 8898, 41631, 204.50084466507812);
INSERT INTO cargo_shipments VALUES (6844269, 378721, 1, 37261, 56621, 534.517897490877);
INSERT INTO cargo_shipments VALUES (7560067, 536454, 3, 20863, 53234, 496.3370136952979);
INSERT INTO cargo_shipments VALUES (4844851, 610948, 3, 46564, 58544, 448.57231084275384);
INSERT INTO cargo_shipments VALUES (2516790, 498912, 4, 9793, 86258, 964.2553667629617);
INSERT INTO cargo_shipments VALUES (1897010, 368069, 2, 42149, 42260, 570.0120981307546);
INSERT INTO cargo_shipments VALUES (1212752, 319696, 2, 1075, 32577, 831.547601437338);
INSERT INTO cargo_shipments VALUES (3793098, 794219, 1, 40732, 84090, 293.8801033988442);
INSERT INTO cargo_shipments VALUES (2113879, 896235, 1, 34300, 97095, 814.5927686244675);
INSERT INTO cargo_shipments VALUES (6302238, 108703, 1, 10719, 90765, 551.8950131112406);
INSERT INTO cargo_shipments VALUES (3284688, 933725, 2, 4726, 73560, 260.1941285861055);
INSERT INTO cargo_shipments VALUES (2509002, 994096, 2, 8707, 30478, 903.0245198721032);
INSERT INTO cargo_shipments VALUES (5508327, 910344, 1, 15541, 56615, 799.9796923367891);
INSERT INTO cargo_shipments VALUES (9065001, 571370, 2, 23717, 41786, 548.9212888598748);
INSERT INTO cargo_shipments VALUES (1941215, 215106, 3, 25119, 73560, 838.3519442775024);
INSERT INTO cargo_shipments VALUES (5601783, 417366, 2, 35774, 88864, 21.825198882220565);
INSERT INTO cargo_shipments VALUES (3459762, 104116, 1, 11981, 70715, 203.89707436114702);
INSERT INTO cargo_shipments VALUES (6974832, 208059, 4, 1945, 57557, 956.7952899518217);
INSERT INTO cargo_shipments VALUES (2734770, 747608, 1, 42878, 11060, 507.7643852390167);
INSERT INTO cargo_shipments VALUES (9122478, 173966, 3, 47503, 38536, 664.7502645275807);
INSERT INTO cargo_shipments VALUES (5528669, 610948, 1, 39017, 69007, 961.3562135223375);
INSERT INTO cargo_shipments VALUES (6391094, 200279, 2, 46116, 42260, 932.8265436156212);
INSERT INTO cargo_shipments VALUES (5153199, 744120, 4, 40684, 31202, 519.1939876738937);
INSERT INTO cargo_shipments VALUES (1841094, 622340, 4, 32676, 69007, 684.0296439925903);
INSERT INTO cargo_shipments VALUES (8829176, 414337, 3, 27805, 59636, 726.2297292306778);
INSERT INTO cargo_shipments VALUES (4809898, 816550, 4, 31863, 19686, 869.7623370243507);
INSERT INTO cargo_shipments VALUES (3329062, 668736, 4, 26910, 13852, 35.02082886238256);
INSERT INTO cargo_shipments VALUES (4658508, 910344, 4, 40084, 36453, 940.3546031932525);
INSERT INTO cargo_shipments VALUES (3818697, 668736, 4, 3681, 25322, 415.6049836010172);
INSERT INTO cargo_shipments VALUES (5686058, 581627, 2, 20952, 69330, 483.2515396004523);
INSERT INTO cargo_shipments VALUES (8552779, 342224, 2, 2631, 97073, 620.49301546539);
INSERT INTO cargo_shipments VALUES (6557245, 588014, 1, 13208, 69007, 43.168538997672925);
INSERT INTO cargo_shipments VALUES (7126273, 158596, 3, 13990, 94338, 566.1190225814836);
INSERT INTO cargo_shipments VALUES (6597616, 910344, 4, 15966, 21780, 534.4225051974593);
INSERT INTO cargo_shipments VALUES (3523627, 931397, 3, 36391, 86201, 292.70452636133183);
INSERT INTO cargo_shipments VALUES (2704327, 954861, 4, 28038, 32643, 333.45058296497);
INSERT INTO cargo_shipments VALUES (4992285, 898133, 1, 1549, 27704, 785.5390515857365);
INSERT INTO cargo_shipments VALUES (1866799, 482618, 3, 48135, 53684, 231.46246459813545);
INSERT INTO cargo_shipments VALUES (5967534, 123476, 3, 20175, 11051, 421.40467198892605);
INSERT INTO cargo_shipments VALUES (8830839, 229560, 2, 2639, 32577, 175.04248072916516);
INSERT INTO cargo_shipments VALUES (1459037, 671988, 3, 4149, 85779, 346.44009343083593);
INSERT INTO cargo_shipments VALUES (6389201, 295660, 1, 10681, 42611, 945.3760643177989);
INSERT INTO cargo_shipments VALUES (3529479, 593162, 4, 26128, 84733, 879.07999698108);
INSERT INTO cargo_shipments VALUES (9245648, 585228, 2, 20558, 32322, 396.7051015612378);
INSERT INTO cargo_shipments VALUES (1555856, 995741, 3, 44287, 86201, 544.4118211035067);
INSERT INTO cargo_shipments VALUES (9090271, 895631, 2, 3494, 18549, 318.4810913379661);
INSERT INTO cargo_shipments VALUES (7755803, 294483, 1, 8927, 21780, 307.01158894516203);
INSERT INTO cargo_shipments VALUES (3410807, 617268, 1, 34451, 56241, 508.3689283792263);
INSERT INTO cargo_shipments VALUES (8388514, 849862, 3, 25209, 69007, 675.0438473687501);
INSERT INTO cargo_shipments VALUES (1185243, 735758, 4, 49235, 45811, 469.80855715252113);
INSERT INTO cargo_shipments VALUES (2247724, 194682, 4, 26992, 84733, 991.3896653299443);
INSERT INTO cargo_shipments VALUES (8097998, 481053, 4, 13790, 53758, 772.8431170786636);
INSERT INTO cargo_shipments VALUES (8318728, 241612, 1, 46659, 58544, 923.6107119198708);
INSERT INTO cargo_shipments VALUES (2724701, 927176, 3, 27075, 25322, 210.93442108710437);
INSERT INTO cargo_shipments VALUES (4932175, 286425, 4, 9046, 46825, 179.51691389320834);
INSERT INTO cargo_shipments VALUES (6713335, 794219, 2, 29242, 10309, 55.057379077397606);
INSERT INTO cargo_shipments VALUES (2256261, 157492, 2, 20039, 50303, 103.62425213234117);
INSERT INTO cargo_shipments VALUES (3261871, 194682, 4, 35927, 69330, 684.7703429556444);
INSERT INTO cargo_shipments VALUES (7572226, 734764, 1, 3985, 41631, 837.9166619093075);
INSERT INTO cargo_shipments VALUES (7536959, 486096, 1, 4499, 84090, 329.28184398639246);
INSERT INTO cargo_shipments VALUES (7570355, 885046, 1, 19404, 91899, 476.56064052921187);
INSERT INTO cargo_shipments VALUES (7886799, 832152, 2, 7682, 97095, 564.6239902327682);
INSERT INTO cargo_shipments VALUES (1459159, 964443, 3, 31324, 11060, 692.1976794599641);
INSERT INTO cargo_shipments VALUES (7476932, 844403, 3, 12559, 30478, 658.8170777548145);
INSERT INTO cargo_shipments VALUES (6711935, 208059, 4, 20404, 91899, 304.1952148382935);
INSERT INTO cargo_shipments VALUES (4485988, 919415, 1, 36873, 58544, 829.1222269548425);
INSERT INTO cargo_shipments VALUES (5245178, 130564, 3, 41721, 17666, 538.6524162293624);
INSERT INTO cargo_shipments VALUES (4217755, 341564, 2, 39446, 49548, 933.6315679955264);
INSERT INTO cargo_shipments VALUES (2858075, 140527, 3, 20975, 69007, 275.97637350957126);
INSERT INTO cargo_shipments VALUES (5146942, 705140, 1, 37978, 42260, 972.6496651415425);
INSERT INTO cargo_shipments VALUES (8720586, 864161, 4, 13152, 46825, 404.1159080158351);
INSERT INTO cargo_shipments VALUES (8833358, 947599, 3, 21144, 59459, 536.4625558881719);
INSERT INTO cargo_shipments VALUES (1602056, 844403, 2, 34687, 58567, 272.28988865826653);
INSERT INTO cargo_shipments VALUES (1381636, 551210, 2, 30585, 41410, 534.8855049766142);
INSERT INTO cargo_shipments VALUES (7283943, 613587, 1, 6845, 50718, 998.3186804555041);
INSERT INTO cargo_shipments VALUES (6531021, 998345, 4, 41916, 18549, 921.1082904378567);
INSERT INTO cargo_shipments VALUES (5482062, 417366, 3, 45952, 50820, 838.2005485495216);
INSERT INTO cargo_shipments VALUES (3019949, 571370, 2, 13214, 40977, 175.5447583462135);
INSERT INTO cargo_shipments VALUES (1733023, 404391, 1, 38088, 65134, 828.3589546298252);
INSERT INTO cargo_shipments VALUES (6694553, 536772, 2, 27870, 81659, 773.9736233430468);
INSERT INTO cargo_shipments VALUES (9998626, 208059, 2, 15265, 31036, 242.5088247273819);
INSERT INTO cargo_shipments VALUES (1224335, 286425, 4, 41233, 86258, 600.9834734513114);
INSERT INTO cargo_shipments VALUES (8761743, 585228, 3, 19187, 58567, 806.3230653556589);
INSERT INTO cargo_shipments VALUES (8741386, 560171, 3, 3492, 40977, 875.4474011545168);
INSERT INTO cargo_shipments VALUES (9645201, 654139, 3, 24069, 36453, 429.27310719587376);
INSERT INTO cargo_shipments VALUES (5983931, 745546, 2, 32446, 38536, 478.24865597804643);
INSERT INTO cargo_shipments VALUES (1247772, 480401, 1, 14446, 56241, 737.968659123114);
INSERT INTO cargo_shipments VALUES (6026688, 679962, 3, 40035, 12385, 616.3243708819409);
INSERT INTO cargo_shipments VALUES (5502011, 993138, 2, 42145, 94072, 838.8756218390297);
INSERT INTO cargo_shipments VALUES (4379376, 587854, 3, 5452, 59636, 170.50272340077777);
INSERT INTO cargo_shipments VALUES (9138598, 306162, 4, 22802, 41631, 76.94745455380625);
INSERT INTO cargo_shipments VALUES (2908701, 536454, 2, 4273, 65303, 1.8026870579697096);
INSERT INTO cargo_shipments VALUES (8668000, 174006, 3, 8173, 57235, 852.7811471120519);
INSERT INTO cargo_shipments VALUES (3359844, 417017, 1, 43692, 46381, 883.6120198925558);
INSERT INTO cargo_shipments VALUES (9715595, 679962, 3, 13838, 87279, 50.12812151423773);
INSERT INTO cargo_shipments VALUES (5106208, 864161, 3, 37276, 41547, 908.1696652074145);
INSERT INTO cargo_shipments VALUES (2642711, 109915, 3, 38895, 82660, 198.13546815575188);
INSERT INTO cargo_shipments VALUES (3976643, 566273, 2, 23374, 13937, 331.499907817877);
INSERT INTO cargo_shipments VALUES (9234375, 671687, 2, 13700, 91899, 76.91545065631156);
INSERT INTO cargo_shipments VALUES (1831710, 246854, 2, 49752, 84733, 353.70624803896766);
INSERT INTO cargo_shipments VALUES (7674478, 388399, 1, 13598, 31202, 599.9018564331049);
INSERT INTO cargo_shipments VALUES (5305567, 751755, 1, 11840, 79895, 344.4540248984438);
INSERT INTO cargo_shipments VALUES (6971894, 734277, 1, 30839, 73560, 804.0115489198316);
INSERT INTO cargo_shipments VALUES (6067651, 341564, 2, 3886, 56615, 908.4044205069105);
INSERT INTO cargo_shipments VALUES (7600079, 430854, 1, 13839, 88516, 595.6461791407179);
INSERT INTO cargo_shipments VALUES (9194338, 477350, 1, 23585, 40197, 568.3236164048058);
INSERT INTO cargo_shipments VALUES (6576295, 758527, 3, 44515, 92389, 937.9704727835633);
INSERT INTO cargo_shipments VALUES (8138843, 630050, 4, 20993, 91899, 688.065280931588);
INSERT INTO cargo_shipments VALUES (8330498, 246854, 2, 24560, 61652, 568.0743915977392);
INSERT INTO cargo_shipments VALUES (9581226, 217450, 4, 39579, 94669, 896.2328368500049);
INSERT INTO cargo_shipments VALUES (5092261, 331143, 4, 23847, 97095, 170.1635189814642);
INSERT INTO cargo_shipments VALUES (7692503, 311617, 3, 48104, 73292, 318.86317553035224);
INSERT INTO cargo_shipments VALUES (3637881, 306162, 2, 15635, 85779, 746.8194123568974);
INSERT INTO cargo_shipments VALUES (1367035, 513030, 3, 6582, 15015, 356.1611873414788);
INSERT INTO cargo_shipments VALUES (2235630, 484116, 3, 37764, 58284, 874.2193064103521);
INSERT INTO cargo_shipments VALUES (1975676, 196524, 2, 14633, 56962, 316.054731575201);
INSERT INTO cargo_shipments VALUES (5477583, 385272, 1, 26062, 36126, 884.9039057026421);
INSERT INTO cargo_shipments VALUES (3566586, 952986, 1, 31470, 62432, 383.99639442279346);
INSERT INTO cargo_shipments VALUES (3796560, 686783, 4, 15784, 41547, 462.8490192536576);
INSERT INTO cargo_shipments VALUES (4483519, 217450, 1, 45973, 82368, 393.53627490734135);
INSERT INTO cargo_shipments VALUES (3122135, 613587, 1, 42999, 56173, 949.6654593275963);
INSERT INTO cargo_shipments VALUES (7841450, 372521, 1, 10680, 56173, 309.9260215618458);
INSERT INTO cargo_shipments VALUES (5315628, 794219, 3, 15126, 64330, 928.1041490177665);
INSERT INTO cargo_shipments VALUES (3729156, 105592, 1, 43297, 90765, 275.86276382046657);
INSERT INTO cargo_shipments VALUES (7069733, 885046, 3, 26483, 27704, 490.35528996731495);
INSERT INTO cargo_shipments VALUES (3050010, 562349, 2, 27246, 50782, 931.0140310540058);
INSERT INTO cargo_shipments VALUES (6455748, 944394, 2, 9036, 57235, 624.8559355433736);
INSERT INTO cargo_shipments VALUES (8045721, 886388, 2, 38675, 70715, 226.68968584690597);
INSERT INTO cargo_shipments VALUES (2264252, 833947, 3, 24731, 79895, 501.1319239889961);
INSERT INTO cargo_shipments VALUES (9829400, 123476, 4, 6314, 41410, 550.198495435274);
INSERT INTO cargo_shipments VALUES (8706775, 306162, 2, 10825, 33485, 649.9177952945696);
INSERT INTO cargo_shipments VALUES (9174573, 607760, 2, 17407, 96305, 281.989545464178);
INSERT INTO cargo_shipments VALUES (3060489, 484116, 1, 8882, 87702, 374.3070700126778);
INSERT INTO cargo_shipments VALUES (7057493, 954861, 3, 14394, 97095, 311.6062576327302);
INSERT INTO cargo_shipments VALUES (4395157, 229560, 4, 16726, 56962, 172.91302461677927);
INSERT INTO cargo_shipments VALUES (9391924, 668736, 4, 36544, 69007, 838.6420868871419);
INSERT INTO cargo_shipments VALUES (3343698, 694403, 3, 7857, 90980, 630.1513772439768);
INSERT INTO cargo_shipments VALUES (8295473, 622034, 2, 32736, 13913, 166.3133499058711);
INSERT INTO cargo_shipments VALUES (7872626, 838513, 4, 33323, 92389, 155.97051479670998);
INSERT INTO cargo_shipments VALUES (4161102, 843961, 3, 32603, 41898, 174.97257729654046);
INSERT INTO cargo_shipments VALUES (7880746, 679962, 3, 47255, 69330, 27.08851312915572);
INSERT INTO cargo_shipments VALUES (4383907, 722031, 2, 5026, 33265, 680.6267580582314);
INSERT INTO cargo_shipments VALUES (5665033, 342326, 2, 48797, 41547, 801.7609311638021);
INSERT INTO cargo_shipments VALUES (6624674, 952986, 3, 8315, 30478, 829.1674249517469);
INSERT INTO cargo_shipments VALUES (7918114, 593162, 1, 42061, 50303, 373.851401094445);
INSERT INTO cargo_shipments VALUES (1942567, 955811, 4, 15758, 30478, 673.3831218173609);
INSERT INTO cargo_shipments VALUES (1369107, 671988, 3, 44847, 82660, 39.391247021245526);
INSERT INTO cargo_shipments VALUES (4846821, 295088, 1, 27916, 34532, 81.86911690661259);
INSERT INTO cargo_shipments VALUES (4850121, 588014, 1, 40160, 48653, 108.28951276628618);
INSERT INTO cargo_shipments VALUES (6205811, 614562, 2, 23518, 35671, 771.2235418100097);
INSERT INTO cargo_shipments VALUES (4152214, 230534, 4, 5287, 15015, 298.2152121509046);
INSERT INTO cargo_shipments VALUES (5496975, 215106, 3, 6847, 42690, 800.9498191268003);
INSERT INTO cargo_shipments VALUES (5463062, 734764, 4, 5507, 11051, 221.83413005788123);
INSERT INTO cargo_shipments VALUES (9994495, 933725, 2, 16162, 41547, 419.12698158259155);
INSERT INTO cargo_shipments VALUES (7229006, 132399, 2, 12249, 73560, 281.7068668910968);
INSERT INTO cargo_shipments VALUES (4173592, 562349, 4, 47488, 41410, 626.3484010960652);
INSERT INTO cargo_shipments VALUES (7513046, 551210, 1, 45874, 42260, 501.80232306320215);
INSERT INTO cargo_shipments VALUES (1586781, 767763, 3, 42511, 66598, 118.01479167204243);
INSERT INTO cargo_shipments VALUES (5532336, 468368, 4, 17393, 53684, 262.6613946671028);
INSERT INTO cargo_shipments VALUES (2931523, 404391, 3, 43175, 11051, 399.30346864387656);
INSERT INTO cargo_shipments VALUES (8958104, 382201, 3, 26970, 13937, 374.52338255069594);
INSERT INTO cargo_shipments VALUES (9544341, 355369, 2, 30950, 41898, 804.6897893546846);
INSERT INTO cargo_shipments VALUES (5939287, 388484, 1, 31660, 31202, 580.3406580322354);
INSERT INTO cargo_shipments VALUES (6463841, 617268, 2, 16826, 58284, 813.0489924005319);
INSERT INTO cargo_shipments VALUES (4785998, 495332, 1, 33286, 15898, 231.6802126702745);
INSERT INTO cargo_shipments VALUES (1645392, 610948, 2, 19304, 88864, 542.0774737322198);
INSERT INTO cargo_shipments VALUES (9479030, 218188, 2, 46683, 59459, 897.0726474394102);
INSERT INTO cargo_shipments VALUES (4693969, 218458, 4, 8226, 58544, 680.7826888351716);
INSERT INTO cargo_shipments VALUES (9076626, 336831, 1, 5619, 31036, 478.13319055187367);
INSERT INTO cargo_shipments VALUES (8175030, 101743, 1, 45292, 41477, 40.30489942173021);
INSERT INTO cargo_shipments VALUES (4286656, 468368, 4, 38823, 58284, 390.64078477651896);
INSERT INTO cargo_shipments VALUES (9905427, 139249, 2, 38352, 15015, 895.3693499506512);
INSERT INTO cargo_shipments VALUES (8148801, 402347, 1, 4518, 44873, 520.9154264668013);
INSERT INTO cargo_shipments VALUES (6105175, 745952, 3, 34585, 94669, 884.9578753093672);
INSERT INTO cargo_shipments VALUES (6904978, 807747, 3, 31462, 48653, 178.72059621130143);
INSERT INTO cargo_shipments VALUES (7569867, 411362, 3, 48004, 91113, 346.3471590446472);
INSERT INTO cargo_shipments VALUES (2060579, 622034, 4, 26121, 81659, 698.4141232817391);
INSERT INTO cargo_shipments VALUES (5855386, 651385, 1, 46604, 87279, 460.41745293961867);
INSERT INTO cargo_shipments VALUES (2799394, 990584, 3, 11645, 91899, 553.9669131757661);
INSERT INTO cargo_shipments VALUES (7956999, 521146, 1, 10568, 42611, 593.4854641698821);
INSERT INTO cargo_shipments VALUES (5602228, 599462, 1, 24624, 84090, 849.9372598975352);
INSERT INTO cargo_shipments VALUES (5667386, 778047, 3, 11473, 12323, 766.3911068840989);
INSERT INTO cargo_shipments VALUES (4232720, 468368, 2, 35329, 62432, 535.2581761607194);
INSERT INTO cargo_shipments VALUES (1688595, 885046, 4, 32853, 15898, 896.0490702456483);
INSERT INTO cargo_shipments VALUES (5033508, 139249, 3, 33084, 59636, 353.76873737293766);
INSERT INTO cargo_shipments VALUES (3949543, 414337, 1, 31848, 82368, 877.7568719357403);
INSERT INTO cargo_shipments VALUES (7705918, 130564, 2, 32677, 57557, 83.94522816166116);
INSERT INTO cargo_shipments VALUES (9455111, 459618, 1, 22241, 42611, 245.04037612397522);
INSERT INTO cargo_shipments VALUES (2002824, 651385, 4, 34448, 41477, 253.71640016247753);
INSERT INTO cargo_shipments VALUES (7187259, 388399, 3, 12794, 97095, 579.4282966079359);
INSERT INTO cargo_shipments VALUES (3796636, 955018, 3, 49187, 13852, 948.064494305348);
INSERT INTO cargo_shipments VALUES (2135721, 656769, 2, 32402, 32322, 638.3413855652879);
INSERT INTO cargo_shipments VALUES (1189577, 801910, 3, 30850, 82660, 929.1629741240262);
INSERT INTO cargo_shipments VALUES (7041687, 990584, 4, 33367, 13937, 962.5184533187039);
INSERT INTO cargo_shipments VALUES (1483567, 587882, 1, 12145, 15015, 337.0047674956984);
INSERT INTO cargo_shipments VALUES (6013989, 378721, 3, 43750, 17666, 663.1925742672285);
INSERT INTO cargo_shipments VALUES (5347764, 174006, 4, 3197, 93450, 355.11105818885045);
INSERT INTO cargo_shipments VALUES (8818724, 264352, 2, 21382, 88516, 485.97908507374143);
INSERT INTO cargo_shipments VALUES (3102498, 735758, 3, 20705, 16955, 43.86843232036253);
INSERT INTO cargo_shipments VALUES (2176484, 730182, 2, 35868, 41631, 423.8445812821188);
INSERT INTO cargo_shipments VALUES (7487721, 599462, 4, 9567, 50782, 248.40786839008211);
INSERT INTO cargo_shipments VALUES (5227365, 371841, 1, 9968, 84275, 340.93830717911857);
INSERT INTO cargo_shipments VALUES (1769137, 558631, 3, 16340, 41786, 243.8015418175823);
INSERT INTO cargo_shipments VALUES (3515964, 686783, 4, 28791, 84690, 260.3909491032078);
INSERT INTO cargo_shipments VALUES (4526806, 346198, 4, 2378, 65303, 847.2501337096874);
INSERT INTO cargo_shipments VALUES (6410642, 582110, 4, 28604, 45811, 620.6979141755198);
INSERT INTO cargo_shipments VALUES (8844288, 200279, 1, 2512, 15015, 774.4847202680393);
INSERT INTO cargo_shipments VALUES (3071004, 230534, 2, 49159, 97073, 555.7693787750726);
INSERT INTO cargo_shipments VALUES (1020702, 368069, 3, 43777, 96305, 750.7425865688898);
INSERT INTO cargo_shipments VALUES (1359148, 218458, 2, 7420, 85779, 35.28659420047398);
INSERT INTO cargo_shipments VALUES (6256172, 702515, 1, 23482, 94669, 251.4196779245136);
INSERT INTO cargo_shipments VALUES (4201530, 289019, 4, 41254, 35671, 930.9579924515323);
INSERT INTO cargo_shipments VALUES (8734224, 896235, 4, 22112, 90765, 847.5539738331828);
INSERT INTO cargo_shipments VALUES (2191297, 319696, 2, 37009, 40977, 978.4149992989826);
INSERT INTO cargo_shipments VALUES (2997069, 876023, 1, 24548, 94072, 300.5742342482173);
INSERT INTO cargo_shipments VALUES (3795218, 898133, 4, 23406, 34595, 317.09102863234995);
INSERT INTO cargo_shipments VALUES (1831549, 477350, 4, 33577, 19686, 146.73347109091617);
INSERT INTO cargo_shipments VALUES (7043855, 797115, 2, 30937, 12323, 880.2850655914376);
INSERT INTO cargo_shipments VALUES (7312466, 368069, 1, 37512, 55085, 666.3837685527718);
INSERT INTO cargo_shipments VALUES (1614936, 230534, 1, 12896, 73292, 41.574312619636736);
INSERT INTO cargo_shipments VALUES (8479296, 103502, 1, 42640, 70715, 94.67091112793413);
INSERT INTO cargo_shipments VALUES (8113833, 132399, 1, 27226, 16955, 810.3665760850469);
INSERT INTO cargo_shipments VALUES (6442352, 414337, 1, 2537, 30478, 513.7743984736219);
INSERT INTO cargo_shipments VALUES (5233375, 955811, 2, 47246, 96305, 669.408841836111);
INSERT INTO cargo_shipments VALUES (4570272, 945423, 4, 12208, 58567, 395.94198934434553);
INSERT INTO cargo_shipments VALUES (7607573, 378721, 1, 20033, 11060, 315.56811902934237);
INSERT INTO cargo_shipments VALUES (1507341, 480401, 3, 41283, 56962, 983.3462096875311);
INSERT INTO cargo_shipments VALUES (4592028, 104719, 2, 6120, 86201, 919.8682174636878);
INSERT INTO cargo_shipments VALUES (6772609, 793892, 3, 9051, 53234, 691.1448151743058);
INSERT INTO cargo_shipments VALUES (2055699, 961289, 2, 6273, 50820, 837.2106380590039);
INSERT INTO cargo_shipments VALUES (2653882, 193915, 4, 13157, 16955, 162.16677698518134);
INSERT INTO cargo_shipments VALUES (7901220, 371841, 2, 34777, 13937, 524.1822301348444);
INSERT INTO cargo_shipments VALUES (1826378, 246438, 2, 25301, 38161, 783.0282821877222);
INSERT INTO cargo_shipments VALUES (7577124, 903883, 3, 28301, 94072, 706.0140798021955);
INSERT INTO cargo_shipments VALUES (2438488, 140527, 1, 32586, 34532, 152.72295554705394);
INSERT INTO cargo_shipments VALUES (1937207, 103502, 1, 49001, 86201, 994.0662833827283);
INSERT INTO cargo_shipments VALUES (5837589, 558631, 3, 46559, 70715, 835.8392888965794);
INSERT INTO cargo_shipments VALUES (9398029, 742571, 4, 10925, 10309, 601.3552788090426);
INSERT INTO cargo_shipments VALUES (8632686, 614562, 3, 12073, 19686, 678.780827034547);
INSERT INTO cargo_shipments VALUES (5492353, 241612, 3, 30706, 41631, 778.3613059985834);
INSERT INTO cargo_shipments VALUES (1863943, 745952, 1, 38231, 52508, 525.6560416570353);
INSERT INTO cargo_shipments VALUES (2672844, 336949, 2, 43455, 93450, 852.9252762369068);
INSERT INTO cargo_shipments VALUES (4061677, 464221, 2, 5803, 57235, 770.2976694430289);
INSERT INTO cargo_shipments VALUES (8296438, 624781, 2, 23086, 32322, 162.5156501617029);
INSERT INTO cargo_shipments VALUES (8359588, 955018, 3, 27702, 13500, 340.1812929600726);
INSERT INTO cargo_shipments VALUES (5406080, 125473, 1, 1617, 10309, 485.85918214899203);
INSERT INTO cargo_shipments VALUES (7780963, 355369, 3, 25980, 42690, 832.4980758252639);
INSERT INTO cargo_shipments VALUES (4384992, 849862, 2, 26904, 84690, 834.6935134494624);
INSERT INTO cargo_shipments VALUES (6993663, 431929, 3, 44571, 56173, 308.36613577171613);
INSERT INTO cargo_shipments VALUES (5216356, 125473, 4, 13494, 19686, 101.84073842215658);
INSERT INTO cargo_shipments VALUES (8275955, 610948, 3, 10446, 41926, 649.9866452398503);
INSERT INTO cargo_shipments VALUES (3664168, 887047, 2, 15722, 64330, 784.17340204079);
INSERT INTO cargo_shipments VALUES (4926011, 208059, 1, 29235, 25322, 283.7350319297609);
INSERT INTO cargo_shipments VALUES (7744919, 916015, 4, 30182, 34595, 152.0396091391223);
INSERT INTO cargo_shipments VALUES (1266720, 558631, 3, 22214, 33265, 941.4008029101709);
INSERT INTO cargo_shipments VALUES (8806864, 374146, 1, 25911, 90980, 953.7321604917068);
INSERT INTO cargo_shipments VALUES (6336185, 264687, 1, 37794, 87702, 246.06057947091463);
INSERT INTO cargo_shipments VALUES (4897709, 101743, 3, 41421, 38161, 519.6433944535696);
INSERT INTO cargo_shipments VALUES (3742087, 132399, 1, 16892, 13500, 543.9479728015091);
INSERT INTO cargo_shipments VALUES (8473306, 554618, 1, 9592, 13937, 374.0676712377977);
INSERT INTO cargo_shipments VALUES (3689111, 136113, 2, 18547, 42611, 265.9119607771576);
INSERT INTO cargo_shipments VALUES (2184086, 702515, 1, 24718, 32322, 819.8616665274211);
INSERT INTO cargo_shipments VALUES (7815297, 173966, 1, 33936, 17666, 267.4149800683575);
INSERT INTO cargo_shipments VALUES (7484049, 816550, 2, 36843, 34595, 496.82743477139167);
INSERT INTO cargo_shipments VALUES (3264413, 895631, 3, 29600, 54679, 362.9862456333879);
INSERT INTO cargo_shipments VALUES (5896545, 374146, 4, 40170, 72190, 929.774437835004);
INSERT INTO cargo_shipments VALUES (6695206, 200279, 1, 6128, 12118, 81.03624420510425);
INSERT INTO cargo_shipments VALUES (8281256, 742571, 2, 12868, 87026, 620.8110947196274);
INSERT INTO cargo_shipments VALUES (9166197, 294483, 3, 49287, 30478, 731.0377613429482);
INSERT INTO cargo_shipments VALUES (1382785, 747608, 4, 6297, 46381, 40.072203862801636);
INSERT INTO cargo_shipments VALUES (7027856, 563359, 3, 14023, 41631, 177.03820382489354);
INSERT INTO cargo_shipments VALUES (3461677, 295660, 4, 31236, 82368, 223.02623331208991);
INSERT INTO cargo_shipments VALUES (6535015, 844403, 2, 29679, 48653, 966.6917894108659);
INSERT INTO cargo_shipments VALUES (8081448, 101743, 3, 18022, 40197, 944.5452361636422);
INSERT INTO cargo_shipments VALUES (6389207, 174006, 2, 19659, 13937, 662.6711011176166);
INSERT INTO cargo_shipments VALUES (7978605, 708494, 3, 31004, 59636, 691.1249545748326);
INSERT INTO cargo_shipments VALUES (6161651, 174006, 1, 47838, 19686, 927.248347541828);
INSERT INTO cargo_shipments VALUES (3628298, 839964, 2, 46010, 15015, 139.83773157059775);
INSERT INTO cargo_shipments VALUES (8151106, 158596, 2, 21555, 38161, 5.344103863379912);
INSERT INTO cargo_shipments VALUES (8589282, 722031, 2, 39434, 94338, 984.0960052367104);
INSERT INTO cargo_shipments VALUES (3199130, 944547, 2, 7457, 65140, 970.1420131951439);
INSERT INTO cargo_shipments VALUES (8592586, 931397, 1, 25208, 48653, 736.9373598786462);
INSERT INTO cargo_shipments VALUES (4004232, 614215, 1, 31214, 31827, 260.1100619896295);
INSERT INTO cargo_shipments VALUES (2475684, 241612, 4, 24832, 28848, 698.5007748353568);
INSERT INTO cargo_shipments VALUES (7734596, 241612, 3, 4685, 50303, 833.5104325045519);
INSERT INTO cargo_shipments VALUES (8959886, 342326, 3, 6239, 59681, 244.46986307642626);
INSERT INTO cargo_shipments VALUES (7595830, 817590, 1, 1251, 13913, 429.52828622550555);
INSERT INTO cargo_shipments VALUES (9072275, 832152, 4, 4831, 76581, 763.9809111566905);
INSERT INTO cargo_shipments VALUES (7522194, 355369, 3, 10279, 25322, 944.9741565916207);
INSERT INTO cargo_shipments VALUES (4005279, 916015, 1, 35221, 96305, 49.50836769684963);
INSERT INTO cargo_shipments VALUES (1513997, 910344, 1, 20603, 13500, 254.4476439577339);
INSERT INTO cargo_shipments VALUES (9144533, 843101, 4, 47107, 47423, 112.2495694114476);
INSERT INTO cargo_shipments VALUES (6207568, 622718, 1, 17945, 56241, 765.1985154887897);
INSERT INTO cargo_shipments VALUES (4750893, 194682, 1, 46690, 41477, 919.6483851544352);
INSERT INTO cargo_shipments VALUES (5747683, 694403, 2, 12170, 90980, 245.08206972337265);
INSERT INTO cargo_shipments VALUES (5392132, 244904, 1, 44276, 52508, 484.6768102260498);
INSERT INTO cargo_shipments VALUES (9977674, 614562, 4, 3196, 84690, 88.96499582830464);
INSERT INTO cargo_shipments VALUES (7515600, 734277, 1, 33427, 42611, 480.6548444400408);
INSERT INTO cargo_shipments VALUES (4171033, 916015, 1, 49770, 86201, 737.758087672923);
INSERT INTO cargo_shipments VALUES (2339243, 341564, 2, 43807, 97073, 490.52290533546284);
INSERT INTO cargo_shipments VALUES (2642452, 650161, 3, 22112, 76084, 520.882261602661);
INSERT INTO cargo_shipments VALUES (9366674, 125473, 2, 31602, 45811, 849.3208339600988);
INSERT INTO cargo_shipments VALUES (3203364, 651385, 1, 17108, 45811, 648.1886655555581);
INSERT INTO cargo_shipments VALUES (3437077, 955018, 3, 43057, 32577, 143.52466445349842);
INSERT INTO cargo_shipments VALUES (8480601, 588014, 2, 30679, 94669, 586.0371019857326);
INSERT INTO cargo_shipments VALUES (8252710, 332486, 2, 5267, 36126, 608.2615601213397);
INSERT INTO cargo_shipments VALUES (5065064, 286425, 3, 16893, 40977, 630.942666477826);
INSERT INTO cargo_shipments VALUES (6683026, 898133, 4, 9362, 94338, 265.30862473072415);
INSERT INTO cargo_shipments VALUES (1848235, 287765, 1, 32799, 42260, 644.3788454046284);
INSERT INTO cargo_shipments VALUES (7838053, 104719, 3, 23906, 82368, 640.9768508104297);
INSERT INTO cargo_shipments VALUES (7247069, 101743, 2, 42825, 92389, 135.4907057910012);
INSERT INTO cargo_shipments VALUES (1451947, 745952, 3, 10366, 34595, 932.7205015098616);
INSERT INTO cargo_shipments VALUES (2339753, 801910, 1, 10359, 49548, 809.2693653771347);
INSERT INTO cargo_shipments VALUES (5063747, 607760, 1, 21406, 40197, 181.40576491440598);
INSERT INTO cargo_shipments VALUES (6456966, 861877, 2, 34547, 58544, 909.2110431499765);
INSERT INTO cargo_shipments VALUES (8834135, 563359, 3, 47444, 25322, 391.8517899223425);
INSERT INTO cargo_shipments VALUES (8257007, 459618, 1, 38666, 57468, 261.1633730477251);
INSERT INTO cargo_shipments VALUES (5938905, 104719, 2, 3641, 60373, 495.43952928301127);
INSERT INTO cargo_shipments VALUES (9461635, 778047, 1, 30883, 34532, 996.3535015544471);
INSERT INTO cargo_shipments VALUES (1952465, 947599, 1, 32333, 58284, 508.9171986765425);
INSERT INTO cargo_shipments VALUES (7741032, 481053, 3, 47927, 73560, 799.3781873332073);
INSERT INTO cargo_shipments VALUES (1224553, 742571, 2, 4738, 42021, 118.41798097099043);
INSERT INTO cargo_shipments VALUES (6709886, 217450, 1, 30913, 88864, 84.92818006655722);
INSERT INTO cargo_shipments VALUES (1525126, 174006, 4, 41402, 36453, 651.5666037664488);
INSERT INTO cargo_shipments VALUES (9509649, 388891, 2, 15651, 34532, 185.14963663865237);
INSERT INTO cargo_shipments VALUES (7122559, 751755, 4, 7868, 65140, 981.3909424592524);
INSERT INTO cargo_shipments VALUES (1875519, 595282, 3, 38378, 41926, 272.0780846004737);
INSERT INTO cargo_shipments VALUES (4434579, 995615, 2, 48315, 11060, 500.82983469945174);
INSERT INTO cargo_shipments VALUES (7043014, 120378, 1, 9100, 41898, 564.4469838821994);
INSERT INTO cargo_shipments VALUES (5587283, 722031, 1, 10388, 32577, 919.8193440198606);
INSERT INTO cargo_shipments VALUES (5709015, 988582, 2, 28276, 58567, 111.44134211271361);
INSERT INTO cargo_shipments VALUES (9122087, 521146, 1, 25180, 69007, 262.93784428065845);
INSERT INTO cargo_shipments VALUES (7310245, 623654, 1, 10765, 13852, 261.1136514445931);
INSERT INTO cargo_shipments VALUES (7466122, 778047, 1, 8072, 18549, 424.68724937767854);
INSERT INTO cargo_shipments VALUES (1810501, 468368, 4, 5123, 82368, 613.0625566581485);
INSERT INTO cargo_shipments VALUES (1401080, 864161, 1, 27905, 65140, 402.37898574342455);
INSERT INTO cargo_shipments VALUES (4813717, 157492, 1, 47328, 40197, 329.43512905889673);
INSERT INTO cargo_shipments VALUES (5892651, 513030, 2, 17502, 40002, 798.6202137620486);
INSERT INTO cargo_shipments VALUES (8505380, 931397, 1, 33279, 93450, 257.8861276359533);
INSERT INTO cargo_shipments VALUES (4095164, 222414, 2, 20563, 42690, 351.46479191120386);
INSERT INTO cargo_shipments VALUES (3678440, 630050, 3, 25747, 67231, 801.6078500458017);
INSERT INTO cargo_shipments VALUES (7585883, 931397, 2, 13467, 66598, 646.1776246677708);
INSERT INTO cargo_shipments VALUES (7202151, 925782, 1, 22182, 46381, 817.0071117981877);
INSERT INTO cargo_shipments VALUES (8102889, 365226, 1, 11334, 76084, 144.00029888027055);
INSERT INTO cargo_shipments VALUES (7489811, 681816, 3, 48748, 64330, 94.24087479034394);
INSERT INTO cargo_shipments VALUES (7628921, 835095, 3, 2895, 99271, 74.00712254413288);
INSERT INTO cargo_shipments VALUES (5608132, 610948, 1, 25770, 94072, 268.29639078094823);
INSERT INTO cargo_shipments VALUES (7993376, 694403, 2, 47881, 41926, 873.8411217136681);
INSERT INTO cargo_shipments VALUES (7670547, 587854, 4, 2028, 40002, 255.23252445194998);
INSERT INTO cargo_shipments VALUES (1744750, 562349, 3, 5220, 42021, 419.3928659607633);
INSERT INTO cargo_shipments VALUES (4143831, 341564, 4, 32588, 54679, 606.2032276598459);
INSERT INTO cargo_shipments VALUES (2844691, 844403, 2, 23496, 53684, 323.2271871565897);
INSERT INTO cargo_shipments VALUES (5790844, 734277, 1, 40866, 42260, 688.0949592478759);
INSERT INTO cargo_shipments VALUES (7329145, 961289, 2, 46127, 41898, 96.2455595350471);
INSERT INTO cargo_shipments VALUES (5705925, 265945, 1, 21895, 94669, 729.557008371443);
INSERT INTO cargo_shipments VALUES (4884777, 452392, 3, 23473, 40390, 661.7200309559363);
INSERT INTO cargo_shipments VALUES (9579541, 582110, 1, 15081, 73292, 407.6120561044701);
INSERT INTO cargo_shipments VALUES (1443302, 404391, 2, 20536, 42260, 66.31075600808522);
INSERT INTO cargo_shipments VALUES (9248847, 686783, 2, 49710, 76581, 535.1746025623256);
INSERT INTO cargo_shipments VALUES (6153375, 610948, 1, 15736, 33265, 693.9139698444164);
INSERT INTO cargo_shipments VALUES (2929738, 617268, 3, 35982, 50303, 88.25272853387011);
INSERT INTO cargo_shipments VALUES (9442653, 388891, 2, 32904, 87026, 114.12517839841163);
INSERT INTO cargo_shipments VALUES (5029278, 624781, 2, 39375, 40390, 545.9250740157129);
INSERT INTO cargo_shipments VALUES (4238165, 955811, 4, 42280, 41786, 482.59797864170974);
INSERT INTO cargo_shipments VALUES (2423806, 385272, 3, 26697, 57235, 12.273954832264122);
INSERT INTO cargo_shipments VALUES (2740093, 217450, 2, 35893, 57235, 921.2165256631108);
INSERT INTO cargo_shipments VALUES (6451021, 988582, 2, 45432, 84690, 542.530461285508);
INSERT INTO cargo_shipments VALUES (7441363, 734277, 4, 15678, 66598, 545.0579035940899);
INSERT INTO cargo_shipments VALUES (8837548, 964443, 3, 1006, 11007, 275.90319475765045);
INSERT INTO cargo_shipments VALUES (8922749, 554618, 1, 49746, 32322, 932.1113613558025);
INSERT INTO cargo_shipments VALUES (7908891, 651365, 4, 39148, 61652, 728.8087748103626);
INSERT INTO cargo_shipments VALUES (8346609, 240561, 4, 24640, 84275, 177.05537767379332);
INSERT INTO cargo_shipments VALUES (2593269, 931397, 2, 8184, 94072, 862.8278521678839);
INSERT INTO cargo_shipments VALUES (7208449, 319696, 4, 15205, 91113, 433.85163713465914);
INSERT INTO cargo_shipments VALUES (2023203, 982325, 3, 48934, 40197, 580.5334307579304);
INSERT INTO cargo_shipments VALUES (1450911, 578296, 1, 24281, 30478, 278.8661720367621);
INSERT INTO cargo_shipments VALUES (9181985, 481053, 2, 29527, 65140, 357.7576633343865);
INSERT INTO cargo_shipments VALUES (8410442, 464221, 1, 41755, 87702, 988.8708584455369);
INSERT INTO cargo_shipments VALUES (1384801, 551210, 4, 29643, 40002, 907.197592576863);
INSERT INTO cargo_shipments VALUES (1002579, 925782, 1, 21246, 58544, 742.2551727207194);
INSERT INTO cargo_shipments VALUES (4205428, 947049, 4, 25906, 31827, 968.513521450508);
INSERT INTO cargo_shipments VALUES (3213437, 844403, 4, 13213, 32643, 724.6024257629516);
INSERT INTO cargo_shipments VALUES (3098174, 818702, 2, 23761, 11007, 156.75698365779212);
INSERT INTO cargo_shipments VALUES (8634204, 694403, 1, 45695, 30478, 851.1701391009997);
INSERT INTO cargo_shipments VALUES (1703390, 593162, 4, 46970, 31036, 405.26340461884735);
INSERT INTO cargo_shipments VALUES (4739706, 607760, 1, 25388, 15015, 875.2297282075149);
INSERT INTO cargo_shipments VALUES (3721520, 229560, 4, 32981, 56241, 445.34103784552923);
INSERT INTO cargo_shipments VALUES (7705830, 622718, 3, 15198, 66598, 380.4066240877518);
INSERT INTO cargo_shipments VALUES (9588072, 654139, 4, 9440, 12118, 424.1525951495937);
INSERT INTO cargo_shipments VALUES (6629404, 481053, 4, 24925, 45811, 882.591891115113);
INSERT INTO cargo_shipments VALUES (9005204, 265945, 2, 24538, 69007, 426.33719842639795);
INSERT INTO cargo_shipments VALUES (4353979, 255144, 2, 22028, 53758, 861.1017669418742);
INSERT INTO cargo_shipments VALUES (4346418, 758527, 2, 40274, 32643, 986.0090257413143);
INSERT INTO cargo_shipments VALUES (4690394, 495332, 3, 20555, 17666, 513.8982885243514);
INSERT INTO cargo_shipments VALUES (6202791, 585228, 2, 12589, 45811, 837.1655581177707);
INSERT INTO cargo_shipments VALUES (4748121, 614562, 3, 33889, 81659, 226.7857794328496);
INSERT INTO cargo_shipments VALUES (3166426, 230534, 1, 6039, 64330, 678.1884672359025);
INSERT INTO cargo_shipments VALUES (3322060, 136113, 4, 26766, 16955, 103.56140052016538);
INSERT INTO cargo_shipments VALUES (7443991, 291137, 2, 7643, 84090, 31.19489055599667);
INSERT INTO cargo_shipments VALUES (4984641, 173966, 1, 35582, 38536, 403.2199807676724);
INSERT INTO cargo_shipments VALUES (3220008, 585228, 4, 2614, 93452, 990.7783664690998);
INSERT INTO cargo_shipments VALUES (2097431, 769533, 3, 16155, 88864, 361.42313049363264);
INSERT INTO cargo_shipments VALUES (4912968, 294483, 2, 45792, 42690, 716.2128328505637);
INSERT INTO cargo_shipments VALUES (6847600, 246438, 3, 7239, 21780, 664.1863265698838);
INSERT INTO cargo_shipments VALUES (7521340, 346198, 2, 13686, 40197, 427.8627793826737);
INSERT INTO cargo_shipments VALUES (8115040, 844403, 3, 39159, 32643, 963.778550452736);
INSERT INTO cargo_shipments VALUES (5393211, 764454, 1, 1694, 38314, 630.8632542585665);
INSERT INTO cargo_shipments VALUES (8591849, 898133, 2, 12596, 87702, 807.784239059091);
INSERT INTO cargo_shipments VALUES (2331080, 745546, 1, 11439, 32577, 311.24954013767393);
INSERT INTO cargo_shipments VALUES (7912311, 599462, 3, 14838, 61652, 892.4779587432237);
INSERT INTO cargo_shipments VALUES (9651298, 853530, 2, 47616, 56621, 564.2044975123309);
INSERT INTO cargo_shipments VALUES (3398320, 898133, 4, 7644, 41926, 187.4883499022446);
INSERT INTO cargo_shipments VALUES (7460115, 477350, 3, 40318, 84090, 338.36003796001444);
INSERT INTO cargo_shipments VALUES (2599017, 813446, 4, 23986, 41926, 13.103229348566447);
INSERT INTO cargo_shipments VALUES (1439455, 898133, 2, 22956, 72190, 256.23821783490195);
INSERT INTO cargo_shipments VALUES (7160096, 622034, 2, 6598, 13852, 339.05028798986456);
INSERT INTO cargo_shipments VALUES (6233198, 430854, 3, 45978, 56241, 75.4666837131246);
INSERT INTO cargo_shipments VALUES (4420267, 656769, 2, 38432, 40977, 38.137995171606654);
INSERT INTO cargo_shipments VALUES (8062944, 843101, 2, 12702, 52508, 321.9108912470574);
INSERT INTO cargo_shipments VALUES (9328155, 952986, 4, 41428, 40390, 841.2872214976234);
INSERT INTO cargo_shipments VALUES (5059703, 903883, 4, 26676, 41547, 850.977027519535);
INSERT INTO cargo_shipments VALUES (9208425, 265945, 1, 1492, 42260, 757.5161824593298);
INSERT INTO cargo_shipments VALUES (9690802, 668736, 1, 15699, 42260, 861.731444471154);
INSERT INTO cargo_shipments VALUES (5734387, 378721, 2, 6430, 11060, 642.3684744970377);
INSERT INTO cargo_shipments VALUES (7644238, 495332, 1, 42403, 54679, 910.7587341633741);
INSERT INTO cargo_shipments VALUES (9773490, 214614, 3, 40708, 27704, 871.2356899220781);
INSERT INTO cargo_shipments VALUES (2309076, 708494, 1, 44516, 65140, 810.9308639632981);
INSERT INTO cargo_shipments VALUES (3188454, 944547, 2, 43427, 17590, 333.6030054808431);
INSERT INTO cargo_shipments VALUES (4852271, 125473, 2, 35880, 45811, 467.4351098108859);
INSERT INTO cargo_shipments VALUES (7123140, 945423, 1, 45488, 76084, 942.2254585386327);
INSERT INTO cargo_shipments VALUES (8105780, 758527, 3, 16682, 93452, 582.1932441151461);
INSERT INTO cargo_shipments VALUES (9710706, 751755, 1, 15655, 48653, 196.77940915710434);
INSERT INTO cargo_shipments VALUES (7044389, 207609, 1, 35156, 19686, 387.2705001284533);
INSERT INTO cargo_shipments VALUES (3962525, 839442, 3, 18806, 32577, 251.61059609393342);
INSERT INTO cargo_shipments VALUES (2049386, 336949, 3, 49494, 46381, 350.9390599761669);
INSERT INTO cargo_shipments VALUES (9249193, 951732, 2, 37767, 84690, 665.2562487925819);
INSERT INTO cargo_shipments VALUES (5960515, 782892, 1, 24232, 87702, 994.3092738078948);
INSERT INTO cargo_shipments VALUES (2581867, 705140, 1, 17673, 36126, 776.1326987050062);
INSERT INTO cargo_shipments VALUES (9283210, 139045, 1, 6331, 41477, 612.0209485630752);
INSERT INTO cargo_shipments VALUES (2464001, 242407, 3, 27634, 41547, 375.2493462063019);
INSERT INTO cargo_shipments VALUES (7647250, 411362, 2, 2634, 15015, 531.7737489785574);
INSERT INTO cargo_shipments VALUES (9859784, 218188, 1, 22562, 65134, 476.9495917566177);
INSERT INTO cargo_shipments VALUES (2791720, 995741, 1, 31488, 65140, 928.1274968797455);
INSERT INTO cargo_shipments VALUES (3046109, 744120, 4, 42994, 31827, 404.0382661283438);
INSERT INTO cargo_shipments VALUES (3155546, 486096, 3, 5818, 50820, 350.4951549839187);
INSERT INTO cargo_shipments VALUES (8852013, 196524, 2, 1285, 82660, 653.4760523072068);
INSERT INTO cargo_shipments VALUES (1809529, 654139, 1, 19514, 50820, 386.3812394029376);
INSERT INTO cargo_shipments VALUES (6235270, 283188, 4, 34313, 64330, 37.23979047429759);
INSERT INTO cargo_shipments VALUES (2008788, 372521, 2, 21049, 13500, 394.39711911863606);
INSERT INTO cargo_shipments VALUES (2389929, 109915, 4, 22692, 50581, 598.6158400637523);
INSERT INTO cargo_shipments VALUES (6645608, 503550, 3, 14884, 12323, 206.04169102432013);
INSERT INTO cargo_shipments VALUES (7541594, 585228, 2, 4729, 41631, 124.25574742682255);
INSERT INTO cargo_shipments VALUES (2403311, 283188, 2, 45367, 97073, 349.91064487563483);
INSERT INTO cargo_shipments VALUES (8614230, 443855, 2, 26527, 90980, 114.21622717925028);
INSERT INTO cargo_shipments VALUES (3704502, 801910, 2, 5095, 31827, 721.9033540810666);
INSERT INTO cargo_shipments VALUES (9342836, 925782, 3, 47885, 12118, 551.1060290718963);
INSERT INTO cargo_shipments VALUES (4283139, 176018, 1, 23527, 34595, 871.2553130693685);
INSERT INTO cargo_shipments VALUES (6563594, 656769, 4, 47275, 13937, 141.65243576516008);
INSERT INTO cargo_shipments VALUES (1065085, 619695, 4, 13782, 53684, 149.22012611008927);
INSERT INTO cargo_shipments VALUES (4672298, 583957, 4, 11801, 50581, 772.1430160795056);
INSERT INTO cargo_shipments VALUES (1886591, 193915, 4, 49670, 47423, 171.8414828922471);
INSERT INTO cargo_shipments VALUES (8544817, 207609, 2, 6328, 38161, 500.2010386017801);
INSERT INTO cargo_shipments VALUES (1040618, 145117, 1, 15201, 13500, 718.6975799599304);
INSERT INTO cargo_shipments VALUES (7453780, 311617, 3, 26247, 11051, 510.768658753721);
INSERT INTO cargo_shipments VALUES (3381662, 132399, 2, 12090, 38314, 636.179314155133);
INSERT INTO cargo_shipments VALUES (3884472, 607760, 1, 9102, 13500, 470.7456677539961);
INSERT INTO cargo_shipments VALUES (4745299, 955811, 1, 36089, 94669, 918.0309822736868);
INSERT INTO cargo_shipments VALUES (5703194, 120378, 3, 26082, 99271, 261.9376477692666);
INSERT INTO cargo_shipments VALUES (4827208, 287765, 1, 27015, 55085, 133.46632473873586);
INSERT INTO cargo_shipments VALUES (4425565, 782892, 4, 10157, 11007, 341.58884573954674);
INSERT INTO cargo_shipments VALUES (3696465, 769533, 1, 15163, 11051, 488.8089177488972);
INSERT INTO cargo_shipments VALUES (8488684, 710029, 4, 48324, 42690, 2.8437580825201314);
INSERT INTO cargo_shipments VALUES (3325352, 722031, 4, 28132, 54679, 534.936288969745);
INSERT INTO cargo_shipments VALUES (5313230, 422672, 1, 31005, 50581, 571.0377721616896);
INSERT INTO cargo_shipments VALUES (9622734, 785591, 2, 22322, 13500, 542.586700132281);
INSERT INTO cargo_shipments VALUES (1346078, 176018, 1, 36366, 16955, 553.2037413046661);
INSERT INTO cargo_shipments VALUES (7613774, 610948, 1, 21001, 33265, 122.28385968168742);
INSERT INTO cargo_shipments VALUES (7394337, 417017, 3, 43356, 60373, 884.924988491135);
INSERT INTO cargo_shipments VALUES (1949354, 567832, 2, 1308, 32577, 527.4486446071086);
INSERT INTO cargo_shipments VALUES (3933253, 797115, 4, 14911, 16955, 129.66766592723778);
INSERT INTO cargo_shipments VALUES (7823113, 925782, 2, 22107, 41631, 424.5755639908508);
INSERT INTO cargo_shipments VALUES (5514466, 480401, 1, 34234, 92389, 275.6921216488081);
INSERT INTO cargo_shipments VALUES (8508493, 368069, 1, 37485, 42260, 701.8729772399423);
INSERT INTO cargo_shipments VALUES (3844776, 955018, 4, 32968, 56173, 935.078245006507);
INSERT INTO cargo_shipments VALUES (6753629, 961289, 2, 34729, 11051, 418.47626766460786);
INSERT INTO cargo_shipments VALUES (5100570, 196524, 2, 23453, 50303, 422.3522814693708);
INSERT INTO cargo_shipments VALUES (3498034, 214614, 3, 27066, 97095, 8.579379628205652);
INSERT INTO cargo_shipments VALUES (9080470, 615352, 3, 30995, 12118, 813.4942913759342);
INSERT INTO cargo_shipments VALUES (1415423, 599462, 2, 26501, 40002, 720.5508415790865);
INSERT INTO cargo_shipments VALUES (9266543, 843101, 3, 38420, 81659, 56.957716080042296);
INSERT INTO cargo_shipments VALUES (8809764, 477350, 2, 2270, 84690, 506.33644002447454);
INSERT INTO cargo_shipments VALUES (3618305, 681816, 4, 15239, 72190, 649.9078725190499);
INSERT INTO cargo_shipments VALUES (8520296, 951732, 2, 30356, 21780, 730.1762954095351);
INSERT INTO cargo_shipments VALUES (4592095, 140527, 4, 45711, 59636, 321.776819314407);
INSERT INTO cargo_shipments VALUES (9548072, 744120, 2, 18007, 30478, 439.90821281516077);
INSERT INTO cargo_shipments VALUES (9268150, 477350, 3, 43291, 50303, 541.2745375301668);
INSERT INTO cargo_shipments VALUES (6660315, 139249, 2, 41626, 17666, 538.3076604777957);
INSERT INTO cargo_shipments VALUES (3881476, 804924, 2, 23652, 73292, 932.321327328074);
INSERT INTO cargo_shipments VALUES (7016200, 587854, 2, 18278, 46381, 313.758629864317);
INSERT INTO cargo_shipments VALUES (3519897, 417366, 1, 14543, 88864, 693.2437384400043);
INSERT INTO cargo_shipments VALUES (8230472, 686783, 3, 1617, 46381, 684.7285510253987);
INSERT INTO cargo_shipments VALUES (2249921, 371982, 3, 42697, 53482, 440.22774609185575);
INSERT INTO cargo_shipments VALUES (5434597, 466776, 4, 19769, 53234, 644.1617297336397);
INSERT INTO cargo_shipments VALUES (5593259, 365267, 2, 49840, 46825, 455.9405882088218);
INSERT INTO cargo_shipments VALUES (3954650, 503550, 3, 7799, 47423, 27.75077580455576);
INSERT INTO cargo_shipments VALUES (3091482, 287765, 1, 32721, 41631, 399.53297610122684);
INSERT INTO cargo_shipments VALUES (9136866, 459618, 4, 21839, 41898, 316.6221310474114);
INSERT INTO cargo_shipments VALUES (3799005, 388484, 1, 11386, 61652, 862.3895101516308);
INSERT INTO cargo_shipments VALUES (6525178, 955018, 2, 21752, 90765, 993.2088060110543);
INSERT INTO cargo_shipments VALUES (6855322, 196524, 3, 42733, 15898, 994.581205879945);
INSERT INTO cargo_shipments VALUES (9476595, 927176, 4, 44129, 13500, 839.4299293411673);
INSERT INTO cargo_shipments VALUES (3193088, 513030, 2, 44728, 40002, 565.1898518611711);
INSERT INTO cargo_shipments VALUES (2899942, 342224, 1, 48239, 86258, 653.1924253065558);
INSERT INTO cargo_shipments VALUES (7665100, 595282, 2, 39928, 41926, 690.2482011353317);
INSERT INTO cargo_shipments VALUES (5365045, 578296, 1, 18290, 40002, 126.41110008594359);
INSERT INTO cargo_shipments VALUES (9508331, 286425, 1, 4756, 33265, 113.02217844288398);
INSERT INTO cargo_shipments VALUES (1218848, 735758, 3, 27280, 99271, 792.7776221899876);
INSERT INTO cargo_shipments VALUES (1314175, 196131, 2, 47525, 46825, 829.1979938865925);
INSERT INTO cargo_shipments VALUES (2119619, 385272, 4, 6639, 11051, 95.24783108000446);
INSERT INTO cargo_shipments VALUES (1535145, 283188, 4, 33438, 35671, 54.378985782808975);
INSERT INTO cargo_shipments VALUES (1841675, 839442, 2, 38290, 56615, 794.5201923585488);
INSERT INTO cargo_shipments VALUES (1071836, 143671, 1, 5727, 60646, 474.01866269653505);
INSERT INTO cargo_shipments VALUES (2724541, 480401, 3, 43929, 36453, 436.38049799146785);
INSERT INTO cargo_shipments VALUES (5427621, 468368, 4, 37127, 21780, 689.4074685421352);
INSERT INTO cargo_shipments VALUES (6574024, 264687, 2, 15185, 76084, 5.357749621526575);
INSERT INTO cargo_shipments VALUES (1409799, 481053, 4, 25963, 65140, 872.24696313234);
INSERT INTO cargo_shipments VALUES (1049813, 582110, 4, 27132, 59459, 4.894982385718616);
INSERT INTO cargo_shipments VALUES (1981709, 702659, 2, 11647, 57468, 652.0822628900073);
INSERT INTO cargo_shipments VALUES (4893561, 388891, 4, 45227, 49548, 780.0641006068315);
INSERT INTO cargo_shipments VALUES (7987967, 120378, 2, 41098, 66509, 904.2223210148784);
INSERT INTO cargo_shipments VALUES (8489081, 944074, 3, 46783, 12118, 275.316955350981);
INSERT INTO cargo_shipments VALUES (2884333, 385272, 2, 7229, 13852, 197.52311452057057);
INSERT INTO cargo_shipments VALUES (1910614, 944547, 3, 16305, 42021, 34.115535492449055);
INSERT INTO cargo_shipments VALUES (5799367, 708494, 1, 30705, 50581, 376.2432038043615);
INSERT INTO cargo_shipments VALUES (9161645, 885046, 2, 27162, 31036, 340.8537104870789);
INSERT INTO cargo_shipments VALUES (1862211, 207609, 1, 17577, 97073, 837.6569330154256);
INSERT INTO cargo_shipments VALUES (2825707, 289019, 3, 28481, 52508, 866.5028065343711);
INSERT INTO cargo_shipments VALUES (1503166, 681816, 4, 9762, 87026, 39.726061333290374);
INSERT INTO cargo_shipments VALUES (5746291, 477627, 4, 30424, 35671, 679.8171214261793);
INSERT INTO cargo_shipments VALUES (2635069, 843961, 1, 20542, 67231, 104.24403776135394);
INSERT INTO cargo_shipments VALUES (1738782, 995615, 4, 16392, 64330, 314.50443625481705);
INSERT INTO cargo_shipments VALUES (6007575, 944074, 3, 36079, 94669, 476.8488480600169);
INSERT INTO cargo_shipments VALUES (1556606, 342326, 1, 23304, 11060, 691.2039124325552);
INSERT INTO cargo_shipments VALUES (9397968, 758527, 1, 41066, 59459, 199.59479406458348);
INSERT INTO cargo_shipments VALUES (5512652, 654139, 1, 4394, 58284, 205.96921194726647);
