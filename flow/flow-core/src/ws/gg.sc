import scala.util.matching.Regex

val r = """^(qp\:){0,1}(?<ns>[a-zA-Z][a-zA-Z0-9]*)(?<h>((?>\/)([a-zA-Z][a-zA-Z0-9]*))*)(?>\/)(?<g>[a-zA-Z][a-zA-Z0-9]*){1}(?>\/)(?<tn>[a-zA-Z][a-zA-Z0-9]*)((?>\#)(?<id>[a-zA-Z][a-zA-Z0-9]*))*$""".r

def matchNs(in:String):Unit= {
  def unwind(m:Regex.Match) = {
    println(s"""ns=${m.group("ns")}""")

    val hs = m.group("h") match {
      case x if x.length>0 => x.split(raw"\/").filter(_.nonEmpty).toSeq
      case _ => Seq()
    }

    hs.foreach(x=> println(s"h=${x}"))

    println(s"""g=${m.group("g")}""")

    println(s"""tn=${m.group("tn")}""")

    println(s"""id=${m.group("id")}""")
  }

  r.findFirstMatchIn(in) match {
     case Some(r) => unwind(r)
     case None => println("no match")
  }
}

matchNs("namesps/h1/h2/h3/typename")

