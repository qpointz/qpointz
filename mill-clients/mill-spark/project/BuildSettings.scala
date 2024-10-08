/*
 * Copyright  2019 qpointz.io
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
 */

import sbt._
import sbt.Keys.{testOptions, _}
import sbt.{TestFrameworks, Tests}

object BuildSettings {
  lazy val scalaLangVersion = "2.13.14"
  lazy val version = "0.0.4-M1"

  lazy val testSettings = Seq(
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oI", "-h", "target/test-reports-out")
  )

}