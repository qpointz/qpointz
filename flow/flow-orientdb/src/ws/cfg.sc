import com.typesafe.config
import com.typesafe.config.{Config, ConfigFactory}

import java.util.UUID

val c = ConfigFactory.load()

println(UUID.randomUUID().toString().replace("-",""))