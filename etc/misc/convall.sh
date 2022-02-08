#!/usr/bin/env bash

rm -f ./nyc/*.avro

./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/03_Bronx.csv -Pout=./nyc/nyc-bronx-2003.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2017_Manhattan.csv -Pout=./nyc/nyc-manhattan-2017.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2008_Manhattan.csv -Pout=./nyc/nyc-manhattan-2008.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/04_Manhattan.csv -Pout=./nyc/nyc-manhattan-2004.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2013_Manhattan.csv -Pout=./nyc/nyc-manhattan-2013.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2016_Manhattan.csv -Pout=./nyc/nyc-manhattan-2016.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2014_Manhattan.csv -Pout=./nyc/nyc-manhattan-2014.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/03_Manhattan.csv -Pout=./nyc/nyc-manhattan-2003.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2007_Manhattan.csv -Pout=./nyc/nyc-manhattan-2007.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2012_Manhattan.csv -Pout=./nyc/nyc-manhattan-2012.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2010_Manhattan.csv -Pout=./nyc/nyc-manhattan-2010.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2011_Manhattan.csv -Pout=./nyc/nyc-manhattan-2011.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2018_Manhattan.csv -Pout=./nyc/nyc-manhattan-2018.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/06_Manhattan.csv -Pout=./nyc/nyc-manhattan-2006.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2019_Manhattan.csv -Pout=./nyc/nyc-manhattan-2019.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/05_Manhattan.csv -Pout=./nyc/nyc-manhattan-2005.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2009_Manhattan.csv -Pout=./nyc/nyc-manhattan-2009.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Manhattan/Manhattan/2015_Manhattan.csv -Pout=./nyc/nyc-manhattan-2015.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2012_Queens.csv -Pout=./nyc/nyc-queens-2012.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2013_Queens.csv -Pout=./nyc/nyc-queens-2013.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2018_Queens.csv -Pout=./nyc/nyc-queens-2018.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2014_Queens.csv -Pout=./nyc/nyc-queens-2014.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2017_Queens.csv -Pout=./nyc/nyc-queens-2017.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2010_Queens.csv -Pout=./nyc/nyc-queens-2010.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2015_Queens.csv -Pout=./nyc/nyc-queens-2015.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2008_Queens.csv -Pout=./nyc/nyc-queens-2008.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/03_Queens.csv -Pout=./nyc/nyc-queens-2003.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2011_Queens.csv -Pout=./nyc/nyc-queens-2011.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/04_Queens.csv -Pout=./nyc/nyc-queens-2004.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/05_Queens.csv -Pout=./nyc/nyc-queens-2005.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2007_Queens.csv -Pout=./nyc/nyc-queens-2007.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/06_Queens.csv -Pout=./nyc/nyc-queens-2006.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2009_Queens.csv -Pout=./nyc/nyc-queens-2009.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2019_Queens.csv -Pout=./nyc/nyc-queens-2019.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Queens/Queens/2016_Queens.csv -Pout=./nyc/nyc-queens-2016.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/06_Bronx.csv -Pout=./nyc/nyc-bronx-2006.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2014_Bronx.csv -Pout=./nyc/nyc-bronx-2014.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2011_Bronx.csv -Pout=./nyc/nyc-bronx-2011.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2016_Bronx.csv -Pout=./nyc/nyc-bronx-2016.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/04_Bronx.csv -Pout=./nyc/nyc-bronx-2004.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2017_Bronx.csv -Pout=./nyc/nyc-bronx-2017.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2008_Bronx.csv -Pout=./nyc/nyc-bronx-2008.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2012_Bronx.csv -Pout=./nyc/nyc-bronx-2012.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/03_Bronx.csv -Pout=./nyc/nyc-bronx-2003.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2007_Bronx.csv -Pout=./nyc/nyc-bronx-2007.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2019_Bronx.csv -Pout=./nyc/nyc-bronx-2019.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2013_Bronx.csv -Pout=./nyc/nyc-bronx-2013.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/05_Bronx.csv -Pout=./nyc/nyc-bronx-2005.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2009_Bronx.csv -Pout=./nyc/nyc-bronx-2009.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2010_Bronx.csv -Pout=./nyc/nyc-bronx-2010.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2015_Bronx.csv -Pout=./nyc/nyc-bronx-2015.avro convert.json
./flow/bin/flow receipt -Pin=./archive/Bronx/Bronx/2018_Bronx.csv -Pout=./nyc/nyc-bronx-2018.avro convert.json
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2008_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/05_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2019_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/04_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2018_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2010_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2013_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2009_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2011_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2015_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2007_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2014_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2012_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/06_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2016_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/2017_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/StatenIsland/StatenIsland/03_StatenIsland.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/06_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2011_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2013_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/05_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2007_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2016_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2014_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2019_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2017_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2015_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2010_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/03_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2009_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2018_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2008_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/2012_Brooklyn.csv
#./flow/bin/flow receipt -Pin=./archive/Brooklyn/Brooklyn/04_Brooklyn.csv
