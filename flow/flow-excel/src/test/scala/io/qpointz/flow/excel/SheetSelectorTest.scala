package io.qpointz.flow.excel

import org.apache.poi.ss.usermodel.Sheet
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class SheetSelectorTest extends FlatSpec with Matchers with MockFactory {

  import SheetSelector._
  import WorkbookMethods._

  def asTuple(s:Set[Sheet]):Set[(String, Int)] = s.map(x=>(x.name, x.index)).toSet

  behavior of "matchBy"

  val sheets = open("./flow/flow-excel/src/test/resources/flow-excel-test/SheetSelectorTest.xlsx")
    .sheets()

  it should "match by name" in {
    val selector =List(
      SheetByName("bbb")
    )
    asTuple( matchBy(sheets, selector)) shouldBe Set(("bbb",1))
  }

  it should "match by index" in {
    val selector =List(
      SheetByIdx(2)
    )
    asTuple( matchBy(sheets, selector)) shouldBe Set(("ccc",2))
  }

  it should "match by pattern" in {
    val selector = List(
      SheetByNamePattern("""a.+\d$""")
    )

    val sheets = open("./flow/flow-excel/src/test/resources/flow-excel-test/TestRangeSheets.xlsx").sheets()

    asTuple(matchBy(sheets, selector)) shouldBe Set(
      ("aa1",0),
      ("ab2", 1)
    )
  }

  it should "throw on unknown criteria" in {
    object crt extends SheetCriteria {}
    val selector = List(crt)
    an[IllegalArgumentException] shouldBe thrownBy (matchBy(sheets, selector))
  }

  it should "match all" in {
    val selector =List(
      SheetByName("aaa"),
      SheetByIdx(2)
    )
    asTuple ( matchBy(sheets, selector)) shouldBe Set(
      ("aaa",0),
      ("ccc",2)
    )
  }

  it should "match any" in {
    asTuple ( matchBy(sheets, List(AnySheet))) shouldBe asTuple(sheets.toSet)
  }

  it should "matches none for empty criteria" in {
    asTuple( matchBy(sheets, List.empty)) shouldBe Set.empty
  }

  behavior of "select"

  it should "return unique sheets" in {
    asTuple (select (SheetSelector.include(List(
      SheetByName("aaa"),
      SheetByIdx(0)
    )), sheets)) shouldBe Set(("aaa",0))
  }

  it should "not return excluded " in {
    asTuple (select(SheetSelector(
      List(AnySheet),
      List(SheetByName("aaa"))
    ), sheets)) shouldBe Set(("bbb",1), ("ccc",2))
  }

  it should "return no sheets if no criteria provided" in {
    asTuple (select(SheetSelector(
      List.empty, List.empty
    ), sheets)) shouldBe Set()
  }

  behavior of "exclude"

  it should "return any sheet except excluded" in {
    asTuple (select(exclude(List(SheetByIdx(1))), sheets)) shouldBe Set(("aaa",0), ("ccc",2))
  }


}
