/*
 *
 *  Copyright 2022 qpointz.io
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

CREATE SCHEMA IF NOT EXISTS qpointz.dev;

DROP TABLE IF EXISTS qpointz.dev.nyc;

CREATE TABLE qpointz.dev.nyc (
                              "borough"  VARCHAR,
                              "neighborhood"  VARCHAR,
                              "Building_Class_Category"  VARCHAR,
                              "Tax_Class_At_Present"  VARCHAR,
                              "Block"  VARCHAR,
                              "Lot"  VARCHAR,
                              "Ease_Ment"  VARCHAR,
                              "Building_Class_At_Present"  VARCHAR,
                              "Address"  VARCHAR,
                              "Apartment_Number"  VARCHAR,
                              "Zip_Code"  VARCHAR,
                              "Residential_Units"  VARCHAR,
                              "Commercial_Units"  VARCHAR,
                              "Total_Units"  VARCHAR,
                              "Land_Square_Feet"  VARCHAR,
                              "Gross_Square_Feet"  VARCHAR,
                              "Year_Built"  VARCHAR,
                              "Tax_Class_At_Time_Of_Sale"  VARCHAR,
                              "Building_Class_At_Time_Of_Sale"  VARCHAR,
                              "Sale_Price"  VARCHAR,
                              "Sale_Date" VARCHAR
)
    WITH (
        format = 'PARQUET',
        external_location = 's3a://sampledata/nyc_pq/nyc_pq'
        );
