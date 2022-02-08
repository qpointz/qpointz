import spire._
import spire.math._
import spire.implicits._

import scala.util.Success

trait F2[T1,T2,R] {

  def mapArg(i:(Any, Any)):(Option[T1], Option[T2])
  def fnOp(x:T1, x2:T2):R

  def apply(a: Any, b: Any): R = {
    mapArg((a,b)) match {
      case (Some(x: T1), Some(y: T2)) => fnOp(x, y)
      case _ => throw new RuntimeException("bbb")
    }
  }
}

object F {
  def asNumber(an: Any): Option[Number] = an match {
    case nu: Number => Some(nu)
    case b: Byte => Some(Number(b))
    case s: Short => Some(Number(s))
    case an: Int => Some(Number(an))
    case lo: Long => Some(Number(lo))
    case fl: Float => Some(Number(fl))
    case d: Double => Some(Number(d))
    case _ => None
  }

  def apply[T1,T2,R](ma:((Any,Any))=>(Option[T1], Option[T2]))(fno:(T1, T2)=>R)
  = new F2[T1,T2,R] {
    override def mapArg(i: (Any, Any)) = ma(i)
    override def fnOp(x: T1, x2: T2) = fno(x,x2)
  }
}

val plus: F2[Number, Number, Number] = F { i=> (F.asNumber(i._1),F.asNumber(i._2))}{ (x, y)=> x+y}
/*def minus(a:Any, b:Any) = op((x,y)=>x-y)(a,b)
def times(a:Any, b:Any) = op((x,y)=>x*y)(a,b)
def div(a:Any, b:Any) = op((x,y)=>x/y)(a,b)
def gt(a:Any, b:Any) = op((x,y)=>x>y)(a,b)
def gte(a:Any, b:Any) = op((x,y)=>x>=y)(a,b),
def lt(a:Any, b:Any) = op((x,y)=>x<y)(a,b)
def lte(a:Any, b:Any) = op((x,y)=>x<=y)(a,b)
def eq(a:Any, b:Any) = op((x,y)=>x==y)(a,b)*/



val b = plus(1,3)