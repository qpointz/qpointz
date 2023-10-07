import shapeless._
import shapeless.syntax.std.traversable.traversableOps

def a[T1,T2](hlist:Seq[Any], fn: (T1,T2)=>Unit):Unit = {
  val a = hlist.toHList[T1::T2::HNil].get.tupled
  fn.tupled(a)
}


a(Seq(1.2,1), (x:Double,y:Double)=>println(s"it2 $x and $y"))