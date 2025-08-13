import java.io.File
import java.nio.file.Files

import sbt._
import sbt.Keys._
import sbt.{Def, _}
import sbt.complete.DefaultParsers._
import scala.sys.process._


/*
 * Copyright 2020 qpointz.io
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

object YarnTasks {
  lazy val yarn = inputKey[Unit]("Executes yarn scripts")
  lazy val buildFrontend = taskKey[Unit]("Execute frontend scripts")
}

object YarnSupport {

  lazy val npmInstallCmd: Seq[String] = Seq("npm install")
  lazy val npmTestCmd: Seq[String] = Seq("npm run test")
  lazy val npmLintCmd: Seq[String] = Seq("npm run lint")
  lazy val npmBuildCmd: Seq[String] = Seq("npm run build")

}

object Yarn extends AutoPlugin {
  import YarnTasks._

  def execProc(cmd: Seq[String], workingDir: File, logger:Logger):Unit = {
    val shell: Seq[String] = if (sys.props("os.name").contains("Windows")) Seq("cmd", "/c") else Seq("bash", "-c")
    val shellcmd: Seq[String] = shell ++ cmd
    logger.info(s"Executing ${shellcmd} in ${workingDir}")

    Process(shellcmd, Some(workingDir)).run(logger, connectInput = true)

    /*if (!workingDir.exists()) {
      logger.info(s"UI directory '${workingDir}' doesn't exists.")
    } else {
      logger.debug(s"Exec process ${cmd}")


    }*/
  }

  override lazy val projectSettings = {
    yarn := execProc(
      "yarn build" +: spaceDelimited("<arg>").parsed ,
      new File(baseDirectory.value.getAbsolutePath + "/src/ui"),
      streams.value.log
    )
  }
}

