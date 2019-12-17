package io.qpointz.flow.excel

import org.apache.poi.ss.usermodel.Sheet

import scala.util.matching.Regex

import WorkbookMethods._

trait SheetCriteria {}
object AnySheet extends SheetCriteria {}

case class SheetByName(sheetName:String) extends SheetCriteria

case class SheetByIdx(sheetIdx:Int)  extends SheetCriteria

case class SheetByNamePattern(namePatter:String) extends SheetCriteria {
  lazy val nameRx: Regex = namePatter.r
}

case class SheetSelector(include:List[SheetCriteria], exclude:List[SheetCriteria])

object SheetSelector {

  def include(selector:List[SheetCriteria]):SheetSelector = {
    SheetSelector(selector, List.empty)
  }: SheetSelector

  def exclude(selector:List[SheetCriteria]):SheetSelector = {
    SheetSelector(List(AnySheet), selector)
  }

  def matchBy(sheets:Iterable[Sheet],criteria:List[SheetCriteria]):Set[Sheet] = {
    def sm(sc:SheetCriteria, sh:Sheet):Boolean = sc match {
      case AnySheet => true
      case x:SheetByIdx => x.sheetIdx == sh.index
      case xn:SheetByName => xn.sheetName.equalsIgnoreCase(sh.name)
      case xr:SheetByNamePattern => xr.nameRx.matches(sh.name)
      case _ => throw new IllegalArgumentException(s"Unknown criteria ${sc}")
    }

    def doMatch():Set[Sheet] = {
      val matched = for (
        sh <- sheets;
        sc <- criteria
      ) yield (sh, sm(sc, sh))

      matched
        .filter(_._2)
        .map(_._1)
        .toSet
    }

    doMatch()
  }

  def select(selector:SheetSelector, sheets:Seq[Sheet]):Set[Sheet] = {
    val include = matchBy(sheets, selector.include)
    val exclude = matchBy(include, selector.exclude)
    include -- exclude
  }

}