val s = org.apache.avro.SchemaBuilder.builder()
  .record("default")
  .fields()
  .optionalString("hallo")
  .nullableBoolean("is",true)
  .endRecord()
