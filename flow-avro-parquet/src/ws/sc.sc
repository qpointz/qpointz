import org.apache.avro.{Schema, SchemaBuilder}
import org.json4s.Extraction
import org.json4s.jackson.Serialization._

val s = SchemaBuilder
  .record("default")
  .fields()
  .nullableString("A","nA")
  .nullableInt("B",0)
  .endRecord()

val json = s.toString(true)
println(json)

val p = new Schema.Parser()
val s1 = p.parse(json)

println(s1.equals(s))