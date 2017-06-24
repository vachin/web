package models

import play.api.libs.json.Json

/**
  * Created by jyothi on 25/6/17.
  */

case class TagWithCount(_id: String, name: String, count: Int)

object TagWithCount {
  implicit val tagWithCount = Json.format[TagWithCount]
}
