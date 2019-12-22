package io.qpointz.flow.excel

import io.qpointz.flow.data.AttributeValue

class RecordReaderSettings {
  var recordTags:Set[String] = Set()
  var noneValue: AttributeValue  = AttributeValue.Null
  var blankValue: AttributeValue = AttributeValue.Empty
  var errorValue: AttributeValue = AttributeValue.Error
  var columns:SheetColumnCollection = _
}
