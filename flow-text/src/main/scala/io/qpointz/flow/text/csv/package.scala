/*
 * Copyright 2021 qpointz.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.qpointz.flow.text

package object csv {
  import com.univocity.parsers.csv.{UnescapedQuoteHandling => univocityUnescapedQuoteHandling}

  object UnescapedQuoteHandling extends Enumeration {
    type Type = Value

    private[text] case class Val(private[text] orig: univocityUnescapedQuoteHandling) extends super.Val {

    }

    private[text] def from(tv: univocityUnescapedQuoteHandling): UnescapedQuoteHandling.Type = tv match {
      case univocityUnescapedQuoteHandling.STOP_AT_CLOSING_QUOTE => StopAtClosingQuote
      case univocityUnescapedQuoteHandling.SKIP_VALUE => SkipValue
      case univocityUnescapedQuoteHandling.BACK_TO_DELIMITER => BackToDelimiter
      case univocityUnescapedQuoteHandling.RAISE_ERROR => RaiseError
      case univocityUnescapedQuoteHandling.STOP_AT_DELIMITER => StopAtDelimiter
      case _ => RaiseError
    }

    val StopAtClosingQuote: Val = Val(univocityUnescapedQuoteHandling.STOP_AT_CLOSING_QUOTE)
    val SkipValue: Val = Val(univocityUnescapedQuoteHandling.SKIP_VALUE)
    val BackToDelimiter: Val = Val(univocityUnescapedQuoteHandling.BACK_TO_DELIMITER)
    val RaiseError: Val = Val(univocityUnescapedQuoteHandling.RAISE_ERROR)
    val StopAtDelimiter: Val = Val(univocityUnescapedQuoteHandling.STOP_AT_DELIMITER)
  }
}
