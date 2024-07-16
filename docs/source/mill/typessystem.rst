Types system
======================

Mills introduces type system to to represent 

Types Mapping
-------------

.. csv-table:: 
   :header: "JDBC", "Logical", "Physical", "Protobuf"
   :widths: 50,25,25,25
   :delim: ;

   "BOOLEAN,BIT";Bool;Bool;bool
   "SMALLINT";SmallInt;I32;int32
   "TINYINT,INTEGER";Int;I32;int32
   "BIGINT";BigInt;I64;int64  
   "DOUBLE(p,s), REAL(p,s), NUMERIC(p,s), DECIMAL(p,s)";Double;FP64;double
   "(N)VARCHAR,(N)CHAR,LONGVARCHAR,(N)CLOB";String;String;string



case Types.FLOAT        -> DatabaseType.fp32(nullable, prec, scale);

case Types.DOUBLE       -> DatabaseType.fp64(nullable, prec, scale);
case Types.REAL         -> DatabaseType.fp64(nullable, prec, scale);
case Types.NUMERIC      -> DatabaseType.fp64(nullable, prec, scale);
case Types.DECIMAL      -> DatabaseType.fp64(nullable, prec, scale);

case Types.BINARY       -> DatabaseType.binary(nullable, prec);
case Types.VARBINARY    -> DatabaseType.binary(nullable, prec);
case Types.LONGVARBINARY-> DatabaseType.binary(nullable, prec);
case Types.BLOB         -> DatabaseType.binary(nullable, prec);

case Types.DATE         -> DatabaseType.date(nullable);
case Types.TIME         -> DatabaseType.time(nullable);
case Types.TIMESTAMP    -> DatabaseType.timetz(nullable);
case Types.TIME_WITH_TIMEZONE       -> DatabaseType.timetz(nullable);
case Types.TIMESTAMP_WITH_TIMEZONE  -> DatabaseType.timetz(nullable);
