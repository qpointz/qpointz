import org.apache.calcite.schema.{Schema, SchemaPlus}

import java.util
class SchemaFactory extends org.apache.calcite.schema.SchemaFactory {
  override def create(parentSchema: SchemaPlus, name: String, operand: util.Map[String, AnyRef]): Schema = ???
}
