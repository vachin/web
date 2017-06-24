package models

import play.api.libs.json.Json

/**
  * Created by jyothi on 25/6/17.
  */
case class TagModel( _id: String, name: String, description: Option[String])

object TagModel {
  implicit val tagModel = Json.format[TagModel]
}

case class TagRequestModel(name: String, description: Option[String])

object TagRequestModel {
  implicit val tagRequestModel = Json.format[TagRequestModel]
}
