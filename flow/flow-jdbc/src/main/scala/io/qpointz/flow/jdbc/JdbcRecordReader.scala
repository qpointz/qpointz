/*
 * Copyright 2020 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.qpointz.flow.jdbc
import java.sql.DriverManager
import java.util.Properties
import io.qpointz.flow
import io.qpointz.flow.utils.IteratorExtensions
import io.qpointz.flow.{MetadataMethods, OperationContext, Record, RecordReader}

trait JdbcConnectionUrlLike {
  val url:String
}

case class JdbcConnectionUrl(url:String) extends JdbcConnectionUrlLike
case class JdbcConnectionUrlWithUsername(url:String, username: String, password:String) extends JdbcConnectionUrlLike
case class JdbcConnectionUrlWithProperties(url:String, props:Map[Any, Any]) extends JdbcConnectionUrlLike


case class JdbcRecordReaderSettings(driver:String,
                                    connectionUrl: JdbcConnectionUrlLike,
                                    query:String,
                                    queryParams: Seq[Any]) {}

class JdbcRecordReader(jdbcSettings: JdbcRecordReaderSettings)(implicit val ctx:OperationContext)
  extends RecordReader  {

  lazy val rs = {
    Class.forName(jdbcSettings.driver)

    val conn = jdbcSettings.connectionUrl match {
      case c:JdbcConnectionUrl => DriverManager.getConnection(c.url)
      case cu:JdbcConnectionUrlWithUsername => DriverManager.getConnection(cu.url, cu.username, cu.password)
      case cp:JdbcConnectionUrlWithProperties => {
        val prop = cp.props.foldLeft(new Properties())( (p,x)=> {p.put(x._1, x._2);p})
        DriverManager.getConnection(cp.url, prop)
      }
    }

    val stmt = conn.prepareStatement(jdbcSettings.query)

    jdbcSettings
      .queryParams
      .zipWithIndex
      .foreach(x=> stmt.setObject(x._2 + 1, x._1))

    stmt.executeQuery()
  }

  private lazy val meta = rs.getMetaData

  case class ColumnInfo(idx:Int, name:String)

  private val columns = (1 to meta.getColumnCount).map(i=>{
    ColumnInfo(i, meta.getColumnName(i))
  })

  private def asRecord:Record = {
    val vals = columns
      .map(c=> c.name -> rs.getObject(c.idx))
      .toMap
    val meta = MetadataMethods.empty
    flow.Record(vals,meta)
  }

  override lazy val iterator: Iterator[Record] = {
    ctx.log.info("Begin iterating")
    IteratorExtensions.unfold(rs.next()){hasNext=>
      if (hasNext) {
        Some((asRecord, rs.next()))
      } else {
        None
      }
    }
  }

}
