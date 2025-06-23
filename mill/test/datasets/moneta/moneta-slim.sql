//=========== moneta-slim clients ==========
DROP TABLE IF EXISTS clients;
CREATE TABLE clients (
	 client_id INT NOT NULL
	,first_name VARCHAR NOT NULL
	,last_name VARCHAR NOT NULL
	,email VARCHAR NOT NULL
	,phone VARCHAR NOT NULL
	,created_at VARCHAR NOT NULL
	,country VARCHAR NOT NULL
	,city VARCHAR NOT NULL
	,address VARCHAR NOT NULL
	,segment VARCHAR NOT NULL

);
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (6139636,'Harry','Hamilton','vanessavance@example.net','001-242-366-6648x1450','2020-10-05','Greenland','Rogersshire','46301 Johnson Vista Apt. 835','REGULAR');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (4095313,'Chad','Wu','shawn64@example.com','001-608-856-9908x81538','2020-08-14','Israel','Robbinsport','778 Hampton Shore','WEALTH');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (2711030,'Lauren','Wu','josenichols@example.com','917-597-2173x58284','2021-09-03','Niger','New Joseph','922 Robert Pass Suite 428','ULTRA');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (8362320,'Larry','Kramer','annette34@example.com','001-429-403-1318x5945','2020-11-21','Albania','Chenland','94795 Katie Plains Apt. 391','WEALTH');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (7694734,'Steven','Fowler','elizabeth94@example.org','001-276-229-3415x7101','2020-02-20','Suriname','Sandraside','39028 Kurt Springs Suite 361','WEALTH');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (3516687,'Andrew','Maldonado','robert37@example.net','(548)328-9033','2022-02-19','China','Tristanmouth','3115 James Green Apt. 142','WEALTH');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (4132223,'Abigail','Young','bcastillo@example.com','+1-605-724-3983x0816','2024-10-10','Cuba','New Williamhaven','9693 Campbell Walks','ULTRA');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (5165588,'Carolyn','Bell','lbush@example.org','001-250-526-1201x41162','2025-02-22','Netherlands','Michaelside','1353 Williams Turnpike','REGULAR');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (7569880,'Ann','Russell','ucline@example.org','+1-677-770-4054x204','2021-09-21','Botswana','Jacobschester','871 Henry Forks','REGULAR');
INSERT INTO clients (client_id,first_name,last_name,email,phone,created_at,country,city,address,segment) VALUES (6583592,'John','Davis','allenjames@example.org','(861)729-1703x8260','2023-02-13','Algeria','North Lauratown','32231 Zachary Stream','ULTRA');
//=========== moneta-slim accounts ==========
DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts (
	 account_id INT NOT NULL
	,client_id INT NOT NULL
	,account_type VARCHAR NOT NULL
	,balance DECIMAL(15,4) NOT NULL
	,opened_at VARCHAR NOT NULL

);
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (7315248,4132223,'checking',39.277868065048516,'2023-06-09');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (8537210,4132223,'checking',289.17376396534354,'2022-04-05');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (9692642,7694734,'savings',60.22409299839626,'2023-04-27');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (5930066,7694734,'savings',313.9574346284345,'2025-03-09');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (3208572,7569880,'savings',630.666908296011,'2020-06-19');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (1817576,7569880,'savings',72.01947327567437,'2022-12-15');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (8076453,5165588,'savings',200.47140254040096,'2021-12-13');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (5693014,2711030,'checking',623.8515953641395,'2020-06-15');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (1712486,4095313,'checking',435.1844870727791,'2025-03-15');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (6525489,4095313,'savings',97.95726141509343,'2020-07-03');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (6081098,4132223,'checking',814.7139909508194,'2024-01-13');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (2032526,6139636,'checking',695.3588516876555,'2023-10-31');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (8859444,7694734,'savings',192.4682628290758,'2024-02-09');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (5903898,6583592,'savings',941.0738258149263,'2023-08-08');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (9289430,7694734,'checking',330.3516066121895,'2024-07-09');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (4145900,5165588,'checking',971.1544270974922,'2021-05-12');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (8665861,4095313,'checking',546.3527141500363,'2022-12-12');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (5933927,7694734,'checking',381.89547463823845,'2021-08-27');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (2027493,6139636,'savings',23.742046295820664,'2025-03-11');
INSERT INTO accounts (account_id,client_id,account_type,balance,opened_at) VALUES (6247830,2711030,'savings',718.7197383507458,'2021-02-09');
//=========== moneta-slim transactions ==========
DROP TABLE IF EXISTS transactions;
CREATE TABLE transactions (
	 transaction_id INT NOT NULL
	,account_id INT NOT NULL
	,transaction_type VARCHAR NOT NULL
	,amount DECIMAL(15,4) NOT NULL
	,transaction_date VARCHAR NOT NULL
	,description VARCHAR NOT NULL

);
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7749718,4145900,'deposit',751.7548569210219,'2025-06-09','Social least deal.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (5342750,8859444,'withdrawal',423.48172904251527,'2025-02-14','Listen cost up language fire themselves.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (9129249,1712486,'deposit',947.2026725851318,'2025-03-17','Never generation rise newspaper.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (2426202,5693014,'withdrawal',179.81247533264656,'2025-03-30','Effect growth member quickly.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (4747663,5930066,'withdrawal',683.4340407339126,'2025-06-08','Fact professor go next admit of.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (3166916,1817576,'deposit',563.8499832311646,'2025-02-22','Music mother Democrat no one foreign also.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (2665465,2027493,'deposit',214.21277743520574,'2025-03-23','End project edge whether institution anyone.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (2261868,6081098,'withdrawal',169.24563758956924,'2025-02-23','Agreement box this perform.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (8775230,7315248,'withdrawal',240.70256418366253,'2025-02-27','Rule it build method.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (6362207,2027493,'transfer',933.746721983103,'2025-05-06','Seek training pick move.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (6330824,8859444,'withdrawal',278.7489218287924,'2025-03-03','Human performance hot enjoy never cell hot language.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (1289080,5693014,'transfer',738.4701494386197,'2025-03-07','Wish candidate discuss physical relationship skill.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (1365602,2027493,'transfer',876.8142403559145,'2025-05-13','That theory chair event thousand push.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (3898101,8859444,'transfer',725.7282505667922,'2025-03-28','Meet day poor impact sort successful hand.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (2687321,8859444,'deposit',162.07624496283844,'2025-01-26','Official capital growth always series PM million room.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (8641914,8859444,'transfer',575.778858107522,'2025-04-02','Example deep imagine collection.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (6909195,2027493,'transfer',196.94173826702888,'2025-01-17','Or case health budget ten contain.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7687895,9692642,'withdrawal',321.87250288091275,'2025-05-24','Difference investment fear adult station early hour movement.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7215957,4145900,'transfer',791.6109986442809,'2025-04-04','Standard against behind current agent view.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (5254496,8859444,'transfer',746.7770015699251,'2025-03-13','Of moment not newspaper actually benefit film.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (6819354,8859444,'deposit',244.76460763974083,'2025-03-18','Today recognize because individual.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (9146487,2027493,'withdrawal',342.96018722929466,'2025-02-21','How service government onto.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7329849,5903898,'transfer',192.69456722563817,'2025-05-29','Also idea forward town main.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (4223190,3208572,'withdrawal',432.7442716217135,'2025-04-15','Project today piece great voice.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (9319404,5693014,'deposit',230.4079645432965,'2025-04-22','Long baby our hope magazine into.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (9582529,6081098,'transfer',571.8302556523461,'2025-05-25','Control manager former I.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (4347560,5903898,'deposit',678.108091711268,'2025-04-23','Billion everything design color.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7251905,7315248,'transfer',688.2664026084062,'2025-06-16','Off trouble then another.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7801062,1712486,'withdrawal',453.12337019569503,'2025-04-05','Show many office think huge painting.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (6236716,8537210,'withdrawal',152.3073514741623,'2025-01-24','Mind part seat up thank door.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (5269866,8076453,'deposit',935.0683955297288,'2025-01-19','Relate top every people whatever design treatment.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (8049960,6525489,'transfer',861.9150433516129,'2025-05-26','Hard kind class pattern college fall road.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (8424756,5933927,'withdrawal',463.235916753072,'2025-05-05','There project own plant must sound part toward.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (8162110,6525489,'withdrawal',651.5954415539455,'2025-04-22','Beat owner even term.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (1470779,4145900,'deposit',16.20182658619751,'2025-06-01','Society forget north resource ready.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7635017,9289430,'transfer',317.5270802467066,'2025-05-03','Task perhaps hour change.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (2196631,9692642,'deposit',928.7537104211817,'2025-01-02','Goal west attention second.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7397875,7315248,'deposit',640.5721305432054,'2025-05-11','Spring focus meeting share.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (3158178,5903898,'withdrawal',58.966716174079004,'2025-05-01','Those fish necessary hit customer.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (1749247,5930066,'transfer',542.7573997039435,'2025-03-17','Identify compare organization lead.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (8058084,8665861,'deposit',985.5788300165599,'2025-04-17','Anything treat job better imagine difference.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (4704463,1712486,'withdrawal',328.3968757161524,'2025-05-28','Somebody real doctor sometimes growth involve describe American.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (4412517,1817576,'deposit',836.9487859609724,'2025-05-03','Laugh create traditional matter throw president yourself.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7147794,1712486,'deposit',127.47047349118745,'2025-02-07','Direction provide country thought guess.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (4442454,8076453,'withdrawal',445.9934539140973,'2025-04-01','Wait throughout PM light.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (6548645,1817576,'transfer',399.60291023048586,'2025-05-05','War could turn provide list.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (8767171,4145900,'transfer',874.2968846485248,'2025-03-06','While heavy age until dinner.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (5109693,8859444,'deposit',176.6588556351628,'2025-06-06','Black morning model expect can knowledge red single.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (7318863,8665861,'withdrawal',920.7461480337323,'2025-01-16','Pay base face real television catch year.');
INSERT INTO transactions (transaction_id,account_id,transaction_type,amount,transaction_date,description) VALUES (6908538,8537210,'withdrawal',291.82316706449075,'2025-04-26','Organization clear occur maintain.');
//=========== moneta-slim loans ==========
DROP TABLE IF EXISTS loans;
CREATE TABLE loans (
	 loan_id INT NOT NULL
	,client_id INT NOT NULL
	,amount DECIMAL(15,4) NOT NULL
	,interest_rate DECIMAL(15,4) NOT NULL
	,start_date VARCHAR NOT NULL
	,end_date VARCHAR NOT NULL
	,status VARCHAR NOT NULL

);
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (101798,2711030,634.9463766893007,671.2735068124429,'2021-12-27','2024-04-04','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (665993,8362320,920.0803940246891,904.4035094690585,'2020-08-08','2025-02-03','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (411215,6583592,908.348775828298,388.11834059897234,'2021-05-18','2022-02-21','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (490766,3516687,488.75190187811467,488.69203249318883,'2020-05-29','2023-07-30','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (263555,4132223,990.9954820556962,899.8758830095471,'2024-04-27','2025-05-09','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (787416,3516687,375.23697354759764,499.52038425277914,'2020-10-19','2025-03-05','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (697640,3516687,462.2214516829448,802.7983836443011,'2024-10-08','2025-03-14','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (249529,2711030,190.8312112951821,745.0780119106474,'2022-05-14','2022-07-31','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (911076,6583592,758.3663316229214,377.707578648661,'2024-06-17','2020-04-02','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (937633,5165588,630.5189873392052,337.7964677623857,'2023-02-01','2021-12-04','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (937422,6139636,238.75609235961838,142.48504859398224,'2023-04-25','2024-11-12','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (905375,3516687,431.74432782902215,127.84109806779831,'2021-07-01','2024-03-01','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (471538,4095313,577.033052941689,287.3582971175026,'2021-05-26','2023-12-18','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (632144,7569880,639.4520052042164,760.2716629834662,'2022-09-02','2020-04-25','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (422532,7569880,337.6672548183357,138.88855679611268,'2020-02-04','2022-02-17','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (879248,7694734,161.69136325195964,541.3677902296512,'2025-06-20','2022-11-17','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (620485,4132223,818.6542203465098,953.7375578434597,'2023-02-09','2024-06-17','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (496098,4095313,538.0471352684339,844.3594063094672,'2022-05-30','2022-01-22','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (253297,6583592,520.2903873201064,483.5634762703154,'2021-06-10','2023-03-01','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (785407,2711030,613.4880254692448,549.9624881126416,'2022-01-09','2024-08-09','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (413097,7694734,213.98556715573702,26.46113660286509,'2025-01-14','2021-09-04','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (920336,3516687,923.6667677942876,568.7786020153884,'2020-06-05','2024-10-11','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (130395,4095313,257.5611901795465,715.802908243158,'2021-07-20','2022-05-16','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (144346,3516687,488.1511366560309,94.87440028411432,'2024-09-15','2024-06-21','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (426197,6583592,828.8412931099492,565.7374392235662,'2020-09-04','2020-01-22','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (187584,4132223,572.2447873439997,451.3367517445791,'2025-02-06','2020-06-15','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (919073,5165588,486.6716615131468,870.1629786258892,'2024-04-07','2022-06-27','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (423100,5165588,920.7593547240851,888.4230013834061,'2024-09-29','2023-09-04','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (977647,5165588,54.842994545039936,305.91291021578706,'2024-04-02','2021-06-25','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (710800,2711030,732.3340533981402,458.26054673437653,'2024-03-14','2022-02-13','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (860277,5165588,657.4952817470795,104.37971948541669,'2025-01-25','2020-10-28','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (163890,7569880,596.3004081850593,734.8469138126383,'2021-10-02','2023-10-29','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (509851,6139636,326.54331492452684,442.1054446495368,'2021-01-17','2024-06-10','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (268258,6139636,538.0836812612044,556.2693156915188,'2025-05-06','2024-01-27','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (345323,5165588,97.57853630771784,643.1285492373664,'2024-12-19','2022-07-19','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (847557,7569880,520.6444901852384,626.1295774916427,'2022-10-23','2024-03-14','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (983657,6139636,415.2278826189244,352.54801216834954,'2021-12-27','2025-04-12','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (490516,2711030,481.83152320687293,344.4206372886962,'2023-02-18','2021-01-15','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (339339,7694734,592.1104612640195,599.9708880427651,'2025-03-06','2022-01-18','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (212802,7694734,49.95263068512878,485.5279076710339,'2021-06-19','2020-08-31','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (380995,6139636,806.7173387397825,525.4903321338354,'2020-08-05','2023-11-03','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (117064,3516687,102.03285353192571,745.7444582768998,'2025-04-15','2025-06-07','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (415988,6139636,839.9737087281492,795.3882779310325,'2020-01-31','2020-04-06','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (363090,4132223,737.0544237680814,174.27721551864585,'2020-05-19','2021-10-05','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (766411,4095313,530.2919801316584,875.144899928956,'2021-11-09','2023-02-13','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (854368,6139636,333.3812099356178,63.896434314766104,'2022-08-04','2025-03-02','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (152998,2711030,91.14313403379958,539.0147671747579,'2024-05-13','2024-10-21','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (424666,3516687,294.9234669807239,999.3933494640227,'2024-04-17','2024-09-28','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (601100,4132223,757.9504223318753,951.7172243703201,'2022-08-18','2021-09-15','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (555594,4095313,941.1195837276424,833.738434447418,'2021-04-06','2024-02-05','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (188307,6139636,389.3055456039165,122.9807212919064,'2022-11-28','2025-05-04','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (972206,4095313,307.6578970961343,126.16445132093568,'2021-01-13','2020-07-19','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (288289,2711030,48.26784630232961,954.3011027650128,'2022-12-08','2021-11-21','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (262314,8362320,898.8291996951955,505.406355621418,'2021-03-30','2022-04-10','closed');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (741926,6583592,395.02565553946255,939.3343575479585,'2022-09-20','2020-06-02','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (651021,8362320,466.62361676007134,255.21010017176505,'2024-04-22','2020-06-18','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (247911,7694734,902.070564618518,705.5169390265364,'2020-11-09','2022-09-06','active');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (782999,5165588,596.9950601990272,187.14463184307306,'2022-10-12','2025-02-04','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (438287,5165588,67.61842191377244,659.0068677676882,'2022-04-07','2023-10-19','defaulted');
INSERT INTO loans (loan_id,client_id,amount,interest_rate,start_date,end_date,status) VALUES (251601,5165588,237.8140590937815,633.8450417203269,'2020-06-29','2023-04-28','defaulted');
//=========== moneta-slim loan_payments ==========
DROP TABLE IF EXISTS loan_payments;
CREATE TABLE loan_payments (
	 payment_id INT NOT NULL
	,loan_id INT NOT NULL
	,payment_date VARCHAR NOT NULL
	,amount DECIMAL(15,4) NOT NULL

);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (566881,977647,'2025-01-11',54.511720202430006);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (438703,263555,'2025-03-15',839.0091553642803);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (594161,144346,'2025-06-04',370.03422489155);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (664037,879248,'2025-05-30',865.8116057367826);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (757805,847557,'2025-03-31',618.9186614342105);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (742045,496098,'2025-01-22',128.61518378486426);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (487123,854368,'2025-06-09',395.39839141016284);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (893374,879248,'2025-01-14',828.3639265140868);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (478393,426197,'2025-01-22',57.34022257803828);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (710847,937422,'2025-02-03',737.9863687987066);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (674992,782999,'2025-03-13',688.4297132795741);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (976835,438287,'2025-05-24',161.38065530425138);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (102575,983657,'2025-02-05',58.437042415023235);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (407536,471538,'2025-06-18',39.73889025165689);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (645055,268258,'2025-05-22',205.54700070117406);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (300424,632144,'2025-02-19',118.42001284786208);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (752486,130395,'2025-01-27',404.715621795249);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (538890,651021,'2025-03-25',127.37863287425466);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (476762,117064,'2025-05-03',717.6186610800775);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (257730,251601,'2025-05-13',336.80751409448817);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (670412,496098,'2025-06-03',787.6637033708002);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (518024,555594,'2025-02-24',437.88320037282205);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (856348,163890,'2025-02-23',211.7965615466768);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (955096,212802,'2025-02-21',709.1225528870464);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (270958,651021,'2025-02-28',831.6014517402497);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (196869,911076,'2025-03-09',179.2974401291807);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (985674,471538,'2025-05-21',734.9803782358069);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (782139,785407,'2025-02-16',628.2158055143054);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (254376,860277,'2025-05-29',394.6716103891473);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (480400,651021,'2025-02-16',886.9819141048174);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (975884,212802,'2025-05-11',800.732780259547);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (331798,363090,'2025-05-29',411.17712270123855);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (337306,152998,'2025-06-02',848.3783079889334);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (257744,424666,'2025-05-03',391.8622735924656);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (193067,555594,'2025-04-21',775.8020858200283);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (301342,937422,'2025-05-04',967.9235756828091);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (980509,380995,'2025-02-21',23.68537202751375);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (855644,509851,'2025-01-07',460.1545938608702);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (242164,101798,'2025-06-02',474.0381291961426);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (492825,251601,'2025-06-04',931.9825810904692);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (142567,152998,'2025-02-12',826.4521822124456);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (935021,854368,'2025-03-27',531.5508991892644);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (248278,860277,'2025-06-21',404.2222086051471);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (925957,937633,'2025-01-15',978.0305309162169);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (728379,601100,'2025-03-10',581.3031507980525);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (691704,509851,'2025-06-10',882.3350164072157);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (519199,854368,'2025-05-07',652.2318949403106);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (357152,426197,'2025-03-23',350.32904285503463);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (579101,665993,'2025-06-02',228.35058415052112);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (918908,860277,'2025-03-04',959.3226773946362);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (647535,651021,'2025-04-06',711.7209413706141);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (872655,766411,'2025-05-11',855.5923123557915);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (965788,380995,'2025-04-21',911.3021242879864);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (181958,263555,'2025-01-21',948.8517883968877);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (541693,413097,'2025-03-10',151.58937509745573);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (269501,263555,'2025-05-09',259.62011387917306);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (453581,188307,'2025-02-27',290.44467460420555);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (500704,363090,'2025-01-03',461.8636511713169);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (621982,782999,'2025-06-02',47.224991487299974);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (293254,117064,'2025-04-17',877.2414738000665);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (395343,415988,'2025-05-05',347.7737835378735);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (253798,490766,'2025-03-06',152.36026842373528);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (535839,911076,'2025-01-29',282.39241762566434);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (358127,117064,'2025-01-14',811.903475157553);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (345532,212802,'2025-01-06',217.610088972475);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (968396,263555,'2025-05-07',696.9507923324962);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (920864,187584,'2025-06-21',234.35196317944963);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (821053,144346,'2025-03-26',538.1664371922845);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (762919,697640,'2025-06-10',40.19779917732358);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (412332,422532,'2025-03-17',521.6285770402011);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (879039,247911,'2025-02-13',466.162459009526);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (558090,787416,'2025-04-09',632.558543138669);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (683467,438287,'2025-02-04',215.8749314001608);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (774627,710800,'2025-02-28',414.40786288032393);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (824379,426197,'2025-05-19',34.97583367475243);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (301872,423100,'2025-02-03',192.99682221288174);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (700623,101798,'2025-05-31',3.1399769303543934);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (719078,249529,'2025-02-16',153.7957640772789);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (356581,426197,'2025-03-05',531.1794057280365);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (946255,782999,'2025-05-11',605.7967517499399);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (132006,782999,'2025-02-26',497.28555852136355);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (469499,413097,'2025-04-18',390.5477209231236);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (440176,424666,'2025-02-24',680.435918498223);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (421216,411215,'2025-03-31',293.910488775845);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (602888,130395,'2025-06-09',846.2850613827204);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (998832,249529,'2025-06-21',357.49072572807825);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (254427,117064,'2025-03-07',698.3432077418817);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (658105,422532,'2025-06-05',527.6120176156151);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (961363,262314,'2025-01-25',983.5378950787466);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (279614,555594,'2025-05-21',408.72137206981006);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (123109,345323,'2025-01-20',556.8138087131928);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (208384,919073,'2025-03-22',436.71510894647315);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (917788,363090,'2025-01-24',674.4900987669295);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (334819,163890,'2025-06-06',703.8680945876255);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (109266,785407,'2025-01-26',62.45845611501799);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (329790,268258,'2025-01-18',269.445580699222);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (893372,426197,'2025-02-26',337.9147911498014);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (821580,253297,'2025-06-22',608.7645049501515);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (765913,787416,'2025-02-26',158.82742348828017);
INSERT INTO loan_payments (payment_id,loan_id,payment_date,amount) VALUES (202355,710800,'2025-01-07',990.8827621463887);
//=========== moneta-slim stock_portfolios ==========
DROP TABLE IF EXISTS stock_portfolios;
CREATE TABLE stock_portfolios (
	 portfolio_id INT NOT NULL
	,client_id INT NOT NULL
	,portfolio_name VARCHAR NOT NULL
	,created_at VARCHAR NOT NULL

);
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (7938584,2711030,'PORTF-SWVHS','2021-09-03');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (6076091,6139636,'PORTF-XFDDC','2025-05-21');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (1075287,6583592,'PORTF-AMLWC','2022-05-23');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5495400,6139636,'PORTF-WUERE','2021-09-12');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (1430436,5165588,'PORTF-BCSFE','2024-04-19');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (3610649,6139636,'PORTF-IXQBD','2020-05-19');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (1305917,7569880,'PORTF-UTRGA','2024-11-21');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8197944,3516687,'PORTF-HHFUL','2022-06-11');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (3212496,6139636,'PORTF-IXXNC','2021-10-24');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (4382910,8362320,'PORTF-ATSAQ','2021-01-25');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5553348,6139636,'PORTF-XYTJQ','2024-06-05');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (4891244,5165588,'PORTF-GEGVU','2022-02-06');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5116052,3516687,'PORTF-TDFII','2022-03-24');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (4993408,7569880,'PORTF-ZQOUC','2024-02-23');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (9141697,6139636,'PORTF-MVLXC','2021-08-09');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (9851590,2711030,'PORTF-LCBVB','2023-07-12');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (1026459,7694734,'PORTF-XRLGK','2021-08-12');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (6448244,8362320,'PORTF-XAOSU','2024-10-26');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8810608,2711030,'PORTF-UCJOQ','2020-05-08');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5531375,4095313,'PORTF-EXQQJ','2021-04-08');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8405694,7569880,'PORTF-ATNKC','2020-06-07');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (4795605,6139636,'PORTF-QBFNC','2023-09-09');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (3398168,3516687,'PORTF-LGNHS','2024-05-26');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (3321525,3516687,'PORTF-IDFSW','2023-10-07');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5699914,6583592,'PORTF-MCGIM','2022-10-07');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8514508,6583592,'PORTF-LSWAG','2020-07-20');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (2197547,6583592,'PORTF-FLAUD','2024-05-02');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (4981394,8362320,'PORTF-APKUI','2021-01-19');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8282783,4095313,'PORTF-UUMOH','2025-04-05');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (9660447,6139636,'PORTF-VAXMY','2020-06-05');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (3726354,2711030,'PORTF-NUNSI','2024-04-22');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (6415840,7694734,'PORTF-QLXZM','2025-05-28');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (7588168,4132223,'PORTF-KPYEB','2024-12-05');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5859184,8362320,'PORTF-DKTCK','2021-07-11');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5585237,5165588,'PORTF-HOYDY','2022-12-19');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (5695353,7694734,'PORTF-QOPCK','2025-06-01');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8297423,6583592,'PORTF-WFQDU','2020-06-25');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8920335,2711030,'PORTF-TODGP','2020-10-25');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (2054001,5165588,'PORTF-GLBTW','2023-09-06');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (6661060,7694734,'PORTF-CRBNR','2023-12-15');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (1654337,6583592,'PORTF-JLGKJ','2021-01-05');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (7178324,4132223,'PORTF-IPJOQ','2021-11-30');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (9764250,3516687,'PORTF-TFSZY','2024-04-19');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (6428281,7569880,'PORTF-KXNMH','2023-02-21');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (6033535,4132223,'PORTF-HGNNW','2021-02-12');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (4893987,2711030,'PORTF-PLDGZ','2021-06-17');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8698258,8362320,'PORTF-AGFHZ','2022-10-16');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8055697,6583592,'PORTF-FYFHJ','2022-08-31');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8336188,7569880,'PORTF-PATHI','2024-09-18');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (7842356,5165588,'PORTF-FODDG','2023-11-08');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (8705449,3516687,'PORTF-PVPQM','2024-12-22');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (4320782,4132223,'PORTF-FUHLU','2021-11-01');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (1524334,6583592,'PORTF-CHMRU','2021-04-05');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (1619128,6139636,'PORTF-IGQZB','2024-12-24');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (3023971,4095313,'PORTF-UPXXQ','2021-09-19');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (9064053,4132223,'PORTF-RGEZQ','2023-10-27');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (6773070,3516687,'PORTF-GPLFB','2021-06-23');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (7858893,6139636,'PORTF-PAYNE','2020-05-26');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (7153437,8362320,'PORTF-FRYSI','2023-06-19');
INSERT INTO stock_portfolios (portfolio_id,client_id,portfolio_name,created_at) VALUES (2321289,8362320,'PORTF-VNDTX','2020-03-30');
//=========== moneta-slim stocks ==========
DROP TABLE IF EXISTS stocks;
CREATE TABLE stocks (
	 stock_id INT NOT NULL
	,ticker VARCHAR NOT NULL
	,company_name VARCHAR NOT NULL
	,sector VARCHAR NOT NULL
	,exchange VARCHAR NOT NULL

);
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (6632,'NJOM','Thomas PLC','Technology','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (5687,'NKLW','Hayes, Malone and Wood','Retail','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (6376,'WNUX','Curry Group','Energy','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (2241,'RXBK','Harris, Brown and Peck','Retail','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (1176,'OBOG','Cruz, Ballard and Bennett','Finance','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (5194,'ACCP','Brown LLC','Technology','NYSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (1088,'JRYI','Gentry-Mitchell','Finance','LSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (9204,'IMQG','Holland-Lewis','Technology','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (3197,'CUHS','Johns-Wolf','Retail','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (6389,'VFUO','Hernandez Ltd','Energy','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (4834,'KMQP','Giles-Cochran','Technology','NYSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (3081,'MDZN','Lawrence-Kaufman','Retail','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (4396,'CFIJ','Carter-Horn','Healthcare','LSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (9197,'OITM','Cook, Morgan and Navarro','Retail','LSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (4930,'YCCT','Hess PLC','Healthcare','NYSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (9804,'CQMS','Johnson Ltd','Energy','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (8540,'AOFU','Reynolds, Rosales and Myers','Finance','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (8009,'IRVY','Taylor, Bray and Perry','Healthcare','LSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (4146,'CNUZ','Gray, Benjamin and Kemp','Energy','NYSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (8621,'ARCO','Weaver, Mccoy and Shaffer','Healthcare','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (9756,'EWCC','Chase, Miller and Abbott','Technology','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (1467,'GETD','Valencia-Garza','Energy','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (3900,'VMTZ','Thompson-Edwards','Healthcare','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (9682,'NZFM','Martin and Sons','Technology','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (6546,'BDOA','Sutton-Hodge','Healthcare','NASDAQ');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (8969,'QFMZ','Olson-Patel','Healthcare','HKEX');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (7669,'ZUIC','Serrano-Ayers','Energy','NYSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (4578,'UJDA','Mcintyre-Chambers','Technology','LSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (4720,'ZJDM','Reed and Sons','Retail','NYSE');
INSERT INTO stocks (stock_id,ticker,company_name,sector,exchange) VALUES (3414,'JBIL','Moore, Parker and Foster','Retail','HKEX');
//=========== moneta-slim stock_holdings ==========
DROP TABLE IF EXISTS stock_holdings;
CREATE TABLE stock_holdings (
	 holding_id INT NOT NULL
	,portfolio_id INT NOT NULL
	,stock_id INT NOT NULL
	,shares INT NOT NULL
	,average_price DECIMAL(15,4) NOT NULL

);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (105844.0,6773070.0,1467.0,330.0,101.42132063975184);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (839890.0,8336188.0,3081.0,169.0,891.0601715883546);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (109288.0,1654337.0,9682.0,534.0,122.0468961002662);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (204680.0,8282783.0,5687.0,381.0,457.6136441144378);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (577854.0,1305917.0,1467.0,907.0,808.6537860668924);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (513874.0,5699914.0,4930.0,248.0,603.5699260966421);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (500669.0,4993408.0,6546.0,778.0,378.294776618738);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (397727.0,6428281.0,4720.0,934.0,488.4171354235614);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (358617.0,9764250.0,4930.0,596.0,414.6062192870801);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (893761.0,3610649.0,5194.0,18.0,2.4132361071528274);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (824953.0,6661060.0,2241.0,553.0,875.2521662393913);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (271990.0,5116052.0,4720.0,728.0,791.8488651277693);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (159709.0,8514508.0,1088.0,392.0,370.4352951261187);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (435129.0,4891244.0,9197.0,390.0,360.4258214893136);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (202712.0,8336188.0,4578.0,140.0,358.90105221177015);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (653682.0,1305917.0,4146.0,280.0,746.372789423916);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (577887.0,3726354.0,3900.0,564.0,745.2717633668667);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (338376.0,2197547.0,6376.0,36.0,430.4427981279613);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (520697.0,8920335.0,3900.0,34.0,362.06345064229004);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (698668.0,8698258.0,3197.0,514.0,754.8348657018747);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (875710.0,1524334.0,6632.0,243.0,507.4245747824743);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (195621.0,8920335.0,8009.0,258.0,127.27816215105403);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (884550.0,7178324.0,6632.0,911.0,669.1282614117779);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (674043.0,6428281.0,8621.0,987.0,404.38622717846283);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (724532.0,4382910.0,3081.0,28.0,633.8140570033651);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (560661.0,8197944.0,3081.0,110.0,324.0855974496208);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (813399.0,7588168.0,3900.0,142.0,200.57449339281808);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (981148.0,5859184.0,5194.0,214.0,405.2971112053368);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (124508.0,4891244.0,3414.0,820.0,581.3737504778773);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (831770.0,6415840.0,6546.0,912.0,4.844003461999757);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (303369.0,4382910.0,6546.0,61.0,930.0140721023618);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (445333.0,8405694.0,9804.0,573.0,817.7447822010392);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (947942.0,8297423.0,5194.0,177.0,925.4506179430898);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (729536.0,1305917.0,1088.0,659.0,388.61046084402494);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (303565.0,4382910.0,6376.0,945.0,271.0397377060559);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (506082.0,4795605.0,1088.0,881.0,774.4007986645488);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (527822.0,2054001.0,9197.0,396.0,33.02673531732259);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (881025.0,1524334.0,3414.0,774.0,892.8785995429226);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (599293.0,1524334.0,6632.0,291.0,855.2471769304428);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (100722.0,5495400.0,5687.0,90.0,98.23752526196417);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (872166.0,2054001.0,4834.0,939.0,616.1756181084977);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (804967.0,7178324.0,1088.0,844.0,882.023732372946);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (587886.0,8405694.0,6389.0,162.0,336.65506402097753);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (243683.0,4891244.0,3081.0,314.0,837.9756431613692);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (817192.0,4320782.0,3197.0,250.0,180.28501149233634);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (963882.0,8336188.0,4930.0,285.0,476.33948104095913);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (604441.0,9764250.0,7669.0,693.0,911.8636119595013);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (546549.0,3212496.0,4930.0,676.0,379.55975279404083);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (340080.0,4891244.0,5687.0,267.0,620.3220288263595);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (113126.0,8920335.0,6546.0,744.0,538.6533681845345);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (994047.0,9064053.0,9197.0,852.0,862.2651548808803);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (783995.0,9660447.0,4720.0,795.0,984.1470945172301);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (265472.0,7178324.0,6546.0,813.0,654.0191497837502);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (796906.0,8810608.0,9804.0,636.0,790.2843796955814);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (735205.0,2197547.0,3081.0,267.0,598.4394196784292);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (674755.0,8920335.0,5194.0,236.0,120.18026662125448);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (472654.0,3321525.0,9682.0,20.0,2.759533892278787);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (873843.0,1654337.0,5687.0,215.0,16.3323192338799);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (365701.0,4981394.0,3414.0,875.0,65.51835136926121);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (483776.0,4993408.0,7669.0,227.0,762.3482918542827);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (893481.0,9141697.0,4146.0,200.0,948.892822870531);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (345600.0,4382910.0,5687.0,186.0,829.855976417324);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (972490.0,9064053.0,1176.0,41.0,741.9718690098734);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (189120.0,8055697.0,3081.0,893.0,369.3131369459297);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (348919.0,4795605.0,6376.0,587.0,983.9892236132989);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (179840.0,4382910.0,4930.0,656.0,758.8457865055713);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (755995.0,7938584.0,8969.0,639.0,571.8373899868219);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (341427.0,3610649.0,4396.0,61.0,226.4931903153815);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (728398.0,8810608.0,6632.0,872.0,624.179861505855);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (823224.0,2054001.0,3197.0,454.0,805.3978493127984);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (547652.0,5585237.0,9682.0,863.0,729.7601311835261);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (335260.0,1619128.0,9682.0,513.0,657.4427925449138);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (507927.0,3023971.0,8621.0,204.0,643.3084656096123);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (412033.0,5699914.0,5687.0,265.0,950.1120957892302);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (414658.0,3321525.0,8009.0,82.0,659.6636687377351);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (793098.0,5695353.0,3081.0,159.0,514.5996887567205);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (232639.0,6773070.0,3414.0,553.0,820.7005401043886);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (983551.0,6428281.0,8621.0,418.0,568.3348185006184);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (150818.0,6661060.0,5194.0,973.0,187.5587641471502);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (466439.0,3023971.0,6632.0,905.0,276.92393228018994);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (572648.0,8514508.0,3081.0,882.0,850.9596793105848);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (771844.0,9851590.0,4396.0,302.0,422.7577942995507);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (911767.0,5553348.0,6546.0,389.0,996.0569912492626);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (677969.0,1619128.0,3414.0,964.0,523.349936039316);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (639899.0,3321525.0,6376.0,791.0,841.8904459579675);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (982242.0,5859184.0,4396.0,691.0,541.1275842502133);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (270205.0,8282783.0,6376.0,545.0,404.11209827363695);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (385431.0,8705449.0,9682.0,153.0,951.9403804203322);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (275765.0,6076091.0,8621.0,887.0,935.2381090360524);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (409834.0,5585237.0,4930.0,861.0,97.67068520863764);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (267468.0,1524334.0,1176.0,979.0,532.446851028799);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (267232.0,9660447.0,5687.0,82.0,167.24632317494502);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (307087.0,1026459.0,6546.0,743.0,225.15508501476455);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (943132.0,6448244.0,4720.0,876.0,778.8128117182795);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (828166.0,6448244.0,1467.0,296.0,438.18279883408695);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (124155.0,7178324.0,8009.0,871.0,538.1411869467998);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (415245.0,1026459.0,9682.0,367.0,469.1563572728871);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (717217.0,6428281.0,6546.0,189.0,492.83533347112575);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (883693.0,1619128.0,8969.0,572.0,777.0980483866222);
INSERT INTO stock_holdings (holding_id,portfolio_id,stock_id,shares,average_price) VALUES (888573.0,3321525.0,8621.0,713.0,934.1090758827451);
//=========== moneta-slim trade_orders ==========
DROP TABLE IF EXISTS trade_orders;
CREATE TABLE trade_orders (
	 order_id INT NOT NULL
	,portfolio_id INT NOT NULL
	,stock_id INT NOT NULL
	,order_type VARCHAR NOT NULL
	,order_date VARCHAR NOT NULL
	,shares INT NOT NULL
	,limit_price DECIMAL(15,4) NOT NULL
	,status VARCHAR NOT NULL

);
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5808246,9660447,3900,'buy','2025-05-18',996,710.6954176229896,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4151102,5699914,1088,'buy','2025-02-21',379,675.421467982542,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5971000,5585237,4396,'sell','2025-04-20',568,670.1270583879839,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1135470,5116052,3197,'sell','2025-03-03',133,313.3589928549648,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4142877,8336188,3414,'buy','2025-01-30',348,422.0224092799579,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5038996,9064053,5687,'buy','2025-05-05',39,213.0098448470573,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9741788,1619128,1467,'sell','2025-02-07',756,13.624181333006913,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7166837,6428281,4396,'sell','2025-06-06',494,936.6303058747758,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4158949,7588168,4396,'buy','2025-02-24',578,129.11277773024267,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3604885,2054001,6389,'buy','2025-03-24',902,151.48252946066586,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (2085316,8297423,3081,'sell','2025-05-08',478,814.0744827924966,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6838866,5495400,5687,'buy','2025-02-12',862,893.2676397723758,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1969455,7178324,4578,'buy','2025-03-09',611,288.77825146536406,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1248518,8405694,4578,'sell','2025-05-21',738,616.9017453317479,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9542142,2197547,8009,'sell','2025-01-31',825,769.5867886117654,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1125955,1524334,5687,'buy','2025-01-05',704,122.97609814598631,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1134569,8514508,3081,'buy','2025-02-07',810,155.35361287351412,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (2985208,4993408,9804,'sell','2025-03-17',653,712.5180078001312,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (2372009,5495400,8009,'sell','2025-05-07',438,9.06457147112616,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9628567,1026459,1176,'sell','2025-04-21',276,15.041972592261033,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6073689,7153437,9756,'buy','2025-03-05',159,631.2972541510925,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6971315,3212496,6376,'sell','2025-04-06',365,346.21612193703066,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5747201,7178324,8969,'sell','2025-02-21',787,379.0120373460615,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3949813,2197547,4834,'sell','2025-03-29',653,559.4178120702732,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6954992,6428281,4396,'buy','2025-02-11',98,861.8418101591989,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8466799,4891244,8540,'sell','2025-01-23',845,170.99379048684338,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8218422,8698258,9197,'sell','2025-02-06',341,927.1643588901062,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7538441,5553348,6376,'buy','2025-04-27',81,608.3237557025737,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6969251,8514508,4396,'sell','2025-04-07',820,388.29576740701253,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4004698,1075287,6376,'buy','2025-06-18',688,699.2748415379845,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4333970,4320782,9682,'buy','2025-02-03',391,715.5776317028914,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6729251,5553348,9756,'sell','2025-01-19',118,595.6049293203479,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9463235,7842356,4146,'sell','2025-03-07',813,792.0925266856364,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9167811,6428281,6546,'buy','2025-05-22',565,679.3455619714322,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9273229,3212496,4146,'sell','2025-04-18',719,782.0860833205484,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5500309,5531375,3197,'sell','2025-01-19',214,758.0986615673819,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7101284,4382910,6389,'buy','2025-04-23',684,649.5003560680536,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9288427,8698258,6376,'sell','2025-03-08',191,121.12352629396695,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4226877,8698258,1088,'sell','2025-04-25',667,329.77087464789446,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (2870539,7178324,9197,'buy','2025-03-20',705,917.1230938460815,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1230992,1026459,6546,'buy','2025-05-31',889,144.25104613725549,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7269563,6415840,5194,'buy','2025-06-12',631,410.5924690487801,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8864429,2197547,8969,'sell','2025-02-08',743,538.0711710491537,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8579480,6661060,3414,'buy','2025-05-26',471,418.4047929690258,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3272984,8055697,6389,'sell','2025-01-04',811,884.3462293772479,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6012062,2197547,3900,'buy','2025-01-12',121,851.3783226289879,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6936834,4795605,6376,'sell','2025-03-04',23,905.6158924490795,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9926258,1619128,4578,'buy','2025-04-02',550,878.3019213476683,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1861361,6076091,4720,'buy','2025-02-19',390,327.14258477666436,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6171046,4320782,3900,'sell','2025-02-04',115,390.2464668328052,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (2795326,3726354,5687,'sell','2025-05-30',70,936.4324803288599,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5859578,6428281,3081,'sell','2025-03-07',234,179.84421216591429,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9680380,3212496,8009,'sell','2025-04-21',561,406.3148518104619,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4430069,3726354,9197,'sell','2025-05-02',427,553.2128539034214,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8610605,1026459,6376,'sell','2025-01-29',231,205.48187959764553,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7201972,6033535,3081,'buy','2025-05-27',793,299.4888830700503,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6026600,7858893,8540,'sell','2025-03-31',120,179.9307282623902,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3360208,8197944,9756,'buy','2025-01-08',627,168.2966210913578,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4613448,7588168,7669,'buy','2025-02-16',280,117.57129045112924,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (2891054,4993408,9804,'sell','2025-03-30',208,779.0921055490785,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6064190,6076091,1176,'sell','2025-05-23',476,196.35567152012524,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4244678,5495400,4396,'buy','2025-02-16',980,187.36553153363667,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4982939,6076091,1088,'sell','2025-02-25',435,41.66026486393149,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8019709,8920335,3197,'sell','2025-06-12',503,78.60176065872182,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1510409,8810608,6632,'buy','2025-01-05',676,85.22694573843958,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7245342,6428281,6632,'buy','2025-02-26',786,970.8451883961615,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1477017,5585237,6546,'sell','2025-02-20',598,869.5627756895525,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8166239,1654337,5194,'buy','2025-02-15',644,997.7816598846225,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3189498,1654337,8009,'buy','2025-03-19',185,87.61476457381967,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1381424,1305917,4396,'buy','2025-06-01',115,135.95984812422668,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5841421,7588168,8621,'buy','2025-04-05',816,219.07828783129347,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1330708,5699914,9197,'buy','2025-03-25',482,175.7629790610985,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3599658,9660447,9204,'sell','2025-06-13',706,379.8619354886893,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5174591,8055697,4720,'buy','2025-06-21',331,389.31929742161054,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1084900,8405694,4930,'sell','2025-02-17',226,154.00468754946695,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1662934,2321289,8540,'buy','2025-03-02',38,401.9877254883656,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5479686,3321525,9682,'sell','2025-06-18',846,637.7648193448033,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9487789,4981394,4834,'sell','2025-03-29',352,805.802305788992,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1327060,2197547,3414,'buy','2025-04-25',470,233.27288387530277,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (9390253,6773070,8009,'sell','2025-03-25',99,940.7607378135688,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6132336,5553348,3414,'buy','2025-01-15',757,361.01518213909225,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7149966,7153437,4578,'buy','2025-01-07',544,935.3584430805371,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4414477,5553348,3197,'sell','2025-01-27',655,117.78143616405812,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6031614,4993408,9682,'sell','2025-06-04',617,303.8279636954544,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3374572,4891244,3900,'sell','2025-02-17',306,149.41859462687214,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (4705970,6076091,9804,'sell','2025-04-28',394,217.92739464798927,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3777972,9660447,4720,'sell','2025-04-12',872,542.2927872250522,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8660126,4320782,9756,'sell','2025-06-11',87,39.94340046012379,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5717362,6033535,9204,'sell','2025-02-25',599,1.529293597982484,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (1052337,4320782,1176,'sell','2025-02-09',123,286.9866578618638,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8524165,5531375,5687,'buy','2025-05-17',304,403.98188064748297,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5863370,7588168,1088,'sell','2025-05-12',23,425.58087519818264,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3159722,4795605,8540,'sell','2025-03-17',37,866.7582428330288,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (5831605,3321525,6376,'sell','2025-03-27',367,462.8231543344292,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (3351113,8705449,7669,'sell','2025-01-06',860,543.9362636222368,'pending');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6035482,4382910,4930,'buy','2025-01-10',380,992.8149420754432,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (7503422,8405694,8969,'buy','2025-02-06',459,340.51231307350196,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (6887710,4320782,4578,'sell','2025-06-02',849,882.7947229516269,'executed');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (8388383,6415840,9197,'buy','2025-05-19',405,501.0018864395198,'cancelled');
INSERT INTO trade_orders (order_id,portfolio_id,stock_id,order_type,order_date,shares,limit_price,status) VALUES (2080057,7153437,8009,'buy','2025-04-24',825,86.02719916685386,'executed');