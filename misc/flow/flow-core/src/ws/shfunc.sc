import shapeless._
import shapeless.syntax.std.traversable._

trait QFunc2[T1, T2, R] {
  val fn:(T1,T2)=>R = apply
  def apply(t1:T1, t2:T2):R
  def mapArg(arg:List[Any]):(T1,T2) = {
    arg.toHList[T1::T2::HNil].get.tupled
  }
  def apply(arg:List[Any]):R = {
    fn.tupled.apply(mapArg(arg))
  }
}

val af:QFunc2[Int,Int,String] = (a,b)=>s"Hello $a, $b times"
val f1:QFunc2[String,String,String] = {
  val fn = (x:String,y:String)=>"lllll"
}

val df = new QFunc2[String,String,String] {
  override def apply(t1:String , t2: String) = s"$t1....$t2"
  override def mapArg(arg: List[Any]): (String, String) = super.mapArg(arg.map(_.toString))
}

af(1,2)
af(List(2,3))