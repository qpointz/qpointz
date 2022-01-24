import io.qpointz.flow.{AttributeKey, AttributeValue, Attributes, EntryDefinition, Metadata, Record}

import scala.Console.println
import io.qpointz.flow.MetadataMethods._

def projectFunc(proj:Seq[(AttributeKey, Record=>AttributeValue)]):Record=>Record = {
  def attributeCombine(key:AttributeKey, fn:Record=>AttributeValue)(in:(Record, Attributes)):(Record,Attributes) = {
    (
      in._1,
      in._2 + (key -> fn(in._1))
    )
  }
  val cf = proj
    .map(x=> attributeCombine(x._1, x._2) _)
    .reduce((l,r)=>l.andThen(r))

  r:Record=> {
    val nr = cf(r, Map())
    Record(nr._2, r.meta)
  }
}

val mapf = projectFunc(Seq[(AttributeKey, Record=>AttributeValue)](
  ("a1" , r => r.getOrElse("a","a missing")),
  ("b"  , r => r.getOrElse("b","b missing")),
  ("c1" , r => r.getOrElse("c","c missing")),
  ("g:foo" , r => r.meta.getOr(EntryDefinition("g","foo"), "missing" ))
))

val inr = Record(
    Map("a" -> "a val","b" -> "b val"),
    Metadata(Seq(("g","foo","bar"))))

val r = mapf(inr)

println(r)