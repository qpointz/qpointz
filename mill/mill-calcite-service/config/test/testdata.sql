DROP TABLE IF EXISTS DATA_TYPES_SAMPLE;

-- Insert sample data into DATA_TYPES_SAMPLE table
CREATE TABLE DATA_TYPES_SAMPLE (
                                   ID INT AUTO_INCREMENT PRIMARY KEY,
                                   BIT_COLUMN BIT ,
                                   TINYINT_COLUMN TINYINT,
                                   SMALLINT_COLUMN SMALLINT,
                                    INTEGER_COLUMN INTEGER,
                                    BIGINT_COLUMN BIGINT,
                                    BOOLEAN_COLUMN BOOLEAN ,
                                    DECIMAL_COLUMN DECIMAL(10, 2),
                                    DOUBLE_COLUMN DOUBLE,
                                    REAL_COLUMN REAL,
                                    TIME_COLUMN TIME,
                                    DATE_COLUMN DATE,
                                    TIMESTAMP_COLUMN TIMESTAMP,
                                    CHAR_COLUMN CHAR(10),
                                    VARCHAR_COLUMN VARCHAR(255),
                                    CLOB_COLUMN CLOB,
                                    BLOB_COLUMN BLOB,
                                    UUID_COLUMN UUID,
                                    ARRAY_COLUMN INT ARRAY,
                                    GEOMETRY_COLUMN GEOMETRY
);

INSERT INTO DATA_TYPES_SAMPLE (
    BIT_COLUMN , TINYINT_COLUMN, SMALLINT_COLUMN, INTEGER_COLUMN, BIGINT_COLUMN,
    BOOLEAN_COLUMN, DECIMAL_COLUMN, DOUBLE_COLUMN, REAL_COLUMN, TIME_COLUMN,
    DATE_COLUMN, TIMESTAMP_COLUMN, CHAR_COLUMN, VARCHAR_COLUMN, CLOB_COLUMN,
    BLOB_COLUMN , UUID_COLUMN, ARRAY_COLUMN, GEOMETRY_COLUMN
) VALUES (
             0, -128, -32768, -2147483648, -9223372036854775808,
             FALSE, -12345.67, -12345.6789, -12345.6789, '23:45:56',
             '2024-07-27', '2024-07-27 23:45:56', 'another ch', 'another varchar', 'another clob',
             X'54657374' , '321e4567-e89b-12d3-a456-426614174001', ARRAY[4, 5, 6], 'POINT(2 2)'
         );



-- Insert another sample row
INSERT INTO DATA_TYPES_SAMPLE (
    BIT_COLUMN, TINYINT_COLUMN, SMALLINT_COLUMN, INTEGER_COLUMN, BIGINT_COLUMN,
    BOOLEAN_COLUMN, DECIMAL_COLUMN, DOUBLE_COLUMN, REAL_COLUMN, TIME_COLUMN,
    DATE_COLUMN, TIMESTAMP_COLUMN, CHAR_COLUMN, VARCHAR_COLUMN, CLOB_COLUMN,
    BLOB_COLUMN, UUID_COLUMN, ARRAY_COLUMN, GEOMETRY_COLUMN
) VALUES (
             1, 127, 32767, 2147483647, 9223372036854775807,
             TRUE, 12345.67, 12345.6789, 12345.6789, '12:34:56',
             '2023-07-27', '2023-07-27 12:34:56', 'char data', 'varchar data', 'clob data',
             X'426C6F62', '123e4567-e89b-12d3-a456-426614174000', ARRAY[1, 2, 3], 'POINT(1 1)'
         );
