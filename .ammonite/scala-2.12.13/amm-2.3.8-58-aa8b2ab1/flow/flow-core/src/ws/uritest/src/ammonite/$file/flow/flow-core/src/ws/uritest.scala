
package ammonite
package $file.flow.`flow-core`.src.ws
import _root_.ammonite.interp.api.InterpBridge.{
  value => interp
}
import _root_.ammonite.interp.api.InterpBridge.value.{
  exit
}
import _root_.ammonite.interp.api.IvyConstructor.{
  ArtifactIdExt,
  GroupIdExt
}
import _root_.ammonite.compiler.CompilerExtensions.{
  CompilerInterpAPIExtensions,
  CompilerReplAPIExtensions
}
import _root_.ammonite.runtime.tools.{
  browse,
  grep,
  time,
  tail
}
import _root_.ammonite.compiler.tools.{
  desugar,
  source
}
import _root_.mainargs.{
  arg,
  main
}
import _root_.ammonite.repl.tools.Util.{
  PathRead
}


object uritest{
/*<script>*/val a = 12 // URI.create("/hhh/jjj/aaaa.txt")

/*<amm>*/val res_1 = /*</amm>*/print(a)/*</script>*/ /*<generated>*/
def $main() = { scala.Iterator[String]() }
  override def toString = "uritest"
  /*</generated>*/
}
