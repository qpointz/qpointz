import io.qpointz.flow.Record
import org.apache.calcite.sql.SqlNode

import scala.util.{Success, Try}

trait AggCtx  {}
trait ArgList {
  def apply[T](idx:Int):T
}
type QlFunctionResult[T] = Try[T]
type QlFunctionT = ArgList => QlFunctionResult[_]
type QlRecordFunctionT = (Record, ArgList) => QlFunctionResult[_]
type QlAggregationFunctionT = (Record, AggCtx, ArgList) => QlFunctionResult[_]

sealed trait QlFunctionLike extends AnyRef {}

trait QlFunction extends QlFunctionLike {
  def apply(args: ArgList):QlFunctionResult[_]
}

trait QlRecordFunction extends QlFunctionLike {
  def apply(rec:Record, args: ArgList):QlFunctionResult[_]
}

trait QlAggregationFunction extends QlFunctionLike {
  def apply(rec:Record, aggCtx: AggCtx, args: ArgList):QlFunctionResult[_]
}


trait QlFunction2[-T1, -T2, R] extends QlFunction {self=>
  def apply(t1:T1, t2:T2):R
  def apply(args:ArgList):QlFunctionResult[_] = {
    Success(self(args(0), args(1)))
  }
}

val a2:QlFunction2[Int, Int,String] = (a,b)=>"sdklskdlskdl"

a2()




