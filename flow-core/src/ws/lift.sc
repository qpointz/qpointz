import java.net.InetAddress

val pf: PartialFunction[Int, Int] = {case i:Int if i>0 => i * 2}

pf.lift(-2)

InetAddress.getLocalHost.getHostName

val a = Seq[String]("a","b")
val b = Seq[String]("c","d")

val c:Seq[String] = a ++ b