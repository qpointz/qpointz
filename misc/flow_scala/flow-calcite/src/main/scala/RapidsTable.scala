import org.apache.calcite.rel.`type`.{RelDataType, RelDataTypeFactory}
import org.apache.calcite.schema.impl.AbstractTable

class RapidsTable extends AbstractTable {
  override def getRowType(typeFactory: RelDataTypeFactory): RelDataType = ???
}


