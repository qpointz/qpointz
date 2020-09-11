//noinspection ScalaStyle
case class g(x:String, y:String, z:String)

//noinspection ScalaStyle
object Ext {

  def unapply(arg: g): Option[(String,String,String)] = {
    Some(arg.x,arg.y,arg.z)
  }

  def unapply(arg: String): Option[(Option[String],Option[String],Option[String])] = {
    val a = arg.split(",")
    a.length match {
      case 1 => Some(Some(a(0)),None,None)
      case 2 => Some(Some(a(0)),Some(a(1)),None)
      case 3 => Some(Some(a(0)),Some(a(1)),Some(a(2)))
      case _ => None
    }
  }
}

val Ext(x,y,z) = "a,b,c"

val Ext(Some(x1),_,Some(z2)) = "jjjj,uuuuu,pppp"

val g(x5,y5,z5) = g("a5","b5","c5")