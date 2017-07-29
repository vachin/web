package models

import play.api.libs.json.Json

/**
  * Created by jyothi on 29/7/17.
  */
object MiscModel {

}

case class PaginationModel(limit: Int, version: Int, total: Int)

object PaginationModel {
  implicit val paginationModelFormat = Json.format[PaginationModel]
}

case class TextPaginatedModel(meta: PaginationModel, texts: List[TextModel])

object TextPaginatedModel {
  implicit val textPaginatedModelFormat = Json.format[TextPaginatedModel]

  def empty = TextPaginatedModel(PaginationModel(0, 0, 0), List.empty[TextModel])

}
