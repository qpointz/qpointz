package io.qpointz.surface.api

import play.api.libs.json.{Format, Json}

case class Organization(key:String, name:String="kkkkk")

object Organization {
  implicit val format: Format[Organization] = Json.format[Organization]
}