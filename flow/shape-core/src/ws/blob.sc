import org.apache.calcite.sql.parser.SqlParser
import org.apache.calcite.sql.util.SqlShuttle

val cfg = SqlParser.config().withCaseSensitive(true)
val parser = SqlParser.create("select * from DEPS",cfg)
