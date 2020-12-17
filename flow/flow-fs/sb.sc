import java.nio.file.{Files, Paths}

import scala.jdk.CollectionConverters._

Files.walk(Paths.get("./")).iterator().asScala.toList